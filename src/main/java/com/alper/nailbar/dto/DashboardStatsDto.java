package com.alper.nailbar.dto;

/**
 * Admin dashboard istatistikleri.
 * Bugünkü randevu sayısı, bekleyen randevular, toplam personel vb.
 */
public class DashboardStatsDto {

    private long todayAppointmentsCount;    // Bugün toplam randevu sayısı
    private long pendingAppointmentsCount;  // Onay bekleyen randevular (tüm zamanlar)
    private long totalStaffCount;           // Toplam STAFF sayısı
    private long completedTodayCount;       // Bugün tamamlanan hizmet sayısı

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long todayAppointmentsCount, long pendingAppointmentsCount,
                             long totalStaffCount, long completedTodayCount) {
        this.todayAppointmentsCount = todayAppointmentsCount;
        this.pendingAppointmentsCount = pendingAppointmentsCount;
        this.totalStaffCount = totalStaffCount;
        this.completedTodayCount = completedTodayCount;
    }

    public long getTodayAppointmentsCount() { return todayAppointmentsCount; }
    public void setTodayAppointmentsCount(long c) { this.todayAppointmentsCount = c; }

    public long getPendingAppointmentsCount() { return pendingAppointmentsCount; }
    public void setPendingAppointmentsCount(long c) { this.pendingAppointmentsCount = c; }

    public long getTotalStaffCount() { return totalStaffCount; }
    public void setTotalStaffCount(long c) { this.totalStaffCount = c; }

    public long getCompletedTodayCount() { return completedTodayCount; }
    public void setCompletedTodayCount(long c) { this.completedTodayCount = c; }
}
