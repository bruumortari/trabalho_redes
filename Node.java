import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.Math.min;

public class Node {
    private final short idNode;
    private int[] distanceVector;
    private final HashMap<String, Integer> linksCost;
    private final int numberNodes;
    private TreeMap<Short, Integer> neighborsCost =new TreeMap<>();
    private HashMap<Short, int[]> neighborsDistanceVectors;

    // Classe Node responsável por cálculo e armazenamento dos Vetores de Distância
    public Node(HashMap<String, Integer> linksCost, short idNode, int numberNodes) {
        this.idNode = idNode;
        this.numberNodes = numberNodes;
        this.linksCost = linksCost;
        this.neighborsDistanceVectors = new HashMap<>();
        this.distanceVector = new int[this.numberNodes];
        Arrays.fill(this.distanceVector, -1);
    }

    public void createDistanceVector() {
        short mainNode = this.idNode;
        this.distanceVector[mainNode-1] = 0;

        for (String key : linksCost.keySet()) {
            String[] parts = key.split(":");
            short node1 = Short.parseShort(parts[0]);
            short node2 = Short.parseShort(parts[1]);
            int cost = linksCost.get(key);

            if (node1 == mainNode) {
                this.distanceVector[node2-1] = cost;
                neighborsCost.put(node2, cost);
            } else if (node2 == mainNode) {
                this.distanceVector[node1-1] = cost;
                neighborsCost.put(node1, cost);
            }
        }
    }
    //Atualizar o custo do enlace
    public synchronized void updateNeighborCost(short neighborId, int cost){
        int[] vectorDistance;
        if(cost == -1){
            neighborsCost.remove(neighborId);
            vectorDistance = new int[numberNodes];
            Arrays.fill(vectorDistance, -1);
        }else{
            neighborsCost.put(neighborId, cost);
            vectorDistance = neighborsDistanceVectors.get(neighborId);
        }

        updateDistanceVector(neighborId, vectorDistance);
    }
    //Retorna os ids dos Nodes vizinhos
    public synchronized Set<Short> getNeighborIds(){
        return neighborsCost.keySet();
    }
    public synchronized int getCost(short nodeId){
        return this.neighborsCost.get(nodeId);
    }
    // Armazena vetor de distância de um vizinho
    public void addNeighborDistanceVector(short neighborId, int[] neighborTable) {
        this.neighborsDistanceVectors.put(neighborId, Arrays.copyOf(neighborTable, neighborTable.length));
    }

    // Retorna os vetores de distância dos vizinhos
    public synchronized HashMap<Short, int[]> getNeighborsDistanceVectors() {
        return this.neighborsDistanceVectors;
    }
    //Retorna o vetor de distância
    public synchronized int[] getDistanceVector() {
        return this.distanceVector;
    }
    //Atualiza o vetor de distância após receber o vetor de distância de um vizinho
    public synchronized boolean updateDistanceVector(short idNeighbor, int[] newNeighborDistanceVector){
        boolean changed = false; //Verifica se o vetor de distância mudou
        short cost;
        //Adiciona o novo vetor de distancia do vizinho
        this.neighborsDistanceVectors.put(idNeighbor, Arrays.copyOf(newNeighborDistanceVector,
                newNeighborDistanceVector.length));
        for(int y = 0; y<numberNodes; y++){
            if (y+1 == this.idNode) { // Desconsidera o custo para esse nó
                continue;
            }else{
                cost = -1; // Inicializa com custo infinito
                for(short x : neighborsDistanceVectors.keySet()){ //Percorre todos os vetores de distância
                    int[] neighborDistanceVector = neighborsDistanceVectors.get(x);
                    if(neighborDistanceVector[y]>-1){
                        //Se dá para chegar ao nó Y pelo X, então
                        // Calcular o novo custo ao nó Y pelo nó X
                        // = distancia do X para Y + distancia até o vizinho X
                        short newCost = (short) (neighborDistanceVector[y] + neighborsCost.get(x));

                        //Se o custo atual é infinito, então
                        if(cost == -1){
                            // Novo custo é o calculado
                            cost = newCost;
                        }else{ //Senão
                            // Custo é o mínimo entre o que já tenho e o que foi calculado agora
                            cost = (short) min(cost, newCost);
                        }
                    }
                }
            }
            //Se mudou o custo já conhecido ao Y
            if(distanceVector[y] != cost){
                //Atualiza o vetor e seta a variável
                changed = true;
                distanceVector[y] = cost;
            }

        }
        return changed;
    }
    //Retorna a tabela de distância em string
    public synchronized String getDistanceTable(){
        StringBuilder tableBuilder = new StringBuilder();

        //Primeiro vetor: o vetor de distância do próprio nó
        for(int i =0; i<distanceVector.length;i++){
            tableBuilder.append(distanceVector[i]).append(":");
        }
        tableBuilder.append(" ");
        // Nós vizinhos ordenados
        for(short i : neighborsCost.keySet()){
            for(int j=0;j<numberNodes;j++){
                tableBuilder.append(neighborsDistanceVectors.get(i)[j]).append(":");
            }
            tableBuilder.append(" ");
        }
        return tableBuilder.toString();
    }
    public short getIdNode() {
        return this.idNode;
    }

}