-keep class com.olapp.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Strip debug/verbose logs from release builds
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}

# Nearby Connections
-keep class com.google.android.gms.nearby.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# Room — keep generated implementations
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
