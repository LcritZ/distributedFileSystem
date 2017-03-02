package com.yu.test;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestSocketServer {

	private static ServerSocket server;

	public static void main(String[] args) throws IOException {
		server = new ServerSocket(3456);
		Socket socket = server.accept();

		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		DataInputStream dis = new DataInputStream(is);
		DataOutputStream dos = new DataOutputStream(os);

		long len = dis.readInt();
		byte[] buffer = new byte[8096];
		long r = 0;
		int rr = 0;

		FileOutputStream fos = new FileOutputStream(new File("test1"));
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		r = 0;
		rr = 0;

		while (r < len) {
			if (len - r >= buffer.length) {
				rr = dis.read(buffer, 0, buffer.length);
			} else {
				rr = dis.read(buffer, 0, (int) (len - r));
			}
			System.out.println(rr);
			r = r + rr;
			bos.write(buffer, 0, rr);
		}
		dos.close();
		bos.close();
		fos.close();

	}
}
