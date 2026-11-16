package upt.pt;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

/** Representa uma estacao com varios cacifos no mesmo edificio. */
@Entity
@Table(name = "ponto_locky")
public class PontoLocky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    // Muitos pontos podem pertencer ao mesmo Local.
    @ManyToOne(optional = false)
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    // Um ponto possui varios cacifos, ordenados pelo seu codigo.
    @OneToMany(mappedBy = "pontoLocky", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("codigoCacifo ASC")
    private List<Locky> cacifos = new ArrayList<>();

    public PontoLocky() { }

    public PontoLocky(String nome, String descricao, Local local) {
        if (nome == null || nome.isBlank() || local == null) {
            throw new IllegalArgumentException("Nome e local sao obrigatorios.");
        }
        this.nome = nome.trim();
        this.descricao = descricao;
        this.local = local;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }
    public List<Locky> getCacifos() { return cacifos; }

    /** Mantem os dois lados da relacao sincronizados. */
    public void addCacifo(Locky cacifo) {
        if (cacifo == null || cacifo.getCodigoCacifo() == null) {
            throw new IllegalArgumentException("Cacifo invalido.");
        }
        for (Locky existente : cacifos) {
            if (existente.getCodigoCacifo().equalsIgnoreCase(cacifo.getCodigoCacifo())) {
                throw new IllegalArgumentException("Ja existe um cacifo com esse codigo.");
            }
        }
        cacifos.add(cacifo);
        cacifo.setPontoLocky(this);
    }

    /** Devolve o primeiro cacifo LIVRE ou null, sem o reservar. */
    public Locky primeiroCacifoDisponivel() {
        for (Locky cacifo : cacifos) {
            if (cacifo.estaLivre()) {
                return cacifo;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Ponto Locky #" + id + " | " + nome
            + " | Edificio: " + (local != null ? local.getNomeEdificio() : "N/A")
            + " | " + (descricao != null ? descricao : "");
    }
}
