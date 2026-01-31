package com.salgados.RSalgados.dto.pedidos;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoDTO(
        UUID id,
        String descricao,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {}