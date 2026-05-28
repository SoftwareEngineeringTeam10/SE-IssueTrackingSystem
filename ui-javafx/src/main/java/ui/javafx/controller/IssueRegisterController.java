package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.issuetracker.model.Issue;
import org.issuetracker.model.Priority;
import org.issuetracker.model.Role;
import org.issuetracker.service.IssueService;
import ui.javafx.session.Session;
import ui.javafx.session.ViewLoader;
import ui.javafx.util.DateFormats;

import java.time.LocalDateTime;

public class IssueRegisterController {

    @FXML private ComboBox<String> projectCombo;
    @FXML private Label userLabel;
    @FXML private TextField projectField;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField reporterField;
    @FXML private TextField reportedDateField;
    @FXML private ListView<String> activityLog;
    @FXML private Label systemMessage;
    @FXML private Button manageButton;

    private final IssueService issueService = new IssueService();

    @FXML
    public void initialize() {
        // 초기화 — 사용자 / 프로젝트 / 우선순위 / 일자 자동 채움
        if (Session.currentUser != null) {
            userLabel.setText("User: " + Session.currentUser.getId());
            reporterField.setText(Session.currentUser.getId());
        }

        // admin 권한이 있을 때만 Manage 메뉴 visible
        if (Session.currentUser != null && Session.currentUser.getRole() != Role.ADMIN) {
            manageButton.setVisible(false);
            manageButton.setManaged(false);
        }

        // 프로젝트 콤보 — 더미값 + 비활성
        projectCombo.getItems().add("기본 프로젝트");
        projectCombo.getSelectionModel().selectFirst();
        projectCombo.setDisable(true);
        projectField.setText("기본 프로젝트");

        // 우선순위 콤보 (기본값 MAJOR)
        for (Priority p : Priority.values()) {
            priorityCombo.getItems().add(p.name());
        }
        priorityCombo.getSelectionModel().select(Priority.MAJOR.name());

        // 등록 일자
        reportedDateField.setText(LocalDateTime.now().format(DateFormats.ISSUE_TIMESTAMP));
    }

    @FXML
    public void onRegister() {
        // 등록 핸들러 — Issue 객체 생성 후 service 호출
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String description = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";
        String priorityStr = priorityCombo.getValue();

        if (title.isEmpty() || description.isEmpty()) {
            systemMessage.setText("제목과 내용을 입력해주세요");
            return;
        }

        if (Session.currentUser == null) {
            systemMessage.setText("로그인이 필요합니다");
            return;
        }

        Issue issue = new Issue();
        issue.title = title;
        issue.description = description;
        issue.priority = Priority.valueOf(priorityStr);

        issueService.createIssue(issue, Session.currentUser);

        new Alert(Alert.AlertType.INFORMATION, "이슈 등록 완료").showAndWait();
        ViewLoader.loadView("/fxml/IssueListView.fxml");
    }

    @FXML
    public void onCancel() {
        // 취소 핸들러 — 저장 없이 목록 복귀
        ViewLoader.loadView("/fxml/IssueListView.fxml");
    }

    @FXML
    public void onOpenList() {
        // 사이드 메뉴 — 이슈 목록 진입
        ViewLoader.loadView("/fxml/IssueListView.fxml");
    }

    @FXML
    public void onOpenRecommend() {
        // 사이드 메뉴 — 등록 화면에서는 목록으로 이동 후 추천 호출
        ViewLoader.loadView("/fxml/IssueListView.fxml");
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

    @FXML
    public void onOpenStats() {
        // 사이드 메뉴 — 통계 화면 진입
        ViewLoader.loadView("/fxml/StatsView.fxml");
    }
}
