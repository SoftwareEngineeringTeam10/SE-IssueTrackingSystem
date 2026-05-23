package org.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class IssueListPanel extends JPanel {
    private MainFrame mainFrame;
    private JTable issueTable;
    private DefaultTableModel tableModel;
    private JButton btnResolve, btnClose;

    // 🔍 [신규 추가] 검색 및 필터 컴포넌트 멤버 변수
    private JTextField fieldSearch;
    private JComboBox<String> comboStatusFilter;
    private JComboBox<String> comboPriorityFilter;
    private JButton btnSearch;

    public IssueListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 1. 메인 레이아웃 및 여백 세팅
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---------------------------------------------------------------
        // [신규 추가] 상단 타이틀 + 검색/필터 복합 패널 (NORTH)
        // ---------------------------------------------------------------
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // (A) 타이틀 레이블
        JLabel listTitle = new JLabel("이슈 목록 (Issue List)");
        listTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        listTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topContainer.add(listTitle);
        topContainer.add(Box.createVerticalStrut(15)); // 타이틀과 검색바 사이 간격

        // 검색 및 필터 컨트롤 바 (FlowLayout)
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBarPanel.setOpaque(false);
        searchBarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 검색어 입력
        searchBarPanel.add(new JLabel("검색어:"));
        fieldSearch = new JTextField(15);
        fieldSearch.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        searchBarPanel.add(fieldSearch);

        // 상태 필터 (명세 정합용)
        searchBarPanel.add(new JLabel("상태:"));
        comboStatusFilter = new JComboBox<>(new String[]{"전체", "NEW", "ASSIGNED", "FIXED", "RESOLVED", "CLOSED", "REOPENED"});
        comboStatusFilter.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        searchBarPanel.add(comboStatusFilter);

        // 우선순위 필터
        searchBarPanel.add(new JLabel("우선순위:"));
        comboPriorityFilter = new JComboBox<>(new String[]{"전체", "BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL"});
        comboPriorityFilter.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        searchBarPanel.add(comboPriorityFilter);

        // 검색 버튼
        btnSearch = new JButton("검색 🔍");
        btnSearch.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnSearch.setBackground(new Color(240, 240, 240));
        searchBarPanel.add(btnSearch);

        topContainer.add(searchBarPanel);
        topContainer.add(Box.createVerticalStrut(15)); // 검색바와 테이블 사이 간격

        add(topContainer, BorderLayout.NORTH);


        // ---------------------------------------------------------------
        // JTable 컬럼 구성 및 더미 데이터 (CENTER)
        // ---------------------------------------------------------------
        String[] columnNames = {"번호", "이슈 제목", "우선순위", "상태", "보고자", "담당자"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 더블클릭 시 텍스트 수정 방지
            }
        };

        // 시나리오 흐름에 맞춰 더미 데이터 상태값 세팅
        tableModel.addRow(new Object[]{1, "로그인 버튼 클릭 시 NullPointerException 발생", "BLOCKER", "NEW", "tester1", "-"});
        tableModel.addRow(new Object[]{2, "IssueRepository 파일 저장 로직 정합성 오류", "CRITICAL", "ASSIGNED", "tester1", "dev1"});
        tableModel.addRow(new Object[]{3, "UI BorderLayout 서측 메뉴 내비게이션 활성화 버그", "MAJOR", "FIXED", "tester1", "dev1"});

        issueTable = new JTable(tableModel);
        issueTable.setRowHeight(32);
        issueTable.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        issueTable.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(issueTable);
        add(scrollPane, BorderLayout.CENTER);


        // ---------------------------------------------------------------
        // 하단 상태 변경 버튼 패널 (SOUTH)
        // ---------------------------------------------------------------
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomButtonPanel.setOpaque(false);

        btnResolve = new JButton("Resolve Issue (FIXED)");
        btnResolve.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnResolve.setBackground(new Color(220, 235, 252));

        btnClose = new JButton("Close Issue (CLOSED)");
        btnClose.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnClose.setBackground(new Color(240, 240, 240));

        bottomButtonPanel.add(btnResolve);
        bottomButtonPanel.add(btnClose);
        add(bottomButtonPanel, BorderLayout.SOUTH);


        // ---------------------------------------------------------------
        // 테이블 더블클릭 -> 상세 페이지 화면 전환 이벤트
        // ---------------------------------------------------------------
        issueTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int selectedRow = issueTable.getSelectedRow();
                    if (selectedRow != -1) {
                        System.out.println("[List] " + (selectedRow + 1) + "번 이슈 더블클릭 -> 상세화면 이동");
                        mainFrame.changeCenterPanel(new IssueDetailPanel(mainFrame));
                    }
                }
            }
        });
    }

    // Controller 연동용 Getter 개방
    public JTable getIssueTable() { return issueTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnResolve() { return btnResolve; }
    public JButton getBtnClose() { return btnClose; }

    // 검색 필터 값 수집용
    public JTextField getFieldSearch() { return fieldSearch; }
    public JComboBox<String> getComboStatusFilter() { return comboStatusFilter; }
    public JComboBox<String> getComboPriorityFilter() { return comboPriorityFilter; }
    public JButton getBtnSearch() { return btnSearch; }
}