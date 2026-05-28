package org.view;

import javax.swing.*;
import java.awt.*;

public class SideMenuPanel extends JPanel {
    private MainFrame mainFrame;

    private JButton btnAdminManage;
    private JButton btnList;
    private JButton btnCreate;
    private JButton btnStats;

    public SideMenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(245, 247, 250));

        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(218, 224, 233)));
        setPreferredSize(new Dimension(180, 700));

        setBorder(BorderFactory.createCompoundBorder(
                getBorder(),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        // 네비게이션 타이틀
        JLabel lblNav = new JLabel("NAVIGATION");
        lblNav.setFont(new Font("Malgun Gothic", Font.BOLD, 11));
        lblNav.setForeground(new Color(140, 150, 170));
        lblNav.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblNav);
        add(Box.createVerticalStrut(20));


        btnAdminManage = createMenuButton("계정/프로젝트 관리");
        btnAdminManage.addActionListener(e -> mainFrame.changeCenterPanel(new AccountAndProjectManagePanel(mainFrame)));
        add(btnAdminManage);
        add(Box.createVerticalStrut(10));


        btnList = createMenuButton("이슈 목록");
        btnList.addActionListener(e -> mainFrame.changeCenterPanel(new IssueListPanel(mainFrame)));
        add(btnList);
        add(Box.createVerticalStrut(10));


        btnCreate = createMenuButton("이슈 등록");
        btnCreate.addActionListener(e -> mainFrame.changeCenterPanel(new IssueCreatePanel(mainFrame)));
        add(btnCreate);
        add(Box.createVerticalStrut(10));


        btnStats = createMenuButton("통계 분석");
        btnStats.addActionListener(e -> mainFrame.changeCenterPanel(new StatsPanel(mainFrame)));
        add(btnStats);

        add(Box.createVerticalGlue());
    }

    // 버튼 생성 헬퍼
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        button.setForeground(new Color(70, 80, 95));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);

        button.setMaximumSize(new Dimension(150, 40));
        button.setPreferredSize(new Dimension(150, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 1));
        return button;
    }

    // Getter 목록
    public JButton getBtnAdminManage() { return btnAdminManage; }
    public JButton getBtnList() { return btnList; }
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnStats() { return btnStats; }
}