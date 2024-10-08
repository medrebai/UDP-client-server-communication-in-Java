import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPClient {
    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(1000);
        Scanner input = new Scanner(System.in);

        Thread receiveThread = new Thread(() -> {
            byte[] receiveData = new byte[1024];
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    String serverMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Message from server: " + serverMessage);
                } catch (IOException e) {
                    // Timeout exception is expected, continue listening
                }
            }
        });

        receiveThread.start();

        while (true) {
            String cmd = input.nextLine();
            if (cmd.equals("QUIT")) {
                clientSocket.close();
                System.exit(1);
            }
            byte[] sendData = cmd.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("127.0.0.1"), 5001);
            clientSocket.send(sendPacket);
        }
    }
}