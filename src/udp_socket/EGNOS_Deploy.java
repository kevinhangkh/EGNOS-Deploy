/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_socket;

import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;
import com.sun.security.ntlm.Client;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import sun.net.ftp.FtpClient;
import java.lang.Object;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.events.StartElement;
import org.apache.commons.net.ftp.FTPClientConfig;
import java.nio.file.Files;
import java.nio.file.Paths;




/**
 *
 * @author admin
 */
public class EGNOS_Deploy {
    
    //------------------------GLOBAL VARIABLES------------------------//
    //Reading IP file:
    public static int count = 0; //number of assets
    public static int [] egnosAddresses; //array containing EGNOS addresses of all assets to deploy (if multiple)
    public static int egnosAddress; //EGNOS address of the asset to deploy (if only one)
    public static String name; //Name of the asset to deploy (if only one)
    public static String[] names; //Names of all assets to deploy (if multiple)
    
    public final String log = "log.txt";
    public static int rejectedCounter = 0; //number of rejected commands
    public static int modeAsset = 0; //NLES G2 asset mode (0 to 6)
    
    //Reading filecodes
    public final String nameRimsA = "RIMSA";
    public final String nameRimsAG2 = "RIMSAG2";
    public final String nameRimsB = "RIMSB";
    public final String nameRimsC = "RIMSC";
    public static final String nameNLES = "NLES";
    public static final String nameNLESG2 = "NLESG2";
    public static String nameNLES_2 = "NLES";
    public static String nameNLESG2_2 = "NLESG2";
    public final String nameCPF_PS = "CPFPS";
    public final String nameCPF_CS = "CPFCS";
    
    //Cyclic monitoring message length
    public static final int SIZE_NLESG2 = 71;
    
    //NLES G2 Modes
    public static final int NLESG2_UNKNOWN = 0;
    public static final int NLESG2_LOADED = 1;
    public static final int NLESG2_INIT = 2;
    public static final int NLESG2_RUNNING = 3;
    public static final int NLESG2_OPERATIONAL = 4;
    public static final int NLESG2_TEST = 5;
    public static final int NLESG2_FAILED = 6;
    
    //NLES G2 List FTP
    public static String[] ftpToDownload;
    public static String pathToDownload;
    public static String[] filesToUpload;
    public static String[] pathsToUpload;
    
    //NLES G2 File Paths
    public static final String UPLOADNLESG2 = "UPLOAD\\NLESG2\\";
    public static final String IR = "CONF\\NLES_ConfigurationData_G2\\Facility.510\\";
    public static final String ER = "CONF\\NLES_Software_G2\\Software\\SWRU\\";
    public static String IR80 = "CONF\\NLES_ConfigurationData_G2\\Facility.510\\IR801101.510";
    public static String IR81 = "CONF\\NLES_ConfigurationData_G2\\Facility.510\\IR811101.510";
    public static String ER64 = "CONF\\NLES_Software_G2\\Software\\SWRU\\ER640301.EXE";
    public static String ER70 = "CONF\\NLES_Software_G2\\Software\\SWRU\\ER700100.EXE";
    public static String ER75 = "CONF\\NLES_Software_G2\\Software\\SWRU\\ER750500.EXE";

