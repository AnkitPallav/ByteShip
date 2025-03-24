import java.io.*;
import java.net.*;

public class FileServer {
    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 5001;
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        new Thread(FileServer::startTCPServer).start();
        new Thread(FileServer::startUDPServer).start();
    }

    // TCP Server
    private static void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("TCP Server started on port " + TCP_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleTCPClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleTCPClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream())) {

            String command = in.readLine();
            if (command == null) return;

            String[] parts = command.split(" ", 2);
            if (parts.length < 2) return;

            String action = parts[0];
            String fileName = parts[1];

            if (action.equals("DOWNLOAD")) {
                sendFileTCP(out, fileName);
            } else if (action.equals("UPLOAD")) {
                receiveFileTCP(clientSocket, fileName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFileTCP(BufferedOutputStream out, String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            return;
        }

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

        fis.close();
        out.flush();
        System.out.println("File sent successfully via TCP: " + fileName);
    }

    private static void receiveFileTCP(Socket clientSocket, String fileName) {
        try (BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
             FileOutputStream fos = new FileOutputStream("Server_" + fileName)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            System.out.println("File received successfully via TCP: " + fileName);
        } catch (IOException e) {
            System.out.println("Error receiving file via TCP: " + e.getMessage());
        }
    }

    // UDP Server
    private static void startUDPServer() {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
            System.out.println("UDP Server started on port " + UDP_PORT);

            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(requestPacket);

                String receivedCommand = new String(requestPacket.getData(), 0, requestPacket.getLength());
                InetAddress clientAddress = requestPacket.getAddress();
                int clientPort = requestPacket.getPort();

                String[] parts = receivedCommand.split(" ", 2);
                if (parts.length < 2) continue;

                String action = parts[0];
                String fileName = parts[1];

                if (action.equals("DOWNLOAD")) {
                    sendFileUDP(udpSocket, clientAddress, clientPort, fileName);
                } else if (action.equals("UPLOAD")) {
                    receiveFileUDP(udpSocket, clientAddress, clientPort, fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFileUDP(DatagramSocket udpSocket, InetAddress clientAddress, int clientPort, String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("File not found: " + fileName);
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                DatagramPacket sendPacket = new DatagramPacket(buffer, bytesRead, clientAddress, clientPort);
                udpSocket.send(sendPacket);
            }

            // Send "END" signal
            byte[] endSignal = "END".getBytes();
            DatagramPacket endPacket = new DatagramPacket(endSignal, endSignal.length, clientAddress, clientPort);
            udpSocket.send(endPacket);

            fis.close();
            System.out.println("File sent successfully via UDP: " + fileName);
        } catch (IOException e) {
            System.out.println("Error sending file via UDP: " + e.getMessage());
        }
    }

    private static void receiveFileUDP(DatagramSocket udpSocket, InetAddress clientAddress, int clientPort, String fileName) {
        try (FileOutputStream fos = new FileOutputStream("Server_" + fileName)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            while (true) {
                udpSocket.receive(receivePacket);

                String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (receivedData.equals("END")) {
                    break;
                }

                fos.write(receivePacket.getData(), 0, receivePacket.getLength());
            }

            System.out.println("File received successfully via UDP: " + fileName);
        } catch (IOException e) {
            System.out.println("Error receiving file via UDP: " + e.getMessage());
        }
    }
}
