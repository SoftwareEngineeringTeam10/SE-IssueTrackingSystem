package org.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import org.issuetracker.model.User;
import org.issuetracker.model.Role;

public class IssueListPanel extends JPanel {
    private MainFrame mainFrame;
    private JTable issueTable;
    private DefaultTableModel tableModel;
    private JButton btnResolve, btnClose;

    private JTextField fieldSearch;
    private JComboBox<String> comboStatusFilter;
    private JComboBox<String> comboPriorityFilter;
    private JButton btnSearch;

    public IssueListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상단 컨테이너
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JLabel listTitle = new JLabel("이슈 목록");
        listTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        listTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topContainer.add(listTitle);
        topContainer.add(Box.createVerticalStrut(15));

        // 검색 바
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBarPanel.setOpaque(false);
        searchBarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchBarPanel.add(new JLabel("검색어:"));
        fieldSearch = new JTextField(15);
        fieldSearch.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        searchBarPanel.add(fieldSearch);

        searchBarPanel.add(new JLabel("상태:"));
        comboStatusFilter = new JComboBox<>(new String[]{"전체", "NEW", "ASSIGNED", "FIXED", "RESOLVED", "CLOSED", "REOPENED"});
        comboStatusFilter.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        searchBarPanel.add(comboStatusFilter);

        searchBarPanel.add(new JLabel("우선순위:"));
        comboPriorityFilter = new JComboBox<>(new String[]{"전체", "BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL"});
        comboPriorityFilter.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        searchBarPanel.add(comboPriorityFilter);

        btnSearch = new JButton("검색");
        btnSearch.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnSearch.setBackground(new Color(240, 240, 240));
        searchBarPanel.add(btnSearch);

        topContainer.add(searchBarPanel);
        topContainer.add(Box.createVerticalStrut(15));

        add(topContainer, BorderLayout.NORTH);

        // 테이블 설정
        String[] columnNames = {"번호", "이슈 제목", "우선순위", "상태", "보고자", "담당자"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        issueTable = new JTable(tableModel);
        issueTable.setRowHeight(32);
        issueTable.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));
        issueTable.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(issueTable);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 패널
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

        // 데이터 로드
        loadIssues();

        // 생성 완료 후 컨트롤러에게 리스너 제어권 강제 위임
        if (mainFrame.getController() != null) {
            mainFrame.getController().bindViewEvents(this);
        }
    }

    public void loadIssues() {
        tableModel.setRowCount(0);

        if (mainFrame.getController() == null || mainFrame.getController().getService() == null) {
            return;
        }

        java.util.List<org.issuetracker.model.Issue> issues = mainFrame.getController().getService().getAllIssues();
        if (issues == null) return;

        User currentUser = mainFrame.getController().getCurrentUser();

        for (org.issuetracker.model.Issue issue : issues) {
            // 개발자 권한 필터링
            if (currentUser != null && currentUser.getRole() == Role.DEV) {
                if (issue.assigneeId == null || !issue.assigneeId.equals(currentUser.getId())) {
                    continue;
                }
            }

            Object[] row = {
                    issue.id,
                    issue.title,
                    issue.priority != null ? issue.priority.name() : "-",
                    issue.status != null ? issue.status.name() : "NEW",
                    issue.reporterId != null ? issue.reporterId : "-",
                    issue.assigneeId != null ? issue.assigneeId : "-"
            };
            tableModel.addRow(row);
        }

        tableModel.fireTableDataChanged();

        revalidate();
        repaint();
    }

    // Getter 목록
    public JTable getIssueTable() { return issueTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnResolve() { return btnResolve; }
    public JButton getBtnClose() { return btnClose; }
    public JTextField getFieldSearch() { return fieldSearch; }
    public JComboBox<String> getComboStatusFilter() { return comboStatusFilter; }
    public JComboBox<String> getComboPriorityFilter() { return comboPriorityFilter; }
    public JButton getBtnSearch() { return btnSearch; }
}