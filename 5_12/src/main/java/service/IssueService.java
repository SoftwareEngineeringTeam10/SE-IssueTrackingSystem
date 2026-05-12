package service;

import model.Comment;
import model.Issue;
import model.User;
import repository.IssueRepository;

import java.util.List;

public class IssueService {

    private final IssueRepository repository =
            new IssueRepository();

    public List<Issue> getAllIssues() {
        return repository.findAll();
    }

    public void createIssue(
            Issue newIssue,
            User currentUser
    ) {

        // 권한 체크
        if(!PermissionManager.canCreateIssue(currentUser)) {

            System.out.println(
                    "No Permission To Create Issue"
            );

            return;
        }

        List<Issue> issues =
                repository.findAll();

        int nextId =
                issues.isEmpty()
                        ? 1
                        : issues.get(
                        issues.size() - 1
                ).id + 1;

        newIssue.id = nextId;

        // reporter 자동 등록
        newIssue.reporter =
                currentUser.getName();

        // 날짜 자동 등록
        newIssue.reportedDate =
                java.time.LocalDateTime.now()
                        .toString();

        issues.add(newIssue);

        repository.saveAll(issues);

        System.out.println(
                "Issue Created Successfully"
        );
    }

    public Issue getIssueById(int id) {

        List<Issue> issues =
                repository.findAll();

        for (Issue issue : issues) {

            if (issue.id == id) {
                return issue;
            }
        }

        return null;
    }

    public void addCommentToIssue(
            int issueId,
            Comment newComment
    ) {

        List<Issue> issues =
                repository.findAll();

        for (Issue issue : issues) {

            if (issue.id == issueId) {

                issue.addComment(newComment);

                break;
            }
        }

        repository.saveAll(issues);
    }
}