import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class NodeWindow implements UnicastServiceUserInterface, Runnable{
    private UnicastServiceInterface usi;
    private short nodeID;
    private JTextField messageField;
    private JComboBox<Short> choiceNode;
    private JPanel window;
    private Short[] nodesList;
    private DefaultListModel<String> receivedMessagesList;
    
    // Construtor
    public NodeWindow(short nodeID, Short[] nodesList){
        this.nodeID = nodeID;
        this.nodesList = nodesList;
        receivedMessagesList = new DefaultListModel<>();
    }
    public void SetUSI( UnicastServiceInterface usi){
        this.usi = usi;
    }

    // Cria a janela para o nó
    public void CreateWindow(){
        JFrame frame = new JFrame("Node "+nodeID);
        window = new JPanel();
        // Janela com 2 linhas e 1 coluna
        window.setLayout(new GridLayout(2,1));

        // Parte de enviar mensagem
        JPanel sendMessage = new JPanel();
        sendMessage.setLayout(new GridLayout(2,2));
        JTextArea sendText = new JTextArea("Enviar Mensagem para o Nó: ");
        sendText.setEditable(false);
        sendMessage.add(sendText);
        choiceNode = new JComboBox<>(nodesList);
        sendMessage.add(choiceNode);
        messageField = new JTextField();
        sendMessage.add(messageField);

        JButton submittMessage = new JButton("Enviar");
        // Action listener para quando o usuário clicar em enviar
        submittMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                messageField.setText("");
                Object targetObj = choiceNode.getSelectedItem();
                Short targetNode = (short) targetObj;
                // Envia a mensagem pára o nó receptor
                usi.UPDataReq(targetNode, message);
            }
            
        });
        sendMessage.add(submittMessage);

        // Parte de recebimento de mensagens
        JPanel receiveMessages = new JPanel();
        receiveMessages.setLayout(new GridLayout(2,0));
        JTextArea receiveText = new JTextArea("Mensagens Recebidas:");
        receiveText.setEditable(false);
        receiveMessages.add(receiveText);

        // Local onde tem as mensagens recebidas por aquele nó
        JList<String> messageList = new JList<>(receivedMessagesList);
        receiveMessages.add(messageList);

        window.add(sendMessage);
        window.add(receiveMessages);

        frame.add(window);
        frame.setMinimumSize(new Dimension(400,500));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // Sai quando fecha (basta fechar uma janela)
    }

    @Override
    public void UPDataInd(short targetId, String message) {
        if(targetId == -1) {
            JOptionPane.showMessageDialog(null, message);
        }
        else {
            // Adiciona na lista de mensagens recebidas
            receivedMessagesList.addElement("De: "+targetId+ " Mensagem: "+message);
            // Atualiza a tela
            window.repaint();
        }
    }

    @Override
    public void run() {
        CreateWindow();
    }
}
