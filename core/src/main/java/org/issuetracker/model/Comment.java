package org.issuetracker.model;

import java.time.LocalDateTime;

public class Comment {

    public String authorId;
    public String content;
    public String date;

    public Comment() {}

    public Comment(String authorId, String content) {
        this.authorId = authorId;
        this.content = content;
        this.date = LocalDateTime.now().toString();
    }
}