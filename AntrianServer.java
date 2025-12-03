import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AntrianServer extends Thread {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private List<Antrian> daftarAntrian;
    private List<ClientHandler> clients;
    private AdminGUI adminGUI;
    private StatistikManager statistikManager;

    public AntrianServer(AdminGUI gui) {
        this.adminGUI = gui;
        this.daftarAntrian = new CopyOnWriteArrayList<>();
        this.clients = new CopyOnWriteArrayList<>();
        this.statistikManager = new StatistikManager();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            adminGUI.log("Server berjalan di port " + PORT);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.start();
                adminGUI.log("Klien terhubung: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                adminGUI.log("Error server: " + e.getMessage());
            }
        }
    }

    public synchronized void tambahAntrian(Antrian antrian) {
        daftarAntrian.add(antrian);
        sortAntrianByPrioritas();
        adminGUI.updateTable();
        notifikasiSemuaClient();
        adminGUI.log("Antrian baru: " + antrian.getId() + " - " + antrian.getNamaMahasiswa() +
                " (Prioritas: " + antrian.getPrioritas() + ")");
    }

    public synchronized void hapusAntrian(String id) {
        Antrian removed = null;
        for (Antrian a : daftarAntrian) {
            if (a.getId().equals(id)) {
                removed = a;
                break;
            }
        }

        daftarAntrian.removeIf(a -> a.getId().equals(id));

        if (removed != null) {
            statistikManager.tambahKeHistory(removed);
        }

        adminGUI.updateTable();
        notifikasiSemuaClient();
    }

    public synchronized void updateStatus(String id, String status) {
        for (Antrian a : daftarAntrian) {
            if (a.getId().equals(id)) {
                a.setStatus(status);
                if (status.equals("Selesai")) {
                    a.setWaktuSelesai(new Date());
                }
                break;
            }
        }
        adminGUI.updateTable();
        notifikasiSemuaClient();
    }

    public synchronized void selesaikanAntrian(String id) {
        Antrian completed = null;
        for (Antrian a : daftarAntrian) {
            if (a.getId().equals(id)) {
                a.setStatus("Selesai");
                a.setWaktuSelesai(new Date());
                completed = a;
                break;
            }
        }

        if (completed != null) {
            statistikManager.tambahKeHistory(completed);
            daftarAntrian.removeIf(a -> a.getId().equals(id));
        }

        adminGUI.updateTable();
        notifikasiSemuaClient();
    }

    // Sort antrian berdasarkan prioritas dan waktu
    private void sortAntrianByPrioritas() {
        List<Antrian> sorted = daftarAntrian.stream()
                .filter(a -> !a.getStatus().equals("Sedang Dilayani"))
                .sorted((a1, a2) -> {
                    // Jika ada jadwal booking yang sudah lewat, prioritaskan
                    Date now = new Date();
                    boolean a1Overdue = a1.getJadwalBooking() != null && a1.getJadwalBooking().before(now);
                    boolean a2Overdue = a2.getJadwalBooking() != null && a2.getJadwalBooking().before(now);

                    if (a1Overdue && !a2Overdue)
                        return -1;
                    if (!a1Overdue && a2Overdue)
                        return 1;

                    // Sort by prioritas
                    int prioritasComp = Integer.compare(a1.getNilaiPrioritas(), a2.getNilaiPrioritas());
                    if (prioritasComp != 0)
                        return prioritasComp;

                    // Jika prioritas sama, sort by waktu
                    return a1.getWaktu().compareTo(a2.getWaktu());
                })
                .collect(Collectors.toList());

        // Tambahkan kembali antrian yang sedang dilayani di awal
        List<Antrian> sedangDilayani = daftarAntrian.stream()
                .filter(a -> a.getStatus().equals("Sedang Dilayani"))
                .collect(Collectors.toList());

        daftarAntrian.clear();
        daftarAntrian.addAll(sedangDilayani);
        daftarAntrian.addAll(sorted);
    }

    public List<Antrian> getDaftarAntrian() {
        return new ArrayList<>(daftarAntrian);
    }

    public List<Antrian> getDaftarAntrianByDosen(String dosen) {
        return daftarAntrian.stream()
                .filter(a -> a.getDosen().equals(dosen))
                .collect(Collectors.toList());
    }

    public StatistikManager getStatistikManager() {
        return statistikManager;
    }

    public void notifikasiSemuaClient() {
        // Buat list copy untuk menghindari concurrent modification
        List<ClientHandler> clientsCopy = new ArrayList<>(clients);
        for (ClientHandler client : clientsCopy) {
            try {
                client.kirimUpdate();
            } catch (Exception e) {
                // Jika client error, remove dari list
                clients.remove(client);
            }
        }
    }

    public void stopServer() {
        try {
            for (ClientHandler client : clients) {
                client.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hitung posisi antrian untuk mahasiswa tertentu
    public int getPosisiAntrian(String id) {
        int posisi = 1;
        for (Antrian a : daftarAntrian) {
            if (a.getId().equals(id)) {
                return posisi;
            }
            if (!a.getStatus().equals("Sedang Dilayani")) {
                posisi++;
            }
        }
        return -1;
    }
}