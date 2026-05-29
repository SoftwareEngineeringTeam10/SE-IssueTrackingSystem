package org.controller;

import org.issuetracker.model.*;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.ProjectService;
import org.view.IssueListPanel;
import org.view.SideMenuPanel;
import org.view.MainFrame;
import javax.swing.*;

public class IssueController {
    private MainFrame mainFrame;
    private final AccountManager accountManager;
    private final IssueService issueService;
    private final ProjectService projectService;
    private User currentUser;

    public IssueController(MainFrame mainFrame, IssueService issueService, AccountManager accountManager, ProjectService projectService) {
        this.mainFrame = mainFrame;
        this.issueService = issueService;
        this.accountManager = accountManager;
        this.projectService = projectService;
    }

    public void loadProjectsToHeader() {
        if (mainFrame == null || mainFrame.getHeaderPanel() == null) return;

        if (projectService != null) {
            java.util.List<Project> realProjects = projectService.getAllProjects();
            mainFrame.getHeaderPanel().renderProjectList(realProjects);
        } else {
            mainFrame.getHeaderPanel().setStatusWaiting();
        }
    }

    public void handleProjectSelectionChanged() {
        if (mainFrame == null || mainFrame.getHeaderPanel() == null) return;

        String selected = (String) mainFrame.getHeaderPanel().getProjectCombo().getSelectedItem();
        if (selected != null && !selected.contains("없습니다") && !selected.contains("대기중")) {
            try {
                String currentProjectId = selected.split(":")[0].trim();

                if (mainFrame.getCenterPanel() instanceof IssueListPanel) {
                    IssueListPanel listPanel = (IssueListPanel) mainFrame.getCenterPanel();
                    listPanel.setCurrentProjectId(currentProjectId);
                    listPanel.loadIssues(); // 이슈 리스트 새로고침 작동
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user == null) {
            accountManager.logout();
        }
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    // 권한별 사이드 메뉴 제어 로직
    public void configureSideMenuByRole() {
        SideMenuPanel sideMenu = mainFrame.getSideMenuPanel();
        if (sideMenu == null) return;

        if (currentUser == null) {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(false);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
            return;
        }

        Role currentRole = currentUser.getRole();

        if (currentRole == Role.ADMIN) {
            sideMenu.getBtnAdminManage().setVisible(true);
            sideMenu.getBtnList().setVisible(false);
            sideMenu.getBtnCreate().setVisible(false);
            sideMenu.getBtnStats().setVisible(false);
        } else {
            sideMenu.getBtnAdminManage().setVisible(false);
            sideMenu.getBtnList().setVisible(true);
            sideMenu.getBtnCreate().setVisible(true);
            sideMenu.getBtnStats().setVisible(true);
        }

        sideMenu.revalidate();
        sideMenu.repaint();
    }

    public IssueService getIssueService() {
        return this.issueService;
    }

    public AccountManager getAccountManager() {
        return this.accountManager;
    }

    public ProjectService getProjectService() {
        return this.projectService;
    }
}