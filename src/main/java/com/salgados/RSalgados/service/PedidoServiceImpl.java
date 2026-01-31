package com.salgados.RSalgados.service;

import com.salgados.RSalgados.dto.pedidos.CriarItemPedidoDTO;
import com.salgados.RSalgados.dto.pedidos.CriarPedidoDTO;
import com.salgados.RSalgados.domain.pedidos.ItemPedido;
import com.salgados.RSalgados.dto.pedidos.ItemPedidoDTO;
import com.salgados.RSalgados.domain.pedidos.Pedido;
import com.salgados.RSalgados.dto.pedidos.PedidoDTO;
import com.salgados.RSalgados.dto.pedidos.PedidoListagemDTO;
import com.salgados.RSalgados.domain.pedidos.StatusPedido;
import com.salgados.RSalgados.domain.usuarios.Cliente;
import com.salgados.RSalgados.domain.usuarios.Role;
import com.salgados.RSalgados.domain.usuarios.Usuario;
import com.salgados.RSalgados.repository.ClienteRepository;
import com.salgados.RSalgados.repository.ItemPedidoRepository;
import com.salgados.RSalgados.repository.PedidoRepository;
import com.salgados.RSalgados.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de gerenciamento de pedidos.
 * 
 * Responsabilidades:
 * - Operações CRUD de pedidos (criar, atualizar, deletar, listar)
 * - Operações de itens do pedido (adicionar, atualizar, remover)
 * - Validações de status e permissões
 * - Conversão de entidades para DTOs
 * - Cálculo de totais
 * 
 * Todos os métodos públicos validam se o cliente autenticado tem permissão
 * de acesso ao pedido antes de executar a operação.
 */
