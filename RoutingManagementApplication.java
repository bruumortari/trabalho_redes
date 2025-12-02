import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;
/**
 * Camada de Aplicação e Interface Gráfica de Gerenciamento (GUI do Gerente).
 * Esta classe fornece um painel de controle visual para o nó Gerente (Node 0).
 * Diferente da {@link NodeWindow}, esta aplicação permite:
 * <ul>
 * <li>Solicitar a Tabela de Distância de qualquer nó da rede.</li>
 * <li>Consultar o custo atual de um enlace entre dois nós.</li>
 * <li><b>Alterar</b> o custo de um enlace (incluindo "excluir" links com custo -1).</li>
 * <li>Visualizar um log de eventos e respostas da rede.</li>
 * </ul>
 */

public class RoutingManagementApplication implements Runnable, RoutingProtocolManagementServiceUserInterface{
    private RoutingProtocolManagementInterface rpmi;
    private short idNode;
    private Vector<Short> nodesList;
    private JComboBox<Short> choiceNodeDistanceTable;
    private JComboBox<Short> choiceNodeAEnlaceCost;
    private JComboBox<Short> choiceNodeBEnlaceCost;
    private JComboBox<Short> choiceNodeAChangeCost;
    private JComboBox<Short> choiceNodeBChangeCost;
    private JComboBox<Integer> choiceCost;
    private Vector<Integer> choiceNumbers;
    private JTextArea logArea;

