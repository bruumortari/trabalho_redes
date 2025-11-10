import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Set;
import java.util.Vector;

public class NodeWindow implements Runnable, WindowNodeInterface{
    private final short idNode;
    private Vector<Short> nodesList;
    private DefaultListModel<String> distancesVectors;
    private DefaultTableModel defaultTableModel;
    private WindowNodeUserInterface wnui;
    public NodeWindow(short id, Set<Short> nodesList){
        this.idNode = id;
        distancesVectors = new DefaultListModel<>();
        nodesList.remove((short) 0);
        this.nodesList = new Vector<>(nodesList);
        defaultTableModel = new DefaultTableModel(this.nodesList, 0);
    }

    public void SetUser(WindowNodeUserInterface wnui) {
        this.wnui = wnui;
    }

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
    }
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

    @Override
    public void run() {
        CreateWindow();
    }

}
