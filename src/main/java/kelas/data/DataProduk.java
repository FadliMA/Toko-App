/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kelas.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kelas.model.Produk;

public class DataProduk {
    private final ObservableList<Produk> daftar = FXCollections.observableArrayList();

    public DataProduk() {
        buatTabelJikaBelumAda();
        muatDariDatabase();
    }

    public ObservableList<Produk> getDaftar() {
        return FXCollections.unmodifiableObservableList(daftar);
    }

    public Produk tambah(String nama, String kategori, long harga) {
        Produk p = new Produk(kodeBerikutnya(), nama, kategori, harga);

        String sql = "INSERT INTO produk (kode, nama, kategori, harga) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, p.getKode());
            statement.setString(2, p.getNama());
            statement.setString(3, p.getKategori());
            statement.setLong(4, p.getHarga());
            statement.executeUpdate();

            daftar.add(p);
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan produk ke database.", e);
        }
    }

    public Produk ubah(String kode, String nama, String kategori, long harga) {
        int index = cariIndex(kode);
        if (index < 0) {
            throw new IllegalArgumentException("Produk tidak ditemukan.");
        }

        Produk produk = new Produk(kode, nama, kategori, harga);
        String sql = "UPDATE produk SET nama = ?, kategori = ?, harga = ? WHERE kode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, produk.getNama());
            statement.setString(2, produk.getKategori());
            statement.setLong(3, produk.getHarga());
            statement.setString(4, produk.getKode());
            if (statement.executeUpdate() == 0) {
                try (PreparedStatement cek = conn.prepareStatement(
                        "SELECT 1 FROM produk WHERE kode = ?")) {
                    cek.setString(1, produk.getKode());
                    try (ResultSet result = cek.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalStateException(
                                    "Produk tidak ditemukan di database.");
                        }
                    }
                }
            }

            daftar.set(index, produk);
            return produk;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui produk di database.", e);
        }
    }

    public void hapus(String kode) {
        int index = cariIndex(kode);
        if (index < 0) {
            throw new IllegalArgumentException("Produk tidak ditemukan.");
        }

        String sql = "DELETE FROM produk WHERE kode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, kode);
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException("Produk tidak ditemukan di database.");
            }

            daftar.remove(index);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus produk dari database.", e);
        }
    }

    private int cariIndex(String kode) {
        for (int i = 0; i < daftar.size(); i++) {
            if (daftar.get(i).getKode().equals(kode)) {
                return i;
            }
        }
        return -1;
    }

    private String kodeBerikutnya() {
        int urutan = 1;
        String kode = String.format("PRD%03d", urutan);
        while (cariIndex(kode) >= 0) {
            urutan++;
            kode = String.format("PRD%03d", urutan);
        }
        return kode;
    }

    private void buatTabelJikaBelumAda() {
        String sql = """
                CREATE TABLE IF NOT EXISTS produk (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    kode VARCHAR(20) NOT NULL UNIQUE,
                    nama VARCHAR(100) NOT NULL,
                    kategori VARCHAR(50) NOT NULL,
                    harga BIGINT NOT NULL
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghubungkan atau membuat tabel produk.", e);
        }
    }

    private void muatDariDatabase() {
        String sql = "SELECT kode, nama, kategori, harga FROM produk ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            daftar.clear();

            while (result.next()) {
                Produk produk = new Produk(
                        result.getString("kode"),
                        result.getString("nama"),
                        result.getString("kategori"),
                        result.getLong("harga")
                );
                daftar.add(produk);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data produk dari database.", e);
        }
    }
}
