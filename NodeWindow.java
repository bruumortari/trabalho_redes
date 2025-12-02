import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Set;
import java.util.Vector;

/**
 * Interface Gráfica do Usuário (GUI) para um nó da rede.
 * <p>
 * Esta classe exibe uma janela contendo o histórico dos vetores de distância calculados pelo nó.
 * Ela implementa {@link Runnable} para ser executada em sua própria thread (ou na thread de interface)
 * e {@link WindowNodeInterface} para receber atualizações da camada RIP.
 * </p>
 */
public class NodeWindow implements Runnable, WindowNodeInterface{
    private final short idNode;
    private Vector<Short> nodesList;
    private DefaultListModel<String> distancesVectors;
    private DefaultTableModel defaultTableModel;
    /**
     * Construtor da Janela do Nó.
     * <p>
     * Configura o modelo da tabela. O Nó 0 (Gerente) é removido da lista de colunas,
     * pois os nós não roteiam tráfego para o gerente, apenas trocam mensagens de controle.
     * </p>
     *
     * @param id O ID deste nó.
     * @param nodesList O conjunto de todos os nós da rede.
     */
    public NodeWindow(short id, Set<Short> nodesList){
        this.idNode = id;
        distancesVectors = new DefaultListModel<>();
        nodesList.remove((short) 0);
        this.nodesList = new Vector<>(nodesList);
        defaultTableModel = new DefaultTableModel(this.nodesList, 0);
    }
    /**
     * Cria e exibe os componentes visuais da janela.
     * <p>
     * Configura o {@link JFrame}, o painel, o título e a {@link JTable} dentro de um {@link JScrollPane}.
     * Define o tamanho mínimo e o comportamento de fechamento.
     * </p>
     */
    private void CreateWindow() {
        JFrame frame = new JFrame("Node "+idNode);
        JPanel window = new JPanel();
        window.setLayout(new GridLayout(2,1));
        JLabel title = new JLabel("Vetores de Distância Calculados:");
        window.add(title);
        JTable table = new JTable(defaultTableModel);
        JScrollPane scrollPane =new JScrollPane(table);
        window.add(scrollPane);
        frame.add(window);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(400,500));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                // Posicionamento das janelas
        int offset = 40; // Muda a posição para cada janela
        frame.setLocation(100 + (idNode * offset), 100 + (idNode * offset));
    }
    /**
     * Adiciona um novo vetor de distância à tabela exibida na janela.
     * <p>
     * Este método é chamado pela camada RIP sempre que o vetor é recalculado.
     * Utiliza {@link SwingUtilities#invokeLater(Runnable)} para garantir que a atualização
     * da GUI ocorra na <i>Event Dispatch Thread</i> (EDT), prevenindo problemas de concorrência do Swing.
     * </p>
     *
     * @param distanceVector O array de inteiros representando os custos para cada nó.
     */
    public void AddNewDistanceVector(int[] distanceVector){
        Vector<Integer> dVector = new Vector<>();
        for(int i:distanceVector){
            dVector.add(i);
        }
        StringBuilder values = new StringBuilder();
        for(int i:distanceVector){
            values.append(i).append(":");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                defaultTableModel.addRow(dVector);
            }
        });

    }
    /**
     * Função chamada ao inicializar a Thread da janela.
     * Inicia a construção da interface gráfica.
     */
    @Override
    public void run() {
        CreateWindow();
    }

}

