package server;

import server.persons.Person;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.time.LocalDateTime;

public class AuditLog {
    private String filename;
    private StringBuilder stringBuilder;

    public AuditLog(String filename){
        this.filename = filename;
    }

    public void log(Person person, boolean bool, String s){
        try{
            File auditLog = new File("src/server/" + filename);
            Scanner reader = new Scanner(auditLog);
            stringBuilder = new StringBuilder();
            while(reader.hasNextLine()){
                stringBuilder.append(reader.nextLine() + "\n");
            }
            reader.close();
            System.out.println("Fetched from auditLog");

            if(bool) {
                stringBuilder.append(person.getName() + " called " + s + " at " + LocalDateTime.now() + "\n");
            } else {
                stringBuilder.append(person.getName() + " tried to call " + s + " but failed at " + LocalDateTime.now() + "\n");
            }

            FileWriter writer = new FileWriter("src/server/" + filename);
            writer.write(stringBuilder.toString());
            writer.close();
            System.out.println("Wrote to auditLog");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
