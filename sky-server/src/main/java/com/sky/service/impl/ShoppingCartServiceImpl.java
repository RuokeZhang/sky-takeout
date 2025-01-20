package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    public void addToCart(ShoppingCartDTO shoppingCartDTO) {
        //check if the item is already in the client's shopping cart
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //get user id
        Long userId=BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list= shoppingCartMapper.list(shoppingCart);
        if(list!=null&&list.size()>0){
            //list only has one entry. This dish is already in the shopping cart
            shoppingCart=list.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber()+1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        }else{
            //insert a new item into the cart. It's either a setmeal or a dish
            Long dishId=shoppingCartDTO.getDishId();
            if (dishId != null) {
                // 本次添加到购物车的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                // 本次添加到购物车的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }
    }
    public List<ShoppingCart> showShoppingCart() {
        Long userID=BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userID).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }
    public void cleanShoppingCart(){
        Long userId=BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    public void subFromShoppingCart(ShoppingCartDTO shoppingCartDTO){
        //if this item's number is 1, delete it from the database
        Long userId=BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .dishId(shoppingCartDTO.getDishId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .setmealId(shoppingCartDTO.getSetmealId())
                .build();
        List<ShoppingCart> list=shoppingCartMapper.list(shoppingCart);
        if(list!=null&&list.size()>0){
            shoppingCart=list.get(0);
            if(shoppingCart.getNumber()==1){
                //remove this item
                shoppingCartMapper.remove(shoppingCart);
            }else{
                shoppingCart.setNumber(shoppingCart.getNumber()-1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }
    }
}