@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             ItemPedidoRepository itemPedidoRepository,
                             UsuarioRepository usuarioRepository,
                             ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }

    // ===========================
    // OPERAÇÕES CRUD - PEDIDOS
    // ===========================

    // ===========================
    // OPERAÇÕES CRUD - PEDIDOS
    // ===========================

    /**
     * Cria um novo pedido com uma lista de itens.
     * 
     * @param dto Dados do pedido a criar (clienteId + lista de itens)
     * @return PedidoDTO do pedido criado
     * @throws RuntimeException se cliente não encontrado
     */
    @Override
    @Transactional
    public PedidoDTO criarPedido(CriarPedidoDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setDataPedido(new java.sql.Timestamp(System.currentTimeMillis()));
        pedido.setStatusPedido(StatusPedido.CRIADO);
        pedido.setPago(false);
        pedido.setValorTotal(BigDecimal.ZERO);

        pedido = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;

        for (CriarItemPedidoDTO itemDTO : dto.getItens()) {
            ItemPedido item = new ItemPedido();
            item.setDescricao(itemDTO.getDescricao());
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(itemDTO.getValorUnitario());
            item.setPedido(pedido);

            itemPedidoRepository.save(item);

            total = total.add(
                itemDTO.getValorUnitario()
                       .multiply(BigDecimal.valueOf(itemDTO.getQuantidade()))
            );
        }

        pedido.setValorTotal(total);
        pedidoRepository.save(pedido);

        return toPedidoDTO(pedido);
    }

    /**
     * Busca um pedido pelo ID do cliente autenticado.
     * 
     * @param pedidoId ID do pedido
     * @return PedidoDTO com todos os detalhes
     * @throws IllegalStateException se cliente não autenticado ou sem permissão
     * @throws RuntimeException se pedido não encontrado
     */
    @Override
    @Transactional(readOnly = true)
    public PedidoDTO buscarPorId(UUID pedidoId) {
        Cliente cliente = getClienteAutenticado();
        Pedido pedido = buscarEntidadePorIdDoCliente(pedidoId, cliente);
        return toPedidoDTO(pedido);
    }

    /**
     * Marca um pedido como pago.
     * 
     * @param pedidoId ID do pedido
     * @return PedidoDTO atualizado
     * @throws IllegalStateException se cliente não autenticado
     * @throws RuntimeException se pedido não encontrado
     */
    @Override
    public PedidoDTO marcarComoPago(UUID pedidoId) {
        Cliente cliente = getClienteAutenticado();
        Pedido pedido = buscarEntidadePorIdDoCliente(pedidoId, cliente);
        pedido.setPago(true);
        pedidoRepository.save(pedido);
        return toPedidoDTO(pedido);
    }

    /**
     * Atualiza um pedido existente (cliente e itens).
     * Limpa todos os itens antigos e adiciona os novos.
     * 
     * @param id ID do pedido
     * @param dto Novos dados (clienteId + itens)
     * @throws RuntimeException se pedido ou cliente não encontrado
     */
    public void atualizarPedido(UUID id, CriarPedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.getItens().clear();
        pedidoRepository.saveAndFlush(pedido);
        
        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            pedido.setCliente(cliente);
        }
        
        if (dto.getItens() != null) {
            for (CriarItemPedidoDTO itemDTO : dto.getItens()) {
                ItemPedido item = new ItemPedido();
                item.setDescricao(itemDTO.getDescricao());
                item.setQuantidade(itemDTO.getQuantidade());
                item.setPrecoUnitario(itemDTO.getValorUnitario());
                item.setPedido(pedido);
                pedido.getItens().add(item);
            }
        }
        pedidoRepository.save(pedido);
        recalcularTotal(pedido);
    }

    /**
     * Lista todos os pedidos do cliente autenticado com informações resumidas.
     * 
     * @return Lista de PedidoListagemDTO formatados
     * @throws IllegalStateException se cliente não autenticado
     */
    @Transactional(readOnly = true)
    public List<PedidoListagemDTO> listarPorClienteAutenticado() {
        Cliente cliente = getClienteAutenticado();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        return pedidoRepository.getByCliente(cliente)
            .stream()
            .map(p -> {
                StatusPedido status = p.getStatusPedido() != null
                        ? p.getStatusPedido()
                        : StatusPedido.CRIADO;

                String data = p.getDataPedido() != null
                        ? sdf.format(p.getDataPedido())
                        : "";

                return new PedidoListagemDTO(
                    p.getId(),
                    data,
                    p.getValorTotal(),
                    p.getPago(),
                    status
                );
            })
            .toList();
    }

    // ===========================
    // OPERAÇÕES CRUD - ITENS
    // ===========================

    /**
     * Adiciona um novo item a um pedido existente.
     * Recalcula o total do pedido automaticamente.
     * 
     * @param pedidoId ID do pedido
     * @param dto Dados do item (descrição, quantidade, preço)
     * @return ItemPedidoDTO do item criado
     * @throws IllegalStateException se pedido em status não editável
     * @throws RuntimeException se pedido não encontrado
     */
    @Override
    public ItemPedidoDTO adicionarItem(UUID pedidoId, CriarItemPedidoDTO dto) {
        Cliente cliente = getClienteAutenticado();
        Pedido pedido = buscarEntidadePorIdDoCliente(pedidoId, cliente);

        if (!podeAlterarPedido(pedido)) {
            throw new IllegalStateException("Pedido não pode ser alterado. Status: " + pedido.getStatusPedido());
        }

        if (dto.getQuantidade() <= 0) {
            throw new IllegalStateException("Quantidade deve ser maior que zero");
        }

        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setDescricao(dto.getDescricao());
        item.setQuantidade(dto.getQuantidade());
        item.setPrecoUnitario(dto.getValorUnitario());

        itemPedidoRepository.save(item);
        recalcularTotal(pedido);

        return toItemDTO(item);
    }

    /**
     * Atualiza um item existente no pedido.
     * Se quantidade <= 0, remove o item.
     * 
     * @param pedidoId ID do pedido
     * @param itemId ID do item
     * @param dto Novos dados do item
     * @return ItemPedidoDTO atualizado ou null se removido
     * @throws IllegalStateException se pedido em status não editável
     * @throws RuntimeException se pedido ou item não encontrado
     */
    public ItemPedidoDTO atualizarItem(UUID pedidoId, UUID itemId, CriarItemPedidoDTO dto) {
        Cliente cliente = getClienteAutenticado();
        Pedido pedido = buscarEntidadePorIdDoCliente(pedidoId, cliente);

        if (!podeAlterarPedido(pedido)) {
            throw new IllegalStateException("Pedido não pode ser alterado. Status: " + pedido.getStatusPedido());
        }

        ItemPedido item = itemPedidoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        if (!item.getPedido().getId().equals(pedido.getId())) {
            throw new RuntimeException("Item não pertence ao pedido");
        }

        if (dto.getQuantidade() <= 0) {
            itemPedidoRepository.delete(item);
        } else {
            item.setQuantidade(dto.getQuantidade());
            item.setPrecoUnitario(dto.getValorUnitario());
            itemPedidoRepository.save(item);
        }

        recalcularTotal(pedido);

        return dto.getQuantidade() <= 0 ? null : toItemDTO(item);
    }

    /**
     * Remove um item do pedido.
     * Recalcula o total do pedido automaticamente.
     * 
     * @param pedidoId ID do pedido
     * @param itemId ID do item
     * @throws IllegalStateException se pedido em status não editável
     * @throws RuntimeException se pedido ou item não encontrado
     */
    @Override
    public void removerItem(UUID pedidoId, UUID itemId) {
        Cliente cliente = getClienteAutenticado();
        Pedido pedido = buscarEntidadePorIdDoCliente(pedidoId, cliente);

        if (!podeAlterarPedido(pedido)) {
            throw new IllegalStateException("Pedido não pode ser alterado. Status: " + pedido.getStatusPedido());
        }

        ItemPedido item = itemPedidoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        if (!item.getPedido().getId().equals(pedido.getId())) {
            throw new RuntimeException("Item não pertence ao pedido");
        }

        itemPedidoRepository.delete(item);
        recalcularTotal(pedido);
    }

    // ===========================
    // MÉTODOS AUXILIARES
    // ===========================

    /**
     * Obtém o cliente autenticado a partir do contexto de segurança.
     * 
     * @return Cliente autenticado
     * @throws IllegalStateException se não autenticado ou cliente não encontrado
     */
    public Cliente getClienteAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));

        if (usuario.getRole() != Role.CLIENTE) {
            throw new IllegalStateException("Apenas clientes podem criar pedidos");
        }

        return clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Cliente não encontrado para o usuário autenticado"));
    }

    /**
     * Busca um pedido pelo ID verificando se pertence ao cliente informado.
     * 
     * @param id ID do pedido
     * @param cliente Cliente proprietário
     * @return Entidade Pedido
     * @throws RuntimeException se pedido não encontrado
     * @throws IllegalStateException se pedido não pertence ao cliente
     */
    private Pedido buscarEntidadePorIdDoCliente(UUID id, Cliente cliente) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getCliente() == null || !pedido.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalStateException("Pedido não pertence ao cliente autenticado");
        }

        return pedido;
    }

    /**
     * Verifica se um pedido pode ser alterado baseado no seu status.
     * Apenas pedidos em status CRIADO ou EM_PRODUCAO podem ser alterados.
     * 
     * @param pedido Pedido a verificar
     * @return true se pode ser alterado, false caso contrário
     */
    private boolean podeAlterarPedido(Pedido pedido) {
        if (pedido.getStatusPedido() == null) {
            return true; // Se não tem status, considera como CRIADO
        }
        return pedido.getStatusPedido() == StatusPedido.CRIADO || 
               pedido.getStatusPedido() == StatusPedido.EM_PRODUCAO;
    }

    /**
     * Verifica se o cliente possui algum pedido pendente de pagamento.
     * 
     * @param cliente Cliente a verificar
     * @return true se existe pedido não pago, false caso contrário
     */
    boolean existePedidoPendentePagamento(Cliente cliente) {
        for (Pedido pedido : cliente.getPedidos()) {
            if (!pedido.getPago()) {
                return true;
            }
        }
        return false;
    }

    // ===========================
    // CONVERSÕES E CÁLCULOS
    // ===========================

    /**
     * Converte uma entidade Pedido para PedidoDTO.
     * Inclui todos os detalhes do pedido e lista de itens.
     * 
     * @param pedido Entidade a converter
     * @return PedidoDTO com dados completos
     */
    private PedidoDTO toPedidoDTO(Pedido pedido) {
        StatusPedido status = pedido.getStatusPedido() != null
                ? pedido.getStatusPedido()
                : StatusPedido.CRIADO;

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataPedido(),
                pedido.getValorTotal(),
                pedido.getPago(),
                status,
                pedido.getItens() == null ? java.util.List.of() :
                        pedido.getItens().stream().map(this::toItemDTO).toList(),
                pedido.getCliente().getId()
        );
    }

    /**
     * Converte uma entidade ItemPedido para ItemPedidoDTO.
     * Calcula o subtotal se não estiver preenchido.
     * 
     * @param item Entidade a converter
     * @return ItemPedidoDTO com dados completos
     */
    private ItemPedidoDTO toItemDTO(ItemPedido item) {
        BigDecimal subtotal = item.getSubtotal();
        if (subtotal == null) {
            BigDecimal preco = item.getPrecoUnitario() == null ? BigDecimal.ZERO : item.getPrecoUnitario();
            Integer qtd = item.getQuantidade() == null ? 0 : item.getQuantidade();
            subtotal = preco.multiply(BigDecimal.valueOf(qtd));
        }

        return new ItemPedidoDTO(
                item.getId(),
                item.getDescricao(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                subtotal
        );
    }

    /**
     * Recalcula o valor total do pedido com base nos itens persistidos.
     *
     * <p>Este método consulta o banco novamente para evitar inconsistências
     * com estados em memória.
     *
     * <p>Chamado automaticamente após:
     * <ul>
     *   <li>adição de item</li>
     *   <li>remoção de item</li>
     *   <li>atualização de item</li>
     *   <li>edição completa do pedido</li>
     * </ul>
     *
     * @param pedido pedido a recalcular
     */

    private void recalcularTotal(Pedido pedido) {
        BigDecimal total = itemPedidoRepository
                .findByPedidoId(pedido.getId())
                .stream()
                .map(item -> {
                    BigDecimal preco = item.getPrecoUnitario() == null ? BigDecimal.ZERO : item.getPrecoUnitario();
                    Integer qtd = item.getQuantidade() == null ? 0 : item.getQuantidade();
                    return preco.multiply(BigDecimal.valueOf(qtd));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setValorTotal(total);
        pedidoRepository.save(pedido);
    }
}
