package com.yu.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;

import org.apache.log4j.Logger;

import com.yu.db.model.SFile;
import com.yu.db.model.StorageServer;
import com.yu.db.service.FileService;
import com.yu.db.service.StorageServerService;
import com.yu.util.Tool;

public class ServerFileProtocol implements IOStrategy {

	private Logger logger = Logger.getLogger(ServerFileProtocol.class);

	/**
	 * 接收来自客户端的文件并上传给存储服务器
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	public boolean recieveFile(Socket socket, DataInputStream dis,
			DataOutputStream dos) throws IOException {
		logger.info("文件服务器收到上传指令");
		long size = dis.readLong();
		logger.info("文件服务器收到文件大小:" + size);
		ArrayList<StorageServer> ssList = new StorageServerService()
				.getStorageServerBySizeLimit(size);
		if (ssList.size() < 2) {
			dos.writeBoolean(false);
			dos.flush();
			dos.writeUTF("没有可用的足够大的文件存储器");
			dos.flush();
			return false;
		}
		dos.writeBoolean(true);
		dos.flush();
		String fileName = dis.readUTF();
		logger.info("文件服务器接收到文件名称：" + fileName);
		logger.info("文件服务器确认可以上传");
		logger.info("文件服务器开始连接选择的存储服务器");
		Socket socket2 = null;
		Socket socket3 = null;
		try {
			socket2 = new Socket(ssList.get(0).getIpAddres(), ssList.get(0)
					.getPort());
			socket3 = new Socket(ssList.get(1).getIpAddres(), ssList.get(1)
					.getPort());
		} catch (ConnectException e1) {
			logger.error("服务器连接存储结点失败");
			return false;
		}
		DataOutputStream dos2 = new DataOutputStream(socket2.getOutputStream());
		DataOutputStream dos3 = new DataOutputStream(socket3.getOutputStream());
		dos2.writeChar('u');
		dos2.flush();
		dos3.writeChar('u');
		dos3.flush();
		logger.info("文件服务器向存储服务器发送上传指令");
		dos2.writeLong(size);
		dos2.flush();
		dos3.writeLong(size);
		dos3.flush();
		logger.info("文件服务器向存储服务器发送文件大小");
		dos2.writeUTF(fileName);
		dos2.flush();
		dos3.writeUTF(fileName);
		dos3.flush();
		logger.info("文件服务器向存储服务器发送文件名称");
		String uuid = Tool.getUUID();
		dos2.writeUTF(uuid);
		dos2.flush();
		dos3.writeUTF(uuid);
		dos3.flush();
		logger.info("文件服务器向存储服务器发送文件uuid");
		int bufferSize = 8096;// 缓冲区大小
		byte[] buffer = new byte[bufferSize];// 缓冲区
		long passedLen = 0;// 当前一共传输大小
		int readLen = 0;// 本次都去大小
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(new File("a1.test")));
		while (passedLen < size) {
			if (size - passedLen >= buffer.length) {
				readLen = dis.read(buffer, 0, buffer.length);
			} else {
				readLen = dis.read(buffer, 0, (int) (size - passedLen));
			}
			if (readLen == -1) {
				break;
			}
			passedLen = readLen + passedLen;
			dos2.write(buffer, 0, readLen);
			dos2.flush();
			dos3.write(buffer, 0, readLen);
			dos3.flush();
		}
		bos.close();
		// 关闭资源
		dos.close();
		dos2.close();
		dos3.close();
		dis.close();
		socket.close();
		socket2.close();
		socket3.close();
		if (passedLen == size) {
			logger.info("文件服务器上传文件成功");
			String fullName1 = ssList.get(0).getStorageDir() + "//" + uuid
					+ "+" + fileName;
			String fullName2 = ssList.get(1).getStorageDir() + "//" + uuid
					+ "+" + fileName;
			new FileService().insert(new SFile(fullName1, fileName, uuid,
					ssList.get(0).getId(), (int) size, ssList.get(1).getId(),
					fullName2));
			logger.info("数据库插入文件表项成功");
			StorageServer ssTemp = new StorageServer();
			ssTemp.setLeftVolume((int) (ssList.get(0).getLeftVolume() - size));
			ssTemp.setName(ssList.get(0).getName());
			ssTemp.setAlive(true);
			new StorageServerService().updateByName(ssTemp);
			ssTemp.setLeftVolume((int) (ssList.get(1).getLeftVolume() - size));
			ssTemp.setName(ssList.get(1).getName());
			ssTemp.setAlive(true);
			new StorageServerService().updateByName(ssTemp);
			logger.info("更改存储器大小成功");
			return true;
		} else {
			logger.info("文件服务器上传文件失败");
			return false;
		}

	}

	/**
	 * 接收来自存储服务器的文件并发送给客户端
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	private boolean sendFile(Socket socket, DataInputStream dis,
			DataOutputStream dos) throws IOException {
		logger.info("文件服务器收到下载指令");
		String uuid = dis.readUTF();
		logger.info("文件服务器收到文件uuid：" + uuid);
		FileService fService = new FileService();
		SFile file = fService.getFileByUUID(uuid);
		logger.info("文件服务器查找后台得到文件信息：" + file.toString());
		dos.writeUTF(file.getName());
		dos.flush();
		logger.info("文件服务器发送文件名给客户端");
		long size = file.getSize();
		dos.writeLong(size);
		dos.flush();
		logger.info("文件服务器发送文件大小给客户端");
		StorageServer ss = fService.getStorageServerByFileUUID(uuid);
		if (!ss.isAlive()) {
			ss = fService.getBackStorageServerByFileUUID(uuid);
			logger.info("主存储服务器宕机，启用备份文件所在存储服务器");
		}
		logger.info("文件服务器开始连接文件所在存储服务器\n\t:" + ss.toString());
		Socket socket2 = new Socket(ss.getIpAddres(), ss.getPort());
		DataOutputStream dos2 = new DataOutputStream(socket2.getOutputStream());
		DataInputStream dis2 = new DataInputStream(socket2.getInputStream());
		dos2.writeChar('d');
		dos2.flush();
		logger.info("文件服务器发送文件下载指令给存储服务器");
		dos2.writeUTF(file.getFullName());
		dos2.flush();
		logger.info("文件服务器发送文件名称和路径给存储服务器");
		logger.info("文件服务器开始将从客户端接收的文件发送给存储服务器");
		long passedLen = 0;// 当前一共传输大小
		int bufferSize = 8192;// 缓冲区大小
		byte[] buf = new byte[bufferSize];// 缓冲区
		while (true) {
			int read = 0;
			if (dis2 != null) {
				try {
					read = dis2.read(buf);
				} catch (SocketException e) {
					logger.error("服务器与存储结点的连接中断，文件下载退出");
					// 关闭资源
					dis2.close();
					dos2.close();
					dis.close();
					dos.close();
					socket.close();
					socket2.close();
					return false;
				}
			}
			if (read == -1) {
				break;
			}
			passedLen += read;
			try {
				dos.write(buf, 0, read);
				dos.flush();
			} catch (SocketException e) {
				logger.error("服务器与客户端的连接中断，下载终止");
				// 关闭资源
				dis2.close();
				dos2.close();
				dis.close();
				dos.close();
				socket.close();
				socket2.close();
				return false;
			}
		}
		// 关闭资源
		dis2.close();
		dos2.close();
		dis.close();
		dos.close();
		socket.close();
		socket2.close();
		if (passedLen == size) {
			logger.info("文件下载成功");
			return true;
		} else {
			logger.info("文件下载失败");
			return false;
		}

	}

	/**
	 * 删除文件
	 * 
	 * @param socket
	 * @throws IOException
	 */
	private boolean deleteFile(Socket socket, DataInputStream dis,
			DataOutputStream dos) throws IOException {
		logger.info("文件服务器收到删除指令");
		String uuid = dis.readUTF();
		logger.info("文件服务器收到文件uuid:" + uuid);
		FileService fService = new FileService();
		SFile file = fService.getFileByUUID(uuid);
		logger.info("文件服务器查找后台得到文件信息：" + file.toString());
		StorageServer ss = fService.getStorageServerByFileUUID(uuid);
		StorageServer ssBack = fService.getBackStorageServerByFileUUID(uuid);
		logger.info("文件服务器开始连接文件所在存储服务器\n\t：" + ss.toString() + "\n"
				+ ssBack.toString());
		Socket socket2 = new Socket(ss.getIpAddres(), ss.getPort());
		DataOutputStream dos2 = new DataOutputStream(socket2.getOutputStream());
		DataInputStream dis2 = new DataInputStream(socket2.getInputStream());
		Socket socket3 = new Socket(ssBack.getIpAddres(), ssBack.getPort());
		DataOutputStream dos3 = new DataOutputStream(socket3.getOutputStream());
		DataInputStream dis3 = new DataInputStream(socket3.getInputStream());
		dos2.writeChar('r');
		dos2.flush();
		dos3.writeChar('r');
		dos3.flush();
		logger.info("文件服务器发送删除指令给存储服务器");
		dos2.writeUTF(file.getFullName());
		dos2.flush();
		dos3.writeUTF(file.getBackFullName());
		dos3.flush();
		logger.info("文件服务器发送文件全名给存储服务器");
		boolean result2 = dis2.readBoolean();
		boolean result3 = dis3.readBoolean();
		logger.info("文件服务器收到存储服务器文件删除的确认信息");
		// 关闭资源
		dos2.close();
		dis2.close();
		dos3.close();
		dis3.close();
		socket.close();
		socket2.close();
		socket3.close();
		dis.close();
		dos2.close();
		if (result2 & result3) {
			// 更新数据库
			fService.deleteFileByUUID(uuid);
			ss.setLeftVolume(ss.getLeftVolume() + file.getSize());
			ssBack.setLeftVolume(ssBack.getLeftVolume() + file.getSize());
			StorageServerService ssService = new StorageServerService();
			ss.setAlive(true);
			ssService.updateByName(ss);
			ssService.updateByName(ssBack);
			logger.info("文件服务器更新数据库成功");
			return true;
		}
		return false;
	}

