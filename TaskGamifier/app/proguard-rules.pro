# Proguard rules for Task Gamifier

# Keep all public classes and methods
-keep public class * {
    public *;
}

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class androidx.activity.compose.** { *; }

# Keep ViewModel
-keep class androidx.lifecycle.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep your app classes
-keep class com.example.taskgamifier.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep serialization
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
