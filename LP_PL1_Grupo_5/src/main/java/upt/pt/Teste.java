package upt.pt;

import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Interface de consola. Os menus estão organizados por tarefas.
 * Toda a lógica de persistência permanece em Gerir.
 */
public class Teste {

    public static void main(String[] args) {
      
        Scanner scanner = new Scanner(System.in);
        Gerir gestor = new Gerir();
        Utilizador utilizador = null;

        System.out.println("==========================================");
        System.out.println("   SISTEMA DE PERDIDOS E ACHADOS - UPT     ");
        System.out.println("==========================================");

        try {
            while (true) {
                if (utilizador == null) {
                    titulo("MENU INICIAL");
                    System.out.println("1 - Iniciar sessão");
                    System.out.println("2 - Criar conta de cliente");
                    System.out.println("0 - Sair");
                    int opcao = escolher(scanner, gestor);

                    switch (opcao) {
						case 1:
						    String identificador = gestor.lerTextoObrigatorio(scanner, "Email ou telemóvel: ");
						    boolean existe = false;

						    // Verificar se o identificador é um número de telemóvel
						    if (identificador.matches("\\d+")) {
						        try {
						            int contacto = Integer.parseInt(identificador);
						            existe = gestor.existeEmailOuContacto("", contacto);
						        } catch (NumberFormatException e) {
						            existe = false;
						        }
						    } else {
						
						        // Verificar se o email existe
						        // -1 representa um contacto que não procuramos
						        existe = gestor.existeEmailOuContacto(identificador, -1);
						    }
						
						    // Se o identificador não existir, não pedir password
						    if (!existe) {
						        System.out.println(
						            "Não existe uma conta com esse email ou telemóvel."
						        );
						        break;
						    }
						
						    // Só pedir password se o identificador existir
						    String password = gestor.lerTextoObrigatorio(scanner, "Password: ");
						
						    utilizador = gestor.fazerLogin(identificador, password);
						
						    if (utilizador != null) {
						        System.out.println("Bem-vindo(a), " + utilizador.getNomeUtilizador() + "!");
						    } else {
						        System.out.println("Credenciais inválidas. Tenta novamente.");
						    }
						    break;
                        case 2:
                            criarConta(scanner, gestor);
                            break;
                        case 0:
                            System.out.println("Até à próxima!");
                            return;
                        default:
                            opcaoInvalida();
                    }
                } else {
                    String tipo = utilizador.getTipoUtilizador().toLowerCase();
                    if (tipo.equals("cliente") || tipo.equals("utilizador")) {
                        menuCliente(scanner, gestor, utilizador);
                    } else if (tipo.equals("funcionario") || tipo.equals("funcionário")) {
                        menuFuncionario(scanner, gestor, utilizador);
                    } else if (tipo.equals("admin") || tipo.equals("administrador")) {
                        menuAdmin(scanner, gestor, utilizador);
                    } else {
                        System.out.println("Perfil desconhecido. Sessão terminada.");
                    }
                    utilizador = null;
                }
            }
        } finally {
            gestor.fecharLigacao();
            scanner.close();
        }
    }

    private static void criarConta(Scanner scanner, Gerir gestor) {
        titulo("CRIAR CONTA DE CLIENTE");
        String nome = gestor.lerTextoObrigatorio(scanner, "Nome: ");
        String email = gestor.lerTextoObrigatorio(scanner, "Email: ");
        int contacto = pedirInt(scanner, gestor, "Telemóvel: ");
        String password = gestor.lerTextoObrigatorio(scanner, "Password: ");
        if (gestor.existeEmailOuContacto(email, contacto)) {
            System.out.println("Esse email ou telemóvel já está registado.");
            return;
        }
        gestor.adicionarUtilizador("Cliente", password, nome, email, contacto);
        System.out.println("Conta criada. Já podes iniciar sessão.");
    }

    // ========================= CLIENTE =========================

