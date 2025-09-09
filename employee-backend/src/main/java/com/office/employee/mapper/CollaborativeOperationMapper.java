package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.CollaborativeOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CollaborativeOperationMapper extends BaseMapper<CollaborativeOperation> {
    
    /**
     * 根据文档ID查询操作记录（分页）
     */
    @Select("SELECT * FROM collaborative_operations WHERE document_id = #{documentId} ORDER BY sequence_number ASC")
    IPage<CollaborativeOperation> selectPageByDocumentId(Page<CollaborativeOperation> page, @Param("documentId") Long documentId);
    
    /**
     * 根据文档ID和序列号范围查询操作记录
     */
    @Select("SELECT * FROM collaborative_operations WHERE document_id = #{documentId} AND sequence_number > #{fromSequence} ORDER BY sequence_number ASC")
    List<CollaborativeOperation> selectByDocumentIdAndSequence(@Param("documentId") Long documentId, @Param("fromSequence") Long fromSequence);
    
    /**
     * 获取文档的最新序列号
     */
    @Select("SELECT MAX(sequence_number) FROM collaborative_operations WHERE document_id = #{documentId}")
    Long selectMaxSequenceByDocumentId(@Param("documentId") Long documentId);
    
    /**
     * 根据用户ID查询操作记录
     */
    @Select("SELECT * FROM collaborative_operations WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<CollaborativeOperation> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询文档的操作统计
     */
    @Select("SELECT operation_type, COUNT(*) as count FROM collaborative_operations WHERE document_id = #{documentId} GROUP BY operation_type")
    List<java.util.Map<String, Object>> selectOperationStatsByDocumentId(@Param("documentId") Long documentId);
}