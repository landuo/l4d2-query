package io.github.landuo.l4d2.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author accidia
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceException extends RuntimeException {
    private Integer code;

    private String msg;

    private Throwable cause;

    public ServiceException(String message) {
        super(message);
        this.msg = message;
        this.code = 500;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.msg = message;
        this.code = 500;
        this.cause = cause;
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }
}
