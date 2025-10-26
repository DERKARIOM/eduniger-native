# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# ==========================================
# Règles ProGuard pour PDFBox Android
# ==========================================

# Conserver les classes PDFBox
-keep class com.tom_roush.pdfbox.** { *; }
-keep class com.tom_roush.harmony.** { *; }
-keep class org.apache.fontbox.** { *; }

# Conserver les ressources
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Éviter l'optimisation excessive
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Conserver les annotations
-keepattributes *Annotation*,Signature,Exception,InnerClasses

# Désactiver les avertissements pour les bibliothèques tierces
-dontwarn com.tom_roush.pdfbox.**
-dontwarn com.tom_roush.harmony.**
-dontwarn org.apache.fontbox.**
-dontwarn javax.xml.crypto.**
-dontwarn org.apache.commons.logging.**

# ==========================================
# Règles générales Android
# ==========================================

# Conserver les classes natives
-keepclasseswithmembernames class * {
    native <methods>;
}

# Conserver les vues personnalisées
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Conserver les Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Conserver les Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==========================================
# Règles pour vos classes (optionnel)
# ==========================================

# Si vous voulez conserver vos classes de modèle
-keep class com.ninotech.fabi.model.** { *; }

# Si vous utilisez Picasso (vous l'utilisez déjà)
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
