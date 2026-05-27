package org;

import javax.swing.*;

import org.issuetracker.service.AccountManager;
import org.view.MainFrame;
import org.issuetracker.service.IssueService;

public class Main {

    public static void main(String[] args) {

        IssueService issueService = new IssueService();
        AccountManager accountManager = new AccountManager();


        SwingUtilities.invokeLater(() -> {

            MainFrame frame = new MainFrame(issueService, accountManager);

            JPanel blankPanel = new JPanel();
            blankPanel.setBackground(java.awt.Color.WHITE);
            frame.changeCenterPanel(blankPanel);

            frame.setVisible(true);
        });
    }
}