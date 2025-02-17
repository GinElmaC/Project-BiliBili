package com.bilibili.Component;

import com.bilibili.Redis.RedisUtils;
import com.bilibili.constants.Constants;
import com.bilibili.entity.dto.TokenUserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 专门操作redis的类
 */
@Component
public class RedisComponent {
    @Autowired
    private RedisUtils redisUtils;
    //向redis中保存验证码,并返回一个随机的用户专属的key
    public String saveCheckCode(String code){
        String randomID = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+randomID,code,Constants.OUTTIME);
        return randomID;
    }
    //获取该用户的正确验证码
    public String getCheckCode(String checkCode){
        return String.valueOf(redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCode));
    }
    //清除用户验证码
    public void leaveCheckCode(String checkCode){
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCode);
    }

    //向redis中添加token来实现短时间内自动登录
    public void saveToken(TokenUserInfoDto tokenUserInfoDto){
        //获取随机id
        String token = UUID.randomUUID().toString();
        //设置token的过期时间为1天
        tokenUserInfoDto.setExpireAt(System.currentTimeMillis()+Constants.ONE_DAY);
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_PREFIX+token,tokenUserInfoDto,tokenUserInfoDto.getExpireAt());
    }
    //清除cookie
    public void cleanToken(String token){
        redisUtils.delete(token);
    }
    //通过token获取对象
    public TokenUserInfoDto getTokenUserInfo(String token){
        return (TokenUserInfoDto) redisUtils.get(token);
    }
}
