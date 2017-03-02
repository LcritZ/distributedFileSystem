package com.yu.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TestResourceRoute {

	public static void main(String[] args) throws IOException {
		//获取当前类的绝对路径 /F:/Users/workspace/fileServer/bin/com/yu/test/
		System.out.println(TestResourceRoute.class.getResource("").getPath());
		//获取当前项目的根路径 F:\Users\workspace\fileServer
		System.out.println(new File("").getCanonicalPath());
		System.out.println((URL)TestResourceRoute.class.getClassLoader()
				.getResource("test.properties"));
		//获取当前项目的根路径 F:\Users\workspace\fileServer
		System.out.println(System.getProperty("user.dir"));
		
		System.out.println(System.getProperty("user.home"));
	}
}
