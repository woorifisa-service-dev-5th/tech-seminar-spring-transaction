package com.example.transaction.service;

import com.example.transaction.repo.PlainJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NonTxOrderService {

    private final PlainJdbcRepository repo;

    /**
     * 트랜잭션 없이 진행 (자동 커밋)
     * 동시성 환경에서 재고가 음수가 될 수 있음 (race condition)
     */
    public void placeOrderNoTx(long productId, int qty) {
        int stock = repo.findStockByProductId(productId);
        try { Thread.sleep(1500); } catch (InterruptedException ignore) {}
        if (stock < qty) throw new IllegalStateException("재고 부족: stock=" + stock);

        repo.decreaseStock(productId, qty);
        repo.insertOrder(productId, qty, "CREATED");
    }
}
