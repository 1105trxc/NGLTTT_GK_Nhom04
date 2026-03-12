DROP DATABASE IF EXISTS mis_language_center;
CREATE DATABASE mis_language_center
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE mis_language_center;

-- =========================
-- 1) CORE: Student
-- =========================
CREATE TABLE students (
  student_id        BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  full_name         VARCHAR(150) NOT NULL,
  date_of_birth     DATE NULL,
  gender            ENUM('Male','Female','Other') NULL,
  phone             VARCHAR(20) NULL,
  email             VARCHAR(150) NULL,
  address           VARCHAR(255) NULL,
  registration_date DATE NOT NULL DEFAULT (CURRENT_DATE),
  status            ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_students_email UNIQUE (email),
  CONSTRAINT uq_students_phone UNIQUE (phone)
);

-- =========================
-- 2) CORE: Teacher
-- =========================
CREATE TABLE teachers (
  teacher_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  full_name    VARCHAR(150) NOT NULL,
  phone        VARCHAR(20) NULL,
  email        VARCHAR(150) NULL,
  specialty    VARCHAR(100) NULL, -- IELTS, TOEIC, GiaoTiep...
  hire_date    DATE NULL,
  status       ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_teachers_email UNIQUE (email),
  CONSTRAINT uq_teachers_phone UNIQUE (phone)
);

-- =========================
-- 3) CORE: Course
-- =========================
CREATE TABLE courses (
  course_id    BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  course_name  VARCHAR(200) NOT NULL,
  description  TEXT NULL,
  level        ENUM('Beginner','Intermediate','Advanced') NULL,
  duration     INT NULL,            -- so gio / so tuan (tuy quy uoc)
  duration_unit ENUM('Hour','Week') NULL DEFAULT 'Week',
  fee          DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  status       ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
-- 4) OPERATIONS: Room
-- =========================
CREATE TABLE rooms (
  room_id    BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  room_name  VARCHAR(100) NOT NULL,
  capacity   INT NOT NULL DEFAULT 0,
  location   VARCHAR(150) NULL,
  status     ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_rooms_name UNIQUE (room_name)
);

-- =========================
-- 5) ACADEMIC: Class
-- =========================
CREATE TABLE classes (
  class_id      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  class_name    VARCHAR(150) NOT NULL,
  course_id     BIGINT UNSIGNED NOT NULL,
  teacher_id    BIGINT UNSIGNED NULL,
  start_date    DATE NOT NULL,
  end_date      DATE NULL,
  max_student   INT NOT NULL DEFAULT 0,
  room_id       BIGINT UNSIGNED NULL,
  status        ENUM('Planned','Open','Ongoing','Completed','Cancelled') NOT NULL DEFAULT 'Planned',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_classes_course
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_classes_teacher
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
    ON UPDATE CASCADE ON DELETE SET NULL,

  CONSTRAINT fk_classes_room
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_classes_course   ON classes(course_id);
CREATE INDEX idx_classes_teacher  ON classes(teacher_id);
CREATE INDEX idx_classes_room     ON classes(room_id);
CREATE INDEX idx_classes_dates    ON classes(start_date, end_date);

-- =========================
-- 6) ACADEMIC: Enrollment (Student <-> Class)
-- =========================
CREATE TABLE enrollments (
  enrollment_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  student_id      BIGINT UNSIGNED NOT NULL,
  class_id        BIGINT UNSIGNED NOT NULL,
  enrollment_date DATE NOT NULL DEFAULT (CURRENT_DATE),
  status          ENUM('Enrolled','Dropped','Completed') NOT NULL DEFAULT 'Enrolled',
  result          ENUM('Pass','Fail','NA') NOT NULL DEFAULT 'NA',
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_enrollments_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_enrollments_class
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT uq_enrollments_student_class UNIQUE (student_id, class_id)
);

CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_class   ON enrollments(class_id);

-- =========================
-- 7) FINANCE: Invoice
-- =========================
CREATE TABLE invoices (
  invoice_id     BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  student_id     BIGINT UNSIGNED NOT NULL,
  total_amount   DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  issue_date     DATE NOT NULL DEFAULT (CURRENT_DATE),
  status         ENUM('Draft','Issued','Paid','Cancelled') NOT NULL DEFAULT 'Issued',
  note           VARCHAR(255) NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_invoices_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_invoices_student ON invoices(student_id);
CREATE INDEX idx_invoices_issue   ON invoices(issue_date);

-- =========================
-- 8) FINANCE: Payment
-- (theo file: PaymentID, StudentID, EnrollmentID, Amount, PaymentDate, Method, Status)
-- =========================
CREATE TABLE payments (
  payment_id      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  student_id      BIGINT UNSIGNED NOT NULL,
  enrollment_id   BIGINT UNSIGNED NULL,
  invoice_id      BIGINT UNSIGNED NULL,
  amount          DECIMAL(15,2) NOT NULL,
  payment_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  payment_method  ENUM('Cash','Bank','Momo','ZaloPay','Card','Other') NOT NULL DEFAULT 'Cash',
  status          ENUM('Pending','Completed','Failed','Refunded') NOT NULL DEFAULT 'Completed',
  reference_code  VARCHAR(100) NULL,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_payments_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_payments_enrollment
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
    ON UPDATE CASCADE ON DELETE SET NULL,

  CONSTRAINT fk_payments_invoice
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_payments_student    ON payments(student_id);
CREATE INDEX idx_payments_enrollment ON payments(enrollment_id);
CREATE INDEX idx_payments_invoice    ON payments(invoice_id);
CREATE INDEX idx_payments_date       ON payments(payment_date);

-- =========================
-- 9) OPERATIONS: Schedule
-- =========================
CREATE TABLE schedules (
  schedule_id  BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  class_id     BIGINT UNSIGNED NOT NULL,
  study_date   DATE NOT NULL,
  start_time   TIME NOT NULL,
  end_time     TIME NOT NULL,
  room_id      BIGINT UNSIGNED NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_schedules_class
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
    ON UPDATE CASCADE ON DELETE CASCADE,

  CONSTRAINT fk_schedules_room
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
    ON UPDATE CASCADE ON DELETE SET NULL,

  CONSTRAINT uq_schedules_class_time UNIQUE (class_id, study_date, start_time, end_time)
);

