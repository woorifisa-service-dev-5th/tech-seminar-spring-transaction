package com.example.transaction.web;

import com.example.transaction.dto.OrderDto;
import com.example.transaction.dto.ProductDto;
import com.example.transaction.repo.PlainJdbcRepository;
import com.example.transaction.service.NonTxOrderService;
import com.example.transaction.service.OrderFacadeService;
import com.example.transaction.service.ProgrammaticTxOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/demo")
public class DemoController {

    private final PlainJdbcRepository repo;
    private final NonTxOrderService nonTx;
    private final ProgrammaticTxOrderService manualTx;
    private final OrderFacadeService facadeService;

    @Autowired
    private final DataSource dataSource;

    // 재고/주문 조회 (캡쳐 용이)
    @GetMapping("/products")
    public List<ProductDto> products() {
        return repo.findAllProducts();
    }

    @GetMapping("/orders")
    public List<OrderDto> orders() {
        return repo.findAllOrders();
    }

    // 미적용 주문
    @PostMapping("/order/no-tx")
    public ResponseEntity<?> orderNoTx(@RequestParam long productId, @RequestParam int qty) {
        try {
            nonTx.placeOrderNoTx(productId, qty);
            return ResponseEntity.ok(Map.of(
                    "message", "NO-TX 주문 완료",
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", e.getMessage(),
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders()
            ));
        }
    }

    // 프로그램 방식 트랜잭션 주문
    @PostMapping("/order/manual-tx")
    public ResponseEntity<?> orderManualTx(@RequestParam long productId,
                                           @RequestParam int qty,
                                           @RequestParam(defaultValue = "false") boolean fail) {
        try {
            manualTx.placeOrderWithManualTx(productId, qty, fail);
            return ResponseEntity.ok(Map.of(
                    "message", "MANUAL-TX 주문 완료",
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", e.getMessage(),
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders()
            ));
        }
    }

    // 빠른 리셋/시드 (재고 1개, 주문 비움)
    @PostMapping("/reset")
    public Map<String, Object> reset() {
        repo.deleteAllOrders();
        repo.deleteAllProducts();
        long pid = repo.insertProduct("영화 티켓", 1); // 재고 1로 세팅 (캡쳐 포인트)
        return Map.of(
                "message", "reset done",
                "productId", pid,
                "products", repo.findAllProducts(),
                "orders", repo.findAllOrders()
        );
    }

    // 재고 강제 설정 (데모 편의)
    @PostMapping("/stock")
    public Map<String, Object> setStock(@RequestParam long productId, @RequestParam int stock) {
        repo.setProductStock(productId, stock);
        return Map.of("products", repo.findAllProducts());
    }

    @PostMapping("/prop/required")
    public ResponseEntity<?> propRequired(@RequestParam long productId,
                                          @RequestParam int qty,
                                          @RequestParam(defaultValue = "false") boolean failLog) {
        try {
            facadeService.orderWithRequired(productId, qty, failLog);
            return ResponseEntity.ok(Map.of(
                    "message", "REQUIRED 체인: 완료(그럴 리 없음?)",
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders(),
                    "logs", findAllLogs()
            ));
        } catch (UnexpectedRollbackException ure) {
            // 스프링이 커밋 시점에 던지는 전형적인 예외
            return ResponseEntity.status(409).body(Map.of(
                    "error", "UnexpectedRollback: 내부 REQUIRED 실패로 전체 롤백됨",
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders(),
                    "logs", findAllLogs()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", e.getMessage(),
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders(),
                    "logs", findAllLogs()
            ));
        }
    }

    @PostMapping("/prop/requires-new")
    public ResponseEntity<?> propRequiresNew(@RequestParam long productId,
                                             @RequestParam int qty,
                                             @RequestParam(defaultValue = "false") boolean failLog) {
        try {
            facadeService.orderWithRequiresNew(productId, qty, failLog);
            return ResponseEntity.ok(Map.of(
                    "message", "REQUIRES_NEW 체인: 외부 커밋 OK",
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders(),
                    "logs", findAllLogs()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", e.getMessage(),
                    "products", repo.findAllProducts(),
                    "orders", repo.findAllOrders(),
                    "logs", findAllLogs()
            ));
        }
    }

    // 로그 조회 헬퍼 (간단 버전)
    private Object findAllLogs() {
        try {
            // 간단하게 직접 조회 (원하면 repo에 메서드 추가해서 사용해도 됩니다)
            var sql = "SELECT id, message, created_at FROM audit_log ORDER BY id";
            try (var con = dataSource.getConnection();
                 var ps = con.prepareStatement(sql);
                 var rs = ps.executeQuery()) {
                var list = new java.util.ArrayList<java.util.Map<String, Object>>();
                while (rs.next()) {
                    list.add(Map.of(
                            "id", rs.getLong("id"),
                            "message", rs.getString("message"),
                            "created_at", rs.getTimestamp("created_at").toString()
                    ));
                }
                return list;
            }
        } catch (Exception e) {
            return java.util.List.of();
        }
    }

}
