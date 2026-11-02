package bodyfat;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;


public class BodyFatController {

    
    private static final double SELISIH_MAKS = 8.0;

    @FXML private TextField tfUsia, tfBerat, tfTinggi, tfPinggang, tfLeher, tfPinggul;
    @FXML private RadioButton rbLaki, rbPerempuan;
    @FXML private ToggleGroup tgGender;
    @FXML private Label lblError, lblBodyFat, lblKategori, lblBmi, lblMassa, lblCatatan;

    @FXML
    private void hitung() {
        lblError.setText("");
        lblCatatan.setText("");

        if (tgGender.getSelectedToggle() == null) {
            lblError.setText("Pilih jenis kelamin dulu.");
            return;
        }

       
        Double usia, berat, tinggi, pinggang, leher, pinggul;
        try {
            usia = baca(tfUsia);
            berat = baca(tfBerat);
            tinggi = baca(tfTinggi);
            pinggang = baca(tfPinggang);
            leher = baca(tfLeher);
            pinggul = baca(tfPinggul);
        } catch (NumberFormatException e) {
            lblError.setText("Isian harus berupa angka.");
            return;
        }

        
        if (usia == null || berat == null || tinggi == null) {
            lblError.setText("Usia, tinggi, dan berat wajib diisi.");
            return;
        }
        if (usia < 18 || usia > 79) {
            lblError.setText("Usia harus 18 - 79 tahun.");
            return;
        }
        if (berat < 25 || berat > 300) {
            lblError.setText("Berat harus 25 - 300 kg.");
            return;
        }
        if (tinggi < 120 || tinggi > 230) {
            lblError.setText("Tinggi harus 120 - 230 cm.");
            return;
        }
        if (pinggang != null && (pinggang < 40 || pinggang > 200)) {
            lblError.setText("Pinggang harus 40 - 200 cm.");
            return;
        }
        if (leher != null && (leher < 20 || leher > 70)) {
            lblError.setText("Leher harus 20 - 70 cm.");
            return;
        }
        if (pinggul != null && (pinggul < 50 || pinggul > 200)) {
            lblError.setText("Pinggul harus 50 - 200 cm.");
            return;
        }

        boolean laki = rbLaki.isSelected();
        double kodePria = laki ? 1 : 0;     // Deurenberg & Gallagher: pria = 1
        double kodeWanita = laki ? 0 : 1;   // CUN-BAE: wanita = 1

        // ini bmi
        double tinggiM = tinggi / 100.0;
        double bmi = berat / (tinggiM * tinggiM);

        
        double deurenberg = 1.20 * bmi + 0.23 * usia - 10.8 * kodePria - 5.4;

        double gallagher = 64.5 - 848.0 / bmi + 0.079 * usia - 16.4 * kodePria
                + 0.05 * kodePria * usia + 39.0 * kodePria / bmi;

        double cunBae = -44.988 + 0.503 * usia + 10.689 * kodeWanita + 3.172 * bmi
                - 0.026 * bmi * bmi + 0.181 * bmi * kodeWanita - 0.02 * bmi * usia
                - 0.005 * bmi * bmi * kodeWanita + 0.00021 * bmi * bmi * usia;

        // ini navy
        Double navy = null;
        boolean navyLengkap = pinggang != null && leher != null && (laki || pinggul != null);
        if (navyLengkap) {
            if (laki) {
                if (pinggang <= leher) {
                    lblError.setText("Pinggang harus lebih besar dari leher.");
                    return;
                }
                navy = 495.0 / (1.0324 - 0.19077 * Math.log10(pinggang - leher)
                        + 0.15456 * Math.log10(tinggi)) - 450.0;
            } else {
                double d = pinggang + pinggul - leher;
                navy = 495.0 / (1.29579 - 0.35004 * Math.log10(d)
                        + 0.22100 * Math.log10(tinggi)) - 450.0;
            }
        }

        // gabung
        double rataBmi = (deurenberg + gallagher + cunBae) / 3.0;
        double akhir = rataBmi;
        if (navy != null) {
            if (Math.abs(navy - rataBmi) <= SELISIH_MAKS) {
                akhir = (rataBmi + navy) / 2.0;
            } else {
                lblCatatan.setText(String.format(
                        "Navy (%.1f %%) tidak dipakai: beda terlalu jauh, cek ukuran lingkar.", navy));
            }
        }

        
        int level = levelLemak(akhir, laki, usia);
        double massaLemak = berat * Math.max(akhir, 0) / 100.0;

        lblBodyFat.setText(String.format("%.1f %%", akhir));
        lblKategori.setText(namaLevel(level) + " (sehat: " + kisaranSehat(laki, usia) + ")");
        lblBmi.setText(String.format("%.1f (%s)", bmi, kategoriBmi(bmi)));
        lblMassa.setText(String.format("lemak %.1f kg, bebas lemak %.1f kg",
                massaLemak, berat - massaLemak));
    }

    @FXML
    private void reset() {
        tfUsia.clear();
        tfBerat.clear();
        tfTinggi.clear();
        tfPinggang.clear();
        tfLeher.clear();
        tfPinggul.clear();
        tgGender.selectToggle(null);

        lblError.setText("");
        lblCatatan.setText("");
        lblBodyFat.setText("-");
        lblKategori.setText("-");
        lblBmi.setText("-");
        lblMassa.setText("-");
    }

   

    // exception
    private Double baca(TextField tf) {
        String teks = tf.getText().trim().replace(',', '.');
        if (teks.isEmpty()) {
            return null;
        }
        double nilai = Double.parseDouble(teks);
        if (Double.isNaN(nilai) || Double.isInfinite(nilai)) {
            throw new NumberFormatException("bukan angka");
        }
        return nilai;
    }

    private String kategoriBmi(double bmi) {
        if (bmi < 18.5) return "kurang";
        if (bmi < 23.0) return "normal";
        if (bmi < 25.0) return "berlebih";
        if (bmi < 30.0) return "obesitas I";
        return "obesitas II";
    }

   
    private double[] batasLemak(boolean laki, double usia) {
        int baris = usia < 40 ? 0 : (usia < 60 ? 1 : 2);
        double[][] pria = { {8, 20, 25}, {11, 22, 28}, {13, 25, 30} };
        double[][] wanita = { {21, 33, 39}, {23, 35, 40}, {24, 36, 42} };
        return laki ? pria[baris] : wanita[baris];
    }

   
    private int levelLemak(double bf, boolean laki, double usia) {
        double[] b = batasLemak(laki, usia);
        if (bf < b[0]) return 0;
        if (bf <= b[1]) return 1;
        if (bf <= b[2]) return 2;
        return 3;
    }

    private String namaLevel(int level) {
        switch (level) {
            case 0: return "Rendah";
            case 1: return "Sehat";
            case 2: return "Berlebih";
            default: return "Obesitas";
        }
    }

    private String kisaranSehat(boolean laki, double usia) {
        double[] b = batasLemak(laki, usia);
        return String.format("%.0f-%.0f %%", b[0], b[1]);
    }
}
