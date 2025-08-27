package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
    
    /**
     * 分页查询文档
     */
    @Select("SELECT * FROM documents WHERE status != 'deleted' " +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY create_time DESC")
    IPage<Document> selectPageWithKeyword(Page<Document> page, @Param("keyword") String keyword);
    
    /**
     * 根据类型查询模板
     */
    @Select("SELECT * FROM documents WHERE is_template = 1 AND status = 'active' " +
            "AND category = #{category} ORDER BY download_count DESC")
    List<Document> selectTemplatesByCategory(@Param("category") String category);
    
    /**
     * 查询用户的文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND status != 'deleted' " +
            "ORDER BY create_time DESC")
    List<Document> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询任务相关文档
     */
    @Select("SELECT * FROM documents WHERE task_id = #{taskId} AND status != 'deleted' " +
            "ORDER BY create_time DESC")
    List<Document> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 更新下载次数
     */
    @Select("UPDATE documents SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(@Param("id") Long id);
}