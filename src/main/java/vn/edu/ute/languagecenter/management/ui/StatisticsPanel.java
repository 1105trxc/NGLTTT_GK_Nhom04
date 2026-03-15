package vn.edu.ute.languagecenter.management.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import vn.edu.ute.languagecenter.management.service.DashboardStatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Map;

/**
 * Modern Statistics Page with charts and date filtering.
 */
public class StatisticsPanel extends JPanel {

    private final DashboardStatsService statsService = new DashboardStatsService();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    private JLabel lblStudents;
    private JLabel lblTeachers;
    private JLabel lblClasses;
    private JLabel lblCourses;
    private JLabel lblEnrollments;
    private JLabel lblRevenue;

    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset<String> pieDataset;

    public StatisticsPanel() {
        initUI();
        refreshStats();
    }

    public void refreshData() {
        refreshStats();
    }

    private void initUI() {
        setBackground(new Color(241, 245, 249)); // Match MainDashboard.C_BG
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // --- TOP HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📈  Dashboard Thống Kê");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- WRAP CONTENT IN SCROLLPLANE ---
        JPanel mainContent = new JPanel(new BorderLayout(0, 25));
        mainContent.setOpaque(false);

        // 1. CARDS GRID (2 rows x 3 columns)
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsGrid.setOpaque(false);
        
        JPanel cardStudents = createStatCard("Học Viên", "10", "Tổng số học viên đã đăng ký", new Color(59, 130, 246));
        lblStudents = (JLabel) cardStudents.getClientProperty("valueLabel");
        cardsGrid.add(cardStudents);

        JPanel cardTeachers = createStatCard("Giáo Viên", "3", "Giảng viên đang công tác", new Color(16, 185, 129));
        lblTeachers = (JLabel) cardTeachers.getClientProperty("valueLabel");
        cardsGrid.add(cardTeachers);

        JPanel cardClasses = createStatCard("Lớp Học", "3", "Tổng số lớp học", new Color(245, 158, 11));
        lblClasses = (JLabel) cardClasses.getClientProperty("valueLabel");
        cardsGrid.add(cardClasses);

        JPanel cardCourses = createStatCard("Khóa Học", "3", "Danh mục khóa học", new Color(139, 92, 246));
        lblCourses = (JLabel) cardCourses.getClientProperty("valueLabel");
        cardsGrid.add(cardCourses);

        JPanel cardEnrollments = createStatCard("Ghi Danh", "10", "Lượt ghi danh", new Color(236, 72, 153));
        lblEnrollments = (JLabel) cardEnrollments.getClientProperty("valueLabel");
        cardsGrid.add(cardEnrollments);

        JPanel cardRevenue = createStatCard("Doanh Thu", "0 VND", "Doanh thu đã thu (Paid invoices)", new Color(20, 184, 166));
        lblRevenue = (JLabel) cardRevenue.getClientProperty("valueLabel");
        cardsGrid.add(cardRevenue);

        mainContent.add(cardsGrid, BorderLayout.NORTH);

        // 2. CHARTS PANEL (Side by side)
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.setPreferredSize(new Dimension(0, 380));

        // A. Bar Chart (Revenue Monthly)
        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "Doanh Thu 6 Tháng Gần Nhất",
                "Tháng", "Doanh Thu (Triệu VNĐ)",
                barDataset,
                PlotOrientation.VERTICAL,
                false, true, false);
        styleBarChart(barChart);
        ChartPanel barChartPanel = new ChartPanel(barChart);
        styleChartPanel(barChartPanel);
        chartsPanel.add(barChartPanel);

        // B. Pie Chart (Pass/Fail)
        pieDataset = new DefaultPieDataset<>();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Tỷ Lệ Đạt/Trượt",
                pieDataset,
                true, true, false);
        stylePieChart(pieChart);
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        styleChartPanel(pieChartPanel);
        chartsPanel.add(pieChartPanel);

        mainContent.add(chartsPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void styleBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(241, 245, 249));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(59, 130, 246));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.1);
    }

    @SuppressWarnings("unchecked")
    private void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Đạt", new Color(16, 185, 129));
        plot.setSectionPaint("Trượt", new Color(239, 68, 68));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 180));
    }

    private void styleChartPanel(ChartPanel p) {
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
    }

    private JPanel createStatCard(String title, String value, String desc, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                
                // Accent top border
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 0, 0));
                
                // Subtle outline
                g2.setColor(new Color(226, 232, 240));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(71, 85, 105));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(accentColor);
        
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(148, 163, 184));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblDesc, BorderLayout.SOUTH);
        
        card.putClientProperty("valueLabel", lblValue);
        return card;
    }

    private void refreshStats() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            long students, teachers, classes, courses, enrollments;
            BigDecimal totalRevenue;
            Map<String, BigDecimal> monthly;
            Map<String, Long> passFail;

            @Override
            protected Void doInBackground() {
                try {
                    students = statsService.getTotalStudents(LocalDate.of(2000, 1, 1), LocalDate.now());
                    teachers = statsService.getTotalTeachers();
                    classes = statsService.getTotalClasses(LocalDate.of(2000, 1, 1), LocalDate.now());
                    courses = statsService.getTotalCourses();
                    enrollments = statsService.getTotalEnrollments();
                    totalRevenue = statsService.getTotalRevenue(LocalDate.of(2000, 1, 1), LocalDate.now());
                    monthly = statsService.getMonthlyRevenue(6);
                    passFail = statsService.getPassFailRatio();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    lblStudents.setText(String.valueOf(students));
                    lblTeachers.setText(String.valueOf(teachers));
                    lblClasses.setText(String.valueOf(classes));
                    lblCourses.setText(String.valueOf(courses));
                    lblEnrollments.setText(String.valueOf(enrollments));
                    
                    if (totalRevenue == null) lblRevenue.setText("0 VND");
                    else lblRevenue.setText(moneyFormat.format(totalRevenue) + " VND");

                    // Update Charts
                    barDataset.clear();
                    for (Map.Entry<String, BigDecimal> entry : monthly.entrySet()) {
                        // Convert to Million VND for better axis labels
                        double millions = entry.getValue().doubleValue() / 1_000_000.0;
                        barDataset.addValue(millions, "Doanh Thu", entry.getKey());
                    }

                    pieDataset.clear();
                    for (Map.Entry<String, Long> entry : passFail.entrySet()) {
                        pieDataset.setValue(entry.getKey(), entry.getValue());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