CREATE INDEX idx_schedules_class_date ON schedules(class_id, study_date);

-- =========================
-- 10) OPERATIONS: Attendance
-- (Trong file: AttendanceID, StudentID, ClassID, Date, Status)
-- =========================
CREATE TABLE attendances (
  attendance_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  student_id    BIGINT UNSIGNED NOT NULL,
  class_id      BIGINT UNSIGNED NOT NULL,
  attend_date   DATE NOT NULL,
  status        ENUM('Present','Absent','Late') NOT NULL DEFAULT 'Present',
  note          VARCHAR(255) NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_attendances_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_attendances_class
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
    ON UPDATE CASCADE ON DELETE CASCADE,

  CONSTRAINT uq_attendances UNIQUE (student_id, class_id, attend_date)
);

CREATE INDEX idx_attendances_class_date   ON attendances(class_id, attend_date);
CREATE INDEX idx_attendances_student_date ON attendances(student_id, attend_date);

-- =========================
-- 11) SYSTEM: Staff
-- =========================
CREATE TABLE staffs (
  staff_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  full_name  VARCHAR(150) NOT NULL,
  role       ENUM('Admin','Consultant','Accountant','Manager','Other') NOT NULL DEFAULT 'Other',
  phone      VARCHAR(20) NULL,
  email      VARCHAR(150) NULL,
  status     ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_staffs_email UNIQUE (email),
  CONSTRAINT uq_staffs_phone UNIQUE (phone)
);

-- =========================
-- 12) SYSTEM: User Account
-- (Role: Admin/Teacher/Student; RelatedID trỏ 1 trong 3 bảng)
-- MySQL không FK động được, nên dùng 3 FK nullable + CHECK.
-- =========================
CREATE TABLE user_accounts (
  user_id        BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  username       VARCHAR(80) NOT NULL,
  password_hash  VARCHAR(255) NOT NULL,
  role           ENUM('Admin','Teacher','Student','Staff') NOT NULL,
  teacher_id     BIGINT UNSIGNED NULL,
  student_id     BIGINT UNSIGNED NULL,
  staff_id       BIGINT UNSIGNED NULL,
  is_active      TINYINT(1) NOT NULL DEFAULT 1,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT uq_user_accounts_username UNIQUE (username),

  -- Đã bỏ ON UPDATE CASCADE ON DELETE SET NULL để không bị xung đột với hàm CHECK
  CONSTRAINT fk_user_accounts_teacher
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),

  CONSTRAINT fk_user_accounts_student
    FOREIGN KEY (student_id) REFERENCES students(student_id),

  CONSTRAINT fk_user_accounts_staff
    FOREIGN KEY (staff_id) REFERENCES staffs(staff_id),

  -- Hàm CHECK phân luồng dữ liệu vẫn giữ nguyên để đảm bảo Data Integrity
  CONSTRAINT chk_user_accounts_related
    CHECK (
      (role='Teacher' AND teacher_id IS NOT NULL AND student_id IS NULL AND staff_id IS NULL) OR
      (role='Student' AND student_id IS NOT NULL AND teacher_id IS NULL AND staff_id IS NULL) OR
      (role='Staff'   AND staff_id   IS NOT NULL AND teacher_id IS NULL AND student_id IS NULL) OR
      (role='Admin'   AND teacher_id IS NULL AND student_id IS NULL AND staff_id IS NULL)
    )
);

-- =========================
-- 13) ACADEMIC: Result
-- =========================
CREATE TABLE results (
  result_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  student_id  BIGINT UNSIGNED NOT NULL,
  class_id    BIGINT UNSIGNED NOT NULL,
  score       DECIMAL(5,2) NULL,
  grade       VARCHAR(10) NULL,
  comment     VARCHAR(255) NULL,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_results_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_results_class
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
    ON UPDATE CASCADE ON DELETE CASCADE,

  CONSTRAINT uq_results UNIQUE (student_id, class_id)
);

