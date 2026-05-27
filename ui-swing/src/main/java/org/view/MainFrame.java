package org.view;

import org.controller.IssueController;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.IssueService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel centerContainer;
    private IssueController controller;
    private SideMenuPanel sideMenuPanel;

    public MainFrame(IssueService issueService, AccountManager accountManager) {
        setTitle("이슈 관리 시스템");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.controller = new IssueController(this, issueService, accountManager);

        HeaderPanel headerPanel = new HeaderPanel(this);
        this.sideMenuPanel = new SideMenuPanel(this);

        add(headerPanel, BorderLayout.NORTH);
        add(sideMenuPanel, BorderLayout.WEST);

        centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Color.WHITE);
        add(centerContainer, BorderLayout.CENTER);

        // 초기 화면 주입
        changeCenterPanel(new IssueListPanel(this));

        this.controller.configureSideMenuByRole();
    }

    public SideMenuPanel getSideMenuPanel() {
        return sideMenuPanel;
    }

    public IssueController getController() {
        return this.controller;
    }

    // 중앙 패널 교체 및 리렌더링
    public void changeCenterPanel(JPanel newPanel) {
        centerContainer.removeAll();
        centerContainer.add(newPanel, BorderLayout.CENTER);

        centerContainer.revalidate();
        centerContainer.repaint();
    }
}