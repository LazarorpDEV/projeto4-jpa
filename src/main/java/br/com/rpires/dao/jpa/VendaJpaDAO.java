package br.com.rpires.dao.jpa;
import br.com.rpires.dao.generic.jpa.GenericJpaDAO;
import br.com.rpires.domain.jpa.*;
import br.com.rpires.exceptions.DAOException;
public class VendaJpaDAO extends GenericJpaDAO<VendaJpa,Long> implements IVendaJpaDAO {
 public VendaJpaDAO(){super(VendaJpa.class);}
 @Override public VendaJpa cadastrar(VendaJpa venda) throws DAOException {
  return executar(em->{
   venda.setCliente(em.getReference(ClienteJpa.class,venda.getCliente().getId()));
   venda.getProdutos().forEach(item->{
    item.setProduto(em.getReference(ProdutoJpa.class,item.getProduto().getId()));
    item.setVenda(venda);
   });
   em.persist(venda);return venda;
  });
 }
 @Override public void finalizarVenda(VendaJpa venda) throws DAOException {
  mudarStatus(venda,VendaJpa.Status.CONCLUIDA);
 }
 @Override public void cancelarVenda(VendaJpa venda) throws DAOException {
  mudarStatus(venda,VendaJpa.Status.CANCELADA);
 }
 private void mudarStatus(VendaJpa venda,VendaJpa.Status status) throws DAOException {
  executar(em->{
   VendaJpa atual=em.find(VendaJpa.class,venda.getId());
   if(atual==null) throw new IllegalArgumentException("Venda não encontrada");
   if(atual.getStatus()!=VendaJpa.Status.INICIADA) throw new IllegalStateException("Venda já encerrada");
   atual.setStatus(status);return null;
  });
  venda.setStatus(status);
 }
 @Override public void excluir(VendaJpa venda) {
  throw new UnsupportedOperationException("Use cancelarVenda para preservar o histórico");
 }
 @Override public VendaJpa consultarComCollection(Long id) throws DAOException {
  return executar(em->em.createQuery("select distinct v from VendaJpa v join fetch v.cliente left join fetch v.produtos i left join fetch i.produto where v.id=:id",VendaJpa.class)
   .setParameter("id",id).getResultStream().findFirst().orElse(null));
 }
}
