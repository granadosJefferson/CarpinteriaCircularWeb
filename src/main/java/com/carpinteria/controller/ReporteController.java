
package com.carpinteria.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;
import com.carpinteria.model.Producto;
import com.carpinteria.repository.ClienteRepository;
import com.carpinteria.repository.PedidoRepository;
import com.carpinteria.repository.ProductoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ReporteController {

    private static final int LIMITE_EXISTENCIAS_BAJAS = 5;

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ReporteController(
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            PedidoRepository pedidoRepository) {

        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Loads the administrative report metrics and sends them to the view.
     */
    @GetMapping("/admin/reportes")
    public String mostrarReportes(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        long totalProductos =
                productoRepository.count();

        long totalClientes =
                clienteRepository.count();

        long totalPedidos =
                pedidoRepository.count();

        long pedidosPendientes =
                pedidoRepository.countByEstado(
                        EstadoPedido.PENDIENTE
                );

        long pedidosEnProceso =
                pedidoRepository.countByEstado(
                        EstadoPedido.EN_PROCESO
                );

        long pedidosCompletados =
                pedidoRepository.countByEstado(
                        EstadoPedido.COMPLETADO
                );

        long pedidosCancelados =
                pedidoRepository.countByEstado(
                        EstadoPedido.CANCELADO
                );

        BigDecimal totalVendido =
                pedidoRepository.sumarTotalPorEstado(
                        EstadoPedido.COMPLETADO
                );

        if (totalVendido == null) {
            totalVendido = BigDecimal.ZERO;
        }

        BigDecimal promedioVenta =
                calcularPromedioVenta(
                        totalVendido,
                        pedidosCompletados
                );

        List<Producto> productosPocasExistencias =
                obtenerProductosPocasExistencias();

        List<Pedido> pedidosRecientes =
                obtenerPedidosRecientes();

        model.addAttribute(
                "totalProductos",
                totalProductos
        );

        model.addAttribute(
                "totalClientes",
                totalClientes
        );

        model.addAttribute(
                "totalPedidos",
                totalPedidos
        );

        model.addAttribute(
                "pedidosPendientes",
                pedidosPendientes
        );

        model.addAttribute(
                "pedidosEnProceso",
                pedidosEnProceso
        );

        model.addAttribute(
                "pedidosCompletados",
                pedidosCompletados
        );

        model.addAttribute(
                "pedidosCancelados",
                pedidosCancelados
        );

        model.addAttribute(
                "totalVendido",
                totalVendido
        );

        model.addAttribute(
                "promedioVenta",
                promedioVenta
        );

        model.addAttribute(
                "productosPocasExistencias",
                productosPocasExistencias
        );

        model.addAttribute(
                "pedidosRecientes",
                pedidosRecientes
        );

        model.addAttribute(
                "limiteExistenciasBajas",
                LIMITE_EXISTENCIAS_BAJAS
        );

        return "admin/reportes";
    }

    /**
     * Calculates the average sale using only completed orders.
     */
    private BigDecimal calcularPromedioVenta(
            BigDecimal totalVendido,
            long cantidadPedidosCompletados) {

        if (cantidadPedidosCompletados <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalSeguro =
                totalVendido != null
                        ? totalVendido
                        : BigDecimal.ZERO;

        return totalSeguro.divide(
                BigDecimal.valueOf(
                        cantidadPedidosCompletados
                ),
                2,
                RoundingMode.HALF_UP
        );
    }

    /**
     * Returns active products with stock between zero and the low-stock limit.
     */
    private List<Producto> obtenerProductosPocasExistencias() {

        List<Producto> productos =
                productoRepository.findAll();

        if (productos == null || productos.isEmpty()) {
            return List.of();
        }

        return productos.stream()
                .filter(producto -> producto != null)
                .filter(producto ->
                        Boolean.TRUE.equals(
                                producto.getActivo()
                        )
                )
                .filter(producto ->
                        producto.getCantidad() >= 0
                )
                .filter(producto ->
                        producto.getCantidad()
                                <= LIMITE_EXISTENCIAS_BAJAS
                )
               .sorted(
        Comparator
                .comparingInt(
                        (Producto producto) ->
                                producto.getCantidad()
                )
                .thenComparing(
                        producto ->
                                obtenerNombreProducto(producto),
                        String.CASE_INSENSITIVE_ORDER
                )
)
                .toList();
    }

    /**
     * Returns the ten most recent orders or an empty list.
     */
    private List<Pedido> obtenerPedidosRecientes() {

        List<Pedido> pedidos =
                pedidoRepository
                        .findTop10ByOrderByFechaDesc();

        return pedidos != null
                ? pedidos
                : List.of();
    }

    /**
     * Returns a normalized product name for safe sorting.
     */
    private String obtenerNombreProducto(
            Producto producto) {

        if (producto == null
                || producto.getNombre() == null
                || producto.getNombre().isBlank()) {

            return "";
        }

        return producto.getNombre().trim();
    }

    /**
     * Verifies that the current session belongs to an administrator.
     */
    private boolean esAdministrador(
            HttpSession session) {

        if (session == null) {
            return false;
        }

        Object usuarioLogueado =
                session.getAttribute(
                        "usuarioLogueado"
                );

        Object rol =
                session.getAttribute("rol");

        return usuarioLogueado != null
                && rol != null
                && "ADMIN".equalsIgnoreCase(
                        rol.toString().trim()
                );
    }
}

