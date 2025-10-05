public interface UnicastServiceInterface {
    // Envio de uma mensagem para um usuário do serviço de unicast
    // Entidade destino, mensagem
    boolean UPDataReq(short sh, String str);
}