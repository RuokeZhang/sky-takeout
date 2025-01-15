package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/setmeal")
@Api(tags="套餐相关接口")
@Slf4j
public class SetMealController {

    @Autowired
    SetmealService setmealService;

    @PostMapping
    @ApiOperation("create a setmeal")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("create a setmeal");
        setmealService.save(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("find setmeal by page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("find setmeal by page");
        PageResult pageResult=setmealService.query(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("show setmeal by id")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id){
        log.info("show setmeal by id");
        SetmealVO setmealVO=setmealService.getByIdWithDishes(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("modify setmeal information")
    public Result modify(@RequestBody SetmealDTO setmealDTO){
        log.info("modify setmeal information");
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("delete setmeals by batch")
    public Result delete(@RequestParam Long[] ids){
        log.info("delete setmeals by batch");
        setmealService.batchDelete(ids);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("change setmeal status")
    public Result changeSetmealStatus(@PathVariable Integer status, Long id){
        log.info("change setmeal status");
        setmealService.changeStatus(status, id);
        return Result.success();
    }

}
