import java.util.HashMap;
import java.util.TreeMap;
/**
 * Implementação do protocolo RIP para um nó comum da rede. Esta classe gerencia a lógica de
 * roteamento de um nó específico. Ela é responsável por:
 * <ul>
 * <li>Manter e atualizar o vetor de distâncias local (via objeto {@link Node}).</li>
 * <li>Propagar periodicamente seu vetor de distâncias para os vizinhos.</li>
 * <li>Responder a comandos de gerenciamento (alterar custos, consultar tabelas).</li>
 * <li>Atualizar a Interface Gráfica (GUI) com o estado atual da rede.</li>
 * </ul>
 */
public class RoutingInformationNode extends AbstractRIP implements UnicastServiceUserInterface, Runnable{
    private UnicastServiceInterface usi;
    private final Node nd;
    private final short idNode;
    private WindowNodeInterface nodeWindow;
    /**
     * Define a interface gráfica (GUI) que será controlada por este nó.
     *
     * @param nodeWindow A instância da janela do nó.
    */
    public void SetGUI(NodeWindow nodeWindow){
        this.nodeWindow = nodeWindow;
    }

    /**
     * Construtor do Nó RIP.
     * <p>
     * Inicializa a lógica interna do nó (classe {@link Node}) com os custos iniciais dos enlaces.
     * </p>
     *
     * @param idNode O ID único deste nó.
     * @param numberNodes O número total de nós na rede (para dimensionar vetores).
     * @param linksCost Mapa contendo os custos iniciais dos vizinhos diretos.
     */
    public RoutingInformationNode(short idNode, int numberNodes,
                                  HashMap<String, Integer> linksCost){
        super(idNode);
        this.idNode = idNode;
        nd = new Node(linksCost, idNode, numberNodes);

    }
    /**
     * Função chamada ao inicializar a Thread. O comportamento é:
     * <ol>
     * <li>Cria o vetor de distância inicial.</li>
     * <li>Propaga imediatamente para os vizinhos.</li>
     * <li>Entra em loop infinito: Espera 10 segundos e propaga novamente.</li>
     * </ol>
     */
    @Override
    public void run() {
        nd.createDistanceVector(); // Criar o vetor de distância
        propagateDistanceVector(); //Propaga
        while ((true)){
            try {
                Thread.sleep(10000);
                // A cada 10s, propaga o vetor de distância
                propagateDistanceVector();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
    /**
     * Define a interface de serviço Unicast.
     *
     * @param usi A instância do protocolo Unicast.
     */
    public void SetUSI(UnicastServiceInterface usi){
        this.usi = usi;
    }
    /**
     * Envia o vetor de distâncias atual para todos os vizinhos diretos.
     * O método realiza as seguintes ações:
     * <ol>
     * <li>Obtém o vetor atualizado da lógica interna (`nd`).</li>
     * <li>Atualiza a visualização na GUI.</li>
     * <li>Formata a mensagem no protocolo: {@code "RIPIND <ID> <Vetor>"}.</li>
     * <li>Envia via Unicast para cada vizinho (custo diferente de -1).</li>
     * </ol>
     * Método sincronizado para evitar condições de corrida durante atualizações simultâneas.
     */

    public synchronized void propagateDistanceVector(){
        int[] distanceVector = nd.getDistanceVector(); //Solicita pra camada Node o vetor
        nodeWindow.AddNewDistanceVector(distanceVector); //Informa pra GUI
        StringBuilder distanceVectorString = new StringBuilder();
        //Constroi a string do vetor
        for(int i : distanceVector){
            distanceVectorString.append(i).append(":");
        }
        //Monta mensagem
        String msg = "RIPIND" + " " + idNode + " " + distanceVectorString;
        TreeMap<Short, Integer> neighbors = nd.getNeighbors(); //Solicita os Ids dos vizinhos
        for(short neighbor: neighbors.keySet()){
            if(neighbors.get(neighbor) == -1) continue;
            // Propaga através do Unicast o vetor para os vizinhos
            usi.UPDataReq(neighbor, msg);
        }
    }
    /**
     * Processa mensagens recebidas.
     * Trata os seguintes tipos de PDUs (Protocol Data Units):
     * <ul>
     * <li><b>RIPGET:</b> Solicitação do custo de um link específico (pelo Gerente).</li>
     * <li><b>RIPSET:</b> Ordem para alterar o custo de um link (pelo Gerente). Dispara recálculo e propagação.</li>
     * <li><b>RIPIND:</b> Vetor de distância recebido de um vizinho. Se houver melhoria nas rotas, propaga a atualização.</li>
     * <li><b>RIPRQT:</b> Solicitação da tabela de roteamento completa (pelo Gerente).</li>
     * </ul>
     *
     * @param originId O ID do nó que enviou a mensagem.
     * @param message A string contendo o comando e os dados.
     */
    @Override
    public void UPDataInd(short originId, String message) {
        String[] parts = message.split(" ");
        short node, nodeB;
        int cost;
        String msg;
        switch (parts[0]){

            case "RIPGET": //Recebeu do manager solicitação do custo
                //<RIPGET><espaço><RIPNode_A><espaço>< RIPNode_B>
                nodeB = Short.parseShort(parts[2]);
                cost = nd.getCost(nodeB);
                msg = "RIPNTF "+ idNode + " "+nodeB + " " + cost;
                usi.UPDataReq((short) 0,msg);
                break;
            case "RIPSET": //Recebeu do manager solicitação de mudança de custo
                //<RIPSET><espaço><RIPNode_A><espaço>< RIPNode_B><espaço><custo>
                nodeB = Short.parseShort(parts[2]);
                cost = Integer.parseInt(parts[3]);
                nd.updateNeighborCost(nodeB, cost);
                propagateDistanceVector();
                nodeWindow.AddNewDistanceVector(nd.getDistanceVector());
                msg = "RIPNTF "+ idNode + " "+nodeB + " " + cost;
                usi.UPDataReq((short) 0,msg);
                break;
            case "RIPIND": //Recebeu de outro node o vetor de distância
                //<RIPIND><espaço><RIPNode><espaço><vetor_distancia>
                node = Short.parseShort(parts[1]);
                String[] vector = parts[2].split(":");
                int[] values = new int[vector.length];
                for(int i = 0; i<vector.length; i++){
                    values[i] = Short.parseShort(vector[i]);
                }
                if(nd.updateDistanceVector(node, values)){
                    propagateDistanceVector();
                }
                break;
            case "RIPRQT": //Recebeu do manager solicitação paa a tabela de distância
                //RIPRQT
                String table = nd.getDistanceTable();
                msg = "RIPRSP " + idNode + " "+table;
                usi.UPDataReq((short) 0, msg);
                break;
        }
    }
}
