package org.issuetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.issuetracker.model.Issue;
import org.issuetracker.model.IssueStatus;
import org.issuetracker.model.User;
import org.issuetracker.model.Role;

import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IssueRepository 파일 영속성 및 데이터 엑세스 단위 테스트")
class IssueRepositoryTest {

    private IssueService issueService;
    private ProjectService projectService;
    private String originalUserDir;
    private String testUserDir;
    private final int TEST_PROJECT_ID = 777;

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
        projectService = new ProjectService();
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    @DisplayName("영속성 검증: 이슈 생성 시 데이터 파일 적재 및 프로젝트별 식별 조회")
    void testRepositorySaveAndFind() {
        User admin = new User();
        admin.setId("admin01");
        admin.setRole(Role.ADMIN);
        projectService.addProject("Test Project 777", "Test Description", admin);

        List<org.issuetracker.model.Project> projs = projectService.getAllProjects();
        int realProjectId = TEST_PROJECT_ID;
        if (projs != null && !projs.isEmpty()) {
            realProjectId = projs.get(0).id;
        }

        Issue issue = new Issue();
        issue.title = "리포지토리 영속성 테스트 이슈";
        issue.status = IssueStatus.NEW;

        User dummy = new User();
        dummy.setId("test_runner");
        dummy.setRole(Role.TESTER);

        issueService.createIssue(realProjectId, issue, dummy);

        List<Issue> repoIssues = issueService.searchIssues(realProjectId, null, null, null, null);

        assertNotNull(repoIssues);
        assertTrue(repoIssues.size() >= 1, "Repository를 통해 저장된 데이터가 파일에서 정상 로드되어야 합니다.");
        assertEquals("리포지토리 영속성 테스트 이슈", repoIssues.get(0).title, "파일에 저장된 필드 값이 일치해야 합니다.");
    }
}