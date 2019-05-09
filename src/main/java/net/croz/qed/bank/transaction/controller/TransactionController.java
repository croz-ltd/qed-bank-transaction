package net.croz.qed.bank.transaction.controller;

import net.croz.qed.bank.transaction.exception.BalanceServiceNotAvailableException;
import net.croz.qed.bank.transaction.model.AddFund;
import net.croz.qed.bank.transaction.model.Balance;
import net.croz.qed.bank.transaction.model.ModifyBalanceRequest;
import net.croz.qed.bank.transaction.model.Response;
import net.croz.qed.bank.transaction.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class TransactionController {

    private final BalanceService balanceService;

    @Autowired
    public TransactionController(final BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @PostMapping("/transaction/add")
    public ResponseEntity<Response<String>> getBalance(@RequestBody final ModifyBalanceRequest modifyBalanceRequest) {
        // Check if account exists
        final Optional<Balance> balanceByIban = balanceService.getByIban(modifyBalanceRequest.getIban());
        if (balanceByIban.isPresent()) {
            // Account exists, perform balance addition
            return performModifyBalance(modifyBalanceRequest);
        }
        // Account not found, respond back to user
        return new ResponseEntity<>(Response.fail("Balance for account with IBAN: '" + modifyBalanceRequest.getIban() + "' currently not available."), HttpStatus.NOT_FOUND);
    }

    @PostMapping("/transaction/withdraw")
    public ResponseEntity<Response<String>> withdrawMoney(@RequestBody final ModifyBalanceRequest modifyBalanceRequest) {
        // Check if account exists
        final Optional<Balance> balanceByIban = balanceService.getByIban(modifyBalanceRequest.getIban());
        if (balanceByIban.isPresent()) {
            // Account exists, validate if user has enough balance to perform withdraw
            final Balance balance = balanceByIban.get();
            if (balance.getBalance().compareTo(modifyBalanceRequest.getAmount()) > 0) {
                // Account exists and user has enough balance, perform balance withdrawal
                modifyBalanceRequest.setAmount(modifyBalanceRequest.getAmount().negate());
                return performModifyBalance(modifyBalanceRequest);
            } else {
                // Account exists but user dont have enough balance to do this withdrawal
                return new ResponseEntity<>(Response.fail("Balance for account with IBAN: '" + modifyBalanceRequest.getIban() + "' has insufficient balance for this transaction."), HttpStatus.NOT_FOUND);
            }
        }
        // Account not found, respond back to user
        return new ResponseEntity<>(Response.fail("Balance for account with IBAN: '" + modifyBalanceRequest.getIban() + "' currently not available."), HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Response<String>> performModifyBalance(@RequestBody ModifyBalanceRequest modifyBalanceRequest) {
        final AddFund addFund = new AddFund(modifyBalanceRequest.getIban(), modifyBalanceRequest.getAmount());
        // Perform fund addition
        final Optional<Boolean> success = balanceService.addFund(addFund);
        if (success.isPresent()) {
            // We got some response, check of addition status
            if (success.get()) {
                // Fund addition succeed!
                return new ResponseEntity<>(Response.success("Balance modified successfully!"), HttpStatus.OK);
            }
            // Fund addition failed. This should never occurred :S
            return new ResponseEntity<>(Response.success("Balance not modified."), HttpStatus.BAD_REQUEST);
        }
        // We did not get response from balance service
        throw new BalanceServiceNotAvailableException();
    }
}
