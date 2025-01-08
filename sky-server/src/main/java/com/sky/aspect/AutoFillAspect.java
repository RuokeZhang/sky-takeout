package com.sky.aspect;

import com.sky.annotation.Autofill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Slf4j
@Component
public class AutoFillAspect {
    //拦截mapper包下面所有带有@Autofill注解的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) &&  @annotation(com.sky.annotation.Autofill))")
    public void autoFillPointCut()
    {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充");
        //获取数据库操作类型
        MethodSignature signature=(MethodSignature) joinPoint.getSignature();
        Autofill autofill=signature.getMethod().getAnnotation(Autofill.class);
        OperationType operationType=autofill.value();

        //获得操作的 Entity
        Object[] args = joinPoint.getArgs();
        if(args==null||args.length==0){
            return;
        }
        Object entity=args[0];

        //准备赋值的数据（时间和当前用户的 Id）
        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        //根据不同的操作类型，通过反射来为不同的属性赋值
        if(operationType==OperationType.INSERT){
            Method setCreateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setCreateTime.invoke(entity, now);
            setCreateUser.invoke(entity, currentId);
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        }else if(operationType==OperationType.UPDATE){
            Method setUpdateTime=entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
            Method setUpdateUser=entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
            Method setCreateTime=entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
            Method setCreateUser=entity.getClass().getDeclaredMethod("setCreateUser", Long.class);

        }
    }

}
