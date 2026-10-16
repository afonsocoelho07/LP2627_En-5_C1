package upt.pt;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "objeto")
public class Objeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora_perda")
    private LocalDateTime dataHoraPerda;

    private String marca;
    private String tipo; 
    private String cor;
    private String estado; 

    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador utilizador;

    @ManyToOne
    @JoinColumn(name = "prateleira_id")
    private Prateleira prateleira;

    @ManyToOne
    @JoinColumn(name = "associado_id")
    private Objeto objetoAssociado; 

    @ManyToOne
    @JoinColumn(name = "local_recolha_id")
    private Local localRecolha;

    public Objeto() {}

    public Objeto(LocalDateTime dataHoraPerda, String marca, String tipo, String cor, String estado) {
        this.dataHoraPerda = dataHoraPerda;
        this.marca = marca;
        this.tipo = tipo;
        this.cor = cor;
        this.estado = estado;
    }

    public Long getId() { 
    	return id; }
    public LocalDateTime getDataHoraPerda() { 
    	return dataHoraPerda; }
    public String getMarca() { 
    	return marca; }
    public String getTipo() { 
    	return tipo; }
    public String getCor() { 
    	return cor; }
    public String getEstado() { 
    	return estado; }
    public void setEstado(String estado) { 
    	this.estado = estado; }
    
    public Utilizador getUtilizador() { 
    	return utilizador; }
    public void setUtilizador(Utilizador utilizador) { 
    	this.utilizador = utilizador; }
    public Prateleira getPrateleira() { 
    	return prateleira; }
    public void setPrateleira(Prateleira prateleira) { 
    	this.prateleira = prateleira; }
    
    public Objeto getObjetoAssociado() { 
    	return objetoAssociado; }
    public void setObjetoAssociado(Objeto objetoAssociado) { 
    	this.objetoAssociado = objetoAssociado; }
    
    public Local getLocalRecolha() { 
    	return localRecolha; }
    public void setLocalRecolha(Local localRecolha) { 
    	this.localRecolha = localRecolha; }

    @Override
    public String toString() {
        String infoLocal = (localRecolha != null) ? " | Entregue em: " + localRecolha.getNomeEdificio() : "";
        return "ID: " + id + " | Marca: " + marca + " | Tipo: " + tipo + " | Cor: " + cor + " | Estado: " + estado + infoLocal;
    }
}