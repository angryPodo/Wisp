# Keep Wisp-generated classes from being removed or obfuscated by ProGuard.

# Keep the WispModuleRegistry implementation for ServiceLoader.
# ServiceLoader requires a public class with a public no-argument constructor.
-keep public class com.angrypodo.wisp.generated.WispModuleRegistry_* {
    public <init>();
}

# Keep the generated RouteFactory implementations. These are instantiated by the registry.
-keep class com.angrypodo.wisp.generated.*RouteFactory {
    *;
}

# The following rule is generally not needed because kotlinx-serialization-json
# includes its own consumer proguard rules that keep @Serializable classes.
# However, it can be added for extra safety if you encounter issues.
# -keep @kotlinx.serialization.Serializable class * { *; }
