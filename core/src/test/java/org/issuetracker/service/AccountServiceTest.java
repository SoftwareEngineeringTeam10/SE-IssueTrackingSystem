package org.issuetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.issuetracker.model.User;
import org.issuetracker.model.Role;

import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountManager 계정 관리 및 가드 단위 테스트")
class AccountManagerTest {

    private AccountManager accountManager;
    private User newPlUser;

    private String originalUserDir;
    private String testUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        testUserDir = originalUserDir + File.separator + "test_sandbox";

        File sandboxDir = new File(testUserDir);
        if (!sandboxDir.exists()) {
            sandboxDir.mkdirs();
        }

        System.setProperty("user.dir", testUserDir);

        accountManager = new AccountManager();
        accountManager.resetAll();

        newPlUser = new User();
        newPlUser.setId("pl_test_999");
        newPlUser.setPassword("2222");
        newPlUser.setName("테스트PL");
        newPlUser.setRole(Role.PL);
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Nested
    @DisplayName("계정 생성 및 중복 ID 원천 차단 가드 검증")
    class UserCreationAndDuplicateGuard {

        @Test
        @DisplayName("정상 흐름: 신규 계정 추가 시 전체 유저 목록 및 파일 영속화 반영 검증")
        void testAddUserSuccess() {
            int beforeSize = accountManager.getUsers().size();

            accountManager.addUser(newPlUser);

            int afterSize = accountManager.getUsers().size();
            assertEquals(beforeSize + 1, afterSize, "신규 계정이 추가되면 전체 유저 수가 1개 증가해야 합니다.");
            assertEquals("테스트PL", accountManager.getUserNameById("pl_test_999"), "등록된 ID로 이름이 정상 조회되어야 합니다.");
        }

        @Test
        @DisplayName("예외 테스트: 동일한 ID로 중복 가입 시도 시 시스템 차단 검증")
        void testDuplicateIdGuard() {
            accountManager.addUser(newPlUser);
            int baseSize = accountManager.getUsers().size();

            User fakeUser = new User();
            fakeUser.setId("pl_test_999");
            fakeUser.setPassword("9999");
            fakeUser.setName("가짜유저");
            fakeUser.setRole(Role.DEV);

            accountManager.addUser(fakeUser);

            int finalSize = accountManager.getUsers().size();
            assertEquals(baseSize, finalSize, "중복된 ID를 가진 계정 추가 요청은 시스템 가드에 의해 무시되어야 합니다.");
        }
    }

    @Nested
    @DisplayName("역할(Role) 기반 유저 필터링 조회 검증")
    class RoleFilteringFilter {

        @Test
        @DisplayName("특정 Role(PL)을 가진 유저들만 정확히 가려내는지 검증")
        void testGetUsersByRolePipeline() {
            accountManager.addUser(newPlUser);

            User devUser = new User();
            devUser.setId("dev_test_888");
            devUser.setRole(Role.DEV);
            accountManager.addUser(devUser);

            List<User> plList = accountManager.getUsersByRole(Role.PL);

            assertNotNull(plList, "필터링 결과 리스트가 존재해야 합니다.");
            assertEquals(1, plList.size(), "PL 권한을 가진 유저는 정확히 1명만 뽑혀야 합니다.");
            assertEquals("pl_test_999", plList.get(0).getId(), "필터링된 유저의 ID와 일치해야 합니다.");
        }
    }
}