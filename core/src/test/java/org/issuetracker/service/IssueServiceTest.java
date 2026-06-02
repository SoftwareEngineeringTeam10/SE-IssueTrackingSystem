package org.issuetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.issuetracker.model.*;

import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IssueService 핵심 비즈니스 로직 단위 테스트")
class IssueServiceTest {

    private IssueService issueService;

    private User adminUser;
    private User testerUser;
    private User plUser;
    private User devUser;

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

        issueService = new IssueService();

        adminUser = new User();
        adminUser.setId("admin01");
        adminUser.setPassword("1234");
        adminUser.setName("admin");
        adminUser.setRole(Role.ADMIN);

        testerUser = new User();
        testerUser.setId("tester01");
        testerUser.setPassword("1111");
        testerUser.setName("tester");
        testerUser.setRole(Role.TESTER);

        plUser = new User();
        plUser.setId("pl01");
        plUser.setPassword("2222");
        plUser.setName("PL1");
        plUser.setRole(Role.PL);

        devUser = new User();
        devUser.setId("dev01");
        devUser.setPassword("3333");
        devUser.setName("DEV1");
        devUser.setRole(Role.DEV);
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Nested
    @DisplayName("상태 기계(State Machine) 생명주기 및 전이 가드 검증")
    class StateMachineTesting {

        private int issueId;

        @BeforeEach
        void insertBaseIssue() {
            Issue issue = new Issue();
            issue.title = "계정 시드 이슈";
            issue.status = IssueStatus.NEW;

            issueService.createIssue(999, issue, testerUser);
            List<Issue> list = issueService.searchIssues(999, "NEW", null, null, null);
            issueId = list.get(0).id;
        }

        @Test
        @DisplayName("정상 흐름: PL이 담당자 지정하고 개발자가 해결 상태로 바꾸는 과정 확인")
        void testValidStateTransitionPath() {
            boolean assignResult = issueService.assignIssue(issueId, "dev01", plUser);
            assertTrue(assignResult);
            assertEquals(IssueStatus.ASSIGNED, issueService.getIssueById(issueId).status);

            boolean fixResult = issueService.changeStatus(issueId, IssueStatus.FIXED, devUser);
            assertTrue(fixResult);
            assertEquals(IssueStatus.FIXED, issueService.getIssueById(issueId).status);
        }

        @Test
        @DisplayName("예외 테스트: 단계를 건너뛰고 바로 해결 상태로 변경할 때 차단되는지 확인")
        void testInvalidStateTransitionGuard() {
            boolean illegalResult = issueService.changeStatus(issueId, IssueStatus.FIXED, devUser);
            assertFalse(illegalResult);

            Issue currentIssue = issueService.getIssueById(issueId);
            assertEquals(IssueStatus.NEW, currentIssue.status);
        }

        @Test
        @DisplayName("권한 테스트: 권한이 없는 어드민 계정이 상태를 변경하려고 할 때 거부되는지 확인")
        void testStatusPermissionGuard() {
            issueService.assignIssue(issueId, "dev01", plUser);
            boolean adminIllegalFix = issueService.changeStatus(issueId, IssueStatus.FIXED, adminUser);
            assertFalse(adminIllegalFix);
        }
    }

    @Nested
    @DisplayName("복합 조건 검색 기능 검증")
    class MultiFilterTesting {

        private final int targetProjectId = 888;

        @BeforeEach
        void setupFilterDummyData() {
            Issue bug1 = new Issue();
            bug1.title = "Critical Memory Leak UI Bug";
            bug1.description = "메모리 누수로 인한 화면 멈춤";
            bug1.status = IssueStatus.NEW;
            issueService.createIssue(targetProjectId, bug1, devUser);

            Issue bug2 = new Issue();
            bug2.title = "Simple Typography Error";
            bug2.description = "오탈자 수정 요청";
            bug2.status = IssueStatus.ASSIGNED;
            issueService.createIssue(targetProjectId, bug2, devUser);

            Issue bug3 = new Issue();
            bug3.title = "Critical Network Timeout";
            bug3.description = "패킷 유실 현상";
            bug3.status = IssueStatus.NEW;
            issueService.createIssue(targetProjectId, bug3, devUser);
        }

        @Test
        @DisplayName("정상 테스트: 상태 조건과 검색 키워드가 모두 맞는 데이터만 필터링되는지 확인")
        void testStatusAndKeywordAndFilter() {
            List<Issue> searchResult = issueService.searchIssues(
                    targetProjectId,
                    "NEW",
                    null,
                    null,
                    "UI"
            );

            assertNotNull(searchResult);
            assertTrue(searchResult.size() >= 1);

            Issue matchedIssue = searchResult.get(0);
            assertEquals("Critical Memory Leak UI Bug", matchedIssue.title);
        }
    }
}