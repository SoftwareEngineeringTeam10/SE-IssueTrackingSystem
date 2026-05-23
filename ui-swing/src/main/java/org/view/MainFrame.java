package org.view;

import org.controller.IssueController;
import org.issuetracker.service.IssueService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    // 나중에 컨트롤러가 화면 전환을 제어할 수 있도록 중앙 컨테이너를 보관합니다.
    private JPanel centerContainer;
    private IssueController controller;
    private SideMenuPanel sideMenuPanel;

    public MainFrame(IssueService issueService) {
        // 1. 전체 창틀의 기본적인 설정
        setTitle("이슈 관리 시스템 (ITS) - Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        this.controller = new IssueController(this, issueService);

        // 2. 조각 부품 패널들 생성
        HeaderPanel headerPanel = new HeaderPanel(this);
        this.sideMenuPanel = new SideMenuPanel(this);

        // 3. 생성한 고정 부품들을 창틀 제자리에 붙이기
        add(headerPanel, BorderLayout.NORTH);
        add(sideMenuPanel, BorderLayout.WEST);

        // 4. 중앙 영역 빈 컨테이너 상자 짜두기
        centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Color.WHITE);
        add(centerContainer, BorderLayout.CENTER);

        // 5. 프로그램을 처음 켰을 때 중앙에 가장 먼저 뜰 화면 주입
        changeCenterPanel(new IssueListPanel(this));

        // 모든 부품이 프레임에 완전히 add되고 초기 화면까지 주입된 후,
        // 생성자 맨 마지막 줄에서 호출해야 숨김 처리가 정상적으로 화면에 반영됩니다.
        this.controller.configureSideMenuByRole();
    }

    public SideMenuPanel getSideMenuPanel() {
        return sideMenuPanel;
    }

    /**
     * 시스템의 핵심 리모컨 함수
     * 좌측 메뉴를 누르거나 테이블을 더블클릭했을 때, 이 함수에 새 패널만 던져주면
     * 상단바와 좌측메뉴는 가만히 있고 중앙 화면만 갈아끼워진다.
     */


    public void changeCenterPanel(JPanel newPanel) {
        centerContainer.removeAll();      // 기존에 중앙에 있던 화면(테이블 등)을 걷어내고
        centerContainer.add(newPanel, BorderLayout.CENTER); // 새 화면을 쑤셔 넣은 뒤

        // 픽셀을 다시 그려줍니다.

        // 화면이 바뀔 때마다 Controller에게 새 패널을 토스해서 이벤트를 묶고 버튼을 제어하게 만든다.
        if (controller != null) {
            controller.bindViewEvents(newPanel);
        }

        centerContainer.revalidate();     // 구조를 새로고침
        centerContainer.repaint();


    }

}