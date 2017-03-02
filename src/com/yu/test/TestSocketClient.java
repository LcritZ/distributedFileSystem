package com.yu.test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class TestSocketClient{

	public static void main(String[] args) throws IOException {
		Socket s = null;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			s = new Socket("localhost", 3456);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());
		File f = new File("a.rar");
		
		dos.writeInt((int) f.length());

		byte[] buffer = new byte[4096];
		int rr = 0;

		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);

		while ((rr = bis.read(buffer)) != -1) {
			dos.write(buffer, 0, rr);
			System.out.println(rr);
			dos.flush();
		}
		dis.close();
		bis.close();
		fis.close();
	}
}
