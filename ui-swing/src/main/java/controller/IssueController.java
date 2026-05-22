package controller;

import org.example.view.swing.*;
import javax.swing.*;
import java.awt.event.ActionEvent;

public class IssueController {
    private MainFrame mainFrame;

    // 👥 테스트용 가상 세션 권한 세팅 (ADMIN, PL, DEVELOPER, TESTER)
    private String currentRole = "ADMIN";
    private String currentIssueStatus = "NEW";

    public IssueController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
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

    /**
     * 메인 이슈 목록 화면의 버튼들 통제
     */
    private void setupIssueListListners(IssueListPanel panel) {
        if (currentRole.equals("DEVELOPER")) {
            panel.getBtnResolve().setVisible(true);
            panel.getBtnClose().setVisible(false);
        } else if (currentRole.equals("PL")) {
            panel.getBtnResolve().setVisible(false);
            panel.getBtnClose().setVisible(true);
        } else {
            panel.getBtnResolve().setVisible(false);
            panel.getBtnClose().setVisible(false);
        }

        panel.revalidate();
        panel.repaint();

        panel.getBtnSearch().addActionListener(e -> {
            String keyword = panel.getFieldSearch().getText().trim();
            String status = (String) panel.getComboStatusFilter().getSelectedItem();
            String priority = (String) panel.getComboPriorityFilter().getSelectedItem();
            System.out.println("[Controller] 이슈 검색 필터링 가동 -> 키워드: " + keyword + " | 상태: " + status + " | 우선순위: " + priority);
            JOptionPane.showMessageDialog(mainFrame, "조건 필터링 시뮬레이션\n상태: [" + status + "] | 우선순위: [" + priority + "]", "검색 완료", JOptionPane.INFORMATION_MESSAGE);
        });

        panel.getBtnResolve().addActionListener((ActionEvent e) -> {
            int selectedRow = panel.getIssueTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainFrame, "처리할 이슈를 테이블에서 선택해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            panel.getTableModel().setValueAt("FIXED", selectedRow, 3);
            JOptionPane.showMessageDialog(mainFrame, "선택한 이슈가 FIXED(해결) 상태로 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });

        panel.getBtnClose().addActionListener((ActionEvent e) -> {
            int selectedRow = panel.getIssueTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainFrame, "종료할 이슈를 선택해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            panel.getTableModel().setValueAt("CLOSED", selectedRow, 3);
            JOptionPane.showMessageDialog(mainFrame, "이슈가 CLOSED(종료) 되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * 이슈 등록 화면의 등록 버튼 통제
     */
    private void setupIssueCreateListners(IssueCreatePanel panel) {
        panel.getBtnSubmit().addActionListener((ActionEvent e) -> {
            String title = panel.getTitleField().getText().trim();
            String description = panel.getDescriptionArea().getText().trim();
            String priority = (String) panel.getPriorityCombo().getSelectedItem();

            if (title.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "제목과 내용을 모두 입력해주세요!", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(mainFrame, "컨트롤러를 통해 이슈 등록 성공!", "알림", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
        });
    }

    /**
     * 🔒 로그인한 역할에 따라 좌측 메뉴 버튼 숨김/노출 제어
     */
    public void configureSideMenuByRole() {
        SideMenuPanel sideMenu = mainFrame.getSideMenuPanel();
        if (sideMenu == null) return;

        if (currentRole.equals("ADMIN")) {
            sideMenu.getBtnAdminManage().setVisible(true);
            sideMenu.getBtnList().setVisible(false);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
        } else if (currentRole.equals("DEVELOPER")) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
        } else if (currentRole.equals("PL")) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(true);
            sideMenu.getBtnStats().setVisible(true);
        } else if (currentRole.equals("TESTER")) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(true);
            sideMenu.getBtnStats().setVisible(false);
        }

        sideMenu.revalidate();
        sideMenu.repaint();
    }

    /**
     * 관리자(Admin) 전용 프로젝트 및 계정 생성 이벤트 통제
     */
    private void setupAdminManageListeners(AccountAndProjectManagePanel panel) {
        panel.getBtnCreateProject().addActionListener(e -> {
            String projName = panel.getFieldProjectName().getText().trim();
            if (projName.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "생성할 프로젝트명을 입력해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(mainFrame, "[" + projName + "] 프로젝트가 성공적으로 개설되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            panel.getFieldProjectName().setText("");
        });

        panel.getBtnCreateAccount().addActionListener(e -> {
            String userId = panel.getFieldUserId().getText().trim();
            String password = new String(panel.getFieldPassword().getPassword()).trim();
            String role = (String) panel.getComboRole().getSelectedItem();

            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "ID와 패스워드를 누락 없이 입력해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(mainFrame, userId + " (" + role + ") 계정이 시스템에 정합 등록되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            panel.getFieldUserId().setText("");
            panel.getFieldPassword().setText("");
        });
    }

    /**
     * 4. 이슈 상세 보기 화면 컴포넌트 동적 제어 및 시나리오 리스너 바인딩
     */
    private void setupIssueDetailListeners(IssueDetailPanel panel) {
        // ⭐ [결함 교정] 현재 선택한 이슈의 상태가 무엇인지 필터 레이블이나
        // 컨트롤러 세션 상태와 동기화하여 화면 전환 시 데이터 락이 걸리는 현상을 방어합니다.
        panel.getLblStatus().setText(currentIssueStatus);

        // 초기 버튼 상태 초기화
        panel.getBtnRecommend().setVisible(false);
        panel.getBtnDevFixed().setVisible(false);
        panel.getBtnTesterVerify().setVisible(false);
        panel.getBtnReopen().setVisible(false);
        panel.getBtnPlClose().setVisible(false);

        // 🔒 명세서 정합성 시나리오 조건 분기 트리
        if (currentIssueStatus.equals("NEW")) {
            if (currentRole.equals("PL")) {
                panel.getBtnRecommend().setVisible(true);
            }
        } else if (currentIssueStatus.equals("ASSIGNED")) {
            if (currentRole.equals("DEVELOPER")) {
                panel.getBtnDevFixed().setVisible(true);
            }
        } else if (currentIssueStatus.equals("FIXED")) {
            if (currentRole.equals("TESTER")) {
                panel.getBtnTesterVerify().setVisible(true);
                panel.getBtnReopen().setVisible(true);
            }
        } else if (currentIssueStatus.equals("RESOLVED")) {
            if (currentRole.equals("PL")) {
                panel.getBtnPlClose().setVisible(true);
            }
        }

        panel.revalidate();
        panel.repaint();

        // 담당자 자동 추천 기능 리스너
        panel.getBtnRecommend().addActionListener(e -> {
            String[] recommendations = {"dev1 (가장 적합 - 추천도 98%)", "dev2 (추천도 85%)", "dev3 (추천도 70%)"};
            String selectedDeveloper = (String) JOptionPane.showInputDialog(
                    mainFrame, "시스템 분석 기반 자동 추천 담당자 명단 (3명)", "Assignee 추천 결과 호출",
                    JOptionPane.QUESTION_MESSAGE, null, recommendations, recommendations[0]
            );

            if (selectedDeveloper != null) {
                String targetId = selectedDeveloper.split(" ")[0];
                panel.getLblAssignee().setText(targetId);
                currentIssueStatus = "ASSIGNED";
                panel.getLblStatus().setText(currentIssueStatus);
                panel.getBtnRecommend().setVisible(false);
                panel.getAreaCommentList().append("\n[시스템]: PL 권한에 의해 담당자가 " + targetId + "로 배정되어 상태가 ASSIGNED로 변경되었습니다.");
                JOptionPane.showMessageDialog(mainFrame, targetId + " 개발자에게 이슈 배정이 완료되었습니다.", "정합성 완료", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Developer 전용 FIXED 리스너
        panel.getBtnDevFixed().addActionListener(e -> {
            currentIssueStatus = "FIXED";
            panel.getLblStatus().setText(currentIssueStatus);
            panel.getBtnDevFixed().setVisible(false);
            panel.getAreaCommentList().append("\n[시스템]: dev1 개발자가 조치를 완료하여 상태가 FIXED로 업데이트되었습니다.");
            JOptionPane.showMessageDialog(mainFrame, "이슈 상태가 FIXED로 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });

        // Tester 전용 RESOLVED 리스너
        panel.getBtnTesterVerify().addActionListener(e -> {
            currentIssueStatus = "RESOLVED";
            panel.getLblStatus().setText(currentIssueStatus);
            panel.getBtnTesterVerify().setVisible(false);
            panel.getBtnReopen().setVisible(false);
            panel.getAreaCommentList().append("\n[시스템]: 테스터 검증 결과 정상 동작이 확인되어 RESOLVED 처리되었습니다.");
            JOptionPane.showMessageDialog(mainFrame, "검증 완료 처리되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });

        // Tester 전용 REOPENED 리스너
        panel.getBtnReopen().addActionListener(e -> {
            currentIssueStatus = "REOPENED";
            panel.getLblStatus().setText(currentIssueStatus);
            panel.getBtnTesterVerify().setVisible(false);
            panel.getBtnReopen().setVisible(false);
            panel.getAreaCommentList().append("\n[시스템]: 테스터 검증 실패로 인해 결함이 재발(REOPENED)되었습니다.");
            JOptionPane.showMessageDialog(mainFrame, "이슈가 REOPENED 상태로 회귀되었습니다.", "알림", JOptionPane.WARNING_MESSAGE);
        });

        // PL 전용 CLOSED 리스너
        panel.getBtnPlClose().addActionListener(e -> {
            currentIssueStatus = "CLOSED";
            panel.getLblStatus().setText(currentIssueStatus);
            panel.getBtnPlClose().setVisible(false);
            panel.getAreaCommentList().append("\n[시스템]: 프로젝트 리더(PL) 검토 후 최종 CLOSED(종결) 처리되었습니다.");
            JOptionPane.showMessageDialog(mainFrame, "해당 이슈 트래킹 사이클이 완전히 종결되었습니다.", "최종 완수", JOptionPane.INFORMATION_MESSAGE);
        });

        // 댓글 추가 리스너
        panel.getBtnAddComment().addActionListener(e -> {
            String text = panel.getFieldCommentInput().getText().trim();
            if(!text.isEmpty()) {
                panel.getAreaCommentList().append("\n[2026-05-21] " + currentRole + ": " + text);
                panel.getFieldCommentInput().setText("");
            }
        });
    }
}