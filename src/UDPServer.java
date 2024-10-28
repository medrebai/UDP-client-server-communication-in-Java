import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;

public class UDPServer {
    // List bech nsauvgardiw les client
    private static CopyOnWriteArrayList<InetSocketAddress> clientAddresses = new CopyOnWriteArrayList<>();
    // liaison bin l pseudo wel code
    private static HashMap<InetSocketAddress, String> clientPseudos = new HashMap<>();
    // liason bin l pseudo wel adresse
    private static HashMap<String, InetSocketAddress> pseudoToAddress = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Create a DatagramSocket to listen on port 5001
        DatagramSocket serverSocket = new DatagramSocket(5001);
        System.out.println("Server Started. Listening for Clients on port 5001...");

        byte[] receiveData = new byte[1024];

        while (true) {
            // Receive packet from client
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            InetSocketAddress clientAddress = new InetSocketAddress(IPAddress, port);

            String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

            if (clientMessage.startsWith("PSEUDO:")) {
                // Handle new client connection with pseudonym
                String pseudo = clientMessage.substring(7);
                clientPseudos.put(clientAddress, pseudo);
                pseudoToAddress.put(pseudo, clientAddress);
                if (!clientAddresses.contains(clientAddress)) {
                    clientAddresses.add(clientAddress);
                }
                System.out.println("New client connected: " + pseudo + " [" + IPAddress + ":" + port + "]");
            } else if (clientMessage.equals("LIST")) {
                // Handle request for list of connected clients
                StringBuilder listMessage = new StringBuilder("Connected clients: ");
                for (String clientPseudo : clientPseudos.values()) {
                    listMessage.append(clientPseudo).append(", ");
                }
                if (listMessage.length() > 0) {
                    listMessage.setLength(listMessage.length() - 2); // Remove the last comma and space
                }
                byte[] sendData = listMessage.toString().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress.getAddress(), clientAddress.getPort());
                serverSocket.send(sendPacket);
            } else {
                String pseudo = clientPseudos.getOrDefault(clientAddress, "Unknown");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String messageToBroadcast = pseudo + " a envoyé : " + clientMessage;
                System.out.println("[" + timestamp.toString() + " ,IP: " + IPAddress + " ,Port: " + port + "] " + messageToBroadcast);

                if (clientMessage.startsWith("@")) {
                    // Private message
                    int separatorIndex = clientMessage.indexOf(":");
                    if (separatorIndex != -1) {
                        String targetPseudo = clientMessage.substring(1, separatorIndex).trim();
                        String privateMessage = clientMessage.substring(separatorIndex + 1).trim();
                        InetSocketAddress targetAddress = pseudoToAddress.get(targetPseudo);
                        if (targetAddress != null) {
                            String privateMessageToSend = "Message privé de " + pseudo + " : " + privateMessage;
                            byte[] sendData = privateMessageToSend.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, targetAddress.getAddress(), targetAddress.getPort());
                            serverSocket.send(sendPacket);
                        }
                    }
                } else {
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
}