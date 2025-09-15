package com.example.transaction.dto;

public class ProductDto {
    private long id;
    private String name;
    private int stock;

    public ProductDto() {}
    public ProductDto(long id, String name, int stock) {
        this.id = id; this.name = name; this.stock = stock;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public int getStock() { return stock; }
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setStock(int stock) { this.stock = stock; }
}
