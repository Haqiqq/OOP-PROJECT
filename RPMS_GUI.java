package org.rpms;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.File;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.*;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;



// -----------------------------------------
// ---------- USER(BASE CLASS) -------------
class User{
    private int id;
    private String name;
    private String email;
    private String password;
    private String address;
    private String phoneNumber;
    private String userType;

    public User(int id, String name, String email, String password, String address, String phoneNumber, String userType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
    }

    // getter methods
    public int getId(){ return id;}
    public String getName(){ return name;}
    public String getEmail(){ return email;}
    public String getPassword(){ return password;}
    public String getAddress(){ return address;}
    public String getPhoneNumber(){ return phoneNumber;}
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    // Setter methods (ADD THESE)
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber;
    }

    //override toString method
    @Override
    public String toString() {
        return "Id: " + id + "\nName: " + name +"\nemail: "+ email + "\npassword" + password+ "\naddress: "+ address + "\nPhoneNumber: "+ phoneNumber;
    }
}

// -----------------------------------------
// -------- PATIENT(DERIVED CLASS)----------
// -----------------------------------------
class Patient extends User {
    private String gender;
    private String emergencyContact;
    private ArrayList<VitalSign> vital_signs;
    private ArrayList<Appointment> appointments;
    private MedicalHistory medical_history;
    private Doctor assignedDoctor;  // New field

    public Patient(int id, String name, String email, String password, String address, String gender, String phoneNumber, String emergencyContact,String userType ) {
        super(id, name, email, password, address, phoneNumber,userType);
        this.gender = gender;
        this.emergencyContact = emergencyContact;
        this.vital_signs = new ArrayList<>();
        this.appointments = new ArrayList<>();
        this.medical_history = new MedicalHistory();
        this.assignedDoctor = null;
    }

    // Add these setter methods for Patient-specific fields
    public void setGender(String gender) { this.gender = gender; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public void setAssignedDoctor(Doctor doctor) { this.assignedDoctor = doctor; }

    // Setters for collections
    public void setVitalSigns(ArrayList<VitalSign> vital_signs) { this.vital_signs = vital_signs; }
    public void setAppointments(ArrayList<Appointment> appointments) { this.appointments = appointments; }
    public void setMedicalHistory(MedicalHistory medical_history) { this.medical_history = medical_history; }

    // CSV methods
    public String toCSV() {
        return getId() + "," +
                getName() + "," +
                getEmail() + "," +
                getPassword() + "," +
                getAddress() + "," +
                gender + "," +
                getPhoneNumber()+ "," +
                emergencyContact + "," +
                (assignedDoctor != null ? assignedDoctor.getId() : "");
    }

    public static Patient fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        Patient patient = new Patient(
                Integer.parseInt(parts[0]),   // id
                parts[1],                     // name
                parts[2],                     // email
                parts[3],                     // password
                parts[4],                     // address
                parts[5],                     // gender
                parts[6],                     // phoneNumber
                parts[7],
                parts[8]// emergencyContact
        );
        return patient;
    }

    // getter methods
    public String getGender() { return gender; }
    public String getEmergencyContact() { return emergencyContact; }
    public ArrayList<VitalSign> getVitalSign() { return vital_signs; }
    public MedicalHistory getMedicalHistory() { return medical_history; }
    public ArrayList<Appointment> getAppointment() { return appointments; }
    public Doctor getAssignedDoctor() { return assignedDoctor; }
    public boolean hasAssignedDoctor() { return assignedDoctor != null; }

    // planAppointments
    public void planAppointments(Appointment appointment) {
        appointments.add(appointment);
    }

    // add vital signs
    public void addVitalSign(VitalSign vital_sign) {
        vital_signs.add(vital_sign);
    }

    // add feedbacks to patients medical history
    public void addFeedbackToMedHistory(Feedback feedback) {
        medical_history.addFeedback(feedback);
    }

    public void getDoctorFeedback(Feedback feedback) {
        System.out.println("\nFeedback given: " + feedback.getComments());
    }

    public void cancelAppointments(Integer appointmentNumber) {
        appointments.remove(appointmentNumber);
        System.out.println("appointment removed");
    }
}

// -----------------------------------------
// -------- Doctor(DERIVED CLASS)----------
// -----------------------------------------
class Doctor extends User {
    private String doctorSpeciality;
    private ArrayList<Patient> patients;
    private Administrator administrator;  // Add this field


    // Add this getter method

    public Doctor(int id, String name, String email, String password,String address, String doctorSpeciality, String phoneNumber,String userType) {
        super(id, name, email, password, address, phoneNumber, userType);
        this.doctorSpeciality = doctorSpeciality;
        this.patients = new ArrayList<>();
    }


    // add patient in the patient list of doctor
    public void addPatient(Patient patient) {
        if (!patients.contains(patient)) {
            patients.add(patient);
        } else {
            System.out.println("Patient already exists in the doctor's list.");
        }
    }


