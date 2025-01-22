package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //handle exceptions: address/shopping cart is null
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userID = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userID);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        //insert one row into the order table
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(addressBook.getCityName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userID);
        orders.setPhone(addressBook.getPhone());

        orderMapper.insert(orders);


        //insert n rows into the orderDetail table, batch insert
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCartItem : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCartItem, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.batchInsert(orderDetailList);

        //empty the user's shopping cart
        shoppingCartMapper.deleteByUserId(userID);

        OrderSubmitVO submitVO = OrderSubmitVO.builder().id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
        return submitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        //JSONObject jsonObject = WeChatPayUtil.pay(
        // ordersPaymentDTO.getOrderNumber(), //商户订单号
        //  new BigDecimal(0.01), //支付金额，单位 元
        //  "苍穹外卖订单", //商品描述
        //   user.getOpenid() //微信用户的openid
        //);

        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        System.out.println("ordersDB:" + ordersDB);
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public OrderVO getOrderById(Long id) {
        OrderVO orderVO = new OrderVO();

        //get 订单菜品信息 orderDishes
        Orders orders=orderMapper.getById(id);
        if(orders==null){
            log.error("orders is null");
        }
        //TODO: 配送地址为 null
        BeanUtils.copyProperties(orders, orderVO);

       //get order details
        List<OrderDetail> orderDetailList=orderDetailMapper.getListByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;

    }
/**    private int page;

 private int pageSize;

 private String number;

 private  String phone;

 private Integer status;

 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
 private LocalDateTime beginTime;

 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
 private LocalDateTime endTime;

 private Long userId;**/
    public PageResult query(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);

        Page<Orders> pageRes=orderMapper.queryPage(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        //return order details as well
        if(pageRes!=null&&pageRes.getTotal()>0){
            for(Orders orders:pageRes){
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailMapper.getListByOrderId(orders.getId()));
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(pageRes.getTotal(), orderVOList);

    }

    /**
     * - 待支付和待接单状态下，用户可直接取消订单
     * - 商家已接单状态下，用户取消订单需电话沟通商家
     * - 派送中状态下，用户取消订单需电话沟通商家
     * - 如果在待接单状态下取消订单，需要给用户退款
     * - 取消订单后需要将订单状态修改为“已取消”
     * */
    public void cancelOrder(Long id) throws Exception {
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //支付状态 0未支付 1已支付 2退款
        Orders orders=orderMapper.getById(id);
        if(orders==null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        if(orders.getStatus()==Orders.TO_BE_CONFIRMED) {
            //refund
            /**weChatPayUtil.refund(
                    orders.getNumber(), //商户订单号
                    orders.getNumber(), //商户退款单号
                    new BigDecimal(String.valueOf(orders.getAmount())),//退款金额，单位 元
                    new BigDecimal(String.valueOf(orders.getAmount())));//原订单金额
            */
            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        //status =1 or 2, cancel
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    //再来一单就是将原订单中的商品重新加入到购物车中
    //TODO: optimize this function
    public void repetition(Long id) {
        //get the order
        Orders orders = orderMapper.getById(id);
        if(orders==null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //for every dish in that orders, create a shoppingCart and insert it into the table
        for(OrderDetail orderDetail:orderDetailMapper.getListByOrderId(id)){
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(orders.getUserId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }
}
