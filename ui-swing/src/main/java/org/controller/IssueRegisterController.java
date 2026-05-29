package org.controller;

import org.issuetracker.model.*;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.PermissionManager;
import org.view.IssueListPanel;
import org.view.IssueCreatePanel;
import org.view.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class IssueRegisterController {
    private MainFrame mainFrame;
    private final IssueService issueService;
    private final IssueController issueController;

    public IssueRegisterController(MainFrame mainFrame, IssueService issueService, IssueController issueController) {
        this.mainFrame = mainFrame;
        this.issueService = issueService;
        this.issueController = issueController;
    }

    public void bindViewEvents(JPanel currentPanel) {
        if (currentPanel instanceof IssueCreatePanel) {
            setupIssueCreateListeners((IssueCreatePanel) currentPanel);
        }
    }

    private void setupIssueCreateListeners(IssueCreatePanel panel) {
        for (java.awt.event.ActionListener al : panel.getBtnSubmit().getActionListeners()) {
            panel.getBtnSubmit().removeActionListener(al);
        }

        panel.getBtnSubmit().addActionListener((ActionEvent e) -> {
            User currentUser = issueController.getCurrentUser();

            if (currentUser == null || currentUser.getRole() == Role.ADMIN) {
                JOptionPane.showMessageDialog(mainFrame, "이슈 등록 권한이 없습니다.", "권한 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String title = panel.getTitleField().getText().trim();
            String description = panel.getDescriptionArea().getText().trim();
            String priorityStr = (String) panel.getPriorityCombo().getSelectedItem();

            int projectId = -1;
            if (mainFrame.getHeaderPanel() != null && mainFrame.getHeaderPanel().getProjectCombo().getSelectedItem() != null) {
                String selectedProj = (String) mainFrame.getHeaderPanel().getProjectCombo().getSelectedItem();
                if (selectedProj.contains(" : ")) {
                    projectId = Integer.parseInt(selectedProj.split(" : ")[0]);
                }
            }

            if (projectId == -1) {
                JOptionPane.showMessageDialog(mainFrame, "선택된 프로젝트를 찾을 수 없습니다. 헤더를 확인해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (title.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "제목과 내용을 모두 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!PermissionManager.canCreateIssue(currentUser)) {
                JOptionPane.showMessageDialog(mainFrame, "해당 프로젝트에 이슈를 생성할 권한이 없습니다. (가드 규칙 위배)", "권한 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Priority priority = Priority.valueOf(priorityStr.toUpperCase());
                Issue newIssue = new Issue();
                newIssue.title = title;
                newIssue.description = description;
                newIssue.priority = priority;
                newIssue.status = IssueStatus.NEW;
                newIssue.projectId = projectId;

                issueService.createIssue(projectId, newIssue, currentUser);

                JOptionPane.showMessageDialog(mainFrame, "이슈가 성공적으로 등록되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);


                mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "이슈 등록 처리 중 예기치 못한 시스템 오류가 발생했습니다.", "등록 오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}