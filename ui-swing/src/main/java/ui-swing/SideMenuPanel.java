import javax.swing.*;
import java.awt.*;

public class SideMenuPanel extends JPanel {
    private MainFrame mainFrame;

    // 컨트롤러가 직접 가리고 켜야 하므로 멤버 변수로 확실하게 선언
    private JButton btnAdminManage;
    private JButton btnList;
    private JButton btnCreate;
    private JButton btnStats;

    public SideMenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 위에서 아래로 세로로 정렬하는 Layout 세팅
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(245, 247, 250));

        // 우측에만 연한 회색 테두리선 배치
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(218, 224, 233)));
        setPreferredSize(new Dimension(180, 700));

        // 내부 안쪽 여백 세팅 (상20, 좌15, 하20, 우15)
        setBorder(BorderFactory.createCompoundBorder(
                getBorder(),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        // 상단 가이드 텍스트
        JLabel lblNav = new JLabel("NAVIGATION");
        lblNav.setFont(new Font("Malgun Gothic", Font.BOLD, 11));
        lblNav.setForeground(new Color(140, 150, 170));
        lblNav.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblNav);
        add(Box.createVerticalStrut(20)); // 위아래 간격 조절

        // 1. 관리자 전용 메뉴 버튼 생성 및 부착
        btnAdminManage = createMenuButton("계정/프로젝트 관리");
        add(btnAdminManage);
        add(Box.createVerticalStrut(10));

        // 2. 이슈 목록 버튼 생성 및 부착
        btnList = createMenuButton("이슈 목록");
        add(btnList);
        add(Box.createVerticalStrut(10));

        // 3. 이슈 등록 버튼 생성 및 부착
        btnCreate = createMenuButton("이슈 등록");
        add(btnCreate);
        add(Box.createVerticalStrut(10));

        // 4. 통계 분석 버튼 생성 및 부착
        btnStats = createMenuButton("통계 분석");
        add(btnStats);

        // 아래 남은 여백을 채워 버튼들을 위로 밀어주는 글루 고정
        add(Box.createVerticalGlue());

        // 버튼 클릭 시 중앙 화면 교체 처리 리스너 연동
        btnAdminManage.addActionListener(e -> mainFrame.changeCenterPanel(new AccountAndProjectManagePanel(mainFrame)));
        btnList.addActionListener(e -> mainFrame.changeCenterPanel(new IssueListPanel(mainFrame)));
        btnCreate.addActionListener(e -> mainFrame.changeCenterPanel(new IssueCreatePanel(mainFrame)));
        btnStats.addActionListener(e -> JOptionPane.showMessageDialog(mainFrame, "통계 분석 패널 준비 중", "알림", JOptionPane.INFORMATION_MESSAGE));
    }

    /**
     * 규격화된 사이드 메뉴 버튼 양식을 찍어내는 헬퍼 메서드
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        button.setForeground(new Color(70, 80, 95));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);

        // BoxLayout에서 가로 크기가 제각각으로 깨지는 현상을 막기 위한 가이드라인 고정
        button.setMaximumSize(new Dimension(150, 40));
        button.setPreferredSize(new Dimension(150, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 1));
        return button;
    }

    // 컨트롤러 연동용 Getter 세트
    public JButton getBtnAdminManage() { return btnAdminManage; }
    public JButton getBtnList() { return btnList; }
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnStats() { return btnStats; }
}