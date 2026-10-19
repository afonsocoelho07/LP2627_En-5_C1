package upt.pt;

public class Teste {

	public static void main(String[] args) {


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
