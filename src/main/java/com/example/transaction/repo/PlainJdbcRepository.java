package com.example.transaction.repo;

import com.example.transaction.dto.OrderDto;
import com.example.transaction.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlainJdbcRepository {
    private final DataSource dataSource;

    // ----- 기존 메서드 (미적용 버전용) -----
    public int findStockByProductId(long productId) {
        String sql = "SELECT stock FROM product WHERE id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("product not found: " + productId);
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("findStockByProductId failed", e);
        }
    }

//    public void updateStock(long productId, int newStock) {
//        String sql = "UPDATE product SET stock = ? WHERE id = ?";
//        try (Connection con = dataSource.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//            ps.setInt(1, newStock);
//            ps.setLong(2, productId);
//            if (ps.executeUpdate() != 1) throw new IllegalStateException("updateStock affected != 1");
//        } catch (SQLException e) {
//            throw new RuntimeException("updateStock failed", e);
//        }
//    }

    public void decreaseStock(long productId, int qty) {
        String sql = "UPDATE product SET stock = stock - ? WHERE id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setLong(2, productId);
            if (ps.executeUpdate() != 1) throw new IllegalStateException("decreaseStock affected != 1");
        } catch (SQLException e) {
            throw new RuntimeException("decreaseStock failed", e);
        }
    }

    public void insertOrder(long productId, int qty, String status) {
        String sql = "INSERT INTO orders(product_id, qty, status) VALUES(?,?,?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setInt(2, qty);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertOrder failed", e);
        }
    }

    // ----- 프로그램 방식 트랜잭션용 내부 JDBC 유틸 -----
    public int selectStockForUpdate(Connection con, long productId) throws SQLException {
        String sql = "SELECT stock FROM product WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("product not found: " + productId);
                return rs.getInt(1);
            }
        }
    }
    public void updateStock(Connection con, long productId, int newStock) throws SQLException {
        String sql = "UPDATE product SET stock = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setLong(2, productId);
            if (ps.executeUpdate() != 1) throw new IllegalStateException("updateStock affected != 1");
        }
    }
    public void insertOrder(Connection con, long productId, int qty, String status) throws SQLException {
        String sql = "INSERT INTO orders(product_id, qty, status) VALUES(?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setInt(2, qty);
            ps.setString(3, status);
            ps.executeUpdate();
        }
    }

    // ----- 조회/리셋/시드 -----
    public List<ProductDto> findAllProducts() {
        String sql = "SELECT id, name, stock FROM product ORDER BY id";
        List<ProductDto> list = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ProductDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("stock")));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("findAllProducts failed", e);
        }
    }

    public List<OrderDto> findAllOrders() {
        String sql = "SELECT id, product_id, qty, status, created_at FROM orders ORDER BY id DESC";
        List<OrderDto> list = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                list.add(new OrderDto(
                        rs.getLong("id"),
                        rs.getLong("product_id"),
                        rs.getInt("qty"),
                        rs.getString("status"),
                        ts == null ? null : ts.toLocalDateTime()));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("findAllOrders failed", e);
        }
    }

    public void deleteAllOrders() {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM orders")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteAllOrders failed", e);
        }
    }

    public void deleteAllProducts() {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM product")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteAllProducts failed", e);
        }
    }

    public long insertProduct(String name, int stock) {
        String sql = "INSERT INTO product(name, stock) VALUES(?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, stock);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new IllegalStateException("No generated key");
            }
        } catch (SQLException e) {
            throw new RuntimeException("insertProduct failed", e);
        }
    }

    public void setProductStock(long productId, int stock) {
        String sql = "UPDATE product SET stock=? WHERE id=?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stock);
            ps.setLong(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("setProductStock failed", e);
        }
    }
}
