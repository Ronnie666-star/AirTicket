package com.ronnie.airTicket.interfaces.common;

import com.ronnie.airTicket.domain.exception.AuthenticationException;
import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.ForbiddenException;
import com.ronnie.airTicket.domain.exception.ResourceNotFoundException;
import com.ronnie.airTicket.domain.exception.UsernameTakenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：把异常翻译成统一 HTTP 响应。
 *   业务规则违规（DomainException）        -> 400
 *   登录认证失败（AuthenticationException）-> 401
 *   资源不存在（ResourceNotFoundException）-> 404
 *   唯一约束冲突（DuplicateKeyException）  -> 409
 *   参数校验异常（MethodArgumentNotValid） -> 400
 *   其余兜底                               -> 500
 * 异常处理是接口层的职责，domain 里没有任何 HTTP/Spring 概念。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleDomain(DomainException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    /** 登录认证失败（用户不存在 / 密码错误 / 账号禁用 / 尝试次数过多）统一 401 */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(AuthenticationException e) {
        return ApiResponse.error(401, e.getMessage());
    }

    /** 无权限：角色不匹配 / 操作他人订单 -> 403 */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleForbidden(ForbiddenException e) {
        return ApiResponse.error(403, e.getMessage());
    }

    /** 资源不存在：改/删一个不存在的航班、订单 -> 404，而不是 400 */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .findFirst()
                .orElse("参数错误");
        return ApiResponse.error(400, msg);
    }

    /** 唯一约束冲突：重复提交（同航班号同出发时间、订单号撞号等）-> 409 Conflict */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDuplicateKey(DuplicateKeyException e) {
        return ApiResponse.error(409, "数据已存在，请勿重复提交");
    }

    /** 用户名已占用（注册预判）-> 409，文案比通用唯一冲突更明确 */
    @ExceptionHandler(UsernameTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleUsernameTaken(UsernameTakenException e) {
        return ApiResponse.error(409, e.getMessage());
    }

    /** 路径不存在：Spring 6.1 对未映射路径抛 NoResourceFoundException，这里转成 404 而不是 500 */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(NoResourceFoundException e) {
        return ApiResponse.error(404, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleOther(Exception e) {
        log.error("未处理异常", e);
        return ApiResponse.error(500, "服务器开小差了");
    }
}
