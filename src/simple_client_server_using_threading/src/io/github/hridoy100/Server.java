package simple_client_server_using_threading.src.io.github.hridoy100;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    public static int P=1;
    
    public static void Semawait(){
       
        while(P<=0);
        P--;
            
    }
    public static void Signal(){
      
        P++;
            
    }
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(22222);
        System.out.println("Server Started..");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected..");

            // new Server Thread Start.....
            new ServerThread(socket);


        }
    }
}

class ServerThread implements Runnable {

    Socket clientSocket;
    Thread t;

    ServerThread(Socket clientSocket){
        this.clientSocket = clientSocket;
        t= new Thread(this);
        t.start();
    }


    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());

            while (true) {
                //read from client...
                Object cMsg = ois.readObject();
                if(cMsg==null)
                    break;
                System.out.println("From Client: " + (String) cMsg);
                
                Server.Semawait();
                
                Thread.sleep(5000);

                String serverMsg = (String) cMsg;
                serverMsg = serverMsg.toUpperCase();
                
 
                //send to client..
                oos.writeObject(serverMsg);
                Server.Signal();
                
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
