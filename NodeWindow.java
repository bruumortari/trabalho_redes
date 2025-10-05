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
    
    public NodeWindow(short nodeID, Short[] nodesList){
        this.nodeID = nodeID;
        this.nodesList = nodesList;
        receivedMessagesList = new DefaultListModel<>();
    }
    public void SetUSI( UnicastServiceInterface usi){
        this.usi = usi;
    }

    public void CreateWindow(){
        JFrame frame = new JFrame("Node "+nodeID);
        window = new JPanel();
        window.setLayout(new GridLayout(2,1));

        //parte de enviar mensagem
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
        //action listener para quando o usuario clicar em enviar
        submittMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                messageField.setText("");
                Object targetObj = choiceNode.getSelectedItem();
                Short targetNode = (short) targetObj;
                usi.UPDataReq(targetNode, message);
            }
            
        });
        sendMessage.add(submittMessage);

        //parte de recebimento de mensagens
        JPanel receiveMessages = new JPanel();
        receiveMessages.setLayout(new GridLayout(2,0));
        JTextArea receiveText = new JTextArea("Mensagens Recebidas:");
        receiveText.setEditable(false);
        receiveMessages.add(receiveText);

        //local onde tem as mensagens recebidas por aquele no
        JList<String> messageList = new JList<>(receivedMessagesList);
        receiveMessages.add(messageList);

        window.add(sendMessage);
        window.add(receiveMessages);

        frame.add(window);
        frame.setMinimumSize(new Dimension(400,500));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //sai quando fecha (basta fechar uma janela)
    }

    @Override
    public void UPDataInd(short targetId, String message) {
        //adiciona na lista de mensagens recebidas
        receivedMessagesList.addElement("De: "+targetId+ " Mensagem: "+message);
        //atualiza a tela
        window.repaint();
    }

    @Override
    public void run() {
        CreateWindow();
    }
}
