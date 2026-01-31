package com.salgados.RSalgados.dto.pedidos;

import com.salgados.RSalgados.domain.pedidos.StatusPedido;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public record PedidoDTO(
    UUID id,
    Timestamp dataPedido,
    BigDecimal valorTotal,
    Boolean pago,
    StatusPedido statusPedido,
    List<ItemPedidoDTO> itens,
    UUID clienteId  // ← ADICIONE ISSO
) {}