package upt.pt;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

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
     * desfaz a juncao dos dois objetos
     * @param id o numero do objeto
     */
    public void rejeitarCorrespondencia(Long id) { 
        atualizarEstadoObjeto(id, "Em Armazém", -1L); 
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
}
