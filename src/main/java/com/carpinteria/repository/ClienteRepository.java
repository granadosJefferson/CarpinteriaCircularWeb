package com.carpinteria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpinteria.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByNombreAsc();

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
}