package com.bilibili.constants;

public class Constants {
    //redis中key的前缀
    public static final String REDIS_KEY_PREFIX = "bilibili:";
    //redis中checkcode
    public static String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkCode";
    //redis中token的前缀
    public static final String REDIS_KEY_TOKEN_PREFIX = "token:web";
    //服务端专用
    public static final String TOKEN_WEB = "token";
    //redis中验证码的过期时间
    public static final Integer OUTTIME = 1000*100;
    //密码的正则表达式，至少要一个大写一个小写一个数字一个特殊字符
    public static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    //用户随机id的长度
    public static final Integer RANDOM_USERID_LENGTH = 10;
    //默认的主题常量
    public static final Integer THEME_NUMBER = 1;
    //毫秒1天
    public static final Long ONE_DAY = 1000*60*60*24L;
    public static final Integer TIME_SECOND_ONE_DAY = 60*60*24;
}
