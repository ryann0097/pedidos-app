package com.salgados.RSalgados.dto.pedidos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CriarItemPedidoDTO {
    private String descricao;
    private Integer quantidade;
    private BigDecimal valorUnitario;
}
