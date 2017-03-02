package com.yu.server;

public interface IOStrategy {
	public void service(java.net.Socket socket) throws Exception; // 对传入的socket对象进行处理
}
