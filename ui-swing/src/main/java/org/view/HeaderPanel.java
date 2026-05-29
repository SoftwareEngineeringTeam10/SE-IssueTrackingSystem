package org.view;

import javax.swing.*;
import java.awt.*;
import org.issuetracker.model.User;
import org.issuetracker.model.Role;
import org.controller.AuthController;

public class HeaderPanel extends JPanel {
    private MainFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel authCardContainer;
    private JComboBox<String> projectCombo;

    // 로그인 전 컴포넌트
    private JTextField idField;
    private JPasswordField pwField;
    private JButton loginBtn;

    // 로그인 후 컴포넌트
    private JLabel userContainerLabel;
    private JButton logoutBtn;

    public HeaderPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setBackground(new Color(43, 43, 43)); // 어두운 테마 배경
        setPreferredSize(new Dimension(0, 55));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        leftPanel.setOpaque(false);

        JLabel headerTitle = new JLabel("ITS Project");
        headerTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        headerTitle.setForeground(Color.WHITE);
        leftPanel.add(headerTitle);

        JLabel projectLabel = new JLabel("Project:");
        projectLabel.setForeground(Color.WHITE);
        projectLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));

        projectCombo = new JComboBox<>();
        projectCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));

        projectCombo.addActionListener(e -> {
            if (mainFrame.getController() != null) {
                mainFrame.getController().handleProjectSelectionChanged();
            }
        });

        leftPanel.add(projectLabel);
        leftPanel.add(projectCombo);
        add(leftPanel, BorderLayout.WEST);

        // 우측 구역: 로그인/로그아웃 카드 레이아웃
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

        loginBtn.addActionListener(e -> {
            String inputId = idField.getText().trim();
            String inputPw = new String(pwField.getPassword()).trim();

            if (mainFrame != null && mainFrame.getAuthController() != null) {
                AuthController authCtrl = (AuthController) mainFrame.getAuthController();
                authCtrl.handleLogin(inputId, inputPw);
            }
        });

        logoutBtn.addActionListener(e -> {
            if (mainFrame != null && mainFrame.getAuthController() != null) {
                AuthController authCtrl = (AuthController) mainFrame.getAuthController();
                authCtrl.handleLogout();
            }
        });
    }

    public void renderProjectList(java.util.List<org.issuetracker.model.Project> realProjects) {
        projectCombo.removeAllItems();
        if (realProjects == null || realProjects.isEmpty()) {
            projectCombo.addItem("생성된 프로젝트가 없습니다");
        } else {
            for (org.issuetracker.model.Project p : realProjects) {
                projectCombo.addItem(p.id + " : " + p.name);
            }
        }
        projectCombo.revalidate();
        projectCombo.repaint();
    }

    public void setStatusWaiting() {
        projectCombo.removeAllItems();
        projectCombo.addItem("프로젝트 로드 대기중...");
    }

    public void showUserMode(User loggedInUser, Role matchedRole) {
        userContainerLabel.setText("User: " + loggedInUser.getName() + " [" + matchedRole + "]");
        cardLayout.show(authCardContainer, "USER");
    }

    public void showGuestMode() {
        idField.setText("");
        pwField.setText("");
        cardLayout.show(authCardContainer, "GUEST");
    }

    // Getter 선언부
    public JComboBox<String> getProjectCombo() { return projectCombo; }
    public JTextField getIdField() { return idField; }
    public JPasswordField getPwField() { return pwField; }
    public JButton getLoginBtn() { return loginBtn; }
    public JButton getLogoutBtn() { return logoutBtn; }
}