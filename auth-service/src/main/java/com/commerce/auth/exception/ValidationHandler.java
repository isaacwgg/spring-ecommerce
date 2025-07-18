//package com.commerce.auth.exception;
//
//import com.tecial.product_service.constants.StatusCodes;
//import com.tecial.product_service.response.UniversalResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//@ControllerAdvice
//@Slf4j
//public class ValidationHandler extends ResponseEntityExceptionHandler {
//
//
//    @ExceptionHandler(ObjectNotFoundException.class)
//    protected ResponseEntity<Object> handleNoObjectDataFoundException(ObjectNotFoundException e) {
//        UniversalResponse universalResponse = UniversalResponse.builder()
//                .status(StatusCodes.CONFLICT)
//                .message(e.getMessage())
//                .data(new HashMap<>())
//                .build();
//        log.info("No object record found", e);
//        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(universalResponse);
//    }
//
//    @ExceptionHandler(ListNotFoundException.class)
//    protected ResponseEntity<Object> handleListNotFoundExceptionException(ListNotFoundException e) {
//        UniversalResponse universalResponse = UniversalResponse.builder()
//                .status(StatusCodes.CONFLICT)
//                .message(e.getMessage())
//                .data(new ArrayList<>())
//                .build();
//        log.info("No List record found", e);
//        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(universalResponse);
//    }
//
//
//}
