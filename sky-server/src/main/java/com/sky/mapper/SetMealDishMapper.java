package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {
    //select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)
    public List<Long> getSetMealDishIdListByDishIds(List<Long> dishIds);


    void batchSave(List<SetmealDish> dishList);


}
