package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
    
    /**
     * 分页查询文档
     */
    IPage<Document> selectPageWithKeyword(Page<Document> page, @Param("keyword") String keyword);
    
    /**
     * 根据分类查询模板
     */
    @Select("SELECT * FROM documents WHERE is_template = true " +
            "AND status = 'active' " +
            "AND (#{category} IS NULL OR category = #{category}) " +
            "ORDER BY create_time DESC")
    List<Document> selectTemplatesByCategory(@Param("category") String category);
    
    /**
     * 根据用户ID查询文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} " +
            "AND status != 'deleted' ORDER BY create_time DESC")
    List<Document> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据任务ID查询文档
     */
    @Select("SELECT * FROM documents WHERE task_id = #{taskId} " +
            "AND status != 'deleted' ORDER BY create_time DESC")
    List<Document> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 增加下载次数
     */
    @Update("UPDATE documents SET download_count = download_count + 1 WHERE id = #{documentId}")
    int incrementDownloadCount(@Param("documentId") Long documentId);
}