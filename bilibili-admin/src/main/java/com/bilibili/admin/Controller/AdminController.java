package com.bilibili.admin.Controller;

import com.bilibili.Component.RedisComponent;
import com.bilibili.Redis.RedisUtils;
import com.bilibili.admin.Config.AppConfig;
import com.bilibili.constants.Constants;
import com.bilibili.entity.dto.TokenUserInfoDto;
import com.bilibili.entity.po.UserInfo;
import com.bilibili.entity.query.UserInfoQuery;
import com.bilibili.entity.vo.ResponseVO;
import com.bilibili.exception.BusinessException;
import com.bilibili.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端
 */
@RestController
@RequestMapping("/account")
//开启参数校验
@Validated
public class AdminController extends ABaseController{
	@Autowired
	private AppConfig appConfig;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private RedisUtils<String> redisUtils;

	@Autowired
	private RedisComponent redisComponent;

	private static final String CODEKEY = "checkCode";
	/**
	 * 用redis实现验证码
	 * @return
	 */
	@RequestMapping(value = "/checkCode")
	public ResponseVO checkCode(){
		ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(100,42);
		//图片的答案
		String code = arithmeticCaptcha.text();
		System.out.println("答案为"+code);
		//图片的64位编码
		String codeBase64 = arithmeticCaptcha.toBase64();
		//checkCode这个key如果写死了，则整个系统只能给一个人用，所以我们一个用户应该有一个key
//		redisUtils.setex("checkCode",code,1000*10);
		//获取该用户的专属验证码id，10秒后过期
		String id = redisComponent.saveCheckCode(code);

		Map<String,String> map = new HashMap<>();
		map.put("checkCode",codeBase64);
		map.put("checkCodeKey",id);
		return getSuccessResponseVO(map);
	}


	/**
	 * 使用token实现登录
	 * @return
	 */
	@RequestMapping("/login")
	public ResponseVO Login(@NotEmpty String account,
							@NotEmpty String password,
							@NotEmpty String checkCodeKey,
							@NotEmpty String checkCode,
							HttpServletRequest request,
							HttpServletResponse response){
		try {
			String res = redisComponent.getCheckCode(checkCodeKey);
			//验证验证码
			if(!checkCode.equals(res)){
				throw new BusinessException("验证码错误");
			}
			//验证账号密码（配置文件中的）
			if(!account.equals(appConfig.getAdminAccount()) && password.equals(appConfig.getAdminPassword())){
				throw  new BusinessException("账号或密码错误");
			}
			//配置token，格式为随机UUID+account，同时向redis中保存
			String token = redisComponent.saveTokenInfoForAdmin(account);

			//向cookie中保存登录记号
			saveTokenToCookie(request,response,token);

			return getSuccessResponseVO(account);
		}finally {
			//清除验证码
			redisComponent.leaveCheckCode(checkCodeKey);
			Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for(Cookie cookie:cookies){
                    if(cookie.getName().equals(Constants.TOKEN_ADMIN)){
                        redisComponent.cleanAdminToken(cookie.getValue());
//					request.removeAttribute(Constants.TOKEN_ADMIN);
                    }
                }
            }
        }
    }

	/**
	 * 退出登录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/logout")
	public ResponseVO logout(HttpServletRequest request,HttpServletResponse response){
		removeCookie(request,response);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList(UserInfoQuery query){
		return getSuccessResponseVO(userInfoService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(UserInfo bean) {
		userInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId查询对象
	 */
	@RequestMapping("/getUserInfoByUserId")
	public ResponseVO getUserInfoByUserId(String userId) {
		return getSuccessResponseVO(userInfoService.getUserInfoByUserId(userId));
	}

	/**
	 * 根据UserId修改对象
	 */
	@RequestMapping("/updateUserInfoByUserId")
	public ResponseVO updateUserInfoByUserId(UserInfo bean,String userId) {
		userInfoService.updateUserInfoByUserId(bean,userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId删除
	 */
	@RequestMapping("/deleteUserInfoByUserId")
	public ResponseVO deleteUserInfoByUserId(String userId) {
		userInfoService.deleteUserInfoByUserId(userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email查询对象
	 */
	@RequestMapping("/getUserInfoByEmail")
	public ResponseVO getUserInfoByEmail(String email) {
		return getSuccessResponseVO(userInfoService.getUserInfoByEmail(email));
	}

	/**
	 * 根据Email修改对象
	 */
	@RequestMapping("/updateUserInfoByEmail")
	public ResponseVO updateUserInfoByEmail(UserInfo bean,String email) {
		userInfoService.updateUserInfoByEmail(bean,email);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email删除
	 */
	@RequestMapping("/deleteUserInfoByEmail")
	public ResponseVO deleteUserInfoByEmail(String email) {
		userInfoService.deleteUserInfoByEmail(email);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据NickName查询对象
	 */
	@RequestMapping("/getUserInfoByNickName")
	public ResponseVO getUserInfoByNickName(String nickName) {
		return getSuccessResponseVO(userInfoService.getUserInfoByNickName(nickName));
	}

	/**
	 * 根据NickName修改对象
	 */
	@RequestMapping("/updateUserInfoByNickName")
	public ResponseVO updateUserInfoByNickName(UserInfo bean,String nickName) {
		userInfoService.updateUserInfoByNickName(bean,nickName);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据NickName删除
	 */
	@RequestMapping("/deleteUserInfoByNickName")
	public ResponseVO deleteUserInfoByNickName(String nickName) {
		userInfoService.deleteUserInfoByNickName(nickName);
		return getSuccessResponseVO(null);
	}
}