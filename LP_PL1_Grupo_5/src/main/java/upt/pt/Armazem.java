package upt.pt;

import java.util.ArrayList;
import java.util.List;

@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "Armazem")
public class Armazem {

	@jakarta.persistence.Id
	@jakarta.persistence.GeneratedValue(
			strategy = jakarta.persistence.GenerationType.IDENTITY)
	private Long id;
	
	@jakarta.persistence.Column(name ="nome_armazem")
	private String nomeArmazem;
	
	@jakarta.persistence.ManyToOne
	@jakarta.persistence.JoinColumn(name = "local_id")
	private Local local;
	
	@jakarta.persistence.OneToMany(mapppedBy = "armazem", cascade =
			jakarta.persistence.CascadeType.ALL)
	private List <Prateleira> prateleiras = new ArrayList<>();
	
	public Armazem() {}
	
	public Armazem(String nomeArmazem) {
		this.nomeArmazem = nomeArmazem;
	}

	public String getNomeArmazem() {
		return nomeArmazem;
	}

	public void setNomeArmazem(String nomeArmazem) {
		this.nomeArmazem = nomeArmazem;
	}

	public Local getLocal() {
		return local;
	}

	public void setLocal(Local local) {
		this.local = local;
	}
	
	public Long getId() {
		return id;
	}
	
	public List<Prateleira> getPrateleiras() {
		return prateleiras;
	}
	
	public void addPrateleira(Prateleira prateleira) {
		prateleiras.add(prateleira);
		prateleira.setArmazem(this);
	}
	
	@Override
	public String toString() {
		return "Armazem: " + id + "; Nome: " + nomeArmazem;
	}
}
