package com.carpinteria.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.carpinteria.model.EstadoPedido;
import com.carpinteria.repository.ClienteRepository;
import com.carpinteria.repository.PedidoRepository;
import com.carpinteria.repository.ProductoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public DashboardController(
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            PedidoRepository pedidoRepository) {

        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/admin")
    public String mostrarDashboard(
            HttpSession session,
            Model model) {

        String rol = (String) session.getAttribute("rol");

        if (!"ADMIN".equals(rol)) {
            return "redirect:/login";
        }

        long totalProductos =
                productoRepository.count();

        long productosActivos =
                productoRepository.countByActivoTrue();

        long totalClientes =
                clienteRepository.count();

        long pedidosPendientes =
                pedidoRepository.countByEstado(
                        EstadoPedido.PENDIENTE
                );

        long pedidosCompletados =
                pedidoRepository.countByEstado(
                        EstadoPedido.COMPLETADO
                );

        BigDecimal totalVendido =
                pedidoRepository.sumarTotalPorEstado(
                        EstadoPedido.COMPLETADO
                );

        if (totalVendido == null) {
            totalVendido = BigDecimal.ZERO;
        }

        model.addAttribute(
                "totalProductos",
                totalProductos
        );

        model.addAttribute(
                "productosActivos",
                productosActivos
        );

        model.addAttribute(
                "totalClientes",
                totalClientes
        );

        model.addAttribute(
                "pedidosPendientes",
                pedidosPendientes
        );

        model.addAttribute(
                "pedidosCompletados",
                pedidosCompletados
        );

        model.addAttribute(
                "totalVendido",
                totalVendido
        );

        return "dashboard";
    }
}