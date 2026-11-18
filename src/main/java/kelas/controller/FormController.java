package kelas.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import kelas.Navigator;
import kelas.data.DataProduk;
import kelas.model.Produk;

public class FormController {
    @FXML private Label judulLabel;
    @FXML private TextField namaField;
    @FXML private ComboBox<String> kategoriBox;
    @FXML private TextField hargaField;
    @FXML private Button simpanButton;
    @FXML private Label pesanLabel;

    private Navigator navigator;
    private DataProduk data;
    private Produk produkYangDiedit;

    @FXML
    private void initialize() {
        kategoriBox.getItems().addAll("Makanan", "Minuman");
        kategoriBox.setValue("Makanan");
    }

    public void setKonteks(Navigator navigator, DataProduk data) {
        this.navigator = navigator;
        this.data = data;
    }

    public void setProdukUntukDiedit(Produk produk) {
        produkYangDiedit = produk;
        judulLabel.setText("Ubah Produk");
        namaField.setText(produk.getNama());
        kategoriBox.setValue(produk.getKategori());
        hargaField.setText(String.valueOf(produk.getHarga()));
        simpanButton.setText("Simpan Perubahan");
    }

    @FXML
    private void simpan() {
        Produk produk;
        try {
            long harga = Long.parseLong(hargaField.getText().trim());
            if (produkYangDiedit == null) {
                produk = data.tambah(namaField.getText(), kategoriBox.getValue(), harga);
            } else {
                produk = data.ubah(produkYangDiedit.getKode(), namaField.getText(),
                        kategoriBox.getValue(), harga);
            }
        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus berupa angka.");
            return;
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
            return;
        } catch (RuntimeException e) {
            pesanLabel.setText(e.getMessage());
            return;
        }

        try {
            navigator.bukaDetail(produk);
        } catch (IOException e) {
            pesanLabel.setText("Detail gagal dibuka.");
            e.printStackTrace();
        }
    }

    @FXML
    private void bukaDaftar() {
        try {
            navigator.bukaDaftar();
        } catch (IOException e) {
            pesanLabel.setText("Daftar gagal dibuka.");
            e.printStackTrace();
        }
    }
}