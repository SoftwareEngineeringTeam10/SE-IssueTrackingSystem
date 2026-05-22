package org.issuetracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.issuetracker.model.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    private final File file = new File("projects.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Project> findAll() {
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(
                    file,
                    mapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    Project.class
                            )
            );
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveAll(List<Project> projects) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, projects);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}