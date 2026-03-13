package vn.edu.ute.languagecenter.management.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

public class ExcelExporter {

    /**
     * Xuất dữ liệu từ JTable ra file Excel (.xlsx)
     *
     * @param table           JTable chứa dữ liệu cần xuất
     * @param defaultFileName Tên file mặc định (VD: "DanhSachHocVien")
     * @param sheetTitle      Tên của Sheet trong Excel
     */
    public static void exportJTableToExcel(JTable table, String defaultFileName, String sheetTitle) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        fileChooser.setSelectedFile(new File(defaultFileName + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Tự động thêm đuôi .xlsx nếu người dùng quên gõ
            if (!fileToSave.getAbsolutePath().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(sheetTitle);
                TableModel model = table.getModel();

                // 1. Tạo style cho Header (In đậm, nền xanh)
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);

                // 2. Tạo style cho Cell thường (Có viền)
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);

                // 3. Viết Header (Tiêu đề cột)
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(model.getColumnName(i));
                    cell.setCellStyle(headerStyle);
                }

                // 4. Viết Data (Dữ liệu)
                for (int i = 0; i < model.getRowCount(); i++) {
                    Row dataRow = sheet.createRow(i + 1);
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Cell cell = dataRow.createCell(j);
                        Object value = model.getValueAt(i, j);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        } else {
                            cell.setCellValue("");
                        }
                        cell.setCellStyle(cellStyle);
                    }
                }

                // 5. Tự động căn chỉnh độ rộng các cột cho đẹp
                for (int i = 0; i < model.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // 6. Ghi file ra ổ cứng
                try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                    workbook.write(out);
                }

                // 7. Thông báo và hỏi xem có muốn mở file lên không
                int openChoice = JOptionPane.showConfirmDialog(null,
                        "Xuất Excel thành công!\nBạn có muốn mở file này ngay bây giờ không?",
                        "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (openChoice == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(fileToSave);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Đã xảy ra lỗi khi xuất Excel:\n" + ex.getMessage(),
                        "Lỗi Xuất File", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}