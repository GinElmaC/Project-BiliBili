package com.bilibili.admin.Controller;
import com.bilibili.Component.RedisComponent;
import com.bilibili.constants.Constants;
import com.bilibili.entity.dto.TokenUserInfoDto;
import com.bilibili.entity.enums.ResponseCodeEnum;
import com.bilibili.entity.vo.ResponseVO;
import com.bilibili.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.bilibili.constants.Constants.STATUC_ERROR;
import static com.bilibili.constants.Constants.STATUC_SUCCESS;

/**
 * 设置返回的信息
 */
public class ABaseController {

    @Autowired
    private RedisComponent redisComponent;

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    /**
     * 获取ip
     * @return
     */
    protected String getIPAddr(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 向cookie中保存token
     * @param response
     * @param token
     */
    protected void saveTokenToCookie(HttpServletRequest request,HttpServletResponse response,String token){
        //将token存入cookie
        Cookie cookie = new Cookie(Constants.TOKEN_ADMIN,token);
        //设置cookie的过期时间
        /**
         * 设置-1是为了让保存的adminToken变成“会话”模式，这样关闭浏览器就会消失
         */
        cookie.setMaxAge(-1);
        //设置cookie的根域，意思是这个cookie能被哪些路径访问到，这里“/”表示所有路径都可以访问到，即所有路径都能自动登录
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 从cookie获取用户Token
     * @param request
     * @return
     */
    protected TokenUserInfoDto getTokenUserDTOFromCookie(HttpServletRequest request){
        //获取cookie
        String token = request.getHeader(Constants.TOKEN_ADMIN);
        return redisComponent.getTokenUserInfo(token);
    }

    /**
     * 退出登录用，清除cookie
     * @param request
     */
    protected void removeCookie(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(Constants.TOKEN_ADMIN)){
                redisComponent.cleanAdminToken(cookie.getValue());
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }
        }
    }
}
