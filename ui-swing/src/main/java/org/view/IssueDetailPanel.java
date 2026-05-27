package org.view;

import javax.swing.*;
import java.awt.*;
import org.issuetracker.model.Comment;
import org.issuetracker.model.Issue;

public class IssueDetailPanel extends JPanel {
    private MainFrame mainFrame;
    private Issue issue;

    private JLabel lblTitle, lblPriority, lblStatus, lblReporter, lblAssignee;
    private JTextArea areaDescription, areaCommentList;
    private JTextField fieldCommentInput;
    private JButton btnAddComment, btnBack;

    private JButton btnRecommend;
    private JButton btnDevFixed;
    private JButton btnTesterVerify;
    private JButton btnPlClose;
    private JButton btnReopen;

    public Issue getIssue() {
        return this.issue;
    }

    public IssueDetailPanel(MainFrame mainFrame, Issue issue) {
        this.mainFrame = mainFrame;
        this.issue = issue;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTitle = new JLabel();
        lblPriority = new JLabel();
        lblStatus = new JLabel();
        lblReporter = new JLabel();
        lblAssignee = new JLabel();

        areaDescription = new JTextArea(6, 30);
        areaCommentList = new JTextArea(8, 30);
        fieldCommentInput = new JTextField();

        // 상단 바
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel pageTitle = new JLabel("이슈 상세 정보 - No." + (issue != null ? issue.id : ""));
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        topBar.add(pageTitle, BorderLayout.WEST);

        btnBack = new JButton("목록으로");
        btnBack.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnBack.setBackground(new Color(240, 240, 240));
        topBar.add(btnBack, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // 메인 컨테이너
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.add(Box.createVerticalStrut(15));

        // 메타데이터 정보 패널
        JPanel metaPanel = new JPanel(new GridBagLayout());
        metaPanel.setBackground(new Color(248, 249, 250));
        metaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 15);

        // 제목
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("이슈 제목:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.gridwidth = 3;
        lblTitle = new JLabel(issue != null ? issue.title : "");
        lblTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        metaPanel.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        // 우선순위 및 상태
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("우선순위:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        lblPriority = new JLabel(issue != null && issue.priority != null ? issue.priority.name() : "NONE");
        lblPriority.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        metaPanel.add(lblPriority, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("현재 상태:"), gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.5;
        lblStatus = new JLabel(issue != null && issue.status != null ? issue.status.name() : "NEW");
        lblStatus.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        lblStatus.setForeground(Color.RED);
        metaPanel.add(lblStatus, gbc);

        // 보고자 및 담당자
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("보 고 자:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.5;
        lblReporter = new JLabel(issue != null ? issue.reporterId : "");
        lblReporter.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        metaPanel.add(lblReporter, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0;
        metaPanel.add(createMetaLabel("담당자:"), gbc);

        JPanel assigneeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        assigneeWrapper.setOpaque(false);

        lblAssignee = new JLabel(issue != null && issue.assigneeId != null ? issue.assigneeId : "");
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

        // 본문 설명
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setOpaque(false);
        descPanel.add(createMetaLabel("상세 내용 설명"), BorderLayout.NORTH);

        areaDescription.setText(issue != null ? issue.description : "");
        areaDescription.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        areaDescription.setEditable(false);
        areaDescription.setBackground(new Color(252, 252, 252));
        areaDescription.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane descScrollPane = new JScrollPane(areaDescription);
        descScrollPane.setPreferredSize(new Dimension(400, 100));
        descPanel.add(descScrollPane, BorderLayout.CENTER);

        centerContainer.add(descPanel);
        centerContainer.add(Box.createVerticalStrut(15));

        // 댓글 영역
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setOpaque(false);
        commentPanel.add(createMetaLabel("댓글 및 히스토리 (Comments)"), BorderLayout.NORTH);

        areaCommentList = new JTextArea(5, 30);
        areaCommentList.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        areaCommentList.setEditable(false);
        areaCommentList.setBackground(new Color(245, 245, 245));
        areaCommentList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane commentScrollPane = new JScrollPane(areaCommentList);
        commentScrollPane.setPreferredSize(new Dimension(400, 150));
        commentPanel.add(commentScrollPane, BorderLayout.CENTER);

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

        // 액션 버튼 패널
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

        // 목록 복귀 이벤트
        btnBack.addActionListener(e -> {
            mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
        });

        // 데이터 반영
        if (issue != null) {
            lblTitle.setText(issue.title);
            lblPriority.setText(issue.priority != null ? issue.priority.name() : "NONE");
            lblStatus.setText(issue.status != null ? issue.status.name() : "NEW");
            lblReporter.setText(issue.reporterId != null ? issue.reporterId : "");
            lblAssignee.setText(issue.assigneeId != null ? issue.assigneeId : "");
            areaDescription.setText(issue.description != null ? issue.description : "");

            if (issue.comments != null && !issue.comments.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Comment c : issue.comments) {
                    sb.append("[").append(c.date != null ? c.date : "").append("] ")
                            .append(c.authorId != null ? c.authorId : "").append(": ")
                            .append(c.content != null ? c.content : "").append("\n");
                }
                areaCommentList.setText(sb.toString().trim());
            } else {
                areaCommentList.setText("등록된 댓글이 없습니다.");
            }
        }

        // 생성 완료 시점 컨트롤러 바인딩 강제 호출
        if (mainFrame.getController() != null) {
            mainFrame.getController().bindViewEvents(this);
        }

        revalidate();
        repaint();
    }

    private JLabel createMetaLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    // Getter 목록
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