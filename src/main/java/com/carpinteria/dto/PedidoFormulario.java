package com.carpinteria.dto;

import java.util.ArrayList;
import java.util.List;

public class PedidoFormulario {

    private Long clienteId;
    private String observaciones;
    private List<ItemPedidoFormulario> items = new ArrayList<>();

    public PedidoFormulario() {
        items.add(new ItemPedidoFormulario());
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<ItemPedidoFormulario> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoFormulario> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void agregarItem() {
        items.add(new ItemPedidoFormulario());
    }
}