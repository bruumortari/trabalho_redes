/**
 * Classe base abstrata para a implementação do protocolo de roteamento RIP (Routing Information Protocol).
 * <p>
 * Esta classe define a estrutura comum compartilhada tanto pelos nós comuns quanto pelo nó gerente.
 * Ela implementa {@link UnicastServiceUserInterface} para receber mensagens da camada de transporte (Unicast)
 * e {@link Runnable} para permitir que o protocolo de roteamento execute em sua própria thread.
 * </p>
 */
public abstract class AbstractRIP implements UnicastServiceUserInterface, Runnable{
    protected UnicastServiceInterface usi;
    protected short idNode;
    /**
        * Define a interface de serviço Unicast a ser utilizada por este protocolo.
        * <p>
        * Este método é utilizado para realizar a injeção de dependência da camada inferior,
        * permitindo que o RIP envie dados pela rede.
        * </p>
        *
        * @param usi A instância da interface de serviço Unicast.
    */
    public void SetUSI(UnicastServiceInterface usi) {
        this.usi = usi;
    }

    /**
     * Construtor base para o protocolo RIP.
     *
     * @param id O identificador único (ID) do nó que está sendo inicializado.
     */
    public AbstractRIP(short id){
        this.idNode = id;
    }
}
