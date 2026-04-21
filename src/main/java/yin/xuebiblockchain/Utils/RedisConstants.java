package yin.xuebiblockchain.Utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token";
    public static final Long LOGIN_USER_TTL = 7 * 24 * 60L;
    public static final Long SEND_CODE_TTL = 30L;


    public static final String Post_User_Like = "blog:liked:";
    public static final String Post_User_Share = "blog:share:";

}
