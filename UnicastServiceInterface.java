/**
 * Interface do Serviço Unicast (Camada de Transporte Simulada).
 * <p>
 * Define as operações disponíveis para envio de mensagens ponto a ponto na rede.
 * </p>
 */
public interface UnicastServiceInterface {
    /**
     * Solicita o envio de uma mensagem para um usuário do serviço de unicast
     *
     * @param targetId O ID do nó de destino.
     * @param str A mensagem a ser enviada.
     * @return {@code true} se o envio foi iniciado com sucesso, {@code false} se houve erro.
     */
    boolean UPDataReq(short targetId, String str);
}