    /////////////PARAMETERS FOR RESET///////////////
        //HEADER
        public static byte msgType = 0x6e;
        public static byte flowType = 0x21;
        public static byte[] headerLength = new byte[]{(byte) 0x00, (byte) 0x1f};
        public static byte[] originAddress = new byte[]{(byte) 0x01, (byte) 0x94};
//        byte[] originAddress = new byte[]{(byte) 0x01, (byte) 0x91};
        public static byte[] destinationAddress = new byte[]{(byte) 0x01, (byte) 0xfe}; //EGNOS ADDR NLES G2
        public static byte[] spare = new byte[]{(byte) 0x29, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        //COMMAND
        public static byte sectionIdentifier = 0x65;
        public static byte[] confirmationKey = new byte[] {(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x00};
        public static byte commandType = 0x01;
        public static byte commandParameter = 0x00;
        
        public static byte[] wholeMessage = null;
        
        public static String[] listIPAddr;
        public static byte[] listFileCode;
        public static boolean next = false;
        /////////////PARAMETERS FOR RESET///////////////
        
        /////////////PARAMETERS FOR INIT///////////////
        public static byte commandTypeInit = 0x02;
        public static byte commandParamInitInstalled = 0x01;
        /////////////PARAMETERS FOR INIT///////////////
        
    //EGNOS CLIENT
    public static EGNOS_Deploy Client = new EGNOS_Deploy();
        
    //GUI
    public static GUI window = new GUI();
    
    public String[] readIPFile() throws IOException {

        System.out.println("*************READING IP FILE*************");
        System.out.println();
        
        window.log("READING IP FILE");
        
        try (BufferedReader br = new BufferedReader(new FileReader("ip_addresses.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            
            String[] everyStr;
            int[] egnosAddr;
            int indexOfIp = 0;
            int indexOfEGNOS = 1;
            int indexOfName = 2;
//            int countFEE = 0;
//            int countCCF = 0;
            List<String> listIP;

            //READ THE ENTIRE FILE AND COUNT LINES
            while (line != null) {
                sb.append(line);
                sb.append(" ");
                line = br.readLine();
                count++;
//                System.out.println("countLine = " + ++count);
            }
            //IF FILE EMPTY => EXIT
            if (count == 0) {
                System.out.println("IP file is empty");
                window.log("IP file is empty");
                System.exit(0);
            }
            
            String everything = sb.toString();
            System.out.println("everything: " + everything);

            //SPLIT STRING EVERY SPACE CHARACTER
            everyStr = everything.split(" ");
            System.out.println("everyStr split \" \": ");
            for (String everyStr1 : everyStr) {
                System.out.println(everyStr1);
            }
            
            //STRING ARRAY AND INT ARRAY
            String[][] everyStr2 = new String[everyStr.length][];
            egnosAddr = new int[everyStr2.length];
            String[] localNames = new String[everyStr.length];
            
            //SPLIT STRINGS EVERY ";"
            for (int i = 0; i < everyStr.length; i++) {
                everyStr2[i] = everyStr[i].split(";");
            }

            //PUT ALL IP ADDRESSES IN STRING ARRAY
            for (int i = 0; i < everyStr.length; i++) {
                everyStr[i] = everyStr2[i][indexOfIp];
                System.out.println(everyStr[i]);
            }
            //PUT AND CONVERT TO INT ALL EGNOS ADDRESSES IN INT ARRAY
            for (int i = 0; i < everyStr2.length; i++) {
                egnosAddr[i] = Integer.valueOf(everyStr2[i][indexOfEGNOS]);
                System.out.println(egnosAddr[i]);
            }
            //PUT ALL NAMES IN STRING ARRAY
            System.out.println("Names: ");
            for (int i = 0; i < everyStr2.length; i++) {
                localNames[i] = everyStr2[i][indexOfName];
                System.out.println(localNames[i]);
                
//                if (localNames[i].equals(nameFEE))
//                    countFEE++;
//                else if (localNames[i].equals(nameCCF))
//                    countCCF++;
            }
            
//            System.out.println("Count FEE: " + countFEE);
//            indexOfFEE = new int[countFEE];
//            System.out.println("Count CCF: " + countCCF);
//            indexOfCCF = new int[countCCF];
            
            //PUT INDEX OF FEE AND CCF IN A INT ARRAY
            //THANKS FRANCOIS BALTAZAR
//            int countTheFEE = 0;
//            int countTheCCF = 0;
//            for (int j = 0; j < localNames.length; j++) {
//                if (localNames[j].equals(nameFEE))
//                {
//                    indexOfFEE[countTheFEE] = j;
//                    countTheFEE++;
//                }
//                else if (localNames[j].equals(nameCCF)) {
//                    indexOfCCF[countTheCCF] = j;
//                    countTheCCF++;
//                }
//            }
            
//            if (countFEE > 0) {
//                System.out.println("index of FEE:");
//                for (int i = 0; i < indexOfFEE.length; i++) {
//                    System.out.printf("%d ", indexOfFEE[i]);
//                }
//                System.out.println("");
//            }
            
//            if (countCCF > 0) {
//                System.out.println("index of CCF:");
//                for (int i = 0; i < indexOfCCF.length; i++) {
//                    System.out.printf("%d ", indexOfCCF[i]);
//                }
//                System.out.println("");
//            }
            
            
            //IF ONLY 1 LINE IN TXT FILE, PUT THE EGNOS ADDRESS IN AN INTEGER
            if (count == 1) {
                egnosAddress = egnosAddr[0];
                System.out.printf("One egnos address: 0x%02X", egnosAddress);
                System.out.println("");
                name = localNames[0];
                System.out.printf("One name: " + name);
            }
            //IF SEVERAL LINES, PUT ALL EGNOS ADDRESSES IN AN INTEGER ARRAY AND ALL NAMES IN STRING ARRAY
            else if (count > 1) {
                //EGNOS ADDRESSES
                egnosAddresses = new int[egnosAddr.length];
                System.out.println("length of egnosAddresses: " + egnosAddresses.length);
                System.arraycopy(egnosAddr, 0, egnosAddresses, 0, egnosAddr.length);
                
                System.out.printf("%d egnos addresses:\n", egnosAddresses.length);
                for (int i = 0; i < egnosAddresses.length; i++) {
                    System.out.printf("%02X ", egnosAddresses[i]);
                }
                System.out.println("");
                
                //NAMES
                names = new String[localNames.length];
                System.out.println("length of names: " + names.length);
                System.arraycopy(localNames, 0, names, 0, localNames.length);
                
                System.out.printf("%d names:\n", names.length);
                for (String name1 : names) {
                    System.out.printf("%s ", name1);
                }
                System.out.println("");
            }
            
            //CONVERT STRING ARRAY TO LIST OF STRINGS
            listIP = new ArrayList<>(Arrays.asList(everyStr));
            
            //TEST ALL IP ADDRESSES
            for (int i = 0; i < listIP.size(); i++) {
                System.out.println(listIP.get(i));
                CheckIPAddress testIPAddress = new CheckIPAddress(listIP.get(i));
                //IF IP ADDRESS IS NOT VALID, REMOVE IT FROM THE LIST
                if (testIPAddress.checkValidity()) {
                    listIP.remove(i);
                    i--;
                }
            }
//            System.out.println(listIP);

            //CONVERT LIST OF STRINGS TO STRING ARRAY CONTAINING ONLY VALID IP ADDRESSES
            everyStr = listIP.toArray(new String[0]);
            System.out.println("\nValid IP addresse(s): ");
            
            for (int i = 0; i < everyStr.length; i++) {
                System.out.println(everyStr[i]);
                window.log("Valid IP address: " + everyStr[i]);
            }
            
            return everyStr;
            
        }    
    }
    
    public byte [] readFileCode(String name) throws IOException {

        String choice = null;
        String txt = ".txt";

        String fileCPFCS = "CPF_CS.txt";
        
        switch (name) {
            case nameRimsA:
                choice = nameRimsA + txt;
                break;
            case nameRimsAG2:
                choice = nameRimsAG2 + txt;
                break;
            case nameRimsB:
                choice = nameRimsB + txt;
                break;
            case nameRimsC:
                choice = nameRimsC + txt;
                break;
            case nameNLES:
                choice = nameNLES + txt;
                break;
            case nameNLESG2:
                choice = nameNLESG2 + txt;
                break;
//            case nameCPF_CS1:
//            case nameCPF_CS2:
//                choice = fileCPFCS; //same file for both CPF CS
//                break;
            case nameCPF_CS:
                choice = nameCPF_CS + txt;
                break;
            case nameCPF_PS:
                choice = nameCPF_PS + txt;
                break;
//            case nameCCF:
//                choice = nameCCF + txt;
//                break;
            default:
                System.err.println("IP address does not correspond to any asset");
                break;
        }
        
        System.out.println("Reading filecode(s) to deploy: ");
        try (BufferedReader br = new BufferedReader(new FileReader(choice))) {
            
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            String[] everyStr;

            while (line != null) {
                sb.append(line);
                sb.append(" ");
//                sb.append(System.lineSeparator()); //////THIS LINE IS EVIL//////
                line = br.readLine();
            }
            String everything = sb.toString();
            System.out.println("Parameters: " + everything);
           
//            System.out.println("everyStr: ");
            everyStr = everything.split(" ");
//            for (int j = 0; j < everyStr.length; j++) {
//                System.out.println(everyStr[j] + " ");
//            }
            
//            System.out.println("versions: ");
            int[] versions = new int[everyStr.length];

            //Convert String to int
            for (int i = 0; i < everyStr.length; i++) {
                versions[i] = Integer.parseInt(everyStr[i]);
//                System.out.println(versions[i] + " ");
            }
            
            //Convert int to byte
            byte[] listVer = new byte [versions.length];
            for (int i = 0; i < versions.length; i++) {
                listVer[i] = (byte) versions[i];
                System.out.printf("%02x  ", listVer[i]);
            }
            
            System.out.println("");

            return listVer;
        }
        
            
        
    }
    
    public void multicastSocket(String name) throws IOException, InterruptedException {
        
        int portNumber = 32900;
        String hostName = "224.64.64.97";
        int counter = 0;
        int size = 0;
        
        MulticastSocket s = new MulticastSocket(portNumber);
        s.joinGroup(InetAddress.getByName(hostName));
        
        //According to the name, assign the size of message we are looking for
        switch (name)
        {
            case nameNLESG2:
                size = SIZE_NLESG2;
                System.out.println("LOOKING FOR NLESG2");
                break;
            default:
                System.out.println("Multicast Socket: Name of asset not recognized");
                System.exit(0);
        }
        
        byte[] buffer = new byte[size];
        DatagramPacket pack = new DatagramPacket(buffer, buffer.length);
        
        byte[] egnosAddr = new byte[2];
        int locationEgnosAddr = 7;
        int egnosAddrNLESG2 = 510;
        
        System.out.println("\n");
        
        while (true) {
//            Thread.sleep(250);
            s.receive(pack);
            System.out.println("Time: " + counter++ + " ; Received from: " + pack.getAddress().toString() + ": " + pack.getPort() + " with length: " + pack.getLength());
//            System.out.write(pack.getData(),0,pack.getLength());

//            System.out.println();
//
//            for (int k = 0; k < buffer.length; k++) {
//                System.out.printf("%02X ", buffer[k]);
//            }
//            System.out.println("\n");
            
            System.arraycopy(buffer, locationEgnosAddr, egnosAddr, 0, egnosAddr.length);
            System.out.printf("EGNOS Address: %d\n\n", new BigInteger(egnosAddr).intValue());
            
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////CHANGE egnosAddrNLESG2. Must be global var
            if ((buffer[1] == 0x29)&&(new BigInteger(egnosAddr).intValue() == egnosAddrNLESG2)) //Message Type 41
            {
                System.out.println("*************NLES FOUND*************");
                break;
            }
                
        }
        
        s.leaveGroup(InetAddress.getByName(hostName));
        s.close();
        
        whatMode(buffer);
    }
    
    public void whatMode(byte [] data) {
        int location = 34;
        byte mode;
        int modeInt = 0;
        
        
        System.out.println("whatMode data: ");
        for (int k = 0; k < data.length; k++) {
                System.out.printf("%02X ", data[k]);
            }
            System.out.println();
        
        switch (data.length)
        {
            case SIZE_NLESG2:
                mode = data[location];
//                System.out.printf("mode = %02X\n", mode);
                
                mode = shiftBit(mode);
                
                System.out.printf("MODE: ");
                switch (modeAsset = mode)
                {
                    case NLESG2_UNKNOWN:
                        System.out.println("UNKNOWN");
                        window.log("UNKNOWN");
                        break;
                    case NLESG2_LOADED:
                        System.out.println("LOADED");
                        window.log("LOADED");
                        break;
                    case NLESG2_INIT:
                        System.out.println("INITIALISED");
                        window.log("INITIALISED");
                        break;
                    case NLESG2_RUNNING:
                        System.out.println("RUNNING");
                        break;
                    case NLESG2_OPERATIONAL:
                        System.out.println("OPERATIONAL");
                        window.log("OPERATIONAL");
                        break;
                    case NLESG2_TEST:
                        System.out.println("TEST");
                        window.log("TEST");
                        break;
                    case NLESG2_FAILED:
                        System.out.println("FAILED");
                        window.log("FAILED");
                        break;
                    default:
                        System.err.println("ERROR COULD NOT IDENTIFY NLESG2 MODE");
                        window.log("ERROR COULD NOT IDENTIFY NLESG2 MODE");
                }
                
                break;
            default:
                break;
        }
    }
        
    //Extract the mode of the asset
    public byte shiftBit(byte data) {
        int tmp = 0;
        
        //Convert to byte to int
        tmp = data;
//        System.out.println("tmp = " + tmp);
                
        //Bit shifting
        tmp = tmp >> 2;
//        System.out.println("tmp >> 2 = " + tmp);
                
        //Isolate first 3 bits (0x07 = 0b00000111)
        tmp = tmp & 0x07;
//        System.out.println("tmp & 0x07 = " + tmp);
                
        //Convert int to byte
        data = (byte) tmp;
        System.out.printf("mode = %02X\n", data);
        
        return data;
    }

    public void  manageModeNLESG2(String ip, String name) throws FileNotFoundException, IOException, InterruptedException {
        
        //If the asset is not in LOADED nor UNKNOWN mode nor OPERATIONAL
        if (modeAsset != NLESG2_LOADED && modeAsset != NLESG2_UNKNOWN && modeAsset != NLESG2_OPERATIONAL) {
            
            cmdReset(ip);
            
            //checking if asset has switched to LOADED mode
            NLESG2_isItLoaded(name);
            
        }
        else if (modeAsset == NLESG2_UNKNOWN) {
            //UNKNOWN MODE
            System.err.println("UNKNOWN MODE, END");
            System.exit(0);
        }
        else if (modeAsset == NLESG2_OPERATIONAL) {
            //OPERATIONAL MODE
            System.out.println("OPERATIONAL MODE, END");
            System.exit(0);
        }
    }

    public byte [] createMessage(String name, byte msgType, byte flowType, byte []headerLength, byte [] origAddress, byte [] destAddress, byte []spare, 
            byte sectionId, byte [] confirmKey, byte cmdType, byte cmdParam) throws FileNotFoundException {
        
        byte[] entireMessage;
        boolean notNLESInstall = false;
        boolean notNLESUninstall = false;
        
        EGNOS_Message msg = new EGNOS_Message(name, notNLESInstall, notNLESUninstall);
        msg.EGNOS_Header = new EGNOS_StandardHeader(msgType, flowType, headerLength, origAddress, destAddress, spare);
        msg.EGNOS_Cmd = new EGNOS_Command(sectionId, confirmKey, cmdType, cmdParam);
        msg.computeDataLength();
        entireMessage = msg.concatenate();
        msg.computeCRC(entireMessage);
        msg.addCRC(entireMessage, msg.crcByte1, msg.crcByte2, msg.crcByte3, msg.crcByte4);

        resetStdOutput();
        System.out.println("Complete Data Created Message:");
        for (int i = 0; i < entireMessage.length; i++) {
            System.out.printf("%02X ", entireMessage[i]);
        }
        System.out.println("\n");

        msg.EGNOS_Header.printHeader();
        msg.EGNOS_Cmd.printCmd();
        //setLog();
        
        return entireMessage;
    }
    
    public byte [] createMessageInstall(String name, byte msgType, byte flowType, byte []headerLength, byte [] origAddress, byte [] destAddress, byte []spare, 
            byte sectionId, byte [] confirmKey, byte fCode, byte [] cmdParam) throws FileNotFoundException {
        
        byte[] entireMessage;
        boolean NLES_install = true;
        boolean NLES_uninstall = false;
        
        EGNOS_Message msg = new EGNOS_Message(name, NLES_install, NLES_uninstall);
        msg.EGNOS_Header = new EGNOS_StandardHeader(msgType, flowType, headerLength, origAddress, destAddress, spare);
        msg.EGNOS_Cmd_Install = new EGNOS_Command_Install(sectionId, confirmKey, fCode, cmdParam);
        msg.computeDataLength();
        entireMessage = msg.concatenate();
        msg.computeCRC(entireMessage);
        msg.addCRC(entireMessage, msg.crcByte1, msg.crcByte2, msg.crcByte3, msg.crcByte4);

        resetStdOutput();
        System.out.println("Complete Data Created Message:");
        for (int i = 0; i < entireMessage.length; i++) {
            System.out.printf("%02X ", entireMessage[i]);
        }
        System.out.println("\n");

        msg.EGNOS_Header.printHeader();
        msg.EGNOS_Cmd_Install.printCmd();
        
        return entireMessage;
    }
    
    public byte [] createMessageUninstall(String name, byte msgType, byte flowType, byte []headerLength, byte [] origAddress, byte [] destAddress, byte []spare, 
            byte sectionId, byte [] confirmKey, byte fCode, byte [] cmdParam) throws FileNotFoundException {
        
        byte[] entireMessage;
        boolean NLES_install = false;
        boolean NLES_uninstall = true;
        
        EGNOS_Message msg = new EGNOS_Message(name, NLES_install, NLES_uninstall);
        msg.EGNOS_Header = new EGNOS_StandardHeader(msgType, flowType, headerLength, origAddress, destAddress, spare);
        msg.EGNOS_Cmd_Uninstall = new EGNOS_Command_Uninstall(sectionId, confirmKey, fCode, cmdParam);
        msg.computeDataLength();
        entireMessage = msg.concatenate();
        msg.computeCRC(entireMessage);
        msg.addCRC(entireMessage, msg.crcByte1, msg.crcByte2, msg.crcByte3, msg.crcByte4);

        resetStdOutput();
        System.out.println("Complete Data Created Message:");
        for (int i = 0; i < entireMessage.length; i++) {
            System.out.printf("%02X ", entireMessage[i]);
        }
        System.out.println("\n");

        msg.EGNOS_Header.printHeader();
        msg.EGNOS_Cmd_Uninstall.printCmd();
        
        return entireMessage;
    }
    
    public void sendMessage(String hostName, byte [] msg) {

        //Define the port number
        int portNumber = 32896;

//        System.out.println("IN SENDMESSAGE()");
        try (Socket sock = new Socket(hostName, portNumber);
             OutputStream dOutput = sock.getOutputStream();) 
        {
            System.out.println();
            System.out.println("***NOW SENDING MESSAGE***");
            dOutput.write(msg);
            System.out.println("***MESSAGE SENT***");
            System.out.println();

            Thread t_getResponse = new Thread(new getResponse(sock));
            t_getResponse.start();
            
            System.out.println("********THREAD STARTED*********");
            System.out.println();
            
            try {
                //Wait for t_getResponse to end
                t_getResponse.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(EGNOS_Deploy.class.getName()).log(Level.SEVERE, null, ex);
            }
   
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to "
                    + hostName + e);
            System.exit(1);
        }
        
        System.out.println();
        System.out.println("********THREAD ENDED*********");
        System.out.println();
        
    }
    
    public String[] whatsInTheFolder(String path) {
        
        File dir = new File(path);
        String[] files = dir.list();
        
        if (files.length == 0) {
            System.out.println(path + " is empty");
        }
//        else{
//            for (String aFile : files) {
//                System.out.println(aFile);
//            }
//        }
        
        return files;
    }
    
    public void linkFileCodeAndFile(String[] paths) {
       
        int k = 0;
        boolean next = false;
        List<String> listPaths;
        //Create list from String array
        listPaths = new ArrayList<>(Arrays.asList(paths));
        
        for (int i = 0; i < listFileCode.length; i++) {
            
//            System.out.println(listPaths.get(i));
            for (int j = 0; j < listPaths.size(); j++) {
                k = j;
                if (paths[j].contains(String.valueOf((int)listFileCode[i]))) {
                    next = true;
                    System.out.println(listPaths.get(j) + " contains " + String.valueOf((int)listFileCode[i]));
                    
                }
                if (next) {
                    
                    break;
                }
                
                
            }
            
                //Remove the path from list if the file to upload corresponding to the filecode is not found
                if (!next) {
                    
                    System.out.println("Removing " + listPaths.get(i));
                    listPaths.remove(i);
                    if (i != 0) {
                        i--;
                    }
                }
                    
                
            
            next = false;
        }
        

        
    }
    
    public void uploadFile(String hostName, String path, String[] fileName){
        
        int port = 21;
        
        String user = "egnosop";
        String pass = "egnosop";
        
        
        FTPClient ftpClient = new FTPClient();
        System.out.println("\nUPLOADING");
        try {
            ftpClient.connect(hostName, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            
            
            
            for (int i = 0; i < fileName.length; i++) {
                
                File localFile = new File(path + fileName[i]);
                String remoteFile = fileName[i];


                InputStream inputStream = new FileInputStream(localFile);
            
                System.out.println("Start uploading " + fileName[i]);
            
                boolean done = ftpClient.storeFile(remoteFile, inputStream);
                inputStream.close();
                if (done) {
                    System.out.println(fileName[i] + " uploaded successfully");
                }
                
            }
//            String remoteFile = fileName;
//
//
//            InputStream inputStream = new FileInputStream(localFile);
//            
//            System.out.println("Start uploading " + fileName);
//            
//            boolean done = ftpClient.storeFile(remoteFile, inputStream);
//            inputStream.close();
//            if (done) {
//                System.out.println(fileName + " uploaded successfully");
//            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
                Logger.getLogger(EGNOS_Deploy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public String[] listFilesFTP(int egnosAddr) throws IOException {
        
        String hostName = "199.234.195.1";
        int port = 21;
        String path = "/mnt/system/inst/data";
        String[] content = null;
        
        String EXE = "EXE";
        int j = 0;
        List<String> listPaths;
        
        String user = "egnosop";
        String pass = "egnosop";
        
        FTPClient client = new FTPClient();
        client.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
        
        client.connect(hostName);
        client.enterLocalPassiveMode();
        client.login(user, pass);
        
        System.out.println("Listing files in " + path);
        content = client.listNames(path);
//        content = client.listNames();
        
        if (content == null && content.length == 0) {
            System.out.println(path + " is empty");
        }
        else {
            for (int i = 0; i < content.length; i++) {
                
                System.out.println(content[i]);
            }
            
//            //Create list from String array
//            listPaths = new ArrayList<>(Arrays.asList(content));
//            
//            for (int i = 0 ; i < listPaths.size() ; i++) {
//                
//                //If the extension is not the EGNOS ADDRESS nor EXE
//                if (!(listPaths.get(i).substring(listPaths.get(i).lastIndexOf(".") + 1).equals(String.valueOf(egnosAddr))) 
//                        && !(listPaths.get(i).substring(listPaths.get(i).lastIndexOf(".") + 1).equals(EXE))) {
//                    
//                    System.out.println("Removing " + listPaths.get(i));
//                    listPaths.remove(i);
//                    i--;
//                }
//            }
//            
//            //Convert list to String array
//            content = listPaths.toArray(new String[0]);
//            
//            for (int i = 0; i < content.length; i++) {
//                
//                System.out.println("good content: " + content[i]);
//            }
//            System.out.println("size of Content: " + content.length);
        }
        
        return content;
    }
    
    public void downloadFTP(String[] content){
        
        String hostName = "199.234.195.1";
        int port = 21;
        
        String user = "egnosop";
        String pass = "egnosop";
        
        
        FTPClient ftpClient = new FTPClient();
        System.out.println("\nDOWNLOADING " + content[0]);
        
        try {
            ftpClient.connect(hostName, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            String remoteFile = content[0];
            File downloadFile = new File("DOWNLOAD\\");
            
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile));
            boolean success = ftpClient.retrieveFile(remoteFile, outputStream1);
            outputStream1.close();
 
            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }
            
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
                Logger.getLogger(EGNOS_Deploy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //Read the version of the file inside the file.
    public byte [] readNLESG2Version(File file, String name) throws FileNotFoundException, IOException {
        
        System.out.println("readNLESG2Version");
        
        RandomAccessFile raf = new RandomAccessFile(name, "r");
        int length = 28;
        int offset = 39;
        
        
        raf.seek(offset);
        
        //Array equal to the length of raf
        byte[] equalSize = new byte[(int) raf.length()];
        
        //Read file
        raf.readFully(equalSize, offset, length);
        
        //String based on equalSize
        String hey = new String(equalSize);
        
        byte[] version = hey.getBytes();
        
        version = Arrays.copyOfRange(version, offset, offset + length);
        
        System.out.println("after");
        System.out.println("NLESG2 File version: ");
        for (int i = 0; i < version.length; i++) {
            System.out.printf("%02X ", version[i]);
        }
        System.out.println("");
        
        return version;
    }
    
    public byte readNLESG2FileCode(String name) throws FileNotFoundException, IOException {
        
        FileInputStream fs = new FileInputStream(name);
        BufferedReader br = new BufferedReader(new InputStreamReader(fs));
        
        //Read 1st line and do nothing
        br.readLine();
        
        //2nd line is the one we want
        String fileCodeSTRING = br.readLine();
        fileCodeSTRING = fileCodeSTRING.substring(0,2);
        
        System.out.println("LINE WE WANT: " + fileCodeSTRING);
        
        int fileCodeINT = Integer.parseInt(fileCodeSTRING);
        System.out.println("INT: " + fileCodeINT);
        
        byte fileCode = (byte) fileCodeINT;
        System.out.printf("%02X\n", fileCode);
        return fileCode;
    }
    
    public byte [] installNLESG2(String path, String fileName, byte msgType, byte flowType, byte []headerLength, byte[] originAddress, byte[] destinationAddress, 
        byte []spare, byte sectionId, byte [] confirmKey) throws FileNotFoundException, IOException {
        
        byte[] command;
        String name = "NLES G2 INSTALL " + fileName;
        byte fileCode;
        
        File file = new File(fileName);
        
        byte commandType = (byte) 0xc9;
        byte [] commandParameter = new byte[28];
        
        fileCode = readNLESG2FileCode(path + fileName);
        
        //The version of the IR80 file is read in the file and must figure in the command parameter
        commandParameter = readNLESG2Version(file, path + fileName);
        
        //Last byte of commandParameter must be 0x00 instead of 0x20 for INSTALL COMMAND
        commandParameter[27] = (byte) 0x00;
        
        command = createMessageInstall(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionId, confirmKey, fileCode, commandParameter);
        
        return command;
    }
    
    public byte [] installNLESG2_IR80(byte msgType, byte flowType, byte []headerLength, byte[] originAddress, byte[] destinationAddress, 
        byte []spare, byte sectionId, byte [] confirmKey) throws FileNotFoundException, IOException {
        
        byte[] command;
        String name = "NLES G2 INSTALL IR80";
        String nameOfFile = "CONF\\NLES_ConfigurationData_G2\\Facility.510\\IR801101.510";
        
        byte fileCode = 0x50; //80
        
        File file = new File("IR801101.510");
        
        byte commandType = (byte) 0xc9;
        byte [] commandParameter = new byte[28];

        //The version of the IR80 file is read in the file and must figure in the command parameter
        commandParameter = readNLESG2Version(file,nameOfFile);
        
        //Last byte of commandParameter must be 0x00 instead of 0x20 for INSTALL COMMAND
        commandParameter[27] = (byte) 0x00;
        
        command = createMessageInstall(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionId, confirmKey, fileCode, commandParameter);
        
        return command;
    }
    
    public byte [] installNLESG2_IR81(byte msgType, byte flowType, byte []headerLength, byte[] originAddress, byte[] destinationAddress, 
        byte []spare, byte sectionId, byte [] confirmKey) throws FileNotFoundException, IOException {
        
        byte[] command;
        String name = "NLES G2 INSTALL IR81";
        String nameOfFile = "CONF\\NLES_ConfigurationData_G2\\Facility.510\\IR811101.510";
        
        byte fileCode = 0x51; //81
        
        File file = new File("IR811101.510");
        
        byte commandType = (byte) 0xc9;
        byte [] commandParameter = new byte[28];

        commandParameter = readNLESG2Version(file,nameOfFile);
        
        //Last byte of commandParameter must be 0x00 instead of 0x20 for INSTALL COMMAND otherwise it's REJECTED
        commandParameter[27] = (byte) 0x00;
        
//        commandParameter[5] = 0x33;
        
        command = createMessageInstall(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionId, confirmKey, fileCode, commandParameter);
        
        return command;
    }
    
    public byte [] installNLESG2_ER64(byte msgType, byte flowType, byte []headerLength, byte[] originAddress, byte[] destinationAddress, 
        byte []spare, byte sectionId, byte [] confirmKey) throws FileNotFoundException, IOException {
        
        byte[] command;
        String name = "NLES G2 INSTALL ER64";
        String nameOfFile = "CONF\\NLES_Software_G2\\Software\\SWRU\\ER640301.EXE";
        
        byte fileCode = 0x40; //64
        
        File file = new File("ER640301.EXE");
        
        byte commandType = (byte) 0xc9;
        byte [] commandParameter = new byte[28];

        //The version of the file is read in the file and must figure in the command parameter
        commandParameter = readNLESG2Version(file,nameOfFile);
        
        //Last byte of commandParameter must be 0x00 instead of 0x20 for INSTALL COMMAND
        commandParameter[27] = (byte) 0x00;
        
        command = createMessageInstall(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionId, confirmKey, fileCode, commandParameter);
        
        return command;
    }
    
    public byte [] installNLESG2_ER70(byte msgType, byte flowType, byte []headerLength, byte[] originAddress, byte[] destinationAddress, 
        byte []spare, byte sectionId, byte [] confirmKey) throws FileNotFoundException, IOException {
        
        byte[] command;
        String name = "NLES G2 INSTALL ER70";
        String nameOfFile = "CONF\\NLES_Software_G2\\Software\\SWRU\\ER700100.EXE";
        
        byte fileCode = 0x46; //70
        
        File file = new File("ER700100.EXE");
        
        byte commandType = (byte) 0xc9;
        byte [] commandParameter = new byte[28];
        
        //The version of the file is read in the file and must figure in the command parameter
        commandParameter = readNLESG2Version(file,nameOfFile);
        
        //Last byte of commandParameter must be 0x00 instead of 0x20 for INSTALL COMMAND
        commandParameter[27] = (byte) 0x00;
        
        command = createMessageInstall(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionId, confirmKey, fileCode, commandParameter);
        
        return command;
    }
    
    public byte [] installNLESG2_ER75(byte msgType, byte flowType, byte []headerLength, byte[] originAddress, byte[] destinationAddress, 
        byte []spare, byte sectionId, byte [] confirmKey) throws FileNotFoundException, IOException {
        
        byte[] command;
        String name = "NLES G2 INSTALL ER75";
        String nameOfFile = "CONF\\NLES_Software_G2\\Software\\SWRU\\ER750500.EXE";
        
        byte fileCode = 0x4b; //75
        
        File file = new File("ER750500.EXE");
        
        byte commandType = (byte) 0xc9;
        byte [] commandParameter = new byte[28];
        
        //The version of the file is read in the file and must figure in the command parameter
        commandParameter = readNLESG2Version(file,nameOfFile);
        
        //Last byte of commandParameter must be 0x00 instead of 0x20 for INSTALL COMMAND
        commandParameter[27] = (byte) 0x00;
        
        command = createMessageInstall(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionId, confirmKey, fileCode, commandParameter);
        
        return command;
    }
    
    public byte [] uninstallNLESG2(String path, String fileName, byte msgType, byte flowType, byte []headerLength, byte[] originAddress, byte[] destinationAddress, 
        byte []spare, byte sectionId, byte [] confirmKey) throws FileNotFoundException, IOException {
        
        byte[] command;
        String name = "NLES G2 UNINSTALL " + fileName;
        byte fileCode;
        
        File file = new File(fileName);
        
        byte commandType = (byte) 0xca;
        byte [] commandParameter = new byte[28];

        fileCode = readNLESG2FileCode(path + fileName);
        
        //The version of the IR80 file is read in the file and must figure in the command parameter
        commandParameter = readNLESG2Version(file, path + fileName);
        
        //Last byte of commandParameter must be 0x00 instead of 0x20 for UNINSTALL COMMAND
        commandParameter[27] = (byte) 0x00;
        
        command = createMessageUninstall(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionId, confirmKey, fileCode, commandParameter);
        
        return command;
    }
    
    public void verifyEgnosAddress() {
        
        //If the EGNOS address is smaller than 255 (can be stored in 1 byte)
        if (egnosAddress < 0xFF)
            destinationAddress = new byte[] {(byte)0x00,(byte)egnosAddress};
        //If the EGNOS address is bigger than 255 (cannot be stored in 1 byte)
        else
            destinationAddress = BigInteger.valueOf(egnosAddress).toByteArray();
    }
    
    public void cmdReset(String ip) throws FileNotFoundException {
        
        byte[] msg;
        String name = "RESET";
        
        msg = createMessage(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey, commandType, commandParameter);
        sendMessage(ip, msg);
    }
    
    public void cmdInitInstall(String ip) throws FileNotFoundException {
        
        byte[] msg;
        String name = "INITIALISE INSTALL";
//        byte commandTypeInit = 0x02;
//        byte commandParamInitInstalled = 0x01;
        
        msg = createMessage(name, msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey, commandTypeInit, commandParamInitInstalled);
        sendMessage(ip, msg);
    }
    
    public void NLESG2_isItLoaded(String nameOfAsset) throws IOException, InterruptedException {
        
        do {            
            multicastSocket(nameOfAsset);
        } while (modeAsset != NLESG2_LOADED);
    }
    
    public void NLESG2_isItInit(String nameOfAsset) throws IOException, InterruptedException {
        
        do {                
            multicastSocket(nameOfAsset);
        } while (modeAsset != NLESG2_INIT);
    }
    
    /*****************************************************************************
     * Name : setLog
     * Purpose : Redirect the output to a log file
     * Argument I/O: 
     * I/O Files: No input file
     * Returns :
     * 
     * @throws java.io.FileNotFoundException *************************************************************************/
    public void setLog() throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(log,true));
        System.setOut(out);
    }
    
    /*****************************************************************************
     * Name : resetStdOutput
     * Purpose : Redirect the output to the standard output
     * Argument I/O: 
     * I/O Files: No input file
     * Returns :
     *  *************************************************************************/
    public void resetStdOutput() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
}
    
    //USELESS
    public void bigLoop() throws IOException, FileNotFoundException, InterruptedException {
        
        //FINAL LOOP
        for (int i = 0 ; i < listIPAddr.length ; i++){
            
            System.out.println("\nDEPLOYING " + listIPAddr[i] + "\n");
            
            if (count == 1) {
//                listFileCode = Client.readFileCode(name);
                
                Client.verifyEgnosAddress();
                
                //Check asset mode
//                Client.multicastSocket(name);

                //NLES G2: MANAGE MODE, UPLOAD, INSTALL, INIT INSTALL, RESET
                if (name.equals(nameNLESG2)) {
                    //Manage asset mode
                    Client.manageModeNLESG2(listIPAddr[i], name);
                    
                    System.out.println("UPLOADING NLESG2 FILES:");
                    
                    //UPLOAD all files found in the folder
                    filesToUpload = Client.whatsInTheFolder(UPLOADNLESG2);
                    Client.uploadFile(listIPAddr[i], UPLOADNLESG2, filesToUpload);
                    
                    //INSTALL the uploaded files
                    for (int j = 0; j < filesToUpload.length; j++) {
                        wholeMessage = Client.installNLESG2(UPLOADNLESG2, filesToUpload[j], msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey);
                        Client.sendMessage(listIPAddr[i], wholeMessage);
                    }

                    //Init Install
                    Client.cmdInitInstall(listIPAddr[i]);
                    Client.NLESG2_isItInit(name); 
                    
                    //Reset
//                    Client.cmdReset(listIPAddr[i]);
//                    Client.NLESG2_isItLoaded(name);  
                    
                    //Uninstall
//                    for (int j = 0; j < filesToUpload.length; j++) {
//                        
//                        wholeMessage = Client.uninstallNLESG2(UPLOADNLESG2, filesToUpload[i], msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey);
//                        Client.sendMessage(listIPAddr[i], wholeMessage);
//                    }

                    //LIST FILES IN FTP
//                    Client.listFilesFTP(510);
                    
                }
                
            }
            //If there are more than one to audit
//            else {
//                listFileCode = Client.readFileCode(names[i]);
//                //If the EGNOS address is smaller than 255 (can be stored in 1 byte)
//                if (egnosAddresses[i] < 0xFF)
//                    destinationAddress = new byte[] {(byte)0x00,(byte)egnosAddresses[i]};
//                //If the EGNOS address is bigger than 255 (cannot be stored in 1 byte)
//                else
//                    destinationAddress = BigInteger.valueOf(egnosAddresses[i]).toByteArray();
//                
//                //Check asset mode
//                Client.multicastSocket(names[i]);
//                
//                //Manage asset mode
//                Client.manageModeNLESG2(listIPAddr[i], names[i]);
//                
//                //Upload all the files found in the folder
//                if (names[i].equals(nameNLESG2)) {
//                    System.out.println("UPLOADING NLESG2 FILES:");
//                    
//                    //Upload files found in the folder
//                    filesToUpload = Client.whatsInTheFolder(UPLOADNLESG2);
//                    Client.uploadFile(listIPAddr[i], UPLOADNLESG2, filesToUpload);
//                    
//                    //Install the uploaded files
//                    for (int j = 0; j < filesToUpload.length; j++) {
//                        wholeMessage = Client.installNLESG2(UPLOADNLESG2, filesToUpload[j], msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey);
//                        Client.sendMessage(listIPAddr[i], wholeMessage);
//                    }
//
//                    //Init Install
//                    Client.cmdInitInstall(listIPAddr[i]);
//                    Client.NLESG2_isItInit(names[i]); 
//                    
//                    //Reset
//                    Client.cmdReset(listIPAddr[i]);
//                    Client.NLESG2_isItLoaded(names[i]);
//                    
//                }
//            }
        }
        
    }
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        
        
        window.setVisible(true);
//        String hostName = "199.234.195.1";
//        int timer = 0;
        
        
        
//        System.out.println("Starting");
        
        //READ IP FILE
//        listIPAddr = Client.readIPFile();
        
        //FINAL LOOP
//        for (int i = 0 ; i < listIPAddr.length ; i++){
//            
//            System.out.println("\nDEPLOYING " + listIPAddr[i] + "\n");
////            
//            if (count == 1) {
////                listFileCode = Client.readFileCode(name);
//                
//                Client.verifyEgnosAddress();
//                
//                //Check asset mode
////                Client.multicastSocket(name);
//
//                //NLES G2: MANAGE MODE, UPLOAD, INSTALL, INIT INSTALL, RESET
//                if (name.equals(nameNLESG2)) {
//                    //Manage asset mode
////                    Client.manageModeNLESG2(listIPAddr[i], name);
//                    
////                    System.out.println("UPLOADING NLESG2 FILES:");
//                    
//                    //UPLOAD all files found in the folder
////                    filesToUpload = Client.whatsInTheFolder(UPLOADNLESG2);
////                    Client.uploadFile(listIPAddr[i], UPLOADNLESG2, filesToUpload);
//                    
//                    //INSTALL the uploaded files
////                    for (int j = 0; j < filesToUpload.length; j++) {
////                        wholeMessage = Client.installNLESG2(UPLOADNLESG2, filesToUpload[j], msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey);
////                        Client.sendMessage(listIPAddr[i], wholeMessage);
////                    }
//
//                    //Init Install
////                    Client.cmdInitInstall(listIPAddr[i]);
////                    Client.NLESG2_isItInit(name); 
//                    
//                    //Reset
////                    Client.cmdReset(listIPAddr[i]);
////                    Client.NLESG2_isItLoaded(name);  
//                    
//                    //Uninstall
////                    for (int j = 0; j < filesToUpload.length; j++) {
////                        
////                        wholeMessage = Client.uninstallNLESG2(UPLOADNLESG2, filesToUpload[i], msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey);
////                        Client.sendMessage(listIPAddr[i], wholeMessage);
////                    }
//
//                    //LIST FILES IN FTP
////                    Client.listFilesFTP(510);
//                    
//                }
////                
//            }
//            //If there are more than one to audit
////            else {
////                listFileCode = Client.readFileCode(names[i]);
////                //If the EGNOS address is smaller than 255 (can be stored in 1 byte)
////                if (egnosAddresses[i] < 0xFF)
////                    destinationAddress = new byte[] {(byte)0x00,(byte)egnosAddresses[i]};
////                //If the EGNOS address is bigger than 255 (cannot be stored in 1 byte)
////                else
////                    destinationAddress = BigInteger.valueOf(egnosAddresses[i]).toByteArray();
////                
////                //Check asset mode
////                Client.multicastSocket(names[i]);
////                
////                //Manage asset mode
////                Client.manageModeNLESG2(listIPAddr[i], names[i]);
////                
////                //Upload all the files found in the folder
////                if (names[i].equals(nameNLESG2)) {
////                    System.out.println("UPLOADING NLESG2 FILES:");
////                    
////                    //Upload files found in the folder
////                    filesToUpload = Client.whatsInTheFolder(UPLOADNLESG2);
////                    Client.uploadFile(listIPAddr[i], UPLOADNLESG2, filesToUpload);
////                    
////                    //Install the uploaded files
////                    for (int j = 0; j < filesToUpload.length; j++) {
////                        wholeMessage = Client.installNLESG2(UPLOADNLESG2, filesToUpload[j], msgType, flowType, headerLength, originAddress, destinationAddress, spare, sectionIdentifier, confirmationKey);
////                        Client.sendMessage(listIPAddr[i], wholeMessage);
////                    }
////
////                    //Init Install
////                    Client.cmdInitInstall(listIPAddr[i]);
////                    Client.NLESG2_isItInit(names[i]); 
////                    
////                    //Reset
////                    Client.cmdReset(listIPAddr[i]);
////                    Client.NLESG2_isItLoaded(names[i]);
////                    
////                }
////            }
//        }


    //END!
    }
    
}


/*****************************************************************************
 * Class Name : getResponse
 * Purpose : Receive the message response from the asset and analyze it.
 * *************************************************************************/
class getResponse implements Runnable {
    
    public final String output = "output.csv";
    private final Socket socket;
    BufferedReader reader;
    byte[] data = new byte[1024];
    int data_length;
    InputStream stream ;
    int responseCounter = 2;
    int normalAnswer = 57;
    int dataRequested = 92;
    int dataRequested_NLES = 88;
    
    boolean cmd_install = false;
    boolean inProgress = false;
    
    
    //XML
    Document doc;
    
    public getResponse(Socket s) throws IOException {
        socket=s;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        stream = s.getInputStream();
        
    }

    /*****************************************************************************
     * Name : setLog
     * Purpose : Redirect the output to a log file
     * Argument I/O: 
     * I/O Files: No input file
     * Returns :
     * 
     * @throws java.io.FileNotFoundException *************************************************************************/
    public void setLog() throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream("log.txt",true));
        System.setOut(out);
    }
    
    /*****************************************************************************
     * Name : resetStdOutput
     * Purpose : Redirect the output to the standard output
     * Argument I/O: 
     * I/O Files: No input file
     * Returns :
     *  *************************************************************************/
    public void resetStdOutput() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }
    
    /*****************************************************************************
     * Name : reduceData
     * Purpose : Reduce the version number
     * Argument I/O: Byte array containing the version number
     * I/O Files: No input file
     * Returns : 
     * 
     * @param data
     * @return 
     * *************************************************************************/
    public byte[] reduceData(byte [] data) {
        
        int start = 4;
        int dataLength = 32;
        byte[] reduced = new byte[dataLength];
        
        System.arraycopy(data, start, reduced, 0, dataLength);
        
        return reduced;
    }
    
    /*****************************************************************************
     * Name : hexToAscii
     * Purpose : Convert hexadecimal version number to String and remove invalid characters in XML
     * Argument I/O: Hex Byte array containing the version number
     * I/O Files: No input file
     * Returns : 
     * 
     * @param data
     * @return
     * *************************************************************************/
    public String hexToAscii(byte [] data) throws UnsupportedEncodingException, FileNotFoundException {
        
        //----------
        byte[] notInvalid = null;
        int invalidCounter = 0;
        //Remove 0x00 from the version number for XML
        
        //----------------------------1st approach : Replace 0x00 with 0x20 (space)
//        for (int i = 0; i<data.length ; i++){
//            if (data[i] == 0x00){
//                data[i] = 0x20;
//                invalidCounter++;
//            }
//        }
//        resetStdOutput();
//        System.out.println("Invalid characters removed: " + invalidCounter);
//        setLog();

        //----------------------------2nd approach : Cut the version number at 0x00
        //Get the index of the first encountered invalid char
        for (int i = 0; i<data.length ; i++){
            if (data[i] == 0x00){
                invalidCounter=i;
                break;
            }
        }
        
        //Declare a smaller byte array and copy only the valid part in it
        if (invalidCounter != 0){
            notInvalid = new byte[invalidCounter];
            System.arraycopy(data, 0, notInvalid, 0, invalidCounter);
        }
        
        //Print
        resetStdOutput();
        if (invalidCounter != 0)
            System.out.println("Invalid characters at index: " + invalidCounter);
        else
            System.out.println("No invalid character");
        setLog();
        
        //-----------
        String response2 = null;
        if (invalidCounter != 0)
            response2 = new String(notInvalid, "UTF-8");
        String response1 = new String(data, "UTF-8");
        
        
        if (invalidCounter != 0){
            resetStdOutput();
            System.out.println("Version number: " + response2);
            setLog();
            return response2;
        }
        else{
            resetStdOutput();
            System.out.println("Version number: " + response1);
            setLog();
            return response1;
        }
            
    }
    
    /*****************************************************************************
     * Name : writeVersion
     * Purpose : Write the version number in the output file
     * Argument I/O: String containing the version number
     * I/O Files: No input file
     * Returns : 
     * 
     * @param data
     * *************************************************************************/
//    public void writeVersion(String data) {
//
//        File file = new File(output);
//        try {
//            try (FileWriter writer = new FileWriter(file, true)) {
//                writer.write(data);
//                writer.flush();
//                writer.close();
//            }
//
//        } catch (IOException ex) {
//            System.out.println("Error while writing in the file");
//        }
//
//    }
    
    /*****************************************************************************
     * Name : writeRejected
     * Purpose : Write "REJECTED" in the output file when a command is rejected
     * Argument I/O:
     * I/O Files: No input file
     * Returns : 
     * 
     * *************************************************************************/
    public void writeRejected() {

        String rejected = "REJECTED";
        File file = new File(output);
        try {
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(rejected);
                writer.flush();
                writer.close();
            }

        } catch (IOException ex) {
            System.out.println("Error while writing in the file");
        }

    }
    
