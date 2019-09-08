package net.croz.qed.bank.transaction.model;

import java.math.BigDecimal;

public class AddFund {

    private String     iban;
    private BigDecimal fund;

    public AddFund() {
    }

    public AddFund(final String iban, final BigDecimal fund) {
        this.iban = iban;
        this.fund = fund;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public BigDecimal getFund() {
        return fund;
    }

    public void setFund(BigDecimal fund) {
        this.fund = fund;
    }

}
