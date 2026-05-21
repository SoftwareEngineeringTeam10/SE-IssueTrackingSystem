package org.issuetracker.model;

import java.util.ArrayList;
import java.util.List;

public class Issue {

    public int id;
    public String title;
    public String description;
    public String reporter;
    public String reportedDate;
    public String fixer;
    public String assignee;
    public Priority priority = Priority.MAJOR;
    public IssueStatus status = IssueStatus.NEW;    public List<Comment> comments = new ArrayList<>();

    public Issue() {}

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}