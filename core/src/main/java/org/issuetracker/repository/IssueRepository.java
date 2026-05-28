package org.issuetracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.issuetracker.model.Issue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IssueRepository {

    // [수정된 부분] 상대 경로 대신 프로젝트 루트를 기준으로 절대 경로를 생성합니다.
    private final String filePath = System.getProperty("user.dir") + File.separator + "issues.json";
    private final File file = new File(filePath);
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Issue> findAll() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(
                    file,
                    mapper.getTypeFactory()
                            .constructCollectionType(List.class, Issue.class)
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

    /**
     * 앱 시작 시 issues.json 을 완전히 초기화합니다.
     */
    public void clear() {
        saveAll(new ArrayList<>());
    }
}