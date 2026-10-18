package upt.pt;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "utilizador")
public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_utilizador")
    private String tipoUtilizador;

    private String password;
    
    @Column(name = "nome_utilizador")
    private String nomeUtilizador;

    
    @Column(unique = true)
    private String email;
    
    
    @Column(unique = true)
    private int contacto;

    @OneToMany(mappedBy = "utilizador", cascade = CascadeType.ALL)
    private List<Objeto> objetos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "local_id")
    private Local localTrabalho;

    public Utilizador() {}

    public Utilizador(String tipoUtilizador, String password, String nomeUtilizador, String email, int contacto) {
        this.tipoUtilizador = tipoUtilizador;
        this.password = password;
        this.nomeUtilizador = nomeUtilizador;
        this.email = email;
        this.contacto = contacto;
    }

    public Long getId() { return id; }
    public String getTipoUtilizador() { return tipoUtilizador; }
    public String getPassword() { return password; }
    public String getNomeUtilizador() { return nomeUtilizador; }
    public String getEmail() { return email; }
    public int getContacto() { return contacto; }
    
    public List<Objeto> getObjetos() { return objetos; }
    public Local getLocalTrabalho() { return localTrabalho; }
    public void setLocalTrabalho(Local localTrabalho) { this.localTrabalho = localTrabalho; }

    @Override
    public String toString() {
        return "id: " + id + " | nome: " + nomeUtilizador + " | email: " + email + " | contacto: " + contacto + " | perfil: " + tipoUtilizador + 
               (localTrabalho != null ? " | local: " + localTrabalho.getNomeEdificio() : "");
    }
}