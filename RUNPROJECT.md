This is the final code for my end semester project and can be fully tested using the following instructions.
1.Make an IntelliJ java project file ( because the code is tried and tested in IntelliJ).
2.Use the following pom.xml file directly for efficiency. Attached as.

(POM FILE)

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.Hospital</groupId>
    <artifactId>hat</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>23</maven.compiler.source>
        <maven.compiler.target>23</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
    <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>17.0.2</version>
        </dependency>
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>jakarta.mail</artifactId>
            <version>2.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.jfree</groupId>
            <artifactId>jfreechart</artifactId>
            <version>1.5.4</version> <!-- latest version as of 2024 -->
        </dependency>
        <dependency>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-fxml</artifactId>
                <version>17.0.2</version>
            <!-- Jakarta Mail -->
            </dependency>
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>javax.mail</artifactId>
            <version>1.6.2</version>
        </dependency>
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>
        <!-- iText PDF -->
            <dependency>
                <groupId>com.itextpdf</groupId>
                <artifactId>itextpdf</artifactId>
                <version>5.5.13.3</version>
            </dependency>
            <dependency>
                <groupId>jakarta.websocket</groupId>
                <artifactId>jakarta.websocket-api</artifactId>
                <version>2.1.0</version>
            </dependency>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.42.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>itext7-core</artifactId>
            <version>7.2.5</version>
        </dependency>

            <!-- JSON Processing -->
            <dependency>
                <groupId>org.json</groupId>
                <artifactId>json</artifactId>
                <version>20231013</version>
            </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.27</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>23</source>
                    <target>23</target>
                    <compilerArgs>--enable-preview</compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>


</project>

3. Use the sql connectivitor server for database connection named as mysql-connector-java-8.0.28.
4. Add libray as this in project structure.  E.G. I added like this in libraries. ( C:\Users\Zain\Desktop\mysql-connector-java-8.0.28.jar )
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
 
