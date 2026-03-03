package vn.edu.ute.languagecenter.management.ui;

import com.toedter.calendar.JDateChooser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utility class tiện ích cho JDateChooser ↔ LocalDate.
 */
public final class DateUtil {

    private DateUtil() {
    }

    /** Lấy LocalDate từ JDateChooser, trả null nếu chưa chọn */
    public static LocalDate getLocalDate(JDateChooser chooser) {
        Date date = chooser.getDate();
        if (date == null)
            return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Set LocalDate vào JDateChooser */
    public static void setLocalDate(JDateChooser chooser, LocalDate localDate) {
        if (localDate == null) {
            chooser.setDate(null);
        } else {
            chooser.setDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
    }

    /** Tạo JDateChooser với format yyyy-MM-dd */
    public static JDateChooser createDateChooser() {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("yyyy-MM-dd");
        chooser.setPreferredSize(new java.awt.Dimension(150, 25));
        return chooser;
    }
}
