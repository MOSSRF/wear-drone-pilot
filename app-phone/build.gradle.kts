import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// 从 local.properties 读取 App Key，密钥不进仓库
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val djiAppKey: String = localProps.getProperty("DJI_APP_KEY") ?: ""

android {
    namespace = "io.github.mossrf.dronewatch.phone"
    // 必须与 DJI 开发者后台注册的 Package Name 完全一致（含大小写），后台不可修改
    applicationId = "io.github.MOSSRF.dronewatch"
    compileSdk = 35

    defaultConfig {
        minSdk = 24 // MSDK V5 最低要求
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        // MSDK 仅提供 arm64 原生库
        ndk { abiFilters += "arm64-v8a" }
        manifestPlaceholders["API_KEY"] = djiAppKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // TODO: 真机发布前配置专属签名（不要提交 jks）
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { jvmToolchain(17) }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core"))

    // DJI MSDK V5 5.20.1（Maven Central）
    implementation(libs.msdk.aircraft)
    compileOnly(libs.msdk.aircraft.provided)
    runtimeOnly(libs.msdk.network.imp)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    // Wear Data Layer：与手表通信
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.android)
}
