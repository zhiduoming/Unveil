package com.jieqi.protocol.json;

/** 老师公共接口 error.code 枚举。 */
public final class JsonErrorCodes {

    private JsonErrorCodes() {}

    public static final int LOGIN_FAILED = 1001;
    public static final int DUPLICATE_LOGIN = 1002;
    public static final int ILLEGAL_MOVE = 2001;
    public static final int NOT_YOUR_TURN = 2002;
    public static final int TIMEOUT_MOVE = 2003;
    public static final int ROOM_NOT_FOUND = 3001;
    public static final int MATCH_FAILED = 3002;
    public static final int JSON_FORMAT = 4001;
}
