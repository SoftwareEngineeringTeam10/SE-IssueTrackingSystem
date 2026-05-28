package org.view;

import org.controller.IssueController;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.ProjectService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel centerContainer;
    private IssueController controller;
    private SideMenuPanel sideMenuPanel;
    private ProjectService projectService;
    private HeaderPanel headerPanel;
    private JPanel currentCenterPanel;

    public MainFrame(IssueService issueService, AccountManager accountManager) {
        setTitle("이슈 관리 시스템");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.projectService = new ProjectService();

        this.controller = new IssueController(this, issueService, accountManager, this.projectService);

        this.headerPanel = new HeaderPanel(this);
        this.sideMenuPanel = new SideMenuPanel(this);

        add(this.headerPanel, BorderLayout.NORTH);
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
    public HeaderPanel getHeaderPanel() {
        return this.headerPanel;
    }

    // 중앙 패널 교체 및 리렌더링
    public void changeCenterPanel(JPanel newPanel) {
        centerContainer.removeAll();
        centerContainer.add(newPanel, BorderLayout.CENTER);

        this.currentCenterPanel = newPanel;

        if (this.controller != null) {
            this.controller.bindViewEvents(newPanel);
        } else {
            Timer timer = new Timer(50, e -> {
                if (this.getController() != null) {
                    this.getController().bindViewEvents(newPanel);
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        centerContainer.revalidate();
        centerContainer.repaint();
    }

    public JPanel getCenterPanel() {
        return this.currentCenterPanel;
    }


}
