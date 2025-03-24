import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 5001;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose protocol: 1. TCP  2. UDP");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.println("Choose operation: 1. Download File  2. Upload File");
        int operation = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter the filename: ");
        String fileName = scanner.nextLine();

        if (choice == 1) {
            if (operation == 1) {
                requestFileTCP(fileName);
            } else if (operation == 2) {
                sendFileTCP(fileName);
            }
        } else if (choice == 2) {
            if (operation == 1) {
                requestFileUDP(fileName);
            } else if (operation == 2) {
                sendFileUDP(fileName);
            }
        } else {
            System.out.println("Invalid choice. Exiting.");
        }

        scanner.close();
    }

    private static void requestFileTCP(String fileName) {
        try (Socket socket = new Socket(SERVER_ADDRESS, TCP_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedInputStream in = new BufferedInputStream(socket.getInputStream())) {

            out.println("DOWNLOAD " + fileName);

            File receivedFile = new File("Received_" + fileName);
            FileOutputStream fos = new FileOutputStream(receivedFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            fos.close();
            System.out.println("File received successfully via TCP!");

        } catch (IOException e) {
            System.out.println("Error receiving file via TCP: " + e.getMessage());
        }
    }

    private static void sendFileTCP(String fileName) {
        try (Socket socket = new Socket(SERVER_ADDRESS, TCP_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream())) {

            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("File not found.");
                return;
            }

            out.println("UPLOAD " + fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            fis.close();
            bos.flush();
            System.out.println("File sent successfully via TCP!");

        } catch (IOException e) {
            System.out.println("Error sending file via TCP: " + e.getMessage());
        }
    }

    private static void requestFileUDP(String fileName) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);

            byte[] fileNameBytes = ("DOWNLOAD " + fileName).getBytes();
            DatagramPacket requestPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, serverAddress, UDP_PORT);
            udpSocket.send(requestPacket);

            File receivedFile = new File("Received_" + fileName);
            FileOutputStream fos = new FileOutputStream(receivedFile);

            byte[] buffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            while (true) {
                udpSocket.receive(receivePacket);
                String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());

                if (receivedData.equals("END")) {
                    break;
                }

                fos.write(receivePacket.getData(), 0, receivePacket.getLength());
            }

            fos.close();
            System.out.println("File received successfully via UDP!");

        } catch (IOException e) {
            System.out.println("Error receiving file via UDP: " + e.getMessage());
        }
    }

    private static void sendFileUDP(String fileName) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);

            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("File not found.");
                return;
            }

            byte[] fileNameBytes = ("UPLOAD " + fileName).getBytes();
            DatagramPacket requestPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, serverAddress, UDP_PORT);
            udpSocket.send(requestPacket);

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            DatagramPacket sendPacket;

            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                sendPacket = new DatagramPacket(buffer, bytesRead, serverAddress, UDP_PORT);
                udpSocket.send(sendPacket);
            }

            byte[] endSignal = "END".getBytes();
            sendPacket = new DatagramPacket(endSignal, endSignal.length, serverAddress, UDP_PORT);
            udpSocket.send(sendPacket);

            fis.close();
            System.out.println("File sent successfully via UDP!");

        } catch (IOException e) {
            System.out.println("Error sending file via UDP: " + e.getMessage());
        }
    }
}
