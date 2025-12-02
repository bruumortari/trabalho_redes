/**
 * Interface usada pela aplicação de gerência.
 * <p>
 * Define os métodos que a camada de protocolo (Manager) utiliza para notificar
 * a aplicação (GUI) sobre respostas recebidas da rede.
 * </p>
 */
public interface RoutingProtocolManagementServiceUserInterface {
    /**
     * Notifica o recebimento de uma tabela de distância solicitada.
     *
     * @param id O ID do nó que enviou a tabela.
     * @param table Matriz representando os vetores de distância do nó.
     */
    void distanceTableIndication(short id, int[][] table);
    /**
     * Notifica o recebimento de uma informação de custo de enlace.
     *
     * @param nodeA ID do primeiro nó do enlace.
     * @param nodeB ID do segundo nó do enlace.
     * @param cost O custo atual do enlace.
     */
    void linkCostIndication(short nodeA, short nodeB, int cost);
}
