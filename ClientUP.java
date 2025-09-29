// Código para rodar o cliente (peguei do EchoClient que Clever mostrou em aula)

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientUP {
    // Socket
    DatagramSocket datagramSocket;  
    // Endereco IP 	
    InetAddress address;     
    // Pacote enviado   	
    DatagramPacket requestPacket;   
    // Pacote recebido
    DatagramPacket responsePacket;
    // Número da porta de destino
    short port;
    String message;
    byte[] buffer;

    public void runClient(InetAddress address, short port, String message) {
        try {
            this.address = address;
            this.port = port;
            this.message = message;
            // Inicializa o socket
            datagramSocket = new DatagramSocket();
            buffer = message.getBytes();
            // Cria datagrama para armazenar mensagem de requisição
            requestPacket = new DatagramPacket(buffer, buffer.length, this.address, this.port);
            System.out.println("Envia requisicao...");
            datagramSocket.send(requestPacket);
            // Cria datagrama para armazenar mensagem de resposta
			buffer = new byte[1024];
			responsePacket = new DatagramPacket(buffer, buffer.length);
			// Recebe datagrama contendo mensagem de resposta
            System.out.println("Recebe resposta...");
			datagramSocket.receive(responsePacket);
            // Imprime a mensagem de resposta
			message = new String(responsePacket.getData()).trim();
			System.out.println("Resposta -> " + message);
			// Fecha socket de datagrama
			datagramSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

}