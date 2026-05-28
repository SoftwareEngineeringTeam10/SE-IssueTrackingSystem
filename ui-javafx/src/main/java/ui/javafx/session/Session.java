package ui.javafx.session;

import org.issuetracker.model.User;

// 화면 간 공유 상태 — UI 세션 데이터
public class Session {

    public static User currentUser;
    public static int selectedIssueId;
    public static int currentProjectId = 1;
}