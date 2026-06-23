package com.carpinteria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.carpinteria.model.Producto;
import com.carpinteria.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria);
    }

   public Producto guardar(Producto producto) {
    validarProducto(producto);

    if (producto.getActivo() == null) {
        producto.setActivo(true);
    }

    return productoRepository.save(producto);
}

public Producto actualizar(Long id, Producto productoActualizado) {
    Producto productoExistente = productoRepository.findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException("El producto no existe"));

    validarProducto(productoActualizado);

    productoExistente.setNombre(productoActualizado.getNombre());
    productoExistente.setDescripcion(productoActualizado.getDescripcion());
    productoExistente.setPrecio(productoActualizado.getPrecio());
    productoExistente.setCantidad(productoActualizado.getCantidad());
    productoExistente.setCategoria(productoActualizado.getCategoria());
    productoExistente.setImagen(productoActualizado.getImagen());

    
    return productoRepository.save(productoExistente);
}

    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("El producto no existe");
        }

        productoRepository.deleteById(id);
    }

    public Producto cambiarEstado(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("El producto no existe"));

        producto.setActivo(!Boolean.TRUE.equals(producto.getActivo()));

        return productoRepository.save(producto);
    }

   private void validarProducto(Producto producto) {

    if (producto.getNombre() == null
            || producto.getNombre().isBlank()) {
        throw new IllegalArgumentException(
                "El nombre del producto es obligatorio");
    }

    if (producto.getPrecio() == null
            || producto.getPrecio().signum() < 0) {
        throw new IllegalArgumentException(
                "El precio debe ser mayor o igual a cero");
    }

    if (producto.getCantidad() == null
            || producto.getCantidad() < 0) {
        throw new IllegalArgumentException(
                "La cantidad debe ser mayor o igual a cero");
    }

    producto.setNombre(producto.getNombre().trim());

    if (producto.getDescripcion() != null) {
        producto.setDescripcion(producto.getDescripcion().trim());
    }

    if (producto.getCategoria() != null) {
        producto.setCategoria(producto.getCategoria().trim());
    }
}
}