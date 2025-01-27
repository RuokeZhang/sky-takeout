package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额数据统计：{},{}",begin,end);
        return Result.success(reportService.getTurnoverReport(begin,end));
    }

    @GetMapping("/userStatistics")
    @ApiOperation("userStatistics")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                                               @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        return Result.success(reportService.getUserStatistics(begin, end));

    }

    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                                                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end){
        return Result.success(reportService.getOrderStatistics(begin, end));
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> too10Report(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end){
        return Result.success(reportService.getTop10(begin, end));
    }

    @GetMapping("/export")
    public Result export(HttpServletResponse httpServletResponse){
        reportService.exportBusinessData(httpServletResponse);
        return Result.success();
    }

}
