package com.yu.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.yu.server.IOStrategy;

public class StorageFileProtocol implements IOStrategy {

	private Logger logger = Logger.getLogger(StorageFileProtocol.class);

	DataInputStream dis = null;
	DataOutputStream dos = null;

	public boolean recieveFile(Socket socket) throws IOException {
		logger.info("存储服务器接收到上传指令");
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		long size = dis.readLong();
		logger.info("存储服务器接收到文件大小:" + size);
		String fileName = dis.readUTF();
		logger.info("存储服务器接收到文件名称:" + fileName);
		String uuid = dis.readUTF();
		logger.info("存储服务器接收到文件uuid:" + uuid);
		String path = com.yu.storage.FileStorage.storageServer.getStorageDir()
				+ "//" + uuid + "+" + fileName;
		File file = new File(path);
		logger.info("存储服务器存储文件" + fileName + "于" + path);
		FileOutputStream fos = new FileOutputStream(file);
		long passedLen = 0;// 当前一共传输大小
		int bufferSize = 8192;// 缓冲区大小
		byte[] buf = new byte[bufferSize];// 缓冲区
		while (true) {
			int read = 0;
			if (dis != null) {
				read = dis.read(buf);
			}
			if (read == -1) {
				break;
			}
			passedLen += read;
			fos.write(buf, 0, read);
		}
		fos.close();
		if (passedLen == size) {
			logger.info("存储服务器接收文件成功");
			return true;
		} else {
			logger.info("存储服务器接收文件失败");
			return false;
		}
	}

	/**
	 * 发送文件
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	private boolean sendFile(Socket socket) throws IOException {
		logger.info("存储服务器收到下载指令");
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		String fullName = dis.readUTF();
		logger.info("存储服务器接收到文件全名：" + fullName);
		File file = new File(fullName);
		FileInputStream fis = new FileInputStream(file);
		int bufferSize = 8192;// 缓冲区大小
		byte[] buf = new byte[bufferSize];// 缓冲区
		long passedlen = 0; // 已传输大小
		long len = 0; // 每次用read读取返回的值，代表本次上传的字节数
		long size = file.length();// 文件大小
		logger.info("存储服务器开始发送文件给文件服务器");
		while ((len = fis.read(buf)) > 0) {
			passedlen += len;
			dos.write(buf, 0, (int) len);
		}
		dos.flush();
		// 关闭资源
		fis.close();
		dis.close();
		dos.close();
		socket.close();
		if (passedlen == size) {
			logger.info("存储服务器发送文件成功");
			return true;
		} else {
			logger.error("存储服务器发送文件失败");
			return false;
		}

	}

	/**
	 * 删除文件
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	private boolean deletFile(Socket socket) throws IOException {
		logger.info("存储服务器收到删除指令");
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		String fullName = dis.readUTF();
		logger.info("存储服务器接收到文件全名:" + fullName);
		File file = new File(fullName);
		if (file.delete()) {
			dos.writeBoolean(true);
			logger.info("存储服务器删除文件成功");
			dos.flush();
		}
		dis.close();
		dos.close();
		return true;
	}

	/**
	 * 重命名文件
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	private boolean rename(Socket socket) throws IOException {
		logger.info("存储服务器收到重命名指令");
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		String fullName = dis.readUTF();
		logger.info("存储服务器接收到文件全名:" + fullName);
		File file = new File(fullName);
		String newName = dis.readUTF();
		logger.info("存储服务器接收到新的文件名:" + newName);
		String uuid = dis.readUTF();
		logger.info("存储服务器接收到uuid:" + uuid);
		String newFullName = fullName.substring(0, fullName.indexOf('+'))
				+ newName;
		File newFile = new File(newFullName);
		logger.info("存储器更改文件名称为：" + newFullName);
		file.renameTo(newFile);
		dos.writeBoolean(true);
		dis.close();
		dos.close();
		return true;
	}

	@Override
	public void service(Socket socket) throws IOException {
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		String command = dis.readUTF();
		switch (command) {
		case "uplode":// 上传指令
			recieveFile(socket);
			break;
		case "downlode":// 下载指令
			sendFile(socket);
			break;
		case "delete":// 删除指令
			deletFile(socket);
			break;
		case "rename":// 重命名指令
			rename(socket);
			break;

		default:
			break;
		}
	}

}
