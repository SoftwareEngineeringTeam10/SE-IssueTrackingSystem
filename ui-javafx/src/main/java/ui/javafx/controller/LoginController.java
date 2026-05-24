package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.issuetracker.service.AccountManager;
import ui.javafx.session.Session;
import ui.javafx.session.ViewLoader;

public class LoginController {

    @FXML private TextField idField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AccountManager accountManager = new AccountManager();

    @FXML
    public void initialize() {
        // 초기화 — 빈 상태 유지
    }

    @FXML
    public void onLogin() {
        // 로그인 핸들러 — 인증 + 성공 시 IssueListView 전환
        String id = idField.getText() != null ? idField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (id.isEmpty() || password.isEmpty()) {
            messageLabel.setText("ID와 Password를 입력해주세요");
            return;
        }

        boolean success = accountManager.login(id, password);
        if (success) {
            Session.currentUser = accountManager.getCurrentUser();
            ViewLoader.loadView("/fxml/IssueListView.fxml");
        } else {
            messageLabel.setText("로그인 실패 — ID 또는 Password 오류");
        }
    }
}
