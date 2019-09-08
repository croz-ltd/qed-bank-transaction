package net.croz.qed.bank.transaction.service.impl;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import net.croz.qed.bank.transaction.config.ServiceUrlProperties;
import net.croz.qed.bank.transaction.exception.BalanceServiceNotAvailableException;
import net.croz.qed.bank.transaction.model.AddFund;
import net.croz.qed.bank.transaction.model.Balance;
import net.croz.qed.bank.transaction.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class BalanceServiceImpl implements BalanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceServiceImpl.class);

    private final RestTemplate         restTemplate;
    private final ServiceUrlProperties serviceUrlProperties;

    @Autowired
    public BalanceServiceImpl(final RestTemplate restTemplate, final ServiceUrlProperties serviceUrlProperties) {
        this.restTemplate = restTemplate;
        this.serviceUrlProperties = serviceUrlProperties;
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallbackGetByIban")
    public Optional<Balance> getByIban(final String iban) {
        LOGGER.info("Calling balance service to get balance...");

        final ResponseEntity<Optional<Balance>> response = restTemplate.exchange( //
            serviceUrlProperties.getBalance() + "/balance/iban/" + iban, //
            HttpMethod.GET, //
            null, //
            new ParameterizedTypeReference<Optional<Balance>>() { //
            } //
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("Balance service is alive and responded with OK.");
            return response.getBody();
        }

        LOGGER.info("Balance service is alive but responded with " + response.getStatusCode().name() + ".");
        return Optional.empty();
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallbackAddFund")
    public Optional<Boolean> addFund(final AddFund addFund) {
        LOGGER.info("Calling balance service to add fund...");

        final ResponseEntity<Void> response = restTemplate.postForEntity(
            serviceUrlProperties.getBalance() + "/balance/modify", //
            addFund, //
            Void.class //
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("Balance service is alive and responded with OK.");
            return Optional.of(true);
        }

        LOGGER.info("Balance service is alive but responded with " + response.getStatusCode().name() + ".");
        return Optional.of(false);
    }

    public Optional<Balance> fallbackGetByIban(final String iban) {
        LOGGER.warn("Circuit breaker fallback method invoked. Calling to balance service failed!");
        throw new BalanceServiceNotAvailableException();
    }

    public Optional<Boolean> fallbackAddFund(final AddFund addFund) {
        LOGGER.warn("Circuit breaker fallback method invoked. Calling to balance service failed!");
        throw new BalanceServiceNotAvailableException();
    }

}
