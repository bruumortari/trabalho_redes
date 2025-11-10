import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class RoutingInformationNode extends AbstractRIP implements WindowNodeUserInterface, UnicastServiceUserInterface, Runnable{
    private UnicastServiceInterface usi;
    private final Node nd;
    private final short idNode;
    private WindowNodeInterface nodeWindow;
    public void SetGUI(NodeWindow nodeWindow){
        this.nodeWindow = nodeWindow;
    }
    public RoutingInformationNode(short idNode, int numberNodes,
                                  HashMap<String, Integer> linksCost){
        super(idNode);
        this.idNode = idNode;
        nd = new Node(linksCost, idNode, numberNodes);

    }
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
    public void SetUSI(UnicastServiceInterface usi){
        this.usi = usi;
    }
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
