public interface UnicastServiceUserInterface{
    // Notificar a chegada de uma mensagem
    // Entidade origem, mensagem
    void UPDataInd(short originId, String message);
}