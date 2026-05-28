package org.issuetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.issuetracker.model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {

    private List<User> users = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    // [수정된 부분] 실행 환경에 관계없이 프로젝트 루트 폴더를 기준으로 절대 경로 설정
    private final String FILE_PATH = System.getProperty("user.dir") + File.separator + "users.json";

    // 현재 로그인한 사용자
    private User currentUser;

    // 생성자
    public AccountManager() {
        loadUsers();
    }

    // 계정 추가
    public void addUser(User user) {
        if(isDuplicateId(user.getId())) {
            System.out.println("중복된 아이디입니다");
            return;
        }
        users.add(user);
        saveUsers();
        System.out.println(
                user.getName() + " 계정이 생성되었습니다"
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
                        user.getName() + " 로그인 성공"
                );
                return true;
            }
        }
        System.out.println("로그인 실패");
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
            // [수정된 부분] 명시적으로 지정된 절대 경로를 사용하여 파일 저장
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE_PATH), users);
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

    /**
     * 앱 시작 시 users.json 을 완전히 초기화합니다.
     * Main.java 맨 앞에서 한 번 호출하면 매 실행마다 클린 스타트가 보장됩니다.
     */
    public void resetAll() {
        users = new ArrayList<>();
        saveUsers();
    }
}