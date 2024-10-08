import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;

public class UDPServer {
    private static CopyOnWriteArrayList<InetSocketAddress> clientAddresses = new CopyOnWriteArrayList<>();
    private static HashMap<InetSocketAddress, String> clientPseudos = new HashMap<>();

    public static void main(String[] args) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(5001);
        System.out.println("Server Started. Listening for Clients on port 5001...");

        byte[] receiveData = new byte[1024];

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            InetSocketAddress clientAddress = new InetSocketAddress(IPAddress, port);

            String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

            if (clientMessage.startsWith("PSEUDO:")) {
                String pseudo = clientMessage.substring(7);
                clientPseudos.put(clientAddress, pseudo);
                if (!clientAddresses.contains(clientAddress)) {
                    clientAddresses.add(clientAddress);
                }
                System.out.println("New client connected: " + pseudo + " [" + IPAddress + ":" + port + "]");
            } else {
                String pseudo = clientPseudos.getOrDefault(clientAddress, "Unknown");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String messageToBroadcast = pseudo + " a envoyé : " + clientMessage;
                System.out.println("[" + timestamp.toString() + " ,IP: " + IPAddress + " ,Port: " + port + "] " + messageToBroadcast);

                // Broadcast the message to all clients
                byte[] sendData = messageToBroadcast.getBytes();
                for (InetSocketAddress address : clientAddresses) {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address.getAddress(), address.getPort());
                    serverSocket.send(sendPacket);
                }
            }
        }
    }
}