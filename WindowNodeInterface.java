/**
 * Interface da Janela do Nó.
 * <p>
 * Define o contrato que a GUI de um nó deve implementar para permitir
 * que a camada RIP atualize a visualização dos dados.
 * </p>
 */
public interface WindowNodeInterface {
    /**
     * Adiciona/Atualiza um vetor de distância na visualização da janela.
     *
     * @param distanceVector Array contendo os custos calculados para cada destino.
     */
    void AddNewDistanceVector(int[] distanceVector);
}
