package com.example.transaction.service;

import com.example.transaction.repo.SpringJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceRequired {
    private final SpringJdbcRepository repo;
    // 기본 전파: REQUIRED (생략해도 동일)
    @Transactional(propagation = Propagation.REQUIRED)
    public void writeLog(String msg, boolean fail) {
        repo.insertAuditLogTx("[REQUIRED] " + msg);
        if (fail) throw new RuntimeException("REQUIRED 내부 로그 쓰기 실패");
    }
}
