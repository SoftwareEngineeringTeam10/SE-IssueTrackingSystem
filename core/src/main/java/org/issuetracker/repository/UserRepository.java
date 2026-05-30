package org.issuetracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.issuetracker.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final String filePath = System.getProperty("user.dir") + File.separator + "users.json";
    private final File file = new File(filePath);
    private final ObjectMapper mapper = new ObjectMapper();

    public List<User> findAll() {
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(
                    file,
                    mapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    User.class
                            )
            );
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveAll(List<User> users) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}