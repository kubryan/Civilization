# CivilizationMod — AI 專案指令

## 專案身份

本專案是 **Minecraft Java Edition 26.2 + Fabric** 的整合型文明模擬模組，Mod ID 為 `civilizationmod`，Java package 根為 `com.civilizationmod`，專案路徑為 `C:\Minecraft`。

文明聚落必須能整合原版村莊、其他模組村莊與日後資料驅動的聚落。玩家可以選擇觀察者／干預者、領袖、居民或獨立者，並透過遊戲內條件轉換身份。第一個核心需求是食物。

## 給所有 AI 的最高優先規則

1. **先讀檔再改檔。** 開始任何程式碼、Gradle、JSON、資產、資料生成、測試或文件修改前，先讀取本檔案與專案根目錄的 `Mods.md`。如果 `Mods.md` 引用的文件或技能需要重新確認，也要先讀取相應檔案。
2. **每次修改完成後必須更新 `Mods.md`。** 修改、測試與查證完成後，在 `Mods.md` 追加一筆歷史記錄，記錄變更、影響檔案、版本、命令、結果、風險與下一步。不得只更新 Manus 共享檔案，也不得建立另一個同用途的進度檔。
3. **不要猜 Fabric 26.2 API。** 版本敏感的類別、方法、事件、Mixin 注入點、registry、structure/worldgen、Saved Data 與 networking API，必須查 Fabric 官方文件、Minecraft 官方文件、Fabric Example Mod、26.2 mappings 或由實際編譯驗證。第三方模組與論壇只能作功能線索，不能當成 API 證明。
4. **保護 server/client 邊界。** 文明資料與規則由 logical server 保存、驗證與推進。`src/main/java` 不得依賴 client-only 類別；Screen、HUD、renderer、client command 與 client-only networking handler 放在 `src/client/java`。
5. **外部村莊不能是核心硬依賴。** 使用 settlement adapter、設定、資料包或 tags 整合外部村莊。缺少某個外部村莊模組時，`civilizationmod` 仍應能啟動。
6. **先做可驗證的垂直切片。** 不要同時實作完整 NPC AI、自然 worldgen、外交、戰爭、新維度與高成本 renderer。先完成 Saved Data、聚落掃描、食物需求、身份、命令與保存重開閉環。
7. **所有命令與參數必須支援 Tab completion。** Brigadier literal 與子命令要能以 Tab 補全；動態資料必須提供 suggestions；權限受限節點使用 `requires()`；新增命令後要實測根命令、子命令前綴、參數與權限差異。
8. **所有玩家可見文字必須本地化。** 使用 `Component.translatable` 與語言 JSON，不要在 Java、GUI、HUD、事件或命令中硬編碼玩家可見訊息；`en_us` 使用英文，`zh_tw` 使用台灣繁體中文，`zh_cn` 依使用者政策也使用相同繁體中文。
9. **所有傳給玩家的訊息必須有金色品牌前綴。** 一律使用共用的 `CivilizationMessages` 包裝器；中文格式為 `|創世紀元| : {模組訊息}`，英文格式為 `|Civitas| : {模組訊息}`。只有前綴使用金色，正文維持一般樣式，不再加入尾端品牌。不得直接呼叫 `sendSuccess`、`sendFailure` 或發送未包裝的玩家訊息；console logger 不屬於玩家訊息，可維持開發用途格式。
9. **不要因方便而刪除歷史或模板檔。** 若需要清理模板程式，先在 `Mods.md` 記錄原因與影響；除非必要，不要刪除使用者已有的進度或設計文件。

## 必須遵守的修改順序

```text
1. 讀取 AGENTS.md、Mods.md、Cursor skill 判斷捷徑與相關程式檔
2. 判斷任務類型與 common/client/server side
3. 對不確定的版本或 API：先讀 skill 已查證內容；缺口才查官方來源或 javap
4. 先寫出最小設計與失敗 fallback
5. 修改程式或資料
6. 執行最小驗證
7. 把本次新的已確認知識追加進 .cursor/skills/civitas-fabric-262/ 與 skills/minecraft-civilization-fabric-262/
8. 將完整結果追加到 Mods.md
9. 回報已完成、未完成與下一步
```

若任一步無法完成，必須在 `Mods.md` 記錄為未完成，不得把推測當成完成結果。

## 專案目前工具鏈

| 項目 | 目前設定 |
|---|---|
| Minecraft | `26.2` |
| Fabric Loader | `0.19.3` |
| Fabric Loom | `1.17-SNAPSHOT`；實際執行曾顯示 `1.17.19` |
| Fabric API | `0.158.0+26.2` |
| Gradle | `9.5.1` wrapper |
| Java 編譯目標 | `25` |
| 專案 Java toolchain | `25`；由 Gradle 自動偵測本機 `C:\Program Files\Java\jdk-25.0.2` |
| 全域 Windows Java | 保持原本 Java 21，不要求全域切換 |
| source set | 已啟用 `splitEnvironmentSourceSets()` |
| datagen | 已啟用，`fabricApi.configureDataGeneration { client = true }` |

**重要：** 專案 `build.gradle` 已設定 Java toolchain 25，並保留 `release/source/target = 25`；全域 Java 21 不需要改變。Gradle 本身可以繼續由 Java 21 daemon 執行，但 Java 編譯任務必須使用已偵測到的 JDK 25。執行 `gradlew.bat javaToolchains` 應能看到 `C:\Program Files\Java\jdk-25.0.2`，不要把專案降回 Java 21。

## 常用 Windows 命令

