// service/StatisticsService.java
package org.issuetracker.service;

import org.issuetracker.model.Issue;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.model.Priority;
import org.issuetracker.model.User;
import org.issuetracker.repository.IssueRepository;

import java.util.*;

public class StatisticsService {

    private final IssueRepository repository =
            new IssueRepository();

    // 일별 이슈 등록 수
    public Map<String, Integer> getDailyStats() {
        List<Issue> issues = repository.findAll();
        Map<String, Integer> dailyMap = new TreeMap<>(); // 날짜순 정렬

        for (Issue issue : issues) {
            if (issue.reportedDate == null) continue;
            // "2025-05-18T13:45:00" → "2025-05-18"
            String day = issue.reportedDate.substring(0, 10);
            dailyMap.put(day, dailyMap.getOrDefault(day, 0) + 1);
        }
        return dailyMap;
    }

    // 월별 이슈 등록 수
    public Map<String, Integer> getMonthlyStats() {
        List<Issue> issues = repository.findAll();
        Map<String, Integer> monthlyMap = new TreeMap<>();

        for (Issue issue : issues) {
            if (issue.reportedDate == null) continue;
            // "2025-05-18T13:45:00" → "2025-05"
            String month = issue.reportedDate.substring(0, 7);
            monthlyMap.put(month, monthlyMap.getOrDefault(month, 0) + 1);
        }
        return monthlyMap;
    }

    // 상태별 이슈 수
    public Map<IssueStatus, Integer> getStatusStats() {
        List<Issue> issues = repository.findAll();
        Map<IssueStatus, Integer> statusMap = new LinkedHashMap<>();

        // 모든 상태 0으로 초기화 (없는 상태도 출력되게)
        for (IssueStatus s : IssueStatus.values()) {
            statusMap.put(s, 0);
        }
        for (Issue issue : issues) {
            statusMap.put(issue.status, statusMap.get(issue.status) + 1);
        }
        return statusMap;
    }

    // 우선순위별 이슈 수
    public Map<Priority, Integer> getPriorityStats() {
        List<Issue> issues = repository.findAll();
        Map<Priority, Integer> priorityMap = new LinkedHashMap<>();

        for (Priority p : Priority.values()) {
            priorityMap.put(p, 0);
        }
        for (Issue issue : issues) {
            if (issue.priority != null) {
                priorityMap.put(issue.priority, priorityMap.get(issue.priority) + 1);
            }
        }
        return priorityMap;
    }

    // 전체 통계 출력
    public void printAllStats(User currentUser) {
        if (currentUser == null) {
            System.out.println("로그인이 필요합니다");
            return;
        }
        System.out.println("\n=== 이슈 통계 분석 ===");

        // 일별
        System.out.println("\n[일별 이슈 등록 수]");
        Map<String, Integer> daily = getDailyStats();
        if (daily.isEmpty()) {
            System.out.println("  데이터 없음");
        } else {
            for (Map.Entry<String, Integer> e : daily.entrySet()) {
                System.out.println("  " + e.getKey() + " : " + buildBar(e.getValue()) + " (" + e.getValue() + "건)");
            }
        }

        // 월별
        System.out.println("\n[월별 이슈 등록 수]");
        Map<String, Integer> monthly = getMonthlyStats();
        if (monthly.isEmpty()) {
            System.out.println("  데이터 없음");
        } else {
            for (Map.Entry<String, Integer> e : monthly.entrySet()) {
                System.out.println("  " + e.getKey() + " : " + buildBar(e.getValue()) + " (" + e.getValue() + "건)");
            }
        }

        // 상태별
        System.out.println("\n[상태별 이슈 수]");
        for (Map.Entry<IssueStatus, Integer> e : getStatusStats().entrySet()) {
            System.out.println("  " + String.format("%-10s", e.getKey()) + " : " + buildBar(e.getValue()) + " (" + e.getValue() + "건)");
        }

        // 우선순위별
        System.out.println("\n[우선순위별 이슈 수]");
        for (Map.Entry<Priority, Integer> e : getPriorityStats().entrySet()) {
            System.out.println("  " + String.format("%-10s", e.getKey()) + " : " + buildBar(e.getValue()) + " (" + e.getValue() + "건)");
        }

        System.out.println("\n====================================");
    }

    // ASCII 막대 그래프 생성 (1건 = ■ 1개)
    private String buildBar(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("■");
        }
        return sb.toString();
    }
}