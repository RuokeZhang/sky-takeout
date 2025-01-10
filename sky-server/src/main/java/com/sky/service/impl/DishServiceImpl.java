package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;



    @Transactional
    public void saveWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //save dish to the menu
        dishMapper.insert(dish);
        //get generated id
        Long dishId = dish.getId();

        //save flavor
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if(dishFlavors!=null&&dishFlavors.size()>0){
            dishFlavors.forEach(dishFlavor->{dishFlavor.setDishId(dishId);});
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }

    public PageResult query(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);


        return new PageResult(page.getTotal(), page.getResult());
    }

    public void deleteBatch(List<Long> ids){
        //check if the dish could be deleted
        for(Long id:ids){
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()==StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //if dishes is binded with some set, it can't be deleted
        List<Long> setMealIds=   setMealDishMapper.getSetMealDishIdListByDishIds(ids);
        if(setMealIds!=null&&setMealIds.size()>0){
        throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //delete dishes from the dish table
        dishMapper.deleteByIds(ids);
        //delete their flavours in the dish flavour table
        //delete from dish_flavor where dish_id in ( , , ,)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    public DishVO getByIdWithFlavor(Long id){
       Dish dish= dishMapper.getById(id );
       List<DishFlavor> flavors=dishFlavorMapper.getByDishId(id);
       DishVO dishVO=new DishVO();
       BeanUtils.copyProperties(dish,dishVO);
       dishVO.setFlavors(flavors);
       return dishVO;
    }

    public void updateWithFlavor(DishDTO dishDTO){
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
        dishMapper.update(dish);
        //删除原有的口味数据
        dishFlavorMapper.deleteById (dishDTO.getId());
        //重新插入口味数据
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if(dishFlavors!=null&&dishFlavors.size()>0){
            dishFlavors.forEach(dishFlavor->{dishFlavor.setDishId(dishDTO.getId());});
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }
}
