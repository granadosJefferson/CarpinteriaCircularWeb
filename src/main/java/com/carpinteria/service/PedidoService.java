package com.carpinteria.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpinteria.dto.ItemPedidoFormulario;
import com.carpinteria.dto.PedidoFormulario;
import com.carpinteria.model.Cliente;
import com.carpinteria.model.DetallePedido;
import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;
import com.carpinteria.model.Producto;
import com.carpinteria.repository.ClienteRepository;
import com.carpinteria.repository.PedidoRepository;
import com.carpinteria.repository.ProductoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaDesc();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el pedido"));
    }

    @Transactional
    public Pedido crearPedido(PedidoFormulario formulario) {

        validarFormulario(formulario);

        Cliente cliente = clienteRepository
                .findById(formulario.getClienteId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el cliente"));

        validarCliente(cliente);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setObservaciones(
                normalizarObservaciones(
                        formulario.getObservaciones()));

        Set<Long> productosAgregados = new HashSet<>();

        for (ItemPedidoFormulario item : formulario.getItems()) {

            if (item == null) {
                continue;
            }

            Long productoId = item.getProductoId();
            Integer cantidad = item.getCantidad();

            if (productoId == null) {
                throw new IllegalArgumentException(
                        "Debe seleccionar un producto en todas las filas");
            }

            if (!productosAgregados.add(productoId)) {
                throw new IllegalArgumentException(
                        "No puede agregar el mismo producto más de una vez");
            }

            Producto producto = productoRepository
                    .findById(productoId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No se encontró uno de los productos"));

            validarProducto(producto);
            validarCantidad(cantidad);
            validarExistencias(producto, cantidad);

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());

            pedido.agregarDetalle(detalle);

            producto.setCantidad(
                    producto.getCantidad() - cantidad);

            productoRepository.save(producto);
        }

        if (pedido.getDetalles() == null
                || pedido.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un producto");
        }

        pedido.calcularTotal();

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void cambiarEstado(
            Long id,
            EstadoPedido nuevoEstado) {

        Pedido pedido = buscarPorId(id);

        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un estado válido");
        }

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException(
                    "No se puede modificar un pedido cancelado");
        }

        if (nuevoEstado == EstadoPedido.CANCELADO) {
            devolverExistencias(pedido);
        }

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);
    }

    private void validarFormulario(
            PedidoFormulario formulario) {

        if (formulario == null) {
            throw new IllegalArgumentException(
                    "Los datos del pedido son inválidos");
        }

        if (formulario.getClienteId() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente");
        }

        if (formulario.getItems() == null
                || formulario.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe agregar al menos un producto");
        }
    }

    private void validarCliente(Cliente cliente) {

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new IllegalArgumentException(
                    "El cliente seleccionado está inactivo");
        }
    }

    private void validarProducto(Producto producto) {

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new IllegalArgumentException(
                    "El producto seleccionado está inactivo");
        }

        if (producto.getPrecio() == null
                || producto.getPrecio()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El producto tiene un precio inválido");
        }
    }

    private void validarCantidad(Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero");
        }
    }

    private void validarExistencias(
            Producto producto,
            Integer cantidad) {

        int disponibles = producto.getCantidad() == null
                ? 0
                : producto.getCantidad();

        if (cantidad > disponibles) {
            throw new IllegalArgumentException(
                    "No hay existencias suficientes para "
                            + producto.getNombre()
                            + ". Disponibles: "
                            + disponibles);
        }
    }

    private String normalizarObservaciones(
            String observaciones) {

        if (observaciones == null
                || observaciones.isBlank()) {

            return null;
        }

        return observaciones.trim();
    }

    private void devolverExistencias(Pedido pedido) {

        for (DetallePedido detalle : pedido.getDetalles()) {

            Producto producto = detalle.getProducto();

            int existenciaActual =
                    producto.getCantidad() == null
                            ? 0
                            : producto.getCantidad();

            producto.setCantidad(
                    existenciaActual
                            + detalle.getCantidad());

            productoRepository.save(producto);
        }
    }
}