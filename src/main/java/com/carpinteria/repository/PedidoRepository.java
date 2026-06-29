package com.carpinteria.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;

public interface PedidoRepository
        extends JpaRepository<Pedido, Long> {

    /**
     * Returns all orders sorted from newest to oldest.
     */
    List<Pedido> findAllByOrderByFechaDesc();

    /**
     * Returns the ten most recent orders.
     */
    List<Pedido> findTop10ByOrderByFechaDesc();

    /**
     * Counts orders that match the specified status.
     */
    long countByEstado(
            EstadoPedido estado
    );

    /**
     * Filters orders by status and sorts them by date descending.
     */
    List<Pedido> findByEstadoOrderByFechaDesc(
            EstadoPedido estado
    );

    /**
     * Sums order totals by status and returns zero when no records exist.
     */
    @Query("""
        SELECT COALESCE(SUM(p.total), 0)
        FROM Pedido p
        WHERE p.estado = :estado
    """)
    BigDecimal sumarTotalPorEstado(
            @Param("estado")
            EstadoPedido estado
    );
}