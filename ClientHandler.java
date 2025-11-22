
// File: ClientHandler.java
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private Socket socket;
    private AntrianServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean running = true;

    public ClientHandler(Socket socket, AntrianServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (running && !socket.isClosed()) {
                try {
                    Object obj = in.readObject();

                    if (obj instanceof String) {
                        String command = (String) obj;

                        if (command.equals("DAFTAR")) {
                            handleDaftar();
                        } else if (command.equals("GET_LIST")) {
                            handleGetList();
                        } else if (command.equals("GET_POSISI")) {
                            handleGetPosisi();
                        } else if (command.equals("GET_STATISTIK")) {
                            handleGetStatistik();
                        }
                    }
                } catch (EOFException | SocketException e) {
                    // Client disconnected
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private synchronized void handleDaftar() {
        try {
            String nama = (String) in.readObject();
            String npm = (String) in.readObject();
            String keperluan = (String) in.readObject();
            String prioritas = (String) in.readObject();
            String kategori = (String) in.readObject();
            String dosen = (String) in.readObject();
            Date jadwalBooking = (Date) in.readObject();

            String id = "A" + System.currentTimeMillis();
            Antrian antrian = new Antrian(id, nama, npm, keperluan, prioritas, kategori, dosen, jadwalBooking);
            server.tambahAntrian(antrian);

            out.writeObject("SUCCESS");
            out.writeObject(id);
            out.flush();
        } catch (Exception e) {
            try {
                out.writeObject("ERROR");
                out.writeObject(e.getMessage());
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private synchronized void handleGetList() {
        try {
            List<Antrian> list = server.getDaftarAntrian();
            out.writeObject(list);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleGetPosisi() {
        try {
            String id = (String) in.readObject();
            int posisi = server.getPosisiAntrian(id);
            double rataRata = server.getStatistikManager().getRataRataWaktuLayanan();

            out.writeObject(Integer.valueOf(posisi));
            out.writeObject(NotifikasiManager.estimasiWaktu(posisi, rataRata));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleGetStatistik() {
        try {
            String laporan = server.getStatistikManager().generateLaporan();
            out.writeObject(laporan);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void kirimUpdate() {
        try {
            if (out != null && !socket.isClosed() && running) {
                out.reset();
                out.writeObject("UPDATE");
                List<Antrian> list = server.getDaftarAntrian();
                // Kirim list baru untuk menghindari reference issues
                out.writeObject(new ArrayList<>(list));
                out.flush();
            }
        } catch (IOException e) {
            running = false;
        }
    }

    public void close() {
        try {
            running = false;
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}