    // displaying patient data
    public void displayPatientData(Patient patient) {
        System.out.println("Patient Data for: " + patient.getName());
        System.out.println("Gender: " + patient.getGender());
        System.out.println("-----------------------------");
        for (Patient names: patients){
            System.out.println("Patients list: "+ names.getName());
        }
    }

    // getting vitalSigns of given patient
    public void displayPatientVitalSign(Patient patient) {
        System.out.println("\nVital Signs:");
        for (VitalSign vitalsign : patient.getVitalSign()) {
            System.out.println("\nPulse: " + vitalsign.getPulse() +  ",\nBlood Pressure: " + vitalsign.getBloodPressure()+ "\nBMI: "+ vitalsign.getbmi()+ "\nTemperature: "+ vitalsign.getTemp()+ "\nOxygen Saturation: "+ vitalsign.getOxygenSaturation()+ "Respiratory Rate: " + vitalsign.getRespiratoryRate());
        }
    }

    public void provideFeedback(Patient patient ,String comments, Prescription prescription) {
        Feedback feed_back=new Feedback(this, comments, prescription);
        patient.addFeedbackToMedHistory(feed_back);
    }

    //---------------csv
    public String toCSV() {
        return getId() + "," + getName() + "," + getEmail() + "," + getPassword() + "," + getAddress() + "," + doctorSpeciality + "," + getPhoneNumber();
    }

    public static Doctor fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");

        // Assuming the CSV follows the structure: id, name, email, password, address, doctorSpeciality, phoneNumber
        return new Doctor(
                Integer.parseInt(parts[0]),   // id
                parts[1],                     // name
                parts[2],                     // email
                parts[3],                     // password
                parts[4],                     // address
                parts[5],                     // doctorSpeciality
                parts[6],
                parts[7]// phoneNumber
        );
    }

    public Administrator getAdministrator() {
        return administrator;
    }

    // Add this setter method
    public void setAdministrator(Administrator administrator) {
        this.administrator = administrator;
    }

    // Getters
    public String getDoctorSpeciality() {
        return doctorSpeciality;
    }
    public ArrayList<Patient> getPatients() {
        return patients;
    }
}

// -----------------------------------------
// ---- ADMINISTRATOR(DERIVED CLASS)--------
// -----------------------------------------
class Administrator extends User {
    private ArrayList<Doctor> doctors;
    private ArrayList<Patient> patients;
    private String department;
    private ArrayList<Appointment> appointments;
    private ArrayList<Feedback> feedbacks;
    private ArrayList<Prescription> prescriptions;
    private ArrayList<MedicalHistory> medicalHistories;
    private ArrayList<VitalSign> vitals;
    private ArrayList<Integer> vitalPatientIds;
    private ArrayList<DoctorPatientAssignment> assignments;

    private class DoctorPatientAssignment {
        Doctor doctor;
        Patient patient;
        String assignmentDate;

        DoctorPatientAssignment(Doctor doctor, Patient patient) {
            this.doctor = doctor;
            this.patient = patient;

            // Use UTC timezone
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.assignmentDate = sdf.format(new Date());
        }

        public String toCSV() {
            return doctor.getId() + "," + patient.getId() + "," + assignmentDate;
        }
    }

