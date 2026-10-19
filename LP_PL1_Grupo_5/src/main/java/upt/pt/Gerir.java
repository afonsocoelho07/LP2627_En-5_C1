package upt.pt;

public class Gerir {
	
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
    
}
