package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.ExpenseItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报销明细Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface ExpenseItemMapper extends BaseMapper<ExpenseItem> {

    /**
     * 根据申请ID查询报销明细
     */
    @Select("SELECT * FROM expense_items WHERE application_id = #{applicationId} ORDER BY created_at ASC")
    List<ExpenseItem> findByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据费用类型查询报销明细
     */
    @Select("SELECT * FROM expense_items WHERE expense_type = #{expenseType} ORDER BY created_at DESC")
    List<ExpenseItem> findByExpenseType(@Param("expenseType") String expenseType);

    /**
     * 根据申请ID和费用类型查询报销明细
     */
    @Select("SELECT * FROM expense_items WHERE application_id = #{applicationId} AND expense_type = #{expenseType}")
    List<ExpenseItem> findByApplicationIdAndExpenseType(@Param("applicationId") Long applicationId, 
                                                       @Param("expenseType") String expenseType);

    /**
     * 计算申请的明细总金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM expense_items WHERE application_id = #{applicationId}")
    BigDecimal sumAmountByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 统计申请的明细数量
     */
    @Select("SELECT COUNT(*) FROM expense_items WHERE application_id = #{applicationId}")
    int countByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 统计指定费用类型的明细数量
     */
    @Select("SELECT COUNT(*) FROM expense_items WHERE expense_type = #{expenseType}")
    int countByExpenseType(@Param("expenseType") String expenseType);

    /**
     * 查询有附件的报销明细
     */
    @Select("SELECT * FROM expense_items WHERE attachment_path IS NOT NULL AND attachment_path != '' " +
            "ORDER BY created_at DESC")
    List<ExpenseItem> findItemsWithAttachment();

    /**
     * 根据申请ID查询有附件的明细
     */
    @Select("SELECT * FROM expense_items WHERE application_id = #{applicationId} " +
            "AND attachment_path IS NOT NULL AND attachment_path != ''")
    List<ExpenseItem> findItemsWithAttachmentByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据附件路径查询明细
     */
    @Select("SELECT * FROM expense_items WHERE attachment_path = #{attachmentPath}")
    ExpenseItem findByAttachmentPath(@Param("attachmentPath") String attachmentPath);

    /**
     * 批量删除申请的所有明细
     */
    @Delete("DELETE FROM expense_items WHERE application_id = #{applicationId}")
    int deleteByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据费用类型统计金额
     */
    @Select("SELECT expense_type, COALESCE(SUM(amount), 0) as total_amount " +
            "FROM expense_items ei " +
            "JOIN expense_applications ea ON ei.application_id = ea.application_id " +
            "WHERE ea.status = 'approved' " +
            "GROUP BY expense_type " +
            "ORDER BY total_amount DESC")
    List<Object> getExpenseStatisticsByType();

    /**
     * 查询指定申请ID范围内的明细（用于批量操作）
     */
    @Select("SELECT * FROM expense_items WHERE application_id IN (${applicationIds}) ORDER BY application_id, created_at")
    List<ExpenseItem> findByApplicationIds(@Param("applicationIds") String applicationIds);
}