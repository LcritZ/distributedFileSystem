package com.yu.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class FileClient {

	private Logger logger = Logger.getLogger(FileClient.class);

	private Socket socket = null;

	private DataInputStream dis = null;
	private DataOutputStream dos = null;

	public FileClient() throws UnknownHostException, IOException {
		socket = new Socket("localhost", 4321);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * 上传
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public boolean upload(String filePath) throws IOException {
		dos.writeChar('u');// 上传文件指令
		dos.flush();
		logger.info("客户端 发送上传指令给文件服务器");
		File file = new File(filePath);
		dos.writeLong(file.length());// 将文件大小告诉文件服务器以确定是否可以上传
		dos.flush();
		logger.info("客户端发送文件大小给文件服务器");
		boolean result = dis.readBoolean();// 文件服务器返回信息告诉客户端是否可上传
		if (!result) {
			String erroInfo = dis.readUTF();// 读出错误信息
			logger.error(erroInfo);
			return false;
		}
		logger.info("服务器返回确定信息可以上传");
		dos.writeUTF(file.getName());// 将文件名字发送给服务器
		dos.flush();
		logger.info("客户端发送文件名称给文件服务器");
		logger.info("客户端开始上传文件给文件服务器");
		// 文件传输
		int bufferSize = 8192;// 缓冲区大小
		byte[] buf = new byte[bufferSize];// 缓冲区
		long passedlen = 0; // 已传输大小
		long len = 0; // 每次用read读取返回的值，代表本次上传的字节数
		long size = file.length();// 文件大小
		FileInputStream fis = new FileInputStream(file);// 打开要上传的文件流
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
			logger.info("客户端文件上传完成");
			return true;
		} else {
			logger.error("客户端文件上传失败");
			return false;
		}
	}

	/**
	 * 下载
	 * 
	 * @param fileUUID
	 * @return
	 * @throws IOException
	 */
	public boolean download(String fileUUID) throws IOException {
		dos.writeChar('d');
		dos.flush();
		dos.writeUTF(fileUUID);
		dos.flush();
		logger.info("客户端发送文件uuid给文件服务器");
		String fileName = dis.readUTF();
		logger.info("客户端接收到文件服务器发送的文件名称:"+fileName);
		long size = dis.readLong();
		logger.info("客户端接收到文件服务器发送的文件大小:"+size);
		File file = new File(fileName);
		FileOutputStream fos = new FileOutputStream(file);
		logger.info("客户端开始接收文件服务器发送的文件");
		// 文件传输
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
		// 关闭资源
		fos.close();
		dos.close();
		dis.close();
		if (passedLen == size) {
			logger.info("客户端接收文件成功");
			return true;
		} else {
			logger.info("客户端接收文件失败");
			return false;
		}
	}

	/**
	 * 根据uuid删除文件
	 * 
	 * @param fileUUID
	 * @return
	 * @throws IOException
	 */
	public boolean remove(String fileUUID) throws IOException {
		dos.writeChar('r');
		dos.flush();
		dos.writeUTF(fileUUID);
		dos.flush();
		logger.info("客户端发送文件uuid给文件服务器");
		dos.close();
		return true;
	}

	/**
	 * 根据uuid重命名文件
	 * 
	 * @param fileUUID
	 * @param newFileName
	 * @return
	 * @throws IOException
	 */
	public boolean rename(String fileUUID, String newFileName)
			throws IOException {
		dos.writeChar('m');
		dos.flush();
		dos.writeUTF(fileUUID);
		dos.flush();
		logger.info("客户端发送文件uuid给文件服务器");
		dos.writeUTF(newFileName);
		dos.flush();
		logger.info("客户端发送文件新名称给文件服务器");
		dos.close();
		return true;
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		FileClient client = new FileClient();
		if (args[0].trim().equals("-u")) {
			client.upload(args[1]);
		} else if (args[0].trim().equals("-d")) {
			client.download(args[1]);
		} else if (args[0].trim().equals("-r")) {
			client.remove(args[1]);
		} else if (args[0].trim().equals("-m")) {
			client.rename(args[1], args[2]);
		}
	}

}
