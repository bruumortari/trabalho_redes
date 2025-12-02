import java.io.*;
import java.net.*;
import java.util.HashMap;
/**
 * Implementação do protocolo de transferência unicast não
 * confiável (Unicast) utilizando sockets UDP.
 * <p>
 * Esta classe é responsável por abstrair a comunicação de rede. Ela converte identificadores
 * lógicos de nós (IDs) em endereços físicos (IP:Porta) e vice-versa.
 * Além disso, gerencia o envio e recebimento de datagramas e notifica a camada superior
 * {@link UnicastServiceUserInterface} quando novos dados chegam.
 * </p>
 */
public class UnicastProtocol implements UnicastServiceInterface, Runnable{
    private final short ucsap_id; //guarda o proprio ip
    private int port_number; //guarda a porta
    private HashMap<Short,String> idEnd; //lista onde id do no eh chave e o endereco (ip:porta) eh o valor
    private HashMap<String,Short> ipID; //lista onde o endereco (ip:porta) eh a chave e o id eh o valor
    private UnicastServiceUserInterface usui; //classe "cliente" do UP
    private DatagramSocket datagram; //datagrama para receber/enviar as PDUs

    /**
     * Construtor do protocolo Unicast.
     * <p>
     * Inicializa os identificadores e abre o socket UDP na porta especificada.
     * </p>
     *
     * @param id O ID lógico deste nó.
     * @param port A porta UDP local para escuta.
     * @param idEnd O mapa de tradução ID -> Endereço.
     * @param ipID O mapa de tradução Endereço -> ID.
     */
    public UnicastProtocol(short id, int port, HashMap<Short,String> idEnd, HashMap<String,Short> ipID){
        // Construtor
        ucsap_id = id;
        port_number = port;
        this.idEnd = idEnd;
        this.ipID = ipID;
        
        try{
            datagram = new DatagramSocket(port_number); // Abre o socket na porta informada
        }catch(IOException e){
            System.err.println("Erro: " + e);
        }
        
    }
    /**
     * Define a camada superior que utilizará este serviço.
     *
     * @param usui A instância que implementa a interface de usuário do serviço Unicast (RIP).
     */
    public void SetUser(UnicastServiceUserInterface usui){
        this.usui = usui; // Seta o cliente que implementa a interface de usuário
    }

    /**
     * Solicita o envio de dados para outro nó (Request).
     * <p>
     * O método encapsula a mensagem no formato:
     * {@code "UPDREQPDU <tamanho> <mensagem>"}.
     * Em seguida, resolve o endereço IP/Porta do destino baseando-se no ID fornecido
     * e envia via UDP.
     * </p>
     *
     * @param idTarget O ID do nó de destino.
     * @param str A mensagem (string) a ser enviada.
     * @return {@code true} se o envio foi iniciado com sucesso, {@code false} se houve erro de I/O,
     * se a mensagem for nula ou se exceder o tamanho máximo.
     */
    @Override
    public boolean UPDataReq(short idTarget, String str) {
        InetAddress address;        	// Endereço IP do socket
        DatagramPacket requestPacket;   // Pacote sendo enviado
        try{
            if(str == null) return false; // Mensagem vazia eh descartada
            // Cria mensagem no formato padrão
            String pdu = "UPDREQPDU" +" "+ str.length() + " " + str;

            byte[] buffer = pdu.getBytes(); // Buffer para o PDU

            if(buffer.length >1024){ // Limite imposto de 1024 bytes
                //System.err.println("Tamanho da mensagem excedida!");
                short errorId = -1;
                usui.UPDataInd(errorId, "Tamanho da mensagem excedida!");
                return false;
            }
            String[] end = idEnd.get(idTarget).split(":"); // Busca o endereço (ip:porta) do nó destino a partir do id usando o hashmap 
            address = InetAddress.getByName(end[0]); // Transforma em ip
            requestPacket = new DatagramPacket(buffer, buffer.length, address, Integer.parseInt(end[1])); // Cria o pacote 
            datagram.send(requestPacket);   // Envia o pacote
            return true;    
        }catch(IOException e){
            System.err.println("Erro: " + e);
            return false;
        }
        
    }
    /**
     * Função chamada ao inicializar a Thread. Fica em looping esperando receber mensagens. Ao receber:
     * <ol>
     * <li>Identifica quem enviou através do IP/Porta de origem.</li>
     * <li>Reconstroi a mensagem a partir do PDU recebido.</li>
     * <li>Notifica a camada superior através de {@code usui.UPDataInd}.</li>
     * </ol>
     */
    @Override
    public void run() {
        DatagramPacket requestPacket;   // Pacote enviado pelo cliente
        byte[] buffer;                 	// Buffer de dados
		String message;					// Mensagem recebida/enviada
        try{
            // Fica rodando para verificar se recebe alguma mensagem
            while(true){
                // Cria datagrama para armazenar mensagem de requisição
                buffer = new byte[1024];
                requestPacket = new DatagramPacket(buffer, buffer.length);			
                
                // Recebe datagrama contendo mensagem de requisição
                datagram.receive(requestPacket);

                message = new String(requestPacket.getData()).trim(); // Recebe o pdu
                InetAddress address = requestPacket.getAddress(); // Encontra o ip do nó remetente
                int port = requestPacket.getPort(); // Encontra a porta do nó remetente
                String end = address.toString()+":"+port; // Junta os dois para criar o endereço
                Short id = ipID.get(end); // Busca o id pelo endereço no hash map
                if(id==null){
                    // Se nao achou, endereço é inválido
                    System.err.println("Erro: id nao encontrado!"); 
                }else{
                    // Se achou, notifica a camada de cima
                    System.out.println("Mensagem do node "+id+": "+message+ " destinatario: "+ucsap_id);

                    // Reconstrução da mensagem
                    String msg = getString(message);
                    usui.UPDataInd(id, msg); // Manda a mensagem limpa
                }
                
            }
        }catch(IOException e){
            System.err.println("Erro: " + e);
            datagram.close();
        }   
        
    }
    /**
     * Método para reconstruir a mensagem original a partir do PDU.
     * <p>
     * Como o protocolo utiliza espaços para separar o cabeçalho do corpo,
     * e o corpo da mensagem também pode conter espaços, este método
     * concatena as partes corretamente baseando-se no tamanho informado no cabeçalho.
     * </p>
     *
     * @param message A string crua recebida no pacote (incluindo cabeçalhos).
     * @return A mensagem de texto original enviada pelo usuário.
     */
    private static String getString(String message) {
        String[] parts = message.split(" "); // Split da mensagem usando espaço como separador
        int i = Integer.parseInt(parts[1]) - 1 - parts[2].length(); // Tamanho da mensagem
        int j = 3;
        String msg = parts[2];
        // Fica rodando até passar pela mensagem toda
        while(i>0){
            msg = msg + " "+ parts[j]; // Concatena as partes (com espaço)
            i = i - parts[j].length() - 1; // Tira o que ja foi e o espaco (que também contou no tamanho da mensagem)
            j++;
        }
        return msg;
    }

}
