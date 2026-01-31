package com.salgados.RSalgados.repository;

import com.salgados.RSalgados.domain.pedidos.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import com.salgados.RSalgados.domain.usuarios.Cliente;


public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    List<Pedido> getByCliente(Cliente cliente);
}
