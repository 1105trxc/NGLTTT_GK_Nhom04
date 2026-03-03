package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.repo.jpa.JpaStaffRepository;
import vn.edu.ute.languagecenter.management.model.Staff;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * StaffService - Xử lý nghiệp vụ quản lý hồ sơ nhân viên.
 */
public class StaffService {

    private final JpaStaffRepository staffDAO = new JpaStaffRepository();

    /**
     * Lấy tất cả nhân viên.
     */
    public List<Staff> getAllStaff() {
        return staffDAO.findAllSorted();
    }

    /**
     * Lấy nhân viên đang Active.
     */
    public List<Staff> getActiveStaff() {
        return staffDAO.findAllActive();
    }

    /**
     * Tìm nhân viên theo ID.
     */
    public Optional<Staff> getStaffById(Long id) {
        return staffDAO.findById(id);
    }

    /**
     * Thêm mới nhân viên vào database.
     *
     * @param staff đối tượng Staff cần lưu
     */
    public void addStaff(Staff staff) {
        staff.setCreatedAt(LocalDateTime.now());
        staff.setUpdatedAt(LocalDateTime.now());
        if (staff.getStatus() == null) {
            staff.setStatus(Staff.ActiveStatus.Active);
        }
        staffDAO.save(staff);
    }

    /**
     * Cập nhật thông tin nhân viên.
     *
     * @param staff đối tượng Staff đã chỉnh sửa
     */
    public Staff updateStaff(Staff staff) {
        staff.setUpdatedAt(LocalDateTime.now());
        return staffDAO.update(staff);
    }

    /**
     * Xóa nhân viên theo ID.
     *
     * @param id ID của nhân viên cần xóa
     */
    public void deleteStaff(Long id) {
        staffDAO.delete(id);
    }
}
