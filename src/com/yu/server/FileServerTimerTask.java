package com.yu.server;

import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.yu.db.model.StorageServer;
import com.yu.db.service.StorageServerService;


class FileServerTimerTask extends TimerTask {
	
	private Logger logger = Logger.getLogger(getClass());

	private String storageServerName;

	public FileServerTimerTask(String storageServerName) {
		super();
		this.storageServerName = storageServerName;
	}

	@Override
	public void run() {
		StorageServerService ssService = new StorageServerService();
		StorageServer ss = ssService
				.selectStorageServerByName(storageServerName);
		ss.setAlive(true);
		ssService.updateByName(ss);
		FileServer.timerMap.get(storageServerName).cancel();
		logger.info("文件服务器超过10秒没有收到来自存储服务器 "+ss.getName()+" 的心跳包");
		logger.info("文件服务器将存储服务器 "+ss.getName()+" 设置为dead");
	}
}