package com.yu.test;

import java.io.File;

public class TestRename {

	public static void main(String[] args) {
		File file = new File("F://test.txt");
		file.renameTo(new File("F://test"));
	}
}
