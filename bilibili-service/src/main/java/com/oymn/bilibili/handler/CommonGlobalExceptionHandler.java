package com.oymn.bilibili.handler;

import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)   //优先级是最高的
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResponse<String>  commonExceptionHandler(HttpServletRequest request, Exception e){
        String errorMsg = e.getMessage();
        if(e instanceof ConditionException){
            String errorCode = ((ConditionException) e).getCode();
            return new JsonResponse<>(errorCode, errorMsg);
        }else{
            //普通异常
            return new JsonResponse<>("500",errorMsg);
        }
    }
}
