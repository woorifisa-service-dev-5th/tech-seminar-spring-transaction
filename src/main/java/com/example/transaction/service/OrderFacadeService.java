// src/main/java/com/example/transaction/service/OrderFacadeService.java
package com.example.transaction.service;

import com.example.transaction.repo.SpringJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderFacadeService {

    private final SpringJdbcRepository repo;
    private final AuditLogServiceRequired logRequired;
    private final AuditLogServiceRequiresNew logRequiresNew;

    /**
     * REQUIRED 체인:
     * - 내부에서 예외 발생 → 외부 트랜잭션이 rollback-only로 마킹됨
     * - 외부에서 try-catch로 예외를 삼켰어도, 마지막 커밋 시점에 전역 롤백 발생(UnexpectedRollback)
     */
    @Transactional
    public void orderWithRequired(long productId, int qty, boolean failLog) {
        repo.insertOrderTx(productId, qty, "CREATED");
        try {
            logRequired.writeLog("주문 생성됨(id=auto)", failLog);
        } catch (Exception e) {
        }
    }

    /**
     * REQUIRES_NEW 체인:
     * - 내부에서 예외 발생 → 내부 트랜잭션만 롤백
     * - 외부 트랜잭션은 정상 커밋(주문 성공, 로그만 빠짐)
     */
    @Transactional
    public void orderWithRequiresNew(long productId, int qty, boolean failLog) {
        repo.insertOrderTx(productId, qty, "CREATED");

        try {
            logRequiresNew.writeLog("주문 생성됨(id=auto)", failLog);
        } catch (Exception e) {
            // 내부 트랜잭션만 롤백되며, 외부는 영향을 받지 않음
            // 즉, 주문은 성공/커밋되고 로그만 누락
        }
    }
}
