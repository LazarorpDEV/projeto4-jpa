package br.com.rpires.util;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
/** Uma fábrica por execução; cada operação usa seu próprio EntityManager. */
public final class JpaUtil {
 private static EntityManagerFactory factory;
 private JpaUtil() {}
 public static synchronized EntityManagerFactory factory() {
  if (factory == null || !factory.isOpen()) {
   Map<String,Object> config = new HashMap<>();
   put(config,"javax.persistence.jdbc.url","DB_URL");
   put(config,"javax.persistence.jdbc.user","DB_USER");
   put(config,"javax.persistence.jdbc.password","DB_PASSWORD");
   if (System.getenv("DB_URL") != null && System.getenv("DB_URL").startsWith("jdbc:postgresql:")) {
    config.put("javax.persistence.jdbc.driver","org.postgresql.Driver");
    config.put("hibernate.dialect","org.hibernate.dialect.PostgreSQLDialect");
   }
   // Testes sempre usam o banco isolado, mesmo havendo DB_URL no ambiente.
   String unit = System.getProperty("jpa.unit", "Projeto4");
   if (unit.equals("Projeto4Teste")) config.clear();
   factory=Persistence.createEntityManagerFactory(unit,config);
  }
  return factory;
 }
 private static void put(Map<String,Object> config,String key,String env) {
  if(System.getenv(env)!=null) config.put(key,System.getenv(env));
 }
 public static synchronized void close() {
  if(factory!=null && factory.isOpen()) factory.close();
 }
}
