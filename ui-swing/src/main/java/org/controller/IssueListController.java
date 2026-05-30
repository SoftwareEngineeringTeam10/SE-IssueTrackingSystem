package org.controller;

import org.view.IssueListPanel;
import org.view.MainFrame;
import org.view.IssueDetailPanel;
import org.issuetracker.service.IssueService;
import org.issuetracker.model.IssueStatus;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IssueListController {
    private MainFrame mainFrame;
    private final IssueService issueService;
    private final IssueController issueController;

    public IssueListController(MainFrame mainFrame, IssueService issueService, IssueController issueController) {
        this.mainFrame = mainFrame;
        this.issueService = issueService;
        this.issueController = issueController;
    }

    public void bindViewEvents(JPanel currentPanel) {
        if (currentPanel instanceof IssueListPanel) {
            setupListPanelListeners((IssueListPanel) currentPanel);
        }
    }

    private void setupListPanelListeners(IssueListPanel panel) {
        for (java.awt.event.ActionListener al : panel.getBtnSearch().getActionListeners()) {
            panel.getBtnSearch().removeActionListener(al);
        }
        panel.getBtnSearch().addActionListener(e -> {
            panel.loadIssues();
        });

        for (java.awt.event.ActionListener al : panel.getComboStatusFilter().getActionListeners()) {
            panel.getComboStatusFilter().removeActionListener(al);
        }
        panel.getComboStatusFilter().addActionListener(e -> {
            panel.loadIssues();
        });

        for (java.awt.event.ActionListener al : panel.getComboPriorityFilter().getActionListeners()) {
            panel.getComboPriorityFilter().removeActionListener(al);
        }
        panel.getComboPriorityFilter().addActionListener(e -> {
            panel.loadIssues();
        });

        if (panel.getComboReporterFilter() != null) {
            for (java.awt.event.ActionListener al : panel.getComboReporterFilter().getActionListeners()) {
                panel.getComboReporterFilter().removeActionListener(al);
            }
            panel.getComboReporterFilter().addActionListener(e -> {
                panel.loadIssues();
            });
        }

        if (panel.getComboAssigneeFilter() != null) {
            for (java.awt.event.ActionListener al : panel.getComboAssigneeFilter().getActionListeners()) {
                panel.getComboAssigneeFilter().removeActionListener(al);
            }
            panel.getComboAssigneeFilter().addActionListener(e -> {
                panel.loadIssues();
            });
        }

        for (java.awt.event.ActionListener al : panel.getBtnResolve().getActionListeners()) {
            panel.getBtnResolve().removeActionListener(al);
        }
        btnResolveAction(panel);

        for (java.awt.event.ActionListener al : panel.getBtnClose().getActionListeners()) {
            panel.getBtnClose().removeActionListener(al);
        }
        panel.getBtnClose().addActionListener(e -> {
            handleStatusTransition(panel, IssueStatus.CLOSED);
        });

        JTable issueTable = panel.getIssueTable();
        issueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    int selectedRow = issueTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int modelRow = issueTable.convertRowIndexToModel(selectedRow);
                        Object idObj = panel.getTableModel().getValueAt(modelRow, 0);

                        if (idObj != null) {
                            int issueId = Integer.parseInt(idObj.toString());

                            try {
                                org.issuetracker.model.Issue selectedIssue = issueService.getIssueById(issueId);
                                if (selectedIssue != null) {
                                    IssueDetailPanel detailPanel = new IssueDetailPanel(mainFrame, selectedIssue);
                                    mainFrame.changeCenterPanel(detailPanel);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    private void btnResolveAction(IssueListPanel panel) {
        panel.getBtnResolve().addActionListener(e -> {
            handleStatusTransition(panel, IssueStatus.FIXED);
        });
    }

    private void handleStatusTransition(IssueListPanel panel, IssueStatus targetStatus) {
        int selectedRow = panel.getIssueTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "이슈를 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int issueId = (int) panel.getTableModel().getValueAt(selectedRow, 0);

            boolean isSuccess = issueService.changeStatus(issueId, targetStatus, issueController.getCurrentUser());

            if (isSuccess) {
                panel.loadIssues();
                JOptionPane.showMessageDialog(mainFrame, "이슈 상태가 " + targetStatus.name() + "(으)로 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "유효하지 않은 상태 전환이거나 권한이 부족합니다.", "전이 실패", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "상태 전이 처리 중 예기치 못한 시스템 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}