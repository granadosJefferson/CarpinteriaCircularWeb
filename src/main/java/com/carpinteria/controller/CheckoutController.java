package com.carpinteria.controller;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.carpinteria.dto.CheckoutFormulario;
import com.carpinteria.model.Pedido;
import com.carpinteria.model.Usuario;
import com.carpinteria.repository.PedidoRepository;
import com.carpinteria.service.CheckoutService;

@Controller
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final PedidoRepository pedidoRepository;

    public CheckoutController(
            CheckoutService checkoutService,
            PedidoRepository pedidoRepository) {

        this.checkoutService = checkoutService;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/checkout")
    public String mostrarCheckout(
            HttpSession session,
            Model model) {

        Usuario usuario = obtenerUsuarioSesion(session);

        if (usuario == null) {
            return "redirect:/login?redirect=/checkout";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("costoEnvio", 10000);

        return "checkout";
    }

    @PostMapping("/checkout/confirmar")
    @ResponseBody
    public ResponseEntity<?> confirmarCompra(
            @RequestBody CheckoutFormulario formulario,
            HttpSession session) {

        Usuario usuario = obtenerUsuarioSesion(session);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error",
                            "La sesión ha finalizado. Inicie sesión nuevamente."
                    ));
        }

        try {
            Pedido pedido = checkoutService.procesarCompra(
                    formulario,
                    usuario
            );

            return ResponseEntity.ok(
                    Map.of(
                            "pedidoId", pedido.getId(),
                            "redirect",
                            "/compra-exitosa/" + pedido.getId()
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            ex.getMessage()
                    ));

        } catch (Exception ex) {

            ex.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "No se pudo completar la compra. Intente nuevamente."
                    ));
        }
    }

    @GetMapping("/compra-exitosa/{id}")
    public String mostrarCompraExitosa(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Usuario usuario = obtenerUsuarioSesion(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        Pedido pedido = pedidoRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pedido no encontrado"
                        )
                );

        /*
         * Evita que un usuario autenticado consulte
         * pedidos pertenecientes a otro cliente.
         */
        if (pedido.getCliente() == null
                || pedido.getCliente().getCorreo() == null
                || !pedido.getCliente()
                        .getCorreo()
                        .equalsIgnoreCase(usuario.getCorreo())) {

            return "redirect:/catalogo";
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);

        return "compra-exitosa";
    }

    private Usuario obtenerUsuarioSesion(
            HttpSession session) {

        Object usuarioSesion =
                session.getAttribute("usuarioLogueado");

        if (usuarioSesion instanceof Usuario usuario) {
            return usuario;
        }

        return null;
    }
}