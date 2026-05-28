package org.issuetracker.service;

import org.issuetracker.model.Comment;
import org.issuetracker.model.Issue;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.model.RecommendationResult;
import org.issuetracker.model.User;
import org.issuetracker.repository.IssueRepository;
import org.issuetracker.model.Priority;
import org.issuetracker.model.Project;

import java.util.ArrayList;
import java.util.List;

public class IssueService {

    private final IssueRepository repository =
            new IssueRepository();

    private final ProjectService projectService =
            new ProjectService();

    // 전체 이슈 조회
    public List<Issue> getAllIssues() {
        return repository.findAll();
    }

    // 이슈 생성
    public void createIssue(int projectId,
                            Issue newIssue,
                            User currentUser) {
        if (newIssue.title == null || newIssue.title.trim().isEmpty()) {
            System.out.println("제목은 필수입니다");
            return;
        }

        if (newIssue.description == null || newIssue.description.trim().isEmpty()) {
            System.out.println("설명은 필수입니다");
            return;
        }

        Project project =
                projectService.getProjectById(projectId);

        if (project == null) {
            System.out.println("존재하지 않는 프로젝트입니다");
            return;
        }

        if (!PermissionManager.canCreateIssue(currentUser)) {
            System.out.println("이슈 생성 권한이 없습니다");
            return;
        }

        List<Issue> issues = repository.findAll();

        int nextId = issues.isEmpty()
                ? 1
                : issues.get(issues.size() - 1).id + 1;

        newIssue.id = nextId;
        newIssue.projectId = projectId;
        newIssue.reporter = currentUser.getId();
        newIssue.reportedDate = java.time.LocalDateTime.now().toString();

        issues.add(newIssue);
        repository.saveAll(issues);

        System.out.println("이슈가 생성되었습니다");
    }

    // ID로 이슈 조회
    public Issue getIssueById(int id) {
        for (Issue issue : repository.findAll()) {
            if (issue.id == id) return issue;
        }
        return null;
    }

    // 댓글 추가
    public void addCommentToIssue(int issueId, Comment newComment, User currentUser) {
        // 권한: All 허용 (로그인만 되어 있으면 됨)
        if (currentUser == null) {
            System.out.println("로그인이 필요합니다");
            return;
        }

        List<Issue> issues = repository.findAll();
        for (Issue issue : issues) {
            if (issue.id == issueId) {
                issue.addComment(newComment);
                break;
            }
        }
        repository.saveAll(issues);
        System.out.println("댓글이 추가되었습니다");
    }

    // 이슈 검색/필터링 (All 허용)
    public List<Issue> searchIssues(int projectId,
                                    String status,
                                    String assignee,
                                    String reporter,
                                    String priority
    ) {
        List<Issue> allIssues = repository.findAll();
        List<Issue> result = new ArrayList<>();

        for (Issue issue : allIssues) {
            if (issue.projectId != projectId) {
                continue;
            }

            boolean matches = true;

            // status 필터 (enum 이름으로 비교)
            if (status != null && !status.isEmpty()) {
                try {
                    IssueStatus filterStatus = IssueStatus.valueOf(status.toUpperCase());
                    if (issue.status != filterStatus) matches = false;
                } catch (IllegalArgumentException e) {
                    matches = false; // 잘못된 status 값이면 탈락
                }
            }

            if (assignee != null && !assignee.isEmpty()) {
                if (issue.assignee == null || !issue.assignee.equalsIgnoreCase(assignee))
                    matches = false;
            }

            if (reporter != null && !reporter.isEmpty()) {
                if (issue.reporter == null || !issue.reporter.equalsIgnoreCase(reporter))
                    matches = false;
            }

            if (priority != null && !priority.isEmpty()) {

                try {
                    Priority filterPriority =
                            Priority.valueOf(priority.toUpperCase());

                    if (issue.priority != filterPriority) {
                        matches = false;
                    }

                } catch (IllegalArgumentException e) {
                    matches = false;
                }
            }

            if (matches) result.add(issue);
        }
        return result;
    }

    // 담당자 배정
    public void assignIssue(int issueId, String assignee, User currentUser) {
        if (!PermissionManager.canAssignIssue(currentUser)) {
            System.out.println("담당자 배정 권한이 없습니다");
            return;
        }

        List<Issue> issues = repository.findAll();
        for (Issue issue : issues) {
            if (issue.id == issueId) {
                if (issue.status != IssueStatus.NEW && issue.status != IssueStatus.REOPENED) {
                    System.out.println("현재 상태에서는 배정할 수 없습니다");
                    return;
                }
                issue.assignee = assignee;
                issue.status = IssueStatus.ASSIGNED;
                System.out.println("담당자가 배정되었습니다");
                break;
            }
        }
        repository.saveAll(issues);
    }

