package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.CollaborativeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CollaborativeDocumentMapper extends BaseMapper<CollaborativeDocument> {
    
    /**
     * 分页查询协同文档
     */
    @Select("<script>" +
            "SELECT * FROM collaborative_documents " +
            "WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY update_time DESC" +
            "</script>")
    IPage<CollaborativeDocument> selectPageWithKeyword(Page<CollaborativeDocument> page, @Param("keyword") String keyword);
    
    /**
     * 根据用户ID查询协同文档
     */
    @Select("SELECT * FROM collaborative_documents WHERE creator_id = #{userId} AND is_deleted = 0 ORDER BY update_time DESC")
    List<CollaborativeDocument> selectByCreatorId(@Param("userId") Long userId);
    
    /**
     * 根据文档类型查询
     */
    @Select("SELECT * FROM collaborative_documents WHERE document_type = #{documentType} AND is_deleted = 0 ORDER BY update_time DESC")
    List<CollaborativeDocument> selectByDocumentType(@Param("documentType") String documentType);
    
    /**
     * 查询用户有权限访问的文档
     */
    @Select("<script>" +
            "SELECT DISTINCT cd.* FROM collaborative_documents cd " +
            "LEFT JOIN collaborative_permissions cp ON cd.id = cp.document_id " +
            "WHERE cd.is_deleted = 0 AND (" +
            "cd.creator_id = #{userId} " +
            "OR cp.user_id = #{userId} " +
            "OR cd.public_access = 1" +
            ") " +
            "ORDER BY cd.update_time DESC" +
            "</script>")
    List<CollaborativeDocument> selectAccessibleByUserId(@Param("userId") Long userId);
    
    /**
     * 更新文档版本号
     */
    @Update("UPDATE collaborative_documents SET version = version + 1, update_time = NOW() WHERE id = #{documentId}")
    int incrementVersion(@Param("documentId") Long documentId);
    
    /**
     * 更新文档内容
     */
    @Update("UPDATE collaborative_documents SET content = #{content}, version = version + 1, update_time = NOW() WHERE id = #{documentId}")
    int updateDocumentContent(@Param("documentId") Long documentId, @Param("content") String content);
}