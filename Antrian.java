
// File: Antrian.java
import java.io.Serializable;
import java.util.Date;

public class Antrian implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String namaMahasiswa;
    private String npm;
    private String keperluan;
    private Date waktu;
    private Date waktuSelesai;
    private String status;
    private String prioritas; // "Normal", "Menengah", "Tinggi", "Urgent"
    private String kategori; // "Bimbingan Skripsi", "Konsultasi Nilai", "Administrasi", "Lainnya"
    private String dosen; // Nama dosen yang dipilih
    private Date jadwalBooking; // Untuk fitur booking
    private String catatan;

    public Antrian(String id, String nama, String npm, String keperluan,
            String prioritas, String kategori, String dosen, Date jadwalBooking) {
        this.id = id;
        this.namaMahasiswa = nama;
        this.npm = npm;
        this.keperluan = keperluan;
        this.waktu = new Date();
        this.status = "Menunggu";
        this.prioritas = prioritas;
        this.kategori = kategori;
        this.dosen = dosen;
        this.jadwalBooking = jadwalBooking;
        this.catatan = "";
    }

    // Constructor untuk backward compatibility
    public Antrian(String id, String nama, String npm, String keperluan) {
        this(id, nama, npm, keperluan, "Normal", "Lainnya", "Umum", null);
    }

    public String getId() {
        return id;
    }

    public String getNamaMahasiswa() {
        return namaMahasiswa;
    }

    public String getNpm() {
        return npm;
    }

    public String getKeperluan() {
        return keperluan;
    }

    public Date getWaktu() {
        return waktu;
    }

    public Date getWaktuSelesai() {
        return waktuSelesai;
    }

    public String getStatus() {
        return status;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public String getKategori() {
        return kategori;
    }

    public String getDosen() {
        return dosen;
    }

    public Date getJadwalBooking() {
        return jadwalBooking;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setStatus(String status) {
        this.status = status;
        if (status.equals("Selesai")) {
            this.waktuSelesai = new Date();
        }
    }

    public void setWaktuSelesai(Date waktuSelesai) {
        this.waktuSelesai = waktuSelesai;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    // Hitung durasi layanan dalam menit
    public long getDurasiLayanan() {
        if (waktuSelesai != null && waktu != null) {
            return (waktuSelesai.getTime() - waktu.getTime()) / (1000 * 60);
        }
        return 0;
    }

    // Nilai prioritas untuk sorting (semakin kecil semakin prioritas)
    public int getNilaiPrioritas() {
        switch (prioritas) {
            case "Urgent":
                return 1;
            case "Tinggi":
                return 2;
            case "Menengah":
                return 3;
            case "Normal":
                return 4;
            default:
                return 5;
        }
    }
}