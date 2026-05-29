package org.view;

import javax.swing.*;
import java.awt.*;

public class IssueCreatePanel extends JPanel {
    private MainFrame mainFrame;

    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> priorityCombo;
    private JButton btnSubmit, btnCancel;
    private JComboBox<String> comboProject;

    public IssueCreatePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 패널 기본 레이아웃
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel pageTitle = new JLabel("새 이슈 등록");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        add(pageTitle, BorderLayout.NORTH);

        // 입력 폼 구성
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        // 프로젝트
        JLabel lblProject = new JLabel("프로젝트 배정:");
        comboProject = new JComboBox<>();
        comboProject.setBackground(Color.WHITE);

        if (mainFrame != null && mainFrame.getController() != null) {
            org.issuetracker.service.ProjectService projService = mainFrame.getController().getProjectService();
            if (projService != null) {
                for (org.issuetracker.model.Project p : projService.getAllProjects()) {
                    comboProject.addItem(p.id + " : " + p.name);
                }
            }
        }

        // 우선순위
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel priorityLabel = new JLabel("우선순위 : ");
        priorityLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        formPanel.add(priorityLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        priorityCombo = new JComboBox<>(new String[]{"BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL"});
        priorityCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        formPanel.add(priorityCombo, gbc);

        // 제목
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel titleLabel = new JLabel("이슈 제목 : ");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        titleField = new JTextField();
        titleField.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        formPanel.add(titleField, gbc);

        // 상세 내용
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel descLabel = new JLabel("상세 내용 : ");
        descLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        formPanel.add(descLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(10, 30);
        descriptionArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        formPanel.add(descScroll, gbc);

        add(formPanel, BorderLayout.CENTER);

        // 하단 버튼 바
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setOpaque(false);

        btnSubmit = new JButton("등록");
        btnSubmit.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnSubmit.setBackground(new Color(220, 235, 252));

        btnCancel = new JButton("취소");
        btnCancel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnCancel.setBackground(new Color(240, 240, 240));

        bottomPanel.add(btnSubmit);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> {
            mainFrame.changeCenterPanel(new IssueListPanel(mainFrame));
        });

        revalidate();
        repaint();
    }

    // 게터 메서드
    public JTextField getTitleField() { return titleField; }
    public JTextArea getDescriptionArea() { return descriptionArea; }
    public JComboBox<String> getPriorityCombo() { return priorityCombo; }
    public JButton getBtnSubmit() { return btnSubmit; }
    public JButton getBtnCancel() { return btnCancel; }
    public JComboBox<String> getComboProject() { return comboProject; }
}