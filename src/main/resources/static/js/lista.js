let pedidoAtual = null;
let modoEdicao = false;
let alterado = false;

function carregarDetalhes(btn) {
    const id = btn.dataset.id;
    modoEdicao = btn.dataset.pago === "false" &&
                    (btn.dataset.status === "CRIADO" || btn.dataset.status === "EM_PRODUCAO");

    fetch(`/pedido/api/${id}`)
        .then(r => r.json())
        .then(pedido => {
            pedidoAtual = pedido;
            renderizar();
        });
}

function renderizar() {
    const c = document.getElementById("detalhesContent");
    const btnSalvar = document.getElementById("btnSalvar");

    btnSalvar.classList.toggle("d-none", !modoEdicao || !alterado);

    let linhas = pedidoAtual.itens.map((item, i) => `
        <tr>
            <td>
                ${modoEdicao
                    ? `<input class="form-control form-control-sm"
                                value="${item.descricao}"
                                onchange="update(${i}, 'descricao', this.value)">`
                    : item.descricao}
            </td>
            <td>
                ${modoEdicao
                    ? `<input type="number" class="form-control form-control-sm text-center"
                                value="${item.quantidade}"
                                onchange="update(${i}, 'quantidade', this.value)">`
                    : item.quantidade}
            </td>
            <td>
                ${modoEdicao
                    ? `<input type="number" step="0.01"
                                class="form-control form-control-sm text-end"
                                value="${item.precoUnitario}"
                                onchange="update(${i}, 'precoUnitario', this.value)">`
                    : `R$ ${item.precoUnitario.toFixed(2)}`}
            </td>
            <td class="text-end">R$ ${(item.quantidade * item.precoUnitario).toFixed(2)}</td>
        </tr>
    `).join("");

    const total = pedidoAtual.itens.reduce((s, i) => s + i.quantidade * i.precoUnitario, 0);

    c.innerHTML = `
        ${modoEdicao ? '<p class="text-muted"><i class="bi bi-pencil"></i> Modo edição ativo</p>' : ''}
        <h5 class="text-end text-success">Total: R$ ${total.toFixed(2)}</h5>
        <table class="table table-sm table-hover">
            <thead>
            <tr>
                <th>Descrição</th>
                <th class="text-center">Qtd</th>
                <th class="text-end">Unitário</th>
                <th class="text-end">Subtotal</th>
            </tr>
            </thead>
            <tbody>${linhas}</tbody>
        </table>
    `;
}

function update(index, campo, valor) {
    alterado = true;
    pedidoAtual.itens[index][campo] =
        campo === "descricao" ? valor : Number(valor);
    renderizar();
}

function salvarAlteracoes() {
    const payload = {
        clienteId: pedidoAtual.clienteId,
        itens: pedidoAtual.itens.map(item => ({
            descricao: item.descricao,
            quantidade: item.quantidade,
            // Certifique-se de usar 'valorUnitario' para bater com o CriarItemPedidoDTO
            valorUnitario: item.precoUnitario 
        }))
    };

    fetch(`/pedido/api/${pedidoAtual.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    }).then(response => {
        if (response.ok) {
            location.reload();
        } else {
            alert("Erro ao salvar");
        }
    });
}

function marcarComoPago(btn) {
    fetch(`/pedido/api/${btn.dataset.id}/pagar`, {method: "PUT"})
        .then(() => location.reload());
}

function deletarPedido(btn) {
    fetch(`/pedido/api/${btn.dataset.id}`, {method: "DELETE"})
        .then(() => location.reload());
}