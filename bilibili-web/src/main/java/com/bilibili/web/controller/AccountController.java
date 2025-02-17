package com.bilibili.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bilibili.Component.RedisComponent;
import com.bilibili.Redis.RedisUtils;
import com.bilibili.constants.Constants;
import com.bilibili.entity.dto.TokenUserInfoDto;
import com.bilibili.entity.query.UserInfoQuery;
import com.bilibili.entity.po.UserInfo;
import com.bilibili.entity.vo.ResponseVO;
import com.bilibili.exception.BusinessException;
import com.bilibili.service.UserInfoService;
import com.bilibili.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sound.midi.Soundbank;
import javax.validation.constraints.*;

/**
 *  Controller
 */
@RestController
@RequestMapping("/account")
//开启参数校验
@Validated
public class AccountController extends ABaseController{

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
	@RequestMapping("/checkCode")
	public ResponseVO checkCode(){
		ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(100,42);
		//图片的答案
		String code = arithmeticCaptcha.text();
		//图片的64位编码
		String codeBase64 = arithmeticCaptcha.toBase64();
		//checkCode这个key如果写死了，则整个系统只能给一个人用，所以我们一个用户应该有一个key
//		redisUtils.setex("checkCode",code,1000*10);
		//获取该用户的专属验证码id，10秒后过期
		String id = redisComponent.saveCheckCode(code);

		Map<String,String> map = new HashMap<>();
		map.put("checkCode",codeBase64);
		map.put("id",id);
		return getSuccessResponseVO(map);
	}
	@RequestMapping("/register")
	public ResponseVO register(@NotEmpty @Email @Size(max = 150) String email, // 必须为邮箱格式同时不能为空，大小最大150
							   @NotEmpty @Size(max = 20) String nick_name,
							   @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password, //密码，应符合正则表达式
							   @NotEmpty String checkCode, //传回来的该用户的随机id
							   @NotEmpty String code){
        try {
            String id = redisComponent.getCheckCode(checkCode);
            if(id != null && id.equals(code)){
                //插入数据
				userInfoService.register(email, nick_name, password);
				return getSuccessResponseVO(null);
            }else{
                throw new BusinessException("验证码错误");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
			//清除验证码
			redisComponent.leaveCheckCode(checkCode);
		}
    }

	/**
	 * 使用token实现登录
	 * @return
	 */
	@RequestMapping("/login")
	public ResponseVO register(HttpServletResponse response,
							   @NotEmpty @Email String email,
							   @NotEmpty String password,
							   @NotEmpty String id,
							   @NotEmpty String code){
        try {
            String res = redisComponent.getCheckCode(id);
            if(!code.equals(res)){
                throw new BusinessException("验证码错误");
            }
			//获取ip
			String ip = getIPAddr();
			//获得返回给前端的DTO，redis中已经存有Token
			password = StringTools.TurnMD5String(password);
			TokenUserInfoDto userInfoDto = userInfoService.login(email,password,ip);
			//第一种方式，返回整个对象，让前端将token存在cookie中
			//但是前端可能写错或者时间不对，自己的事还是自己做吧
//			return getServerErrorResponseVO(userInfoDto);
			saveTokenToCookie(response,userInfoDto.getToken());
			//TODO 设置粉丝数，关注数，硬币数
			return getSuccessResponseVO(userInfoDto);
        }finally {
			//清除验证码
			redisComponent.leaveCheckCode(id);
			//TODO 清除旧的cookie
        }
    }

	/**
	 * 利用token实现自动登录
	 * @param
	 * @return
	 */
	@RequestMapping("/autologin")
	public ResponseVO register(HttpServletResponse response,HttpServletRequest request){
		TokenUserInfoDto userInfoDto = getTokenUserDTOFromCookie(request);
		//没有，则返回null，前端进行从新登录
		if(userInfoDto == null){
			return getSuccessResponseVO(null);
		}
		//如果剩余时间小于5小时，续期
		if((userInfoDto.getExpireAt()-System.currentTimeMillis())/(60*60)<5){
			//续期
			redisComponent.saveToken(userInfoDto);
		}
		//重新保存token
		saveTokenToCookie(response, userInfoDto.getToken());
		return getSuccessResponseVO(userInfoDto);
	}


	/**
	 * 利用session实现的验证码
	 * @param httpSession
	 * @return
	 */
//	@RequestMapping("/checkCode")
//	public ResponseVO checkCode(HttpSession httpSession){
//		//获取图片验证码,并设置图片验证码的高和宽
//		ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(100,42);
//		//拿到结果
//		String code = arithmeticCaptcha.text();
//		//获取图片的64位编码格式
//		String codeBase64 = arithmeticCaptcha.toBase64();
//		//将正确结果储存在sesison中，key位checkCode
//		//在发送请求的时候会返回一个带有sessionID的Cookie，session中有正确的结果，第二次请求的时候会带上这个Cookie，然后我们可以在
//		//register方法中请求这个cookie中的session并获取正确结果，然后与用户输入的结果进行比对
//		httpSession.setAttribute("checkCode",code);
//		return getSuccessResponseVO(codeBase64);
//	}
//	@RequestMapping("/register")
//	public ResponseVO register(HttpSession session,String checkCode){
//		String mycheckcode = (String)session.getAttribute("checkCode");
//		return getSuccessResponseVO(mycheckcode.equalsIgnoreCase(checkCode));
//	}


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