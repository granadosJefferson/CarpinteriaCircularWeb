package com.carpinteria.controller;

import java.math.BigDecimal;
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

    @GetMapping("/admin/reportes")
    public String mostrarReportes(
            HttpSession session,
            Model model) {

        String rol = (String) session.getAttribute("rol");

        if (!"ADMIN".equals(rol)) {
            return "redirect:/login";
        }

        long totalProductos = productoRepository.count();
        long totalClientes = clienteRepository.count();
        long totalPedidos = pedidoRepository.count();

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

        List<Pedido> pedidosCompletadosLista =
                pedidoRepository.findByEstado(
                        EstadoPedido.COMPLETADO
                );

        BigDecimal totalVendido = pedidosCompletadosLista
                .stream()
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedioVenta = BigDecimal.ZERO;

        if (!pedidosCompletadosLista.isEmpty()) {
            promedioVenta = totalVendido.divide(
                    BigDecimal.valueOf(
                            pedidosCompletadosLista.size()
                    ),
                    2,
                    java.math.RoundingMode.HALF_UP
            );
        }

        List<Producto> productosPocasExistencias =
                productoRepository.findAll()
                        .stream()
                        .filter(producto ->
                                producto.getCantidad() != null
                                && producto.getCantidad()
                                        <= LIMITE_EXISTENCIAS_BAJAS
                        )
                        .sorted(
                                Comparator.comparing(
                                        Producto::getCantidad
                                )
                        )
                        .toList();

        List<Pedido> pedidosRecientes =
                pedidoRepository
                        .findAllByOrderByFechaDesc()
                        .stream()
                        .limit(10)
                        .toList();

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
}