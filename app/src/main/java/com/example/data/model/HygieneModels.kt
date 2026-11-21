package com.example.data.model

// Uygulamadaki ortak alan seçenekleri
enum class CommonArea(val displayName: String, val shortLabel: String) {
    FLOOR_1_RESTROOM("1. Kat Lavabo", "1. Kat"),
    FLOOR_2_RESTROOM("2. Kat Lavabo", "2. Kat"),
    GROUND_COMMON("Zemin Kat Ortak Alan", "Zemin Kat"),
    CANTEEN("Kantin ve Yemekhane", "Kantin"),
    GYM_LOCKER("Spor Salonu", "Spor Salonu"),
    GARDEN_FOUNTAIN("Bahçe Çeşmesi", "Bahçe");

    companion object {
        fun fromName(name: String): CommonArea =
            entries.firstOrNull { it.name == name } ?: FLOOR_1_RESTROOM
    }
}

// Tek dokunuşla seçilebilen hijyen ve malzeme durumları
enum class ReportCondition(val label: String, val needsAction: Boolean) {
    CLEAN("Temiz / Sorun Yok", false),
    SUPPLY_MISSING("Sabun / Peçete Eksik", true),
    CLEANING_NEEDED("Temizlik Gerekli", true),
    WATER_ISSUE("Su / Musluk Sorunu", true);

    companion object {
        fun fromName(name: String): ReportCondition =
            entries.firstOrNull { it.name == name } ?: CLEAN
    }
}

// Bildirimlerin çözüm durumu
enum class ResolutionStatus(val label: String) {
    OPEN("Açık"),
    RESOLVED("Çözüldü")
}

// Genel alan sağlık özeti seviyesi
enum class OverallHealthLevel(val title: String, val subtitle: String) {
    CLEAN("Alanlar Temiz ve Hazır", "Ortak kullanım alanlarında bekleyen kritik bildirim yok."),
    SUPPLY_NEEDED("Malzeme Eksikliği Var", "Bazı alanlarda sabun veya peçete yenilenmesi bekleniyor."),
    ACTION_NEEDED("Müdahale Bekliyor", "Temizlik veya musluk kontrolü gereken ortak alanlar var.")
}

// Su tasarrufu ve sıfır atık aksiyon türleri
enum class EcoActionType(val label: String, val defaultLitersSaved: Double) {
    HANDWASH_20S("20 Sn El Yıkama Turu", 2.5),
    WATER_SAVING("Gereksiz Su Akışını Önleme", 3.0),
    ZERO_WASTE("Sıfır Atık / Geri Dönüşüm", 0.0);

    companion object {
        fun fromName(name: String): EcoActionType =
            entries.firstOrNull { it.name == name } ?: HANDWASH_20S
    }
}

data class DailyTip(
    val id: Int,
    val category: String,
    val title: String,
    val description: String
)

data class HandwashStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val rangeSeconds: IntRange
)

data class ChecklistItemDef(
    val id: String,
    val title: String,
    val category: String
)

data class HygieneRuleCard(
    val id: String,
    val title: String,
    val summary: String,
    val bullets: List<String>
)

data class TeamMember(
    val name: String,
    val role: String,
    val initials: String
)

object TertemizStaticData {

    val dailyTips = listOf(
        DailyTip(
            id = 0,
            category = "Su Tasarrufu",
            title = "Sabunlarken Musluğu Kapat",
            description = "20 saniyelik el yıkamada sabunlama aşamasında musluğu kapatmak her yıkamada ortalama 2,5 litre temiz su tasarrufu sağlar."
        ),
        DailyTip(
            id = 1,
            category = "Hijyen",
            title = "Parmak Aralarını Unutma",
            description = "Mikropların en çok tutunduğu başparmak çevresi, parmak araları ve tırnak uçlarını en az 10 saniye boyunca özenle ovala."
        ),
        DailyTip(
            id = 2,
            category = "Sıfır Atık",
            title = "Tek Yaprak Kağıt Havlu Yeterli",
            description = "Ellerini lavabo içinde hafifçe silkeledikten sonra tek yaprak peçete ile kurulamak kağıt israfını yarı yarıya azaltır."
        ),
        DailyTip(
            id = 3,
            category = "Ortak Alan",
            title = "Eksikleri Hemen Bildir",
            description = "Sabun veya peçete bittiğinde uygulama üzerinden tek dokunuşla bildirim bırakarak görevli ekibin hızlıca yenilemesine destek ol."
        )
    )

