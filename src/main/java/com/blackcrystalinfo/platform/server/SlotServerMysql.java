package com.blackcrystalinfo.platform.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.blackcrystalinfo.platform.common.Constants;

@Configuration
public class SlotServerMysql {
	private static final Logger logger = LoggerFactory.getLogger(SlotServerMysql.class);

	@Bean(initMethod = "run")
	public App app() throws InterruptedException {
		CometScanner.tiktok();
		Thread.sleep(3000);
		logger.info("SlotServer app()");
		return new App(Constants.SERVER_PORT);
	}

	public static void main(String[] args) throws Exception {
		String prefix = "";
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { prefix + "server.xml", prefix + "beans.xml" });
		ctx.refresh();
		ctx.close();
	}

}
