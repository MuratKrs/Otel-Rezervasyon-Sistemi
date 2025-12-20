# Otel Rezervasyon ve Yönetim Sistemi 🏨

Gazi Üniversitesi Teknoloji Fakültesi BMT-311 Veri Tabanı Yönetim Sistemleri dersi için geliştirilmiş dönem projesidir.

## 🚀 Proje Hakkında
Bu proje, otel rezervasyon süreçlerini dijitalleştirmek amacıyla Java ve PostgreSQL kullanılarak geliştirilmiş bir masaüstü uygulamasıdır. Yönetici ve Müşteri olmak üzere iki farklı kullanıcı paneli bulunur.

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

## 💾 Kurulum
1.  `database_backup.sql` dosyasını pgAdmin üzerinden import edin veya Query Tool ile çalıştırın.
2.  `src/main/java/com/otel/util/DbHelper.java` dosyasındaki veritabanı kullanıcı adı ve şifresini kendi yerel ayarlarınıza göre güncelleyin.
3.  Projeyi IntelliJ IDEA ile açıp `Main.java` dosyasını çalıştırın.

## 👥 Grup Üyeleri
* Murat KARASU - 22181616414
* Arif ÜÇGÜL - 22181616052