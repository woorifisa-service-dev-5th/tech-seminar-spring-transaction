package com.example.transaction.service;

import com.example.transaction.repo.SpringJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
// 추상화

@Service
@RequiredArgsConstructor
public class TxManagerOrderService {
    private final PlatformTransactionManager txManager;
    private final SpringJdbcRepository repo;
    public void placeOrderTxManager(long productId, int qty, boolean fail) {
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int stock = repo.selectStockForUpdateTx(productId);
            if (stock < qty) throw new IllegalStateException("재고 부족: stock=" + stock);

            repo.updateStockTx(productId, stock - qty);
            repo.insertOrderTx(productId, qty, "CREATED");

            if (fail) throw new RuntimeException("강제 실패");

            txManager.commit(status);
        } catch (Exception e) {
            txManager.rollback(status);
            throw e;
        } finally {
            repo.releaseTxConnection();
        }
    }
}
