package org;

import javax.swing.*;
import org.controller.*;
import org.issuetracker.service.AccountManager;
import org.view.MainFrame;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.ProjectService;

public class Main {

    public static void main(String[] args) {

        IssueService issueService = new org.issuetracker.service.IssueService();
        AccountManager accountManager = new org.issuetracker.service.AccountManager();
        ProjectService projectService = new org.issuetracker.service.ProjectService();

        MainFrame mainFrame = new MainFrame();

        IssueController issueController = new IssueController(mainFrame, issueService, accountManager, projectService);
        AuthController authController = new AuthController(mainFrame, accountManager, issueController);
        IssueListController issueListController = new IssueListController(mainFrame, issueService, issueController);
        IssueRegisterController issueRegisterController = new IssueRegisterController(mainFrame, issueService, issueController);
        ManageController manageController = new ManageController(mainFrame, projectService, accountManager, issueController);
        IssueDetailController issueDetailController = new IssueDetailController(mainFrame, issueService, accountManager, issueController);

        mainFrame.setControllers(
                issueController,
                authController,
                issueListController,
                issueRegisterController,
                manageController,
                issueDetailController
        );

        issueController.loadProjectsToHeader();


        org.view.LoginPanel loginPanel = new org.view.LoginPanel();
        mainFrame.changeCenterPanel(loginPanel);


        if (mainFrame.getHeaderPanel() != null) {
            authController.bindViewEvents(mainFrame.getHeaderPanel());
        }

        mainFrame.setVisible(true);
    }
}