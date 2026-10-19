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

    
}
