package com.salgados.RSalgados.service;

import com.salgados.RSalgados.domain.usuarios.Cliente;
import com.salgados.RSalgados.domain.usuarios.Role;
import com.salgados.RSalgados.domain.usuarios.Usuario;
import com.salgados.RSalgados.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;

    public ClienteService(ClienteRepository clienteRepository, UsuarioService usuarioService) {
        this.clienteRepository = clienteRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public Cliente cadastrarCliente(
            String email,
            String senha,
            String nome,
            String telefone,
            String endereco
    ) {
        Usuario usuario = usuarioService.criarUsuario(email, senha, Role.CLIENTE);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setEndereco(endereco);

        return clienteRepository.save(cliente);
    }
}
