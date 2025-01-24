package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

@Autowired
private OrderMapper orderMapper;

    //每分钟触发一次
    @Scheduled(cron="0 * * ? * * ")
    public void processTimeoutOrder(){
        log.info("timeout order handling:{}", LocalDateTime.now());

        //select * from orders where status= ? and order_time< (current time-15 mins)
       List<Orders> ordersList= orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(ordersList!=null && ordersList.size()>0){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("timeout!");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    //每天凌晨一点check
    @Scheduled(cron="0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("delivery order handling:{}", LocalDateTime.now());
        List<Orders> ordersList=orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        if(ordersList!=null && !ordersList.isEmpty()){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
