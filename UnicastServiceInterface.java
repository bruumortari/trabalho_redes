public interface UnicastServiceInterface {
    // Notificar a chegada de uma mensagem
    // Entidade origem, mensagem
    void UPDataInd(short sh, String str);
}