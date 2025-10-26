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
        // Função para ler o arquivo de configuração
        // Preenche os hashmaps com as informações do arquivo (id endereçoIP porta)
        try{
            // Hashmap "id" : "endereço:porta"
            idEnd = new HashMap<>();
            // Hashmap "/endereço:porta" : "id"
            ipID = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            // Lê cada linha do arquivo
            while ((line = reader.readLine()) !=null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    short id = Short.parseShort(parts[0]);
                    // Apenas localmente
                    if(parts[1].equals("localhost")){
                        parts[1] = "127.0.0.1";
                    }
                    int port = Integer.parseInt(parts[2]);
                    // Checa se o número de porta é válido
                    if(port < 0 || port > 65535) {
                        System.out.println("Número de porta inválido: " + port);
                        continue;
                    }
                    // Junta o endereço ip e a porta
                    String addressAndPort = parts[1] + ":" + parts[2];
                    idEnd.put(id, addressAndPort);
                    ipID.put("/"+parts[1]+":"+parts[2], id);
                    // Adiciona o id na lista de nós
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
        // Chama função para ler o arquivo
        SetConfig("config.txt");
        // Se o hashmap estiver vazio, algo deu errado na leitura do arquivo
        if(idEnd == null){ 
            System.err.println("Erro: arquivo de configuracao inválido!");
            System.exit(-1);
        }
        // Grupo de threads
        ThreadGroup uniGroup = new ThreadGroup("Unicasts");
        ThreadGroup nwGroup = new ThreadGroup("NodeWindow");

        // Percorre todos os nós
        for(short id : idEnd.keySet()){ 
            String[] end = idEnd.get(id).split(":"); // Splita o endereço guardado (endereçoIP:porta)

            if(!end[0].equals("127.0.0.1") ){
                continue; // Só cria instâncias que forem locais
            }
            // Instancia o unicast protocol, informando os hashmaps, id e porta
            UnicastProtocol up = new UnicastProtocol(id, Integer.parseInt(end[1]), idEnd, ipID);

            // Instancia a janela (cliente) para cada nó (interface gráfica)
            NodeWindow nw = new NodeWindow(id, nodesList.toArray(new Short[0]));

            // Ligação das classes
            up.SetUser(nw);
            nw.SetUSI(up);

            // Cria as threads e coloca nos grupos
            Thread upThread = new Thread(uniGroup, up);
            Thread nwThread = new Thread(nwGroup, nw);

            // Inicializa as threads
            upThread.start();
            nwThread.start();
        }

    }
    
}

