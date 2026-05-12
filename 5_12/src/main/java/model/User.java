package model;

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

    // Getter, Setter
    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Role getRole() { return role; }
}