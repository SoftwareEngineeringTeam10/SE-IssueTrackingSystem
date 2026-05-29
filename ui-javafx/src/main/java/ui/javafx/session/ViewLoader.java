package ui.javafx.session;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import ui.javafx.app.MainApp;

// 화면 전환 헬퍼 — FXML 로드 + Scene 교체
public class ViewLoader {

    public static void loadView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(
                ViewLoader.class.getResource(fxmlPath)
            );
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            MainApp.getPrimaryStage().setScene(scene);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T loadViewWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ViewLoader.class.getResource(fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            MainApp.getPrimaryStage().setScene(scene);
            return loader.getController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 공통 스타일시트 적용
    public static void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(
            ViewLoader.class.getResource("/css/application.css").toExternalForm()
        );
    }
}