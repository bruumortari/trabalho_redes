public interface UnicastServiceUserInterface {
    // Envio de uma mensagem para um usuário do serviço de unicast
    // Entidade destino, mensagem
    boolean UPDataRequest(short targetId, String message);
}