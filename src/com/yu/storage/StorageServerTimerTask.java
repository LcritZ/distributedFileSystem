package com.yu.storage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;

public class StorageServerTimerTask extends TimerTask {

	@Override
	public void run() {
		try {
			Socket socket = new Socket("localhost", 4321);
			DataOutputStream dos = new DataOutputStream(
					socket.getOutputStream());
			dos.writeChar('a');
			dos.writeUTF(FileStorage.storageServer.getName());
			dos.flush();
			dos.close();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
