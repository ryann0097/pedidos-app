package com.salgados.RSalgados.dto.pedidos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CriarPedidoDTO {
    private UUID clienteId;
    private List<CriarItemPedidoDTO> itens;
}
