import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {

    private List<User> users = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String FILE_PATH = "users.json";

    // 현재 로그인한 사용자
    private User currentUser;

    // 생성자
    public AccountManager() {
        loadUsers();
    }

    // 계정 추가
    public void addUser(User user) {

        if(isDuplicateId(user.getId())) {
            System.out.println("Duplicated ID");
            return;
        }

        users.add(user);

        saveUsers();

        System.out.println(
                user.getName()
                        + " Account Created Successfully"
        );
    }

    // 중복 ID 검사
    private boolean isDuplicateId(String id) {

        for(User user : users) {

            if(user.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    // 로그인
    public boolean login(String id, String password) {

        for(User user : users) {

            if(user.getId().equals(id)
                    && user.getPassword().equals(password)) {

                currentUser = user;

                System.out.println(
                        user.getName()
                                + " Login Success"
                );

                return true;
            }
        }

        System.out.println("Login Failed");

        return false;
    }

    // 로그아웃
    public void logout() {
        currentUser = null;
    }

    // 현재 로그인 사용자 반환
    public User getCurrentUser() {
        return currentUser;
    }

    // JSON 저장
    private void saveUsers() {

        try {

            objectMapper.writeValue(
                    new File(FILE_PATH),
                    users
            );

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // JSON 불러오기
    private void loadUsers() {

        try {

            File file = new File(FILE_PATH);

            if(file.exists()) {

                users = objectMapper.readValue(
                        file,
                        objectMapper.getTypeFactory()
                                .constructCollectionType(
                                        List.class,
                                        User.class
                                )
                );
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public List<User> getUsers() {
        return users;
    }
}