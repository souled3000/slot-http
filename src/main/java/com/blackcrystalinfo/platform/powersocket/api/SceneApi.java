package com.blackcrystalinfo.platform.powersocket.api;

import static com.blackcrystalinfo.platform.util.ErrorCode.SUCCESS;
import static com.blackcrystalinfo.platform.util.ErrorCode.SYSERROR;
import static com.blackcrystalinfo.platform.util.RespField.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.blackcrystalinfo.platform.HandlerAdapter;
import com.blackcrystalinfo.platform.RpcRequest;
import com.blackcrystalinfo.platform.annotation.Path;
import com.blackcrystalinfo.platform.util.CookieUtil;
import com.blackcrystalinfo.platform.util.DataHelper;

@Path(path = "/mobile/scene")
public class SceneApi extends HandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SceneApi.class);

	@Override
	public Object rpc(RpcRequest req) throws Exception {
		Map<Object, Object> r = new HashMap<Object, Object>();
		r.put(status, SYSERROR.toString());

		String userId = CookieUtil.gotUserIdFromCookie(req.getParameter("cookie"));
		String scenename = req.getParameter("sceneName");
		String scenecode = req.getParameter("sceneCode");
		String mac = req.getParameter("mac");

		Jedis j = null;
		try {
			j = DataHelper.getJedis();

			// 添加 or 修改
			if (StringUtils.isNotBlank(scenename) && StringUtils.isNotBlank(scenecode) && StringUtils.isNotBlank(mac))
				j.hset("scene:" + userId, scenecode, scenename + ":" + mac);

			// 删除
			if (StringUtils.isBlank(scenename) && StringUtils.isNotBlank(scenecode) && StringUtils.isBlank(mac))
				j.hdel("scene:" + userId, scenecode);

			if (StringUtils.isBlank(scenename) && StringUtils.isBlank(scenecode) && StringUtils.isBlank(mac)) {
				String f = j.hget("user:family", userId);
				Map<String,String> sm = new HashMap<String,String>();
				if (StringUtils.isBlank(f)) {
					sm = j.hgetAll("scene:" + userId);
				} else {
					Set<String> mems = j.smembers("family:" + f);
					for (String m : mems) {
						Map<String,String> t = j.hgetAll("scene:" + m);
						sm.putAll(t);
					}
				}
				r.put("scene", sm);
			}

		} catch (Exception e) {
			logger.info("",e);
			DataHelper.returnBrokenJedis(j);
		} finally {
			DataHelper.returnJedis(j);
		}

		r.put(status, SUCCESS.toString());
		return r;
	}
}
