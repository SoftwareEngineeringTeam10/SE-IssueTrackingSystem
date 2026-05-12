package service;

import model.Role;
import model.User;

public class PermissionManager {

    // Tester만 이슈 생성 가능
    public static boolean canCreateIssue(User user) {

        return user.getRole() == Role.TESTER;
    }

    // PL만 assign 가능
    public static boolean canAssignIssue(User user) {

        return user.getRole() == Role.PL;
    }

    // Admin만 계정 관리 가능
    public static boolean canManageUsers(User user) {

        return user.getRole() == Role.ADMIN;
    }
}