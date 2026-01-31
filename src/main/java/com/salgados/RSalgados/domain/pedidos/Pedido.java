package com.salgados.RSalgados.domain.pedidos;

import com.salgados.RSalgados.domain.usuarios.Cliente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "data_pedido", nullable = false)
    private Timestamp dataPedido;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private Boolean pago = false;

    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.statusPedido == null) {
            this.statusPedido = StatusPedido.CRIADO;
        }
        if (this.dataPedido == null) {
            this.dataPedido = new java.sql.Timestamp(System.currentTimeMillis());
        }
        if (this.pago == null) {
            this.pago = false;
        }
    }
}