    // Kalan saniyeye göre (20 -> 0) adım yönlendirmesi
    val handwashSteps = listOf(
        HandwashStep(
            stepNumber = 1,
            title = "Islatma",
            instruction = "Ellerini akan suyla ıslat ve musluğu kapat.",
            rangeSeconds = 17..20
        ),
        HandwashStep(
            stepNumber = 2,
            title = "Sabunlama",
            instruction = "Yeterli miktarda sıvı sabun al ve ellerinde köpürt.",
            rangeSeconds = 13..16
        ),
        HandwashStep(
            stepNumber = 3,
            title = "Ovalama",
            instruction = "Avuç içi, el sırtı, parmak araları ve tırnakları ovala.",
            rangeSeconds = 5..12
        ),
        HandwashStep(
            stepNumber = 4,
            title = "Durulama",
            instruction = "Musluğu az açarak tüm sabunu temiz suyla durula.",
            rangeSeconds = 2..4
        ),
        HandwashStep(
            stepNumber = 5,
            title = "Kurulama",
            instruction = "Musluğu kapat, tek peçete ile kurula ve atık kutusuna at.",
            rangeSeconds = 0..1
        )
    )

    fun getStepForRemainingSeconds(remainingSeconds: Int): HandwashStep {
        return handwashSteps.firstOrNull { remainingSeconds in it.rangeSeconds }
            ?: handwashSteps.first()
    }

    val checklistItems = listOf(
        ChecklistItemDef(
            id = "chk_tap_off",
            title = "Ellerimi sabunlarken musluğu kapalı tuttum",
            category = "Su Tasarrufu"
        ),
        ChecklistItemDef(
            id = "chk_single_towel",
            title = "Kurulanmak için en fazla 1-2 yaprak peçete kullandım",
            category = "Sıfır Atık"
        ),
        ChecklistItemDef(
            id = "chk_reusable_bottle",
            title = "Tek kullanımlık şişe yerine kendi mataramı kullandım",
            category = "Sıfır Atık"
        ),
        ChecklistItemDef(
            id = "chk_recycle_sort",
            title = "Kağıt ve plastik atıkları geri dönüşüm kutusuna ayrıştırdım",
            category = "Sıfır Atık"
        ),
        ChecklistItemDef(
            id = "chk_report_check",
            title = "Lavaboda gördüğüm eksik veya arızayı bildirdim",
            category = "Ortak Alan"
        )
    )

    val hygieneRules = listOf(
        HygieneRuleCard(
            id = "rule_handwash",
            title = "Doğru El Yıkama Rehberi (20 Saniye)",
            summary = "Düzenli ve doğru el yıkama bulaşıcı hastalık riskini önemli ölçüde azaltır.",
            bullets = listOf(
                "Yemeklerden önce ve sonra, tuvalet kullanımından sonra ve teneffüs dönüşünde mutlaka yıka.",
                "Sabunlama ve ovalama sırasında musluğu kapalı tutarak temiz suyu koru.",
                "Parmak uçları, başparmak ve bilek çevresini atlamadan en az 20 saniye ayır."
            )
        ),
        HygieneRuleCard(
            id = "rule_restroom",
            title = "Ortak Lavabo ve Tuvalet Kullanımı",
            summary = "Temiz bir okul ortamı herkesin küçük özeniyle korunur.",
            bullets = listOf(
                "Kullandıktan sonra sifonu çek ve alanı bir sonraki arkadaşın için temiz bırak.",
                "Peçete ve ıslak mendilleri kesinlikle lavabo giderine değil çöp kutusuna at.",
                "Damlayan musluk veya biten sabun gördüğünde Bildirim sekmesinden hemen kayıt oluştur."
            )
        ),
        HygieneRuleCard(
            id = "rule_zerowaste",
            title = "Sıfır Atık ve Su Verimliliği",
            summary = "Kaynakları verimli kullanmak yarınlarımızı korumanın en pratik yoludur.",
            bullets = listOf(
                "Matara kullanarak okul içi plastik şişe tüketimini azalt.",
                "Kağıt, plastik ve cam atıkları okul koridorlarındaki renkli kutulara ayrıştır.",
                "Bahçe çeşmelerini ve lavabo musluklarını tam kapalı bırakmayı alışkanlık haline getir."
            )
        )
    )

    // İstenen 8 kişilik proje ekibi listesi (eksiksiz)
    val projectTeam = listOf(
        TeamMember("Merve Arslan", "Yardımcı Öğretmen / Müdür Yardımcısı", "MA"),
        TeamMember("Yakub Çadırcı", "App Geliştiricisi", "YÇ"),
        TeamMember("Mustafa Adıgüzel", "İçerik ve Hijyen Rehberi Sorumlusu", "MA"),
        TeamMember("Yusuf Bozlak", "Sosyal Medya Yöneticisi, Yapay Zekâ Uzmanı", "YB"),
        TeamMember("Ecrin Ekici", "Sıfır Atık Elçisi", "EE"),
        TeamMember("Kaan Talha Doğan", "Saha, QR ve Altyapı Koordinasyonu", "KD"),
        TeamMember("Ömer Kaan Köksal", "Test, Veri ve Geri Bildirim Sorumlusu", "ÖK"),
        TeamMember("Seyid Talha Elmalı", "Sıfır Atık Elçisi", "SE")
    )