CREATE INDEX idx_results_class ON results(class_id);

-- =========================
-- (OPTIONAL) Một số bảng mở rộng gợi ý trong tài liệu:
-- Branch, Promotion, PlacementTest, Certificate, Notification
-- Bạn có thể bật dùng nếu muốn triển khai bản "đầy đủ".
-- =========================

CREATE TABLE branches (
  branch_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  branch_name VARCHAR(150) NOT NULL,
  address     VARCHAR(255) NULL,
  phone       VARCHAR(20) NULL,
  status      ENUM('Active','Inactive') NOT NULL DEFAULT 'Active',
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_branches_name UNIQUE (branch_name)
);

ALTER TABLE rooms
  ADD COLUMN branch_id BIGINT UNSIGNED NULL,
  ADD CONSTRAINT fk_rooms_branch
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
    ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE classes
  ADD COLUMN branch_id BIGINT UNSIGNED NULL,
  ADD CONSTRAINT fk_classes_branch
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
    ON UPDATE CASCADE ON DELETE SET NULL;

CREATE TABLE promotions (
  promotion_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  promo_name     VARCHAR(150) NOT NULL,
  discount_type  ENUM('Percent','Amount') NOT NULL DEFAULT 'Percent',
  discount_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  start_date     DATE NULL,
  end_date       DATE NULL,
  status         ENUM('Active','Inactive') NOT NULL DEFAULT 'Active'
);

ALTER TABLE invoices
  ADD COLUMN promotion_id BIGINT UNSIGNED NULL,
  ADD CONSTRAINT fk_invoices_promotion
    FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id)
    ON UPDATE CASCADE ON DELETE SET NULL;

CREATE TABLE placement_tests (
  test_id     BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  student_id  BIGINT UNSIGNED NOT NULL,
  test_date   DATE NOT NULL DEFAULT (CURRENT_DATE),
  score       DECIMAL(5,2) NULL,
  suggested_level ENUM('Beginner','Intermediate','Advanced') NULL,
  note        VARCHAR(255) NULL,
  CONSTRAINT fk_placement_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE certificates (
  certificate_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  student_id     BIGINT UNSIGNED NOT NULL,
  class_id       BIGINT UNSIGNED NULL,
  cert_name      VARCHAR(150) NOT NULL,
  issue_date     DATE NOT NULL DEFAULT (CURRENT_DATE),
  serial_no      VARCHAR(80) NULL,
  CONSTRAINT fk_cert_student
    FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_cert_class
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT uq_cert_serial UNIQUE (serial_no)
);

CREATE TABLE notifications (
  notification_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  title           VARCHAR(200) NOT NULL,
  content         TEXT NOT NULL,
  target_role     ENUM('All','Student','Teacher','Staff') NOT NULL DEFAULT 'All',
  created_by_user BIGINT UNSIGNED NULL,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notifications_user
    FOREIGN KEY (created_by_user) REFERENCES user_accounts(user_id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

-- =========================
-- =========================
-- 0) CLEAN SAMPLE (optional)
-- =========================
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;
DELETE FROM notifications;
DELETE FROM certificates;
DELETE FROM placement_tests;
DELETE FROM promotions;
DELETE FROM results;
DELETE FROM attendances;
DELETE FROM schedules;
DELETE FROM payments;
DELETE FROM invoices;
DELETE FROM enrollments;
DELETE FROM classes;
DELETE FROM rooms;
DELETE FROM branches;
DELETE FROM user_accounts;
DELETE FROM staffs;
DELETE FROM teachers;
DELETE FROM courses;
DELETE FROM students;
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- =========================
-- 1) Branches
-- =========================
INSERT INTO branches(branch_name, address, phone, status) VALUES
('Cơ sở Quận 1',  '12 Nguyễn Huệ, Q.1, TP.HCM',           '0909000001', 'Active'),
('Cơ sở Thủ Đức', '88 Võ Văn Ngân, TP.Thủ Đức, TP.HCM',   '0909000002', 'Active');

-- =========================
-- 2) Rooms
-- =========================
INSERT INTO rooms(room_name, capacity, location, status, branch_id) VALUES
('P101',  25, 'Tầng 1', 'Active', (SELECT branch_id FROM branches WHERE branch_name = 'Cơ sở Quận 1')),
('P202',  30, 'Tầng 2', 'Active', (SELECT branch_id FROM branches WHERE branch_name = 'Cơ sở Quận 1')),
('TD-01', 22, 'Khu A',  'Active', (SELECT branch_id FROM branches WHERE branch_name = 'Cơ sở Thủ Đức')),
('TD-02', 28, 'Khu B',  'Active', (SELECT branch_id FROM branches WHERE branch_name = 'Cơ sở Thủ Đức'));

-- =========================
-- 3) Courses
-- =========================
INSERT INTO courses(course_name, description, level, duration, duration_unit, fee, status) VALUES
('English Communication A1', 'Giao tiếp cơ bản A1, luyện phản xạ',              'Beginner',     8,  'Week', 2500000, 'Active'),
('IELTS Foundation',          'Nền tảng IELTS: từ vựng + ngữ pháp + kỹ năng',   'Intermediate', 10, 'Week', 4500000, 'Active'),
('TOEIC 650+',                'Luyện đề TOEIC mục tiêu 650+',                    'Intermediate', 8,  'Week', 3200000, 'Active');

