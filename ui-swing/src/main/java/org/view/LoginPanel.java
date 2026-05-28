package org.view;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField fieldId;
    private JPasswordField fieldPassword;
    private JButton btnLogin;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel boxPanel = new JPanel(new GridBagLayout());
        boxPanel.setBackground(new Color(248, 249, 250));
        boxPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        JLabel lblTitle = new JLabel("이슈 관리 시스템 로그인", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        boxPanel.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        JLabel lblId = new JLabel("아이디:");
        lblId.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 1;
        boxPanel.add(lblId, gbc);

        fieldId = new JTextField(15);
        fieldId.setPreferredSize(new Dimension(0, 28));
        gbc.gridx = 1; gbc.gridy = 1;
        boxPanel.add(fieldId, gbc);

        JLabel lblPw = new JLabel("비밀번호:");
        lblPw.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 2;
        boxPanel.add(lblPw, gbc);

        fieldPassword = new JPasswordField(15);
        fieldPassword.setPreferredSize(new Dimension(0, 28));
        gbc.gridx = 1; gbc.gridy = 2;
        boxPanel.add(fieldPassword, gbc);


        btnLogin = new JButton("로그인");
        btnLogin.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        btnLogin.setBackground(new Color(70, 130, 180)); // 신뢰감을 주는 스틸 블루 테마
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(0, 35));

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        boxPanel.add(btnLogin, gbc);

        add(boxPanel, new GridBagConstraints());
    }

    public JTextField getFieldId() {
        return fieldId;
    }

    public JPasswordField getFieldPassword() {
        return fieldPassword;
    }

    public JButton getBtnLogin() {
        return btnLogin;
    }
}