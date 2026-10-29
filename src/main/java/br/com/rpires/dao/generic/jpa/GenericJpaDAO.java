package br.com.rpires.dao.generic.jpa;
import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;
import javax.persistence.EntityManager;
import br.com.rpires.dao.Persistente;
import br.com.rpires.exceptions.DAOException;
import br.com.rpires.util.JpaUtil;
/** CRUD migrado para JPA: persist, find, merge, remove e JPQL. */
public class GenericJpaDAO<T extends Persistente,E extends Serializable> implements IGenericJapDAO<T,E> {
 private final Class<T> tipo;
 public GenericJpaDAO(Class<T> tipo) {this.tipo=tipo;}
 protected <R> R executar(Function<EntityManager,R> operacao) throws DAOException {
  EntityManager em=JpaUtil.factory().createEntityManager();
  try {
   em.getTransaction().begin();
   R resultado=operacao.apply(em);
   em.getTransaction().commit();
   return resultado;
  } catch(RuntimeException e) {
   if(em.getTransaction().isActive()) em.getTransaction().rollback();
   throw new DAOException("Erro de persistência JPA: "+tipo.getSimpleName(),e);
  } finally {em.close();}
 }
 public T cadastrar(T entity) throws DAOException {return executar(em->{em.persist(entity);return entity;});}
 public T consultar(E id) throws DAOException {return executar(em->em.find(tipo,id));}
 public T alterar(T entity) throws DAOException {return executar(em->em.merge(entity));}
 public void excluir(T entity) throws DAOException {
  executar(em->{T atual=em.find(tipo,entity.getId());if(atual!=null) em.remove(atual);return null;});
 }
 public Collection<T> buscarTodos() throws DAOException {
  return executar(em->em.createQuery("select e from "+tipo.getSimpleName()+" e",tipo).getResultList());
 }
}
