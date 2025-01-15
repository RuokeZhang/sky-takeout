package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Transactional
    public void save(SetmealDTO setmealDTO) {
        //1. save this setmeal to setmeal table
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);

        //2. save information to the setmeal_dish table
        //get generated id

        //save dish
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setMealDishMapper.batchSave(setmealDishes);
    }

    @Override
    public PageResult query(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.queryPage(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public SetmealVO getByIdWithDishes(Long id){
        //1. get the setmeal
        Setmeal setmeal=setmealMapper.getById(id);
        //2. get the dishes by setmeal id
        List<SetmealDish> dishList=setMealDishMapper.getDishesBySetmealId(id);
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(dishList);
        return setmealVO;
    }

    public void update(SetmealDTO setmealDTO){
        //update information in the setmeal table
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //delete all the dishes by setmeal id in the setmeal_dish table
        setMealDishMapper.deleteById(setmealDTO.getId());
        //insert dishes into the setmeal_dish table
        List<SetmealDish> list=setmealDTO.getSetmealDishes();
        if(list!=null&&list.size()>0){
            list.forEach(dish->{dish.setSetmealId(setmealDTO.getId());});
            setMealDishMapper.batchSave(list);
        }
    }

    public void batchDelete(Long[] ids){
        //check if the setmeals could be deleted
        for(Long id:ids){
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteByIds(ids);
        setMealDishMapper.deleteBySetMealIds(ids);
    }

    @Override
    public void changeStatus(Integer status, Long id) {
        Setmeal setmeal=setmealMapper.getById(id);
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status==StatusConstant.ENABLE){
           //get the dishes
            List<Dish> dishes= dishMapper.getBySetmealID(id);
            if(dishes!=null&&dishes.size()>0){
                dishes.forEach(dish->{
                    if(dish.getStatus()== StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        //change the status
        Setmeal setmeal1=Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal1);
    }
}
