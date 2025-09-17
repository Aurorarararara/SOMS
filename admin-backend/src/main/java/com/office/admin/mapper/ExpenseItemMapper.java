package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.ExpenseItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销明细Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface ExpenseItemMapper extends BaseMapper<ExpenseItem> {

}