    /**
     * Construtor da Aplicação de Gerência.
     * <p>
     * Inicializa os dados para os componentes de seleção (ComboBoxes).
     * O nó gerente (0) é removido da lista de alvos, e os custos possíveis
     * são definidos de -1 (infinito) até 15.
     * </p>
     *
     * @param id O ID do gerente.
     * @param nodesList O conjunto de nós presentes na topologia.
     */
    public RoutingManagementApplication(short id, Set<Short> nodesList){
        this.idNode = id;
        nodesList.remove((short) 0);
        this.nodesList = new Vector<>(nodesList);
        choiceNumbers = new Vector<>();
        choiceNumbers.add(-1);
        for (int i = 1; i <=15; i++) {
            choiceNumbers.add(i);
        }

    }
    /**
     * Define a ligação com a camada do protocolo de routing
     *
     * @param rpmi A instância do RoutingInformationManager.
     */
    public void SetRPMI(RoutingProtocolManagementInterface rpmi){
        this.rpmi = rpmi;
    }
    /**
     * Atualiza a área de log da interface de forma Thread-Safe.
     * <p>
     * Como as mensagens chegam através de threads diferentes,
     * é necessário usar {@link SwingUtilities#invokeLater} para garantir que
     * a atualização do componente Swing ocorra na <i>Event Dispatch Thread</i> (EDT).
     * </p>
     *
     * @param msg A mensagem a ser adicionada ao log.
     */
    private void updateLog(String msg){
        // Função para atualizar o log do gerente
        // usa função do swing para evitar condições de corrida
        // Swing cria uma thread específica chamada Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logArea.append(msg);
                logArea.setCaretPosition(logArea.getDocument().getLength()); // rola pro fim automaticamente
            }
        });
    }
    /**
     * Primitiva utilizada para notificar à aplicação de gerência
     * de roteamento uma dada tabela de distância previamente requisitada.
     * <p>
     * Formata a matriz de inteiros recebida em uma representação textual legível
     * e a exibe no log.
     * </p>
     *
     * @param id O ID do nó que enviou a tabela.
     * @param table A matriz contendo os vetores de distância.
     */

    @Override
    public void distanceTableIndication(short id, int[][] table) {
        StringBuilder tableString = new StringBuilder();
        tableString.append("\nRECEBIDO: Tabela de distância do nó").append(id);
        for (int[] ints : table) {
            tableString.append("\n");
            tableString.append(Arrays.toString(ints)).append(" ");

        }
        tableString.append("\n");
        updateLog(tableString.toString());

    }
    /**
     * Primitiva utilizada para notificar/confirmar à aplicação de gerência de
     * roteamento os custos de um enlace estabelecido entre os nós previamente
     * requisitado/definido
     * <p>
     * Notifica no log o custo atual entre dois nós (resposta de um RIPGET ou RIPSET).
     * </p>
     *
     * @param nodeA O primeiro nó do enlace.
     * @param nodeB O segundo nó do enlace.
     * @param cost O custo do enlace.
     */
    @Override
    //
    public void linkCostIndication(short nodeA, short nodeB, int cost) {
        updateLog("\nRECEBIDO: Custo do nó "+ nodeA + " com nó "+ nodeB + " é "+ cost+"\n");
    }
    /**
     * Constrói e exibe a janela principal da aplicação.
     * <p>
     * Utiliza {@link GridBagLayout} para organizar os componentes em quatro seções:
     * <ol>
     * <li>Requisição de Tabela de Distância.</li>
     * <li>Requisição de Custo de Enlace.</li>
     * <li>Alteração de Custo de Enlace.</li>
     * <li>Área de Log.</li>
     * </ol>
     * </p>
     */
    private void CreateWindow() {
        JFrame frame = new JFrame("Aplicação de Gerência: " + idNode);
        frame.setMinimumSize(new Dimension(500, 500));

        JPanel window = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Espaçamento entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;

        // Parte da Tabela de Distância
        JLabel distanceTableTitle = new JLabel("Tabela de Distância");
        distanceTableTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = row++;
        window.add(distanceTableTitle, gbc);

        JPanel distanceTable = new JPanel(new GridBagLayout());
        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.insets = new Insets(5, 5, 5, 5);

        dgbc.gridx = 0;
        distanceTable.add(new JLabel("Nó:"), dgbc);

        dgbc.gridx = 1;
        choiceNodeDistanceTable = new JComboBox<>(nodesList);
        distanceTable.add(choiceNodeDistanceTable, dgbc);

        dgbc.gridx = 2;
        JButton requestTableButton = getRequestTableButton();
        distanceTable.add(requestTableButton, dgbc);

        gbc.gridy = row++;
        window.add(distanceTable, gbc);

        //Parte de consulta do Custo de Enlace
        JLabel enlaceCostTitle = new JLabel("Custo de Enlace");
        enlaceCostTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = row++;
        window.add(enlaceCostTitle, gbc);

        JPanel requestEnlaceCost = new JPanel(new GridBagLayout());
        GridBagConstraints egbc = new GridBagConstraints();
        egbc.insets = new Insets(5, 5, 5, 5);

        egbc.gridx = 0;
        requestEnlaceCost.add(new JLabel("Nó A:"), egbc);

        egbc.gridx = 1;
        choiceNodeAEnlaceCost = new JComboBox<>(nodesList);
        requestEnlaceCost.add(choiceNodeAEnlaceCost, egbc);

        egbc.gridx = 2;
        requestEnlaceCost.add(new JLabel("Nó B:"), egbc);

        egbc.gridx = 3;
        choiceNodeBEnlaceCost = new JComboBox<>(nodesList);
        requestEnlaceCost.add(choiceNodeBEnlaceCost, egbc);

        egbc.gridx = 4;
        JButton requestEnlaceCostButton = getRequestEnlaceCostButton();
        requestEnlaceCost.add(requestEnlaceCostButton, egbc);

        gbc.gridy = row++;
        window.add(requestEnlaceCost, gbc);

        // Parte de definição do Custo de Enlace
        JPanel setEnlaceCost = new JPanel(new GridBagLayout());
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.insets = new Insets(5, 5, 5, 5);

        sgbc.gridx = 0;
        setEnlaceCost.add(new JLabel("Nó A:"), sgbc);
        sgbc.gridx = 1;
        choiceNodeAChangeCost = new JComboBox<>(nodesList);
        setEnlaceCost.add(choiceNodeAChangeCost, sgbc);
        sgbc.gridx = 2;
        setEnlaceCost.add(new JLabel("Nó B:"), sgbc);
        sgbc.gridx = 3;
        choiceNodeBChangeCost = new JComboBox<>(nodesList);
        setEnlaceCost.add(choiceNodeBChangeCost, sgbc);
        sgbc.gridx = 4;
        setEnlaceCost.add(new JLabel("Custo:"), sgbc);
        sgbc.gridx = 5;
        choiceCost = new JComboBox<>(choiceNumbers);
        setEnlaceCost.add(choiceCost, sgbc);
        sgbc.gridx = 6;
        JButton newCostButton = getNewCostButton();
        setEnlaceCost.add(newCostButton, sgbc);

        gbc.gridy = row++;
        window.add(setEnlaceCost, gbc);

        //Parte do Log de Eventos
        JLabel logLabel = new JLabel("Log de Eventos:");
        logLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = row++;
        window.add(logLabel, gbc);

        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane logScroll = new JScrollPane(logArea);
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        window.add(logScroll, gbc);

        frame.add(window);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    /**
     * Cria e configura o botão de Requisição de Tabela.
     * @return O botão configurado.
     */
    private JButton getRequestTableButton(){
        JButton requestTableButton = new JButton("Requisitar Tabela");
        requestTableButton.addActionListener(e -> {
            Object nodeObj = choiceNodeDistanceTable.getSelectedItem();
            updateLog("\nENVIADO: RIPRQT " + nodeObj+"\n");
            rpmi.getDistanceTable((Short) nodeObj);
        });

        return requestTableButton;
    }
    /**
     * Cria e configura o botão de Requisição de Custo.
     * <p>
     * Se o gerente estiver ocupado ou o enlace não existir localmente,
     * exibe uma mensagem de erro.
     * </p>
     * @return O botão configurado.
     */
    private  JButton getRequestEnlaceCostButton(){
        JButton requestEnlaceCostButton = new JButton("Requisitar Custo");
        requestEnlaceCostButton.addActionListener(e -> {
            Object nodeAObj = choiceNodeAEnlaceCost.getSelectedItem();
            Object nodeBObj = choiceNodeBEnlaceCost.getSelectedItem();

            if(rpmi.getLinkCost((Short) nodeAObj,(Short) nodeBObj)){
                updateLog("\nENVIADO: RIPGET " + nodeAObj + " "+ nodeBObj+"\n");
            }else{
                JOptionPane.showMessageDialog(null,
                        "Enlace Inexistente ou Gerente Aguardando Requisição!",
                        "Erro",JOptionPane.ERROR_MESSAGE);
            }

        });
        return requestEnlaceCostButton;
    }
    /**
     * Cria e configura o botão de Alteração de Custo.
     * <p>
     * Envia o comando para alterar o custo entre dois nós. Suporta custo -1 (falha de link).
     * Exibe erro se o gerente estiver ocupado ou o enlace não existir.
     * </p>
     * @return O botão configurado.
     */
    private JButton getNewCostButton(){
        JButton newCostButton = new JButton("Definir Custo");
        newCostButton.addActionListener(e -> {
            Object nodeAObj = choiceNodeAChangeCost.getSelectedItem();
            Object nodeBObj = choiceNodeBChangeCost.getSelectedItem();
            Object cost = choiceCost.getSelectedItem();
            if(rpmi.setLinkCost((Short) nodeAObj, (Short) nodeBObj, (int) cost)){
                updateLog("\nENVIADO: RIPSET " + nodeAObj + " "+ nodeBObj+" "+cost+"\n");
            }else{
                JOptionPane.showMessageDialog(null,
                        "Enlace Inexistente ou Gerente Aguardando Requisição!",
                        "Erro",JOptionPane.ERROR_MESSAGE);
            }
        });
        return newCostButton;
    }
    /**
     * Função chamada ao inicializar a Thread.
     */
    @Override
    public void run() {

        CreateWindow();
    }
}