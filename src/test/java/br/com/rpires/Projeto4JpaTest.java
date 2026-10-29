package br.com.rpires;
import br.com.rpires.dao.jpa.*;
import br.com.rpires.domain.jpa.*;
import br.com.rpires.exceptions.DAOException;
import br.com.rpires.util.JpaUtil;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class Projeto4JpaTest {
 ClienteJpaDAO clientes=new ClienteJpaDAO();
 ProdutoJpaDAO produtos=new ProdutoJpaDAO();
 VendaJpaDAO vendas=new VendaJpaDAO();
 static long sequencia=100;
 @AfterAll static void fechar(){JpaUtil.close();}
 @BeforeEach void limpar(){
  var em=JpaUtil.factory().createEntityManager();
  try {
   em.getTransaction().begin();
   em.createQuery("delete from ProdutoQuantidadeJpa").executeUpdate();
   em.createQuery("delete from VendaJpa").executeUpdate();
   em.createQuery("delete from ProdutoJpa").executeUpdate();
   em.createQuery("delete from ClienteJpa").executeUpdate();
   em.getTransaction().commit();
  } finally {em.close();}
 }
 ClienteJpa cliente() throws Exception {
  var c=new ClienteJpa();c.setNome("Lazaro");c.setCpf(++sequencia);c.setTel(73999999999L);
  c.setEnd("Rua Exemplo");c.setNumero(10);c.setCidade("Teixeira de Freitas");c.setEstado("BA");
  return clientes.cadastrar(c);
 }
 ProdutoJpa produto() throws Exception {
  var p=new ProdutoJpa();p.setCodigo("P"+(++sequencia));p.setNome("Produto teste");
  p.setDescricao("Descrição teste");p.setValor(new BigDecimal("25.50"));return produtos.cadastrar(p);
 }
 VendaJpa venda() throws Exception {
  var v=new VendaJpa();v.setCodigo("V"+(++sequencia));v.setCliente(cliente());
  v.adicionarProduto(produto(),2);return vendas.cadastrar(v);
 }
 @Test void crudCliente() throws Exception {
  var c=cliente();assertNotNull(c.getId());
  assertEquals("Lazaro",clientes.consultar(c.getId()).getNome());
  c.setNome("Nome atualizado");clientes.alterar(c);
  assertEquals("Nome atualizado",clientes.consultar(c.getId()).getNome());
  assertEquals(1,clientes.buscarTodos().size());clientes.excluir(c);
  assertNull(clientes.consultar(c.getId()));assertTrue(clientes.buscarTodos().isEmpty());
 }
 @Test void crudProduto() throws Exception {
  var p=produto();assertNotNull(p.getId());assertEquals(p.getCodigo(),produtos.consultar(p.getId()).getCodigo());
  p.setValor(new BigDecimal("30.00"));produtos.alterar(p);
  assertEquals(0,new BigDecimal("30.00").compareTo(produtos.consultar(p.getId()).getValor()));
  assertEquals(1,produtos.buscarTodos().size());produtos.excluir(p);
  assertNull(produtos.consultar(p.getId()));assertTrue(produtos.buscarTodos().isEmpty());
 }
 @Test void vendaComRelacionamentosETotal() throws Exception {
  var v=venda();var lida=vendas.consultarComCollection(v.getId());
  assertEquals(v.getCliente().getId(),lida.getCliente().getId());
  assertEquals(1,lida.getProdutos().size());assertEquals(2,lida.getQuantidadeTotalProdutos());
  assertEquals(0,new BigDecimal("51.00").compareTo(lida.getValorTotal()));
  assertEquals("Produto teste",lida.getProdutos().iterator().next().getProduto().getNome());
  assertEquals(1,vendas.buscarTodos().size());assertNotNull(vendas.consultar(v.getId()));
 }
 @Test void finalizarVenda() throws Exception {
  var v=venda();vendas.finalizarVenda(v);
  assertEquals(VendaJpa.Status.CONCLUIDA,vendas.consultar(v.getId()).getStatus());
  assertThrows(UnsupportedOperationException.class,()->v.removerTodosProdutos());
  assertThrows(DAOException.class,()->vendas.cancelarVenda(v));
 }
 @Test void cancelarVendaPreservaClienteEProduto() throws Exception {
  var v=venda();vendas.cancelarVenda(v);
  assertEquals(VendaJpa.Status.CANCELADA,vendas.consultar(v.getId()).getStatus());
  assertNotNull(clientes.consultar(v.getCliente().getId()));assertEquals(1,produtos.buscarTodos().size());
  assertThrows(UnsupportedOperationException.class,()->v.removerTodosProdutos());
  assertThrows(UnsupportedOperationException.class,()->vendas.excluir(v));
 }
 @Test void removerItemPersistido() throws Exception {
  var v=venda();var carregada=vendas.consultarComCollection(v.getId());
  carregada.removerTodosProdutos();vendas.alterar(carregada);
  assertTrue(vendas.consultarComCollection(v.getId()).getProdutos().isEmpty());
  assertEquals(1,produtos.buscarTodos().size());assertEquals(1,clientes.buscarTodos().size());
  var em=JpaUtil.factory().createEntityManager();
  try {assertEquals(0L,em.createQuery("select count(i) from ProdutoQuantidadeJpa i",Long.class).getSingleResult());}
  finally {em.close();}
 }
 @Test void cpfDuplicadoFazRollback() throws Exception {
  var primeiro=cliente();var repetido=new ClienteJpa();
  repetido.setNome("Duplicado");repetido.setCpf(primeiro.getCpf());repetido.setTel(123L);
  repetido.setEnd("Rua");repetido.setNumero(1);repetido.setCidade("Cidade");repetido.setEstado("BA");
  assertThrows(DAOException.class,()->clientes.cadastrar(repetido));
  cliente();assertEquals(2,clientes.buscarTodos().size());
 }
 @Test void codigoProdutoDuplicadoFazRollback() throws Exception {
  var p=produto();var duplicado=new ProdutoJpa();duplicado.setCodigo(p.getCodigo());
  duplicado.setNome("Outro");duplicado.setDescricao("Outro");duplicado.setValor(BigDecimal.ONE);
  assertThrows(DAOException.class,()->produtos.cadastrar(duplicado));
  produto();assertEquals(2,produtos.buscarTodos().size());
 }
 @Test void quantidadePositivaERecalculo() throws Exception {
  var p=produto();var v=new VendaJpa();
  assertThrows(IllegalArgumentException.class,()->v.adicionarProduto(p,0));
  v.adicionarProduto(p,2);v.adicionarProduto(p,1);assertEquals(3,v.getQuantidadeTotalProdutos());
  v.removerProduto(p,1);assertEquals(2,v.getQuantidadeTotalProdutos());
  assertEquals(0,new BigDecimal("51.00").compareTo(v.getValorTotal()));
 }
 @Test void consultaInexistente() throws Exception {
  assertNull(clientes.consultar(-1L));assertNull(produtos.consultar(-1L));
  assertNull(vendas.consultarComCollection(-1L));
 }
}
