import javax.swing.*;
import java.awt.*;

public class IssueCreatePanel extends JPanel {
    private MainFrame mainFrame;

    // 컨트롤러가 입력된 값을 뜯어가야 하므로 멤버 변수로 선언합니다.
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> priorityCombo;
    private JButton btnSubmit, btnCancel;

    public IssueCreatePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 1. 전체 레이아웃 세팅 (BorderLayout) 및 여백 주기
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 2. 상단 타이틀 배치
        JLabel pageTitle = new JLabel("새 이슈 등록 (Create New Issue)");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        add(pageTitle, BorderLayout.NORTH);

        // 3. 중앙 입력 폼 영역 (GridBagLayout을 써서 깔끔하게 오열 맞추기)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5); // 컴포넌트 간 위아래 간격 설정

        // (A) 우선순위 선택 행
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel priorityLabel = new JLabel("우선순위 (Priority) : ");
        priorityLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        formPanel.add(priorityLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        priorityCombo = new JComboBox<>(new String[]{"BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL"});
        priorityCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        formPanel.add(priorityCombo, gbc);

        // (B) 이슈 제목 입력 행
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel titleLabel = new JLabel("이슈 제목 (Title) : ");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        titleField = new JTextField();
        titleField.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        formPanel.add(titleField, gbc);

        // (C) 이슈 내용 입력 행 (설명 영역은 널찍하게)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST; // 글자 레이블은 위쪽 정렬
        JLabel descLabel = new JLabel("상세 내용 (Description) : ");
        descLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        formPanel.add(descLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1; gbc.weighty = 1; // 세로 공간 꽉 채우기
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(10, 30);
        descriptionArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true); // 우측 끝에 도달하면 자동 줄바꿈
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        formPanel.add(descScroll, gbc);

        add(formPanel, BorderLayout.CENTER);

        // 4. 하단 버튼 영역 (등록 / 취소)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setOpaque(false);

        btnSubmit = new JButton("이슈 등록 완료");
        btnSubmit.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnSubmit.setBackground(new Color(220, 235, 252)); // 연푸른빛 포인트

        btnCancel = new JButton("취소");
        btnCancel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnCancel.setBackground(new Color(240, 240, 240));

        bottomPanel.add(btnSubmit);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        // =======================================================
        // 💡 [임시 이벤트] 버튼 누르면 작동하는 시뮬레이션 로직
        // =======================================================

        // 등록 버튼 누르면 콘솔에 입력값 찍고 다시 목록 화면으로 튕겨내기
        btnSubmit.addActionListener(e -> {
            System.out.println("[Create] --- 새 이슈 등록 요청 ---");
            System.out.println("[Create] 우선순위: " + priorityCombo.getSelectedItem());
            System.out.println("[Create] 제목: " + titleField.getText());
            System.out.println("[Create] 내용: " + descriptionArea.getText());

            JOptionPane.showMessageDialog(mainFrame, "이슈가 정상적으로 등록되었습니다!", "알림", JOptionPane.INFORMATION_MESSAGE);

            // 등록 완료 후 다시 메인 이슈 목록 화면으로 자동 전환
            mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
        });

        // 취소 버튼 누르면 그냥 목록으로 튕겨내기
        btnCancel.addActionListener(e -> {
            System.out.println("[Create] 이슈 등록 취소됨");
            mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
        });
    }

    // ⭐ Controller가 입력폼 데이터를 싹 긁어갈 수 있도록 셔터 개방 (Getter)
    public JTextField getTitleField() { return titleField; }
    public JTextArea getDescriptionArea() { return descriptionArea; }
    public JComboBox<String> getPriorityCombo() { return priorityCombo; }
    public JButton getBtnSubmit() { return btnSubmit; }
    public JButton getBtnCancel() { return btnCancel; }
}