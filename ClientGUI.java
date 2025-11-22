
// File: ClientGUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ClientGUI extends JFrame {
    private JTextField txtNama, txtNIM, txtTanggal;
    private JTextArea txtKeperluan, areaAntrian;
    private JComboBox<String> cbPrioritas, cbKategori, cbDosen, cbJam, cbMenit;
    private JCheckBox chkBooking;
    private JButton btnDaftar, btnCekPosisi, btnStatistik;
    private JLabel lblEstimasi, lblPosisi;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;
    private Object lock = new Object();
    private String myAntrianId = null;
    private int lastNotifiedPosition = -1;

    public ClientGUI() {
        setTitle("Mahasiswa - Pendaftaran Antrian Bimbingan");
        setSize(750, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        connectToServer();

        // Timer untuk auto-check posisi
        javax.swing.Timer posisiTimer = new javax.swing.Timer(10000, e -> autoCheckPosisi());
        posisiTimer.start();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel lblTitle = new JLabel("PENDAFTARAN ANTRIAN BIMBINGAN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Main Panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Form Pendaftaran
        JPanel formPanel = createFormPanel();
        tabbedPane.addTab("📝 Daftar Antrian", formPanel);

        // Tab 2: Daftar Antrian
        JPanel listPanel = createListPanel();
        tabbedPane.addTab("📋 Daftar Antrian", listPanel);

        add(tabbedPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Nama
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Nama Mahasiswa:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNama = new JTextField(20);
        panel.add(txtNama, gbc);
        row++;

        // NIM
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("NIM:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNIM = new JTextField(20);
        panel.add(txtNIM, gbc);
        row++;

        // Prioritas
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Prioritas:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cbPrioritas = new JComboBox<>(new String[] { "Normal", "Menengah", "Tinggi", "Urgent" });
        cbPrioritas.setToolTipText("Pilih prioritas antrian Anda");
        panel.add(cbPrioritas, gbc);
        row++;

        // Kategori
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cbKategori = new JComboBox<>(new String[] {
                "Bimbingan Skripsi", "Konsultasi Nilai", "Administrasi", "Konsultasi Akademik", "Lainnya"
        });
        panel.add(cbKategori, gbc);
        row++;

        // Dosen
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Dosen:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cbDosen = new JComboBox<>(new String[] { "Umum", "Dr. Budi", "Dr. Siti", "Dr. Ahmad" });
        panel.add(cbDosen, gbc);
        row++;

        // Booking checkbox
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        chkBooking = new JCheckBox("Jadwalkan untuk waktu tertentu");
        chkBooking.addActionListener(e -> toggleBooking());
        panel.add(chkBooking, gbc);
        gbc.gridwidth = 1;
        row++;

        // Date & Time picker
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lblTanggal = new JLabel("Tanggal & Waktu:");
        panel.add(lblTanggal, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Tanggal field (format: dd/MM/yyyy)
        txtTanggal = new JTextField(10);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtTanggal.setText(sdf.format(new Date()));
        txtTanggal.setEnabled(false);
        txtTanggal.setToolTipText("Format: dd/MM/yyyy");
        dateTimePanel.add(txtTanggal);

        // Jam
        String[] jam = new String[24];
        for (int i = 0; i < 24; i++) {
            jam[i] = String.format("%02d", i);
        }
        cbJam = new JComboBox<>(jam);
        cbJam.setEnabled(false);
        cbJam.setSelectedIndex(8); // Default 08:00
        dateTimePanel.add(cbJam);
        dateTimePanel.add(new JLabel(":"));

        // Menit
        String[] menit = { "00", "15", "30", "45" };
        cbMenit = new JComboBox<>(menit);
        cbMenit.setEnabled(false);
        dateTimePanel.add(cbMenit);

        panel.add(dateTimePanel, gbc);
        row++;

        // Keperluan
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Keperluan:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtKeperluan = new JTextArea(4, 20);
        txtKeperluan.setLineWrap(true);
        txtKeperluan.setWrapStyleWord(true);
        JScrollPane scrollKeperluan = new JScrollPane(txtKeperluan);
        panel.add(scrollKeperluan, gbc);
        row++;

        // Button
        gbc.gridx = 1;
        gbc.gridy = row;
        btnDaftar = new JButton("Daftar Antrian");
        btnDaftar.setBackground(new Color(41, 128, 185));
        btnDaftar.setForeground(Color.WHITE);
        btnDaftar.setFocusPainted(false);
        btnDaftar.setPreferredSize(new Dimension(150, 35));
        btnDaftar.addActionListener(e -> daftarAntrian());
        panel.add(btnDaftar, gbc);
        row++;

        // Status panel
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status Antrian Anda"));
        lblPosisi = new JLabel("Belum ada antrian");
        lblEstimasi = new JLabel("");
        lblPosisi.setFont(new Font("Arial", Font.BOLD, 12));
        lblEstimasi.setFont(new Font("Arial", Font.PLAIN, 11));
        statusPanel.add(lblPosisi);
        statusPanel.add(lblEstimasi);
        panel.add(statusPanel, gbc);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCekPosisi = new JButton("🔍 Cek Posisi Saya");
        btnStatistik = new JButton("📊 Lihat Statistik");

        btnCekPosisi.addActionListener(e -> cekPosisi());
        btnStatistik.addActionListener(e -> lihatStatistik());

        topPanel.add(btnCekPosisi);
        topPanel.add(btnStatistik);
        panel.add(topPanel, BorderLayout.NORTH);

        areaAntrian = new JTextArea();
        areaAntrian.setEditable(false);
        areaAntrian.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollAntrian = new JScrollPane(areaAntrian);
        panel.add(scrollAntrian, BorderLayout.CENTER);

        return panel;
    }

    private void toggleBooking() {
        boolean enabled = chkBooking.isSelected();
        txtTanggal.setEnabled(enabled);
        cbJam.setEnabled(enabled);
        cbMenit.setEnabled(enabled);
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;

            new Thread(() -> {
                try {
                    while (connected) {
                        try {
                            Object obj = in.readObject();

                            if (obj instanceof String) {
                                String msg = (String) obj;
                                if (msg.equals("UPDATE")) {
                                    Object nextObj = in.readObject();
                                    if (nextObj instanceof List) {
                                        List<Antrian> list = (List<Antrian>) nextObj;
                                        updateAntrianDisplay(list);
                                    }
                                }
                            } else if (obj instanceof List) {
                                List<Antrian> list = (List<Antrian>) obj;
                                updateAntrianDisplay(list);
                            }
                        } catch (EOFException | SocketException e) {
                            // Connection closed
                            break;
                        } catch (ClassNotFoundException e) {
                            System.err.println("Class not found: " + e.getMessage());
                        } catch (IOException e) {
                            System.err.println("IO Error: " + e.getMessage());
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    connected = false;
                }
            }).start();

            synchronized (lock) {
                out.writeObject("GET_LIST");
                out.flush();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Tidak dapat terhubung ke server!\nPastikan server berjalan.",
                    "Error Koneksi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void daftarAntrian() {
        String nama = txtNama.getText().trim();
        String nim = txtNIM.getText().trim();
        String keperluan = txtKeperluan.getText().trim();

        if (nama.isEmpty() || nim.isEmpty() || keperluan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        new Thread(() -> {
            try {
                String prioritas = (String) cbPrioritas.getSelectedItem();
                String kategori = (String) cbKategori.getSelectedItem();
                String dosen = (String) cbDosen.getSelectedItem();

                Date jadwalBooking = null;
                if (chkBooking.isSelected()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String tanggal = txtTanggal.getText();
                        String jam = (String) cbJam.getSelectedItem();
                        String menit = (String) cbMenit.getSelectedItem();
                        String dateTimeStr = tanggal + " " + jam + ":" + menit;
                        jadwalBooking = sdf.parse(dateTimeStr);
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid!");
                        });
                        return;
                    }
                }

                synchronized (lock) {
                    out.writeObject("DAFTAR");
                    out.writeObject(nama);
                    out.writeObject(nim);
                    out.writeObject(keperluan);
                    out.writeObject(prioritas);
                    out.writeObject(kategori);
                    out.writeObject(dosen);
                    out.writeObject(jadwalBooking);
                    out.flush();

                    String response = (String) in.readObject();
                    if (response.equals("SUCCESS")) {
                        String id = (String) in.readObject();
                        myAntrianId = id;

                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this,
                                    "Pendaftaran berhasil!\nNomor antrian: " + id +
                                            "\nPrioritas: " + prioritas +
                                            "\nKategori: " + kategori,
                                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

                            txtNama.setText("");
                            txtNIM.setText("");
                            txtKeperluan.setText("");
                            cbPrioritas.setSelectedIndex(0);
                            cbKategori.setSelectedIndex(0);

                            cekPosisi();
                        });
                    }
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void cekPosisi() {
        if (myAntrianId == null) {
            JOptionPane.showMessageDialog(this, "Anda belum mendaftar antrian!");
            return;
        }

        new Thread(() -> {
            try {
                synchronized (lock) {
                    out.writeObject("GET_POSISI");
                    out.writeObject(myAntrianId);
                    out.flush();

                    Object posisiObj = in.readObject();
                    Object estimasiObj = in.readObject();

                    if (posisiObj == null || estimasiObj == null) {
                        SwingUtilities.invokeLater(() -> {
                            lblPosisi.setText("Error: Data tidak valid");
                            lblEstimasi.setText("");
                        });
                        return;
                    }

                    int posisi = ((Integer) posisiObj).intValue();
                    String estimasi = (String) estimasiObj;

                    SwingUtilities.invokeLater(() -> {
                        if (posisi > 0) {
                            lblPosisi.setText("Posisi antrian Anda: " + posisi);
                            lblEstimasi.setText("Estimasi tunggu: " + estimasi);

                            // Notifikasi jika tinggal 3 atau kurang
                            if (NotifikasiManager.perluNotifikasi(posisi) && posisi != lastNotifiedPosition) {
                                NotifikasiManager.playNotification();
                                JOptionPane.showMessageDialog(this,
                                        "Antrian Anda tinggal " + posisi + " lagi!\nSegera bersiap.",
                                        "Notifikasi", JOptionPane.INFORMATION_MESSAGE);
                                lastNotifiedPosition = posisi;
                            }
                        } else {
                            lblPosisi.setText("Antrian tidak ditemukan");
                            lblEstimasi.setText("");
                        }
                    });
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found error: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error checking position: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void autoCheckPosisi() {
        if (myAntrianId != null) {
            cekPosisi();
        }
    }

    private void lihatStatistik() {
        new Thread(() -> {
            try {
                synchronized (lock) {
                    out.writeObject("GET_STATISTIK");
                    out.flush();

                    Object obj = in.readObject();
                    if (obj == null) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Gagal mendapatkan statistik");
                        });
                        return;
                    }

                    String laporan = (String) obj;

                    SwingUtilities.invokeLater(() -> {
                        JTextArea textArea = new JTextArea(laporan);
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(600, 400));

                        JOptionPane.showMessageDialog(this, scrollPane,
                                "Statistik Antrian", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error mendapatkan statistik: " + e.getMessage());
                });
            }
        }).start();
    }

    private void updateAntrianDisplay(List<Antrian> list) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            sb.append(String.format("%-12s %-18s %-10s %-12s %-10s %-12s %s\n",
                    "No.Antrian", "Nama", "Prioritas", "Kategori", "Dosen", "Status", "Waktu"));
            sb.append("=".repeat(95)).append("\n");

            for (Antrian a : list) {
                sb.append(String.format("%-12s %-18s %-10s %-12s %-10s %-12s %s\n",
                        a.getId(),
                        truncate(a.getNamaMahasiswa(), 18),
                        a.getPrioritas(),
                        truncate(a.getKategori(), 12),
                        a.getDosen(),
                        a.getStatus(),
                        sdf.format(a.getWaktu())));
            }

            areaAntrian.setText(sb.toString());
        });
    }

    private String truncate(String str, int len) {
        return str.length() > len ? str.substring(0, len - 2) + ".." : str;
    }

    private void disconnect() {
        try {
            connected = false;
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}