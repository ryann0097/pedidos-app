package com.salgados.RSalgados.mapper;

import com.salgados.RSalgados.domain.pedidos.Pedido;
import com.salgados.RSalgados.dto.pedidos.PedidoDTO;
import com.salgados.RSalgados.domain.pedidos.StatusPedido;
import com.salgados.RSalgados.dto.pedidos.ItemPedidoDTO;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    public PedidoDTO toPedidoDTO(Pedido pedido) {
        StatusPedido status = pedido.getStatusPedido() != null 
            ? pedido.getStatusPedido() 
            : StatusPedido.CRIADO;

        return new PedidoDTO(
            pedido.getId(),
            pedido.getDataPedido(),
            pedido.getValorTotal(),
            pedido.getPago(),
            status,
            pedido.getItens().stream()
                .map(item -> new ItemPedidoDTO(
                    item.getId(),
                    item.getDescricao(),
                    item.getQuantidade(),
                    item.getPrecoUnitario(),
                    item.getSubtotal()
                ))
                .collect(Collectors.toList()),
            pedido.getCliente().getId()
        );
    }
}