-- =========================
-- 4) Teachers
-- =========================
INSERT INTO teachers(full_name, phone, email, specialty, hire_date, status) VALUES
('Nguyễn Minh Anh', '0911000001', 'minhanh.teacher@center.vn', 'GiaoTiep', '2024-06-15', 'Active'),
('Trần Quốc Huy',   '0911000002', 'quochuy.teacher@center.vn', 'IELTS',    '2023-10-01', 'Active'),
('Lê Thu Trang',    '0911000003', 'thutrang.teacher@center.vn','TOEIC',    '2024-02-20', 'Active');

-- =========================
-- 5) Staffs
-- =========================
INSERT INTO staffs(full_name, role, phone, email, status) VALUES
('Phạm Hoài Nam', 'Manager',    '0922000001', 'nam.manager@center.vn', 'Active'),
('Võ Ngọc Linh',  'Consultant', '0922000002', 'linh.consult@center.vn','Active'),
('Đặng Hải Yến',  'Accountant', '0922000003', 'yen.acc@center.vn',     'Active');

-- =========================
-- 6) Students (10 học viên)
-- =========================
INSERT INTO students(full_name, date_of_birth, gender, phone, email, address, registration_date, status) VALUES
('Phạm Gia Bảo',    '2006-04-10', 'Male',   '0933000001', 'baopg01@mail.com',   'Q.1, TP.HCM',      '2026-01-05', 'Active'),
('Nguyễn Mỹ Linh',  '2005-09-21', 'Female', '0933000002', 'linhnm02@mail.com',  'Q.3, TP.HCM',      '2026-01-06', 'Active'),
('Trần Đức Long',   '2004-11-02', 'Male',   '0933000003', 'longtd03@mail.com',  'Q.5, TP.HCM',      '2026-01-06', 'Active'),
('Lê Ngọc Hân',     '2006-07-15', 'Female', '0933000004', 'hanln04@mail.com',   'TP.Thủ Đức',       '2026-01-07', 'Active'),
('Võ Khánh Vy',     '2005-02-18', 'Female', '0933000005', 'vyvk05@mail.com',    'TP.Thủ Đức',       '2026-01-07', 'Active'),
('Đỗ Minh Khang',   '2003-12-30', 'Male',   '0933000006', 'khangdm06@mail.com', 'Q.7, TP.HCM',      '2026-01-08', 'Active'),
('Phan Thảo Nguyên','2004-05-09', 'Female', '0933000007', 'nguyenpt07@mail.com','Q.10, TP.HCM',     '2026-01-09', 'Active'),
('Bùi Anh Tuấn',    '2002-08-28', 'Male',   '0933000008', 'tuanba08@mail.com',  'Q.Bình Thạnh',     '2026-01-10', 'Active'),
('Ngô Quỳnh Chi',   '2005-01-12', 'Female', '0933000009', 'chingq09@mail.com',  'Q.Tân Bình',       '2026-01-10', 'Active'),
('Lý Quốc Bảo',     '2003-03-03', 'Male',   '0933000010', 'baolq10@mail.com',   'Q.Gò Vấp',         '2026-01-11', 'Active');

-- =========================
-- 7) Classes
-- (dùng subquery để tra course_id, teacher_id, room_id, branch_id theo tên)
-- =========================
INSERT INTO classes(class_name, course_id, teacher_id, start_date, end_date, max_student, room_id, status, branch_id) VALUES
(
  'COM-A1-0201',
  (SELECT course_id  FROM courses  WHERE course_name = 'English Communication A1'),
  (SELECT teacher_id FROM teachers WHERE email = 'minhanh.teacher@center.vn'),
  '2026-02-10', '2026-04-05', 25,
  (SELECT room_id    FROM rooms    WHERE room_name = 'P101'),
  'Ongoing',
  (SELECT branch_id  FROM branches WHERE branch_name = 'Cơ sở Quận 1')
),
(
  'IELTS-F-0201',
  (SELECT course_id  FROM courses  WHERE course_name = 'IELTS Foundation'),
  (SELECT teacher_id FROM teachers WHERE email = 'quochuy.teacher@center.vn'),
  '2026-02-12', '2026-04-20', 30,
  (SELECT room_id    FROM rooms    WHERE room_name = 'P202'),
  'Ongoing',
  (SELECT branch_id  FROM branches WHERE branch_name = 'Cơ sở Quận 1')
),
(
  'TOEIC-650-0201',
  (SELECT course_id  FROM courses  WHERE course_name = 'TOEIC 650+'),
  (SELECT teacher_id FROM teachers WHERE email = 'thutrang.teacher@center.vn'),
  '2026-02-15', '2026-04-10', 28,
  (SELECT room_id    FROM rooms    WHERE room_name = 'TD-02'),
  'Ongoing',
  (SELECT branch_id  FROM branches WHERE branch_name = 'Cơ sở Thủ Đức')
);

