package upt.pt;

import java.util.ArrayList;
import java.util.List;

/**
 * classe que representa uma prateleira do armazem onde se guardam as coisas
 */
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "prateleira")
public class Prateleira {

    /**
     * numero identificador unico da prateleira na base de dados
     */
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * codigo ou nome que identifica a prateleira no armazem
     */
    @jakarta.persistence.Column(name = "codigo_prateleira")
    private String codigoPrateleira;

    /**
     * quantia maxima de coisas que cabem aqui
     */
    @jakarta.persistence.Column(name = "lotacao_prateleira")
    private int lotacaoPrateleira;

    /**
     * armazem onde esta prateleira se encontra
     */
    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "armazem_id")
    private Armazem armazem;

    /**
     * lista com as coisas que estao arrumadas nesta prateleira
     */
    @jakarta.persistence.OneToMany(mappedBy = "prateleira")
    private List<Objeto> objetos = new ArrayList<>(); 

    /**
     * construtor basico para o sistema conseguir criar a prateleira
     */
    public Prateleira() {
    }

    /**
     * cria uma prateleira com o seu codigo e a sua capacidade
     * 
     * @param codigoPrateleira codigo de identificacao
     * @param lotacaoPrateleira quantia maxima que cabe
     */
    public Prateleira(String codigoPrateleira, int lotacaoPrateleira) {
        this.codigoPrateleira = codigoPrateleira;
        this.lotacaoPrateleira = lotacaoPrateleira;
    }

    /**
     * apanha o id da prateleira
     * 
     * @return o id unico
     */
    public Long getId() { 
        return id; 
    }
    
    /**
     * apanha o codigo da prateleira
     * 
     * @return o codigo
     */
    public String getCodigoPrateleira() { 
        return codigoPrateleira; 
    }
    
    /**
     * muda o codigo da prateleira
     * 
     * @param codigoPrateleira novo codigo
     */
    public void setCodigoPrateleira(String codigoPrateleira) { 
        this.codigoPrateleira = codigoPrateleira; 
    }
    
    /**
     * apanha quanto espaco total existe na prateleira
     * 
     * @return limite de lotacao
     */
    public int getLotacaoPrateleira() { 
        return lotacaoPrateleira; 
    }
    
    /**
     * altera o limite de coisas que a prateleira aguenta
     * 
     * @param lotacaoPrateleira nova capacidade
     */
    public void setLotacaoPrateleira(int lotacaoPrateleira) { 
        this.lotacaoPrateleira = lotacaoPrateleira; 
    }
    
    /**
     * apanha o armazem onde esta a prateleira
     * 
     * @return o armazem
     */
    public Armazem getArmazem() { 
        return armazem; 
    }
    
    /**
     * guarda o armazem a que esta prateleira pertence
     * 
     * @param armazem o armazem correto
     */
    public void setArmazem(Armazem armazem) { 
        this.armazem = armazem; 
    }
    
    /**
     * apanha a lista das coisas todas guardadas na prateleira
     * 
     * @return lista de objetos
     */
    public List<Objeto> getObjetos() { 
        return objetos; 
    }

    /**
     * mostra os dados basicos da prateleira numa frase
     * 
     * @return texto com id codigo e lotacao
     */
    @Override
    public String toString() {
        return "Prateleira: " + id + "| Codigo: " + codigoPrateleira+ "| Lotacao: " + lotacaoPrateleira ;
    }
}