    /*****************************************************************************
     * Name : writeLineSeparator
     * Purpose : Write a line separator in the output file
     * Argument I/O: 
     * I/O Files: No input file
     * Returns : 
     * 
     * *************************************************************************/
//    public void writeLineSeparator() {
//        File file = new File(output);
//        try {
//            try (FileWriter writer = new FileWriter(file, true)) {
//                writer.write(System.lineSeparator());
//                writer.flush();
//                writer.close();
//            }
//
//        } catch (IOException ex) {
//            System.out.println("Error while writing in the file");
//        }
//    }
    
    
    /*****************************************************************************
     * Name : parseResponse
     * Purpose : Analyze the response message from the asset
     * Argument I/O: Byte array containing the message
     * I/O Files: No input file
     * Returns : 
     * 
     * @param data
     * *************************************************************************/
    public void parseResponse(byte[] data) throws UnsupportedEncodingException, FileNotFoundException, TransformerException {
        
        //Header
        byte msgType;
        byte flowType;
        byte[] headerLength = new byte[2];
        byte[] originAddress = new byte[2];
        byte[] destinationAddress = new byte[2];
        byte[] spare = new byte[4];
        int startVerNum = 0;
        int startMsgType = 1;
        int startFlowType = 2;
        int startDataLength = 3;
        int startHdrLength = 5;
        int startOrAddr = 7;
        int startDestAddr = 10;
        int startTimeStamp = 13;
        int startSpare = 19;
        int startCRC = 27;
        
        //Command
        byte sectionId;
        byte[] sectionLength = new byte[2];
        byte[] confirmKey = new byte[20];
        byte cmdId;
        byte ackType;
        byte ackValue;
        byte[] ackValueData = new byte[36];
        byte[] ackValueDataNLES = new byte[32];
        int startSectionId = 31;
        int startSectionLength = 32;
        int startConfirmKey = 34;
        int startCmdId = 54;
        int startAckType = 55;
        int startAckValue = 56;
        
        //NLES G2 INSTALL
        byte[] confKey_extracted = new byte[7];
        byte[] confKey_install = new byte[] {(byte) 0x49, (byte) 0x4e, (byte) 0x53, (byte) 0x54, (byte) 0x41, (byte) 0x4c, (byte) 0x4c}; //INSTALL
        
        String nameOfMsg;
        
        //ACKNOWLEDGEMENT TYPE
        byte reject = 0x02;
        byte start = 0x04;
        byte unsuccessful = 0x07;
        
        if(data_length == normalAnswer)
            nameOfMsg = "Response";
        else
            nameOfMsg = "Requested Data";
        
        
        
        //CHECK HERE IF THERE'S NO ERROR SINCE CCF COMMAND CHANGES
        udp_socket.EGNOS_Message response = new udp_socket.EGNOS_Message(nameOfMsg, false,false);
        
        
        System.out.println("Data length: " + data_length);
        
        System.out.println("");
        
        //Header copy
        msgType = data[startMsgType];
        flowType = data[startFlowType];
        System.arraycopy(data, startHdrLength, headerLength, 0, headerLength.length);
        System.arraycopy(data, startOrAddr, originAddress, 0, originAddress.length);
        System.arraycopy(data, startDestAddr, destinationAddress, 0, destinationAddress.length);
        System.arraycopy(data, startSpare, spare, 0, spare.length);
        
        //Command copy
        sectionId = data[startSectionId];
        System.arraycopy(data, startSectionLength, sectionLength, 0, sectionLength.length);
        System.arraycopy(data, startConfirmKey, confirmKey, 0, confirmKey.length);
        cmdId = data[startCmdId];
        ackType = data[startAckType];
        
        
       //If NLES G2 INSTALL repsonse, we are expecting 2 responses
       System.arraycopy(confirmKey, 0, confKey_extracted, 0, confKey_extracted.length);
        if (Arrays.equals(confKey_extracted, confKey_install)) {
            cmd_install = true;
        }
       
        
        response.EGNOS_Header = new udp_socket.EGNOS_StandardHeader(msgType, flowType, headerLength, originAddress, destinationAddress, spare);
        response.EGNOS_Header.versionNum = data[startVerNum];
        System.arraycopy(data, startDataLength, response.EGNOS_Header.dataLength, 0, response.EGNOS_Header.dataLength.length);
        System.arraycopy(data, startTimeStamp, response.EGNOS_Header.timeStamp, 0, response.EGNOS_Header.timeStamp.length);
        System.arraycopy(data, startCRC, response.EGNOS_Header.fullMessageCRC, 0, response.EGNOS_Header.fullMessageCRC.length);
        
//        response.EGNOS_Header.printHeader();
        
        //If the message is 57 bytes long (normal message answer)
        if (data_length == normalAnswer) {
            ackValue = data[startAckValue];
            response.EGNOS_Cmd_Resp = new EGNOS_Command_Response(sectionId, sectionLength, confirmKey, cmdId, ackType, ackValue);
            response.EGNOS_Cmd_Resp.printCmdResp();
            //If command rejected
//            if (response.EGNOS_Cmd_Resp.acknowledgementType == reject) {
//                System.out.println("COMMAND REJECTED REJECTED");
//                EGNOS_Deploy.rejectedCounter++;
//                writeRejected();
//                responseCounter--;
//            }
            
            //How is the command received by the asset
            switch (response.EGNOS_Cmd_Resp.acknowledgementType){
                case 0x02 :
                    System.out.println("COMMAND REJECTED");
                    System.exit(0);
                    break;
                case 0x03 :
                    System.out.println("COMMAND ILLEGAL");
                    System.exit(0);
                    break;
                case 0x04 :
                    System.out.println("COMMAND STARTED");
                    break;
                case 0x05 :
                    System.out.println("COMMAND IN PROGRESS");
                    inProgress = true;
                    break;
                case 0x06 :
                    System.out.println("COMMAND SUCCESSFUL");
                    break;
                case 0x07 :
                    System.out.println("COMMAND UNSUCCESSFUL");
                    break;
                default:
                    System.out.printf("UNKNOWN ACK TYPE %02X\n", response.EGNOS_Cmd_Resp.acknowledgementType);
                    break;
            }
        }
    }
    
