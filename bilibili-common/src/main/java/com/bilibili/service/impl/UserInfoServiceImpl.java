package com.bilibili.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.bilibili.Component.RedisComponent;
import com.bilibili.constants.Constants;
import com.bilibili.entity.dto.TokenUserInfoDto;
import com.bilibili.entity.enums.UserSexEnum;
import com.bilibili.entity.enums.UserStatusEnum;
import com.bilibili.exception.BusinessException;
import com.bilibili.utils.CopyTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bilibili.entity.enums.PageSize;
import com.bilibili.entity.query.UserInfoQuery;
import com.bilibili.entity.po.UserInfo;
import com.bilibili.entity.vo.PaginationResultVO;
import com.bilibili.entity.query.SimplePage;
import com.bilibili.mappers.UserInfoMapper;
import com.bilibili.service.UserInfoService;
import com.bilibili.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Autowired
	private RedisComponent redisComponent;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}

	/**
	 * 根据NickName获取对象
	 */
	@Override
	public UserInfo getUserInfoByNickName(String nickName) {
		return this.userInfoMapper.selectByNickName(nickName);
	}

	/**
	 * 根据NickName修改
	 */
	@Override
	public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
		return this.userInfoMapper.updateByNickName(bean, nickName);
	}

	/**
	 * 根据NickName删除
	 */
	@Override
	public Integer deleteUserInfoByNickName(String nickName) {
		return this.userInfoMapper.deleteByNickName(nickName);
	}

	@Override
	public void register(String email, String nickName, String password) {
		UserInfo userInfo = null;
		userInfo = userInfoMapper.selectByEmail(email);
		if(userInfo != null){
			throw new BusinessException("邮箱重复");
		}
		userInfo = userInfoMapper.selectByNickName(nickName);
		if(userInfo != null){
			throw new BusinessException("用户名重复");
		}
		userInfo = new UserInfo();
		userInfo.setUserId(StringTools.getRandomNumber(Constants.RANDOM_USERID_LENGTH));
		userInfo.setEmail(email);
		userInfo.setNickName(nickName);
		userInfo.setPassword(StringTools.TurnMD5String(password));
		userInfo.setJoinTime(new Date());
		userInfo.setStatus(UserStatusEnum.ENABLE.getNumber());
		userInfo.setSex(UserSexEnum.SECRECY.getSexNumber());
		userInfo.setTheme(Constants.THEME_NUMBER);
		//TODO 初始化硬币
		userInfo.setCurrentCoinCount(10);
		userInfo.setTotalCoinCount(10);
		userInfoMapper.insert(userInfo);
		//这样写返回的值不好确定
//		UserInfo userInfo = null;
//		userInfo = userInfoMapper.selectByEmail(email);
//		if(userInfo == null){
//			userInfo = userInfoMapper.selectByNickName(nickName);
//			if(userInfo == null){
//				userInfo = new UserInfo();
//				userInfo.setUserId(StringTools.getRandomNumber(Constants.RANDOM_USERID_LENGTH));
//				userInfo.setEmail(email);
//				userInfo.setNickName(nickName);
//				userInfo.setPassword(StringTools.TurnMD5String(password));
//				userInfo.setJoinTime(new Date());
//				userInfo.setStatus(UserStatusEnum.ENABLE.getNumber());
//				userInfo.setSex(UserSexEnum.SECRECY.getSexNumber());
//				userInfo.setTheme(Constants.THEME_NUMBER);
//				//TODO 初始化硬币
//				userInfoMapper.insert(userInfo);
//				return true;
//			}else{
//				return false;
//			}
//		}else{
//			return false;
//		}
	}

	@Override
	public TokenUserInfoDto login(String email,String password , String ip) {
		//先从数据库进行查询
		UserInfo userInfo = userInfoMapper.selectByEmail(email);
		//检查账号密码
		if(null == userInfo || !password.equals(userInfo.getPassword())){
			throw new BusinessException("邮箱或密码错误");
		}
		//检查账号状态
		if(userInfo.getStatus() == UserStatusEnum.DISABLE.getNumber()){
			throw new BusinessException("账号被封禁");
		}
		//要更新的数据
		UserInfo updateInfo = new UserInfo();
		updateInfo.setLastLoginTime(new Date());
		updateInfo.setLastLoginIp(ip);
		//将数据更新进数据库
		userInfoMapper.updateByUserId(updateInfo,userInfo.getUserId());
		//给前端返回的数据
		TokenUserInfoDto tokenUserInfoDto = CopyTools.copy(userInfo,TokenUserInfoDto.class);
		//保存token
		redisComponent.saveToken(tokenUserInfoDto);
		return tokenUserInfoDto;
	}
}