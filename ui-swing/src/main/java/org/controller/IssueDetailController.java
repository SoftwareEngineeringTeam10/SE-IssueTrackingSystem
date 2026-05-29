package org.controller;

import org.issuetracker.model.*;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.AccountManager;
import org.view.IssueDetailPanel;
import org.view.MainFrame;

import javax.swing.*;
import java.util.List;

public class IssueDetailController {
    private MainFrame mainFrame;
    private final IssueService issueService;
    private final AccountManager accountManager;
    private final IssueController issueController;

    public IssueDetailController(MainFrame mainFrame, IssueService issueService, AccountManager accountManager, IssueController issueController) {
        this.mainFrame = mainFrame;
        this.issueService = issueService;
        this.accountManager = accountManager;
        this.issueController = issueController;
    }

    public void bindViewEvents(JPanel currentPanel) {
        if (currentPanel instanceof IssueDetailPanel) {
            setupIssueDetailListeners((IssueDetailPanel) currentPanel);
        }
    }

    private void setupIssueDetailListeners(IssueDetailPanel panel) {
        User currentUser = issueController.getCurrentUser();
        if (currentUser == null || panel.getIssue() == null) return;

        Issue currentIssue = panel.getIssue();
        Role currentRole = currentUser.getRole();

        panel.getLblStatus().setText(currentIssue.getStatus().name());
        panel.getBtnRecommend().setVisible(false);
        panel.getBtnDevFixed().setVisible(false);
        panel.getBtnTesterVerify().setVisible(false);
        panel.getBtnReopen().setVisible(false);
        panel.getBtnPlClose().setVisible(false);

        if (currentIssue.getStatus() == IssueStatus.NEW || currentIssue.getStatus() == IssueStatus.REOPENED) {
            if (currentRole == Role.PL) panel.getBtnRecommend().setVisible(true);
        } else if (currentIssue.getStatus() == IssueStatus.ASSIGNED) {
            if (currentRole == Role.DEV) panel.getBtnDevFixed().setVisible(true);
        } else if (currentIssue.getStatus() == IssueStatus.FIXED) {
            if (currentRole == Role.TESTER) {
                panel.getBtnTesterVerify().setVisible(true);
                panel.getBtnReopen().setVisible(true);
            }
        } else if (currentIssue.getStatus() == IssueStatus.RESOLVED) {
            if (currentRole == Role.PL) panel.getBtnPlClose().setVisible(true);
        }

        panel.revalidate();
        panel.repaint();

        // 담당자 추천 버튼
        for (java.awt.event.ActionListener al : panel.getBtnRecommend().getActionListeners()) {
            panel.getBtnRecommend().removeActionListener(al);
        }
        panel.getBtnRecommend().addActionListener(e -> {
            try {
                org.issuetracker.service.RecommendationServiceImpl recService = new org.issuetracker.service.RecommendationServiceImpl();
                List<Issue> allHistoricalIssues = issueService.getAllIssues();
                List<RecommendationResult> recs = recService.recommend(currentIssue, allHistoricalIssues);

                if (recs == null || recs.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, "커트라인(5%)을 통과한 개발자가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] selectionOptions = new String[recs.size()];
                for (int i = 0; i < recs.size(); i++) {
                    RecommendationResult r = recs.get(i);
                    selectionOptions[i] = r.getFixerId() + " | (추천 점수: " + (int)(r.getScore() * 100) + "%)";
                }

                String selectedOption = (String) JOptionPane.showInputDialog(mainFrame, "개발자를 선택하세요.", "추천 시스템", JOptionPane.QUESTION_MESSAGE, null, selectionOptions, selectionOptions[0]);
                if (selectedOption == null) return;

                String targetDeveloperId = selectedOption.split(" \\| ")[0].trim();
                if (!targetDeveloperId.isEmpty()) {
                    panel.getLblAssignee().setText(targetDeveloperId);
                    currentIssue.assignee = targetDeveloperId;
                    issueService.assignIssue(currentIssue.id, targetDeveloperId, currentUser);
                    currentIssue.setStatus(IssueStatus.ASSIGNED);
                    panel.getLblStatus().setText(IssueStatus.ASSIGNED.name());
                    panel.getBtnRecommend().setVisible(false);
                    panel.getAreaCommentList().append("\n[시스템]: 담당자가 [" + targetDeveloperId + "]로 배정되었습니다.");
                    JOptionPane.showMessageDialog(mainFrame, "배정 완료.", "성공", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "추천 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // FIXED 버튼
        for (java.awt.event.ActionListener al : panel.getBtnDevFixed().getActionListeners()) {
            panel.getBtnDevFixed().removeActionListener(al);
        }
        panel.getBtnDevFixed().addActionListener(e -> {
            try {
                issueService.changeStatus(currentIssue.id, IssueStatus.FIXED, currentUser);
                currentIssue.setStatus(IssueStatus.FIXED);
                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnDevFixed().setVisible(false);
                panel.getAreaCommentList().append("\n[시스템]: 조치 완료.");
                JOptionPane.showMessageDialog(mainFrame, "FIXED 변경 완료.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "권한 위배.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        //  VERIFY 버튼
        for (java.awt.event.ActionListener al : panel.getBtnTesterVerify().getActionListeners()) {
            panel.getBtnTesterVerify().removeActionListener(al);
        }
        panel.getBtnTesterVerify().addActionListener(e -> {
            try {
                issueService.changeStatus(currentIssue.id, IssueStatus.RESOLVED, currentUser);
                currentIssue.setStatus(IssueStatus.RESOLVED);
                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnTesterVerify().setVisible(false);
                panel.getBtnReopen().setVisible(false);
                JOptionPane.showMessageDialog(mainFrame, "검증 완료.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "오류 발생.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // REOPEN 버튼
        for (java.awt.event.ActionListener al : panel.getBtnReopen().getActionListeners()) {
            panel.getBtnReopen().removeActionListener(al);
        }
        panel.getBtnReopen().addActionListener(e -> {
            try {
                issueService.changeStatus(currentIssue.id, IssueStatus.REOPENED, currentUser);
                currentIssue.setStatus(IssueStatus.REOPENED);
                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnTesterVerify().setVisible(false);
                panel.getBtnReopen().setVisible(false);
                JOptionPane.showMessageDialog(mainFrame, "재오픈 완료.", "알림", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "오류 발생.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // CLOSE 버튼
        for (java.awt.event.ActionListener al : panel.getBtnPlClose().getActionListeners()) {
            panel.getBtnPlClose().removeActionListener(al);
        }
        panel.getBtnPlClose().addActionListener(e -> {
            try {
                issueService.changeStatus(currentIssue.id, IssueStatus.CLOSED, currentUser);
                currentIssue.setStatus(IssueStatus.CLOSED);
                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnPlClose().setVisible(false);
                JOptionPane.showMessageDialog(mainFrame, "종결 완료.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "오류 발생.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 댓글 등록 버튼
        for (java.awt.event.ActionListener al : panel.getBtnAddComment().getActionListeners()) {
            panel.getBtnAddComment().removeActionListener(al);
        }
        panel.getBtnAddComment().addActionListener(e -> {
            String text = panel.getFieldCommentInput().getText().trim();
            if (text.isEmpty()) return;
            try {
                org.issuetracker.model.Comment newComment = new org.issuetracker.model.Comment();
                newComment.authorId = currentUser.getId();
                newComment.content = text;
                newComment.date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                issueService.addCommentToIssue(currentIssue.id, newComment, currentUser);
                panel.getAreaCommentList().append("\n[" + newComment.date + "] " + newComment.authorId + ": " + text);
                panel.getFieldCommentInput().setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "댓글 오류.", "에러", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 직접 배정 버튼
        for (java.awt.event.ActionListener al : panel.getBtnDirectAssign().getActionListeners()) {
            panel.getBtnDirectAssign().removeActionListener(al);
        }
        panel.getBtnDirectAssign().addActionListener(e -> {
            try {
                List<User> devUsers = accountManager.getUsersByRole(Role.DEV);
                java.util.ArrayList<String> userOptions = new java.util.ArrayList<>();
                userOptions.add("선택 안 함");
                if (devUsers != null) {
                    for (User u : devUsers) userOptions.add(u.getId());
                }
                String[] selectionValues = userOptions.toArray(new String[0]);
                String initialValue = (currentIssue.assignee != null && !currentIssue.assignee.isEmpty()) ? currentIssue.assignee : "선택 안 함";
                String selectedDeveloper = (String) JOptionPane.showInputDialog(mainFrame, "개발자 선택", "직접 지정", JOptionPane.QUESTION_MESSAGE, null, selectionValues, initialValue);
                if (selectedDeveloper == null) return;

                if ("선택 안 함".equals(selectedDeveloper)) {
                    issueService.assignIssue(currentIssue.id, "", currentUser);
                    currentIssue.assignee = "";
                    panel.getLblAssignee().setText("미지정");
                } else {
                    issueService.assignIssue(currentIssue.id, selectedDeveloper, currentUser);
                    currentIssue.assignee = selectedDeveloper;
                    panel.getLblAssignee().setText(selectedDeveloper);
                }
                panel.revalidate();
                panel.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "배정 오류.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}