import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main{
    private static HashMap<Short,String> idEnd;
    private static HashMap<String,Short> ipID;
    private static final Set<Short> nodesList = new HashSet<>();
    private static HashMap<String, Integer> linksCost;
    private static int numberNodes = 0;
    private static final TreeSet<Short> nodesGraph = new TreeSet<>();

    private static void SetConfig(){
        // Função para ler o arquivo de configuração
        // Preenche os hashmaps com as informações do arquivo (id endereçoIP porta)
        try{
            // Hashmap "id" : "endereço:porta"
            idEnd = new HashMap<>();
            // Hashmap "/endereço:porta" : "id"
            ipID = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
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
    private static void SetRouting(){
        // Função para ler o arquivo de configuração de roteamento
        // Preenche os hashmaps com as informações do arquivo (nóX nó custo)
        try{
            // Hashmap "nóX:nóY" : "custo"
            linksCost = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader("routingConfig.txt"));
            String line;
            // Lê cada linha do arquivo
            while ((line = reader.readLine()) !=null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    short node1 = Short.parseShort(parts[0]);
                    short node2 = Short.parseShort(parts[1]);
                    int cost = Integer.parseInt(parts[2]);

                    // Checa se o custo é válido
                    if(cost < 0 ) {
                        System.out.println("Número de custo inválido: " + cost);
                        continue;
                    }
                    // Checa se os nós são válidos
                    if(node1 <= 0 || node2 <= 0 || node1 == node2 || !nodesList.contains(node1) || !nodesList.contains(node2)) {
                        System.out.println("Número de nó inválido: " + node1 + " ou " + node2);
                        continue;
                    }
                    linksCost.put(node1 + ":" + node2, cost);
                    numberNodes = Math.max(numberNodes, Math.max(node1, node2));
                } else {
                    System.out.println("Formato inválido na linha: " + line);
                }
            }
            reader.close();
        }catch(IOException e){
            System.out.println("Erro ao ler o arquivo de roteamento: " + e.getMessage());
            System.exit(-1);
        }
    }
    public static void main(String[] args){
        // Chama função para ler o arquivo
        SetConfig();
        SetRouting();
        // Se o hashmap estiver vazio, algo deu errado na leitura do arquivo
        if(idEnd == null){
            System.err.println("Erro: arquivo de configuracao inválido!");
            System.exit(-1);
        }
        if(linksCost == null){
            System.err.println("Erro: arquivo de configuracao de roteamento inválido!");
            System.exit(-1);
        }
        // Grupo de threads
        ThreadGroup uniGroup = new ThreadGroup("Unicasts");
        ThreadGroup rpiGroup = new ThreadGroup("RPIs");
        ThreadGroup windGroup = new ThreadGroup("Windows");
        Set<Short> nodesGraph = new HashSet<>(nodesList);
        // Percorre todos os nós
        for(Short id : nodesList){
            String[] end = idEnd.get(id).split(":"); // Splita o endereço guardado (endereçoIP:porta)

            if(!end[0].equals("127.0.0.1") ){
                continue; // Só cria instâncias que forem locais
            }
            // Instancia o unicast protocol, informando os hashmaps, id e porta
            UnicastProtocol up = new UnicastProtocol(id, Integer.parseInt(end[1]), idEnd, ipID);

            // Inicialização da classe Abstrata RoutingInformationProtocol
            AbstractRIP rip;

            if(id == 0){ // Se for nó gerente
                // Cria o RIP tipo gerente
                rip = new RoutingInformationManager(id, numberNodes, linksCost, nodesGraph);
                // Cria a camada de aplicação
                RoutingManagementApplication rma = new RoutingManagementApplication(id, nodesGraph);
                //Faz a ligação entre as camadas (RIP e Aplicação)
                rma.SetRPMI((RoutingProtocolManagementInterface) rip);
                ((RoutingInformationManager) rip).SetRPMUser(rma);
                //Inicializa a thread de aplicação
                Thread rmaThread = new Thread(rma);
                rmaThread.start();
            }else{ //Se for nó
                // Cria o RIP tipo nó
                rip = new RoutingInformationNode(id, numberNodes, linksCost);
                // Cria a GUI do nó
                NodeWindow nw = new NodeWindow(id, nodesGraph);
                // Faz a ligação entre as camadas (RIP e GUI)
                nw.SetUser((WindowNodeUserInterface) rip);
                ((RoutingInformationNode) rip).SetGUI(nw);
                // Inicializa a thread de GUI
                Thread nwThread = new Thread(windGroup, nw);
                nwThread.start();
            }
            // Faz a ligação entre o RIP e Unicast
            up.SetUser(rip);
            rip.SetUSI(up);

            // Cria as threads (RIP e Unicast) e coloca nos grupos
            Thread upThread = new Thread(uniGroup, up);
            Thread ripThread = new Thread(rpiGroup, rip);

            // Inicializa as threads
            upThread.start();
            ripThread.start();
        }

    }
}
