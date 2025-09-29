import java.util.*;
import java.net.*;
import java.io.*;

public class main {

    public static void main(String[] args) {
        String message;
        InetAddress address;
        short port;
        short id;
        // Verifica quantidade de argumentos
        if (args.length != 1){
            System.err.println("Erro: informe o endereço IP do servidor");
            System.exit(0);
        }

        try {
            Scanner stdIn = new Scanner(System.in);
            System.out.print("Mensagem -> ");
			message = stdIn.nextLine();

            ClientUP client = new ClientUP();
            // Instância do protocolo de unicast
            UnicastProtocol unicastProtocol = new UnicastProtocol();

            // Leitura do arquivo de configuração (id, end IP e porta)
            HashMap<Short, String> configMap = new HashMap<>();
            configMap = unicastProtocol.readConfigFile("config.txt");
            unicastProtocol.setHashMap(configMap);
            //for (Short id : configMap.keySet()) {
                //System.out.println("ID: " + id + ", Address and Port: " + configMap.get(id));
            //}

            // Checagem do formato da PDU
            //String testMessage = "UPDREQPDU 10 HelloWorld";
            address = InetAddress.getByName(args[0]);
            short[] id_port = unicastProtocol.getPortId(args[0]);
            id = id_port[0];
            port = id_port[1];
            System.out.println("ID do destino: " + id + ", Porta do destino: " + port);
            boolean result = unicastProtocol.checkPDUFormat(message);
            if(result) {
                System.out.println("Formato PDU válido.");
                // Inicialização da thread do servidorr
                Thread serverThread = new Thread(() -> {
                System.out.println("Thread do servidor iniciando...");
                    try {
                        unicastProtocol.runServer(port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Thread do servidor finalizou.");
                });

                // Inicia a thread do cliente
                Thread clientThread = new Thread(() -> {
                    System.out.println("Thread do cliente iniciando...");
                    try {
                        client.runClient(address, port, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Thread do cliente finalizou.");
                });

                // Inicia as threads
                clientThread.start();
                serverThread.start();

                // Aguarda a thread do cliente terminar antes de continuar
                try {
                    clientThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Aguarda a thread do servidor terminar
                try {
                    serverThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Formato PDU inválido.");
            }   
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }

    }
}