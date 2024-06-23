# General Project Rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Suppress warnings for specific classes
-dontwarn io.grpc.internal.DnsNameResolverProvider
-dontwarn io.grpc.internal.PickFirstLoadBalancerProvider

# Keep - Applications and Activities
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.core.content.FileProvider

# Firebase Rules
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Picasso Image Library Rules
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**

# AndroidX and Material Design
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }
-dontwarn androidx.**
-dontwarn com.google.android.material.**

# RecyclerView
-keep public class * extends androidx.recyclerview.widget.RecyclerView

# ConstraintLayout
-keep class androidx.constraintlayout.widget.ConstraintLayout { *; }

# Room Persistence Library
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.* class * extends java.lang.annotation.Annotation { *; }
-keepclasseswithmembers class * {
    @androidx.room.* <fields>;
}
-keepclasseswithmembers class * {
    @androidx.room.* <methods>;
}

# UCrop Library
-keep class com.yalantis.ucrop.** { *; }
-dontwarn com.yalantis.ucrop.**

# Shimmer for Facebook
-keep class com.facebook.shimmer.** { *; }
-dontwarn com.facebook.shimmer.**

# Dexter Runtime Permissions
-keep class com.karumi.dexter.** { *; }
-dontwarn com.karumi.dexter.**

# Places Library
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.libraries.places.**

# Volley Networking Library
-keep class com.android.volley.** { *; }
-dontwarn com.android.volley.**

# Preserve all native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

# Recapcha Library
-keep class com.google.android.recaptcha.** { *; }
-dontwarn com.google.android.recaptcha.**

# Glide Library
-keep public class * implements com.bumptech.glide.module.GlideModule
-keepnames class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keepclassmembers class * {
    @com.bumptech.glide.annotation.GlideExtension *;
}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule


# TargetView Library
-keep class com.getkeepsafe.taptargetview.** { *; }
-dontwarn com.getkeepsafe.taptargetview.**


# Keep names of classes and methods your classes use in XML layouts
-keepclassmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Avoid obfuscation of classes that are referenced in the manifest
-keep public class com.ryca.** { *; }

# If using custom views or methods accessed via reflection, keep them as well
#-keep class com.ryca.views.** { *; }
#-keepclassmembers class com.ryca.views.** { *; }

# Uncomment and modify if using WebView with JavaScript
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
