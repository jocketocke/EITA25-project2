package server;

import server.persons.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {
    private String patient;
    private String division;
    private List<String> authorizedUsers = new ArrayList<>();
    private String medicalData;
    private AuditLog auditLog;

    public MedicalRecord(String patient, String medicalData, Person doctor, String nurse, AuditLog auditLog){
        try {
            if (doctor.getType().equals("Doctor")) {
                this.patient = patient;
                this.division = doctor.getDivision();
                this.authorizedUsers.add(doctor.getName());
                if (nurse != null) {
                    this.authorizedUsers.add(nurse);
                }
                this.medicalData = medicalData;
                this.auditLog = auditLog;
                auditLog.log(doctor, true, "create");
            } else {
                auditLog.log(doctor, false, "create");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean exists(){
        if(auditLog == null){
            return false;
        }
        return true;
    }

    public String readMedicalRecord(Person person){
        if(checkAuthorized(person, "read")){
            auditLog.log(person, true, "readMedicalRecord");
            return medicalData;
        }else{
            auditLog.log(person, false, "readMedicalRecord");
            return "Unauthorized user";
        }
    }

    private boolean checkAuthorized(Person person, String request){
        switch (request){
            case "read":
                return (person.getName().equals(patient) || person.getType().equals("Government"))
                        || (person.getDivision().equals(division) && !person.getType().equals("Patient"))
                        || (authorizedUsers.contains(person.getName()));
            case "write":
                return authorizedUsers.contains(person.getName());
            case "delete":
                return person.getType().equals("Government");
        }
        return false;
    }

    public boolean writeMedicalRecord(Person person, String medicalData){
        if(checkAuthorized(person, "write")){
            auditLog.log(person, true, "writeMedicalRecord");
            this.medicalData = medicalData;
            return true;
        }else{
            try {
                throw new IOException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            auditLog.log(person, false, "writeMedicalRecord");
            return false;
        }
    }
}
