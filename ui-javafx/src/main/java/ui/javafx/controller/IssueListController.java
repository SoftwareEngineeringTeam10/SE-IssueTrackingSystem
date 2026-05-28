package ui.javafx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.issuetracker.model.Issue;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.model.Priority;
import org.issuetracker.model.RecommendationResult;
import org.issuetracker.model.Role;
import org.issuetracker.model.User;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.IssueService;
import ui.javafx.session.Session;
import ui.javafx.session.ViewLoader;
import ui.javafx.util.DateFormats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class IssueListController {

    @FXML private ComboBox<String> projectCombo;
    @FXML private Label userLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private ComboBox<String> assigneeFilter;
    @FXML private ComboBox<String> reporterFilter;
    @FXML private TableView<Issue> issueTable;
    @FXML private ListView<String> activityLog;
    @FXML private Label systemMessage;
    @FXML private Button manageButton;
    @FXML private Button registerButton;

    private final IssueService issueService = new IssueService();
    private final AccountManager accountManager = new AccountManager();

    @FXML
    public void initialize() {
        // 초기화 — 필터 / 테이블 / 데이터 로드
        if (Session.currentUser != null) {
            userLabel.setText("User: " + Session.currentUser.getId());
        }

        // admin 권한이 있을 때만 Manage 메뉴 visible
        if (Session.currentUser != null && Session.currentUser.getRole() != Role.ADMIN) {
            manageButton.setVisible(false);
            manageButton.setManaged(false);
        }

        // admin은 이슈 등록 대상이 아니므로 등록 메뉴 숨김
        if (Session.currentUser != null && Session.currentUser.getRole() == Role.ADMIN) {
            registerButton.setVisible(false);
            registerButton.setManaged(false);
        }

        // 프로젝트 콤보 — 더미값 + 비활성
        projectCombo.getItems().add("기본 프로젝트");
        projectCombo.getSelectionModel().selectFirst();
        projectCombo.setDisable(true);

        // 상태 필터
        statusFilter.getItems().add("전체");
        for (IssueStatus s : IssueStatus.values()) {
            statusFilter.getItems().add(s.name());
        }
        statusFilter.getSelectionModel().selectFirst();

        // 우선순위 필터
        priorityFilter.getItems().add("전체");
        for (Priority p : Priority.values()) {
            priorityFilter.getItems().add(p.name());
        }
        priorityFilter.getSelectionModel().selectFirst();

        // assignee 필터 — 전체 사용자 ID 목록
        assigneeFilter.getItems().add("전체");
        for (User u : accountManager.getUsers()) {
            assigneeFilter.getItems().add(u.getId());
        }
        assigneeFilter.getSelectionModel().selectFirst();

        // reporter 필터 — 전체 사용자 ID 목록
        reporterFilter.getItems().add("전체");
        for (User u : accountManager.getUsers()) {
            reporterFilter.getItems().add(u.getId());
        }
        reporterFilter.getSelectionModel().selectFirst();

        // 테이블 컬럼 셀 매핑
        setupTableColumns();

        // 행 더블클릭 → 상세 화면 전환
        issueTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Issue selected = issueTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Session.selectedIssueId = selected.id;
                    ViewLoader.loadView("/fxml/IssueDetailView.fxml");
                }
            }
        });

        // 데이터 로드
        loadIssues(issueService.getAllIssues());
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        // 0:# / 1:Title / 2:Priority / 3:Status / 4:Reporter / 5:Assignee / 6:Reported Date
        var cols = issueTable.getColumns();
        ((TableColumn<Issue, String>) cols.get(0)).setCellValueFactory(
            c -> new SimpleStringProperty(String.valueOf(c.getValue().id)));
        ((TableColumn<Issue, String>) cols.get(1)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().title));
        ((TableColumn<Issue, String>) cols.get(2)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().priority != null ? c.getValue().priority.name() : ""));
        ((TableColumn<Issue, String>) cols.get(3)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().status != null ? c.getValue().status.name() : ""));
        ((TableColumn<Issue, String>) cols.get(4)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().reporter));
        ((TableColumn<Issue, String>) cols.get(5)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().assignee != null ? c.getValue().assignee : "-"));
        ((TableColumn<Issue, String>) cols.get(6)).setCellValueFactory(
            c -> new SimpleStringProperty(formatDate(c.getValue().reportedDate)));
    }

    // 일자 포맷 정정 — ISO LocalDateTime → 공통 12h AM/PM
    private static String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            return LocalDateTime.parse(raw).format(DateFormats.ISSUE_TIMESTAMP);
        } catch (Exception e) {
            return raw;
        }
    }

    private void loadIssues(List<Issue> issues) {
        ObservableList<Issue> data = FXCollections.observableArrayList(issues);
        issueTable.setItems(data);
    }

    @FXML
    public void onSearch() {
        // 검색 핸들러 — 상태/담당자/보고자는 service, 제목/우선순위는 후처리 필터
        String keyword = searchField.getText() != null ? searchField.getText().trim() : "";
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();
        String assignee = assigneeFilter.getValue();
        String reporter = reporterFilter.getValue();

        // "전체" 또는 null 처리
        String statusArg = ("전체".equals(status) || status == null) ? null : status;
        String assigneeArg = ("전체".equals(assignee) || assignee == null) ? null : assignee;
        String reporterArg = ("전체".equals(reporter) || reporter == null) ? null : reporter;

        // 백엔드 다중 조건 호출
        List<Issue> result = issueService.searchIssues(Session.currentProjectId, statusArg, assigneeArg, reporterArg, null);

        // priority 프론트 후처리 (백엔드 미지원)
        if (!"전체".equals(priority) && priority != null) {
            result = result.stream()
                    .filter(i -> i.priority != null && i.priority.name().equals(priority))
                    .collect(Collectors.toList());
        }

        // 제목 키워드 후처리
        if (!keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            result.removeIf(i -> i.title == null || !i.title.toLowerCase().contains(kw));
        }

        loadIssues(result);
    }

    @FXML
    public void onOpenRegister() {
        // 이슈 등록 화면 진입 핸들러
        ViewLoader.loadView("/fxml/IssueRegisterView.fxml");
    }

    @FXML
    public void onOpenDetail() {
        // 이슈 상세 화면 진입 핸들러
        Issue selected = issueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            systemMessage.setText("이슈를 선택해주세요");
            return;
        }
        Session.selectedIssueId = selected.id;
        ViewLoader.loadView("/fxml/IssueDetailView.fxml");
    }

    @FXML
    public void onRecommendAssignee() {
        // 담당자 추천 호출 핸들러
        Issue selected = issueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            systemMessage.setText("이슈를 선택해주세요");
            return;
        }
        List<RecommendationResult> results = issueService.getAssigneeRecommendations(selected.id);
        if (results.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "추천 결과 없음").showAndWait();
            return;
        }
        StringBuilder sb = new StringBuilder("추천 담당자\n\n");
        for (RecommendationResult r : results) {
            sb.append(r.getFixerId())
              .append(" (score: ")
              .append(String.format("%.2f", r.getScore()))
              .append(")\n");
        }
        new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait();
    }

    @FXML
    public void onLogout() {
        // 로그아웃 핸들러
        Session.currentUser = null;
        ViewLoader.loadView("/fxml/LoginView.fxml");
    }

    @FXML
    public void onOpenManage() {
        // 사이드 메뉴 — 관리 화면 진입 (admin만 visible)
        ViewLoader.loadView("/fxml/ManageView.fxml");
    }

    @FXML
    public void onOpenStats() {
        // 사이드 메뉴 — 통계 화면 진입
        ViewLoader.loadView("/fxml/StatsView.fxml");
    }
}