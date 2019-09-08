package net.croz.qed.bank.transaction.controller;

import net.croz.qed.bank.transaction.exception.BalanceServiceNotAvailableException;
import net.croz.qed.bank.transaction.model.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionController {

    @ExceptionHandler(BalanceServiceNotAvailableException.class)
    public ResponseEntity<Response<Void>> handleBalanceServiceNotAvailableException(
        final BalanceServiceNotAvailableException exception) {
        return new ResponseEntity<>(
            Response.fail("It is not possible to carry out transaction right now. Please try again later."),
            HttpStatus.FAILED_DEPENDENCY);
    }

}
