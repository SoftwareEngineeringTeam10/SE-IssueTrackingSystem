package org.issuetracker.model;

import java.util.ArrayList;
import java.util.List;

public class Issue {

    public int id;
    public String title;
    public String description;
    public String reporterId;
    public String reportedDate;
    public String fixerId;
    public String assigneeId;
    public Priority priority = Priority.MAJOR;
    public IssueStatus status = IssueStatus.NEW;    public List<Comment> comments = new ArrayList<>();

    public void setStatus(IssueStatus status) {
        this.status = status;
    }
    public IssueStatus getStatus(){
        return this.status;
    }


    public Issue() {}

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}