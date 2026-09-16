# Shunya R8 rules. Keep this file short: libraries ship their own consumer rules
# (Compose, AndroidX, kotlinx.serialization, coroutines).

# Line numbers make crash reports from release builds readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization: our persisted models are only (de)serialized through the
# plugin-generated serializers, but keep their companions and serializers so a
# model can never be stripped out from under DataStore or backup/restore.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,InnerClasses
-if @kotlinx.serialization.Serializable class dev.apn7.shunya.**
-keepclassmembers class <1> {
    static <1>$Companion Companion;
    static <1> INSTANCE;
}
-if @kotlinx.serialization.Serializable class dev.apn7.shunya.** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.apn7.shunya.**$$serializer { *; }
