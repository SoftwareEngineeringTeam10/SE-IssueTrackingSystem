package org.view;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.service.StatisticsService;

public class StatsPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel lblTotalCount, lblNewCount, lblFixedCount, lblClosedCount;
    private JPanel pnlDailyReport, pnlMonthlyReport, pnlStatusReport;

    public StatsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel pageTitle = new JLabel("시스템 통합 이슈 통계 분석 (Total Issue Analytics)");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        add(pageTitle, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.HORIZONTAL;
        mainGbc.weightx = 1.0;
        mainGbc.gridx = 0;

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(0, 80));

        lblTotalCount = new JLabel("0");
        lblNewCount = new JLabel("0");
        lblFixedCount = new JLabel("0");
        lblClosedCount = new JLabel("0");

        summaryPanel.add(createStatCard("전체 이슈", lblTotalCount, new Color(70, 130, 180)));
        summaryPanel.add(createStatCard("신규 (NEW)", lblNewCount, new Color(220, 53, 69)));
        summaryPanel.add(createStatCard("해결 (FIXED)", lblFixedCount, new Color(40, 167, 69)));
        summaryPanel.add(createStatCard("종료 (CLOSED)", lblClosedCount, new Color(108, 117, 125)));

        mainGbc.gridy = 0;
        mainGbc.insets = new Insets(20, 0, 20, 0);
        centerContainer.add(summaryPanel, mainGbc);

        pnlDailyReport = createReportSection("일별 이슈 등록 현황");
        mainGbc.gridy = 1;
        mainGbc.insets = new Insets(0, 0, 20, 0);
        centerContainer.add(pnlDailyReport, mainGbc);

        pnlMonthlyReport = createReportSection("월별 이슈 등록 현황");
        mainGbc.gridy = 2;
        centerContainer.add(pnlMonthlyReport, mainGbc);

        pnlStatusReport = createReportSection("상태별 이슈 분포 현황");
        mainGbc.gridy = 3;
        centerContainer.add(pnlStatusReport, mainGbc);

        mainGbc.gridy = 4;
        mainGbc.weighty = 1.0;
        mainGbc.fill = GridBagConstraints.BOTH;
        centerContainer.add(Box.createGlue(), mainGbc);

        JScrollPane scrollPane = new JScrollPane(centerContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        updateStatistics();
    }

    private JPanel createReportSection(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 0);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        panel.add(lblTitle, c);

        return panel;
    }

    private JPanel createChartBarRow(String labelText, int count, int maxCount) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(100, 25));
        label.setMinimumSize(new Dimension(100, 25));

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(3, 5, 3, 15);
        row.add(label, gbc);

        JProgressBar bar = new JProgressBar(0, maxCount == 0 ? 10 : maxCount);
        bar.setValue(count);
        bar.setStringPainted(true);
        bar.setString(count + "건");
        bar.setForeground(new Color(235, 104, 65));
        bar.setBackground(new Color(232, 234, 237));
        bar.setBorder(null);
        bar.setPreferredSize(new Dimension(150, 22));

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(3, 0, 3, 5);
        row.add(bar, gbc);

        return row;
    }

    public void updateStatistics() {
        try {
            StatisticsService statsService = new StatisticsService();

            int projectId = 888; // 콤보박스가 비어있거나 대기 중일 때를 대비

            if (mainFrame != null && mainFrame.getHeaderPanel() != null && mainFrame.getHeaderPanel().getProjectCombo() != null) {
                String selected = (String) mainFrame.getHeaderPanel().getProjectCombo().getSelectedItem();

                if (selected != null && !selected.contains("없습니다") && !selected.contains("대기중")) {
                    String currentProjectIdStr = selected.split(":")[0].trim();
                    projectId = Integer.parseInt(currentProjectIdStr); // "888" -> 888
                }
            }

            Map<String, Integer> dailyStats = statsService.getDailyStats(projectId);
            Map<String, Integer> monthlyStats = statsService.getMonthlyStats(projectId);
            Map<IssueStatus, Integer> statusStats = statsService.getStatusStats(projectId);

            int newCount = statusStats.getOrDefault(IssueStatus.NEW, 0);
            int fixedCount = statusStats.getOrDefault(IssueStatus.FIXED, 0);
            int closedCount = statusStats.getOrDefault(IssueStatus.CLOSED, 0);

            int totalCount = statusStats.values().stream().mapToInt(Integer::intValue).sum();

            lblTotalCount.setText(String.valueOf(totalCount));
            lblNewCount.setText(String.valueOf(newCount));
            lblFixedCount.setText(String.valueOf(fixedCount));
            lblClosedCount.setText(String.valueOf(closedCount));

            clearComponentExceptTitle(pnlDailyReport);
            int maxDaily = dailyStats.values().stream().mapToInt(Integer::intValue).max().orElse(10);
            int[] dRowIndex = {1};
            dailyStats.forEach((date, count) -> {
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; c.gridx = 0; c.gridy = dRowIndex[0]++;
                pnlDailyReport.add(createChartBarRow(date, count, maxDaily), c);
            });

            clearComponentExceptTitle(pnlMonthlyReport);
            int maxMonthly = monthlyStats.values().stream().mapToInt(Integer::intValue).max().orElse(10);
            int[] mRowIndex = {1};
            monthlyStats.forEach((month, count) -> {
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; c.gridx = 0; c.gridy = mRowIndex[0]++;
                pnlMonthlyReport.add(createChartBarRow(month, count, maxMonthly), c);
            });

            clearComponentExceptTitle(pnlStatusReport);
            int maxStatus = statusStats.values().stream().mapToInt(Integer::intValue).max().orElse(10);
            int[] sRowIndex = {1};
            statusStats.forEach((status, count) -> {
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; c.gridx = 0; c.gridy = sRowIndex[0]++;
                pnlStatusReport.add(createChartBarRow(status.name(), count, maxStatus), c);
            });

            revalidate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearComponentExceptTitle(JPanel panel) {
        Component[] comps = panel.getComponents();
        for (Component c : comps) {
            if (!(c instanceof JLabel)) {
                panel.remove(c);
            }
        }
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color titleColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 240), 2));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(), BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        lblTitle.setForeground(titleColor);

        lblValue.setFont(new Font("Impact", Font.PLAIN, 32));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.SOUTH);

        return card;
    }
}