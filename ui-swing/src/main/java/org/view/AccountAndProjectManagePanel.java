package org.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AccountAndProjectManagePanel extends JPanel {
    private MainFrame mainFrame;

    private JTextField fieldProjectName;
    private JButton btnCreateProject;
    private JTextField fieldUserId;
    private JPasswordField fieldPassword;
    private JComboBox<String> comboRole;
    private JButton btnCreateAccount;

    public AccountAndProjectManagePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel pageTitle = new JLabel("시스템 관리자 모드");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        add(pageTitle, BorderLayout.NORTH);

        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // 프로젝트 생성 패널
        JPanel projectPanel = new JPanel(new GridBagLayout());
        projectPanel.setBackground(Color.WHITE);
        projectPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "프로젝트 생성",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Malgun Gothic", Font.BOLD, 13), new Color(70, 130, 180)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 15, 15, 15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblProjName = new JLabel("프로젝트명 : ");
        lblProjName.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        projectPanel.add(lblProjName, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        fieldProjectName = new JTextField();
        fieldProjectName.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        projectPanel.add(fieldProjectName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 1;
        projectPanel.add(Box.createVerticalGlue(), gbc);

        gbc.gridy = 2; gbc.weighty = 0;
        btnCreateProject = new JButton("프로젝트 생성");
        btnCreateProject.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        btnCreateProject.setBackground(new Color(220, 235, 252));
        projectPanel.add(btnCreateProject, gbc);

        // 사용자 계정 생성 패널
        JPanel accountPanel = new JPanel(new GridBagLayout());
        accountPanel.setBackground(Color.WHITE);
        accountPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "사용자 계정 생성",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Malgun Gothic", Font.BOLD, 13), new Color(40, 167, 69)
        ));

        GridBagConstraints gbcAcc = new GridBagConstraints();
        gbcAcc.fill = GridBagConstraints.HORIZONTAL;
        gbcAcc.insets = new Insets(10, 15, 10, 15);

        gbcAcc.gridx = 0; gbcAcc.gridy = 0; gbcAcc.weightx = 0;
        JLabel lblId = new JLabel("사용자 ID : ");
        lblId.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        accountPanel.add(lblId, gbcAcc);

        gbcAcc.gridx = 1; gbcAcc.gridy = 0; gbcAcc.weightx = 1;
        fieldUserId = new JTextField();
        fieldUserId.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        accountPanel.add(fieldUserId, gbcAcc);

        gbcAcc.gridx = 0; gbcAcc.gridy = 1; gbcAcc.weightx = 0;
        JLabel lblPw = new JLabel("비밀번호 : ");
        lblPw.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        accountPanel.add(lblPw, gbcAcc);

        gbcAcc.gridx = 1; gbcAcc.gridy = 1; gbcAcc.weightx = 1;
        fieldPassword = new JPasswordField();
        accountPanel.add(fieldPassword, gbcAcc);

        gbcAcc.gridx = 0; gbcAcc.gridy = 2; gbcAcc.weightx = 0;
        JLabel lblRole = new JLabel("권한 (Role) : ");
        lblRole.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        accountPanel.add(lblRole, gbcAcc);

        gbcAcc.gridx = 1; gbcAcc.gridy = 2; gbcAcc.weightx = 1;
        comboRole = new JComboBox<>(new String[]{"ADMIN", "PL", "DEVELOPER", "TESTER"});
        comboRole.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        accountPanel.add(comboRole, gbcAcc);

        gbcAcc.gridx = 0; gbcAcc.gridy = 3; gbcAcc.gridwidth = 2; gbcAcc.weighty = 1;
        accountPanel.add(Box.createVerticalGlue(), gbcAcc);

        gbcAcc.gridy = 4; gbcAcc.weighty = 0;
        btnCreateAccount = new JButton("계정 생성");
        btnCreateAccount.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        btnCreateAccount.setBackground(new Color(230, 245, 230));
        accountPanel.add(btnCreateAccount, gbcAcc);

        centerGrid.add(projectPanel);
        centerGrid.add(accountPanel);
        add(centerGrid, BorderLayout.CENTER);
    }

    public JTextField getFieldProjectName() { return fieldProjectName; }
    public JButton getBtnCreateProject() { return btnCreateProject; }
    public JTextField getFieldUserId() { return fieldUserId; }
    public JPasswordField getFieldPassword() { return fieldPassword; }
    public JComboBox<String> getComboRole() { return comboRole; }
    public JButton getBtnCreateAccount() { return btnCreateAccount; }
}