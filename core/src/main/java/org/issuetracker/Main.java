package org.issuetracker;

// ============================================================
//  완전 독립 실행 버전 – 외부 JSON / Scanner 입력 없음
//  필요한 모든 클래스(model, service, repository, stub)를
//  이 파일 하나에 내장했습니다.
//  실행: javac Main_hardcoded.java && java org.issuetracker.Main
// ============================================================

import java.util.*;
import java.time.LocalDateTime;

// ─────────────────── Enum 정의 ────────────────────────────────
enum Role        { ADMIN, PL, DEV, TESTER }
enum Priority    { BLOCKER, CRITICAL, MAJOR, MINOR, TRIVIAL }
enum IssueStatus { NEW, ASSIGNED, FIXED, RESOLVED, CLOSED, REOPENED }

// ─────────────────── Model ────────────────────────────────────
class User {
    private String id, password, name;
    private Role role;
    public User(String id, String password, String name, Role role) {
        this.id = id; this.password = password;
        this.name = name; this.role = role;
    }
    public String getId()       { return id; }
    public String getPassword() { return password; }
    public String getName()     { return name; }
    public Role   getRole()     { return role; }
}

class Comment {
    public String authorId, content, date;
    public Comment(String authorId, String content) {
        this.authorId = authorId;
        this.content  = content;
        this.date     = LocalDateTime.now().toString();
    }
}

class Issue {
    public int id, projectId;
    public String title, description, reporter, reportedDate, fixer, assignee;
    public Priority    priority = Priority.MAJOR;
    public IssueStatus status   = IssueStatus.NEW;
    public List<Comment> comments = new ArrayList<>();
    public Issue() {}
    public void addComment(Comment c) { this.comments.add(c); }
}

class Project {
    public int id;
    public String name, description;
    public Project(int id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }
}

class RecommendationResult {
    private String fixerId;
    private double score;
    private List<Integer> matchedIssueIds;
    public RecommendationResult(String fixerId, double score, List<Integer> matchedIssueIds) {
        this.fixerId = fixerId; this.score = score; this.matchedIssueIds = matchedIssueIds;
    }
    public String        getFixerId()        { return fixerId; }
    public double        getScore()          { return score; }
    public List<Integer> getMatchedIssueIds(){ return matchedIssueIds; }
}

// ─────────────────── PermissionManager ───────────────────────
class PermissionManager {
    public static boolean canCreateIssue  (User u) { return u != null && (u.getRole()==Role.TESTER||u.getRole()==Role.DEV||u.getRole()==Role.PL||u.getRole()==Role.ADMIN); }
    public static boolean canAssignIssue  (User u) { return u != null && (u.getRole()==Role.PL||u.getRole()==Role.ADMIN); }
    public static boolean canFixIssue     (User u) { return u != null && (u.getRole()==Role.DEV||u.getRole()==Role.ADMIN); }
    public static boolean canReopenIssue  (User u) { return u != null && (u.getRole()==Role.TESTER||u.getRole()==Role.PL||u.getRole()==Role.ADMIN); }
    public static boolean canResolveIssue (User u) { return u != null && (u.getRole()==Role.TESTER||u.getRole()==Role.PL||u.getRole()==Role.ADMIN); }
    public static boolean canCloseIssue   (User u) { return u != null && (u.getRole()==Role.PL||u.getRole()==Role.ADMIN); }
    public static boolean canViewIssue    (User u) { return u != null; }
    public static boolean canManageUsers  (User u) { return u != null && (u.getRole()==Role.ADMIN||u.getRole()==Role.PL); }
}

// ─────────────────── In-Memory Repository ────────────────────
class IssueRepository {
    private final List<Issue> store = new ArrayList<>();
    public List<Issue> findAll()            { return new ArrayList<>(store); }
    public void saveAll(List<Issue> issues) { store.clear(); store.addAll(issues); }
    public void clear()                     { store.clear(); }
}

class ProjectRepository {
    private final List<Project> store = new ArrayList<>();
    public List<Project> findAll()               { return new ArrayList<>(store); }
    public void saveAll(List<Project> projects)  { store.clear(); store.addAll(projects); }
}

