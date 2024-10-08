import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.concurrent.CopyOnWriteArrayList;

public class UDPServer {
    private static CopyOnWriteArrayList<InetSocketAddress> clientAddresses = new CopyOnWriteArrayList<>();

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

            if (!clientAddresses.contains(clientAddress)) {
                clientAddresses.add(clientAddress);
            }

            String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println("[" + timestamp.toString() + " ,IP: " + IPAddress + " ,Port: " + port + "]  " + clientMessage);

            // Broadcast the message to all clients
            for (InetSocketAddress address : clientAddresses) {
                DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), address.getAddress(), address.getPort());
                serverSocket.send(sendPacket);
            }
        }
    }
}