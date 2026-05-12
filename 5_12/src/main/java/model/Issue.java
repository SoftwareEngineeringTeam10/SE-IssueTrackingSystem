package model;

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
    public String priority = "major";
    public String status = "new";
    public List<Comment> comments = new ArrayList<>();

    public Issue() {}

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}