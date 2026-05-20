package org.issuetracker;

import org.issuetracker.model.Comment;
import org.issuetracker.model.Issue;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.model.Priority;
import org.issuetracker.model.Role;
import org.issuetracker.model.User;
import org.issuetracker.model.RecommendationResult;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.IssueService;
import org.issuetracker.service.PermissionManager;
import org.issuetracker.service.StatisticsService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        AccountManager manager = new AccountManager();
        IssueService issueService = new IssueService();

        // 계정 초기화
        manager.addUser(new User("admin01", "1234", "admin", Role.ADMIN));
        manager.addUser(new User("tester01", "1111", "tester", Role.TESTER));
        manager.addUser(new User("pl01", "2222", "PL1", Role.PL));
        manager.addUser(new User("dev01", "3333", "DEV1", Role.DEV));

        // 1. TESTER 이슈 등록 및 코멘트 추가
        manager.login("tester01", "1111");
        User testerUser = manager.getCurrentUser();

        System.out.println("현재 사용자 : " + testerUser.getName());
        System.out.println("이슈 생성 권한 : " + PermissionManager.canCreateIssue(testerUser));

        Issue testIssue = new Issue();
        testIssue.title = "Login Error";
        testIssue.description = "로그인 버튼이 작동하지 않음";
        testIssue.priority = Priority.CRITICAL;
        issueService.createIssue(testIssue, testerUser);

        issueService.addCommentToIssue(
                1,
                new Comment(testerUser.getId(), "이슈 재현 확인했습니다."),
                testerUser
        );
        manager.logout();

        // 2. PL 담당자 배정
        manager.login("pl01", "2222");
        User plUser = manager.getCurrentUser();
        issueService.assignIssue(1, "dev01", plUser);
        manager.logout();

        // 3. DEV 이슈 해결 (FIXED 상태 변경)
        manager.login("dev01", "3333");
        User devUser = manager.getCurrentUser();
        issueService.changeStatus(1, IssueStatus.FIXED, devUser);
        manager.logout();

        // 4. TESTER 검증 (RESOLVED 상태 변경)
        manager.login("tester01", "1111");
        User testerResolveUser = manager.getCurrentUser();
        issueService.changeStatus(1, IssueStatus.RESOLVED, testerResolveUser);
        manager.logout();

        // 5. PL 종료 (CLOSED 상태 변경)
        manager.login("pl01", "2222");
        User plCloseUser = manager.getCurrentUser();
        issueService.changeStatus(1, IssueStatus.CLOSED, plCloseUser);
        manager.logout();

        // 6. 검색 테스트 및 통계용 추가 이슈 등록
        System.out.println("\n--- 검색 테스트 ---");
        manager.login("tester01", "1111");
        User searchUser = manager.getCurrentUser();

        Issue secondIssue = new Issue();
        secondIssue.title = "UI 정렬 깨짐";
        secondIssue.description = "메인 화면 버튼 위치 이상";
        secondIssue.priority = Priority.MINOR;
        issueService.createIssue(secondIssue, searchUser);

        Issue thirdIssue = new Issue();
        thirdIssue.title = "로그인 오류";
        thirdIssue.description = "로그인 버튼 간헐적 먹통";
        thirdIssue.priority = Priority.CRITICAL;
        issueService.createIssue(thirdIssue, searchUser);

        Issue fourthIssue = new Issue();
        fourthIssue.title = "UI 정렬 깨짐";
        fourthIssue.description = "사이드바 텍스트 겹침 현상";
        fourthIssue.priority = Priority.MINOR;
        issueService.createIssue(fourthIssue, searchUser);
        manager.logout();

        // 7. 이슈 조건별 조회 검증
        List<Issue> allIssues = issueService.getAllIssues();
        System.out.println("전체 이슈: " + allIssues.size() + "개");

        List<Issue> newIssues = issueService.searchIssues("NEW", null, null);
        System.out.println("NEW 상태 이슈: " + newIssues.size() + "개");
        for (Issue i : newIssues) {
            System.out.println("  - " + i.title + " / 우선순위: " + i.priority);
        }

        List<Issue> reporterIssues = issueService.searchIssues(null, null, "tester01");
        System.out.println("tester01이 등록한 이슈: " + reporterIssues.size() + "개");

        // 8. 상세 조회 및 통계 출력
        manager.login("pl01", "2222");
        User viewUser = manager.getCurrentUser();
        issueService.printIssueDetail(1, viewUser);
        manager.logout();

        StatisticsService statsService = new StatisticsService();
        manager.login("pl01", "2222");
        User statsUser = manager.getCurrentUser();
        statsService.printAllStats(statsUser);
        manager.logout();

        // 9. 담당자 추천 시스템 검증
        System.out.println("\n--- 담당자 자동 추천 기능 테스트 ---");
        manager.login("pl01", "2222");

        int targetIssueId = 3;
        System.out.println("대상 이슈 ID: " + targetIssueId + " (분석 중...)");

        List<RecommendationResult> recommendations = issueService.getAssigneeRecommendations(targetIssueId);

        System.out.println("\n[추천 담당자 목록 (Top 3)]");
        if (recommendations.isEmpty()) {
            System.out.println("추천 가능한 후보자가 없습니다.");
        } else {
            for (int i = 0; i < recommendations.size(); i++) {
                RecommendationResult result = recommendations.get(i);
                System.out.printf("%d. 개발자: %s (유사도: %.4f, 참조 이슈: %s)\n",
                        (i + 1),
                        result.getFixerId(),
                        result.getScore(),
                        result.getMatchedIssueIds().toString()
                );
            }
        }
        System.out.println("--------------------------------------------------");
        manager.logout();
    }
}