package org.view;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import org.issuetracker.model.Issue;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.service.StatisticsService;

public class StatsPanel extends JPanel {
    private MainFrame mainFrame;

    private JLabel lblTotalCount, lblNewCount, lblFixedCount, lblClosedCount;
    private JProgressBar progressBar;

    public StatsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel pageTitle = new JLabel("프로젝트 이슈 통계 분석 (Issue Analytics)");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        add(pageTitle, BorderLayout.NORTH);


        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);


        centerContainer.add(Box.createVerticalStrut(20));

        // 대시보드 상단
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // 높이 고정

        lblTotalCount = new JLabel("0");
        lblNewCount = new JLabel("0");
        lblFixedCount = new JLabel("0");
        lblClosedCount = new JLabel("0");

        summaryPanel.add(createStatCard("전체 이슈", lblTotalCount, new Color(70, 130, 180)));
        summaryPanel.add(createStatCard("신규 (NEW)", lblNewCount, new Color(220, 53, 69)));
        summaryPanel.add(createStatCard("해결 (FIXED)", lblFixedCount, new Color(40, 167, 69)));
        summaryPanel.add(createStatCard("종료 (CLOSED)", lblClosedCount, new Color(108, 117, 125)));

        centerContainer.add(summaryPanel);
        centerContainer.add(Box.createVerticalStrut(30));


        // 하단 상세 리포트 구역
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBackground(new Color(248, 249, 250));
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel reportTitle = new JLabel("프로젝트 종합 진척도 (Progress)");
        reportTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        reportPanel.add(reportTitle, BorderLayout.NORTH);

        // 실시간 연동을 위해 프로그레스 바도 멤버 변수로 승격 및 세팅
        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        progressBar.setForeground(new Color(40, 167, 69));
        progressBar.setPreferredSize(new Dimension(0, 30));

        JPanel progressWrapper = new JPanel(new BorderLayout());
        progressWrapper.setOpaque(false);
        progressWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        progressWrapper.add(progressBar, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("※ 본 통계는 현재 선택된 프로젝트의 실시간 정합성 데이터를 기반으로 분석되었습니다.");
        infoLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);
        progressWrapper.add(infoLabel, BorderLayout.SOUTH);

        reportPanel.add(progressWrapper, BorderLayout.CENTER);
        centerContainer.add(reportPanel);

        add(centerContainer, BorderLayout.CENTER);

        //  실물 카운트 가져오기
        updateStatistics();
    }

    public void updateStatistics() {
        try {
            if (mainFrame.getController() == null || mainFrame.getController().getService() == null) {
                return;
            }

            StatisticsService statsService = new StatisticsService();

            Map<IssueStatus, Integer> statusStats = statsService.getStatusStats();


            int newCount = statusStats.getOrDefault(IssueStatus.NEW, 0);
            int fixedCount = statusStats.getOrDefault(IssueStatus.FIXED, 0);
            int closedCount = statusStats.getOrDefault(IssueStatus.CLOSED, 0);


            int totalCount = mainFrame.getController().getService().getAllIssues().size();


            lblTotalCount.setText(String.valueOf(totalCount));
            lblNewCount.setText(String.valueOf(newCount));
            lblFixedCount.setText(String.valueOf(fixedCount));
            lblClosedCount.setText(String.valueOf(closedCount));


            if (totalCount > 0) {
                int completedCount = fixedCount + closedCount;
                int progressPercent = (int) (((double) completedCount / totalCount) * 100);
                progressBar.setValue(progressPercent);
            } else {
                progressBar.setValue(0);
            }

            revalidate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color titleColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 240), 2));

        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

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