### Backend Tarefa  
API RESTful que permitirá aos usuários gerenciar uma lista de tarefas

<b>Versão Java:</b> 8 
<img align="center" alt="Daniel-Java" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg">
</br>
<b>Versão Spring:</b> 2.7.14 <img align="center" alt="Daniel-Spring" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg">
</br>

### Introdução
1) Tecnologias utilizadas
2) Requisitos para montagem de ambiente
3) Como configurar o projeto no ambiente local (Spring Tools)
4) OpenAPI | Swagger (Documentação dos endpoints)
5) Como Autenticar para acessar os demais endpoints
6) Acessar o pgAdmin
7) Acessar o Redis pelo docker

##
### 1) Tecnologias utilizadas
- <b>Spring Web:</b> O Spring Web é um módulo do framework Spring que fornece suporte para o desenvolvimento de aplicações web. Ele facilita a criação de aplicações baseadas em Java, oferecendo diversas funcionalidades.

- <b>Spring Hateoas:</b> 
Spring HATEOAS é um projeto do Spring que facilita a construção de APIs RESTful que seguem o princípio HATEOAS (Hypermedia as the Engine of Application State). Esse princípio é uma das restrições da arquitetura REST e sugere que um cliente interaja com uma aplicação através de links fornecidos pelo servidor
- <b>Spring Actuator:</b> Spring Actuator é um módulo do Spring Boot que fornece uma série de funcionalidades para monitoramento e gerenciamento de aplicações. Ele permite que você acesse informações detalhadas sobre a aplicação em execução, facilitando a manutenção, operação e observabilidade.
- <b>Spring Data JPA:</b> Spring Data JPA é um módulo do Spring Data que simplifica o acesso a dados em aplicações Java, utilizando a especificação JPA (Java Persistence API). Ele proporciona uma forma fácil e eficiente de interagir com bancos de dados relacionais.
- <b>Spring Cache:</b> 
Spring Cache é um módulo do Spring Framework que fornece suporte para caching (armazenamento em cache) de dados em aplicações Java. O caching é uma técnica que melhora a performance e a eficiência das aplicações ao armazenar temporariamente os resultados de operações, como consultas a bancos de dados ou chamadas a serviços externos.
- <b>Spring Security:</b> Spring Security é um módulo do Spring Framework que fornece uma robusta e flexível solução de segurança para aplicações Java. Ele oferece suporte para autenticação e autorização, permitindo que você proteja suas aplicações contra uma variedade de ameaças e vulnerabilidades.
- <b>PostgreSQL:</b> PostgreSQL é um sistema de gerenciamento de banco de dados relacional.
- <b>JWT:</b> JWT (JSON Web Token) é um padrão aberto (RFC 7519) que define um formato compacto e autossuficiente para transmitir informações de forma segura entre partes como um objeto JSON. Os tokens JWT são frequentemente usados para autenticação e autorização em aplicações web e APIs.
- <b>Redis</b> Redis é um sistema de armazenamento de dados em memória, de código aberto, que atua como um banco de dados chave-valor, cache e broker de mensagens. É conhecido por sua alta performance, escalabilidade e flexibilidade, e é amplamente utilizado em aplicações web para otimizar a performance e melhorar a eficiência.
- <b>Docker Compose:</b> Docker Compose é uma ferramenta que facilita a definição e a execução de aplicações Docker multi-contêiner. Com o Docker Compose, você pode descrever a configuração da sua aplicação em um arquivo YAML e, em seguida, usar comandos simples para gerenciar a execução de todos os contêineres que fazem parte dessa aplicação
- <b>Mapper Structs:</b> O MapStruct é uma biblioteca Java usada para facilitar a transformação de objetos (ou "mapeamento") entre diferentes tipos, especialmente quando se trabalha com DTOs (Data Transfer Objects) e entidades de banco de dados. 
- <b>Logger Sl4j:</b> SLF4J (Simple Logging Facade for Java) é uma abstração de logging que permite que os desenvolvedores usem uma interface única para registrar mensagens em suas aplicações.
- <b>Documentação OpenApi (Swagger):</b> A documentação OpenAPI, comumente referida como Swagger, é uma especificação padrão para descrever APIs RESTful. O OpenAPI permite que desenvolvedores e equipes documentem suas APIs de forma clara e estruturada, facilitando a comunicação entre diferentes partes interessadas, como desenvolvedores, testadores e usuários.
- <b>Teste de unidade com Junit:</b> 
O teste de unidade com JUnit é uma prática fundamental no desenvolvimento de software em Java que envolve a criação de testes automatizados para verificar o comportamento de unidades individuais de código, como métodos ou classes.
- <b>Lombok:</b> Lombok é uma biblioteca Java que tem como objetivo reduzir o código boilerplate (código repetitivo) em classes Java. Ela oferece anotações que geram automaticamente métodos comuns, como getters, setters, construtores, métodos toString(), hashCode(), equals(), e mais. Isso ajuda a manter o código mais limpo e legível, facilitando o desenvolvimento e a manutenção.
  <div style="display: inline_block"><br>
  <img align="center" alt="Daniel-Java" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg">
  <img align="center" alt="Daniel-Spring" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg">
  <img align="center" alt="Daniel-Redis" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg">
  <img align="center" alt="Daniel-Postgres" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg">
  <img align="center" alt="Daniel-Docker" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg">
  <img align="center" alt="Daniel-Junit" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/junit/junit-original.svg">