-- =========================
-- 8) Schedules (dùng subquery tra class_id và room_id)
-- =========================
-- COM-A1-0201: T3/T5 18:30-20:30
INSERT INTO schedules(class_id, study_date, start_time, end_time, room_id)
SELECT c.class_id, d.study_date, '18:30:00', '20:30:00', r.room_id
FROM
  (SELECT class_id FROM classes WHERE class_name = 'COM-A1-0201') c,
  (SELECT '2026-02-10' AS study_date UNION ALL
   SELECT '2026-02-12' UNION ALL
   SELECT '2026-02-17' UNION ALL
   SELECT '2026-02-19') d,
  (SELECT room_id  FROM rooms   WHERE room_name  = 'P101') r;

-- IELTS-F-0201: T2/T4 19:00-21:00
INSERT INTO schedules(class_id, study_date, start_time, end_time, room_id)
SELECT c.class_id, d.study_date, '19:00:00', '21:00:00', r.room_id
FROM
  (SELECT class_id FROM classes WHERE class_name = 'IELTS-F-0201') c,
  (SELECT '2026-02-12' AS study_date UNION ALL
   SELECT '2026-02-16' UNION ALL
   SELECT '2026-02-18' UNION ALL
   SELECT '2026-02-23') d,
  (SELECT room_id  FROM rooms   WHERE room_name  = 'P202') r;

-- TOEIC-650-0201: CN 08:00-10:00
INSERT INTO schedules(class_id, study_date, start_time, end_time, room_id)
SELECT c.class_id, d.study_date, '08:00:00', '10:00:00', r.room_id
FROM
  (SELECT class_id FROM classes WHERE class_name = 'TOEIC-650-0201') c,
  (SELECT '2026-02-15' AS study_date UNION ALL
   SELECT '2026-02-22' UNION ALL
   SELECT '2026-03-01' UNION ALL
   SELECT '2026-03-08') d,
  (SELECT room_id  FROM rooms   WHERE room_name  = 'TD-02') r;

-- =========================
-- 9) Enrollments (dùng subquery tra student_id và class_id)
-- =========================
-- Lớp COM (students 1..5)
INSERT INTO enrollments(student_id, class_id, enrollment_date, status, result)
SELECT s.student_id,
       (SELECT class_id FROM classes WHERE class_name = 'COM-A1-0201'),
       v.enrollment_date, 'Enrolled', 'NA'
FROM students s
JOIN (
  SELECT 'baopg01@mail.com'  AS email, '2026-02-01' AS enrollment_date UNION ALL
  SELECT 'linhnm02@mail.com',           '2026-02-01'                   UNION ALL
  SELECT 'longtd03@mail.com',           '2026-02-02'                   UNION ALL
  SELECT 'hanln04@mail.com',            '2026-02-02'                   UNION ALL
  SELECT 'vyvk05@mail.com',             '2026-02-03'
) v ON s.email = v.email;

-- Lớp IELTS (students 6..9)
INSERT INTO enrollments(student_id, class_id, enrollment_date, status, result)
SELECT s.student_id,
       (SELECT class_id FROM classes WHERE class_name = 'IELTS-F-0201'),
       v.enrollment_date, 'Enrolled', 'NA'
FROM students s
JOIN (
  SELECT 'khangdm06@mail.com' AS email, '2026-02-03' AS enrollment_date UNION ALL
  SELECT 'nguyenpt07@mail.com',          '2026-02-04'                    UNION ALL
  SELECT 'tuanba08@mail.com',            '2026-02-05'                    UNION ALL
  SELECT 'chingq09@mail.com',            '2026-02-05'
) v ON s.email = v.email;

-- Lớp TOEIC (students 10, 3, 7)
INSERT INTO enrollments(student_id, class_id, enrollment_date, status, result)
SELECT s.student_id,
       (SELECT class_id FROM classes WHERE class_name = 'TOEIC-650-0201'),
       v.enrollment_date, 'Enrolled', 'NA'
FROM students s
JOIN (
  SELECT 'baolq10@mail.com'   AS email, '2026-02-06' AS enrollment_date UNION ALL
  SELECT 'longtd03@mail.com',            '2026-02-06'                    UNION ALL
  SELECT 'nguyenpt07@mail.com',          '2026-02-07'
) v ON s.email = v.email;

-- =========================
-- 10) Promotions
-- =========================
INSERT INTO promotions(promo_name, discount_type, discount_value, start_date, end_date, status) VALUES
('Tết 2026 -10%', 'Percent', 10.00,     '2026-01-01', '2026-02-28', 'Active'),
('Giảm 200k',     'Amount',  200000.00, '2026-02-01', '2026-03-31', 'Active');

