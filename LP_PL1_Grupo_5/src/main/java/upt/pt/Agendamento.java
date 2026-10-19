package upt.pt;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador utilizador;

    @OneToOne
    @JoinColumn(name = "objeto_id")
    private Objeto objeto;

    public Agendamento() {}

    public Agendamento(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Long getId() { 
    	return id; }
    public LocalDateTime getDataHora() { 
    	return dataHora; }
    
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
        return "Agendamento ID: " + id + " | Data/Hora: " + dataHora +" | Objeto ID: " + (objeto != null ? objeto.getId() : "N/A");
    }
}