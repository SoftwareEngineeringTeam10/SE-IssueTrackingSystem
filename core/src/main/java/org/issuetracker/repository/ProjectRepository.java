package org.issuetracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.issuetracker.model.Project;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    // 실행 위치 관계없이 루트 폴더 기준으로 절대 경로 고정
    private final String filePath = System.getProperty("user.dir") + File.separator + "projects.json";
    private final File file = new File(filePath);
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Project> findAll() {
        if (!file.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Project.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveAll(List<Project> projects) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, projects);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}