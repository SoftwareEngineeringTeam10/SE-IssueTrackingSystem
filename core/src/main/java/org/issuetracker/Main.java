package org.issuetracker;

import java.util.Scanner;
import org.issuetracker.model.*;
import org.issuetracker.service.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        AccountManager manager   = new AccountManager();
        IssueService issueService     = new IssueService();
        ProjectService projectService = new ProjectService();

        // ── ① 매 실행마다 데이터 초기화 (중복 방지) ──────────────────────
        manager.resetAll();         // users.json 초기화
        projectService.resetAll();  // projects.json 초기화

        Scanner sc = new Scanner(System.in);

        // ── ② 사용자 시드 (명세 2.2 기준 18명) ───────────────────────────
        manager.addUser(new User("admin01", "1234", "어드민", Role.ADMIN));

        for (int i = 1; i <= 2; i++) {
            manager.addUser(new User("pl0" + i, "2222", "PL" + i, Role.PL));
        }

        for (int i = 1; i <= 10; i++) {
            manager.addUser(new User(
                    String.format("dev%02d", i),
                    "3333",
                    "DEV" + i,
                    Role.DEV
            ));
        }

        for (int i = 1; i <= 5; i++) {
            manager.addUser(new User(
                    String.format("tester%02d", i),
                    "1111",
                    "TESTER" + i,
                    Role.TESTER
            ));
        }

        // ── ③ 프로젝트 생성 ───────────────────────────────────────────────
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

        // ── tester01: 이슈 생성 + 댓글 ───────────────────────────────────
        manager.login("tester01", "1111");

        User testerUser = manager.getCurrentUser();

        System.out.println("현재 사용자 : " + testerUser.getName());

        System.out.println(
                "이슈 생성 권한 : "
                        + PermissionManager.canCreateIssue(testerUser)
        );

        projectService.printProjects();

        System.out.print("\n이슈를 등록할 프로젝트 ID 입력: ");

        int selectedProjectId = sc.nextInt();
        sc.nextLine();

        Issue testIssue = new Issue();

        testIssue.title       = "Login Error";
        testIssue.description = "로그인 버튼이 작동하지 않음";
        testIssue.priority    = Priority.CRITICAL;

        if (!issueService.createIssue(
                selectedProjectId,
                testIssue,
                testerUser
        )) {

            System.out.println("[경고] 이슈 생성 실패");
        }

        if (!issueService.addCommentToIssue(
                1,
                new Comment(
                        testerUser.getId(),
                        "이슈 재현 확인했습니다."
                ),
                testerUser
        )) {

            System.out.println("[경고] 댓글 추가 실패");
        }

        manager.logout();

        // ── pl01: 담당자 배정 ─────────────────────────────────────────────
        manager.login("pl01", "2222");

        if (!issueService.assignIssue(
                1,
                "dev01",
                manager.getCurrentUser()
        )) {

            System.out.println("[경고] 담당자 배정 실패");
        }

        manager.logout();

        // ── dev01: FIXED ──────────────────────────────────────────────────
        manager.login("dev01", "3333");

        if (!issueService.changeStatus(
                1,
                IssueStatus.FIXED,
                manager.getCurrentUser()
        )) {

            System.out.println("[경고] 상태 변경 실패 (FIXED)");
        }

        manager.logout();

        // ── tester01: RESOLVED ────────────────────────────────────────────
        manager.login("tester01", "1111");

        if (!issueService.changeStatus(
                1,
                IssueStatus.RESOLVED,
                manager.getCurrentUser()
        )) {

            System.out.println("[경고] 상태 변경 실패 (RESOLVED)");
        }

        manager.logout();

        // ── pl01: CLOSED ──────────────────────────────────────────────────
        manager.login("pl01", "2222");

        if (!issueService.changeStatus(
                1,
                IssueStatus.CLOSED,
                manager.getCurrentUser()
        )) {

            System.out.println("[경고] 상태 변경 실패 (CLOSED)");
        }

        manager.logout();

        // ── 검색 테스트용 추가 이슈 (이슈 2~4) ───────────────────────────
        System.out.println("\n--- 검색 테스트 ---");

        manager.login("tester01", "1111");

        User searchUser = manager.getCurrentUser();

        Issue secondIssue = new Issue();

        secondIssue.title       = "UI 정렬 깨짐";
        secondIssue.description = "메인 화면 버튼 위치 이상";
        secondIssue.priority    = Priority.MINOR;

        if (!issueService.createIssue(
                selectedProjectId,
                secondIssue,
                searchUser
        )) {

            System.out.println("[경고] 이슈 생성 실패 (2번)");
        }

        Issue thirdIssue = new Issue();

        thirdIssue.title       = "로그인 오류";
        thirdIssue.description = "로그인 버튼 간헐적 먹통";
        thirdIssue.priority    = Priority.CRITICAL;

        if (!issueService.createIssue(
                selectedProjectId,
                thirdIssue,
                searchUser
        )) {

            System.out.println("[경고] 이슈 생성 실패 (3번)");
        }

        Issue fourthIssue = new Issue();

        fourthIssue.title       = "UI 정렬 깨짐";
        fourthIssue.description = "사이드바 텍스트 겹침 현상";
        fourthIssue.priority    = Priority.MINOR;

        if (!issueService.createIssue(
                selectedProjectId,
                fourthIssue,
                searchUser
        )) {

            System.out.println("[경고] 이슈 생성 실패 (4번)");
        }

        manager.logout();

        // ── 추천 학습용 이슈 5~7 ──────────────────────────────────────────
        String[][] recIssues = {
                {"로그인 에러 발생",       "로그인이 안 됩니다."},
                {"네이버 로그인 연동 실패", "로그인 버튼이 안 눌려요."},
                {"구글 로그인 오류",        "로그인 기능에 문제가 있습니다."}
        };

        String[] devs = {"dev01", "dev02", "dev03"};

        for (int i = 0; i < recIssues.length; i++) {

            int issueId = 5 + i;

            // 이슈 생성
            manager.login("tester01", "1111");

            Issue rec = new Issue();

            rec.title       = recIssues[i][0];
            rec.description = recIssues[i][1];
            rec.priority    = Priority.CRITICAL;

            issueService.createIssue(
                    selectedProjectId,
                    rec,
                    manager.getCurrentUser()
            );

            manager.logout();

            // 담당자 배정
            manager.login("pl01", "2222");

            issueService.assignIssue(
                    issueId,
                    devs[i % devs.length],
                    manager.getCurrentUser()
            );

            manager.logout();

            // FIXED 처리
            String currentDev = devs[i % devs.length];

            manager.login(currentDev, "3333");

            issueService.changeStatus(
                    issueId,
                    IssueStatus.FIXED,
                    manager.getCurrentUser()
            );

            manager.logout();

            // RESOLVED 처리
            manager.login("tester01", "1111");

            issueService.changeStatus(
                    issueId,
                    IssueStatus.RESOLVED,
                    manager.getCurrentUser()
            );

            manager.logout();

            // CLOSED 처리
            manager.login("pl01", "2222");

            issueService.changeStatus(
                    issueId,
                    IssueStatus.CLOSED,
                    manager.getCurrentUser()
            );

            manager.logout();
        }

        // ── 검색 결과 출력 ────────────────────────────────────────────────
        List<Issue> allIssues =
                issueService.getIssuesByProject(selectedProjectId);

        System.out.println(
                "전체 이슈: "
                        + allIssues.size()
                        + "개"
        );

        List<Issue> newIssues = issueService.searchIssues(
                selectedProjectId,
                "NEW",
                null,
                null,
                null
        );

        System.out.println(
                "NEW 상태 이슈: "
                        + newIssues.size()
                        + "개"
        );

        for (Issue i : newIssues) {

            System.out.println(
                    "  - "
                            + i.title
                            + " / 우선순위: "
                            + i.priority
            );
        }

        List<Issue> reporterIssues =
                issueService.searchIssues(
                        selectedProjectId,
                        null,
                        null,
                        "tester01",
                        null
                );

        System.out.println(
                "tester01이 등록한 이슈: "
                        + reporterIssues.size()
                        + "개"
        );

        // ── 이슈 상세 조회 ────────────────────────────────────────────────
        manager.login("pl01", "2222");

        issueService.printIssueDetail(
                1,
                manager.getCurrentUser()
        );

        manager.logout();

        // ── 통계 출력 ─────────────────────────────────────────────────────
        StatisticsService statsService =
                new StatisticsService();

        manager.login("pl01", "2222");

        statsService.printAllStats(
                manager.getCurrentUser(),
                selectedProjectId
        );

        manager.logout();

        // ── 담당자 자동 추천 ──────────────────────────────────────────────
        System.out.println(
                "\n--- 담당자 자동 추천 기능 테스트 ---"
        );

        manager.login("pl01", "2222");

        int targetIssueId = 3;

        System.out.println(
                "대상 이슈 ID: "
                        + targetIssueId
                        + " (분석 중...)"
        );

        List<RecommendationResult> recommendations =
                issueService.getAssigneeRecommendations(
                        targetIssueId
                );

        System.out.println("\n[추천 담당자 목록 (Top 3)]");

        if (recommendations.isEmpty()) {

            System.out.println("추천 가능한 후보자가 없습니다.");

        } else {

            for (int i = 0; i < recommendations.size(); i++) {

                RecommendationResult r =
                        recommendations.get(i);

                System.out.printf(
                        "%d. 개발자: %s (유사도: %.4f, 참조 이슈: %s)\n",
                        i + 1,
                        r.getFixerId(),
                        r.getScore(),
                        r.getMatchedIssueIds()
                );
            }
        }

        System.out.println(
                "\n===================================="
        );

        manager.logout();

        sc.close();
    }
}