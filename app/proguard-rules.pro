# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep data classes and models
-keep class com.cocido.nonna.domain.model.** { *; }
-keep class com.cocido.nonna.data.remote.dto.** { *; }
-keep class com.cocido.nonna.data.local.entity.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Retrofit interfaces
-keep interface com.cocido.nonna.data.remote.** { *; }

# Keep Dagger Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep security-related classes
-keep class com.cocido.nonna.core.security.** { *; }
-keep class com.cocido.nonna.core.logging.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove our custom logger in release builds
-assumenosideeffects class com.cocido.nonna.core.logging.Logger {
    public static void d(...);
    public static void i(...);
    public static void w(...);
    public static void v(...);
}

# Keep debug logging for security and critical events
-keep class com.cocido.nonna.core.logging.Logger {
    public static void e(...);
    public static void critical(...);
    public static void security(...);
}

# Obfuscate sensitive data
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove unused resources
-keep class **.R$* {
    public static <fields>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep reflection-accessed classes
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# No añadir bloques de optimización extra aquí: ya aplica proguard-android-optimize.txt;
# optimizaciones manuales extra suelen ser la causa de fallos solo en release.

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Retrofit / OkHttp (refuerzo; parte ya viene embebido en los AAR) ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# --- OkHttp (platform / registro de APIs internas) ---
-dontwarn okhttp3.internal.platform.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# --- Gson (factories por reflexión) ---
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# --- Moshi en classpath (Retrofit usa Gson; evita ruido R8) ---
-dontwarn com.squareup.moshi.**

# --- Kotlin ---
-dontwarn kotlin.reflect.jvm.internal.**

# --- CameraX ---
-keep androidx.camera.** { *; }

# --- Media3 / ExoPlayer ---
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- DataStore / protobuf interno ---
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# --- WorkManager ---
-dontwarn androidx.work.impl.**

# --- Glide (por si R8 elimina generados del compilador) ---
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# --- App (manifest / Hilt) ---
-keep class com.cocido.nonna.NonnaApplication { *; }
-keep class com.cocido.nonna.MainActivity { *; }