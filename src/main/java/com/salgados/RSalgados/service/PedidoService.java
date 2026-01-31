package com.salgados.RSalgados.service;

import com.salgados.RSalgados.dto.pedidos.CriarItemPedidoDTO;
import com.salgados.RSalgados.dto.pedidos.CriarPedidoDTO;
import com.salgados.RSalgados.dto.pedidos.ItemPedidoDTO;
import com.salgados.RSalgados.dto.pedidos.PedidoDTO;

import java.util.UUID;

public interface PedidoService {
    PedidoDTO criarPedido(CriarPedidoDTO dto);
    ItemPedidoDTO adicionarItem(UUID pedidoId, CriarItemPedidoDTO dto);
    void removerItem(UUID pedidoId, UUID itemId);
    PedidoDTO marcarComoPago(UUID pedidoId);
    PedidoDTO buscarPorId(UUID pedidoId);
}

