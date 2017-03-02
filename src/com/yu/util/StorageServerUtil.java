package com.yu.util;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.yu.db.model.StorageServer;
import com.yu.db.service.StorageServerService;

public class StorageServerUtil {

	private static Logger logger = Logger.getLogger(StorageServerUtil.class);

	/**
	 * 获取目录下所有文件和目录的名字
	 * 
	 * @param dirPath
	 * @return
	 */
	public static String[] getAllFileNamesOfDir(String dirPath) {
		dirPath = System.getProperty("user.dir") + "\\" + dirPath;
		File file = new File(dirPath);
		File[] fileList = file.listFiles();
		String[] nameList = new String[fileList.length];
		for (int i = 0; i < fileList.length; i++) {
			nameList[i] = fileList[i].getName();
		}
		return nameList;
	}

	/**
	 * 读取一个存储服务器的配置文件组装成一个实例
	 * 
	 * @param fileName
	 * @return
	 */
	public static StorageServer getStorageServerFromFile(String fileName) {
		Properties properties = Tool.loadProperties(fileName);
		StorageServer ss = new StorageServer();
		ss.setName(properties.getProperty("name"));
		String maxVolume = properties.getProperty("maxVolume");
		String num = "";
		int volume = 0;
		for (int i = 0; i < maxVolume.length(); i++) {
			if (Character.isDigit(maxVolume.charAt(i))) {
				num += maxVolume.charAt(i);
				continue;
			}
			volume = Integer.parseInt(num);
			switch (maxVolume.charAt(i)) {
			case 'B':
				break;
			case 'K':
				volume = volume * 1024;
				break;
			case 'M':
				volume = volume * 1024 * 1024;
				break;
			case 'G':
				volume = volume * 1024 * 1024 * 1024;
				break;
			default:
				break;
			}
			break;
		}
		ss.setMaxVolume(volume);
		ss.setPort(Integer.valueOf(properties.getProperty("port")));
		ss.setIpAddres(properties.getProperty("ip"));
		ss.setStorageDir(properties.getProperty("storageDir"));
		return ss;
	}

	/**
	 * 读取一个目录下的所有存储服务器的配置文件组装成实例返回
	 * 
	 * @param dirPath
	 * @return
	 */
	public static StorageServer[] getAllStorageServerFromDir(String dirPath) {
		String[] fileName = getAllFileNamesOfDir(dirPath);
		StorageServer[] servers = new StorageServer[fileName.length];
		for (int i = 0; i < fileName.length; i++) {
			servers[i] = getStorageServerFromFile(fileName[i]);
		}
		return servers;
	}

	/**
	 * 重启时添加存储服务器到系统
	 * 
	 * @param ss
	 * @return
	 */
	public static boolean addStorageServerToSystem(StorageServer ss) {
		StorageServerService ssService = new StorageServerService();
		if (ssService.selectStorageServerByName(ss.getName()) != null) {
			// 添加失败，默认为所属存储器已经在系统中，更新剩余容量字段
			StorageServer ssTemp = ssService.selectStorageServerByName(ss
					.getName());
			if (ss.getMaxVolume().equals(ssTemp.getMaxVolume()) && ss.isAlive()) {
				return true;// 该存储服务器的最大容量没有更改
			} else if (ss.getMaxVolume().equals(ssTemp.getMaxVolume())
					&& !ss.isAlive()) {
				ssTemp.setAlive(true);
				ssService.updateByName(ssTemp);
				return true;
			}
			ss.setLeftVolume(ss.getMaxVolume() - ssTemp.getMaxVolume()
					+ ssTemp.getLeftVolume());
			logger.info("更新存储服务器" + ss.getName() + "的最大容量为" + ss.getMaxVolume());
			ss.setAlive(true);
			if (ssService.updateByName(ss) == 0) {
				logger.error("更存储服务器" + ss.getName() + "失败");
				return false;
			}
			logger.info("更存储服务器" + ss.getName() + "成功");
			return true;
		}
		// 新的存储服务器
		ss.setAlive(true);
		ss.setLeftVolume(ss.getMaxVolume());
		if (ssService.insert(ss) == 0) {
			return false;
		}
		logger.info("添加新的存储服务器" + ss.getName() + "成功");
		return true;
	}

	public static void main(String[] args) {
		// StorageServer[] servers =
		// getAllStorageServerFromDir("storage_server_conf");
		// for (int i = 0; i < servers.length; i++) {
		// addStorageServerToSystem(servers[i]);
		// }

	}

}
