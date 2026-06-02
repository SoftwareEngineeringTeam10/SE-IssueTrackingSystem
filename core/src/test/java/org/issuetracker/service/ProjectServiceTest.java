package org.issuetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.issuetracker.model.Project;
import org.issuetracker.model.User;
import org.issuetracker.model.Role;

import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectService 핵심 기능 및 영속성 단위 테스트")
class ProjectServiceTest {

    private ProjectService projectService;
    private User adminUser;
    private User devUser;

    private String originalUserDir;
    private String testUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        testUserDir = originalUserDir + File.separator + "test_sandbox";

        File sandboxDir = new File(testUserDir);
        if (!sandboxDir.exists()) {
            sandboxDir.mkdirs();
        }

        System.setProperty("user.dir", testUserDir);

        projectService = new ProjectService();

        adminUser = new User();
        adminUser.setId("admin01");
        adminUser.setPassword("1234");
        adminUser.setName("admin");
        adminUser.setRole(Role.ADMIN);

        devUser = new User();
        devUser.setId("dev01");
        devUser.setRole(Role.DEV);
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Nested
    @DisplayName("프로젝트 생성 권한 가드 및 실시간 영속화 검증")
    class ProjectCreationAndPersistence {

        @Test
        @DisplayName("정상 흐름: ADMIN 계정으로 신규 프로젝트 생성 및 실제 저장소 영속화 반영 검증")
        void testProjectCreationByAdmin() {
            List<Project> beforeList = projectService.getAllProjects();
            int beforeSize = beforeList != null ? beforeList.size() : 0;

            String newProjName = "JUnit Test Project " + System.currentTimeMillis();
            String newProjDesc = "소프트웨어 공학 단위 테스트용 프로젝트 설명문";

            projectService.addProject(newProjName, newProjDesc, adminUser);

            List<Project> afterList = projectService.getAllProjects();
            int afterSize = afterList != null ? afterList.size() : 0;

            assertEquals(beforeSize + 1, afterSize, "프로젝트가 정상 추가되어 전체 리스트 개수가 1개 늘어나야 합니다.");

            boolean isFound = false;
            for (Project p : afterList) {
                if (newProjName.equals(p.name)) {
                    isFound = true;
                    assertEquals(newProjDesc, p.description, "프로젝트의 설명문 내용과 일치해야 합니다.");
                    break;
                }
            }
            assertTrue(isFound, "JSON에서 새로 생성한 프로젝트 이름이 정밀 조회되어야 합니다.");
        }

        @Test
        @DisplayName("예외 테스트: 일반 개발자(DEV) 계정의 불법 프로젝트 생성 차단 검증")
        void testProjectCreationPermissionGuard() {
            List<Project> beforeList = projectService.getAllProjects();
            int beforeSize = beforeList != null ? beforeList.size() : 0;

            String illegalProjName = "DEV가 생성 시도한 프로젝트 " + System.currentTimeMillis();

            projectService.addProject(illegalProjName, "불법 생성 시도", devUser);

            List<Project> afterList = projectService.getAllProjects();
            int afterSize = afterList != null ? afterList.size() : 0;

            assertEquals(beforeSize, afterSize, "권한이 없는 사용자의 프로젝트 생성 요청은 거절되어 데이터가 오염되지 않아야 합니다.");
        }
    }
}