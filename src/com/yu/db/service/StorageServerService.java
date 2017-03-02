package com.yu.db.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.yu.db.model.StorageServer;

public class StorageServerService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String resource = "\\mybatis_conf.xml";
	private Reader reader;
	SqlSessionFactory sessionFactory;
	private SqlSession session;

	public StorageServerService() {
		try {
			reader = Resources.getResourceAsReader(resource);
			// 构建sqlSession的工厂
			sessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据id删除指定存储服务器
	 * 
	 * @param id
	 * @return
	 */
	public int deleteByPrimaryKey(Integer id) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.StorageServerMapper.deleteByPrimaryKey";
		int result = (int) session.delete(statement, id);
		session.commit();
		session.close();
		return result;
	}

	/**
	 * 插入新的存储服务器
	 * 
	 * @param record
	 * @return
	 */
	public int insert(StorageServer record) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.StorageServerMapper.insert";
		int result = (int) session.insert(statement, record);
		session.commit();
		session.close();
		return result;
	}

	/**
	 * 根据id查找存储服务器
	 * 
	 * @param id
	 * @return
	 */
	public StorageServer selectByPrimaryKey(Integer id) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.StorageServerMapper.selectByPrimaryKey";
		StorageServer ss = (StorageServer) session.selectOne(statement, id);
		session.commit();
		session.close();
		return ss;
	}

	/**
	 * 根据名字更新存储服务器
	 * 
	 * @param name
	 * @return
	 */
	public int updateByName(StorageServer ss) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.StorageServerMapper.updateByName";
		int result = (int) session.update(statement, ss);
		session.commit();
		session.close();
		return result;
	}

	/**
	 * 根据大小限制查找存储服务器
	 * 
	 * @param limit
	 * @return
	 */
	public ArrayList<StorageServer> getStorageServerBySizeLimit(long limit) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.StorageServerMapper.getStorageServerBySizeLimit";
		@SuppressWarnings("unchecked")
		ArrayList<StorageServer> ssList = (ArrayList<StorageServer>) session
				.selectList(statement, limit);
		session.commit();
		session.close();
		return ssList;
	}

	/**
	 * 根据名字查找存储服务器
	 * 
	 * @param name
	 * @return
	 */
	public StorageServer selectStorageServerByName(String name) {
		// 创建能执行映射文件中sql的sqlSession
		session = sessionFactory.openSession();
		String statement = "com.yu.db.mapping.StorageServerMapper.selectStorageServerByName";
		StorageServer ss = (StorageServer) session.selectOne(statement, name);
		session.commit();
		session.close();
		return ss;
	}

	public static void main(String[] args) {
		StorageServerService sss = new StorageServerService();
		StorageServer ss = sss.selectStorageServerByName("ss1");
		System.out.println(ss);
	}
}
