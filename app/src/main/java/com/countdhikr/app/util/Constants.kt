package com.countdhikr.app.util

import com.countdhikr.app.data.model.PrayerMethod
import com.countdhikr.app.data.model.AzanSound

object Constants {

    const val STORAGE_KEY = "dhikr-counter-state"

    val PRAYER_METHODS: List<PrayerMethod> = listOf(
        PrayerMethod(0, "Shia Ithna-Ansari"),
        PrayerMethod(1, "University of Islamic Sciences, Karachi"),
        PrayerMethod(2, "Islamic Society of North America"),
        PrayerMethod(3, "Muslim World League"),
        PrayerMethod(4, "Umm Al-Qura University, Makkah"),
        PrayerMethod(5, "Egyptian General Authority of Survey"),
        PrayerMethod(7, "Institute of Geophysics, University of Tehran"),
        PrayerMethod(8, "Gulf Region"),
        PrayerMethod(9, "Kuwait"),
        PrayerMethod(10, "Qatar"),
        PrayerMethod(11, "Majlis Ugama Islam Singapura, Singapore"),
        PrayerMethod(12, "Union Organization Islamic de France"),
        PrayerMethod(13, "Diyanet İşleri Başkanlığı, Turkey"),
        PrayerMethod(14, "Spiritual Administration of Muslims of Russia"),
        PrayerMethod(15, "Moonsighting Committee Worldwide"),
    )

    val AZAN_SOUNDS: List<AzanSound> = listOf(
        AzanSound(
            id = "makkah",
            name = "Makkah",
            url = "https://www.islamcan.com/audio/adhan/azan1.mp3",
        ),
        AzanSound(
            id = "madinah",
            name = "Madinah",
            url = "https://www.islamcan.com/audio/adhan/azan2.mp3",
        ),
        AzanSound(
            id = "alaqsa",
            name = "Al-Aqsa",
            url = "https://www.islamcan.com/audio/adhan/azan3.mp3",
        ),
        AzanSound(
            id = "mishari",
            name = "Mishari Rashid",
            url = "https://www.islamcan.com/audio/adhan/azan4.mp3",
        ),
    )
}
