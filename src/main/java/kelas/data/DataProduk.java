/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kelas.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kelas.model.Produk;

public class DataProduk {
    private final ObservableList<Produk> daftar = FXCollections.observableArrayList();
    private int urutan = 1;

    public DataProduk() {
        buatTabelJikaBelumAda();
        muatDariDatabase();
    }

    public ObservableList<Produk> getDaftar() {
        return FXCollections.unmodifiableObservableList(daftar);
    }

    public Produk tambah(String nama, String kategori, long harga) {
        Produk p = new Produk(String.format("PRD%03d", urutan), nama, kategori, harga);

        String sql = "INSERT INTO produk (kode, nama, kategori, harga) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, p.getKode());
            statement.setString(2, p.getNama());
            statement.setString(3, p.getKategori());
            statement.setLong(4, p.getHarga());
            statement.executeUpdate();

            daftar.add(p);
            urutan++;
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan produk ke database.", e);
        }
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
            urutan = 1;

            while (result.next()) {
                Produk produk = new Produk(
                        result.getString("kode"),
                        result.getString("nama"),
                        result.getString("kategori"),
                        result.getLong("harga")
                );
                daftar.add(produk);
                urutan++;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data produk dari database.", e);
        }
    }
}
