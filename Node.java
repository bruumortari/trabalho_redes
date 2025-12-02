import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.Math.min;

/**
 * Núcleo lógico do algoritmo de roteamento (Vetor de Distância).
 * Esta classe é responsável por armazenar e manipular a Tabela de Roteamento de um nó específico.
 * Ela não realiza comunicações de rede diretamente, mas fornece os métodos para:
 * <ul>
 * <li>Calcular custos mínimos de rotas.</li>
 * <li>Gerenciar custos de enlaces diretos.</li>
 * <li>Armazenar vetores de distância recebidos de vizinhos.</li>
 * </ul>
 */
public class Node {
    private final short idNode;
    private int[] distanceVector;
    private final HashMap<String, Integer> linksCost;
    private final int numberNodes;
    private TreeMap<Short, Integer> neighborsCost =new TreeMap<>();
    private HashMap<Short, int[]> neighborsDistanceVectors;

    /**
     * Construtor da classe lógica Node.
     *
     * @param linksCost Mapa contendo a topologia inicial (quem conecta com quem e o custo).
     * @param idNode O ID deste nó.
     * @param numberNodes A quantidade total de nós (define o tamanho dos vetores).
     */
    public Node(HashMap<String, Integer> linksCost, short idNode, int numberNodes) {
        this.idNode = idNode;
        this.numberNodes = numberNodes;
        this.linksCost = linksCost;
        this.neighborsDistanceVectors = new HashMap<>();
        this.distanceVector = new int[this.numberNodes];
        Arrays.fill(this.distanceVector, -1);
    }
    /**
     * Inicializa o vetor de distância baseando-se apenas nos vizinhos diretos.
     * <p>
     * Deve ser chamado na inicialização do sistema. O custo para si mesmo é definido como 0.
     * </p>
     */
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
    /**
     * Atualiza o custo de um enlace direto.
     * <p>
     * Este método é chamado quando há uma alteração na topologia (ex: comando RIPSET).
     * Após atualizar o custo local, ele dispara o recálculo do vetor de distância.
     * </p>
     *
     * @param neighborId O ID do vizinho cujo custo do link mudou.
     * @param cost O novo custo (ou -1 para link infinito).
     */
    public synchronized void updateNeighborCost(short neighborId, int cost){
        int[] vectorDistance;
        if(cost == -1){
            vectorDistance = new int[numberNodes];
            Arrays.fill(vectorDistance, -1);
        }else{
            vectorDistance = neighborsDistanceVectors.get(neighborId);
        }
        neighborsCost.put(neighborId, cost);
        updateDistanceVector(neighborId, vectorDistance);
    }
    /**
     * Retorna o mapa de vizinhos diretos e seus custos.
     * @return TreeMap onde Key = ID Vizinho e Value = Custo.
     */
    public synchronized TreeMap<Short, Integer>  getNeighbors(){
        return neighborsCost;
    }
    /**
     * Obtém o custo do enlace direto para um nó específico.
     * @param nodeId ID do nó desejado.
     * @return O custo do link.
     */
    public synchronized int getCost(short nodeId){
        return this.neighborsCost.get(nodeId);
    }

    /**
     * Retorna o vetor de distância calculado deste nó (Resultado do algoritmo).
     * @return Array de inteiros com os custos para cada nó da rede.
     */
    public synchronized int[] getDistanceVector() {
        return this.distanceVector;
    }
    /**
     * Executa o Algoritmo de Vetor de Distância.
     * <p>
     * Este método é chamado sempre que recebemos um vetor de um vizinho ou quando
     * o custo de um link muda. Ele itera sobre todos os destinos possíveis e recalcula o
     * caminho de menor custo.
     * </p>
     * Fórmula: {@code D(y) = min( c(x,v) + Dv(y) )} para cada vizinho v.
     *
     * @param idNeighbor O ID do vizinho que enviou a atualização.
     * @param newNeighborDistanceVector O vetor de distância recebido deste vizinho.
     * @return {@code true} se o vetor de distância local mudou (exige propagação), {@code false} caso contrário.
     */
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
    /**
     * Gera uma representação em String de toda a tabela de roteamento.
     * <p>
     * Formato: {@code "VetorLocal: : VetorVizinho1 : VetorVizinho2 : ..."}
     * </p>
     *
     * @return String contendo todos os vetores concatenados.
     */
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
    /**
     * Retorna o ID deste nó.
     * @return Short contendo o ID.
     */
    public short getIdNode() {
        return this.idNode;
    }

}