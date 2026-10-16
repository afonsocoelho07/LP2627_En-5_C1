package upt.pt;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamacao")
public class Reclamacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_reclamacao")
    private LocalDateTime dataReclamacao;

    private String detalhes;
    
    private String estado; 

    @Column(name = "motivo_rejeicao")
    private String motivoRejeicao;

    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador utilizador;

    @ManyToOne
    @JoinColumn(name = "objeto_id")
    private Objeto objeto;

    public Reclamacao() {}

    public Reclamacao(LocalDateTime dataReclamacao, String detalhes, String estado) {
        this.dataReclamacao = dataReclamacao;
        this.detalhes = detalhes;
        this.estado = estado;
    }

    public Long getId() { 
        return id; }
    public LocalDateTime getDataReclamacao() { 
        return dataReclamacao; }
    public String getDetalhes() { 
        return detalhes; }
    public String getEstado() { 
        return estado; }
    public void setEstado(String estado) { 
        this.estado = estado; }
    public String getMotivoRejeicao() { 
        return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { 
        this.motivoRejeicao = motivoRejeicao; }
    
    public Utilizador getUtilizador() { 
        return utilizador; }
    public void setUtilizador(Utilizador utilizador) { 
        this.utilizador = utilizador; }
    public Objeto getObjeto() { 
        return objeto; }
    public void setObjeto(Objeto objeto) { 
        this.objeto = objeto; }

    @Override
    public String toString() {
        return "Reclamação ID: " + id + " | Estado: " + estado + " | Objeto ID: " + (objeto != null ? objeto.getId() : "N/A") + 
               " | Reclamante: " + (utilizador != null ? utilizador.getNomeUtilizador() : "N/A");
    }
}