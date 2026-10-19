package upt.pt;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

/** 
 * esta classe cria a tabela dos utilizadores na base de dados
 * guarda as contas todas dos administradores funcionarios e pessoas normais
 */
@Entity
@Table(name = "utilizador")
public class Utilizador {

    /** o numero de identificacao que e dado as pessoas automaticamente */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** diz se a pessoa e admin funcionario ou apenas utilizador */
    @Column(name = "tipo_utilizador")
    private String tipoUtilizador;

    /** a palavra passe para fazer login */
    private String password;
    
    /** o nome verdadeiro da pessoa */
    @Column(name = "nome_utilizador")
    private String nomeUtilizador;

    /** o email dela que e unico e nao da para repetir noutra conta */
    @Column(unique = true)
    private String email;
    
    /** o numero de telemovel que tambem tem de ser unico */
    @Column(unique = true)
    private int contacto;

    /** a lista com as coisas todas que a pessoa registou */
    @OneToMany(mappedBy = "utilizador", cascade = CascadeType.ALL)
    private List<Objeto> objetos = new ArrayList<>();

    /** o edificio onde a pessoa trabalha se for um funcionario */
    @ManyToOne
    @JoinColumn(name = "local_id")
    private Local localTrabalho;

    /** 
     * o hibernate obriga a ter um contrutor vazio para funcionar bem e criar tabelas
     */
    public Utilizador() {}

    /** 
     * construtor para criar uma conta nova e preencher logo os dados dela
     * @param tipoUtilizador que tipo de conta e
     * @param password a senha de entrar
     * @param nomeUtilizador o nome da pessoa
     * @param email o email para contacto
     * @param contacto o telemovel
     */
    public Utilizador(String tipoUtilizador, String password, String nomeUtilizador, String email, int contacto) {
        this.tipoUtilizador = tipoUtilizador;
        this.password = password;
        this.nomeUtilizador = nomeUtilizador;
        this.email = email;
        this.contacto = contacto;
    }

    /** 
     * vai buscar o id da conta
     * @return o id
     */
    public Long getId() { return id; }

    /** 
     * ve que tipo de perfil a pessoa tem
     * @return diz se e admin ou outra coisa
     */
    public String getTipoUtilizador() { return tipoUtilizador; }

    /** 
     * vai ler a palavra passe
     * @return a password
     */
    public String getPassword() { return password; }

    /** 
     * mostra o nome da pessoa
     * @return o nome
     */
    public String getNomeUtilizador() { return nomeUtilizador; }

    /** 
     * da o email da conta
     * @return o email
     */
    public String getEmail() { return email; }

    /** 
     * da o numero de telemovel
     * @return o contacto
     */
    public int getContacto() { return contacto; }
    
    /** 
     * vai buscar as coisas todas desta pessoa
     * @return a lista de objetos associados a conta
     */
    public List<Objeto> getObjetos() { return objetos; }

    /** 
     * diz em que local o funcionario trabalha
     * @return o local de trabalho
     */
    public Local getLocalTrabalho() { return localTrabalho; }

    /** 
     * guarda o local onde o funcionario vai passar a trabalhar
     * @param localTrabalho o edificio escolhido pelo admin
     */
    public void setLocalTrabalho(Local localTrabalho) { this.localTrabalho = localTrabalho; }

    /** 
     * arruma o texto para quando quisermos imprimir a conta aparecer bonito no ecra
     * @return uma linha de texto com as informacoes todas da pessoa
     */
    @Override
    public String toString() {
        return "id: " + id + " | nome: " + nomeUtilizador + " | email: " + email + " | contacto: " + contacto + " | perfil: " + tipoUtilizador + 
               (localTrabalho != null ? " | local: " + localTrabalho.getNomeEdificio() : "");
    }
}