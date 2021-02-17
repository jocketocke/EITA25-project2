package server;

import server.persons.Person;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {
    private String patient;
    private String division;
    private List<String> authorizedUsers = new ArrayList<>();
    private String medicalData;
    private AuditLog auditLog;

    public MedicalRecord(String patient, String medicalData, Person doctor, Person nurse, AuditLog auditLog){
        try {
            if (doctor.getType().equals("Doctor")) {
                this.patient = patient;
                this.division = doctor.getDivision();
                if (nurse != null) {
                    this.authorizedUsers.add(nurse.getName());
                }
                this.medicalData = medicalData;
                this.auditLog = auditLog;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String readMedicalRecord(Person person){
        if(person.getName().equals(patient) || person.getType().equals("government")){
            auditLog.log(person, true, "getMedicalRecord");
            return medicalData;
        }else if(person.getDivision().equals(division) && !person.getType().equals("patient")){
            auditLog.log(person, true, "getMedicalRecord");
            return medicalData;
        }else if(authorizedUsers.contains(person.getName())){
            auditLog.log(person, true, "getMedicalRecord");
            return medicalData;
        }else{
            auditLog.log(person, false, "getMedicalRecord");
            return "Unauthorized user";
        }
    }

    public void writeMedicalRecord(Person person, String medicalData){
        if(person.getDivision().equals(division) && !person.getType().equals("patient") && !person.getType().equals("government")){
            auditLog.log(person, true, "writeMedicalRecord");
            this.medicalData = medicalData;
        }else{
            auditLog.log(person, false, "writeMedicalRecord");
        }
    }
}
