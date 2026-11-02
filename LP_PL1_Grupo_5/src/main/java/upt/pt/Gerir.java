package upt.pt;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal de gestão que contém toda a lógica de negócio,
 * regras de armazém e ligação ao Hibernate.
 */
public class Gerir {

    private SessionFactory sessionFactory;

    /**
     * Construtor da classe Gerir.
     * Arranca a ligação à base de dados através do ficheiro hibernate.cfg.xml.
     */
    public Gerir() {
        this.sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
    }

    /**
     * Verifica as credenciais para permitir a entrada no sistema.
     * @param identificador o email ou o telemóvel da pessoa.
     * @param password a palavra-passe associada à conta.
     * @return devolve a conta da pessoa se os dados estiverem corretos, ou null se falhar.
     */
    public Utilizador fazerLogin(String identificador, String password) {
        try (Session session = sessionFactory.openSession()) {
            Utilizador u = session.createQuery("from Utilizador u where (u.email = :id or cast(u.contacto as string) = :id) and u.password = :pass", Utilizador.class)
                                  .setParameter("id", identificador)
                                  .setParameter("pass", password)
                                  .uniqueResult();
            
            if (u != null) {
                verificarObjetosExpirados();
            }
            
            return u;
        }
    }

    /**
     * Verifica se o email ou telemóvel já existem na base de dados para evitar contas duplicadas.
     * @param email o email digitado no registo.
     * @param contacto o telemóvel digitado no registo.
     * @return true se o email ou contacto já existirem, false caso contrário.
     */
    public boolean existeEmailOuContacto(String email, int contacto) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("select count(u) from Utilizador u where u.email = :email or u.contacto = :contacto", Long.class)
                                .setParameter("email", email)
                                .setParameter("contacto", contacto)
                                .uniqueResult();
            return count != null && count > 0;
        }
    }

    /**
     * Cria uma nova conta no sistema e guarda na base de dados.
     * @param tipo o nível de acesso (Admin, Funcionário ou Cliente).
     * @param pass a palavra-passe escolhida.
     * @param nome o nome completo do utilizador.
     * @param email o endereço de email.
     * @param contacto o número de telemóvel.
     */
    public void adicionarUtilizador(String tipo, String pass, String nome, String email, int contacto) {
        guardar(new Utilizador(tipo, pass, nome, email, contacto));
    }

    /**
     * Mostra no ecrã a lista de utilizadores filtrada pelo seu cargo/tipo.
     * @param tipo a string com o cargo a filtrar (ex: "Funcionario", "Cliente").
     */
    public void listarUtilizadoresPorTipo(String tipo) {
        try (Session session = sessionFactory.openSession()) {
            List<Utilizador> lista = session.createQuery("from Utilizador u where lower(u.tipoUtilizador) = :tipo", Utilizador.class)
                                          .setParameter("tipo", tipo.toLowerCase())
                                          .getResultList();
            if (lista.isEmpty()) {
                System.out.println("Nenhum " + tipo + " encontrado.");
            } else {
                for (Utilizador u : lista) {
                    System.out.println(u);
                }
            }
        }
    }

    /**
     * Apaga a conta de um utilizador e remove em cascata todo o seu histórico
     * (objetos, reclamações, agendamentos) para manter a integridade da base de dados.
     * @param id o número identificador (ID) da conta a apagar.
     */
    public void removerUtilizador(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Utilizador u = session.get(Utilizador.class, id);
            
            if (u != null) {
                if (u.getTipoUtilizador().equalsIgnoreCase("Admin")) {
                    System.out.println("Erro: Não tem permissão para remover um Administrador.");
                } else {
                    List<Agendamento> agendamentosUser = session.createQuery("from Agendamento a where a.utilizador.id = :uid", Agendamento.class)
                            .setParameter("uid", id).getResultList();
                    for(Agendamento a : agendamentosUser) session.remove(a);
                    
                    List<Reclamacao> recsUser = session.createQuery("from Reclamacao r where r.utilizador.id = :uid", Reclamacao.class)
                            .setParameter("uid", id).getResultList();
                    for(Reclamacao r : recsUser) session.remove(r);

                    List<Objeto> objetosUser = session.createQuery("from Objeto o where o.utilizador.id = :uid", Objeto.class)
                            .setParameter("uid", id).getResultList();
                            
                    for(Objeto o : objetosUser) {
                        if (o.getObjetoAssociado() != null) {
                            Objeto associado = o.getObjetoAssociado();
                            associado.setObjetoAssociado(null);
                            session.merge(associado);
                        }
                        
                        List<Agendamento> agendsObj = session.createQuery("from Agendamento a where a.objeto.id = :oid", Agendamento.class)
                                .setParameter("oid", o.getId()).getResultList();
                        for(Agendamento ao : agendsObj) session.remove(ao);
                        
                        List<Reclamacao> recsObj = session.createQuery("from Reclamacao r where r.objeto.id = :oid", Reclamacao.class)
                                .setParameter("oid", o.getId()).getResultList();
                        for(Reclamacao ro : recsObj) session.remove(ro);
                        
                        session.remove(o);
                    }
                    
                    session.remove(u); 
                    t.commit();
                    System.out.println("Conta (" + u.getTipoUtilizador() + ") e todos os processos foram removidos com sucesso!");
                }
            } else {
                System.out.println("Erro: ID de cliente/conta não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao remover conta: " + e.getMessage());
        }
    }

    /**
     * Motor genérico privado para abrir transação, persistir um objeto e fazer o commit.
     * @param obj o objeto (entidade) que queremos guardar na base de dados.
     */
    private void guardar(Object obj) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            session.persist(obj); 
            t.commit();
        } catch (Exception e) {
            System.out.println("Erro BD: " + e.getMessage());
        }
    }

    /**
     * Regista um novo edifício/local de recolha no sistema.
     * @param edificio o nome do edifício.
     * @param descricao informação adicional sobre o local.
     * @param horaInicio hora de abertura do edifício.
     * @param horaFim hora de fecho do edifício.
     */
    public void adicionarLocal(String edificio, String descricao, String horaInicio, String horaFim) { 
        guardar(new Local(edificio, descricao, horaInicio, horaFim)); 
    }
    
    /**
     * Cria um novo armazém dentro de um edifício, validando se o nome já existe.
     * @param nome o nome a atribuir ao armazém.
     * @param localId o ID do edifício onde o armazém vai ficar.
     */
    public void adicionarArmazem(String nome, Long localId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("select count(a) from Armazem a where lower(a.nomeArmazem) = :nome", Long.class)
                                .setParameter("nome", nome.toLowerCase())
                                .uniqueResult();
            if (count != null && count > 0) {
                System.out.println("Erro: Já existe um armazém com esse nome.");
                return;
            }

            Transaction t = session.beginTransaction();
            Local l = session.get(Local.class, localId);
            if (l != null) {
                Armazem a = new Armazem(nome);
                l.addArmazem(a);
                session.persist(a);
                t.commit();
                System.out.println("Armazém registado com sucesso! (ID: " + a.getId() + ")");
            } else {
                System.out.println("Erro: Local não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao criar armazém: " + e.getMessage());
        }
    }

    /**
     * Cria uma prateleira ou cacifo dentro de um armazém com um limite de lotação.
     * @param cod código de identificação da prateleira (ex: A1, Cacifo-B).
     * @param lotacao número máximo de objetos que cabem nesta prateleira.
     * @param armazemId o ID do armazém onde a prateleira será instalada.
     */
    public void adicionarPrateleira(String cod, int lotacao, Long armazemId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Armazem a = session.get(Armazem.class, armazemId);
            if (a != null) {
                Prateleira p = new Prateleira(cod, lotacao);
                a.addPrateleira(p);
                session.persist(p);
                t.commit();
                System.out.println("Prateleira criada com sucesso!");
            } else {
                System.out.println("Erro: Armazém não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao criar prateleira: " + e.getMessage());
        }
    }

    /**
     * Define o local de trabalho atual de um funcionário.
     * @param userId o ID do funcionário.
     * @param localId o ID do edifício onde o funcionário vai operar.
     */
    public void atribuirFuncionarioLocal(Long userId, Long localId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Utilizador u = session.get(Utilizador.class, userId);
            Local l = session.get(Local.class, localId);
            if (u != null && l != null) { 
                u.setLocalTrabalho(l); 
                t.commit();
                System.out.println("Funcionário " + u.getNomeUtilizador() + " atribuído ao local " + l.getNomeEdificio() + " com sucesso!"); 
            } else {
                System.out.println("Erro: Funcionário ou Local não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao atribuir funcionário: " + e.getMessage());
        }
    }

    /**
     * Lista todos os edifícios, mostrando os funcionários atribuídos e calculando a lotação atual de cada prateleira.
     */
    public void listarLocaisComDetalhes() {
        try (Session session = sessionFactory.openSession()) {
            List<Local> locais = session.createQuery("from Local", Local.class).getResultList();
            
            if (locais.isEmpty()) {
                System.out.println("Nenhum edifício registado.");
                return;
            }
            
            for (Local l : locais) {
                System.out.println("\n========================================");
                System.out.println("EDIFÍCIO [ID: " + l.getId() + "]: " + l.getNomeEdificio());
                System.out.println("Descrição: " + l.getDescricaoLocal());
                System.out.println("Horário: " + l.getHoraInicio() + " às " + l.getHoraFim());
                System.out.println("----------------------------------------");
                
                List<Utilizador> funcionarios = session.createQuery("from Utilizador u where u.localTrabalho.id = :idLocal", Utilizador.class)
                                                       .setParameter("idLocal", l.getId()).getResultList();
                
                System.out.println("FUNCIONÁRIOS ATRIBUÍDOS:");
                if (funcionarios.isEmpty()) {
                    System.out.println("  - Nenhum funcionário atribuído.");
                } else {
                    for (Utilizador f : funcionarios) {
                        System.out.println("  - [ID: " + f.getId() + "] " + f.getNomeUtilizador() + " (" + f.getContacto() + ")");
                    }
                }
                
                System.out.println("----------------------------------------");
                System.out.println("ARMAZÉNS E PRATELEIRAS:");
                if (l.getArmazens().isEmpty()) {
                    System.out.println("  - Nenhum armazém criado neste edifício.");
                } else {
                    for (Armazem a : l.getArmazens()) {
                        System.out.println("  > Armazém ID " + a.getId() + " | Nome: " + a.getNomeArmazem());
                        if (a.getPrateleiras().isEmpty()) {
                            System.out.println("      Sem prateleiras registadas.");
                        } else {
                            for (Prateleira p : a.getPrateleiras()) {
                                Long ocupados = session.createQuery("select count(o) from Objeto o where o.prateleira.id = :idPrat and o.estado not in ('Concluído', 'Arquivado (Correspondido)', 'Arquivado', 'Doado/Eliminado', 'Expirado (No Armazém)')", Long.class)
                                                       .setParameter("idPrat", p.getId()).uniqueResult();
                                long qtdOcupada = (ocupados != null) ? ocupados : 0;
                                System.out.println("      - Prateleira: " + p.getCodigoPrateleira() + " | Lotação: " + qtdOcupada + " ocupados de " + p.getLotacaoPrateleira() + " máx.");
                            }
                        }
                    }
                }
                System.out.println("========================================\n");
            }
        }
    }

    /**
     * Regista um novo objeto (perdido) e verifica a lotação da prateleira antes de gravar.
     * @param data data de registo.
     * @param marca a marca do objeto.
     * @param tipo o tipo de objeto.
     * @param cor a cor do objeto.
     * @param estado estado inicial (ex: Perdido).
     * @param idUser ID do cliente que está a reportar.
     * @param idPrat ID da prateleira (pode ser null).
     * @param idLocal ID do edifício onde foi perdido.
     */
    public void adicionarObjeto(LocalDateTime data, String marca, String tipo, String cor, String estado, Long idUser, Long idPrat, Long idLocal) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            
            Prateleira p = null;
            if (idPrat != null) {
                p = session.get(Prateleira.class, idPrat);
                if (p != null) {
                    Long ocupados = session.createQuery("select count(o) from Objeto o where o.prateleira.id = :idPrat and o.estado not in ('Arquivado', 'Concluído', 'Arquivado (Correspondido)', 'Doado/Eliminado', 'Expirado (No Armazém)')", Long.class)
                                           .setParameter("idPrat", idPrat).uniqueResult();
                    long atual = (ocupados != null) ? ocupados : 0;
                    if (atual >= p.getLotacaoPrateleira()) {
                        System.out.println("Erro: A prateleira " + p.getCodigoPrateleira() + " e o armazém já atingiram a lotação máxima (" + atual + "/" + p.getLotacaoPrateleira() + "). Volte mais tarde, está ocupado.");
                        return; 
                    }
                }
            }

            Objeto obj = new Objeto(data, marca, tipo, cor, estado);
            if (idUser != null) obj.setUtilizador(session.get(Utilizador.class, idUser));
            if (p != null) obj.setPrateleira(p);
            if (idLocal != null) obj.setLocalRecolha(session.get(Local.class, idLocal));
            
            session.persist(obj); 
            t.commit();
            System.out.println("Objeto registado na BD com sucesso!");
        } catch (Exception e) {
            System.out.println("ERRO AO GUARDAR OBJETO: " + e.getMessage());
        }
    }

    /**
     * Regista um objeto achado pelo funcionário, associando automaticamente ao seu local de trabalho.
     * @param data data e hora da recolha.
     * @param marca a marca do objeto.
     * @param tipo categoria/tipo do objeto.
     * @param cor a cor.
     * @param idUser o ID do funcionário que está a registar.
     */
    public void adicionarObjetoEncontrado(LocalDateTime data, String marca, String tipo, String cor, Long idUser) {
        try (Session session = sessionFactory.openSession()) {
            Utilizador u = session.get(Utilizador.class, idUser);
            if (u.getLocalTrabalho() == null) {
                System.out.println("Erro: Não tem nenhum local de trabalho atribuído. Não é possível registar o objeto.");
                return;
            }
            registarAchadoNaPrateleira(session, data, marca, tipo, cor, u, u.getLocalTrabalho(), true);
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    /**
     * Método interno para calcular automaticamente a prateleira com mais espaço e guardar um objeto achado.
     * @param session a sessão ativa do Hibernate.
     * @param data a data atual.
     * @param marca a marca.
     * @param tipo o tipo.
     * @param cor a cor.
     * @param u o utilizador associado.
     * @param local o edifício de recolha.
     * @param isFuncionario flag booleana para formatar a mensagem de saída.
     */
    private void registarAchadoNaPrateleira(Session session, LocalDateTime data, String marca, String tipo, String cor, Utilizador u, Local local, boolean isFuncionario) {
        Transaction t = session.beginTransaction();
        Prateleira melhorPrateleira = null;
        long menorOcupacao = Long.MAX_VALUE;

        for (Armazem a : local.getArmazens()) {
            for (Prateleira p : a.getPrateleiras()) {
                Long ocupados = session.createQuery("select count(o) from Objeto o where o.prateleira.id = :idPrat and o.estado not in ('Concluído', 'Arquivado (Correspondido)', 'Arquivado', 'Doado/Eliminado', 'Expirado (No Armazém)')", Long.class)
                                       .setParameter("idPrat", p.getId()).uniqueResult();
                long atual = (ocupados != null) ? ocupados : 0;

                if (atual < p.getLotacaoPrateleira() && atual < menorOcupacao) {
                    menorOcupacao = atual;
                    melhorPrateleira = p;
                }
            }
        }
        
        if (melhorPrateleira == null) {
            System.out.println("Erro: O armazém deste edifício está cheio! Volte mais tarde, de momento está ocupado.");
            t.rollback(); 
            return; 
        }

        Objeto obj = new Objeto(data, marca, tipo, cor, "Em Armazém");
        if (u != null) obj.setUtilizador(u);
        obj.setLocalRecolha(local);
        obj.setPrateleira(melhorPrateleira);

        session.persist(obj); 
        t.commit();
        
        if (isFuncionario) {
            System.out.println("Registado de forma associada ao seu local (" + local.getNomeEdificio() + ").");
        } else {
            System.out.println("Registado e entregue no local " + local.getNomeEdificio() + ".");
        }
        System.out.println("Colocado AUTOMATICAMENTE na Prateleira " + melhorPrateleira.getCodigoPrateleira() + ".");
    }

    /**
     * Utilitário privado para alterar a string de estado de um objeto e opcionalmente criar um vínculo (Foreign Key).
     * @param id o ID do objeto a editar.
     * @param novoEstado o novo estado.
     * @param idAssociado o ID do objeto a associar (usar -1 para anular associação).
     */
    private void atualizarEstadoObjeto(Long id, String novoEstado, Long idAssociado) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Objeto o = session.get(Objeto.class, id);
            if (o != null) {
                if (novoEstado != null) o.setEstado(novoEstado);
                if (idAssociado != null && idAssociado != -1L) {
                    o.setObjetoAssociado(session.get(Objeto.class, idAssociado));
                } else if (idAssociado != null && idAssociado == -1L) {
                    o.setObjetoAssociado(null);
                }
                t.commit();
                System.out.println("Estado do Objeto ID " + o.getId() + " alterado para: " + o.getEstado());
            } else {
                System.out.println("Erro: Objeto ID " + id + " não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao atualizar objeto: " + e.getMessage());
        }
    }

    /**
     * Altera o estado do objeto definitivamente para "Arquivado", encerrando o processo.
     * @param idObjeto o ID do objeto.
     */
    public void arquivarProcesso(Long idObjeto) { 
        atualizarEstadoObjeto(idObjeto, "Arquivado", null); 
    }
    
    /**
     * Une um objeto perdido (reportado pelo cliente) a um achado pelo funcionário, validando se pertencem ao mesmo local.
     * @param idPerdido o ID do objeto reclamado.
     * @param idAchado o ID do objeto no armazém.
     * @param idFuncionario o funcionário que está a processar a correspondência.
     */
    public void associarManual(Long idPerdido, Long idAchado, Long idFuncionario) { 
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Objeto perdido = session.get(Objeto.class, idPerdido);
            Objeto achado = session.get(Objeto.class, idAchado);
            Utilizador f = session.get(Utilizador.class, idFuncionario);
            
            if (f.getLocalTrabalho() == null) {
                System.out.println("Erro: Não tem nenhum local de trabalho atribuído.");
                return;
            }
            
            if (perdido != null && achado != null) {
                if (achado.getLocalRecolha() == null || !f.getLocalTrabalho().getId().equals(achado.getLocalRecolha().getId())) {
                    System.out.println("Erro: O objeto achado não se encontra no seu edifício (" + f.getLocalTrabalho().getNomeEdificio() + "). Ação negada.");
                    return;
                }

                perdido.setObjetoAssociado(achado);
                achado.setObjetoAssociado(perdido);
                
                perdido.setLocalRecolha(achado.getLocalRecolha());
                perdido.setPrateleira(achado.getPrateleira());
                
                achado.setLocalRecolha(null);
                achado.setPrateleira(null);
                
                perdido.setEstado("Autorizado para Levantamento");
                achado.setEstado("Arquivado (Correspondido)");
                
                t.commit();
                System.out.println("Associação feita! O objeto Perdido tem agora o local do Achado e pode ser agendado.");
            } else {
                System.out.println("Erro: IDs não encontrados.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao associar: " + e.getMessage());
        }
    }

    /**
     * Anula a união entre dois objetos e devolve o objeto original ao armazém.
     * @param id o ID do objeto.
     */
    public void rejeitarCorrespondencia(Long id) { 
        atualizarEstadoObjeto(id, "Em Armazém", -1L); 
    }

    /**
     * Imprime no ecrã a percentagem de sucesso do sistema (Objetos Concluídos/Arquivados vs Total).
     */
    public void calcularEstatisticaEntregas() {
        try (Session session = sessionFactory.openSession()) {
            Long total = session.createQuery("select count(o) from Objeto o", Long.class).uniqueResult();
            Long concluidos = session.createQuery("select count(o) from Objeto o where o.estado = 'Arquivado' or o.estado = 'Entregue' or o.estado = 'Concluído' or o.estado = 'Arquivado (Correspondido)'", Long.class).uniqueResult();
            if (total != null && total > 0) {
                System.out.printf(">> Estatística: %.2f%% dos objetos foram devolvidos/arquivados.\n", ((double) concluidos / total) * 100);
            } else {
                System.out.println("Sem dados suficientes para estatísticas.");
            }
        }
    }

    /**
     * Regista uma nova reclamação (tentativa de reaver um objeto) submetida por um cliente.
     * @param idObjeto o ID do objeto reclamado.
     * @param idUser o ID do cliente.
     * @param detalhes descrição da prova de posse fornecida pelo cliente.
     */
    public void submeterReclamacao(Long idObjeto, Long idUser, String detalhes) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Objeto o = session.get(Objeto.class, idObjeto);
            Utilizador u = session.get(Utilizador.class, idUser);
            
            if (o != null && u != null) {
                Reclamacao r = new Reclamacao(LocalDateTime.now(), detalhes, "Pendente");
                r.setObjeto(o); 
                r.setUtilizador(u);
                
                session.persist(r);
                t.commit();
                System.out.println("Reclamação submetida com sucesso! O objeto permanece na lista até decisão. (ID: " + r.getId() + ")");
            } else { 
                System.out.println("Erro: Objeto ou Cliente não encontrado na base de dados."); 
            }
        } catch (Exception e) {
            System.out.println("ERRO AO SUBMETER RECLAMAÇÃO: " + e.getMessage());
        }
    }

    /**
     * Lista para o Administrador todas as reclamações pendentes ativas a nível global.
     */
    public void listarReclamacoesPendentes() {
        try (Session session = sessionFactory.openSession()) {
            List<Reclamacao> recs = session.createQuery("from Reclamacao r where r.estado in ('Pendente', 'Aprovada') and r.objeto.estado != 'Arquivado'", Reclamacao.class).getResultList();
            
            if (recs.isEmpty()) {
                System.out.println("Sem reclamações ativas no sistema.");
            } else {
                for (Reclamacao r : recs) {
                    String objInfo = (r.getObjeto() != null) ? "Objeto ID " + r.getObjeto().getId() + " [Estado: " + r.getObjeto().getEstado() + "]" : "Sem Objeto";
                    String userInfo = (r.getUtilizador() != null) ? "Reclamante: " + r.getUtilizador().getNomeUtilizador() : "Desconhecido";
                    System.out.println("ID Reclamação: " + r.getId() + " | " + objInfo + " | " + userInfo + " | Provas: " + r.getDetalhes());
                }
            }
        }
    }

    /**
     * Lista reclamações, filtrando apenas aquelas cujo objeto se encontra no edifício do funcionário autenticado.
     * @param idFuncionario o ID do funcionário a executar a pesquisa.
     */
    public void listarReclamacoesPendentes(Long idFuncionario) {
        try (Session session = sessionFactory.openSession()) {
            Utilizador f = session.get(Utilizador.class, idFuncionario);
            
            if (f.getLocalTrabalho() == null) {
                System.out.println("Aviso: Não tem local de trabalho atribuído. Nenhuma reclamação disponível.");
                return;
            }
            
            Long localId = f.getLocalTrabalho().getId();

            List<Reclamacao> recs = session.createQuery("from Reclamacao r where r.estado in ('Pendente', 'Aprovada') and r.objeto.estado != 'Arquivado' and r.objeto.localRecolha.id = :lid", Reclamacao.class)
                                           .setParameter("lid", localId).getResultList();
            
            if (recs.isEmpty()) {
                System.out.println("Sem reclamações ativas para objetos do seu edifício (" + f.getLocalTrabalho().getNomeEdificio() + ").");
            } else {
                for (Reclamacao r : recs) {
                    String objInfo = (r.getObjeto() != null) ? "Objeto ID " + r.getObjeto().getId() + " [Estado: " + r.getObjeto().getEstado() + "]" : "Sem Objeto";
                    String userInfo = (r.getUtilizador() != null) ? "Reclamante: " + r.getUtilizador().getNomeUtilizador() : "Desconhecido";
                    System.out.println("ID Reclamação: " + r.getId() + " | " + objInfo + " | " + userInfo + " | Provas: " + r.getDetalhes());
                }
            }
        }
    }

    /**
     * Aprova as provas dadas pelo cliente, atribuindo-lhe a titularidade do objeto e rejeitando outras reclamações concorrentes.
     * @param idReclamacao o ID da reclamação.
     * @param idFuncionario o ID do funcionário que está a despachar.
     */
    public void aprovarReclamacao(Long idReclamacao, Long idFuncionario) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Reclamacao r = session.get(Reclamacao.class, idReclamacao);
            Utilizador f = session.get(Utilizador.class, idFuncionario);
            
            if (r != null && "Pendente".equals(r.getEstado())) {
                Objeto obj = r.getObjeto();
                
                if (f.getLocalTrabalho() == null || obj.getLocalRecolha() == null || !f.getLocalTrabalho().getId().equals(obj.getLocalRecolha().getId())) {
                    System.out.println("Erro: Esta reclamação pertence a um objeto de outro edifício. Não tem permissão para aprovar.");
                    return;
                }

                r.setEstado("Aprovada");
                
                if (obj != null) {
                    obj.setEstado("Autorizado para Levantamento");
                    obj.setUtilizador(r.getUtilizador()); 
                    
                    List<Reclamacao> outras = session.createQuery("from Reclamacao where objeto.id = :objId and estado = 'Pendente'", Reclamacao.class)
                                                     .setParameter("objId", obj.getId()).getResultList();
                    for(Reclamacao outra : outras) {
                        outra.setEstado("Rejeitada");
                        outra.setMotivoRejeicao("Outra reclamação foi aprovada como verdadeira para este objeto.");
                    }
                }
                t.commit();
                System.out.println("Reclamação Aprovada! O objeto está agora associado ao Cliente e pronto para Agendamento.");
            } else {
                System.out.println("Erro: Reclamação não encontrada ou já processada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao aprovar reclamação: " + e.getMessage());
        }
    }

    /**
     * Rejeita a reclamação se as provas fornecidas forem insuficientes ou incorretas.
     * @param idReclamacao o ID da reclamação.
     * @param motivo o texto explicativo da nega.
     * @param idFuncionario o funcionário que chumba o pedido.
     */
    public void rejeitarReclamacao(Long idReclamacao, String motivo, Long idFuncionario) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Reclamacao r = session.get(Reclamacao.class, idReclamacao);
            Utilizador f = session.get(Utilizador.class, idFuncionario);
            
            if (r != null && "Pendente".equals(r.getEstado())) {
                
                if (f.getLocalTrabalho() == null || r.getObjeto().getLocalRecolha() == null || !f.getLocalTrabalho().getId().equals(r.getObjeto().getLocalRecolha().getId())) {
                    System.out.println("Erro: Esta reclamação pertence a um objeto de outro edifício. Não tem permissão para rejeitar.");
                    return;
                }

                r.setEstado("Rejeitada");
                r.setMotivoRejeicao(motivo);
                t.commit();
                System.out.println("Reclamação Rejeitada com sucesso.");
            } else {
                System.out.println("Erro: Reclamação não encontrada ou já processada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao rejeitar reclamação: " + e.getMessage());
        }
    }

    /**
     * Reserva uma vaga horária para o cliente levantar o objeto. Conclui o processo e liberta a lotação da prateleira.
     * @param idObjeto o objeto a levantar.
     * @param idUser o cliente.
     * @param dataHora o dia e hora escolhidos (limite de 3 agendamentos por slot).
     */
    public void agendarRecolha(Long idObjeto, Long idUser, LocalDateTime dataHora) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            
            Objeto o = session.get(Objeto.class, idObjeto);
            if (o == null) {
                System.out.println("Erro: Objeto não encontrado.");
                return;
            }

            Long ocupados = session.createQuery("select count(a) from Agendamento a where a.dataHora = :dataHora", Long.class)
                                   .setParameter("dataHora", dataHora).uniqueResult();
            
            if (ocupados != null && ocupados >= 3) {
                System.out.println("Erro: Esta slot de horário atingiu a capacidade máxima (3). Escolha outra hora.");
                return;
            }
            
            if ("Encontrado".equalsIgnoreCase(o.getEstado()) || 
                "Autorizado para Levantamento".equalsIgnoreCase(o.getEstado()) || 
                "Correspondência Proposta".equalsIgnoreCase(o.getEstado())) {
                
                Agendamento a = new Agendamento(dataHora);
                a.setObjeto(o); 
                a.setUtilizador(session.get(Utilizador.class, idUser));
                session.persist(a);

                o.setEstado("Concluído");
                if (o.getObjetoAssociado() != null) {
                    o.getObjetoAssociado().setEstado("Concluído");
                }

                t.commit();
                System.out.println("Agendamento confirmado! O estado passou para 'Concluído' e o espaço na prateleira foi libertado.");
            } else {
                System.out.println("Erro: Não pode agendar a recolha! O estado atual do objeto é '" + o.getEstado() + "'.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao agendar: " + e.getMessage());
        }
    }

    /**
     * Mostra os objetos físicos que se encontram guardados e visíveis ao público.
     */
    public void listarObjetosDisponiveis() {
        pesquisarObjetos("from Objeto o where o.estado = 'Em Armazém' or o.estado = 'Aguardando Armazém'", null, null);
    }

    /**
     * Motor principal de execução de queries de listagem de objetos (DRY pattern).
     * @param sql a query formatada em HQL.
     * @param parametro o nome do bind parameter.
     * @param valor o valor a injetar na query.
     */
    public void pesquisarObjetos(String sql, String parametro, Object valor) {
        try (Session session = sessionFactory.openSession()) {
            var query = session.createQuery(sql, Objeto.class);
            if (parametro != null) query.setParameter(parametro, valor);
            List<Objeto> lista = query.getResultList();
            
            if (lista.isEmpty()) {
                System.out.println("Nenhum resultado encontrado.");
            } else {
                for (Objeto o : lista) {
                    System.out.println(o);
                }
            }
        }
    }

    /**
     * Filtra objetos recorrendo a pesquisa de texto livre por marca ou tipo.
     * @param kw a keyword/palavra.
     */
    public void pesquisaPalavraChave(String kw) { 
        pesquisarObjetos("from Objeto o where lower(o.marca) like :kw or lower(o.tipo) like :kw", "kw", "%" + kw.toLowerCase() + "%"); 
    }
    
    /**
     * Filtra objetos através da correspondência exata de categoria/tipo.
     * @param categoria a categoria pretendida.
     */
    public void pesquisaCategoria(String categoria) { 
        pesquisarObjetos("from Objeto o where lower(o.tipo) = :cat", "cat", categoria.toLowerCase()); 
    }
    
    /**
     * Imprime no ecrã um único objeto através da chave primária (ID).
     * @param id o ID do objeto.
     */
    public void pesquisaPorId(Long id) { 
        pesquisarObjetos("from Objeto o where o.id = :id", "id", id); 
    }
    
    /**
     * Realiza o fetch proativo de dados preguiçosos (Lazy Fetching) para impedir crashes de sessão inativa
     * nos menus de visualização.
     * @param id o ID do objeto.
     * @return o objeto Objeto preenchido com utilizador e locais.
     */
    public Objeto getObjetoInfo(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Objeto o = session.get(Objeto.class, id);
            if (o != null) {
                if(o.getUtilizador() != null) o.getUtilizador().getId();
                if(o.getLocalRecolha() != null) o.getLocalRecolha().getHoraInicio();
                if(o.getObjetoAssociado() != null && o.getObjetoAssociado().getLocalRecolha() != null) 
                    o.getObjetoAssociado().getLocalRecolha().getHoraInicio();
            }
            return o;
        }
    }
    
    /**
     * Compila e exibe o dashboard pessoal do cliente com as suas reclamações 
     * e estritamente os seus objetos registados como perdidos.
     * @param userId o ID do utilizador (cliente).
     */
    public void listarMeusProcessos(Long userId) { 
        try (Session session = sessionFactory.openSession()) {
            
            
            List<Objeto> meusObjetos = session.createQuery(
                "from Objeto o where o.utilizador.id = :id and o.estado in ('Perdido', 'Autorizado para Levantamento', 'Concluído')", Objeto.class)
                .setParameter("id", userId)
                .getResultList();
            
            
            List<Reclamacao> minhasReclamacoes = session.createQuery(
                "from Reclamacao r where r.utilizador.id = :id", Reclamacao.class)
                .setParameter("id", userId)
                .getResultList();

            if (meusObjetos.isEmpty() && minhasReclamacoes.isEmpty()) {
                System.out.println("Nenhum objeto perdido ou reclamação encontrado na sua conta.");
                return;
            }

            if (!meusObjetos.isEmpty()) {
                System.out.println("\n--- Os Meus Objetos Perdidos ---");
                for (Objeto o : meusObjetos) {
                    System.out.println(o);
                }
            }

            if (!minhasReclamacoes.isEmpty()) {
                System.out.println("\n--- As Minhas Reclamações ---");
                for (Reclamacao r : minhasReclamacoes) {
                    if (r.getObjeto() != null) {
                        String infoReclamacao = "Reclamação ID: " + r.getId() + " (" + r.getEstado() + ") -> " + r.getObjeto();
                        
                        
                        if ("Rejeitada".equalsIgnoreCase(r.getEstado()) && r.getMotivoRejeicao() != null) {
                            infoReclamacao += " | Motivo: " + r.getMotivoRejeicao();
                        }
                        System.out.println(infoReclamacao);
                    }
                }
            }
        }
    }

    /** Devolve a lista global de edifícios */
    public List<Local> getLocais() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Local", Local.class).getResultList(); }
    }

    /** Devolve a lista global de armazéns */
    public List<Armazem> getArmazens() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Armazem", Armazem.class).getResultList(); }
    }

    /** Devolve a lista global de prateleiras */
    public List<Prateleira> getPrateleiras() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Prateleira", Prateleira.class).getResultList(); }
    }

    /** Devolve a lista global de contas de utilizador (Admins, Funcionários, Clientes) */
    public List<Utilizador> getUtilizadores() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Utilizador", Utilizador.class).getResultList(); }
    }

    /** Devolve o histórico completo de objetos já registados */
    public List<Objeto> getTodosObjetos() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Objeto", Objeto.class).getResultList(); }
    }

    /**
     * Validador de teclado. Pede e devolve em modo "safe" um número Integer lido da consola. 
     * Impede crashes do sistema causados por letras ou caracteres incorretos.
     * @param scanner a instância ativa da class java.util.Scanner.
     * @return o número capturado ou -1 em caso de erro.
     */
    public int lerInt(Scanner scanner) {
        String texto = scanner.nextLine().trim();
        if (texto.matches("[0-9]+")) {
            try { return Integer.parseInt(texto); } catch (NumberFormatException e) { return -1; }
        }
        return -1;
    }

    /**
     * Validador de teclado para números grandes (identificadores).
     * @param scanner a instância ativa da class java.util.Scanner.
     * @return o número (Long) capturado ou -1L em caso de erro.
     */
    public Long lerLong(Scanner scanner) {
        String texto = scanner.nextLine().trim();
        if (texto.matches("[0-9]+")) {
            try { return Long.parseLong(texto); } catch (NumberFormatException e) { return -1L; }
        }
        return -1L;
    }

    /**
     * Validador de teclado que assegura o preenchimento obrigatório de campos.
     * Entra em loop bloqueante até o utilizador escrever texto e não apenas premir Enter vazio.
     * @param scanner a instância do scanner.
     * @param mensagem a label a perguntar ao utilizador.
     * @return a String validada.
     */
    public String lerTextoObrigatorio(Scanner scanner, String mensagem) {
        String texto = "";
        while (texto.isEmpty()) {
            System.out.print(mensagem);
            texto = scanner.nextLine().trim();
            if (texto.isEmpty()) {
                System.out.println("Erro: Este campo não pode ficar vazio.");
            }
        }
        return texto;
    }
    
    /**
     * JOB AUTOMÁTICO (Despoletado no Login). 
     * Lê todos os objetos de cacifos. Se o tempo da data do sistema face à data original do objeto for > 30 dias,
     * retira a lotação da prateleira e lança o objeto para a fase final do Armazém com o estado "Expirado".
     */
    public void verificarObjetosExpirados() {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            
            List<Objeto> objetos = session.createQuery("from Objeto o where o.prateleira is not null and o.estado not in ('Concluído', 'Arquivado (Correspondido)', 'Doado/Eliminado')", Objeto.class).getResultList();
            
            LocalDateTime hoje = LocalDateTime.now();
            int movidos = 0;
            
            for (Objeto o : objetos) {
                if (o.getData() != null && java.time.temporal.ChronoUnit.DAYS.between(o.getData(), hoje) > 30) {
                    o.setPrateleira(null); 
                    o.setEstado("Expirado (No Armazém)"); 
                    session.merge(o);
                    movidos++;
                }
            }
            t.commit();
            
            if (movidos > 0) {
                System.out.println("\n[SISTEMA AUTOMÁTICO] " + movidos + " objetos ultrapassaram o tempo limite nos cacifos e foram movidos para o Armazém para serem eliminados.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao verificar tempo dos objetos: " + e.getMessage());
        }
    }
    
    /**
     * Remove ou doa definitivamente objetos órfãos ou expirados, libertando o armazém.
     * Exclusivo à atuação do funcionário.
     * @param idObjeto o ID do objeto.
     * @param idFuncionario o funcionário que autoriza a saída.
     */
    public void doarOuEliminarObjeto(Long idObjeto, Long idFuncionario) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            Objeto o = session.get(Objeto.class, idObjeto);
            Utilizador f = session.get(Utilizador.class, idFuncionario);

            if (o != null && f != null) {
                if ("Expirado (No Armazém)".equals(o.getEstado()) || "Em Armazém".equals(o.getEstado())) {
                    o.setEstado("Doado/Eliminado");
                    o.setPrateleira(null);
                    o.setLocalRecolha(null);
                    session.merge(o);
                    t.commit();
                    System.out.println("Sucesso: O Objeto ID " + idObjeto + " foi Doado/Eliminado e o armazém foi esvaziado!");
                } else {
                    System.out.println("Erro: Não pode eliminar este objeto. O estado atual é '" + o.getEstado() + "'.");
                }
            } else {
                System.out.println("Erro: Objeto ou Funcionário não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao eliminar/doar objeto: " + e.getMessage());
        }
    }
    
    /** 
     * Mostra a lista de objetos que excederam o tempo (30 dias) e estão no armazém 
     * a aguardar que o funcionário os elimine ou doe.
     */
    public void listarObjetosExpirados() {
        System.out.println("\n--- OBJETOS EXPIRADOS (A AGUARDAR ELIMINAÇÃO/DOAÇÃO) ---");
        pesquisarObjetos("from Objeto o where o.estado = 'Expirado (No Armazém)'", null, null);
    }
    
    /** 
     * Mostra apenas a lista de tudo o que os clientes reportaram ter perdido.
     * Útil para o funcionário consultar antes de fazer associações.
     */
    public void listarObjetosPerdidos() {
        try (Session session = sessionFactory.openSession()) {
            List<Objeto> lista = session.createQuery("from Objeto o where o.estado = 'Perdido'", Objeto.class).getResultList();
            
            if (lista.isEmpty()) {
                System.out.println("Nenhum objeto perdido reportado no sistema.");
            } else {
                System.out.println("\n--- LISTA DE OBJETOS PERDIDOS (REPORTADOS PELOS CLIENTES) ---");
                for (Objeto o : lista) {
                    String dono = (o.getUtilizador() != null) ? o.getUtilizador().getNomeUtilizador() : "Desconhecido";
                    System.out.println(o + " | Dono: " + dono);
                }
            }
        }
    }

    /** 
     * Encerra a ligação e o contexto global do Hibernate. A executar antes do programa desligar.
     */
    public void fecharLigacao() { 
        if (sessionFactory != null) sessionFactory.close(); 
    }
}