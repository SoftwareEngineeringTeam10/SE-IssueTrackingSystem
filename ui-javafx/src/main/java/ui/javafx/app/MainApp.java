package ui.javafx.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ui.javafx.session.ViewLoader;

public class MainApp extends Application {

    // 화면 전환 시 공유할 메인 Stage
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Pretendard 폰트 로드
        Font.loadFont(getClass().getResourceAsStream("/fonts/Pretendard-Regular.otf"), 13);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Pretendard-Bold.otf"), 14);

        MainApp.primaryStage = primaryStage;

        Parent root = FXMLLoader.load(
            getClass().getResource("/fxml/LoginView.fxml")
        );

        Scene scene = new Scene(root);
        ViewLoader.applyStylesheet(scene);

        primaryStage.setTitle("Issue Tracking System");
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}