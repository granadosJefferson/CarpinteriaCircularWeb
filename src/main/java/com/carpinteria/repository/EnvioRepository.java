package com.carpinteria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpinteria.model.Envio;
import com.carpinteria.model.EstadoEnvio;

public interface EnvioRepository
        extends JpaRepository<Envio, Long> {

    List<Envio> findAllByOrderByFechaRegistroDesc();

    List<Envio> findByEstadoOrderByFechaRegistroDesc(
            EstadoEnvio estado
    );

    Optional<Envio> findByPedidoId(Long pedidoId);

    boolean existsByPedidoId(Long pedidoId);

    long countByEstado(EstadoEnvio estado);
}