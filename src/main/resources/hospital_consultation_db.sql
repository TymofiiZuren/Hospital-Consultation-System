-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Apr 17, 2026 at 04:11 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `hospital_consultation_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE `accounts` (
  `account_id` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role_id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `ppsn` varchar(20) NOT NULL,
  `phone` varchar(50) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_admin` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`account_id`, `email`, `password_hash`, `role_id`, `first_name`, `last_name`, `ppsn`, `phone`, `gender`, `is_active`, `created_at`, `is_admin`) VALUES
(9, 'jay@icloud.com', 'c49ea13013272ea0e3e7e9ad1a99035fb50454044d801ba4ddbcf6ca3fb517b4', 2, 'Jay', 'Klepetz', 'lkjdfads', '0873234323', 'Male', 1, '2026-02-21 21:18:57', 0),
(25, 'lufe@icloud.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 1, 'jack', 'lufe', '9218343F', '0878349343', 'Male', 1, '2026-02-22 16:59:56', 0),
(28, 'kevin@icloud.com', '487891b2988ab53db16fe5c4f0b10df3b3a867b8cf06302d7dd51a3ef0f510e3', 3, 'kevin', 'qe', '2384371A', '0842983423', 'Male', 1, '2026-04-10 17:55:32', 0),
(30, 'grace@icloud.com', 'a36f9fa98e5b8ddbb27852af79c8b3e0bec32b5e425375ebc064b98270901386', 4, 'grace', 'ivo', '9238742A', '0873234234', 'Male', 1, '2026-04-10 18:08:09', 1);

-- --------------------------------------------------------

--
-- Table structure for table `administrators`
--

CREATE TABLE `administrators` (
  `admin_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `job_title` varchar(30) NOT NULL,
  `employee_num` varchar(50) NOT NULL,
  `dep_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `administrators`
--

INSERT INTO `administrators` (`admin_id`, `account_id`, `job_title`, `employee_num`, `dep_id`) VALUES
(2, 30, 'Head of moving', '3492087', 3);

-- --------------------------------------------------------

--
-- Table structure for table `appointments`
--

CREATE TABLE `appointments` (
  `appointment_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `appointment_datetime` datetime NOT NULL,
  `status` varchar(30) NOT NULL,
  `medical_need` text DEFAULT NULL,
  `consultation_room` varchar(120) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `appointments`
--

INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `status`, `medical_need`, `consultation_room`) VALUES
(1, 3, 1, '2026-04-10 09:00:00', 'Completed', NULL, NULL),
(2, 3, 1, '2026-04-10 09:00:00', 'Pending', NULL, NULL),
(3, 3, 1, '2026-04-10 10:00:00', 'Pending', 'fe', ''),
(4, 3, 1, '2026-04-12 12:00:00', 'Pending', 'helrae', ''),
(5, 3, 1, '2026-04-13 11:00:00', 'Completed', 'fef', NULL),
(6, 3, 1, '2026-04-13 11:00:00', 'Rejected', 'fef', NULL),
(7, 3, 1, '2026-04-21 16:00:00', 'Pending', 'Follow-up for Acute upper respiratory infection (common cold)', '123A');

-- --------------------------------------------------------

--
-- Table structure for table `consultation`
--

CREATE TABLE `consultation` (
  `consultation_id` int(11) NOT NULL,
  `appointment_id` int(11) NOT NULL,
  `diagnosis` text NOT NULL,
  `notes` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `consultation`
--

INSERT INTO `consultation` (`consultation_id`, `appointment_id`, `diagnosis`, `notes`, `created_at`) VALUES
(1, 1, 'Acute upper respiratory infection (common cold)w', 'Patient reports sore throat, mild fever (37.8°C), and nasal congestion for 3 days. No history of chronic illness. Advised rest and hydration.', '2026-04-14 12:21:26'),
(4, 5, 'heriu', '', '2026-04-14 09:18:09');

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

CREATE TABLE `departments` (
  `dep_id` int(11) NOT NULL,
  `name` varchar(40) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`dep_id`, `name`) VALUES
(1, 'Cardiology'),
(2, 'Neurology'),
(3, 'Hospital Administration'),
(4, 'General Practice');

-- --------------------------------------------------------

--
-- Table structure for table `doctors`
--

CREATE TABLE `doctors` (
  `doctor_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `specialization` varchar(100) NOT NULL,
  `license_number` varchar(20) NOT NULL,
  `years_of_experience` int(5) NOT NULL,
  `consultation_fee` int(10) NOT NULL,
  `dep_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctors`
--

INSERT INTO `doctors` (`doctor_id`, `account_id`, `specialization`, `license_number`, `years_of_experience`, `consultation_fee`, `dep_id`) VALUES
(1, 9, 'Neurologist', '3928473', 3, 0, 2);

-- --------------------------------------------------------

--
-- Table structure for table `insurance`
--

CREATE TABLE `insurance` (
  `insurance_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `provider_name` varchar(100) NOT NULL,
  `policy_number` varchar(50) NOT NULL,
  `status` varchar(30) NOT NULL,
  `expiration_date` date NOT NULL,
  `card_document_path` varchar(512) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `insurance`
--

INSERT INTO `insurance` (`insurance_id`, `patient_id`, `provider_name`, `policy_number`, `status`, `expiration_date`, `card_document_path`) VALUES
(3, 3, 'lufe', 'efe', 'Active', '2027-04-12', NULL),
(4, 3, 'fkewf', 'HS-029837425', 'Pending Verification', '2027-04-13', '/Users/thomas/Documents/ProgrammeSETU/A_C/AssemblyAndC_2025_2026/PRACTICAL_10/PORT.png');

-- --------------------------------------------------------

--
-- Table structure for table `invoices`
--

CREATE TABLE `invoices` (
  `invoice_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `consultation_id` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `invoice_status` varchar(30) NOT NULL,
  `issued_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `paid_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `invoices`
--

INSERT INTO `invoices` (`invoice_id`, `patient_id`, `consultation_id`, `amount`, `invoice_status`, `issued_at`, `paid_at`) VALUES
(1, 3, 1, 50, 'PAID', '2026-04-14 08:52:09', '2026-04-14 08:52:09'),
(4, 3, 4, 23123, 'PAID', '2026-04-17 10:42:09', '2026-04-17 10:42:09');

-- --------------------------------------------------------

--
-- Table structure for table `lab_results`
--

CREATE TABLE `lab_results` (
  `lab_result_id` int(11) NOT NULL,
  `consultation_id` int(11) DEFAULT NULL,
  `technician_id` int(11) NOT NULL,
  `test_type` varchar(40) NOT NULL,
  `result` text NOT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `appointment_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lab_results`
--

INSERT INTO `lab_results` (`lab_result_id`, `consultation_id`, `technician_id`, `test_type`, `result`, `uploaded_at`, `appointment_id`) VALUES
(1, 1, 3, 'werwerwe', 'werwerwerwer', '2026-04-10 18:14:35', NULL),
(3, 4, 3, 'Kfjew', 'fekwfjwefwe', '2026-04-14 09:18:28', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `lab_technicians`
--

CREATE TABLE `lab_technicians` (
  `technician_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `qualification` varchar(100) NOT NULL,
  `employee_num` varchar(50) NOT NULL,
  `lab_name` varchar(30) NOT NULL,
  `shift` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lab_technicians`
--

INSERT INTO `lab_technicians` (`technician_id`, `account_id`, `qualification`, `employee_num`, `lab_name`, `shift`) VALUES
(3, 28, 'Supercell', '239847234', 'Immunology Lab', 'Afternoon (14:00-22:00)');

-- --------------------------------------------------------

--
-- Table structure for table `medical_records`
--

CREATE TABLE `medical_records` (
  `record_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `consultation_id` int(11) NOT NULL,
  `prescription` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `medical_records`
--

INSERT INTO `medical_records` (`record_id`, `patient_id`, `consultation_id`, `prescription`, `created_at`) VALUES
(1, 3, 1, 'Paracetamol 500mg – take 1 tablet every 6 hours as needed\nIbuprofen 200mg – take 1 tablet every 8 hours after meals\nSaline nasal spray – use twice daily', '2026-04-14 12:21:26'),
(4, 3, 4, 'efwef', '2026-04-14 09:18:09');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `patient_id`, `title`, `message`, `is_read`, `created_at`) VALUES
(1, 3, 'Appointment Accepted', 'Your appointment with Dr. Jay Klepetz on 12 Apr 2026 12:00 is now Accepted.', 1, '2026-04-14 09:45:14'),
(2, 3, 'Appointment Rejected', 'Your appointment with Dr. Jay Klepetz on 13 Apr 2026 11:00 is now Rejected.', 1, '2026-04-14 09:48:55'),
(3, 3, 'Appointment Completed', 'Your appointment with Dr. Jay Klepetz on 13 Apr 2026 11:00 is now Completed.', 1, '2026-04-14 10:14:23'),
(4, 3, 'Appointment Accepted', 'Your appointment with Dr. Jay Klepetz on 13 Apr 2026 11:00 is now Accepted.', 1, '2026-04-14 10:16:15'),
(5, 3, 'Follow-up scheduled', 'A follow-up appointment with Dr. Jay Klepetz has been scheduled for 21 Apr 2026 16:00. Reason: Follow-up for Acute upper respiratory infection (common cold).', 1, '2026-04-14 10:41:25');

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE `patients` (
  `patient_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `date_of_birth` date NOT NULL,
  `address` varchar(100) NOT NULL,
  `eircode` varchar(20) NOT NULL,
  `blood_type` varchar(10) NOT NULL,
  `medical_record_number` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `patients`
--

INSERT INTO `patients` (`patient_id`, `account_id`, `date_of_birth`, `address`, `eircode`, `blood_type`, `medical_record_number`) VALUES
(3, 25, '2003-11-22', 'Dublin', 'A43 F323', 'B1', '234132413');

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL,
  `name` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `name`) VALUES
(1, 'Patient'),
(2, 'Doctor'),
(3, 'LabTechnician'),
(4, 'Administrator');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`account_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `fk_account_role` (`role_id`);

--
-- Indexes for table `administrators`
--
ALTER TABLE `administrators`
  ADD PRIMARY KEY (`admin_id`),
  ADD KEY `fk_admin_dep` (`dep_id`),
  ADD KEY `fk_admin_account` (`account_id`);

--
-- Indexes for table `appointments`
--
ALTER TABLE `appointments`
  ADD PRIMARY KEY (`appointment_id`),
  ADD KEY `fk_appointment_doctor` (`doctor_id`),
  ADD KEY `fk_appointment_patient` (`patient_id`);

--
-- Indexes for table `consultation`
--
ALTER TABLE `consultation`
  ADD PRIMARY KEY (`consultation_id`),
  ADD UNIQUE KEY `unique_appointment` (`appointment_id`);

--
-- Indexes for table `departments`
--
ALTER TABLE `departments`
  ADD PRIMARY KEY (`dep_id`);

--
-- Indexes for table `doctors`
--
ALTER TABLE `doctors`
  ADD PRIMARY KEY (`doctor_id`),
  ADD UNIQUE KEY `unique_account` (`account_id`) USING BTREE,
  ADD UNIQUE KEY `unique_license_num` (`license_number`),
  ADD KEY `fk_doctor_dep` (`dep_id`);

--
-- Indexes for table `insurance`
--
ALTER TABLE `insurance`
  ADD PRIMARY KEY (`insurance_id`),
  ADD KEY `fk_insurance_patient` (`patient_id`);

--
-- Indexes for table `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`invoice_id`),
  ADD UNIQUE KEY `unique_consultation` (`consultation_id`),
  ADD KEY `fk_invoice_patient` (`patient_id`);

--
-- Indexes for table `lab_results`
--
ALTER TABLE `lab_results`
  ADD PRIMARY KEY (`lab_result_id`),
  ADD KEY `fk_lab_rs_consultation` (`consultation_id`),
  ADD KEY `fk_lab_rs_technician` (`technician_id`);

--
-- Indexes for table `lab_technicians`
--
ALTER TABLE `lab_technicians`
  ADD PRIMARY KEY (`technician_id`),
  ADD UNIQUE KEY `unique_account` (`account_id`) USING BTREE;

--
-- Indexes for table `medical_records`
--
ALTER TABLE `medical_records`
  ADD PRIMARY KEY (`record_id`),
  ADD KEY `fk_medical_rd_patient` (`patient_id`),
  ADD KEY `fk_medical_rd_consultation` (`consultation_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`patient_id`),
  ADD UNIQUE KEY `account_id` (`account_id`),
  ADD UNIQUE KEY `unique_medical_record` (`medical_record_number`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accounts`
--
ALTER TABLE `accounts`
  MODIFY `account_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `administrators`
--
ALTER TABLE `administrators`
  MODIFY `admin_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `appointments`
--
ALTER TABLE `appointments`
  MODIFY `appointment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `consultation`
--
ALTER TABLE `consultation`
  MODIFY `consultation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `departments`
--
ALTER TABLE `departments`
  MODIFY `dep_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `doctors`
--
ALTER TABLE `doctors`
  MODIFY `doctor_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `insurance`
--
ALTER TABLE `insurance`
  MODIFY `insurance_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `invoices`
--
ALTER TABLE `invoices`
  MODIFY `invoice_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `lab_results`
--
ALTER TABLE `lab_results`
  MODIFY `lab_result_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `lab_technicians`
--
ALTER TABLE `lab_technicians`
  MODIFY `technician_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `medical_records`
--
ALTER TABLE `medical_records`
  MODIFY `record_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `patients`
--
ALTER TABLE `patients`
  MODIFY `patient_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `accounts`
--
ALTER TABLE `accounts`
  ADD CONSTRAINT `fk_account_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`);

--
-- Constraints for table `administrators`
--
ALTER TABLE `administrators`
  ADD CONSTRAINT `fk_admin_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_admin_dep` FOREIGN KEY (`dep_id`) REFERENCES `departments` (`dep_id`);

--
-- Constraints for table `appointments`
--
ALTER TABLE `appointments`
  ADD CONSTRAINT `fk_appointment_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`),
  ADD CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`);

--
-- Constraints for table `consultation`
--
ALTER TABLE `consultation`
  ADD CONSTRAINT `fk_consultation_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`);

--
-- Constraints for table `doctors`
--
ALTER TABLE `doctors`
  ADD CONSTRAINT `fk_doctor_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_doctor_dep` FOREIGN KEY (`dep_id`) REFERENCES `departments` (`dep_id`);

--
-- Constraints for table `insurance`
--
ALTER TABLE `insurance`
  ADD CONSTRAINT `fk_insurance_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`);

--
-- Constraints for table `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `fk_invoice_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`consultation_id`),
  ADD CONSTRAINT `fk_invoice_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`);

--
-- Constraints for table `lab_results`
--
ALTER TABLE `lab_results`
  ADD CONSTRAINT `fk_lab_rs_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`consultation_id`),
  ADD CONSTRAINT `fk_lab_rs_technician` FOREIGN KEY (`technician_id`) REFERENCES `lab_technicians` (`technician_id`);

--
-- Constraints for table `lab_technicians`
--
ALTER TABLE `lab_technicians`
  ADD CONSTRAINT `fk_technician_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE;

--
-- Constraints for table `medical_records`
--
ALTER TABLE `medical_records`
  ADD CONSTRAINT `fk_medical_rd_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`consultation_id`),
  ADD CONSTRAINT `fk_medical_rd_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`);

--
-- Constraints for table `patients`
--
ALTER TABLE `patients`
  ADD CONSTRAINT `fk_patient_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
