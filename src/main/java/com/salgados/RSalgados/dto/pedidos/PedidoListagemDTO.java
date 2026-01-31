package com.salgados.RSalgados.dto.pedidos;

import com.salgados.RSalgados.domain.pedidos.StatusPedido;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoListagemDTO(
    UUID id,
    String dataPedido,  // ← String já formatada
    BigDecimal valorTotal,
    Boolean pago,
    StatusPedido statusPedido
) {}