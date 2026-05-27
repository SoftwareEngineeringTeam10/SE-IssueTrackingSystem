package org.controller;

import org.issuetracker.model.*;
import org.view.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.AccountManager;
import java.util.List;

public class IssueController {
    private MainFrame mainFrame;
    private final IssueService issueService;
    private final AccountManager accountManager;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user == null) {
            accountManager.logout();
        }
    }
    public User getCurrentUser() {
        return this.currentUser;
    }

    public IssueController(MainFrame mainFrame, IssueService issueService, AccountManager accountManager) {
        this.mainFrame = mainFrame;
        this.issueService = issueService;
        this.accountManager = accountManager;
    }

    public void bindViewEvents(JPanel currentPanel) {
        if (currentPanel instanceof IssueListPanel) {
            setupIssueListListners((IssueListPanel) currentPanel);
        } else if (currentPanel instanceof IssueCreatePanel) {
            setupIssueCreateListners((IssueCreatePanel) currentPanel);
        } else if (currentPanel instanceof AccountAndProjectManagePanel) {
            setupAdminManageListeners((AccountAndProjectManagePanel) currentPanel);
        } else if (currentPanel instanceof IssueDetailPanel) {
            setupIssueDetailListeners((IssueDetailPanel) currentPanel);
        }
    }

    // 이슈 목록 화면 리스너 설정
    private void setupIssueListListners(IssueListPanel panel) {
        if (currentUser == null) {
            panel.getBtnResolve().setVisible(false);
            panel.getBtnClose().setVisible(false);
            return;
        }

        Role currentRole = currentUser.getRole();

        if (currentRole == Role.DEV) {
            panel.getBtnResolve().setVisible(true);
            panel.getBtnClose().setVisible(false);
        } else if (currentRole == Role.PL) {
            panel.getBtnResolve().setVisible(false);
            panel.getBtnClose().setVisible(true);
        } else {
            panel.getBtnResolve().setVisible(false);
            panel.getBtnClose().setVisible(false);
        }

        panel.revalidate();
        panel.repaint();

        // 검색 버튼
        panel.getBtnSearch().addActionListener(e -> {
            String keyword = panel.getFieldSearch().getText().trim().toLowerCase();
            String status = (String) panel.getComboStatusFilter().getSelectedItem();
            String priority = (String) panel.getComboPriorityFilter().getSelectedItem();

            String filterStatus = "전체".equals(status) ? null : status;
            List<Issue> filteredIssues = issueService.searchIssues(filterStatus, null, null);

            panel.getTableModel().setRowCount(0);

            if (filteredIssues != null) {
                for (Issue issue : filteredIssues) {
                    if (!keyword.isEmpty()) {
                        boolean matchTitle = issue.title != null && issue.title.toLowerCase().contains(keyword);
                        boolean matchDesc = issue.description != null && issue.description.toLowerCase().contains(keyword);
                        if (!matchTitle && !matchDesc) continue;
                    }
                    if (!"전체".equals(priority)) {
                        if (issue.priority == null || !issue.priority.name().equalsIgnoreCase(priority)) continue;
                    }

                    Object[] row = {
                            issue.id,
                            issue.title,
                            issue.priority != null ? issue.priority.name() : "-",
                            issue.status != null ? issue.status.name() : "NEW",
                            issue.reporterId != null ? issue.reporterId : "-",
                            issue.assigneeId != null ? issue.assigneeId : "-"
                    };
                    panel.getTableModel().addRow(row);
                }
            }
            panel.getTableModel().fireTableDataChanged();
        });

        // Resolve 버튼
        panel.getBtnResolve().addActionListener((ActionEvent e) -> {
            int selectedRow = panel.getIssueTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainFrame, "처리할 이슈를 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int issueId = (int) panel.getTableModel().getValueAt(selectedRow, 0);

            try {
                Issue realIssue = issueService.getIssueById(issueId);
                if (realIssue != null) {
                    realIssue.setStatus(IssueStatus.FIXED);
                }

                issueService.changeStatus(issueId, IssueStatus.FIXED, currentUser);
                panel.loadIssues();

                JOptionPane.showMessageDialog(mainFrame, "이슈 상태가 FIXED로 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "상태 변경 권한이 없거나 전이 규칙에 위배됩니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Close 버튼
        panel.getBtnClose().addActionListener((ActionEvent e) -> {
            int selectedRow = panel.getIssueTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainFrame, "종료할 이슈를 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int issueId = (int) panel.getTableModel().getValueAt(selectedRow, 0);

            try {
                Issue realIssue = issueService.getIssueById(issueId);
                if (realIssue != null) {
                    realIssue.setStatus(IssueStatus.CLOSED);
                }

                issueService.changeStatus(issueId, IssueStatus.CLOSED, currentUser);
                panel.loadIssues();

                JOptionPane.showMessageDialog(mainFrame, "이슈가 CLOSED 처리되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "상태 변경 권한이 없거나 전이 규칙에 위배됩니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // 이슈 등록 버튼 리스너
    private void setupIssueCreateListners(IssueCreatePanel panel) {
        panel.getBtnSubmit().addActionListener((ActionEvent e) -> {
            String title = panel.getTitleField().getText().trim();
            String description = panel.getDescriptionArea().getText().trim();
            String priorityStr = (String) panel.getPriorityCombo().getSelectedItem();

            if (title.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "제목과 내용을 모두 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Priority priority = Priority.valueOf(priorityStr.toUpperCase());
                Issue newIssue = new Issue();
                newIssue.title = title;
                newIssue.description = description;
                newIssue.priority = priority;
                newIssue.status = IssueStatus.NEW;

                issueService.createIssue(newIssue, currentUser);
                JOptionPane.showMessageDialog(mainFrame, "이슈가 등록되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "이슈 등록 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // 권한별 사이드 메뉴 제어
    public void configureSideMenuByRole() {
        SideMenuPanel sideMenu = mainFrame.getSideMenuPanel();
        if (sideMenu == null) return;

        if (currentUser == null) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(false);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
            sideMenu.revalidate();
            sideMenu.repaint();
            return;
        }

        Role currentRole = currentUser.getRole();

        if (currentRole == Role.ADMIN) {
            sideMenu.getBtnAdminManage().setVisible(true);
            sideMenu.getBtnList().setVisible(false);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
        } else if (currentRole == Role.DEV) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
        } else if (currentRole == Role.PL) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(true);
            sideMenu.getBtnStats().setVisible(true);
        } else if (currentRole == Role.TESTER) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(true);
            sideMenu.getBtnStats().setVisible(false);
        }

        sideMenu.revalidate();
        sideMenu.repaint();
    }

    // 프로젝트 및 계정 관리 리스너
    private void setupAdminManageListeners(AccountAndProjectManagePanel panel) {
        panel.getBtnCreateProject().addActionListener(e -> {
            String projName = panel.getFieldProjectName().getText().trim();
            if (projName.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "프로젝트명을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(mainFrame, "프로젝트가 생성되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            panel.getFieldProjectName().setText("");
        });

        panel.getBtnCreateAccount().addActionListener(e -> {
            String userId = panel.getFieldUserId().getText().trim();
            String password = new String(panel.getFieldPassword().getPassword()).trim();
            String roleStr = (String) panel.getComboRole().getSelectedItem();

            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "ID와 패스워드를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String standardRoleStr;
                switch (roleStr.toUpperCase().trim()) {
                    case "DEVELOPER":
                    case "DEV":
                        standardRoleStr = "DEV";
                        break;
                    case "PROJECT LEADER":
                    case "PL":
                        standardRoleStr = "PL";
                        break;
                    case "TESTER":
                    case "QA":
                    case "QUALITY ASSURANCE":
                        standardRoleStr = "TESTER";
                        break;
                    case "ADMIN":
                    case "ADMINISTRATOR":
                        standardRoleStr = "ADMIN";
                        break;
                    default:
                        standardRoleStr = roleStr.toUpperCase().trim();
                        break;
                }

                Role selectedRole = Role.valueOf(standardRoleStr);

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
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "계정 등록 중 오류가 발생했습니다.", "에러", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // 이슈 상세 화면 리스너 설정
    private void setupIssueDetailListeners(IssueDetailPanel panel) {
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
            if (currentRole == Role.PL) {
                panel.getBtnRecommend().setVisible(true);
            }
        } else if (currentIssue.getStatus() == IssueStatus.ASSIGNED) {
            if (currentRole == Role.DEV) {
                panel.getBtnDevFixed().setVisible(true);
            }
        } else if (currentIssue.getStatus() == IssueStatus.FIXED) {
            if (currentRole == Role.TESTER) {
                panel.getBtnTesterVerify().setVisible(true);
                panel.getBtnReopen().setVisible(true);
            }
        } else if (currentIssue.getStatus() == IssueStatus.RESOLVED) {
            if (currentRole == Role.PL) {
                panel.getBtnPlClose().setVisible(true);
            }
        }

        panel.revalidate();
        panel.repaint();

        // 담당자 추천 기능
        panel.getBtnRecommend().addActionListener(e -> {
            List<RecommendationResult> recs = issueService.getAssigneeRecommendations(currentIssue.id);

            String[] recommendations;
            if (recs == null || recs.isEmpty()) {
                recommendations = new String[]{"추천 개발자가 없습니다."};
            } else {
                recommendations = new String[recs.size()];
                for (int i = 0; i < recs.size(); i++) {
                    recommendations[i] = recs.get(i).getFixerId() + " (추천도: " + String.format("%.1f", recs.get(i).getScore() * 100) + "%)";
                }
            }

            String selectedDeveloper = (String) JOptionPane.showInputDialog(
                    mainFrame, "담당자 선택", "추천 결과",
                    JOptionPane.QUESTION_MESSAGE, null, recommendations, recommendations[0]
            );

            if (selectedDeveloper != null && recs != null && !recs.isEmpty()) {
                String targetId = selectedDeveloper.split(" ")[0];
                panel.getLblAssignee().setText(targetId);

                issueService.assignIssue(currentIssue.id, targetId, currentUser);

                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnRecommend().setVisible(false);
                panel.getAreaCommentList().append("\n[시스템]: 담당자가 " + targetId + "로 배정되었습니다.");
                JOptionPane.showMessageDialog(mainFrame, "이슈 배정이 완료되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // FIXED 처리
        panel.getBtnDevFixed().addActionListener(e -> {
            currentIssue.setStatus(IssueStatus.FIXED);
            issueService.changeStatus(currentIssue.id, IssueStatus.FIXED, currentUser);

            panel.getLblStatus().setText(currentIssue.getStatus().name());
            panel.getBtnDevFixed().setVisible(false);

            panel.getAreaCommentList().append("\n[시스템]: " + currentUser.getId() + " 개발자가 조치를 완료했습니다.");
            JOptionPane.showMessageDialog(mainFrame, "상태가 FIXED로 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });

        // RESOLVED 처리
        panel.getBtnTesterVerify().addActionListener(e -> {
            currentIssue.setStatus(IssueStatus.RESOLVED);
            issueService.changeStatus(currentIssue.id, IssueStatus.RESOLVED, currentUser);

            panel.getLblStatus().setText(currentIssue.getStatus().name());
            panel.getBtnTesterVerify().setVisible(false);
            panel.getBtnReopen().setVisible(false);

            panel.getAreaCommentList().append("\n[시스템]: 검증 결과 정상 동작이 확인되었습니다.");
            JOptionPane.showMessageDialog(mainFrame, "검증이 완료되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });

        // REOPENED 처리
        panel.getBtnReopen().addActionListener(e -> {
            currentIssue.setStatus(IssueStatus.REOPENED);
            issueService.changeStatus(currentIssue.id, IssueStatus.REOPENED, currentUser);

            panel.getLblStatus().setText(currentIssue.getStatus().name());
            panel.getBtnTesterVerify().setVisible(false);
            panel.getBtnReopen().setVisible(false);

            panel.getAreaCommentList().append("\n[시스템]: 검증 실패로 이슈가 재오픈되었습니다.");
            JOptionPane.showMessageDialog(mainFrame, "이슈가 재오픈되었습니다.", "알림", JOptionPane.WARNING_MESSAGE);
        });

        // CLOSED 처리
        panel.getBtnPlClose().addActionListener(e -> {
            currentIssue.setStatus(IssueStatus.CLOSED);
            issueService.changeStatus(currentIssue.id, IssueStatus.CLOSED, currentUser);

            panel.getLblStatus().setText(currentIssue.getStatus().name());
            panel.getBtnPlClose().setVisible(false);

            panel.getAreaCommentList().append("\n[시스템]: 최종 CLOSED 처리되었습니다.");
            JOptionPane.showMessageDialog(mainFrame, "이슈가 최종 종결되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });

        // 댓글 등록
        panel.getBtnAddComment().addActionListener(e -> {
            String text = panel.getFieldCommentInput().getText().trim();
            if (!text.isEmpty()) {
                try {
                    org.issuetracker.model.Comment newComment = new org.issuetracker.model.Comment();
                    newComment.authorId = currentUser.getId();
                    newComment.content = text;
                    newComment.date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    issueService.addComment(currentIssue.id, newComment, currentUser);

                    panel.getAreaCommentList().append("\n[" + newComment.date + "] " + newComment.authorId + ": " + text);
                    panel.getFieldCommentInput().setText("");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "댓글 등록 중 오류가 발생했습니다.", "에러", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "댓글 내용을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    public AccountManager getAccountManager() { return this.accountManager; }
    public IssueService getService() { return this.issueService; }
}