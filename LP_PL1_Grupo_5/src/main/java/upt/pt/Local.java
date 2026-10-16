package upt.pt;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "locais")
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_edificio")
    private String nomeEdificio;

    @Column(name = "descricao_local")
    private String descricaoLocal;
    
    @Column(name = "hora_inicio")
    private String horaInicio;

    @Column(name = "hora_fim")
    private String horaFim;

    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL)
    private List<Armazem> armazens = new ArrayList<>();

    public Local() {
    }

    public Local(String nomeEdificio, String descricaoLocal, String horaInicio, String horaFim) {
        this.nomeEdificio = nomeEdificio;
        this.descricaoLocal = descricaoLocal;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getId() { 
    	return id; }
    public String getNomeEdificio() { 
    	return nomeEdificio; }
    public void setNomeEdificio(String nomeEdificio) { 
    	this.nomeEdificio = nomeEdificio; }
    public String getDescricaoLocal() { 
    	return descricaoLocal; }
    public void setDescricaoLocal(String descricaoLocal) { 
    	this.descricaoLocal = descricaoLocal; }
    
    public String getHoraInicio() { 
    	return horaInicio; }
    //
    public void setHoraInicio(String horaInicio) { 
    	this.horaInicio = horaInicio; }
    //
    public String getHoraFim() { 
    	return horaFim; }
    //
    public void setHoraFim(String horaFim) { 
    	this.horaFim = horaFim; }
    //
    public List<Armazem> getArmazens() { 
    	return armazens; }
    public void addArmazem(Armazem armazem) {
        armazens.add(armazem);
        armazem.setLocal(this);
    }

    @Override
    public String toString() {
        return "Local: " + id + " | Edificio: " + nomeEdificio + " | Horário: " + horaInicio + " às " + horaFim;
    }
}