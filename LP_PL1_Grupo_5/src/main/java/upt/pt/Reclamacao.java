package upt.pt;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * classe para guardar as queixas que a malta faz do sistema
 */
@Entity
@Table(name = "reclamacao")
public class Reclamacao {

    /**
     * numero identificador unico da queixa na base de dados
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * dia e hora em que a queixa foi feita
     */
    @Column(name = "data_reclamacao")
    private LocalDateTime dataReclamacao;

    /**
     * texto a explicar o que se passou
     */
    private String detalhes;
    
    /**
     * como esta a queixa tipo pendente ou resolvida
     */
    private String estado; 

    /**
     * razao pela qual a queixa foi chumbada se for o caso
     */
    @Column(name = "motivo_rejeicao")
    private String motivoRejeicao;

    /**
     * pessoa que fez a queixa
     */
    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador utilizador;

    /**
     * coisa sobre a qual estao a queixar se
     */
    @ManyToOne
    @JoinColumn(name = "objeto_id")
    private Objeto objeto;

    // Preferencia escolhida pelo cliente depois da aprovacao.
    @Column(name = "tipo_levantamento", length = 15)
    private String tipoLevantamento; // PRESENCIAL ou LOCKY

    @ManyToOne
    @JoinColumn(name = "local_levantamento_id")
    private Local localLevantamento;

    @ManyToOne
    @JoinColumn(name = "ponto_locky_id")
    private PontoLocky pontoLockyEscolhido;

    /**
     * construtor vazio para o banco de dados conseguir trabalhar
     */
    public Reclamacao() {}

    /**
     * cria uma nova queixa com a data os detalhes e o estado
     * 
     * @param dataReclamacao quando foi feita
     * @param detalhes o que aconteceu
     * @param estado situacao atual
     */
    public Reclamacao(LocalDateTime dataReclamacao, String detalhes, String estado) {
        this.dataReclamacao = dataReclamacao;
        this.detalhes = detalhes;
        this.estado = estado;
    }

    /**
     * apanha o numero identificador
     * 
     * @return o id da queixa
     */
    public Long getId() { 
        return id; }

    /**
     * apanha a data e hora em que foi feita
     * 
     * @return a data da queixa
     */
    public LocalDateTime getDataReclamacao() { 
        return dataReclamacao; }

    /**
     * apanha os pormenores do problema
     * 
     * @return o texto dos detalhes
     */
    public String getDetalhes() { 
        return detalhes; }

    /**
     * apanha o ponto em que a queixa esta
     * 
     * @return o estado atual
     */
    public String getEstado() { 
        return estado; }

    /**
     * altera o ponto em que a queixa esta
     * 
     * @param estado novo estado a guardar
     */
    public void setEstado(String estado) { 
        this.estado = estado; }

    /**
     * apanha a razao de ter sido rejeitada
     * 
     * @return o motivo do chumbo
     */
    public String getMotivoRejeicao() { 
        return motivoRejeicao; }

    /**
     * guarda a razao para rejeitar a queixa
     * 
     * @param motivoRejeicao porque foi chumbada
     */
    public void setMotivoRejeicao(String motivoRejeicao) { 
        this.motivoRejeicao = motivoRejeicao; }

    /**
     * apanha a pessoa que fez a queixa
     * 
     * @return o utilizador
     */
    public Utilizador getUtilizador() { 
        return utilizador; }

    /**
     * define quem e a pessoa que esta a queixar se
     * 
     * @param utilizador quem fez a queixa
     */
    public void setUtilizador(Utilizador utilizador) { 
        this.utilizador = utilizador; }

    /**
     * apanha a coisa relacionada com a queixa
     * 
     * @return o objeto em causa
     */
    public Objeto getObjeto() { 
        return objeto; }

    /**
     * associa uma coisa a esta queixa
     * 
     * @param objeto a coisa a associar
     */
    public void setObjeto(Objeto objeto) { 
        this.objeto = objeto; }

    public String getTipoLevantamento() { return tipoLevantamento; }
    public Local getLocalLevantamento() { return localLevantamento; }
    public PontoLocky getPontoLockyEscolhido() { return pontoLockyEscolhido; }

    public void escolherPresencial(Local local) {
        if (local == null) throw new IllegalArgumentException("Local obrigatorio.");
        tipoLevantamento = "PRESENCIAL";
        localLevantamento = local;
        pontoLockyEscolhido = null;
    }

    public void escolherLocky(PontoLocky ponto) {
        if (ponto == null) throw new IllegalArgumentException("Ponto Locky obrigatorio.");
        tipoLevantamento = "LOCKY";
        pontoLockyEscolhido = ponto;
        localLevantamento = ponto.getLocal();
    }

    /**
     * mostra os dados da queixa em texto corrido
     * 
     * @return resumo da queixa
     */
    @Override
    public String toString() {
        return "Reclamação ID: " + id + " | Estado: " + estado + " | Objeto ID: " + (objeto != null ? objeto.getId() : "N/A") + 
               " | Reclamante: " + (utilizador != null ? utilizador.getNomeUtilizador() : "N/A");
    }
}