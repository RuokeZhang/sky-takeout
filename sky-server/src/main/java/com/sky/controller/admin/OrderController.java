package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    Result<PageResult>  conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult page=orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(page);
    }

    @GetMapping("/statistics")
    Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO res=orderService.statistics();
        return Result.success(res);
    }

    @GetMapping("details/{id}")
    Result<OrderVO> details(@PathVariable Long id) {
        OrderVO orderVO=orderService.getOrderById(id);
        return Result.success(orderVO);
    }
    @PutMapping("confirm")
    Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }
    @PutMapping("rejection")
    Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {

        orderService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("/cancel")
    Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    Result delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }

}