// ─────────────────── AccountManager ──────────────────────────
class AccountManager {
    private final List<User> users = new ArrayList<>();
    private User currentUser;

    public void addUser(User user) {
        for (User u : users) if (u.getId().equals(user.getId())) { System.out.println("중복 ID: " + user.getId()); return; }
        users.add(user);
        System.out.println(user.getName() + " 계정 생성");
    }
    public boolean login(String id, String pw) {
        for (User u : users) if (u.getId().equals(id) && u.getPassword().equals(pw)) {
            currentUser = u;
            System.out.println("  [로그인] " + u.getName() + " (" + u.getRole() + ")");
            return true;
        }
        System.out.println("  [로그인 실패] " + id); return false;
    }
    public void logout()           { currentUser = null; }
    public User getCurrentUser()   { return currentUser; }
    public List<User> getUsers()   { return users; }
    public void resetAll()         { users.clear(); }
}

// ─────────────────── Recommendation (TF-IDF 유사도) ──────────
class RecommendationServiceImpl {
    public List<RecommendationResult> recommend(Issue target, List<Issue> history) {
        String[] targetTokens = tokenize(target.title + " " + target.description);

        // fixer -> (총점, 매칭 이슈 ID 목록)
        Map<String, Double> scoreMap  = new LinkedHashMap<>();
        Map<String, List<Integer>> matchMap = new LinkedHashMap<>();

        for (Issue h : history) {
            if (h.fixer == null || h.fixer.isEmpty()) continue;
            String[] hTokens = tokenize(h.title + " " + h.description);
            double sim = cosineSim(targetTokens, hTokens);
            if (sim <= 0) continue;

            scoreMap.merge(h.fixer, sim, Double::sum);
            matchMap.computeIfAbsent(h.fixer, k -> new ArrayList<>()).add(h.id);
        }

        // 점수 내림차순 정렬 후 Top3
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(scoreMap.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<RecommendationResult> result = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            String fixer = sorted.get(i).getKey();
            result.add(new RecommendationResult(fixer, sorted.get(i).getValue(), matchMap.get(fixer)));
        }
        return result;
    }

    private String[] tokenize(String text) {
        return text.toLowerCase().replaceAll("[^\\w가-힣]", " ").trim().split("\\s+");
    }

    private double cosineSim(String[] a, String[] b) {
        Map<String, Integer> fa = freq(a), fb = freq(b);
        Set<String> vocab = new HashSet<>(fa.keySet()); vocab.addAll(fb.keySet());
        double dot = 0, na = 0, nb = 0;
        for (String w : vocab) {
            int va = fa.getOrDefault(w, 0), vb = fb.getOrDefault(w, 0);
            dot += va * vb; na += va * va; nb += vb * vb;
        }
        return (na == 0 || nb == 0) ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private Map<String, Integer> freq(String[] tokens) {
        Map<String, Integer> m = new HashMap<>();
        for (String t : tokens) if (!t.isEmpty()) m.merge(t, 1, Integer::sum);
        return m;
    }
}

// ─────────────────── IssueService ────────────────────────────
class IssueService {
    private final IssueRepository repository = new IssueRepository();

    public List<Issue> getAllIssues() { return repository.findAll(); }

    public boolean createIssue(int projectId, Issue newIssue, User user) {
        if (!PermissionManager.canCreateIssue(user)) { System.out.println("  이슈 생성 권한 없음"); return false; }
        List<Issue> issues = repository.findAll();
        newIssue.id          = issues.isEmpty() ? 1 : issues.get(issues.size()-1).id + 1;
        newIssue.projectId   = projectId;
        newIssue.reporter    = user.getId();
        newIssue.reportedDate= LocalDateTime.now().toString();
        issues.add(newIssue);
        repository.saveAll(issues);
        System.out.println("  이슈 생성 완료 (ID:" + newIssue.id + " / " + newIssue.title + ")");
        return true;
    }

