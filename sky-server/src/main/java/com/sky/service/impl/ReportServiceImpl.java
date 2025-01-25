package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    public TurnoverReportVO getTurnoverReport(LocalDate beginTime, LocalDate endTime){
log.info("beginTime:{},endTime:{}", beginTime, endTime);
        //concat a dateList String
        List<LocalDate> dateTimeList=new ArrayList<>();
        while(!beginTime.equals(endTime)){
            dateTimeList.add(beginTime);
            beginTime = beginTime.plusDays(1);
        }
        String dateString= StringUtils.join(dateTimeList, ",");

        //get turnover from the database
        List<Double> turnoverList=new ArrayList<>();
        for(LocalDate date:dateTimeList){
            //turnover: the sum of all the COMPELETED orders' amounut
            LocalDateTime dateBeginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dateEndTime=LocalDateTime.of(date, LocalTime.MAX);

            //select sum(amount) from orders where order_time > dateBeginTime and order_time < dateEndTime and status=5
            Map map=new HashMap();
            map.put("begin",dateBeginTime);
            map.put("end",dateEndTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover=turnover==null?0.0 :turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder().dateList(dateString).turnoverList(StringUtils.join(turnoverList, ",")).build();
    }
}
