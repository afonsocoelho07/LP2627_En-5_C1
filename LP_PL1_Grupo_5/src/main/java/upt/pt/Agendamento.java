package upt.pt;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * esta classe representa a tabela agendamento guarda a data a hora o Cliente e o objeto
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
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

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
     * devolve o Cliente associado
     * 
     * @return o Cliente
     */
    public Cliente getCliente() {  
        return cliente; 
    }
    
    /**
     * define o Cliente associado
     * 
     * @param Cliente o novo Cliente
     */
    public void setCliente(Cliente cliente) {  
        this.cliente = cliente; 
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