package upt.pt;

public class Gerir {
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
