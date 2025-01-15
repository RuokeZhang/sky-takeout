package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

public interface SetmealService {
    void save(SetmealDTO setmealDTO);

    PageResult query(SetmealPageQueryDTO setmealPageQueryDTO);
    SetmealVO getByIdWithDishes(Long id);

    void update(SetmealDTO setmealDTO);

    void batchDelete(Long[] ids);

    void changeStatus(Integer status, Long id);
}
