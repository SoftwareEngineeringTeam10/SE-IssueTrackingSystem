package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.StringConverter;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.model.Project;
import org.issuetracker.model.Role;
import org.issuetracker.service.ProjectService;
import org.issuetracker.service.StatisticsService;
import ui.javafx.session.Session;
import ui.javafx.session.ViewLoader;

import java.util.Map;

public class StatsController {

    @FXML private ComboBox<Project> projectCombo;
    @FXML private Label userLabel;
    @FXML private Button manageButton;

    @FXML private Label totalLabel;
    @FXML private Label newLabel;
    @FXML private Label fixedResolvedLabel;
    @FXML private Label closedLabel;

    @FXML private BarChart<String, Number> dailyChart;
    @FXML private BarChart<String, Number> monthlyChart;
    @FXML private BarChart<String, Number> statusChart;

    @FXML private ListView<String> activityLog;
    @FXML private Label systemMessage;

    private final StatisticsService statisticsService = new StatisticsService();
    private final ProjectService projectService = new ProjectService();

    @FXML
    public void initialize() {
        // 초기화 — 사용자 / 프로젝트 / admin 권한 / 통계 데이터 로드
        if (Session.currentUser != null) {
            userLabel.setText("User: " + Session.currentUser.getId());
        }

        // 프로젝트 콤보 — 멀티프로젝트 연동
        projectCombo.setConverter(new StringConverter<Project>() {
            @Override public String toString(Project p) { return p == null ? "" : p.name; }
            @Override public Project fromString(String s) { return null; }
        });
        projectCombo.getItems().setAll(projectService.getAllProjects());
        for (Project p : projectCombo.getItems()) {
            if (p.id == Session.currentProjectId) {
                projectCombo.getSelectionModel().select(p);
                break;
            }
        }
        projectCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Session.currentProjectId = newVal.id;
                loadStats();
            }
        });

        // admin 권한이 있을 때만 Manage 메뉴 visible
        if (Session.currentUser != null && Session.currentUser.getRole() != Role.ADMIN) {
            manageButton.setVisible(false);
            manageButton.setManaged(false);
        }

        loadStats();
    }

    private void loadStats() {
        // 재호출 시 시리즈 중첩 방지
        dailyChart.getData().clear();
        monthlyChart.getData().clear();
        statusChart.getData().clear();

        // 상태별 stats
        Map<IssueStatus, Integer> statusStats = statisticsService.getStatusStats(Session.currentProjectId);
        int total = 0;
        for (int v : statusStats.values()) total += v;
        int newCount = statusStats.getOrDefault(IssueStatus.NEW, 0);
        int fixedResolved = statusStats.getOrDefault(IssueStatus.FIXED, 0)
                          + statusStats.getOrDefault(IssueStatus.RESOLVED, 0);
        int closed = statusStats.getOrDefault(IssueStatus.CLOSED, 0);

        totalLabel.setText(String.valueOf(total));
        newLabel.setText(String.valueOf(newCount));
        fixedResolvedLabel.setText(String.valueOf(fixedResolved));
        closedLabel.setText(String.valueOf(closed));

        // 일별 BarChart
        XYChart.Series<String, Number> dailySeries = new XYChart.Series<>();
        dailySeries.setName("일별 발생");
        for (Map.Entry<String, Integer> e : statisticsService.getDailyStats(Session.currentProjectId).entrySet()) {
            dailySeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        dailyChart.getData().add(dailySeries);

        // 월별 BarChart
        XYChart.Series<String, Number> monthlySeries = new XYChart.Series<>();
        monthlySeries.setName("월별 발생");
        for (Map.Entry<String, Integer> e : statisticsService.getMonthlyStats(Session.currentProjectId).entrySet()) {
            monthlySeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        monthlyChart.getData().add(monthlySeries);

        // 상태별 BarChart
        XYChart.Series<String, Number> statusSeries = new XYChart.Series<>();
        statusSeries.setName("상태별 분포");
        for (Map.Entry<IssueStatus, Integer> e : statusStats.entrySet()) {
            statusSeries.getData().add(new XYChart.Data<>(e.getKey().name(), e.getValue()));
        }
        statusChart.getData().add(statusSeries);
    }

    @FXML
    public void onOpenList() {
        // 사이드 메뉴 — 이슈 목록 진입
        ViewLoader.loadView("/fxml/IssueListView.fxml");
    }

    @FXML
    public void onOpenRegister() {
        // 사이드 메뉴 — 이슈 등록 진입
        ViewLoader.loadView("/fxml/IssueRegisterView.fxml");
    }

    @FXML
    public void onOpenManage() {
        // 사이드 메뉴 — 관리 화면 진입 (admin만 visible)
        ViewLoader.loadView("/fxml/ManageView.fxml");
    }

    @FXML
    public void onLogout() {
        // 로그아웃 핸들러
        Session.currentUser = null;
        ViewLoader.loadView("/fxml/LoginView.fxml");
    }
}