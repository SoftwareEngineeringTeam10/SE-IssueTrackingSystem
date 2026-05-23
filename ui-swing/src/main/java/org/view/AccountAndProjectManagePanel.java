package org.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AccountAndProjectManagePanel extends JPanel {
    private MainFrame mainFrame;

    // 컨트롤러가 데이터를 수집할 수 있도록 멤버 변수로 선언
    private JTextField fieldProjectName;
    private JButton btnCreateProject;

    private JTextField fieldUserId;
    private JPasswordField fieldPassword;
    private JComboBox<String> comboRole;
    private JButton btnCreateAccount;

    public AccountAndProjectManagePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 레이아웃 및 여백 설정 (20픽셀 마진)
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상단 메인 타이틀
        JLabel pageTitle = new JLabel("🛠️ 시스템 관리자 모드 (Admin Dashboard)");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        add(pageTitle, BorderLayout.NORTH);

        // 중앙 영역을 반으로 쪼갤 컨테이너 (GridLayout 1행 2열 사용)
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // ---------------------------------------------------------------
        // 프로젝트 추가 및 관리 영역
        // ---------------------------------------------------------------
        JPanel projectPanel = new JPanel(new GridBagLayout());
        projectPanel.setBackground(Color.WHITE);
        // TitledBorder를 써서 구역을 명확하게 분리합니다.
        projectPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "프로젝트 생성 (Project Management)",
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

        // 아래 빈 공간을 채워주기 위한 컴포넌트 고정
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 1;
        projectPanel.add(Box.createVerticalGlue(), gbc);

        // 프로젝트 생성 버튼 하단 배치
        gbc.gridy = 2; gbc.weighty = 0;
        btnCreateProject = new JButton("새 프로젝트 생성 등록");
        btnCreateProject.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        btnCreateProject.setBackground(new Color(220, 235, 252));
        projectPanel.add(btnCreateProject, gbc);


        // ---------------------------------------------------------------
        //  [우측 섹션] 사용자 계정 생성 영역 (명세 2.1 #1 기능)
        // ---------------------------------------------------------------
        JPanel accountPanel = new JPanel(new GridBagLayout());
        accountPanel.setBackground(Color.WHITE);
        accountPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "사용자 계정 생성 (Account Provisioning)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Malgun Gothic", Font.BOLD, 13), new Color(40, 167, 69)
        ));

        GridBagConstraints gbcAcc = new GridBagConstraints();
        gbcAcc.fill = GridBagConstraints.HORIZONTAL;
        gbcAcc.insets = new Insets(10, 15, 10, 15);

        // 계정 ID 행
        gbcAcc.gridx = 0; gbcAcc.gridy = 0; gbcAcc.weightx = 0;
        JLabel lblId = new JLabel("사용자 ID : ");
        lblId.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        accountPanel.add(lblId, gbcAcc);

        gbcAcc.gridx = 1; gbcAcc.gridy = 0; gbcAcc.weightx = 1;
        fieldUserId = new JTextField();
        fieldUserId.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        accountPanel.add(fieldUserId, gbcAcc);

        // 비밀번호 행
        gbcAcc.gridx = 0; gbcAcc.gridy = 1; gbcAcc.weightx = 0;
        JLabel lblPw = new JLabel("비밀번호 : ");
        lblPw.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        accountPanel.add(lblPw, gbcAcc);

        gbcAcc.gridx = 1; gbcAcc.gridy = 1; gbcAcc.weightx = 1;
        fieldPassword = new JPasswordField();
        accountPanel.add(fieldPassword, gbcAcc);

        // 역할 권한 부여 행 (명세 영역: admin / PL / dev / tester)
        gbcAcc.gridx = 0; gbcAcc.gridy = 2; gbcAcc.weightx = 0;
        JLabel lblRole = new JLabel("권한 (Role) : ");
        lblRole.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        accountPanel.add(lblRole, gbcAcc);

        gbcAcc.gridx = 1; gbcAcc.gridy = 2; gbcAcc.weightx = 1;
        comboRole = new JComboBox<>(new String[]{"ADMIN", "PL", "DEVELOPER", "TESTER"});
        comboRole.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        accountPanel.add(comboRole, gbcAcc);

        // 수직 정렬 글루
        gbcAcc.gridx = 0; gbcAcc.gridy = 3; gbcAcc.gridwidth = 2; gbcAcc.weighty = 1;
        accountPanel.add(Box.createVerticalGlue(), gbcAcc);

        // 계정 추가 버튼 하단 배치
        gbcAcc.gridy = 4; gbcAcc.weighty = 0;
        btnCreateAccount = new JButton("신규 사용자 계정 발급");
        btnCreateAccount.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        btnCreateAccount.setBackground(new Color(230, 245, 230));
        accountPanel.add(btnCreateAccount, gbcAcc);

        // 두 조각 패널을 그리드에 장착하고 센터에 배치
        centerGrid.add(projectPanel);
        centerGrid.add(accountPanel);
        add(centerGrid, BorderLayout.CENTER);
    }

    // Controller가 입력 폼 제어 및 이벤트 위임을 가로챌 수 있게 Getter 개방
    public JTextField getFieldProjectName() { return fieldProjectName; }
    public JButton getBtnCreateProject() { return btnCreateProject; }
    public JTextField getFieldUserId() { return fieldUserId; }
    public JPasswordField getFieldPassword() { return fieldPassword; }
    public JComboBox<String> getComboRole() { return comboRole; }
    public JButton getBtnCreateAccount() { return btnCreateAccount; }
}