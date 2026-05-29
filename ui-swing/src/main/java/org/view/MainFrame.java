package org.view;

import org.controller.*;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.ProjectService;


import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel centerContainer;
    private SideMenuPanel sideMenuPanel;
    private ProjectService projectService;
    private HeaderPanel headerPanel;
    private JPanel currentCenterPanel;
    private AuthController authController;
    private IssueListController issueListController;
    private IssueRegisterController issueRegisterController;
    private ManageController manageController;
    private IssueDetailController issueDetailController;


    private IssueController issueController;

    public MainFrame() {
        setTitle("이슈 관리 시스템");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.projectService = new ProjectService();

        this.headerPanel = new HeaderPanel(this);
        this.sideMenuPanel = new SideMenuPanel(this);

        add(this.headerPanel, BorderLayout.NORTH);
        add(sideMenuPanel, BorderLayout.WEST);

        centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Color.WHITE);
        add(centerContainer, BorderLayout.CENTER);


        currentCenterPanel = new JPanel();
        centerContainer.add(currentCenterPanel, BorderLayout.CENTER);
    }

    public void setControllers(IssueController issueCtrl, AuthController auth, IssueListController list,
                               IssueRegisterController reg, ManageController manage, IssueDetailController detail) {
        this.issueController = issueCtrl;
        this.authController = auth;
        this.issueListController = list;
        this.issueRegisterController = reg;
        this.manageController = manage;
        this.issueDetailController = detail;

        if (this.issueController != null) {
            this.issueController.configureSideMenuByRole();
        }

        if (this.headerPanel != null && this.authController != null) {
            this.authController.bindViewEvents(this.headerPanel);
        }
    }

    public SideMenuPanel getSideMenuPanel() {
        return sideMenuPanel;
    }

    public IssueController getController() {
        return this.issueController;
    }

    public HeaderPanel getHeaderPanel() {
        return this.headerPanel;
    }

    public void changeCenterPanel(JPanel newPanel) {
        centerContainer.removeAll();
        centerContainer.add(newPanel, BorderLayout.CENTER);

        this.currentCenterPanel = newPanel;

        if (newPanel instanceof LoginPanel && authController != null) {
            authController.bindViewEvents(newPanel);
        }
        else if (newPanel instanceof IssueListPanel && issueListController != null) {
            issueListController.bindViewEvents(newPanel);
            ((IssueListPanel) newPanel).loadIssues();
        }
        else if (newPanel instanceof IssueCreatePanel && issueRegisterController != null) {
            issueRegisterController.bindViewEvents(newPanel);
        }
        else if (newPanel instanceof AccountAndProjectManagePanel && manageController != null) {
            manageController.bindViewEvents(newPanel);
        }
        else if (newPanel instanceof IssueDetailPanel && issueDetailController != null) {
            issueDetailController.bindViewEvents(newPanel);
        }

        centerContainer.revalidate();
        centerContainer.repaint();
    }

    public JPanel getCenterPanel() {
        return this.currentCenterPanel;
    }

    public AuthController getAuthController() {
        return this.authController;
    }
}