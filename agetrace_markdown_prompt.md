# Proje Geliştirme Promptu: "AgeTrace"

Aşağıdaki detaylara ve gereksinimlere sahip, **"AgeTrace"** isimli çapraz platform (Kotlin Multiplatform / Web / Mobil) uygulaması için gerekli kod ve mimari yapıyı oluşturmanı istiyorum.

## 🎯 Projenin Temel Amacı ve Özellikleri
1. **Uygulama Adı:** **AgeTrace**
2. **Fonksiyonalite:** 
   - Kullanıcı ister canlı kameradan ister dosya yükleme (upload) yoluyla bir görsel sağlar.
   - Uygulama, görseldeki insan yüzünü tespit eder ve **kırmızı bir çerçeve** (`red bounding box`) içine alır.
   - Çerçevenin hemen üst kısmında (yüzün hizasında) modelin güven skorunu ve tahmin edilen yaşı belirten bir etiket gösterilir. 
   - **Örnek Etiket Formatı:** `%87 - 32 Age` (Yani yüzde güven oranı, tire işareti ve yaş değeri).
3. **Teknolojik Altyapı:**
   - **UI:** Modern, minimalist, karanlık tema (Dark mode) ve glassmorphic tasarım.
   - **Yapay Zeka / Analiz:** Tamamen yerel (offline) Edge AI prensibiyle çalışan ONNX Runtime veya hafif ağırlıklı bilgisayarlı görü motoru.
   - **Platform Desteği:** Android, iOS ve Desktop (Kotlin Multiplatform / Compose Multiplatform veya web tabanlı prototip).

## 🛠️ Teknik Beklentiler ve Bileşenler
- **Kırmızı Çerçeve ve Overlay Katmanı:** Yüz koordinatları (`x, y, w, h`) dinamik olarak hesaplanıp Canvas veya Stack üzerinde çizilmeli, çerçevenin üstünde `%87 - 32 Age` yazan şık bir rozet (badge) yer almalıdır.
- **Canlı Metrikler:** Sağ veya üst panelde anlık doğruluk oranı, yaş sınır durumu (örneğin 18+ Adult veya 18- Minor) göstergeleri bulunmalıdır.
- **Modlar:** Hem canlı kamera akışı hem de fotoğraf yükleme (drag & drop / file picker) desteklenmelidir.

Lütfen bu gereksinimleri eksiksiz karşılayan, temiz, modüler ve modern standartlara uygun kodları/yapıyı üret.