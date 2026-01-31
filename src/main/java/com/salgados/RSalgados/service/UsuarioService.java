package com.salgados.RSalgados.service;

import com.salgados.RSalgados.domain.usuarios.Role;
import com.salgados.RSalgados.domain.usuarios.Usuario;
import com.salgados.RSalgados.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Deprecated
    @Transactional
    public Usuario registrar(Usuario usuario) {
        return criarUsuario(usuario.getEmail(), usuario.getSenha(), Role.CLIENTE);
    }

    @Deprecated
    public Usuario criarUsuarioCliente(String email, String senha) {
        return criarUsuario(email, senha, Role.CLIENTE);
    }

    @Transactional
    public Usuario criarUsuario(String email, String senha, Role role) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }
}
