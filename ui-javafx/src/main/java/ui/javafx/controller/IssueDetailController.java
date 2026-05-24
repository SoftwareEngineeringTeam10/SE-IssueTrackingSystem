package ui.javafx.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.issuetracker.model.Comment;
import org.issuetracker.model.Issue;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.model.RecommendationResult;
import org.issuetracker.model.Role;
import org.issuetracker.model.User;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.PermissionManager;
import ui.javafx.session.Session;
import ui.javafx.session.ViewLoader;
import ui.javafx.util.DateFormats;

import java.time.LocalDateTime;
import java.util.List;

public class IssueDetailController {

    @FXML private ComboBox<String> projectCombo;
    @FXML private Label userLabel;
    @FXML private Label titleLabel;
    @FXML private Label priorityLabel;
    @FXML private Label statusLabel;
    @FXML private Label reporterLabel;
    @FXML private Label reportedDateLabel;
    @FXML private Label assigneeLabel;
    @FXML private Label fixerLabel;
    @FXML private TextArea descriptionArea;
    @FXML private HBox statusActionBox;
    @FXML private ListView<String> commentsList;
    @FXML private TextField newCommentField;
    @FXML private ListView<String> activityLog;
    @FXML private Label systemMessage;
    @FXML private Button manageButton;
    @FXML private Button recommendButton;

    private final IssueService issueService = new IssueService();
    private final AccountManager accountManager = new AccountManager();
    private Issue issue;

    @FXML
    public void initialize() {
        // 초기화 — 사용자 / 프로젝트 / 상태 콤보 / 이슈 데이터 로드
        if (Session.currentUser != null) {
            userLabel.setText("User: " + Session.currentUser.getId());
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

        loadIssue();
    }

    private void loadIssue() {
        issue = issueService.getIssueById(Session.selectedIssueId);
        if (issue == null) {
            systemMessage.setText("이슈를 찾을 수 없습니다");
            return;
        }

        titleLabel.setText(issue.title);
        priorityLabel.setText(issue.priority != null ? issue.priority.name() : "");
        statusLabel.setText(issue.status != null ? issue.status.name() : "");
        reporterLabel.setText(issue.reporter);
        reportedDateLabel.setText(formatDate(issue.reportedDate));
        assigneeLabel.setText(issue.assignee != null ? issue.assignee : "-");
        fixerLabel.setText(issue.fixer != null ? issue.fixer : "-");
        descriptionArea.setText(issue.description);

        // Recommend Assignee — NEW / REOPENED 상태 + PL 권한만 활성
        boolean canRecommend = (issue.status == IssueStatus.NEW || issue.status == IssueStatus.REOPENED)
                               && Session.currentUser != null
                               && PermissionManager.canAssignIssue(Session.currentUser);
        recommendButton.setDisable(!canRecommend);

        populateStatusActions();
        loadComments();
    }

    private void populateStatusActions() {
        // 현재 상태 + 권한 있는 액션만 동적 버튼 추가
        statusActionBox.getChildren().clear();

        User currentUser = Session.currentUser;
        if (currentUser == null) return;
        IssueStatus current = issue.status;

        if (current == IssueStatus.NEW && PermissionManager.canAssignIssue(currentUser)) {
            addActionButton("담당자 배정", IssueStatus.ASSIGNED);
        }
        if (current == IssueStatus.ASSIGNED && PermissionManager.canFixIssue(currentUser)) {
            addActionButton("수정 완료 (Fix)", IssueStatus.FIXED);
        }
        if (current == IssueStatus.FIXED && PermissionManager.canResolveIssue(currentUser)) {
            addActionButton("검증 완료 (Resolve)", IssueStatus.RESOLVED);
        }
        if (current == IssueStatus.FIXED && PermissionManager.canReopenIssue(currentUser)) {
            addActionButton("재오픈 (Reopen)", IssueStatus.REOPENED);
        }
        if (current == IssueStatus.RESOLVED && PermissionManager.canCloseIssue(currentUser)) {
            addActionButton("종료 (Close)", IssueStatus.CLOSED);
        }
        if (current == IssueStatus.REOPENED && PermissionManager.canAssignIssue(currentUser)) {
            addActionButton("담당자 배정 (Reassign)", IssueStatus.ASSIGNED);
        }

        if (statusActionBox.getChildren().isEmpty()) {
            Label noAction = new Label("현재 상태에서 가능한 액션이 없습니다.");
            noAction.setStyle("-fx-text-fill: gray;");
            statusActionBox.getChildren().add(noAction);
        }
    }

    private void addActionButton(String text, IssueStatus targetStatus) {
        Button btn = new Button(text);
        btn.setOnAction(e -> changeStatusTo(targetStatus));
        statusActionBox.getChildren().add(btn);
    }

    private void changeStatusTo(IssueStatus newStatus) {
        issueService.changeStatus(issue.id, newStatus, Session.currentUser);
        systemMessage.setText("상태 변경 완료: " + newStatus.name());
        loadIssue();
    }

    private void loadComments() {
        commentsList.setItems(FXCollections.observableArrayList());
        for (Comment c : issue.comments) {
            commentsList.getItems().add("[" + formatDate(c.date) + "] " + c.authorId + ": " + c.content);
        }
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

    @FXML
    public void onAddComment() {
        // 댓글 추가 핸들러
        if (issue == null || Session.currentUser == null) return;

        String text = newCommentField.getText() != null ? newCommentField.getText().trim() : "";
        if (text.isEmpty()) return;

        Comment comment = new Comment(Session.currentUser.getId(), text);
        issueService.addCommentToIssue(issue.id, comment, Session.currentUser);

        loadIssue();
        newCommentField.clear();
    }

    @FXML
    public void onBack() {
        // 목록 화면 복귀 핸들러
        ViewLoader.loadView("/fxml/IssueListView.fxml");
    }

    @FXML
    public void onOpenRegister() {
        // 사이드 메뉴 — 이슈 등록 진입
        ViewLoader.loadView("/fxml/IssueRegisterView.fxml");
    }

    @FXML
    public void onRecommendAssignee() {
        // 사이드 메뉴 — 현재 이슈에 대한 담당자 추천
        if (issue == null) {
            systemMessage.setText("이슈가 로드되지 않았습니다");
            return;
        }
        systemMessage.setText("추천 호출 중...");
        List<RecommendationResult> results = issueService.getAssigneeRecommendations(issue.id);
        if (results.isEmpty()) {
            systemMessage.setText("추천 결과 없음 — 해결 이력 데이터 부족");
            Alert empty = new Alert(Alert.AlertType.INFORMATION);
            empty.setHeaderText("추천 결과 없음");
            empty.setContentText("해결된 이슈(FIXED/RESOLVED/CLOSED) 이력이 없어\n추천이 불가합니다.");
            empty.showAndWait();
            return;
        }

        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (RecommendationResult r : results) {
            String name = findUserName(r.getFixerId());
            sb.append(idx++).append(". ")
              .append(r.getFixerId())
              .append(" (").append(name).append(")")
              .append(" - score: ")
              .append(String.format("%.2f", r.getScore()))
              .append("\n");
        }

        systemMessage.setText("추천 결과 " + results.size() + "건");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("추천 담당자 (Top " + results.size() + ")");
        alert.setContentText(sb.toString());
        alert.setResizable(true);
        alert.showAndWait();
    }

    // 사용자 id → name 조회 (없으면 "-" 표시)
    private String findUserName(String id) {
        if (id == null) return "-";
        for (User u : accountManager.getUsers()) {
            if (id.equals(u.getId())) return u.getName();
        }
        return "-";
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
