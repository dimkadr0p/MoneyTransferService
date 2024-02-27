package com.khachidze.moneytransferservice.repository;


import com.khachidze.moneytransferservice.entity.MoneyTransfersEntity;
import com.khachidze.moneytransferservice.util.EntityManagerProvider;
import com.khachidze.moneytransferservice.util.TransactionalOperation;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MoneyTransfersRepository {

    private final Map<String, List<MoneyTransfersEntity>> historyTransactionCache = new ConcurrentHashMap<>();

    public List<MoneyTransfersEntity> findHistoryTransactionByPhoneNumber(String phoneNumber) {

        List<MoneyTransfersEntity> cachedResults = historyTransactionCache.get(phoneNumber);

        if (cachedResults != null) {
            return cachedResults;
        }

        return executeInTransaction(entityManager -> {
            Query query = entityManager.createQuery("SELECT u FROM MoneyTransfersEntity u WHERE u.senderPhone = :phone OR u.receiverPhone = :phone", MoneyTransfersEntity.class);
            query.setParameter("phone", phoneNumber);
            List<MoneyTransfersEntity> transactions = query.getResultList();
            historyTransactionCache.put(phoneNumber, transactions);
            return transactions;
        });
    }

    public void save(MoneyTransfersEntity moneyTransfersEntity) {
        executeInTransaction(entityManager -> {
            entityManager.persist(moneyTransfersEntity);
            List<MoneyTransfersEntity> transactions = historyTransactionCache.getOrDefault(moneyTransfersEntity.getSenderPhone(), new ArrayList<>());
            transactions.add(moneyTransfersEntity);
            historyTransactionCache.put(moneyTransfersEntity.getSenderPhone(), transactions);
            return null;
        });
    }

    public List<MoneyTransfersEntity> findAll() {
        return executeInTransaction(entityManager -> {
            Query query = entityManager.createQuery("SELECT u FROM MoneyTransfersEntity u", MoneyTransfersEntity.class);
            return query.getResultList();
        });
    }

    private <T> T executeInTransaction(TransactionalOperation<T> operation) {
        EntityManager entityManager = EntityManagerProvider.createEntityManager();
        try {
            return operation.executeTransactionally(entityManager);
        } finally {
            entityManager.close();
        }
    }

}