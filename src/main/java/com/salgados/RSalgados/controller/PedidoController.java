package com.salgados.RSalgados.controller;

import com.salgados.RSalgados.dto.pedidos.CriarItemPedidoDTO;
import com.salgados.RSalgados.dto.pedidos.CriarPedidoDTO;
import com.salgados.RSalgados.dto.pedidos.ItemPedidoDTO;
import com.salgados.RSalgados.dto.pedidos.PedidoDTO;
import com.salgados.RSalgados.dto.pedidos.PedidoListagemDTO;
import com.salgados.RSalgados.repository.ClienteRepository;
import com.salgados.RSalgados.service.PedidoServiceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Controller híbrido para gerenciamento de pedidos.
 * 
 * <p>Combina endpoints de views Thymeleaf e REST API JSON:
 * <ul>
 *   <li><b>Views (Thymeleaf):</b> listagem, criação e edição de pedidos via interface web</li>
 *   <li><b>REST API:</b> operações CRUD completas com resposta JSON para clientes JavaScript/Mobile</li>
 * </ul>
 * 
 * <p><b>Segurança:</b> Todos os endpoints validam que o usuário autenticado
 * tem permissão para acessar os pedidos solicitados.
 * 
 * <p>Rotas base: {@code /pedido} (views) e {@code /pedido/api} (REST API)
 * 
 * @author RSalgados Team
 * @version 1.0
 * @since 2026-01-31
 */
@Controller
@RequestMapping("/pedido")
@CrossOrigin
public class PedidoController {

    private final PedidoServiceImpl pedidoService;
    private final ClienteRepository clienteRepository;

    /**
     * Construtor com injeção de dependências.
     * 
     * @param pedidoService serviço para operações de pedidos
     * @param clienteRepository repositório de clientes (não utilizado atualmente)
     */
    public PedidoController(PedidoServiceImpl pedidoService, ClienteRepository clienteRepository) {
        this.pedidoService = pedidoService;
        this.clienteRepository = clienteRepository;
    }

    // ===========================
    // ENDPOINTS DE VIEWS (THYMELEAF)
    // ===========================

    /**
     * Lista todos os pedidos do cliente autenticado.
     * 
     * <p>Rota: {@code GET /pedido}
     * 
     * @param model modelo Spring MVC
     * @return view {@code pedido/lista} com lista de pedidos
     */
    @GetMapping("")
    public String listar(Model model) {
        model.addAttribute("titulo", "Pedidos");
        model.addAttribute("pedidos", pedidoService.listarPorClienteAutenticado());
        return "pedido/lista";
    }

    /**
     * Exibe o formulário para criar um novo pedido.
     * 
     * <p>Inicializa um DTO vazio com um item em branco para facilitar o preenchimento.
     * O clienteId é automaticamente preenchido com o usuário autenticado.
     * 
     * <p>Rota: {@code GET /pedido/novo}
     * 
     * @param model modelo Spring MVC
     * @return view {@code pedido/novo} em modo criação
     */
    @GetMapping("/novo")
    public String novoPedido(Model model) {
        CriarPedidoDTO dto = new CriarPedidoDTO();
        dto.setClienteId(pedidoService.getClienteAutenticado().getId());
        dto.setItens(new ArrayList<>());
        dto.getItens().add(new CriarItemPedidoDTO());

        model.addAttribute("pedidoDTO", dto);
        model.addAttribute("modoEdicao", false);
        return "pedido/novo";
    }

    /**
     * Exibe o formulário para editar um pedido existente.
     * 
     * <p>Carrega os dados do pedido e os converte para o formato de edição.
     * Valida se o pedido pertence ao cliente autenticado.
     * 
     * <p>Rota: {@code GET /pedido/{id}/editar}
     * 
     * @param id UUID do pedido a editar
     * @param model modelo Spring MVC
     * @return view {@code pedido/novo} em modo edição
     * @throws RuntimeException se pedido não encontrado ou sem permissão
     */
    @GetMapping("/{id}/editar")
    public String editarPedido(@PathVariable UUID id, Model model) {
        PedidoDTO pedido = pedidoService.buscarPorId(id);
        CriarPedidoDTO dto = new CriarPedidoDTO();

        dto.setClienteId(pedido.clienteId());

        dto.setItens(pedido.itens().stream()
            .map(item -> {
                CriarItemPedidoDTO itemDTO = new CriarItemPedidoDTO();
                itemDTO.setDescricao(item.descricao());
                itemDTO.setQuantidade(item.quantidade());
                itemDTO.setValorUnitario(item.precoUnitario());
                return itemDTO;
            })
            .collect(java.util.stream.Collectors.toList()));

        model.addAttribute("pedidoDTO", dto);
        model.addAttribute("pedidoId", id);
        model.addAttribute("modoEdicao", true);
        return "pedido/novo";
    }

