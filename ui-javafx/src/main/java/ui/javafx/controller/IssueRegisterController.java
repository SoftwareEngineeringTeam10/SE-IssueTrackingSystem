package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    @FXML
    public void initialize() {
        // 초기화 영역 — priorityCombo 채우기 + Reporter / ReportedDate 자동 표기
    }

    @FXML
    public void onRegister() {
        // 등록 핸들러
    }

    @FXML
    public void onCancel() {
        // 취소 핸들러
    }
}