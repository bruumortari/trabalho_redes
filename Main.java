import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Main{
    private static HashMap<Short,String> idEnd;
    private static HashMap<String,Short> ipID;
    private static List<Short> nodesList = new ArrayList<>();

    private static void SetConfig(String fileName){
        //funcao para ler o arquivo de configuracao
        try{
            idEnd = new HashMap<>();
            ipID = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) !=null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    short id = Short.parseShort(parts[0]);
                    String addressAndPort = parts[1] + ":" + parts[2];
                    idEnd.put(id, addressAndPort);
                    ipID.put("/"+parts[1]+":"+parts[2], id);
                    nodesList.add(id);
                } else {
                    System.out.println("Formato inválido na linha: " + line);
                }
            }
            reader.close();
        }catch(IOException e){
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            System.exit(-1);
        }
    }
    public static void main(String[] args){
        SetConfig("config.txt");
        if(idEnd == null){ // se map ta vazio, algo deu errado
            System.err.println("Erro: arquivo de configuracao inválido!");
            System.exit(-1);
        }
        //grupo de threads
        ThreadGroup uniGroup = new ThreadGroup("Unicasts");
        ThreadGroup rpiGroup = new ThreadGroup("Routings");

        for(short id : idEnd.keySet()){ //percorre todos os nos
            String[] end = idEnd.get(id).split(":"); //splita o endereco guardado

            //instancia o up, informando os maps, id e porta
            UnicastProtocol up = new UnicastProtocol(id, Integer.parseInt(end[1]), idEnd, ipID);

            //instancia a janela (cliente) para cada no
            NodeWindow nw = new NodeWindow(id, nodesList.toArray(new Short[0]));

            //ligacao das classes
            up.SetUser(nw);
            nw.SetUSI(up);

            //colocando nos grupos
            Thread upThread = new Thread(uniGroup, up);
            Thread rpiThread = new Thread(rpiGroup, nw);

            //inicializando
            upThread.start();
            rpiThread.start();
        }

    }
    
}
