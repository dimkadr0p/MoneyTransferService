package com.khachidze.moneytransferservice.repository;


import com.khachidze.moneytransferservice.entity.MoneyTransfersEntity;
import com.khachidze.moneytransferservice.util.EntityManagerProvider;
import com.khachidze.moneytransferservice.util.TransactionalOperation;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@ApplicationScoped
public class MoneyTransfersRepository {

    public List<MoneyTransfersEntity> findAll() {
        return executeInTransaction(entityManager -> {
            Query query = entityManager.createQuery("SELECT u FROM MoneyTransfersEntity u", MoneyTransfersEntity.class);
            return query.getResultList();
        });
    }

    public MoneyTransfersEntity findById(Long id) {
        return executeInTransaction(entityManager -> entityManager.find(MoneyTransfersEntity.class, id));
    }

    public void save(MoneyTransfersEntity moneyTransfersEntity) {
        executeInTransaction(entityManager -> {
            entityManager.persist(moneyTransfersEntity);
            return null;
        });
    }

    public void update(MoneyTransfersEntity moneyTransfersEntity) {
        executeInTransaction(entityManager -> {
            entityManager.merge(moneyTransfersEntity);
            return null;
        });
    }

    public void delete(MoneyTransfersEntity moneyTransfersEntity) {
        executeInTransaction(entityManager -> {
            entityManager.remove(moneyTransfersEntity);
            return null;
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