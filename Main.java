
// File: Main.java
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = { "Admin", "Mahasiswa" };
            int choice = JOptionPane.showOptionDialog(null,
                    "Pilih mode aplikasi:",
                    "Sistem Antrian Bimbingan Dosen",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                new AdminGUI().setVisible(true);
            } else if (choice == 1) {
                new ClientGUI().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}