-- =========================
-- 11) Invoices (dùng subquery tra student_id và promotion_id)
-- =========================
INSERT INTO invoices(student_id, total_amount, issue_date, status, note, promotion_id) VALUES
(
  (SELECT student_id FROM students WHERE email = 'baopg01@mail.com'),
  2500000, '2026-02-01', 'Issued', 'Học phí lớp COM-A1-0201',
  (SELECT promotion_id FROM promotions WHERE promo_name = 'Tết 2026 -10%')
),
(
  (SELECT student_id FROM students WHERE email = 'linhnm02@mail.com'),
  2500000, '2026-02-01', 'Issued', 'Học phí lớp COM-A1-0201',
  (SELECT promotion_id FROM promotions WHERE promo_name = 'Tết 2026 -10%')
),
(
  (SELECT student_id FROM students WHERE email = 'longtd03@mail.com'),
  2500000, '2026-02-02', 'Issued', 'Học phí lớp COM-A1-0201', NULL
),
(
  (SELECT student_id FROM students WHERE email = 'hanln04@mail.com'),
  2500000, '2026-02-02', 'Issued', 'Học phí lớp COM-A1-0201', NULL
),
(
  (SELECT student_id FROM students WHERE email = 'vyvk05@mail.com'),
  2500000, '2026-02-03', 'Issued', 'Học phí lớp COM-A1-0201',
  (SELECT promotion_id FROM promotions WHERE promo_name = 'Giảm 200k')
),
(
  (SELECT student_id FROM students WHERE email = 'khangdm06@mail.com'),
  4500000, '2026-02-03', 'Issued', 'Học phí lớp IELTS-F-0201',
  (SELECT promotion_id FROM promotions WHERE promo_name = 'Tết 2026 -10%')
),
(
  (SELECT student_id FROM students WHERE email = 'nguyenpt07@mail.com'),
  4500000, '2026-02-04', 'Issued', 'Học phí lớp IELTS-F-0201', NULL
),
(
  (SELECT student_id FROM students WHERE email = 'tuanba08@mail.com'),
  4500000, '2026-02-05', 'Issued', 'Học phí lớp IELTS-F-0201',
  (SELECT promotion_id FROM promotions WHERE promo_name = 'Giảm 200k')
),
(
  (SELECT student_id FROM students WHERE email = 'chingq09@mail.com'),
  4500000, '2026-02-05', 'Issued', 'Học phí lớp IELTS-F-0201', NULL
),
(
  (SELECT student_id FROM students WHERE email = 'baolq10@mail.com'),
  3200000, '2026-02-06', 'Issued', 'Học phí lớp TOEIC-650-0201',
  (SELECT promotion_id FROM promotions WHERE promo_name = 'Giảm 200k')
);

-- =========================
-- 12) Payments (dùng subquery tra enrollment_id và invoice_id)
-- =========================
-- Student baopg01: trả đủ
INSERT INTO payments(student_id, enrollment_id, invoice_id, amount, payment_date, payment_method, status, reference_code)
SELECT
  s.student_id,
  e.enrollment_id,
  i.invoice_id,
  2500000, '2026-02-01 10:15:00', 'Bank', 'Completed', 'VCB-0001'
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN classes    cl ON cl.class_id  = e.class_id AND cl.class_name = 'COM-A1-0201'
JOIN invoices   i  ON i.student_id = s.student_id AND i.note = 'Học phí lớp COM-A1-0201'
WHERE s.email = 'baopg01@mail.com';

-- Student linhnm02: trả 2 lần
INSERT INTO payments(student_id, enrollment_id, invoice_id, amount, payment_date, payment_method, status, reference_code)
SELECT s.student_id, e.enrollment_id, i.invoice_id,
  1500000, '2026-02-01 11:00:00', 'Cash', 'Completed', 'CASH-0002-A'
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN classes    cl ON cl.class_id  = e.class_id AND cl.class_name = 'COM-A1-0201'
JOIN invoices   i  ON i.student_id = s.student_id AND i.note = 'Học phí lớp COM-A1-0201'
WHERE s.email = 'linhnm02@mail.com';

INSERT INTO payments(student_id, enrollment_id, invoice_id, amount, payment_date, payment_method, status, reference_code)
SELECT s.student_id, e.enrollment_id, i.invoice_id,
  1000000, '2026-02-05 16:30:00', 'Momo', 'Completed', 'MOMO-0002-B'
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN classes    cl ON cl.class_id  = e.class_id AND cl.class_name = 'COM-A1-0201'
JOIN invoices   i  ON i.student_id = s.student_id AND i.note = 'Học phí lớp COM-A1-0201'
WHERE s.email = 'linhnm02@mail.com';

-- Student khangdm06: trả đủ IELTS
INSERT INTO payments(student_id, enrollment_id, invoice_id, amount, payment_date, payment_method, status, reference_code)
SELECT s.student_id, e.enrollment_id, i.invoice_id,
  4500000, '2026-02-03 09:20:00', 'Bank', 'Completed', 'ACB-0006'
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN classes    cl ON cl.class_id  = e.class_id AND cl.class_name = 'IELTS-F-0201'
JOIN invoices   i  ON i.student_id = s.student_id AND i.note = 'Học phí lớp IELTS-F-0201'
WHERE s.email = 'khangdm06@mail.com';