	/**
	 * 根据uuid重命名文件
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	private boolean rename(Socket socket, DataInputStream dis,
			DataOutputStream dos) throws IOException {
		logger.info("文件服务器收到重命名指令");
		String uuid = dis.readUTF();
		logger.info("文件服务器收到文件uuid:" + uuid);
		String newName = dis.readUTF();
		logger.info("文件服务器收到文件新的名称:" + newName);
		FileService fService = new FileService();
		SFile file = fService.getFileByUUID(uuid);
		logger.info("文件服务器查找后台得到文件信息：" + file.toString());
		StorageServer ss = fService.getStorageServerByFileUUID(uuid);
		StorageServer ssBack = fService.getBackStorageServerByFileUUID(uuid);
		logger.info("文件服务器开始连接文件所在存储服务器\n\t：" + ss.toString());
		Socket socket2 = new Socket(ss.getIpAddres(), ss.getPort());
		DataOutputStream dos2 = new DataOutputStream(socket2.getOutputStream());
		DataInputStream dis2 = new DataInputStream(socket2.getInputStream());
		Socket socket3 = new Socket(ssBack.getIpAddres(), ssBack.getPort());
		DataOutputStream dos3 = new DataOutputStream(socket3.getOutputStream());
		DataInputStream dis3 = new DataInputStream(socket3.getInputStream());
		dos2.writeChar('m');
		dos2.flush();
		dos3.writeChar('m');
		dos3.flush();
		logger.info("文件服务器发送重命名指令给存储服务器");
		dos2.writeUTF(file.getFullName());
		dos2.flush();
		dos3.writeUTF(file.getBackFullName());
		dos3.flush();
		logger.info("文件服务器发送文件全名给存储服务器");
		dos2.writeUTF(newName);
		dos2.flush();
		dos3.writeUTF(newName);
		dos3.flush();
		logger.info("文件服务器发送文件新名给存储服务器");
		dos2.writeUTF(uuid);
		dos2.flush();
		dos3.writeUTF(uuid);
		dos3.flush();
		logger.info("文件服务器发送文件uuid给存储服务器");
		boolean result2 = dis2.readBoolean();
		boolean result3 = dis3.readBoolean();
		logger.info("文件服务器收到存储服务器文件重命名的确认信息");
		// 关闭资源
		dos2.close();
		dis2.close();
		dos3.close();
		dis3.close();
		socket.close();
		socket2.close();
		socket3.close();
		dis.close();
		dos2.close();
		if (result2 && result3) {
			dos.writeBoolean(true);
			dos.flush();
			logger.info("文件服务器通知客户端重命名成功");
			dos.close();
			// 更新数据库
			SFile file2 = new SFile();
			file2.setUuid(uuid);
			file2.setName(newName);
			file2.setFullName(ss.getStorageDir() + "//" + uuid + "+" + newName);
			file2.setBackFullName(ssBack.getStorageDir() + "//" + uuid + "+"
					+ newName);
			fService.updateFileNameByUUID(file2);
			logger.info("文件服务器更新数据库成功");
			return true;
		}
		return false;
	}

	/**
	 * 根据存储服务器发来的心跳包更新可用信息
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public void updateStorageServerInfo(Socket socket, DataInputStream dis,
			DataOutputStream dos) throws IOException {
		String name = dis.readUTF();
		StorageServerService ssService = new StorageServerService();
		FileServer.timerMap.get(name).cancel();
		FileServer.timerMap.put(name, new Timer());
		FileServer.timerMap.get(name).schedule(new FileServerTimerTask(name),
				10 * 1000);
		StorageServer ss = ssService.selectStorageServerByName(name);
		if (ss.isAlive()) {
			return;
		}
		ss.setAlive(true);
		ssService.updateByName(ss);
		dis.close();
		socket.close();
		logger.info("文件服务器收到存储服务器 " + name + " 发来的心跳包");
		logger.info("文件服务器更新存储服务器 " + name + "为可用");
	}

	@Override
	public void service(Socket socket) throws IOException {
		DataInputStream dis = null;
		DataOutputStream dos = null;
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		char command = dis.readChar();
		switch (command) {
		case 'u':// 上传指令
			if (recieveFile(socket, dis, dos)) {
				logger.info("文件服务器上传文件成功");
			} else {
				logger.info("文件服务器上传文件失败");
			}
			break;
		case 'd':// 下载指令
			if (sendFile(socket, dis, dos)) {
				logger.info("文件服务器下载文件成功");
			} else {
				logger.info("文件服务器下载文件失败");
			}
			break;
		case 'r':// 删除指令
			if (deleteFile(socket, dis, dos)) {
				logger.info("文件服务器删除文件成功");
			} else {
				logger.info("文件服务器删除文件失败");
			}
			break;
		case 'm':// 重命名指令
			if (rename(socket, dis, dos)) {
				logger.info("文件服务器重命名文件成功");
			} else {
				logger.info("文件服务器重命名文件失败");
			}
			break;
		case 'a':
			updateStorageServerInfo(socket, dis, dos);
			break;
		default:
			break;
		}
	}

}
