package com.toiukha.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class ErrorsController implements ErrorController {

//	// 使用 SLF4J 進行日誌記錄
    private static final Logger logger = LoggerFactory.getLogger(ErrorsController.class);
    
    public String handleError(HttpServletRequest req) {
    	//取得錯誤狀態碼
    	Object status = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    	//取得例外物件
    	Throwable exception = (Throwable)req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    	
    	if(exception != null) 
    		logger.error("發生錯誤!!! 例外訊息：", exception.getMessage(), exception);
    	
    	if(status != null) {
    		Integer statusCode = Integer.valueOf(status.toString());
    		
    		if (statusCode >= 400 && statusCode < 500) {
                return "404error"; // 轉發到 4xx 通用錯誤頁面
            } else if (statusCode >= 500 && statusCode < 600) {
                return "500error"; // 轉發到 5xx 通用錯誤頁面
            }
        }
    	
        return "otherError"; // 轉發到通用錯誤頁面
    	
    }
    
    public String getErrorPath() {
        return null;
    }
	
}
