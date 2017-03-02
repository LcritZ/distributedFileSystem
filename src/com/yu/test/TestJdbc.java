package com.yu.test;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.yu.db.model.StorageServer;

public class TestJdbc {

	public static void main(String[] args) throws IOException {
		
		String resource = "\\mybatis_conf.xml";
		// 使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
		// InputStream is = TestJdbc.class.getClassLoader().getResourceAsStream(
		// resource);
		// 构建sqlSession的工厂
		// SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder()
		// .build(is);
		// 使用MyBatis提供的Resources类加载mybatis的配置文件（它也加载关联的映射文件）
		Reader reader = Resources.getResourceAsReader(resource);
		// 构建sqlSession的工厂
		SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder()
				.build(reader);
		// 创建能执行映射文件中sql的sqlSession
		SqlSession session = sessionFactory.openSession();
		/**
		 * 映射sql的标识字符串，
		 * me.gacl.mapping.userMapper是userMapper.xml文件中mapper标签的namespace属性的值，
		 * getUser是select标签的id属性值，通过select标签的id属性值就可以找到要执行的SQL
		 */
		String statement = "com.yu.db.mapping.StorageServerMapper.getStorageServerBySizeLimit";// 映射sql的标识字符串
		// 执行查询返回一个唯一user对象的sql
		StorageServer ss = (StorageServer) session.selectOne(statement,60);
		System.out.println(ss);
		
		/*String statement = "com.yu.db.mapping.FileMapper.getStorageServerByFileUUID";
		StorageServer ss = (StorageServer) session.selectOne(statement,"1111");
		System.out.println(ss);*/
	}
}
