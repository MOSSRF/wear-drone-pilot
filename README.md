# WearDrone Pilot

基于 **DJI Mobile SDK V5** 的 Wear OS 手表飞行助手。手机端连接飞机并通过 Wear Data Layer
把遥测同步到三星 Galaxy Watch6 等 Wear OS 手表，手表端可查看飞行状态并下发安全控制指令。

- DJI 开发者后台应用名：WearDrone Pilot
- 注册 Package Name：`io.github.MOSSRF.dronewatch`（不可修改，与两个模块的 applicationId 一致）
- 目标机型：DJI Mini 4 Pro（MSDK V5.20.1 官方支持）；Mini 5 Pro / Neo 2 待 MSDK 适配后经抽象层接入

## 模块结构

```
wear-drone-pilot/
├── core/          纯 Kotlin/JVM：DroneState、DroneCommand、Wear Data Layer 协议与 JSON 契约（两端共用，含单元测试）
├── app-phone/     手机端：MSDK 初始化、遥测采集、指令执行、DataClient 状态推送、MessageClient 指令接收
├── wear/          手表端：Compose for Wear OS 界面、DataItem 状态监听、MessageClient 指令发送
└── gradle/        Version Catalog（libs.versions.toml）+ Gradle 8.12 wrapper
```

通信约定（见 `core/.../WearProtocol.kt`）：

| 方向 | API | 路径 | 载荷 |
|---|---|---|---|
| 手机 → 手表 | DataClient | `/dronewatch/state` | DataMap，`state_json` 为 DroneState JSON |
| 手表 → 手机 | MessageClient | `/dronewatch/command` | CommandEnvelope JSON |

## 环境要求

- Android Studio（最新稳定版），JDK 17
- Gradle 8.12（wrapper 已内置）、AGP 8.7.0、Kotlin 2.1.0
- 手机：Android 7.0（API 24）以上，arm64-v8a（MSDK 仅提供 arm64 原生库）
- 手表：Wear OS 3.0（API 30）以上，Watch6 为 Wear OS 4
- 手机与手表需在 **Galaxy Wearable / Wear OS 伴侣中完成配对**，两端 APK 必须 **同 applicationId、同签名**

## 配置 App Key（不进仓库）

1. 复制 `local.properties.example` 为 `local.properties`
2. 填入 DJI 开发者后台的 App Key：
   ```
   DJI_APP_KEY=你的AppKey
   ```
3. `app-phone/build.gradle.kts` 通过 manifestPlaceholders 注入到
   `com.dji.sdk.API_KEY` meta-data；`local.properties` 已在 .gitignore 中

## 开发路线

- [x] 第一期骨架：双模块工程、协议契约、MSDK 初始化、Wear 双向通道、手表状态页
- [ ] MSDK KeyManager 接线：`MsdkCommandExecutor` 中的 TODO_MSDK 桩（返航/刹车/起降/拍照/录像）
- [ ] 遥测 KeyManager 监听：电池、卫星、高度、距离、速度、信号写入 MsdkDroneDataSource.patch
- [ ] 第二期：真机联调（Mini 4 Pro），一键返航 + 紧急悬停
- [ ] 第三期：拍照/录像/跟随，手机端相机图传
- [ ] Mini 5 Pro 进入 MSDK 支持列表后启用机型抽象层适配

## 参考

- MSDK 官方仓库：https://github.com/dji-sdk/Mobile-SDK-Android-V5 （V5.20.1，2026-11-05）
- MSDK 初始化流程：SDKManager.init → INITIALIZE_COMPLETE → registerApp
- Wear OS 开发：https://developer.android.google.cn/wear
