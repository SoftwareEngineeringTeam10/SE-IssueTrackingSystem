package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class IssueListController {

    @FXML private ComboBox<String> projectCombo;
    @FXML private Label userLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private TableView<?> issueTable;
    @FXML private ListView<String> activityLog;
    @FXML private Label systemMessage;

    @FXML
    public void initialize() {
        // 초기화 영역 — 필터 ComboBox 채우기 + 백엔드 머지 후 데이터 로드
    }

    @FXML
    public void onSearch() {
        // 검색 핸들러
    }

    @FXML
    public void onOpenRegister() {
        // 이슈 등록 화면 진입 핸들러
    }

    @FXML
    public void onOpenDetail() {
        // 이슈 상세 화면 진입 핸들러
    }

    @FXML
    public void onRecommendAssignee() {
        // 담당자 추천 호출 핸들러
    }

    @FXML
    public void onLogout() {
        // 로그아웃 핸들러
    }
}