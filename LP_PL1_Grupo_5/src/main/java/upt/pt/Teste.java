package upt.pt;

import java.util.Scanner;

public class Teste {

	/** 
     * aqui e onde o programa comeca a correr e pede o login ou para criar conta
     * @param args coisas obrigatorias do java que nao usamos
     */
    public static void main(String[] args) {
        Gerir gerir = new Gerir();
        Scanner scanner = new Scanner(System.in); 
        Utilizador utilizadorLogado = null;
        int opcao = -1;

        while (opcao != 0) {
            
            if (utilizadorLogado == null) {
                System.out.println("\n--- BEM-VINDO AO ACHADOS E PERDIDOS ---");
                System.out.println("1. Login");
                System.out.println("2. Criar Conta");
                System.out.println("0. Sair");
                
                System.out.print("Opção: ");
                opcao = gerir.lerInt(scanner);

                if (opcao == 1) {
                    System.out.print("Email ou Contacto: "); 
                    String idLogin = scanner.nextLine().trim();
                    
                    System.out.print("Password: "); 
                    String password = scanner.nextLine();
                    
                    utilizadorLogado = gerir.fazerLogin(idLogin, password);
                    
                    if (utilizadorLogado == null) {
                        System.out.println("Erro: Credenciais Inválidas!");
                    } else {
                        System.out.println("Login com sucesso! Perfil: " + utilizadorLogado.getTipoUtilizador());
                    }
                    
                } else if (opcao == 2) {
                    System.out.print("Email (obrigatório ter @): "); 
                    String email = scanner.nextLine().trim();
                    if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) { 
                        System.out.println("Erro: Email inválido."); 
                        continue; 
                    }
                    
                    System.out.print("Contacto (9 números): ");
                    String contactoStr = scanner.nextLine().trim();
                    
                    if (!contactoStr.matches("[0-9]{9}")) { 
                        System.out.println("Erro: Contacto inválido."); 
                        continue; 
                    }
                    int contacto = Integer.parseInt(contactoStr);
                    
                    if (gerir.existeEmailOuContacto(email, contacto)) { 
                        System.out.println("Erro: Conta já existe."); 
                        continue; 
                    }
                    
                    String nome = gerir.lerTextoObrigatorio(scanner, "Nome: ");
                    
                    System.out.print("Tipo (Admin, Funcionario, Utilizador): "); 
                    String tipo = scanner.nextLine().trim();
                    if (!tipo.equalsIgnoreCase("Admin") && !tipo.equalsIgnoreCase("Funcionario") && !tipo.equalsIgnoreCase("Utilizador")) {
                        System.out.println("Erro: Tipo inválido."); 
                        continue;
                    }
                    
                    String password = gerir.lerTextoObrigatorio(scanner, "Password: ");
                    
                    gerir.adicionarUtilizador(tipo, password, nome, email, contacto);
                    System.out.println("Conta criada!");
                    
                } else if (opcao == 0) { 
                    gerir.fecharLigacao(); 
                    System.out.println("A encerrar o sistema...");
                    break; 
                    
                } else {
                    System.out.println("Opção inválida!");
                }
                
            } else {
                String perfil = utilizadorLogado.getTipoUtilizador();
                
                if (perfil.equalsIgnoreCase("Admin")) {
                    utilizadorLogado = menuAdmin(gerir, scanner, utilizadorLogado);
                } else if (perfil.equalsIgnoreCase("Funcionario")) {
                    utilizadorLogado = menuFuncionario(gerir, scanner, utilizadorLogado);
                } else {
                    utilizadorLogado = menuUtilizador(gerir, scanner, utilizadorLogado);
                }
            }
        }
        scanner.close();
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
	/** 
     * mostra e controla o que os funcionarios podem fazer
     * @param gerir ligacao a base de dados
     * @param scanner le os numeros
     * @param utilizador o funcionario
     * @return devolve nulo se ele bazar
     */
    private static Utilizador menuFuncionario(Gerir gerir, Scanner scanner, Utilizador utilizador) {
        int opcao = -1;
        
        while (opcao != 0) {
            System.out.println("\n--- MENU FUNCIONÁRIO ---");
            System.out.println("1. Registar Objeto Encontrado no Meu Edifício");
            System.out.println("2. Listar Objetos Achados/Disponíveis");
            System.out.println("3. Pesquisar Objeto por ID");
            System.out.println("4. Associar Registos Manualmente (Perdido <-> Achado)");
            System.out.println("5. Listar Reclamações Ativas (No meu edifício)");
            System.out.println("6. Aprovar Reclamação (pelo ID Reclamação)");
            System.out.println("7. Rejeitar Reclamação (pelo ID Reclamação)");
            System.out.println("8. Arquivar Processo Concluído");
            System.out.println("9. Listar Objetos Perdidos");
            System.out.println("0. Fazer Logout");
            
            System.out.print("Escolha: ");
            opcao = gerir.lerInt(scanner);

            switch (opcao) {
                
                case 1:
                    String marca = gerir.lerTextoObrigatorio(scanner, "Marca: ");
                    String tipo = gerir.lerTextoObrigatorio(scanner, "Tipo: ");
                    String cor = gerir.lerTextoObrigatorio(scanner, "Cor: ");
                    
                    gerir.adicionarObjetoEncontrado(LocalDateTime.now(), marca, tipo, cor, utilizador.getId());
                    break;
                    
                case 2:
                    gerir.listarObjetosDisponiveis(); 
                    break;
                    
                case 3:
                    System.out.print("ID do Objeto: ");
                    Long idPesquisa = gerir.lerLong(scanner);
                    if (idPesquisa != -1L) gerir.pesquisaPorId(idPesquisa); 
                    break;
                    
                case 4:
                    System.out.print("ID Objeto Perdido: ");
                    Long idPerdido = gerir.lerLong(scanner);
                    System.out.print("ID Objeto Achado: ");
                    Long idAchado = gerir.lerLong(scanner);
                    
                    if (idPerdido != -1L && idAchado != -1L) {
                        gerir.associarManual(idPerdido, idAchado, utilizador.getId()); 
                    }
                    break;
                    
                case 5:
                    System.out.println("--- RECLAMAÇÕES ATIVAS NO MEU EDIFÍCIO ---");
                    gerir.listarReclamacoesPendentes(utilizador.getId()); 
                    break;
                    
                case 6:
                    System.out.print("ID da Reclamação a Aprovar: ");
                    Long idAprovar = gerir.lerLong(scanner);
                    if (idAprovar != -1L) gerir.aprovarReclamacao(idAprovar, utilizador.getId()); 
                    break;
                    
                case 7:
                    System.out.print("ID da Reclamação a Rejeitar: ");
                    Long idRejeitar = gerir.lerLong(scanner);
                    if (idRejeitar != -1L) {
                        String motivo = gerir.lerTextoObrigatorio(scanner, "Motivo da rejeição: ");
                        gerir.rejeitarReclamacao(idRejeitar, motivo, utilizador.getId()); 
                    }
                    break;
                    
                case 8:
                    System.out.print("ID do Objeto a Arquivar: ");
                    Long idArquivar = gerir.lerLong(scanner);
                    if (idArquivar != -1L) gerir.arquivarProcesso(idArquivar); 
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