    public Issue getIssueById(int id) {
        for (Issue i : repository.findAll()) if (i.id == id) return i;
        return null;
    }

    public boolean addCommentToIssue(int issueId, Comment comment, User user) {
        if (user == null) { System.out.println("  로그인 필요"); return false; }
        List<Issue> issues = repository.findAll();
        for (Issue i : issues) if (i.id == issueId) {
            i.addComment(comment);
            repository.saveAll(issues);
            System.out.println("  댓글 추가 완료");
            return true;
        }
        System.out.println("  이슈 없음 (ID:" + issueId + ")"); return false;
    }

    public boolean assignIssue(int issueId, String assignee, User user) {
        if (!PermissionManager.canAssignIssue(user)) { System.out.println("  배정 권한 없음"); return false; }
        List<Issue> issues = repository.findAll();
        for (Issue i : issues) if (i.id == issueId) {
            if (i.status != IssueStatus.NEW && i.status != IssueStatus.REOPENED) {
                System.out.println("  현재 상태(" + i.status + ")에서는 배정 불가"); return false;
            }
            i.assignee = assignee;
            i.status   = IssueStatus.ASSIGNED;
            repository.saveAll(issues);
            System.out.println("  담당자 배정: 이슈" + issueId + " → " + assignee);
            return true;
        }
        System.out.println("  이슈 없음 (ID:" + issueId + ")"); return false;
    }

    public boolean changeStatus(int issueId, IssueStatus newStatus, User user) {
        List<Issue> issues = repository.findAll();
        for (Issue i : issues) if (i.id == issueId) {
            if (!isValidTransition(i.status, newStatus)) {
                System.out.println("  유효하지 않은 전환: " + i.status + " → " + newStatus); return false;
            }
            if (!hasPermission(newStatus, user)) {
                System.out.println("  상태 변경 권한 없음 (" + newStatus + ")"); return false;
            }
            i.status = newStatus;
            if (newStatus == IssueStatus.FIXED)    { i.fixer = user.getId(); }
            if (newStatus == IssueStatus.REOPENED) { i.assignee = null; }
            i.addComment(new Comment(user.getId(), "상태 변경 → " + newStatus));
            repository.saveAll(issues);
            System.out.println("  상태 변경: 이슈" + issueId + " → " + newStatus);
            return true;
        }
        System.out.println("  이슈 없음 (ID:" + issueId + ")"); return false;
    }

    private boolean isValidTransition(IssueStatus cur, IssueStatus nxt) {
        if (cur==IssueStatus.NEW      && nxt==IssueStatus.ASSIGNED) return true;
        if (cur==IssueStatus.ASSIGNED && nxt==IssueStatus.FIXED)    return true;
        if (cur==IssueStatus.FIXED    && nxt==IssueStatus.REOPENED) return true;
        if (cur==IssueStatus.FIXED    && nxt==IssueStatus.RESOLVED) return true;
        if (cur==IssueStatus.REOPENED && nxt==IssueStatus.ASSIGNED) return true;
        if (cur==IssueStatus.RESOLVED && nxt==IssueStatus.CLOSED)   return true;
        return false;
    }
    private boolean hasPermission(IssueStatus s, User u) {
        if (s==IssueStatus.FIXED)    return PermissionManager.canFixIssue(u);
        if (s==IssueStatus.REOPENED) return PermissionManager.canReopenIssue(u);
        if (s==IssueStatus.RESOLVED) return PermissionManager.canResolveIssue(u);
        if (s==IssueStatus.CLOSED)   return PermissionManager.canCloseIssue(u);
        return true;
    }

    public List<Issue> searchIssues(int projectId, String status, String assignee, String reporter, String keyword) {
        List<Issue> result = new ArrayList<>();
        for (Issue i : repository.findAll()) {
            if (i.projectId != projectId) continue;
            if (status   != null && !status.isEmpty()   && i.status != IssueStatus.valueOf(status.toUpperCase())) continue;
            if (assignee != null && !assignee.isEmpty() && !assignee.equalsIgnoreCase(i.assignee)) continue;
            if (reporter != null && !reporter.isEmpty() && (i.reporter==null||!reporter.equalsIgnoreCase(i.reporter))) continue;
            if (keyword  != null && !keyword.isEmpty()) {
                boolean t = i.title!=null && i.title.contains(keyword);
                boolean d = i.description!=null && i.description.contains(keyword);
                if (!t && !d) continue;
            }
            result.add(i);
        }
        return result;
    }

