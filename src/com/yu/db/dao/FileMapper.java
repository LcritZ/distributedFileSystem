package com.yu.db.dao;

import com.yu.db.model.SFile;
import com.yu.db.model.StorageServer;


public interface FileMapper {
	
	/**
	 * 
	 * @param record
	 * @return
	 */
    int insert(SFile record);

    /**
     * 
     * @param record
     * @return
     */
    int insertSelective(SFile record);
    
    /**
     * 
     * @return
     */
    SFile getFileByUUID(String uuid);
    
    /**
     * 通过文件的uuid查找文件所存储的服务器
     * @param uuid
     * @return
     */
    StorageServer getStorageServerByFileUUID(String uuid);
    
    /**
     * 根据uuid更新文件表项
     * @param uuid
     * @return
     */
    int updateFileNameByUUID(SFile file);
    
    /**
     * 根据uuid删除文件表项
     * @param uuid
     * @return
     */
    int deleteFileByUUID(String uuid); 
}