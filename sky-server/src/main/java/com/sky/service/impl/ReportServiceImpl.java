package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        log.info("begin:{},end:{}", begin, end);
        List<LocalDate> dateList=new ArrayList<>();
        LocalDateTime beginTime=begin.atStartOfDay();
        Integer userTotal=userMapper.getUserNumBefore(beginTime);

        while(!begin.equals(end)){
            dateList.add(begin);
            begin=begin.plusDays(1);
        }
        String dateString= StringUtils.join(dateList, ",");


        List<Integer> userTotalList=new ArrayList<>();

        List<Integer> newUserList=new ArrayList<>();
        for(LocalDate date:dateList){
            LocalDateTime dateBeginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dateEndTime=LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap();
            map.put("begin",dateBeginTime);
            map.put("end",dateEndTime);
            Integer numNewUser =userMapper.getNewUserNum(map);
            numNewUser=numNewUser==null?0:numNewUser;
            userTotal+=numNewUser;
            userTotalList.add(userTotal);
            newUserList.add(numNewUser);
        }
        String newUserString= StringUtils.join(newUserList, ",");
        String userTotalString= StringUtils.join(userTotalList, ",");

        return UserReportVO.builder().dateList(dateString).newUserList(newUserString).totalUserList(userTotalString).build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        log.info("begin:{},end:{}", begin, end);
        //String dateString;
        List<LocalDate> dateList=new ArrayList<>();
        while(!begin.equals(end)){
            dateList.add(begin);
            begin=begin.plusDays(1);
        }
        String dateString= StringUtils.join(dateList, ",");

        //每日订单数，以逗号分隔，例如：260,210,215
        List<Integer> orderList=new ArrayList<>();
        List<Integer> validOrderList=new ArrayList<>();


        for(LocalDate date:dateList){
            LocalDateTime dateBeginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dateEndTime=LocalDateTime.of(date, LocalTime.MAX);
            //order
            Map map=new HashMap();
            map.put("begin",dateBeginTime);
            map.put("end",dateEndTime);
            map.put("status", null);
            orderList.add(orderMapper.getOrderNum(map));
            //valid order
            map.put("status", Orders.COMPLETED);
            validOrderList.add(orderMapper.getOrderNum(map));
        }

        String orderCountString= StringUtils.join(orderList, ",");
        String validOrderCountString= StringUtils.join(validOrderList, ",");

        Integer totalOrderCount =orderList.stream().reduce(Integer::sum).get();
        Integer validTotalOrderCount =validOrderList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate=validTotalOrderCount.doubleValue()/totalOrderCount;
        return OrderReportVO.builder()
                .validOrderCount(validTotalOrderCount)
                .totalOrderCount(totalOrderCount)
                .dateList(dateString)
                .orderCountList(orderCountString)
                .validOrderCountList(validOrderCountString)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        //get top 10 dishes
        LocalDateTime dateEndTime=LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> top10=orderMapper.getTop10Dishes(begin.atStartOfDay(), dateEndTime);
        //select od.name, sum(od.number) from order_detail od inner join orders o
        //on o.id=od.order_id
        //where o.status = 5
        //  AND o.order_time > '2022-10-01'
        //  AND o.order_time < '2022-10-01'
        //GROUP BY od.name
        //limit 0,10;
        List<String> names=top10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameString=StringUtils.join(names,",");
        List<Integer> numbers=top10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberString=StringUtils.join(numbers,",");
        return SalesTop10ReportVO.builder().nameList(nameString).numberList(numberString).build();

    }

    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        //查询最近 30天的营业数据
        LocalDate beginDate=LocalDate.now().minusDays(30);
        LocalDate endDate=LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO= workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        //基于模版文件，在内存里新建一个excel文件
        XSSFWorkbook excel = null;
        try {
            excel = new XSSFWorkbook(in);

            XSSFSheet sheet= excel.getSheet("Sheet1");
            //填充时间
            sheet.getRow(1).getCell(1).setCellValue("Time"+beginDate+"至"+endDate);
            //填充营业额
            XSSFRow row=sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = beginDate.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = httpServletResponse.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
