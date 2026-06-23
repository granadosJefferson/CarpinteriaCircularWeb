package com.carpinteria.dto;

import java.math.BigDecimal;

public class ItemPedidoFormulario {

    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;

    public ItemPedidoFormulario() {
        this.cantidad = 1;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}