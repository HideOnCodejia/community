package com.nowcoder.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;
    /**
     * 默认登录凭证超市时间:12小时
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 12 * 30;
    /**
     * 勾选记住我的登录凭证超市时间：一个月
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
}
