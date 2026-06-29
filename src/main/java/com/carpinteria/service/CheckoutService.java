
package com.carpinteria.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpinteria.dto.CheckoutFormulario;
import com.carpinteria.dto.ItemCheckout;
import com.carpinteria.model.Cliente;
import com.carpinteria.model.DetallePedido;
import com.carpinteria.model.EstadoPago;
import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.MetodoPago;
import com.carpinteria.model.Pedido;
import com.carpinteria.model.Producto;
import com.carpinteria.model.TipoEntrega;
import com.carpinteria.model.Usuario;
import com.carpinteria.repository.ClienteRepository;
import com.carpinteria.repository.PedidoRepository;
import com.carpinteria.repository.ProductoRepository;

@Service
public class CheckoutService {

    private static final BigDecimal COSTO_ENVIO =
            new BigDecimal("10000.00");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    public CheckoutService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository) {

        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
    }

    /*
     * Validates the checkout, creates the order and updates stock
     * inside a single database transaction.
     */
    @Transactional
    public Pedido procesarCompra(
            CheckoutFormulario formulario,
            Usuario usuario) {

        validarUsuario(usuario);
        validarFormulario(formulario);

        Cliente cliente = buscarCliente(usuario);

        Map<Long, Integer> productosConsolidados =
                consolidarProductos(formulario);

        Pedido pedido = crearPedidoBase(
                formulario,
                cliente
        );

        for (Map.Entry<Long, Integer> entrada
                : productosConsolidados.entrySet()) {

            Long productoId = entrada.getKey();
            Integer cantidadSolicitada = entrada.getValue();

            Producto producto = buscarProducto(productoId);

            validarProducto(
                    producto,
                    cantidadSolicitada
            );

            DetallePedido detalle = crearDetalle(
                    producto,
                    cantidadSolicitada
            );

            pedido.agregarDetalle(detalle);

            descontarExistencias(
                    producto,
                    cantidadSolicitada
            );
        }

        aplicarCostoEntrega(
                pedido,
                formulario
        );

        pedido.calcularTotal();

        return pedidoRepository.save(pedido);
    }

    private void validarUsuario(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Debe iniciar sesión para continuar con la compra"
            );
        }

        if (estaVacio(usuario.getCorreo())) {
            throw new IllegalArgumentException(
                    "El usuario autenticado no tiene un correo válido"
            );
        }
    }

    /*
     * Validates delivery and payment rules before modifying
     * inventory or creating the order.
     */
    private void validarFormulario(
            CheckoutFormulario formulario) {

        if (formulario == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos de la compra"
            );
        }

        if (formulario.getItems() == null
                || formulario.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "El carrito está vacío"
            );
        }

        if (formulario.getTipoEntrega() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de entrega"
            );
        }

        if (formulario.getMetodoPago() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un método de pago"
            );
        }

        if (formulario.getMetodoPago()
                == MetodoPago.PAGO_AL_RETIRAR
                && formulario.getTipoEntrega()
                != TipoEntrega.RECOGER) {

            throw new IllegalArgumentException(
                    "El pago al retirar solo está disponible "
                            + "cuando el pedido se recoge personalmente"
            );
        }

        if (formulario.getMetodoPago()
                == MetodoPago.SINPE_MOVIL) {

            validarComprobanteSinpe(
                    formulario.getReferenciaPago()
            );
        }

        if (formulario.getTipoEntrega()
                == TipoEntrega.ENVIO) {

            validarDatosEnvio(formulario);
        }
    }

    private Cliente buscarCliente(Usuario usuario) {

        return clienteRepository
                .findByCorreoIgnoreCase(
                        usuario.getCorreo().trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un cliente asociado al correo "
                                        + usuario.getCorreo()
                        )
                );
    }

    /*
     * Groups repeated cart items by product and safely adds
     * their requested quantities.
     */
    private Map<Long, Integer> consolidarProductos(
            CheckoutFormulario formulario) {

        Map<Long, Integer> productos = new LinkedHashMap<>();

        for (ItemCheckout item : formulario.getItems()) {

            if (item == null
                    || item.getProductoId() == null
                    || item.getCantidad() == null
                    || item.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "El carrito contiene un producto inválido"
                );
            }

            Long productoId = item.getProductoId();

            int cantidadActual =
                    productos.getOrDefault(productoId, 0);

            try {
                int cantidadConsolidada = Math.addExact(
                        cantidadActual,
                        item.getCantidad()
                );

                productos.put(
                        productoId,
                        cantidadConsolidada
                );

            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "La cantidad solicitada para un producto "
                                + "es demasiado grande",
                        ex
                );
            }
        }

        return productos;
    }

    /*
     * Creates the order header and copies the required
     * customer delivery and payment information.
     */
    private Pedido crearPedidoBase(
            CheckoutFormulario formulario,
            Cliente cliente) {

        Pedido pedido = new Pedido();

        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTipoEntrega(formulario.getTipoEntrega());
        pedido.setMetodoPago(formulario.getMetodoPago());

        configurarPago(
                pedido,
                formulario
        );

        if (formulario.getTipoEntrega()
                == TipoEntrega.ENVIO) {

            pedido.setProvincia(
                    limpiar(formulario.getProvincia())
            );

            pedido.setCanton(
                    limpiar(formulario.getCanton())
            );

            pedido.setDistrito(
                    limpiar(formulario.getDistrito())
            );

            pedido.setDireccionEntrega(
                    limpiar(formulario.getDireccionEntrega())
            );

            pedido.setTelefonoEntrega(
                    limpiar(formulario.getTelefonoEntrega())
            );

            pedido.setIndicacionesEntrega(
                    limpiar(formulario.getIndicacionesEntrega())
            );
        }

        return pedido;
    }

    /*
     * Assigns the simulated payment status and reference
     * according to the selected payment method.
     */
    private void configurarPago(
            Pedido pedido,
            CheckoutFormulario formulario) {

        MetodoPago metodoPago =
                formulario.getMetodoPago();

        switch (metodoPago) {

            case TARJETA -> {

                pedido.setEstadoPago(
                        EstadoPago.APROBADO_SIMULADO
                );

                pedido.setReferenciaPago(
                        generarReferenciaPago()
                );
            }

            case SINPE_MOVIL -> {

                pedido.setEstadoPago(
                        EstadoPago.APROBADO_SIMULADO
                );

                pedido.setReferenciaPago(
                        limpiar(
                                formulario.getReferenciaPago()
                        )
                );
            }

            case PAGO_AL_RETIRAR -> {

                pedido.setEstadoPago(
                        EstadoPago.PENDIENTE
                );

                pedido.setReferenciaPago(null);
            }

            default -> throw new IllegalArgumentException(
                    "El método de pago seleccionado no es válido"
            );
        }
    }

    private Producto buscarProducto(Long productoId) {

        return productoRepository
                .findById(productoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el producto con ID "
                                        + productoId
                        )
                );
    }

    /*
     * Confirms that the product is active, correctly priced
     * and has enough stock for the requested quantity.
     */
    private void validarProducto(
            Producto producto,
            Integer cantidadSolicitada) {

        if (!Boolean.TRUE.equals(
                producto.getActivo())) {

            throw new IllegalArgumentException(
                    "El producto "
                            + producto.getNombre()
                            + " no está disponible"
            );
        }

        if (producto.getPrecio() == null
                || producto.getPrecio()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El producto "
                            + producto.getNombre()
                            + " tiene un precio inválido"
            );
        }

        if (producto.getCantidad() == null
                || producto.getCantidad()
                < cantidadSolicitada) {

            int disponibles =
                    producto.getCantidad() == null
                            ? 0
                            : producto.getCantidad();

            throw new IllegalArgumentException(
                    "No hay suficientes existencias de "
                            + producto.getNombre()
                            + ". Disponibles: "
                            + disponibles
            );
        }
    }

    private DetallePedido crearDetalle(
            Producto producto,
            Integer cantidadSolicitada) {

        DetallePedido detalle =
                new DetallePedido();

        detalle.setProducto(producto);
        detalle.setCantidad(cantidadSolicitada);
        detalle.setPrecioUnitario(producto.getPrecio());

        return detalle;
    }

    /*
     * Reserves the purchased units immediately after the
     * order passes all product validations.
     */
    private void descontarExistencias(
            Producto producto,
            Integer cantidadSolicitada) {

        try {
            int nuevaCantidad = Math.subtractExact(
                    producto.getCantidad(),
                    cantidadSolicitada
            );

            producto.setCantidad(nuevaCantidad);

        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "No fue posible actualizar las existencias de "
                            + producto.getNombre(),
                    ex
            );
        }
    }

    /*
     * Applies the delivery fee and removes shipping data
     * when the customer chooses store pickup.
     */
    private void aplicarCostoEntrega(
            Pedido pedido,
            CheckoutFormulario formulario) {

        if (formulario.getTipoEntrega()
                == TipoEntrega.ENVIO) {

            pedido.setCostoEnvio(COSTO_ENVIO);

        } else {

            pedido.setCostoEnvio(BigDecimal.ZERO);
            pedido.setProvincia(null);
            pedido.setCanton(null);
            pedido.setDistrito(null);
            pedido.setDireccionEntrega(null);
            pedido.setTelefonoEntrega(null);
            pedido.setIndicacionesEntrega(null);
        }
    }

    private void validarDatosEnvio(
            CheckoutFormulario formulario) {

        if (estaVacio(formulario.getProvincia())) {
            throw new IllegalArgumentException(
                    "Debe indicar la provincia"
            );
        }

        if (estaVacio(formulario.getCanton())) {
            throw new IllegalArgumentException(
                    "Debe indicar el cantón"
            );
        }

        if (estaVacio(formulario.getDistrito())) {
            throw new IllegalArgumentException(
                    "Debe indicar el distrito"
            );
        }

        if (estaVacio(
                formulario.getDireccionEntrega())) {

            throw new IllegalArgumentException(
                    "Debe indicar la dirección exacta"
            );
        }

        if (estaVacio(
                formulario.getTelefonoEntrega())) {

            throw new IllegalArgumentException(
                    "Debe indicar un teléfono de contacto"
            );
        }
    }

    private void validarComprobanteSinpe(
            String referenciaPago) {

        if (estaVacio(referenciaPago)) {
            throw new IllegalArgumentException(
                    "Debe ingresar el número de comprobante "
                            + "del SINPE Móvil"
            );
        }

        String comprobante =
                referenciaPago.trim();

        if (comprobante.length() < 4
                || comprobante.length() > 50) {

            throw new IllegalArgumentException(
                    "El número de comprobante del SINPE Móvil "
                            + "debe tener entre 4 y 50 caracteres"
            );
        }

        if (!comprobante.matches("[A-Za-z0-9\\-]+")) {
            throw new IllegalArgumentException(
                    "El comprobante del SINPE Móvil solo puede "
                            + "contener letras, números y guiones"
            );
        }
    }

    private String generarReferenciaPago() {

        return "SIM-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }

    private String limpiar(String valor) {

        if (valor == null) {
            return null;
        }

        String texto = valor.trim();

        return texto.isEmpty()
                ? null
                : texto;
    }

    private boolean estaVacio(String valor) {

        return valor == null
                || valor.isBlank();
    }
}
