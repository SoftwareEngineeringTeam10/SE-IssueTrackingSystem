package org.issuetracker.model;

public class Project {
    public int id;
    public String name;
    public String description;

    public Project() {}

    public Project(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}