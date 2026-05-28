package org.controller;

import org.issuetracker.model.*;
import org.issuetracker.service.ProjectService;
import org.view.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.AccountManager;
import java.util.List;

public class IssueController {
    private MainFrame mainFrame;
    private final IssueService issueService;
    private final AccountManager accountManager;
    private final ProjectService projectService;
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

    public IssueController(MainFrame mainFrame, IssueService issueService, AccountManager accountManager, ProjectService projectService) {
        this.mainFrame = mainFrame;
        this.issueService = issueService;
        this.accountManager = accountManager;
        this.projectService = projectService;
    }

    public void bindViewEvents(JPanel currentPanel) {
        if (currentPanel instanceof IssueListPanel) {
            setupIssueListListeners((IssueListPanel) currentPanel);
        } else if (currentPanel instanceof IssueCreatePanel) {
            setupIssueCreateListeners((IssueCreatePanel) currentPanel);
        } else if (currentPanel instanceof AccountAndProjectManagePanel) {
            setupAdminManageListeners((AccountAndProjectManagePanel) currentPanel);
        } else if (currentPanel instanceof IssueDetailPanel) {
            setupIssueDetailListeners((IssueDetailPanel) currentPanel);
        }
    }

    // 이슈 목록 화면 리스너 설정
    private void setupIssueListListeners(IssueListPanel panel) {
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


        panel.getIssueTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int selectedRow = panel.getIssueTable().getSelectedRow();
                    if (selectedRow != -1) {
                        int issueId = (int) panel.getTableModel().getValueAt(selectedRow, 0);
                        Issue targetIssue = issueService.getIssueById(issueId);
                        if (targetIssue != null) {
                            mainFrame.changeCenterPanel(new IssueDetailPanel(mainFrame, targetIssue));
                        }
                    }
                }
            }
        });


        panel.getBtnSearch().addActionListener(e -> {
            String keyword = panel.getFieldSearch().getText().trim().toLowerCase();
            String status = (String) panel.getComboStatusFilter().getSelectedItem();
            String priority = (String) panel.getComboPriorityFilter().getSelectedItem();

            String filterStatus = "전체".equals(status) ? null : status;
            String filterPriority = "전체".equals(priority) ? null : priority;

            int currentProjectId = -1;
            if (mainFrame.getHeaderPanel() != null && mainFrame.getHeaderPanel().getProjectCombo().getSelectedItem() != null) {
                String selectedProj = (String) mainFrame.getHeaderPanel().getProjectCombo().getSelectedItem();
                if (selectedProj.contains(" : ")) {
                    currentProjectId = Integer.parseInt(selectedProj.split(" : ")[0]);
                }
            }

            List<Issue> filteredIssues = issueService.searchIssues(currentProjectId, filterStatus, null, null, filterPriority);

            panel.getTableModel().setRowCount(0);

            if (filteredIssues != null) {
                for (Issue issue : filteredIssues) {
                    if (!keyword.isEmpty()) {
                        boolean matchTitle = issue.title != null && issue.title.toLowerCase().contains(keyword);
                        boolean matchDesc = issue.description != null && issue.description.toLowerCase().contains(keyword);
                        if (!matchTitle && !matchDesc) continue;
                    }


                    Object[] row = {
                            issue.id,
                            issue.title,
                            issue.priority != null ? issue.priority.name() : "-",
                            issue.status != null ? issue.status.name() : "NEW",
                            issue.reporter != null ? issue.reporter : "-",
                            issue.assignee != null ? issue.assignee : "-"
                    };
                    panel.getTableModel().addRow(row);
                }
            }
            panel.getTableModel().fireTableDataChanged();
        });

        // Resolve 버튼
        panel.getBtnResolve().addActionListener((ActionEvent e) -> {
            int selectedRow = panel.getIssueTable().getSelectedRow();
            if (selectedRow == -1) return;
            int issueId = (int) panel.getTableModel().getValueAt(selectedRow, 0);

            try {
                issueService.changeStatus(issueId, IssueStatus.FIXED, currentUser);
                panel.loadIssues();
                JOptionPane.showMessageDialog(mainFrame, "이슈 상태가 FIXED로 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
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
                issueService.changeStatus(issueId, IssueStatus.CLOSED, currentUser);
                Issue realIssue = issueService.getIssueById(issueId);
                if (realIssue != null) {
                    realIssue.setStatus(IssueStatus.CLOSED);
                }
                panel.loadIssues();
                JOptionPane.showMessageDialog(mainFrame, "이슈가 CLOSED 처리되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "상태 변경 권한이 없거나 전이 규칙에 위배됩니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    // 이슈 등록 버튼 리스너
    private void setupIssueCreateListeners(IssueCreatePanel panel) {
        //중복 등록 방지
        for (java.awt.event.ActionListener al : panel.getBtnSubmit().getActionListeners()) {
            panel.getBtnSubmit().removeActionListener(al);
        }
        panel.getBtnSubmit().addActionListener((ActionEvent e) -> {
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

            try {
                Priority priority = Priority.valueOf(priorityStr.toUpperCase());
                Issue newIssue = new Issue();
                newIssue.title = title;
                newIssue.description = description;
                newIssue.priority = priority;
                newIssue.status = IssueStatus.NEW;
                newIssue.projectId = projectId;

                issueService.createIssue(projectId, newIssue, currentUser);
                JOptionPane.showMessageDialog(mainFrame, "이슈가 등록되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
            } catch (Exception ex) {
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
            return;
        }

        Role currentRole = currentUser.getRole();

        if (currentRole == Role.ADMIN) {
            sideMenu.getBtnAdminManage().setVisible(true);
            sideMenu.getBtnList().setVisible(false);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
        } else {
            // DEV, PL, TESTER 모두 이슈 목록, 생성, 통계 접근 가능
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(true);
            sideMenu.getBtnStats().setVisible(true);
        }

        sideMenu.revalidate();
        sideMenu.repaint();
    }

    // 프로젝트 및 계정 관리 리스너
    private void setupAdminManageListeners(AccountAndProjectManagePanel panel) {
        panel.getBtnCreateProject().addActionListener(e -> {
            String projName = panel.getFieldProjectName().getText().trim();
            String projDesc = "";

            if (projName.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "프로젝트명을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                projectService.addProject(projName, projDesc, currentUser);

                // 생성 직후 헤더 콤보박스 즉시 동기화
                if (mainFrame.getHeaderPanel() != null) {
                    mainFrame.getHeaderPanel().loadProjects();
                }

                JOptionPane.showMessageDialog(mainFrame, "프로젝트가 성공적으로 생성 및 저장되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                panel.getFieldProjectName().setText("");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "프로젝트 생성 권한이 없거나 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
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

        for (java.awt.event.ActionListener al : panel.getBtnRecommend().getActionListeners()) {
            panel.getBtnRecommend().removeActionListener(al);
        }

        // 담당자 추천 및 배정
        panel.getBtnRecommend().addActionListener(e -> {
            try {
                org.issuetracker.service.RecommendationServiceImpl recService = new org.issuetracker.service.RecommendationServiceImpl();

                List<Issue> allHistoricalIssues = issueService.getAllIssues();

                List<RecommendationResult> recs = recService.recommend(currentIssue, allHistoricalIssues);

                if (recs == null || recs.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "현재 이슈와 유사한 해결 이력이 없거나 커트라인(5%)을 통과한 개발자가 없습니다.",
                            "알림", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] selectionOptions = new String[recs.size()];
                for (int i = 0; i < recs.size(); i++) {
                    RecommendationResult r = recs.get(i);
                    int matchPercent = (int) (r.getScore() * 100);
                    selectionOptions[i] = r.getFixerId() + " | (추천 점수: " + matchPercent + "% / 매칭 이력: " + r.getMatchedIssueIds() + ")";
                }

                String selectedOption = (String) JOptionPane.showInputDialog(
                        mainFrame,
                        "분석 결과 가장 연관성이 높은 후보 TOP-3 명단입니다.\n배정할 개발자를 선택하세요.",
                        " 최적의 담당 개발자 추천 (Best Candidate)",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        selectionOptions,
                        selectionOptions[0]
                );

                if (selectedOption == null) return;


                String targetDeveloperId = selectedOption.split(" \\| ")[0].trim();

                if (!targetDeveloperId.isEmpty()) {
                    panel.getLblAssignee().setText(targetDeveloperId);
                    currentIssue.assignee = targetDeveloperId;

                    issueService.assignIssue(currentIssue.id, targetDeveloperId, currentUser);
                    currentIssue.setStatus(IssueStatus.ASSIGNED);

                    panel.getLblStatus().setText(IssueStatus.ASSIGNED.name());
                    panel.getBtnRecommend().setVisible(false);

                    panel.getAreaCommentList().append("\n[시스템]: 추천 시스템 분석을 통해 담당자가 [" + targetDeveloperId + "]로 최적 배정되었습니다.");

                    JOptionPane.showMessageDialog(mainFrame,
                            "[" + targetDeveloperId + "] 개발자에게 이슈 배정 및 데이터 저장이 완료되었습니다.",
                            "배정 성공", JOptionPane.INFORMATION_MESSAGE);

                    panel.revalidate();
                    panel.repaint();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "추천 알고리즘 처리 중 컨텍스트 예외 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        for (java.awt.event.ActionListener al : panel.getBtnDevFixed().getActionListeners()) {
            panel.getBtnDevFixed().removeActionListener(al);
        }
        // FIXED 처리
        panel.getBtnDevFixed().addActionListener(e -> {
            try {
                issueService.changeStatus(currentIssue.id, IssueStatus.FIXED, currentUser);

                currentIssue.setStatus(IssueStatus.FIXED);

                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnDevFixed().setVisible(false);

                panel.getAreaCommentList().append("\n[시스템]: " + currentUser.getId() + " 개발자가 조치를 완료했습니다.");
                JOptionPane.showMessageDialog(mainFrame, "상태가 FIXED로 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "상태 변경 권한이 없거나 전이 규칙에 위배됩니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // RESOLVED 처리
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

                panel.getAreaCommentList().append("\n[시스템]: 검증 결과 정상 동작이 확인되었습니다.");
                JOptionPane.showMessageDialog(mainFrame, "검증이 완료되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "상태 변경 권한이 없거나 전이 규칙에 위배됩니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        for (java.awt.event.ActionListener al : panel.getBtnReopen().getActionListeners()) {
            panel.getBtnReopen().removeActionListener(al);
        }
        // REOPENED 처리
        panel.getBtnReopen().addActionListener(e -> {
            try {
                issueService.changeStatus(currentIssue.id, IssueStatus.REOPENED, currentUser);
                currentIssue.setStatus(IssueStatus.REOPENED);

                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnTesterVerify().setVisible(false);
                panel.getBtnReopen().setVisible(false);

                panel.getAreaCommentList().append("\n[시스템]: 검증 실패로 이슈가 재오픈되었습니다.");
                JOptionPane.showMessageDialog(mainFrame, "이슈가 재오픈되었습니다.", "알림", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "상태 변경 권한이 없거나 전이 규칙에 위배됩니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        for (java.awt.event.ActionListener al : panel.getBtnPlClose().getActionListeners()) {
            panel.getBtnPlClose().removeActionListener(al);
        }
        // CLOSED 처리
        panel.getBtnPlClose().addActionListener(e -> {
            try {
                issueService.changeStatus(currentIssue.id, IssueStatus.CLOSED, currentUser);
                currentIssue.setStatus(IssueStatus.CLOSED);

                panel.getLblStatus().setText(currentIssue.getStatus().name());
                panel.getBtnPlClose().setVisible(false);

                panel.getAreaCommentList().append("\n[시스템]: 최종 CLOSED 처리되었습니다.");
                JOptionPane.showMessageDialog(mainFrame, "이슈가 최종 종결되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "상태 변경 권한이 없거나 전이 규칙에 위배됩니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        for (java.awt.event.ActionListener al : panel.getBtnAddComment().getActionListeners()) {
            panel.getBtnAddComment().removeActionListener(al);
        }

        // 댓글 등록
        panel.getBtnAddComment().addActionListener(e -> {
            String text = panel.getFieldCommentInput().getText().trim();

            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "댓글 내용을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                org.issuetracker.model.Comment newComment = new org.issuetracker.model.Comment();
                newComment.authorId = currentUser.getId();
                newComment.content = text;
                newComment.date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                issueService.addCommentToIssue(currentIssue.id, newComment, currentUser);

                panel.getAreaCommentList().append("\n[" + newComment.date + "] " + newComment.authorId + ": " + text);
                panel.getFieldCommentInput().setText("");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "댓글 등록 중 오류가 발생했습니다.", "에러", JOptionPane.ERROR_MESSAGE);
            }
        });

        for (java.awt.event.ActionListener al : panel.getBtnDirectAssign().getActionListeners()) {
            panel.getBtnDirectAssign().removeActionListener(al);
        }

        panel.getBtnDirectAssign().addActionListener(e -> {
            try {
                List<User> devUsers = accountManager.getUsersByRole(Role.DEV);

                java.util.ArrayList<String> userOptions = new java.util.ArrayList<>();
                userOptions.add("선택 안 함");

                if (devUsers != null && !devUsers.isEmpty()) {
                    for (User user : devUsers) {
                        userOptions.add(user.getId());
                    }
                }

                String[] selectionValues = userOptions.toArray(new String[0]);

                String initialValue = (currentIssue.assignee != null && !currentIssue.assignee.isEmpty())
                        ? currentIssue.assignee : "선택 안 함";

                String selectedDeveloper = (String) JOptionPane.showInputDialog(
                        mainFrame,
                        "담당 개발자를 선택하세요.", // 팝업 메시지
                        "담당자 직접 지정",         // 팝업 창 제목
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        selectionValues,
                        initialValue
                );

                if (selectedDeveloper == null) {
                    return;
                }

                if ("선택 안 함".equals(selectedDeveloper)) {
                    issueService.assignIssue(currentIssue.id, "", currentUser);
                    currentIssue.assignee = "";
                    panel.getLblAssignee().setText("미지정");
                    panel.getAreaCommentList().append("\n[시스템]: 담당자 지정이 해제되었습니다.");
                } else {
                    issueService.assignIssue(currentIssue.id, selectedDeveloper, currentUser);
                    currentIssue.assignee = selectedDeveloper;
                    panel.getLblAssignee().setText(selectedDeveloper);
                    panel.getAreaCommentList().append("\n[시스템]: 담당자가 " + selectedDeveloper + "(으)로 변경되었습니다.");
                }

                panel.revalidate();
                panel.repaint();

                JOptionPane.showMessageDialog(mainFrame, "담당자 배정이 업데이트되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "담당자 배정 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });



    }

    public AccountManager getAccountManager() { return this.accountManager; }
    public IssueService getService() { return this.issueService; }
    public ProjectService getProjectService() { return this.projectService; }
}