import javax.swing.*;
import org.example.view.swing.MainFrame;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            //  MainFrame 객체 생성
            MainFrame frame = new MainFrame();

            // 화면에 창을 띄운다.
            frame.setVisible(true);
        });
    }
}