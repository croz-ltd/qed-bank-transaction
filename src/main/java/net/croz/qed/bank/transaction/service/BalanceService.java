package net.croz.qed.bank.transaction.service;

import net.croz.qed.bank.transaction.model.AddFund;
import net.croz.qed.bank.transaction.model.Balance;

import java.util.Optional;

public interface BalanceService {

    Optional<Balance> getByIban(final String iban);

    Optional<Boolean> addFund(final AddFund addFund);

}
