package trabalho;

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
        //construtor
        ucsap_id = id;
        port_number = port;
        this.idEnd = idEnd;
        this.ipID = ipID;
        
        try{
            datagram = new DatagramSocket(port_number); //abre o socket
        }catch(IOException e){
            System.err.println("Erro: " + e);
        }
        
    }
    public void SetUser(UnicastServiceUserInterface usui){
        this.usui = usui; //seta o cliente que implementa a interface de user
    }
    

    @Override
    public boolean UPDataReq(short idTarget, String str) {
        InetAddress address;        	// endereco IP do socket servidor
        DatagramPacket requestPacket;   // pacote sendo enviado
        try{
            if(str == null) return false; //mensagem vazia eh descartada
            //criando mensagem
            String pdu = "UPDREQPDU" +" "+ str.length() + " " + str; //layout da mensagem padrao

            byte[] buffer = pdu.getBytes(); //buffer para o PDU

            if(buffer.length >1024){ //limite imposto de 1024 bytes
                System.err.println("Tamanho da mensagem excedida!");
                return false;
            }
            String[] end = idEnd.get(idTarget).split(":"); //busca o endereco (ip:porta) do no destino a partir do id usando o map 
            address = InetAddress.getByName(end[0]); //transforma em ip
            requestPacket = new DatagramPacket(buffer, buffer.length, address, Integer.parseInt(end[1])); //cria package 
            datagram.send(requestPacket);   //envia
            return true;     //deu tudo certo
        }catch(IOException e){
            System.err.println("Erro: " + e);
            return false;
        }
        
    }

    @Override
    public void run() {
        DatagramPacket requestPacket;   // pacote enviado pelo cliente
        byte[] buffer;                 	// buffer de dados
		String message;					// mensagem recebida/enviada
        try{
            //fica rodando para verificar se recebe alguma mensagem
            while(true){
                // cria datagrama para armazenar mensagem de requisicao

                buffer = new byte[1024];
                requestPacket = new DatagramPacket(buffer, buffer.length);			
                
                // recebe datagrama contendo mensagem de requisicao
                datagram.receive(requestPacket);

                message = new String(requestPacket.getData()).trim(); //recebe o pdu
                InetAddress address = requestPacket.getAddress(); //encontra o ip do no remetente
                int port = requestPacket.getPort(); //encontra a porta do no remetente
                String end = address.toString()+":"+port; //junta os dois para criar o endereco
                Short id = ipID.get(end); //busca o id pelo endereco no hash map
                if(id==null){
                    //se nao achou, end invalido
                    System.err.println("Erro: id nao encontrado!"); 
                }else{
                    //se achou, notifica a camada de cima
                    System.out.println("Mensagem do node "+id+": "+message+ " destinatario: "+ucsap_id);

                    //reconstrucao da mensagem
                    String[] parts = message.split(" "); //split da mensagem, pode splitar a mensagem tbm (se ela tiver espaco)
                    int i = Integer.parseInt(parts[1]); //tamanho da mensagem
                    int j = 2; //a partir daqui eh a mensagem
                    String msg = ""; 
                    while(i>0){ //enquanto ainda tem mensagem para processar
                        msg = msg + " "+ parts[j]; // concatena as partes (com espaco)
                        i = i - parts[j].length() - 1; // tira o que ja foi e o espaco (que tambem contou no tamanho da mensagem)
                        j++;
                    }
                    usui.UPDataInd(id, msg); //manda a mensagem limpa
                }
                
            }
        }catch(IOException e){
            System.err.println("Erro: " + e);
            datagram.close();
        }   
        
    }
    
}
