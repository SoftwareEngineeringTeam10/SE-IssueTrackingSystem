package org.view;

import javax.swing.*;
import java.awt.*;
import  org.issuetracker.model.Issue;


public class IssueDetailPanel extends JPanel {
    private MainFrame mainFrame;
    private Issue issue;


    // 데이터 갱신 및 컨트롤러 제어를 위한 컴포넌트 멤버 변수
    private JLabel lblTitle, lblPriority, lblStatus, lblReporter, lblAssignee;
    private JTextArea areaDescription, areaCommentList;
    private JTextField fieldCommentInput;
    private JButton btnAddComment, btnBack;

    // 시나리오 정합성을 위해 새로 추가된 버튼들
    private JButton btnRecommend;   // 담당자 추천 버튼
    private JButton btnDevFixed;    // 개발자용 FIXED 처리 버튼
    private JButton btnTesterVerify; // 테스터용 RESOLVED 처리 버튼
    // PL용 CLOSED 처리 버튼은 목록이나 상세 중 한 곳에 배치하며, 여기서는 상세 화면 검증용으로 추가
    private JButton btnPlClose;
    private JButton btnReopen;      // 테스터용 REOPENED 처리 버튼


    public Issue getIssue() {
        return this.issue;
    }

    public IssueDetailPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상단 바 (타이틀 및 목록 복귀)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel pageTitle = new JLabel("이슈 상세 정보 (Issue Details)");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        topBar.add(pageTitle, BorderLayout.WEST);

        btnBack = new JButton("목록으로");
        btnBack.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnBack.setBackground(new Color(240, 240, 240));
        topBar.add(btnBack, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // 중앙 메인 컨테이너
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.add(Box.createVerticalStrut(15));

        // 메타데이터 정보 판넬 (GridBagLayout)
        JPanel metaPanel = new JPanel(new GridBagLayout());
        metaPanel.setBackground(new Color(248, 249, 250));
        metaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 15);

        // 1행: 제목
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("이슈 제목:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.gridwidth = 3;
        lblTitle = new JLabel("로그인 버튼 클릭 시 NullPointerException 발생");
        lblTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        metaPanel.add(lblTitle, gbc);

        gbc.gridwidth = 1; // 규격 초기화

        // 2행: 우선순위 & 상태
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("우선순위:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        lblPriority = new JLabel("BLOCKER");
        lblPriority.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        metaPanel.add(lblPriority, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("현재 상태:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.5;
        lblStatus = new JLabel("NEW");
        lblStatus.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        lblStatus.setForeground(Color.RED);
        metaPanel.add(lblStatus, gbc);

        // 3행: 보고자 & 담당자 (담당자 옆에 추천 버튼 복합 배치)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("보 고 자:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.5;
        lblReporter = new JLabel("tester1");
        lblReporter.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        metaPanel.add(lblReporter, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("당 담 자:"), gbc);

        // 담당자 레이블과 추천 버튼을 한 칸에 묶어서 배치
        JPanel assigneeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        assigneeWrapper.setOpaque(false);
        lblAssignee = new JLabel("-");
        lblAssignee.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));

        btnRecommend = new JButton("담당자 자동 추천 호출");
        btnRecommend.setFont(new Font("Malgun Gothic", Font.BOLD, 11));
        btnRecommend.setBackground(new Color(230, 240, 250));

        assigneeWrapper.add(lblAssignee);
        assigneeWrapper.add(btnRecommend);

        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.5;
        metaPanel.add(assigneeWrapper, gbc);

        centerContainer.add(metaPanel);
        centerContainer.add(Box.createVerticalStrut(15));

        // 본문 설명 영역
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setOpaque(false);
        descPanel.add(createMetaLabel("상세 내용 설명"), BorderLayout.NORTH);

        areaDescription = new JTextArea(4, 30);
        areaDescription.setText("메인 화면에서 로그인 버튼을 누를 시 AuthController 클래스의 42번째 줄에서\n"
                + "NullPointerException이 터지면서 프로그램이 강제 다운되는 버그가 있습니다.");
        areaDescription.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        areaDescription.setEditable(false);
        areaDescription.setBackground(new Color(252, 252, 252));
        areaDescription.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        descPanel.add(new JScrollPane(areaDescription), BorderLayout.CENTER);

        centerContainer.add(descPanel);
        centerContainer.add(Box.createVerticalStrut(15));

        // 댓글 영역
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setOpaque(false);
        commentPanel.add(createMetaLabel("댓글 및 히스토리 (Comments)"), BorderLayout.NORTH);

        areaCommentList = new JTextArea(5, 30);
        areaCommentList.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        areaCommentList.setText("[2026-05-21 14:22] tester1: 이슈 생성합니다.");
        areaCommentList.setEditable(false);
        areaCommentList.setBackground(new Color(245, 245, 245));
        areaCommentList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        commentPanel.add(new JScrollPane(areaCommentList), BorderLayout.CENTER);

        JPanel commentInputPanel = new JPanel(new BorderLayout(8, 0));
        commentInputPanel.setOpaque(false);
        commentInputPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        fieldCommentInput = new JTextField();
        btnAddComment = new JButton("댓글 등록");
        commentInputPanel.add(fieldCommentInput, BorderLayout.CENTER);
        commentInputPanel.add(btnAddComment, BorderLayout.EAST);
        commentPanel.add(commentInputPanel, BorderLayout.SOUTH);

        centerContainer.add(commentPanel);
        centerContainer.add(Box.createVerticalStrut(20));

        // 시나리오별 하단 액션 버튼 배치 구역
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonPanel.setOpaque(false);

        btnDevFixed = new JButton("이슈 해결 완료 (FIXED)");
        btnDevFixed.setBackground(new Color(220, 235, 252));

        btnTesterVerify = new JButton("검증 완료 (RESOLVED)");
        btnTesterVerify.setBackground(new Color(230, 245, 230));

        btnReopen = new JButton("결함 재발 (REOPENED)");
        btnReopen.setBackground(new Color(252, 230, 230));

        btnPlClose = new JButton("최종 종결 (CLOSED)");
        btnPlClose.setBackground(new Color(240, 240, 240));

        actionButtonPanel.add(btnDevFixed);
        actionButtonPanel.add(btnTesterVerify);
        actionButtonPanel.add(btnReopen);
        actionButtonPanel.add(btnPlClose);

        centerContainer.add(actionButtonPanel);
        add(centerContainer, BorderLayout.CENTER);

        // 목록 복귀 이벤트 내부 구현
        btnBack.addActionListener(e -> {
            mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
        });
    }

    private JLabel createMetaLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    // 통제용 Getter 목록
    public JLabel getLblStatus() { return lblStatus; }
    public JLabel getLblAssignee() { return lblAssignee; }
    public JButton getBtnRecommend() { return btnRecommend; }
    public JButton getBtnDevFixed() { return btnDevFixed; }
    public JButton getBtnTesterVerify() { return btnTesterVerify; }
    public JButton getBtnReopen() { return btnReopen; }
    public JButton getBtnPlClose() { return btnPlClose; }
    public JTextField getFieldCommentInput() { return fieldCommentInput; }
    public JButton getBtnAddComment() { return btnAddComment; }
    public JTextArea getAreaCommentList() { return areaCommentList; }
}