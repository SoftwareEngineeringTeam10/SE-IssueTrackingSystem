package org.controller;

import org.issuetracker.model.Role;
import org.issuetracker.model.User;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.ProjectService;
import org.view.AccountAndProjectManagePanel;
import org.view.MainFrame;

import javax.swing.*;

public class ManageController {
    private MainFrame mainFrame;
    private final ProjectService projectService;
    private final AccountManager accountManager;
    private final IssueController issueController;

    public ManageController(MainFrame mainFrame, ProjectService projectService, AccountManager accountManager, IssueController issueController) {
        this.mainFrame = mainFrame;
        this.projectService = projectService;
        this.accountManager = accountManager;
        this.issueController = issueController;
    }

    public void bindViewEvents(JPanel currentPanel) {
        if (currentPanel instanceof AccountAndProjectManagePanel) {
            setupAdminManageListeners((AccountAndProjectManagePanel) currentPanel);
        }
    }

    private void setupAdminManageListeners(AccountAndProjectManagePanel panel) {
        User currentUser = issueController.getCurrentUser();

        panel.getBtnCreateProject().addActionListener(e -> {
            String projName = panel.getFieldProjectName().getText().trim();

            if (projName.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "프로젝트 이름을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                projectService.addProject(projName, "Project Description", issueController.getCurrentUser());

                if (issueController != null) {
                    issueController.loadProjectsToHeader();
                }

                JOptionPane.showMessageDialog(mainFrame, "프로젝트가 성공적으로 생성 및 저장되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                panel.getFieldProjectName().setText(""); // 입력창 초기화

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "프로젝트 생성 중 권한 오류 또는 시스템 예외 발생", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.getBtnCreateAccount().addActionListener(e -> {
            String userId = panel.getFieldUserId().getText().trim();
            String password = new String(panel.getFieldPassword().getPassword()).trim();
            String roleStr = (String) panel.getComboRole().getSelectedItem();

            if ("ADMIN".equalsIgnoreCase(roleStr)) {
                JOptionPane.showMessageDialog(mainFrame, "최고 관리자(ADMIN) 계정은 추가로 생성하거나 중복 등록할 수 없습니다.", "보안 경고", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "ID와 패스워드를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String standardRoleStr = roleStr.toUpperCase().trim();
                if (standardRoleStr.contains("DEV") || standardRoleStr.contains("DEVELOPER")) standardRoleStr = "DEV";
                else if (standardRoleStr.contains("PL")) standardRoleStr = "PL";
                else if (standardRoleStr.contains("TESTER")) standardRoleStr = "TESTER";
                else if (standardRoleStr.contains("ADMIN")) standardRoleStr = "ADMIN";

                Role selectedRole = Role.valueOf(standardRoleStr);

                if (selectedRole == Role.ADMIN) {
                    JOptionPane.showMessageDialog(mainFrame, "ADMIN 계정은 추가로 생성할 수 없습니다.", "권한 오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User newUser = new User();
                newUser.setId(userId);
                newUser.setPassword(password);
                newUser.setName(userId);
                newUser.setRole(selectedRole);

                this.accountManager.addUser(newUser);
                JOptionPane.showMessageDialog(mainFrame, userId + " 계정이 생성되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
                panel.getFieldUserId().setText("");
                panel.getFieldPassword().setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "계정 등록 중 오류가 발생했습니다.", "에러", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}