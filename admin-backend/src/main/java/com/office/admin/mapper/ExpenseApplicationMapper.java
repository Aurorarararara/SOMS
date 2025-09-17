package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.office.admin.entity.ExpenseApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 报销申请Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface ExpenseApplicationMapper extends BaseMapper<ExpenseApplication> {

    /**
     * 获取费用申请统计数据
     */
    @Select("SELECT status, COUNT(*) as count FROM expense_applications GROUP BY status")
    List<Map<String, Object>> getExpenseStatsByStatus();
}