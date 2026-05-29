package org.issuetracker.service;

import org.issuetracker.model.Project;
import org.issuetracker.model.User;
import org.issuetracker.repository.ProjectRepository;

import java.util.List;

public class ProjectService {

    private final ProjectRepository repository =
            new ProjectRepository();

    public void addProject(
            String name,
            String description,
            User currentUser
    ) {

        if (!PermissionManager.canManageUsers(currentUser)) {
            System.out.println("프로젝트 생성 권한이 없습니다");
            return;
        }

        List<Project> projects = repository.findAll();

        int nextId = projects.isEmpty()
                ? 1
                : projects.get(projects.size() - 1).id + 1;

        Project project =
                new Project(nextId, name, description);

        projects.add(project);
        repository.saveAll(projects);

        System.out.println("프로젝트가 생성되었습니다");
    }

    public Project getProjectById(int id) {

        for (Project p : repository.findAll()) {
            if (p.id == id) {
                return p;
            }
        }

        return null;
    }

    public List<Project> getAllProjects() {
        return repository.findAll();
    }

    // 프로젝트 목록 출력
    public void printProjects() {

        List<Project> projects = repository.findAll();

        if (projects.isEmpty()) {
            System.out.println("생성된 프로젝트가 없습니다");
            return;
        }

        System.out.println("\n=== 프로젝트 목록 ===");

        for (Project p : projects) {
            System.out.println(
                    p.id + " | " +
                            p.name + " | " +
                            p.description
            );
        }
    }


    public void resetAll() {
        repository.saveAll(new java.util.ArrayList<>());
    }
}