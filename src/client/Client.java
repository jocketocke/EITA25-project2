package client;
import tools.Request;

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import java.io.Console;
import java.security.cert.*;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class Client {

    public static void main(String[] args) throws Exception {
        String host = null;
        int port = -1;
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
        if (args.length < 2) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        try { /* get input parameters */
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }

        try { /* set up a key manager for client authentication */
            SSLSocketFactory factory = null;
            try {
                char[] password = System.console().readPassword("Enter password>");
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore ts = KeyStore.getInstance("JKS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                SSLContext ctx = SSLContext.getInstance("TLS");
                ks.load(new FileInputStream("client/patientkeystore"), password);  // keystore password (storepass)
                ts.load(new FileInputStream("client/clienttruststore"), password); // truststore password (storepass);
                kmf.init(ks, password); // user password (keypass)
                tmf.init(ts); // keystore can be used as truststore here
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                factory = ctx.getSocketFactory();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            System.out.println("\nsocket before handshake:\n" + socket + "\n");

            /*
             * send http request
             *
             * See SSLSocketClient.java for more information about why
             * there is a forced handshake here when using PrintWriters.
             */
            socket.startHandshake();

            SSLSession session = socket.getSession();

            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String subject = cert.getSubjectDN().getName();
            String issuer = cert.getIssuerDN().getName();
            String serial = cert.getSerialNumber().toString();
            System.out.println("certificate name (subject DN field) on certificate received from server:\n" + subject + "\n");
            System.out.println("certificate name (issuer DN field) on certificate received from server:\n" + issuer + "\n");
            System.out.println("certificate name (serial field) on certificate received from server:\n" + serial + "\n");
            System.out.println("socket after handshake:\n" + socket + "\n");
            System.out.println("secure connection established\n\n");
            System.out.println("Options: read, write, create, delete, quit \n");

            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;

            boolean run = true;
            while (run) {
                System.out.print(">");
                msg = read.readLine();
                switch (msg){
                    case "read":
                    case "delete":
                        System.out.print("Patient> ");
                        String person = read.readLine();
                        System.out.println("Sending request to server");
                        out.println(msg + "," + person);
                        out.flush();
                        waitForResponse(in);
                        break;
                    case "write":
                        System.out.print("Patient> ");
                        person = read.readLine();
                        System.out.println("Sending request to server");
                        out.println(msg + "," + person);
                        String recordsFromServer = in.readLine();
                        if(recordsFromServer.equals("Unauthorized user")){
                            break;
                        }
                        recordsFromServer = recordsFromServer.replaceAll(";", "\n");
                        String[] records = recordsFromServer.split(",");
                        int selected = 0;
                        if(records.length == 1){
                            System.out.println(records[0]);
                        }else{
                            for (int i = 0; i < records.length; i++) {
                                System.out.println(i + ": " + records[i]);
                                System.out.println("---------------------------------------");
                            }
                            System.out.println("Select>");
                            selected = Integer.parseInt(read.readLine());
                        }
                        System.out.println("> Write new log: ");
                        String log = read.readLine();
                        records[selected] = records[selected] + "\n" + log;
                        String outputRecordsToServer = "";
                        for (int i = 0; i < records.length; i++){
                            outputRecordsToServer = outputRecordsToServer + records[i] + ",";
                        }
                        outputRecordsToServer = outputRecordsToServer.replaceAll("\n", ";");// new line replacement
                        out.println(outputRecordsToServer);
                        out.flush();
                        waitForResponse(in);
                        break;
                    case "create":
                        System.out.print("Patient> ");
                        person = read.readLine();
                        System.out.print("Nurse> ");
                        String nurse = read.readLine();
                        System.out.print("Record> ");
                        String record = read.readLine();
                        System.out.println("Sending request to server");
                        out.println(msg + "," + person + "," + nurse + "," + record);
                        out.flush();
                        waitForResponse(in);
                        break;
                    case "quit":
                        run = false;
                        System.out.println("Exiting program");
                        break;
                    default:
                        System.out.println("Try: read, write, create, delete, quit.");
                        break;
                }
                System.out.println("returning to options\n");

            }
            in.close();
            out.close();
            read.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void waitForResponse(BufferedReader in) throws IOException {
        System.out.println("received '" + in.readLine() + "' from server\n");
    }

}
