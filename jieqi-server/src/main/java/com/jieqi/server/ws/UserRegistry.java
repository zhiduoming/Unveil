package com.jieqi.server.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 简易内存账号：register 写入，Login 校验 userId/password。 */
public final class UserRegistry {

    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();

    public record UserAccount(String userId, String password, String nickname, String avatar) {}

    public boolean register(String userId, String password, String nickname) {
        return register(userId, password, nickname, "");
    }

    public boolean register(String userId, String password, String nickname, String avatar) {
        if (userId == null || userId.isBlank() || password == null) {
            return false;
        }
        String id = userId.trim();
        if (users.containsKey(id)) {
            return false;
        }
        String nick = nickname == null || nickname.isBlank() ? id : nickname.trim();
        String av = avatar == null ? "" : avatar.trim();
        users.put(id, new UserAccount(id, password, nick, av));
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
        return loginOrCreate(userId, password, null, null);
    }

    /**
     * 开发便利：未注册时自动创建账号；已存在时用本次传入的非空 nickname/avatar 更新
     * （支持玩家每次登录随机换昵称/头像）。
     */
    public UserAccount loginOrCreate(String userId, String password, String nickname, String avatar) {
        UserAccount acc = login(userId, password);
        if (acc != null) {
            String nick = nickname == null || nickname.isBlank() ? acc.nickname() : nickname.trim();
            String av = avatar == null || avatar.isBlank() ? acc.avatar() : avatar.trim();
            if (!nick.equals(acc.nickname()) || !av.equals(acc.avatar())) {
                acc = new UserAccount(acc.userId(), acc.password(), nick, av);
                users.put(acc.userId(), acc);
            }
            return acc;
        }
        if (userId == null || userId.isBlank()) {
            return null;
        }
        register(userId.trim(), password == null ? "" : password, nickname, avatar);
        return users.get(userId.trim());
    }

    public String nickname(String userId) {
        UserAccount acc = users.get(userId);
        return acc != null ? acc.nickname() : userId;
    }

    public String avatar(String userId) {
        UserAccount acc = users.get(userId);
        return acc != null ? acc.avatar() : "";
    }
}
