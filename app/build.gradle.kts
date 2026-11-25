plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP: genera el código de Room (DAO, base de datos) al compilar.
    alias(libs.plugins.ksp)
    id("jacoco")
}

val propiedadesDeFirma = java.util.Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.isFile) archivo.inputStream().use { load(it) }
}


fun datoDeFirma(nombre: String): String? =
    (project.findProperty(nombre) as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv(nombre)?.takeIf { it.isNotBlank() }
        ?: propiedadesDeFirma.getProperty(nombre)?.takeIf { it.isNotBlank() }

val firmaArchivo = datoDeFirma("MPAZPRO_STORE_FILE")
val firmaClaveAlmacen = datoDeFirma("MPAZPRO_STORE_PASSWORD")
val firmaAlias = datoDeFirma("MPAZPRO_KEY_ALIAS")
val firmaClaveLlave = datoDeFirma("MPAZPRO_KEY_PASSWORD")

val apiBaseUrl: String = ((project.findProperty("API_BASE_URL") as String?)?.trim())
    ?.takeIf { it.isNotEmpty() } ?: "http://10.0.2.2:3000/"


val usarApi: Boolean = (project.findProperty("USAR_API") as String?)?.trim()?.equals("true", ignoreCase = true) ?: false

val hayFirmaCompleta = listOf(firmaArchivo, firmaClaveAlmacen, firmaAlias, firmaClaveLlave).all { it != null }

android {
    namespace = "com.mpazpro3.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mpazpro3.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("boolean", "USAR_API", usarApi.toString())
    }

    signingConfigs {
        if (hayFirmaCompleta) {
            create("release") {
                storeFile = file(firmaArchivo!!)
                storePassword = firmaClaveAlmacen
                keyAlias = firmaAlias
                keyPassword = firmaClaveLlave
                
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hayFirmaCompleta) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true 
    }
}

dependencies {
    //Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    //Íconos
    implementation(libs.androidx.material.icons.extended)

    //Persistencia local
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //Capa remota (Retrofit + OkHttp + Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.okhttp.logging)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //Pruebas unitarias (JVM, sin emulador)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


// Cobertura JaCoCo de las pruebas unitarias JVM (sin emulador).
// Ejecutar desde la raíz: ./gradlew :app:jacocoTestReport
jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoTestReport/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
    }

    val clasesKotlin = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude("**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*")
    }
    val clasesJava = fileTree("$buildDir/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude("**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*")
    }

    classDirectories.setFrom(files(clasesKotlin, clasesJava))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/testDebugUnitTest.exec")
    })
}
