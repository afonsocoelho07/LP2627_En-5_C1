
package upt.pt;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/** 
 * classe principal que contem os metedos todos
 */
public class Gerir {

    private SessionFactory sessionFactory;

    /** 
     * arranca a ligacao a base de dados 
     */
    public Gerir() {
        this.sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
    }

    /** 
     * ve se o email e a palavra passe estao certos para deixar a pessoa entrar
     * @param identificador o email ou o telemovel da pessoa
     * @param password a palavra passe
     * @return devolve a conta da pessoa se tiver tudo bem ou null se falhar
     */
    public Utilizador fazerLogin(String identificador, String password) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Utilizador u where (u.email = :id or cast(u.contacto as string) = :id) and u.password = :pass", Utilizador.class)
                          .setParameter("id", identificador)
                          .setParameter("pass", password)
                          .uniqueResult();
        }
    }

    /** 
     * ve se o email ou telemovel ja existem na base de dados para nao haver contas repetidas
     * @param email o email que a pessoa escreveu
     * @param contacto o telemovel que a pessoa escreveu
     * @return devolve true se ja existir e false se for novidade
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
     * cria uma conta nova e guarda
     * @param tipo se e admin funcionario ou utilizador normal
     * @param pass a palavra passe escolhida
     * @param nome o nome da pessoa
     * @param email o email
     * @param contacto o numero de telemovel
     */
    public void adicionarUtilizador(String tipo, String pass, String nome, String email, int contacto) {
        guardar(new Utilizador(tipo, pass, nome, email, contacto));
    }

    /** 
     * mostra no ecra a lista de pessoas dependendo do cargo delas
     * @param tipo o cargo que queremos ver
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
     * apaga a conta de alguem e limpa tudo o que essa pessoa fez para a base de dados nao dar erro
     * @param id o numero da conta da pessoa a apagar
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
                System.out.println("Erro: ID de utilizador não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao remover utilizador: " + e.getMessage());
        }
    }

    /** 
     * serve para guardar na base de dados
     * @param obj a coisa que queremos guardar
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
     * cria um novo edificio
     * @param edificio nome do edificio
     * @param descricao informacao sobre o local
     * @param horaInicio hora que o edificio abre
     * @param horaFim hora que o edificio fecha
     */
    public void adicionarLocal(String edificio, String descricao, String horaInicio, String horaFim) { 
        guardar(new Local(edificio, descricao, horaInicio, horaFim)); 
    }
    
    /** 
     * cria um armazem novo mas verifica primeiro se o nome ja existe para nao haver iguais
     * @param nome como se vai chamar o armazem
     * @param localId numero do edificio onde o armazem vai ficar
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
     * cria uma prateleira e diz logo quantos objetos cabem la
     * @param cod letras ou numeros da prateleira
     * @param lotacao quantos objetos cabem no maximo
     * @param armazemId numero do armazem onde ela fica
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
     * diz ao programa em que edificio o funcionario trabalha
     * @param userId o numero da conta do funcionario
     * @param localId o numero do edificio
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
                System.out.println("Erro: Utilizador ou Local não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao atribuir funcionário: " + e.getMessage());
        }
    }

    /** 
     * mostra no ecra todos os edificios e faz as contas de espaco ocupado nas prateleiras
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
                                Long ocupados = session.createQuery("select count(o) from Objeto o where o.prateleira.id = :idPrat and o.estado not in ('Concluído', 'Arquivado (Correspondido)', 'Arquivado')", Long.class)
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
     * guarda um objeto novo e verifica logo se a prateleira escolhida esta cheia e bloqueia se estiver
     * @param data o dia e a hora
     * @param marca a marca da coisa
     * @param tipo que tipo de coisa e
     * @param cor a cor
     * @param estado em que pe esta a situacao do objeto
     * @param idUser quem o esta a registar
     * @param idPrat em que prateleira vai ficar
     * @param idLocal de que edificio e
     */
    public void adicionarObjeto(LocalDateTime data, String marca, String tipo, String cor, String estado, Long idUser, Long idPrat, Long idLocal) {
        try (Session session = sessionFactory.openSession()) {
            Transaction t = session.beginTransaction();
            
            Prateleira p = null;
            if (idPrat != null) {
                p = session.get(Prateleira.class, idPrat);
                if (p != null) {
                    Long ocupados = session.createQuery("select count(o) from Objeto o where o.prateleira.id = :idPrat and o.estado not in ('Arquivado', 'Concluído', 'Arquivado (Correspondido)')", Long.class)
                                           .setParameter("idPrat", idPrat).uniqueResult();
                    long atual = (ocupados != null) ? ocupados : 0;
                    if (atual >= p.getLotacaoPrateleira()) {
                        System.out.println("Erro: A prateleira " + p.getCodigoPrateleira() + " já atingiu a lotação máxima (" + atual + "/" + p.getLotacaoPrateleira() + "). Escolha outra.");
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
     * guarda a coisa achada mas sabe logo onde o funcionario trabalha para nao lhe perguntar
     * @param data o dia e a hora
     * @param marca a marca da coisa
     * @param tipo que tipo de coisa e
     * @param cor a cor
     * @param idUser o numero do funcionario
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
     * guarda a coisa achada para quem e utilizador normal porque o programa tem de lhe perguntar onde a deixou
     * @param data o dia e a hora
     * @param marca a marca da coisa
     * @param tipo que tipo de coisa e
     * @param cor a cor
     * @param idUser o numero de quem achou
     * @param idLocal onde a pessoa deixou a coisa
     */
    public void adicionarObjetoEncontrado(LocalDateTime data, String marca, String tipo, String cor, Long idUser, Long idLocal) {
        try (Session session = sessionFactory.openSession()) {
            Local local = session.get(Local.class, idLocal);
            if (local == null) {
                System.out.println("Erro: Local não encontrado na base de dados.");
                return;
            }
            Utilizador u = session.get(Utilizador.class, idUser);
            
            registarAchadoNaPrateleira(session, data, marca, tipo, cor, u, local, false);
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    /** 
     * ve as prateleiras todas e guarda a coisa automaticamente na que tiver mais lugar
     * @param session ligacao ativa a base de dados
     * @param data o dia e a hora
     * @param marca a marca
     * @param tipo o tipo
     * @param cor a cor
     * @param u quem esta a registar
     * @param local o edificio
     * @param isFuncionario se foi o funcionario ou o utilizador normal
     */
    private void registarAchadoNaPrateleira(Session session, LocalDateTime data, String marca, String tipo, String cor, Utilizador u, Local local, boolean isFuncionario) {
        Transaction t = session.beginTransaction();
        Prateleira melhorPrateleira = null;
        long menorOcupacao = Long.MAX_VALUE;

        for (Armazem a : local.getArmazens()) {
            for (Prateleira p : a.getPrateleiras()) {
                Long ocupados = session.createQuery("select count(o) from Objeto o where o.prateleira.id = :idPrat and o.estado not in ('Concluído', 'Arquivado (Correspondido)', 'Arquivado')", Long.class)
                                       .setParameter("idPrat", p.getId()).uniqueResult();
                long atual = (ocupados != null) ? ocupados : 0;

                if (atual < p.getLotacaoPrateleira() && atual < menorOcupacao) {
                    menorOcupacao = atual;
                    melhorPrateleira = p;
                }
            }
        }

        Objeto obj = new Objeto(data, marca, tipo, cor, "Em Armazém");
        if (u != null) obj.setUtilizador(u);
        obj.setLocalRecolha(local);

        if (melhorPrateleira != null) {
            obj.setPrateleira(melhorPrateleira);
        } else {
            obj.setEstado("Aguardando Armazém");
        }

        session.persist(obj);
        t.commit();
        
        if (isFuncionario) {
            System.out.println("Registado de forma associada ao seu local (" + local.getNomeEdificio() + ").");
        } else {
            System.out.println("Registado e entregue no local " + local.getNomeEdificio() + ".");
        }
        
        if (melhorPrateleira != null) {
            System.out.println("Colocado AUTOMATICAMENTE na Prateleira " + melhorPrateleira.getCodigoPrateleira() + ".");
        } else {
            System.out.println("Aviso: Nenhuma prateleira com espaço disponível neste local. Objeto está 'Aguardando Armazém'.");
        }
    }

    /** 
     * serve so para ir mudar a palavra de estado do objeto para outra coisa
     * @param id qual o objeto
     * @param novoEstado o estado novo a colocar
     * @param idAssociado o objeto que faz par com este
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
     * da o processo como encerrado de vez
     * @param idObjeto o numero do objeto
     */
    public void arquivarProcesso(Long idObjeto) { 
        atualizarEstadoObjeto(idObjeto, "Arquivado", null); 
    }
    
    /** 
     * junta um objeto perdido com um achado mas o funcionario so pode fazer isto no edificio dele
     * @param idPerdido numero do objeto perdido
     * @param idAchado numero do objeto achado
     * @param idFuncionario numero do funcionario que esta a fazer isto
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
     * desfaz a juncao dos dois objetos
     * @param id o numero do objeto
     */
    public void rejeitarCorrespondencia(Long id) { 
        atualizarEstadoObjeto(id, "Em Armazém", -1L); 
    }

    /** 
     * faz a conta para o admin ver a percentagem de coisas devolvidas
     */
    public void calcularEstatisticaEntregas() {
        try (Session session = sessionFactory.openSession()) {
            Long total = session.createQuery("select count(o) from Objeto o", Long.class).uniqueResult();
            Long concluidos = session.createQuery("select count(o) from Objeto o where o.estado = 'Arquivado' or o.estado = 'Entregue' or o.estado = 'Concluído'", Long.class).uniqueResult();
            if (total != null && total > 0) {
                System.out.printf(">> Estatística: %.2f%% dos objetos foram devolvidos/arquivados.\n", ((double) concluidos / total) * 100);
            } else {
                System.out.println("Sem dados suficientes para estatísticas.");
            }
        }
    }

    /** 
     * a pessoa diz que o objeto e dela e o sistema guarda esse pedido para ser avaliado
     * @param idObjeto o que a pessoa diz ser dela
     * @param idUser quem e a pessoa
     * @param detalhes como a pessoa prova que e dela
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
                System.out.println("Erro: Objeto ou Utilizador não encontrado na base de dados."); 
            }
        } catch (Exception e) {
            System.out.println("ERRO AO SUBMETER RECLAMAÇÃO: " + e.getMessage());
        }
    }

    /** 
     * mostra para o administrador tudo o que as pessoas estao a tentar reclamar
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
     * mostra para o funcionario mas so aquilo que pertence ao edificio onde ele trabalha
     * @param idFuncionario o id do funcionario que quer ver
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
     * o funcionario diz que as provas estao certas e a pessoa fica como dona do objeto mas so do seu edificio
     * @param idReclamacao numero da reclamacao
     * @param idFuncionario numero do funcionario que da a aprovacao
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
                System.out.println("Reclamação Aprovada! O objeto está agora associado ao utilizador e pronto para Agendamento.");
            } else {
                System.out.println("Erro: Reclamação não encontrada ou já processada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao aprovar reclamação: " + e.getMessage());
        }
    }

    /** 
     * o funcionario diz que as provas sao fracas e rejeita mas so do seu edificio
     * @param idReclamacao numero do pedido
     * @param motivo texto a explicar porque chumbou
     * @param idFuncionario quem e o funcionario
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
     * a pessoa marca hora para ir buscar e o objeto liberta o espaco da prateleira na hora
     * @param idObjeto o que vai buscar
     * @param idUser quem vai buscar
     * @param dataHora que dia e hora
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
     * mostra as coisas que estao arrumadas no armazem a espera do dono
     */
    public void listarObjetosDisponiveis() {
        pesquisarObjetos("from Objeto o where o.estado = 'Em Armazém' or o.estado = 'Aguardando Armazém'", null, null);
    }

    /** 
     * mostra apenas a lista de tudo o que as pessoas reportaram ter perdido
     */
    public void listarObjetosPerdidos() {
        try (Session session = sessionFactory.openSession()) {
            List<Objeto> lista = session.createQuery("from Objeto o where o.estado = 'Perdido'", Objeto.class).getResultList();
            
            if (lista.isEmpty()) {
                System.out.println("Nenhum objeto perdido encontrado.");
            } else {
                for (Objeto o : lista) {
                    String dono = (o.getUtilizador() != null) ? o.getUtilizador().getNomeUtilizador() : "Desconhecido";
                    System.out.println(o + " | Dono: " + dono);
                }
            }
        }
    }

    /** 
     * pesquisa na base de dados
     * @param sql o codigo de pesquisa
     * @param parametro a palavra de filtro
     * @param valor o valor procurado
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
     * procura palavras soltas no sistema
     * @param kw a palavra
     */
    public void pesquisaPalavraChave(String kw) { 
        pesquisarObjetos("from Objeto o where lower(o.marca) like :kw or lower(o.tipo) like :kw", "kw", "%" + kw.toLowerCase() + "%"); 
    }
    
    /** 
     * procura mesmo a categoria especifica
     * @param categoria palavra a pesquisar
     */
    public void pesquisaCategoria(String categoria) { 
        pesquisarObjetos("from Objeto o where lower(o.tipo) = :cat", "cat", categoria.toLowerCase()); 
    }
    
    /** 
     * procura direitinho o numero do objeto
     * @param id numero a procurar
     */
    public void pesquisaPorId(Long id) { 
        pesquisarObjetos("from Objeto o where o.id = :id", "id", id); 
    }
    
    /** 
     * procura o objeto e prepara os dados de horarios e dono para os menus nao crasharem
     * @param id o numero do objeto
     * @return devolve a coisa com as pecas todas atadas
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
     * diz ao utilizador em que pe estao as coisas dele e se algo chumbou diz o porque
     * @param userId o id de quem quer ver
     */
    public void listarMeusProcessos(Long userId) { 
        try (Session session = sessionFactory.openSession()) {
            List<Objeto> meusObjetos = session.createQuery("from Objeto o where o.utilizador.id = :id and o.estado != 'Arquivado (Correspondido)'", Objeto.class)
                                              .setParameter("id", userId)
                                              .getResultList();
            
            List<Reclamacao> minhasReclamacoes = session.createQuery("from Reclamacao r where r.utilizador.id = :id", Reclamacao.class)
                                                        .setParameter("id", userId)
                                                        .getResultList();

            if (meusObjetos.isEmpty() && minhasReclamacoes.isEmpty()) {
                System.out.println("Nenhum processo ou reclamação encontrado.");
                return;
            }

            if (!meusObjetos.isEmpty()) {
                System.out.println("--- Objetos Registados / Adquiridos por Mim ---");
                for (Objeto o : meusObjetos) {
                    System.out.println(o);
                }
            }

            if (!minhasReclamacoes.isEmpty()) {
                System.out.println("--- Minhas Reclamações ---");
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

    /** 
     * devolve lista dos edificios para por menus mais faceis
     * @return lista com eles
     */
    public List<Local> getLocais() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Local", Local.class).getResultList(); }
    }

    /** 
     * devolve a lista dos armazens todos
     * @return lista deles
     */
    public List<Armazem> getArmazens() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Armazem", Armazem.class).getResultList(); }
    }

    /** 
     * devolve as prateleiras
     * @return prateleiras
     */
    public List<Prateleira> getPrateleiras() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Prateleira", Prateleira.class).getResultList(); }
    }

    /** 
     * vai buscar os utilizadores normais os funcionarios e admins
     * @return lista de malta registada
     */
    public List<Utilizador> getUtilizadores() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Utilizador", Utilizador.class).getResultList(); }
    }

    /** 
     * puxa todos os objetos globais da bd
     * @return lista gigante de coisas
     */
    public List<Objeto> getTodosObjetos() { 
        try (Session s = sessionFactory.openSession()) { return s.createQuery("from Objeto", Objeto.class).getResultList(); }
    }

    /** 
     * ajuda o programa a nao ir abaixo quando em vez de um numero metem letras ao escolher os menus
     * @param scanner que le o teclado
     * @return devolve o numero e se der asneira diz que deu erro para nao rebentar
     */
    public int lerInt(Scanner scanner) {
        String texto = scanner.nextLine().trim();
        if (texto.matches("[0-9]+")) {
            try { return Integer.parseInt(texto); } catch (NumberFormatException e) { return -1; }
        }
        return -1;
    }

    /** 
     * protege para quando a pessoa insere os codigos id que as vezes sao muito longos
     * @param scanner le do teclado
     * @return devolve o codigo grande
     */
    public Long lerLong(Scanner scanner) {
        String texto = scanner.nextLine().trim();
        if (texto.matches("[0-9]+")) {
            try { return Long.parseLong(texto); } catch (NumberFormatException e) { return -1L; }
        }
        return -1L;
    }

    /** 
     * se alguem so carregar no enter e ficar vazio isto chateia ate meterem texto verdadeiro
     * @param scanner teclado
     * @param mensagem pergunta que o sistema faz
     * @return devolve a resposta final certinha
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
     * arruma e acaba a ligacao a base de dados
     */
    public void fecharLigacao() { 
        if (sessionFactory != null) sessionFactory.close(); 
    }
}

