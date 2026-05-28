package ui.javafx.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.issuetracker.model.Project;
import org.issuetracker.model.Role;
import org.issuetracker.model.User;
import org.issuetracker.service.AccountManager;
import org.issuetracker.service.PermissionManager;
import org.issuetracker.service.ProjectService;
import ui.javafx.session.Session;
import ui.javafx.session.ViewLoader;

public class ManageController {

    @FXML private ComboBox<String> projectCombo;
    @FXML private Label userLabel;

    @FXML private TableView<User> accountTable;
    @FXML private TextField newIdField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newNameField;
    @FXML private ComboBox<String> roleCombo;

    @FXML private ListView<String> projectList;
    @FXML private TextField newProjectField;

    @FXML private ListView<String> activityLog;
    @FXML private Label systemMessage;

    private final AccountManager accountManager = new AccountManager();
    private final ProjectService projectService = new ProjectService();
    private final ObservableList<String> projects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 초기화 — admin 권한 확인 + 사용자 / 계정 테이블 / 역할 콤보 / 프로젝트 리스트
        if (Session.currentUser == null || Session.currentUser.getRole() != Role.ADMIN) {
            // admin 외 접근 시 — 자동 LoginView 전환
            ViewLoader.loadView("/fxml/LoginView.fxml");
            return;
        }

        userLabel.setText("User: " + Session.currentUser.getId());

        projectCombo.getItems().add("기본 프로젝트");
        projectCombo.getSelectionModel().selectFirst();
        projectCombo.setDisable(true);

        setupAccountTable();
        accountTable.setItems(FXCollections.observableArrayList(accountManager.getUsers()));

        ObservableList<String> roleNames = FXCollections.observableArrayList();
        for (Role r : Role.values()) {
            if (r == Role.ADMIN) continue;   // admin은 단일 계정이므로 추가 대상에서 제외
            roleNames.add(r.name());
        }
        roleCombo.setItems(roleNames);
        roleCombo.setValue("DEV");

        projectList.setItems(projects);
        reloadProjects();
    }

    @SuppressWarnings("unchecked")
    private void setupAccountTable() {
        // 0:ID / 1:Name / 2:Role
        var cols = accountTable.getColumns();
        ((TableColumn<User, String>) cols.get(0)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getId()));
        ((TableColumn<User, String>) cols.get(1)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getName()));
        ((TableColumn<User, String>) cols.get(2)).setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getRole() != null ? c.getValue().getRole().name() : ""));
    }

    @FXML
    public void onAddAccount() {
        // 계정 추가 핸들러
        String id = newIdField.getText() != null ? newIdField.getText().trim() : "";
        String pw = newPasswordField.getText() != null ? newPasswordField.getText().trim() : "";
        String name = newNameField.getText() != null ? newNameField.getText().trim() : "";
        String roleStr = roleCombo.getValue();

        if (id.isEmpty() || pw.isEmpty() || name.isEmpty() || roleStr == null) {
            systemMessage.setText("모든 항목을 입력해주세요");
            return;
        }

        // 중복 ID 사전 확인
        for (User u : accountManager.getUsers()) {
            if (u.getId().equals(id)) {
                new Alert(Alert.AlertType.WARNING, "중복된 ID입니다").showAndWait();
                return;
            }
        }

        User user = new User(id, pw, name, Role.valueOf(roleStr));
        accountManager.addUser(user);

        accountTable.setItems(FXCollections.observableArrayList(accountManager.getUsers()));

        newIdField.clear();
        newPasswordField.clear();
        newNameField.clear();
        roleCombo.getSelectionModel().clearSelection();

        systemMessage.setText("계정 추가 완료");
    }

    @FXML
    public void onAddProject() {
        // 프로젝트 추가 핸들러 — ProjectService로 영속 저장
        String name = newProjectField.getText() != null ? newProjectField.getText().trim() : "";
        if (name.isEmpty()) {
            systemMessage.setText("프로젝트 이름을 입력해주세요");
            return;
        }

        if (!PermissionManager.canManageUsers(Session.currentUser)) {
            systemMessage.setText("프로젝트 추가 권한이 없습니다");
            return;
        }

        projectService.addProject(name, "", Session.currentUser);
        reloadProjects();
        newProjectField.clear();
        systemMessage.setText("프로젝트 추가 완료");
    }

    private void reloadProjects() {
        // 저장된 프로젝트 목록 다시 로드
        projects.clear();
        for (Project p : projectService.getAllProjects()) {
            projects.add(p.name);
        }
    }

    @FXML
    public void onOpenList() {
        // 사이드 메뉴 — 이슈 목록 진입
        ViewLoader.loadView("/fxml/IssueListView.fxml");
    }

    @FXML
    public void onOpenRegister() {
        // 사이드 메뉴 — 이슈 등록 진입
        ViewLoader.loadView("/fxml/IssueRegisterView.fxml");
    }

    @FXML
    public void onOpenRecommend() {
        // 사이드 메뉴 — 목록으로 이동 후 추천 호출
        ViewLoader.loadView("/fxml/IssueListView.fxml");
    }

    @FXML
    public void onLogout() {
        // 로그아웃 핸들러
        Session.currentUser = null;
        ViewLoader.loadView("/fxml/LoginView.fxml");
    }

    @FXML
    public void onOpenStats() {
        // 사이드 메뉴 — 통계 화면 진입
        ViewLoader.loadView("/fxml/StatsView.fxml");
    }
}