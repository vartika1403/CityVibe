# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }

# Retrofit / Gson models
-keep class com.cityvibe.app.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
