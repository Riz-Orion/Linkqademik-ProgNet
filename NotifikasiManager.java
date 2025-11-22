
// File: NotifikasiManager.java
import javax.sound.sampled.*;
import java.io.*;

public class NotifikasiManager {

    // Play sound notification
    public static void playNotification() {
        new Thread(() -> {
            try {
                // Generate simple beep sound
                AudioFormat af = new AudioFormat(44100, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                byte[] buf = new byte[1000];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (44100.0 / 880) * 2.0 * Math.PI;
                    buf[i] = (byte) (Math.sin(angle) * 127.0);
                }

                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                // Jika tidak bisa play sound, skip saja
                System.out.println("Cannot play sound: " + e.getMessage());
            }
        }).start();
    }

    // Estimasi waktu tunggu berdasarkan posisi antrian
    public static String estimasiWaktu(int posisiAntrian, double rataRataLayanan) {
        if (rataRataLayanan == 0) {
            rataRataLayanan = 10; // Default 10 menit per layanan
        }

        int estimasiMenit = (int) (posisiAntrian * rataRataLayanan);

        if (estimasiMenit < 60) {
            return estimasiMenit + " menit";
        } else {
            int jam = estimasiMenit / 60;
            int menit = estimasiMenit % 60;
            return jam + " jam " + menit + " menit";
        }
    }

    // Cek apakah sudah waktunya untuk notifikasi
    public static boolean perluNotifikasi(int posisiAntrian) {
        // Notifikasi jika tinggal 3 antrian atau kurang
        return posisiAntrian <= 3;
    }
}