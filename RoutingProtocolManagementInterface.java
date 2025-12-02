/**
 * Interface de gerenciamento do protocolo de roteamento.
 * <p>
 * Define os métodos que a camada de aplicação de gerência (GUI) pode invocar
 * na camada de protocolo (Manager) para realizar consultas e modificações na rede.
 * </p>
 */
public interface RoutingProtocolManagementInterface{
    /**
     * Solicita a tabela de vetores de distância de um nó específico.
     *
     * @param id O ID do nó alvo.
     * @return {@code true} se a solicitação foi aceita e enviada, {@code false} caso contrário.
     */
    boolean getDistanceTable(short id);
    /**
     * Solicita o custo atual do enlace entre dois nós.
     *
     * @param node1 ID do primeiro nó.
     * @param node2 ID do segundo nó.
     * @return {@code true} se a solicitação foi aceita e enviada, {@code false} caso contrário.
     */
    boolean getLinkCost(short node1, short node2);
    /**
     * Altera o custo do enlace entre dois nós.
     *
     * @param node1 ID do primeiro nó.
     * @param node2 ID do segundo nó.
     * @param cost O novo custo a ser aplicado (use -1 para simular falha/infinito).
     * @return {@code true} se o comando foi aceito e iniciado, {@code false} caso contrário.
     */
    boolean setLinkCost(short node1, short node2, int cost);
}
