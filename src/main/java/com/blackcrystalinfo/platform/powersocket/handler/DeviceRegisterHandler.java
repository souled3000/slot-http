package com.blackcrystalinfo.platform.powersocket.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSONObject;
import com.blackcrystalinfo.platform.HandlerAdapter;
import com.blackcrystalinfo.platform.RpcRequest;
import com.blackcrystalinfo.platform.dao.IDeviceDao;
import com.blackcrystalinfo.platform.exception.InternalException;
import com.blackcrystalinfo.platform.util.CookieUtil;

@Controller("/api/device/register")
public class DeviceRegisterHandler extends HandlerAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(DeviceRegisterHandler.class);

	@Autowired
	private IDeviceDao deviceDao;

	@Override
	public Object rpc(JSONObject req) throws InternalException {
		String mac = req.getString("mac");
		String sn = req.getString("sn");
		String dv = req.getString("dv");
		String pid = req.getString("pid");
		String name = req.getString("name");
		String key = req.getString("key");
		return deal(mac, sn, dv, pid, name, key);
	}

	public Object rpc(RpcRequest req) throws InternalException {
		String mac = req.getParameter("mac");
		String sn = req.getParameter("sn");
		String dv = req.getParameter("dv");
		String pid = req.getParameter("pid");
		String name = req.getParameter("name");
		String key = req.getParameter("key");
		return deal(mac, sn, dv, pid, name, key);
	}

	private Object deal(String... args) throws InternalException {
		Map<Object, Object> r = new HashMap<Object, Object>();
		r.put("status", -1);

		String mac = args[0];
		String sn = args[1];
		String dv = args[2];
		String pid = args[3];
		String name = args[4];
		String key = args[5];

		logger.info("Device regist begin mac:{}|sn:{}|bv:{}|key:{}", mac, sn, dv, key);
		if (!isValidSN(sn)) {
			r.put("status", 1);
			logger.info("Device sn", mac, sn, dv);
			return r;
		}

		try {
			// 1. 设备MAC是否已被注册
			Long existId = deviceDao.getIdByMac(mac);
			if (null == existId) {

				Long lPid = null;
				Integer iDv = null;
				if (StringUtils.isNotBlank(pid)) {
					lPid = Long.valueOf(pid);
				}
				if (StringUtils.isNotBlank(dv)) {
					iDv = Integer.valueOf(dv);
				}

				deviceDao.regist(mac, sn, name, lPid, iDv);
				existId = deviceDao.getIdByMac(mac);
			}

			String cookie = "";
			if (null != existId) {
				cookie = CookieUtil.generateDeviceKey(mac, existId.toString());
			}
			r.put("cookie", cookie);
		} catch (Exception e) {
			logger.error("Device regist error mac:{}|sn:{}|bv:{}", mac, sn, dv,
					e);
			return r;
		}

		logger.info("response:  mac:{}|sn:{}|dv:{}", mac, sn, dv);
		r.put("status", 0);
		return r;
	}

	private boolean isValidSN(String sn) {
		return true;
	}

}
