package br.com.rpires;
import br.com.rpires.dao.jpa.*;
import br.com.rpires.domain.jpa.*;
import br.com.rpires.util.JpaUtil;
import java.math.BigDecimal;
public class Main {
 public static void main(String[] args) throws Exception {
  try {
   long chave=System.currentTimeMillis();
   var clientes=new ClienteJpaDAO();var produtos=new ProdutoJpaDAO();var vendas=new VendaJpaDAO();
   var c=new ClienteJpa();c.setNome("Cliente demonstração");c.setCpf(chave);c.setTel(73999999999L);
   c.setEnd("Rua Exemplo");c.setNumero(10);c.setCidade("Teixeira de Freitas");c.setEstado("BA");clientes.cadastrar(c);
   var p=new ProdutoJpa();p.setCodigo(java.util.UUID.randomUUID().toString().substring(0,8));
   p.setNome("Produto demonstração");p.setDescricao("Exemplo JPA");p.setValor(new BigDecimal("25.50"));produtos.cadastrar(p);
   var v=new VendaJpa();v.setCodigo("V"+java.util.UUID.randomUUID());v.setCliente(c);v.adicionarProduto(p,2);vendas.cadastrar(v);
   vendas.finalizarVenda(v);var salva=vendas.consultarComCollection(v.getId());
   System.out.println("Venda salva no banco: "+salva.getId());
   System.out.println("Cliente: "+salva.getCliente().getNome());
   System.out.println("Quantidade: "+salva.getQuantidadeTotalProdutos());
   System.out.println("Total: R$ "+salva.getValorTotal()+" | Status: "+salva.getStatus());
  } finally {JpaUtil.close();}
 }
}
