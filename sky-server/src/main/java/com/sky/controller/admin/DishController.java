package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags="菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("create dish")
    public  Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品");
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("find dishes by page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("find dishes by page");
        PageResult pageResult=dishService.query(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    //MVC解析 string,把ID放进list里面
    @DeleteMapping
    @ApiOperation("delete dishes")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("delete dishes");
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("get dish by id")
    public Result<DishVO> getById(@PathVariable Long id){
         log.info("get dish by id: {}", id);
         DishVO dishVo=dishService.getByIdWithFlavor(id);
         return Result.success(dishVo);
    }

    @PutMapping
    @ApiOperation("modify dish with flavor")
    public Result update(@RequestBody DishDTO dishDTO) {
    log.info("modify dish with flavor");
    dishService.updateWithFlavor(dishDTO);
    return Result.success();
    }
}
