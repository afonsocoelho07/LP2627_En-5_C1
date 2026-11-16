package upt.pt;

import jakarta.persistence.*;
import java.security.SecureRandom;

/**
 * Representa um cacifo individual de um ponto Locky.
 * Projeto académico: o código de acesso é guardado em texto simples.
 */
@Entity
@Table(name = "locky", uniqueConstraints = @UniqueConstraint(
        columnNames = {"ponto_locky_id", "codigo_cacifo"}))
public class Locky {

    public enum EstadoLocky { LIVRE, RESERVADO, OCUPADO }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_TENTATIVAS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exemplo: L01 (identifica o cacifo; não é o código secreto).
    @Column(name = "codigo_cacifo", nullable = false, length = 20)
    private String codigoCacifo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoLocky estado = EstadoLocky.LIVRE;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ponto_locky_id", nullable = false)
    private PontoLocky pontoLocky;

    @OneToOne
    @JoinColumn(name = "reclamacao_id", unique = true)
    private Reclamacao reclamacaoReservada;

    @OneToOne
    @JoinColumn(name = "objeto_id", unique = true)
    private Objeto objeto;

    // Apenas para a simulação da cadeira: não usar em cacifos reais.
    @Column(name = "codigo_acesso", length = 8)
    private String codigoAcesso;

    @Column(name = "tentativas_falhadas", nullable = false)
    private int tentativasFalhadas = 0;

    // Indica apenas autorização lógica de abertura (não sensores reais).
    @Column(name = "porta_aberta", nullable = false)
    private boolean portaAberta = false;

    // O Hibernate precisa de um construtor sem argumentos.
    public Locky() { }

    public Locky(String codigoCacifo, PontoLocky pontoLocky) {
        if (codigoCacifo == null || codigoCacifo.isBlank() || pontoLocky == null) {
            throw new IllegalArgumentException("Código e ponto Locky são obrigatórios.");
        }
        this.codigoCacifo = codigoCacifo.trim().toUpperCase();
        this.pontoLocky = pontoLocky;
    }

    public Long getId() { return id; }
    public String getCodigoCacifo() { return codigoCacifo; }
    public EstadoLocky getEstado() { return estado; }
    public PontoLocky getPontoLocky() { return pontoLocky; }
    public void setPontoLocky(PontoLocky pontoLocky) { this.pontoLocky = pontoLocky; }
    public Reclamacao getReclamacaoReservada() { return reclamacaoReservada; }
    public Objeto getObjeto() { return objeto; }
    public boolean isPortaAberta() { return portaAberta; }
    public int getTentativasFalhadas() { return tentativasFalhadas; }

    /** Um cacifo reservado também é considerado indisponível. */
    public boolean estaLivre() {
        return estado == EstadoLocky.LIVRE;
    }

    /** Reserva o cacifo para uma reclamação aprovada. */
    public boolean reservar(Reclamacao reclamacao) {
        if (reclamacao == null || reclamacao.getObjeto() == null) {
            throw new IllegalArgumentException("Reclamação inválida.");
        }
        if (!"Aprovada".equalsIgnoreCase(reclamacao.getEstado()) || !estaLivre()) {
            return false;
        }
        reclamacaoReservada = reclamacao;
        estado = EstadoLocky.RESERVADO;
        codigoAcesso = null;
        tentativasFalhadas = 0;
        portaAberta = false;
        return true;
    }

    /** Cancela uma reserva antes de depositar o objeto. */
    public boolean cancelarReserva() {
        if (estado != EstadoLocky.RESERVADO) return false;
        reclamacaoReservada = null;
        estado = EstadoLocky.LIVRE;
        codigoAcesso = null;
        tentativasFalhadas = 0;
        portaAberta = false;
        return true;
    }

    /** O funcionário confirma que depositou o objeto da reclamação. */
    public boolean depositar(Objeto objetoParaDepositar) {
        if (objetoParaDepositar == null) {
            throw new IllegalArgumentException("Objeto inválido.");
        }
        if (estado != EstadoLocky.RESERVADO) return false;
        if (reclamacaoReservada == null || reclamacaoReservada.getObjeto() == null) {
            throw new IllegalStateException("Não existe uma reclamação válida nesta reserva.");
        }
        Long idEsperado = reclamacaoReservada.getObjeto().getId();
        Long idRecebido = objetoParaDepositar.getId();
        if (idEsperado == null || idRecebido == null) {
            throw new IllegalArgumentException("O objeto deve ter um ID na base de dados.");
        }
        if (!idEsperado.equals(idRecebido)) return false;

        objeto = objetoParaDepositar;
        estado = EstadoLocky.OCUPADO;
        portaAberta = false;
        return true;
    }

    /** Cria um código aleatório de oito dígitos, incluindo zeros iniciais. */
    private String gerarCodigo() {
        return String.format("%08d", RANDOM.nextInt(100_000_000));
    }

    /** Gera o código uma só vez depois de confirmado o depósito. */
    public String emitirCodigoAcesso() {
        if (estado != EstadoLocky.OCUPADO || objeto == null) {
            throw new IllegalStateException("O objeto ainda não foi depositado.");
        }
        if (portaAberta || codigoAcesso != null || tentativasFalhadas >= MAX_TENTATIVAS) {
            throw new IllegalStateException("Não é possível emitir outro código de acesso.");
        }
        codigoAcesso = gerarCodigo();
        return codigoAcesso;
    }

    /**
     * Simula a abertura: código certo -> true.
     * Um código errado incrementa o contador (até cinco tentativas).
     * Após uma abertura, o código é invalidado.
     */
    public boolean abrirComCodigo(String codigo) {
        if (codigo == null) throw new IllegalArgumentException("Código inválido.");
        if (estado != EstadoLocky.OCUPADO || portaAberta || codigoAcesso == null
                || tentativasFalhadas >= MAX_TENTATIVAS) return false;

        if (!codigoAcesso.equals(codigo)) {
            tentativasFalhadas++;
            return false;
        }
        portaAberta = true;
        codigoAcesso = null;
        return true;
    }

    /** Após verificação presencial, o administrador repõe as tentativas. */
    public boolean desbloquearTentativas() {
        if (estado != EstadoLocky.OCUPADO || portaAberta || codigoAcesso == null
                || tentativasFalhadas < MAX_TENTATIVAS) return false;
        tentativasFalhadas = 0;
        return true;
    }

    /**
     * A confirmação final de recolha será integrada pelo colega.
     * Não é chamado por nenhum menu desta versão.
     */
    public Objeto libertarAposConfirmacao() {
        if (estado != EstadoLocky.OCUPADO || !portaAberta || objeto == null) {
            throw new IllegalStateException("Ainda não foi autorizada a retirada do objeto.");
        }
        Objeto retirado = objeto;
        objeto = null;
        reclamacaoReservada = null;
        estado = EstadoLocky.LIVRE;
        portaAberta = false;
        codigoAcesso = null;
        tentativasFalhadas = 0;
        return retirado;
    }

    @Override
    public String toString() {
        return "Cacifo " + codigoCacifo + " | " + estado;
    }
}
