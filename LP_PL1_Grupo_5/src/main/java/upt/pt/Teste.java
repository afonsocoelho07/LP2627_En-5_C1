package upt.pt;

import java.time.LocalDateTime;
import java.util.Scanner;

public class Teste {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Gerir gestor = new Gerir();
        Utilizador userLogado = null;

        System.out.println("===========================================");
        System.out.println("   SISTEMA DE OBJETOS PERDIDOS E ACHADOS   ");
        System.out.println("===========================================");

        while (true) {
            if (userLogado == null) {
                System.out.println("\n--- MENU PRINCIPAL ---");
                System.out.println("1 - Fazer Login");
                System.out.println("2 - Criar Conta de Cliente");
                System.out.println("0 - Sair");
                System.out.print("Escolha uma opção: ");
                
                int opcao = gestor.lerInt(scanner);

                switch (opcao) {
                    case 1:
                        String idLogin = gestor.lerTextoObrigatorio(scanner, "Email ou Telemóvel: ");
                        String passLogin = gestor.lerTextoObrigatorio(scanner, "Password: ");
                        userLogado = gestor.fazerLogin(idLogin, passLogin);
                        
                        if (userLogado != null) {
                            System.out.println("Login efetuado com sucesso! Bem-vindo(a), " + userLogado.getNomeUtilizador() + ".");
                        } else {
                            System.out.println("Erro: Credenciais inválidas.");
                        }
                        break;
                    case 2:
                        System.out.println("\n--- REGISTO DE NOVO CLIENTE ---");
                        String nome = gestor.lerTextoObrigatorio(scanner, "Nome: ");
                        String email = gestor.lerTextoObrigatorio(scanner, "Email: ");
                        System.out.print("Telemóvel: ");
                        int contacto = gestor.lerInt(scanner);
                        String pass = gestor.lerTextoObrigatorio(scanner, "Password: ");

                        if (gestor.existeEmailOuContacto(email, contacto)) {
                            System.out.println("Erro: Email ou Telemóvel já registados no sistema.");
                        } else {
                            gestor.adicionarUtilizador("Cliente", pass, nome, email, contacto);
                            System.out.println("Conta de Cliente criada com sucesso! Já pode fazer login.");
                        }
                        break;
                    case 0:
                        System.out.println("A encerrar o sistema...");
                        gestor.fecharLigacao();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opção inválida!");
                }
            } else {
                String tipo = userLogado.getTipoUtilizador().toLowerCase();
                
                if (tipo.equals("cliente")|| tipo.equals("utilizador")) {
                    menuCliente(scanner, gestor, userLogado);
                    userLogado = null; 
                } else if (tipo.equals("funcionario") || tipo.equals("funcionário")) {
                    menuFuncionario(scanner, gestor, userLogado);
                    userLogado = null;
                } else if (tipo.equals("admin") || tipo.equals("administrador")) {
                    menuAdmin(scanner, gestor, userLogado);
                    userLogado = null;
                } else {
                    System.out.println("Tipo de conta desconhecido. A fazer logout de segurança.");
                    userLogado = null;
                }
            }
        }
    }

    // ==========================================
    //              MENU CLIENTE
    // ==========================================
    private static void menuCliente(Scanner scanner, Gerir gestor, Utilizador userLogado) {
        while (true) {
            System.out.println("\n--- MENU CLIENTE ---");
            System.out.println("1 - Registar Objeto Perdido");
            System.out.println("2 - Pesquisar por Palavra-Chave (Marca/Tipo)");
            System.out.println("3 - Listar Objetos Disponíveis no Armazém");
            System.out.println("4 - Filtrar por Categoria");
            System.out.println("5 - Submeter Reclamação (Provar que é meu)");
            System.out.println("6 - Agendar Recolha (Marcar Hora)");
            System.out.println("7 - Consultar Meus Processos e Reclamações");
            System.out.println("0 - Fazer Logout");
            System.out.print("Escolha uma opção: ");

            int opcao = gestor.lerInt(scanner);

            switch (opcao) {
                case 1:
                    System.out.println("\n-- REGISTAR OBJETO PERDIDO --");
                    String marca = gestor.lerTextoObrigatorio(scanner, "Marca do objeto: ");
                    String tipo = gestor.lerTextoObrigatorio(scanner, "Tipo (ex: Telemóvel, Carteira): ");
                    String cor = gestor.lerTextoObrigatorio(scanner, "Cor: ");
                    System.out.print("ID do Edifício onde o perdeu (opcional, digite -1 se não souber): ");
                    Long idLocal = gestor.lerLong(scanner);
                    
                    Long localFinal = (idLocal == -1L) ? null : idLocal;
                    gestor.adicionarObjeto(LocalDateTime.now(), marca, tipo, cor, "Perdido", userLogado.getId(), null, localFinal);
                    break;
                case 2:
                    String kw = gestor.lerTextoObrigatorio(scanner, "Digite a palavra-chave a pesquisar: ");
                    gestor.pesquisaPalavraChave(kw);
                    break;
                case 3:
                    gestor.listarObjetosDisponiveis();
                    break;
                case 4:
                    String cat = gestor.lerTextoObrigatorio(scanner, "Digite a categoria a filtrar: ");
                    gestor.pesquisaCategoria(cat);
                    break;
                case 5:
                    System.out.print("ID do Objeto que quer reclamar: ");
                    Long idObjRec = gestor.lerLong(scanner);
                    String provas = gestor.lerTextoObrigatorio(scanner, "Descreva as provas (O que tem lá dentro? Sinais particulares?): ");
                    gestor.submeterReclamacao(idObjRec, userLogado.getId(), provas);
                    break;
                case 6:
	System.out.println("--- Objetos Disponíveis ---");
                	gerir.listarObjetosDisponiveis();
                    System.out.print("ID do Objeto a Reclamar: ");
                    Long idReclamar = gerir.lerLong(scanner);
                    if (idReclamar == -1L) break;
                    
                    String detalhes = gerir.lerTextoObrigatorio(scanner, "Detalhes/Provas (ex: capa amarela, rachadela no ecrã): ");
                    gerir.submeterReclamacao(idReclamar, utilizador.getId(), detalhes); 
=======
                    System.out.print("ID do Objeto a recolher: ");
                    Long idObjAgend = gestor.lerLong(scanner);
                    LocalDateTime amanha = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0);
                    gestor.agendarRecolha(idObjAgend, userLogado.getId(), amanha);
                    break;
                case 7:
                    gestor.listarMeusProcessos(userLogado.getId());
                    break;
                case 0:
                    return; 
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

 // ==========================================
    //            MENU FUNCIONÁRIO
    // ==========================================
    private static void menuFuncionario(Scanner scanner, Gerir gestor, Utilizador userLogado) {
        while (true) {
            System.out.println("\n--- MENU FUNCIONÁRIO ---");
            System.out.println("1 - Registar Objeto Achado no Edifício");
            System.out.println("2 - Listar Objetos Perdidos (Procurados pelos Clientes)"); // <-- NOVA OPÇÃO AQUI
            System.out.println("3 - Listar Objetos Disponíveis (Em Armazém)");
            System.out.println("4 - Pesquisar Objeto por ID");
            System.out.println("5 - Associar Registos Manualmente (Perdido <-> Achado)");
            System.out.println("6 - Listar Reclamações Pendentes do Meu Edifício");
            System.out.println("7 - Aprovar Reclamação");
            System.out.println("8 - Rejeitar Reclamação");
            System.out.println("9 - Arquivar Processo Concluído");
            System.out.println("10 - Listar Objetos Expirados no Armazém");
            System.out.println("11 - Doar/Eliminar Objeto Expirado (Limpar Armazém)");
            System.out.println("0 - Fazer Logout");
            System.out.print("Escolha uma opção: ");

            int opcao = gestor.lerInt(scanner);

            switch (opcao) {
                case 1:
                    System.out.println("\n-- REGISTAR OBJETO ACHADO --");
                    String marca = gestor.lerTextoObrigatorio(scanner, "Marca: ");
                    String tipo = gestor.lerTextoObrigatorio(scanner, "Tipo (ex: Telemóvel, Carteira): ");
                    String cor = gestor.lerTextoObrigatorio(scanner, "Cor: ");
                    
                    gestor.adicionarObjetoEncontrado(LocalDateTime.now(), marca, tipo, cor, userLogado.getId());
                    break;
                case 2:
                    gestor.listarObjetosPerdidos();
                    break;
                case 3:
                    gestor.listarObjetosDisponiveis();
                    break;
                case 4:
System.out.println("--- Objetos Perdidos ---");
                	gerir.listarObjetosPerdidos();
                	System.out.println("--- Objetos Disponíveis ---");
                	gerir.listarObjetosDisponiveis();
                    System.out.print("ID Objeto Perdido: ");
                    Long idPerdido = gerir.lerLong(scanner);
                    System.out.print("ID Objeto Achado: ");
                    Long idAchado = gerir.lerLong(scanner);
                    
                    if (idPerdido != -1L && idAchado != -1L) {
                        gerir.associarManual(idPerdido, idAchado, utilizador.getId()); 
                    }
=======
                    System.out.print("ID do Objeto: ");
                    Long idPesq = gestor.lerLong(scanner);
                    gestor.pesquisaPorId(idPesq);
                    break;
                case 5:
                    System.out.print("ID do Objeto Perdido: ");
                    Long idPerdido = gestor.lerLong(scanner);
                    System.out.print("ID do Objeto Achado: ");
                    Long idAchado = gestor.lerLong(scanner);
                    gestor.associarManual(idPerdido, idAchado, userLogado.getId());
                    break;
                case 6:
                    gestor.listarReclamacoesPendentes(userLogado.getId());
                    break;
                case 7:
                    System.out.print("ID da Reclamação a Aprovar: ");
                    Long idApr = gestor.lerLong(scanner);
                    gestor.aprovarReclamacao(idApr, userLogado.getId());
                    break;
                case 8:
                    System.out.print("ID da Reclamação a Rejeitar: ");
                    Long idRej = gestor.lerLong(scanner);
                    String motivo = gestor.lerTextoObrigatorio(scanner, "Motivo da rejeição: ");
                    gestor.rejeitarReclamacao(idRej, motivo, userLogado.getId());
                    break;
                case 9:
                    System.out.print("ID do Processo/Objeto a Arquivar: ");
                    Long idArq = gestor.lerLong(scanner);
                    gestor.arquivarProcesso(idArq);
                    System.out.println("Processo arquivado.");
                    break;
                case 10:
                    gestor.listarObjetosExpirados();
                    break;
                case 11:
                    System.out.println("\n-- DOAR / ELIMINAR OBJETO EXPIRADO --");
                    System.out.print("ID do Objeto a eliminar do armazém: ");
                    Long idEliminar = gestor.lerLong(scanner);
                    gestor.doarOuEliminarObjeto(idEliminar, userLogado.getId());
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    // ==========================================
    //              MENU ADMIN
    // ==========================================
    private static void menuAdmin(Scanner scanner, Gerir gestor, Utilizador userLogado) {
        while (true) {
            System.out.println("\n--- MENU ADMINISTRADOR ---");
            System.out.println("1 - Criar Edifício / Local de Recolha");
            System.out.println("2 - Atribuir Funcionário a um Edifício");
            System.out.println("3 - Criar Armazém num Edifício");
            System.out.println("4 - Criar Prateleira/Cacifo num Armazém");
            System.out.println("5 - Ver Estatística de Entregas");
            System.out.println("6 - Listar Funcionários");
            System.out.println("7 - Listar Clientes");
            System.out.println("8 - Remover Conta do Sistema");
            System.out.println("9 - Listar Edifícios e Lotações");
            System.out.println("0 - Fazer Logout");
            System.out.print("Escolha uma opção: ");

            int opcao = gestor.lerInt(scanner);

            switch (opcao) {
                case 1:
                    String nomeEd = gestor.lerTextoObrigatorio(scanner, "Nome do Edifício: ");
                    String descEd = gestor.lerTextoObrigatorio(scanner, "Descrição: ");
                    String inicio = gestor.lerTextoObrigatorio(scanner, "Hora Abertura (ex: 09:00): ");
                    String fim = gestor.lerTextoObrigatorio(scanner, "Hora Fecho (ex: 18:00): ");
                    gestor.adicionarLocal(nomeEd, descEd, inicio, fim);
                    System.out.println("Edifício criado.");
                    break;
                case 2:
                    System.out.print("ID do Funcionário: ");
                    Long idF = gestor.lerLong(scanner);
                    System.out.print("ID do Edifício: ");
                    Long idL = gestor.lerLong(scanner);
                    gestor.atribuirFuncionarioLocal(idF, idL);
                    break;
                case 3:
                    String nomeArm = gestor.lerTextoObrigatorio(scanner, "Nome do Armazém: ");
                    System.out.print("ID do Edifício a que pertence: ");
                    Long idEdArm = gestor.lerLong(scanner);
                    gestor.adicionarArmazem(nomeArm, idEdArm);
                    break;
                case 4:
                    String codPrat = gestor.lerTextoObrigatorio(scanner, "Código da Prateleira (ex: A1): ");
                    System.out.print("Lotação Máxima (número): ");
                    int lotacao = gestor.lerInt(scanner);
                    System.out.print("ID do Armazém a que pertence: ");
                    Long idArmPrat = gestor.lerLong(scanner);
                    gestor.adicionarPrateleira(codPrat, lotacao, idArmPrat);
                    break;
                case 5:
                    gestor.calcularEstatisticaEntregas();
                    break;
                case 6:
                    gestor.listarUtilizadoresPorTipo("Funcionario");
                    break;
                case 7:
                    gestor.listarUtilizadoresPorTipo("Cliente");
                    break;
                case 8:
                    System.out.print("ID da Conta (Cliente ou Funcionário) a Remover: ");
                    Long idRem = gestor.lerLong(scanner);
                    gestor.removerUtilizador(idRem);
                    break;
                case 9:
                    gestor.listarLocaisComDetalhes();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }
}