// src/main/java/com/example/transaction/service/AuditLogServiceRequiresNew.java
package com.example.transaction.service;

import com.example.transaction.repo.SpringJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceRequiresNew {
    private final SpringJdbcRepository repo;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeLog(String msg, boolean fail) {
        repo.insertAuditLogTx("[REQUIRES_NEW] " + msg);
        if (fail) throw new RuntimeException("REQUIRES_NEW 내부 로그 쓰기 실패");
    }
}
