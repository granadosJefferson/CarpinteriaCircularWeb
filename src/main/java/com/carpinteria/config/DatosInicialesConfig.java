package com.carpinteria.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.carpinteria.model.Producto;
import com.carpinteria.service.ProductoService;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarProductosIniciales(
            ProductoService productoService) {

        return args -> {

            registrarOActualizar(
                    productoService,
                    "Mecedora artesanal",
                    "Mecedora de madera elaborada artesanalmente, ideal para sala, terraza o corredor.",
                    "70000",
                    3,
                    "Sillas",
                    "mecedora.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Mueble para televisión",
                    "Mueble de madera con espacio para televisión, decoración y almacenamiento.",
                    "95000",
                    2,
                    "Muebles",
                    "muebleTV.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Puerta para baño",
                    "Puerta de madera fabricada a medida, con acabado resistente para interiores.",
                    "65000",
                    2,
                    "Puertas",
                    "puertaBaño.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Alacena de madera",
                    "Alacena artesanal con espacios de almacenamiento para cocina o comedor.",
                    "110000",
                    2,
                    "Cocina",
                    "Alasena.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Juego de sillas artesanales",
                    "Conjunto de sillas de madera con diseño tradicional y acabado artesanal.",
                    "90000",
                    2,
                    "Sillas",
                    "sillas.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Gabetero de cocina",
                    "Gabetero de madera para organizar utensilios y productos de cocina.",
                    "85000",
                    2,
                    "Cocina",
                    "gabeteroCocina.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Mueble grande para baño",
                    "Mueble de baño con almacenamiento amplio y acabado protector.",
                    "120000",
                    1,
                    "Baño",
                    "muebleBañoGrande.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Lámpara luna artesanal",
                    "Lámpara decorativa de madera con diseño inspirado en la luna.",
                    "28000",
                    4,
                    "Iluminación",
                    "lamparaLunaArtesanal.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Timón de barco decorativo",
                    "Pieza decorativa de madera con forma de timón de barco.",
                    "95000",
                    3,
                    "Decoración",
                    "timonBarcoColeccion.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Set de cucharas de madera",
                    "Conjunto de cucharas artesanales para cocina y decoración.",
                    "10000",
                    8,
                    "Utensilios",
                    "cucharasVarias.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Tabla grande para carnes",
                    "Tabla artesanal grande y resistente para cortar y servir carnes.",
                    "25000",
                    5,
                    "Tablas artesanales",
                    "tablaCarnesGrandeArtesanal.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Tabla grande para picar",
                    "Tabla artesanal de madera para preparación de alimentos.",
                    "22000",
                    5,
                    "Tablas artesanales",
                    "tablaPicarArtesanalGrande.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Tabla mediana para quesos",
                    "Tabla artesanal mediana para servir quesos, frutas y bocadillos.",
                    "12000",
                    6,
                    "Tablas artesanales",
                    "tablaQuesosPequeña.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Sobre de mesa grande",
                    "Superficie de madera sólida para fabricar o renovar una mesa.",
                    "215000",
                    2,
                    "Mesas",
                    "sobreDeMesaGrande.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Tabla grande para quesos",
                    "Tabla artesanal grande para presentar quesos y alimentos.",
                    "20000",
                    5,
                    "Tablas artesanales",
                    "tablaQuesosGrande.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Mesa artesanal",
                    "Mesa de madera elaborada artesanalmente para comedor o sala.",
                    "85000",
                    2,
                    "Mesas",
                    "mesaArtesanal.jpeg"
            );

            registrarOActualizar(
                    productoService,
                    "Centro de mesa artesanal",
                    "Pieza decorativa de madera para mesas de sala o comedor.",
                    "120000",
                    5,
                    "Decoración",
                    "centroDeMesa.jpeg"
            );

        };
    }

    private void registrarOActualizar(
            ProductoService productoService,
            String nombre,
            String descripcion,
            String precio,
            Integer cantidad,
            String categoria,
            String imagen) {

        Producto productoExistente = productoService.listarTodos()
                .stream()
                .filter(producto ->
                        producto.getNombre() != null
                                && producto.getNombre()
                                        .equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);

        if (productoExistente != null) {

            productoExistente.setDescripcion(descripcion);
            productoExistente.setPrecio(new BigDecimal(precio));
            productoExistente.setCategoria(categoria);
            productoExistente.setImagen(imagen);
            productoExistente.setActivo(true);

            productoService.guardar(productoExistente);
            return;
        }

        Producto productoNuevo = new Producto();

        productoNuevo.setNombre(nombre);
        productoNuevo.setDescripcion(descripcion);
        productoNuevo.setPrecio(new BigDecimal(precio));
        productoNuevo.setCantidad(cantidad);
        productoNuevo.setCategoria(categoria);
        productoNuevo.setImagen(imagen);
        productoNuevo.setActivo(true);

        productoService.guardar(productoNuevo);
    }
}