package upt.pt;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * esta classe representa a tabela agendamento guarda a data a hora o utilizador e o objeto
 */
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

    /**
     * construtor vazio exigido pelo jpa
     */
    public Agendamento() {}

    /**
     * cria um agendamento com uma data e hora
     * 
     * @param dataHora a data e hora pretendida
     */
    public Agendamento(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    /**
     * devolve o id deste agendamento
     * 
     * @return o id
     */
    public Long getId() {  
        return id; 
    }

    /**
     * devolve a data e hora do agendamento
     * 
     * @return a data e hora
     */
    public LocalDateTime getDataHora() {  
        return dataHora; 
    }
    
    /**
     * devolve o utilizador associado
     * 
     * @return o utilizador
     */
    public Utilizador getUtilizador() {  
        return utilizador; 
    }
    
    /**
     * define o utilizador associado
     * 
     * @param utilizador o novo utilizador
     */
    public void setUtilizador(Utilizador utilizador) {  
        this.utilizador = utilizador; 
    }
    
    /**
     * devolve o objeto associado
     * 
     * @return o objeto
     */
    public Objeto getObjeto() {  
        return objeto; 
    }
    
    /**
     * define o objeto associado
     * 
     * @param objeto o novo objeto
     */
    public void setObjeto(Objeto objeto) {  
        this.objeto = objeto; 
    }

    /**
     * transforma o agendamento numa frase com os dados principais
     * 
     * @return a frase com a informacao
     */
    @Override
    public String toString() {
        return "Agendamento ID: " + id + " | Data/Hora: " + dataHora +" | Objeto ID: " + (objeto != null ? objeto.getId() : "N/A");
    }
}