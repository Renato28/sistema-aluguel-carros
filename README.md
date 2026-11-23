# Sistema de Aluguel de Carros

API REST desenvolvida em Java com Spring Boot para gerenciamento de uma locadora de veículos. O sistema contempla o cadastro de clientes e veículos, gerenciamento de reservas, controle de disponibilidade e autenticação com autorização baseada em perfis de usuário.

O projeto foi desenvolvido com foco em boas práticas de desenvolvimento backend, organização em camadas, segurança, validação de dados, persistência relacional, versionamento de banco de dados e testes automatizados.

## Objetivos do projeto

- Desenvolver uma API REST para gerenciamento de uma locadora de veículos.
- Aplicar conceitos de arquitetura em camadas e separação de responsabilidades.
- Implementar autenticação e autorização utilizando Spring Security e JWT.
- Garantir a integridade dos dados com PostgreSQL e Flyway.
- Implementar regras de negócio para gerenciamento de reservas.
- Desenvolver testes automatizados para validar os comportamentos da aplicação.

## Tecnologias utilizadas

- **Java 25**
- **Spring Boot 4.1.2**
- **Spring Web MVC** — construção dos endpoints REST.
- **Spring Data JPA / Hibernate** — persistência e mapeamento objeto-relacional.
- **Spring Security** — autenticação e autorização.
- **JWT (JSON Web Token)** — autenticação baseada em tokens.
- **BCrypt** — criptografia de senhas.
- **PostgreSQL 16** — banco de dados relacional.
- **Flyway** — versionamento e gerenciamento de migrations.
- **Bean Validation** — validação de dados de entrada.
- **Springdoc OpenAPI / Swagger UI** — documentação da API.
- **JUnit 5, Mockito e Spring Boot Test** — testes automatizados.
- **JaCoCo** — análise de cobertura dos testes.
- **Maven** — gerenciamento de dependências e build.
- **Docker / Docker Compose** — ambiente do banco de dados.
- **Spring Boot Actuator** — monitoramento e exposição de informações operacionais da aplicação.

## Funcionalidades

### Usuários e autenticação
- Cadastro de clientes.
- Login com autenticação JWT.
- Criação automática do administrador inicial.
- Senhas armazenadas com BCrypt.
- Autorização baseada em três perfis: `ADMIN`, `ATENDENTE` e `CLIENTE`.

### Veículos e categorias
- Cadastro de veículos.
- Consulta de veículos.
- Consulta de veículo por identificador.
- Atualização de veículos.
- Exclusão lógica de veículos.
- Cadastro e consulta de categorias de veículos.

### Reservas
- Cadastro de reservas.
- Consulta de todas as reservas, conforme o perfil de acesso.
- Consulta das reservas do cliente autenticado.
- Consulta de reserva por identificador.
- Confirmação de reservas.
- Cancelamento de reservas.
- Atualização do período de uma reserva.
- Validação de disponibilidade dos veículos.
- Verificação de conflitos entre períodos de reservas.

### Segurança e validações
- Proteção dos endpoints com Spring Security.
- Autenticação stateless utilizando JWT.
- Controle de acesso por perfil.
- Validação dos dados recebidos pela API.
- Tratamento padronizado de erros.
- Proteção de senhas com BCrypt.
- Monitoramento da aplicação com Spring Boot Actuator.
- Exposição controlada de indicadores de saúde, informações e métricas.
- Proteção dos endpoints de monitoramento com autenticação JWT, exceto o health.

## Regras de negócio

### Veículos
- Veículos inativos não podem receber novas reservas.
- Apenas veículos com status `DISPONIVEL` podem ser reservados.
- A exclusão de veículos é lógica, alterando seu status para `INATIVO`.

