package org.view;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {
    private MainFrame mainFrame;

    // 오른쪽 펜 자리를 스위칭할 카드 레이아웃 리모컨
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

        // 상단 바 전체 디자인 (검은색 배경, 높이 55)
        setBackground(new Color(43, 43, 43)); // 어두운 챠콜색
        setPreferredSize(new Dimension(0, 55));
        setLayout(new BorderLayout());
        // 양옆에 15픽셀씩 줌으로써 컴포넌트들이 벽에 바짝 붙는 걸 방지
        setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // [상단 좌측] 프로젝트 선택 드롭다운 (와이어프레임 반영)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        leftPanel.setOpaque(false); // 배경 투명하게 해서 검은색 비치게 함

        JLabel projectLabel = new JLabel("Project:");
        projectLabel.setForeground(Color.WHITE);
        projectLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));

        JComboBox<String> projectCombo = new JComboBox<>(new String[]{"Model-Based-ITS", "NextGen-ERP", "SmartFactory-IoT"});
        projectCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));

        leftPanel.add(projectLabel);
        leftPanel.add(projectCombo);
        add(leftPanel, BorderLayout.WEST);

        // [상단 우측]
        // 로그인 전/후 상태 화면을 갈아끼울 카드 컨테이너 생성
        cardLayout = new CardLayout();
        authCardContainer = new JPanel(cardLayout);
        authCardContainer.setOpaque(false);

        // 로그인 전 상태의 미니 폼 구성
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

        // 로그인 후 상태의 유저 정보 구성
        JPanel afterLoginPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        afterLoginPanel.setOpaque(false);

        userContainerLabel = new JLabel("User: 김연욱 [DEV]");
        userContainerLabel.setForeground(Color.WHITE);
        userContainerLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));

        logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Malgun Gothic", Font.BOLD, 11));
        logoutBtn.setBackground(new Color(70, 73, 75));
        logoutBtn.setForeground(Color.WHITE);

        afterLoginPanel.add(userContainerLabel);
        afterLoginPanel.add(logoutBtn);

        // 카드 레이아웃에 두 상태 등록
        authCardContainer.add(beforeLoginPanel, "GUEST");
        authCardContainer.add(afterLoginPanel, "USER");

        add(authCardContainer, BorderLayout.EAST);

        // [임시 이벤트] 버튼 누르면 펜 자리만 바뀌게 연동
        loginBtn.addActionListener(e -> {
            String inputId = idField.getText();
            System.out.println("[Header] 로그인 시도 ID: " + inputId);
            // 나중에 백엔드와 연동하겠지만, 일단 무조건 성공 처리해서 화면 전환 시연!
            cardLayout.show(authCardContainer, "USER");
        });

        logoutBtn.addActionListener(e -> {
            System.out.println("[Header] 로그아웃 버튼 클릭됨");
            idField.setText("");
            pwField.setText("");
            cardLayout.show(authCardContainer, "GUEST");
        });
    }

    // Controller가 리스너를 묶거나 데이터를 조작할 수 있게 Getter 개방
    public JTextField getIdField() { return idField; }
    public JPasswordField getPwField() { return pwField; }
    public JButton getLoginBtn() { return loginBtn; }
    public JButton getLogoutBtn() { return logoutBtn; }
}