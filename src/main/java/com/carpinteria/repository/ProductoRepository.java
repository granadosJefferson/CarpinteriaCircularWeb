package com.carpinteria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpinteria.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

     long countByActivoTrue();

    List<Producto> findByActivoTrue();

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByCategoriaIgnoreCase(String categoria);
}