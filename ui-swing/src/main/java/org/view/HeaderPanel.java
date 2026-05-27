package org.view;

import javax.swing.*;
import java.awt.*;
import org.issuetracker.model.User;
import org.issuetracker.model.Role;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.AccountManager;

public class HeaderPanel extends JPanel {
    private MainFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel authCardContainer;

    // 로그인 전 컴포넌트
    private JTextField idField;
    private JPasswordField pwField;
    private JButton loginBtn;

    // 로그인 후 컴포넌트
    private JLabel userContainerLabel;
    private JButton logoutBtn;

    public HeaderPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 헤더 패널 기본 설정
        setBackground(new Color(43, 43, 43));
        setPreferredSize(new Dimension(0, 55));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // 좌측 프로젝트 선택 구역
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        leftPanel.setOpaque(false);

        JLabel projectLabel = new JLabel("Project:");
        projectLabel.setForeground(Color.WHITE);
        projectLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));

        JComboBox<String> projectCombo = new JComboBox<>(new String[]{"Model-Based-ITS", "NextGen-ERP", "SmartFactory-IoT"});
        projectCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));

        leftPanel.add(projectLabel);
        leftPanel.add(projectCombo);
        add(leftPanel, BorderLayout.WEST);

        // 우측 로그인/로그아웃 카드 레이아웃 구성
        cardLayout = new CardLayout();
        authCardContainer = new JPanel(cardLayout);
        authCardContainer.setOpaque(false);

        // 로그인 전 패널
        JPanel beforeLoginPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        beforeLoginPanel.setOpaque(false);

        idField = new JTextField(8);
        pwField = new JPasswordField(8);
        loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Malgun Gothic", Font.BOLD, 11));

        JLabel idLabel = new JLabel("ID:");
        idLabel.setForeground(Color.WHITE);
        JLabel pwLabel = new JLabel("PW:");
        pwLabel.setForeground(Color.WHITE);

        beforeLoginPanel.add(idLabel);
        beforeLoginPanel.add(idField);
        beforeLoginPanel.add(pwLabel);
        beforeLoginPanel.add(pwField);
        beforeLoginPanel.add(loginBtn);

        // 로그인 후 패널
        JPanel afterLoginPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        afterLoginPanel.setOpaque(false);

        userContainerLabel = new JLabel();
        userContainerLabel.setForeground(Color.WHITE);
        userContainerLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));

        logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Malgun Gothic", Font.BOLD, 11));
        logoutBtn.setBackground(new Color(70, 73, 75));
        logoutBtn.setForeground(Color.WHITE);

        afterLoginPanel.add(userContainerLabel);
        afterLoginPanel.add(logoutBtn);

        authCardContainer.add(beforeLoginPanel, "GUEST");
        authCardContainer.add(afterLoginPanel, "USER");

        add(authCardContainer, BorderLayout.EAST);

        // 로그인 버튼 이벤트
        loginBtn.addActionListener(e -> {
            String inputId = idField.getText().trim();
            String inputPw = new String(pwField.getPassword()).trim();

            if (inputId.isEmpty() || inputPw.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "ID와 PW를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AccountManager am = mainFrame.getController().getAccountManager();
            boolean loginSuccess = am.login(inputId, inputPw);

            if (loginSuccess) {
                User loggedInUser = am.getCurrentUser();
                Role matchedRole = loggedInUser.getRole();

                userContainerLabel.setText("User: " + loggedInUser.getName() + " [" + matchedRole + "]");
                mainFrame.getController().setCurrentUser(loggedInUser);
                JOptionPane.showMessageDialog(mainFrame, loggedInUser.getName() + "님 환영합니다.", "로그인 성공", JOptionPane.INFORMATION_MESSAGE);

                cardLayout.show(authCardContainer, "USER");
                mainFrame.getController().configureSideMenuByRole();


                if (matchedRole == Role.ADMIN) {
                    mainFrame.changeCenterPanel(new AccountAndProjectManagePanel(mainFrame));
                } else {
                    mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "ID 또는 PW가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 로그아웃 버튼 이벤트
        logoutBtn.addActionListener(e -> {
            mainFrame.getController().setCurrentUser(null);

            idField.setText("");
            pwField.setText("");
            cardLayout.show(authCardContainer, "GUEST");

            mainFrame.getController().configureSideMenuByRole();
            JPanel blankPanel = new JPanel();
            blankPanel.setBackground(Color.WHITE);
            mainFrame.changeCenterPanel(blankPanel);
        });
    }

    public JTextField getIdField() { return idField; }
    public JPasswordField getPwField() { return pwField; }
    public JButton getLoginBtn() { return loginBtn; }
    public JButton getLogoutBtn() { return logoutBtn; }
}