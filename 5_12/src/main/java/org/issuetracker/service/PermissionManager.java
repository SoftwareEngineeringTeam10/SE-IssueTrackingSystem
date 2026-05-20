package org.issuetracker.service;

import org.issuetracker.model.Role;
import org.issuetracker.model.User;

public class PermissionManager {

    // 이슈 생성 권한. ADMIN 제외 전체 허용 (PL, DEV, TESTER)
    public static boolean canCreateIssue(User user) {
        Role r = user.getRole();
        return r == Role.PL || r == Role.DEV || r == Role.TESTER;
    }

    // 담당자 배정 권한. PL만 허용
    public static boolean canAssignIssue(User user) {
        return user.getRole() == Role.PL;
    }

    // 계정 관리 권한. ADMIN만 허용
    public static boolean canManageUsers(User user) {
        return user.getRole() == Role.ADMIN;
    }

    // 이슈 해결(FIXED) 권한. DEV만 허용
    public static boolean canFixIssue(User user) {
        return user.getRole() == Role.DEV;
    }

    // 이슈 검증(RESOLVED) 권한. TESTER만 허용
    public static boolean canResolveIssue(User user) {
        return user.getRole() == Role.TESTER;
    }

    // 이슈 종료(CLOSED) 권한. PL만 허용
    public static boolean canCloseIssue(User user) {
        return user.getRole() == Role.PL;
    }

    // 이슈 재오픈(REOPEN) 권한. TESTER만 허용
    public static boolean canReopenIssue(User user) {
        return user.getRole() == Role.TESTER;
    }

    // 조회 및 코멘트 권한. 로그인한 사용자 전체 허용
    public static boolean canViewIssue(User user) {
        return user != null;
    }
}