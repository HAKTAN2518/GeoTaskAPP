# 🗺️ GeoTask Mobil Uygulaması

GeoTask, kullanıcıların belirledikleri coğrafi konumlara görevler atamasını ve bu konumlara yaklaştıklarında otomatik olarak bildirim almalarını sağlayan modern bir lokasyon tabanlı hatırlatıcı uygulamasıdır.

Bu proje, staj sürecim boyunca geliştirdiğim ve Android'in en güncel teknolojilerini (Jetpack Compose, MVVM) kullanarak inşa ettiğim uçtan uca bir çözümdür.

## ✨ Öne Çıkan Özellikler

* **📍 Lokasyon Bazlı Tetikleyiciler:** Harita üzerinde seçilen bir noktaya ve yarıçapa girildiğinde arka planda çalışan servisler (Foreground Service) sayesinde anında anlık bildirim (Push Notification) gönderir.
* **🗺️ Ücretsiz Harita Entegrasyonu:** Google Maps API kısıtlamaları/ücretlendirmelerini aşmak adına OpenStreetMap (OSMDroid) ve OpenTopoMap kullanılarak tamamen bağımsız ve ücretsiz bir harita altyapısı kurulmuştur.
* **🎨 Modern Kullanıcı Arayüzü:** Jetpack Compose ile tamamen deklaratif bir yaklaşımla tasarlanmış; Material Design 3 bileşenleri, yumuşak geçiş animasyonları ve karanlık/aydınlık tema uyumu ile harika bir UX sunulmuştur.
* **🔐 Güvenli Oturum Yönetimi:** Kullanıcı girişleri Jetpack DataStore üzerinden güvenle (JWT Token) şifrelenip saklanır, uygulama her açılışta kullanıcıyı tanır.
* **📡 Gerçek Zamanlı API Entegrasyonu:** .NET Core ile yazılmış Backend API'si ile Retrofit kullanılarak asenkron iletişim kurulur.

## 🛠️ Kullanılan Teknolojiler ve Mimariler

* **Dil:** Kotlin
* **Kullanıcı Arayüzü:** Jetpack Compose, Material Design 3
* **Mimari:** MVVM (Model-View-ViewModel), Clean Architecture prensipleri
* **Asenkron İşlemler:** Kotlin Coroutines & Flow
* **Ağ & API:** Retrofit2, OkHttp3, Gson
* **Veri Saklama:** Jetpack DataStore (Preferences)
* **Harita & Lokasyon:** OSMDroid, Google Play Services Location API
* **Arka Plan Servisleri:** Foreground Service, NotificationManager

## 📱 Ekran Görüntüleri

*(Buraya projenin ekran görüntülerini koyabilirsiniz)*
<!-- 
<img src="screenshot_login.png" width="250">
<img src="screenshot_home.png" width="250">
<img src="screenshot_map.png" width="250">
-->

## ⚙️ Kurulum ve Çalıştırma

1. Projeyi bilgisayarınıza klonlayın:
   ```bash
   git clone https://github.com/KULLANICI_ADIN/GeoTask.git
   ```
2. Android Studio'da açın ve Gradle senkronizasyonunun bitmesini bekleyin.
3. `.NET Backend` projesini çalıştırın ve API'nin yayın yaptığı yerel IP adresini bulun (Örn: `10.189.253.170`).
4. Android Studio'da `app/src/main/java/.../api/RetrofitClient.kt` ve `network_security_config.xml` dosyalarındaki IP adreslerini kendi backend IP adresinizle değiştirin.
5. Uygulamayı bir emülatör veya gerçek Android (10+) cihaz üzerinde çalıştırın.

## 👨‍💻 Geliştirici

Bu proje **[Senin Adın/Soyadın]** tarafından geliştirilmiştir. 
Staj sürecim boyunca bana kattıkları değerler için şirket ekibine teşekkür ederim.
