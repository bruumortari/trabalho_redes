import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.net.*;

public class UnicastProtocol implements UnicastServiceUserInterface, UnicastServiceInterface {

    HashMap<Short, String> map;

    public void setHashMap(HashMap<Short, String> map) {
        this.map = map;
    }

    // Método para checar se a mensagem está no formato correto
    public boolean checkPDUFormat(String message) {
        String[] parts = message.split(" ");
        System.out.println("Termo 1: " + parts[0]);
        System.out.println("Termo 2: " + parts[1]);
        if (parts.length < 3) {
            return false;
        }
       else {
            try {
                // Checagem primeiro termo
                if(!parts[0].equals("UPDREQPDU")) {
                    System.out.println("Primeiro termo inválido: " + parts[0]);
                    return false;
                }
                int length = Integer.parseInt(parts[1]);
                // Checagem segundo termo
                if(length > 1024) {
                    System.out.println("Tamanho inválido: " + length);
                    return false;
                }
                System.out.println("Número de termos na mensagem: " + (parts[2].length()));
                if(parts[2].length() != Short.parseShort(parts[1])) {
                    System.out.println("Número de termos inválido: " + (parts.length - 2));
                    return false;
                }
                // Cria string a partir do terceiro elemento até o final
                String[] messageContent = Arrays.copyOfRange(parts, 2, parts.length);
                String msgContent = String.join(" ", messageContent);
                System.out.println("Conteúdo da mensagem: " + msgContent);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }   
    }

    // Método para ler o id, endereço ip e porta do arquivo config.txt
    public HashMap<Short, String> readConfigFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            HashMap<Short, String> mapping = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    short id = Short.parseShort(parts[0]);
                    String addressAndPort = parts[1] + " " + parts[2];
                    mapping.put(id, addressAndPort);
                } else {
                    System.out.println("Formato inválido na linha: " + line);
                }
            }
            reader.close();
            return mapping;
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            // Retorna um HashMap vazio em caso de erro
            return new HashMap<Short, String>();
        }
    }

    @Override
    public boolean UPDataRequest(short targetId, String message) {
        if (checkPDUFormat(message)) {
            ClientUP clientNode = new ClientUP();
            String target_address = "";
            short target_port = 0;
            boolean targetAddressFound = false;
            for(Short id : this.map.keySet()) {
                String[] addrParts = this.map.get(id).split(" ");
                String get_address = addrParts[0];
                short port = Short.parseShort(addrParts[1]); 
                if(id == targetId) {
                    target_address = get_address;
                    target_port = port;
                    targetAddressFound = true;
                    break;
                }
            }
            try {
                InetAddress inet_address = InetAddress.getByName(target_address);
                clientNode.runClient(inet_address, target_port, message);
            } catch (Exception e) {
                System.out.println("Erro: " + e);
                return false;
            }
            return true;
        } else {
            System.out.println("Formato inválido!");
            return false;
        }
    }

    @Override
    public void UPDataInd(short address, String message) {
       System.out.println("Mensagem recebida pelo servidor do endereço " + address + ": " + message);
    }

    public void runServer(short port) {
           try{
            // Inicializa o socket
            DatagramSocket datagramSocket = new DatagramSocket(port);
 
			// Cria datagrama para armazenar mensagem de requisicao
            byte[] buffer = new byte[1024];
			DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);			
			 
			// Recebe datagrama contendo mensagem de requisição
            System.out.println("Recebe requisicao...");
            datagramSocket.receive(requestPacket);

			// Extrai endereço
			InetAddress client_address = requestPacket.getAddress();
			int client_port = requestPacket.getPort();
			
            // extrai mensagem, converte para letras maiuscular e imprime mensagem convertida
            String message = new String(requestPacket.getData()).trim().toUpperCase();
            System.out.println(message);

            UPDataInd(Short.parseShort(client_address.toString().replace("/", "")), message);

			// Cria datagrama contendo mensagem de resposta
            buffer = message.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, client_address, client_port);
			
			// Envia datagrama contendo mensagem de resposta
			System.out.println("Envia resposta...");
            datagramSocket.send(responsePacket);
 
            // Fecha socket de datagrama
            datagramSocket.close();
        } catch(IOException e){
            System.err.println("Erro: " + e);
        }   
    } 

    public short[] getPortId(String args) {
        short[] result = new short[2];
        for(Short id : this.map.keySet()) {
            String[] addrParts = this.map.get(id).split(" ");
            String get_address = addrParts[0];
            short port = Short.parseShort(addrParts[1]); 
            if(get_address.equals(args)) {
                result[0] = id;
                result[1] = port;
                return result;
            }
        }
        return result;
    }  
}