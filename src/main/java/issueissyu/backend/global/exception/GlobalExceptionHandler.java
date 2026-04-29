package issueissyu.backend.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.global.api.ApiResponse;
import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.api.code.ReasonDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// 전역 예외 처리
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    //ConstraintViolationException
    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("ConstraintViolationException 처리 중 에러 발생");
        return handleExceptionInternalConstraint(e, GeneralErrorCode.BAD_REQUEST, HttpHeaders.EMPTY, request);
    }

    //GeneralException
    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> onThrowException(GeneralException generalException,
                                                   HttpServletRequest request) {
        return handleExceptionInternal(generalException, generalException.getCode(), null, request);
    }

    //LocationException
    @ExceptionHandler(value = LocationException.class)
    public ResponseEntity<Object> onThrowLocationException(LocationException locationException,
                                                           HttpServletRequest request) {
        return handleExceptionInternal(locationException, locationException.getCode(), null, request);
    }

    // MethodArgumentNotValidException
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
            errors.put(fieldName, errorMessage);
        });
        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, GeneralErrorCode.BAD_REQUEST, request, errors);
    }

    // Exception
    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();
        return handleExceptionInternalFalse(e, GeneralErrorCode.INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY,
                GeneralErrorCode.INTERNAL_SERVER_ERROR.getReason().getHttpStatus(), request, e.getMessage());
    }

    // 공통 에러 응답
    private ResponseEntity<Object> handleExceptionInternal(Exception e, BaseErrorCode code,
                                                           HttpHeaders headers, HttpServletRequest request) {
        ReasonDTO reason = code.getReason();
        ApiResponse<Void> body = ApiResponse.onFailure(code);
        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(e, body, headers, reason.getHttpStatus(), webRequest);
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, BaseErrorCode errorCode,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request,
                                                                String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCode, errorPoint);
        return super.handleExceptionInternal(e, body, headers, status, request);
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers,
                                                               BaseErrorCode errorCode, WebRequest request,
                                                               Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCode, errorArgs);
        return super.handleExceptionInternal(e, body, headers, errorCode.getReason().getHttpStatus(), request);
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, BaseErrorCode errorCode,
                                                                     HttpHeaders headers, WebRequest request) {
        ApiResponse<Void> body = ApiResponse.onFailure(errorCode);
        return super.handleExceptionInternal(e, body, headers, errorCode.getReason().getHttpStatus(), request);
    }
}
