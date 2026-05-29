package org.issuetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.issuetracker.model.*;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IssueService 핵심 비즈니스 로직 단위 테스트")
class IssueServiceTest {

    private IssueService issueService;

    private User adminUser;
    private User testerUser;
    private User plUser;
    private User devUser;

    @BeforeEach
    void setUp() {
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


    // State Machine 생명주기 및 권한 가드 테스트
    @Nested
    @DisplayName("상태 기계(State Machine) 생명주기 및 전이 가드 검증")
    class StateMachineTesting {

        private int issueId;

        @BeforeEach
        void insertBaseIssue() {

            Issue issue = new Issue();
            issue.title = "계정 시드 이슈";
            issue.status = IssueStatus.NEW;

            // 테스터 권한으로 이슈 생성 성공 유무 확인 겸 시드 주입
            issueService.createIssue(999, issue, testerUser);
            List<Issue> list = issueService.searchIssues(999, "NEW", null, null, null);
            issueId = list.get(0).id;
        }

        @Test
        @DisplayName("정상 흐름: PL1 배정 및 DEV1 수정 전이 패스 검증")
        void testValidStateTransitionPath() {
            //  NEW -> ASSIGNED: 프로젝트 리더(pl01)가 실무 개발자(dev01)에게 이슈를  배정
            boolean assignResult = issueService.assignIssue(issueId, "dev01", plUser);
            assertTrue(assignResult, "PL(pl01)이 담당자(dev01)를 배정하면 ASSIGNED 상태로 정상 전이되어야 합니다.");
            assertEquals(IssueStatus.ASSIGNED, issueService.getIssueById(issueId).status);

            // ASSIGNED -> FIXED: 배정받은 개발자(dev01)가 버그 수정 완료 처리
            boolean fixResult = issueService.changeStatus(issueId, IssueStatus.FIXED, devUser);
            assertTrue(fixResult, "개발자(dev01)의 FIXED 상태 전이 요청은 유효해야 합니다.");
            assertEquals(IssueStatus.FIXED, issueService.getIssueById(issueId).status);
        }

        @Test
        @DisplayName("예외 가드: 단계를 건너뛰는 불법 상태 전이 차단 및 메모리 격리 검증")
        void testInvalidStateTransitionGuard() {
            // 상태 기계 파괴 시도: 현재 'NEW' 상태인데 바로 'FIXED'로 요청
            boolean illegalResult = issueService.changeStatus(issueId, IssueStatus.FIXED, devUser);

            // 허용되지 않은 비정상 루트이므로 isValidTransition 가드에 의해 무조건 false 리턴
            assertFalse(illegalResult, "단계를 건너뛰는 예외적인 상태 전이는 시스템 가드가 원천 차단해야 합니다.");

            // 요청이 차단되었으므로 실제 메모리 상의 이슈 상태는 순정 'NEW'를 유지해야 함
            Issue currentIssue = issueService.getIssueById(issueId);
            assertEquals(IssueStatus.NEW, currentIssue.status, "전이 가드 실패 시 데이터가 오염되지 않고 기존 상태를 유지해야 합니다.");
        }

        @Test
        @DisplayName("권한 가드: 최고 관리자(admin01)의 업무 프로세스 변조 가드 차단 검증")
        void testStatusPermissionGuard() {
            // 현재 이슈를 ASSIGNED 상태로 정석 전이 시킴
            issueService.assignIssue(issueId, "dev01", plUser);

            // 최고 관리자(admin01)는 계정/프로젝트 생성 주체이지 실무 코드 수정(FIXED) 권한 주체가 아님.
            // 명세상 admin01이 강제로 FIXED 상태 변경을 시도할 때 가드가 완벽히 처내는지 검증
            boolean adminIllegalFix = issueService.changeStatus(issueId, IssueStatus.FIXED, adminUser);

            assertFalse(adminIllegalFix, "실무 권한 매트릭스를 위배하는 ADMIN 계정의 상태 변경은 가드에 의해 거부되어야 합니다.");
        }
    }


    // 복합 조건 검색 (Multi-Filter Search) 파이프라인 테스트
    @Nested
    @DisplayName("복합 조건 검색(Multi-Filter Search) 파이프라인 검증")
    class MultiFilterTesting {

        private final int targetProjectId = 888;

        @BeforeEach
        void setupFilterDummyData() {
            // 복합 검색 교집합 조건 검증을 위한 정밀 샘플 데이터 적재
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
        @DisplayName("정밀 타격: 상태(Status) + 키워드(Keyword) 교집합 복합 필터링 검증")
        void testStatusAndKeywordAndFilter() {
            // 프로젝트 내부에서 상태가 'NEW' 이면서, 제목/설명에 'UI'가 포함된 이슈만 교집합 검색
            List<Issue> searchResult = issueService.searchIssues(
                    targetProjectId,
                    "NEW",
                    null,
                    null,
                    "UI"
            );

            // Assertion
            assertNotNull(searchResult, "검색 결과 리스트 객체는 실재해야 합니다.");
            assertTrue(searchResult.size() >= 1, "상태와 키워드 복합 조건을 만족하는 이슈가 최소 1개 이상 정상 조회되어야 합니다.");

            Issue matchedIssue = searchResult.get(0);
            assertEquals("Critical Memory Leak UI Bug", matchedIssue.title, "복합 필터링이 정확한 타깃 데이터를 도려내야 합니다.");
        }
    }
}