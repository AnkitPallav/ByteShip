
# **ByteShip 🚀**  
*A Client-Server File Transfer System using TCP & UDP in Java.*  

## 📌 **Overview**  
**ByteShip** is a simple yet efficient **socket programming** project that enables file transfer between a client and a server. It supports both **TCP (Reliable)** and **UDP (Fast but Unreliable)** connections, making it a great learning resource for **computer networks** and **distributed systems**.  

## 🛠 **Features**  
✔ **Supports TCP & UDP connections** for file transfer.  
✔ **Client can request multiple files** from the server.  
✔ **Server handles multiple client requests efficiently.**  
✔ **Showcases practical socket programming** in Java.  

## 📂 **Project Structure**  
```bash
ByteShip/
│── Client_Side/
│   ├── FileClient.java   # Client-side implementation
│   ├── Ankit.txt        # Sample file for transfer
│── Server_Side/
│   ├── FileServer.java   # Server-side implementation
│   ├── Pallav.txt       # Sample file on server
│   ├── Sarthak.txt      # Sample file on server
└── README.md
```

## 🚀 **Getting Started**  
Follow these steps to run the project:  

### 1️⃣ **Clone the Repository**  
```sh
git clone https://github.com/AnkitPallav/ByteShip.git
cd ByteShip
```

### 2️⃣ **Compile the Java Files**  
```sh
javac Server_Side/FileServer.java Client_Side/FileClient.java
```

### 3️⃣ **Start the Server**  
```sh
java Server_Side.FileServer
```

### 4️⃣ **Run the Client**  
```sh
java Client_Side.FileClient
```

### 5️⃣ **Transfer Files & Test the Connection!** 🚀  

## 📜 **Usage**  
1️⃣ **Start the server** and wait for client connections.  
2️⃣ **Run multiple clients** to send/receive files from the server.  
3️⃣ **Monitor logs** to check the transfer status.  

## 🤝 **Contributors**  
- **👤 Ankit Pallav**  

