package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField idField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        // 초기화 영역 — 백엔드 머지 후 박힘
    }

    @FXML
    public void onLogin() {
        // 로그인 핸들러 — 백엔드 머지 후 박힘
    }
}