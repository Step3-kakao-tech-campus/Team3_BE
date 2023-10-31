package com.bungaebowling.server._core.errors;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Arrays;

@Aspect
@Component
public class GlobalValidationHandler {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {
    }

    @Before("postMapping() || putMapping()")
    public void validationAdvice(JoinPoint jp) {
        Object[] args = jp.getArgs();

        Arrays.stream(args)
                .filter(arg -> arg instanceof Errors)
                .forEach(arg -> {
                    Errors errors = (Errors) arg;

                    if (errors.hasErrors()) {
                        var error = errors.getFieldErrors().get(0);
                        throw new CustomException(ErrorCode.INVALID_RUQUEST_DATA,
                                error.getDefaultMessage() + ":" + error.getField()
                        );
                    }
                });
    }
}
