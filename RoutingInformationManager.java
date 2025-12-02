import java.util.HashMap;
import java.util.Set;
/**
 * Implementação do Gerente do Protocolo de Roteamento.
 * Esta classe atua como uma entidade controladora que pode consultar e modificar o estado da rede.
 * Diferente dos nós comuns, o Gerente não participa do roteamento de dados, mas possui privilégios para:
 * <ul>
 * <li>Consultar a Tabela de Distância de qualquer nó.</li>
 * <li>Consultar o custo de um enlace específico.</li>
 * <li>Alterar o custo de um enlace (afetando o cálculo de rotas dos nós).</li>
 * </ul>
 * A classe implementa uma <b>Máquina de Estados</b> para gerir as requisições, garantindo que
 * uma operação seja concluída (ou expire por timeout) antes de iniciar outra.
 */
public class RoutingInformationManager extends AbstractRIP implements Runnable, RoutingProtocolManagementInterface,
        UnicastServiceUserInterface {
    private UnicastServiceInterface usi;
    private RoutingProtocolManagementServiceUserInterface rpmsui;
    private enum State {Idle, LinkCostRequest, LinkCostSetRequest_1,
        LinkCostSetRequest_2, DistanceTableRequest}
    private State currentState;
    private short pendingNodeA;
    private short pendingNodeB;
    private int pendingCost;
    private long timeRequest;
    private int numberNodes;
    private HashMap<String, Integer> linksCost;
    private Set<Short> nodesGraph;
    /**
     * Construtor do Gerente.
     *
     * @param id ID do nó gerente (geralmente 0).
     * @param numbeNodes Número total de nós na rede.
     * @param linksCost Mapa da topologia da rede.
     * @param nodesGraph Conjunto de nós ativos.
     */
    public RoutingInformationManager(short id, int numbeNodes, HashMap<String, Integer> linksCost, Set<Short> nodesGraph) {
        super(id);
        this.numberNodes = numbeNodes;
        this.linksCost = linksCost;
        this.nodesGraph = nodesGraph;
        currentState = State.Idle;
    }
    /**
     * Define a ligação com a camada de aplicação.
     * @param rpmsui Instância da interface de usuário.
     */
    public void  SetRPMUser(RoutingProtocolManagementServiceUserInterface rpmsui){
        this.rpmsui = rpmsui;
    }
    /**
     * Define a ligação com a camada de unicast.
     * @param usi Instância do protocolo de transporte.
     */
    public void SetUSI(UnicastServiceInterface usi){
        this.usi = usi;
    }
    /**
     * Primitiva utilizada pela aplicação para requisitar a tabela de distância de um nó.
     * <p>
     * Envia uma mensagem {@code RIPRQT} para o nó alvo.
     * </p>
     *
     * @param id ID do nó alvo.
     * @return {@code true} se a solicitação foi enviada, {@code false} se o gerente estiver ocupado ou o ID for inválido.
     */
    @Override
    public boolean getDistanceTable(short id) {
        if(currentState != State.Idle) return false; // Só funciona se ocioso

        if(nodesGraph.contains(id)){ //Id válido
            //Muda o estado para Esperando Requisição de Tabela
            currentState = State.DistanceTableRequest;
            //Guarda Id para se precisar requisitar de novo
            pendingNodeA = id;
            // Guarda hora de requisição
            timeRequest = System.currentTimeMillis();
            //Monta mensagem
            String msg = "RIPRQT "+ id;
            //Manda para o unicast
            usi.UPDataReq(id, msg);
            return true;
        }
        return false;
    }
    /**
     * Primitiva utilizada pela aplicação para requisitar o custo do enlace conectando dois nós (A/B).
     * <p>
     * Envia uma mensagem {@code RIPGET} para o nó de origem (node1).
     * </p>
     *
     * @param node1 ID do primeiro nó.
     * @param node2 ID do segundo nó.
     * @return {@code true} se enviado com sucesso, {@code false} se ocupado ou link inexistente.
     */
    @Override
    public boolean getLinkCost(short node1, short node2) {
        if(currentState != State.Idle) return false; // Só funciona se ocioso

        //Monta par
        String pair1 = node1 + ":" + node2;
        String pair2 = node2 + ":" + node1;
        if(linksCost.get(pair1) ==null && linksCost.get(pair2) ==null) return false; // Link não existe

        // Muda o estado para Esperando Requisição de Custo
        currentState = State.LinkCostRequest;
        //Guarda Ids para se precisar requisitar de novo
        pendingNodeA = node1;
        pendingNodeB = node2;
        // Guarda hora de requisição
        timeRequest = System.currentTimeMillis();
        //Monta mensagem
        String msg = "RIPGET "+ node1 + " "+node2;
        //Manda para o unicast
        usi.UPDataReq(node1, msg);
        return true;
    }

    /**
     * Primitiva utilizada pela aplicação para redefinir o custo do enlace conectando dois nós (A/B).
     * Esta operação é realizada em duas etapas para garantir consistência:
     * <ol>
     * <li>Envia {@code RIPSET} para o {@code node1} e aguarda confirmação.</li>
     * <li>Após receber a confirmação, envia {@code RIPSET} para o {@code node2}.</li>
     * </ol>
     *
     * @param node1 ID do primeiro nó.
     * @param node2 ID do segundo nó.
     * @param cost Novo custo do enlace.
     * @return {@code true} se o processo iniciou, {@code false} caso contrário.
     */
    @Override
    public boolean setLinkCost(short node1, short node2, int cost) {
        if(currentState != State.Idle) return false; // Só funciona se ocioso

        //Monta par
        String pair1 = node1 + ":" + node2;
        String pair2 = node2 + ":" + node1;
        if(linksCost.get(pair1) ==null && linksCost.get(pair2) ==null) return false; // Link não existe

        // Muda o estado para Esperando a Primeira Requisição Após Mudança de Custo
        currentState = State.LinkCostSetRequest_1;
        //Guarda Ids e Custo para se precisar requisitar de novo
        pendingNodeA = node1;
        pendingNodeB = node2;
        pendingCost = cost;
        // Guarda hora de requisição
        timeRequest = System.currentTimeMillis();
        //Monta mensagem para o primeiro nó informado
        String msg = "RIPSET "+ node1 + " "+node2 + " "+cost;
        //Manda pelo unicast para o primeiro nó informado
        usi.UPDataReq(node1, msg);
        return true;

    }
    /**
     * Processa as respostas recebidas da rede.
     * Trata as mensagens:
     * <ul>
     * <li><b>RIPNTF:</b> Notificação de custo. Pode ser apenas uma resposta de consulta ou a confirmação
     * de uma etapa de alteração de custo (avançando a máquina de estados).</li>
     * <li><b>RIPRSP:</b> Resposta contendo a tabela de distância completa de um nó.</li>
     * </ul>
     *
     * @param originId ID de quem enviou a resposta.
     * @param message Conteúdo da mensagem.
     */
    @Override
    public void UPDataInd(short originId, String message) { //Recebeu mensagem
        String[] parts = message.split(" ");
        switch (parts[0]){
            case "RIPNTF": //Mensagem do Tipo: Recebendo custo de nó
                //<RIPNTF><espaço><RIPNode_A><espaço><RIPNode_B><espaço><custo>
                //Se está no estado Esperando a Primeira Requisição Após Mudança de Custo
                if(currentState == State.LinkCostSetRequest_1){
                    // Muda para Esperando a Segunda Requisição Após Mudança de Custo
                    currentState = State.LinkCostSetRequest_2;
                    // Monta mensagem para o segundo nó informado
                    String msg = "RIPSET "+ pendingNodeB + " "+pendingNodeA + " "+pendingCost;
                    // Manda pelo unicast para o segundo nó informado
                    usi.UPDataReq(pendingNodeB, msg);
                    // Guarda hora de requisição
                    timeRequest = System.currentTimeMillis();
                }else{
                    // Se não estiver
                    currentState = State.Idle; //Volta para ocioso
                    //Identifica as partes da mensagem
                    short nodeA = Short.parseShort(parts[1]);
                    short nodeB = Short.parseShort(parts[2]);
                    int cost = Integer.parseInt(parts[3]);
                    //Notifica GUI
                    rpmsui.linkCostIndication(nodeA, nodeB, cost);
                }


                break;
            case "RIPRSP": //Mensagem do Tipo: Recebendo Tabela de Distancia
                //<RIPRSP><espaço><RIPNode><espaço><tabela_distancia>
                if(currentState == State.DistanceTableRequest) // Estava esperando a requisição
                    currentState = State.Idle; //Volta para o idle
                int neighborNodes = parts.length - 2; //Quantidade de nós vizinhos do nó que mandou a msg
                short node = Short.parseShort(parts[1]); // Id do nó que enviou a mensagem
                int[][] table = new int[neighborNodes][numberNodes]; // Variável para guardar a tabela
                String[] vector;
                //Construção a partir da mensagem
                for(int i = 2; i < parts.length; i++){
                    vector = parts[i].split(":");
                    for(int j = 0; j< numberNodes; j++){
                        table[i-2][j] = Integer.parseInt(vector[j]);
                    }
                }
                //Notifica a GUI
                rpmsui.distanceTableIndication(node, table);
                break;
        }
    }
    /**
     * Loop principal de controle de tempo.
     * <p>
     * Verifica periodicamente (a cada 1 segundo) se alguma requisição pendente excedeu
     * o tempo limite de 5 segundos. Se excedeu, realiza a retransmissão da última mensagem
     * baseada no estado atual.
     * </p>
     */
    @Override
    public void run() {
        String msg;
        while (true){
            try {
                Thread.sleep(1000); // Thread esperando
                if(currentState != State.Idle){ // Se não está ocioso
                    long now = System.currentTimeMillis();
                    if(now - timeRequest > 5000){ // Se o tempo foi excedido desde a última requisição (5s)
                        timeRequest = System.currentTimeMillis();
                        //Novas mensagens serão mandadas, de acordo com o estado
                        if(currentState == State.DistanceTableRequest){
                            msg = "RIPRQT "+ pendingNodeA;
                            usi.UPDataReq(pendingNodeA, msg);
                        }else if(currentState == State.LinkCostRequest){
                            msg = "RIPGET "+ pendingNodeA + " "+pendingNodeB;
                            usi.UPDataReq(pendingNodeA, msg);
                        }else if(currentState == State.LinkCostSetRequest_1){
                            msg = "RIPSET "+ pendingNodeA + " "+pendingNodeB + " "+pendingCost;
                            usi.UPDataReq(pendingNodeA, msg);
                        }else{
                            msg = "RIPSET "+ pendingNodeB + " "+pendingNodeA + " "+pendingCost;
                            usi.UPDataReq(pendingNodeB, msg);
                        }

                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }


}
