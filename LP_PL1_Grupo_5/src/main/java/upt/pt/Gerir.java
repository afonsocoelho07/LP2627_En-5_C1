package upt.pt;

public class Gerir {
	
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
     * procura palavras soltas no sistema
     * @param kw a palavra
     */
    public void pesquisaPalavraChave(String kw) { 
        pesquisarObjetos("from Objeto o where lower(o.marca) like :kw or lower(o.tipo) like :kw", "kw", "%" + kw.toLowerCase() + "%"); 
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
    
     /*
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
    
}
