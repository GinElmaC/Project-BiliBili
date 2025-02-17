package com.bilibili.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

//当java对象反序列化时遇到未知字段，不进行报错，例如删除了某些字段后在进行反序列化就不会报错
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserInfoDto implements Serializable {
    private String userId;
    private String email;
    private String avatar; // 头像
    private Long expireAt; //token失效时间，一旦失效就无法自动登录
    private String token;
    private Integer fansCount; //粉丝数
    private Integer currentCoinCount; //当前硬币数
    private Integer focusCount;

    private static final long serialVersionUID = 1L;


    public TokenUserInfoDto() {
    }

    public TokenUserInfoDto(String userId, String email, String avatar, Long expireAt, Integer fansCount, Integer currentCoinCount, Integer focusCount) {
        this.userId = userId;
        this.email = email;
        this.avatar = avatar;
        this.expireAt = expireAt;
        this.fansCount = fansCount;
        this.currentCoinCount = currentCoinCount;
        this.focusCount = focusCount;
    }

    public TokenUserInfoDto(String userId, String email, String avatar, Long expireAt, String token, Integer fansCount, Integer currentCoinCount, Integer focusCount) {
        this.userId = userId;
        this.email = email;
        this.avatar = avatar;
        this.expireAt = expireAt;
        this.token = token;
        this.fansCount = fansCount;
        this.currentCoinCount = currentCoinCount;
        this.focusCount = focusCount;
    }

    /**
     * 获取
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取
     * @return avatar
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * 设置
     * @param avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * 获取
     * @return expireAt
     */
    public Long getExpireAt() {
        return expireAt;
    }

    /**
     * 设置
     * @param expireAt
     */
    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }

    /**
     * 获取
     * @return fansCount
     */
    public Integer getFansCount() {
        return fansCount;
    }

    /**
     * 设置
     * @param fansCount
     */
    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }

    /**
     * 获取
     * @return currentCoinCount
     */
    public Integer getCurrentCoinCount() {
        return currentCoinCount;
    }

    /**
     * 设置
     * @param currentCoinCount
     */
    public void setCurrentCoinCount(Integer currentCoinCount) {
        this.currentCoinCount = currentCoinCount;
    }

    /**
     * 获取
     * @return focusCount
     */
    public Integer getFocusCount() {
        return focusCount;
    }

    /**
     * 设置
     * @param focusCount
     */
    public void setFocusCount(Integer focusCount) {
        this.focusCount = focusCount;
    }

    public String toString() {
        return "TokenUserInfoDto{userId = " + userId + ", email = " + email + ", avatar = " + avatar + ", expireAt = " + expireAt + ", fansCount = " + fansCount + ", currentCoinCount = " + currentCoinCount + ", focusCount = " + focusCount + "}";
    }

    /**
     * 获取
     * @return token
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置
     * @param token
     */
    public void setToken(String token) {
        this.token = token;
    }
}
