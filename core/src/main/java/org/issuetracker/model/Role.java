package org.issuetracker.model;

public enum Role {
    ADMIN, PL, DEV, TESTER;

    // 대문자로
    @Override
    public String toString() {
        return this.name().toUpperCase();
    }
}