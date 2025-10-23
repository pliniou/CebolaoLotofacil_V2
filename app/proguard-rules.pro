# Regras para ofuscação e otimização do ProGuard/R8.

# --- Regras Padrão Android ---
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# Manter nomes de classes anotadas com @Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * { @androidx.annotation.Keep <methods>; }
-keepclasseswithmembers class * { @androidx.annotation.Keep <fields>; }
-keepclasseswithmembers class * { @androidx.annotation.Keep <init>(...); }

# --- Regras Kotlin & Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.flow.** { *; }
-keep class kotlinx.coroutines.flow.StateFlow { *; }
-keep class kotlinx.coroutines.flow.SharedFlow { *; }
-keepnames class kotlinx.coroutines.channels.** { *; }
-keepnames class kotlinx.coroutines.selects.** { *; }
-keepnames class kotlinx.coroutines.sync.** { *; }
-keepclassmembers class ** { volatile <fields>; }
-keepclassmembers class kotlinx.coroutines.internal.* { *; }
# Manter nomes relacionados à Reflection se realmente necessário (evitar se possível)
-keepnames class kotlin.reflect.KClass
-keepnames class kotlin.reflect.KFunction
-keepnames class kotlin.reflect.KProperty*

# --- Regras Hilt (Injeção de Dependência) ---
# Regras abrangentes para Hilt e suas extensões androidx
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class androidx.hilt.** { *; }
-keep class **_HiltComponents*_* { *; }
-keep class **_HiltModules*_* { *; }
# Manter construtores e campos injetados
-keepclassmembers class * { @javax.inject.Inject <init>(...); }
-keepclassmembers class * { @javax.inject.Inject <fields>; }
# Manter ViewModels injetados
-keep public class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- Regras Jetpack Compose ---
-keepclassmembers class * { @androidx.compose.runtime.Composable <methods>; }
# Regra específica para Snapshots (pode ajudar com avisos de lock verification)
-keep class androidx.compose.runtime.snapshots.** { *; }

# --- Regras Kotlinx Serialization ---
-keepclassmembers class kotlinx.serialization.internal.* { *; }
-keep class **$$serializer { *; }
-keepclassmembers class * { @kotlinx.serialization.Serializable <fields>; }
-keepclassmembers class * { @kotlinx.serialization.Serializable <init>(...); }
# Manter nomes de classes marcadas como Serializable
-keepnames class * implements kotlinx.serialization.KSerializer

# --- Regras Kotlinx Collections Immutable ---
-keep class kotlinx.collections.immutable.implementations.** { *; }

# --- Regras para Modelos de Dados (manter nomes para serialização/reflection) ---
-keep class com.cebolao.lotofacil.data.model.** { *; }
-keep class com.cebolao.lotofacil.data.network.** { *; }
-keep class com.cebolao.lotofacil.data.** { *; }
-keep class com.cebolao.lotofacil.domain.model.** { *; }

# --- Regras para Bibliotecas de Rede (Retrofit/OkHttp) ---
# Geralmente incluídas pelas bibliotecas, mas mantidas por segurança
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okio.** # Não avisar sobre problemas em Okio

# --- Regras Gerais de Otimização e Atributos ---
# Manter atributos essenciais
-keepattributes *Annotation*
-keepattributes Signature # Necessário para Generics
-keepattributes InnerClasses # Necessário para classes aninhadas/internas
-keepattributes Exceptions # Necessário para tratamento de exceções