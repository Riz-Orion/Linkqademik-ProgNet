
// File: StatistikManager.java
import java.util.*;
import java.text.SimpleDateFormat;

public class StatistikManager {
    private List<Antrian> historyAntrian;

    public StatistikManager() {
        this.historyAntrian = new ArrayList<>();
    }

    public void tambahKeHistory(Antrian antrian) {
        historyAntrian.add(antrian);
    }

    public int getTotalAntrianHariIni() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();

        int count = 0;
        for (Antrian a : historyAntrian) {
            if (a.getWaktu().after(startOfDay)) {
                count++;
            }
        }
        return count;
    }

    public double getRataRataWaktuLayanan() {
        long total = 0;
        int count = 0;

        for (Antrian a : historyAntrian) {
            if (a.getStatus().equals("Selesai") && a.getDurasiLayanan() > 0) {
                total += a.getDurasiLayanan();
                count++;
            }
        }

        return count > 0 ? (double) total / count : 0;
    }

    public Map<String, Integer> getStatistikPerKategori() {
        Map<String, Integer> stats = new HashMap<>();

        for (Antrian a : historyAntrian) {
            String kategori = a.getKategori();
            stats.put(kategori, stats.getOrDefault(kategori, 0) + 1);
        }

        return stats;
    }

    public Map<String, Integer> getStatistikPerDosen() {
        Map<String, Integer> stats = new HashMap<>();

        for (Antrian a : historyAntrian) {
            String dosen = a.getDosen();
            stats.put(dosen, stats.getOrDefault(dosen, 0) + 1);
        }

        return stats;
    }

    public List<Antrian> getHistory() {
        return new ArrayList<>(historyAntrian);
    }

    public String generateLaporan() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        sb.append("=".repeat(80)).append("\n");
        sb.append("                    LAPORAN STATISTIK ANTRIAN\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append("Tanggal Generate: ").append(sdf.format(new Date())).append("\n\n");

        sb.append("RINGKASAN:\n");
        sb.append("- Total Antrian Hari Ini: ").append(getTotalAntrianHariIni()).append("\n");
        sb.append("- Rata-rata Waktu Layanan: ").append(String.format("%.1f", getRataRataWaktuLayanan()))
                .append(" menit\n");
        sb.append("- Total History: ").append(historyAntrian.size()).append(" antrian\n\n");

        sb.append("STATISTIK PER KATEGORI:\n");
        Map<String, Integer> statsKategori = getStatistikPerKategori();
        for (Map.Entry<String, Integer> entry : statsKategori.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" antrian\n");
        }

        sb.append("\nSTATISTIK PER DOSEN:\n");
        Map<String, Integer> statsDosen = getStatistikPerDosen();
        for (Map.Entry<String, Integer> entry : statsDosen.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" antrian\n");
        }

        sb.append("\n").append("=".repeat(80)).append("\n");

        return sb.toString();
    }
}