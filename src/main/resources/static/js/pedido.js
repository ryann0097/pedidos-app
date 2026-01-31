let itemIndex = 1; // Começa em 1 porque já existe o item 0

function criarItemHtml(index) {
    return `
    <div class="card mb-3 item-pedido" data-index="${index}">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <strong>Item ${index + 1}</strong>
                <button type="button" class="btn btn-sm btn-outline-danger" onclick="removerItem(this)">Remover</button>
            </div>
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Descrição</label>
                    <input type="text" class="form-control item-descricao" name="itens[${index}].descricao" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label">Quantidade</label>
                    <input type="number" class="form-control item-quantidade" name="itens[${index}].quantidade" min="1" value="1" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label">Valor Unitário</label>
                    <input type="number" class="form-control item-valorUnitario" name="itens[${index}].valorUnitario" step="0.01" min="0" value="0.00" required>
                </div>
            </div>
        </div>
    </div>`;
}

function adicionarItem() {
    const container = document.getElementById("itens-container");
    const html = criarItemHtml(itemIndex);
   
    container.insertAdjacentHTML('beforeend', html);
    itemIndex++;
    
    attachListenersToLastItem();
    calcularTotal();
}

function removerItem(botao) {
    const item = botao.closest(".item-pedido");
    item.remove();
    reindexarItens();
    calcularTotal();
}

function reindexarItens() {
    const itens = document.querySelectorAll(".item-pedido");
    itemIndex = itens.length;
    
    itens.forEach((item, index) => {
        item.dataset.index = index;
        item.querySelector("strong").innerText = `Item ${index + 1}`;
        
        item.querySelector('.item-descricao').name = `itens[${index}].descricao`;
        item.querySelector('.item-quantidade').name = `itens[${index}].quantidade`;
        item.querySelector('.item-valorUnitario').name = `itens[${index}].valorUnitario`;
    });
}

function calcularTotal() {
    let total = 0.0;
    
    document.querySelectorAll('.item-pedido').forEach(item => {
        const qtd = parseFloat(item.querySelector('.item-quantidade').value) || 0;
        const valor = parseFloat(item.querySelector('.item-valorUnitario').value) || 0;
        total += qtd * valor;
    });
    
    document.getElementById('total-pedido').innerText = total.toFixed(2).replace('.', ',');
}

// Adiciona listeners apenas no último item criado
function attachListenersToLastItem() {
    const ultimoItem = document.querySelector('.item-pedido:last-child');
    if (ultimoItem) {
        ultimoItem.querySelectorAll('.item-quantidade, .item-valorUnitario').forEach(input => {
            input.addEventListener('input', calcularTotal);
        });
    }
}

// Adiciona listeners em TODOS os itens existentes
function attachListeners() {
    document.querySelectorAll('.item-quantidade, .item-valorUnitario').forEach(input => {
        // Remove listener duplicado antes de adicionar
        input.removeEventListener('input', calcularTotal);
        input.addEventListener('input', calcularTotal);
    });
}

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    attachListeners(); // Adiciona listener no item inicial
    calcularTotal();
});