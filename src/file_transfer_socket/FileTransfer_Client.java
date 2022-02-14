
package file_transfer_socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.net.InetAddress;
import java.net.Socket;

public class FileTransfer_Client {
	public  DataInputStream din;
	public  DataOutputStream dout;
	public  Socket clientSocket;
//	

    public FileTransfer_Client(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.din = new DataInputStream(clientSocket.getInputStream());
        this.dout = new DataOutputStream(clientSocket.getOutputStream());;
        
    }

    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 2480);
            //host is 'localhost' when Client and Server are on the same machine, input IpAaddress when both are on different machines
             FileTransfer_Client obj=new FileTransfer_Client(clientSocket);
//            DataInputStream din = new DataInputStream(clientSocket.getInputStream());
//            DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String str = "";
            int i = 0;

            //Enter Name
            str =obj.din.readUTF();
            System.out.println("Server: " + str);
            obj.dout.writeUTF(br.readLine());
            
            
            //Enter PIN
            str =obj.din.readUTF();
            System.out.println("Server: " + str);
            obj.dout.writeUTF(br.readLine());
            str =obj.din.readUTF();
            
            System.out.println("Server: " + str);
            while(!str.equalsIgnoreCase("ok")){
                 obj.dout.writeUTF(br.readLine());
                 str =obj.din.readUTF();
                 System.out.println("Server: " + str);
            }
            
            str =obj.din.readUTF();
            System.out.println("Server: " + str);

            while (!(i == 3)) {
                str = obj.din.readUTF();
                System.out.println("\nServer: " + str);

                do {
                    try {
                        i = Integer.parseInt(br.readLine());

                        if (!(1 <= i && i <= 3)) {
                            System.out.println("        Please Check your Input!");
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Input should be a Number. Please Try again!");
                        i = -1;
                    }
                } while (!(1 <= i && i <= 3));				//loops until i matches the desired values
                obj.dout.writeInt(i);

                if (i == 1) {
                    do {
                        str =obj.din.readUTF();
                        System.out.println("\nServer: " + str);

                        str = br.readLine().trim();

                       obj.dout.writeUTF(str);					//writes the inputed fileName
                    } while (!(str.length() == 11));			//loops until the length of the input is matched

                   obj.receiveFile();					//file is received over the network
                } else if (i == 2) {
                    int j = 0;
                    while (!(j == 4)) {
                        str = obj.din.readUTF();
                        System.out.println("\nServer: " + str);

                        do {
                            try {
                                j = Integer.parseInt(br.readLine());

                                if (!(1 <= j && j <= 4)) {
                                    System.out.println("        Please Check your Input!");
                                }
                            } catch (NumberFormatException ex) {
                                System.out.println("        Input should be a Number. Please Try again!");
                                j = -1;
                            }
                        } while (!(1 <= j && j <= 4));				//loops until j matches the desired values
                       obj. dout.writeInt(j);

                        if (j == 1) {
                            str = obj.din.readUTF();
                            System.out.println("\nServer: " + str);

                            str = br.readLine();
                            obj.dout.writeUTF(str);					//returns String of departmentID
                        } else if (j == 2) {
                            str = obj.din.readUTF();
                            System.out.println("\nServer: " + str);

                            int files_length =obj.din.readInt();				//number of files present is received
                            int input_serial_no = 0;
                            do {
                                try {
                                    input_serial_no = Integer.parseInt(br.readLine());
                                } catch (NumberFormatException ex) {
                                    System.out.println("        Serial No. should be a Number. Please Try again!");
                                    input_serial_no = -1;
                                }
                                obj.dout.writeInt(input_serial_no);
                            } while (!(input_serial_no <= files_length && input_serial_no >= 0));			//loops until proper input is submitted

                           obj.receiveFile();					//file is received over the network
                        } else if (j == 3) {
                            do {
                                str =obj.din.readUTF();
                                System.out.println("\nServer: " + str);

                                str = br.readLine().trim();
                               obj.dout.writeUTF(str);
                            } while (!(str.length() == 11));					//loops until the length is matched

                           obj.receiveFile();					//file is received over the network
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public  void receiveFile() {
        int bytesRead = 0, current = 0;

        try {
            String fileName = din.readUTF();
            int fileLength = din.readInt();
            byte[] byteArray = new byte[fileLength];					//creating byteArray with length same as file length

            BufferedInputStream bis = new BufferedInputStream(din);

            File file = new File("Client Files\\" + fileName + ".pdf");

            //fileFoundFlag is a Flag which denotes the file is present or absent from the Server directory, is present int 0 is sent, else 1
            int fileFoundFlag = din.readInt();
            if (fileFoundFlag == 1) {
                return;
            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            bytesRead = bis.read(byteArray, 0, byteArray.length);			//reads bytes of length byteArray from BufferedInputStream & writes into the byteArray, (Offset 0 and length is of byteArray)
            current = bytesRead;

            //Sometimes only a portion of the file is read, hence to read the remaining portion...
            do {
                //BufferedInputStream is read again into the byteArray, offset is current (which is the amount of bytes read previously) and length is the empty space in the byteArray after current is subtracted from its length
                bytesRead = bis.read(byteArray, current, (byteArray.length - current));

                if (bytesRead >= 0) {
                    current += bytesRead;					//current is updated after the new bytes are read
                }
            } while (bytesRead > 0);
            bos.write(byteArray, 0, current);				//writes bytes from the byteArray into the BufferedOutputStream, offset is 0 and length is current (which is the amount of bytes read into byteArray)

            bos.close();

            System.out.println("        File " + fileName + " Successfully Downloaded!");
            dout.writeInt(0);						//writeInt is used to reset if any bytes are present in the buffer after the file transfer
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
