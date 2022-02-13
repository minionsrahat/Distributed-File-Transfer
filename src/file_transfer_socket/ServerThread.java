/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file_transfer_socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Formatter;
import java.util.Locale;

/**
 *
 * @author RAHAT
 */
class ServerThread extends Thread {

    Socket socket;
    Thread t;
    File file;
    File[] files;

    public  DataInputStream din;
    public  DataOutputStream dout;
    public  StringBuilder sb;
    public  String fileName, accountID, departmentID;

    ServerThread(Socket clientSocket) {
        this.socket = clientSocket;
        file = new File("Server Files");
        files = new File[0];
        sb = new StringBuilder();
        this.start();
    }

    @Override
    public void run() {
        try {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            String clientName;
            int i;
            boolean stopFlag = false;				//Flag which stops the application when set true

            dout.writeUTF("Enter your name ");
            clientName = din.readUTF();

            dout.writeUTF("Welcome, " + clientName);

            while (!stopFlag) {
                sb.append("Choose an option\n"
                        + "        1. Download File\n"
                        + "        2. List Files\n"
                        + "        3. Exit");
                dout.writeUTF(sb.toString());

                i = din.readInt();
                sb.setLength(0);					//resets the StringBuilder to 0 length

                switch (i) {
                    case 1:
                        this.inputFileName();				//asks client to input fileName to download

                        //departmentID and accountID are obtained from the inputed filename
                        file = new File("Server Files\\" + departmentID + "-" + accountID + ".pdf");

                        this.sendFile(file, accountID, departmentID);			//the file if present, is sent over the network
                        file = new File("Server Files");					//file path is reset to the root directory
                        break;

                    case 2:
                        file = new File("Server Files");
                        files = file.listFiles();
                        int j = 0;
                        sb.append("Total Files in folder - " + files.length + "\n");

                        this.listFiles(files);			//lists files of the Server directory in a Table Format

                        while (!(j == 4)) {
                            sb.append("Select an Option\n"
                                    + "        1. Filter Files by Department\n"
                                    + "        2. Enter Serial No. to Download\n"
                                    + "        3. Enter FileName to Download\n"
                                    + "        4. Go To Main Menu");
                            dout.writeUTF(sb.toString());

                            j = din.readInt();			//receives input for the above 4 options presented
                            sb.setLength(0);			//StringBuilder is reset to length 0

                            //A Switch Case with new options after listing files is presented
                            switch (j) {
                                case 1:
                                    dout.writeUTF("Enter the Department Code to Filter");
                                    departmentID = din.readUTF().trim();				//client inputs the departmentID to filter

                                    if (departmentID.length() < 4) //padding zero's to the left
                                    {
                                        departmentID = String.format("%4s", departmentID).replace(' ', '0');
                                    } else if (departmentID.length() > 4) //removing the extra values from the left
                                    {
                                        departmentID = departmentID.substring(departmentID.length() - 4);
                                    }

                                    //Final String DEP is created to use with the listFiles Filter below, because startsWith method requires a final String as input
                                    final String DEP = departmentID;

                                    files = file.listFiles((no, name) -> name.trim().startsWith(DEP));			//filtering directory with the inputed departmentID

                                    sb.append("Total Files with Department ID [" + DEP + "] - " + files.length + "\n");
                                    this.listFiles(files);				//lists files of the Server Directory in a Table Format
                                    break;

                                case 2:
                                    dout.writeUTF("Enter the Serial No. of File to download ");

                                    dout.writeInt(files.length);					//Sends the number of files present
                                    int sr_no;
                                    //serial no. of the file to download is taken from the client
                                    do {
                                        sr_no = din.readInt();
                                    } while (!(sr_no <= files.length && sr_no > 0));				//loops until a proper input is submitted

                                    //departmentID and accountID of the file is retrieved from the file chosen by the client
                                    departmentID = files[sr_no - 1].getName().substring(0, 4);
                                    accountID = files[sr_no - 1].getName().substring(5, 11);

                                    file = new File("Server Files\\" + departmentID + "-" + accountID + ".pdf");				//file path points to the chosen file
                                    this.sendFile(file, accountID, departmentID);				//the file if present, is sent over the network

                                    file = new File("Server Files");					//file path is reset to the root directory
                                    break;

                                case 3:
                                    this.inputFileName();				//asks client to input fileName to download

                                    //departmentID and accountID are obtained from the inputed filename
                                    file = new File("Server Files\\" + departmentID + "-" + accountID + ".pdf");

                                    this.sendFile(file, accountID, departmentID);				//the file if present, is sent over the network
                                    file = new File("Server Files");					//file path is reset to the root directory
                                    break;

                                case 4:
                                    break;

                                default:
                                    sb.append("Unable to identify option. Please try again!\n");
                            }
                        }
                        break;

                    case 3:
                        stopFlag = true;
                        break;

                    default:
                        sb.append("Unable to identify option. Please try again!\n");

                }
            }

            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public  void inputFileName() {
        try {
            do {
                dout.writeUTF("Enter the FileName which you want to Download\n"
                        + "        File Naming Convention:  DepartmentID-AccountID  eg: 1234-123456");

                fileName = din.readUTF();
            } while (!(fileName.length() == 11));				//loops until the input length matches

            //departmentID and accountID is retrieved from the inputed filename
            departmentID = fileName.substring(0, 4);
            accountID = fileName.substring(fileName.length() - 6);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public  void sendFile(File file, String accountID, String departmentID) {
        try {
            fileName = departmentID + "-" + accountID;
            dout.writeUTF(fileName);

            byte[] byteArray = new byte[(int) file.length()];					//creating byteArray with length same as file length
            dout.writeInt(byteArray.length);

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            //Writing int 0 as a Flag which denotes the file is present in the Server directory, if file was absent, FileNotFound exception will be thrown and int 1 will be written
            dout.writeInt(0);

            BufferedOutputStream bos = new BufferedOutputStream(dout);

            int count;
            while ((count = bis.read(byteArray)) != -1) {			//reads bytes of byteArray length from the BufferedInputStream into byteArray
                bos.write(byteArray, 0, count);					//writes bytes from byteArray into the BufferedOutputStream (0 is the offset and count is the length)
            }

            bos.flush();
            bis.close();

            din.readInt();					//readInt is used to reset if any bytes are present in the buffer after the file transfer
        } catch (FileNotFoundException ex) {
            sb.append("File " + fileName + " Not Found! \n        Please Check the input and try again.\n\n        ");

            try {
                //Writing int 1 as a Flag which denotes the file is absent from the Server directory, if file was present int 0 would be written
                dout.writeInt(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public  void listFiles(File[] files) {
        int k = 0;

        sb.append("\n        +---------+----------------------+\n");
        Formatter formatter = new Formatter(sb, Locale.US);
        //formats the fields to create table like structure while displaying on console
        formatter.format("        | %-7s | %-20s |\n", "Sr No", "Filename");
        sb.append("        +---------+----------------------+\n");

        for (File f : files) {
            if (!f.isDirectory()) {
                formatter.format("        | %-7s | %-20s |\n", ++k, f.getName());
            }
        }

        sb.append("        +---------+----------------------+\n\n        ");
        formatter.close();
    }

}