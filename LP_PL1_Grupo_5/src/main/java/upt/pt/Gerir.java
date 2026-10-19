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
