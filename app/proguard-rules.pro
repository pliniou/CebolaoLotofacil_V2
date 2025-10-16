# Regras para ofuscação e otimização do ProGuard/R8.
# Essencial para builds de produção para reduzir o tamanho do APK e proteger o código.

# Manter nomes de Activity, Service, etc., que são referenciados pelo AndroidManifest.xml
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# Manter os nomes de classes anotadas com @Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# Regras para Coroutines do Kotlin
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.flow.** { *; }
-keepnames class kotlinx.coroutines.sync.** { *; }
-keepnames class kotlinx.coroutines.** { *; }
-keepclassmembers class ** {
    volatile <fields>;
}

# Regras para Metadados do Kotlin
-keep class kotlin.Metadata { *; }
-keepnames class kotlin.Function
-keepnames class kotlin.reflect.KClass
-keepnames class kotlin.reflect.KFunction
-keepnames class kotlin.reflect.KProperty*
-keepnames class kotlin.Result
-keepnames interface kotlin.coroutines.Continuation

# Regras para o Hilt (Injeção de Dependência)
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}
-keep class **_HiltComponents*_* { *; }
-keep class **_HiltModules*_* { *; }
-keep class dagger.hilt.internal.aggregatedroot.codegen.*
-keep class dagger.hilt.android.internal.managers.*
-keep class dagger.hilt.android.internal.modules.*
-keep class dagger.hilt.android.internal.testing.*
-keep class dagger.hilt.android.testing.*

# Regras para Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Regras para Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.internal.* { *; }
-keep class **$$serializer { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Manter nomes de classes de dados (data classes) para evitar problemas com serialização ou reflection
-keep class com.cebolao.lotofacil.data.** { *; }

# OTIMIZAÇÃO: Novas regras para evitar problemas com SnapshotStateList e kotlinx.collections.immutable
# Estas regras são CRUCIAIS para resolver o aviso "failed lock verification" que pode
# degradar a performance do Compose em tempo de execução.
-keep class androidx.compose.runtime.snapshots.** { *; }
-keepclassmembers class androidx.compose.runtime.snapshots.SnapshotStateList { *; }