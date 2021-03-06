package server;
import server.persons.Person;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

public class Server implements Runnable {
    private ServerSocket serverSocket = null;
    private static int numConnectedClients = 0;
    private static AuditLog auditLog = new AuditLog("audit.txt");

    private Person connectedPerson;

    private Map<String, LinkedList<MedicalRecord>> medicalRecords = new HashMap<String, LinkedList<MedicalRecord>>();

    public Server(ServerSocket ss) throws IOException {
        serverSocket = ss;
        newListener();
    }

    public void run() {
        try {
            SSLSocket socket=(SSLSocket)serverSocket.accept();
            newListener();
            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String subject = cert.getSubjectDN().getName();
            String issuer = cert.getIssuerDN().getName();
            String serial = cert.getSerialNumber().toString();
            connectedPerson = new Person(cert.getSubjectDN().getName());
            numConnectedClients++;
            System.out.println("client connected");
            System.out.println("client name (cert subject DN field): " + subject);
            System.out.println("client name (cert issuer DN field): " + issuer);
            System.out.println("client name (serial field): " + serial);
            System.out.println(numConnectedClients + " concurrent connection(s)\n");

            PrintWriter out = null;
            BufferedReader in = null;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientMsg = null;
            while ((clientMsg = in.readLine()) != null) {
                System.out.println("received '" + clientMsg + "' from client");
                String[] input = clientMsg.split(",");

                StringBuilder sb = new StringBuilder();

                LinkedList<MedicalRecord> records = medicalRecords.getOrDefault(input[1], new LinkedList<>());
                switch (input[0]) {
                    case "read" :
                        String s = readMedicalRecord(records);
                        sb = new StringBuilder(s);
                        if(sb.toString().contains(",")){
                            sb.append("Read records");
                        }else{
                            sb.append("Unauthorized user");
                        }
                        break;
                    case "create" :

                        MedicalRecord record = new MedicalRecord(input[1], input[3], connectedPerson, input[2], auditLog);
                        if(record.exists()) {
                            if (medicalRecords.containsKey(input[1])) {
                                records.add(record);
                            } else {
                                records = new LinkedList<MedicalRecord>();
                                records.add(record);
                                medicalRecords.put(input[1], records);
                            }
                            sb.append("Created record");
                        }else{
                            sb.append("Unauthorized user");
                        }
                        break;
                    case "write":
                        //input[1] = patient
                        out.println(readMedicalRecord(records));
                        String changedRecord = in.readLine();
                        changedRecord = changedRecord.replaceAll(";", "\n");
                        String[] changedRecords = changedRecord.split(",");
                        int index = 0;
                        boolean successful = false;
                        for (MedicalRecord temp : records) {
                            String medicalData = temp.readMedicalRecord(connectedPerson);
                            if(medicalData.equals("Unauthorized user")){
                                continue;
                            }
                            if(temp.writeMedicalRecord(connectedPerson, changedRecords[index]) && !successful){
                                successful = true;
                            }
                            index++;
                        }
                        medicalRecords.put(input[1], records);
                        if(successful){
                            sb.append("Wrote to records");
                        }else{
                            sb.append("Unauthorized user");
                        }
                        break;
                    case "delete" :
                        if(connectedPerson.getType().equals("Government")) {
                            medicalRecords.remove(input[1]);
                            auditLog.log(connectedPerson, true, "delete");
                            sb.append("Deleted records");
                        } else {
                            auditLog.log(connectedPerson, false, "delete");
                            sb.append("Unauthorized user");
                        }
                }

                String returnMessage = sb.toString();
                System.out.print("sending '" + returnMessage  + "' to client...");
                out.println(returnMessage);
                out.flush();
                System.out.println("done\n");
            }
            in.close();
            out.close();
            socket.close();
            numConnectedClients--;
            System.out.println("client disconnected");
            System.out.println(numConnectedClients + " concurrent connection(s)\n");
        } catch (IOException e) {
            System.out.println("Client died: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    private String readMedicalRecord(LinkedList<MedicalRecord> records) {
        StringBuilder tempreader = new StringBuilder();
        for (MedicalRecord temp : records) {
            String medicalData = temp.readMedicalRecord(connectedPerson);
            if(medicalData.equals("Unauthorized user")){
                continue;
            }
            tempreader.append(medicalData);
            tempreader.append(","); //Split records.
        }
        String s = tempreader.toString();
        s = s.replaceAll("\n", ";");// new line replacement
        return s;
    }

    //Replace \n with ;
    private String replaceNewLine(String input){
        return input.replaceAll("\n", ";");
    }

    //Replace ; with \n
    private String returnNewLine(String input){
        return input.replaceAll(";", "\n");
    }


    private void newListener() { (new Thread(this)).start(); } // calls run()

    public static void main(String args[]) {
        auditLog = new AuditLog("audit.txt");
        System.out.println("\nServer Started\n");
        int port = -1;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        String type = "TLS";
        try {
            ServerSocketFactory ssf = getServerSocketFactory(type);
            ServerSocket ss = ssf.createServerSocket(port);
            ((SSLServerSocket)ss).setNeedClientAuth(true); // enables client authentication
            new Server(ss);
        } catch (IOException e) {
            System.out.println("Unable to start Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ServerSocketFactory getServerSocketFactory(String type) {
        if (type.equals("TLS")) {
            SSLServerSocketFactory ssf = null;
            try { // set up key manager to perform server authentication
                SSLContext ctx = SSLContext.getInstance("TLS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore ts = KeyStore.getInstance("JKS");
                char[] password = "password".toCharArray();

                ks.load(new FileInputStream("src/server/serverkeystore"), password);  // keystore password (storepass)
                ts.load(new FileInputStream("src/server/servertruststore"), password); // truststore password (storepass)
                kmf.init(ks, password); // certificate password (keypass)
                tmf.init(ts);  // possible to use keystore as truststore here
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }
}
