package org.controller;

import org.issuetracker.model.User;
import org.issuetracker.service.AccountManager;
import org.view.IssueListPanel;
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
        if (currentPanel instanceof LoginPanel) {
            setupLoginListeners((LoginPanel) currentPanel);
        }
    }

    private void setupLoginListeners(LoginPanel panel) {
        for (java.awt.event.ActionListener al : panel.getBtnLogin().getActionListeners()) {
            panel.getBtnLogin().removeActionListener(al);
        }

        panel.getBtnLogin().addActionListener(e -> {
            String id = panel.getFieldId().getText().trim();
            String pw = new String(panel.getFieldPassword().getPassword()).trim();

            if (id.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "아이디와 비밀번호를 모두 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean loginSuccess = this.accountManager.login(id, pw);

            if (loginSuccess) {
                User authenticatedUser = this.accountManager.getCurrentUser();

                issueController.setCurrentUser(authenticatedUser);
                issueController.configureSideMenuByRole();

                if (mainFrame.getHeaderPanel() != null) {
                    mainFrame.getHeaderPanel().revalidate();
                    mainFrame.getHeaderPanel().repaint();
                }

                mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
                mainFrame.revalidate();
                mainFrame.repaint();

                JOptionPane.showMessageDialog(mainFrame, authenticatedUser.getName() + "님, 환영합니다!", "로그인 성공", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "아이디 또는 비밀번호가 일치하지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}