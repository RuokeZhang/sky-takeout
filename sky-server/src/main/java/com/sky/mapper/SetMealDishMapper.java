package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {
    public List<Long> getSetMealDishIdListByDishIds(List<Long> dishIds);
    //select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)

}
