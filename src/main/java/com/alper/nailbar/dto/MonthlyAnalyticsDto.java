package com.alper.nailbar.dto;

import java.util.List;

/**
 * Aylık iş analizi istatistikleri.
 * Tamamlanan randevu sayısı, toplam gelir, personel ve hizmet bazlı dağılım.
 */
public class MonthlyAnalyticsDto {

    private int year;
    private int month;
    private long totalAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private double totalRevenue;
    private double averageRevenuePerAppointment;
    private List<StaffRevenueDto> staffBreakdown;
    private List<ServiceRevenueDto> serviceBreakdown;

    public MonthlyAnalyticsDto() {}

    // --- Getters & Setters ---

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }

    public long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(long completedAppointments) { this.completedAppointments = completedAppointments; }

    public long getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(long cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getAverageRevenuePerAppointment() { return averageRevenuePerAppointment; }
    public void setAverageRevenuePerAppointment(double averageRevenuePerAppointment) { this.averageRevenuePerAppointment = averageRevenuePerAppointment; }

    public List<StaffRevenueDto> getStaffBreakdown() { return staffBreakdown; }
    public void setStaffBreakdown(List<StaffRevenueDto> staffBreakdown) { this.staffBreakdown = staffBreakdown; }

    public List<ServiceRevenueDto> getServiceBreakdown() { return serviceBreakdown; }
    public void setServiceBreakdown(List<ServiceRevenueDto> serviceBreakdown) { this.serviceBreakdown = serviceBreakdown; }

    // --- Inner DTOs ---

    public static class StaffRevenueDto {
        private String staffName;
        private long completedCount;
        private double revenue;

        public StaffRevenueDto() {}

        public StaffRevenueDto(String staffName, long completedCount, double revenue) {
            this.staffName = staffName;
            this.completedCount = completedCount;
            this.revenue = revenue;
        }

        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }

        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
    }

    public static class ServiceRevenueDto {
        private String serviceName;
        private long completedCount;
        private double revenue;

        public ServiceRevenueDto() {}

        public ServiceRevenueDto(String serviceName, long completedCount, double revenue) {
            this.serviceName = serviceName;
            this.completedCount = completedCount;
            this.revenue = revenue;
        }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
    }
}
