package org.issuetracker;
import java.util.Scanner;

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
import org.issuetracker.service.ProjectService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        AccountManager manager = new AccountManager();
        IssueService issueService = new IssueService();
        ProjectService projectService = new ProjectService();

        Scanner sc = new Scanner(System.in);

        // 계정 초기화
        manager.addUser(new User("admin01", "1234", "admin", Role.ADMIN));
        manager.addUser(new User("tester01", "1111", "tester", Role.TESTER));
        manager.addUser(new User("pl01", "2222", "PL1", Role.PL));
        manager.addUser(new User("dev01", "3333", "DEV1", Role.DEV));

        // Admin 프로젝트 생성
        manager.login("admin01", "1234");

        User adminUser = manager.getCurrentUser();

        projectService.addProject(
                "ITS Main Project",
                "메인 이슈 관리 프로젝트",
                adminUser
        );

        projectService.addProject(
                "Mobile App",
                "모바일 앱 프로젝트",
                adminUser
        );

        manager.logout();

        // 1. TESTER 이슈 등록 및 코멘트 추가
        manager.login("tester01", "1111");
        User testerUser = manager.getCurrentUser();

        System.out.println("현재 사용자 : " + testerUser.getName());
        System.out.println("이슈 생성 권한 : " + PermissionManager.canCreateIssue(testerUser));

        projectService.printProjects();

        System.out.print("\n이슈를 등록할 프로젝트 ID 입력: ");
        int selectedProjectId = sc.nextInt();
        sc.nextLine();

        Issue testIssue = new Issue();
        testIssue.title = "Login Error";
        testIssue.description = "로그인 버튼이 작동하지 않음";
        testIssue.priority = Priority.CRITICAL;
        issueService.createIssue(selectedProjectId, testIssue, testerUser);

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
        issueService.createIssue(selectedProjectId, secondIssue, searchUser);

        Issue thirdIssue = new Issue();
        thirdIssue.title = "로그인 오류";
        thirdIssue.description = "로그인 버튼 간헐적 먹통";
        thirdIssue.priority = Priority.CRITICAL;
        issueService.createIssue(selectedProjectId, thirdIssue, searchUser);

        Issue fourthIssue = new Issue();
        fourthIssue.title = "UI 정렬 깨짐";
        fourthIssue.description = "사이드바 텍스트 겹침 현상";
        fourthIssue.priority = Priority.MINOR;
        issueService.createIssue(selectedProjectId, fourthIssue, searchUser);
        manager.logout();

        // 첫 번째 해결 이슈
        manager.login("tester01", "1111");
        User recTester1 = manager.getCurrentUser();

        Issue recIssue1 = new Issue();
        recIssue1.title = "로그인 에러 발생";
        recIssue1.description = "로그인이 안 됩니다.";
        recIssue1.priority = Priority.CRITICAL;

        issueService.createIssue(selectedProjectId, recIssue1, recTester1);
        manager.logout();

        manager.login("pl01", "2222");
        issueService.assignIssue(5, "dev01", manager.getCurrentUser());
        manager.logout();

        manager.login("dev01", "3333");
        issueService.changeStatus(5, IssueStatus.FIXED, manager.getCurrentUser());
        manager.logout();

        manager.login("tester01", "1111");
        issueService.changeStatus(5, IssueStatus.RESOLVED, manager.getCurrentUser());
        manager.logout();

        manager.login("pl01", "2222");
        issueService.changeStatus(5, IssueStatus.CLOSED, manager.getCurrentUser());
        manager.logout();


        // 두 번째 해결 이슈
        manager.login("tester01", "1111");
        User recTester2 = manager.getCurrentUser();

        Issue recIssue2 = new Issue();
        recIssue2.title = "네이버 로그인 연동 실패";
        recIssue2.description = "로그인 버튼이 안 눌려요.";
        recIssue2.priority = Priority.CRITICAL;

        issueService.createIssue(selectedProjectId, recIssue2, recTester2);
        manager.logout();

        manager.login("pl01", "2222");
        issueService.assignIssue(6, "dev01", manager.getCurrentUser());
        manager.logout();

        manager.login("dev01", "3333");
        issueService.changeStatus(6, IssueStatus.FIXED, manager.getCurrentUser());
        manager.logout();

        manager.login("tester01", "1111");
        issueService.changeStatus(6, IssueStatus.RESOLVED, manager.getCurrentUser());
        manager.logout();

        manager.login("pl01", "2222");
        issueService.changeStatus(6, IssueStatus.CLOSED, manager.getCurrentUser());
        manager.logout();


        // 세 번째 해결 이슈
        manager.login("tester01", "1111");
        User recTester3 = manager.getCurrentUser();

        Issue recIssue3 = new Issue();
        recIssue3.title = "구글 로그인 오류";
        recIssue3.description = "로그인 기능에 문제가 있습니다.";
        recIssue3.priority = Priority.CRITICAL;

        issueService.createIssue(selectedProjectId, recIssue3, recTester3);
        manager.logout();

        manager.login("pl01", "2222");
        issueService.assignIssue(7, "dev01", manager.getCurrentUser());
        manager.logout();

        manager.login("dev01", "3333");
        issueService.changeStatus(7, IssueStatus.FIXED, manager.getCurrentUser());
        manager.logout();

        manager.login("tester01", "1111");
        issueService.changeStatus(7, IssueStatus.RESOLVED, manager.getCurrentUser());
        manager.logout();

        manager.login("pl01", "2222");
        issueService.changeStatus(7, IssueStatus.CLOSED, manager.getCurrentUser());
        manager.logout();

        // 7. 이슈 조건별 조회 검증
        List<Issue> allIssues = issueService.getAllIssues();
        System.out.println("전체 이슈: " + allIssues.size() + "개");

        List<Issue> newIssues = issueService.searchIssues(1, "NEW", null, null, null);
        System.out.println("NEW 상태 이슈: " + newIssues.size() + "개");
        for (Issue i : newIssues) {
            System.out.println("  - " + i.title + " / 우선순위: " + i.priority);
        }

        List<Issue> reporterIssues = issueService.searchIssues(1, null, null, "tester01", null);
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
        System.out.println("\n====================================");
        manager.logout();
    }
}