package com.salgados.RSalgados.domain.usuarios;

import com.salgados.RSalgados.domain.pedidos.Pedido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String nome;

    private String telefone;

    private String endereco;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "cliente")
    private List<Pedido>pedidos;
}
