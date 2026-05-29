package org.issuetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.issuetracker.model.*;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatisticsService 통계 산출 파이프라인 단위 테스트 ")
class StatisticsServiceTest {

    private StatisticsService statisticsService;
    private IssueService issueService;
    private User devUser;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService();
        issueService = new IssueService();

        devUser = new User();
        devUser.setId("dev01");
        devUser.setRole(Role.DEV);

        Issue bug1 = new Issue();
        bug1.title = "통계 테스트용 NEW 버그 1";
        bug1.status = IssueStatus.NEW;
        bug1.priority = Priority.CRITICAL;
        issueService.createIssue(111, bug1, devUser);

        Issue bug2 = new Issue();
        bug2.title = "통계 테스트용 NEW 버그 2";
        bug2.status = IssueStatus.NEW;
        bug2.priority = Priority.MAJOR;
        issueService.createIssue(111, bug2, devUser);
    }

    @Nested
    @DisplayName("상태별 이슈 통계 집계 검증")
    class StatusStatistics {

        @Test
        @DisplayName("getStatusStats 호출 시 NEW 상태 이슈 개수가 정확히 집계되는지 검증")
        void testGetStatusStatsSuccess() {
            Map<IssueStatus, Integer> statusMap = statisticsService.getStatusStats();

            assertNotNull(statusMap, "산출된 상태 통계 Map 객체는 실재해야 합니다.");

            int newCount = statusMap.getOrDefault(IssueStatus.NEW, 0);
            assertTrue(newCount >= 2, "시스템 전체의 'NEW' 상태 이슈는 최소 2개 이상으로 카운트되어야 합니다.");

            assertTrue(statusMap.containsKey(IssueStatus.CLOSED), "로직에 따라 데이터가 없는 CLOSED 상태도 Map에 키값으로 존재해야 합니다.");
        }
    }

    @Nested
    @DisplayName("우선순위별 이슈 통계 집계 검증")
    class PriorityStatistics {

        @Test
        @DisplayName("getPriorityStats 호출 시 CRITICAL 및 MAJOR 우선순위가 정상 누적되는지 검증")
        void testGetPriorityStatsSuccess() {
            Map<Priority, Integer> priorityMap = statisticsService.getPriorityStats();

            assertNotNull(priorityMap, "산출된 우선순위 통계 Map 객체는 실재해야 합니다.");

            int criticalCount = priorityMap.getOrDefault(Priority.CRITICAL, 0);
            assertTrue(criticalCount >= 1, "CRITICAL 우선순위 이슈 카운트 파이프라인이 정상 작동해야 합니다.");

            int majorCount = priorityMap.getOrDefault(Priority.MAJOR, 0);
            assertTrue(majorCount >= 1, "MAJOR 우선순위 이슈 카운트 파이프라인이 정상 작동해야 합니다.");
        }
    }
}