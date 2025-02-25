package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Page<Orders> queryPage(OrdersPageQueryDTO ordersPageQueryDTO);


    @Select("select count(id) from orders where status=#{status} ")
    Integer countStatus(Integer status);

    @Select("select * from orders where status= #{status} and order_time< #{time}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime time);

    Double sumByMap(Map map);

    Integer getOrderNum(Map map);

    List<GoodsSalesDTO> getTop10Dishes(LocalDateTime begin, LocalDateTime end);
    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
}
