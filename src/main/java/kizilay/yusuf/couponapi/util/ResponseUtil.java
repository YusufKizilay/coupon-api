package kizilay.yusuf.couponapi.util;

import kizilay.yusuf.couponapi.model.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> ResponseEntity<Response> successResponse(T value, HttpStatus httpStatus) {
        return new ResponseEntity<>(new Response(value),httpStatus);
    }


    public static ResponseEntity<Response> errorResponse(String msg, HttpStatus httpStatus) {
        return new ResponseEntity<>(new Response(msg),httpStatus);
    }
}
