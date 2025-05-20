This is the final code for my end semester project and can be fully tested using the following instructions.

1.Make an IntelliJ java project file (because the code is tried and tested in IntelliJ).

2.Use the pom.xml file directly as your pom as it has all dependencies included to run project.

3. Use the sql connectivitor server for database connection named as mysql-connector-java-8.0.28.

4. Add libray for sql connector in project structure.  E.G. I added like this in libraries. ( C:\Users\Zain\Desktop\mysql-connector-java-8.0.28.jar )

5. MOST IMPORTANTLY, add your actual email and database user name and passwords in MyJavaEmail class in RPMS_GUI, and in Database classes for them to function.

6. Add pathway as this ( --module-path "C:\Program Files\Java\javafx-sdk-24.0.1\lib" --add-modules javafx.controls ) in edit configurations to use java fx sdk files
   that you have in your pc with name of Main class which is RPMS_GUI to run gui properly.

8. Your database should have all the specific required schema to store as the following are sql commands to create such as database.

   CREATE DATABASE rpms;
USE rpms;

-- USERS TABLE
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    address VARCHAR(255),
    phone_number VARCHAR(20),
    user_type ENUM('admin', 'patient', 'doctor')
);

-- PATIENTS TABLE
CREATE TABLE patients (
    user_id INT PRIMARY KEY,
    gender VARCHAR(10),
    emergency_contact VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- DOCTORS TABLE
CREATE TABLE doctors (
    user_id INT PRIMARY KEY,
    speciality VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- VITALS TABLE
CREATE TABLE vitals (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT,
    pulse DOUBLE,
    respiratory_rate DOUBLE,
    blood_pressure VARCHAR(20),
    oxygen_saturation DOUBLE,
    temperature DOUBLE,
    bmi DOUBLE,
    date DATE,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(user_id)
);

-- APPOINTMENT TABLE
CREATE TABLE appointment (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    appointment_date DATETIME,
    patient_user_id INT,
    doctor_user_id INT,
    reason TEXT,
    status VARCHAR(50),
    reminder_sent TINYINT(1),
    created_at DATETIME,
    created_by VARCHAR(100),
    last_modified DATETIME,
    modified_by VARCHAR(100),
    FOREIGN KEY (patient_user_id) REFERENCES patients(user_id),
    FOREIGN KEY (doctor_user_id) REFERENCES doctors(user_id)
);

-- FEEDBACK TABLE
CREATE TABLE feedback (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT,
    doctor_id INT,
    comments TEXT,
    medicine VARCHAR(255),
    frequency VARCHAR(100),
    created_at DATETIME,
    FOREIGN KEY (patient_id) REFERENCES patients(user_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(user_id)
);

-- MESSAGES TABLE
CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender INT,
    receiver INT,
    message TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender) REFERENCES users(id),
    FOREIGN KEY (receiver) REFERENCES users(id)
);

-- LOGIN HISTORY TABLE
CREATE TABLE login_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100),
    role VARCHAR(50),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

YOUR CODE IS NOW GOOD TO GO!
 