    public boolean printIssueDetail(int issueId, User user) {
        if (!PermissionManager.canViewIssue(user)) { System.out.println("조회 권한 없음"); return false; }
        Issue i = getIssueById(issueId);
        if (i == null) { System.out.println("이슈 없음 (ID:" + issueId + ")"); return false; }
        System.out.println("\n=== Issue Detail ===");
        System.out.println("ID: " + i.id + " | Title: " + i.title);
        System.out.println("Status: " + i.status + " | Priority: " + i.priority);
        System.out.println("Reporter: " + i.reporter + " | Assignee: " + i.assignee + " | Fixer: " + i.fixer);
        System.out.println("Description: " + i.description);
        if (!i.comments.isEmpty()) {
            System.out.println("--- Comments (" + i.comments.size() + ") ---");
            for (Comment c : i.comments) System.out.println("  [" + c.authorId + "] " + c.content);
        }
        return true;
    }

    public List<RecommendationResult> getAssigneeRecommendations(int issueId) {
        Issue target = getIssueById(issueId);
        if (target == null) return new ArrayList<>();
        List<Issue> history = new ArrayList<>();
        for (Issue i : getAllIssues())
            if (i.status==IssueStatus.FIXED||i.status==IssueStatus.RESOLVED||i.status==IssueStatus.CLOSED)
                history.add(i);
        return new RecommendationServiceImpl().recommend(target, history);
    }
}

// ─────────────────── ProjectService ──────────────────────────
class ProjectService {
    private final ProjectRepository repository = new ProjectRepository();

    public void addProject(String name, String description, User user) {
        if (!PermissionManager.canManageUsers(user)) { System.out.println("프로젝트 생성 권한 없음"); return; }
        List<Project> projects = repository.findAll();
        int nextId = projects.isEmpty() ? 1 : projects.get(projects.size()-1).id + 1;
        projects.add(new Project(nextId, name, description));
        repository.saveAll(projects);
        System.out.println("  프로젝트 생성: [" + nextId + "] " + name);
    }

    public void printProjects() {
        List<Project> ps = repository.findAll();
        System.out.println("\n=== 프로젝트 목록 ===");
        for (Project p : ps) System.out.println("  " + p.id + " | " + p.name + " | " + p.description);
    }

