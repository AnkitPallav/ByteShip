import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    private static final String SERVER_IP = "192.168.104.241"; // Change this to your server IP
    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 5001;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Do you want to (SEND/RECEIVE) a file? ");
        String action = scanner.nextLine().toUpperCase();

        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine();

        System.out.print("Choose Protocol (TCP/UDP): ");
        String protocol = scanner.nextLine().toUpperCase();

        if (protocol.equals("TCP")) {
            if (action.equals("SEND")) {
                sendFileTCP(fileName);
            } else if (action.equals("RECEIVE")) {
                receiveFileTCP(fileName);
            } else {
                System.out.println("Invalid action!");
            }
        } else if (protocol.equals("UDP")) {
            if (action.equals("SEND")) {
                sendFileUDP(fileName);
            } else if (action.equals("RECEIVE")) {
                receiveFileUDP(fileName);
            } else {
                System.out.println("Invalid action!");
            }
        } else {
            System.out.println("Invalid protocol! Use TCP or UDP.");
        }

        scanner.close();
    }

    // 📌 Send File via TCP
    private static void sendFileTCP(String fileName) {
        try (Socket socket = new Socket(SERVER_IP, TCP_PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(fileName)) {

            dos.writeUTF("SEND");
            dos.writeUTF(fileName); // Send file name

            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("Sending file via TCP...");

            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            System.out.println("File sent successfully via TCP!");
        } catch (IOException e) {
            System.out.println("Error: File not found or connection issue.");
        }
    }

    // 📌 Receive File via TCP
    private static void receiveFileTCP(String fileName) {
        try (Socket socket = new Socket(SERVER_IP, TCP_PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             FileOutputStream fos = new FileOutputStream("received_" + fileName)) {

            dos.writeUTF("RECEIVE");
            dos.writeUTF(fileName);

            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("Receiving file via TCP...");

            while ((bytesRead = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            System.out.println("File received successfully via TCP!");
        } catch (IOException e) {
            System.out.println("Error: File not found on server.");
        }
    }

    // 📌 Send File via UDP
    private static void sendFileUDP(String fileName) {
        try (DatagramSocket udpSocket = new DatagramSocket();
             FileInputStream fis = new FileInputStream(fileName)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            // Send File Name
            byte[] fileNameBytes = fileName.getBytes();
            DatagramPacket fileNamePacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, serverAddress, UDP_PORT);
            udpSocket.send(fileNamePacket);

            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("Sending file via UDP...");

            while ((bytesRead = fis.read(buffer)) != -1) {
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverAddress, UDP_PORT);
                udpSocket.send(packet);
            }

            // Send "END" Signal
            byte[] endSignal = "END".getBytes();
            DatagramPacket endPacket = new DatagramPacket(endSignal, endSignal.length, serverAddress, UDP_PORT);
            udpSocket.send(endPacket);

            System.out.println("File sent successfully via UDP!");
        } catch (IOException e) {
            System.out.println("Error: File not found or connection issue.");
        }
    }

    // 📌 Receive File via UDP (✅ NEWLY ADDED FUNCTION)
    private static void receiveFileUDP(String fileName) {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT);
             FileOutputStream fos = new FileOutputStream("received_" + fileName)) {

            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Receiving file via UDP...");

            while (true) {
                udpSocket.receive(packet);
                String receivedData = new String(packet.getData(), 0, packet.getLength());

                if (receivedData.equals("END")) break; // Stop when "END" signal is received

                fos.write(packet.getData(), 0, packet.getLength());
            }

            fos.flush();
            System.out.println("File received successfully via UDP!");
        } catch (IOException e) {
            System.out.println("Error: UDP receive failed.");
        }
    }
}