-- Student nguyenpt07: trả 2 lần IELTS
INSERT INTO payments(student_id, enrollment_id, invoice_id, amount, payment_date, payment_method, status, reference_code)
SELECT s.student_id, e.enrollment_id, i.invoice_id,
  2500000, '2026-02-04 14:00:00', 'Card', 'Completed', 'VISA-0007-A'
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN classes    cl ON cl.class_id  = e.class_id AND cl.class_name = 'IELTS-F-0201'
JOIN invoices   i  ON i.student_id = s.student_id AND i.note = 'Học phí lớp IELTS-F-0201'
WHERE s.email = 'nguyenpt07@mail.com';

INSERT INTO payments(student_id, enrollment_id, invoice_id, amount, payment_date, payment_method, status, reference_code)
SELECT s.student_id, e.enrollment_id, i.invoice_id,
  2000000, '2026-02-10 14:10:00', 'ZaloPay', 'Completed', 'ZALO-0007-B'
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN classes    cl ON cl.class_id  = e.class_id AND cl.class_name = 'IELTS-F-0201'
JOIN invoices   i  ON i.student_id = s.student_id AND i.note = 'Học phí lớp IELTS-F-0201'
WHERE s.email = 'nguyenpt07@mail.com';

-- Student baolq10: trả đủ TOEIC
INSERT INTO payments(student_id, enrollment_id, invoice_id, amount, payment_date, payment_method, status, reference_code)
SELECT s.student_id, e.enrollment_id, i.invoice_id,
  3200000, '2026-02-06 18:00:00', 'Cash', 'Completed', 'CASH-0010'
FROM students s
JOIN enrollments e ON e.student_id = s.student_id
JOIN classes    cl ON cl.class_id  = e.class_id AND cl.class_name = 'TOEIC-650-0201'
JOIN invoices   i  ON i.student_id = s.student_id AND i.note = 'Học phí lớp TOEIC-650-0201'
WHERE s.email = 'baolq10@mail.com';

-- Cập nhật trạng thái invoice Paid (dùng subquery thay hardcoded ID)
SET SQL_SAFE_UPDATES = 0;
UPDATE invoices SET status = 'Paid'
WHERE student_id IN (
  SELECT student_id FROM students WHERE email IN ('baopg01@mail.com','khangdm06@mail.com','baolq10@mail.com','linhnm02@mail.com','nguyenpt07@mail.com')
)
AND status = 'Issued';
SET SQL_SAFE_UPDATES = 1;

-- =========================
-- 13) Attendance (dùng subquery tra student_id và class_id)
-- =========================
-- Lớp COM (class_id từ class_name), buổi 2026-02-10 và 2026-02-12
INSERT INTO attendances(student_id, class_id, attend_date, status, note)
SELECT s.student_id, c.class_id, v.attend_date, v.status, v.note
FROM students s
JOIN (
  SELECT 'baopg01@mail.com'  AS email, '2026-02-10' AS attend_date, 'Present' AS status, NULL        AS note UNION ALL
  SELECT 'linhnm02@mail.com',           '2026-02-10',               'Late',            'Đến trễ 10 phút' UNION ALL
  SELECT 'longtd03@mail.com',           '2026-02-10',               'Present',          NULL          UNION ALL
  SELECT 'hanln04@mail.com',            '2026-02-10',               'Absent',           'Bận việc gia đình' UNION ALL
  SELECT 'vyvk05@mail.com',             '2026-02-10',               'Present',          NULL          UNION ALL
  SELECT 'baopg01@mail.com',            '2026-02-12',               'Present',          NULL          UNION ALL
  SELECT 'linhnm02@mail.com',           '2026-02-12',               'Present',          NULL          UNION ALL
  SELECT 'longtd03@mail.com',           '2026-02-12',               'Present',          NULL          UNION ALL
  SELECT 'hanln04@mail.com',            '2026-02-12',               'Present',          NULL          UNION ALL
  SELECT 'vyvk05@mail.com',             '2026-02-12',               'Late',             'Kẹt xe'
) v ON s.email = v.email
JOIN classes c ON c.class_name = 'COM-A1-0201';

-- Lớp IELTS, buổi 2026-02-12
INSERT INTO attendances(student_id, class_id, attend_date, status, note)
SELECT s.student_id, c.class_id, v.attend_date, v.status, v.note
FROM students s
JOIN (
  SELECT 'khangdm06@mail.com' AS email, '2026-02-12' AS attend_date, 'Present' AS status, NULL AS note UNION ALL
  SELECT 'nguyenpt07@mail.com',          '2026-02-12',               'Present',            NULL        UNION ALL
  SELECT 'tuanba08@mail.com',            '2026-02-12',               'Absent',             'Ốm'        UNION ALL
  SELECT 'chingq09@mail.com',            '2026-02-12',               'Present',            NULL
) v ON s.email = v.email
JOIN classes c ON c.class_name = 'IELTS-F-0201';

