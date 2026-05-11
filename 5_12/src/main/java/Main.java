public class Main {

    public static void main(String[] args) {

        AccountManager manager =
                new AccountManager();

        // 계정 생성
        manager.addUser(
                new User(
                        "admin01",
                        "1234",
                        "admin",
                        Role.ADMIN
                )
        );

        manager.addUser(
                new User(
                        "tester01",
                        "1111",
                        "tester",
                        Role.TESTER
                )
        );

        // 로그인
        manager.login(
                "tester01",
                "1111"
        );

        // 현재 로그인 사용자
        User current =
                manager.getCurrentUser();

        System.out.println(
                "Current User : "
                        + current.getName()
        );

        // 권한 테스트
        System.out.println(
                "Can Create Issue : "
                        + PermissionManager
                        .canCreateIssue(current)
        );
    }
}