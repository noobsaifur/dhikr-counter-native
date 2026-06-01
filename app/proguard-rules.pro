# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# ── Retrofit & OkHttp ────────────────────────────────────────────────────────
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers interface * {
    @retrofit2.http/* <methods>;
}

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# ── Gson & Response DTOs (Preserve reflect-mapped field names) ───────────────
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

-keep class com.countdhikr.app.data.remote.** { *; }

# ── Kotlinx Serialization ─────────────────────────────────────────────────────
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-dontwarn kotlinx.serialization.**

