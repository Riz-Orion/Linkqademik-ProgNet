
// File: AdminGUI.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AdminGUI extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private AntrianServer server;
    private JButton btnLayani, btnSelesai, btnHapus, btnRefresh, btnStatistik;
    private JComboBox<String> filterDosen;
    private JLabel lblTotalAntrian, lblRataRata;

    public AdminGUI() {
        setTitle("Admin - Sistem Antrian Bimbingan Dosen");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();

        server = new AntrianServer(this);
        server.start();

        // Timer untuk update statistik
        javax.swing.Timer statsTimer = new javax.swing.Timer(5000, e -> updateStatistik());
        statsTimer.start();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(0, 80));

        JLabel lblTitle = new JLabel("SISTEM ANTRIAN BIMBINGAN DOSEN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        // Panel statistik mini
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(new Color(41, 128, 185));
        lblTotalAntrian = new JLabel("Total Hari Ini: 0");
        lblRataRata = new JLabel("Rata-rata: 0 menit");
        lblTotalAntrian.setForeground(Color.WHITE);
        lblRataRata.setForeground(Color.WHITE);
        statsPanel.add(lblTotalAntrian);
        statsPanel.add(new JLabel("  |  "));
        statsPanel.add(lblRataRata);

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Panel Tengah - Table
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter Dosen:"));
        filterDosen = new JComboBox<>(new String[] { "Semua", "Umum", "Dr. Budi", "Dr. Siti", "Dr. Ahmad" });
        filterDosen.addActionListener(e -> updateTable());
        filterPanel.add(filterDosen);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = { "No. Antrian", "Nama", "NPM", "Prioritas", "Kategori", "Dosen", "Keperluan", "Waktu",
                "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setFont(new Font("Arial", Font.PLAIN, 10));

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnLayani = new JButton("▶ Layani");
        btnSelesai = new JButton("✓ Selesai");
        btnHapus = new JButton("✗ Hapus");
        btnStatistik = new JButton("📊 Statistik");
        btnRefresh = new JButton("🔄 Refresh");

        btnLayani.setBackground(new Color(46, 204, 113));
        btnLayani.setForeground(Color.WHITE);
        btnLayani.setFocusPainted(false);

        btnSelesai.setBackground(new Color(52, 152, 219));
        btnSelesai.setForeground(Color.WHITE);
        btnSelesai.setFocusPainted(false);

        btnHapus.setBackground(new Color(231, 76, 60));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFocusPainted(false);

        btnStatistik.setBackground(new Color(155, 89, 182));
        btnStatistik.setForeground(Color.WHITE);
        btnStatistik.setFocusPainted(false);

        btnRefresh.setBackground(new Color(52, 73, 94));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);

        buttonPanel.add(btnLayani);
        buttonPanel.add(btnSelesai);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnStatistik);
        buttonPanel.add(btnRefresh);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Panel Bawah - Log
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        logPanel.setPreferredSize(new Dimension(0, 150));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);

        logPanel.add(new JLabel("Log Aktivitas:"), BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);

        // Event Handlers
        btnLayani.addActionListener(e -> layaniAntrian());
        btnSelesai.addActionListener(e -> selesaikanAntrian());
        btnHapus.addActionListener(e -> hapusAntrian());
        btnStatistik.addActionListener(e -> tampilkanStatistik());
        btnRefresh.addActionListener(e -> updateTable());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                server.stopServer();
            }
        });
    }

    public void updateTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            List<Antrian> list = server.getDaftarAntrian();

            String selectedDosen = (String) filterDosen.getSelectedItem();

            for (Antrian a : list) {
                if (!selectedDosen.equals("Semua") && !a.getDosen().equals(selectedDosen)) {
                    continue;
                }

                Object[] row = new Object[] {
                        a.getId(),
                        a.getNamaMahasiswa(),
                        a.getNpm(),
                        a.getPrioritas(),
                        a.getKategori(),
                        a.getDosen(),
                        a.getKeperluan().length() > 30 ? a.getKeperluan().substring(0, 30) + "..." : a.getKeperluan(),
                        sdf.format(a.getWaktu()),
                        a.getStatus()
                };
                tableModel.addRow(row);

            }
        });
    }

    private void layaniAntrian() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            server.updateStatus(id, "Sedang Dilayani");
            log("Melayani antrian: " + id);
            NotifikasiManager.playNotification();
        } else {
            JOptionPane.showMessageDialog(this, "Pilih antrian terlebih dahulu!");
        }
    }

    private void selesaikanAntrian() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            server.selesaikanAntrian(id);
            log("Antrian selesai: " + id);
            NotifikasiManager.playNotification();
        } else {
            JOptionPane.showMessageDialog(this, "Pilih antrian terlebih dahulu!");
        }
    }

    private void hapusAntrian() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Hapus antrian ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                server.hapusAntrian(id);
                log("Antrian dihapus: " + id);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih antrian terlebih dahulu!");
        }
    }

    private void tampilkanStatistik() {
        String laporan = server.getStatistikManager().generateLaporan();

        JTextArea textArea = new JTextArea(laporan);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Laporan Statistik", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatistik() {
        int total = server.getStatistikManager().getTotalAntrianHariIni();
        double rataRata = server.getStatistikManager().getRataRataWaktuLayanan();

        lblTotalAntrian.setText("Total Hari Ini: " + total);
        lblRataRata.setText(String.format("Rata-rata: %.1f menit", rataRata));
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            logArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminGUI().setVisible(true);
        });
    }
}