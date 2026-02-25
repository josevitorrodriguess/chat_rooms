# 💬 Real-Time Chat - Spring Boot

Projeto de chat em tempo real com:
- Spring Boot
- WebSocket
- JWT
- PostgreSQL
- Controle de limite de usuários por sala
- Scroll de mensagens (cursor pagination)
- Arquitetura preparada para Redis + RabbitMQ

---

# 🗺 Roadmap de Implementação (MVP)

## ✅ 0. Setup Inicial

- [ ] Criar projeto Spring Boot
    - Web
    - Security
    - WebSocket
    - Validation
    - JPA
    - PostgreSQL
    - Flyway
- [ ] Configurar `application.yml`
- [ ] Criar `docker-compose.yml` com PostgreSQL

**Entrega esperada:**  
Aplicação sobe e conecta no banco.

---

## ✅ 1. Banco de Dados + Migrations

Criar migration `V1__init.sql` com:

### users
- id
- username (unique)
- password_hash
- created_at

### rooms
- id
- name
- join_code (unique)
- max_members
- current_members
- creator_id
- created_at
- expires_at (opcional)

### room_members
- room_id
- user_id
- joined_at
- PK (room_id, user_id)

### messages
- id
- room_id
- sender_id
- client_message_id (UUID)
- content
- created_at
- UNIQUE(sender_id, client_message_id)  ← deduplicação

**Entrega esperada:**  
Tabelas criadas automaticamente pelo Flyway.

---

## ✅ 2. Autenticação (JWT)

- [ ] Criar entidade User
- [ ] POST /auth/register
- [ ] POST /auth/login
- [ ] Configurar Spring Security stateless
- [ ] Criar filtro JWT
- [ ] Criar endpoint GET /me

**Entrega esperada:**  
Usuário consegue registrar, logar e acessar rota protegida.

---

## ✅ 3. Rooms (CRUD básico)

- [ ] POST /rooms (name, maxMembers)
    - gerar join_code único (6–8 chars)
    - current_members = 0
- [ ] GET /rooms
- [ ] GET /rooms/{id}

**Entrega esperada:**  
Criar e listar salas.

---

## ✅ 4. Join com Controle de Limite (Sem Race Condition)

### POST /rooms/join (joinCode)

Implementar com transação:

```UPDATE rooms
SET current_members = current_members + 1
WHERE join_code = ?
AND current_members < max_members 
```


Se:
- 0 linhas afetadas → sala cheia
- sucesso → inserir em room_members

### POST /rooms/{id}/leave
- remover de room_members
- decrementar current_members com segurança

**Entrega esperada:**  
Sala respeita limite mesmo com múltiplos acessos simultâneos.

---

## ✅ 5. WebSocket (MVP)

- [ ] Configurar endpoint `/ws`
- [ ] Autenticar via JWT no handshake
- [ ] Criar evento:

```
SEND_MESSAGE {
roomId,
clientMessageId,
content
}
```

Validações:
- usuário pertence à sala
- deduplicação via clientMessageId

Salvar no banco e fazer broadcast.

**Entrega esperada:**  
2 clientes conectados recebem mensagens em tempo real.

---

## ✅ 6. Scroll Up (Histórico)

Endpoint:
GET /rooms/{id}/messages?cursor=...&limit=50


Implementar:
- paginação por cursor
- ordenação por created_at desc

**Entrega esperada:**  
Mensagens carregam progressivamente ao subir o scroll.

---

## ✅ 7. Retenção de Salas

- Usar `expires_at`
- Criar scheduler diário que remove salas expiradas (cascade)

**Entrega esperada:**  
Salas antigas são automaticamente removidas.

---

# 🔜 Pós-MVP (Escala)

## Redis
- Presença (TTL)
- Pub/Sub entre múltiplas instâncias
- Rate limit

## RabbitMQ
- Persistência garantida
- Worker separado
- DLQ
- Retry

## Load Balancer
- Nginx / Traefik
- WebSocket upgrade
- Sticky session opcional

---

# 🧠 Arquitetura Final (Objetivo)

Client
↓
Load Balancer
↓
Spring Boot (múltiplas instâncias)
↓
RabbitMQ → Worker → PostgreSQL
↓
Redis Pub/Sub (broadcast entre instâncias)

---

# 🎯 Próximo Passo

Começar pelo **Setup Inicial (Task 0)**:

- Escolher Maven ou Gradle
- Escolher Java 17 ou 21
- Criar docker-compose do PostgreSQL