    // 상태 변경
    public void changeStatus(int issueId, IssueStatus newStatus, User currentUser) {
        List<Issue> issues = repository.findAll();

        for (Issue issue : issues) {
            if (issue.id == issueId) {

                if (!isValidTransition(issue.status, newStatus)) {
                    System.out.println("잘못된 상태 전이입니다");
                    return;
                }

                if (!hasPermissionForStatus(newStatus, currentUser)) {
                    System.out.println("상태 변경 권한이 없습니다");
                    return;
                }

                issue.status = newStatus;

                if (newStatus == IssueStatus.FIXED) {
                    issue.fixer = currentUser.getId();
                }
                if (newStatus == IssueStatus.REOPENED) {
                    issue.assignee = null;
                }

                // 상태 변경 시 코멘트 자동 추가
                Comment autoComment = new Comment(
                        currentUser.getId(),
                        "상태가 " + newStatus + "으로 변경되었습니다."
                );
                issue.addComment(autoComment);

                System.out.println("상태가 변경되었습니다");
                break;
            }
        }
        repository.saveAll(issues);
    }

    private boolean isValidTransition(IssueStatus current, IssueStatus next) {
        if (current == IssueStatus.NEW && next == IssueStatus.ASSIGNED) return true;
        if (current == IssueStatus.ASSIGNED && next == IssueStatus.FIXED) return true;
        if (current == IssueStatus.FIXED && next == IssueStatus.REOPENED) return true;
        if (current == IssueStatus.FIXED && next == IssueStatus.RESOLVED) return true;
        if (current == IssueStatus.REOPENED && next == IssueStatus.ASSIGNED) return true;
        if (current == IssueStatus.RESOLVED && next == IssueStatus.CLOSED) return true;
        return false;
    }

    private boolean hasPermissionForStatus(IssueStatus status, User user) {
        if (status == IssueStatus.FIXED) return PermissionManager.canFixIssue(user);
        if (status == IssueStatus.REOPENED) return PermissionManager.canReopenIssue(user);
        if (status == IssueStatus.RESOLVED) return PermissionManager.canResolveIssue(user);
        if (status == IssueStatus.CLOSED) return PermissionManager.canCloseIssue(user);
        return true;
    }

    // IssueService에 추가
    public void printIssueDetail(int issueId, User currentUser) {
        if (!PermissionManager.canViewIssue(currentUser)) {
            System.out.println("권한이 없습니다");
            return;
        }

        Issue issue = getIssueById(issueId);
        if (issue == null) {
            System.out.println("이슈를 찾을 수 없습니다");
            return;
        }

        System.out.println("\n=== Issue Detail ===");
        System.out.println("ID       : " + issue.id);
        System.out.println("Title    : " + issue.title);
        System.out.println("Desc     : " + issue.description);
        System.out.println("Reporter : " + issue.reporter);
        System.out.println("Date     : " + issue.reportedDate);
        System.out.println("Assignee : " + issue.assignee);
        System.out.println("Fixer    : " + issue.fixer);
        System.out.println("Priority : " + issue.priority);
        System.out.println("Status   : " + issue.status);
        System.out.println("\n=== Comments ===");
        for (Comment c : issue.comments) {
            System.out.println("[" + c.date + "] " + c.authorId + " : " + c.content);
        }
    }

    // 담당자 자동 추천 기능
    public List<RecommendationResult> getAssigneeRecommendations(int issueId) {
        // 1. 현재 추천을 받고자 하는 타겟 이슈를 가져옴
        Issue target = getIssueById(issueId);
        if (target == null) return new ArrayList<>();

        // 2. 알고리즘이 참고할 과거 해결 완료된 데이터(FIXED, RESOLVED, CLOSED)만 필터링
        List<Issue> historicalIssues = new ArrayList<>();
        for (Issue i : getAllIssues()) {
            if (i.status == IssueStatus.FIXED ||
                    i.status == IssueStatus.RESOLVED ||
                    i.status == IssueStatus.CLOSED) {
                historicalIssues.add(i);
            }
        }

        // 3. TF-IDF 추천 기계를 가동
        RecommendationService recommender = new RecommendationServiceImpl();

        // 4. 알고리즘 연산 결과 반환
        return recommender.recommend(target, historicalIssues);
    }
}