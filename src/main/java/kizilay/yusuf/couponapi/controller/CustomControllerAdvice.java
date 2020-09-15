package kizilay.yusuf.couponapi.controller;

import kizilay.yusuf.couponapi.execption.BaseException;
import kizilay.yusuf.couponapi.model.Response;
import kizilay.yusuf.couponapi.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class CustomControllerAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Response> handleCustomException(BaseException ex, WebRequest request) {
        return ResponseUtil.errorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGenericException(Exception ex, WebRequest request) {
        //todo:log
        ex.printStackTrace();
        return ResponseUtil.errorResponse("Error occurred!", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
