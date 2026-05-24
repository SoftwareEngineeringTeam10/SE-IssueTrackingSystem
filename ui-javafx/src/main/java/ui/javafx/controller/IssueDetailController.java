package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    @FXML private ComboBox<String> statusCombo;
    @FXML private ListView<String> commentsList;
    @FXML private TextField newCommentField;
    @FXML private ListView<String> activityLog;
    @FXML private Label systemMessage;

    @FXML
    public void initialize() {
        // 초기화 영역 — statusCombo 채우기 + 이슈 데이터 표기
    }

    @FXML
    public void onChangeStatus() {
        // 상태 변경 핸들러
    }

    @FXML
    public void onAddComment() {
        // 댓글 추가 핸들러
    }

    @FXML
    public void onBack() {
        // 목록 화면 복귀 핸들러
    }
}