package com.khachidze.moneytransferservice.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

@FunctionalInterface
public interface TransactionalOperation<T> {
    T execute(EntityManager entityManager);

    default T executeTransactionally(EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            T result = execute(entityManager);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}