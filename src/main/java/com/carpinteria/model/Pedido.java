package com.carpinteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido {

    private static final BigDecimal COSTO_ENVIO_FIJO =
            new BigDecimal("10000.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    /*
     * Total de los productos sin incluir el envío.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /*
     * Costo adicional por envío.
     * Será ₡0 para retiro personal y ₡10 000 para envío.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    /*
     * Total definitivo:
     * subtotal + costoEnvio.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEntrega tipoEntrega = TipoEntrega.RECOGER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPago metodoPago = MetodoPago.TARJETA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    /*
     * Referencia generada para el pago simulado.
     * Ejemplo: SIM-A12B34CD.
     */
    @Column(length = 100)
    private String referenciaPago;

    /*
     * Estos campos se utilizan únicamente cuando
     * tipoEntrega es ENVIO.
     */
    @Column(length = 100)
    private String provincia;

    @Column(length = 100)
    private String canton;

    @Column(length = 100)
    private String distrito;

    @Column(length = 500)
    private String direccionEntrega;

    @Column(length = 30)
    private String telefonoEntrega;

    @Column(length = 500)
    private String indicacionesEntrega;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    /*
     * Se ejecuta antes de insertar el pedido.
     */
    @PrePersist
    public void prepararPedido() {

        if (fecha == null) {
            fecha = LocalDateTime.now();
        }

        prepararValoresPredeterminados();
        calcularTotal();
    }

    /*
     * Mantiene los cálculos consistentes cuando
     * el pedido se actualiza.
     */
    @PreUpdate
    public void prepararActualizacion() {

        prepararValoresPredeterminados();
        calcularTotal();
    }

    private void prepararValoresPredeterminados() {

        if (estado == null) {
            estado = EstadoPedido.PENDIENTE;
        }

        if (tipoEntrega == null) {
            tipoEntrega = TipoEntrega.RECOGER;
        }

        if (metodoPago == null) {
            metodoPago = MetodoPago.TARJETA;
        }

        if (estadoPago == null) {
            estadoPago = EstadoPago.PENDIENTE;
        }

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (costoEnvio == null) {
            costoEnvio = BigDecimal.ZERO;
        }

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        if (detalles == null) {
            detalles = new ArrayList<>();
        }
    }

    public void agregarDetalle(DetallePedido detalle) {

        if (detalle == null) {
            throw new IllegalArgumentException(
                    "El detalle del pedido no puede ser nulo"
            );
        }

        if (detalles == null) {
            detalles = new ArrayList<>();
        }

        detalles.add(detalle);
        detalle.setPedido(this);
    }

    public void eliminarDetalle(DetallePedido detalle) {

        if (detalle == null || detalles == null) {
            return;
        }

        if (detalles.remove(detalle)) {
            detalle.setPedido(null);
        }
    }

    /*
     * Calcula:
     *
     * subtotal = suma de precio unitario × cantidad
     * total = subtotal + costo de envío
     */
    public void calcularTotal() {

        BigDecimal subtotalCalculado = BigDecimal.ZERO;

        if (detalles != null) {

            for (DetallePedido detalle : detalles) {

                if (detalle == null) {
                    continue;
                }

                BigDecimal precioUnitario =
                        detalle.getPrecioUnitario();

                Integer cantidad =
                        detalle.getCantidad();

                if (precioUnitario == null) {
                    throw new IllegalStateException(
                            "Un detalle del pedido no tiene precio unitario"
                    );
                }

                if (precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalStateException(
                            "El precio unitario de un detalle no puede ser negativo"
                    );
                }

                if (cantidad == null || cantidad <= 0) {
                    throw new IllegalStateException(
                            "La cantidad de un detalle debe ser mayor que cero"
                    );
                }

                BigDecimal subtotalDetalle =
                        precioUnitario.multiply(
                                BigDecimal.valueOf(cantidad)
                        );

                subtotalCalculado =
                        subtotalCalculado.add(subtotalDetalle);
            }
        }

        BigDecimal envioSeguro =
                costoEnvio != null
                        ? costoEnvio
                        : BigDecimal.ZERO;

        if (envioSeguro.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "El costo de envío no puede ser negativo"
            );
        }

        this.subtotal = subtotalCalculado;
        this.costoEnvio = envioSeguro;
        this.total = subtotalCalculado.add(envioSeguro);
    }

    /*
     * Configura automáticamente el costo según
     * el tipo de entrega seleccionado.
     */
    public void configurarCostoEntrega() {

        if (tipoEntrega == TipoEntrega.ENVIO) {
            costoEnvio = COSTO_ENVIO_FIJO;
        } else {
            costoEnvio = BigDecimal.ZERO;
        }

        calcularTotal();
    }

    public boolean requiereEnvio() {
        return tipoEntrega == TipoEntrega.ENVIO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal != null
                ? subtotal
                : BigDecimal.ZERO;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio != null
                ? costoEnvio
                : BigDecimal.ZERO;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total != null
                ? total
                : BigDecimal.ZERO;
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

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {

        /*
         * Desconecta los detalles anteriores para mantener
         * consistente la relación bidireccional.
         */
        if (this.detalles != null) {
            for (DetallePedido detalleAnterior : this.detalles) {
                if (detalleAnterior != null) {
                    detalleAnterior.setPedido(null);
                }
            }
        }

        this.detalles = new ArrayList<>();

        if (detalles == null) {
            return;
        }

        for (DetallePedido detalle : detalles) {

            if (detalle == null) {
                continue;
            }

            this.detalles.add(detalle);
            detalle.setPedido(this);
        }
    }
}