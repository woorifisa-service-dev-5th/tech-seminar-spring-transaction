// src/main/java/com/example/txdemo/service/ProgrammaticTxOrderService.java
package com.example.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;

@Service
@RequiredArgsConstructor
public class ProgrammaticTxOrderService {

    private final DataSource dataSource;

    /**
     * 프로그래밍 방식 트랜잭션:
     * - 하나의 Connection으로 재고 차감 + 주문 생성을 원자적으로 수행
     * - 실패 시 롤백, 성공 시 커밋
     */
    public void placeOrderWithManualTx(long productId, int qty, boolean forceFail) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            // 트랜잭션 시작
            con.setAutoCommit(false);

            // 1) 재고 조회 (같은 커넥션)
            int stock = selectStock(con, productId);
            if (stock < qty) {
                throw new IllegalStateException("재고 부족: stock=" + stock);
            }

            // 2) 재고 차감
            updateStock(con, productId, stock - qty);

            // 3) 주문 생성
            insertOrder(con, productId, qty, "CREATED");

            // (테스트용) 강제 실패 플래그
            if (forceFail) {
                throw new RuntimeException("결제 실패 시뮬레이션");
            }

            // 모든 작업 OK → 커밋
            con.commit();
        } catch (Exception e) {
            // 실패 → 롤백
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("placeOrderWithManualTx failed", e);
        } finally {
            if (con != null) {
                try {
                    // 자동 커밋 모드 원복 (커넥션 풀 고려)
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {}
                try { con.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ----- 내부 JDBC 유틸 -----

    private int selectStock(Connection con, long productId) throws SQLException {
        String sql = "SELECT stock FROM product WHERE id = ? FOR UPDATE";
        // FOR UPDATE: 같은 행 갱신 경쟁 시 직관적 데모용(선택)
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("product not found: " + productId);
                return rs.getInt(1);
            }
        }
    }

    private void updateStock(Connection con, long productId, int newStock) throws SQLException {
        String sql = "UPDATE product SET stock = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setLong(2, productId);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("updateStock affected != 1");
            }
        }
    }

    private void insertOrder(Connection con, long productId, int qty, String status) throws SQLException {
        String sql = "INSERT INTO orders(product_id, qty, status) VALUES(?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setInt(2, qty);
            ps.setString(3, status);
            ps.executeUpdate();
        }
    }
}
