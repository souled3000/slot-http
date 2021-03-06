package com.blackcrystalinfo.platform.powersocket.api;

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
import com.blackcrystalinfo.platform.util.ErrorCode;
import com.blackcrystalinfo.platform.util.Utils;

@Path(path="/mobile/quit")
public class QuitApi extends HandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(QuitApi.class);

	public Object rpc(RpcRequest req) throws Exception {
		Map<Object, Object> r = new HashMap<Object, Object>();
		r.put(status, SYSERROR.toString());
		String uId = CookieUtil.gotUserIdFromCookie(req.getParameter( "cookie"));
		Jedis j = null;
		try {
			j = DataHelper.getJedis();
			String fId = j.hget("user:family", uId);
			
			if(StringUtils.isBlank(fId)){
				r.put(status, ErrorCode.C0029);
				logger.debug("要退出的人:{},家庭:{}",uId,fId);
				return r;
			}
			
			if(StringUtils.equals(fId, uId)){
				logger.debug("要退出的人:{},家庭:{}",uId,fId);
				r.put(status, "0020");
				return r;
			}
			
			j.hdel("user:family", uId);
			j.srem("family:"+fId, uId);
			
			Set<String>members = j.smembers("family:"+fId);
			for(String m : members){
				Set<String> devices = j.smembers("u:"+m+":devices");
				for(String o : devices){
					StringBuilder sb = new StringBuilder();
					//将uid与oper下的所有设备做关联
					sb.append(o).append("|").append(uId).append("|").append("0");
					j.publish("PubDeviceUsers", sb.toString());
				}
			}
			j.publish("PubCommonMsg:0x36".getBytes(), Utils.genMsg(uId+","+fId+"|",3, Integer.parseInt(uId), ""));
		} catch (Exception e) {
			DataHelper.returnBrokenJedis(j);
			return r;
		} finally {
			DataHelper.returnJedis(j);
		}
		r.put(status, ErrorCode.SUCCESS.toString());
		return r;
	}
}
