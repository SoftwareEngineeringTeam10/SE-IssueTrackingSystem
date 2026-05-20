package org.issuetracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.issuetracker.model.Issue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IssueRepository {

    private final File file = new File("issues.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Issue> findAll() {

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {

            return mapper.readValue(
                    file,
                    mapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    Issue.class
                            )
            );

        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveAll(List<Issue> issues) {

        try {

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, issues);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}