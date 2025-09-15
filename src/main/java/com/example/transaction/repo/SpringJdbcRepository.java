package com.example.transaction.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
@RequiredArgsConstructor
public class SpringJdbcRepository {

    private final DataSource dataSource;

    public int selectStockForUpdateTx(long productId) {
        String sql = "SELECT stock FROM product WHERE id = ? FOR UPDATE";
        Connection con = DataSourceUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setLong(1, productId);
            rs = ps.executeQuery();
            if (!rs.next()) throw new IllegalStateException("product not found: " + productId);
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("selectStockForUpdateTx failed", e);
        } finally {
            close(rs);
            close(ps);
            // 커넥션은 트랜잭션 범위에서 공유되므로 여기서 닫지 않음
        }
    }

    public void updateStockTx(long productId, int newStock) {
        String sql = "UPDATE product SET stock = ? WHERE id = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, newStock);
            ps.setLong(2, productId);
            if (ps.executeUpdate() != 1) throw new IllegalStateException("update affected != 1");
        } catch (SQLException e) {
            throw new RuntimeException("updateStockTx failed", e);
        } finally {
            close(ps);
        }
    }

    public void insertOrderTx(long productId, int qty, String status) {
        String sql = "INSERT INTO orders(product_id, qty, status) VALUES(?,?,?)";
        Connection con = DataSourceUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setLong(1, productId);
            ps.setInt(2, qty);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertOrderTx failed", e);
        } finally {
            close(ps);
        }
    }

    // (선택) 트랜잭션 경계 바깥에서 정리하고 싶을 때만 호출
    public void releaseTxConnection() {
        Connection con = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private void close(AutoCloseable c) {
        if (c != null) try { c.close(); } catch (Exception ignore) {}
    }

    public void insertAuditLogTx(String message) {
        String sql = "INSERT INTO audit_log(message) VALUES(?)";
        Connection con = DataSourceUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertAuditLogTx failed", e);
        } finally {
            close(ps);
        }
    }


}
