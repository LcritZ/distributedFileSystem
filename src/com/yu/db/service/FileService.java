package com.yu.db.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.yu.db.model.SFile;
import com.yu.db.model.StorageServer;

public class FileService implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String resource = "\\mybatis_conf.xml";
	private Reader reader;
	SqlSessionFactory sessionFactory;
	private SqlSession session;

	public FileService() {
		try {
			reader = Resources.getResourceAsReader(resource);
			// 构建sqlSession的工厂
			sessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入新文件项
	 * 
	 * @param record
	 * @return
	 */
	public int insert(SFile record) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.insert";
		int result = (int) session.insert(statement, record);
		session.commit();
		session.close();
		return result;
	}

	/**
	 * 插入新文件项
	 * 
	 * @param record
	 * @return
	 */
	public int insertSelective(SFile record) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.insertSelective";
		int result = (int) session.insert(statement, record);
		session.commit();
		session.close();
		return result;
	}

	/**
	 * 根据uuid获取文件项
	 * 
	 * @param uuid
	 * @return
	 */
	public SFile getFileByUUID(String uuid) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.getFileByUUID";
		SFile file = (SFile) session.selectOne(statement, uuid);
		session.commit();
		session.close();
		return file;
	}

	/**
	 * 根据uuid获取文件所在的存储服务器
	 * 
	 * @param uuid
	 * @return
	 */
	public StorageServer getStorageServerByFileUUID(String uuid) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.getStorageServerByFileUUID";
		StorageServer ss = (StorageServer) session.selectOne(statement, uuid);
		session.commit();
		session.close();
		return ss;
	}

	/**
	 * 根据uuid获取文件备份所在的存储服务器
	 * 
	 * @param uuid
	 * @return
	 */
	public StorageServer getBackStorageServerByFileUUID(String uuid) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.getBackStorageServerByFileUUID";
		StorageServer ss = (StorageServer) session.selectOne(statement, uuid);
		session.commit();
		session.close();
		return ss;
	}

	/**
	 * 根据uuid更新文件名
	 * 
	 * @param file
	 * @return
	 */
	public int updateFileNameByUUID(SFile file) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.updateFileNameByUUID";
		int rs = (int) session.update(statement, file);
		session.commit();
		session.close();
		return rs;
	}

	/**
	 * 根据uuid删除文件表项
	 * 
	 * @param uuid
	 * @return
	 */
	public int deleteFileByUUID(String uuid) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.deleteFileByUUID";
		int rs = (int) session.delete(statement, uuid);
		session.commit();
		session.close();
		return rs;
	}

	public ArrayList<SFile> listFileByStorageServerName(String name) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.FileMapper.listFileByStorageServerName";
		@SuppressWarnings("unchecked")
		ArrayList<SFile> fileList = (ArrayList<SFile>) session.selectList(statement, name);
		session.commit();
		session.close();
		return fileList;
	}

	public static void main(String[] args) {
		FileService fs = new FileService();
		ArrayList<SFile> fileList = fs.listFileByStorageServerName("ss1");
		for (Iterator<SFile> iterator = fileList.iterator(); iterator.hasNext();) {
			SFile sFile = (SFile) iterator.next();
			System.out.println(sFile);
		}
	}
}
