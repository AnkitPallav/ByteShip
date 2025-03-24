import java.io.*; 
import java.net.*;

public class FileServer {
    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 5001;

    public static void main(String[] args) {
        try {
            new Thread(() -> startTCPServer()).start();
            new Thread(() -> startUDPServer()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 📌 TCP Server
    private static void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("TCP Server started on port " + TCP_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleTCPClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 📌 Handle TCP Client (Send or Receive)
    private static void handleTCPClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            String action = dis.readUTF(); // "SEND" or "RECEIVE"
            String fileName = dis.readUTF();

            if (action.equals("SEND")) {
                // Receive File
                try (FileOutputStream fos = new FileOutputStream("received_" + fileName)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.flush();
                    System.out.println("File received successfully via TCP!");
                }
            } else if (action.equals("RECEIVE")) {
                // Send File
                File file = new File(fileName);
                if (!file.exists()) {
                    dos.writeUTF("ERROR: File not found");
                    return;
                }

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    System.out.println("File sent successfully via TCP!");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 📌 UDP Server
    private static void startUDPServer() {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
            System.out.println("UDP Server started on port " + UDP_PORT);
            handleUDPClient(udpSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 📌 Handle UDP Client
    private static void handleUDPClient(DatagramSocket udpSocket) {
        byte[] buffer = new byte[4096];

        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(packet);
            String fileName = new String(packet.getData(), 0, packet.getLength());
            FileOutputStream fos = new FileOutputStream("received_" + fileName);

            System.out.println("Receiving file via UDP...");

            while (true) {
                packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String data = new String(packet.getData(), 0, packet.getLength());

                if (data.equals("END")) break; // Stop when "END" signal is received

                fos.write(packet.getData(), 0, packet.getLength());
            }

            fos.flush();
            System.out.println("File received successfully via UDP!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}