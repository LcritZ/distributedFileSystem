package com.yu.storage;

import java.util.Timer;

import com.yu.db.model.StorageServer;
import com.yu.server.NwServer;
import com.yu.server.ThreadPoolSupport;
import com.yu.util.StorageServerUtil;

public class FileStorage {

	public static StorageServer storageServer;

	static {
		FileStorage.storageServer = StorageServerUtil
				.getStorageServerFromFile("storage_server_conf/ss1.properties");
	}

	public FileStorage() {
	}

	public static void main(String[] args) {

		new Timer().schedule(new StorageServerTimerTask(), 1*1000, 5 * 1000);

		new NwServer(storageServer.getPort(), new ThreadPoolSupport(
				new StorageFileProtocol()));
	}

}