    val digitalBadges = listOf(
        DigitalBadgeDef(
            id = "badge_first_report",
            title = "İlk Adım",
            subtitle = "İlk hijyen bildirimini oluştur",
            description = "Ortak alanlardaki hijyen veya malzeme durumunu ilk kez bildirerek farkındalık hareketine katıldın.",
            category = BadgeCategory.HYGIENE,
            targetValue = 1,
            unitLabel = "bildirim"
        ),
        DigitalBadgeDef(
            id = "badge_five_reports",
            title = "5 Hijyen Bildirimi",
            subtitle = "Toplam 5 alan bildirimi kaydet",
            description = "Ortak kullanım alanlarını düzenli takip ederek 5 farklı hijyen ve bakım bildirimi oluşturdun.",
            category = BadgeCategory.HYGIENE,
            targetValue = 5,
            unitLabel = "bildirim"
        ),
        DigitalBadgeDef(
            id = "badge_problem_solver",
            title = "Sorun Çözücü",
            subtitle = "3 bildirimi 'Çözüldü' yap",
            description = "Açık bekleyen alan bildirimlerinin çözüme kavuşmasını takip edip 3 kaydı tamamladın.",
            category = BadgeCategory.HYGIENE,
            targetValue = 3,
            unitLabel = "çözüm"
        ),
        DigitalBadgeDef(
            id = "badge_handwash_master",
            title = "20 Sn Ustası",
            subtitle = "5 kez 20 sn el yıkama turu tamamla",
            description = "Etkileşimli 20 saniyelik el yıkama rehberini 5 kez eksiksiz tamamlayarak doğru hijyeni alışkanlığa dönüştürdün.",
            category = BadgeCategory.WATER,
            targetValue = 5,
            unitLabel = "tur"
        ),
        DigitalBadgeDef(
            id = "badge_week_water_saver",
            title = "1 Hafta Su Tasarrufu",
            subtitle = "Son 7 günde 7 su tasarrufu eylemi",
            description = "Hafta boyunca musluk kapatma ve 20 saniyelik bilinçli el yıkamayla düzenli su tasarrufu sağladın.",
            category = BadgeCategory.WATER,
            targetValue = 7,
            unitLabel = "haftalık eylem"
        ),
        DigitalBadgeDef(
            id = "badge_water_guardian",
            title = "Su Koruyucusu",
            subtitle = "Toplam 25 L su tasarrufu yap",
            description = "Bilinçli su kullanımıyla en az 25 litre temiz suyun boşa akmasını önledin.",
            category = BadgeCategory.WATER,
            targetValue = 25,
            unitLabel = "litre"
        ),
        DigitalBadgeDef(
            id = "badge_zero_waste_ambassador",
            title = "Sıfır Atık Elçisi",
            subtitle = "5 sıfır atık aksiyonu kaydet",
            description = "Matara kullanımı ve atık ayrıştırma gibi 5 sürdürülebilir sıfır atık adımını uyguladın.",
            category = BadgeCategory.ZERO_WASTE,
            targetValue = 5,
            unitLabel = "aksiyon"
        ),
        DigitalBadgeDef(
            id = "badge_full_checklist",
            title = "Tam Bilinç",
            subtitle = "Kontrol listesindeki 5 maddeyi tamamla",
            description = "Günlük hijyen ve sıfır atık kontrol listesindeki tüm sorumlulukları eksiksiz işaretledin.",
            category = BadgeCategory.ZERO_WASTE,
            targetValue = 5,
            unitLabel = "madde"
        )
    )

    fun getEcoRank(unlockedCount: Int): EcoRank {
        return when {
            unlockedCount >= 7 -> EcoRank(
                title = "Tertemiz Yarınlar Şampiyonu",
                nextHint = "Tüm hedeflerde zirveye ulaştın!"
            )
            unlockedCount >= 5 -> EcoRank(
                title = "Sıfır Atık Elçisi",
                nextHint = "Şampiyon unvanı için ${7 - unlockedCount} rozet daha kazan."
            )
            unlockedCount >= 3 -> EcoRank(
                title = "Su ve Çevre Dostu",
                nextHint = "Elçi unvanı için ${5 - unlockedCount} rozet daha kazan."
            )
            unlockedCount >= 1 -> EcoRank(
                title = "Hijyen Gönüllüsü",
                nextHint = "Çevre Dostu unvanı için ${3 - unlockedCount} rozet daha kazan."
            )
            else -> EcoRank(
                title = "Yeni Başlayan",
                nextHint = "İlk rozetini kazanmak için bir bildirim veya tasarruf ekle."
            )
        }
    }
}

enum class BadgeCategory(val label: String) {
    HYGIENE("Hijyen"),
    WATER("Su Tasarrufu"),
    ZERO_WASTE("Sıfır Atık")
}

data class DigitalBadgeDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: BadgeCategory,
    val targetValue: Int,
    val unitLabel: String
)

data class EcoRank(
    val title: String,
    val nextHint: String
)
