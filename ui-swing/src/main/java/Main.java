import javax.swing.*;
import org.view.MainFrame;
import org.controller.IssueController;
import org.issuetracker.service.IssueService;
import org.issuetracker.model.User;

public class Main {
    public static void main(String[] args) {

        IssueService issueService = new IssueService();

        SwingUtilities.invokeLater(() -> {
            // MainFrame 객체 생성
            MainFrame frame = new MainFrame(issueService);

            IssueController issueController = new IssueController(frame, issueService);

            // ----------------------------------------------------------------------
            // 임시 로그인 테스트
            /*
            User testUser = new User();
            testUser.setId("dev");
            testUser.setRole(org.issuetracker.model.Role.DEV);
            issueController.setCurrentUser(testUser);
            */
            // ----------------------------------------------------------------------

            // 화면에 창을 띄운다.
            frame.setVisible(true);
        });
    }
}