-- Lớp TOEIC, buổi 2026-02-15
INSERT INTO attendances(student_id, class_id, attend_date, status, note)
SELECT s.student_id, c.class_id, v.attend_date, v.status, v.note
FROM students s
JOIN (
  SELECT 'baolq10@mail.com'   AS email, '2026-02-15' AS attend_date, 'Present' AS status, NULL AS note UNION ALL
  SELECT 'longtd03@mail.com',            '2026-02-15',               'Present',            NULL        UNION ALL
  SELECT 'nguyenpt07@mail.com',          '2026-02-15',               'Late',               'Đến trễ 5 phút'
) v ON s.email = v.email
JOIN classes c ON c.class_name = 'TOEIC-650-0201';

-- =========================
-- 14) Results (điểm cuối khóa demo)
-- =========================
INSERT INTO results(student_id, class_id, score, grade, comment)
SELECT s.student_id, c.class_id, v.score, v.grade, v.comment
FROM students s
JOIN (
  SELECT 'baopg01@mail.com'  AS email, 'COM-A1-0201'    AS class_name, 86.50 AS score, 'A'  AS grade, 'Phản xạ tốt, phát âm rõ'              AS comment UNION ALL
  SELECT 'linhnm02@mail.com',           'COM-A1-0201',                 78.00,          'B+',          'Tiến bộ rõ rệt sau 2 tuần'               UNION ALL
  SELECT 'khangdm06@mail.com',          'IELTS-F-0201',                80.50,          'B+',          'Writing cần luyện thêm cấu trúc'          UNION ALL
  SELECT 'baolq10@mail.com',            'TOEIC-650-0201',              75.00,          'B',           'Nghe Part 3-4 cần cải thiện'
) v ON s.email = v.email
JOIN classes c ON c.class_name = v.class_name;

-- =========================
-- 15) Placement tests
-- =========================
INSERT INTO placement_tests(student_id, test_date, score, suggested_level, note)
SELECT s.student_id, v.test_date, v.score, v.suggested_level, v.note
FROM students s
JOIN (
  SELECT 'hanln04@mail.com'  AS email, '2026-01-20' AS test_date, 45.00 AS score, 'Beginner'     AS suggested_level, 'Nên học A1 trước'        AS note UNION ALL
  SELECT 'tuanba08@mail.com',           '2026-01-22',             62.50,           'Intermediate',                    'Phù hợp IELTS Foundation'
) v ON s.email = v.email;

-- =========================
-- 16) Certificates
-- =========================
INSERT INTO certificates(student_id, class_id, cert_name, issue_date, serial_no)
SELECT
  (SELECT student_id FROM students WHERE email = 'baopg01@mail.com'),
  (SELECT class_id   FROM classes  WHERE class_name = 'COM-A1-0201'),
  'Certificate of Completion - Communication A1',
  '2026-04-06',
  'CERT-COM-A1-0001';

-- =========================
-- 17) User accounts
-- password_hash: placeholder. Khi làm app, hash bằng BCrypt/Argon2
-- =========================
INSERT INTO user_accounts(username, password_hash, role, teacher_id, student_id, staff_id, is_active) VALUES
('admin',       '$2a$10$PLACEHOLDER_HASH_ADMIN', 'Admin',   NULL, NULL, NULL, 1),
('t.minhanh',   '$2a$10$PLACEHOLDER_HASH_T1',    'Teacher',
  (SELECT teacher_id FROM teachers WHERE email = 'minhanh.teacher@center.vn'), NULL, NULL, 1),
('t.quochuy',   '$2a$10$PLACEHOLDER_HASH_T2',    'Teacher',
  (SELECT teacher_id FROM teachers WHERE email = 'quochuy.teacher@center.vn'), NULL, NULL, 1),
('t.thutrang',  '$2a$10$PLACEHOLDER_HASH_T3',    'Teacher',
  (SELECT teacher_id FROM teachers WHERE email = 'thutrang.teacher@center.vn'),NULL, NULL, 1),
('s.baopg01',   '$2a$10$PLACEHOLDER_HASH_S1',    'Student',
  NULL, (SELECT student_id FROM students WHERE email = 'baopg01@mail.com'),  NULL, 1),
('s.linh02',    '$2a$10$PLACEHOLDER_HASH_S2',    'Student',
  NULL, (SELECT student_id FROM students WHERE email = 'linhnm02@mail.com'), NULL, 1),
('staff.linh',  '$2a$10$PLACEHOLDER_HASH_ST',    'Staff',
  NULL, NULL, (SELECT staff_id FROM staffs WHERE email = 'linh.consult@center.vn'), 1);

-- =========================
-- 18) Notifications
-- =========================
INSERT INTO notifications(title, content, target_role, created_by_user, created_at) VALUES
(
  'Khai giảng tháng 2/2026',
  'Các lớp COM/IELTS/TOEIC bắt đầu từ ngày 10-15/02/2026. Vui lòng đến trước 10 phút.',
  'All',
  (SELECT user_id FROM user_accounts WHERE username = 'admin'),
  '2026-02-08 09:00:00'
),
(
  'Quy định nghỉ học',
  'Nếu vắng mặt, học viên báo trước cho tư vấn viên để được hỗ trợ học bù.',
  'Student',
  (SELECT user_id FROM user_accounts WHERE username = 'admin'),
  '2026-02-09 10:00:00'
);

