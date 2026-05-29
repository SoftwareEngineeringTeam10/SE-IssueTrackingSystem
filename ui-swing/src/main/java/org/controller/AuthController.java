package org.controller;

import org.issuetracker.model.User;
import org.issuetracker.model.Role;
import org.issuetracker.service.AccountManager;
import org.view.IssueListPanel;
import org.view.AccountAndProjectManagePanel;
import org.view.HeaderPanel;
import org.view.LoginPanel;
import org.view.MainFrame;

import javax.swing.*;

public class AuthController {
    private MainFrame mainFrame;
    private final AccountManager accountManager;
    private final IssueController issueController;

    public AuthController(MainFrame mainFrame, AccountManager accountManager, IssueController issueController) {
        this.mainFrame = mainFrame;
        this.accountManager = accountManager;
        this.issueController = issueController;
    }

    public void bindViewEvents(JPanel currentPanel) {
        if (currentPanel instanceof HeaderPanel) {
            setupHeaderListeners((HeaderPanel) currentPanel);
        }
        else if (currentPanel instanceof LoginPanel) {
            setupLoginPanelListeners((LoginPanel) currentPanel);
        }
    }

    private void setupLoginPanelListeners(LoginPanel panel) {

        for (java.awt.event.ActionListener al : panel.getBtnLogin().getActionListeners()) {
            panel.getBtnLogin().removeActionListener(al);
        }


        panel.getBtnLogin().addActionListener(e -> {
            String id = panel.getFieldId().getText().trim();
            String pw = new String(panel.getFieldPassword().getPassword()).trim();


            handleLogin(id, pw);
        });
    }

    private void setupHeaderListeners(HeaderPanel panel) {
        for (java.awt.event.ActionListener al : panel.getLoginBtn().getActionListeners()) {
            panel.getLoginBtn().removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : panel.getLogoutBtn().getActionListeners()) {
            panel.getLogoutBtn().removeActionListener(al);
        }

        panel.getLoginBtn().addActionListener(e -> {
            String id = panel.getIdField().getText().trim();
            String pw = new String(panel.getPwField().getPassword()).trim();
            handleLogin(id, pw);
        });

        panel.getLogoutBtn().addActionListener(e -> {
            handleLogout();
        });
    }

    public void handleLogin(String id, String pw) {
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "ID와 PW를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean loginSuccess = this.accountManager.login(id, pw);

        if (loginSuccess) {
            User loggedInUser = this.accountManager.getCurrentUser();
            Role matchedRole = loggedInUser.getRole();

            issueController.setCurrentUser(loggedInUser);
            issueController.configureSideMenuByRole();

            // 상단 바 UI 상태를 USER 상태
            if (mainFrame.getHeaderPanel() != null) {
                mainFrame.getHeaderPanel().showUserMode(loggedInUser, matchedRole);
            }

            // 권한별 중앙 패널 화면 전환
            if (matchedRole == Role.ADMIN) {
                mainFrame.changeCenterPanel(new AccountAndProjectManagePanel(mainFrame));
            } else {
                mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
            }

            JOptionPane.showMessageDialog(mainFrame, loggedInUser.getName() + "님 환영합니다.", "로그인 성공", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(mainFrame, "ID 또는 PW가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleLogout() {
        issueController.setCurrentUser(null);
        issueController.configureSideMenuByRole();

        if (mainFrame.getHeaderPanel() != null) {
            mainFrame.getHeaderPanel().showGuestMode();
        }

        org.view.LoginPanel loginPanel = new org.view.LoginPanel();
        mainFrame.changeCenterPanel(loginPanel);
    }
}