所有命令從 `C:\Minecraft` 執行：

```bat
java -version

gradlew.bat --version
gradlew.bat tasks
gradlew.bat build
gradlew.bat compileJava --stacktrace --console=plain
gradlew.bat runDatagen
gradlew.bat runClient
gradlew.bat runServer
```

`runClient`、`runServer` 是持續執行的任務，不要在沒有需要時啟動它們。若只是確認專案可建置，優先使用 `gradlew.bat build`；若是只檢查 common Java，使用 `gradlew.bat compileJava --stacktrace --console=plain`。

## 實作邊界與優先順序

第一個垂直切片是「一座會運作的外部村莊聚落」。第一版的合理順序如下：

| 順序 | 內容 | 完成標準 |
|---|---|---|
| 1 | Gradle/JDK 環境 | build、client、dedicated server 的阻塞原因已確認 |
| 2 | 世界級 Saved Data | 重新開世界後資料仍存在，並有資料版本 |
| 3 | `/civilization status` | 能從 server 查詢文明狀態 |
| 4 | `/civilization scan` | 能依設定辨識既有村莊並保存聚落來源與中心 |
| 5 | 食物需求 | 模擬步進會消耗食物並產生短缺／穩定度結果 |
| 6 | 玩家身份 | 四種身份由 server 保存並限制可用操作 |
| 7 | `/civilization simulate`、`reset` | 模擬可重現、可清除並可除錯 |
| 8 | networking/UI | client 只顯示摘要，server 驗證所有請求 |
| 9 | 起始村莊與 worldgen | 先做搜尋既有村莊＋安全 fallback；保證起點生成必須另行查證 26.2 API |

## API 與資料設計原則

Saved Data 優先保存世界級文明狀態，資料變更要正確標記 dirty，序列化使用已查證的 26.2 Codec／Saved Data 方式。事件優先使用 Fabric API callbacks；只有沒有適合 hook 才建立自訂 event 或 Mixin。命令使用已查證的 Fabric command registration 方式。主要命令根為 `/civitas`；`/civilization` 暫時保留為相容別名。兩個根命令都必須有相同的子命令、Tab completion、server 驗證、權限與本地化訊息。命令縮寫必須註冊為額外 literal alias，與完整命令共用同一套執行邏輯、參數、suggestions、權限與本地化訊息，不得複製出兩套容易分歧的實作。不要加入沒有補全與 server 驗證的任意字串參數。

Networking payload 必須由 server 驗證玩家、位置、身份、權限、數值與頻率；client 不能直接傳入文明真實狀態。聚落適配至少要保存來源 namespace、structure ID 或 tag、維度、中心位置、人口來源、可用資源與適配狀態。未知結構不可自動視為文明聚落。

資料內容應逐步資料驅動化。datagen 適合 tags、翻譯、配方、loot、advancement 與 JSON 資源；不要把所有內容硬編碼在大型 Java 方法中。玩家可見文字使用 `Component.translatable` 與 `assets/civilizationmod/lang/` 下的 locale JSON；`zh_cn` 必須沿用繁體中文內容。所有玩家訊息再經由 `CivilizationMessages` 套用 locale 金色品牌前綴；正文維持一般樣式，不加入尾端品牌，不得繞過包裝器。

## 交付檢查

交付任何修改前，確認 `Mods.md` 已追加本次記錄；確認本次新的已查證知識已寫入 `.cursor/skills/civitas-fabric-262/`（並同步 `skills/minecraft-civilization-fabric-262/`）；確認 common/client/server side；確認沒有把未查證的 API 當作事實；確認至少有一項實際驗證命令或明確標記未執行；確認失敗原因、風險與下一步已寫清楚。

## 專案內技能

可重複使用的文明模組技能位於：

```text
.cursor/skills/civitas-fabric-262/SKILL.md
skills/minecraft-civilization-fabric-262/SKILL.md
```

Cursor Agent 以 `.cursor/skills/civitas-fabric-262/` 為準。每次 javap 或官方文件查證到新的 API／規則後，必須把可重用結論追加進 skill（判斷捷徑或對應 reference），讓後續任務先讀 skill 再決定要不要重查。Claude／Codex 副本仍在 `skills/minecraft-civilization-fabric-262/`，兩邊要同步。此專案內的 `AGENTS.md` 與 `Mods.md` 是專案現況與強制規則的第一優先來源。

## 官方參考來源

- [Fabric Creating a Project 26.2](https://docs.fabricmc.net/develop/getting-started/creating-a-project)
- [Fabric Project Structure 26.2](https://docs.fabricmc.net/develop/getting-started/project-structure)
- [Fabric Data Generation Setup 26.2](https://docs.fabricmc.net/develop/data-generation/setup)
- [Fabric Saved Data](https://docs.fabricmc.net/develop/saved-data)
- [Fabric Events 26.2](https://docs.fabricmc.net/develop/events)
- [Fabric Networking 26.2](https://docs.fabricmc.net/develop/networking)
- [Fabric Creating Commands 26.2](https://docs.fabricmc.net/develop/commands/basics)
- [Fabric Command Arguments 26.2](https://docs.fabricmc.net/develop/commands/arguments)
- [Fabric Text and Translations 26.2](https://docs.fabricmc.net/develop/text-and-translations)
- [Fabric Translation Generation 26.2](https://docs.fabricmc.net/develop/data-generation/translations)
- [Fabric Porting to 26.2](https://docs.fabricmc.net/develop/porting/)
- [Fabric for Minecraft 26.2](https://fabricmc.net/2026/06/15/262.html)
