# Projeto 4 — Persistência com JPA (módulo 35)

Sistema de vendas com Cliente, Produto, Venda e ProdutoQuantidade. A camada de persistência usa JPA 2.2 / Hibernate no lugar das operações JDBC manuais.

## Requisitos e execução

- JDK 17 ou superior e Maven 3.9 ou superior (`java -version` e `mvn -version`).
- Abra o terminal na pasta que contém este `pom.xml`.

```sh
mvn clean test
mvn exec:java
```

O primeiro comando executa 10 testes de integração em H2 em memória, isolados do banco da demonstração. O segundo cria as tabelas e grava uma venda no H2 em arquivo (`data/projeto4.mv.db`). Cada execução da demonstração adiciona novos registros. Não é necessário instalar um servidor de banco. Dados e arquivos compilados estão no `.gitignore`.

Para conferir o banco com DBeaver: conexão H2 Embedded, URL `jdbc:h2:file:/CAMINHO/ABSOLUTO/Projeto4_JPA/data/projeto4`, usuário `sa`, senha vazia. Feche a aplicação antes de conectar. O arquivo `consultas.sql` contém consultas das tabelas e seus relacionamentos.

## Migração JDBC → JPA

| Operação anterior | Implementação atual |
| --- | --- |
| Connection / PreparedStatement | EntityManager e transações JPA |
| INSERT | persist |
| SELECT por chave | find |
| UPDATE | merge |
| DELETE | remove |
| Listagem | JPQL tipado |
| Montagem manual de relacionamentos | ManyToOne / OneToMany |

`GenericJpaDAO` centraliza o CRUD, commit, rollback e fechamento do EntityManager. `JpaUtil` reutiliza a EntityManagerFactory. A configuração está em `src/main/resources/META-INF/persistence.xml`.

O cliente pode ter várias vendas. Cada venda tem vários itens; cada item referencia um produto. Somente os itens pertencem ao ciclo de vida da venda: remover um item não apaga o produto nem o cliente. A consulta `consultarComCollection` carrega itens e produtos antes de fechar a sessão. Vendas são canceladas, não excluídas, preservando o histórico; só vendas iniciadas podem ser concluídas ou canceladas.

## Testes

CRUD de clientes e produtos, consulta de venda com relacionamentos e total, conclusão, cancelamento, remoção persistida de itens, CPF/código duplicados com rollback, quantidades e consulta inexistente. Falhas de unicidade provocadas pelos testes são esperadas; o resultado final deve ser `BUILD SUCCESS`.

## PostgreSQL (opcional)

Crie um banco vazio chamado `projeto4`. No PowerShell, configure antes da execução:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/projeto4"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "SUA_SENHA_LOCAL"
mvn exec:java
```

O Hibernate cria/atualiza as tabelas automaticamente (`hbm2ddl.auto=update`, configuração de estudo). Testes continuam usando apenas H2 em memória. A execução com PostgreSQL depende de um servidor local e não foi validada nesta entrega. Não coloque senhas reais no repositório.

## Publicar no GitHub

Crie na sua conta um repositório vazio chamado `projeto4-jpa`, sem README, licença ou `.gitignore` pelo site. Na pasta deste projeto, após os testes:

```sh
git init
git add .
git commit -m "Migra persistencia do Projeto 4 para JPA"
git branch -M main
git remote add origin https://github.com/LazarorpDEV/projeto4-jpa.git
git push -u origin main
```

Envie na plataforma o link `https://github.com/LazarorpDEV/projeto4-jpa`. Confira no GitHub se aparecem `pom.xml`, `src`, `README.md` e `consultas.sql`. O ZIP não inclui pasta `.git`; os comandos acima são para a pasta extraída, como novo repositório.

## Referência e adaptações

Base didática de Rodrigo Pires: https://github.com/digaomilleniun/backend-java-ebac/tree/a841df3ca36f5ba0878c6ca0338e63ccc072b815/mod35/ExemploVendasMod35

Foram preservados os nomes e a estrutura principal das entidades e contratos JPA do professor, com atribuição nos arquivos originais. Esta entrega organiza a parte JPA em Maven, sem carregar os DAOs JDBC antigos; corrige configuração de persistência, ciclo de vida da fábrica, rollback, cascatas e remoção de itens, acrescenta demonstração e testes de integração. É uma versão adaptada da referência; não é uma alteração de um projeto local do aluno que não foi fornecido.