    /**
     * Processa a criação de um novo pedido via formulário.
     * 
     * <p>O clienteId é automaticamente sobrescrito com o usuário autenticado
     * para garantir segurança (evita manipulação no formulário).
     * 
     * <p>Rota: {@code POST /pedido}
     * 
     * @param dto dados do pedido enviados pelo formulário
     * @return redirect para {@code /pedido} após criação
     * @throws RuntimeException se cliente não encontrado
     */
    @PostMapping
    public String criarPedido(@ModelAttribute CriarPedidoDTO dto) {
        dto.setClienteId(pedidoService.getClienteAutenticado().getId());
        pedidoService.criarPedido(dto);
        return "redirect:/pedido";
    }

    // ===========================
    // REST API ENDPOINTS (JSON)
    // ===========================

    /**
     * Busca um pedido pelo ID.
     * 
     * <p>Rota: {@code GET /pedido/api/{id}}
     * 
     * @param id UUID do pedido
     * @return pedido completo em formato JSON
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    /**
     * Marca um pedido como pago.
     * 
     * <p>Rota: {@code PUT /pedido/api/{id}/pagar}
     * 
     * @param id UUID do pedido
     * @return pedido atualizado em JSON
     */
    @PutMapping("/api/{id}/pagar")
    @ResponseBody
    public ResponseEntity<PedidoDTO> marcarComoPago(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.marcarComoPago(id));
    }

    /**
     * Adiciona um item a um pedido existente.
     * 
     * <p>Rota: {@code POST /pedido/api/{id}/itens}
     * 
     * @param id UUID do pedido
     * @param dto dados do item a ser criado
     * @return item criado com status 201 (Created)
     */
    @PostMapping("/api/{id}/itens")
    @ResponseBody
    public ResponseEntity<ItemPedidoDTO> adicionarItem(
        @PathVariable UUID id,
        @RequestBody CriarItemPedidoDTO dto
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(pedidoService.adicionarItem(id, dto));
    }

    /**
     * Atualiza um item específico de um pedido.
     * 
     * <p>Rota: {@code PUT /pedido/api/{pedidoId}/itens/{itemId}}
     * 
     * @param pedidoId UUID do pedido
     * @param itemId UUID do item
     * @param dto novos dados do item
     * @return item atualizado ou 204 se não encontrado
     */
    @PutMapping("/api/{pedidoId}/itens/{itemId}")
    @ResponseBody
    public ResponseEntity<ItemPedidoDTO> atualizarItem(
            @PathVariable UUID pedidoId,
            @PathVariable UUID itemId,
            @RequestBody CriarItemPedidoDTO dto
    ) {
        ItemPedidoDTO updated = pedidoService.atualizarItem(pedidoId, itemId, dto);
        if (updated == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Remove um item de um pedido.
     * 
     * <p>Rota: {@code DELETE /pedido/api/{pedidoId}/itens/{itemId}}
     * 
     * @param pedidoId UUID do pedido
     * @param itemId UUID do item
     * @return status 204 (No Content)
     */
    @DeleteMapping("/api/{pedidoId}/itens/{itemId}")
    @ResponseBody
    public ResponseEntity<Void> removerItem(
            @PathVariable UUID pedidoId,
            @PathVariable UUID itemId
    ) {
        pedidoService.removerItem(pedidoId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista pedidos do cliente autenticado (API).
     * 
     * <p>Rota: {@code GET /pedido/api}
     * 
     * @return lista resumida de pedidos em JSON
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<java.util.List<PedidoListagemDTO>> listarPorCliente() {
        return ResponseEntity.ok(pedidoService.listarPorClienteAutenticado());
    }

    /**
     * Lista todos os pedidos visíveis ao cliente autenticado.
     * 
     * <p>Atualmente retorna os mesmos dados que o endpoint padrão.
     * 
     * <p>Rota: {@code GET /pedido/api/todos}
     * 
     * @return lista de pedidos em JSON
     */
    @GetMapping("/api/todos")
    @ResponseBody
    public ResponseEntity<java.util.List<PedidoListagemDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarPorClienteAutenticado());
    }

    /**
     * Atualiza um pedido existente.
     * 
     * <p>Rota: {@code PUT /pedido/api/{id}}
     * 
     * @param id UUID do pedido
     * @param dto novos dados do pedido
     * @return status 204 (No Content)
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> atualizarPedido(
            @PathVariable UUID id,
            @RequestBody CriarPedidoDTO dto
    ) {
        pedidoService.atualizarPedido(id, dto);
        return ResponseEntity.noContent().build();
    }
}