### Reservas
- A data de retirada não pode ser anterior à data atual.
- A data de devolução deve ser posterior à data de retirada.
- Reservas com status `PENDENTE` ou `CONFIRMADA` bloqueiam períodos conflitantes.
- O sistema verifica a disponibilidade do veículo antes de cadastrar uma reserva.
- A atualização do período é permitida somente para reservas com status `PENDENTE`.
- Reservas canceladas ou concluídas não podem ser confirmadas.
- Reservas canceladas ou concluídas não podem ser canceladas novamente.
- Clientes podem consultar e gerenciar apenas suas próprias reservas, respeitando as permissões de acesso.

## Arquitetura e organização do projeto

O projeto utiliza arquitetura em camadas para separar as responsabilidades e facilitar a manutenção e evolução da aplicação.

```text
src/main/java/br/com/aluguelcarros/
├── config/        # Configurações e inicializações da aplicação
├── controller/    # Endpoints REST
├── dto/           # Objetos de transferência de dados
├── entity/        # Entidades e enums do domínio
├── exception/     # Exceções e tratamento de erros
├── repository/    # Acesso e persistência de dados
├── security/      # Configurações de autenticação e autorização
└── service/       # Regras de negócio
```

### Responsabilidades das camadas

- **Controller:** recebe as requisições HTTP e delega operações aos serviços.
- **Service:** concentra as regras de negócio e validações.
- **Repository:** realiza operações de persistência utilizando Spring Data JPA.
- **Entity:** representa as entidades persistidas no banco de dados.
- **DTO:** define os dados de entrada e saída da API, evitando expor diretamente as entidades.
- **Security:** configura autenticação JWT e controle de acesso.
- **Exception:** centraliza exceções e respostas de erro.
- **Config:** reúne configurações e inicializações da aplicação.

## Endpoints da API

Todos os endpoints exigem autenticação JWT, exceto os endpoints de autenticação (`/api/auth/**`), a documentação Swagger e o endpoint de saúde do Actuator (`/actuator/health`).

### Autenticação — `/api/auth`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/auth/login` | Realizar login | Público |
| POST | `/api/auth/cadastro` | Cadastrar cliente | Público |

### Clientes — `/api/clientes`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/clientes` | Cadastrar cliente | ADMIN, ATENDENTE |

### Categorias — `/api/categorias`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/categorias` | Listar categorias | Autenticado |
| POST | `/api/categorias` | Cadastrar categoria | ADMIN, ATENDENTE |

### Veículos — `/api/veiculos`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/veiculos` | Cadastrar veículo | ADMIN, ATENDENTE |
| PUT | `/api/veiculos/{id}` | Atualizar veículo | ADMIN, ATENDENTE |
| GET | `/api/veiculos` | Listar veículos | Autenticado |
| GET | `/api/veiculos/{id}` | Buscar veículo por ID | Autenticado |
| DELETE | `/api/veiculos/{id}` | Excluir veículo logicamente | ADMIN, ATENDENTE |

### Reservas — `/api/reservas`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/reservas` | Cadastrar reserva | ADMIN, ATENDENTE, CLIENTE |
| GET | `/api/reservas` | Listar todas as reservas | ADMIN, ATENDENTE |
| GET | `/api/reservas/minhas` | Listar reservas do cliente autenticado | CLIENTE |
| GET | `/api/reservas/{id}` | Buscar reserva por ID | ADMIN, ATENDENTE, CLIENTE |
| PATCH | `/api/reservas/{id}/cancelar` | Cancelar reserva | ADMIN, ATENDENTE, CLIENTE |
| PATCH | `/api/reservas/{id}/confirmar` | Confirmar reserva | ADMIN, ATENDENTE |
| PUT | `/api/reservas/{id}/periodo` | Atualizar período da reserva | ADMIN, ATENDENTE, CLIENTE |

**Observação:** as operações de consulta e alteração de reservas também respeitam as regras de propriedade e autorização implementadas na camada de serviço.

### Documentação interativa

Após iniciar a aplicação, a documentação Swagger estará disponível em:

`http://localhost:8080/swagger-ui/index.html`

A especificação OpenAPI estará disponível em:

`http://localhost:8080/v3/api-docs`

### Monitoramento — Spring Boot Actuator

A aplicação utiliza Spring Boot Actuator para disponibilizar informações operacionais e métricas.

Os seguintes endpoints estão habilitados:

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/actuator/health` | Consultar o estado de saúde da aplicação | Público |
| GET | `/actuator/info` | Consultar informações da aplicação | Autenticado |
| GET | `/actuator/metrics` | Listar métricas disponíveis | Autenticado |
| GET | `/actuator/metrics/{metricName}` | Consultar uma métrica específica | Autenticado |

**Observações:**

- Os endpoints são expostos de forma controlada.
- Os endpoints protegidos exigem autenticação JWT.
- Os detalhes de saúde são apresentados somente a usuários autorizados.
- A aplicação não expõe indiscriminadamente todos os endpoints do Actuator.

Exemplo de consulta de saúde:

```bash
curl http://localhost:8080/actuator/health
```

Exemplo de consulta de informações com JWT:

```bash
curl http://localhost:8080/actuator/info \
  -H "Authorization: Bearer SEU_TOKEN"
```

## Pré-requisitos

Para executar o projeto localmente, é necessário ter instalado:

- **Java 25**
- **Docker Desktop** com Docker Compose
- **Git**
- **OpenSSL** (para gerar a chave secreta JWT)

O Maven Wrapper está incluído no projeto e configura automaticamente o Maven 3.10.0, não sendo necessário instalá-lo separadamente.

## Configuração do ambiente

### 1. Configurar a senha do PostgreSQL

Crie o diretório `secrets` na raiz do projeto e, dentro dele, o arquivo `postgres_password.txt`. Esse arquivo deve conter a senha que será utilizada pelo PostgreSQL.

A mesma senha deve ser configurada na variável de ambiente `DB_PASSWORD`.

**Importante:** o diretório `secrets/` está incluído no `.gitignore` e não deve ser enviado ao GitHub.

### 2. Configurar as variáveis de ambiente

No terminal Git Bash, configure as seguintes variáveis:

```bash
export DB_PASSWORD='sua_senha_do_postgres'
export JWT_SECRET="$(openssl rand -base64 32)"
export ADMIN_EMAIL='admin@locadora.com'
export ADMIN_PASSWORD='sua_senha_forte'
export ADMIN_NAME='Administrador'
```

O valor de `DB_PASSWORD` deve ser exatamente igual ao conteúdo de `secrets/postgres_password.txt`.

A variável `JWT_SECRET` deve conter uma chave Base64 gerada aleatoriamente. Gere a chave uma vez e reutilize o mesmo valor nas próximas inicializações. Se gerar uma nova chave, os tokens emitidos anteriormente deixarão de ser válidos.

### 3. Iniciar o PostgreSQL

Com o arquivo de senha configurado, execute:

```bash
docker compose up -d db
```

O comando inicia o PostgreSQL 16 utilizando Docker Compose.

### 4. Criar o banco de dados de testes

Além do banco principal `aluguel_carros`, utilizado pela aplicação, é necessário criar o banco `aluguel_carros_test`, utilizado pelos testes automatizados.

Execute uma única vez:

```bash
docker exec aluguel-carros-db createdb -U postgres aluguel_carros_test
```

As migrations do Flyway são executadas no banco de testes durante a inicialização dos testes.

### 5. Executar a aplicação

Com o PostgreSQL em execução e as variáveis de ambiente configuradas:

```bash
./mvnw spring-boot:run
```

A API estará disponível em:

`http://localhost:8080`

### 6. Executar os testes automatizados

Para executar a suíte de testes:

```bash
./mvnw test
```

Os testes utilizam o banco `aluguel_carros_test`, separado do banco principal da aplicação.