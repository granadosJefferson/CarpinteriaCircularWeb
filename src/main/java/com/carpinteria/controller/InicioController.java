package com.carpinteria.controller;

import com.carpinteria.model.Producto;
import com.carpinteria.repository.ProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class InicioController {

    private static final int MAXIMO_PRODUCTOS_CARRUSEL = 6;

    private final ProductoRepository productoRepository;

    public InicioController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping("/")
    public String mostrarInicio(Model model) {

        // Only products with an uploaded image are displayed in the home carousel.
        List<Producto> productosCarrusel = productoRepository.findAll()
                .stream()
                .filter(producto -> producto.getImagen() != null)
                .filter(producto -> !producto.getImagen().isBlank())
                .limit(MAXIMO_PRODUCTOS_CARRUSEL)
                .toList();

        model.addAttribute("productosCarrusel", productosCarrusel);

        return "index";
    }
}