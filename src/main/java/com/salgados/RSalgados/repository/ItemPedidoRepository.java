package com.salgados.RSalgados.repository;

import com.salgados.RSalgados.domain.pedidos.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, UUID> {
    List<ItemPedido> findByPedidoId(UUID id);
}