    public Administrator(int id, String name, String email, String password, String address, String department, String phoneNumber,String userType) {
        super(id, name, email, password, address, phoneNumber, userType);
        this.doctors = new ArrayList<>();
        this.patients = new ArrayList<>();
        this.department = department;
        this.feedbacks = new ArrayList<>();
        this.appointments = new ArrayList<>();
        this.prescriptions = new ArrayList<>();
        this.medicalHistories = new ArrayList<>();
        this.vitals = new ArrayList<>();
        this.vitalPatientIds = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    // Hospital details method
    public void hospitalDetails() {
        System.out.println("-------Hospital Details-------:");
        System.out.println("Total Doctors: " + doctors.size());
        System.out.println("Total Patients: " + patients.size());
    }

    // Adding objects methods
    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
    }

    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }

    public void addMedicalHistory(MedicalHistory medicalHistory) {
        medicalHistories.add(medicalHistory);
    }

    public void addVitals(VitalSign vital) {
        vitals.add(vital);
    }

    // Doctor-Patient Assignment methods with UTC timestamps
    public void assignDoctorToPatient(Doctor doctor, Patient patient) {
        removeAssignment(patient); // Remove any existing assignment
        assignments.add(new DoctorPatientAssignment(doctor, patient));
        patient.setAssignedDoctor(doctor); // Update the patient object
    }

    public Doctor getAssignedDoctor(Patient patient) {
        for (DoctorPatientAssignment assignment : assignments) {
            if (assignment.patient.getId() == patient.getId()) {
                return assignment.doctor;
            }
        }
        return null;
    }

    public ArrayList<Patient> getPatientsForDoctor(Doctor doctor) {
        ArrayList<Patient> doctorsPatients = new ArrayList<>();
        for (DoctorPatientAssignment assignment : assignments) {
            if (assignment.doctor.getId() == doctor.getId()) {
                doctorsPatients.add(assignment.patient);
            }
        }
        return doctorsPatients;
    }

    public void removeAssignment(Patient patient) {
        assignments.removeIf(assignment -> assignment.patient.getId() == patient.getId());
        patient.setAssignedDoctor(null); // Clear the patient's doctor reference
    }

    // Find methods
    public Patient findPatientById(int id) {
        for (Patient p : patients) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public Doctor findDoctorById(int id) {
        for (Doctor d : doctors) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    // Getters
    public ArrayList<Doctor> getDoctors() {
        return doctors;
    }

    public ArrayList<Patient> getPatients() {
        return patients;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    // CSV Data management with proper UTC handling
    public void saveAllData() {
        // Save Patients
        ArrayList<String> patientLines = new ArrayList<>();
        for (Patient p : patients) {
            patientLines.add(p.toCSV());
        }
        CSVManager.saveToCSV(patientLines, "data/patients.csv");

        // Save Doctors
        ArrayList<String> doctorLines = new ArrayList<>();
        for (Doctor d : doctors) {
            doctorLines.add(d.toCSV());
        }
        CSVManager.saveToCSV(doctorLines, "data/doctors.csv");

        // Save Appointments
        ArrayList<String> lines = new ArrayList<>();
        for (Appointment a : appointments) {
            lines.add(a.toCSV());
        }
        CSVManager.saveToCSV(lines, "data/appointments.csv");

        // Save Feedbacks
        lines.clear();
        for (Patient p : patients) {
            for (Feedback f : p.getMedicalHistory().getFeedback()) {
                lines.add(f.toCSV(p.getId()));
            }
        }
        CSVManager.saveToCSV(lines, "data/feedback.csv");

        // Save Vitals
        lines.clear();
        for (Patient p : patients) {
            for (VitalSign vs : p.getVitalSign()) {
                lines.add(p.getId() + "," + vs.toCSV());
            }
        }
        CSVManager.saveToCSV(lines, "data/vitals.csv");

        // Save Doctor-Patient Assignments
        lines.clear();
        for (DoctorPatientAssignment assignment : assignments) {
            lines.add(assignment.toCSV());
        }
        CSVManager.saveToCSV(lines, "data/assignments.csv");
    }

    public void loadAllData() {
        // Load Patients
        for (String line : CSVManager.loadFromCSV("data/patients.csv")) {
            patients.add(Patient.fromCSV(line));
        }

        // Load Doctors
        for (String line : CSVManager.loadFromCSV("data/doctors.csv")) {
            doctors.add(Doctor.fromCSV(line));
        }

        // Load Appointments
        for (String line : CSVManager.loadFromCSV("data/appointments.csv")) {
            Appointment a = Appointment.fromCSV(line, this);
            if (a != null) {
                appointments.add(a);
            }
        }

        // Load Vitals
        for (String line : CSVManager.loadFromCSV("data/vitals.csv")) {
            String[] p = line.split(",", 2);
            int pid = Integer.parseInt(p[0]);
            VitalSign vs = VitalSign.fromCSV(p[1]);
            Patient pt = findPatientById(pid);
            if (pt != null) pt.addVitalSign(vs);
        }

        // Load Feedback + inline Prescription
        for (String line : CSVManager.loadFromCSV("data/feedback.csv")) {
            String[] parts = line.split(",", 5);
            int pid = Integer.parseInt(parts[0]);
            int did = Integer.parseInt(parts[1]);
            String comments = parts[2];
            String med = parts[3];
            String freq = parts[4];

            Patient pt = findPatientById(pid);
            Doctor dc = findDoctorById(did);
            Prescription pr = new Prescription(med, freq);
            Feedback fb = new Feedback(dc, comments, pr);
            if (pt != null) pt.addFeedbackToMedHistory(fb);
        }

        // Load Doctor-Patient Assignments
        for (String line : CSVManager.loadFromCSV("data/assignments.csv")) {
            String[] parts = line.split(",");
            int doctorId = Integer.parseInt(parts[0]);
            int patientId = Integer.parseInt(parts[1]);

            Doctor doctor = findDoctorById(doctorId);
            Patient patient = findPatientById(patientId);

            if (doctor != null && patient != null) {
                assignments.add(new DoctorPatientAssignment(doctor, patient));
                patient.setAssignedDoctor(doctor); // Set the doctor reference in the patient object
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + "\nDepartment: " + department;
    }
}


// -----------------------------------------
// ----------- VITALSDATABASE---------------
// -----------------------------------------
class VitalDataBase {
    private ArrayList<VitalSign> vital_sign;

    public VitalDataBase(){
        vital_sign= new ArrayList<>();
    }

    // adding vital sign
    public void add_vital_sign(VitalSign vital_signs){
        vital_sign.add(vital_signs);
    }

    public void remove_vital_sign(VitalSign vital_sign) {
        this.vital_sign.remove(vital_sign);
    }

    //getter
    public ArrayList<VitalSign> getVitalSign(){
        return vital_sign;
    }
}


// -----------------------------------------
// ---------------- VITALSIGN---------------
// -----------------------------------------
class VitalSign {
    private double pulse;
    private double respiratory_rate;
    private String blood_pressure;
    private double oxygen_saturation;
    private double temperature;
    private double bmi;
    private LocalDate date;
    private String notes;

    public VitalSign(double pulse, double respiratory_rate, String blood_pressure, double oxygen_saturation, double temperature, double bmi) {
        this.pulse = pulse;
        this.respiratory_rate = respiratory_rate;
        this.blood_pressure = blood_pressure;
        this.oxygen_saturation = oxygen_saturation;
        this.temperature = temperature;
        this.bmi = bmi;
        this.date = date;
        this.notes = notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    //--------csv

    public String toCSV() {
        return pulse + "," + respiratory_rate + "," + oxygen_saturation + "," +
                temperature + "," + blood_pressure + "," + bmi + "," +
                (notes != null ? notes.replace(",", ";") : "");
    }

    // Update fromCSV method to handle notes
    public static VitalSign fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        double pulse = Double.parseDouble(parts[0]);
        double respiratoryRate = Double.parseDouble(parts[1]);
        double oxygenSaturation = Double.parseDouble(parts[2]);
        double temperature = Double.parseDouble(parts[3]);
        String bloodPressure = parts[4];
        double bmi = Double.parseDouble(parts[5]);

        VitalSign vs = new VitalSign(
                pulse,
                respiratoryRate,
                bloodPressure,
                oxygenSaturation,
                temperature,
                bmi
        );

        // Set notes if present in CSV
        if (parts.length > 6) {
            vs.setNotes(parts[6].replace(";", ",")); // Convert semicolons back to commas
        }

        return vs;
    }

    // getters
    public double getPulse(){
        return pulse;
    }
    public double getRespiratoryRate(){
        return respiratory_rate;
    }
    public double getTemp(){
        return temperature;
    }
    public String getBloodPressure(){
        return blood_pressure;
    }
    public double getOxygenSaturation(){
        return oxygen_saturation;
    }
    public double getbmi(){
        return bmi;
    }
    // These methods should be in the VitalSign class
    public LocalDate getDate() {
        return date;
    }
    public String getNotes() {
        return notes != null ? notes : "";
    }
    public double getTemperature() {
        return temperature;
    }


}


// -----------------------------------------
// ----------- PRESCRIPTION---------------
// -----------------------------------------
class Prescription{
    private String medicine;
    private String frequency;

    public Prescription(String medicine, String frequency){
        this.medicine=medicine;
        this.frequency=frequency;
    }

    //----------csv
    public String toCSV() {
        return medicine + "," + frequency;
    }

    public static Prescription fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        return new Prescription(
                parts[0],  // medicine
                parts[1]   // frequency
        );
    }

    //getters
    public String getMedicine (){
        return medicine;
    }
    public String getFrequency(){
        return frequency;
    }
}



// -----------------------------------------
// ------------------FEEDBACK---------------
// -----------------------------------------
class Feedback{
    private String comments;
    private Prescription prescription;
    private Doctor doctor;

    public Feedback(Doctor doctor, String comments, Prescription prescription){
        this.comments=comments;
        this.prescription=prescription;
        this.doctor=doctor;
    }

    //---------csv
// in Feedback.java
    public String toCSV(int patientId) {
        return patientId + ","
                + doctor.getId() + ","
                + comments + ","
                + prescription.getMedicine() + ","
                + prescription.getFrequency();
    }


    public static Feedback fromCSV(String csvLine, Administrator admin) {
        try {
            String[] parts = csvLine.split(",");

            Doctor doctor = admin.findDoctorById(Integer.parseInt(parts[0]));
            String comments = parts[1];
            String medicine = parts[2];
            String frequency = parts[3];

            Prescription prescription = new Prescription(medicine, frequency);

            return new Feedback(doctor, comments, prescription);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    //gettters
    public Doctor getDoctor(){
        return doctor;
    }
    public Prescription getPrescription(){
        return prescription;
    }
    public String getComments(){
        return comments;
    }
}


// -----------------------------------------
// ----------MEDICAL-HISTORY----------------
// -----------------------------------------
class MedicalHistory{
    private ArrayList<Feedback> feed_back;

    public MedicalHistory(){
        feed_back=new ArrayList<>();
    }

    // Method to add feedback
    public void addFeedback(Feedback feedbacks) {
        feed_back.add(feedbacks);
    }
    //--------------csv
    public String toCSV() {
        String csv = "";
        for (int i = 0; i < feed_back.size(); i++) {
            Feedback fb = feed_back.get(i);
            csv += fb.getDoctor().getId() + ";"
                    + fb.getComments() + ";"
                    + fb.getPrescription().getMedicine() + ";"
                    + fb.getPrescription().getFrequency();

            if (i != feed_back.size() - 1) {
                csv += "|"; // add | between feedbacks, but not after the last one
            }
        }
        return csv;
    }

    public static MedicalHistory fromCSV(String csvLine, Administrator admin) {
        try {
            MedicalHistory history = new MedicalHistory();

            if (!csvLine.isEmpty()) {
                String[] feedbackParts = csvLine.split("\\|");
                for (String feedbackData : feedbackParts) {
                    String[] fields = feedbackData.split(";");

                    int doctorId = Integer.parseInt(fields[0]);
                    String comments = fields[1];
                    String medicine = fields[2];
                    String frequency = fields[3];

                    Doctor doctor = admin.findDoctorById(doctorId);
                    Prescription prescription = new Prescription(medicine, frequency);
                    Feedback feedback = new Feedback(doctor, comments, prescription);

                    history.addFeedback(feedback);
                }
            }

            return history;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //getter
    public ArrayList<Feedback> getFeedback(){
        return feed_back;
    }
}


// -----------------------------------------
// ----------APPOINTMENT--------------------
// ----------------------------------------

class Appointment {

    private Date appointmentDate;
    private Doctor doctor;
    private Patient patient;
    private boolean reminderSent = false;
    private String reason;
    private String status;
    private String createdAt;
    private String createdBy;
    private String lastModified;
    private String modifiedBy;

    public Appointment(Date appointmentDate, Patient patient, Doctor doctor) {
        this.appointmentDate = appointmentDate;
        this.doctor = doctor;
        this.patient = patient;
        this.status = "Pending";
        this.createdAt = "2025-05-10 16:29:02"; // Current UTC time
        this.createdBy = "Haqiqq"; // Current user
        this.lastModified = this.createdAt;
        this.modifiedBy = this.createdBy;
    }

    public void showAppointmentDetails() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        System.out.println("------Appointment details-------");
        System.out.println("Patient Name: " + patient.getName());
        System.out.println("Doctor Name: " + doctor.getName());
        System.out.println("Appointment Date: " + sdf.format(appointmentDate));
        System.out.println("Status: " + status);
        if (reason != null) {
            System.out.println("Reason: " + reason);
        }
    }

    public String toCSV() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return sdf.format(appointmentDate) + "," + patient.getId() + "," + doctor.getId();
    }

    public static Appointment fromCSV(String csvLine, Administrator admin) {
        if (csvLine == null || csvLine.isBlank()) {
            return null;
        }

        try {
            String[] parts = csvLine.split(",", 3);
            if (parts.length < 3) {
                return null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            Date date = sdf.parse(parts[0].trim());

            int patientId = Integer.parseInt(parts[1].trim());
            int doctorId = Integer.parseInt(parts[2].trim());

            Patient patient = admin.findPatientById(patientId);
            Doctor doctor = admin.findDoctorById(doctorId);
            if (patient == null || doctor == null) {
                return null;
            }

            return new Appointment(date, patient, doctor);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
        this.lastModified = "2025-05-10 16:29:02";
        this.modifiedBy = "Haqiqq";
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    // New getters and setters
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
        this.lastModified = "2025-05-10 16:29:02";
        this.modifiedBy = "Haqiqq";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.lastModified = "2025-05-10 16:29:02";
        this.modifiedBy = "Haqiqq";
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }
}
// -----------------------------------------
// -------APPOINTMENT-SCHEDULING------------
// -----------------------------------------
class AppointmentManager{
    private ArrayList<Appointment> appointment;

    public AppointmentManager(){
        appointment=new ArrayList<>();
    }

    public void bookAppointment(Appointment appointments) {
        appointment.add(appointments);
    }

    public void cancelAppointment(Appointment appointments){
        appointment.remove(appointments);
    }
    public ArrayList<Appointment> getAppointments(){
        return appointment;
    }
}



// -----------------------------------------
// ----------module 2--------------------
// -----------------------------------------
class ChatServer {
    private ArrayList<String> messages;
    private ArrayList<PrivateChat> privateChats;

    private class PrivateChat {
        String user1;
        String user2;
        ArrayList<String> chatHistory;

        PrivateChat(String user1, String user2) {
            this.user1 = user1;
            this.user2 = user2;
            this.chatHistory = new ArrayList<>();
        }
    }

    public ChatServer() {
        messages = new ArrayList<>();
        privateChats = new ArrayList<>();
    }

    // Preserve original method
    public void broadcastMessage(String sender, String message) {
        String fullMessage = sender + ": " + message;
        messages.add(fullMessage);
        System.out.println(fullMessage);
    }

    // Preserve original method
    public ArrayList<String> getChatHistory() {
        return messages;
    }

    // Essential new methods
    public void sendPrivateMessage(String sender, String receiver, String message) {
        String fullMessage = String.format("[2025-05-10 15:00:45] %s: %s", sender, message);
        PrivateChat chat = findOrCreatePrivateChat(sender, receiver);
        chat.chatHistory.add(fullMessage);
        System.out.println("Private: " + fullMessage);
    }

    public ArrayList<String> getPrivateChatHistory(String user1, String user2) {
        PrivateChat chat = findPrivateChat(user1, user2);
        return chat != null ? chat.chatHistory : new ArrayList<>();
    }

    // Helper methods
    private PrivateChat findPrivateChat(String user1, String user2) {
        for (PrivateChat chat : privateChats) {
            if ((chat.user1.equals(user1) && chat.user2.equals(user2)) ||
                    (chat.user1.equals(user2) && chat.user2.equals(user1))) {
                return chat;
            }
        }
        return null;
    }

    private PrivateChat findOrCreatePrivateChat(String user1, String user2) {
        PrivateChat existingChat = findPrivateChat(user1, user2);
        if (existingChat != null) {
            return existingChat;
        }

        PrivateChat newChat = new PrivateChat(user1, user2);
        privateChats.add(newChat);
        return newChat;
    }

    public void clearChatHistory(String user1, String user2) {
        PrivateChat chat = findPrivateChat(user1, user2);
        if (chat != null) {
            chat.chatHistory.clear();
        }
    }
}

class VideoCall {
    public static void startCall(Patient patient, Doctor doctor) {
        // Get current date and time in UTC with specified format
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = now.format(formatter);

        // Generate a unique meeting ID for Zoom URL
        String uniqueID = UUID.randomUUID().toString().substring(0, 8);
        String zoomLink = "https://zoom.us/j/" + uniqueID;

        System.out.println("\n=====================================");
        System.out.println("     VIDEO CONSULTATION SESSION");
        System.out.println("-------------------------------------");
        System.out.println("Current Date and Time (UTC): " + currentDateTime);
        System.out.println("Current User's Login: Haqiqq");
        System.out.println("Patient : " + patient.getName());
        System.out.println("Doctor  : Dr. " + doctor.getName());
        System.out.println("Link    : " + zoomLink);
        System.out.println("Status  : Call Created Successfully");
        System.out.println("=====================================\n");

        // Show in GUI alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Video Call Created");
        alert.setHeaderText(null);
        alert.setContentText(
                "Video consultation details:\n\n" +
                        "Current Date and Time (UTC): " + currentDateTime + "\n" +
                        "Current User's Login: Haqiqq\n" +
                        "Patient: " + patient.getName() + "\n" +
                        "Doctor: Dr. " + doctor.getName() + "\n" +
                        "Link: " + zoomLink + "\n\n" +
                        "Status: Call Created Successfully"
        );
        alert.showAndWait();
    }
}

class ChatClient {
    private String userName;
    private String role;
    private ChatServer server;

    public ChatClient(String userName, String role, ChatServer server) {
        this.userName = userName;
        this.role = role;
        this.server = server;
    }

    // Method for sending broadcast messages (general chat)
    public void sendMessage(String message) {
        String timeStamp = "[2025-05-10 15:02:19]"; // Using provided UTC time
        String sender = String.format("%-16s", role + " " + userName); // preserved padding
        server.broadcastMessage(timeStamp + " " + sender, message);
    }

    // New method for sending private messages
    public void sendPrivateMessage(String receiverName, String message) {
        server.sendPrivateMessage(userName, receiverName, message);
    }

    // Get chat history between this client and another user
    public ArrayList<String> getChatHistoryWith(String otherUser) {
        return server.getPrivateChatHistory(userName, otherUser);
    }

    // Clear chat history with specific user
    public void clearChatHistoryWith(String otherUser) {
        server.clearChatHistory(userName, otherUser);
    }

    // Getters for client information
    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }
}




// -----------------------------------------
// ----------MODULE 1-------------------
// -----------------------------------------
class NotificationService {

    // Alert to doctor
    public void sendAlertToDoctor(String message, Doctor doctor) {
        System.out.println("\n============================================");
        System.out.println("          EMERGENCY ALERT - DOCTOR");
        System.out.println("----------------------------------------------");
        System.out.println("To      : Dr. " + doctor.getName());
        System.out.println("Message : " + message);
        System.out.println("Status  : Alert sent to Doctor");
        System.out.println("============================================\n");
    }

    // Alert to emergency contact
    public void sendAlertToEmergencyContact(String message, String contact) {
        System.out.println("\n============================================");
        System.out.println("    EMERGENCY ALERT - EMERGENCY CONTACT");
        System.out.println("----------------------------------------------");
        System.out.println("To      : " + contact);
        System.out.println("Message : " + message);
        System.out.println("Status  : Alert sent to Emergency Contact");
        System.out.println("============================================\n");
    }
}


class EmergencyAlert {
    private NotificationService notifier;

    public EmergencyAlert(NotificationService notifier) {
        this.notifier = notifier;
    }

    public void checkVitals(Patient patient) {
        boolean criticalOverall = false; // track if any vital was bad
        StringBuilder msg = new StringBuilder("CRITICAL VITALS for " + patient.getName() + ":\n");
        StringBuilder alertBody = new StringBuilder();

        for (VitalSign vs : patient.getVitalSign()) {
            // Check each vital
            if (vs.getPulse() < 50 || vs.getPulse() > 120) {
                msg.append("- Pulse: ").append(vs.getPulse()).append("\n");
                alertBody.append("- Pulse: <b>").append(vs.getPulse()).append("</b><br>");
                criticalOverall = true;
            }
            if (vs.getRespiratoryRate() < 12 || vs.getRespiratoryRate() > 25) {
                msg.append("- Respiratory Rate: ").append(vs.getRespiratoryRate()).append("\n");
                alertBody.append("- Respiratory Rate: <b>").append(vs.getRespiratoryRate()).append("</b><br>");
                criticalOverall = true;
            }
            if (vs.getTemp() > 103 || vs.getTemp() < 95) {
                msg.append("- Temperature: ").append(vs.getTemp()).append("F\n");
                alertBody.append("- Temperature: <b>").append(vs.getTemp()).append("F</b><br>");
                criticalOverall = true;
            }
            if (vs.getOxygenSaturation() < 90) {
                msg.append("- Oxygen Saturation: ").append(vs.getOxygenSaturation()).append("%\n");
                alertBody.append("- Oxygen Saturation: <b>").append(vs.getOxygenSaturation()).append("%</b><br>");
                criticalOverall = true;
            }
            if (vs.getbmi() > 40 || vs.getbmi() < 15) {
                msg.append("- BMI: ").append(vs.getbmi()).append("\n");
                alertBody.append("- BMI: <b>").append(vs.getbmi()).append("</b><br>");
                criticalOverall = true;
            }
            if (!vs.getBloodPressure().equalsIgnoreCase("120/80")) {
                msg.append("- Blood Pressure: ").append(vs.getBloodPressure()).append("\n");
                alertBody.append("- Blood Pressure: <b>").append(vs.getBloodPressure()).append("</b><br>");
                criticalOverall = true;
            }
        }

        if (criticalOverall) {
            Doctor doctor = findAssignedDoctor(patient);
            if (doctor != null) {
                notifier.sendAlertToDoctor(msg.toString(), doctor);
            } else {
                System.out.println("No doctor assigned. Sending alert to duty doctor.");
            }

            notifier.sendAlertToEmergencyContact(msg.toString(), patient.getEmergencyContact());

            // Send email to patient
            MyJavaEmail emailSender = new MyJavaEmail();
            emailSender.createAndSendEmail(
                    patient.getEmail(),
                    "Critical Alert: Your Vitals Are Abnormal",
                    "Dear " + patient.getName() + ",<br><br>" +
                            "Some of your recent vital signs are outside the normal range:<br><br>" +
                            alertBody.toString() +
                            "<br><br>Please consult your doctor immediately."
            );
        } else {
            System.out.println("Vitals are within normal range.");
        }
    }


    private Doctor findAssignedDoctor(Patient patient) {
        ArrayList<Appointment> appts = patient.getAppointment();
        if (appts.isEmpty()) return null;
        return appts.get(appts.size() - 1).getDoctor(); // Latest assigned doctor
    }
}


class PanicButton {
    private NotificationService notifier;

    public PanicButton(NotificationService notifier) {
        this.notifier = notifier;
    }

    public void trigger(Patient patient) {
        String message = "PANIC BUTTON TRIGGERED by " + patient.getName() +
                ". Immediate attention is required.";

        Doctor doctor = getAssignedDoctor(patient);

        if (doctor != null) {
            notifier.sendAlertToDoctor(message, doctor);

            // Send email to doctor
            MyJavaEmail emailSender = new MyJavaEmail();
            emailSender.createAndSendEmail(
                    doctor.getEmail(),
                    "Emergency Panic Alert: Patient Needs Immediate Help",
                    "Dear Dr. " + doctor.getName() + ",<br><br>" +
                            "Your patient <b>" + patient.getName() + "</b> has triggered the <b>Panic Button</b>.<br>" +
                            "Immediate attention is required.<br><br>" +
                            "Please contact or check on the patient as soon as possible.<br><br>" +
                            "Thank you."
            );
        } else {
            System.out.println("No doctor assigned to patient. Alert skipped for doctor.");
        }

        // Always notify emergency contact
        notifier.sendAlertToEmergencyContact(message, patient.getEmergencyContact());

        // Send email to emergency contact
        MyJavaEmail emailSender = new MyJavaEmail();
        emailSender.createAndSendEmail(
                patient.getEmergencyContact(),
                "Emergency Panic Alert: " + patient.getName() + " Needs Immediate Help",
                "Dear " + patient.getEmergencyContact()+ ",<br><br>" +
                        "The patient <b>" + patient.getName() + "</b> has triggered the <b>Panic Button</b>.<br>" +
                        "Immediate attention is needed.<br><br>" +
                        "Please try to reach them or ensure they are safe.<br><br>" +
                        "Thank you."
        );
    }

    private Doctor getAssignedDoctor(Patient patient) {
        ArrayList<Appointment> appts = patient.getAppointment();
        if (appts.isEmpty()) return null;
        return appts.get(appts.size() - 1).getDoctor(); // Latest appointment's doctor
    }
}



// -----------------------------------------
// ----------MODULE 3--------------------
// -----------------------------------------
interface Notifiable {
    void sendNotification(String message);
}

class EmailNotification implements Notifiable {
    private String email;

    public EmailNotification(String email) {
        this.email = email;
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("\n==========================================");
        System.out.println("              EMAIL REMINDER");
        System.out.println("--------------------------------------------");
        System.out.println("To      : " + email);
        System.out.println("Reminder : " + message);
        System.out.println("Status  :    Sent Successfully");
        System.out.println("==========================================\n");
    }

}

class SMSNotification implements Notifiable {
    private String phoneNumber;

    public SMSNotification(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("\n===========================================");
        System.out.println("                SMS REMINDER");
        System.out.println("---------------------------------------------");
        System.out.println("To      : " + phoneNumber);
        System.out.println("Reminder : " + message);
        System.out.println("Status  :    Sent Successfully");
        System.out.println("===========================================\n");
    }

}




class ReminderService {
    private static Scanner scanner = new Scanner(System.in);
    private Set<Notifiable> channels = new HashSet<>();

    private ArrayList<String> notificationLog; // Track sent reminders

    public ReminderService() {
        channels = new HashSet<>();
        notificationLog = new ArrayList<>();
    }


    public void clearConsole() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }


    // For manual/general reminders like medication
    public void sendReminders(String message) {
        for (Notifiable channel : channels) {
            channel.sendNotification(message);
        }
    }

    public void checkAppointmentsAndSendReminders(Patient patient) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        for (Appointment appt : patient.getAppointment()) {
            // Only proceed if a reminder hasn't already been sent for this appointment
            if (appt.isReminderSent()) {
                continue;
            }

            clearConsole();

            long timeDiffMillis = appt.getAppointmentDate().getTime() - now.getTime();
            long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis);

            // Check if the appointment is in the future and within 24 hours (1440 minutes)
            if (minutesLeft > 0 && minutesLeft <= 1440) {
                String message = "Reminder: Appointment with Dr. " + appt.getDoctor().getName() +
                        " at " + sdf.format(appt.getAppointmentDate());

                // Assuming you have valid channels already added,
                // you can use them directly to send the notification.
                try {
                    // 1. Dummy email notification (for console print)
                    Notifiable emailDummy = new EmailNotification(patient.getEmail());
                    emailDummy.sendNotification(message);

                    // 2. Real email sending (through Gmail)
                    MyJavaEmail emailSender = new MyJavaEmail();
                    emailSender.createAndSendEmail(
                            patient.getEmail(),
                            "Appointment Reminder",
                            "Dear " + patient.getName() + ",<br><br>" +
                                    "This is a reminder for your upcoming appointment:<br><br>" +
                                    "<b>Doctor:</b> " + appt.getDoctor().getName() + "<br>" +
                                    "<b>Date & Time:</b> " + sdf.format(appt.getAppointmentDate()) + "<br><br>" +
                                    "Please be on time.<br><br>Thank you!"
                    );

                    // 3. Dummy SMS notification (for console print)
                    Notifiable sms = new SMSNotification(patient.getPhoneNumber());
                    sms.sendNotification(message);

                } catch (Exception e) {
                    System.out.println("Error sending reminder: " + e.getMessage());
                }


                // Mark this appointment as reminded so it won't trigger again.
                appt.setReminderSent(true);

                // Log the reminder
                notificationLog.add("[" + sdf.format(now) + "] " + message);

                // Pause to let the user see the reminder clearly
                try {
                    Thread.sleep(5000);  // Wait for 1 second before proceeding
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                clearConsole();
            }
        }
    }


    public ArrayList<String> getNotificationLog() {
        return notificationLog;
    }
}

class CSVManager {

    public static void saveToCSV(ArrayList<String> lines, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (String line : lines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadFromCSV(String filename) {
        List<String> lines = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(filename))) {
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println(filename + " not found, starting empty.");
        }
        return lines;
    }
}


class MyJavaEmail {
    private String emailAddressTo;
    private String msgSubject;
    private String msgText;

    final String USER_NAME = "MAIL";   // Your Gmail address
    final String PASSWORD = "PASSWORD";        // Your Gmail app password
    final String FROM_ADDRESS = "MAIL"; // Same Gmail address

    public MyJavaEmail() {
        // Empty constructor
    }

    public void createAndSendEmail(String emailAddressTo, String msgSubject, String msgText) {
        this.emailAddressTo = emailAddressTo;
        this.msgSubject = msgSubject;
        this.msgText = msgText;
        sendEmailMessage();
    }



    private void sendEmailMessage() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER_NAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_ADDRESS));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddressTo));
            message.setSubject(msgSubject);

            // Create HTML message
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msgText, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            // Send message
            Transport.send(message);

            // Print console output
            System.out.println("\n==========================================");
            System.out.println("            EMAIL SENT");
            System.out.println("------------------------------------------");
            System.out.println("To      : " + emailAddressTo);
            System.out.println("Subject : " + msgSubject);
            System.out.println("Status  : Sent Successfully");
            System.out.println("==========================================\n");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters and setters
    public void setEmailAddressTo(String emailAddressTo) {
        this.emailAddressTo = emailAddressTo;
    }

    public void setSubject(String subject) {
        this.msgSubject = subject;
    }

    public void setMessageText(String msgText) {
        this.msgText = msgText;
    }
}


public class RPMS_GUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        LoginPage loginPage = new LoginPage(primaryStage);
        primaryStage.setScene(loginPage.createLoginScene());
        primaryStage.setTitle("RPMS Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}