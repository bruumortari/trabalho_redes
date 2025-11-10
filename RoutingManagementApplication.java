import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;

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
    //Ligação com a camada do protocolo de routing
    public void SetRPMI(RoutingProtocolManagementInterface rpmi){
        this.rpmi = rpmi;
    }
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

    @Override
    //Primitiva utilizada para notificar à aplicação de gerência
    //de roteamento uma dada tabela de distância previamente requisitada.
    public void distanceTableIndication(short id, int[][] table) {
        StringBuilder tableString = new StringBuilder();
        tableString.append("\nRECEBIDO: Tabela de distância do nó"+id);
        for(int i =0; i<table.length;i++){
            tableString.append("\n");
            tableString.append(Arrays.toString(table[i])).append(" ");

        }
        tableString.append("\n");
        updateLog(tableString.toString());

    }

    @Override
    //Primitiva utilizada para notificar/confirmar à aplicação de gerência
    //de roteamento os custos de um enlace estabelecido entre os nós
    //previamente requisitado/definido
    public void linkCostIndication(short nodeA, short nodeB, int cost) {
        updateLog("\nRECEBIDO: Custo do nó "+ nodeA + " com nó "+ nodeB + " é "+ cost+"\n");
    }
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

    private JButton getRequestTableButton(){
        JButton requestTableButton = new JButton("Requisitar Tabela");
        requestTableButton.addActionListener(e -> {
            Object nodeObj = choiceNodeDistanceTable.getSelectedItem();
            updateLog("\nENVIADO: RIPRQT " + nodeObj+"\n");
            rpmi.getDistanceTable((Short) nodeObj);
        });

        return requestTableButton;
    }

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

    @Override
    public void run() {

        CreateWindow();
    }
}