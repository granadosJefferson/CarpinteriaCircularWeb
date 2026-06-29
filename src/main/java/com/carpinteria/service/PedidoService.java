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
import com.carpinteria.model.EstadoPago;
import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.MetodoPago;
import com.carpinteria.model.Pedido;
import com.carpinteria.model.Producto;
import com.carpinteria.repository.ClienteRepository;
import com.carpinteria.repository.PedidoRepository;
import com.carpinteria.repository.ProductoRepository;

@Service
public class PedidoService {

    private static final int LIMITE_OBSERVACIONES = 500;

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

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {

        validarIdPedido(id);

        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el pedido con ID " + id
                        )
                );
    }

    /**
     * Crea un pedido desde el panel administrativo.
     *
     * El pedido inicia en estado PENDIENTE y descuenta
     * las existencias de cada producto seleccionado.
     */
    @Transactional
    public Pedido crearPedido(PedidoFormulario formulario) {

        validarFormulario(formulario);

        Cliente cliente = buscarCliente(
                formulario.getClienteId()
        );

        validarCliente(cliente);

        Pedido pedido = new Pedido();

        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        pedido.setObservaciones(
                normalizarObservaciones(
                        formulario.getObservaciones()
                )
        );

        Set<Long> productosAgregados = new HashSet<>();

        for (ItemPedidoFormulario item : formulario.getItems()) {

            validarItem(item);

            Long productoId = item.getProductoId();
            Integer cantidad = item.getCantidad();

            if (!productosAgregados.add(productoId)) {
                throw new IllegalArgumentException(
                        "No puede agregar el mismo producto más de una vez"
                );
            }

            Producto producto = buscarProducto(productoId);

            validarProducto(producto);
            validarCantidad(cantidad);
            validarExistencias(producto, cantidad);

            DetallePedido detalle = crearDetalle(
                    producto,
                    cantidad
            );

            pedido.agregarDetalle(detalle);

            descontarExistencias(
                    producto,
                    cantidad
            );
        }

        if (pedido.getDetalles() == null
                || pedido.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un producto"
            );
        }

        pedido.calcularTotal();

        return pedidoRepository.save(pedido);
    }

    /**
     * Cambia el estado del pedido respetando estas transiciones:
     *
     * PENDIENTE -> EN_PROCESO
     * PENDIENTE -> CANCELADO
     * EN_PROCESO -> COMPLETADO
     * EN_PROCESO -> CANCELADO
     *
     * COMPLETADO y CANCELADO son estados finales.
     */
    @Transactional
    public void cambiarEstado(
            Long id,
            EstadoPedido nuevoEstado) {

        Pedido pedido = buscarPedidoParaModificar(id);

        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un estado válido"
            );
        }

        EstadoPedido estadoActual = pedido.getEstado();

        if (estadoActual == null) {
            throw new IllegalArgumentException(
                    "El pedido no tiene un estado válido"
            );
        }

        if (estadoActual == nuevoEstado) {
            throw new IllegalArgumentException(
                    "El pedido ya se encuentra en el estado "
                            + formatearEstado(nuevoEstado)
            );
        }

        validarTransicionEstado(
                estadoActual,
                nuevoEstado
        );

        /*
         * La devolución solo ocurre cuando la transición
         * hacia CANCELADO fue validada correctamente.
         *
         * Como CANCELADO es un estado final, las existencias
         * no pueden devolverse una segunda vez.
         */
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            devolverExistencias(pedido);
        }

        pedido.setEstado(nuevoEstado);

        pedidoRepository.save(pedido);
    }

    /**
     * Registra el pago de un pedido creado con la opción
     * PAGO_AL_RETIRAR.
     */
    @Transactional
    public void marcarPagoAlRetirarComoPagado(Long id) {

        Pedido pedido = buscarPedidoParaModificar(id);

        EstadoPedido estadoPedido = pedido.getEstado();

        if (estadoPedido == null) {
            throw new IllegalArgumentException(
                    "El pedido no tiene un estado válido"
            );
        }

        if (estadoPedido == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException(
                    "No se puede registrar el pago de un pedido cancelado"
            );
        }

        if (pedido.getMetodoPago() != MetodoPago.PAGO_AL_RETIRAR) {
            throw new IllegalArgumentException(
                    "Este pedido no utiliza el método de pago al retirar"
            );
        }

        if (pedido.getEstadoPago() == EstadoPago.APROBADO_SIMULADO) {
            throw new IllegalArgumentException(
                    "El pago de este pedido ya fue registrado"
            );
        }

        if (pedido.getEstadoPago() != EstadoPago.PENDIENTE) {
            throw new IllegalArgumentException(
                    "El pedido no tiene un pago pendiente"
            );
        }

        if (estadoPedido != EstadoPedido.EN_PROCESO
                && estadoPedido != EstadoPedido.COMPLETADO) {

            throw new IllegalArgumentException(
                    "El pago al retirar solo puede registrarse "
                            + "cuando el pedido está en proceso o completado"
            );
        }

        pedido.setEstadoPago(
                EstadoPago.APROBADO_SIMULADO
        );

        pedido.setReferenciaPago(
                generarReferenciaPagoRetiro(
                        pedido.getId()
                )
        );

        pedidoRepository.save(pedido);
    }

    private void validarIdPedido(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del pedido es inválido"
            );
        }
    }

    private Pedido buscarPedidoParaModificar(Long id) {

        validarIdPedido(id);

        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el pedido con ID " + id
                        )
                );
    }

    private void validarFormulario(
            PedidoFormulario formulario) {

        if (formulario == null) {
            throw new IllegalArgumentException(
                    "Los datos del pedido son inválidos"
            );
        }

        if (formulario.getClienteId() == null
                || formulario.getClienteId() <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente válido"
            );
        }

        if (formulario.getItems() == null
                || formulario.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe agregar al menos un producto"
            );
        }
    }

    private void validarItem(ItemPedidoFormulario item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "El pedido contiene una fila de producto inválida"
            );
        }

        if (item.getProductoId() == null
                || item.getProductoId() <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un producto en todas las filas"
            );
        }

        validarCantidad(item.getCantidad());
    }

    private Cliente buscarCliente(Long clienteId) {

        return clienteRepository.findById(clienteId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el cliente seleccionado"
                        )
                );
    }

    private void validarCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente seleccionado es inválido"
            );
        }

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new IllegalArgumentException(
                    "El cliente seleccionado está inactivo"
            );
        }
    }

    private Producto buscarProducto(Long productoId) {

        return productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el producto con ID "
                                        + productoId
                        )
                );
    }

    private void validarProducto(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto seleccionado es inválido"
            );
        }

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new IllegalArgumentException(
                    "El producto "
                            + obtenerNombreProducto(producto)
                            + " está inactivo"
            );
        }

        if (producto.getPrecio() == null
                || producto.getPrecio()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El producto "
                            + obtenerNombreProducto(producto)
                            + " tiene un precio inválido"
            );
        }

        if (producto.getCantidad() == null
                || producto.getCantidad() < 0) {

            throw new IllegalArgumentException(
                    "El producto "
                            + obtenerNombreProducto(producto)
                            + " tiene existencias inválidas"
            );
        }
    }

    private void validarCantidad(Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
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
                            + obtenerNombreProducto(producto)
                            + ". Disponibles: "
                            + disponibles
            );
        }
    }

    private DetallePedido crearDetalle(
            Producto producto,
            Integer cantidad) {

        DetallePedido detalle = new DetallePedido();

        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());

        return detalle;
    }

    private void descontarExistencias(
            Producto producto,
            Integer cantidad) {

        Integer existenciaActual = producto.getCantidad();

        if (existenciaActual == null) {
            throw new IllegalArgumentException(
                    "El producto "
                            + obtenerNombreProducto(producto)
                            + " no tiene existencias válidas"
            );
        }

        try {
            int nuevaExistencia = Math.subtractExact(
                    existenciaActual,
                    cantidad
            );

            if (nuevaExistencia < 0) {
                throw new IllegalArgumentException(
                        "No hay existencias suficientes para "
                                + obtenerNombreProducto(producto)
                );
            }

            producto.setCantidad(nuevaExistencia);

            /*
             * No es necesario llamar save(producto).
             *
             * Como el producto fue recuperado dentro de esta
             * transacción, JPA detectará el cambio y actualizará
             * la existencia mediante dirty checking.
             */

        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "No fue posible actualizar las existencias de "
                            + obtenerNombreProducto(producto),
                    ex
            );
        }
    }

    private void validarTransicionEstado(
            EstadoPedido estadoActual,
            EstadoPedido nuevoEstado) {

        boolean transicionValida = switch (estadoActual) {

            case PENDIENTE ->
                    nuevoEstado == EstadoPedido.EN_PROCESO
                            || nuevoEstado == EstadoPedido.CANCELADO;

            case EN_PROCESO ->
                    nuevoEstado == EstadoPedido.COMPLETADO
                            || nuevoEstado == EstadoPedido.CANCELADO;

            case COMPLETADO, CANCELADO -> false;
        };

        if (!transicionValida) {
            throw new IllegalArgumentException(
                    "No se puede cambiar el pedido de "
                            + formatearEstado(estadoActual)
                            + " a "
                            + formatearEstado(nuevoEstado)
            );
        }
    }

    private void devolverExistencias(Pedido pedido) {

        if (pedido == null) {
            throw new IllegalArgumentException(
                    "El pedido es inválido"
            );
        }

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException(
                    "Las existencias de este pedido ya fueron devueltas"
            );
        }

        if (pedido.getEstado() == EstadoPedido.COMPLETADO) {
            throw new IllegalArgumentException(
                    "No se pueden devolver las existencias "
                            + "de un pedido completado"
            );
        }

        if (pedido.getDetalles() == null
                || pedido.getDetalles().isEmpty()) {

            throw new IllegalStateException(
                    "El pedido no contiene detalles para devolver"
            );
        }

        for (DetallePedido detalle : pedido.getDetalles()) {

            if (detalle == null) {
                throw new IllegalStateException(
                        "El pedido contiene un detalle inválido"
                );
            }

            Producto producto = detalle.getProducto();
            Integer cantidad = detalle.getCantidad();

            if (producto == null) {
                throw new IllegalStateException(
                        "Uno de los detalles no tiene un producto asociado"
                );
            }

            if (cantidad == null || cantidad <= 0) {
                throw new IllegalStateException(
                        "La cantidad que debe devolverse es inválida"
                );
            }

            int existenciaActual = producto.getCantidad() == null
                    ? 0
                    : producto.getCantidad();

            try {
                int nuevaExistencia = Math.addExact(
                        existenciaActual,
                        cantidad
                );

                producto.setCantidad(nuevaExistencia);

                /*
                 * El producto está administrado por JPA dentro
                 * de la transacción. No requiere save individual.
                 */

            } catch (ArithmeticException ex) {
                throw new IllegalStateException(
                        "No se pudieron devolver las existencias de "
                                + obtenerNombreProducto(producto),
                        ex
                );
            }
        }
    }

    private String normalizarObservaciones(
            String observaciones) {

        if (observaciones == null
                || observaciones.isBlank()) {

            return null;
        }

        String observacionesNormalizadas =
                observaciones.trim();

        if (observacionesNormalizadas.length()
                > LIMITE_OBSERVACIONES) {

            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar los "
                            + LIMITE_OBSERVACIONES
                            + " caracteres"
            );
        }

        return observacionesNormalizadas;
    }

    private String generarReferenciaPagoRetiro(
            Long pedidoId) {

        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException(
                    "No se puede generar la referencia del pago"
            );
        }

        return "RETIRO-"
                + pedidoId
                + "-"
                + System.currentTimeMillis();
    }

    private String obtenerNombreProducto(
            Producto producto) {

        if (producto == null
                || producto.getNombre() == null
                || producto.getNombre().isBlank()) {

            return "seleccionado";
        }

        return producto.getNombre().trim();
    }

    private String formatearEstado(
            EstadoPedido estado) {

        if (estado == null) {
            return "DESCONOCIDO";
        }

        return estado.name()
                .replace("_", " ");
    }
}