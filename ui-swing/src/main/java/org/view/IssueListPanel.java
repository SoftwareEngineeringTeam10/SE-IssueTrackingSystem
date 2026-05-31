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
    private JComboBox<String> comboReporterFilter;
    private JComboBox<String> comboAssigneeFilter;
    private JButton btnSearch;
    private String currentProjectId = "";

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
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBarPanel.setOpaque(false);
        searchBarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchBarPanel.add(new JLabel("검색어:"));
        fieldSearch = new JTextField(10);
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

        searchBarPanel.add(new JLabel("보고자:"));
        comboReporterFilter = new JComboBox<>();
        comboReporterFilter.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        searchBarPanel.add(comboReporterFilter);

        searchBarPanel.add(new JLabel("담당자:"));
        comboAssigneeFilter = new JComboBox<>();
        comboAssigneeFilter.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        searchBarPanel.add(comboAssigneeFilter);

        btnSearch = new JButton("검색");
        btnSearch.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnSearch.setBackground(new Color(240, 240, 240));
        searchBarPanel.add(btnSearch);

        topContainer.add(searchBarPanel);
        topContainer.add(Box.createVerticalStrut(15));

        add(topContainer, BorderLayout.NORTH);

        String[] columnNames = {"번호", "이슈 제목", "우선순위", "상태", "보고자", "담당자", "보고일"};

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


        updateUserFilters();
        loadIssues();
    }

    public void updateUserFilters() {
        try {
            if (mainFrame == null || mainFrame.getController() == null || mainFrame.getController().getAccountManager() == null) {
                comboReporterFilter.setModel(new DefaultComboBoxModel<>(new String[]{"전체"}));
                comboAssigneeFilter.setModel(new DefaultComboBoxModel<>(new String[]{"전체"}));
                return;
            }

            org.issuetracker.service.AccountManager accountManager = mainFrame.getController().getAccountManager();
            java.util.List<User> allUsers = accountManager.getUsers();

            DefaultComboBoxModel<String> reporterModel = new DefaultComboBoxModel<>();
            DefaultComboBoxModel<String> assigneeModel = new DefaultComboBoxModel<>();

            reporterModel.addElement("전체");
            assigneeModel.addElement("전체");

            if (allUsers != null) {
                for (User user : allUsers) {
                    String userId = user.getId();
                    reporterModel.addElement(userId);
                    assigneeModel.addElement(userId);
                }
            }

            comboReporterFilter.setModel(reporterModel);
            comboAssigneeFilter.setModel(assigneeModel);

        } catch (Exception e) {
            e.printStackTrace();
            comboReporterFilter.setModel(new DefaultComboBoxModel<>(new String[]{"전체"}));
            comboAssigneeFilter.setModel(new DefaultComboBoxModel<>(new String[]{"전체"}));
        }
    }

    public void setCurrentProjectId(String currentProjectId) {
        this.currentProjectId = currentProjectId;
    }

    public void loadIssues() {
        try {
            if (tableModel != null) {
                tableModel.setRowCount(0);
            }

            int projectIdInt = -1;
            if (mainFrame != null && mainFrame.getHeaderPanel() != null) {
                JComboBox<String> combo = mainFrame.getHeaderPanel().getProjectCombo();
                if (combo != null && combo.getSelectedItem() != null) {
                    String selectedProj = (String) combo.getSelectedItem();
                    if (selectedProj.contains(" : ")) {
                        try {
                            projectIdInt = Integer.parseInt(selectedProj.split(" : ")[0].trim());
                        } catch (NumberFormatException nfe) {
                            projectIdInt = -1;
                        }
                    }
                }
            }

            if (projectIdInt == -1) return;

            String selectedStatus = null;
            if (comboStatusFilter != null && comboStatusFilter.getSelectedItem() != null) {
                String status = (String) comboStatusFilter.getSelectedItem();
                if (!"전체".equals(status) && !status.trim().isEmpty()) {
                    selectedStatus = status.trim();
                }
            }

            String selectedPriority = null;
            if (comboPriorityFilter != null && comboPriorityFilter.getSelectedItem() != null) {
                String priority = (String) comboPriorityFilter.getSelectedItem();
                if (!"전체".equals(priority) && !priority.trim().isEmpty()) {
                    selectedPriority = priority.trim();
                }
            }

            String selectedReporter = null;
            if (comboReporterFilter != null && comboReporterFilter.getSelectedItem() != null) {
                String reporter = comboReporterFilter.getSelectedItem().toString().trim();
                if (!"전체".equals(reporter) && !reporter.isEmpty()) {
                    selectedReporter = reporter;
                }
            }

            String selectedAssignee = null;
            if (comboAssigneeFilter != null && comboAssigneeFilter.getSelectedItem() != null) {
                String assignee = comboAssigneeFilter.getSelectedItem().toString().trim();
                if (!"전체".equals(assignee) && !assignee.isEmpty()) {
                    selectedAssignee = assignee;
                }
            }

            String searchKeyword = null;
            if (fieldSearch != null && !fieldSearch.getText().trim().isEmpty()) {
                searchKeyword = fieldSearch.getText().trim();
            }

            if (mainFrame != null && mainFrame.getController() != null && mainFrame.getController().getIssueService() != null) {

                java.util.List<org.issuetracker.model.Issue> issues =
                        mainFrame.getController().getIssueService().searchIssues(
                                projectIdInt,
                                selectedStatus,
                                null,
                                null,
                                searchKeyword
                        );

                if (issues != null) {
                    for (org.issuetracker.model.Issue issue : issues) {

                        if (selectedPriority != null) {
                            if (issue.priority == null || !issue.priority.name().equalsIgnoreCase(selectedPriority)) {
                                continue;
                            }
                        }

                        if (selectedReporter != null) {
                            if (issue.reporter == null || !issue.reporter.equalsIgnoreCase(selectedReporter)) {
                                continue;
                            }
                        }

                        if (selectedAssignee != null) {
                            if (issue.assignee == null || !issue.assignee.equalsIgnoreCase(selectedAssignee)) {
                                continue;
                            }
                        }

                        Object[] row = {
                                issue.id,
                                issue.title,
                                issue.priority != null ? issue.priority.name() : "-",
                                issue.status != null ? issue.status.name() : "NEW",
                                issue.reporter != null ? issue.reporter : "-",
                                issue.assignee != null ? issue.assignee : "-",
                                issue.reportedDate != null ? issue.reportedDate : "-"
                        };
                        tableModel.addRow(row);
                    }
                }
            }

            if (tableModel != null) {
                tableModel.fireTableDataChanged();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "이슈 필터링 렌더링 중 오류 발생", "시스템 에러", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getter 목록
    public JTable getIssueTable() { return issueTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnResolve() { return btnResolve; }
    public JButton getBtnClose() { return btnClose; }
    public JTextField getFieldSearch() { return fieldSearch; }
    public JComboBox<String> getComboStatusFilter() { return comboStatusFilter; }
    public JComboBox<String> getComboPriorityFilter() { return comboPriorityFilter; }
    public JComboBox<String> getComboReporterFilter() { return comboReporterFilter; }
    public JComboBox<String> getComboAssigneeFilter() { return comboAssigneeFilter; }
    public JButton getBtnSearch() { return btnSearch; }
}