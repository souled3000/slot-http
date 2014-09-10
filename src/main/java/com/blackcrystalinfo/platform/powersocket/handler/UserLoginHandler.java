package com.blackcrystalinfo.platform.powersocket.handler;

import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.blackcrystalinfo.platform.IHandler;
import com.blackcrystalinfo.platform.RpcRequest;
import com.blackcrystalinfo.platform.exception.InternalException;
import com.blackcrystalinfo.platform.powersocket.dao.DataHelper;
import com.blackcrystalinfo.platform.powersocket.dao.pojo.user.UserLoginResponse;
import com.blackcrystalinfo.platform.util.CometScanner;
import com.blackcrystalinfo.platform.util.CookieUtil;
import com.blackcrystalinfo.platform.util.HttpUtil;
import com.blackcrystalinfo.platform.util.PBKDF2;

public class UserLoginHandler implements IHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(UserLoginHandler.class);

	@Override
	public Object rpc(RpcRequest req) throws InternalException {

		UserLoginResponse result = new UserLoginResponse();
		result.setStatus(-1);
		result.setStatusMsg("");
		result.setUrlOrigin(req.getUrlOrigin());

		String email = HttpUtil.getPostValue(req.getParams(), "email");
		String pwd = HttpUtil.getPostValue(req.getParams(), "passwd");
		
		logger.info("UserLoginHandler begin email:{}|pwd:{}",email,pwd);
		
		if(StringUtils.isBlank(pwd)){
			result.setStatus(3);
			return result;
		}
		
		Jedis jedis = null;
		
		try {
			jedis = DataHelper.getJedis();
			email=email.toLowerCase();
			// 1. 根据Email获取userId
			String userId = jedis.hget("user:mailtoid", email);
			if (null == userId) {
				result.setStatusMsg("User not exist.");
				result.setStatus(1);
				return result;
			}

			// 2. encodePwd与passwd加密后的串做比较
			String encodePwd = jedis.hget("user:shadow", userId);
			if (!PBKDF2.validate(pwd, encodePwd)) {
				result.setStatusMsg("Password error.");
				result.setStatus(2);
				return result;
			}
			
			
			String cookie = CookieUtil.encode(userId, CookieUtil.EXPIRE_SEC);
//			String proxyKey = CookieUtil.generateKey(userId, String.valueOf(System.currentTimeMillis()/1000), CookieUtil.EXPIRE_SEC);
//			String proxyAddr = CometScanner.take();
//			logger.info("proxykey:{} | size:{} | proxyAddr:{} ", proxyKey, proxyKey.getBytes().length, proxyAddr);
			result.setStatus(0);
			result.setUserId(userId);
//			result.setHeartBeat(CookieUtil.EXPIRE_SEC);
			result.setCookie(cookie);
//			result.setProxyKey(proxyKey);
//			result.setProxyAddr(proxyAddr);

		} catch (Exception e) {
			DataHelper.returnBrokenJedis(jedis);
			logger.error("User login error.");
			throw new InternalException(e.getMessage());
		} finally {
			DataHelper.returnJedis(jedis);
		}

		logger.info("response: {}", result.getStatus());
		return result;
	}

}