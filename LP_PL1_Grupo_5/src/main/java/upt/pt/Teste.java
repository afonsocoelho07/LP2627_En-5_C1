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

}
