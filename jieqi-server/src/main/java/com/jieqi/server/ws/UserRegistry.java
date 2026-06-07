package com.jieqi.server.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 简易内存账号：register 写入，Login 校验 userId/password。 */
public final class UserRegistry {

    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();

    public record UserAccount(String userId, String password, String nickname) {}

    public boolean register(String userId, String password, String nickname) {
        if (userId == null || userId.isBlank() || password == null) {
            return false;
        }
        String id = userId.trim();
        if (users.containsKey(id)) {
            return false;
        }
        String nick = nickname == null || nickname.isBlank() ? id : nickname.trim();
        users.put(id, new UserAccount(id, password, nick));
        return true;
    }

    public UserAccount login(String userId, String password) {
        if (userId == null || password == null) {
            return null;
        }
        UserAccount acc = users.get(userId.trim());
        if (acc == null || !acc.password().equals(password)) {
            return null;
        }
        return acc;
    }

    /** 开发便利：未注册时自动创建账号。 */
    public UserAccount loginOrCreate(String userId, String password) {
        UserAccount acc = login(userId, password);
        if (acc != null) {
            return acc;
        }
        if (userId == null || userId.isBlank()) {
            return null;
        }
        register(userId.trim(), password == null ? "" : password, userId.trim());
        return users.get(userId.trim());
    }

    public String nickname(String userId) {
        UserAccount acc = users.get(userId);
        return acc != null ? acc.nickname() : userId;
    }
}
