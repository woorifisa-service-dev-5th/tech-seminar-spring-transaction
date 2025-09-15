package com.example.transaction.dto;

import java.time.LocalDateTime;

public class OrderDto {
    private long id;
    private long productId;
    private int qty;
    private String status;
    private LocalDateTime createdAt;

    public OrderDto() {}
    public OrderDto(long id, long productId, int qty, String status, LocalDateTime createdAt) {
        this.id = id; this.productId = productId; this.qty = qty; this.status = status; this.createdAt = createdAt;
    }
    public long getId() { return id; }
    public long getProductId() { return productId; }
    public int getQty() { return qty; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(long id) { this.id = id; }
    public void setProductId(long productId) { this.productId = productId; }
    public void setQty(int qty) { this.qty = qty; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
