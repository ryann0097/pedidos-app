package com.salgados.RSalgados.controller;

import com.salgados.RSalgados.dto.usuarios.RegistroClienteForm;
import com.salgados.RSalgados.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller responsável pela autenticação e registro de usuários.
 * 
 * <p>Gerencia as páginas e ações relacionadas a:
 * <ul>
 *   <li>Login de usuários existentes</li>
 *   <li>Registro de novos clientes</li>
 * </ul>
 * 
 * <p>Rotas base: {@code /auth}
 * 
 * @author RSalgados Team
 * @version 1.0
 * @since 2026-01-31
 */
@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private final ClienteService clienteService;

    /**
     * Construtor com injeção de dependências.
     * 
     * @param clienteService serviço para operações de clientes
     */
    public AuthController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Exibe a página de login.
     * 
     * <p>A autenticação é gerenciada pelo Spring Security.
     * 
     * @return nome da view {@code auth/login}
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * Exibe a página de registro de novos clientes.
     * 
     * <p>Adiciona um formulário vazio ao modelo para preenchimento.
     * 
     * @param model modelo Spring MVC
     * @return nome da view {@code auth/register}
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registro", new RegistroClienteForm());
        return "auth/register";
    }

    /**
     * Processa o registro de um novo cliente.
     * 
     * <p>Validações e criação de usuário/cliente são delegadas ao {@link ClienteService}.
     * Em caso de sucesso, redireciona para o login com mensagem de sucesso.
     * 
     * @param registro formulário com dados do novo cliente (email, senha, nome, telefone, endereço)
     * @return redirect para {@code /auth/login?success}
     * @throws IllegalArgumentException se email já estiver cadastrado
     */
    @PostMapping("/register")
    public String register(@ModelAttribute("registro") RegistroClienteForm registro) {
        clienteService.cadastrarCliente(
                registro.getEmail(),
                registro.getSenha(),
                registro.getNome(),
                registro.getTelefone(),
                registro.getEndereco()
        );
        return "redirect:/auth/login?success";
    }
}