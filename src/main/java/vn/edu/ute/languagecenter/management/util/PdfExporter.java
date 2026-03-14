package vn.edu.ute.languagecenter.management.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

public class PdfExporter {

    /**
     * Hàm hỗ trợ xuất Hóa đơn ra file PDF
     */
    public static void exportInvoiceToPdf(
            String invoiceId, String studentName, String issueDate,
            String totalAmount, String paidAmount, String remainingAmount, String status) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu Hóa Đơn PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        fileChooser.setSelectedFile(new File("HoaDon_" + studentName.replaceAll(" ", "") + "_" + invoiceId + ".pdf"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }

            Document document = new Document(PageSize.A5); // Dùng khổ A5 cho giống biên lai thu tiền
            try {
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // Cấu hình Font Tiếng Việt (Sử dụng Arial của Windows, nếu lỗi sẽ dùng font mặc định)
                Font titleFont;
                Font normalFont;
                Font boldFont;
                try {
                    BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    titleFont = new Font(bf, 16, Font.BOLD, BaseColor.BLUE);
                    normalFont = new Font(bf, 12, Font.NORMAL, BaseColor.BLACK);
                    boldFont = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);
                } catch (Exception e) {
                    titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
                    normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
                    boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
                }

                // Tiêu đề trung tâm
                Paragraph centerName = new Paragraph("TRUNG TÂM NGOẠI NGỮ UTE", boldFont);
                centerName.setAlignment(Element.ALIGN_CENTER);
                document.add(centerName);

                Paragraph address = new Paragraph("Địa chỉ: Số 1 Võ Văn Ngân, Thủ Đức, TP.HCM", normalFont);
                address.setAlignment(Element.ALIGN_CENTER);
                document.add(address);

                document.add(new Paragraph(" ")); // Dòng trống
                document.add(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2));
                document.add(new Paragraph(" "));

                // Tiêu đề Hóa Đơn
                Paragraph title = new Paragraph("HÓA ĐƠN THU HỌC PHÍ", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                Paragraph pId = new Paragraph("Mã HĐ: #" + invoiceId, normalFont);
                pId.setAlignment(Element.ALIGN_CENTER);
                document.add(pId);
                document.add(new Paragraph(" "));

                // Bảng thông tin
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{4f, 6f});

                addTableRow(table, "Họ tên học viên:", studentName, normalFont, boldFont);
                addTableRow(table, "Ngày lập phiếu:", issueDate, normalFont, normalFont);
                addTableRow(table, "Trạng thái:", status, normalFont, boldFont);
                addTableRow(table, "Tổng tiền học phí:", totalAmount, normalFont, boldFont);
                addTableRow(table, "Đã thanh toán:", paidAmount, normalFont, normalFont);
                addTableRow(table, "Còn nợ lại:", remainingAmount, normalFont, new Font(normalFont.getBaseFont(), 12, Font.BOLD, BaseColor.RED));

                document.add(table);