    /*****************************************************************************
     * Name : splitResponse
     * Purpose : Split the response in two byte arrays when both messages are concatenated (happens from time to time)
     * Argument I/O: Byte array containing the response message
     * I/O Files: No input file
     * Returns : 
     * 
     * @param data
     * *************************************************************************/
    public void splitResponse(byte [] data) throws UnsupportedEncodingException, FileNotFoundException, TransformerException {
        
        System.out.println("DATA TOO BIG (" + data_length + " bytes) --------------------------------------------------------- SPLITTING RESPONSE\n\n\n");
        
        byte[] small = new byte[normalAnswer];
        byte[] big = new byte[dataRequested];
        
        //Copy first part of the message (regular response) in a small byte array (57 bytes long)
        System.arraycopy(data, 0, small, 0, normalAnswer);
        for (int i = 0; i < small.length; i++) {
            System.out.printf("%02x ", small[i]);
        }
        System.out.println("");
        
        //Copy second part of the message (data requested) in a big byte array (92 bytes long)
        System.arraycopy(data, normalAnswer, big, 0, dataRequested);
        for (int i = 0; i < big.length; i++) {
            System.out.printf("%02x ", big[i]);
        }
        System.out.println("");
        
        //Parse both messages
        data_length = normalAnswer;
        parseResponse(small);
        data_length = dataRequested;
        parseResponse(big);
    }
    
