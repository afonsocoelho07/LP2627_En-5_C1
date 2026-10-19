package upt.pt;

public class Teste {

	public static void main(String[] args) {
		
	}
		/** 
	     * mostra e controla as opcoes da pessoa normal
	     * @param gerir ligacao a base de dados
	     * @param scanner le as teclas
	     * @param utilizador quem entrou no sistema
	     * @return devolve a conta ou nulo para sair
	     */
	    private static Utilizador menuUtilizador(Gerir gerir, Scanner scanner, Utilizador utilizador) {
	        int opcao = -1;
	        
	        while (opcao != 0) {
	            System.out.println("\n--- MENU UTILIZADOR ---");
	            System.out.println("1. Registar Objeto Perdido");
	            System.out.println("2. Entregar Objeto Encontrado no Edifício");
	            System.out.println("3. Listar Objetos Achados/Disponíveis");
	            System.out.println("4. Pesquisar por Palavra-Chave");
	            System.out.println("5. Filtrar por Categoria (Tipo)");
	            System.out.println("6. Submeter Reclamação de Posse (Reivindicar Objeto)");
	            System.out.println("7. Agendar Recolha (Para os seus objetos autorizados)");
	            System.out.println("8. Consultar Meus Processos / Reclamações");
	            System.out.println("9. Listar Objetos Perdidos");
	            System.out.println("0. Fazer Logout");
	            
	            System.out.print("Escolha: ");
	            opcao = gerir.lerInt(scanner);

	            switch (opcao) {
	                
	                case 1:
	                    String marcaPerdido = gerir.lerTextoObrigatorio(scanner, "Marca: ");
	                    String tipoPerdido = gerir.lerTextoObrigatorio(scanner, "Tipo (ex: Eletronica): ");
	                    String corPerdida = gerir.lerTextoObrigatorio(scanner, "Cor: ");
	                    
	                    gerir.adicionarObjeto(LocalDateTime.now(), marcaPerdido, tipoPerdido, corPerdida, "Perdido", utilizador.getId(), null, null);
	                    System.out.println("Registado como Perdido!"); 
	                    break;
	                    
	                case 2:
	                    if (gerir.getLocais().isEmpty()) {
	                        System.out.println("Erro: Ainda não existem locais de recolha criados.");
	                        break;
	                    }
	                    
	                    String marcaEncontrado = gerir.lerTextoObrigatorio(scanner, "Marca: ");
	                    String tipoEncontrado = gerir.lerTextoObrigatorio(scanner, "Tipo: ");
	                    String corEncontrada = gerir.lerTextoObrigatorio(scanner, "Cor: ");
	                    
	                    System.out.println("--- Locais de Recolha Disponíveis ---");
	                    for(Local local : gerir.getLocais()) {
	                        System.out.println("ID: " + local.getId() + " - " + local.getNomeEdificio());
	                    }
	                    
	                    System.out.print("ID do Edifício onde entregou o objeto: ");
	                    Long idLocal = gerir.lerLong(scanner);
	                    if (idLocal == -1L) break;
	                    
	                    gerir.adicionarObjetoEncontrado(LocalDateTime.now(), marcaEncontrado, tipoEncontrado, corEncontrada, utilizador.getId(), idLocal);
	                    break;
	                    
	                case 3:
	                    gerir.listarObjetosDisponiveis(); 
	                    break;
	                    
	                case 4:
	                    String palavraChave = gerir.lerTextoObrigatorio(scanner, "Palavra-Chave: ");
	                    gerir.pesquisaPalavraChave(palavraChave); 
	                    break;
	                    
	                case 5:
	                    String categoria = gerir.lerTextoObrigatorio(scanner, "Categoria: ");
	                    gerir.pesquisaCategoria(categoria); 
	                    break;
	                    
	                case 6:
	                    System.out.print("ID do Objeto a Reclamar: ");
	                    Long idReclamar = gerir.lerLong(scanner);
	                    if (idReclamar == -1L) break;
	                    
	                    String detalhes = gerir.lerTextoObrigatorio(scanner, "Detalhes/Provas (ex: capa amarela, rachadela no ecrã): ");
	                    gerir.submeterReclamacao(idReclamar, utilizador.getId(), detalhes); 
	                    break;
	                    
	                case 7:
	                    System.out.print("ID do Objeto: ");
	                    Long idAgendar = gerir.lerLong(scanner);
	                    if (idAgendar == -1L) break;
	                    
	                    Objeto objAgendar = gerir.getObjetoInfo(idAgendar);
	                    if (objAgendar == null) {
	                        System.out.println("Erro: Objeto não encontrado.");
	                        break;
	                    }
	                    
	                    boolean meuObjeto = (objAgendar.getUtilizador() != null && objAgendar.getUtilizador().getId().equals(utilizador.getId()));
	                    if (!meuObjeto) {
	                        System.out.println("Erro: Não tem permissão para agendar a recolha de um objeto que não é o seu.");
	                        break;
	                    }
	                    
	                    if (!"Encontrado".equalsIgnoreCase(objAgendar.getEstado()) && 
	                        !"Autorizado para Levantamento".equalsIgnoreCase(objAgendar.getEstado()) && 
	                        !"Correspondência Proposta".equalsIgnoreCase(objAgendar.getEstado())) {
	                        System.out.println("Erro: Não pode agendar a recolha! O estado atual do objeto é '" + objAgendar.getEstado() + "'.");
	                        break;
	                    }
	                    
	                    String horaInicioStr = "09:00";
	                    String horaFimStr = "18:00";
	                    
	                    if (objAgendar.getLocalRecolha() != null) {
	                        horaInicioStr = objAgendar.getLocalRecolha().getHoraInicio();
	                        horaFimStr = objAgendar.getLocalRecolha().getHoraFim();
	                    } else if (objAgendar.getObjetoAssociado() != null && objAgendar.getObjetoAssociado().getLocalRecolha() != null) {
	                        horaInicioStr = objAgendar.getObjetoAssociado().getLocalRecolha().getHoraInicio();
	                        horaFimStr = objAgendar.getObjetoAssociado().getLocalRecolha().getHoraFim();
	                    }
	                    
	                    // tenta limpar a hora que a pessoa escreve para isto nao rebentar
	                    int baseStartH = 9, baseStartM = 0, baseEndH = 18, baseEndM = 0;
	                    try {
	                        String[] sParts = horaInicioStr.split(":");
	                        baseStartH = Integer.parseInt(sParts[0].trim());
	                        if (sParts.length > 1) baseStartM = Integer.parseInt(sParts[1].trim());

	                        String[] eParts = horaFimStr.split(":");
	                        baseEndH = Integer.parseInt(eParts[0].trim());
	                        if (eParts.length > 1) baseEndM = Integer.parseInt(eParts[1].trim());
	                    } catch (Exception e) {}

	                    java.time.LocalTime tInicio = java.time.LocalTime.of(baseStartH, baseStartM);
	                    java.time.LocalTime tFim = java.time.LocalTime.of(baseEndH, baseEndM);
	                    String labelHorario = String.format("%02d:%02d às %02d:%02d", baseStartH, baseStartM, baseEndH, baseEndM);

	                    java.time.LocalTime horaDigitada = null;
	                    
	                    while (true) {
	                        String inputHora = gerir.lerTextoObrigatorio(scanner, "Hora da recolha (Horário do edifício: " + labelHorario + ") [HH:MM]: ");
	                        try {
	                            String[] p = inputHora.split(":");
	                            int h = Integer.parseInt(p[0].trim());
	                            int m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
	                            horaDigitada = java.time.LocalTime.of(h, m);

	                            if (horaDigitada.isBefore(tInicio) || horaDigitada.isAfter(tFim)) {
	                                System.out.println("Erro: A hora tem de ser obrigatoriamente entre " + labelHorario + " (ex: 14:30). Tente de novo.");
	                            } else {
	                                break;
	                            }
	                        } catch (Exception e) {
	                            System.out.println("Erro: Formato de hora inválido. Use os dois pontos como separador (ex: 14:30).");
	                        }
	                    }
	                    
	                    LocalDateTime dataRecolha = LocalDateTime.now().withHour(horaDigitada.getHour()).withMinute(horaDigitada.getMinute());
	                    gerir.agendarRecolha(idAgendar, utilizador.getId(), dataRecolha); 
	                    break;
	                    
	                case 8:
	                    System.out.println("MEUS PROCESSOS:"); 
	                    gerir.listarMeusProcessos(utilizador.getId()); 
	                    break;

	                case 9:
	                    System.out.println("--- OBJETOS PERDIDOS ---");
	                    gerir.listarObjetosPerdidos();
	                    break;
	                    
	                case 0: 
	                    return null;
	                    
	                default:
	                    System.out.println("Opção inválida! Tente novamente.");
	                    break;
	            }
	        }
	        return utilizador;
	        
	}

}
