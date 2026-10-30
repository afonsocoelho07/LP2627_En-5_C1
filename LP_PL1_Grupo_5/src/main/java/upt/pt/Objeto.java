package upt.pt;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * classe que representa um objeto no sistema
 */
@Entity
@Table(name = "objeto")
public class Objeto {

    /** identificador unico do objeto */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** data e hora da perda/achado do objeto */
    @Column(name = "data_hora_perda")
    private LocalDateTime data;

    /** marca do objeto */
    private String marca;
    
    /** tipo do objeto */
    private String tipo; 
    
    /** cor do objeto */
    private String cor;
    
    /** estado atual do objeto */
    private String estado; 

    /** utilizador associado ao objeto */
    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador utilizador;

    /** prateleira onde o objeto esta guardado */
    @ManyToOne
    @JoinColumn(name = "prateleira_id")
    private Prateleira prateleira;

    /** outro objeto associado a este (CORRIGIDO: Tem de ser Um-para-Um) */
    @OneToOne
    @JoinColumn(name = "associado_id")
    private Objeto objetoAssociado; 

    /** local onde o objeto foi recolhido */
    @ManyToOne
    @JoinColumn(name = "local_recolha_id")
    private Local localRecolha;

    /** construtor vazio por omissao */
    public Objeto() {}

    /** construtor com os dados principais do objeto */
    public Objeto(LocalDateTime data, String marca, String tipo, String cor, String estado) {
        this.data = data;
        this.marca = marca;
        this.tipo = tipo;
        this.cor = cor;
        this.estado = estado;
    }

    
    /** devolve o identificador do objeto */
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    /** devolve a data e hora */
    public LocalDateTime getData() { 
        return data; 
    }
    
    public void setData(LocalDateTime data) {
        this.data = data;
    }

    /** devolve a marca do objeto */
    public String getMarca() { 
        return marca; 
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }

    /** devolve o tipo do objeto */
    public String getTipo() { 
        return tipo; 
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /** devolve a cor do objeto */
    public String getCor() { 
        return cor; 
    }
    
    public void setCor(String cor) {
        this.cor = cor;
    }

    /** devolve o estado do objeto */
    public String getEstado() { 
        return estado; 
    }

    /** define o estado do objeto */
    public void setEstado(String estado) { 
        this.estado = estado; 
    }

    /** devolve o utilizador associado */
    public Utilizador getUtilizador() { 
        return utilizador; 
    }

    /** define o utilizador associado */
    public void setUtilizador(Utilizador utilizador) { 
        this.utilizador = utilizador; 
    }

    /** devolve a prateleira onde esta guardado */
    public Prateleira getPrateleira() { 
        return prateleira; 
    }

    /** define a prateleira onde fica guardado */
    public void setPrateleira(Prateleira prateleira) { 
        this.prateleira = prateleira; 
    }

    /** devolve o objeto associado */
    public Objeto getObjetoAssociado() { 
        return objetoAssociado; 
    }

    /** define o objeto associado */
    public void setObjetoAssociado(Objeto objetoAssociado) { 
        this.objetoAssociado = objetoAssociado; 
    }

    /** devolve o local de recolha */
    public Local getLocalRecolha() { 
        return localRecolha; 
    }

    /** define o local de recolha */
    public void setLocalRecolha(Local localRecolha) { 
        this.localRecolha = localRecolha; 
    }

    /** cria um texto com os dados principais do objeto para mostrar */
    @Override
    public String toString() {
        String infoLocal = (localRecolha != null) ? " | Entregue em: " + localRecolha.getNomeEdificio() : "";
        return "ID: " + id + " | Marca: " + marca + " | Tipo: " + tipo + " | Cor: " + cor + " | Estado: " + estado + infoLocal;
    }
}