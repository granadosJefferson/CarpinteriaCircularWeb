package com.carpinteria.dto;

import java.util.ArrayList;
import java.util.List;

import com.carpinteria.model.MetodoPago;
import com.carpinteria.model.TipoEntrega;

public class CheckoutFormulario {

    private TipoEntrega tipoEntrega;
    private MetodoPago metodoPago;

    private String referenciaPago;

    private String provincia;
    private String canton;
    private String distrito;
    private String direccionEntrega;
    private String telefonoEntrega;
    private String indicacionesEntrega;

    private List<ItemCheckout> items = new ArrayList<>();

    public CheckoutFormulario() {
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(TipoEntrega tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCanton() {
        return canton;
    }

    public void setCanton(String canton) {
        this.canton = canton;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getTelefonoEntrega() {
        return telefonoEntrega;
    }

    public void setTelefonoEntrega(String telefonoEntrega) {
        this.telefonoEntrega = telefonoEntrega;
    }

    public String getIndicacionesEntrega() {
        return indicacionesEntrega;
    }

    public void setIndicacionesEntrega(String indicacionesEntrega) {
        this.indicacionesEntrega = indicacionesEntrega;
    }

    public List<ItemCheckout> getItems() {
        return items;
    }

    public void setItems(List<ItemCheckout> items) {
        this.items = items;
    }
}