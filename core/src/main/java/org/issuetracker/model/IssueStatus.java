package org.issuetracker.model;

public enum IssueStatus {
    NEW,
    ASSIGNED,
    FIXED,
    REOPENED,
    RESOLVED,
    CLOSED;

    //대문자로
    @Override
    public String toString() {
        return this.name().toUpperCase();
    }
}