package file_transfer_socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Formatter;
import java.util.Locale;



public class FileTransfer_Server {


    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(2480);					//Server Port: 69696

            System.out.println("Server Started...");
            //waits for the Client to connect
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected..");
            // new Server Thread Start.....
            ServerThread obj=new ServerThread(socket);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
