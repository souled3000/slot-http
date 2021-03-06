package com.blackcrystalinfo.platform.powersocket;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackcrystalinfo.platform.App;
import com.blackcrystalinfo.platform.HandlerManager;
import com.blackcrystalinfo.platform.exception.HandlerExistedException;
import com.blackcrystalinfo.platform.powersocket.handler.GeLiWebsocketHandler;
import com.blackcrystalinfo.platform.util.CometScanner;
import com.blackcrystalinfo.platform.util.Constants;

public class GeLiSrv {

	private static final Logger logger = LoggerFactory.getLogger(SlotServer.class);

	public static void bindHandler() throws HandlerExistedException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		HandlerManager.regHandler("/api/geli/getUrl", new GeLiWebsocketHandler());
	}

	public static void main(String[] args) throws Exception {
		logger.info("Start Port {}", Constants.SERVER_PORT);
		CometScanner.tiktok();
		Thread.sleep(3000);
		bindHandler();
		new App(Constants.SERVER_PORT).run();
	}

}
