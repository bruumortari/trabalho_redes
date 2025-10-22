import java.io.*;
import java.net.*;
import java.util.HashMap;

public class UnicastProtocol implements UnicastServiceInterface, Runnable{
    private short ucsap_id; //guarda o proprio ip
    private int port_number; //guarda a porta 
    private HashMap<Short,String> idEnd; //lista onde id do no eh chave e o endereco (ip:porta) eh o valor
    private HashMap<String,Short> ipID; //lista onde o endereco (ip:porta) eh a chave e o id eh o valor
    private UnicastServiceUserInterface usui; //classe "cliente" do UP
    private DatagramSocket datagram; //datagrama para receber/enviar as PDUs

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
    public void SetUser(UnicastServiceUserInterface usui){
        this.usui = usui; // Seta o cliente que implementa a interface de usuário
    }
    

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
                    String[] parts = message.split(" "); // Split da mensagem usando espaço como separador
                    int i = Integer.parseInt(parts[1]); // Tamanho da mensagem
                    int j = 2; 
                    String msg = ""; 
                    // Fica rodando até passar pela mensagem toda
                    while(i>0){
                        msg = msg + " "+ parts[j]; // Concatena as partes (com espaço)
                        i = i - parts[j].length() - 1; // Tira o que ja foi e o espaco (que também contou no tamanho da mensagem)
                        j++;
                    }
                    usui.UPDataInd(id, msg); // Manda a mensagem limpa
                }
                
            }
        }catch(IOException e){
            System.err.println("Erro: " + e);
            datagram.close();
        }   
        
    }
    
}
