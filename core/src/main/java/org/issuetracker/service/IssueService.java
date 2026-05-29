package org.issuetracker.service;

import org.issuetracker.model.*;
import org.issuetracker.repository.IssueRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class IssueService {

    private final IssueRepository repository = new IssueRepository();

    public List<Issue> getAllIssues() {
        return repository.findAll();
    }

    // 프로젝트별 전체 이슈 조회
    public List<Issue> getIssuesByProject(int projectId) {
        return searchIssues(projectId, null, null, null, null);
    }

    /**
     * 이슈 생성
     * @param projectId  이슈를 등록할 프로젝트 ID
     * @param newIssue   생성할 이슈 객체
     * @param currentUser 현재 로그인 사용자
     * @return 성공 시 true, 실패 시 false
     */
    public boolean createIssue(int projectId, Issue newIssue, User currentUser) {
        if (!PermissionManager.canCreateIssue(currentUser)) {
            System.out.println("이슈 생성 권한이 없습니다");
            return false;
        }

        List<Issue> issues = repository.findAll();
        int nextId = issues.isEmpty() ? 1 : issues.get(issues.size() - 1).id + 1;
        newIssue.id = nextId;
        newIssue.projectId = projectId;
        newIssue.reporter = currentUser.getId();
        newIssue.reportedDate = java.time.LocalDateTime.now().toString();

        issues.add(newIssue);
        repository.saveAll(issues);
        System.out.println("이슈가 생성되었습니다 (ID: " + nextId + ", 프로젝트: " + projectId + ")");
        return true;
    }

    public Issue getIssueById(int id) {
        for (Issue issue : repository.findAll()) {
            if (issue.id == id) return issue;
        }
        return null;
    }

    public boolean addCommentToIssue(int issueId, Comment newComment, User currentUser) {
        if (currentUser == null) {
            System.out.println("로그인이 필요합니다");
            return false;
        }

        List<Issue> issues = repository.findAll();
        for (Issue issue : issues) {
            if (issue.id == issueId) {
                issue.addComment(newComment);
                repository.saveAll(issues);
                System.out.println("댓글이 추가되었습니다");
                return true;
            }
        }
        System.out.println("이슈를 찾을 수 없습니다 (ID: " + issueId + ")");
        return false;
    }

    /**
     * 이슈 검색
     * @param projectId  프로젝트 ID (필수 필터)
     * @param status     이슈 상태 문자열, null 이면 전체
     * @param assignee   담당자 ID, null 이면 전체
     * @param reporter   보고자 ID, null 이면 전체
     * @param keyword    제목/설명 키워드, null 이면 전체
     * @return 조건에 맞는 이슈 리스트
     */
    public List<Issue> searchIssues(int projectId, String status, String assignee,
                                    String reporter, String keyword) {
        List<Issue> allIssues = repository.findAll();
        List<Issue> result = new ArrayList<>();

        for (Issue issue : allIssues) {
            // 프로젝트 필터
            if (issue.projectId != projectId) continue;

            // 상태 필터
            if (status != null && !status.isEmpty()) {
                try {
                    if (issue.status != IssueStatus.valueOf(status.toUpperCase())) continue;
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }

            // 담당자 필터
            if (assignee != null && !assignee.isEmpty()) {
                if (issue.assignee == null || !issue.assignee.equalsIgnoreCase(assignee)) continue;
            }

            // 보고자 필터
            if (reporter != null && !reporter.isEmpty()) {
                if (issue.reporter == null || !issue.reporter.equalsIgnoreCase(reporter)) continue;
            }

            // 키워드 필터 (제목 또는 설명 포함 여부)
            if (keyword != null && !keyword.isEmpty()) {
                boolean titleMatch = issue.title != null && issue.title.contains(keyword);
                boolean descMatch  = issue.description != null && issue.description.contains(keyword);
                if (!titleMatch && !descMatch) continue;
            }

            result.add(issue);
        }
        return result;
    }

    public List<IssueStatus> getNextStates(IssueStatus currentStatus) {
        List<IssueStatus> nextStates = new ArrayList<>();

        switch (currentStatus) {
            case NEW:
                nextStates.add(IssueStatus.ASSIGNED);
                break;

            case ASSIGNED:
                nextStates.add(IssueStatus.FIXED);
                break;

            case FIXED:
                nextStates.add(IssueStatus.REOPENED);
                nextStates.add(IssueStatus.RESOLVED);
                break;

            case REOPENED:
                nextStates.add(IssueStatus.ASSIGNED);
                break;

            case RESOLVED:
                nextStates.add(IssueStatus.CLOSED);
                break;

            default:
                break;
        }

        return nextStates;
    }

    /**
     * 담당자 배정
     * @param issueId     대상 이슈 ID
     * @param assignee    배정할 개발자 ID
     * @param currentUser 현재 로그인 사용자 (PL 이상 필요)
     * @return 성공 시 true, 실패 시 false
     */
    public boolean assignIssue(int issueId, String assignee, User currentUser) {
        if (!PermissionManager.canAssignIssue(currentUser)) {
            System.out.println("담당자 배정 권한이 없습니다");
            return false;
        }

        List<Issue> issues = repository.findAll();
        for (Issue issue : issues) {
            if (issue.id == issueId) {
                if (issue.status != IssueStatus.NEW && issue.status != IssueStatus.REOPENED) {
                    System.out.println("현재 상태(" + issue.status + ")에서는 배정할 수 없습니다");
                    return false;
                }
                issue.assignee = assignee;
                issue.status = IssueStatus.ASSIGNED;
                repository.saveAll(issues);
                System.out.println("담당자가 배정되었습니다 (이슈: " + issueId + " → " + assignee + ")");
                return true;
            }
        }
        System.out.println("이슈를 찾을 수 없습니다 (ID: " + issueId + ")");
        return false;
    }

    /**
     * 이슈 상태 변경
     * @param issueId     대상 이슈 ID
     * @param newStatus   변경할 상태
     * @param currentUser 현재 로그인 사용자
     * @return 성공 시 true, 실패 시 false
     */
    public boolean changeStatus(int issueId, IssueStatus newStatus, User currentUser) {
        List<Issue> issues = repository.findAll();
        for (Issue issue : issues) {
            if (issue.id == issueId) {
                if (!isValidTransition(issue.status, newStatus)) {
                    System.out.println("유효하지 않은 상태 전환입니다 (" + issue.status + " → " + newStatus + ")");
                    return false;
                }
                if (!hasPermissionForStatus(newStatus, currentUser)) {
                    System.out.println("상태 변경 권한이 없습니다 (요청 상태: " + newStatus + ")");
                    return false;
                }

                issue.status = newStatus;
                if (newStatus == IssueStatus.FIXED)    issue.fixer    = currentUser.getId();
                if (newStatus == IssueStatus.REOPENED) issue.assignee = null;

                issue.addComment(new Comment(currentUser.getId(),
                        "상태가 " + newStatus + "으로 변경되었습니다."));
                repository.saveAll(issues);
                System.out.println("상태가 변경되었습니다 (이슈: " + issueId + " → " + newStatus + ")");
                return true;
            }
        }
        System.out.println("이슈를 찾을 수 없습니다 (ID: " + issueId + ")");
        return false;
    }

    private boolean isValidTransition(IssueStatus current, IssueStatus next) {
        if (current == IssueStatus.NEW      && next == IssueStatus.ASSIGNED) return true;
        if (current == IssueStatus.ASSIGNED && next == IssueStatus.FIXED)    return true;
        if (current == IssueStatus.FIXED    && next == IssueStatus.REOPENED) return true;
        if (current == IssueStatus.FIXED    && next == IssueStatus.RESOLVED) return true;
        if (current == IssueStatus.REOPENED && next == IssueStatus.ASSIGNED) return true;
        if (current == IssueStatus.RESOLVED && next == IssueStatus.CLOSED)   return true;
        return false;
    }

    private boolean hasPermissionForStatus(IssueStatus status, User user) {
        if (status == IssueStatus.FIXED)    return PermissionManager.canFixIssue(user);
        if (status == IssueStatus.REOPENED) return PermissionManager.canReopenIssue(user);
        if (status == IssueStatus.RESOLVED) return PermissionManager.canResolveIssue(user);
        if (status == IssueStatus.CLOSED)   return PermissionManager.canCloseIssue(user);
        return true;
    }

    public boolean printIssueDetail(int issueId, User currentUser) {
        if (!PermissionManager.canViewIssue(currentUser)) {
            System.out.println("이슈 조회 권한이 없습니다");
            return false;
        }
        Issue issue = getIssueById(issueId);
        if (issue == null) {
            System.out.println("이슈를 찾을 수 없습니다 (ID: " + issueId + ")");
            return false;
        }
        System.out.println("\n=== Issue Detail ===");
        System.out.println("ID: " + issue.id + " | Title: " + issue.title);
        System.out.println("Status: " + issue.status + " | Priority: " + issue.priority);
        System.out.println("Reporter: " + issue.reporter + " | Assignee: " + issue.assignee);
        System.out.println("Fixer: " + issue.fixer);
        System.out.println("Description: " + issue.description);
        if (issue.comments != null && !issue.comments.isEmpty()) {
            System.out.println("--- Comments (" + issue.comments.size() + ") ---");
            for (Comment c : issue.comments) {
                System.out.println("  [" + c.authorId + "] " + c.content);
            }
        }
        return true;
    }

    public Map<IssueStatus, Integer> getStatusTotals(int projectId) {
        List<Issue> issues = repository.findAll();

        Map<IssueStatus, Integer> result = new LinkedHashMap<>();

        for (IssueStatus status : IssueStatus.values()) {
            result.put(status, 0);
        }

        for (Issue issue : issues) {
            if (issue.projectId != projectId) continue;

            result.put(
                    issue.status,
                    result.get(issue.status) + 1
            );
        }

        return result;
    }

    public List<RecommendationResult> getAssigneeRecommendations(int issueId) {
        Issue target = getIssueById(issueId);
        if (target == null) return new ArrayList<>();

        List<Issue> historicalIssues = new ArrayList<>();
        for (Issue i : getAllIssues()) {
            if (i.status == IssueStatus.FIXED
                    || i.status == IssueStatus.RESOLVED
                    || i.status == IssueStatus.CLOSED) {
                historicalIssues.add(i);
            }
        }

        return new RecommendationServiceImpl().recommend(target, historicalIssues);
    }
}