    public void resetAll() { repository.saveAll(new ArrayList<>()); }
}

// ─────────────────── StatisticsService (stub) ────────────────
class StatisticsService {
    public void printAllStats(User user) {
        System.out.println("\n=== 이슈 통계 (stub) ===");
        System.out.println("  [통계 기능은 GUI/Web UI에서 차트로 표시됩니다]");
    }
}

// ─────────────────── Main ────────────────────────────────────
public class Main {
    public static void main(String[] args) {

        AccountManager manager        = new AccountManager();
        IssueService   issueService   = new IssueService();
        ProjectService projectService = new ProjectService();

        // ① 데이터 초기화
        manager.resetAll();
        projectService.resetAll();

        // ② 사용자 시드 (18명: admin1 + PL2 + dev10 + tester5)
        System.out.println("\n======= [1] 계정 생성 =======");
        manager.addUser(new User("admin01",  "1234", "어드민",  Role.ADMIN));
        manager.addUser(new User("pl01",     "2222", "PL1",    Role.PL));
        manager.addUser(new User("pl02",     "2222", "PL2",    Role.PL));
        for (int i = 1; i <= 10; i++)
            manager.addUser(new User(String.format("dev%02d", i), "3333", "DEV" + i, Role.DEV));
        for (int i = 1; i <= 5; i++)
            manager.addUser(new User(String.format("tester%02d", i), "1111", "TESTER" + i, Role.TESTER));

        // ③ 프로젝트 생성 (admin)
        System.out.println("\n======= [2] 프로젝트 생성 =======");
        manager.login("admin01", "1234");
        User adminUser = manager.getCurrentUser();
        projectService.addProject("ITS Main Project", "메인 이슈 관리 프로젝트", adminUser);
        projectService.addProject("Mobile App",       "모바일 앱 프로젝트",      adminUser);
        manager.logout();

        projectService.printProjects();

        // ④ 프로젝트 ID 하드코딩 (Scanner 제거)
        int selectedProjectId = 1;
        System.out.println("\n[선택된 프로젝트 ID: " + selectedProjectId + " (하드코딩)]");

        // ⑤ tester01: 이슈1 생성 + 댓글
        System.out.println("\n======= [3] tester01 → 이슈 생성 & 댓글 =======");
        manager.login("tester01", "1111");
        User testerUser = manager.getCurrentUser();
        System.out.println("  이슈 생성 권한: " + PermissionManager.canCreateIssue(testerUser));

        Issue issue1 = new Issue();
        issue1.title       = "Login Error";
        issue1.description = "로그인 버튼이 작동하지 않음";
        issue1.priority    = Priority.CRITICAL;
        if (!issueService.createIssue(selectedProjectId, issue1, testerUser))
            System.out.println("  [경고] 이슈 생성 실패");

        if (!issueService.addCommentToIssue(1, new Comment(testerUser.getId(), "이슈 재현 확인했습니다."), testerUser))
            System.out.println("  [경고] 댓글 추가 실패");
        manager.logout();

        // ⑥ pl01: 담당자 배정
        System.out.println("\n======= [4] pl01 → 담당자 배정 (dev01) =======");
        manager.login("pl01", "2222");
        if (!issueService.assignIssue(1, "dev01", manager.getCurrentUser()))
            System.out.println("  [경고] 담당자 배정 실패");
        manager.logout();

        // ⑦ dev01: FIXED
        System.out.println("\n======= [5] dev01 → FIXED =======");
        manager.login("dev01", "3333");
        if (!issueService.changeStatus(1, IssueStatus.FIXED, manager.getCurrentUser()))
            System.out.println("  [경고] FIXED 변경 실패");
        manager.logout();

        // ⑧ tester01: RESOLVED
        System.out.println("\n======= [6] tester01 → RESOLVED =======");
        manager.login("tester01", "1111");
        if (!issueService.changeStatus(1, IssueStatus.RESOLVED, manager.getCurrentUser()))
            System.out.println("  [경고] RESOLVED 변경 실패");
        manager.logout();

        // ⑨ pl01: CLOSED
        System.out.println("\n======= [7] pl01 → CLOSED =======");
        manager.login("pl01", "2222");
        if (!issueService.changeStatus(1, IssueStatus.CLOSED, manager.getCurrentUser()))
            System.out.println("  [경고] CLOSED 변경 실패");
        manager.logout();

        // ⑩ 검색 테스트용 추가 이슈 2~4
        System.out.println("\n======= [8] 검색 테스트용 이슈 생성 =======");
        manager.login("tester01", "1111");
        User searchUser = manager.getCurrentUser();

        Issue issue2 = new Issue();
        issue2.title = "UI 정렬 깨짐"; issue2.description = "메인 화면 버튼 위치 이상"; issue2.priority = Priority.MINOR;
        issueService.createIssue(selectedProjectId, issue2, searchUser);

        Issue issue3 = new Issue();
        issue3.title = "로그인 오류"; issue3.description = "로그인 버튼 간헐적 먹통"; issue3.priority = Priority.CRITICAL;
        issueService.createIssue(selectedProjectId, issue3, searchUser);

        Issue issue4 = new Issue();
        issue4.title = "UI 정렬 깨짐"; issue4.description = "사이드바 텍스트 겹침 현상"; issue4.priority = Priority.MINOR;
        issueService.createIssue(selectedProjectId, issue4, searchUser);
        manager.logout();

        // ⑪ 추천 학습용 이슈 5~7 (CLOSED까지 완주)
        System.out.println("\n======= [9] 추천 학습용 이슈 5~7 생성 및 CLOSED =======");
        String[][] recIssues = {
                {"로그인 에러 발생",       "로그인이 안 됩니다."},
                {"네이버 로그인 연동 실패", "로그인 버튼이 안 눌려요."},
                {"구글 로그인 오류",        "로그인 기능에 문제가 있습니다."}
        };

        for (int idx = 0; idx < recIssues.length; idx++) {
            int issueId = 5 + idx;

            manager.login("tester01", "1111");
            Issue rec = new Issue();
            rec.title = recIssues[idx][0]; rec.description = recIssues[idx][1]; rec.priority = Priority.CRITICAL;
            issueService.createIssue(selectedProjectId, rec, manager.getCurrentUser());
            manager.logout();

            manager.login("pl01", "2222");
            issueService.assignIssue(issueId, "dev01", manager.getCurrentUser());
            manager.logout();

            manager.login("dev01", "3333");
            issueService.changeStatus(issueId, IssueStatus.FIXED, manager.getCurrentUser());
            manager.logout();

            manager.login("tester01", "1111");
            issueService.changeStatus(issueId, IssueStatus.RESOLVED, manager.getCurrentUser());
            manager.logout();

            manager.login("pl01", "2222");
            issueService.changeStatus(issueId, IssueStatus.CLOSED, manager.getCurrentUser());
            manager.logout();
        }

        // ⑫ 검색 결과 출력
        System.out.println("\n======= [10] 검색 결과 =======");
        List<Issue> allIssues = issueService.getAllIssues();
        System.out.println("전체 이슈: " + allIssues.size() + "개");

        List<Issue> newIssues = issueService.searchIssues(selectedProjectId, "NEW", null, null, null);
        System.out.println("NEW 상태 이슈: " + newIssues.size() + "개");
        for (Issue i : newIssues)
            System.out.println("  - [" + i.id + "] " + i.title + " / 우선순위: " + i.priority);

        List<Issue> reporterIssues = issueService.searchIssues(selectedProjectId, null, null, "tester01", null);
        System.out.println("tester01이 등록한 이슈: " + reporterIssues.size() + "개");

        // ⑬ 이슈 상세 조회
        System.out.println("\n======= [11] 이슈 상세 조회 (ID:1) =======");
        manager.login("pl01", "2222");
        issueService.printIssueDetail(1, manager.getCurrentUser());
        manager.logout();

        // ⑭ 통계 (stub)
        System.out.println("\n======= [12] 통계 =======");
        StatisticsService statsService = new StatisticsService();
        manager.login("pl01", "2222");
        statsService.printAllStats(manager.getCurrentUser());
        manager.logout();

        // ⑮ 담당자 자동 추천 (대상: 이슈3 = 로그인 오류)
        System.out.println("\n======= [13] 담당자 자동 추천 =======");
        manager.login("pl01", "2222");
        int targetIssueId = 3;
        Issue target = issueService.getIssueById(targetIssueId);
        System.out.println("대상 이슈 ID: " + targetIssueId + " / 제목: " + (target != null ? target.title : "없음"));

        List<RecommendationResult> recommendations = issueService.getAssigneeRecommendations(targetIssueId);
        System.out.println("\n[추천 담당자 목록 (Top 3)]");
        if (recommendations.isEmpty()) {
            System.out.println("  추천 가능한 후보자가 없습니다.");
        } else {
            for (int i = 0; i < recommendations.size(); i++) {
                RecommendationResult r = recommendations.get(i);
                System.out.printf("  %d. 개발자: %s (유사도: %.4f, 참조 이슈: %s)%n",
                        i + 1, r.getFixerId(), r.getScore(), r.getMatchedIssueIds());
            }
        }
        manager.logout();

        System.out.println("\n====================================");
        System.out.println("  실행 완료");
        System.out.println("====================================");
    }
}