</div>

### Estrutura projeto

```plaintext
├─── tarefas
  └─── config
  │      └─── cache 
  │      └─── security
  │      └─── openapi
  └─── controllers
  └─── dtos
  └─── mappers
  └─── entities
  │      └─── enums
  └─── exceptions
  │      └─── handlers
  └─── repositories
  │      └─── custom
  └─── services
  │      └─── security
  └─── utils
  └── tests
```



##
### 2) Requisitos para montagem de ambiente
- Java 8;
- Lombok configurado na IDE;
  
##
### 3) Como configurar o projeto no ambiente local (Spring Tools)
- 3.1 Após clonar e importar o projeto como maven, é necessário entrar na pasta raiz do projeto e rodar esse comando:
~~~
mvn clean package -DskipTests
~~~
- 3.2 Depois da um <b>maven update</b> e <b>refresh</b> no projeto para verificar se foi gerado no target esses arquivos:
![image](https://github.com/user-attachments/assets/b7b392be-e5d2-441a-bcba-78a91e267775)
- 3.3 Ainda na pasta raiza do projeto, rodar o comando do docker compose para subir os containers do postgres, pgadmin e redis:
~~~
docker compose up -d
~~~
- 3.4 Pronto agora pode startar a aplicação

##
### 4 OpenAPI | Swagger (Documentação dos endpoints)
Esta é a documentação da API RESTful. Ao clicar em qualquer serviço listado, você poderá expandir a seção correspondente para visualizar uma descrição detalhada de como o serviço funciona, incluindo informações sobre:

- Métodos HTTP: Quais métodos (GET, POST, PUT, DELETE) são suportados.
- URL do Endpoint: O caminho para acessar o serviço.
- Parâmetros: Quais parâmetros são necessários e suas descrições.
- Formato de Resposta: Exemplos do formato de resposta retornada pelo serviço.
- Códigos de Status: Os possíveis códigos de status HTTP que podem ser retornados e seu significado.

Explore cada serviço para entender melhor sua funcionalidade.

<b>URL:</b> http://localhost:8080/swagger-ui/swagger-ui/index.html#/
![image](https://github.com/user-attachments/assets/3ae6700d-eeff-490d-85c1-fa23f799dec8)

##
### 5 Como Autenticar para acessar os demais endpoints

- 5.1 Primeiro vocês irão criar um usuário
![image](https://github.com/user-attachments/assets/7ce3f97b-b148-4734-897d-4d33d603b4bc)

- 5.2 Efetuar o login
![image](https://github.com/user-attachments/assets/05b180ab-911d-4fa3-8335-086a71125c9d)
![image](https://github.com/user-attachments/assets/6d688f3c-b528-4cee-92b6-2b56df5d41e6)

- 5.3 Informar o bearer token no header
![image](https://github.com/user-attachments/assets/595ae41d-d024-4e1d-888e-6b4f4d5c5b92)

Pronto agora você consegue acessar os demais endpoints que exigem autenticação.

##
### 6 Acessar o pgAdmin
<b>URL:</b> http://localhost:5050/

- 6.1 Defina a senha mestre como <b>postgresl</b>:
![image](https://github.com/user-attachments/assets/19bb09b0-84a1-47ca-a5ee-624057b2e6ed)
- 6.2 Configure um novo servidor:
![image](https://github.com/user-attachments/assets/8af569c0-c263-4c46-8497-2e1a3db767d0)
- 6.3 Para a autenticação, utilize a senha postgres:
![image](https://github.com/user-attachments/assets/207c4564-b254-46e0-ba78-c65bfa0ce091)
- 6.4 Pronto pgAdmin configurado: </br>
![image](https://github.com/user-attachments/assets/88114c1d-3aad-461c-8edf-db6f1143afe6)

##
### 7 Acessar o Redis pelo docker

- 7.1 Acessar o container redis:
~~~
docker exec -it {id_container} bash
~~~
- 7.2 Autentiticar:
~~~
redis-cli -a redis
~~~
- 7.3 Verifique se há registros no cache. Se você já chamou os serviços que armazenam dados no cache e não encontrou nenhum registro, recomendo sair do contêiner e entrar novamente:
~~~
keys *
~~~
![image](https://github.com/user-attachments/assets/3e42dfcf-0b99-4a29-9b8f-c11d3b82428a)