    /*****************************************************************************
     * Name : run
     * Purpose : Thread for receiving the response message
     * Argument I/O: None  
     * I/O Files: No input file
     * Returns : 
     * .
     * *************************************************************************/
    @Override
    public void run() {
        
        try {

            while ( ((data_length = stream.read(data)) != -1)) {
                System.out.println();
                System.out.printf("Server: ");
                
                for (int k = 0; k < data_length; k++) {
                    System.out.printf("%02X ", data[k]);
                }
                System.out.println();
                System.out.println();
                
                resetStdOutput();
                parseResponse(data);
                
                
                if (cmd_install) {
                    
                    if (inProgress){
                        //Expecting 3 answers from the asset
                        System.out.println("IN PROGRESS!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        inProgress = false;
                    }
                    else {
                        //Expecting 2 answers from the asset
                        responseCounter--;
                        Thread.sleep(500); 
                    }
                }
                else {
                    //Expecting 1 answer from the asset
                    responseCounter-=2;
                }
                
                //We are expecting 2/3 answers from the asset when INSTALL cmd sent
                if (responseCounter == 0) {
                    responseCounter = 2;
                    break;
                }
            }    
        } catch (IOException | InterruptedException | TransformerException ex) {
            Logger.getLogger(getResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
//class listenServer implements Runnable {
//
//    private final Socket socket;
//    InputStream stream;
//    BufferedReader reader;
//    int data_length;
//    byte[] data = new byte[1024];
//    
//    public listenServer(Socket s) throws IOException {
//        socket = s;
//        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        stream = s.getInputStream();                                                                                                                                                                                                                                               
//    }
//    
//    @Override
//    public void run() {
//        
//        String fromServer = null;
//        try {
//            while ( ((data_length = stream.read(data)) != -1)) {
//                System.out.println();
//                System.out.printf("Server: ");
//                
//                for (int k = 0; k < data_length; k++) {
//                    System.out.printf("%02X ", data[k]);
//                }
//                System.out.println();
//                
//            }  
//        
////            while ((fromServer = reader.readLine()) != null) {
////                System.out.println("Server: " + fromServer);
////            }
//        } catch (IOException ex) {
//            Logger.getLogger(listenServer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//}
