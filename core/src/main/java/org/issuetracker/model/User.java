package org.issuetracker.model;

public class User {
    private String id;
    private String password;
    private String name;
    private Role role;

    // 기본 생성자 (Jackson 라이브러리가 사용함)
    public User() {}

    public User(String id, String password, String name, Role role) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // Getter
    public String getId() {
        return id;
    }
    public String getPassword() {
        return password;
    }
    public String getName() {
        return name;
    }
    public Role getRole() {
        return role;
    }

    // Setter
    public void setId(String id) {
        this.id = id;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setRole(Role role) {
        this.role = role;
    }
}