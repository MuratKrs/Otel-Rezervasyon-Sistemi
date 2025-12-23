# Otel Rezervasyon ve Yönetim Sistemi 🏨

Gazi Üniversitesi Teknoloji Fakültesi BMT-311 Veri Tabanı Yönetim Sistemleri dersi için geliştirilmiş dönem projesidir.

## 🚀 Proje Hakkında
Bu proje, otel rezervasyon süreçlerini dijitalleştirmek amacıyla Java ve PostgreSQL kullanılarak geliştirilmiş bir masaüstü uygulamasıdır. Yönetici ve Müşteri olmak üzere iki farklı kullanıcı paneli bulunur.

## 🔑 Varsayılan Giriş Bilgileri (Admin)
Projeyi çalıştırdıktan sonra Yönetici Paneline erişmek için aşağıdaki bilgileri kullanabilirsiniz:
* **Kullanıcı Adı:** `admin`
* **Şifre:** `1234`

## 🛠 Kullanılan Teknolojiler
* **Dil:** Java (JDK 21)
* **Veritabanı:** PostgreSQL 16
* **Arayüz:** Java Swing (Nimbus L&F)
* **IDE:** IntelliJ IDEA

## ⚙️ Özellikler
### Yönetici (Admin) Paneli
* Oda Ekleme / Silme / Güncelleme
* Tüm rezervasyonları görüntüleme
* Rezervasyon iptal etme (Trigger ile otomatik oda boşa çıkarma)
* Toplam ciro raporlaması

### Müşteri Paneli
* Müsait odaları listeleme
* Tarih seçerek rezervasyon yapma
* Kendi rezervasyonlarını görüntüleme ve iptal etme

## 💾 Kurulum Adımları
1.  **Veritabanını Kurun:** Proje klasöründeki `database_backup.sql` dosyasını pgAdmin üzerinden import edin (veya Query Tool ile çalıştırın).
2.  **Bağlantı Ayarını Yapın:** `src/main/java/com/otel/util/DbHelper.java` dosyasını açın. `PASSWORD` değişkenine **kendi yerel PostgreSQL şifrenizi** yazın.
3.  **Çalıştırın:** IntelliJ IDEA ile projeyi açıp `Main.java` dosyasını çalıştırın.

## 👥 Grup Üyeleri
* Murat KARASU - 22181616414
* Arif ÜÇGÜL - 22181616052