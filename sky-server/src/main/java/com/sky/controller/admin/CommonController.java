package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/common")
@Api(tags="common interface")
@Slf4j
public class CommonController {
    @PostMapping("/upload")
    @ApiOperation(("upload file"))
    public Result<String> upload(MultipartFile file){
        log.info("upload file");
        return null;

    }

}
