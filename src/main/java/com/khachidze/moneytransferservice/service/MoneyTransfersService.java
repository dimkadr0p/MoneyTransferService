package com.khachidze.moneytransferservice.service;


import com.khachidze.moneytransferservice.dto.*;
import com.khachidze.moneytransferservice.entity.MoneyTransfersEntity;
import com.khachidze.moneytransferservice.enums.TransferStatus;
import com.khachidze.moneytransferservice.exception.CommissionOperationException;
import com.khachidze.moneytransferservice.exception.InsufficientMoneyException;
import com.khachidze.moneytransferservice.exception.TransferMoneyFailedException;
import com.khachidze.moneytransferservice.exception.UserNotFoundException;
import com.khachidze.moneytransferservice.repository.MoneyTransfersRepository;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MoneyTransfersService {
    @Inject
    private MoneyTransfersRepository moneyTransfersRepository;

    public List<MoneyTransfersEntity> getAllMoneyTransfers() {
        return moneyTransfersRepository.findAll();
    }
    public List<MoneyTransfersEntity> getHistoryTransaction(String phoneNumber) {
        return moneyTransfersRepository.findHistoryTransactionByPhoneNumber(phoneNumber);
    }

    public void transferMoney(TransferMoneyRequestDto transferMoneyRequestDto) {

        UserDto userSender = getUserByPhoneNumber(transferMoneyRequestDto.getSenderPhone())
                .orElseThrow(() -> new UserNotFoundException("Sender phone number does not exist"));
        UserDto userReceiver = getUserByPhoneNumber(transferMoneyRequestDto.getReceiverPhone())
                .orElseThrow(() -> new UserNotFoundException("Recipient phone number does not exist"));
        BigDecimal totalAmountTransfer = getAmountIncludingCommission(transferMoneyRequestDto.getAmount())
                .orElseThrow(() -> new CommissionOperationException("The amount is not used to calculate the commission"));

        if (!checkSolvency(userSender, totalAmountTransfer))
            throw new InsufficientMoneyException("Insufficient funds");

        MoneyTransfersEntity moneyTransfersEntity = MoneyTransfersEntity
                .builder()
                .amount(totalAmountTransfer)
                .senderPhone(transferMoneyRequestDto.getSenderPhone())
                .receiverPhone(transferMoneyRequestDto.getReceiverPhone())
                .date(new Date()).build();

        setFinalBalance(userSender, userReceiver, totalAmountTransfer);

        boolean resultOperationTransferMoney = updateBalanceUsers(userSender, userReceiver);

        moneyTransfersEntity.setStatus(resultOperationTransferMoney ? TransferStatus.SUCCESSFULLY : TransferStatus.REJECTED);

        moneyTransfersRepository.save(moneyTransfersEntity);

        if (!resultOperationTransferMoney) {
            throw new TransferMoneyFailedException("Transaction failed");
        }
    }

    private boolean checkSolvency(UserDto userDto, BigDecimal amount) {
        return userDto.getBalance().compareTo(amount) >= 0;
    }

    private void setFinalBalance(UserDto userSender, UserDto userReceiver, BigDecimal totalAmountTransfer) {
        userSender.setBalance(userSender.getBalance().subtract(totalAmountTransfer));
        userReceiver.setBalance(userReceiver.getBalance().add(totalAmountTransfer));
    }

    private boolean updateBalanceUsers(UserDto userSender, UserDto userReceiver) {
        String url = "http://user-system-tomcat:8080/UserService/api/users/balance";

        RestTemplate restTemplate = new RestTemplate();

        ResultMoneyTransferDto resultMoneyTransferDto = new ResultMoneyTransferDto(userSender, userReceiver);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ResultMoneyTransferDto> requestUpdate = new HttpEntity<>(resultMoneyTransferDto, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestUpdate, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException ex) {
            return false;
        }
    }


    private Optional<BigDecimal> getAmountIncludingCommission(BigDecimal amount) {
        String url = "http://commission-system-tomcat:8080/CommissionService/api/commission?amount=" + amount;

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<AmountDto> response = restTemplate.getForEntity(url, AmountDto.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.getBody().getTotalAmount());
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        }
    }

    private Optional<UserDto> getUserByPhoneNumber(String phoneNumber) {
        String url = "http://user-system-tomcat:8080/UserService/api/users?phone=" + phoneNumber;

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        }
    }

}
