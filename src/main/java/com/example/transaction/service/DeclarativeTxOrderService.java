package com.example.transaction.service;

import com.example.transaction.repo.SpringJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//선언형

@Service
@RequiredArgsConstructor
public class DeclarativeTxOrderService {
    private final SpringJdbcRepository repo;
    @Transactional
    public void placeOrderWithAnno(long productId, int qty, boolean fail) {
        try {
            int stock = repo.selectStockForUpdateTx(productId);
            if (stock < qty) throw new IllegalStateException("재고 부족: stock=" + stock);

            repo.updateStockTx(productId, stock - qty);
            repo.insertOrderTx(productId, qty, "CREATED");

            if (fail) throw new RuntimeException("강제 실패");
        } finally {
            // @Transactional이 커밋/롤백/연결정리를 해주므로 보통 release 불필요
            // repo.releaseTxConnection(); // 일반적으로 호출하지 않음
        }
    }
}