                document.add(new Paragraph(" "));
                document.add(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2));
                document.add(new Paragraph(" "));

                // Chữ ký
                PdfPTable signTable = new PdfPTable(2);
                signTable.setWidthPercentage(100);
                PdfPCell cell1 = new PdfPCell(new Phrase("Người nộp tiền\n(Ký, ghi rõ họ tên)", normalFont));
                cell1.setBorder(Rectangle.NO_BORDER);
                cell1.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell cell2 = new PdfPCell(new Phrase("Người thu tiền\n(Ký, ghi rõ họ tên)", normalFont));
                cell2.setBorder(Rectangle.NO_BORDER);
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

                signTable.addCell(cell1);
                signTable.addCell(cell2);
                document.add(signTable);

                document.close();

                // Mở file sau khi lưu
                int openChoice = JOptionPane.showConfirmDialog(null,
                        "Xuất Hóa Đơn PDF thành công!\nBạn có muốn mở file này ngay bây giờ không?",
                        "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (openChoice == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(fileToSave);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi khi tạo PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xuất chứng chỉ dạng PDF đẹp mắt với iText.
     */
    public static void exportCertificatePdf(String studentName, String className, String certName, String issueDate, String serial) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu chứng chỉ PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        fileChooser.setSelectedFile(new File("ChungChi_" + studentName.replaceAll("\\s+", "") + ".pdf"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            try {
                // Trang nằm ngang (Landscape)
                Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Vẽ Border bằng PdfContentByte
                PdfContentByte canvas = writer.getDirectContent();
                Rectangle rect = document.getPageSize();
                canvas.setColorStroke(new BaseColor(100, 149, 237)); // Xanh dương nhạt
                canvas.setLineWidth(15);
                canvas.rectangle(rect.getLeft() + 20, rect.getBottom() + 20, rect.getWidth() - 40, rect.getHeight() - 40);
                canvas.stroke();

                canvas.setColorStroke(new BaseColor(25, 25, 112)); // Xanh đậm
                canvas.setLineWidth(5);
                canvas.rectangle(rect.getLeft() + 30, rect.getBottom() + 30, rect.getWidth() - 60, rect.getHeight() - 60);
                canvas.stroke();

                // Fonts mặc định (sẽ gặp vấn đề với dấu tiếng Việt, nhưng có thể cải thiện bằng basefont thực tế sau)
                Font titleFont;
                Font subTitleFont;
                Font nameFont;
                Font normalFont;
                Font smallFont;
                
                try {
                    BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    titleFont = new Font(bf, 36, Font.BOLD, BaseColor.BLUE);
                    subTitleFont = new Font(bf, 22, Font.NORMAL, BaseColor.DARK_GRAY);
                    nameFont = new Font(bf, 32, Font.BOLD, BaseColor.RED);
                    normalFont = new Font(bf, 16, Font.ITALIC, BaseColor.BLACK);
                    smallFont = new Font(bf, 14, Font.NORMAL, BaseColor.GRAY);
                } catch (Exception e) {
                    titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36, BaseColor.BLUE);
                    subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 22, BaseColor.DARK_GRAY);
                    nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, BaseColor.RED);
                    normalFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 16, BaseColor.BLACK);
                    smallFont = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.GRAY);
                }

                Paragraph mainTitle = new Paragraph("CHỨNG CHỈ HOÀN THÀNH", titleFont);
                mainTitle.setAlignment(Element.ALIGN_CENTER);
                mainTitle.setSpacingBefore(20f);
                mainTitle.setSpacingAfter(20f);
                document.add(mainTitle);

                Paragraph p1 = new Paragraph("Chứng nhận học viên:", subTitleFont);
                p1.setAlignment(Element.ALIGN_CENTER);
                p1.setSpacingAfter(10f);
                document.add(p1);

                Paragraph name = new Paragraph(studentName.toUpperCase(), nameFont);
                name.setAlignment(Element.ALIGN_CENTER);
                name.setSpacingAfter(20f);
                document.add(name);

                Paragraph p2 = new Paragraph("Đã hoàn thành xuất sắc khoá học / lớp học:", subTitleFont);
                p2.setAlignment(Element.ALIGN_CENTER);
                p2.setSpacingAfter(10f);
                document.add(p2);

                Paragraph pClass = new Paragraph(className, new Font(nameFont.getBaseFont(), nameFont.getSize(), nameFont.getStyle(), BaseColor.DARK_GRAY)); // dùng chung style font lớn màu xám
                pClass.setAlignment(Element.ALIGN_CENTER);
                pClass.setSpacingAfter(20f);
                document.add(pClass);

                Paragraph pCert = new Paragraph("Loại chứng chỉ: " + certName, normalFont);
                pCert.setAlignment(Element.ALIGN_CENTER);
                pCert.setSpacingAfter(20f);
                document.add(pCert);

                // Dòng ký tên và ngày tháng
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(85); // Mở rộng thêm để đẹp hơn
                
                PdfPCell cellLeft = new PdfPCell(new Phrase("Ngày cấp: " + issueDate + "\nSố khung (Serial): " + serial, smallFont));
                cellLeft.setBorder(Rectangle.NO_BORDER);
                cellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellLeft.setVerticalAlignment(Element.ALIGN_BOTTOM);
                table.addCell(cellLeft);

                // Thêm không gian trống để Giám đốc có thể ký tên thực tế, rồi mới đến dòng chữ ký
                Paragraph pSign = new Paragraph();
                pSign.setAlignment(Element.ALIGN_CENTER);
                pSign.add(new Chunk("Giám Đốc Trung Tâm\n", new Font(smallFont.getBaseFont(), 16, Font.BOLD, BaseColor.BLACK)));
                pSign.add(new Chunk("(Ký, ghi rõ họ tên)\n\n\n", smallFont));
                pSign.add(new Chunk("__________________________", smallFont));
                
                PdfPCell cellRight = new PdfPCell(pSign);
                cellRight.setBorder(Rectangle.NO_BORDER);
                cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellRight);

                document.add(table);

                document.close();

                int openChoice = JOptionPane.showConfirmDialog(null, 
                        "Xuất PDF thành công!\nBạn có muốn mở file ngay không?", 
                        "Thành công", JOptionPane.YES_NO_OPTION);
                if (openChoice == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(file);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi khi xuất PDF:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void addTableRow(PdfPTable table, String col1, String col2, Font font1, Font font2) {
        PdfPCell cell1 = new PdfPCell(new Phrase(col1, font1));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setPaddingBottom(8f);

        PdfPCell cell2 = new PdfPCell(new Phrase(col2, font2));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setPaddingBottom(8f);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}