    private static void menuCliente(Scanner scanner, Gerir gestor, Utilizador cliente) {
        while (true) {
            titulo("MENU CLIENTE");
            System.out.println("1 - Registar objeto perdido");
            System.out.println("2 - Pesquisar objetos");
            System.out.println("3 - As minhas reclamações");
            System.out.println("4 - Levantamento de objetos");
            System.out.println("0 - Terminar sessão");
            switch (escolher(scanner, gestor)) {
                case 1: registarPerdido(scanner, gestor, cliente); break;
                case 2: menuPesquisaCliente(scanner, gestor); break;
                case 3: menuReclamacoesCliente(scanner, gestor, cliente); break;
                case 4: menuLevantamentosCliente(scanner, gestor, cliente); break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void registarPerdido(Scanner scanner, Gerir gestor, Utilizador cliente) {
        titulo("REGISTAR OBJETO PERDIDO");
        String marca = gestor.lerTextoObrigatorio(scanner, "Marca: ");
        String tipo = gestor.lerTextoObrigatorio(scanner, "Tipo: ");
        String cor = gestor.lerTextoObrigatorio(scanner, "Cor: ");
        mostrarLocais(gestor);
        Long idLocal = pedirLong(scanner, gestor,
                "ID do edifício onde o perdeste (-1 se não souberes): ");
        Long localFinal = idLocal == -1L ? null : idLocal;
        gestor.adicionarObjeto(LocalDateTime.now(), marca, tipo, cor,
                "Perdido", cliente.getId(), null, localFinal);
    }

    private static void menuPesquisaCliente(Scanner scanner, Gerir gestor) {
        while (true) {
            titulo("PESQUISAR OBJETOS");
            System.out.println("1 - Pesquisar por palavra-chave");
            System.out.println("2 - Listar objetos disponíveis");
            System.out.println("3 - Filtrar por categoria");
            System.out.println("0 - Voltar ao menu do cliente");
            switch (escolher(scanner, gestor)) {
                case 1:
                    gestor.pesquisaPalavraChave(gestor.lerTextoObrigatorio(
                            scanner, "Palavra-chave: "));
                    break;
                case 2: gestor.listarObjetosDisponiveis(); break;
                case 3:
                    gestor.pesquisaCategoria(gestor.lerTextoObrigatorio(
                            scanner, "Categoria: "));
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuReclamacoesCliente(Scanner scanner, Gerir gestor,
                                                 Utilizador cliente) {
        while (true) {
            titulo("AS MINHAS RECLAMAÇÕES");
            System.out.println("1 - Consultar os meus processos e reclamações");
            System.out.println("2 - Submeter reclamação de posse");
            System.out.println("0 - Voltar ao menu do cliente");
            switch (escolher(scanner, gestor)) {
                case 1:
                    gestor.listarMeusProcessos(cliente.getId());
                    break;
                case 2:
                    System.out.println("Objetos encontrados disponíveis:");
                    gestor.listarObjetosDisponiveis();
                    Long id = pedirLong(scanner, gestor, "ID do objeto a reclamar: ");
                    String provas = gestor.lerTextoObrigatorio(scanner,
                            "Provas de posse / características exclusivas: ");
                    gestor.submeterReclamacao(id, cliente.getId(), provas);
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    /** Mantém uma reclamação selecionada enquanto o cliente está neste submenu. */
    private static void menuLevantamentosCliente(Scanner scanner, Gerir gestor,
                                                  Utilizador cliente) {
        Long reclamacaoSelecionada = null;
        while (true) {
            titulo("LEVANTAMENTO DE OBJETOS");
            System.out.println("Reclamação selecionada: "
                    + (reclamacaoSelecionada == null ? "nenhuma" : "#" + reclamacaoSelecionada));
            System.out.println("1 - Selecionar reclamação");
            System.out.println("2 - Escolher presencial ou Locky");
            System.out.println("3 - Consultar local e estado do levantamento");
            System.out.println("4 - Agendar levantamento presencial (módulo original)");
            System.out.println("5 - Obter código do Locky");
            System.out.println("6 - Abrir Locky com código");
            System.out.println("0 - Voltar ao menu do cliente");

            int opcao = escolher(scanner, gestor);
            if (opcao == 0) return;
            if (opcao == 1) {
                gestor.listarMeusProcessos(cliente.getId());
                Long id = pedirLong(scanner, gestor, "ID da reclamação a selecionar: ");
                if (gestor.reclamacaoPertenceAoCliente(id, cliente.getId())) {
                    reclamacaoSelecionada = id;
                    System.out.println("Reclamação #" + id + " selecionada.");
                } else {
                    System.out.println("Não encontrámos essa reclamação na tua conta.");
                }
                continue;
            }
            if (opcao < 2 || opcao > 6) {
                opcaoInvalida();
                continue;
            }
            if (reclamacaoSelecionada == null) {
                System.out.println("Seleciona primeiro uma reclamação (opção 1).");
                continue;
            }
          
            switch (opcao) {
                case 2:
                    escolherLevantamento(scanner, gestor, cliente, reclamacaoSelecionada);
                    break;
                case 3:
                    gestor.consultarMeuLevantamento(reclamacaoSelecionada, cliente.getId());
                    gestor.verificarObjetosExpirados();
                    break;
                case 4:
                    // Método original do grupo, mantido sem mudanças (integração pendente).
                    System.out.println("ATENÇÃO: esta opção ainda usa o agendamento original.");
                    System.out.println("É necessário indicar o ID do objeto, não da reclamação.");
                    Long idObjeto = pedirLong(scanner, gestor, "ID do objeto a recolher: ");
                    LocalDateTime amanha = LocalDateTime.now().plusDays(1)
                            .withHour(14).withMinute(0).withSecond(0).withNano(0);
                    gestor.agendarRecolha(idObjeto, cliente.getId(), amanha);
                    break;
                case 5:
                    gestor.emitirCodigoLocky(reclamacaoSelecionada, cliente.getId());
                    break;
                case 6:
                    String codigo = gestor.lerTextoObrigatorio(scanner, "Código de 8 dígitos: ");
                    gestor.abrirLocky(reclamacaoSelecionada, cliente.getId(), codigo);
                    break;
                default: opcaoInvalida();
            }
        }
    }

    private static void escolherLevantamento(Scanner scanner, Gerir gestor,
                                             Utilizador cliente, Long reclamacao) {
        titulo("ESCOLHER MODALIDADE");
        System.out.println("1 - Levantamento presencial");
        System.out.println("2 - Ponto Locky");
        System.out.println("0 - Cancelar");
        switch (escolher(scanner, gestor)) {
            case 1:
                mostrarLocais(gestor);
                gestor.escolherLevantamentoPresencial(reclamacao, cliente.getId(),
                        pedirLong(scanner, gestor, "ID do edifício: "));
                break;
            case 2:
                gestor.listarPontosLocky();
                gestor.escolherLevantamentoLocky(reclamacao, cliente.getId(),
                        pedirLong(scanner, gestor, "ID do ponto Locky: "));
                break;
            case 0: break;
            default: opcaoInvalida();
        }
    }

    // ======================== FUNCIONÁRIO ========================

    private static void menuFuncionario(Scanner scanner, Gerir gestor,
                                         Utilizador funcionario) {
        while (true) {
            titulo("MENU FUNCIONÁRIO");
            System.out.println("1 - Gestão de objetos");
            System.out.println("2 - Correspondências");
            System.out.println("3 - Gestão de reclamações");
            System.out.println("4 - Armazém e arquivamento");
            System.out.println("5 - Depósitos Locky");
            System.out.println("0 - Terminar sessão");
            switch (escolher(scanner, gestor)) {
                case 1: menuObjetosFuncionario(scanner, gestor, funcionario); break;
                case 2: menuCorrespondencias(scanner, gestor, funcionario); break;
                case 3: menuReclamacoesFuncionario(scanner, gestor, funcionario); break;
                case 4: menuArmazemFuncionario(scanner, gestor, funcionario); break;
                case 5: menuDepositosLocky(scanner, gestor, funcionario); break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuObjetosFuncionario(Scanner scanner, Gerir gestor,
                                                 Utilizador funcionario) {
        while (true) {
            titulo("GESTÃO DE OBJETOS");
            System.out.println("1 - Registar objeto encontrado");
            System.out.println("2 - Listar objetos perdidos");
            System.out.println("3 - Listar objetos disponíveis no armazém");
            System.out.println("4 - Pesquisar objeto pelo ID");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1:
                    String marca = gestor.lerTextoObrigatorio(scanner, "Marca: ");
                    String tipo = gestor.lerTextoObrigatorio(scanner, "Tipo: ");
                    String cor = gestor.lerTextoObrigatorio(scanner, "Cor: ");
                    gestor.adicionarObjetoEncontrado(LocalDateTime.now(), marca, tipo,
                            cor, funcionario.getId());
                    break;
                case 2: gestor.listarObjetosPerdidos(); break;
                case 3: gestor.listarObjetosDisponiveis(); break;
                case 4:
                    gestor.pesquisaPorId(pedirLong(scanner, gestor, "ID do objeto: "));
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }
    
    private static void menuCorrespondencias(Scanner scanner, Gerir gestor,Utilizador funcionario) {
    		while (true) {
    			titulo("CORRESPONDÊNCIAS");
    			System.out.println("1 - Associar registo perdido a achado");
    			System.out.println("0 - Voltar");
    			switch (escolher(scanner, gestor)) {
    			case 1: gestor.listarObjetosPerdidos();
    				Long perdido = pedirLong(scanner, gestor, "ID do objeto perdido: ");
    				gestor.listarObjetosDisponiveis();
    				Long achado = pedirLong(scanner, gestor, "ID do objeto encontrado: ");
    				gestor.associarManual(perdido, achado, funcionario.getId());
    			break;
    		case 0: return;
    		default: opcaoInvalida();
    		}
    	}
    }


    private static void menuReclamacoesFuncionario(Scanner scanner, Gerir gestor,
                                                    Utilizador funcionario) {
        while (true) {
            titulo("GESTÃO DE RECLAMAÇÕES");
            System.out.println("1 - Listar reclamações pendentes do meu edifício");
            System.out.println("2 - Aprovar reclamação");
            System.out.println("3 - Rejeitar reclamação");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1:
                    gestor.listarReclamacoesPendentes(funcionario.getId());
                    break;
                case 2:
                    gestor.listarReclamacoesPendentes(funcionario.getId());
                    gestor.aprovarReclamacao(pedirLong(scanner, gestor,
                            "ID da reclamação: "), funcionario.getId());
                    break;
                case 3:
                    gestor.listarReclamacoesPendentes(funcionario.getId());
                    Long id = pedirLong(scanner, gestor, "ID da reclamação: ");
                    String motivo = gestor.lerTextoObrigatorio(scanner, "Motivo da rejeição: ");
                    if (confirmar(scanner, "Confirmar rejeição da reclamação " + id + "?")) {
                        gestor.rejeitarReclamacao(id, motivo, funcionario.getId());
                    }
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuArmazemFuncionario(Scanner scanner, Gerir gestor,
                                                Utilizador funcionario) {
        while (true) {
            titulo("ARMAZÉM E ARQUIVAMENTO");
            System.out.println("1 - Arquivar processo concluído");
            System.out.println("2 - Listar objetos expirados (regra original)");
            System.out.println("3 - Doar/eliminar objeto expirado");
            System.out.println("4 - Confirmar levantamento de objeto");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1:
                    Long id = pedirLong(scanner, gestor, "ID do processo/objeto: ");
                    if (confirmar(scanner, "Arquivar o processo " + id + "?")) {
                        gestor.arquivarProcesso(id);
                    }
                    break;
                case 2: gestor.listarObjetosExpirados(); break;
                case 3:
                    gestor.listarObjetosExpirados();
                    Long eliminar = pedirLong(scanner, gestor, "ID do objeto: ");
                    if (confirmar(scanner, "Doar/eliminar definitivamente o objeto " + eliminar + "?")) {
                        gestor.doarOuEliminarObjeto(eliminar, funcionario.getId());
                    }
                    break;
                case 4:
                    Long idObjeto = pedirLong(scanner, gestor, "Insira o ID do objeto levantado: ");
                    Long idFuncionario = pedirLong(scanner, gestor, "Insira o seu ID de funcionário: ");
                    gestor.confirmarLevantamento(idObjeto, idFuncionario);
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuDepositosLocky(Scanner scanner, Gerir gestor,
                                            Utilizador funcionario) {
        while (true) {
            titulo("DEPÓSITOS LOCKY");
            System.out.println("1 - Consultar depósitos pendentes e cacifos atribuídos");
            System.out.println("2 - Confirmar depósito físico no Locky");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1: gestor.listarLockysPorPreparar(funcionario.getId()); break;
                case 2:
                    gestor.listarLockysPorPreparar(funcionario.getId());
                    Long reclamacao = pedirLong(scanner, gestor, "ID da reclamação a preparar: ");
                    if (confirmar(scanner,
                            "Confirmas que colocaste o objeto da reclamação "
                            + reclamacao + " no cacifo indicado?")) {
                        gestor.depositarObjetoNoLocky(reclamacao, funcionario.getId());
                    }
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    // ======================= ADMINISTRADOR =======================

    private static void menuAdmin(Scanner scanner, Gerir gestor, Utilizador admin) {
        while (true) {
            titulo("MENU ADMINISTRADOR");
            System.out.println("1 - Edifícios e funcionários");
            System.out.println("2 - Armazéns e prateleiras");
            System.out.println("3 - Gestão de pontos Locky");
            System.out.println("4 - Gestão de utilizadores");
            System.out.println("5 - Estatísticas");
            System.out.println("0 - Terminar sessão");
            switch (escolher(scanner, gestor)) {
                case 1: menuLocaisAdmin(scanner, gestor); break;
                case 2: menuArmazensAdmin(scanner, gestor); break;
                case 3: menuLockyAdmin(scanner, gestor, admin); break;
                case 4: menuUtilizadoresAdmin(scanner, gestor); break;
                case 5: gestor.calcularEstatisticaEntregas(); break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuLocaisAdmin(Scanner scanner, Gerir gestor) {
        while (true) {
            titulo("EDIFÍCIOS E FUNCIONÁRIOS");
            System.out.println("1 - Criar edifício/local");
            System.out.println("2 - Listar edifícios e lotações");
            System.out.println("3 - Atribuir funcionário a edifício");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1:
                    String nome = gestor.lerTextoObrigatorio(scanner, "Nome do edifício: ");
                    String descricao = gestor.lerTextoObrigatorio(scanner, "Descrição: ");
                    String inicio = gestor.lerTextoObrigatorio(scanner, "Hora abertura (09:00): ");
                    String fim = gestor.lerTextoObrigatorio(scanner, "Hora fecho (18:00): ");
                    gestor.adicionarLocal(nome, descricao, inicio, fim);
                    break;
                case 2: gestor.listarLocaisComDetalhes(); break;
                case 3:
                    gestor.listarUtilizadoresPorTipo("Funcionario");
                    Long funcionario = pedirLong(scanner, gestor, "ID do funcionário: ");
                    mostrarLocais(gestor);
                    Long local = pedirLong(scanner, gestor, "ID do edifício: ");
                    gestor.atribuirFuncionarioLocal(funcionario, local);
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuArmazensAdmin(Scanner scanner, Gerir gestor) {
        while (true) {
            titulo("ARMAZÉNS E PRATELEIRAS");
            System.out.println("1 - Criar armazém num edifício");
            System.out.println("2 - Criar prateleira num armazém");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1:
                    mostrarLocais(gestor);
                    Long local = pedirLong(scanner, gestor, "ID do edifício: ");
                    String nome = gestor.lerTextoObrigatorio(scanner, "Nome do armazém: ");
                    gestor.adicionarArmazem(nome, local);
                    break;
                case 2:
                    System.out.println("Armazéns registados:");
                    for (Armazem a : gestor.getArmazens()) System.out.println(a);
                    Long armazem = pedirLong(scanner, gestor, "ID do armazém: ");
                    String codigo = gestor.lerTextoObrigatorio(scanner, "Código da prateleira: ");
                    int lotacao = pedirInt(scanner, gestor, "Lotação máxima: ");
                    gestor.adicionarPrateleira(codigo, lotacao, armazem);
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuLockyAdmin(Scanner scanner, Gerir gestor, Utilizador admin) {
        while (true) {
            titulo("GESTÃO DE PONTOS LOCKY");
            System.out.println("1 - Criar ponto com cacifos");
            System.out.println("2 - Adicionar cacifo a um ponto");
            System.out.println("3 - Consultar pontos e disponibilidade");
            System.out.println("4 - Consultar cacifos de um ponto");
            System.out.println("5 - Desbloquear tentativas de um cacifo");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1:
                    mostrarLocais(gestor);
                    Long idLocal = pedirLong(scanner, gestor, "ID do edifício: ");
                    String nome = gestor.lerTextoObrigatorio(scanner, "Nome do ponto: ");
                    String descricao = gestor.lerTextoObrigatorio(scanner, "Localização do ponto: ");
                    int quantidade = pedirInt(scanner, gestor, "Quantidade de cacifos (1-100): ");
                    if (confirmar(scanner, "Criar ponto '" + nome + "' com " + quantidade + " cacifos?")) {
                        gestor.adicionarPontoLocky(nome, descricao, idLocal, quantidade, admin.getId());
                    }
                    break;
                case 2:
                    gestor.listarPontosLocky();
                    Long ponto = pedirLong(scanner, gestor, "ID do ponto Locky: ");
                    String codigo = gestor.lerTextoObrigatorio(scanner, "Código do novo cacifo (ex.: L11): ");
                    gestor.adicionarCacifoAoPonto(ponto, codigo, admin.getId());
                    break;
                case 3: gestor.listarPontosLocky(); break;
                case 4:
                    gestor.listarPontosLocky();
                    gestor.listarCacifosPonto(pedirLong(scanner, gestor,
                            "ID do ponto Locky: "), admin.getId());
                    break;
                case 5:
                    gestor.listarPontosLocky();
                    Long idPonto = pedirLong(scanner, gestor, "ID do ponto: ");
                    gestor.listarCacifosPonto(idPonto, admin.getId());
                    Long idCacifo = pedirLong(scanner, gestor, "ID do cacifo bloqueado: ");
                    if (confirmar(scanner, "Repor as tentativas do cacifo " + idCacifo + "?")) {
                        gestor.desbloquearLocky(idCacifo, admin.getId());
                    }
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    private static void menuUtilizadoresAdmin(Scanner scanner, Gerir gestor) {
        while (true) {
            titulo("GESTÃO DE UTILIZADORES");
            System.out.println("1 - Listar funcionários");
            System.out.println("2 - Listar clientes");
            System.out.println("3 - Remover conta");
            System.out.println("0 - Voltar");
            switch (escolher(scanner, gestor)) {
                case 1: gestor.listarUtilizadoresPorTipo("Funcionario"); break;
                case 2: gestor.listarUtilizadoresPorTipo("Cliente"); break;
                case 3:
                    gestor.listarUtilizadoresPorTipo("Funcionario");
                    gestor.listarUtilizadoresPorTipo("Cliente");
                    Long id = pedirLong(scanner, gestor, "ID da conta a remover: ");
                    if (confirmar(scanner, "Remover a conta " + id + " e o histórico associado?")) {
                        gestor.removerUtilizador(id);
                    } else {
                        System.out.println("Operação cancelada.");
                    }
                    break;
                case 0: return;
                default: opcaoInvalida();
            }
        }
    }

    // ======================= AJUDANTES DE MENU =======================

    private static void mostrarLocais(Gerir gestor) {
        System.out.println("Edifícios registados:");
        for (Local local : gestor.getLocais()) System.out.println(local);
    }

    private static int escolher(Scanner scanner, Gerir gestor) {
        System.out.print("Escolhe uma opção: ");
        return gestor.lerInt(scanner); // -1 se não for número
    }

    private static Long pedirLong(Scanner scanner, Gerir gestor, String mensagem) {
        System.out.print(mensagem);
        return gestor.lerLong(scanner);
    }

    private static int pedirInt(Scanner scanner, Gerir gestor, String mensagem) {
        System.out.print(mensagem);
        return gestor.lerInt(scanner);
    }

    private static boolean confirmar(Scanner scanner, String pergunta) {
        System.out.print(pergunta + " (s/n): ");
        String resposta = scanner.nextLine().trim();
        return resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim");
    }

    private static void titulo(String texto) {
        System.out.println("\n==========================================");
        System.out.println(" " + texto);
        System.out.println("==========================================");
    }

    private static void opcaoInvalida() {
        System.out.println("Opção inválida. Escolhe um número do menu.");
    }
}
