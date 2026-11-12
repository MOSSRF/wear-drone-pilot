plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // 纯 JVM 模块：只依赖 coroutines-core，手机/手表两端共用
    // 必须用 api：WearProtocol.json(Json) 与 DroneDataSource.state(StateFlow) 属于本模块公开 API，
    // 用 implementation 会让调用方（app-phone / wear）编译期看不到这两个类型
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}
