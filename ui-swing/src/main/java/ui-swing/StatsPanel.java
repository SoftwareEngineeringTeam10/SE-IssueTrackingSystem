import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {
    private MainFrame mainFrame;

    // 나중에 데이터 갱신을 위해 스코어 보드 레이블들을 멤버 변수로 선언
    private JLabel lblTotalCount, lblNewCount, lblFixedCount, lblClosedCount;

    public StatsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 1. 전체 레이아웃 세팅 (BorderLayout) 및 여백 주기
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 2. 상단 타이틀 배치
        JLabel pageTitle = new JLabel("📊 프로젝트 이슈 통계 분석 (Issue Analytics)");
        pageTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        add(pageTitle, BorderLayout.NORTH);

        // 3. [중앙 영역] 통계 카드들과 세부 지표를 담을 대형 컨테이너
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // 간격 벌리기
        centerContainer.add(Box.createVerticalStrut(20));

        // ---------------------------------------------------------------
        // 📊 (A) 대시보드 상단 4종 스코어 보드 (GridLayout)
        // ---------------------------------------------------------------
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // 높이 고정

        // 더미 데이터 기반 통계 카드 생성
        summaryPanel.add(createStatCard("전체 이슈", "3", new Color(70, 130, 180)));
        summaryPanel.add(createStatCard("신규 (NEW)", "1", new Color(220, 53, 69)));
        summaryPanel.add(createStatCard("해결 (FIXED)", "1", new Color(40, 167, 69)));
        summaryPanel.add(createStatCard("종료 (CLOSED)", "1", new Color(108, 117, 125)));

        centerContainer.add(summaryPanel);
        centerContainer.add(Box.createVerticalStrut(30));

        // ---------------------------------------------------------------
        // 📈 (B) 하단 상세 리포트 구역 (간단한 진척도 매체 표현)
        // ---------------------------------------------------------------
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBackground(new Color(248, 249, 250));
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel reportTitle = new JLabel("프로젝트 종합 진척도 (Progress)");
        reportTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        reportPanel.add(reportTitle, BorderLayout.NORTH);

        // Swing 기본 JProgressBar를 이용해 시각적인 그래프 효과 폰트 내기
        JProgressBar progressBar = new JProgressBar();
        progressBar.setValue(66); // 3개 중 2개 처리 완료 시뮬레이션 (FIXED + CLOSED)
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
    }

    /**
     * 통계 숫자를 예쁘게 시각화해 주는 카드 컴포넌트 생성 도우미
     */
    private JPanel createStatCard(String title, String value, Color titleColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 240), 2));

        // 카드 내 패딩
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        lblTitle.setForeground(titleColor);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Impact", Font.PLAIN, 32)); // 숫자는 묵직하게 폰트 적용
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.SOUTH);

        return card;
    }
}