package com.carpinteria.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpinteria.model.Producto;
import com.carpinteria.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAllByOrderByNombreAsc();
    }

    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc();
    }

    public Producto buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del producto es obligatorio"
            );
        }

        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe"
                        )
                );
    }

    public List<Producto> buscarPorNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return listarTodos();
        }

        return productoRepository
                .findByNombreContainingIgnoreCase(
                        nombre.trim()
                );
    }

    public List<Producto> buscarPorCategoria(String categoria) {

        if (categoria == null || categoria.isBlank()) {
            return listarTodos();
        }

        return productoRepository
                .findByCategoriaIgnoreCase(
                        categoria.trim()
                );
    }

    // Validates and normalizes product data before storing it.
    @Transactional
    public Producto guardar(Producto producto) {

        validarProducto(producto);
        normalizarDatos(producto);

        producto.setId(null);

        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }

        return productoRepository.save(producto);
    }

    // Updates product information while preserving its current status.
    @Transactional
    public Producto actualizar(
            Long id,
            Producto productoActualizado) {

        Producto productoExistente = buscarPorId(id);

        validarProducto(productoActualizado);
        normalizarDatos(productoActualizado);

        productoExistente.setNombre(
                productoActualizado.getNombre()
        );

        productoExistente.setDescripcion(
                productoActualizado.getDescripcion()
        );

        productoExistente.setPrecio(
                productoActualizado.getPrecio()
        );

        productoExistente.setCantidad(
                productoActualizado.getCantidad()
        );

        productoExistente.setCategoria(
                productoActualizado.getCategoria()
        );

        if (productoActualizado.getImagen() != null
                && !productoActualizado.getImagen().isBlank()) {

            productoExistente.setImagen(
                    productoActualizado.getImagen()
            );
        }

        return productoRepository.save(productoExistente);
    }

    // Toggles product availability without deleting its sales history.
    @Transactional
    public Producto cambiarEstado(Long id) {

        Producto producto = buscarPorId(id);

        producto.setActivo(
                !Boolean.TRUE.equals(producto.getActivo())
        );

        return productoRepository.save(producto);
    }

    private void validarProducto(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "Los datos del producto son inválidos"
            );
        }

        if (producto.getNombre() == null
                || producto.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio"
            );
        }

        if (producto.getPrecio() == null
                || producto.getPrecio().signum() < 0) {

            throw new IllegalArgumentException(
                    "El precio debe ser mayor o igual a cero"
            );
        }

        if (producto.getCantidad() == null
                || producto.getCantidad() < 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor o igual a cero"
            );
        }
    }

    private void normalizarDatos(Producto producto) {

        producto.setNombre(
                producto.getNombre().trim()
        );

        producto.setDescripcion(
                normalizarTexto(producto.getDescripcion())
        );

        producto.setCategoria(
                normalizarTexto(producto.getCategoria())
        );

        producto.setImagen(
                normalizarTexto(producto.getImagen())
        );
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}