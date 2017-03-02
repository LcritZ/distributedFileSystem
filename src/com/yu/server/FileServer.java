package com.yu.server;

import java.util.HashMap;
import java.util.Timer;

import com.yu.db.model.StorageServer;
import com.yu.util.StorageServerUtil;

public class FileServer {

	public static HashMap<String, Timer> timerMap = new HashMap<String, Timer>();

	public static void main(String[] args) {
		StorageServer[] servers = StorageServerUtil
				.getAllStorageServerFromDir("storage_server_conf");
		for (int i = 0; i < servers.length; i++) {
			StorageServerUtil.addStorageServerToSystem(servers[i]);
			// 为每一个storageServer启动一个计时器
			timerMap.put(servers[i].getName(), new Timer());
			timerMap.get(servers[i].getName()).schedule(
					new FileServerTimerTask(servers[i].getName()), 1000 * 6);
		}
		new NwServer(4321, new ThreadPoolSupport(new ServerFileProtocol()));
	}
}
