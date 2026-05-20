package org.issuetracker.model;

public enum IssueStatus {
    NEW,
    ASSIGNED,
    FIXED,
    REOPENED,
    RESOLVED,
    CLOSED;

    @Override
    public String toString() {
        return this.name().toUpperCase(); // 대문자로
    }
}