/**
 * Interface de Usuário do Serviço Unicast.
 * <p>
 * Deve ser implementada por qualquer classe que deseje receber mensagens da camada Unicast
 * (como o protocolo RIP). Define o método de callback para indicação de dados recebidos.
 * </p>
 */
public interface UnicastServiceUserInterface{
    /**
     * Notifica a chegada de uma mensagem (Indication).
     *
     * @param originId O ID do nó que enviou a mensagem.
     * @param message O conteúdo da mensagem recebida.
     */
    void UPDataInd(short originId, String message);
}