package server;

import server.persons.Person;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

public class AuditLog {
    private String filename;
    private StringBuilder stringBuilder;

    public AuditLog(String filename){
        this.filename = filename;
        try{
            File auditLog = new File(filename);
            Scanner reader = new Scanner(auditLog);
            stringBuilder = new StringBuilder();
            while(reader.hasNextLine()){
                stringBuilder.append(reader.nextLine() + "\n");
            }
            reader.close();
            System.out.println(stringBuilder.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        AuditLog a = new AuditLog("src/server/audit.txt");
    }

    public void log(Person person, boolean bool, String s){
    }
}
