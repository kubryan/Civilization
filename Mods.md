# 文明模擬模組開發進度

## 專案基線

| 欄位 | 狀態 |
|---|---|
| 專案路徑 | `C:\Minecraft` |
| Minecraft | `26.2` |
| Fabric Loader | `0.19.3` |
| Fabric Loom | `1.17-SNAPSHOT` |
| Fabric API | `0.158.0+26.2` |
| Java | `25` |
| Mod ID | `civilizationmod` |
| Maven group | `com.civilizationmod` |
| 專案版本 | `1.0.0` |
| 進度檔 | `Mods.md`；本檔案為專案根目錄的 canonical 進度檔 |

## 開發目標

本專案是 Minecraft Java Edition 26.2 的 Fabric 整合型文明模擬模組。文明聚落可以來自原版村莊、其他模組村莊，以及日後由資料驅動建立的聚落。玩家可以選擇觀察者／干預者、領袖、居民或獨立者，並透過遊戲內的聲望、資源、事件與政治條件轉換身份。第一個文明需求採用食物。

## 2026-08-19 — 確認現有專案與進度檔位置

### 變更

確認使用者的 Minecraft 專案位於 `C:\Minecraft`。專案根目錄已存在 Fabric 26.2 Gradle 專案與空白 `Mods.md`；原有的 `文明模擬模組開發進度.md` 保留作為舊進度資料，但從本次開始以 `Mods.md` 作為唯一正式進度檔。

### 專案現況

Fabric 專案骨架已建立完成，`build.gradle` 已啟用 `splitEnvironmentSourceSets()`、client/common source set 分離與 Fabric datagen；`gradle.properties` 已設定 Minecraft 26.2、Loader 0.19.3、Loom 1.17-SNAPSHOT、Fabric API 0.158.0+26.2 與 Java 25。現有 Java 程式仍是模板初始化器，文明模擬功能尚未開始實作，因此目前是「專案骨架完成」，不是「文明模組功能完成」。

### 模板選項決策

| 選項 | 決定 | 理由 |
|---|---|---|
| Kotlin 程式語言 | 不勾 | 目前以 Java 與 Fabric 26.2 官方範例為主，減少 runtime 與學習成本。 |
| 數據生成 | 勾選 | 文明模組需要 tags、翻譯、配方、loot、advancement 與日後資料驅動內容。 |
| 拆分客戶端和公共資源 | 勾選 | server/common 保存文明狀態；client 放 Screen、HUD 與 renderer，避免 dedicated server 載入 client 類別。 |
| Kotlin 建置腳本 | 不勾 | 使用 Groovy Gradle script 即可，不增加 Kotlin DSL 的額外複雜度。 |

### 影響檔案

- `build.gradle`：確認 source set 分離與 datagen 已啟用。
- `gradle.properties`：確認 26.2 工具鏈與 Java 25 設定。
- `Mods.md`：建立並移入專案根目錄正式進度記錄。
- `文明模擬模組開發進度.md`：保留為既有舊進度檔，不再作為主要更新目標。

### 查證與驗證

- Fabric 官方建立專案文件：https://docs.fabricmc.net/develop/getting-started/creating-a-project
- Fabric 官方 Data Generation Setup 26.2：https://docs.fabricmc.net/develop/data-generation/setup
- Fabric 官方 Project Structure 26.2：https://docs.fabricmc.net/develop/getting-started/project-structure
- 已讀取並確認 `build.gradle` 與 `gradle.properties` 的專案設定。
- 本次尚未執行 `gradlew build`；實際編譯狀態仍待下一步驗證。

### 未完成與風險

- 尚未實作 Saved Data、文明資料模型、聚落適配器、玩家身份、食物需求、事件引擎或 `/civilization` 命令。
- 尚未在 26.2 mappings 下驗證 structure tag、村莊搜尋與起始 spawn 的具體 Java API。
- 外部村莊模組仍應是可選相容性測試目標，不列為核心硬依賴。

### 下一步

先執行 `gradlew.bat build` 確認現有骨架可編譯；通過後建立最小的世界級 Saved Data 與 `/civilization status`，每次修改後繼續追加本檔案。

## 進度檔規則

今後每次修改 Java、Gradle、JSON、資產、資料生成、測試或架構，都必須先讀取本檔案，完成修改與最小驗證後，在本檔案追加日期、變更內容、影響檔案、驗證結果、未完成風險與下一步；不得覆寫既有歷史，也不得另建第二個同用途進度檔。

## 2026-08-19 — 現有專案建置驗證

### 變更

檢查並嘗試編譯目前已建立的 Fabric 26.2 專案骨架；本次沒有修改 Java 功能程式碼。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| Minecraft | `26.2` |
| Fabric Loader | `0.19.3` |
| Fabric Loom | 設定為 `1.17-SNAPSHOT`；實際執行顯示 Loom `1.17.19` |
| Fabric API | `0.158.0+26.2` |
| Gradle | `9.5.1` |
| 專案 Java 設定 | `release/source/target = 25` |
| 實際 Windows Java | Temurin OpenJDK `21.0.12`；Gradle Daemon 使用 `jdk-21.0.6` |
| 驗證命令 | `gradlew.bat build`、`gradlew.bat compileJava --stacktrace --console=plain` |
| 結果 | 失敗：`error: release version 25 not supported` |

### 判定

專案的 Fabric 26.2 骨架與模板設定已建立，但目前不能稱為「可成功建置完成」；失敗原因不是文明模組 Java 程式碼，而是 `build.gradle` 要求 Java 25，使用者 Windows 環境目前只有 Java 21。這表示下一步應先安裝或指定可用的 JDK 25，再重新執行 build；在未查證 Fabric 26.2 的 Java 要求與使用者環境前，不直接把 release 降回 21。

### 影響檔案

- `Mods.md`：追加本次建置驗證結果。
- `civ_build.log`：暫存的 Gradle 錯誤日誌，內容顯示 release 25 不受目前 Java 21 編譯器支援。

### 未完成與風險

- 尚未確認使用者電腦是否已安裝其他 JDK 25，只確認目前 `java` 與 Gradle Daemon 使用 Java 21。
- 尚未修改 `build.gradle` 或 `gradle.properties`。
- 尚未執行文明模擬功能，因為目前仍停留在模板骨架與建置環境驗證階段。

### 下一步

先確認或安裝 JDK 25，設定 Gradle 使用正確 JDK 後重新執行 `gradlew.bat build`；build 通過後再開始 Saved Data 與 `/civilization status` 的第一個文明模擬切片。

## 2026-08-19 — 專用技能規則修正與驗證

### 變更

將文明模組技能的進度規則修正為：直接使用實際 mod 專案根目錄既有的 `Mods.md`／`mods.md`，若 Windows 只存在 `Mods.md` 就沿用該檔名，不建立第二個重複進度檔；Manus 共享檔案不再作為實際專案的唯一進度來源。

### 查證與驗證

- 執行：`python /home/ubuntu/skills/skill-creator/scripts/quick_validate.py minecraft-civilization-fabric-262`
- 結果：`Skill is valid!`
- 已確認本專案根目錄正式進度檔為 `Mods.md`。

### 下一步

下一次修改直接在 `C:\Minecraft` 專案內進行，流程固定為讀取 `Mods.md`、修改、驗證，再追加本檔案。

## 2026-08-19 — 建立專案內 AI 指令與技能副本

### 變更

在專案根目錄新增 `AGENTS.md`，提供其他 AI 使用的專案級指令、注意事項、常用 Windows Gradle 命令、common/client/server 邊界、外部村莊整合規則、API 查證要求與文明模擬垂直切片順序。

在專案根目錄新增 `skills/minecraft-civilization-fabric-262/SKILL.md`，放置可重複使用的文明模組技能副本，內容涵蓋 Fabric 26.2 版本查證、Saved Data、events、commands、networking、datagen、玩家身份、食物需求、外部模組村莊、起始村莊與每次修改後更新 `Mods.md` 的流程。

### 影響檔案

- `AGENTS.md`：其他 AI 進入專案時必須先讀取的專案指令。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：專案內可重複使用的技能副本。
- `Mods.md`：追加本次文件建立與驗證紀錄。

### 查證與驗證

- 已確認專案根目錄原本沒有 `AGENTS.md` 或同用途 AI 指令檔，因此本次沒有覆寫既有規則。
- 已確認 `AGENTS.md` 明確要求所有 AI 先讀取 `AGENTS.md` 與 `Mods.md`，完成修改與驗證後再更新 `Mods.md`。
- 使用 `cmp` 比對全域技能與專案內技能副本，結果：`SKILL_COPY_MATCH`。
- 全域技能先前已通過 `quick_validate.py minecraft-civilization-fabric-262`；專案副本與全域版本一致。
- 官方參考已寫入 `AGENTS.md` 與技能副本：Fabric 26.2 建立專案、Project Structure、Data Generation、Saved Data、Events、Networking、Commands 與 Porting 文件。

### 未完成與風險

- `gradlew.bat build` 仍受使用者 Windows Java 21 與專案 Java 25 target 不相容阻擋；此問題已記錄在前一筆進度。
- 尚未開始實作文明模擬 Java 功能。

### 下一步

其他 AI 進入 `C:\Minecraft` 後，先讀取 `AGENTS.md`、`Mods.md` 與 `skills/minecraft-civilization-fabric-262/SKILL.md`；下一個程式工作先處理 JDK 25／Gradle build，再建立 Saved Data 與 `/civilization status`。

## 2026-08-19 — 專案級 Java 25 Toolchain 建置修復

### 變更

依照使用者要求，只讓 `C:\Minecraft` 專案使用 Java 25，不修改 Windows 全域 Java 21。已在 `build.gradle` 的 `java` 區塊加入 Gradle Java Toolchain：`languageVersion = JavaLanguageVersion.of(25)`，並保留 `release/source/target = 25`。

### 版本與路徑

| 項目 | 結果 |
|---|---|
| 專案 JDK | `C:\Program Files\Java\jdk-25.0.2` |
| 全域 Java | 保持 Java 21，不要求切換 |
| Minecraft | `26.2` |
| Fabric Loader | `0.19.3` |
| Fabric Loom | `1.17.19` 實際執行版本 |
| Fabric API | `0.158.0+26.2` |
| Gradle | `9.5.1` |

### 查證與驗證

- Fabric 官方開發環境文件要求 JDK 25：https://docs.fabricmc.net/develop/getting-started/setting-up
- Gradle 官方 Java Toolchains 文件確認可在 `java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }` 指定專案級 JDK：https://docs.gradle.org/current/userguide/toolchains.html
- 命令：`gradlew.bat javaToolchains --console=plain`
- 結果：成功偵測 `Oracle JDK 25 (25.0.2+10-LTS-69)`，路徑為 `C:\Program Files\Java\jdk-25.0.2`。
- 命令：`gradlew.bat build --console=plain`
- 結果：`BUILD SUCCESSFUL`；`compileJava`、`compileClientJava`、`jar`、`assemble` 與 `build` 均通過。

### 影響檔案

- `build.gradle`：新增專案級 Java 25 toolchain。
- `AGENTS.md`：更新 Java 25 toolchain 與全域 Java 21 的說明。
- `Mods.md`：追加本次修復與驗證結果。

### 未完成與風險

- 本次只修復建置環境，尚未實作文明模擬 Java 功能。
- Gradle daemon 仍可由全域 Java 21 執行；真正的 Java 編譯任務已由專案 toolchain 使用 JDK 25。
- 尚未啟動 `runClient` 或 dedicated server；下一步仍需做遊戲啟動驗證。

### 下一步

開始第一個文明模擬垂直切片：建立世界級 Saved Data、資料版本與 `/civilization status`，並在完成後再次更新本檔案。

## 2026-08-19 — 第一個文明模擬垂直切片：世界狀態與命令

### 變更

完成第一個可編譯的文明模擬核心切片。新增世界級 `SavedData`，保存文明資料 schema version、simulation steps 與 settlement count；新增 server-side `/civilization status` 查詢命令與 `/civilization simulate` 測試步進命令。命令與世界資料都放在 common source，沒有引入 client-only 類別。

目前可用命令如下：

| 命令 | 用途 |
|---|---|
| `/civilization status` | 顯示 schema、simulation steps 與 settlements 數量。 |
| `/civilization simulate` | 將模擬步數增加 1，呼叫 `setDirty()` 使世界資料可保存。 |

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：世界級持久化文明狀態與 Codec。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：`status` 與 `simulate` 命令。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：註冊 `CommandRegistrationCallback`。
- `Mods.md`：追加本次實作與驗證結果。

### 技術設計

`CivilizationWorldData` 使用 Minecraft `SavedData`、`SavedDataType`、`Codec` 與 Overworld `DimensionDataStorage`。狀態由 logical server 讀寫，變更模擬步數時呼叫 `setDirty()`。資料模型保留 `schema_version`，未來新增聚落、玩家身份、資源與需求時可沿用版本化資料方向。

命令透過 Fabric 26.2 `CommandRegistrationCallback.EVENT.register(...)` 註冊，並由 `CommandSourceStack` 取得 server。`/civilization simulate` 目前只是驗證持久化步進，不代表完整文明模擬已完成。

### 查證與驗證

- Fabric Saved Data 文件：https://docs.fabricmc.net/develop/saved-data；該頁目前標示 26.1.2，因此具體 26.2 相容性以本專案實際編譯結果確認。
- Fabric Commands 26.2 文件：https://docs.fabricmc.net/develop/commands/basics
- Fabric Events 26.2 文件：https://docs.fabricmc.net/develop/events
- 命令：`gradlew.bat compileJava --console=plain`
- 結果：`BUILD SUCCESSFUL`。
- 命令：`gradlew.bat build --console=plain`
- 結果：`BUILD SUCCESSFUL`；common、client、jar 與 assemble 均通過。

### 未完成與風險

- 尚未在遊戲內實際輸入 `/civilization status` 與 `/civilization simulate`。
- 尚未重開世界驗證 `.dat` 保存後 simulation steps 仍存在。
- 尚未加入聚落掃描、外部模組村莊適配器、食物需求、玩家身份或 networking。
- `SavedData` 官方頁面版本標示為 26.1.2，後續若使用更進階的資料欄位或遷移 API，仍須以 26.2 編譯驗證。

### 下一步

請重新啟動或重新載入目前的 client，在世界中輸入 `/civilization status`，再輸入 `/civilization simulate`，確認步數從 0 變成 1；退出世界後重新進入，再輸入 `/civilization status` 驗證步數仍為 1。通過後，下一個切片是建立 `/civilization scan` 與 `SettlementAdapter`，先辨識已生成的原版或外部模組村莊。

## 2026-08-19 — 命令系統加入 Tab completion 強制規則

### 變更

依照使用者要求，將「每個命令、子命令與參數都必須支援 Tab completion」設定為文明模組的正式開發標準。現有 `/civilization status` 與 `/civilization simulate` 使用 Brigadier literal 子命令，已具備基本命令樹補全；後續 `/civilization scan`、聚落來源、身份、政策、事件與其他資料驅動參數都必須提供適當的 argument type 或 suggestion provider。

同步更新其他 AI 進入專案時會讀取的規則與技能：

- `AGENTS.md`：加入命令補全、權限節點與參數驗證規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：加入命令 Tab completion 工作流、測試項目與官方參考。
- 全域 `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步更新並重新驗證。

### 官方查證

- Fabric 26.2 Creating Commands：https://docs.fabricmc.net/develop/commands/basics
- Fabric 26.2 Command Arguments：https://docs.fabricmc.net/develop/commands/arguments

官方文件確認 Fabric 命令使用 Brigadier 命令樹；literal 與子命令提供基本補全，`requires()` 會影響沒有權限的節點是否出現在 Tab completion。新增參數必須使用已查證的 argument type；動態資料需提供 suggestions；自訂 argument type 還必須在 server 與 client 兩側註冊。命令樹在登入時同步給 client，因此避免未同步的 runtime 動態命令修改。

### 驗證

- `cmp`：全域技能與專案內技能副本結果為 `SKILL_COPY_MATCH`。
- `quick_validate.py minecraft-civilization-fabric-262`：結果為 `Skill is valid!`。
- 現有 Java 命令程式未需修改；`status` 與 `simulate` 的 literal 節點由 Brigadier 自動提供基本補全。
- 尚未在遊戲內重新實測 `/civilization <Tab>`、`/civilization s<Tab>`；此項列為下一個手動驗證項目。

### 未完成與風險

- 尚未加入帶動態參數的文明命令，因此 suggestion provider 的實際資料源尚未實作。
- 權限與身份系統尚未加入，之後需要測試不同身份與 OP／非 OP 的補全差異。

### 下一步

下一個 `/civilization scan` 切片從一開始就加入 Tab completion 設計：命令名稱、聚落來源類型、設定 tag／adapter 選項與可能的掃描模式都必須可補全並由 server 驗證。完成遊戲內命令補全手動測試後，再把結果追加到本檔案。

## 2026-08-19 — 第二個文明模擬垂直切片：civilization scan

### 變更

完成 `/civilization scan [radius]`，先掃描玩家目前所在維度附近已生成的原版村莊結構，再將找到的村莊保存成 `SettlementAdapter`。本切片不重寫 worldgen、不生成新村莊，也不把任何外部村莊模組列為核心依賴。

命令介面如下：

| 命令 | 用途 | Tab completion |
|---|---|---|
| `/civilization scan` | 使用預設半徑 128 搜尋最近村莊。 | `scan` literal 可補全。 |
| `/civilization scan <radius>` | 使用 16 至 512 的半徑搜尋。 | 提供 `32`、`64`、`128`、`256` 建議值。 |
| `/civilization status` | 顯示已保存的 settlement 數量。 | 原有 literal 補全保持不變。 |

### 影響檔案

- `src/main/java/com/civilizationmod/SettlementAdapter.java`：新增可 Codec 序列化的聚落適配記錄。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：加入 settlement list、去重邏輯、schema version 2 與舊資料的 optional 欄位相容處理。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：加入 scan 命令、radius integer argument 與 suggestions provider。
- `Mods.md`：追加本次實作、查證、錯誤修正與測試結果。

### 技術設計與 API 查證

從專案實際 Fabric Loom 26.2 merged jar 核對到 `ServerLevel.findNearestMapStructure(TagKey<Structure>, BlockPos, int, boolean)`、`StructureTags.VILLAGE`、`CommandSourceStack.getPosition()`、`CommandSourceStack.getLevel()`、`ResourceKey.identifier()` 與 `BlockPos.containing(Position)`，再以 `compileJava` 驗證；沒有把舊版或未查證的 API 當作事實。

`SettlementAdapter` 保存來源 `minecraft:village`、維度 identifier、中心座標、server tick 辨識時間與 `discovered` 狀態。`CivilizationWorldData` 以 `optionalFieldOf("settlements", List.of())` 載入舊有沒有 settlement 欄位的世界資料，並將 schema version 提升至 2。新增聚落會依維度與中心位置去重。

radius argument 限制在 16 至 512，並透過 `SuggestionProvider` 提供 32、64、128、256；這使 `/civilization scan <Tab>` 不只是文字補全，也能提供受驗證範圍內的可選值。

### 查證與驗證

官方來源：

- [Fabric Commands 26.2](https://docs.fabricmc.net/develop/commands/basics)
- [Fabric Command Arguments 26.2](https://docs.fabricmc.net/develop/commands/arguments)
- [Fabric Command Suggestions 26.2](https://docs.fabricmc.net/develop/commands/suggestions)
- [Fabric Saved Data](https://docs.fabricmc.net/develop/saved-data)

實際驗證命令與結果如下：

| 命令 | 結果 |
|---|---|
| `javap ... net.minecraft.server.level.ServerLevel` | 確認 `findNearestMapStructure(...)` 存在。 |
| `javap ... net.minecraft.tags.StructureTags` | 確認 `VILLAGE` 欄位存在。 |
| `gradlew.bat compileJava --console=plain` | 初次只因 scan 命令括號少一個 `)` 失敗；修正後通過。 |
| `gradlew.bat build --console=plain` | 最終 `BUILD SUCCESSFUL`，common、client、jar、assemble 均通過。 |

### 未完成與風險

- 尚未在遊戲內實測 `/civilization scan` 的成功搜尋、找不到村莊 fallback、重複掃描與 radius Tab completion。
- 本切片目前只使用原版 `StructureTags.VILLAGE`；外部模組村莊的自訂 tag／adapter 設定檔尚未加入。
- `findNearestMapStructure` 的實際搜尋行為仍需要在新世界與已生成村莊世界中觀察；本次只完成 API、編譯與資料模型驗證。
- 目前 settlement count 與聚落記錄已保存，但食物需求與人口模擬尚未接入。

### 下一步

重新啟動或重新載入 client，在已知村莊附近測試：

```text
/civilization <Tab>
/civilization sc<Tab>
/civilization scan <Tab>
/civilization scan 128
/civilization status
```

確認找到村莊後 `settlements` 從 0 變成 1，再重複 scan 確認不會新增重複記錄；退出並重開世界後再次執行 status，確認聚落資料仍存在。通過後，下一個切片是把村莊的可用食物與居民需求接入低頻率模擬步進。

## 2026-08-19 — 玩家語言本地化：en_us、zh_tw、zh_cn

### 變更

依照使用者語言政策，文明模組現在使用玩家選擇的 Minecraft locale 顯示文字：英文 locale 顯示英文；台灣繁體中文 locale 顯示繁體中文；中國簡體中文 locale 也顯示相同的繁體中文，不另外轉成簡體中文。

新增三份語言資源：

- `src/main/resources/assets/civilizationmod/lang/en_us.json`
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`

將 `CivilizationCommands.java` 的 status、simulate、scan 成功、scan 重複與 scan 找不到村莊訊息，從 `Component.literal` 改為 `Component.translatable` translation keys。未來 GUI、HUD、身份、食物需求、事件與錯誤訊息都必須沿用相同模式。

### 語言政策

| Minecraft locale | 顯示內容 |
|---|---|
| `en_us` | 英文。 |
| `zh_tw` | 台灣繁體中文。 |
| `zh_cn` | 與 `zh_tw` 相同的繁體中文。 |

### 官方查證

- [Fabric Text and Translations 26.2](https://docs.fabricmc.net/develop/text-and-translations)：確認使用 `Component.translatable` 與 translation key；缺少 key 時才會退回顯示 key。
- [Fabric Translation Generation 26.2](https://docs.fabricmc.net/develop/data-generation/translations)：確認語言資源可依 locale 建立，並可由 datagen provider 生成。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationCommands.java`：所有現有命令回覆改用翻譯 key。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：英文翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：繁體中文翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依政策使用相同繁體中文翻譯。
- `AGENTS.md`：加入所有玩家可見文字必須本地化的規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：加入 locale 與 Component.translatable 工作流。
- `Mods.md`：追加本次本地化變更與驗證結果。

### 查證與驗證

- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；common、client、resources、jar、assemble 均通過。
- 全域技能與專案內技能副本比對：`SKILL_COPY_MATCH`。
- 語言 JSON 目前由 Gradle `processResources` 成功處理；尚未在 client 內逐一切換三種 locale 手動確認顯示結果。

### 未完成與風險

- 尚未在遊戲內切換 `en_us`、`zh_tw`、`zh_cn` 逐一測試命令回覆。
- 現有語言檔只有目前命令使用的 keys；後續加入 GUI、身份、食物、事件或政策時，必須同步更新三份語言檔。
- 尚未啟用 datagen 產生語言檔；目前先使用手寫 JSON，之後可在翻譯規模增大時加入 language provider。

### 下一步

請重新啟動 client 或重新載入資源，分別切換英文、台灣繁體中文與中國簡體中文，再執行 `/civilization status`、`/civilization simulate` 與 `/civilization scan 128`，確認英文顯示英文，而 `zh_tw` 與 `zh_cn` 都顯示繁體中文。

## 2026-08-19 — 玩家訊息品牌前綴：創世紀元／Civitas

### 變更

依照使用者要求，所有傳給玩家的文明模組訊息必須有統一品牌前綴與尾標。新增 common-side `CivilizationMessages` 包裝器，現有文明命令的玩家回覆全部改由此包裝器產生；後續事件、身份、食物、GUI、HUD、錯誤與提示訊息也必須使用同一入口。

### 格式政策

| Locale | 玩家訊息格式 |
|---|---|
| `zh_tw` | `|創世紀元| : {模組訊息} §6 |創世紀元|§r` |
| `zh_cn` | 與 `zh_tw` 相同：`|創世紀元| : {模組訊息} §6 |創世紀元|§r` |
| `en_us` | `|Civitas| : {模組訊息} §6|Civitas|§r` |

實作使用 `Component` 與 `ChatFormatting.GOLD` 將金色範圍限制在尾標元件，達到 `§6` 後接品牌尾標並以元件邊界重置樣式的效果，避免顏色洩漏到後續聊天文字。中文尾標依使用者指定保留品牌前的空格；英文尾標依使用者指定不加入該空格。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationMessages.java`：新增共用品牌訊息包裝器。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：status、simulate、scan 全部改用 `CivilizationMessages.translatable(...)`。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：加入 `civilizationmod.message.prefix/suffix` 的 `|Civitas|`。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：加入 `civilizationmod.message.prefix/suffix` 的 `|創世紀元|`。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：加入相同的繁體中文 `|創世紀元|`。
- `AGENTS.md`：加入玩家訊息必須經過品牌包裝器的強制規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步加入品牌訊息工作流。
- `Mods.md`：追加本次變更與驗證結果。

### 查證與驗證

- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；common、client、resources、jar、assemble 均通過。
- `quick_validate.py minecraft-civilization-fabric-262`：結果為 `Skill is valid!`。
- 全域技能與專案內技能副本：`SKILL_COPY_MATCH`。

### 未完成與風險

目前只改造已存在的命令訊息；後續新增任何玩家訊息都必須先建立 translation key，再透過 `CivilizationMessages` 發送。尚未在遊戲內切換 `en_us`、`zh_tw`、`zh_cn`，確認前綴、尾標空格與金色顯示效果；這是下一個手動測試項目。

### 下一步

重啟或重新載入 client，分別使用三種 locale 執行 `/civilization status`、`/civilization simulate` 與 `/civilization scan 128`，確認每則訊息都有對應前綴，中文 locale 顯示 `|創世紀元|`，英文 locale 顯示 `|Civitas|`，且尾標為金色並在訊息後重置格式。

## 2026-08-19 — civilization scan 簡寫別名：sc

### 變更

依照使用者要求，新增 `/civilization sc` 作為 `/civilization scan` 的簡寫。兩個命令都支援無參數的預設半徑 128，以及帶有 radius suggestions 的 `<radius>` 參數；兩者共用同一個 `scanCommand(String literal)` builder、同一個 scan handler、同一套資料保存、翻譯訊息與後續驗證邏輯，不複製兩套容易分歧的命令實作。

目前可用命令如下：

| 命令 | 用途 |
|---|---|
| `/civilization scan` | 完整命令，使用預設半徑 128。 |
| `/civilization scan <radius>` | 完整命令，radius 範圍 16–512，Tab 建議 32、64、128、256。 |
| `/civilization sc` | scan 的簡寫，使用預設半徑 128。 |
| `/civilization sc <radius>` | scan 的簡寫版本，使用相同 radius 限制與 suggestions。 |

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 `scanCommand(String literal)`，以 `scan` 與 `sc` 兩個 literal 註冊共用命令樹。
- `AGENTS.md`：規定命令 alias 必須共用 handler、參數、suggestions、權限與翻譯 key。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步加入 alias 設計規則。
- `Mods.md`：追加本次命令別名與驗證結果。

### 查證與驗證

- 命令別名採用現有已編譯使用中的 Brigadier literal builder，不引入未查證的 alias API。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；common、client、resources、jar、assemble 均通過。
- 全域技能與專案內技能副本：`SKILL_COPY_MATCH`。

### 未完成與下一步

尚未在遊戲內重新測試 `/civilization sc` 與 `/civilization sc <Tab>`；下一次啟動 client 後請分別測試：

```text
/civilization sc
/civilization sc <Tab>
/civilization sc 128
/civilization scan 128
```

確認兩個命令回覆相同、都套用 `|創世紀元|`／`|Civitas|` 品牌前綴，且重複掃描去重規則一致。

## 2026-08-19 — 命令根改為 civitas

### 變更

依照使用者選擇，文明模組的主要命令根改為 `/civitas`，與英文品牌 `|Civitas|` 一致。為避免既有世界、測試與文件失效，`/civilization` 暫時保留為相容別名。兩個根命令各自建立相同的 Brigadier literal 命令樹，共用相同的 status、simulate、scan、sc、radius suggestions、server 驗證、本地化訊息與品牌包裝器。

目前推薦命令：

```text
/civitas status
/civitas simulate
/civitas scan
/civitas sc
```

相容命令仍可使用：

```text
/civilization status
/civilization simulate
/civilization scan
/civilization sc
```

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 `rootCommand(String literal)`，註冊 `civitas` 主命令與 `civilization` 相容別名。
- `AGENTS.md`：指定 `/civitas` 為主要命令根，保留 `/civilization` 相容別名。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步命令根遷移與 alias 規則。
- `Mods.md`：追加本次命令根變更與驗證結果。

### 查證與驗證

命令根使用現有已編譯的 Brigadier literal builder，沒有引入未查證的 alias API；兩個根命令各自建立命令樹，但所有節點都共用相同 handler 與命令 builder helper。

- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；common、client、resources、jar、assemble 均通過。
- 全域技能與專案內技能副本：`SKILL_COPY_MATCH`。

### 未完成與下一步

尚未在遊戲內重新測試 `/civitas <Tab>`、`/civitas status`、`/civitas sc <Tab>` 與 `/civilization <Tab>`。下一步應先確認新主命令與舊相容命令都能正常補全和執行，再進入聚落食物需求切片。

## 2026-08-19 — 玩家訊息格式修正：只保留金色前綴

### 變更

依照使用者最新確認，移除所有玩家訊息尾端品牌。現在格式只保留前綴與正文：

| Locale | 最新格式 |
|---|---|
| `zh_tw` | `|創世紀元| : {模組訊息}` |
| `zh_cn` | `|創世紀元| : {模組訊息}`，正文仍使用繁體中文 |
| `en_us` | `|Civitas| : {message}` |

只有最前面的 `|創世紀元|` 或 `|Civitas|` 使用金色；冒號、空格與正文使用一般樣式。`CivilizationMessages` 不再讀取或追加 `message.suffix`。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationMessages.java`：移除 suffix key 與尾端品牌，將 prefix component 套用 `ChatFormatting.GOLD`，分隔符和正文套用一般樣式。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：移除 `civilizationmod.message.suffix`，保留 `|Civitas|` prefix。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：移除 suffix，保留 `|創世紀元|` prefix。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：移除 suffix，保留相同繁體中文 prefix。
- `AGENTS.md`：將玩家訊息規則修正為金色前綴、一般正文、無尾端品牌。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步最新訊息格式規則。
- `Mods.md`：追加本次格式修正與驗證結果。

### 查證與驗證

- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；common、client、resources、jar、assemble 均通過。
- 全域技能與專案內技能副本：`SKILL_COPY_MATCH`。
- `Component` 樣式設計：prefix 使用 `ChatFormatting.GOLD`；separator 與 translated body 使用 `ChatFormatting.RESET`，避免金色延伸到正文。

### 下一步

在遊戲內切換 `zh_tw`、`zh_cn` 與 `en_us`，測試 `/civitas status`。預期畫面為：

```text
|創世紀元| : 文明狀態：資料版本=2，模擬步數=1，聚落=1
|Civitas| : Civilization status: schema=2, simulation steps=1, settlements=1
```

前綴應為金色，正文不應是金色，且訊息末尾不再出現第二個品牌名稱。

## 2026-08-19 — 第一版聚落級食物需求模擬

### 變更

`/civitas simulate` 與 `/civilization simulate` 已從單純增加步數，擴充為第一版 server-side 聚落級食物需求模擬。命令仍可不帶參數執行一次，也新增可補全的步數參數：

```text
/civitas simulate
/civitas simulate <steps>
/civilization simulate
/civilization simulate <steps>
```

`steps` 目前限制為 `1–100`，Tab suggestions 為 `1、5、10、25、50、100`。每一步以 `population × 2` 計算食物需求，從聚落級 `food_stock` 扣除；不足量成為 `food_shortage`，並累積 `stability_debt`、降低 `stability`。食物充足時，短缺債務逐步下降，穩定度每步最多恢復 1。

本切片刻意不查詢每個村民、箱子或外部模組內部資料。新掃描到的原版村莊先使用可替換的 deterministic bootstrap provider：人口 `10`、食物存量 `20`、穩定度 `100`。這是模擬閉環的測試 fallback，未來可由外部 SettlementAdapter 或資源掃描器替換，不構成外部模組硬依賴。

### 影響檔案

- `src/main/java/com/civilizationmod/SettlementAdapter.java`：新增可保存的 `population`、`foodStock`、`stabilityDebt`、`stability` 欄位；新增 optional Codec 欄位預設值與 `withFoodState`。
- `src/main/java/com/civilizationmod/FoodDemandModel.java`：建立聚落級人口食物需求、消耗與短缺運算。
- `src/main/java/com/civilizationmod/FoodStockProvider.java`：建立 deterministic bootstrap 資源來源，保留未來接入箱子、村民或外部模組資料的替換點。
- `src/main/java/com/civilizationmod/StabilityDebt.java`：建立短缺債務與穩定度的 clamp、增加、恢復規則。
- `src/main/java/com/civilizationmod/FoodSimulationSummary.java`：建立非持久化的模擬摘要。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：新增 `advanceSimulation()` 與 `getFoodSummary()`；逐座聚落更新資料並呼叫 `setDirty()`。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 simulate steps argument 與 suggestions；status 顯示人口、食物存量、穩定度債務與穩定度；scan 建立初始食物狀態。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增食物模擬與穩定度英文 placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文食物模擬與穩定度訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依專案政策同步相同繁體中文訊息。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：加入已查證的 Saved Data、命令與食物需求切片設計規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能副本。
- `Mods.md`：追加本次實作與驗證結果。

### API 查證

Fabric 官方 Saved Data 文件確認本次沿用的 `SavedData`、`SavedDataType`、Codec 與 `DimensionDataStorage.computeIfAbsent()` 保存模式；修改後呼叫 `setDirty()` 才會寫回世界資料。Fabric 官方命令文件確認 server command 使用 Brigadier literal、argument、suggestions 與正整數結果。本次沒有新增未查證的 worldgen、Mixin、networking 或 client-only API。

來源：

- [Fabric Saved Data](https://docs.fabricmc.net/develop/saved-data)，查證日期 2026-08-19。
- [Fabric Creating Commands 26.2](https://docs.fabricmc.net/develop/commands/basics)，查證日期 2026-08-19。
- [Fabric Porting to 26.2](https://docs.fabricmc.net/develop/porting/)，查證日期 2026-08-19。

### 查證與驗證

- Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Loom 實際顯示 `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；`compileJava`、resources、jar、assemble 與 build 均通過。
- 技能副本同步：`SKILL_COPY_MATCH`。
- `quick_validate.py minecraft-civilization-fabric-262`：`Skill is valid!`。
- 尚未在遊戲內實測 locale 顯示、`/civitas simulate <steps>` Tab completion、世界重開後的新增聚落欄位與實際村莊掃描結果，標記為未驗證。

### 未完成與風險

目前人口與食物存量仍是 deterministic bootstrap fallback，不代表實際村民數量或箱子內食物數量。穩定度債務是第一版聚落級壓力指標，尚未觸發離村、事件或身份轉換。舊 Saved Data 的新聚落欄位依 optional Codec 預設值載入，但仍需用既有世界實際重開確認。

### 下一步

重啟 client 後先測試 `/civitas scan 128`、`/civitas status`、`/civitas simulate`、`/civitas simulate 5`、`/civitas simulate <Tab>`，並分別切換 `zh_tw`、`zh_cn`、`en_us` 確認品牌前綴與食物摘要。遊戲內確認後，再進入人口來源與食物庫存 provider 的資料驅動化，之後才加入身份服務與政策規則。

## 2026-08-19 — 採用 Grok 可重現食物需求演算法：schema 3 與最近事件

### 設計審查結論

已將使用者提供的 Grok 深入演算法與目前實作逐項比對。其核心方向被採用：低頻率、聚落級、無隨機、同一初始狀態加同一步數必須得到相同結果、不掃描個別村民、不把自動步進與第一版手動驗證混在一起。現有的 stability debt 保留作為短缺壓力指標，但穩定度損失改為 Grok 建議的「每次發生短缺固定損失 5」，而不是依短缺單位量逐點扣除。

### 變更

`SettlementAdapter` 的資料模型升級為：

| 欄位 | 型別 | 第一版規則 |
|---|---|---|
| `food_stock` | `long` | 抽象食物單位，扣除後不低於 0 |
| `population` | `int` | 新掃描聚落 fallback 為 20 |
| `food_consumption` | `int` | 每人每步消耗，預設 1，最小值 1 |
| `stability_debt` | `int` | 短缺量累積，受上限保護 |
| `stability` | `int` | 0–100，初始 100 |
| `last_food_event` | `String` | 受控識別字串：`stable`、`surplus`、`shortage` |

世界資料 `CURRENT_SCHEMA_VERSION` 升至 3。新增聚落欄位使用 optional Codec 預設值，因此舊資料可用人口 0、食物 0、消耗 1、穩定度債務 0、穩定度 100、事件 `stable` 載入。重複 scan 仍沿用既有位置去重，不會重置食物、穩定度或最近事件。

單步演算法現在固定為：需求 `population × food_consumption`；消耗 `min(food_stock, demand)`；不足量形成 shortage；有 shortage 時 stability debt 增加 shortage、stability 固定減 5、事件為 `shortage`；無 shortage 且扣除後存量大於 `demand × 2` 時 stability 最多增加 1、debt 減 1、事件為 `surplus`；其餘情況事件為 `stable`。每個世界步驟完成所有聚落後才增加 simulation steps 並呼叫 `setDirty()`。

初始 fallback 從上一版的人口 10、食物 20 改為 Grok 建議的人口 20、食物 100。以每步需求 20 計算，前五步會把食物消耗至 0，穩定度維持 100；第六步開始因短缺 20 而穩定度下降 5、債務增加 20。這個結果完全由保存狀態決定，不使用隨機數。

### 影響檔案

- `src/main/java/com/civilizationmod/SettlementAdapter.java`：新增 long `foodStock`、`foodConsumption` 與 `lastFoodEvent`，更新 Codec optional 欄位與 `withFoodState`。
- `src/main/java/com/civilizationmod/FoodDemandModel.java`：改採每人消耗量、吃光剩餘食物、固定短缺損失、盈餘條件與三種事件識別字串。
- `src/main/java/com/civilizationmod/FoodStockProvider.java`：初始 fallback 改為人口 20、食物 100、每步消耗 1。
- `src/main/java/com/civilizationmod/StabilityDebt.java`：加入穩定度上下限與固定損失／恢復常數，短缺累積支援 long。
- `src/main/java/com/civilizationmod/FoodSimulationSummary.java`：食物需求、消耗、短缺與存量改為 long，加入最近事件。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：schema version 改為 3，保存並彙總最近事件與 long 食物數值。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：scan 使用新欄位；status 與 simulate 顯示最近事件；事件識別字串透過受控 translation key 本地化後再經品牌包裝器輸出。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：加入 stable、surplus、shortage 英文翻譯與事件 placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：加入穩定、盈餘、短缺繁體中文翻譯與事件 placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文翻譯。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：寫入可重複使用的 Grok 演算法規則與驗證情境。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能副本。
- `Mods.md`：追加本次設計審查、實作與驗證結果。

### 查證與驗證

- 版本：Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Loom 實際顯示 `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。
- 版本敏感部分只沿用已存在並已編譯驗證的 Saved Data、Codec 與 Brigadier API；新增 `Codec.LONG` 也由專案實際編譯確認。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、resources、jar、assemble 與 build 均通過。
- 技能同步：`SKILL_COPY_MATCH`。
- `quick_validate.py minecraft-civilization-fabric-262`：`Skill is valid!`。
- 尚未遊戲內驗證：三種 locale 的事件 Component 顯示、schema 2 舊世界實際重開、`/civitas simulate <Tab>`、重複 scan 不重置數值，以及 status 中巢狀事件 Component 的顯示格式。

### 未完成與風險

目前人口與食物仍是 deterministic bootstrap fallback，不是真實村民或箱子資料。最近事件目前是世界聚落摘要中最後一個聚落的事件，尚未做逐座聚落 status 子命令。穩定度低於門檻尚未觸發移民、暴動、身份轉換或其他複雜事件。自動定時步進仍刻意延後，先以手動 simulate 完成回歸測試。

### 下一步

重啟 client 後測試：先 `/civitas scan 128`，再 `/civitas status` 確認人口 20、食物 100、穩定度 100；執行 `/civitas simulate 5` 後確認食物 0 且穩定度仍 100；再執行一次 simulate，確認事件變為短缺、債務增加、穩定度下降 5。接著退出並重開世界，確認 schema 3 與所有聚落欄位仍存在；通過後才進入實際人口／食物 provider 與逐座聚落觀測命令。

## 2026-08-19 — 玩家訊息可讀性修正：明確品牌豎線、短摘要與平均穩定度

### 問題來源

使用者已成功完成雙聚落食物模擬測試，但回報遊戲內訊息顯示怪異。從畫面結果確認數值運算正確，主要可讀性問題有兩項：品牌前綴的左右豎線由 locale 文字提供，視覺上容易與聊天換行或字形混在一起；status 與 simulate 的完整欄位名稱過長，造成聊天欄大量換行。此外，多座聚落的穩定度原本直接加總，兩座穩定度 100 的聚落會顯示 `穩定度=200`，容易被誤解為單一聚落超過上限。

### 變更

`CivilizationMessages` 現在由 Java 明確建立：

```text
金色 | + locale 品牌名稱 + 金色 | + 一般樣式 " : " + 一般樣式正文
```

因此 locale 只保存品牌名稱本身：`Civitas` 或 `創世紀元`，不再保存左右豎線。訊息格式仍符合專案政策：`|Civitas| : {message}` 與 `|創世紀元| : {模組訊息}`，只有前綴金色，沒有尾端品牌。

status 與 simulate 的正文改成短欄位名稱，減少換行：

| Locale | status 範例 |
|---|---|
| `zh_tw`／`zh_cn` | `狀態：版本=3，步數=7，聚落=2，人口=40，食物=0，債務=40，穩定度=95，事件=短缺` |
| `en_us` | `Status: schema=3, step=7, settlements=2, population=40, food=0, debt=40, stability=95, event=shortage` |

多聚落摘要的 `stability` 改為所有已登記聚落的整數平均值，保持 `0–100` 的語意；人口、食物、短缺與債務仍然是總和。兩座各自穩定度 100 的聚落現在顯示穩定度 100；兩座各自 95 的聚落顯示 95。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationMessages.java`：由 Java 明確建立金色左右豎線，locale 只提供品牌名稱。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：`getFoodSummary()` 與 `advanceSimulation()` 回傳平均穩定度，不再回傳跨聚落穩定度總和。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：縮短 status／simulate 文字與品牌 prefix value。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：縮短繁體中文 status／simulate 文字與品牌 prefix value。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文文字。
- `Mods.md`：追加本次問題、設計、API 查證與驗證結果。

### API 查證

Fabric 官方 Text and Translations 26.2 文件確認 `Component.literal` 用於直接建立文字、`Component.translatable` 用於 locale key 與參數，且 `%1$s` 可以接收另一個 Component 作為翻譯參數；官方格式文件也確認 `ChatFormatting.GOLD` 與 `RESET` 可用於樣式控制。本次只使用已查證的 Component、translation 與 ChatFormatting API，沒有加入未查證的換行或 client-only API。

來源：[Fabric Text and Translations 26.2](https://docs.fabricmc.net/develop/text-and-translations)，查證日期 2026-08-19。

### 查證與驗證

- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、resources、jar、assemble 與 build 均通過。
- `Component.literal`、`Component.translatable`、`ChatFormatting.GOLD` 與 `ChatFormatting.RESET`：由 Fabric 官方文字文件查證，並由 Java 25 編譯通過。
- 使用者已實際確認雙聚落模擬、食物消耗、短缺與世界級步進成功；本次修正後尚待重新啟動 client 確認新版外觀。

### 未完成與風險

新版訊息的實際字體、顏色與聊天欄換行仍需在遊戲內重新確認。當聚落數量非常多時，status 仍會使用世界級彙總；逐座聚落觀測命令尚未加入。平均穩定度採整數除法，若多座聚落穩定度不同會捨去小數部分，但仍保持 0–100 的可理解範圍。

### 下一步

重啟 client 或重新載入模組後，執行 `/civitas status`、`/civitas simulate`、`/civitas scan 128`，確認每則訊息都呈現為 `|創世紀元| : ...`，左右豎線完整、前綴金色、正文一般樣式，且 status／simulate 不再出現過度破碎的換行。雙聚落測試應顯示穩定度 100 或 95，而不是 200 或 190。

## 2026-08-19 — 逐座聚落觀測命令：settlement

### 變更

為了避免只看世界級彙總而無法確認每座村莊的獨立狀態，新增：

```text
/civitas settlement
/civitas settlement <index>
```

`/civilization settlement` 也同步支援。無參數時列出所有已登記聚落；帶有一-based `index` 時只顯示指定聚落。這是純查詢命令，不會推進模擬、不會修改 Saved Data，也不依賴玩家目前所在位置。

每座聚落目前顯示來源、維度、中心座標、人口、需求、食物、每人消耗、穩定度債務、穩定度與最近食物事件。`/civitas settlement <Tab>` 會根據 server 當前已登記聚落動態提供 `1` 到聚落數量的 suggestions；非法 index 會回傳本地化錯誤，不會繞過 server 驗證。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：新增一-based `getSettlement(int)`，只讀取聚落資料。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 `settlement` literal、index integer argument、動態 suggestions、逐座查詢與空／非法 index fallback。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增 settlement entry、none、invalid 翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增逐座聚落繁體中文翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文翻譯。
- `Mods.md`：追加本次查詢命令與驗證結果。

### 設計與邊界

查詢命令沒有新增 client-only 類別、Mixin 或 networking；它直接讀取 server-owned `CivilizationWorldData`。命令根 `/civitas` 與相容別名 `/civilization` 共享同一個 `settlementCommand()` builder、handler、argument、suggestions 與翻譯 key。世界級 `/civitas status` 仍顯示彙總，逐座狀態改由 `/civitas settlement` 觀測。

### 查證與驗證

- Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Loom 實際顯示 `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。
- 沿用已編譯驗證的 Brigadier literal、integer argument、suggestion provider 與 Saved Data getter；沒有猜測新的版本敏感 API。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、resources、jar、assemble 與 build 均通過。
- 尚未在遊戲內測試 `/civitas settlement`、`/civitas settlement <Tab>`、空聚落與非法 index，標記為未驗證。

### 未完成與風險

目前 settlement entry 顯示的是保存的聚落級 bootstrap 人口與食物資料，還不是真實村民或箱子掃描結果。逐座查詢使用 index，聚落順序是 Saved Data 登記順序；未來若加入刪除、合併或排序，需要改成穩定 ID 或顯示來源與座標 suggestions。

### 下一步

重啟 client 後執行 `/civitas settlement <Tab>`，確認能補全已登記聚落編號；執行 `/civitas settlement` 確認列出所有聚落，再執行 `/civitas settlement 1`、`/civitas settlement 2` 比較各自食物與穩定度。之後再進入實際人口 provider 與 FoodStockProvider 的資料來源切片。

## 2026-08-19 — Claude 審查修正：多聚落事件聚合、count 真相來源與純 Java 回歸測試

### 審查採用結論

依照 Claude 對目前程式的審查，先處理會直接影響長期正確性的三項問題：世界級事件不能只留下最後一座聚落的事件；`settlementCount` 不應和 settlement list 維護兩份可變真相；食物純 Java 規則需要可重複回歸測試。Overworld null fallback 則保留非破壞性 fallback，但加入明確 logger 警告，定義該物件為不會保存的 transient state。

### 變更

`CivilizationWorldData` 現在只以 `settlements.size()` 作為聚落數量真相來源。Saved Data 的既有 `settlement_count` 欄位仍保留以讀寫舊格式，但載入時不再採用該欄位覆蓋 list；constructor 的相容參數明確標為 ignored。未使用的 `setSettlementCount()` 已移除，避免外部呼叫讓數量與 list 脫鉤。

世界級食物事件改為優先級聚合：

```text
任一聚落 shortage → 世界事件 shortage
否則任一聚落 surplus → 世界事件 surplus
否則 → 世界事件 stable
```

因此多座聚落中即使最後一座是 stable，只要前面任一座有 shortage，`/civitas status` 仍會顯示短缺，不再被最後迭代的聚落覆蓋。逐座 `/civitas settlement <index>` 仍顯示每座聚落自己的事件。

當 `CivilizationWorldData.get()` 在 Overworld 尚未可用時，仍回傳 transient `CivilizationWorldData` 以避免在非正常生命週期直接崩潰，但會透過 `CivilizationMod.LOGGER.warn(...)` 明確記錄「資料不會保存」。正常 server 命令於 Overworld ready 後仍使用 `DimensionDataStorage.computeIfAbsent(TYPE)`。

新增 `FoodModelRegressionTest` 純 Java 回歸檢查，涵蓋需求 normalization、短缺消耗、債務增加、穩定度下降、盈餘恢復、stable 門檻、debt／stability clamp，以及初始食物 100 經五步耗盡後第六步短缺的 deterministic 結果。為不增加第三方 JUnit 版本依賴，測試使用 `main` 與 `AssertionError`，由 `runFoodModelRegressionTest` JavaExec task 執行。

`build.gradle` 新增：

```text
gradlew.bat runFoodModelRegressionTest --console=plain
```

該 task 使用 `sourceSets.test.runtimeClasspath`，並以 Gradle 官方 `javaToolchains.launcherFor` 固定 Java 25。由於專案目前是 main-based regression check 而不是 JUnit test discovery，`test` task 設定 `failOnNoDiscoveredTests = false`；真正的回歸檢查由專用 task 明確執行。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：移除可變 settlementCount field／setter，聚落數量改由 list size 決定；加入聚合事件優先級；加入 Overworld null logger 警告。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：新增純 Java 食物模型回歸檢查。
- `build.gradle`：新增 Java 25 `runFoodModelRegressionTest` task 與無 JUnit discovery 時的 test task fallback。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：加入 Gradle Java 25 回歸測試工作流與官方 toolchain 查證。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能副本。
- `Mods.md`：追加本次審查修正、失敗修復與驗證結果。

### API／工具查證

Gradle 官方 Toolchains 文件確認 JavaExec／Test task 可使用 `JavaLauncher`，並可透過 `javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }` 固定 task 使用的 Java 版本；Gradle 官方 JavaExec DSL 確認 `classpath`、`mainClass` 與 `javaLauncher` 屬性。本次 Minecraft/Fabric 程式碼只沿用已編譯驗證的 Saved Data 與 Logger 使用方式，沒有猜測新的 Minecraft 26.2 API。

來源：[Gradle Toolchains for JVM projects](https://docs.gradle.org/current/userguide/toolchains.html)、[Gradle JavaExec DSL](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.JavaExec.html)，查證日期 2026-08-19。

### 查證與驗證

- `gradlew.bat runFoodModelRegressionTest --console=plain`：第一次建立後通過，輸出 `FoodModelRegressionTest: PASS`。
- 第一次 `gradlew.bat build --console=plain`：因加入 `src/test` 但沒有 JUnit discovered test，Gradle `:test` 失敗；此原因已修正並記錄，不視為程式邏輯失敗。
- 修正後再次執行 `gradlew.bat runFoodModelRegressionTest --console=plain`：`FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。
- 修正後再次執行 `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、compileTestJava、resources、jar、assemble、test、check 與 build 均通過。
- 技能同步：`SKILL_COPY_MATCH`。
- `quick_validate.py minecraft-civilization-fabric-262`：`Skill is valid!`。
- 尚未遊戲內驗證：多聚落混合事件 status、Overworld fallback logger、舊世界 schema 3 重開與 pure regression task 外的 dedicated server。

### 未完成與風險

`settlement_count` 仍保留在 Saved Data Codec 只是為了讀寫舊格式，未來若確認所有存檔可安全遷移，可以再設計正式的 schema migration，而不是立即刪除欄位。座標精確去重風險仍存在；目前先不使用距離閾值，避免把相鄰的不同村莊錯誤合併，待穩定 structure ID／來源 ID 設計完成後再處理。純 Java 測試目前是可執行回歸檢查，不是 JUnit test report。

### 下一步

遊戲內建立兩座聚落，讓一座進入 shortage、另一座保持 stable，執行 `/civitas status` 確認世界事件顯示 shortage；再執行 `/civitas settlement 1` 與 `/civitas settlement 2` 確認個別事件不同。接著測試 `/civitas settlement <Tab>`、世界重開與三份 locale。通過後才進入實際人口 provider 與 FoodStockProvider 資料來源切片。

## 2026-08-19 — 重複 scan bug：結構代表座標漂移與容差去重

### 根因確認

依照使用者提供的分析，`findNearestMapStructure` 的搜尋結果不能被視為同一座結構永遠具有同一個精確代表座標。玩家在不同位置執行 scan 時，底層區塊搜尋可能命中同一座村莊不同區塊或 piece 的代表位置，因此第二次回傳的 `BlockPos` 可能與第一次不同。原本 `SettlementAdapter.isAt()` 使用 x、y、z 全部精確相等，導致同一座村莊被誤判成新聚落。

### 實作修正

本次將去重從精確座標比對改為同來源、同維度、容許座標漂移的聚落辨識：

| 條件 | 規則 |
|---|---|
| source | 必須相同，例如 `minecraft:village` |
| dimension | 必須完全相同，例如 `minecraft:overworld` |
| 水平 X/Z | 各自差異不超過 64 格 |
| 垂直 Y | 差異不超過 32 格 |

新增 `SettlementAdapter.isSameSettlement(...)` 與容差常數 `DEDUP_HORIZONTAL_RADIUS=64`、`DEDUP_VERTICAL_RADIUS=32`。`CivilizationWorldData.addSettlement()` 改用此方法；如果判定為既有聚落，直接拒絕新物件，不會觸碰原有聚落，因此原本的食物、債務、穩定度與事件會自然保留，不需要額外合併邏輯。

這個容差是目前可調整的第一版預設，不宣稱是所有村莊結構的正式邊界。它比單一 48 格球形半徑更寬鬆地容納 X/Z 軸方向的掃描代表點漂移，但仍保留不同來源、不同維度與明顯遠距離村莊的區分。

### 回歸測試

`FoodModelRegressionTest` 新增去重案例：

- 同來源、同維度、座標偏移 X=48、Z=32 的第二次 scan 被拒絕。
- 重複 scan 後聚落數量仍為 1。
- 原聚落食物、穩定度債務與穩定度仍保持原值。
- 不同 source 即使座標接近仍可登記。
- 同 source 但超過水平 64 格仍可登記為不同聚落。

### 影響檔案

- `src/main/java/com/civilizationmod/SettlementAdapter.java`：新增容差去重常數與 `isSameSettlement(...)`。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：`addSettlement()` 改用穩健聚落辨識。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：新增重複 scan、狀態保留與不同聚落區分測試。
- `Mods.md`：追加根因、修正、風險與驗證結果。

### 查證與驗證

- 本次沒有新增版本敏感的 Minecraft/Fabric API；只使用既有已編譯的資料模型與純 Java 座標數學。
- `gradlew.bat runFoodModelRegressionTest --console=plain`：`FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、compileTestJava、test、check、jar、assemble 與 build 均通過。
- 尚未取得使用者實際兩次 scan 的座標差，因此目前 64／32 容差仍是可調整預設，不是由該世界的實測差值反推。

### 未完成與風險

若兩座不同村莊在同一 source、同一 dimension 且水平 X/Z 各自落在 64 格容差內，可能被視為同一聚落；這是容差去重的必要誤判風險。下一階段應優先保存穩定的 structure ID、結構 tag 加上可驗證的中心／邊界資訊，取代單純座標容差。`isAt()` 仍保留作為精確座標相容 helper，目前已確認新 `addSettlement()` 不再呼叫它。

### 下一步

重新啟動 client，在同一座村莊的兩個明顯不同位置執行：

```text
/civitas scan 128
/civitas simulate 1
/civitas settlement
前往同一座村莊另一側
/civitas scan 128
/civitas settlement
```

預期第二次 scan 回報已知村莊，聚落數量不增加，且第一次模擬後的食物、債務、穩定度與事件保持不變。再到距離超過 64 格的另一座村莊執行 scan，確認真正不同的聚落仍可新增。

## 2026-08-19 — 舊世界重複聚落自動清理

### 變更

在前一版容差去重之外，`CivilizationWorldData.get()` 載入或取得世界 Saved Data 後，現在會執行一次 `removeDuplicateSettlements()`。它由後往前檢查同來源、同維度且落在去重容差內的聚落，保留最早登記的第一筆，移除後續重複筆，並在有清理時呼叫 `setDirty()` 讓修正保存回世界。

保留第一筆代表既有的食物存量、穩定度債務、穩定度與最近事件不會被新的 bootstrap 物件覆蓋。新的 scan 則在 `addSettlement()` 階段直接拒絕近距離重複，不會新增資料。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：加入取得世界資料時的重複聚落清理流程。
- `src/main/java/com/civilizationmod/SettlementAdapter.java`：沿用同來源／同維度／水平 64／垂直 32 的容差辨識。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：保留去重與狀態保留回歸案例。
- `Mods.md`：追加本次修正與驗證。

### 查證與驗證

- `gradlew.bat runFoodModelRegressionTest --console=plain`：`FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、compileTestJava、test、check、jar、assemble 與 build 均通過。
- 沒有新增版本敏感的 Minecraft/Fabric API；清理流程只使用既有 Saved Data 生命週期與純 Java 聚落辨識。

### 未完成與風險

目前去重容差仍是可調整的第一版預設，尚未使用使用者世界中兩次 scan 的真實座標差反推。若兩座不同村莊在同來源、同維度且 64 格水平容差內，仍可能被合併；未來應以穩定 structure ID、結構邊界或資料驅動來源識別取代單純容差。

### 下一步

重啟 client 後在同一座村莊的不同位置執行兩次 `/civitas scan 128`，確認聚落數量不增加。若舊世界已經有重複資料，先執行 `/civitas status` 或其他會取得 `CivilizationWorldData` 的命令，讓一次性清理執行，再確認重複項目是否消失且第一筆資料狀態保留。

## 2026-08-19 — 修正 `/civitas status` 未列出聚落 #1、#2

### 問題

使用者回報 `/civitas status` 只顯示世界級總覽，雖然 `settlement` 命令已有逐座查詢，但 status 本身沒有列出已登記聚落，因此玩家無法在單次 status 輸出中看到聚落 #1、#2。

### 變更

`CivilizationCommands.status()` 現在保留原本的世界總覽訊息，接著遍歷 `CivilizationWorldData` 中所有已登記聚落，逐行使用既有的 `settlementEntry(index, settlement)` 輸出每座聚落摘要。輸出順序是 Saved Data 登記順序，index 從 1 開始。

```text
/civitas status

世界總覽
聚落 #1 摘要
聚落 #2 摘要
```

`/civilization status` 也會因為共用相同 root builder 與 status handler 而同步修正。沒有新增硬編碼玩家文字、client-only 類別或新的版本敏感 API；逐座摘要仍經由 `Component.translatable` 與 `CivilizationMessages` 品牌包裝器。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationCommands.java`：status 總覽後新增逐座聚落輸出迴圈。
- `Mods.md`：追加本次 bug、修正與驗證。

### 查證與驗證

- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`。
- `compileJava`、`compileClientJava`、`compileTestJava`、`test`、`check`、`jar`、`assemble` 與 `build` 均通過。
- `/civitas settlement` 原有逐座查詢未移除；status 與 settlement 使用相同 `settlementEntry` 格式。
- 尚未在遊戲內重新啟動 client 驗證聊天欄實際順序與長訊息換行。

### 下一步

重新啟動 client 後執行：

```text
/civitas status
/civilization status
```

確認每個命令都依序顯示世界總覽、`聚落 #1` 與 `聚落 #2`。若已登記兩座聚落，聊天欄預期會出現三則訊息；若只有一座，則會出現總覽與 `聚落 #1`。

## 2026-08-19 — 可插拔人口與食物 provider：原版村民觀測

### 變更

將新掃描聚落的固定 bootstrap 資料改為可插拔 provider 流程。新增 `SettlementPopulationProvider` 與 `SettlementFoodProvider` 介面，讓觀測來源和 `FoodDemandModel` 純模擬核心分離。

第一版人口 provider 為 `VanillaVillagerPopulationProvider`：在聚落中心周圍建立有限 AABB，只計算已載入範圍內的原版 `Villager`。它不強制載入 chunk；若查詢不到已載入村民，回傳 empty，由 `FoodStockProvider` 使用人口 20 的 deterministic fallback，不把未載入狀態誤判為人口 0。

第一版食物 provider 為 `BootstrapFoodStockProvider`，仍回傳 100 單位的 bootstrap 食物，但已透過 `SettlementFoodProvider` 介面隔離；未來可以替換成箱子、農田、村民儲備或外部模組 adapter，而不修改 `FoodDemandModel`。

`FoodStockProvider.initialize(...)` 現在只初始化空白的新聚落；若聚落已有人口、食物、債務、非滿穩定度或非 stable 事件，直接保留原物件，避免 scan 或 provider 重跑重置既有狀態。既有的無世界參數 `initialize(SettlementAdapter)` 仍保留作純 Java／相容 bootstrap 入口。

### 已查證的 26.2 API

以專案已解析的 Minecraft 26.2 common jar 實際核對：

- `net.minecraft.world.entity.npc.villager.Villager`
- `Level.getEntities(EntityTypeTest, AABB, Predicate)`
- `EntityTypeTest.forClass(Class<T>)`
- `AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)`

沒有使用猜測的 `getEntitiesOfClass`；實際 26.2 mappings 可用的是 `Level.getEntities(...)` 搭配 `EntityTypeTest` 與 `AABB`。查證方式、provider 設計規則與版本記錄已寫入全域及專案技能副本。

### 影響檔案

- `src/main/java/com/civilizationmod/SettlementPopulationProvider.java`：人口 provider 介面。
- `src/main/java/com/civilizationmod/SettlementFoodProvider.java`：食物 provider 介面。
- `src/main/java/com/civilizationmod/VanillaVillagerPopulationProvider.java`：原版已載入村民人口 provider。
- `src/main/java/com/civilizationmod/BootstrapFoodStockProvider.java`：第一版可替換 bootstrap 食物 provider。
- `src/main/java/com/civilizationmod/FoodStockProvider.java`：provider 初始化、fallback 與既有狀態保留。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：scan 接入原版人口與 bootstrap 食物 provider。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：加入 provider 觀測資料、fallback 與狀態保留測試。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：新增 26.2 provider API 查證與設計規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步技能副本。

### 查證與驗證

- `gradlew.bat runFoodModelRegressionTest --console=plain`：`FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、compileTestJava、test、check、jar、assemble 與 build 均通過。
- 技能同步：`SKILL_COPY_MATCH`。
- 技能格式：`Skill is valid!`。
- 第一次 provider build 曾因誤用 `OptionalInt.map`／`OptionalLong.map` 失敗；已改用 26.2／Java 可用的 `isPresent()`、`getAsInt()` 與 `getAsLong()`，修正後 build 通過。

### 未完成與風險

目前只計算 AABB 範圍內已載入的原版村民，不會主動載入區塊，也尚未把村民是否真正屬於某座村莊的 POI／村莊邊界納入過濾；因此這是第一版「附近已載入村民」觀測，不是完整村莊人口統計。食物仍是 bootstrap 100，尚未掃描箱子、農田或村民物品。外部模組村莊仍沒有硬依賴。

### 下一步

重啟 client 後，在一座有已載入村民的原版村莊附近執行：

```text
/civitas scan 128
/civitas settlement 1
/civitas status
```

比較新掃描聚落的人口是否反映附近已載入村民數量；移動到另一座沒有載入村民的村莊或較遠位置重新測試，確認沒有觀測資料時仍使用人口 20 fallback。接著進入真正的食物來源 provider，先查證 26.2 容器／物品資料 API，再決定箱子掃描範圍與食物 tag 策略。

## 2026-08-19 — 修正已知聚落重新 scan 不更新人口

### 根因

使用者站在村民旁邊重新執行 `/civitas scan 128` 仍看到人口 20，原因不是原版村民查詢 API 沒有回傳資料，而是已登記聚落會在 `FoodStockProvider.initialize(...)` 的 `hasExistingState(...)` 分支直接保留舊物件；重新 scan 從未重新套用人口 provider。

### 變更

現在 scan 分成兩條路徑：

| 情況 | 行為 |
|---|---|
| 新聚落 | 使用人口 provider 與食物 provider 初始化人口／食物，沒有觀測資料才使用 fallback |
| 已知聚落 | 使用人口 provider 重新觀測，只替換人口欄位 |
| 已知聚落的食物 | 完全保留 |
| 已知聚落的債務 | 完全保留 |
| 已知聚落的穩定度 | 完全保留 |
| 已知聚落的最近事件 | 完全保留 |

新增 `SettlementAdapter.withPopulation(...)`、`CivilizationWorldData.findSettlement(...)`、`CivilizationWorldData.replaceSettlement(...)` 與 `FoodStockProvider.refreshPopulation(...)`。`scan` 的已知聚落分支現在會發送 `civilizationmod.command.scan.refreshed`，顯示重新觀測到的人口。

### 本地化

三份 locale 新增 `civilizationmod.command.scan.refreshed`：

```text
en_us：Refreshed village ... observed population=%5$d
zh_tw：已重新觀測村莊 ... 目前觀測人口=%5$d
zh_cn：與 zh_tw 相同的繁體中文
```

### 回歸測試

`FoodModelRegressionTest` 新增案例，確認人口從 20 更新到 7 時，食物 42、債務 9、穩定度 87 與 shortage 事件都保持不變；provider 回傳 empty 時也不會把既有人口清除。

### 查證與驗證

- `gradlew.bat runFoodModelRegressionTest --console=plain`：`FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`；compileJava、compileTestJava、test、check、jar、assemble 與 build 均通過。
- 沒有新增版本敏感的 Minecraft/Fabric API；沿用前次已查證的 provider 與 Saved Data 方法。
- 尚未重新啟動 client 進行本次 refresh 分支的遊戲內確認。

### 下一步

重啟 client 後，先在同一座已登記且附近有村民的村莊執行：

```text
/civitas scan 128
```

預期會顯示「已重新觀測村莊」與目前觀測人口。接著執行：

```text
/civitas settlement 1
/civitas status
```

確認人口更新，但食物、債務、穩定度與事件沒有重置。若仍為人口 20，請先確認附近的村民確實在以村莊中心為中心、水平 64／垂直 32 的 AABB 內，並把 refresh 訊息與 `/civitas settlement 1` 完整輸出貼回來。

## 2026-08-19 — 修正人口 provider 只以結構代表中心查詢

### 根因確認

使用者提供的遊戲畫面顯示已知聚落 refresh 分支確實執行，但重新觀測人口仍是 20。這表示 refresh 流程與狀態保存正常，真正限制是人口 provider 原本只以 `SettlementAdapter` 保存的結構代表中心建立 AABB；玩家站在村民旁邊不代表村民位於該代表中心的水平 64／垂直 32 範圍內。結構搜尋代表座標可能漂移，因此容易漏掉玩家實際附近的村民並回傳 empty，最後使用 fallback 20。

### 變更

`SettlementPopulationProvider` 新增帶 `BlockPos observationOrigin` 的 default overload；原本兩參數方法仍保留，既有純 Java lambda 與 provider 相容。`VanillaVillagerPopulationProvider` 在 scan 提供 origin 時，以玩家執行 scan 的位置為 AABB 中心；未提供 origin 時才退回聚落保存中心。

`FoodStockProvider.initialize(...)` 與 `refreshPopulation(...)` 都新增帶 origin 的 overload，`CivilizationCommands.scan()` 將本次 scan 的玩家起點傳入新聚落初始化與已知聚落 refresh。重新 scan 仍然只更新人口，食物、債務、穩定度與事件完全保留。

### 影響檔案

- `src/main/java/com/civilizationmod/SettlementPopulationProvider.java`：新增 observation-origin overload。
- `src/main/java/com/civilizationmod/VanillaVillagerPopulationProvider.java`：以 scan 起點或保存中心建立村民查詢 AABB。
- `src/main/java/com/civilizationmod/FoodStockProvider.java`：初始化與 refresh 傳遞 origin。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：scan 傳入玩家 origin。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：記錄結構代表中心漂移與 scan origin provider 規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步技能副本。
- `Mods.md`：追加本次根因與修正。

### 查證與驗證

- 沿用已實際核對的 Minecraft 26.2 `Level.getEntities(EntityTypeTest, AABB, Predicate)`、`EntityTypeTest.forClass(Villager.class)`、`AABB` 與 `BlockPos` API，沒有猜測新的版本敏感 API。
- `gradlew.bat build --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat runFoodModelRegressionTest --console=plain`：`FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。
- 技能同步：`SKILL_COPY_MATCH`。
- 技能格式：`Skill is valid!`。
- 尚未重新啟動 client 驗證本次 origin 分支的實際村民數量。

### 下一步測試

重啟 client 後，在村民旁邊執行：

```text
/civitas scan 128
```

預期 `scan.refreshed` 的「目前觀測人口」不再因結構代表中心漂移而直接使用 20；應接近玩家 scan 起點水平 64／垂直 32 內的已載入原版村民數量。接著執行：

```text
/civitas settlement 1
/civitas status
```

確認人口更新，且食物、債務、穩定度與事件維持原值。若仍為 20，請貼上新的 `scan.refreshed` 訊息；此時要再確認村民是否真的在 scan 起點的查詢範圍內，或加入更明確的診斷數值，而不是再修改 fallback。


## 2026-08-20 — TekTopia 建築標識與倉庫架構研究

### 變更

依照使用者提供的 TekTopia-Deobfuscated GitHub repository，閱讀並整理 `VillageManager.java`、`VillageStructure.java`、`VillageStructureType.java`、`VillageStructureStorage.java`、`ModItems.java`、`Village.java` 與 `storage` 目錄的反編譯原始碼。這次只提取可借鑑的設計模式，不直接複製 TekTopia 的 Minecraft 1.12／Forge 類別、事件、NBT 或 runtime object graph。

研究確認 TekTopia 的核心流程是：玩家附近的 scan box 找到 `EntityItemFrame` → marker item 映射 `VillageStructureType` → 以 frame position 去重並綁定 village → 由 factory 建立具體結構 → 從展示框附近尋找門 → 從門內側做連通 floor scan 建立 AABB 與 floor tiles → 定期重新 validate → 倉庫結構只掃描已驗證建築邊界內的箱子。

Civitas 將採用相同的概念分層，但以 Fabric 26.2 server-side、資料驅動 registry 與 Saved Data 實作：marker item 是功能宣告，ItemFrame 是 activation／observation source，`BuildingObservation` 保存 marker 位置、維度、功能、驗證狀態、門口 anchor、邊界與 lastSeen；建築功能與資源 provider 不直接塞入 `SettlementAdapter` 的大型 runtime graph。

### 影響檔案

- `/home/ubuntu/tektopia_research.md`：保存 GitHub 來源、類別索引、核心流程與 Civitas 轉化設計。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：新增 TekTopia 建築標識、ItemFrame、結構驗證、倉庫 provider 與不複製舊版 API 的可重複工作流。
- `C:\Minecraft\skills\minecraft-civilization-fabric-262\SKILL.md`：同步全域技能研究結果到專案副本。
- `Mods.md`：追加本次研究與下一步。

### 查證與驗證

- TekTopia repository：https://github.com/TheShermanTanker/TekTopia-Deobfuscated/tree/master/net/tangotek/tektopia
- `VillageManager.java` raw source：確認 player scan boxes、ItemFrame 掃描、marker factory、village binding、frame 去重與有效 tag 更新。
- `VillageStructure.java` raw source：確認 door anchor、floor scan、AABB、ItemFrame identity 與週期性 validate。
- `VillageStructureType.java` raw source：確認 marker ItemStack 到結構 factory 的映射模式。
- `VillageStructureStorage.java` raw source：確認只在驗證後的建築 floor tiles 內收集箱子並更新 storage 集合。
- Fabric 26.2 item 官方文件：https://docs.fabricmc.net/develop/items/first-item
- Fabric 26.2 item interaction 官方文件：https://docs.fabricmc.net/develop/items/custom-item-interactions
- 全域技能與專案技能副本：`SKILL_COPY_MATCH`。
- `quick_validate.py minecraft-civilization-fabric-262`：`Skill is valid!`。
- 本次沒有修改 Minecraft Java、Gradle 或資產程式，因此未重新執行 `gradlew.bat build`；建築標識實作尚未開始。

### 未完成與風險

- 尚未建立 Civitas 的 marker item、`BuildingMarkerRegistry`、`BuildingObservation` 或倉庫結構資料模型。
- TekTopia 原始碼使用 1.12／Forge API，只能作為功能與分層參考，不能直接當作 Fabric 26.2 API 證明。
- Minecraft 26.2 ItemFrame 即時互動 callback 尚未作為本次實作依據；第一版建議先做 server 命令主動 scan，避免猜測事件名稱。
- 門、室內連通 floor、建築邊界與箱子 Container／BlockEntity 掃描仍需在實作前逐項查證 26.2 API。

### 下一步

先實作最小 `warehouse_marker` 建築標識切片：註冊自訂 marker item → 以 server 命令掃描已載入 ItemFrame → 辨識展示框中的 marker → 保存 marker position、dimension、功能與 validation 狀態 → 顯示在 `/civitas status`／`/civitas settlement`。通過後，再加入門口與 floor validation，最後才把已驗證倉庫 AABB 接入 `SettlementFoodProvider`。

## 2026-08-20 — MineColonies 參考研究與 warehouse marker 垂直切片完成

### 變更

依照使用者提供的 MineColonies repository，研究其官方首頁、Wiki、Getting Started、Requests、Warehouse、Courier's Hut、Town Hall，以及 GitHub 目前主分支的 `IColony`、`IColonyManager`、`IWareHouse`、`IRequestManager`、`Colony`、`ColonyManager`、`AbstractBuilding`、`BuildingTownHall`、`StandardRequestManager` 與 `AbstractEntityCitizen`。研究結論不是直接移植 Forge API，而是採用其責任分層：聚落 aggregate、建築 instance、storage topology、request requester/provider/resolver/state 與 worker/citizen 分離。

完成第一個 `warehouse_marker` 建築標識垂直切片。玩家可取得專用 `warehouse_marker` 物品，將其放入 ItemFrame，再以 server-side `/civitas building scan [radius]` 掃描附近已載入展示框。scanner 只辨識已註冊 marker，不將普通箱子視為倉庫，也不把 marker 自動視為倉庫內容。掃描結果保存為獨立的 `BuildingObservation`，並以維度與 marker 座標去重；若附近已有已登記聚落，則保存 settlement binding；既有觀測會保留 `first_seen` 並更新 `last_seen`、狀態與功能。

新增命令如下：

| 命令 | 用途 | Tab completion |
|---|---|---|
| `/civitas building scan` | 以預設半徑 32 掃描附近 ItemFrame marker。 | `building`、`scan` literal 可補全。 |
| `/civitas building scan <radius>` | 以 8–256 半徑掃描；只查詢已載入 entity。 | 提供 16、32、64、128 建議值。 |
| `/civitas building list` | 列出所有已保存建築觀測。 | `list` literal 可補全。 |
| `/civitas building inspect <index>` | 查詢指定一-based 建築觀測。 | 依目前 Saved Data 動態建議建築 index。 |

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationItems.java`：依 Fabric 26.2 官方 item registry 註冊單格 warehouse marker。
- `src/main/java/com/civilizationmod/BuildingFunction.java`：建立穩定的 building function enum，目前包含 `WAREHOUSE`。
- `src/main/java/com/civilizationmod/BuildingMarkerRegistry.java`：將 marker item 映射到 BuildingFunction id。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：保存 marker 維度、座標、功能、狀態、首次／最近觀測時間與聚落綁定座標。
- `src/main/java/com/civilizationmod/BuildingMarkerScanner.java`：使用已查證的 `Level.getEntities(EntityTypeTest.forClass(ItemFrame.class), AABB, Predicate)` 掃描展示框。
- `src/main/java/com/civilizationmod/BuildingScanSummary.java`：保存非持久化 scan 統計。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：schema version 升至 4，新增 optional `buildings` Codec 欄位、建築去重、建築查詢、marker scan、聚落綁定與 `setDirty()`。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：加入 building scan/list/inspect 命令與所有 suggestions。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：在 common initializer 呼叫 `CivilizationItems.initialize()`。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增 Warehouse Marker 與 building 命令英文翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增倉庫標識與 building 命令繁體中文翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依政策同步相同繁體中文翻譯。
- `src/main/resources/assets/civilizationmod/models/item/warehouse_marker.json`：建立 item/generated 模型。
- `src/main/resources/assets/civilizationmod/items/warehouse_marker.json`：建立 Minecraft 26.2 client item model 連接檔。
- `src/main/resources/assets/civilizationmod/textures/item/warehouse_marker.png`：新增 16×16 倉庫箱像素圖示。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：新增 BuildingFunction、BuildingObservation lifecycle 與 settlement binding 回歸檢查；測試保持純 Java，不在 registry 未 bootstrap 的 JavaExec 中初始化 Minecraft item registry。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/minecolonies-reference.md`：保存 MineColonies 研究、來源與 Civitas 映射規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：加入 MineColonies reference 導航與不可直接移植 Forge API 的規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能副本。
- `skills/minecraft-civilization-fabric-262/references/minecolonies-reference.md`：同步專案技能 reference。
- `Mods.md`：追加本次正式紀錄。

### API 查證與設計決策

MineColonies 的 GitHub 原始碼只用於確認功能責任與資料分層，沒有被加入 `fabric.mod.json`、Gradle dependencies 或 Java import；缺少 MineColonies 時 `civilizationmod` 仍可啟動。MineColonies Warehouse 與 IWareHouse 顯示「倉庫功能」與「實際容器 topology」必須分離，因此本次不掃描箱子內容，也沒有把箱子放進 SettlementAdapter。

Fabric 26.2 自訂物品格式依官方文件查證：使用 `ResourceKey<Item>`、`Registries.ITEM`、`Registry.register(BuiltInRegistries.ITEM, itemKey, item)`、`Item.Properties.setId(...)`；模型使用 `assets/<namespace>/models/item` 的 `minecraft:item/generated` 與 `layer0`；client item 使用 `assets/<namespace>/items` 的 `minecraft:model` 與 model identifier。ItemFrame 與 entity query 方法已透過專案 26.2 mappings／javap 及實際 compile 驗證。

### 查證與驗證

官方與研究來源：

- [MineColonies 官方首頁](https://minecolonies.com/)
- [MineColonies Wiki](https://minecolonies.com/wiki/)
- [MineColonies Getting Started](https://minecolonies.com/wiki/tutorials/getting-started/)
- [MineColonies Requests](https://minecolonies.com/wiki/systems/request/)
- [MineColonies Warehouse](https://minecolonies.com/wiki/buildings/warehouse/)
- [MineColonies Courier's Hut](https://minecolonies.com/wiki/buildings/deliveryman/)
- [MineColonies Town Hall](https://minecolonies.com/wiki/buildings/townhall/)
- [MineColonies GitHub](https://github.com/ldtteam/minecolonies)
- [Fabric Creating Your First Item 26.2](https://docs.fabricmc.net/develop/items/first-item)

| 命令 | 結果 |
|---|---|
| `C:\Minecraft\gradlew.bat runFoodModelRegressionTest --console=plain` | 最終 `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `C:\Minecraft\gradlew.bat build --console=plain` | 最終 `BUILD SUCCESSFUL`；common、client、resources、jar、test、assemble 與 build 通過。 |
| 技能副本同步 | 全域 SKILL.md 與專案副本已同步，MineColonies reference 已建立於兩處。 |

回歸測試中曾先嘗試在純 JavaExec 直接初始化 `CivilizationItems`，因 Minecraft registry 尚未 bootstrap 而失敗；已將該測試改成不需要 runtime registry 的 `BuildingFunction` contract，item registry 本身仍由完整 mod initializer 與 Gradle compile/build 驗證。這是測試邊界修正，不是遊戲內 item registry 失敗。

### 未完成與風險

本次 warehouse slice 尚未完成真正的 TekTopia／MineColonies 等級建築驗證：沒有門口 anchor、連通 floor scan、AABB validation、Container／BlockEntity inventory scan、建築 invalid／inactive refresh、箱子歸屬、Courier、request graph、building level、worker AI、GUI 或 networking。ItemFrame scan 目前只查詢命令中心附近的已載入 entity，不強制載入 chunk。

尚未把 MineColonies 作為遊戲內可選相容性測試模組，也尚未以 dedicated server 或遊戲畫面手動測試 `/civitas building scan`、`list`、`inspect`、Tab completion、三份 locale 與世界重開保存。這些項目不能以本次 build 代替，仍標記為未驗證。

### 下一步

先在遊戲內給予 `warehouse_marker`，將它放入展示框，在已 scan 的原版聚落中心附近執行 `/civitas building scan 32`、`/civitas building list`、`/civitas building inspect 1`，確認功能為倉庫、位置正確、聚落綁定狀態與重複 scan 去重。之後才實作可查證的門口／floor validation，再建立獨立 `BuildingStorageProvider`，最後才設計受 MineColonies request／Warehouse／Courier 概念啟發的低頻物流與資源請求層。

## 2026-08-20 — warehouse marker 第一版建築幾何驗證

### 變更

完成 warehouse marker 垂直切片的下一階段：建立 server-side `BuildingGeometryValidator`，將 ItemFrame marker 觀測從單純 `detected` 升級為保守的 `valid`／`invalid` 幾何驗證。驗證器由 `/civitas building scan [radius]` 主動呼叫，不加入未查證的 ItemFrame 互動事件，也不強制載入區塊。

第一版規則如下：在 marker 周圍水平 4、垂直 3 的已載入範圍內尋找最近 `DoorBlock`；若找到門的 upper half，先正規化到 lower half；依門的 `FACING` 掃描 facing side 與 opposite side；在 depth 3、左右各 1、height 3 的有限房間樣本中統計空氣、地板支撐與天花板支撐。至少需要 8 個室內空氣方塊、2 個地板支撐方塊與 1 個天花板支撐方塊才判定為 `valid`，否則保存為 `invalid` 與受控原因。

`BuildingObservation` 現在將 settlement binding 與 geometry validation 分開保存：`status` 保留 `bound`／`unbound`；新增 optional `validation_status` 與 `validation_reason`，舊世界沒有欄位時預設為 `detected`／`unknown`。首次觀測時間仍然保留，重新 scan 只更新最近觀測、功能、綁定與驗證結果，不刪除失效歷史記錄。`CivilizationWorldData` schema version 提升為 5。

`building scan` 回覆新增 radius、detected、updated、bound、valid、invalid 統計；`building list` 與 `building inspect` 新增 binding status、validation status 與本地化驗證原因。三份 locale 均已同步，`zh_cn` 依專案政策維持繁體中文。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：新增有限範圍 DoorBlock／房間幾何驗證器與純 Java decision seam。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：新增 `validationStatus`、`validationReason`、optional Codec 欄位、狀態常數與 backward-compatible constructor／refresh overload。
- `src/main/java/com/civilizationmod/BuildingScanSummary.java`：新增 valid／invalid 計數並保留三參數相容 constructor。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：schema 5；building scan 接入 geometry validator、保存 validation 結果並統計 valid／invalid。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：擴充 scan、list、inspect 的 geometry 狀態與原因輸出。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增英文 geometry 狀態與原因翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文 geometry 狀態與原因翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文 geometry 翻譯。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：新增 valid、no_door、no_room、unloaded 幾何規則純 Java 回歸檢查。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：保存 26.2 API 查證、驗證門檻與資料邊界。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：加入 building geometry reference 導航。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步專案技能 reference。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能修改。
- `Mods.md`：追加本次建築幾何驗證紀錄。

### 26.2 API 查證

本次沒有猜測門或方塊 API。先閱讀 Fabric 官方文件，再使用專案實際 Loom 26.2 common jar 執行 javap：`C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`。

已確認 `DoorBlock.FACING`、`DoorBlock.HALF`、`DoubleBlockHalf.UPPER／LOWER`、`StateHolder.getValue(...)`、`Direction.getOpposite／getClockWise`、`BlockPos.offset／relative／betweenClosed`、`BlockStateBase.isAir／blocksMotion`、`Level.getBlockState` 與 `Level.isLoaded` 存在於實際 26.2 jar。容器仍未接入；Fabric 官方 Block Entities／Containers 文件支持將 container 觀測延後到幾何驗證完成後的獨立 provider。

### 查證與驗證

| 命令／項目 | 結果 |
|---|---|
| `C:\Minecraft\gradlew.bat runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `C:\Minecraft\gradlew.bat build --console=plain` | `BUILD SUCCESSFUL`；compileJava、compileClientJava、resources、jar、test、assemble、check 與 build 通過。 |
| 實際 26.2 common jar javap | DoorBlock、DoubleBlockHalf、Direction、BlockPos、BlockStateBase、Level API 已核對。 |
| 全域／專案技能同步 | `SKILL_GEOMETRY_COPY_MATCH`。 |
| geometry validator 純 Java decision seam | 已由回歸測試覆蓋 valid、no_door、no_room、unloaded。 |

`compileJava` 有一項 deprecated API note，但不影響 build 或測試成功；後續若 26.2 mappings 提供更合適的非 deprecated solid／support accessor，再單獨處理，不在本切片擴大修改範圍。

### 官方來源

- [Fabric Block States 26.2](https://docs.fabricmc.net/develop/blocks/blockstates)，查證日期 2026-08-20。
- [Fabric Block Entities 26.2](https://docs.fabricmc.net/develop/blocks/block-entities)，查證日期 2026-08-20。
- [Fabric Block Containers 26.2](https://docs.fabricmc.net/develop/blocks/block-containers)，查證日期 2026-08-20。
- [Fabric Debugging Mods 26.2](https://docs.fabricmc.net/develop/debugging)，查證日期 2026-08-20。
- [Fabric Block Tinting 26.2](https://docs.fabricmc.net/develop/blocks/block-tinting)，查證日期 2026-08-20。
- [Fabric Porting to 26.2](https://docs.fabricmc.net/develop/porting/)，查證日期 2026-08-20。

### 未完成與風險

第一版幾何驗證是保守有限取樣，不等同 TekTopia 的完整連通 floor flood fill，也尚未保存 door anchor、AABB、floor tiles 或容器清單。門方向、室內／室外判斷目前以兩側 bounded sample 的空氣與支撐分數選擇，可能在不規則房屋、無天花板建築、marker 遠離門或雙門結構中判定 invalid；這些是可觀測且不會破壞 Saved Data 的第一版限制。

尚未在遊戲內驗證不同方向門、marker 在門內／門外、無效房屋、未載入邊界、三份 locale、Tab completion、重複 scan 與世界重開後 validation 欄位保存。也尚未實作 `BuildingStorageProvider`、Container／BlockEntity inventory scan 或將倉庫內容接入 `SettlementFoodProvider`。

### 下一步

先在遊戲內執行：

```text
/give @s civilizationmod:warehouse_marker
/civitas building scan 32
/civitas building list
/civitas building inspect 1
```

準備至少兩個測試場景：一個 marker 靠近有門、地板與天花板的房屋，預期為 `valid`；一個沒有門或室內空間不足的 marker，預期為 `invalid`。確認重複 scan 不增加建築數量、`first_seen` 不重置、validation 結果會更新並在世界重開後保存。遊戲內通過後，下一個切片才是保存 door anchor／AABB，然後建立只掃描已驗證建築範圍的 `BuildingStorageProvider`。

## 2026-08-20 — 依 latest.log 修正木屋地板支撐誤判

### 問題與根因

使用者提供的遊戲截圖顯示 warehouse marker 已放在木屋入口附近的 ItemFrame；`latest.log` 進一步確認模組初始化、物品註冊與 ItemFrame 掃描都正常。第一次 `/civitas building scan 32` 時展示框尚未位於掃描範圍內，之後成功回報 `detected=1`、`updated=1`，但 `validation=invalid`、`reason=地板支撐不足`。

因此根因不是 marker registry、ItemFrame entity query 或 Saved Data，而是第一版幾何驗證器在兩側 bounded sample 中選擇了不適合的門側，且使用 `blocksMotion()` 判斷地板支撐過於保守。畫面與 log 沒有顯示 Java crash、模組初始化失敗或資源載入失敗。

### 變更

`BuildingGeometryValidator` 的地板與天花板支撐判定改為 `!state.isAir() && state.isSolid()`。兩側取樣都完成時，現在先選擇達到最低地板支撐門檻的一側，再以空氣、地板與天花板加權分數選擇；不再只以空氣與天花板分數選擇可能是室外的一側。

本次保留既有門搜尋半徑、房間取樣大小、valid／invalid 門檻與 Saved Data 格式，避免把一個遊戲內誤判擴大成新資料遷移或結構演算法重寫。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：改用 `BlockState.isSolid()`，優先選擇有地板支撐的門側。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：記錄 latest.log 根因與新的支撐判定規則。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步專案技能 reference。
- `Mods.md`：追加本次 log-driven bug fix。

### API 查證

`BlockStateBase.isSolid()` 已由實際 Minecraft 26.2 common jar javap 確認；本次仍只使用已查證的 `Level.getBlockState`、`BlockPos` 與 `DoorBlock` API。Fabric 官方 Block States 文件說明 `BlockState` 保存單一方塊 properties；Fabric 官方 Porting 文件要求版本敏感 API 以 26.2 mappings、文件與實際編譯確認。[1] [2]

### 查證與驗證

| 項目 | 結果 |
|---|---|
| 附件 `latest.log` | 確認 marker 後續成功偵測 1 個；原判定為 invalid／floor support insufficient。 |
| `C:\Minecraft\gradlew.bat runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `C:\Minecraft\gradlew.bat build --console=plain` | `BUILD SUCCESSFUL`。 |
| geometry reference 同步 | `GEOMETRY_REFERENCE_COPY_MATCH`。 |
| Mojang 401／Realms 錯誤 | 判定為開發環境帳號／Realms 認證問題，與 `civilizationmod` marker 驗證無關。 |

### 未完成與風險

目前尚未取得修正後的新遊戲內 scan log，因此不能宣稱木屋已經通過 `valid`。仍需測試 marker 靠近門、marker 在門內／門外、不同門方向、無天花板房屋、室外展示框、未載入邊界，以及重複 scan 與世界重開保存。若木屋仍顯示 invalid，下一步應加入診斷數值（選定門座標、兩側 air／floor／ceiling 計數），不要直接猜測再放寬門檻。

### 下一步

重新建置並啟動 `C:\Minecraft` client 後，在同一個木屋執行：

```text
/civitas building scan 32
/civitas building inspect 1
```

預期至少不再因錯誤選側而顯示地板支撐不足；若仍無效，請再次附上新的 `latest.log`，並保留 `/civitas building inspect 1` 的完整輸出。

### 來源

- [1] https://docs.fabricmc.net/develop/blocks/blockstates — Fabric Block States 26.2，查證日期 2026-08-20。
- [2] https://docs.fabricmc.net/develop/porting/ — Fabric Porting to 26.2，查證日期 2026-08-20。

## 2026-08-20 — latest.log 重現無效結果與幾何診斷加強

### latest.log 重現結果

使用者提供的修正後 `latest.log` 顯示模組初始化與 ItemFrame marker 掃描仍正常：連續三次 building scan 都偵測到 1 個 marker，第一次 `updated=1`，後兩次 `updated=0`，代表既有 BuildingObservation 去重與保存流程正常。然而三次結果仍為 `valid=0`、`invalid=1`；本次 log 沒有包含 `building inspect 1`，因此玩家訊息仍只顯示 aggregate 統計，沒有新的 failure reason 或實際 air／floor／ceiling 計數。

### 變更

在前一版 `isSolid()` 修正之上，地板與天花板支撐現在使用已查證的 `BlockState.isFaceSturdy(...)`：地板傳入 `Direction.UP`，天花板傳入 `Direction.DOWN`，並保留 `isSolid()` 作為 fallback。這比單純的 solid 判定更接近「某一面能否支撐結構」的語意。

`CivilizationWorldData.scanBuildingMarkers(...)` 在 geometry validation 失敗時加入 server-side logger 診斷，輸出 dimension、marker position、受控 reason，以及 `air`、`floor`、`ceiling` 計數。這些是 console diagnostics，不是玩家可見文字，因此不需要經過 `CivilizationMessages`。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：使用 `isFaceSturdy(level, position, Direction.UP／DOWN)`，保留 `isSolid()` fallback。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：invalid geometry scan 記錄 air／floor／ceiling 數值診斷。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：加入 `isFaceSturdy` API 與診斷規則。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步專案 reference。
- `Mods.md`：追加本次 latest.log 重現與診斷修正。

### API 查證與驗證

`BlockStateBase.isFaceSturdy(BlockGetter, BlockPos, Direction)` 已存在於本專案實際 Minecraft 26.2 common jar 的 javap 結果；本次未猜測新版本 API。Fabric 官方 Block States 與 Porting 文件仍是本次設計依據。[1] [2]

| 命令／項目 | 結果 |
|---|---|
| 使用者提供的 latest.log | 確認 1 個 marker 持續被偵測，仍為 invalid；沒有新的 inspect reason。 |
| `C:\Minecraft\gradlew.bat runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `C:\Minecraft\gradlew.bat build --console=plain` | `BUILD SUCCESSFUL`。 |
| geometry reference 同步 | `GEOMETRY_REFERENCE_COPY_MATCH`。 |

### 未完成與下一步

目前仍不能宣稱木屋已通過 `valid`，因為 `isFaceSturdy` 修正後尚未有新的遊戲內 log。下一次請在重建置後執行：

```text
/civitas building scan 32
/civitas building inspect 1
```

並重新附上 `latest.log`。這次 server log 應包含 `Building geometry invalid: ... air=..., floor=..., ceiling=...`；若仍無效，便可根據實際計數判斷是門位置、marker 與門的距離、房屋地板高度，還是取樣方向問題，而不是繼續盲目放寬門檻。

### 來源

- [1] https://docs.fabricmc.net/develop/blocks/blockstates — Fabric Block States 26.2，查證日期 2026-08-20。
- [2] https://docs.fabricmc.net/develop/porting/ — Fabric Porting to 26.2，查證日期 2026-08-20。

## 2026-08-20 — 書本右鍵 ItemFrame 建築除錯互動

### 需求與設計

依照使用者需求，新增一個不修改世界狀態的遊戲內除錯入口：玩家手持普通 `minecraft:book` 對著放有已註冊建築 marker 的 ItemFrame 右鍵時，server 會重新執行同一套 `BuildingGeometryValidator`，並透過品牌化本地化訊息回報功能、marker 座標、validation status、validation reason、air、floor 與 ceiling 計數。

這個互動是診斷用途，不會消耗書本、不會更新 `BuildingObservation`、不會呼叫 `setDirty()`，也不會改變 ItemFrame rotation。非主手、非普通書本、非 ItemFrame、未註冊 marker、client side 或 spectator 玩家都回傳 `InteractionResult.PASS`，讓原版與其他模組互動保持可用；符合條件的 server-side debug interaction 回傳 `SUCCESS`，避免原版 ItemFrame 繼續處理 rotation。

### 變更

新增 `BuildingDebugInteraction`，在 common initializer 使用 `UseEntityCallback.EVENT.register(...)` 註冊。handler 只對 `BuildingMarkerRegistry` 可辨識的 marker 生效，目前可診斷 `warehouse_marker`。玩家訊息使用 `CivilizationMessages.translatable(...)`，所有內容同步加入 `en_us`、`zh_tw` 與依專案政策使用繁體中文的 `zh_cn`。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingDebugInteraction.java`：新增 server-side 普通書本右鍵 ItemFrame 診斷 handler。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：註冊 `BuildingDebugInteraction`。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增英文 debug result 翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文 debug result 翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文 debug result 翻譯。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：加入 UseEntityCallback、InteractionResult 與書本除錯邊界規則。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步專案技能 reference。
- `Mods.md`：追加本次除錯互動紀錄。

### 26.2 API 查證

Fabric API 26.2 `UseEntityCallback` Javadoc 確認 callback signature 為 `interact(Player, Level, InteractionHand, Entity, EntityHitResult)`；官方說明該 callback 在 spectator check 前觸發，因此 handler 明確自行檢查 `player.isSpectator()`。logical client 回傳 `PASS`，logical server 對已處理的 debug interaction 回傳非 `PASS` 以取消後續處理。[1]

Fabric 官方 Events 文件確認 callback 使用 `EVENT.register(...)`；Custom Item Interactions 文件確認互動結果使用 `InteractionResult`，且應優先使用現有事件／物品互動 hook，而不是直接加入 Mixin。[2] [3] 實際 Minecraft 26.2 common jar 的 javap 也確認 `Player.sendSystemMessage(Component)` 存在。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| `UseEntityCallback` 26.2 Javadoc | 已確認右鍵實體 callback、signature 與 client/server InteractionResult 語意。 |
| `Player` 26.2 javap | 已確認 `sendSystemMessage(Component)`。 |
| `C:\Minecraft\gradlew.bat runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `C:\Minecraft\gradlew.bat build --console=plain` | `BUILD SUCCESSFUL`。 |
| skill validation | `Skill is valid!`。 |
| geometry debug reference 同步 | `GEOMETRY_DEBUG_REFERENCE_COPY_MATCH`。 |

### 未完成與遊戲內測試

尚未在遊戲內實測書本右鍵事件。請使用最新 build 啟動 client，將普通書本放在主手，對著 warehouse marker 所在的 ItemFrame 右鍵。預期聊天欄會顯示類似：

```text
|創世紀元| : 建築除錯：功能=倉庫，標識位置=...，驗證=無效，原因=...，空氣=...，地板=...，天花板=...
```

測試時應確認：普通書本右鍵 marker 會顯示診斷；普通書本右鍵沒有 marker 的 ItemFrame 不會攔截原版互動；空手、其他物品、非主手與非 ItemFrame 不會顯示診斷；重複右鍵不會新增建築、不會改變 `first_seen`、不會消耗書本；英文與兩個中文 locale 的品牌前綴與正文都正確。

### 來源

- [1] https://maven.fabricmc.net/docs/fabric-api-0.154.2+26.2/net/fabricmc/fabric/api/event/player/UseEntityCallback.html — Fabric API 0.154.2+26.2 UseEntityCallback Javadoc，查證日期 2026-08-20。
- [2] https://docs.fabricmc.net/develop/events — Fabric Events 26.2，查證日期 2026-08-20。
- [3] https://docs.fabricmc.net/develop/items/custom-item-interactions — Fabric Custom Item Interactions 26.2，查證日期 2026-08-20。

## 2026-08-20 — 修正 geometry invalid 診斷計數被歸零

### 根因確認

依使用者提供的 Claude 分析與前一份 `latest.log`，`BuildingGeometryValidator` 回報 `reason=no_ceiling, air=0, floor=0, ceiling=0`。進一步檢查程式後確認，這不是取樣迴圈沒有執行，也還不能證明門方向或 `isFaceSturdy` 錯誤；直接根因是 `evaluate(...)` 在 no_room、no_floor、no_ceiling 與 unloaded 失敗分支呼叫 `invalid(String reason)`，而該 helper 將三個診斷欄位全部固定成零。因此錯誤訊息掩蓋了實際取樣計數。

### 變更

`BuildingGeometryValidator.evaluate(...)` 現在在每個可取得取樣結果的 invalid 分支保留當時的 `interiorAirBlocks`、`floorSupportBlocks` 與 `ceilingBlocks`；只有 no_context 與 no_door 這類尚未有取樣結果的早期失敗維持零值。這是診斷資料流程修正，不調整 `isSolid()`、`isFaceSturdy()`、門搜尋半徑、房間範圍或有效門檻。

### 回歸測試

`FoodModelRegressionTest` 新增 no_room、no_floor、no_ceiling 的診斷計數檢查，確保失敗 reason 與三項輸入計數一致，避免未來再次把所有 invalid 結果壓成零。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：保留 invalid validation 的實際取樣計數。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：新增 invalid metrics regression checks。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：加入 zero metrics 根因與失敗計數保留規則。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步專案技能 reference。
- `Mods.md`：追加本次診斷修正紀錄。

### 驗證

| 命令／項目 | 結果 |
|---|---|
| `C:\Minecraft\gradlew.bat runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `C:\Minecraft\gradlew.bat build --console=plain` | `BUILD SUCCESSFUL`。 |
| geometry reference 同步 | `GEOMETRY_REFERENCE_COPY_MATCH`。 |
| skill validation | `Skill is valid!`。 |

### 尚待遊戲內確認

目前尚未取得這次修正後的遊戲內 log。重新啟動最新 build 後，使用普通書本右鍵 warehouse marker，或執行 `/civitas building scan 32`，新的 invalid 診斷應該會顯示保留下來的實際 `air`、`floor`、`ceiling` 計數。只有取得這些非零或真實零值後，才繼續判斷門兩側取樣框是否需要修正；本次不再盲目更改幾何門檻。

## 2026-08-20 — 擴充書本右鍵詳細 geometry diagnostics

### 需求與設計

為定位木屋 `ceiling=0` 的實際原因，書本右鍵 ItemFrame 除錯現在除了 validation result 與 air／floor／ceiling 統計，還會顯示最近門 lower half 座標、`DoorBlock.FACING`、選用的 facing／opposite 側，以及兩側各自的 sample 完整度、實際／預期 sample 數、air、floor、ceiling 與 sample start/end bounds。

詳細診斷保持 transient，不擴大 `BuildingObservation` 或 `CivilizationWorldData` schema，不寫入 Saved Data，也不呼叫 `setDirty()`。既有 `BuildingGeometryValidator.validate(...)` API 保持不變，內部委派 `diagnose(...).result()`；既有 scan、Saved Data 與 building list／inspect 的持久化行為不變。

### 變更

`BuildingGeometryValidator` 新增 `diagnose(ServerLevel, BlockPos)`、`GeometryDiagnostic` 與 `SideDiagnostics`。兩側取樣現在把 side label、Direction、sample bounds、total／expected samples 與各項計數保留下來；debug handler 使用相同取樣計算輸出門、方向、選側與兩側資料。sample bounds 與實際 scan loop 使用同一組 door position、side、depth、lateral width 與 room height 計算，避免診斷範圍和實際掃描範圍不一致。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：新增 transient geometry diagnostics model 與 `diagnose(...)` 入口；保留 `validate(...)` 相容入口。
- `src/main/java/com/civilizationmod/BuildingDebugInteraction.java`：書本除錯顯示門座標、方向、選側、兩側 sample 計數與 bounds。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增 geometry 與 side debug 翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文 geometry 與 side debug 翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文翻譯。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：加入詳細 diagnostics 資料模型與 bounds 規則。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步專案技能 reference。
- `Mods.md`：追加本次詳細 diagnostics 紀錄。

### 驗證

| 命令／項目 | 結果 |
|---|---|
| `C:\Minecraft\gradlew.bat runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `C:\Minecraft\gradlew.bat build --console=plain` | `BUILD SUCCESSFUL`。 |
| geometry diagnostics reference 同步 | `GEOMETRY_DIAGNOSTICS_REFERENCE_COPY_MATCH`。 |
| skill validation | `Skill is valid!`。 |

### 尚待遊戲內測試

重新啟動最新 build，手持普通書本對 warehouse marker ItemFrame 右鍵。預期會收到四行品牌化訊息：原本的功能／驗證摘要、Geometry 門與選側摘要、facing side 詳情，以及 opposite side 詳情。請特別記錄 `door`、`facing`、`selected_side`、兩側 bounds 與 ceiling 計數，再決定屋頂高度、門方向或取樣框是否需要進入下一個正式幾何修正；本次仍不放寬「需要天花板」的有效條件。


## 2026-08-20 — TekTopia 房屋結構判定查證與 reference 同步

### 變更

針對目前 warehouse marker 的 `ceiling=0` 問題，查閱 TekTopia 反編譯原始碼與 Wiki，確認它不是以固定 `doorY + ROOM_HEIGHT` 矩形取樣判定房屋。TekTopia 由 ItemFrame 朝向推算門的少數候選位置，從門內側一格開始做四方向 flood fill；每個 floor tile 動態向上尋找第一個不可通行方塊，要求至少兩格可通行高度，並以最多 500 個 floor tiles、房高搜尋最多 30 格作為界線。其 `BasePathingNode.isPassable()` 以液體與原版方塊 passability 為核心，並將木門／fence gate 視為 portal，不以 `isFaceSturdy` 作為 ceiling 的唯一判定。

確認 TekTopia 的基本有效條件包含至少 4 個 floor tiles、至少 2 格高度、屋頂、至少一扇門、單一 y-level floor，以及最多 500 個 floor spaces；Storage marker 啟用後，箱子只在有效 floor scan 範圍內作為後續 storage 資源觀測，不等同 marker 幾何判定。

### 影響檔案

- `skills/minecraft-civilization-fabric-262/references/tektopia-structure-detection.md`：新增 TekTopia 原始碼、Wiki、判定流程及 Civitas 轉化限制。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：加入 TekTopia reference 導航與動態 flood-fill 查證摘要。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/tektopia-structure-detection.md`：同步全域技能 reference。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：加入相同 reference 導航。
- `tektopia-house-detection-findings.md`：保存完整查證紀錄與來源，供本專案研究追蹤。
- `Mods.md`：追加本次研究紀錄。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| TekTopia 原始碼 | 已讀取 `VillageManager.java`、`VillageStructure.java`、`VillageStructureType.java`、`VillageStructureStorage.java` 與 `BasePathingNode.java`。 |
| TekTopia Wiki | 已交叉讀取 Structures 與 Getting Started 頁面。 |
| 技能驗證 | `python /home/ubuntu/skills/skill-creator/scripts/quick_validate.py minecraft-civilization-fabric-262`：`Skill is valid!`。 |
| Fabric 26.2 程式碼 build | 本次為研究與文件同步，未修改 Java；未重新執行 `gradlew.bat build`。 |

### 未完成與風險

TekTopia 程式碼是 Minecraft 1.12.2 的舊版 Forge/MCP 反編譯碼，只能作為結構判定演算法參考，不能直接證明 Fabric 26.2 的 API。Civitas 目前仍使用固定矩形取樣與 `isFaceSturdy`／`isSolid` 保守支撐判定；本次尚未把 validator 改成 flood fill，也尚未查證 26.2 中與 TekTopia `Block.isPassable` 等價的正式 API。`ceilingNonAirBlocks` 的 Java／locale 修改仍需完成並重新 build。

### 下一步

先完成目前中斷的 `ceilingNonAirBlocks` diagnostics 資料流與三份 locale，再執行 `runFoodModelRegressionTest` 和 `build`。取得遊戲內新診斷後，若 ceiling non-air 為 0，設計 bounded door-inside flood fill 與動態 room-height scan；若 non-air 大於 0 但 sturdy ceiling 為 0，另行決定是否採用已查證的 26.2 passability／collision 判定替代或補充 sturdy 判定。

### 來源

- [1] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/VillageManager.java
- [2] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java
- [3] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureType.java
- [4] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureStorage.java
- [5] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/pathing/BasePathingNode.java
- [6] https://sites.google.com/view/tektopia/home/structures
- [7] https://sites.google.com/view/tektopia/home/getting-started


## 2026-08-20 — 完成 ceilingNonAirBlocks geometry diagnostics

### 變更

完成前次中斷的 `ceilingNonAirBlocks` diagnostics 資料流。`SideScan` 現在在每一個 ceiling 取樣位置統計非空氣方塊數，並把數值傳遞到 `SideDiagnostics`；書本右鍵 ItemFrame 除錯訊息會同步顯示 `ceiling_non_air`。此欄位只用於 transient diagnostics，不寫入 `BuildingObservation` 或 `CivilizationWorldData`，也不改變既有 validation 門檻。

三份 locale 已同步更新 side 訊息的 placeholder：英文顯示 `ceiling_non_air`，`zh_tw` 與 `zh_cn` 顯示「天花板非空氣」。geometry reference 也補上欄位的語意，用來區分固定 ceiling 位置是空氣，或位置有方塊但未通過 sturdy／solid 支撐判定。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：完成 `SideScan`、`SideDiagnostics`、`empty()`、`toDiagnostics()` 與完整掃描建構子的 ceilingNonAirBlocks 傳遞。
- `src/main/java/com/civilizationmod/BuildingDebugInteraction.java`：side diagnostics 傳入 `side.ceilingNonAirBlocks()`。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：加入 `ceiling_non_air=%8$d`，bounds 移至 `%9$s`／`%10$s`。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：加入「天花板非空氣=%8$d」，同步調整 bounds placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步繁體中文 placeholder。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：補充 ceilingNonAirBlocks diagnostics 語意。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步專案 reference。
- `Mods.md`：追加本次完成與驗證紀錄。

### 查證與驗證

| 命令／項目 | 結果 |
|---|---|
| `cmd /c "cd /d C:\\Minecraft && gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain"` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL in 12 sec`。 |
| `cmd /c "cd /d C:\\Minecraft && gradlew.bat --no-daemon build --console=plain"` | `BUILD SUCCESSFUL in 8 sec`；compileJava、compileClientJava、resources、jar、sourcesJar、test、check 與 build 通過。 |
| Java 編譯警告 | `BuildingGeometryValidator.java` 使用或覆寫 deprecated API 的 note；不是 compile failure，build 已成功。 |
| 版本 | Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Fabric Loom `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。 |

### 未完成與風險

本次只完成固定矩形取樣的診斷欄位，尚未把 validator 改成 TekTopia-style bounded flood fill，也尚未使用遊戲內新訊息取得 `ceiling_non_air` 實際數值。`ValidationResult` 仍只保存既有 air／floor／ceiling 三個摘要欄位，這是刻意維持既有 API 的相容設計。deprecated API note 目前不阻擋 build，但若後續修改幾何邏輯，應一併確認 26.2 mappings 的替代方法。

### 下一步

重新啟動最新 client，手持普通書本對 warehouse marker ItemFrame 右鍵，記錄 facing／opposite 兩側的 `samples`、`air`、`floor`、`ceiling`、`ceiling_non_air` 與 bounds。若兩側 `ceiling_non_air=0`，進入 bounded door-inside flood fill 與動態 room-height 設計；若 `ceiling_non_air>0` 但 `ceiling=0`，再查證 26.2 passability／collision 等價 API，決定是否補充或替代 sturdy 判定。


## 2026-08-20 — Claude 房屋結構分析結構化筆記

### 變更

將使用者提供的 Claude TekTopia 房屋結構分析整理成結構化 Markdown 筆記，並與 TekTopia 反編譯原始碼、Civitas 現有 `BuildingGeometryValidator` 與最新 `latest.log` 交叉對照。筆記將建議分為「可直接採用」、「可作架構方向但需查證」與「不可直接移植」三類。

整理後確認可採用的核心方向是：以門內側為起點的 bounded flood fill、queue／visited set、逐 floor tile 動態房高、明確的 floor／房高／訪問數上限、transient diagnostics，以及保持 marker 功能、建築幾何與 storage provider 的責任分離。不可直接採用的部分包括舊版 TekTopia 的 Forge/MCP API、`Block.isPassable` 等未經 26.2 查證的簽名、Claude 根據舊版全零 log 推導出的「目前取樣一定完全落在屋外」結論，以及未經資料驗證的 8–10 格固定半徑數值。

### 影響檔案

- `Claude分析-房屋結構與Civitas應用筆記.md`：新增結構化分析筆記、最新 log 對照、應用可行性分級、實作順序與來源。
- `Mods.md`：追加本次文件整理紀錄。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| Claude 分析原文 | 以先前保存的 `pasted_content_2.txt` 交叉整理；Claude share 動態頁面本次未提供可擷取正文。 |
| 最新 log | 確認 `samples=27/27`、`air=27`、`floor=9`、`ceiling=0`、`ceiling_non_air=0`，固定 ceiling 位置是空氣的證據比「掃描完全未執行」更直接。 |
| TekTopia 原始碼 | 沿用已查證的 `VillageManager`、`VillageStructure`、`BasePathingNode` 與 `VillageStructureStorage` 來源。 |
| 程式碼修改 | 本次未修改 Java、Gradle、locale 或 Saved Data；未重新執行 build。 |

### 未完成與風險

筆記中的 flood fill、dynamic room height 與 passability 只確立為下一階段設計方向，尚未實作。TekTopia 是舊版 Minecraft 1.12.2 Forge/MCP 反編譯碼，不能直接作為 Fabric 26.2 API 證明。26.2 等價的 passability／collision API 仍需另行以官方文件、mappings、`javap` 或 compile 查證。

### 下一步

若正式採用 Claude 分析，先建立 bounded door-inside flood fill 的最小 common/server 實作與純決策測試；保留既有 `validate(level, marker)` 和 `ValidationResult` 相容入口，不先改 Saved Data schema。完成後再以規則木屋、L 形木屋、屋頂高於 3 格、無屋頂、不同門方向與未載入邊界進行遊戲內驗證。

### 來源

- [1] https://claude.ai/share/a5942760-109d-4bdc-8c26-687f45fe105e
- [2] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java
- [3] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/pathing/BasePathingNode.java
- [4] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureStorage.java


## 2026-08-20 — 建築幾何改為 bounded door-side flood fill

### 變更

針對 warehouse 建築持續出現「天花板支撐不足」的問題，根據最新診斷 `facing/opposite` 兩側皆為 `air=27`、`floor=9`、`ceiling=0`、`ceiling_non_air=0` 的結果，停止微調固定 `ROOM_HEIGHT=3`。`BuildingGeometryValidator` 已由固定兩個 3×3×3 矩形改為 bounded door-side flood fill：每側從門旁一格開始，以 queue／visited set 向四個水平方向擴張；每個 floor candidate 逐格向上搜尋第一個非 passable 方塊，不再假設 ceiling 位於 `doorY + 3`。

新掃描保留明確安全界線：水平半徑 `8`、最多 `256` 個 floor tiles、每格最高向上搜尋 `30` 格、至少 `2` 格 room clearance。所有 floor、candidate 與高度 sample 都必須已載入；不強制載入 chunk。超過 floor 上限時回傳受控 `scan_limit` reason，不讓 scan 無限擴張。

房間 passability 依已查證的 Minecraft 26.2 common API 判定：方塊不是 liquid，且 `BlockState.getCollisionShape(level, position).isEmpty()`。這是以 26.2 的 collision shape API 取代直接猜測或移植 TekTopia 舊版 `Block.isPassable`；地板支撐仍透過 collision shape 非空判定，保留 server-side common 邊界。

Transient diagnostics 已擴充：side 現在顯示 complete／scan limit 狀態、實際 floor candidate 數、air、floor、ceiling、ceiling non-air、最小／最大 room height 與動態 bounds。`BuildingObservation` 與 `CivilizationWorldData` 的 Saved Data schema 沒有加入 floor graph 或 room geometry；持久化仍只保存 coarse validation status／reason。三份 locale 已加入 `scan_limit` 與新 placeholder。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：以 bounded flood fill、dynamic room-height scan 與 collision-shape passability 取代固定 ceiling box。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：加入受控 `VALIDATION_REASON_SCAN_LIMIT` 常數。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：加入 `scan_limit` 的玩家可見 reason mapping。
- `src/main/java/com/civilizationmod/BuildingDebugInteraction.java`：顯示 scan status、floor candidate 數與 room height range。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：同步新 geometry 與 scan-limit 翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：同步繁體中文新 geometry 與 scan-limit 翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依專案政策同步繁體中文。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：更新 bounded flood fill 規則與 26.2 collision shape API。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步全域 reference。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：更新 geometry reference 導航。
- `/home/ubuntu/skills/minecraft-civilization-fabric-26.2/SKILL.md`：同步全域技能導航。
- `Mods.md`：追加本次修改紀錄。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| 26.2 `BlockStateBase` API | 以 `javap` 查證 `liquid()`、`getCollisionShape(BlockGetter, BlockPos)`、`isSolid()`、`isFaceSturdy(...)` 等方法。 |
| 26.2 `VoxelShape` API | 以 `javap` 查證 `VoxelShape.isEmpty()`。 |
| Fabric 26.2 官方文件 | 讀取 Block States 與 Block Model 26.2 文件，確認 collision shape 是方塊幾何與碰撞的正式 API 方向。[1] [2] |
| `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain` | 通過；`BUILD SUCCESSFUL in 12 sec`。 |
| `gradlew.bat --no-daemon build --console=plain` | 通過；`BUILD SUCCESSFUL in 10 sec`，compileJava、compileClientJava、jar、sourcesJar、test、check 與 build 均完成。 |
| 版本 | Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Fabric Loom `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。 |

### 未完成與風險

本次已完成程式編譯與純 Java 回歸測試，但尚未在遊戲內使用新 jar 重新對同一棟木屋執行書本右鍵診斷，因此尚未證明該木屋會在新演算法下變成 `valid`。目前兩側仍都會掃描，選側策略仍沿用 bounded scan 的完整度、scan limit、floor、air、ceiling 分數；若房屋外側存在大面積有地板空間，後續可能需要使用 ItemFrame 掛載方向作為主要 door-inside side，而不是永遠同時評估兩側。

TekTopia 的舊版 Forge/MCP `Block.isPassable`、`rotateY` 與其他舊 API 沒有直接移植；本次採用的 collision shape 判定雖已由 26.2 common jar 查證方法存在，但「所有特殊方塊的房屋語意」仍需遊戲內回歸測試。編譯仍可能顯示 `BuildingGeometryValidator.java uses or overrides a deprecated API` note；目前不是 failure。

### 下一步

重新啟動最新 client，手持普通書本對同一個 warehouse marker ItemFrame 右鍵，記錄新的 `floors`、`air`、`floor`、`ceiling`、`ceiling_non_air`、`room_height`、`complete` 與 bounds。預期若屋頂位於 y=78 或更高，新的 diagnostics 應能顯示 `room_height` 至少為 4 且 ceiling 大於 0；若仍為 invalid，依 reason 分別處理 `no_room`、`no_floor`、`no_ceiling`、`unloaded` 或 `scan_limit`，不再直接猜測固定 ceiling 高度。

### 來源

- [1] https://docs.fabricmc.net/develop/data-generation/block-models — Fabric Block Model Generation 26.2，包含 getCollisionShape／block support shape 方向。
- [2] https://docs.fabricmc.net/develop/blocks/blockstates — Fabric Block States 26.2。
- [3] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java — TekTopia flood-fill 與動態房高演算法參考。


## 2026-08-20 — 地板高度改為動態 floor column 搜尋

### 變更

確認前一版 bounded flood fill 雖已移除固定 ceiling Y，但仍以 `roomAnchor.below()` 假設地板一定位於 `doorY - 1`。這會讓門與室內地板存在台階、凹陷或高度差時誤判。此次將每個 room anchor 的地板解析改為動態搜尋：以 `doorY - 1` 為中心，向上／下最多 `MAX_FLOOR_SEARCH_VERTICAL=8` 格，尋找第一個「支撐方塊不可通行、其上方 room block 可通行」的 floor column。

flood fill 的水平 anchor 維持同一平面擴張，但每個 anchor 都會重新解析自己的 floor Y，因此可處理同一房間內有限的地板高度差。每個有效 floor column 再從其 room block 向上搜尋第一個非 passable 方塊，最高 30 格；ceiling 與 floor 都不再依賴固定世界 Y。Transient `SideDiagnostics` 新增 `minFloorY`／`maxFloorY`，書本除錯訊息會顯示地板高度範圍與房間高度範圍。

若掃描完成但沒有任何可解析 floor column，diagnose 會回傳 `no_floor`；若候選數超過 256，回傳既有受控 `scan_limit`。Saved Data schema、BuildingObservation 欄位與 settlement binding 邏輯不變。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：加入 `MAX_FLOOR_SEARCH_VERTICAL`、`findFloorColumn(...)`、`FloorColumn`／`FloorSearchResult`，並加入 floor Y diagnostics。
- `src/main/java/com/civilizationmod/BuildingDebugInteraction.java`：顯示 `floor_y` 與動態 room height。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：更新 geometry／side placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：更新繁體中文 geometry／side placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步繁體中文 geometry／side placeholder。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：記錄 floor column 動態搜尋規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步全域 geometry reference。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：更新 geometry reference 導航。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能導航。
- `Mods.md`：追加本次修改紀錄。

### 查證與驗證

| 命令／項目 | 結果 |
|---|---|
| `cmd /c "cd /d C:\\Minecraft && gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain"` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL in 9 sec`。 |
| `cmd /c "cd /d C:\\Minecraft && gradlew.bat --no-daemon build --console=plain"` | `BUILD SUCCESSFUL in 8 sec`；所有 Java、client、resources、jar、test、check 與 build 任務通過。 |
| Minecraft 26.2 API | `BlockState.getCollisionShape(...)`、`VoxelShape.isEmpty()`、`BlockState.liquid()` 已以 common jar `javap` 查證；Fabric 26.2 block shape 文件已讀取。 |
| 版本 | Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Fabric Loom `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。 |

### 未完成與風險

目前純 Java 回歸測試驗證的是既有 `evaluate(...)` 決策 seam，尚未以真實 `ServerLevel` fixture 自動驗證不同 floor Y；因此台階、凹陷、斜坡與多層樓仍需遊戲內測試。水平 flood fill 仍以 room anchor 的 X/Z 平面擴張，不會把樓梯當成真正的多層連通房間。若同一個 anchor 的搜尋範圍內有多個支撐方塊，現在採用距離中心較近的第一個候選，未來可再加入方向偏好與高度連續性評分。

### 下一步

重新啟動最新 client，測試至少三種結構：門與地板同高、室內地板比門低一格、室內地板比門高一格。手持普通書本對 marker ItemFrame 右鍵，回傳 `floor_y`、`room_height`、`floors`、`floor`、`ceiling`、`reason` 與 bounds。若 `floor_y` 能隨實際地板改變，表示固定 `doorY - 1` 問題已解除；若仍無效，再依具體 reason 判定是 floor candidate、collision shape、門內側方向或 scan limit。

### 來源

- [1] https://docs.fabricmc.net/develop/data-generation/block-models — Fabric Block Model Generation 26.2，包含 getCollisionShape／block support shape 方向。
- [2] https://docs.fabricmc.net/develop/blocks/blockstates — Fabric Block States 26.2。
- [3] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java — TekTopia flood-fill 與動態房高演算法參考。


## 2026-08-20 — 限制 floor height delta，避免誤認遠處地形與無法進入的房屋

### 變更

根據三組遊戲內截圖與 diagnostics，確認前一版動態 floor search 的搜尋範圍過寬：`floor_y=-61--61`、`floor=1`、`ceiling=0`，表示門附近沒有真正 floor column 時，搜尋器把房屋下方的遠處地形當成 floor 候選。這同時會讓「室內地板比門檻高一格」的房屋有機會被誤判為有效，但玩家實際上無法正常進入。

本次將入口地板高度規則收緊：預期 floor support 為 `doorY - 1`，目前只接受門檻同高或低一格的 floor column，使用 `MAX_FLOOR_LEVEL_RISE=0` 與 `MAX_FLOOR_LEVEL_DROP=1`。室內地板高於門檻的候選一律拒絕，不再把遠處 y=-61 地形納入 `floor_y` diagnostics。若所有 room anchor 都沒有符合規則的 floor column，diagnose 回傳 `no_floor`；若通過，`floor_y` 只顯示真正接受的 floor Y。

這個規則把「自由建築高度」與「入口可通行性」分開：天花板仍可相對門位置動態向上搜尋；地板則必須與入口有合理高度關係，不能只因為世界某處存在支撐方塊就視為室內地板。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：加入 `MAX_FLOOR_LEVEL_DROP`／`MAX_FLOOR_LEVEL_RISE`，讓 `findFloorColumn(...)` 以門檻相對高度限制候選；拒絕候選時不污染 floor Y diagnostics；無 floor candidate 時明確回傳 `no_floor`。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：記錄入口 floor height delta 規則與誤認地形 fallback。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步全域 geometry reference。
- `Mods.md`：追加本次正式紀錄。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| 遊戲內診斷 | 使用者提供的三組圖片顯示同高／低一格／高一格入口情境；最新錯誤診斷含 `floor_y=-61--61`、`floor=1`、`ceiling=0`，作為本次修正根據。圖片未以額外工具重新讀取，遵循使用者指示。 |
| `cmd /c "cd /d C:\\Minecraft && gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain"` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL in 13 sec`。 |
| `cmd /c "cd /d C:\\Minecraft && gradlew.bat --no-daemon build --console=plain"` | `BUILD SUCCESSFUL in 9 sec`；Java、client、resources、jar、test、check 與 build 通過。 |
| Minecraft 26.2 API | 沿用已由 common jar `javap` 查證的 collision shape、liquid 與 BlockPos API；本次沒有新增未查證版本 API。 |

### 未完成與風險

目前只允許地板比門檻低一格，不允許室內 floor 高於門檻；這是為了保證普通門入口可通行的保守規則，不代表未來所有特殊入口（樓梯、半磚、梯子、不同門型）都能被辨識。地板同高／低一格的真實木屋仍需使用最新 build 遊戲內重新測試；純 Java 回歸測試目前沒有 ServerLevel fixture，不能單獨證明三組實景都會得到預期結果。

### 下一步

重新啟動最新 client，分別測試：門與室內地板同高、室內地板低一格、室內地板高一格。確認前兩者的 `floor_y` 不再是遠處 y=-61，並觀察有效房屋是否得到 `floor>=2`、`ceiling>=1`；第三者預期應回傳 `no_floor` 或其他受控 invalid，而不應被判定 valid。若未來要支援真正高入口，應新增 door threshold／stair／slab 的明確模型，而不是放寬目前的高度 gate。


## 2026-08-20 — 第一版建築結構規格實作

### 變更

依照使用者確認的建築規格，將 `BuildingGeometryValidator` 從舊的 floor／ceiling 計數驗證擴充為第一版結構驗證：一棟建築必須有恰好一個門附近的合法 Marker；至少四格同一 Y 高度且由入口連通的地板；每個 floor column 上方都要找到非空氣屋頂；門所在面之外的三面邊界必須由非空氣、非液體方塊形成；入口必須具備可由門外站立位置進入房內的空間。牆體不限制為 full cube，玻璃、階梯、半磚、柵欄門與柵欄等非空氣方塊可作為邊界。

`warehouse_marker` 額外要求有效室內 floor 範圍內至少一個 `Blocks.CHEST`，否則回傳 `no_container`。Marker ambiguity、Marker 不在門旁、牆體缺失與入口不可通行分別使用受控 reason，不把不同問題混成 `no_ceiling`。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：加入合法 Marker 數量檢查、同 Y floor gate、動態非空氣屋頂、三面牆體、入口可進入性與 warehouse chest 條件；擴充 transient diagnostics。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：加入 `marker_not_at_door`、`marker_ambiguous`、`no_walls`、`no_entry`、`no_container` reason 常數。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：以 candidate function id 呼叫 validator，使 warehouse 能套用箱子條件。
- `src/main/java/com/civilizationmod/BuildingDebugInteraction.java`：顯示合法 Marker 數、牆體、入口與箱子 diagnostics，並以 function id 執行診斷。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：加入新增 reason 的 locale mapping。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：更新四格 floor／ceiling 門檻並加入 walls、entry、warehouse container regression cases。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：同步英文 reason 與 diagnostics placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：同步繁體中文 reason 與 diagnostics placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步專案政策下的繁體中文 reason 與 diagnostics placeholder。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：記錄第一版正式結構規則與已查證 API。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：更新專案內技能導航。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步全域 geometry reference。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能導航。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL in 13s`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL in 9s`；common、client、resources、jar、test、check 與 build 通過。 |
| Minecraft 26.2 API | 以目標 common jar 與實際 compile/build 驗證 `DoorBlock`、`ItemFrame`、`Level.getEntities`、`BlockState.getCollisionShape`、`VoxelShape.isEmpty`、`BlockState.isAir`、`BlockState.liquid` 與 `Blocks.CHEST`。 |
| Java | 專案 Java 25 toolchain。 |

### 未完成與風險

目前純 Java regression test 驗證的是 decision seam，尚未建立真實 `ServerLevel` fixture；Marker 在門左／右／上方、門附近多 Marker、4 格同 Y floor、屋頂洞、玻璃／階梯／半磚／柵欄牆與不同門方向仍需遊戲內測試。牆體檢查以 floor flood fill 的外圍邊界為基準；不支援階梯式室內 floor、多層樓或複雜入口過渡。液體雖然不是空氣，第一版仍拒絕液體作牆體或屋頂，以避免把水域當作房屋結構。

### 下一步

重新啟動最新 client，在測試房屋門附近只保留一個合法 Marker，確保至少 2×2 同 Y 地板、屋頂覆蓋 floor、門以外三面牆完整，warehouse 室內放一個箱子，再使用 `/civitas building scan` 與普通書本右鍵 ItemFrame。回傳 `reason`、`legal_markers`、`floor`、`ceiling`、`walls`、`entry`、`containers` 與 bounds，以驗證實際世界幾何。

### 來源

- [1] https://docs.fabricmc.net/develop/blocks/blockstates — Fabric Block States 26.2。
- [2] https://docs.fabricmc.net/develop/blocks/block-entities — Fabric Block Entities 26.2。
- [3] https://docs.fabricmc.net/develop/blocks/block-containers — Fabric Block Containers 26.2。
- [4] https://docs.fabricmc.net/develop/porting/ — Fabric Porting to 26.2。
- [5] https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java — TekTopia 結構掃描參考，不作 Fabric API 證明。



## 2026-08-20 — ItemFrame 附著牆判定與 Marker 有效狀態光效

### 變更

針對遊戲內診斷顯示 `合法標識=0`、`標識不在門附近` 的問題，確認原本流程只保存 `ItemFrame.blockPosition()`，沒有保留展示框實體的附著方向與背後支撐牆。現在 `BuildingMarkerScanner.MarkerCandidate` 保留實際 `ItemFrame`，正式 building scan 與書本除錯都改用 frame-aware validator。

ItemFrame 合法條件現在包含：展示框內是已註冊 Marker、展示框具有水平 `getDirection()`，且支撐牆位置 `frame.blockPosition().relative(frame.getDirection().getOpposite())` 是非空氣、非液體方塊。門附近合法 Marker 必須恰好一個；沒有附著牆回傳 `marker_not_at_door`，超過一個回傳 `marker_ambiguous`。

新增 `BuildingMarkerVisualState`。正式 `/civitas building scan` 驗證成功時，server 對來源 ItemFrame 裡的 Marker stack 設定 `DataComponents.ENCHANTMENT_GLINT_OVERRIDE=true`，使 Marker 顯示附魔光效；驗證失敗時移除該 component。這只切換視覺狀態，不替換 Marker、不消耗 Marker、不改 Saved Data schema。書本除錯仍保持 read-only，只顯示 `marker_attached`。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：加入 ItemFrame overload、附著牆支撐判定與 frame-aware marker context。
- `src/main/java/com/civilizationmod/BuildingMarkerScanner.java`：`MarkerCandidate` 保留實際 ItemFrame entity。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：正式 scan 使用 ItemFrame validator 並同步 Marker glint。
- `src/main/java/com/civilizationmod/BuildingDebugInteraction.java`：使用實際 ItemFrame 診斷並顯示 `marker_attached`。
- `src/main/java/com/civilizationmod/BuildingMarkerVisualState.java`：新增 server-side Marker 附魔光效切換 helper。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：同步 marker attached debug placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：同步標識附著 debug placeholder。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步繁體中文 debug placeholder。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：加入 ItemFrame 支撐牆公式、ItemStack component 與 glint 規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：加入 frame-aware validator 與 Marker visual state 導航。
- 全域 `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步查證 reference。
- 全域 `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步技能導航。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| Minecraft 26.2 ItemFrame API | `javap` 確認 `ItemFrame(Level, BlockPos, Direction)`、`blockPosition()`、`getDirection()`、`getItem()`、`setItem(ItemStack, boolean)`。 |
| ItemFrame 支撐牆語意 | 26.2 bytecode `ItemFrame.survives()` 實際讀取 `pos.relative(getDirection().getOpposite())`，已寫入 geometry reference。 |
| 附魔光效 API | `ItemStack.copy()`、`set(...)`、`remove(...)`、`isSameItemSameComponents(...)` 與 `DataComponents.ENCHANTMENT_GLINT_OVERRIDE` 已由 common jar 查證。 |
| `gradlew.bat --no-daemon compileJava --console=plain` | `BUILD SUCCESSFUL in 10s`。 |
| `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL in 10s`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL in 8s`；common、client、resources、jar、test、check 與 build 通過。 |

### 未完成與風險

最新提供的 log 是在本次修正前產生的，仍顯示 `合法標識=0`，不能用來判斷新版本是否已成功。新的 frame-aware 版本尚未在遊戲內測試。若重新測試後仍顯示 `marker_attached=no`，需要回傳新的 `marker` 座標、`door` 座標、ItemFrame facing 與背後支撐方塊狀態；若 `marker_attached=yes` 但仍 invalid，則再依新的 floor／wall／roof／entry reason 繼續診斷。

### 下一步

重新啟動最新 client，確保使用新 build；在門上方或左右牆面放置**恰好一個** warehouse Marker ItemFrame，背後必須是非空氣牆體，室內放至少一個箱子，執行 `/civitas building scan`。確認 Marker 出現附魔光效，再故意破壞屋頂或取下箱子重新 scan，確認附魔光效消失；使用書本右鍵查看 `legal_markers`、`marker_attached`、`reason`、`walls`、`entry` 與 `containers`。

### 來源

- [1] https://docs.fabricmc.net/develop/blocks/blockstates — Fabric Block States 26.2。
- [2] https://docs.fabricmc.net/develop/blocks/block-entities — Fabric Block Entities 26.2。
- [3] https://docs.fabricmc.net/develop/blocks/block-containers — Fabric Block Containers 26.2。
- [4] https://docs.fabricmc.net/develop/porting/ — Fabric Porting to 26.2。
- [5] 本機 `minecraft-common.jar` 的 `javap` 與 bytecode 輸出：`ItemFrame`、`HangingEntity`、`ItemStack`、`DataComponents`；查證日期 2026-08-20。


## 2026-08-20 — `/civitas building generate` 測試 warehouse 生成指令

### 變更

新增 server-side 測試建築生成器，提供一座可重現的標準 warehouse fixture。建築以命令執行位置為基準，在東側 6 格生成，包含同一 Y 高度的 4×4 室內木板地板、南向兩格木門、四面封閉木板牆、木板屋頂、室內箱子，以及附著在門上方牆面的單一 `warehouse_marker` ItemFrame。生成器先清除受控範圍內的方塊，再放置測試結構；它不自動執行 building scan，讓測試可以分開確認生成、掃描、驗證與 Marker 附魔光效。

新增 `/civitas building generate`，並由相同的 `rootCommand(String)` 自動提供 `/civilization building generate` 相容命令。命令使用 `Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)` 限制為 OP，成功訊息回報門與 Marker 座標，失敗時只輸出本地化且受控的原因。命令 literal 本身提供 Tab completion；既有 `scan`、`list` 與 `inspect` 子命令保持不變。

三份 locale 同步加入成功與失敗訊息；`zh_tw` 與 `zh_cn` 維持繁體中文政策，英文使用英文。所有命令訊息均經 `CivilizationMessages.translatable(...)` 包裝，只有品牌前綴使用金色。

### 26.2 API 查證

| API／行為 | 查證結果 |
|---|---|
| `Level.setBlock(BlockPos, BlockState, int)` | 已由現有 26.2 common jar 與實際 build 確認，生成器使用 flag `3`。 |
| `ServerLevel.addFreshEntity(Entity)` | 已由 26.2 `HangingEntityItem` bytecode 與實際 build 確認，生成器檢查 boolean 結果。 |
| `ItemFrame(Level, BlockPos, Direction)` | 已由 26.2 `HangingEntityItem` bytecode 確認；vanilla 放置流程將 clicked block relative(clicked face) 作為 constructor position。 |
| `ItemFrame.setItem(ItemStack, boolean)` | 已查證並使用 `false` 避免播放設定物品音效。 |
| ItemFrame 支撐牆公式 | 依 reference 使用 `frame.blockPosition().relative(frame.getDirection().getOpposite())`；生成器使用 `Direction.NORTH`，Marker 位於北牆外側位置，支撐牆為其南側木板。 |
| OP 權限 | 26.2 `Commands.LEVEL_GAMEMASTERS` 型別為 `PermissionCheck`，`CommandSourceStack` 沒有舊式 `hasPermission`；正確寫法為 `Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)`，已以 javap 查證並通過 compile。 |
| 方塊與門狀態 | `Blocks.OAK_PLANKS`、`Blocks.OAK_DOOR`、`Blocks.CHEST`、`Blocks.AIR`、`DoorBlock.FACING`、`DoorBlock.HALF`、`DoubleBlockHalf.LOWER/UPPER` 均已查證。 |

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingTestStructureGenerator.java`：新增可重現的 server-side 測試 warehouse 生成器與受控失敗結果。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：加入 `building generate`、OP 權限、座標成功回覆與本地化失敗原因映射。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：加入英文生成成功與失敗訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：加入繁體中文生成成功與失敗訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依專案政策同步繁體中文生成成功與失敗訊息。
- `Mods.md`：追加本次正式進度紀錄。

### 驗證

| 命令 | 結果 |
|---|---|
| `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain` | 初次因 26.2 權限 API 寫法錯誤失敗；改用 `Commands.hasPermission(...)` 後通過，`FoodModelRegressionTest: PASS`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；`compileJava`、`compileClientJava`、resources、jar、sourcesJar、test、check 與 build 通過。 |
| Gradle `processResources` | 通過，三份 locale JSON 可被資源處理流程讀取。 |

### 未完成與風險

尚未在遊戲內實際執行 `/civitas building generate` 與 `/civitas building scan`，因此 Marker 是否在目標 client 中顯示、掃描後是否得到 `valid`、附魔光效是否切換，仍標記為未驗證。生成器使用固定的測試 fixture 與受控清除範圍，不應視為未來自然 worldgen；若玩家站在狹窄或未載入區域，會回傳受控失敗。生成前只阻止測試範圍內既有 ItemFrame，尚未加入更完整的方塊破壞確認或 undo 機制，正式遊戲使用前仍應保留測試用途定位。

### 下一步

重新啟動最新 client，在測試世界執行 `/civitas building generate`；確認附近出現 warehouse 後執行 `/civitas building scan`，再用 `/civitas building list` 或 `/civitas building inspect 1` 查看驗證狀態。若結果不是 `valid`，手持普通書本右鍵 Marker ItemFrame，記錄 debug 顯示的門位置、方向、floor Y、room height、牆體、入口、箱子與 `marker_attached`，再依實際座標修正 fixture，而不是猜測 API。

### 來源

- [1] https://docs.fabricmc.net/develop/commands/basics — Fabric Creating Commands 26.2。
- [2] https://docs.fabricmc.net/develop/text-and-translations — Fabric Text and Translations 26.2。
- [3] `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar` — 26.2 `javap` 與 bytecode 查證；本次使用後的暫存輸出檔已清理。
- [4] `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md` — 專案內建築幾何與 ItemFrame 支撐牆公式 reference。



## 2026-08-20 — Warehouse 領地兩點選取與宣告優先驗證

### 設計決策

依照使用者確認，warehouse 建築不再由 validator 自動猜測最近的門、室內側、地板高度、屋頂或完整牆體。玩家手持 `warehouse_marker` 左鍵設定第一點、在仍手持同一 Marker 時右鍵設定第二點；兩點正規化後稱為「warehouse 領地」。玩家若切換快捷欄，不再手持 `warehouse_marker`，尚未完成的第一點選取立即取消；已完成的 warehouse 領地資料則保留在 Marker ItemStack 中。

warehouse 成立的第一版條件改為：領地資料完整且位於目前維度、ItemFrame 位於該領地內、ItemFrame 具有合法牆面支撐，並展示帶有同一 warehouse 領地資料的 `warehouse_marker`。門、地板、屋頂、牆體、入口與箱子不再是 warehouse activation 的硬性條件；舊 geometry flood fill 保留作相容與診斷用途。

### 變更

新增 `WarehouseTerritory`，使用 `DataComponents.CUSTOM_DATA` 保存版本化的 pending point A 或完整 `min_x/min_y/min_z/max_x/max_y/max_z` inclusive bounds。新增 `WarehouseMarkerItem`，以 `AttackBlockCallback` 處理左鍵第一點，以已查證的 `Item.useOn(UseOnContext)` 處理右鍵第二點，並使用 `inventoryTick` 在未完成選取離開主手時清除 pending point。

新增 `WarehouseTerritoryValidator`，將 warehouse frame-aware validation 改為 declaration-first；`BuildingGeometryValidator` 在有實際 warehouse ItemFrame 時優先委派 territory validator，並保留既有 door/floor/roof/wall geometry path 給相容入口與診斷。`BuildingObservation` 新增 territory missing、too large、wrong dimension、marker outside territory 與 marker not attached 的受控 reason 常數。

測試 warehouse generator 現在會把完整 warehouse territory 寫入生成的 Marker，並以低鄰居更新 flags 先後放置木門上下半部，避免先前 latest.log 中 generator 成功但 scan 找到其他門、回傳 `marker_not_at_door` 的舊設計問題。

三份 locale 新增 point A、完成、取消、缺少第一點、跨維度、超大領地與 territory validation 訊息；`zh_tw` 與 `zh_cn` 依專案政策維持繁體中文。專案技能與建築幾何 reference 已同步加入本次 26.2 API 查證與 warehouse 領地規則；全域技能也已同步。

### 影響檔案

- `src/main/java/com/civilizationmod/WarehouseTerritory.java`：保存與讀取 pending point／完成 territory bounds。
- `src/main/java/com/civilizationmod/WarehouseMarkerItem.java`：warehouse_marker 左鍵／右鍵選取與快捷欄切換取消。
- `src/main/java/com/civilizationmod/WarehouseTerritoryValidator.java`：領地、維度、ItemFrame 位置與支撐牆驗證。
- `src/main/java/com/civilizationmod/CivilizationItems.java`：改用 WarehouseMarkerItem 並註冊 callback。
- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：warehouse frame-aware path 優先使用 territory declaration，並開放 package 內支撐牆 helper。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：新增受控 territory validation reason。
- `src/main/java/com/civilizationmod/BuildingTestStructureGenerator.java`：生成帶 territory 的 Marker，修正測試門放置順序。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增英文 warehouse 領地訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文 warehouse 領地訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文 warehouse 領地訊息。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：追加 selection callback、inventoryTick、CustomData 與 declaration-first 規則。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：追加 warehouse 領地查證 reference。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步全域 geometry reference。
- `Mods.md`：追加本次正式記錄。

### API 查證與驗證

- Minecraft：`26.2`；Fabric Loader：`0.19.3`；Fabric API：`0.158.0+26.2`；Loom：`1.17.19`；Gradle：`9.5.1`；Java toolchain：`25`。
- Fabric 官方 Events 26.2：https://docs.fabricmc.net/develop/events。
- Fabric 官方 Custom Item Interactions 26.2：https://docs.fabricmc.net/develop/items/custom-item-interactions。
- Fabric 26.2 AttackBlockCallback Javadoc：https://maven.fabricmc.net/docs/fabric-api-0.158.0+26.2/net/fabricmc/fabric/api/event/player/AttackBlockCallback.html。
- 本機 common jar 查證 `Item.inventoryTick`、`Inventory.tick`、`UseOnContext`、`DataComponents.CUSTOM_DATA`、`CustomData`、`CompoundTag`、`ItemStack` 與 Fabric `AttackBlockCallback`／`UseBlockCallback` signatures。
- `gradlew.bat --no-daemon compileJava --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain`：`FoodModelRegressionTest` 執行完成，`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon build --console=plain`：`BUILD SUCCESSFUL`；common/client、resources、jar、sourcesJar、test、check 與 build 均通過。
- UTF-8 locale JSON 檢查：`en_us.json`、`zh_cn.json`、`zh_tw.json` 均 `JSON_OK`。
- `python /home/ubuntu/skills/skill-creator/scripts/quick_validate.py minecraft-civilization-fabric-262`：`Skill is valid!`。

### 未完成與風險

- 尚未在遊戲 client 實測「左鍵 A → 切換快捷欄取消」、「左鍵 A → 右鍵 B → 放入 ItemFrame → `/civitas building scan`」完整流程；目前只完成 API、編譯、回歸測試與 generator 整合。
- `warehouse_marker` 的領地安全上限目前是每軸 64 格、總體積 262144 格，這是第一版防止誤選與高成本掃描的保守限制，未來可改為設定值。
- 目前仍要求 ItemFrame 有牆面支撐；這符合 Minecraft ItemFrame 生存條件與既有 marker attached 規則，但領地內位置不再要求靠近門。
- 已完成 territory 的 Marker 若被複製成多份，現有 building observation 仍以 ItemFrame 位置作為 instance identity；同一領地的多個 Marker 去重與權限管理留待後續。
- 舊世界中沒有 territory CustomData 的 warehouse Marker 會受控回傳 `territory_missing`，不會被誤認為已宣告 warehouse。

### 下一步

重新啟動最新 client，使用一個空白 `warehouse_marker` 測試兩點選取與快捷欄取消；再把完成 territory 的 Marker 放入領地內且有牆支撐的 ItemFrame，執行 `/civitas building scan`，確認 building valid 與 Marker glint。完成手動流程後，再加入領地邊界可視化、`/civitas building selection clear`、同領地去重與未來 `accept/claim` 狀態。


## 2026-08-20 — Warehouse Marker tooltip、蹲下快速部署與同領地 duplicate

### 使用者確認的規則

使用者確認快速部署成功後必須消耗手中的 `warehouse_marker`，把同一份帶有完整 warehouse territory CustomData 的 ItemStack 轉移到 ItemFrame；不得重建一個只有 item identity 的新 Marker。玩家日後拆下 ItemFrame 時，應拿回相同的領地資料與其他 NBT／CustomData，不得遺失或重設。

使用者確認同一個 warehouse 領地若已有有效 Marker，後續相同領地的 Marker 不建立第二棟 warehouse；building scan 保留第二個 observation 供診斷，但標記為 `duplicate_territory`、invalid 並移除其附魔光效。

### 變更

`WarehouseTerritory` 的 CustomData 現在除了 pending point A 與正規化 min/max bounds，也保存原始 point A 與 point B。這讓 tooltip 能顯示玩家實際點擊的兩個位置；server 仍以正規化 bounds 做維度、範圍與 ItemFrame 位置驗證。完成的 Marker tooltip 顯示第一點、第二點與 warehouse territory 已完成；只完成第一點時顯示第二點待確認；空白 Marker 顯示第一點尚未設定。

新增 `WarehouseMarkerQuickDeploy` common-side callback。玩家必須蹲下、手持主手 `warehouse_marker`、瞄準空 ItemFrame，且 Marker 已完成 territory。server 依序驗證 ItemFrame 所在維度、領地包含關係、ItemFrame 牆面支撐與同領地 duplicate；通過後使用 `handStack.copyWithCount(1)` 保留完整 CustomData 寫入 ItemFrame，再使用 `handStack.shrink(1)` 消耗手中物品，套用 glint 並回傳成功。任何失敗都不消耗手上 Marker。非蹲下、非主手、非空展示框或非 warehouse_marker 互動會交回 vanilla 流程。

`CivilizationWorldData.scanBuildingMarkers` 現在以 `HashSet<WarehouseTerritory>` 按 scanner candidate 順序保留第一個有效領地 Marker；同一維度且 min/max 相同的後續有效 Marker 變成 `duplicate_territory` invalid observation，不會建立第二個有效 warehouse。三份 locale 新增 tooltip、快速部署成功與失敗原因，以及 duplicate reason。

純 Java 回歸測試加入 warehouse territory bounds 與 record identity 檢查。原先嘗試在該測試中直接建構 `ItemStack(CivilizationItems.WAREHOUSE_MARKER)`，因沒有完整 Minecraft registry 初始化而產生 `ExceptionInInitializerError`；已移除 registry-dependent 測試，保留可在該測試環境安全執行的純資料 bounds 測試。實際 ItemStack CustomData、tooltip 與 UseEntityCallback 流程仍列為遊戲內驗證項目，而不是宣稱已由純 Java 測試覆蓋。

### 影響檔案

- `src/main/java/com/civilizationmod/WarehouseTerritory.java`：保存原始 A/B 點與正規化 bounds，提供 tooltip 讀取方法。
- `src/main/java/com/civilizationmod/WarehouseMarkerItem.java`：新增 26.2 tooltip override，顯示 point A、point B 與領地完成狀態。
- `src/main/java/com/civilizationmod/WarehouseMarkerQuickDeploy.java`：新增蹲下右鍵空 ItemFrame 的 server-side 原封不動轉移。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：註冊 quick deploy callback。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：加入同領地 duplicate 去重與 `duplicate_territory` invalid observation。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：新增 duplicate territory reason。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：加入純資料 warehouse territory bounds／identity regression。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增英文 tooltip、部署與 duplicate 訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文 tooltip、部署與 duplicate 訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步相同繁體中文 tooltip、部署與 duplicate 訊息。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：追加 tooltip、UseEntityCallback、原封不動轉移與 duplicate 規則。
- `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：追加 tooltip、快速部署與 duplicate reference。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md`：同步全域 reference。
- `Mods.md`：追加本次正式記錄。

### API 查證與驗證

| 項目 | 結果 |
|---|---|
| Minecraft／Fabric 版本 | Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Loom `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。 |
| Item tooltip | 26.2 common jar 查證 `Item.appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`。 |
| 快速部署 callback | Fabric 26.2 官方 Javadoc 查證 `UseEntityCallback.interact(Player, Level, InteractionHand, Entity, EntityHitResult)` 與 client/server `InteractionResult` 語意。 |
| 蹲下方法 | 26.2 common jar 查證 `Entity.isShiftKeyDown()`。 |
| 物品轉移 | 26.2 common jar 查證 `ItemStack.copyWithCount(1)`、`shrink(int)`，並沿用已查證的 `ItemFrame.setItem(ItemStack, boolean)`。 |
| `gradlew.bat --no-daemon compileJava --console=plain` | `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain` | 初次因 registry-dependent ItemStack 測試產生 `ExceptionInInitializerError`；移除該環境不適用的測試後，`FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；common/client、resources、jar、sourcesJar、test、check 與 build 均通過。 |
| locale 與技能 | 前一階段 UTF-8 三份 locale 已通過；本階段新增 keys 已通過 `processResources`。全域技能 `quick_validate.py` 回傳 `Skill is valid!`。 |

官方來源：Fabric 26.2 [UseEntityCallback Javadoc](https://maven.fabricmc.net/docs/fabric-api-0.158.0+26.2/net/fabricmc/fabric/api/event/player/UseEntityCallback.html)、[Custom Item Interactions](https://docs.fabricmc.net/develop/items/custom-item-interactions)、[Events](https://docs.fabricmc.net/develop/events)。

### 未完成與風險

尚未在遊戲 client 實測蹲下右鍵空 ItemFrame 的完整封包流程、手上 Marker 消耗、ItemFrame 拆下後 CustomData 是否完整保留、領地外／非空展示框／未附著展示框的失敗 fallback，以及同領地第二個 Marker 的 duplicate 光效與 observation 結果。這些項目不能以 compile 或純 Java bounds test 代替。

duplicate scan 目前只對本次 scan 取得的 loaded ItemFrame candidate 去重；未載入區域的 ItemFrame 不會被強制載入搜尋。若同一領地的有效 Marker 位於未載入區域，而玩家在另一處部署第二個 Marker，第一版可能無法即時發現跨載入區域 duplicate，未來需要 SavedData territory claim 或明確 owner identity 才能提供更強一致性。

### 下一步

重新啟動最新 client，先查看空白 Marker tooltip；左鍵設定 A 點後確認描述顯示 `第一個點：X,Y,Z` 與 `第二個點：待確認……`，切換快捷欄後確認 pending A 被取消。接著重新選取 A/B，蹲下右鍵領地內的空 ItemFrame，確認手上 Marker 數量減少、ItemFrame 內 tooltip 顯示相同 A/B 與已完成領地，執行 `/civitas building scan` 確認第一個 Marker valid 且發光。最後在同一領地放置或部署第二個相同 Marker，確認第二個顯示 `duplicate_territory`、不發光，且不增加第二個有效 warehouse。


### 2026-08-20 追加交付檢查

本次 tooltip、快速部署與 duplicate 新增 keys 的三份 locale 重新以 UTF-8 PowerShell `ConvertFrom-Json` 驗證，結果為 `JSON_OK en_us.json`、`JSON_OK zh_cn.json`、`JSON_OK zh_tw.json`。檢查腳本執行後已刪除，沒有把暫存檔留在 `C:\Minecraft`。前述 Java 25 `compileJava`、`runFoodModelRegressionTest` 與完整 `build` 均已通過；目前只剩遊戲內互動封包、ItemFrame 轉移與拆下後 CustomData 保留尚未手動驗證。


## 2026-08-20 — Civitas 轉向殖民地建設：有效 warehouse 指派原版村民

### 方向決策

依照使用者提供的專案方向轉彎提示，Civitas 主線由「文明數值儀表板」轉為「玩家親手建設、指派村民、觀察村民移動與工作」的殖民地建設。既有 `BuildingGeometryValidator`、`BuildingMarkerScanner`、`BuildingObservation`、`CivilizationWorldData`、`WarehouseMarkerItem`、`WarehouseTerritory` 與 `/civitas building scan/list/inspect` 全部保留，作為殖民地建設的底層。FoodDemandModel、StabilityDebt 與 simulate 數值步進暫停作為主線，不刪除既有資料與測試。

第一個最小可行殖民地切片定義為：有效 warehouse 建築可以透過 `/civitas assign <building_index> [villager]` 指派一名原版 Villager；指派關係保存到 BuildingObservation；村民穿上 warehouse 黃色皮革胸甲；server 每 20 tick 檢查村民與 Marker 的距離，超過 8 格時要求 vanilla PathNavigation 移動到 Marker 附近。這一階段不替換村民 GoalSelector，也不實作農田收穫、物流配送或容器搬運。

### 實作變更

`BuildingObservation` 新增 optional `residentUuid` 與 `residentName` 欄位。為保持舊 Saved Data 相容，resident UUID 以空字串或 UUID 字串保存，服務層透過 UUID 解析；舊的 14 參數建構子仍保留。`refreshed(...)` 會保留 resident 欄位，building scan 重新觀測不會清除既有村民指派。新增 `hasResident()`、`residentUuidValue()`、`withResident(...)` 與 `withoutResident()`。

`CivilizationWorldData` 新增 `findBuildingAssignedTo(...)` 與 `replaceBuilding(...)`。assign handler 會先要求 BuildingObservation 的 validation status 為 `valid`，驗證目標為存活原版 Villager、位於命令執行者同一 ServerLevel，並拒絕同一村民已被其他建築使用。成功後標記 Saved Data dirty 並套用角色外套。`/civitas` 與 `/civilization` 共用相同 `assign` literal builder、building index suggestions 與 `EntityArgument.entity()` suggestions；省略 villager 參數時，server 只從玩家視線範圍內選取存活且有 line-of-sight 的最近 Villager。

新增 `BuildingResidentService` common-side service。它註冊 `ServerTickEvents.END_SERVER_TICK`，每 20 server ticks 低頻率處理有效 building observation；以 Marker 位置為導航目標，未載入、跨維度、死亡或無法取得的 villager 只跳過本次更新，不自動破壞 Saved Data。warehouse 使用 `Items.LEATHER_CHESTPLATE` 與 `DataComponents.DYED_COLOR`／`DyedItemColor` 套用 `DyeColor.YELLOW` 顏色，其他已知功能先使用綠色作為第一版 fallback。`CivilizationMod` 在 common initializer 註冊 service，沒有引入 client-only 類別或外部村莊模組依賴。

`CivilizationCommands` 的 building inspect/list 輸出新增指派村民欄位。三份 locale 新增 assign 成功、無效 building、建築尚未有效、目標不是 Villager、視線找不到 Villager、跨維度、重複指派、保存失敗，以及 resident assigned/unassigned 訊息；`zh_tw` 與 `zh_cn` 均維持繁體中文。`FoodModelRegressionTest` 新增 resident UUID/name 保存、refreshed 保留指派與 withoutResident 清除指派測試。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingObservation.java`：Saved Data resident UUID/name 與 assignment updater。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：查找既有村民指派與 replacement dirty 流程。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：兩個命令根的 `/assign`、目標驗證、Tab completion 與 inspect resident 輸出。
- `src/main/java/com/civilizationmod/BuildingResidentService.java`：視線選取 Villager、角色外套與每 20 tick Navigation。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：common-side service 註冊。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：resident assignment persistence regression。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：英文 assign/resident 訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：繁體中文 assign/resident 訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步繁體中文 assign/resident 訊息。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：專案內殖民地指派 API 與工作流。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能查證摘要。
- `/home/ubuntu/colony-assignment-api-findings.md`：本次查證摘要。
- `Mods.md`：本次正式進度記錄。

### 26.2 API 查證

- Fabric 官方 Events 26.2：https://docs.fabricmc.net/develop/events；確認 callback 以 `EVENT.register(...)` 註冊。
- Fabric 官方 Creating Commands 26.2：https://docs.fabricmc.net/develop/commands/basics；沿用既有 `CommandRegistrationCallback`／Brigadier 命令樹模式。
- Minecraft 26.2 common jar 已查證 `Villager`、`Mob.getNavigation()`、`PathNavigation.moveTo(...)`、`Entity.getUUID()`、`getEyePosition()`、`getViewVector(float)`、`getBoundingBox()`、`distanceToSqr(...)`、`hasLineOfSight(...)`、`LivingEntity.setItemSlot(...)`、`EntityArgument.entity()`、`ServerLevel.getEntityInAnyDimension(UUID)`、`MinecraftServer.getAllLevels()` 與 `MinecraftServer.getTickCount()`。
- Minecraft 26.2 common jar 已查證 `Items.LEATHER_CHESTPLATE`、`DataComponents.DYED_COLOR`、`DyedItemColor(int)`、`DyeColor.YELLOW` 與 `DyeColor.GREEN`。
- 版本：Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Loom `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。

### 驗證結果

| 命令 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava --console=plain` | `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon compileJava runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`、`BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；common/client、resources、jar、sourcesJar、test、check 與 build 均通過。 |

### 未完成與風險

尚未在實際 client 或 dedicated server 手動執行 `/civitas assign`、Tab completion、玩家視線選村民、村民實際 Navigation、世界重開後 UUID 重新取得與黃色胸甲渲染；這些不能以 compile 或純 Java regression 取代。第一版套用角色胸甲會替換村民原有胸甲，但尚未保存與還原原裝備；正式工作職業系統前需要加入裝備保存策略。村民目前只會被導向 Marker，尚未有倉庫管理、農田耕作、容器掃描、物流配送或工作 Goal。`ServerLevel.getEntityInAnyDimension(UUID)` 只處理已載入／可取得的 entity；村民離開載入範圍時服務會等待下次更新，不會強制載入區塊。

### 下一步

重新啟動 client，建立或生成一個有效 warehouse，執行 `/civitas building scan`，確認 `/civitas assign 1` 能從玩家視線選村民，或用 `/civitas assign 1 <villager>` 指定目標；接著以 `/civitas building inspect 1` 確認 resident name/UUID，觀察村民走向 Marker 並穿上黃色外套。手動流程通過後，下一個垂直切片再處理清除／重新指派、原有裝備還原與 warehouse container provider，不立即擴張到完整物流 AI。

### 來源

- [1] https://docs.fabricmc.net/develop/events — Fabric Events 26.2。
- [2] https://docs.fabricmc.net/develop/commands/basics — Fabric Creating Commands 26.2。


## 2026-08-20 — 修正指派村民外套不顯示：VillagerRenderer client overlay

### 問題根因

使用者已確認 `/civitas assign`、指派保存與村民導航成功，但村民畫面沒有黃色外套。先檢查舊版 latest.log；該 log 是外套修正前的測試紀錄，沒有 assign 後的裝備診斷，因此不能用它證明 server 沒有寫入胸甲。接著以 Minecraft 26.2 client common jar `javap -c -p` 查證 `net.minecraft.client.renderer.entity.VillagerRenderer` constructor：原版只註冊 `CustomHeadLayer`、`VillagerProfessionLayer` 與 `CrossedArmsItemLayer`，沒有 `HumanoidArmorLayer`。所以 server `Villager.setItemSlot(EquipmentSlot.CHEST, leatherChestplate)` 即使成功同步，原版 VillagerRenderer 仍不會把胸甲渲染出來。另查證 `VillagerRenderState` 不是 `HumanoidRenderState`、`VillagerModel` 不是 `HumanoidModel`，因此不能直接套用 vanilla `HumanoidArmorLayer`。

### 修正內容

新增 common-side `BuildingRoleEquipment`，統一建立 warehouse 角色胸甲：使用已查證的 `Items.LEATHER_CHESTPLATE`、`DataComponents.DYED_COLOR`、`DyedItemColor` 與 `DyeColor.YELLOW`，並加入 `DataComponents.CUSTOM_DATA` 欄位 `civitas_role=warehouse`。`BuildingResidentService` 改用此 helper，server 裝備資料仍是真實角色狀態，原有指派與 Navigation 邏輯不變。

新增 client-only `WarehouseRoleLayer`，型別為 `RenderLayer<VillagerRenderState, VillagerModel>`，使用透明 `warehouse_role.png` overlay texture 與已查證的 `RenderLayer.renderColoredCutoutModel(...)`，只覆蓋村民軀幹與手臂，不覆蓋頭部。新增 `WarehouseVillagerRenderStateAccess`、`VillagerRenderStateMixin` 與 `VillagerRendererMixin`：在精確的 `VillagerRenderer.extractRenderState(Villager, VillagerRenderState, float)` RETURN 注入點讀取 client 已同步的胸甲 CustomData；在 VillagerRenderer constructor RETURN 追加 warehouse role layer。所有 renderer、render state Mixin 與 texture 均放在 `src/client`，common source 沒有依賴 client-only 類別。client mixin JSON 已加入兩個新 Mixin。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingRoleEquipment.java`：common/server 角色胸甲與 `civitas_role` 標記。
- `src/main/java/com/civilizationmod/BuildingResidentService.java`：改用角色裝備 helper。
- `src/client/java/com/civilizationmod/client/WarehouseVillagerRenderStateAccess.java`：client render state access contract。
- `src/client/java/com/civilizationmod/client/WarehouseRoleLayer.java`：warehouse overlay layer。
- `src/client/java/com/civilizationmod/client/mixin/VillagerRenderStateMixin.java`：保存同步角色狀態。
- `src/client/java/com/civilizationmod/client/mixin/VillagerRendererMixin.java`：追加 layer 與讀取胸甲 role。
- `src/client/resources/civilizationmod.client.mixins.json`：註冊兩個 client Mixin。
- `src/client/resources/assets/civilizationmod/textures/entity/villager/warehouse_role.png`：透明黃色角色外套 overlay texture。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：追加 VillagerRenderer 根因與 client overlay 查證規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域 skill。
- `/home/ubuntu/villager-renderer-findings.md`：保存根因、bytecode 與修正方向摘要。
- `Mods.md`：本次正式進度記錄。

### API 查證

- Minecraft 26.2 client jar：`VillagerRenderer` constructor layer 註冊與 `extractRenderState` descriptor。
- Minecraft 26.2 client jar：`VillagerRenderState`、`VillagerModel`、`RenderLayer`、`RenderLayerParent`、`LivingEntityRenderer.addLayer(...)`、`RenderLayer.renderColoredCutoutModel(...)`。
- Fabric 官方《Creating Your First Entity 26.2》確認 entity rendering 屬於 client side：https://docs.fabricmc.net/develop/entities/first-entity。
- 未把 `HumanoidArmorLayer` 強行套入 VillagerRenderer，因 26.2 型別與 VillagerModel 不相容；改用已查證的自訂 RenderLayer。

### 驗證結果

| 命令／檢查 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；common/client、client resources、jar、sourcesJar、test、check 與 build 均通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 已啟動 runtime smoke test，載入至 client initialization，未在啟動輸出中看到新的 Mixin crash；因 runClient 是持續任務，已停止背景程序。 |
| 26.2 VillagerRenderer bytecode | 確認原版沒有 HumanoidArmorLayer，支持本次根因判定。 |

### 未完成與風險

尚未由使用者在實際世界中確認 overlay 的最終畫面，因目前 smoke test 沒有進入已指派村民所在的測試世界。這次 runtime smoke 只證明 client 啟動至初始化階段，不能取代遊戲內視覺驗收。overlay texture 是第一版固定黃色角色服，尚未依不同 building function 建立不同 texture。server 目前仍會替換村民原有胸甲，尚未保存與還原原裝備。若下一次 log 顯示 Mixin 在載入 VillagerRenderer 時失敗，應立即回到精確 descriptor 與 layer generic 檢查，不要猜測其他 renderer API。

### 下一步

重新啟動最新 client，重新載入有已指派 warehouse 村民的世界；確認村民身上出現黃色外套。若仍沒有顯示，請提供該次新的 `run/logs/latest.log`，並執行 `/civitas building inspect 1` 確認 resident UUID；下一步將加入一次性 client/server role diagnostic，而不是再次修改 server 指派邏輯。

### 來源

- [1] https://docs.fabricmc.net/develop/entities/first-entity — Fabric Creating Your First Entity 26.2。


## 2026-08-20 17:42 — 修正 VillagerRenderer Mixin 黑屏：改用 LivingEntityRenderer Invoker

### 問題根因
Minecraft 26.2 的 `LivingEntityRenderer.addLayer(RenderLayer<S, M>)` 實際宣告在 superclass `LivingEntityRenderer`，不是 `VillagerRenderer` 本身。`VillagerRendererMixin` 以 `@Mixin(VillagerRenderer.class)` 使用 `@Shadow addLayer(...)`，因此啟動時出現 `@Shadow method addLayer ... was not located in the target class net.minecraft.client.renderer.entity.VillagerRenderer`，造成 client Mixin 套用失敗與主畫面黑屏。

### 修正內容與影響檔案
移除 `VillagerRendererMixin` 的錯誤 `@Shadow`；`LivingEntityRendererAccessor.java` 改為 client-only `@Mixin(LivingEntityRenderer.class)` 的 `@Invoker("addLayer")`，由 `VillagerRendererMixin` constructor RETURN 注入以 invoker 追加 `WarehouseRoleLayer`；`civilizationmod.client.mixins.json` 加入 accessor。所有 renderer/accessor 仍在 `src/client/java`，common/server 指派與導航邏輯未變。同步將本次根因與修正規則寫入專案及全域 `minecraft-civilization-fabric-262/SKILL.md`。

### 查證與驗證

| 命令／檢查 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；common/client、resources、jar、sourcesJar、test、check 與 build 均通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 通過 Fabric/Minecraft 載入、client initialization、Render thread 與 texture atlas 建立，未再出現原本的 VillagerRenderer Mixin failure；已停止持續執行的開發 client。 |
| 修正後 `run/logs/latest.log` | 沒有原本的 `@Shadow addLayer` 黑屏錯誤。Realms authentication error 是開發環境登入服務訊息，與本模組無關。 |

### 未完成與風險
尚未由玩家在含已指派 warehouse 村民的實際世界進行最終畫面驗收；目前已確認主畫面黑屏根因移除且 client 可啟動到渲染初始化。若黃色外套仍不顯示，下一步先檢查新的 `latest.log`、`/civitas building inspect <index>` 的 resident UUID、role state 與 texture，不重新改動 server assignment。

### 下一步
重新啟動 client，載入有已指派 warehouse 村民的世界；先確認主畫面恢復，再確認村民是否出現黃色外套。

### 來源
- [1] https://docs.fabricmc.net/develop/entities/first-entity — Fabric 26.2 entity rendering client-side reference。


## 2026-08-20 19:10 — 修正 warehouse 村民外套延伸到臉部的 overlay 對齊問題

### 問題判定

使用者提供的遊戲截圖顯示黃色 warehouse overlay 已成功載入，但有一小段錯誤延伸到村民頭部／下巴附近。檢查 Minecraft 26.2 client source 後確認，`WarehouseRoleLayer` 原本呼叫 `renderColoredCutoutModel(this.getParentModel(), ...)`，會把 `VillagerModel.root()` 的所有部件一起提交。26.2 `VillagerModel` 的 root 包含 `head`、`body`、`arms`、`right_leg` 與 `left_leg`；因此 warehouse texture 的非透明 UV 也會套用到頭部模型，造成截圖中的異常。

### 修正內容

`WarehouseRoleLayer` 改用已查證的 26.2 `SubmitNodeCollector.submitModelPart(...)`，只提交 `model.root().getChild("body")` 與 `model.root().getChild("arms")`。`body` 會包含原版 VillagerModel 的 `jacket` 子部件；head、hat、nose、legs 不再接收 warehouse overlay。這是 client-only 最小修正，沒有修改 server-side 村民指派、Saved Data、導航或角色判定。

### 影響檔案

- `src/client/java/com/civilizationmod/client/WarehouseRoleLayer.java`：由整個 VillagerModel overlay 改為 body/arms ModelPart overlay。
- `run/logs/latest.log`：保存本次 client smoke test 結果。
- `Mods.md`：追加本次正式進度記錄。

### 26.2 API 查證

以專案內 Minecraft 26.2 client sources/jar 查證：

- `VillagerModel` 的 root 部件名稱為 `head`、`body`、`arms`、`right_leg`、`left_leg`。
- `Model.root()` 為 public API；`ModelPart.getChild(String)` 為 public API。
- `ModelPart.render(...)` 可渲染部件及其子部件。
- `OrderedSubmitNodeCollector.submitModelPart(...)` 可提交單一 `ModelPart`，並接受 `RenderType`、light 與 overlay 座標。
- `LivingEntityRenderer.getOverlayCoords(...)` 與 `RenderTypes.entityCutout(...)` 可沿用既有 client 渲染流程。

### 驗證結果

| 命令／檢查 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 通過 Fabric/Minecraft 載入、client initialization、Render thread 啟動與 texture atlas 建立；沒有新的 Mixin、RenderLayer 或 ModelPart 例外。已停止持續執行的開發 client。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；common/client、resources、jar、sourcesJar、test、check 與 build 均通過。 |
| 修正後 `run/logs/latest.log` | 僅看到既有 Realms authentication error，與本模組渲染修正無關；沒有新的模組渲染錯誤。 |

### 未完成與風險

目前已完成程式與啟動驗證，但尚未由使用者在遊戲內重新觀察同一名 warehouse 村民的最終畫面，因此「下巴附近黃色殘留」仍需要手動視覺驗收。若仍有小範圍錯位，下一步應檢查 warehouse texture 的 body/arms UV，而不是再把 head 加回 overlay。`runClient` 啟動時曾出現 `Unable to delete file C:\Minecraft\run`，但隨後仍正常進入 client initialization，未形成模組錯誤。

### 下一步

重新啟動 client，進入原本有已指派 warehouse 村民的世界，確認黃色外套只覆蓋軀幹與手臂，臉部、鼻子、帽子與腿部不再出現黃色貼圖。若畫面仍有異常，提供新的截圖與 `latest.log`，再針對 texture UV 做下一個最小修正。


## 2026-08-20 — Terran Villagers 資源包授權與 Civitas 整合評估

### 研究來源

- CurseForge 專案頁：https://www.curseforge.com/minecraft/texture-packs/terran-villagers
- CurseForge 26.2 檔案頁：https://www.curseforge.com/minecraft/texture-packs/terran-villagers/files/8650013
- ETF Modrinth：https://modrinth.com/mod/entitytexturefeatures/versions
- EMF Modrinth：https://modrinth.com/mod/entity-model-features/versions
- Creative Commons BY-NC-ND 4.0：https://creativecommons.org/licenses/by-nc-nd/4.0/

### 查證結果

Terran Villagers 作者為 `sunracou`，CurseForge Project ID 為 `1544973`。目前 26.2 檔案為 `Terran Villagers [6.5] 26.1-2.zip`，標示 Release，檔案頁日期為 2026-08-14。`pack.mcmeta` 使用 `pack_format: 15`、`min_format: 15`、`max_format: 999`，並明確標示 `[Requires mods ETF/EMF]`。

資源包內含 `assets/minecraft/textures/entity/villager/` 下大量 64x64 villager variants、profession/type `.properties`，以及 `assets/minecraft/optifine/cem/villager.jem` 和多個 `.jpm` CEM 子模型。這代表它不是單一可直接匯入 Civitas 的紋理，而是依賴 ETF/EMF 與 OptiFine CEM 格式，對整體原版村民模型、紋理變體與職業／生態域條件進行客戶端替換。ETF 與 EMF 的 Modrinth 頁面均顯示支援 Minecraft 26.2、Fabric，且為 client-side mod。

CurseForge Custom License 將 CEM 相容 source code、scripts、configuration files 與 graphical assets 分開：前者為 GPLv3；後者為 CC BY-NC-ND 4.0。Creative Commons 官方條款確認可在署名、授權連結與非商業條件下分享原始內容，但禁止商業使用，也禁止修改、轉換、改作後再分發。故不得把 Terran Villagers 的 PNG、CEM、properties 或其他美術資產拷貝、改色、裁切、合併或重新打包進 `civilizationmod` jar，也不可將其改成 warehouse overlay 後隨模組發布。

### Civitas 決策

| 使用方式 | 判定 | 原因 |
|---|---|---|
| 玩家自行下載 26.2 pack，放進 instance 的 `resourcepacks` 後啟用 | **可以** | 這是外部 resource pack 的正常使用方式，Civitas 不重新發布其藝術資產。需要玩家自行安裝 ETF/EMF。 |
| Civitas 將 Terran Villagers zip 或其中 PNG/CEM 直接放入 mod jar | **目前不採用** | 涉及再發布美術資產；Custom License 的 CC BY-NC-ND NoDerivatives 與嵌入／改作風險不能直接假設允許。 |
| Civitas 修改、改色、裁切或合併其紋理成 warehouse overlay | **禁止直接採用** | 這是衍生作品；CC BY-NC-ND 禁止分發修改後 material。 |
| 只讓被 Civitas 指派的村民使用 Terran 外觀 | **目前不能直接由該 pack 完成** | 該 pack 主要全域覆蓋 `assets/minecraft` 並由 ETF/EMF 選擇；不會自然讀取 Civitas 的 resident role state。若取得作者書面許可，才可另做 role-aware client renderer。 |

### 影響檔案與筆記

- `skills/minecraft-civilization-fabric-262/SKILL.md`：追加授權、ETF/EMF、資源包結構與 Civitas 整合規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域技能副本。
- `/home/ubuntu/terran-villagers-findings.md`：保存 CurseForge、zip、ETF/EMF 與 Creative Commons 查證筆記。
- `Mods.md`：本次正式研究記錄。

### 未完成與風險

尚未聯絡作者取得額外書面授權，因此本專案不會把 Terran Villagers 資產加入 repository 或 mod jar。下載的 zip 只位於 sandbox Downloads 供被動檔案結構檢查，沒有安裝進 `C:\Minecraft`，也沒有修改或重新發布。資源包的 `villager.jem` 參照 `villager_default_arms.jpm`，但在該 zip 中未找到同名檔案；即使玩家自行使用，也應以 ETF/EMF 實際 client 測試結果為準。

### 下一步

若只是希望玩家看到更友善的原版村民，保留「外部資源包相容性說明」即可；若希望 Civitas 指派村民擁有獨立美術風格，建議由專案製作完全原創、非 Terran 衍生的村民／warehouse 角色紋理。若要正式採用 Terran Villagers 的美術，先向作者取得明確書面許可，確認可修改、嵌入模組、再分發及發布渠道後再設計技術整合。


## 2026-08-20 — Warehouse Storage Provider 第一個可驗證切片

### 目標

將已完成的 warehouse marker／領地宣告／建築驗證／村民指派閉環，向真正的殖民地建設玩法推進。第一版只觀測合法 warehouse territory 內的已載入普通箱子，不執行村民搬運、不修改箱子內容、不加入外部模組硬依賴。

### 實作內容

新增 `BuildingStorageItem`，以 server-side registry item ID 與 long count 保存單一物品種類摘要；不保存 client `Component` 或 display name。新增 `BuildingStorageSnapshot`，保存 `scanned`、箱子數量、未載入方塊數、物品總數、最後掃描 tick 與排序後的 item list。

新增 `BuildingStorageProvider`。它使用已查證的 Minecraft 26.2 `Level.isLoaded(BlockPos)`、`getBlockState(BlockPos)`、`ChestBlock.TYPE`、`ChestType.SINGLE/LEFT/RIGHT`、`ChestBlock.getContainer(...)`、`Container.getContainerSize()`、`Container.getItem(int)`、`ItemStack.isEmpty()`、`getCount()`、`BuiltInRegistries.ITEM.getKey(...)` 掃描。掃描範圍直接使用已完成 `WarehouseTerritory` 的 inclusive min/max bounds，不強制載入區塊；未載入位置只累計 `unloadedBlockCount`。普通雙箱只以 `SINGLE` 或 `LEFT` half 作為 primary，將雙箱內容合併為一個 Container，跳過 `RIGHT` half，避免重複計數。

`BuildingObservation` 新增 optional `storage` snapshot。因原本 observation Codec 已有 16 個欄位，直接新增第 17 欄位會觸發 RecordCodecBuilder arity 編譯錯誤；已改用 `RecordCodecBuilder.mapCodec` 建立 `CodecTail`，把既有 top-level `resident_name` 與新 top-level optional `storage` 合併為第 16 個 group 欄位，再由主 Codec 還原 canonical record。舊 Saved Data 沒有 storage 時會取得 `BuildingStorageSnapshot.unscanned()`，不會崩潰。`refreshed(...)`、`withResident(...)` 均保留既有 storage snapshot；只有 valid warehouse marker 且 territory 可解析時才以本次掃描結果更新摘要，invalid／duplicate／未載入 observation 不會用空資料覆蓋上一次成功摘要。

`CivilizationWorldData.scanBuildingMarkers(...)` 已接入 provider。`/civitas building inspect <index>` 與 `/civilization building inspect <index>` 的 building entry 新增 storage 摘要；內容包括箱子數、物品種類、物品總數、未載入方塊、掃描 tick 與各 item registry ID／數量。`en_us`、`zh_tw`、`zh_cn` 已同步新增 storage 翻譯；所有輸出仍透過 `CivilizationMessages` 包裝品牌前綴。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingStorageItem.java`
- `src/main/java/com/civilizationmod/BuildingStorageSnapshot.java`
- `src/main/java/com/civilizationmod/BuildingStorageProvider.java`
- `src/main/java/com/civilizationmod/BuildingObservation.java`
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`
- `src/main/java/com/civilizationmod/CivilizationCommands.java`
- `src/main/resources/assets/civilizationmod/lang/en_us.json`
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`
- `skills/minecraft-civilization-fabric-262/SKILL.md`
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`
- `Mods.md`

### 查證與驗證

| 命令／檢查 | 結果 |
|---|---|
| Minecraft 26.2 common jar javap：`Level`, `Container`, `ChestBlock`, `ChestType`, `Registry`, `BuiltInRegistries` | 已確認使用的 26.2 方法與欄位簽名；未依賴猜測 API。 |
| 第一次 `compileJava compileClientJava` | 預期失敗：直接新增第 17 個 `BuildingObservation` Codec 欄位超過 RecordCodecBuilder group arity。已記錄並改用 `CodecTail`／`mapCodec`。 |
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | `BUILD SUCCESSFUL`。只有既有 `BuildingGeometryValidator` deprecated API note。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；jar、sourcesJar、test、check 與完整 build 通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 通過 Fabric/Minecraft 26.2 載入、51 mods 載入、Mixin 初始化、Indigo renderer、LWJGL 與 texture atlas 建立；已停止 smoke test。 |
| `run/logs/latest.log` | 沒有新的 storage、Mixin、RenderLayer 或模組例外；Realms 401／SignedJWT 是既有開發環境登入服務訊息。 |

### 未完成與限制

目前尚未由玩家在遊戲內用實際 warehouse 進行容器摘要驗收，因此箱子掃描結果仍需要手動確認。第一版只支援普通 `Blocks.CHEST`，不支援 trapped chest、barrel、hopper、shulker box 或外部模組容器。`unloadedBlockCount` 只表示 territory 中未載入位置，並不將未載入區域當成空箱；大領地的命令掃描仍可能有可感知耗時，但目前 `WarehouseTerritory.MAX_VOLUME` 為 262144 且只在玩家主動 `building scan` 時執行。

### 下一步

玩家在 warehouse territory 內放入一個普通單箱或雙箱，放入至少兩種物品，執行 `/civitas building scan`，再執行 `/civitas building inspect <index>`，確認箱子數、物品種類與總數符合實際內容；重複 scan 後確認 storage snapshot 與 resident 指派仍保留。手動驗收完成後，下一個小切片才設計「村民將指定產物存入 warehouse」的最小物流行為，不直接開放全物品自動搬運。


## 2026-08-20 — Warehouse 第一個安全物流行為：指定物品入庫

### 目標與設計決策

在上一個 warehouse storage snapshot 切片之上，加入第一個不會任意搬走物品的物流行為。被指派且已抵達有效 warehouse Marker 的村民，每次 server 週期最多處理自己 inventory 的一個 slot；只有該物品的 registry ID 已出現在上一次成功 warehouse scan 的 storage snapshot 中，才允許嘗試入庫。空 warehouse、尚未掃描的 warehouse 或沒有任何已觀測物品種類的 snapshot 不會自動接收物品。這個 allowlist 是暫時的玩家意圖邊界，避免第一版物流服務把村民或玩家物品無條件搬走。

物流只在 building validation valid、function warehouse、村民仍在相同 `ServerLevel`、存活且不在交易時執行；村民距離 Marker 超過 8 格時仍沿用既有導航，不會同時執行入庫。每次成功轉移後立即重新掃描 territory 並以 `CivilizationWorldData.replaceBuilding(...)` 保存新 storage snapshot；既有 resident UUID／名稱與其它 building 欄位均保留。

### 實作內容

新增 `BuildingLogisticsService`。它重新取得 Marker ItemFrame 的 warehouse territory，使用 `BuildingStorageProvider.primaryChestContainer(...)` 共用普通單箱／雙箱 primary 判定，只掃描已載入的 `Blocks.CHEST`。物品只會放入空 slot 或 `ItemStack.isSameItemSameComponents(...)` 的同類堆疊；寫入時尊重 `Container.canPlaceItem(...)` 與 `Container.getMaxStackSize(ItemStack)`，以 `Item.getDefaultMaxStackSize()` 限制容量，不會覆蓋不同物品。來源 inventory 與 target container 更新後呼叫 `setChanged()`，每次 invocation 最多成功處理一個 villager inventory slot。

`BuildingResidentService` 在原本每 20 server ticks 的 assigned villager 迴圈中接入物流服務。當村民抵達 Marker 附近且 building 是 warehouse 時，嘗試執行一次安全入庫；沒有符合 allowlist、沒有空間或找不到有效 Marker／territory 時不做任何物品變更。沒有新增 client code、networking 或外部模組依賴。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingLogisticsService.java`
- `src/main/java/com/civilizationmod/BuildingResidentService.java`
- `src/main/java/com/civilizationmod/BuildingStorageProvider.java`
- `skills/minecraft-civilization-fabric-262/SKILL.md`
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`
- `Mods.md`

### 26.2 API 查證

以專案實際 `minecraft-common-043a8b3edf-26.2.jar` javap 查證 `Villager`、`AbstractVillager.getInventory()`、`Container`、`ItemStack`、`Item`。已確認使用的方法包括 `getInventory()`、`getContainerSize()`、`getItem(int)`、`setItem(int, ItemStack)`、`setChanged()`、`canPlaceItem(int, ItemStack)`、`getMaxStackSize(ItemStack)`、`split(int)`、`copyWithCount(int)`、`isSameItemSameComponents(...)`、`shrink(int)`、`grow(int)`、`getCount()`、`isEmpty()` 與 `Item.getDefaultMaxStackSize()`。查證規則已同步寫入專案與全域 skill。

### 驗證結果

| 命令／檢查 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；test、check、jar 與 sourcesJar 通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 通過 Minecraft 26.2 client 載入、Fabric Mixin、Render thread、Indigo renderer 與 texture atlas 初始化；已停止 smoke test。 |
| latest.log | 沒有新的物流、Mixin 或渲染例外；開發環境既有 Realms 401／SignedJWT 與 run directory delete warning 不屬於本次物流程式錯誤。 |

### 未完成與風險

目前尚未由玩家完成「村民 inventory 內有已觀測物品 → 抵達 Marker → 箱子數量增加」的遊戲內物流驗收。第一版只支援普通單箱／雙箱，不支援 trapped chest、barrel、hopper、shulker box 或外部模組容器。allowlist 依賴最近一次成功 storage snapshot；玩家如果手動清空或替換箱子內容，應先重新執行 `/civitas building scan`，再測試新的允許物品。村民原有 inventory 內容與 vanilla AI 仍由原版管理，Civitas 不會直接建立、刪除或任意替換村民物品。

### 下一步

遊戲內驗收成功後，才進入下一個切片：讓玩家用明確命令設定 warehouse 允許的物品，而不再以「上一次 scan 出現過」作為暫時 allowlist；之後再設計農田產出與 warehouse 之間的來源／目的地物流，不直接擴張成全域自動搬運。


## 2026-08-20 — Warehouse marker 拆除即刪除與重新啟用

### 使用者決策

warehouse 領地不新增刪除指令。玩家拆掉承載 warehouse marker 的 ItemFrame，視為刪除該 warehouse 建築 observation 與其領地使用記錄；之後重新完成 territory 的 warehouse marker 放入 ItemFrame，再執行 building scan，即可重新建立並啟用。這解決了「拆掉舊 marker 後重新放置卻被 duplicate territory／舊 Saved Data 卡住」的問題。

### 根因

`CivilizationWorldData.scanBuildingMarkers(...)` 原本只會掃描目前存在的 ItemFrame、建立新 observation 或刷新同座標 observation，從不刪除已消失的 marker。因此拆除 ItemFrame 後，舊 `BuildingObservation` 仍留在 Saved Data；重新放置另一個 marker 時，舊資料可能持續造成 duplicate territory 或保留一筆看似存在但實體已消失的建築記錄。

### API 查證與選擇

Fabric 官方 26.2 Events 文件 <https://docs.fabricmc.net/develop/events> 確認 callback 應使用 `EVENT.register(...)`，並優先於 Mixin。專案實際解析的 `fabric-entity-events-v1` `5.0.5+06488ac19e` jar class 清單已查證，包含 `EntityElytraEvents`、`EntitySleepEvents`、`ServerEntityCombatEvents`、`ServerEntityLevelChangeEvents`、`ServerLivingEntityEvents` 與 `ServerPlayerEvents` 等，但沒有通用 `EntityRemoveCallback`、`ItemFrameBreakCallback` 或 ItemFrame 專用 removal callback。沒有猜測不存在的事件名稱，也沒有為這個功能新增不必要的 Mixin。

### 實作內容

`CivilizationWorldData` 新增 `removeBuilding(BuildingObservation)` 與 `removeMissingBuildings(ServerLevel)`。cleanup 只處理相同維度、且 marker BlockPos 已載入的 observation；在該位置找不到相同 `functionId` 的 ItemFrame 時才移除資料並呼叫 `setDirty()`。若區塊尚未載入，保留資料，不會因玩家離開區域而誤刪合法建築。

`BuildingResidentService` 每 20 server ticks 對所有 server dimensions 執行 cleanup；`scanBuildingMarkers(...)` 開始時也先執行一次 cleanup。玩家拆掉 marker 後通常最多等待一個低頻率週期，重新放置同一功能 marker 並 building scan 即可重新啟用。若在 cleanup 週期內已於相同座標放回相同功能 marker，系統視為同一筆 observation，不會重置 resident 或 storage snapshot。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationWorldData.java`
- `src/main/java/com/civilizationmod/BuildingResidentService.java`
- `skills/minecraft-civilization-fabric-262/SKILL.md`
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`
- `Mods.md`

### 驗證結果

| 命令／檢查 | 結果 |
|---|---|
| Minecraft 26.2 common／Fabric API class 查證 | 已完成；未使用猜測的 ItemFrame removal callback。 |
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；test、check、jar 與 sourcesJar 通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 通過 Fabric、Mixin、Render thread、Indigo renderer 與 texture atlas 初始化；已停止 smoke test。 |
| 最新 client log | 沒有新的 marker cleanup、Mixin 或渲染例外；run directory delete warning 與 Realms 401／SignedJWT 是開發環境既有訊息。 |

### 尚未完成的實機驗收

尚未在使用者的實際世界中執行拆除／重新放置流程。建議驗收：先確認 `/civitas building list` 有舊 warehouse，拆掉承載 marker 的 ItemFrame，等待約 1 秒或執行 `/civitas building scan` 觸發 cleanup，再確認 building list 數量下降；接著用同一個已完成 territory 的 warehouse marker 放入新的 ItemFrame，執行 `/civitas building scan`，確認重新出現有效 warehouse，且不再顯示 duplicate territory。若 ItemFrame 所在區塊未載入，cleanup 會刻意延後，這是避免誤刪資料的安全設計。


## 2026-08-20 — Civitas 村民背包介面與交易替代

### 需求決策

取消 warehouse 刪除時的資源回收需求。刪除 warehouse marker 時不搬動、不掉落、不清空 warehouse territory 內的任何原版箱子、物品或方塊；既有 marker lifecycle cleanup 只移除對應的 `BuildingObservation` 登記。資源仍保留在世界原位置。

### 變更

完成第一版 Civitas 村民背包介面。只有已指派到有效 Civitas building、且該村民仍存活的原版 `Villager`，在玩家使用主手右鍵時會開啟 Civitas backpack，不再進入原版村民交易。未指派村民、失效建築、非主手互動與非村民實體仍回傳 PASS，保留原版行為。

第一版背包提供 8 格，直接映射 Minecraft 26.2 `AbstractVillager.getInventory()` 的原版村民 inventory。這不是第二套暫存箱，也沒有把物品複製到新的 Saved Data；原版村民已負責 inventory 的 entity 保存，Menu 只提供 server-authoritative 的操作介面。背包 Menu 的 `stillValid` 會重新檢查村民存活、同維度、玩家互動距離、building validation valid 與 resident UUID，不能由 client 自行決定資格。

### 影響檔案

- `src/main/java/com/civilizationmod/CivitasMenuTypes.java`：註冊 `villager_backpack` common MenuType。
- `src/main/java/com/civilizationmod/CivitasVillagerBackpackContainer.java`：以 8 槽 adapter 映射原版村民 inventory，拒絕不存在的第 9 槽。
- `src/main/java/com/civilizationmod/CivitasVillagerBackpackMenu.java`：server-authoritative menu、8 格村民槽、玩家 inventory、shift-click transfer 與 Saved Data validity check。
- `src/main/java/com/civilizationmod/CivitasVillagerInteraction.java`：使用已查證的 `UseEntityCallback`，只替換有效 Civitas resident 的原版交易。
- `src/main/java/com/civilizationmod/BuildingRoleEquipment.java`：新增任一非空 `civitas_role` 判定，供 client/server interaction gating 使用。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：註冊 MenuType 與村民互動 callback。
- `src/client/java/com/civilizationmod/client/CivitasVillagerBackpackScreen.java`：8 格 backpack 與玩家 inventory 的 client GUI，使用 common menu 同步真實物品。
- `src/client/java/com/civilizationmod/client/CivilizationModClient.java`：註冊 `MenuScreens`。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`、`zh_tw.json`、`zh_cn.json`：新增 backpack title 與玩家 inventory 翻譯；zh_cn 依專案政策維持繁體中文。
- `skills/minecraft-civilization-fabric-262/SKILL.md` 與全域同名 skill：追加 26.2 Villager inventory、Menu、UseEntityCallback、client Screen 與 common/client 邊界規則。

### 查證與驗證

- 版本：Minecraft `26.2`；Fabric Loader `0.19.3`；Fabric API `0.158.0+26.2`；Loom `1.17.19`；Gradle `9.5.1`；Java `25`。
- Minecraft 26.2 common jar `minecraft-common-043a8b3edf-26.2.jar` javap/source 已確認：`AbstractVillager.getInventory()`、原版 `SimpleContainer(8)`、`Player.openMenu(MenuProvider)`、`SimpleMenuProvider`、`AbstractContainerMenu`、`Slot`、`FeatureFlags.DEFAULT_FLAGS` 與 `MenuType` constructor。
- Minecraft 26.2 clientOnly jar 已確認：`MenuScreens.register(...)`、`AbstractContainerScreen`、`GuiGraphicsExtractor.fill(...)` 與 `GuiGraphicsExtractor.text(...)`。
- Fabric API `fabric-events-interaction-v0-5.2.7+515ac5339e.jar` javap 已確認：`UseEntityCallback.interact(Player, Level, InteractionHand, Entity, EntityHitResult)`。
- 命令：`gradlew.bat --no-daemon compileJava compileClientJava --console=plain`；結果：`BUILD SUCCESSFUL`。
- 命令：`gradlew.bat --no-daemon build --console=plain`；結果：`BUILD SUCCESSFUL`，包含 test、check、jar、sourcesJar 與 client classes。
- 命令：`gradlew.bat --no-daemon runClient --console=plain`；結果：成功載入 Minecraft 26.2、Fabric、既有 Villager renderer Mixin、Indigo renderer、Menu registry 與 texture atlas，未出現新的 Mixin／Menu／Screen exception。開發版 client 已在初始化驗證後停止。
- 官方背景來源：[Fabric Events 26.2](https://docs.fabricmc.net/develop/events)、[Fabric Custom Screens 26.2](https://docs.fabricmc.net/develop/rendering/gui/custom-screens)。

### 未完成與風險

- 尚未完成玩家在實際世界中右鍵已指派村民並拖曽物品進出 8 格背包的手動驗收；這需要啟動 client、載入既有世界並實際互動。
- 第一版使用原版 villager inventory 作為 Civitas 背包資料來源，尚未加入額外的專用 backpack inventory、欄位擴充、職業限制、物品過濾或 worker logistics policy。
- GUI 目前使用 client-side 基本面板與槽位繪製，功能優先於最終美術；後續可改成 Civitas 原創 GUI 紋理。
- `runClient` log 中的 Realms SignedJWT／authorization 訊息屬開發環境登入服務問題，與本模組 Menu 或 Mixin 無關。

### 下一步

啟動遊戲測試：確認未指派村民仍可正常交易；確認已指派且有效的 Civitas 村民右鍵會開啟 `|創世紀元| : 村民背包`；確認 8 格 inventory 可放入、取出、shift-click 並在重開世界後保留。手動驗收完成後，再設計專用背包資料與下一個村民建築職能。

### 來源

- [Fabric Events 26.2](https://docs.fabricmc.net/develop/events)
- [Fabric Custom Screens 26.2](https://docs.fabricmc.net/develop/rendering/gui/custom-screens)


## 2026-08-20 — Warehouse Marker 合成配方固定與資料生成

### 設計決策

依照目前設計，`warehouse_marker` 的配方固定為九宮格 shaped recipe：第一列、第三列與第二列左右兩格使用儲物箱，中央使用鐵錠。配方圖樣為 `儲物箱／儲物箱／儲物箱`、`儲物箱／鐵錠／儲物箱`、`儲物箱／儲物箱／儲物箱`。這個 marker 是倉庫領地、ItemFrame 啟用、箱子掃描、村民指派與物流功能的正式核心標誌，產出仍維持單個物品且 stack size 為 1。

### 變更

新增 Fabric 26.2 client-side `FabricRecipeProvider`，使用官方 datagen API 產生 shaped crafting recipe 與 recipe advancement。玩家取得儲物箱後即可解鎖 warehouse marker 配方；配方類別使用 `RecipeCategory.MISC`，不修改既有 warehouse territory、ItemFrame、建築掃描或村民背包行為。

### 影響檔案

- `src/client/java/com/civilizationmod/client/CivilizationModRecipeProvider.java`：新增 shaped recipe provider，使用 `ccc/cic/ccc` pattern、`Items.CHEST` 與 `Items.IRON_INGOT`，輸出 `CivilizationItems.WAREHOUSE_MARKER`。
- `src/client/java/com/civilizationmod/client/CivilizationModDataGenerator.java`：註冊 recipe provider。
- `src/main/generated/data/civilizationmod/recipe/warehouse_marker.json`：datagen 產生的配方 JSON。
- `src/main/generated/data/civilizationmod/advancement/recipes/misc/warehouse_marker.json`：datagen 產生的解鎖 advancement。
- `Mods.md`：追加本次配方與驗證記錄。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：追加 Fabric 26.2 shaped recipe datagen 查證規則；全域技能副本同步更新。

### API 查證

Fabric 官方 Recipe Generation 26.2 文件確認 recipe provider 應繼承 `FabricRecipeProvider`，覆寫 `createRecipeProvider(HolderLookup.Provider, RecipeOutput)`，於 `RecipeProvider.buildRecipes()` 使用 `shaped(...)`、`pattern(...)`、`define(...)`、`unlockedBy(...)` 與 `save(output)`；datagen entrypoint 使用 `fabricDataGenerator.createPack().addProvider(ProviderClass::new)` 註冊。沒有猜測舊版、Forge 或 NeoForge recipe API。

來源：[Fabric Recipe Generation 26.2](https://docs.fabricmc.net/develop/data-generation/recipes)，查證日期 2026-08-20。

### 查證與驗證

| 命令／檢查 | 結果 |
|---|---|
| `gradlew.bat --no-daemon runDatagen --console=plain` | `BUILD SUCCESSFUL in 36s`；datagen 顯示新增 2 個檔案、移除 0 個 stale 檔案。 |
| 讀取 `src/main/generated/data/civilizationmod/recipe/warehouse_marker.json` | 確認 pattern 為 `ccc`、`cic`、`ccc`，`c` 為 `minecraft:chest`，`i` 為 `minecraft:iron_ingot`，輸出為 `civilizationmod:warehouse_marker`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL in 12s`；compileJava、compileClientJava、test、check、jar、sourcesJar 與 build 均通過。 |
| `jar tf build/libs/*.jar` | 確認最終 jar 包含 `data/civilizationmod/recipe/warehouse_marker.json` 與對應 advancement。 |

### 未完成與風險

尚未在遊戲內開啟生存模式配方書，實際點擊配方並消耗八個儲物箱與一個鐵錠完成合成；目前已完成資料生成、JSON 內容、最終 jar 包含檢查與完整 build。因為輸入配方使用原版 `minecraft:chest`，不接受其他模組自訂箱子，這是目前刻意維持的明確規則。

### 下一步

重新啟動 client，進入生存世界取得或使用儲物箱，確認配方書顯示 warehouse marker，完成一次實際合成。通過後，下一個高價值切片不是再調整配方，而是驗收完整閉環：合成 marker → 兩點選取 warehouse territory → 蹲下右鍵空 ItemFrame 快速部署 → `/civitas building scan` → 指派村民 → 開啟 8 格 Civitas 背包 → 讓村民將允許物品入庫。


## 2026-08-20 — Civitas 村民背包改為 27 格與原版三列容器座標

### 使用者需求

使用者回報第一版 8 格 backpack GUI 沒有對齊，要求改成一排 9 個、共三列，也允許套用原版 GUI。這次將 Civitas backpack 正式改為 27 格（9×3），並沿用 Minecraft 26.2 `ChestMenu` 的三列 slot 幾何；玩家 inventory 保持原版 3×9 加 hotbar 排列。

### 設計決策

不改動原版 `AbstractVillager.getInventory()` 的 8 格交易 inventory，避免破壞原版村民交易與 vanilla entity 行為。Civitas backpack 改成只附加在原版 `Villager` 上的獨立 27 格 persistent inventory；只有有效 Civitas resident 能透過模組 GUI 使用。未指派村民仍保留原版交易，失效 building 的 resident 也不能繼續使用 Civitas backpack。

27 格背包資料由 common Mixin 注入 `AbstractVillager` 的 entity save/load lifecycle，但以 `instanceof Villager` 限制實際作用範圍，不影響 Wandering Trader 等其他 `AbstractVillager`。資料使用 `CivitasBackpack` 子節點保存，透過 26.2 `ValueOutput.child(String)`、`ValueInput.childOrEmpty(String)` 與 `ContainerHelper.saveAllItems/loadAllItems` 讀寫。warehouse logistics 的來源也由原版 8 格 `villager.getInventory()` 改為 27 格 Civitas backpack，讓玩家放入 GUI 的物品真正能被倉庫入庫流程處理。

### GUI 變更

`CivitasVillagerBackpackMenu` 改為繼承 26.2 `ChestMenu`，使用 public constructor `ChestMenu(MenuType<?>, int, Inventory, Container, int)` 建立三列 27 槽 menu。由於 client-only `ContainerScreen` 固定綁定 `ChestMenu` generic，直接把自訂 `CivitasVillagerBackpackMenu` screen 套成 `ContainerScreen` 會造成 `MenuScreens.register` generic inference 失敗；因此保留 `AbstractContainerScreen<CivitasVillagerBackpackMenu>`，改用已查證的 vanilla container slot geometry 手繪背景。

目前相對於 screen 左上角的座標為：container slot 背景 `(7,17)` 起，每格 18 px；玩家 inventory 第一列 `(7,85)` 起，hotbar y=`143`；screen size 為 176×168；玩家 inventory label y=`74`。這會產生一個對齊的 9×3 Civitas 區域、玩家 3×9 inventory 與 9 格 hotbar，不再是原先單排 8 格。

### 影響檔案

- `src/main/java/com/civilizationmod/CivitasVillagerBackpackHolder.java`：新增 27 格 holder contract。
- `src/main/java/com/civilizationmod/mixin/CivitasVillagerBackpackMixin.java`：新增 Villager-only backpack 欄位與 ValueInput/ValueOutput persistence。
- `src/main/resources/civilizationmod.mixins.json`：註冊 common backpack persistence Mixin。
- `src/main/java/com/civilizationmod/CivitasVillagerBackpackContainer.java`：由 8 格 vanilla adapter 改為 27 格 holder-backed Container。
- `src/main/java/com/civilizationmod/CivitasVillagerBackpackMenu.java`：改用 `ChestMenu` 27-slot／3-row constructor 與既有 server validity check。
- `src/client/java/com/civilizationmod/client/CivitasVillagerBackpackScreen.java`：改為 9×3 vanilla container geometry，修正玩家 inventory label 與所有槽位位置。
- `src/main/java/com/civilizationmod/BuildingLogisticsService.java`：物流來源改為 27 格 Civitas backpack。
- `skills/minecraft-civilization-fabric-262/SKILL.md` 與全域技能：追加 27 格 backpack、ChestMenu、ContainerScreen generic 邊界、ValueOutput/ContainerHelper API 查證。
- `Mods.md`：追加本次修正與驗證結果。

### 26.2 API 查證

以專案實際 Minecraft 26.2 common/client deobf jars javap 查證：

| API | 查證結果 |
|---|---|
| `ChestMenu(MenuType<?>, int, Inventory, Container, int)` | public constructor 存在，可用自訂 27 格 Container 與 3 rows。 |
| `ContainerScreen` | 26.2 client-only 存在，但 generic 固定為 `ChestMenu`；不可直接套到本專案自訂 menu 的 `MenuScreens.register` 泛型。 |
| `ValueOutput.child(String)` | 存在，可建立獨立 `CivitasBackpack` save 子節點。 |
| `ValueInput.childOrEmpty(String)` | 存在，可在舊村民沒有 backpack 資料時提供空輸入。 |
| `ContainerHelper.saveAllItems/loadAllItems` | 存在，支援 `NonNullList<ItemStack>` 的 entity inventory 保存與載入。 |
| `AbstractVillager.addAdditionalSaveData/readAdditionalSaveData` | 26.2 方法使用 `ValueOutput`／`ValueInput`，可由 common Mixin 在尾端追加 Civitas backpack。 |

官方背景來源：[Fabric Custom Screens 26.2](https://docs.fabricmc.net/develop/rendering/gui/custom-screens)，實際 signature 以本專案 Minecraft 26.2 deobf jar 查證為準。

### 驗證結果

| 命令／檢查 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | 初次因直接套用 `ContainerScreen` 造成 `MenuScreens.register` generic inference 失敗；改回自訂 `AbstractContainerScreen<CivitasVillagerBackpackMenu>` 後 `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL in 15s`；compile、test、check、jar、sourcesJar 通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | `BUILD SUCCESSFUL in 47s`；成功通過 Minecraft 26.2 載入、Civitas common initializer、Mixin、Render thread、Indigo renderer 與 texture atlas，未出現新的 backpack、Menu、Mixin 或渲染例外。 |
| `run/logs/latest.log` | 只看到既有 Realms／401／SignedJWT 開發登入訊息，沒有新的 backpack 或渲染錯誤。 |

### 尚未完成與風險

尚未能由自動化工作階段替玩家實際操作 client GUI，因此「畫面視覺已完全符合使用者截圖」、「拖曽物品」、「重開世界後 27 格內容保留」仍需遊戲內手動驗收。這次已完成程式、編譯、jar 與 client runtime 驗證，但不能把 smoke test 誤稱為完整遊戲內閉環驗收。

完整 warehouse 閉環目前仍有一個設計上的測試前提：物流採用上一次成功 warehouse storage scan 的物品 ID allowlist。測試入庫前必須先在 warehouse 箱子內放入欲測試的物品並執行 `/civitas building scan`；否則物流會安全地不搬運任何未被玩家明確觀測過的物品。

### 下一步與遊戲內驗收

1. 先以生存世界的真實配方合成 warehouse marker；若要排除配方因素，可另外使用 `/civitas building generate` 建立 deterministic test building。
2. 用 warehouse marker 左鍵選取 point A，再以同一個仍完整的 marker 右鍵選取 point B；確認 tooltip 顯示兩點與 configured territory。
3. 在領地內準備普通單箱或雙箱，先放入至少一種預定入庫物品，再蹲下右鍵空 ItemFrame，確認 marker 被消耗並轉移到 ItemFrame。
4. 執行 `/civitas building scan`，再執行 `/civitas building list` 與 `/civitas building inspect <index>`；確認建築為 valid、storage 已掃描、箱子數與物品摘要正確。
5. 對著要指派的原版村民執行 `/civitas assign <index>`；確認村民穿上 warehouse role overlay 並開始走向 marker。
6. 右鍵已指派且有效的村民，確認畫面為 27 格、9×3 的 Civitas backpack，而不是原版交易；放入已在 storage snapshot 出現的物品，關閉 GUI，等待村民距離 marker 小於 8 格後觀察箱子數量增加。
7. 重新開啟 backpack，確認已入庫物品不在背包內；重開世界後再次開啟，確認剩餘背包物品仍存在。再對未指派村民右鍵，確認仍是原版交易。
8. 拆除 ItemFrame，等待約 1 秒或再次執行 building scan，確認 warehouse observation 被清理但領地箱子與物品完全保留。

若手動驗收第 6 步沒有入庫，依序提供 `building inspect` 輸出、村民是否已抵達 marker、箱子內物品是否在最新 snapshot，以及 `run/logs/latest.log`；不要先重新調整 GUI 或 allowlist。


## 2026-08-20 — 最終改用原版 ContainerScreen 背景

### 修正

在 27 格 backpack 改版後，進一步確認 Minecraft 26.2 client-only jar 實際提供的是 `ContainerScreen`，而不是可直接沿用舊版寫法的獨立泛型 `ChestScreen`。由於 `CivitasVillagerBackpackMenu` 已繼承 `ChestMenu` 並使用 `ChestMenu(MenuType<?>, int, Inventory, Container, int)` 三列 constructor，因此最後將 `CivitasVillagerBackpackScreen` 改為真正的 `ContainerScreen`，直接套用原版 container 背景 texture、原版槽位繪製與原版 176×168 三列容器布局。

這次不再使用上一筆中的手繪灰色背景作為正式畫面。只保留覆寫 label，將玩家物品欄文字改成品牌化的 `CivilizationMessages.translatable(...)`；村民 backpack 標題仍由 server-side `SimpleMenuProvider` 傳入並維持三份 locale。

### 影響檔案

- `src/client/java/com/civilizationmod/client/CivitasVillagerBackpackScreen.java`：改用 26.2 `ContainerScreen` 原版 GUI。
- `src/main/java/com/civilizationmod/CivitasVillagerBackpackMenu.java`：維持 `ChestMenu` 子類與三列 27 槽設定，使 `MenuScreens.register` generic 相容。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：修正 ContainerScreen generic 查證結論。
- `skills/minecraft-civilization-fabric-262/SKILL.md` 與全域技能副本：同步最終 GUI／persistence API 規則。
- `Mods.md`：追加最終修正與驗證。

### 驗證結果

| 命令／檢查 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` | `BUILD SUCCESSFUL in 16s`；確認 `ContainerScreen` constructor、MenuScreens generic、27 格 menu 與 Mixin 可編譯。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL in 14s`；test、check、jar、sourcesJar 與 build 通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | `BUILD SUCCESSFUL in 1m 29s`；成功通過 Minecraft 26.2、Civitas initializer、common backpack Mixin、Render thread、Indigo renderer 與 texture atlas 初始化。 |
| `run/logs/latest.log` | 未出現新的 backpack、Menu、Mixin、ClassCast 或渲染例外；Realms／401／SignedJWT 仍是開發登入服務訊息。 |

### 完整 warehouse 閉環狀態

程式閉環已接通：warehouse marker recipe → territory CustomData → ItemFrame deployment → building scan／storage snapshot → `/civitas assign` → 27 格 Civitas backpack → 村民到達 marker → `BuildingLogisticsService` 從 27 格 backpack 依 storage snapshot allowlist 入庫。物流仍每 20 server ticks 檢查一次，村民距離 marker 超過 8 格時先導航，不在抵達前搬運。

尚未完成的是使用者在實際 Minecraft 世界中的手動驗收，不能把 build 或 runClient smoke test 當成已完成遊戲內閉環。手動測試必須先在 warehouse 箱子內放入欲測試物品並執行 `/civitas building scan`，因為第一版物流只允許上一次成功 storage snapshot 已觀測過的物品 ID；接著將同一物品放進 27 格 backpack，等村民抵達 marker，再以 `/civitas building inspect <index>` 與箱子數量確認入庫。


## 2026-08-20 — Civitas backpack 標題回到原版位置

### 問題根因

使用者回報 GUI 背包格子已經接近原版，但 `|創世紀元| : 村民背包` 與 `|創世紀元| : 玩家物品欄` 被畫到 GUI 外側。原因不是 `ContainerScreen` 的 image size 或 slot layout，而是自訂 `extractLabels(...)` 把原本應使用的局部 label 座標再次加上 `leftPos`／`topPos`，造成標題被重複平移。

Minecraft 26.2 `AbstractContainerScreen` 的 label 欄位為 `titleLabelX`、`titleLabelY`、`inventoryLabelX`、`inventoryLabelY`；constructor 預設值是 title `(8,6)`、inventory label x=`8`，三列容器的 inventory label y=`72`。這些值供 `extractLabels` 使用時是 screen 局部座標，不得再加上 `leftPos` 或 `topPos`。

### 修正

`CivitasVillagerBackpackScreen.extractLabels(...)` 現在改為：

```text
title:     this.titleLabelX,     this.titleLabelY
inventory: this.inventoryLabelX, this.inventoryLabelY
```

標題內容與玩家物品欄文字仍然保留 Civitas 品牌化與本地化；只有位置改回原版 `AbstractContainerScreen` layout。這樣 GUI scale、解析度與不同 screen left/top position 不會造成額外偏移。

### 影響檔案

- `src/client/java/com/civilizationmod/client/CivitasVillagerBackpackScreen.java`：移除 `leftPos/topPos` 額外偏移，改用 26.2 protected label fields。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：追加 title label 局部座標查證。
- 全域 `minecraft-civilization-fabric-262` skill：同步追加同一查證規則。
- `Mods.md`：追加本次根因、修正與驗證。

### API 查證

以 Minecraft 26.2 client-only deobf jar `javap` 重新查證 `AbstractContainerScreen`：

| 項目 | 查證結果 |
|---|---|
| `titleLabelX`／`titleLabelY` | protected 欄位，constructor 預設 title `(8,6)`。 |
| `inventoryLabelX`／`inventoryLabelY` | protected 欄位，x=`8`；三列 container inventory label y=`72`。 |
| `extractLabels(...)` 座標語意 | 使用 screen 局部座標，不能自行再加 `leftPos/topPos`。 |
| `ContainerScreen` | 仍由 `ChestMenu` subclass 提供原版背景與 slot geometry；本次不改動 Menu 或 27 格 persistence。 |

查證依據為實際 Minecraft 26.2 client-only deobf bytecode 與專案暫存 `backpack-client-api.txt`，不是舊版或其他模組 API。

### 驗證結果

| 命令 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileClientJava --console=plain` | `BUILD SUCCESSFUL in 10s`。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL in 8s`；test、check、jar、sourcesJar 通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 成功通過 client initializer、Render thread、Indigo renderer、texture atlas 與 Minecraft 26.2 啟動階段；工作階段後由開發環境停止。沒有新的 Menu、Mixin、ClassCast 或 rendering exception。 |
| latest log | 仍有既有的 Realms／401／SignedJWT 與 run directory 清理訊息；與本次 title label 修正無關。 |

### 尚未完成

目前已由 bytecode、編譯與 client smoke test 確認修正方向正確，但仍需要使用者在遊戲內重新右鍵已指派村民，確認兩個標題實際顯示在原版 GUI 的左上相對位置，而不是只依賴啟動測試。

### 下一步

重新啟動 client，右鍵已指派且有效的 warehouse 村民。預期 `|創世紀元| : 村民背包` 位於 GUI 左上標題位置，`|創世紀元| : 玩家物品欄` 位於玩家 inventory 區域上方，兩者都不應出現在 GUI 外側。

## 2026-08-21 00:10 — 新增 Cursor 專案 skill `civitas-fabric-262`

### 變更

依桌面 `minecraft-civilization-fabric-262.skill` 與「Civitas 開發規則（for Cursor Agents）」寫一份 Cursor Agent Skill，放在專案 `.cursor/skills/`，讓 Cursor 能自動發現。主檔只保留硬規則、Cursor 工具流程與 `Mods.md` 格式；長篇已查證 API 與外部模組對照改為按任務讀取的 reference。沒有修改 Java、Gradle 或遊戲資產。

### 影響檔案

- `.cursor/skills/civitas-fabric-262/SKILL.md`：Cursor 專用 skill 主檔。
- `.cursor/skills/civitas-fabric-262/verified-apis.md`：從既有 skill 抽出的已查證 API 與切片規則。
- `.cursor/skills/civitas-fabric-262/building-geometry-26.2.md`：建築幾何 reference 副本。
- `.cursor/skills/civitas-fabric-262/tektopia-structure-detection.md`：TekTopia 對照副本。
- `.cursor/skills/civitas-fabric-262/minecolonies-reference.md`：MineColonies 對照副本。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：既有 Claude／Codex skill，本次未改內容。

### 查證與驗證

- 版本：Minecraft=26.2；Loader=0.19.3；API=0.158.0+26.2；Loom=1.17-SNAPSHOT；Java=25
- 命令：未執行 compile／build（僅新增 Markdown skill）
- 結果：未執行

### 未完成與風險

- Cursor 是否自動套用取決於 Agent Skills 發現；若下一輪對話沒載入，需在對話中點名 `civitas-fabric-262`。
- 桌面 `.skill` 是較短的來源包（無 references 檔）；長篇查證來自專案內既有 skill 副本。

### 下一步

開發本模組時確認 Cursor 有套用 `.cursor/skills/civitas-fabric-262`；若規則有變，同步更新本 skill 與 `skills/minecraft-civilization-fabric-262/SKILL.md`。

## 2026-08-21 00:15 — 查證知識必須寫回 Cursor skill

### 變更

把「每次查證完要把新知識放進 skill，之後先讀 skill 再判斷」寫成強制流程。Cursor skill 新增判斷捷徑表與寫回格式；`verified-apis.md` 註明追加不覆寫；`AGENTS.md` 工作流與交付檢查同步；Codex skill 加簡短寫回規則。沒有修改 Java。

### 影響檔案

- `.cursor/skills/civitas-fabric-262/SKILL.md`：判斷捷徑、查證後寫回、工作流步驟。
- `.cursor/skills/civitas-fabric-262/verified-apis.md`：寫回提醒與既有主題索引。
- `AGENTS.md`：修改順序、技能路徑、交付檢查。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：查證後寫回短規則。
- `Mods.md`：本筆記錄。

### 查證與驗證

- 版本：Minecraft=26.2；Loader=0.19.3；API=0.158.0+26.2；Loom=1.17-SNAPSHOT；Java=25
- 命令：未執行 compile／build（僅流程與文件）
- 結果：未執行

### 未完成與風險

- 舊的長篇查證仍在 `verified-apis.md`；判斷捷徑只收最常用結論，細節仍要按任務讀 reference。
- Agent 若沒載入 skill，仍可能重查；需靠 `AGENTS.md` 與對話點名補強。

### 下一步

之後每次 javap／官方文件有新結論，用 skill 內模板追加一筆，並同步兩邊 skill。


## 2026-08-21 — Residential Marker 多容量住宅第一切片

### 設計決策

依照殖民地建設主線，新增四種容量明確的住宅標誌：`residential_marker_1`、`residential_marker_2`、`residential_marker_4` 與 `residential_marker_6`，分別要求領地內至少 1、2、4、6 張原版床。四種標誌共用既有兩點 territory 選取、ItemFrame 快速部署、同領地只能啟用一個有效 marker、附魔光效與 server-side scan 流程；容量由 marker registry 的 `MarkerDefinition` 保存。

市政廳已保留為後續殖民地核心建築，不在本次住宅切片註冊或實作。住宅第一版只負責 marker、領地、床位容量、Saved Data 觀測與沿用既有村民指派／導航，不加入多居民佔用、床位分配、食物消耗、工作站或市政廳權限，以避免一次跨越多個垂直切片。

住宅 marker 的合成配方使用實際放入的床數表達容量：1 床、2 床、4 床與 6 床配方各使用對應數量的 `ItemTags.BEDS`，並以鐵錠作為控制核心。四種 item 第一版沿用既有 warehouse marker 紋理，容量與用途由 item 名稱及 tooltip 清楚區分；專用住宅圖像資產列為後續視覺改善，不影響 server 功能。

### 變更

- 新增 `BuildingFunction.RESIDENCE`。
- 新增 `CivitasBuildingMarkerItem`，將兩點領地選取、tooltip、切換快捷欄取消 pending selection 與通用訊息抽離為所有建築標誌共用的 common-side 行為。
- `WarehouseMarkerItem` 改為共用 marker item 子類；`ResidentialMarkerItem` 使用相同領地資料契約。
- `CivilizationItems` 註冊四個住宅 marker item 與 registry key。
- `BuildingMarkerRegistry` 改為保存 marker function 與住宅容量。
- `WarehouseMarkerQuickDeploy` 泛化為接受所有已知 Civitas building marker，保留完整 stack、領地維度、範圍、牆面附著與 duplicate territory server 驗證。
- 新增 `ResidenceValidator`，以 `WarehouseTerritoryValidator` 驗證領地與 ItemFrame，再使用 `BlockTags.BEDS`、`BedBlock.PART` 與 `BedPart.FOOT` 在領地內每張床只計一次；床位不足時使用 `insufficient_beds`。
- `BuildingObservation` 新增 optional `capacity` 與 `bed_count` 保存欄位；新增欄位放入 nested `CodecTail`，避免超過 26.2 `RecordCodecBuilder` group 欄位上限，舊 warehouse observation 的預設值為 0。
- `CivilizationWorldData.scanBuildingMarkers` 加入 residence validation、容量／床數保存，並保持 warehouse-only storage snapshot 分支不受影響。
- `BuildingGeometryValidator` 的 debug／直接驗證路徑加入 residence territory 與床位結果。
- `/civitas building list` 與 `/civitas building inspect` 的 building entry 加入住宅資料；住宅 function 與床位不足原因已本地化。
- 新增 1、2、4、6 床 marker 的 item model、26.2 client item definition 與三份 locale 名稱／tooltip／建築訊息。
- `CivilizationModRecipeProvider` 新增四個 shaped recipe 與 recipe advancement。

### 影響檔案

- `src/main/java/com/civilizationmod/CivitasBuildingMarkerItem.java`
- `src/main/java/com/civilizationmod/WarehouseMarkerItem.java`
- `src/main/java/com/civilizationmod/ResidentialMarkerItem.java`
- `src/main/java/com/civilizationmod/CivilizationItems.java`
- `src/main/java/com/civilizationmod/BuildingFunction.java`
- `src/main/java/com/civilizationmod/BuildingMarkerRegistry.java`
- `src/main/java/com/civilizationmod/WarehouseMarkerQuickDeploy.java`
- `src/main/java/com/civilizationmod/ResidenceValidator.java`
- `src/main/java/com/civilizationmod/BuildingObservation.java`
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`
- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`
- `src/main/java/com/civilizationmod/CivilizationCommands.java`
- `src/client/java/com/civilizationmod/client/CivilizationModRecipeProvider.java`
- `src/main/resources/assets/civilizationmod/lang/en_us.json`
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_1.json`
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_2.json`
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_4.json`
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_6.json`
- `src/main/resources/assets/civilizationmod/items/residential_marker_1.json`
- `src/main/resources/assets/civilizationmod/items/residential_marker_2.json`
- `src/main/resources/assets/civilizationmod/items/residential_marker_4.json`
- `src/main/resources/assets/civilizationmod/items/residential_marker_6.json`
- `src/main/generated/data/civilizationmod/recipe/residential_marker_1.json`
- `src/main/generated/data/civilizationmod/recipe/residential_marker_2.json`
- `src/main/generated/data/civilizationmod/recipe/residential_marker_4.json`
- `src/main/generated/data/civilizationmod/recipe/residential_marker_6.json`
- `skills/minecraft-civilization-fabric-262/SKILL.md`
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`
- `Mods.md`

### API 查證與驗證

- Fabric 官方 Creating Your First Item 26.2 確認 `ResourceKey<Item>`、`Registries.ITEM`、`Registry.register`、`Item.Properties.setId` 與 item model／client item definition 流程：https://docs.fabricmc.net/develop/items/first-item
- Minecraft Java Edition 26.2 官方版本說明確認本專案目標版本與 bed 的 26.2 行為背景：https://www.minecraft.net/fi-fi/article/minecraft-java-edition-26-2
- Minecraft 26.2 Snapshot 3 官方技術變更確認 bed block entity 已移除，床改用 block models；住宅掃描因此採 block state，不使用舊版 bed entity／NBT：https://www.minecraft.net/en-us/article/minecraft-26-2-snapshot-3
- 本機 Fabric／Minecraft 26.2 實際編譯確認彩色床不能使用 `Items.BED` 作為單一 `Item`；recipe ingredient 與 unlock criterion 改用 `ItemTags.BEDS` 後成功。
- 本機 Minecraft 26.2 common jar 透過 compile 驗證 `BlockTags.BEDS`、`BedBlock.PART` 與 `BedPart.FOOT` 可用。
- `BuildingObservation` 初次新增欄位時觸發實際 `RecordCodecBuilder.group` 欄位數錯誤；將 `capacity`、`bed_count` 移入 nested `CodecTail` 後成功，該結論已同步寫入全域與專案 skill。

### 查證與驗證結果

| 命令 | 結果 |
|---|---|
| `gradlew.bat --no-daemon compileJava --console=plain` | 初次因 Codec group 欄位過多失敗；CodecTail 修正後 `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon compileClientJava --console=plain` | 初次因 `Items.BED` 是 26.2 `ColorCollection<Item>` 而非單一 Item 失敗；改用 `ItemTags.BEDS` 後 `BUILD SUCCESSFUL`。 |
| `gradlew.bat --no-daemon runDatagen --console=plain` | `BUILD SUCCESSFUL`；已產生四個住宅 recipe 與對應 recipe advancement。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`；compile、resources、client、test、check、jar、sourcesJar 均通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | 已通過 Fabric／Minecraft 26.2 載入、Civitas initializer、Render thread、resource reload、texture atlas 與既有 renderer；沒有住宅 marker、Mixin、Menu 或 rendering exception。完成 smoke test 後已停止 client。 |

### 未完成與風險

- 尚未由玩家在實際世界手動驗證四種住宅 marker 的工作台配方書顯示、ItemFrame 轉移與 `/civitas building scan` 的床位數結果。
- 住宅目前沿用單一 `residentUuid`，因此容量已能驗證與保存，但 2／4／6 床尚未允許多名村民同時入住；多居民 occupancy、床位分配與搬遷是後續切片。
- 四種住宅 marker 暫時共用 warehouse marker 紋理，後續可新增具有床數視覺差異的正式資產。
- `runClient` log 中的 Realms／Minecraft account `401`、SignedJWT 與 authorization 訊息屬開發環境登入服務問題，與住宅 marker 無關。
- 本次沒有實作市政廳；市政廳仍是下一個核心建築規劃，不得以住宅 marker 代替。

### 下一步

進入遊戲後先驗收住宅第一切片：合成四種 marker，使用其中一個 marker 選取 territory，在領地內放置對應數量床，蹲下右鍵空 ItemFrame 部署 marker，執行 `/civitas building scan` 與 `/civitas building inspect <編號>`，確認 `valid`、`床位／容量` 數值與村民指派導航。完成住宅單居民驗收後，再設計市政廳 marker；市政廳應負責殖民地建立、核心領地、居民上限與後續建築解鎖，而不是住宅的別名。


## 2026-08-21 — GitHub 公開 repository 上傳整理

### 變更

依使用者提供的 GitHub repository `https://github.com/kubryan/Civilization.git`，開始整理 `C:\Minecraft` 的公開上傳內容。原本專案資料夾尚未初始化 Git；本次初始化 local `main`、連結 `origin`、抓取 GitHub 既有 `main` 初始提交，並保留遠端原有的 MIT `LICENSE`。GitHub 原始 repository 只有 README 與 LICENSE，沒有需要合併的 Java、Gradle 或遊戲資產。

本次重新整理 README，加入 Civitas 專案定位、目前 warehouse／住宅功能、Minecraft 26.2 工具鏈、建置命令、專案規則與刻意排除的本機檔案說明；保留 AI 輔助開發免責聲明。`.gitignore` 新增 Minecraft runtime、local diagnostics、所有 API 查證暫存文字檔、log、`.tmp-*` 與根目錄空白 `end` 檔案的排除規則。

公開 repository 只保留可重現建置所需的原始碼、client/common 資源、生成 recipe、Gradle wrapper／設定、GitHub Actions build workflow、AGENTS.md、Cursor／專案 skills、Mods.md 與研究文件。`build/`、`run/`、`.gradle/`、`logs/`、`.vscode/`、`.tmp-*`、`*.txt`、`*.log`、本機診斷檔與空白探測檔不會上傳。

### 影響檔案

- `.gitignore`：新增本機執行資料、診斷輸出、API 暫存文字與空白探測檔排除規則。
- `README.md`：改為 Civitas 公開專案說明與建置指南。
- `LICENSE`：保留 GitHub 初始 repository 的 MIT License。
- `.git/`：初始化本機 Git，remote 為 `https://github.com/kubryan/Civilization.git`。
- `Mods.md`：追加本次上傳決策與安全檢查記錄。

### 公開檔案安全檢查

對 `src/`、`.github/`、`.cursor/`、`skills/`、`gradle/` 與根目錄公開設定檔執行 API key、password、secret、private key、GitHub token 等模式掃描，沒有發現匹配內容。候選公開檔案中沒有超過 2 MB 的原始碼、設定、技能或研究檔案；大型 Minecraft 執行與診斷資料均由 `.gitignore` 排除。

### 查證與驗證

- `git ls-remote --heads https://github.com/kubryan/Civilization.git`：確認遠端 `main` 存在。
- 暫時 clone 遠端 main：確認遠端只有初始 README 與 MIT LICENSE。
- `git init -b main`、`git remote add origin https://github.com/kubryan/Civilization.git`：成功。
- `git fetch origin main --no-tags`：成功。
- `git add --all` 後 staged 檔案數：113。
- staged 路徑檢查：沒有 `build/`、`run/`、`logs/`、`.gradle/`、`.tmp-*`、`*.txt`、`*.log` 或 `end`。
- 非忽略未追蹤檔案：沒有。
- 本次尚未執行 commit 或 push；完成前仍需檢查 staged diff，確認後再推送。

### 未完成與風險

GitHub repository 是公開 repository；未來新增文件、log、截圖或 API 查證資料時，必須先確認不含帳號 token、個人路徑、世界資料或其他敏感資訊。`.gitignore` 目前排除所有 `*.txt`，若日後有真正需要版本控制的純文字資料，必須使用 Markdown、JSON 或在例外規則中明確加入。

### 下一步

檢查 staged diff 與檔案統計，建立第一個完整 Civitas commit；確認 commit 不包含本機執行資料後，再推送到 `origin/main`，最後以遠端 log／tree 驗證上傳結果。


## 2026-08-21 — GitHub push 權限阻塞

### 結果

本機 Git repository 已建立 root commit `5ed3133`（`Add Civitas colony building systems`），共 113 個已檢查的公開檔案；working tree 在 push 前為乾淨狀態。推送到 `https://github.com/kubryan/Civilization.git` 的 `main` 時，GitHub 回傳：`Permission to kubryan/Civilization.git denied to KuBryan-614`，HTTP `403`。

### 判定

這不是檔案清單、Gradle、Minecraft 或 commit 內容錯誤，而是目前 Windows Git 認證使用者 `KuBryan-614` 沒有對 `kubryan/Civilization` 的寫入權限，或 repository 所屬帳號與目前 Credential Manager 的登入帳號不同。遠端公開讀取正常，`main` 分支存在且可 fetch；目前沒有任何 commit 成功推送到 GitHub。

### 下一步

需要使用者在 Windows Git Credential Manager 登入擁有 `kubryan/Civilization` 的 GitHub 帳號，或由 repository owner 將 `KuBryan-614` 加入具有寫入權限的 collaborator，之後從 `C:\Minecraft` 再執行 `git push -u origin main`。本機 commit 與排除規則已保留，不需要重新整理 113 個檔案。


## 2026-08-21 — README 公開專案重點與 AI 免責聲明更新

### 變更

依照使用者要求，將 README.md 開頭的免責聲明更新為完整版本：本專案絕大多數的程式碼與內容皆由 AI 輔助生成，雖已盡力確保功能與品質，但開發過程高度依賴 AI，並歡迎貢獻、錯誤回報與反饋。

README.md 同時重新整理為公開 repository 首頁，集中介紹 Civitas 的核心方向與目前成果，包括玩家宣告建築用途、warehouse territory、ItemFrame marker、村民指派、27 格村民背包、自動入庫物流、1／2／4／6 床住宅 marker、server-authoritative Saved Data、本地化與未來 Town Hall 市政廳路線。文件明確區分已完成、等待遊戲內驗收與尚未實作的功能，避免把規劃內容誤認為已完成能力。

### 影響檔案

- `README.md`：完整重寫公開專案說明與核心功能摘要。
- `Mods.md`：追加本次 README 修改記錄。

### 驗證與版本控制

README 修改為文件內容，沒有新增 Minecraft API 或 Java 程式碼，因此未重新執行 Gradle build。已確認檔案內容為有效 Markdown，並準備加入原有本機 commit；目前 GitHub push 仍等待使用者重新登入具有 repository 寫入權限的 GitHub 帳號。

### 未完成與下一步

- 本機 commit 尚未因本次 README 修改重新 amend。
- GitHub repository 仍未完成 push，原因是目前 Windows Git 認證帳號 `KuBryan-614` 收到 HTTP 403。
- 下一步將把 README 與本記錄加入本機 commit；使用者完成 Git Credential Manager 重新登入後，再執行 `git push -u origin main`。


## 2026-08-21 — 住宅 Marker 視覺資產與領地可視化預覽

### 變更

完成住宅 marker 的獨立視覺資產。新增四個原創 16×16 RGBA Minecraft 像素風格圖示：床的側視圖加上金色容量數字 `1`、`2`、`4`、`6`；住宅 marker 不再共用 warehouse marker 紋理。四個 item model 的 `layer0` 已分別指向對應的 residential marker texture。

完成建築領地選取預覽的第一版。新增 common/server-side `CivitasTerritoryPreviewService`，玩家手持任一已註冊 Civitas building marker 時，point A pending 狀態會顯示端點粒子；A/B 都完成後，以 `END_ROD` 粒子描繪正規化 bounds 的 12 條 cuboid 邊線，並以端點粒子標示 A/B。預覽每 5 server ticks 更新一次，每條邊最多 24 個 interval，預覽只發送給選取玩家。切換離開未完成 marker 時沿用既有 `inventoryTick` 清除 pending selection；完成領地仍保存，但不持有 marker 時不顯示。

本次已查證 `LevelRenderContext` 的實際 26.2 API，但沒有猜測 `VertexConsumer` 或 `LevelRenderer.renderLineBox`。`fabric-rendering-v1` `25.3.2+515ac5339e` source 確認 `LevelRenderContext` 繼承 `LevelTerrainRenderContext`，公開 `submitNodeCollector()` 與 `poseStack()`；`LevelRenderEvents.BeforeGizmos` callback 為 `void beforeGizmos(LevelRenderContext context)`。考量目前只需要選取者看見邊界，第一版採 server particle fallback，避免 dedicated server 載入 client-only renderer，也避免把未查證的線框繪製簽名寫入 common/client 程式。

### 影響檔案

- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_1.png`：新增 1 床住宅 marker 16×16 圖示。
- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_2.png`：新增 2 床住宅 marker 16×16 圖示。
- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_4.png`：新增 4 床住宅 marker 16×16 圖示。
- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_6.png`：新增 6 床住宅 marker 16×16 圖示。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_1.json`：指向 `residential_marker_1` texture。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_2.json`：指向 `residential_marker_2` texture。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_4.json`：指向 `residential_marker_4` texture。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_6.json`：指向 `residential_marker_6` texture。
- `src/main/java/com/civilizationmod/CivitasTerritoryPreviewService.java`：新增 server-side 粒子預覽服務。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：註冊領地預覽 server tick service。
- `src/main/java/com/civilizationmod/WarehouseTerritory.java`：新增 `selectedDimension(ItemStack)`，讓 preview 依 marker CustomData 維度過濾。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：同步新的 26.2 render context 與粒子 fallback 查證。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步新的 26.2 render context 與粒子 fallback 查證。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域可重用技能查證。
- `Mods.md`：追加本次歷史記錄。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| 版本 | Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Loom 實際 `1.17.19`、Gradle `9.5.1`、Java toolchain `25`。 |
| LevelRenderContext source | 通過；實際 `fabric-rendering-v1-25.3.2+515ac5339e-sources.jar` 確認 `submitNodeCollector()` 與 `poseStack()`。 |
| `gradlew.bat --no-daemon compileJava --console=plain` | `BUILD SUCCESSFUL`，確認 `CivitasTerritoryPreviewService`、`selectedDimension` 與 server particle API 可編譯。 |
| `gradlew.bat --no-daemon build --console=plain` | `BUILD SUCCESSFUL`，common、client、resources、jar、test 與 assemble 通過。 |
| `gradlew.bat --no-daemon clean build --console=plain` | `BUILD SUCCESSFUL`，清潔建置後完整通過。 |
| 資產檢查 | 通過；四個 PNG 均為 `16×16`，四個 JSON 的 `layer0` 均指向各自 `residential_marker_1/2/4/6`。 |
| `git diff --check` | 沒有 whitespace error；Git 只提示既有跨平台 LF/CRLF 轉換警告。 |
| `runClient` smoke test | 通過 Fabric/Minecraft 載入、client initialization、texture atlas 與 Render thread 初始化；因 `runClient` 是持續任務而主動停止，沒有進行遊戲內滑鼠操作。Realms authentication 訊息屬開發環境登入服務，非本模組錯誤。 |

### 未完成與風險

- 尚未由玩家在實際世界完成一次 A 點、B 點、切換快捷欄、放入 ItemFrame 與重新拿起 marker 的完整視覺驗收；目前已完成編譯與 client 啟動驗證。
- 本版使用 server 粒子預覽，不是 client 線框 renderer；粒子在距離過遠、粒子設定關閉或大型領地時的實際可讀性仍需遊戲內觀察。
- `LevelRenderContext` 的 `PoseStack` 與 `SubmitNodeCollector` 已查證，但若未來要改成真正線框，仍必須另行查證 26.2 collector、render pipeline 與 camera-relative 座標的完整簽名，不能直接移植舊版 `VertexConsumer` 寫法。
- GitHub push 仍未完成；目前 remote 的 HTTP 認證仍回報 `KuBryan-614` 沒有 `kubryan/Civilization` 寫入權限。

### 下一步

先在遊戲內驗收領地預覽：手持 `warehouse_marker` 或住宅 marker，左鍵選 A、右鍵選 B，確認 A 點粒子與完整 12 邊界線出現；切換快捷欄確認未完成選取取消；切回已完成 marker 確認領地預覽恢復。之後再處理真正 client gizmo 線框是否值得替換粒子方案，並等待使用者修正 GitHub Credential Manager 後再執行 `git push -u origin main`。

### 來源

- [1] [Fabric Events 26.2](https://docs.fabricmc.net/develop/events)
- [2] [Fabric Rendering API](https://docs.fabricmc.net/develop/rendering)
- [3] 本機 Fabric API `fabric-rendering-v1-25.3.2+515ac5339e-sources.jar` 的 `LevelRenderContext.java`；查證日期 2026-08-21。


## 2026-08-21 — GitHub main non-fast-forward 整合與成功推送

### 變更

使用者執行 `git push -u origin main` 時收到 `non-fast-forward`。經確認，GitHub repository 的 `main` 先前只有獨立建立的 `Initial commit`，而本機則已有 Civitas 開發提交；兩邊都各自新增過 README，因此直接推送無法套用本機歷史。

本次未使用 `force push`。執行 `git pull --rebase origin main`，rebase 在 `README.md` 發生 add/add 衝突；已保留本機完整 Civitas README、模組功能說明、AI 免責聲明與開發規範，並正常完成 rebase。原本的本機提交重新產生為 `84db515 Add Civitas colony building systems` 與 `0f96eee Add residential marker art and territory preview`。

### 影響檔案

- `README.md`：解決遠端初始 README 與本機 Civitas README 的 add/add 衝突，保留完整本機內容。
- `Mods.md`：記錄本次 Git 歷史整合與推送結果。
- Git history：本機提交因 rebase 產生新的 commit SHA，但內容保留；遠端 `main` 已接上完整 Civitas 歷史。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| `git fetch origin main` | 成功取得遠端 `main`；確認 repository 為 `https://github.com/kubryan/Civilization.git`。 |
| `git pull --rebase origin main` | README add/add 衝突已解決，rebase 成功完成。 |
| `git push -u origin main` | 成功；GitHub 回報 `3dfd257..0f96eee main -> main`。 |
| `git ls-remote origin refs/heads/main` | 回報 `0f96eeecb334dfab9c1359e122501b6d05308631`。 |
| `git status --short --branch` | 工作樹乾淨，`main...origin/main`，本機與遠端同步。 |
| `git diff --check` | 通過，沒有 whitespace error。 |
| force push | 未執行，遠端歷史未被覆蓋。 |

### 未完成與風險

目前 GitHub `main` 已完成同步。Git rebase 改寫了本機先前兩個非遠端 commit 的 SHA，因此後續若其他本機 clone 仍使用舊的 `ef0d7b4` 或 `e9917ec`，需要重新 fetch 並以新的 `84db515`、`0f96eee` 歷史為準；本專案目前工作樹與 `origin/main` 已一致。

### 下一步

回到遊戲內驗收住宅 marker 的床位容量與領地粒子邊界；若未來繼續開發，先以目前已推送的 `main` 為基準建立下一個功能提交，不要重新使用舊的未 rebase commit SHA。


## 2026-08-21 — 住宅 Marker 階梯式配方修正

### 變更

依照新的殖民地建設設計，住宅 marker 配方已由原本的床與鐵錠配方改為階梯式升級配方。`residential_marker_1` 現在使用 3×3 shaped recipe：八個 `ItemTags.WOOL` 羊毛包圍中央鐵錠；`residential_marker_2` 使用 2 個 `residential_marker_1` 的 shapeless recipe；`residential_marker_4` 使用 4 個 `residential_marker_1` 的 shapeless recipe；`residential_marker_6` 使用 6 個 `residential_marker_1` 的 shapeless recipe。

解鎖條件也一併修正：1 床住宅 marker 以持有任一羊毛作為解鎖條件；2、4、6 床住宅 marker 以持有 `residential_marker_1` 作為解鎖條件。這樣住宅系統會先讓玩家取得基礎住宅標誌，再透過多個基礎標誌合成更高容量的住宅標誌，且配方不再依賴床物品。

### 影響檔案

- `src/client/java/com/civilizationmod/client/CivilizationModRecipeProvider.java`：修改住宅 marker 的 shaped／shapeless datagen 定義與解鎖條件。
- `src/main/generated/data/civilizationmod/recipe/residential_marker_1.json`：生成羊毛與鐵錠的 3×3 shaped recipe。
- `src/main/generated/data/civilizationmod/recipe/residential_marker_2.json`：生成 2 個 1 床 marker 的 shapeless recipe。
- `src/main/generated/data/civilizationmod/recipe/residential_marker_4.json`：生成 4 個 1 床 marker 的 shapeless recipe。
- `src/main/generated/data/civilizationmod/recipe/residential_marker_6.json`：生成 6 個 1 床 marker 的 shapeless recipe。
- `src/main/generated/data/civilizationmod/advancement/recipes/misc/residential_marker_1.json`：解鎖條件改為羊毛。
- `src/main/generated/data/civilizationmod/advancement/recipes/misc/residential_marker_2.json`、`residential_marker_4.json`、`residential_marker_6.json`：解鎖條件改為持有 1 床 marker。
- `src/main/generated/.cache/315ef6c70ed81a1f3bfa2fddabcbca61d46c497c`：更新 datagen 內容雜湊記錄。
- `Mods.md`：追加本次住宅配方變更與驗證結果。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| Fabric 26.2 Recipe Generation API | 已查閱官方 Recipe Generation 文件；確認 shapeless recipe 使用 `shapeless(...).requires(...).unlockedBy(...).save(...)`，並可用整數參數要求多個相同物品。 |
| `gradlew.bat --no-daemon runDatagen --console=plain` | `BUILD SUCCESSFUL`；生成四個住宅 recipe 與對應 advancement。 |
| Generated recipe JSON | 已確認 1 床為 `crafting_shaped`、2／4／6 床為 `crafting_shapeless`，ingredients 數量分別為 2、4、6 個 `residential_marker_1`。 |
| `gradlew.bat --no-daemon clean build --console=plain` | `BUILD SUCCESSFUL`；common、client、resources、jar、test 與 assemble 均通過。 |
| `git diff --check` | 沒有 whitespace error；僅有既有的跨平台 LF／CRLF 提示。 |

### 未完成與風險

目前已完成配方資料與建置驗證，尚未在遊戲內開啟配方書或工作台實際合成四種 marker。由於高容量 marker 需要消耗多個 1 床 marker，玩家若要製作 6 床 marker，必須先準備 6 個 1 床 marker；這是本次階梯式設計的預期成本。

### 下一步

在遊戲內驗收：先以羊毛與鐵錠合成 `residential_marker_1`，再以 2、4、6 個 1 床 marker 分別合成 `residential_marker_2`、`residential_marker_4`、`residential_marker_6`；確認配方書解鎖條件、輸出物品數量與住宅 marker 的床位容量驗證一致。

### 來源

- [1] [Fabric Recipe Generation 26.2](https://docs.fabricmc.net/develop/data-generation/recipes)


## 2026-08-21 — Residential Marker 村民與床位容量檢查

### 變更

完成住宅 marker 的 server-side 容量閉環。住宅 marker 的 `capacity` 由 `BuildingMarkerRegistry` 的 1／2／4／6 定義提供；`ResidenceValidator` 繼續以領地內原版床的 block state 計算 `bedCount`，只有 `bedCount >= capacity` 才能保持住宅有效。

`BuildingObservation` 現在保存可向後相容的多居民 roster。舊版 `resident_uuid`／`resident_name` 仍保留，載入舊世界時會自動正規化為 roster 第一名；新居民以 `ResidentAssignment` 保存 UUID 與診斷名稱，重複 UUID 不會重複加入。住宅重掃、storage snapshot 更新與 validation 更新都會保留既有 roster，不會因重新執行 building scan 而清除已指派村民。

`/civitas assign <building_index> [villager]` 對住宅加入即時 server gate：指派前重新找目前載入的 marker ItemFrame、讀取領地資料並重新計算床位；marker／領地缺失或床位不足時拒絕。若同一村民已經在目標住宅，操作維持冪等；若住宅 roster 已達 marker capacity，會回傳住宅已滿訊息並拒絕新的村民。warehouse 仍維持原本的單一居民與物流行為。

building scan 會檢查保存的住宅 roster。若 marker 目前容量低於已保存居民數，資料不會被刪除，但 observation 會標記為 `invalid`、reason=`residents_over_capacity`，並移除 marker 的有效 glint；玩家可換回更高容量 marker 或調整居民資料後重新掃描。`/civitas building list` 與 `inspect` 現在顯示床位需求、實際居民數、居民容量及居民名單。

`BuildingResidentService` 每 20 server ticks 逐一解析住宅 roster 中的原版村民 UUID 並導航到 marker；warehouse 的單居民導航與自動入庫分支未改變。SavedData schema version 升至 6，新增 roster 使用 nested optional Codec 欄位以保持舊世界相容。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingObservation.java`：新增 `ResidentAssignment`、optional roster Codec、居民查詢／追加／validation／容量 helper，保留 legacy single-resident 欄位。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：schema 升至 6；新增 marker ItemFrame 查找；building scan 將 roster 超容量住宅標記 invalid；assigned lookup 改查 roster。
- `src/main/java/com/civilizationmod/ResidenceValidator.java`：公開已查證的領地床位計數 helper，供 assign 即時重驗證。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：加入住宅即時床位檢查、滿額拒絕與多居民 inspect 顯示。
- `src/main/java/com/civilizationmod/BuildingResidentService.java`：逐一導航住宅 roster，保留 warehouse 單居民物流。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增住宅已滿、超容量、居民 roster 與多居民摘要翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增住宅已滿、超容量、居民 roster 與多居民摘要翻譯。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依專案政策使用繁體中文同步新增上述翻譯。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：新增多居民 roster、重複 UUID、重掃保留、Codec round-trip 與舊單居民相容測試。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加住宅容量與 roster 規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：追加住宅容量與 roster 規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域可重用 skill 規則。
- `Mods.md`：追加本次住宅容量功能記錄。

### 查證與驗證

| 項目 | 結果 |
|---|---|
| 版本 | Minecraft `26.2`、Fabric Loader `0.19.3`、Fabric API `0.158.0+26.2`、Loom `1.17.19`、Java `25`。 |
| 26.2 API 依據 | ItemFrame entity query、`ServerLevel.getEntities(EntityTypeTest, AABB, Predicate)`、Villager UUID／navigation 與既有 server tick API 均沿用專案已查證的 Minecraft 26.2 common jar 與 Fabric lifecycle API。新增 roster Codec 沿用既有 `RecordCodecBuilder` 與 `listOf().optionalFieldOf` 模式。 |
| `gradlew.bat --no-daemon compileJava --console=plain` | 最終 `BUILD SUCCESSFUL`。中途的 lambda capture 錯誤已修正，未保留未編譯程式。 |
| `gradlew.bat --no-daemon clean build --console=plain` | 最終 `BUILD SUCCESSFUL`；common、client、resources、jar、sourcesJar、check 與 assemble 通過。 |
| `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain` | `FoodModelRegressionTest: PASS`；包含新增住宅 roster 與 Codec 回歸檢查。 |
| `gradlew.bat --no-daemon runClient --console=plain` | Render thread、Fabric loader、resource reload、texture atlas 與 client initialization 成功；已主動停止持續執行的 client。Realms／帳號授權訊息仍屬開發環境服務，不是本模組錯誤。 |
| `git diff --check` | 沒有 whitespace error；只有既有 LF／CRLF 跨平台提示。 |

### 未完成與風險

目前已完成 server-side 資料模型、容量 gate、重掃 invalid 判定、保存相容與自動導航；尚未在實際 Minecraft 世界中手動指派 1／2／4／6 名村民完成完整互動驗收。遊戲內驗收仍需確認：2 床住宅能成功指派第二名村民、第三名被拒絕；4 床與 6 床依序接受不超過 marker 容量的村民；床位不足或 marker 降級時 building scan 顯示 invalid；世界重開後 roster 仍存在。

目前沒有新增 unassign 指令，因此若玩家要釋放住宅容量，下一個切片需要設計 server-side unassign／搬遷規則；本次不會透過刪除 SavedData 或自動移除村民來繞過容量限制。

### 下一步

在遊戲內建立四種住宅 marker，分別放入有對應床位數的領地，先執行 `/civitas building scan`，再使用 `/civitas assign <building_index> [villager]` 依序測試容量上限與超額拒絕。驗收完成後再設計 unassign／居民搬遷與市政廳居民上限管理。


## 2026-08-21 — Residential Marker 1／2／4／6 材質與模型視覺更新

### 變更

重新設計四個不同容量住宅 marker 的遊戲內材質，使它們維持同一套 Civitas 住宅識別語言，但能在物品欄與展示框中快速區分容量。四個圖示共同採用側視床輪廓、深色木製床架、白色枕頭與清楚的金色容量數字；床墊色彩依容量分級：1 床為深紅色、2 床為皇家藍、4 床為翠綠色、6 床為深紫色。住宅圖示不使用 warehouse 的箱子／容器輪廓，因此不會與倉庫標誌混淆。

每個容量版本維持獨立 PNG 與獨立 item model layer0：`residential_marker_1`、`residential_marker_2`、`residential_marker_4`、`residential_marker_6`。模型沿用 Minecraft `item/generated`，不增加未查證的 model override、renderer 或 client-only model loader；視覺差異由各自材質提供，維持簡單可靠的 common resource asset chain。

### 影響檔案

- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_1.png`：更新 1 床住宅材質。
- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_2.png`：更新 2 床住宅材質。
- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_4.png`：更新 4 床住宅材質。
- `src/main/resources/assets/civilizationmod/textures/item/residential_marker_6.png`：更新 6 床住宅材質。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_1.json`：確認 layer0 指向 1 床專用材質，未需改動。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_2.json`：確認 layer0 指向 2 床專用材質，未需改動。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_4.json`：確認 layer0 指向 4 床專用材質，未需改動。
- `src/main/resources/assets/civilizationmod/models/item/residential_marker_6.json`：確認 layer0 指向 6 床專用材質，未需改動。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加住宅材質與模型資產規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步住宅材質與模型資產規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域可重用資產規則。
- `Mods.md`：追加本次材質更新記錄。

### 資產規格與查證

| 資產 | PNG 尺寸 | 色彩型態 | 主要辨識元素 | 模型 layer0 |
|---|---:|---|---|---|
| `residential_marker_1.png` | 16×16 | RGBA，color type 6 | 深紅床墊、金色數字 1 | `civilizationmod:item/residential_marker_1` |
| `residential_marker_2.png` | 16×16 | RGBA，color type 6 | 皇家藍床墊、金色數字 2 | `civilizationmod:item/residential_marker_2` |
| `residential_marker_4.png` | 16×16 | RGBA，color type 6 | 翠綠床墊、金色數字 4 | `civilizationmod:item/residential_marker_4` |
| `residential_marker_6.png` | 16×16 | RGBA，color type 6 | 深紫床墊、金色數字 6 | `civilizationmod:item/residential_marker_6` |

所有正式 PNG 均保留一像素透明外框，四角 alpha 為 0；這能避免生成圖的背景或床腳貼到 item icon 邊界。圖示先以 AI 生成統一風格概念，再以最近鄰縮放為 Minecraft 16×16 RGBA 貼圖；沒有使用其他受版權保護的資源包素材。

### 查證與驗證

| 命令／檢查 | 結果 |
|---|---|
| 正式 PNG IHDR 與 alpha 檢查 | 四個檔案均為 `16×16`、RGBA color type `6`，四角 alpha 均為 0。 |
| item model JSON 檢查 | 四個 model 均使用 `minecraft:item/generated`，並各自指向專用 layer0。 |
| `git diff --check` | 通過，沒有 whitespace error。一次性資產驗證腳本已刪除，未留在 repository。 |
| `gradlew.bat --no-daemon clean build --console=plain` | `BUILD SUCCESSFUL`；common、client、resources、jar、assemble 與 build 均通過。 |
| `gradlew.bat --no-daemon runClient --console=plain` | Fabric loader、Render thread、texture atlas、resource reload 與 client initialization 成功；完成 smoke test 後已主動停止持續執行的 client。Realms 授權訊息屬開發環境服務，不是材質載入錯誤。 |

### 未完成與風險

目前已完成檔案層級與 client 啟動驗證，尚未在實際遊戲 GUI 中逐一截圖確認四個 icon 在不同 GUI 縮放設定下的最終觀感。由於 16×16 圖示本身解析度很低，數字辨識會受 GUI scale 與玩家顯示器影響；若遊戲內觀感需要調整，下一輪應只針對最不清楚的一個容量數字進行單一局部修正，不重新改動整套色彩系統。

### 下一步

進入遊戲後在創造模式物品欄或以配方取得四種 marker，並將它們放在 ItemFrame 中比較：確認 1／2／4／6 的床墊色彩與金色數字能快速區分，且 warehouse marker 仍維持箱子外觀。之後可再為住宅 marker 增加 tooltip 的容量與即時床位摘要，讓圖示辨識與 server-side 容量規則互相補強。


## 2026-08-21 — 住宅重開恢復、住宅綁定裝置與互動修正

### 變更

本次先處理玩家住宅驗收第 5 項失敗，以及三個題外互動問題。唯讀檢查最近測試世界 `run/saves/新的世界/dimensions/minecraft/overworld/data/civilizationmod/civilization_world.dat` 後，確認 `schema_version=6`、住宅 observation 同時保存 `resident_uuid` 與 `residents` roster；因此目前證據不支持「roster Codec 完全沒有保存」這個判斷，修正重點改為重開後的角色恢復、validation gate 與互動查找。

新增 `residence_binding_device`。玩家蹲下右鍵有效住宅 marker 的 ItemFrame，裝置會由 server 驗證 building observation、住宅功能、有效狀態、目前 marker 與即時床位後保存選取的維度與 marker 座標；之後手持同一裝置右鍵任意可用原版 Villager，即可在不注視特定住宅村民的情況下完成 roster 綁定。裝置不消耗，使用 `/give @p civilizationmod:residence_binding_device` 取得；本次尚未新增正式合成配方。

新增 `/civitas unassign <building_index> [villager]`，`/civilization` 共用相同命令樹與 Tab completion。省略 villager 時使用玩家視線選取；明確指定時 server 限制目標必須是同維度原版 Villager。解除後只移除指定 UUID；若村民沒有其他 Civitas building assignment，才清除帶 `civitas_role` 的角色胸甲。

修正模組村民背包互動：已保存 roster 的 Villager 不再因住宅暫時被標記為 invalid 就被背包 server gate 擋住。`BuildingResidentService` 也會對保存 roster 的村民重新套用 Civitas 角色外套；invalid 建築只停止導航與物流，不阻止居民恢復外觀與背包管理。

修正蹲下右鍵快速放置 marker：若手持同類 marker 沒有完成 territory，server 會在玩家 inventory 中尋找同類且帶完成 `WarehouseTerritory` 的 stack，複製完整 CustomData 後再將 marker 放入空 ItemFrame，並消耗實際帶有 territory 的來源 stack。轉移仍保留原 item type、territory、A/B 點與其他 CustomData，不會只建立遺失資料的新 ItemStack。

新增住宅綁定裝置的 16×16 RGBA item texture 與 `minecraft:item/generated` model。圖示使用金色連結環、紫色核心與床形符號，與 warehouse 箱子輪廓及 residential marker 床位數字圖示區分。三份 locale 新增裝置名稱、tooltip、選取／綁定結果、失敗原因與 unassign 訊息。

### 影響檔案

- `src/main/java/com/civilizationmod/ResidenceBindingDeviceData.java`：保存裝置選取的維度與 marker 座標。
- `src/main/java/com/civilizationmod/ResidenceBindingDeviceItem.java`：裝置物品與 tooltip。
- `src/main/java/com/civilizationmod/ResidenceBindingDeviceInteraction.java`：server-authoritative ItemFrame 選取與 Villager 綁定。
- `src/main/java/com/civilizationmod/CivilizationItems.java`：註冊住宅綁定裝置與 interaction。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 `unassign` 命令。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：新增指定 UUID roster 移除 helper。
- `src/main/java/com/civilizationmod/BuildingResidentService.java`：重開／invalid 狀態下仍恢復角色外套。
- `src/main/java/com/civilizationmod/BuildingRoleEquipment.java`：新增安全清除 Civitas 角色胸甲 helper。
- `src/main/java/com/civilizationmod/CivitasVillagerInteraction.java`、`CivitasVillagerBackpackMenu.java`：放寬已保存 roster 的背包 gate。
- `src/main/java/com/civilizationmod/WarehouseMarkerQuickDeploy.java`、`WarehouseTerritory.java`：加入同類 marker inventory territory fallback 與 CustomData 複製。
- `src/main/resources/assets/civilizationmod/models/item/residence_binding_device.json`：裝置 item model。
- `src/main/resources/assets/civilizationmod/textures/item/residence_binding_device.png`：16×16 裝置貼圖。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`、`zh_tw.json`、`zh_cn.json`：同步新增玩家可見文字。
- `skills/minecraft-civilization-fabric-262/SKILL.md`、`.cursor/skills/civitas-fabric-262/SKILL.md` 與全域 skill：同步寫入 API 與互動修正規則。

### 查證與驗證

- 版本：Minecraft Java Edition 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Fabric Loom 1.17.19、Gradle 9.5.1、Java 25。
- API 查證：使用 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar` 的 `javap` 確認 `Player.getInventory()`、`Inventory.getContainerSize()`、`Inventory.getItem(int)`；CustomData 與既有 `WarehouseTerritory` component 寫法沿用已查證模式。
- 世界資料診斷：確認最近測試世界的 SavedData 為 schema 6，且住宅 roster 實際存在於 NBT；未直接修改玩家世界存檔。
- 命令：`gradlew.bat --no-daemon compileJava --console=plain`；結果：`BUILD SUCCESSFUL`。
- 命令：`gradlew.bat --no-daemon clean build --console=plain`；結果：`BUILD SUCCESSFUL`。
- 命令：`gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain`；結果：`FoodModelRegressionTest: PASS`。
- 命令：`gradlew.bat --no-daemon runClient --console=plain`；結果：client 完成 Fabric／Mixin／texture atlas／Render thread 初始化，未見本次新物品或 interaction 導致的致命錯誤；Realms 授權訊息屬開發環境登入服務訊息，測試完成後已停止 client。
- `git diff --check`：未發現 whitespace 錯誤；本次暫存 javap 輸出已清理。

### 未完成與風險

- 玩家尚未在修正後的實際世界重新驗收「退出世界、重開後 `/civitas building inspect`、背包與角色外套仍可用」；目前只確認 SavedData NBT 原本已保存 roster，並完成 server／client gate 與角色恢復修正。
- 住宅綁定裝置本次尚未有正式合成配方，也尚未加入專用 creative tab；可先使用 `/give` 測試。
- 快速放置的 inventory fallback 會尋找同一 item type 的第一個已完成 territory stack；若玩家同時持有多個不同領地的同類 marker，最好直接手持已完成的目標 marker，以避免消耗背包中另一個領地來源。這是便利 fallback，不改變 server 的 territory、維度、附著牆與 duplicate 驗證。
- 綁定裝置目前只支援原版 Villager，沒有把其他模組 NPC 當作硬依賴或自動納入。

### 下一步

- 使用 `/give @p civilizationmod:residence_binding_device` 實測兩步綁定流程。
- 重開世界後重新驗收住宅 roster、背包、角色外套與 `/civitas building inspect`。
- 測試 `/civitas unassign` 是否釋放容量且不清除其他建築的角色狀態。
- 依實測結果決定住宅綁定裝置的正式合成配方，之後再開始市政廳 marker 設計。

### 來源

- [1] https://docs.fabricmc.net/develop/events — Fabric Events 26.2。
- [2] https://docs.fabricmc.net/develop/items/custom-item-interactions — Fabric Custom Item Interactions。
- [3] 本機 Minecraft 26.2 common jar：`C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`，`javap` 查證日期 2026-08-21。


## 2026-08-21 — 修正住宅綁定裝置 missing-texture 破圖

### 變更

根據玩家提供的遊戲截圖，住宅綁定裝置的 tooltip 能正常顯示，但物品欄圖示出現紫／青棋盤狀 missing-texture 佔位圖。檢查原始 PNG 後確認貼圖檔本身存在、尺寸為 16×16 RGBA，`models/item/residence_binding_device.json` 也存在且 layer0 指向正確；真正缺失的是 Minecraft 26.2 新 item model 資源鏈所需的 `assets/civilizationmod/items/residence_binding_device.json`。

補上 item definition，讓 `civilizationmod:residence_binding_device` 透過 `minecraft:model` 導向既有的 `civilizationmod:item/residence_binding_device` model，再由 model 的 layer0 載入既有 PNG。這與專案中已正常工作的 `warehouse_marker` 與 `residential_marker_1` item definition 格式一致，沒有改動住宅綁定裝置的 Java 行為、tooltip 內容、模型或 PNG。

### 影響檔案

- `src/main/resources/assets/civilizationmod/items/residence_binding_device.json`：新增 Minecraft 26.2 item definition，修正物品圖示資源鏈。
- `src/main/resources/assets/civilizationmod/models/item/residence_binding_device.json`：確認既有 `minecraft:item/generated` 與 layer0 不需修改。
- `src/main/resources/assets/civilizationmod/textures/item/residence_binding_device.png`：確認既有 16×16 RGBA 貼圖不需修改。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加 26.2 item definition 必要節點與檢查規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步追加同一項可重用查證規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域 skill 規則。
- `Mods.md`：追加本次破圖根因、修正與驗證紀錄。

### 查證與驗證

- 版本：Minecraft Java Edition 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Fabric Loom 1.17.19、Gradle 9.5.1、Java 25。
- 截圖檢查：原始截圖為 496×124；確認 tooltip 與 item ID 可見，圖示區出現紫／青 missing-texture 佔位圖。
- 資產檢查：確認 PNG 為 16×16，且 model、texture 路徑存在；對照正常 marker 的 `assets/civilizationmod/items/<id>.json` 格式。
- 命令：`gradlew.bat --no-daemon processResources --console=plain`；結果：`BUILD SUCCESSFUL`。
- 命令：`gradlew.bat --no-daemon compileJava compileClientJava build --console=plain`；結果：`BUILD SUCCESSFUL`。
- 命令：`gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain`；結果：`FoodModelRegressionTest: PASS`。
- 命令：`gradlew.bat --no-daemon runClient --console=plain`；結果：完成 Fabric loader、Render thread、texture atlas 與 client initialization；Realms 授權訊息仍是開發環境服務訊息，未見本次資源修正造成的 fatal error。測試完成後已停止持續執行的 client。
- jar 檢查：`build/libs/civilizationmod-1.0.0.jar` 同時包含 `assets/civilizationmod/items/residence_binding_device.json`、`models/item/residence_binding_device.json` 與 `textures/item/residence_binding_device.png`。
- 最新 client log 以 `findstr` 檢查未找到 `residence_binding_device`、`Unable to load model`、`Missing texture` 或 `Failed to load model` 警告。

### 未完成與風險

檔案層級、jar 封裝與 client 啟動驗證均已通過，但本次無法替玩家直接操作 GUI 重新截圖，因此仍需要玩家在遊戲中重新載入資源或重啟 client，確認住宅綁定裝置圖示已恢復正常。若仍看到舊紫／青圖示，請先按 `F3 + T` 重新載入資源；若仍未修正，再提供新的 `latest.log` 與截圖。

### 下一步

進入遊戲後重新取得或查看 `civilizationmod:residence_binding_device`，確認物品欄、快捷欄與 tooltip 左側圖示不再破圖。確認後即可繼續住宅綁定裝置的遊戲內流程驗收；本次修正尚未新增正式合成配方。

### 來源

- [1] 專案既有正常資源：`assets/civilizationmod/items/warehouse_marker.json` 與 `assets/civilizationmod/items/residential_marker_1.json`。
- [2] Minecraft 26.2 實際 `build/resources/main` 與 `build/libs/civilizationmod-1.0.0.jar` 資源封裝檢查，日期 2026-08-21。


## 2026-08-21 — 新增住宅綁定裝置正式合成配方

### 變更

玩家確認住宅綁定裝置採用 3×3 有序合成配方：外圈 8 個原版木棍，中央 1 個鐵錠，輸出 1 個 `civilizationmod:residence_binding_device`。本次將配方加入既有 `CivilizationModRecipeProvider`，沒有建立平行 datagen provider，也沒有修改住宅綁定裝置的互動、保存資料或物品材質。

配方解鎖條件設定為玩家取得木棍後解鎖，生成的 advancement 使用 `has_stick` criterion。此設計符合配方主要材料為木棍的基本遊戲流程，也保留中央鐵錠作為裝置的金屬核心成本。

### 影響檔案

- `src/client/java/com/civilizationmod/client/CivilizationModRecipeProvider.java`：新增 `RESIDENCE_BINDING_DEVICE` 的 shaped recipe，pattern 為 `sss`、`sis`、`sss`，`s = Items.STICK`、`i = Items.IRON_INGOT`。
- `src/main/generated/data/civilizationmod/recipe/residence_binding_device.json`：datagen 產生的正式 recipe JSON。
- `src/main/generated/data/civilizationmod/advancement/recipes/misc/residence_binding_device.json`：datagen 產生的配方解鎖 advancement。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加 `Items.STICK` 與 shaped recipe 查證。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步追加配方查證規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域 skill 規則。
- `Mods.md`：追加本次配方與驗證歷史。

### 查證與驗證

- 版本：Minecraft Java Edition 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Fabric Loom 1.17.19、Gradle 9.5.1、Java 25。
- API 查證：以 `C:\Program Files\Java\jdk-25.0.2\bin\javap.exe` 查詢 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar` 的 `net.minecraft.world.item.Items`，確認 `public static final Item STICK` 存在。
- 命令：`gradlew.bat --no-daemon runDatagen --console=plain`；結果：`BUILD SUCCESSFUL`，生成 recipe 與 advancement。
- 生成 recipe 檢查：確認 pattern 為 `sss`、`sis`、`sss`，key 為 `minecraft:stick` 與 `minecraft:iron_ingot`，result 為 `civilizationmod:residence_binding_device`。
- 生成 advancement 檢查：確認使用 `has_stick`，recipe unlock 指向 `civilizationmod:residence_binding_device`。
- 命令：`gradlew.bat --no-daemon compileJava compileClientJava build runFoodModelRegressionTest --console=plain`；結果：`BUILD SUCCESSFUL`，`FoodModelRegressionTest: PASS`。
- jar 檢查：`build/libs/civilizationmod-1.0.0.jar` 包含住宅綁定裝置的 item definition、model、texture、recipe 與 advancement。
- `git diff --check`：通過；datagen 的 tracked cache 雜訊已還原，未納入本次功能提交。

### 未完成與風險

本次已完成檔案與建置驗證，尚未由玩家在遊戲內用工作台實際放入 8 個木棍與中央鐵錠確認合成結果與配方書解鎖顯示。若遊戲內配方書沒有立即更新，請先重新進入世界或使用 `F3 + T` 重新載入資源；配方本身已封裝於正式 jar。

### 下一步

在遊戲內以工作台驗收住宅綁定裝置配方，確認 8 木棍加中央鐵錠可合成 1 個裝置，並確認木棍取得後配方能解鎖。驗收成功後可進入住宅綁定裝置的完整兩步綁定流程，接著再規劃市政廳 marker。

### 來源

- [1] 本機 Minecraft 26.2 common jar 的 `net.minecraft.world.item.Items` javap 查證，日期 2026-08-21。
- [2] Fabric 26.2 配方生成文件：https://docs.fabricmc.net/develop/data-generation/recipes。


## 2026-08-21 — 建立市政廳 marker 視覺 prototype

### 變更

依照殖民地建設主線，新增 `town_hall_marker` 的第一版視覺 prototype。由於目前 marker 是放入 ItemFrame 的物品，本次所稱的「方塊模型」實際採 Minecraft item model，而不是可放置的獨立方塊 block model；資源鏈為 `items/town_hall_marker.json` → `models/item/town_hall_marker.json` → `textures/item/town_hall_marker.png`。

視覺方向定義為殖民地行政核心：對稱石磚市政建築、深藍圓頂、金色頂飾與中央金色市政徽記、兩側深紅旗幟。深藍／金色／石灰／深紅色組與 warehouse 的箱子輪廓、residential marker 的床與容量數字保持差異，玩家在 ItemFrame 或背包內能以輪廓與顏色快速辨識。

新增 `TownHallMarkerItem` 與 `CivilizationItems.TOWN_HALL_MARKER`，物品單件堆疊，並加入英文與兩份繁體中文 locale 名稱。這一輪刻意只完成可顯示的資產 prototype；沒有把 `town_hall_marker` 加入 `BuildingMarkerRegistry`，也沒有加入 `BuildingFunction.TOWN_HALL`，因此不會提前觸發 warehouse／residence 的 territory selection、building scan、validation 或村民物流。這是為了先把市政廳的 server 規則定義清楚，再開啟真正的殖民地核心功能。

### 影響檔案

- `src/main/java/com/civilizationmod/TownHallMarkerItem.java`：市政廳 marker 的純視覺 item wrapper。
- `src/main/java/com/civilizationmod/CivilizationItems.java`：註冊 `TOWN_HALL_MARKER_KEY` 與 item。
- `src/main/resources/assets/civilizationmod/items/town_hall_marker.json`：Minecraft 26.2 item definition。
- `src/main/resources/assets/civilizationmod/models/item/town_hall_marker.json`：`minecraft:item/generated` item model，layer0 指向市政廳材質。
- `src/main/resources/assets/civilizationmod/textures/item/town_hall_marker.png`：16×16 RGBA 市政廳 marker 貼圖；透明外框、石磚建築、藍色圓頂、金色徽記、紅色旗幟。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增 `Town Hall Marker`。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`、`zh_cn.json`：新增 `市政廳標誌`。
- `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md`、全域 skill：同步市政廳資產與 server 功能分層規則。
- `Mods.md`：追加本次設計、驗證與下一步。

### 資產設計與查證

AI 概念圖先以對稱市政建築作為視覺基準，再經透明背景處理與最近鄰縮放產生 16×16 RGBA 貼圖。生成工具輸出的棋盤格背景實際是全不透明像素，因此沒有直接把概念圖當正式材質；一次性處理只移除邊緣連通的亮色棋盤格，保留建築深色像素外框。正式貼圖四角 alpha 均為 0，alpha bbox 為 `(2,1,13,15)`。

### 查證與驗證

- 版本：Minecraft Java Edition 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Fabric Loom 1.17.19、Gradle 9.5.1、Java 25。
- 現有程式查證：`BuildingFunction` 目前只有 `WAREHOUSE`、`RESIDENCE`；`BuildingMarkerRegistry.registerDefaults()` 目前只有 warehouse 與四種 residence marker，因此本次不提前註冊 Town Hall function。
- 命令：`gradlew.bat --no-daemon compileJava compileClientJava build --console=plain`；結果：`BUILD SUCCESSFUL`。
- 命令：`gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain`；結果：`FoodModelRegressionTest: PASS`。
- 命令：`gradlew.bat --no-daemon runClient --console=plain`；結果：完成 Fabric loader、Render thread、texture atlas 與 client initialization，未見市政廳資產導致的 fatal error；測試完成後已停止 client。
- jar 檢查：正式 `civilizationmod-1.0.0.jar` 包含 `items/town_hall_marker.json`、`models/item/town_hall_marker.json` 與 `textures/item/town_hall_marker.png`；最新 client log 未找到 `Unable to load model`、`Missing texture` 或 `Failed to load model`。
- `git diff --check`：待提交前執行；本次只保留正式專案資產，未把一次性概念圖與處理腳本加入 repository。

### 未完成與風險

目前市政廳 marker 只是可用 `/give @p civilizationmod:town_hall_marker` 取得的視覺 prototype，尚無正式配方、creative tab、ItemFrame 掃描、領地驗證、殖民地保存資料、居民上限或建築解鎖效果。這不是 bug，而是刻意將視覺資產與尚未決定的 server 規則分開。

### 下一步

下一個最小可玩功能應先設計並實作 **Town Hall server registration**：玩家將市政廳 marker 放入 ItemFrame 後，系統驗證唯一性、位置與領地，建立一筆殖民地核心 SavedData。第一版建議先只允許每個維度一座市政廳，保存核心座標、建立時間、範圍半徑與殖民地 ID；確認核心存在後，再把住宅與倉庫建築綁定到該殖民地，最後才加入居民上限與建築解鎖規則。市政廳 marker 的正式配方應在這套 server 規則確定後再設計，避免先固定成本卻要因玩法平衡重做。

### 來源

- [1] 專案現有 `BuildingFunction.java`、`BuildingMarkerRegistry.java` 與 `CivitasBuildingMarkerItem.java`。
- [2] `imagegen` skill 的 game asset、transparent asset 與 lightweight validation 規則。


## 2026-08-21 — 取消 marker 切換物品時自動清除座標

### 變更

依照玩家操作體驗回饋，移除 `CivitasBuildingMarkerItem.inventoryTick(...)` 中「當 marker 不在手上時清除 pending point-A」的機制。現在玩家可以左鍵設定 A 點後切換快捷欄、暫時拿其他物品或整理背包，再拿回同一個 marker 右鍵設定 B 點；已完成的 WarehouseTerritory 也不受任何影響。

保留必要的資料安全邊界：右鍵 B 點時若兩點位於不同維度，`WarehouseTerritory.complete(...)` 仍會清除不相容的 pending selection 並回傳 `DIFFERENT_DIMENSION`；若領地超出軸長或體積限制，則保留 A 點讓玩家重新選擇 B 點，而不是丟失整個選點流程。切換手持物品不再是取消條件。

同步移除三份 locale 中已不再使用的 `civilizationmod.building.selection.cancelled` stale key。原本 `WarehouseTerritory.clearPendingSelection(...)` 保留給跨維度錯誤與其他明確 server-side 清理路徑使用，沒有刪除資料層能力。

### 影響檔案

- `src/main/java/com/civilizationmod/CivitasBuildingMarkerItem.java`：移除 inventory tick 依 equipment slot 自動清除 pending selection 的 override 與不再使用的 imports。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：還原未適合 headless registry 環境的 ItemStack 測試嘗試，保留既有純 Java regression suite。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：移除英文 stale cancellation key。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`、`zh_cn.json`：同步移除繁體中文 stale cancellation key。
- `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md`、全域 skill：同步記錄新的選點保存規則。
- `Mods.md`：追加本次行為變更、邊界與驗證結果。

### 查證與驗證

- 版本：Minecraft Java Edition 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Fabric Loom 1.17.19、Gradle 9.5.1、Java 25。
- 行為查證：確認舊清除唯一位於 `CivitasBuildingMarkerItem.inventoryTick(...)`，`WarehouseTerritory.complete(...)` 的跨維度清除仍保留。
- 初次 regression 嘗試失敗：新增的 `ItemStack` pending-selection 測試在 headless `runFoodModelRegressionTest` 環境觸發 `CivilizationItems`／Minecraft registry 初始化的 `ExceptionInInitializerError`；該測試與正式功能無關，已移除並清理暫存 `regression_failure.log`，沒有把未通過測試宣稱為成功。
- 命令：`gradlew.bat --no-daemon runFoodModelRegressionTest compileJava compileClientJava build --console=plain`；結果：`BUILD SUCCESSFUL`，`FoodModelRegressionTest: PASS`。
- 命令：`gradlew.bat --no-daemon runClient --console=plain`；結果：Fabric loader、Render thread、texture atlas 與 client initialization 成功；Realms 授權訊息屬開發環境服務訊息，測試完成後已停止 client。
- `git diff --check`：通過；沒有保留本次診斷暫存檔。

### 未完成與風險

目前 automated regression 能確認既有模型與建築邏輯未被破壞，但沒有在 headless test 中直接模擬玩家切換快捷欄，因為那需要完整 Minecraft entity／inventory runtime。這次行為修正本身是移除唯一會清除座標的 `inventoryTick` 路徑，仍需玩家在遊戲中實際驗收。

### 下一步

在遊戲內使用 warehouse marker 或 residential marker 左鍵設定 A 點，切換到其他快捷欄物品，等待幾秒後再切回同一 marker；查看 tooltip 確認 A 點仍存在，再右鍵方塊完成 B 點。另測試跨維度或超大範圍錯誤，確認只有明確的 server-side 無效條件會改變 pending selection。

### 來源

- [1] 專案 `CivitasBuildingMarkerItem.java` 與 `WarehouseTerritory.java`，行為查證日期 2026-08-21。
- [2] 專案 `FoodModelRegressionTest.java` 與 Gradle `runFoodModelRegressionTest` 實際輸出，日期 2026-08-21。


## 2026-08-21 — 實作市政廳核心辨識與 SavedData 保存

### 變更

本次完成殖民地建設主線的第一個市政廳 server 垂直切片。`town_hall_marker` 現在正式加入 `BuildingFunction.TOWN_HALL` 與 `BuildingMarkerRegistry`，因此 `/civitas building scan` 能在已載入範圍內辨識放置於 ItemFrame 的市政廳標誌。

市政廳採用獨立的 `TownHallValidator`。第一版只要求 ItemFrame 位於已載入範圍且依 Minecraft 26.2 已查證公式附著在有效牆面，不套用 warehouse／residence 的門、地板、屋頂、牆壁、箱子或床位容量條件。這符合市政廳是殖民地宣告核心，而不是一般居住／物流房屋的設計邊界。

新增 `TownHallCore` 保存模型，保存殖民地 ID、維度、marker 座標、建立時間與範圍半徑。核心資料寫入既有 overworld-scoped `CivilizationWorldData` 的 optional `town_halls` Codec list，`schema_version` 從 6 升為 7；舊世界沒有 `town_halls` 欄位時以空清單載入，不影響既有聚落、建築、住宅 roster 與 warehouse 資料。

第一版唯一性規則是每個維度只能有一座市政廳。同一 marker 重掃回傳 `EXISTING`，不重設建立時間；同維度不同 marker 回傳 `DUPLICATE`，第二座仍保留 BuildingObservation 但以 `duplicate_town_hall` 標記 invalid；不同維度可以各自建立一座核心。核心初始範圍半徑固定為 64，實際範圍運算延後到下一個殖民地切片。

新增 `/civitas townhall` 與 `/civilization townhall` 查詢命令，顯示殖民地 ID、維度、核心座標、建立 tick 與範圍半徑。`/civitas building scan` 的結果也新增市政廳核心新增數與衝突數。市政廳成為 known marker 後，已同步限制共用 territory 選點與 warehouse 快速放置只接受 warehouse／residence，避免市政廳誤觸發領地選取或物流部署。

### 影響檔案

- `src/main/java/com/civilizationmod/TownHallCore.java`：新增市政廳核心 immutable record 與 Codec。
- `src/main/java/com/civilizationmod/TownHallValidator.java`：新增 ItemFrame 附著牆面 validator。
- `src/main/java/com/civilizationmod/BuildingFunction.java`：新增 `TOWN_HALL("town_hall")`。
- `src/main/java/com/civilizationmod/BuildingMarkerRegistry.java`：註冊市政廳 marker metadata，新增 territory marker 過濾。
- `src/main/java/com/civilizationmod/BuildingGeometryValidator.java`：加入市政廳專用 validation branch。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：新增 schema 7、`town_halls` optional Codec、核心註冊／查詢／去重／marker 遺失清理與 scan 整合。
- `src/main/java/com/civilizationmod/BuildingScanSummary.java`：新增市政廳新增與衝突計數。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：新增 `duplicate_town_hall` validation reason。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 `townhall` 查詢與 scan 統計，兩個根命令共用相同 command builder。
- `src/main/java/com/civilizationmod/CivitasBuildingMarkerItem.java`：territory 選點只接受 warehouse／residence。
- `src/main/java/com/civilizationmod/WarehouseMarkerQuickDeploy.java`：快速部署只接受 warehouse marker。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`、`zh_tw.json`、`zh_cn.json`：新增市政廳核心、功能與 duplicate reason 翻譯。
- `src/test/java/com/civilizationmod/FoodModelRegressionTest.java`：新增 TownHallCore Codec、同位置 idempotent、同維度唯一性與跨維度保存測試。
- `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md`、全域 skill：同步 API 查證與市政廳核心規則。

### API 查證

本次不以猜測使用版本敏感 API。Fabric Saved Data 官方文件說明 `SavedData`、Codec、`SavedDataType`、`getDataStorage().computeIfAbsent(...)` 與 `setDirty()` 的保存流程；本機 Minecraft 26.2 common jar 的 javap 確認 `ServerLevel.getDataStorage()` 實際回傳 `net.minecraft.world.level.storage.SavedDataStorage`，其公開方法為 `computeIfAbsent(SavedDataType<T>)`，並確認 `SavedDataType` 建構子簽名。ItemFrame 的 `getItem()`、`setItem(...)`、`blockPosition()`、`getDirection()` 與牆面支撐位置則沿用專案既有 26.2 geometry reference。

### 查證與驗證

- 版本：Minecraft Java Edition 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Fabric Loom 1.17.19、Gradle 9.5.1、Java 25。
- `gradlew.bat --no-daemon runFoodModelRegressionTest compileJava compileClientJava build --console=plain`：`FoodModelRegressionTest: PASS`，`BUILD SUCCESSFUL`。
- regression 新增內容實際驗證 TownHallCore Codec round-trip、首次註冊、同 marker 重掃不重置、同維度第二核心拒絕，以及不同維度允許一座核心。
- `gradlew.bat --no-daemon runClient --console=plain`：Fabric loader、Render thread、texture atlas、registry 與 client initialization 成功；測試後已停止 client。開發環境 Realms 授權訊息與 run 目錄鎖定訊息未造成模組初始化失敗。
- `gradlew.bat --no-daemon runServer --console=plain`：Fabric loader、模組 registry 與 server 初始化流程已載入，但 dedicated server 因開發環境尚未同意 `eula.txt` 而沒有進入可連線狀態；因此 dedicated server 內實際命令與 SavedData 重開尚未驗證，不把它宣稱為完成。
- javap 暫存輸出已清理；`git diff --check` 已執行。

### 未完成與風險

目前市政廳核心是由 `/civitas building scan` 主動辨識，不是即時 ItemFrame 右鍵事件；玩家放置標誌後需要執行掃描。核心移除只會在對應 marker 位置已載入且確認 ItemFrame 不存在時發生，避免未載入區塊誤刪 SavedData。固定半徑 64 已保存，但尚未用於限制住宅／warehouse，也尚未把建築正式綁定到殖民地 ID。

Dedicated server 尚未完成實際啟動驗證，原因是 `run/eula.txt` 尚未設為同意；這是測試環境狀態，不是市政廳程式錯誤。遊戲內的 ItemFrame 附著方向、重複核心顯示、移除核心與世界重開仍需要玩家驗收。

### 遊戲內驗收

先取得並放置市政廳標誌：`/give @p civilizationmod:town_hall_marker`。將它放入附著於牆面的 ItemFrame，站在附近執行 `/civitas building scan`；第一次結果應顯示新增市政廳核心 1 個，並可執行 `/civitas townhall` 查詢核心 ID、座標與半徑。再次執行 scan，新增核心應為 0，`/civitas townhall` 的建立時間不可被重置。

在同一維度另一處放置第二個附著牆面的市政廳 marker，再次 scan；第二個 BuildingObservation 應顯示無效，原因為「這個維度已經存在另一座市政廳」，而 `/civitas townhall` 仍只顯示一座核心。進入另一個維度放置市政廳並 scan，則應可建立另一座核心。最後退出世界、重新進入並執行 `/civitas townhall`，確認核心資料仍存在。

### 下一步

下一個最小切片是使用已保存的 Town Hall core：將通過掃描的 warehouse／residence BuildingObservation 綁定到同維度、核心半徑 64 內的殖民地 ID，並在核心不存在或超出範圍時標記為未綁定。完成這個歸屬閉環後，再實作市政廳範圍內的居民上限與建築解鎖。

### 來源

- [1] https://docs.fabricmc.net/develop/saved-data — Fabric Saved Data 文件，保存資料、Codec、SavedDataType、computeIfAbsent 與 setDirty。
- [2] https://docs.fabricmc.net/develop/events — Fabric Events 26.2 文件，事件 callback 註冊模式。
- [3] 專案 `skills/minecraft-civilization-fabric-262/references/building-geometry-26.2.md` — ItemFrame 與 26.2 幾何 API 查證。
- [4] 本機 Minecraft 26.2 common jar javap：`C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`，日期 2026-08-21。


## 2026-08-21 — 修正 marker 拆除後附魔光效殘留

### 變更

玩家驗收確認：住宅與 warehouse 拆除建築後，server 已正確判定領地無效，但從 ItemFrame 拿下或打掉 marker 時，物品仍保留附魔光效。這是視覺狀態殘留，不是 territory 或 building validation 失敗。

根因是兩條不同的生命週期。marker 仍在 ItemFrame 內但掃描結果變成 invalid 時，既有 `BuildingMarkerVisualState.apply(frame, false)` 可以移除 `ENCHANTMENT_GLINT_OVERRIDE`；然而玩家直接打掉 ItemFrame 後，ItemFrame 在原版掉落流程中即將消失，沒有後續 frame 可供 invalid scan 更新，因此掉落的 ItemStack 會把 glint component 一起帶走。

新增 `BuildingMarkerGlintCleanup` common-side handler，使用 Fabric API `AttackEntityCallback.EVENT` 監聽玩家攻擊 ItemFrame。server 端確認 ItemFrame 內是已知 Civitas marker 後，複製 stack 並只移除 `DataComponents.ENCHANTMENT_GLINT_OVERRIDE`，再寫回 ItemFrame，最後回傳 `PASS`，讓原版繼續完成 ItemFrame 拆除、掉落與 CustomData／warehouse territory 保留。沒有 glint 的 marker、未知物品、client、spectator 或其他實體全部不介入。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingMarkerGlintCleanup.java`：新增 ItemFrame 拆除前 glint 清理 handler。
- `src/main/java/com/civilizationmod/CivilizationMod.java`：在 common initializer 註冊 handler。
- `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md`、全域 skill：同步記錄根因、26.2 component API、ItemFrame.setItem bytecode 與 callback 邊界。
- `Mods.md`：追加本次 bug 修正與驗收說明。

### API 查證

本次以實際 Minecraft 26.2 common jar javap 確認 `ItemStack.remove(DataComponentType<? extends T>)` 會回傳被移除的 component，且 `DataComponents.ENCHANTMENT_GLINT_OVERRIDE` 為公開 Boolean component type；以 ItemFrame bytecode 確認 `setItem(ItemStack, boolean)` 的 boolean 只控制紅石鄰居更新，entity data 在兩種呼叫中都會更新，因此不是 client sync 根因。另以實際 Fabric API 0.158.0+26.2 events-interaction jar javap 確認 `AttackEntityCallback.EVENT` 與 `interact(Player, Level, InteractionHand, Entity, EntityHitResult)`。

### 查證與驗證

- 版本：Minecraft Java Edition 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Fabric Loom 1.17.19、Gradle 9.5.1、Java 25。
- `gradlew.bat --no-daemon runFoodModelRegressionTest compileJava compileClientJava build --console=plain`：`FoodModelRegressionTest: PASS`，`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon runClient --console=plain`：Fabric loader、registry、Render thread、texture atlas 與 client initialization 成功；測試後已停止 client。開發環境 Realms 授權與 run 目錄鎖定訊息沒有造成模組初始化失敗。
- `git diff --check`：已通過；javap 暫存輸出已清理。

### 未完成與風險

目前自動化驗證已確認 callback 可編譯、註冊與載入，但尚未以完整遊戲操作直接觀察 ItemFrame 掉落物的 tooltip。這需要玩家在遊戲內打掉一個已發光的住宅或 warehouse marker，確認掉落／拿回的物品不再發光，同時確認 warehouse territory 的 CustomData 沒有被清除。

### 遊戲內驗收

先用有效住宅或 warehouse marker 讓 ItemFrame 顯示附魔光效，再直接打掉 ItemFrame。拿起掉落 marker 或查看背包 tooltip，確認附魔光效消失；重新放置同一 marker 後，CustomData 與 warehouse territory 應仍然存在。接著可以故意拆除住宅／warehouse 的必要條件並執行 `/civitas building scan`，確認系統仍判定 invalid，且 ItemFrame 內的 marker 也會移除 glint。

### 下一步

完成這輪視覺修正驗收後，繼續市政廳殖民地歸屬切片：將有效 warehouse／residence 綁定到同維度、核心半徑 64 內的 Town Hall colony ID，並在市政廳不存在或建築超出範圍時標記為未綁定。

### 來源

- [1] https://docs.fabricmc.net/develop/events — Fabric Events 26.2。
- [2] https://docs.fabricmc.net/develop/items/custom-item-interactions — Fabric Custom Item Interactions 26.2。
- [3] https://maven.fabricmc.net/docs/fabric-api-0.158.0+26.2/net/fabricmc/fabric/api/event/player/AttackEntityCallback.html — AttackEntityCallback API。
- [4] 本機 Minecraft 26.2 common jar 與 Fabric API 0.158.0+26.2 jar javap，日期 2026-08-21。


## 2026-08-21 — 通用 Civitas 綁定裝置與蹲下右鍵空氣快速部署

### 變更摘要

本次將原本住宅專用的綁定流程升級為 **Civitas 綁定裝置**。新的正式物品 ID 是 `civilizationmod:civitas_binding_device`，可選取並綁定所有已知且有效的 Civitas 建築標誌，包括 warehouse、residence 與 Town Hall。原本的 `civilizationmod:residence_binding_device` 不移除，而是保留為 legacy registry ID，改由 `ResidenceBindingDeviceItem` 繼承通用 `CivitasBindingDeviceItem`，使既有世界與玩家持有的舊物品仍可使用通用綁定行為，不因改名而失效。新配方輸出通用 ID，未來新取得的裝置統一使用 Civitas 品牌。

`ResidenceBindingDeviceData` 的資料 key 從住宅專用的 `civitas_residence_binding` 通用化為 `civitas_binding`；讀取時仍保留舊 key fallback，座標 schema 與兩步綁定流程不變。住宅 marker 綁定仍會即時重新驗證 territory、床位與容量，避免只因裝置通用化而跳過住宅規則；warehouse 與 Town Hall 沿用既有 `BuildingResidentService` 的單一綁定語義，沒有擅自引入尚未設計的容量規則。

蹲下右鍵空氣的快速部署也已補上。直接右鍵空 ItemFrame 的路徑仍由 `UseEntityCallback` 處理；蹲下右鍵空氣則由 Fabric API 0.158.0+26.2 已查證存在的 `UseItemCallback.EVENT` 接收，再以玩家眼睛位置、視線向量、AABB、dot product alignment、line-of-sight 與最近距離尋找視線中的空 ItemFrame。找到後共用原本的 server-side `deployToFrame` 驗證，包含維度、已完成 territory、ItemFrame 牆面附著、重複 marker、CustomData 轉移與玩家背包 fallback。快速部署接受 warehouse 與 residence territory marker，仍排除不屬於 territory 的 Town Hall marker；未完成 territory 時會保留本地化錯誤訊息且不修改 ItemFrame。

### 影響檔案

| 類別 | 檔案與內容 |
|---|---|
| 通用物品 | `src/main/java/com/civilizationmod/CivitasBindingDeviceItem.java`：新增通用綁定裝置基類與 tooltip。 |
| Registry／相容 | `src/main/java/com/civilizationmod/CivilizationItems.java`：註冊 `CIVITAS_BINDING_DEVICE` 並保留 legacy `RESIDENCE_BINDING_DEVICE`；`src/main/java/com/civilizationmod/ResidenceBindingDeviceItem.java`：改為通用 subclass。 |
| 綁定流程 | `src/main/java/com/civilizationmod/ResidenceBindingDeviceInteraction.java`：選取所有有效 Civitas marker，住宅保留床位與容量驗證；`src/main/java/com/civilizationmod/ResidenceBindingDeviceData.java`：使用新 NBT key 並支援舊 key fallback。 |
| 空氣快速部署 | `src/main/java/com/civilizationmod/WarehouseMarkerQuickDeploy.java`：新增 `UseItemCallback` air path、視線 ItemFrame 選取與 warehouse／residence territory 過濾，並與直接 ItemFrame path 共用部署邏輯。 |
| 配方／資料生成 | `src/main/java/com/civilizationmod/CivilizationModRecipeProvider.java`、`src/client/java/com/civilizationmod/client/CivilizationModRecipeProvider.java`：配方輸出通用 ID；新增 `src/main/generated/data/civilizationmod/recipe/civitas_binding_device.json` 與對應 recipe advancement。舊 residence recipe generated output 已移除。 |
| 資產／本地化 | `src/main/resources/assets/civilizationmod/items/civitas_binding_device.json`、`models/item/civitas_binding_device.json`、`textures/item/civitas_binding_device.png`；`en_us.json`、`zh_tw.json`、`zh_cn.json` 同步通用裝置文案。 |
| 技能文件 | `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md` 與全域 `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md` 已追加 API 查證與設計規則。 |
| 進度記錄 | `Mods.md`：本節為本次 canonical 開發紀錄。 |

### API 查證

本次沒有猜測 Fabric 26.2 API。已以 Fabric API `0.158.0+26.2` 實際 jar 的 `javap` 確認 `net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT` 與 callback `interact(Player, Level, InteractionHand)`；它正好用於補足「右鍵空氣」不會走直接實體互動 callback 的情境。視線選取沿用專案中已驗證的 `getEyePosition`、`getViewVector`、AABB、dot product 與 `hasLineOfSight` 模式。官方文件參考為 [Fabric Events 26.2](https://docs.fabricmc.net/develop/events) 與 [Custom Item Interactions 26.2](https://docs.fabricmc.net/develop/items/custom-item-interactions)。

### 驗證結果

| 驗證項目 | 結果 |
|---|---|
| `runFoodModelRegressionTest` | `FoodModelRegressionTest: PASS`。 |
| `gradlew.bat compileJava compileClientJava build --console=plain` | `BUILD SUCCESSFUL`；common、client、resources、jar、assemble 與 build 通過。 |
| `gradlew.bat runDatagen --console=plain` | `BUILD SUCCESSFUL`；生成通用綁定裝置 recipe 與 advancement。 |
| `gradlew.bat runClient --console=plain` | smoke test 已確認 Fabric Loader、Minecraft 26.2、Render thread、Indigo、texture atlas、registry 與 client initialization 成功；測試完成後手動停止執行中的 client，未將 Realms 授權提示視為模組錯誤。 |
| `git diff --check` | 無程式碼 whitespace error；Windows Git 僅提示部分檔案的 LF／CRLF 轉換警告。 |

### 尚未完成與風險

本次尚未完成遊戲內人工驗收，因此仍需確認玩家實際蹲下右鍵有效 warehouse／residence marker、空氣視線選取最近空 ItemFrame、通用裝置右鍵村民綁定，以及舊 `residence_binding_device` 在既有存檔中的相容性。視線 dot threshold `0.92` 與搜尋 AABB `16` 格是目前互動實作參數，後續若玩家回報遠距離或斜角選取體驗不佳，再以實際遊戲行為調整，不以猜測 API 的方式處理。

Town Hall 目前可以被通用裝置選取，但 territory 快速部署刻意排除 Town Hall；市政廳的下一個主線仍是將 warehouse／residence 綁定到同維度、核心半徑 64 內的 `colony_id`，並處理核心不存在或超出範圍時的未綁定狀態。配方、居民上限與殖民地解鎖規則應等市政廳歸屬閉環驗收後再設計。

### 下一步

請在遊戲內先使用 `/give @p civilizationmod:civitas_binding_device`，分別對有效 warehouse 與 residence marker 蹲下右鍵選取，再右鍵村民確認綁定；另以 `/give @p civilizationmod:residence_binding_device` 驗證 legacy 物品仍然可用。接著手持已完成 territory 的 warehouse 或 residence marker，對視線中的空 ItemFrame 蹲下右鍵空氣，確認 marker 被消耗並轉移到 ItemFrame。市政廳則以 `/civitas building scan` 與 `/civitas townhall` 驗證核心登記後，進入「市政廳殖民地歸屬切片」。


## 2026-08-21 — 市政廳 marker 有序合成配方

### 變更

依照使用者指定，市政廳 marker 現在使用五件原版材料的有序合成：第一列為綠寶石、鐵劍、鐵鎬；第二列為鐵斧、鐵鏟。配方輸出 `civilizationmod:town_hall_marker`，解鎖 advancement 以玩家持有綠寶石為條件。

使用者提供的第二列只有兩個材料，但 Minecraft 26.2 的 `ShapedRecipeBuilder.pattern(...)` 要求每一列寬度相同，因此實作 pattern 為 `esp` 與 `ah `，第二列最右格為空白，材料仍然是左對齊的兩格，不會增加額外材料。這是保持使用者指定排列同時符合 26.2 recipe builder 驗證的必要表示方式。

### 配方排列

| 合成格 | 材料 |
|---|---|
| 第一列第一格 | 綠寶石 `minecraft:emerald` |
| 第一列第二格 | 鐵劍 `minecraft:iron_sword` |
| 第一列第三格 | 鐵鎬 `minecraft:iron_pickaxe` |
| 第二列第一格 | 鐵斧 `minecraft:iron_axe` |
| 第二列第二格 | 鐵鏟 `minecraft:iron_shovel` |
| 第二列第三格 | 空白 |

### API 查證

Fabric 官方 [Recipe Generation 26.2](https://docs.fabricmc.net/develop/data-generation/recipes) 確認此專案使用的 client-side `FabricRecipeProvider`、內部 `RecipeProvider.buildRecipes()`、`shaped(...)`、`pattern(...)`、`define(...)`、`unlockedBy(...)` 與 `save(output)` datagen 流程。本機 Minecraft 26.2 common jar 以 JDK 25 `javap` 確認 `Items.EMERALD`、`Items.IRON_SWORD`、`Items.IRON_PICKAXE`、`Items.IRON_AXE` 與 `Items.IRON_SHOVEL` 均為公開 `Item` 常數，沒有猜測版本 API。

### 影響檔案

- `src/client/java/com/civilizationmod/client/CivilizationModRecipeProvider.java`：加入市政廳 marker 的 shaped recipe；第二列使用等寬 pattern `ah `。
- `src/main/generated/data/civilizationmod/recipe/town_hall_marker.json`：datagen 生成的正式 recipe JSON。
- `src/main/generated/data/civilizationmod/advancement/recipes/misc/town_hall_marker.json`：配方解鎖 advancement，條件為持有綠寶石。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加 Item 常數、官方 recipe 流程與 shaped pattern 等寬規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步相同查證結果。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域可重用規則。
- `Mods.md`：追加本次 recipe 變更、錯誤診斷、驗證結果與下一步。

### 查證與驗證

| 命令／項目 | 結果 |
|---|---|
| Minecraft 26.2 common jar `javap` | 確認五個 `Items` 常數存在；JDK 路徑為 `C:\Program Files\Java\jdk-25.0.2\bin\javap.exe`，common jar 為 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`。 |
| 第一次 `gradlew.bat --no-daemon runDatagen --console=plain` | 失敗，完整 log 確認原因是 `Pattern must be the same width on every line!`，定位至市政廳 recipe 的第二列 `ah`。不是 Item registry 或 client/server 邊界錯誤。 |
| 修正後 `gradlew.bat --no-daemon runDatagen --console=plain` | `BUILD SUCCESSFUL`；provider 完成，寫入 14 個 datagen 檔案，recipe 與 advancement 均生成。 |
| 生成 recipe JSON 檢查 | pattern 為 `esp`、`ah `；key 正確對應 emerald、iron sword、iron pickaxe、iron axe、iron shovel；result 為 `civilizationmod:town_hall_marker`。 |
| 生成 advancement JSON 檢查 | `has_emerald` 條件與 `civilizationmod:town_hall_marker` recipe unlock 正確。 |
| `gradlew.bat --no-daemon runFoodModelRegressionTest compileJava compileClientJava build --console=plain` | `FoodModelRegressionTest: PASS`；`BUILD SUCCESSFUL`。 |

### 未完成與風險

本次尚未在遊戲內打開合成台人工確認 recipe book 圖示與實際合成動畫；datagen、JSON、common/client compile 與完整 build 已通過。第二列右側空白是 shaped recipe 的必要空位，若未來希望第二列置中或改成三格排列，需另行修改 pattern 並重新驗證，不應直接刪除空白以免再次觸發 26.2 等寬錯誤。

### 下一步

請在遊戲內重新載入資源或重啟 client，在合成台依照下列排列測試：

```text
[綠寶石][鐵劍][鐵鎬]
[鐵斧  ][鐵鏟][空白  ]
```

確認能合成 `town_hall_marker` 後，繼續市政廳殖民地歸屬切片：將有效 warehouse／residence 綁定到同維度、Town Hall 半徑 64 內的 `colony_id`。

### 來源

- [1] https://docs.fabricmc.net/develop/data-generation/recipes — Fabric Recipe Generation 26.2。
- [2] https://docs.fabricmc.net/develop/data-generation/setup — Fabric Data Generation Setup 26.2。
- [3] 本機 Minecraft 26.2 common jar `javap`：`Items` Item 常數與實際 datagen stacktrace，查證日期 2026-08-21。


## 2026-08-21 — 自訂 NPC 路線評估與 ResidentRecord 分層決策

### 評估結論

本次評估 Grok 提出的自訂 NPC 建議。整體方向正確：Entity 應分成 server-side 行為與資料、client-side renderer／model／texture，文明真實狀態不應只依賴世界中目前載入的實體；但 Civitas 現階段不立即新增自訂 Entity。

目前模組已經以原版 Villager 完成居民指派、住宅 roster、warehouse 物流、27 格 Civitas 背包與導向建築 marker。此時直接引入 `CivitasResidentEntity` 會同時牽涉 EntityType、attributes、goals、renderer、model layer、資料同步、背包互動、物流服務、住宅容量、命令與既有存檔遷移，工作面積遠大於單純新增一種生物。正確順序是先完成 Town Hall colony ownership 與住宅／warehouse 的殖民地閉環，再抽離居民資料層。

### 建議的資料分層

| 層 | Civitas 責任 | 現階段策略 |
|---|---|---|
| `BuildingObservation` | 建築 marker、建築功能、驗證狀態、住宅容量與暫時 roster | 保留現有 `residents`，作為過渡相容資料。 |
| `ResidentRecord`／`ResidentRegistry` | 居民 UUID、`colony_id`、住宅 building key、工作 building key、角色、生命週期與 body type | 下一個居民資料切片才新增，server SavedData 為真相來源。 |
| 原版 Villager | 目前世界中的居民 body、導航、背包與物流執行者 | 現階段繼續使用，不轉換既有村民。 |
| `CivitasResidentEntity` | 未來可替換的自訂居民 body | 等資料層與服務介面穩定後，再做最小召喚／行走切片。 |

目前 `BuildingObservation` 已保存 `ResidentAssignment` UUID／名稱清單，`CivilizationWorldData.findBuildingAssignedTo(String)` 也能依 UUID 找到建築，因此具備抽離的起點；但仍缺全域 `colony_id`、住宅與工作建築 key、角色、生命週期與 body type。未來新增 `ResidentRecord` 時，必須避免 `BuildingObservation` 與 `ResidentRecord` 同時成為可修改的兩個真相來源；應由 `ResidentRecord` 作 canonical，舊 roster 只作 legacy compatibility 或由服務層同步。

### 分階段路線

1. **現在：完成市政廳殖民地歸屬。** 依同維度與 Town Hall 半徑 64 將有效 warehouse／residence 綁定 `colony_id`；沒有核心、核心衝突或超出範圍時標記未綁定。
2. **下一個居民資料切片：建立 `ResidentRecord`。** 先保存原版 Villager UUID、殖民地 ID、住宅建築、工作建築、角色與 `body_type=vanilla_villager`。從既有 BuildingObservation roster 做一次相容遷移，舊世界資料缺少新欄位時使用 optional defaults；此階段不新增 Entity。
3. **建立 body adapter／服務邊界。** 將指派、導航、背包開啟、物流入庫與角色視覺操作集中於 common-side resident service，使呼叫端不直接假設一定是 `Villager`。既有原版村民實作先通過此介面，命令與殖民地規則改讀 ResidentRecord。
4. **最小自訂 Entity 垂直切片。** 未來只實作一個可 `/summon` 的 `CivitasResidentEntity`：註冊 EntityType、基本 attributes、最少 goal、重開存檔與簡單 client renderer。第一版不做交易、完整工作 AI、背包 GUI、物流與住宅入住，避免一次跨越太多系統。
5. **最後才接入殖民地工作。** 讓自訂居民透過同一套 resident service 執行 warehouse／住宅工作；既有原版 Villager 不自動轉換，避免玩家存檔中的 UUID、村民交易與原版 AI 被破壞。

### 26.2 API 查證

Fabric 官方 [Creating Your First Entity 26.2](https://docs.fabricmc.net/develop/entities/first-entity) 說明最小自訂 Entity 流程：`PathfinderMob` 子類、`EntityType.Builder.of(...).sized(...)`、`FabricDefaultAttributeRegistry.register(...)`、server-side goals，以及 client-only render state、model layer、renderer 與 `EntityRenderers.register(...)`。持久的 entity-local state 使用 `addAdditionalSaveData(ValueOutput)`／`readAdditionalSaveData(ValueInput)`；需要供 client 顯示的短期狀態才使用 `SynchedEntityData`。[1]

這次只完成官方文件查證與架構決策，沒有把文件範例當成已實作 API，也沒有猜測如何直接重用原版 Villager renderer。若未來要共用村民外觀，仍需針對 Minecraft 26.2 的 renderer／model class 以實際 `javap` 與 compile 查證；官方教學本身採用自訂 model layer 與 renderer，不能直接推論重用成本為零。

### 影響檔案

- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加自訂 Entity 官方流程與延後導入規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步專案 skill。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域 skill。
- `Mods.md`：保存本次架構評估與決策。
- 尚未修改 Java、registry、EntityType、renderer 或世界資料 schema；本次不新增自訂 NPC。

### 驗證與風險

本次查證使用 Fabric 官方 26.2 Entity 文件，並檢查目前 `BuildingObservation` 與 `CivilizationWorldData`。沒有執行自訂 Entity build，因為本次是架構評估而非實作；不能宣稱自訂 Entity 已可用。主要風險是未來新增 ResidentRecord 時產生雙重真相、居民死亡／卸載／換 body 時 UUID 生命週期不清，以及直接重用原版 Villager renderer 可能需要額外 26.2 API 查證。

### 下一步

下一個實作應是「市政廳殖民地歸屬切片」，不是自訂 NPC：先讓有效住宅與 warehouse 依同維度、Town Hall 核心半徑 64 寫入 `colony_id`，並讓 scan／inspect 能顯示已綁定、無核心、超出範圍與核心衝突。殖民地歸屬通過遊戲內驗收後，再設計 ResidentRecord。

### 來源

- [1] https://docs.fabricmc.net/develop/entities/first-entity — Fabric Creating Your First Entity 26.2。
- [2] https://docs.fabricmc.net/develop/porting/ — Fabric Porting to 26.2。


## 2026-08-21 — Grok ResidentRecord 雙 UUID 分析與 26.2 API 修正

### 附件重點

Grok 的附件以「戶籍／身體」模型說明居民資料：`residentId` 是 Civitas 永久邏輯身份，`entityUuid` 是目前世界中的 Minecraft Entity 身體 UUID；建築應關聯 residentId，而不是直接關聯目前身體。這個方向適合 Civitas 的長期殖民地架構，也能支援原版村民死亡、未載入、換身體與未來自訂 Entity。

附件也正確指出 `BuildingObservation` 不應長期成為完整居民資料的唯一容器。現在的 `BuildingObservation.residents` 與 `CivilizationWorldData.findBuildingAssignedTo(String)` 可以作為過渡，但未來要抽出 `ResidentRecord`／`ResidentRegistry`，集中保存 colony ID、住宅 building key、工作 building key、角色、body type 與生命週期。

### 採用的雙 UUID 設計

| 欄位 | 意義 | 生命週期 |
|---|---|---|
| `residentId` | Civitas 戶籍／邏輯身份主鍵 | 建立後永久保存；換身體不變。 |
| `entityUuid` | 目前 Minecraft body 的 UUID | 初次指派原版村民時使用 `villager.getUUID()`；死亡、移除或 rebind 時才更新。 |
| `bodyType` | 目前身體種類 | 第一版為 `vanilla_villager`，未來可加入 `civitas_resident`。 |
| `lifecycle` | 居民狀態 | 建議至少區分 `active`、`body_missing`、`deceased`、`retired`。 |
| `lastKnownPosition` | 重連與除錯提示 | 只作提示，不作居民身份真相。 |

舊世界遷移時，既有 `BuildingObservation.ResidentAssignment.uuid` 沒有獨立 residentId，因此第一階段可把既有 UUID 同時視為 residentId 與 entityUuid，確保既有住宅與 warehouse 指派可被讀回。新 `/civitas assign` 才建立獨立 residentId。未來 BuildingObservation 應保存 residentId；一個 residentId 與一個 entityUuid 的 runtime index 可在 SavedData 載入後重建，但不能讓兩份資料同時成為可修改的真相來源。

### 重要生命週期修正

不能在 chunk unload 時清除 entityUuid。原版村民可能只是暫時離開載入範圍，lookup 找不到不代表死亡或資料遺失。entityUuid 應保留，並以 runtime lookup miss 或 `body_missing` 診斷狀態表達目前沒有可操作 body。只有 server 驗證的死亡、移除或 rebind 才能改 entityUuid／lifecycle；第一版不自動復活死亡原版村民。

建築關係不得保存目前 `/civitas building list` 的 one-based index，因為重掃、刪除或排序都可能使 index 改變。應使用 dimension + marker 座標作為穩定 building key，或日後新增 immutable building ID。

### 26.2 API 查證與附件修正

本機 Minecraft 26.2 common jar 以 JDK 25 `javap` 查證：`Entity.getUUID()`、`Entity.setUUID(UUID)` 可用；`ServerLevel` 公開的是 `getEntity(int)` 與 `getEntityInAnyDimension(UUID)`，沒有查到附件示例中的通用 `level.getEntity(UUID)`。`EntityGetter` 公開空間 `getEntities(...)` 與 `getPlayerByUUID(UUID)`，也沒有通用 `getEntity(UUID)`。因此附件中的：

```text
Entity entity = level.getEntity(entityUuid);
```

不能直接當成已驗證的 Fabric／Minecraft 26.2 API。未來 ResidentRecord lookup 應以已查證的 server-side strategy 實作，候選為 `ServerLevel.getEntityInAnyDimension(UUID)`，並在取得後再次驗證 Entity 所在維度、類型與 server 權限。這個 lookup 尚未寫入 Java，也未宣稱已完成 runtime 行為驗證。

`Entity.getUUID()`、`Entity.setUUID(UUID)` 與 `ServerLevel.getEntityInAnyDimension(UUID)` 的查證結果已同步寫入三份 Minecraft skill。附件原始內容則保留在本次任務提供的 `pasted_content.txt`，不納入模組 runtime。

### 影響檔案

- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加雙 UUID、chunk unload 與 26.2 lookup 規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步相同結論。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域規則。
- `Mods.md`：追加本附件分析與 API 修正。
- 尚未修改 Java、Entity registry、ResidentRecord Codec 或 schema；本次是架構與 API 查證，不是實作切片。

### 驗證結果與風險

已讀取附件、目前 `BuildingObservation`／`CivilizationWorldData`，並完成本機 26.2 common jar `javap`。沒有執行 ResidentRecord 編譯或 runtime 測試，因為尚未開始實作。後續新增資料層時，需先決定 schema version、舊 roster 遷移、entity death／removal event、跨維度 lookup 與 duplicate UUID 修復策略。

### 下一步

先完成市政廳殖民地歸屬切片；之後才建立第一版 `ResidentRecord`／`ResidentRegistry`，包含 `residentId`、optional `entityUuid`、`colonyId`、home/work building key、role、body type 與 lifecycle。完成 SavedData round-trip、舊住宅／warehouse 指派遷移與 `/civitas resident list` 後，才進入自訂 NPC Entity。

### 來源

- [1] https://docs.fabricmc.net/develop/entities/first-entity — Fabric Creating Your First Entity 26.2。
- [2] https://docs.fabricmc.net/develop/saved-data — Fabric Saved Data。
- [3] 本機 Minecraft 26.2 common jar `javap`：`Entity`、`ServerLevel`、`ServerEntityGetter`、`EntityGetter`，JDK 25，查證日期 2026-08-21。


## 2026-08-21 — Civitas 自動化單元測試第一批

### 變更

本次將原本只有 `FoodModelRegressionTest` main-based regression 的測試架構，擴充為 Fabric Loader JUnit 5 自動化測試。新增 `CivitasCoreTest`，使用 Fabric 官方 26.2 建議的 registry bootstrap，將 common-side 的核心規則納入 Gradle `test` 與 `build`。

測試分層採用兩階段策略：Fabric Loader JUnit 測試純邏輯、Codec 與 server-independent invariants；Minecraft GameTest 日後再測試真實 ServerLevel、ItemFrame、SavedData reload、居民互動、背包、物流與 client renderer。這次沒有假稱 GameTest 已完成。

### 目前自動化覆蓋

| 測試類別 | 驗證內容 |
|---|---|
| `FoodDemandModel` | 食物需求、短缺、盈餘、穩定度與事件結果可重現。 |
| `SettlementAdapter`／`CivilizationWorldData` | 聚落座標漂移去重、不同來源與遠距離聚落區分，且重複 scan 不重置既有食物／債務／穩定度。 |
| `BuildingObservation` | marker 維度與座標身份、住宅 roster 新增與去重、重掃保留居民、Codec round-trip、legacy single-resident compatibility。 |
| 建築與聚落綁定 | warehouse 維度、水平／垂直範圍與超出範圍未綁定。 |
| `WarehouseTerritory` | X/Y/Z inclusive bounds 與相等比較。 |
| `TownHallCore`／`CivilizationWorldData` | 同一維度核心註冊冪等、第二核心衝突、不同維度可註冊、半徑預設值與 Codec round-trip。 |

### API 查證

Fabric 官方 [Automated Testing 26.2](https://docs.fabricmc.net/develop/automatic-testing) 確認 Fabric Loader JUnit 使用：

```text
testImplementation "net.fabricmc:fabric-loader-junit:${project.loader_version}"
test { useJUnitPlatform() }
```

同一份官方文件確認 registry-dependent test 在 `@BeforeAll` 先呼叫 `SharedConstants.tryDetectVersion()` 與 `Bootstrap.bootStrap()`；GameTest 則使用 Loom `configureTests` source set 與 `fabric-gametest` entrypoint。本次只採用已查證的 JUnit 路線，沒有猜測 GameTest 設定或新增未驗證的 game test source set。

### 影響檔案

- `build.gradle`：加入 Fabric Loader JUnit test dependency、`useJUnitPlatform()`、test logging，並讓 `check` 依賴既有 `runFoodModelRegressionTest`。
- `src/test/java/com/civilizationmod/CivitasCoreTest.java`：新增六個 JUnit 5 common-side 測試案例。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：記錄 JUnit／GameTest 分層、bootstrap 與覆蓋邊界。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步測試規則。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域測試規則。
- `Mods.md`：追加本次測試內容、命令、結果、風險與下一步。

### 驗證結果

| 命令 | 結果 |
|---|---|
| `gradlew.bat --no-daemon test --console=plain` | 第一次執行發現測試預期值錯誤：短缺穩定度實際為 95，修正測試預期後再次執行 `BUILD SUCCESSFUL`；`CivitasCoreTest` 六個案例全部通過。 |
| `gradlew.bat --no-daemon build --console=plain` | `FoodModelRegressionTest: PASS`；JUnit test 通過；`check`、jar、assemble 與完整 `build` 均 `BUILD SUCCESSFUL`。 |
| JUnit report | `CivitasCoreTest`：6 tests、0 failures、0 errors。 |
| `git diff --check` | 無 whitespace error；Windows Git 只提示 LF／CRLF 轉換警告。 |

### 未完成與風險

目前 JUnit 測試不會啟動真實 Minecraft server，因此尚未驗證 ItemFrame 掃描、Town Hall marker 在實際 ServerLevel 的登記、SavedData 重開載入、原版村民互動、27 格背包、warehouse 物流與 client GUI／renderer。這些必須使用 Fabric 26.2 GameTest 或遊戲內人工驗收，不應由純 JUnit 測試假裝覆蓋。

現有 `FoodModelRegressionTest` 名稱歷史上雖然只叫 FoodModel，實際已包含多個文明核心回歸檢查；本次保留它作相容入口，並由 `check` 自動執行。未來若測試規模增加，可以再將其拆成多個 JUnit class，但不要在拆分時刪除既有回歸案例。

### 下一步

先繼續完成 Town Hall `colony_id` 歸屬切片；接著建立 ResidentRecord 後，為 resident migration、UUID lookup、重開 SavedData round-trip 與 duplicate assignment 新增 JUnit。等 common 資料層穩定，再建立獨立 Fabric GameTest source set，測試 ItemFrame、SavedData reload、住宅容量與 warehouse 入庫閉環。

### 來源

- [1] https://docs.fabricmc.net/develop/automatic-testing — Fabric Automated Testing 26.2。
- [2] https://docs.gradle.org/current/userguide/java_testing.html — Gradle Java Testing。


## 2026-08-21 — 撤回失敗的蹲下右鍵空氣快速部署

### 變更

遊戲內驗收發現「蹲下右鍵空氣，依視線尋找空 ItemFrame 並部署 marker」不穩定，因此依使用者要求撤回這條功能。`WarehouseMarkerQuickDeploy` 現在只註冊並處理直接對 ItemFrame 的 `UseEntityCallback`；已移除 `UseItemCallback` air-target callback、16 格 AABB 視線搜尋、dot alignment 與 `findLookedAtEmptyItemFrame` 方法。

保留的直接部署流程不變：玩家蹲下直接右鍵空 ItemFrame，server 驗證主手、非 spectator、territory、維度、領地範圍、ItemFrame 牆面附著、重複 marker 與 CustomData，成功後使用帶完整資料的 stack copy 寫入 ItemFrame，再消耗 marker。住宅與 warehouse 的兩點領地、marker ItemFrame 掃描、通用 Civitas 綁定裝置與舊 `residence_binding_device` 相容性不受此次移除影響。

### 影響檔案

- `src/main/java/com/civilizationmod/WarehouseMarkerQuickDeploy.java`：移除 `UseItemCallback` air-target 路徑與視線搜尋，只保留直接 ItemFrame callback。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：將現行規則改為直接 ItemFrame 部署，保留 air-target 的歷史查證但標記為撤回。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步撤回規則。
- `.cursor/skills/civitas-fabric-262/verified-apis.md`：同步直接部署與撤回說明。
- `/home/ubuntu/skills/minecraft-civilization-fabric-262/SKILL.md`：同步全域 skill 規則。
- `Mods.md`：追加本次移除原因、影響、驗證與下一步。

### 驗證結果

- source search：`WarehouseMarkerQuickDeploy.java` 只剩 `UseEntityCallback`、`deployToFrame` 與 territory bounds；不再包含 `UseItemCallback`、`interactWithAir` 或 `findLookedAtEmptyItemFrame`。
- `gradlew.bat --no-daemon build --console=plain`：`BUILD SUCCESSFUL`。
- `FoodModelRegressionTest`：由 `check` 自動執行並通過。
- `CivitasCoreTest`：由 `build` 自動執行，既有 6 個 JUnit 案例通過。
- `git diff --check`：提交前需確認無 whitespace error。

### 未完成與風險

本次只移除 air-target 快速部署，沒有新增直接 ItemFrame 的 GameTest；直接 ItemFrame 部署仍需在遊戲內人工確認。歷史 skill／Mods.md 記錄沒有刪除，而是標記 air-target 路徑已撤回，避免未來代理把已失敗方案重新當成現行功能。`UseItemCallback` 的 26.2 API 查證仍保留作歷史知識，但不再是本模組 runtime 依賴。

### 下一步

下一個主線功能建議回到 **市政廳殖民地歸屬切片**：同維度、Town Hall 半徑 64 內的有效住宅與 warehouse 綁定 Town Hall `colony_id`；沒有核心、超出範圍或核心衝突時明確標記為未綁定。完成後再建立 ResidentRecord，讓居民、住宅與工作建築具備穩定的殖民地資料關係。

### 來源

- [1] https://docs.fabricmc.net/develop/events — Fabric Events 26.2。
- [2] https://docs.fabricmc.net/develop/items/custom-item-interactions — Fabric Custom Item Interactions 26.2。
- [3] 本機 Minecraft 26.2／Fabric API 0.158.0+26.2 compile 與 `build` 驗證，日期 2026-08-21。


## 2026-08-21 — Town Hall 可設定範圍與殖民地 colony_id 歸屬切片

### 變更

本次將市政廳規則由「每個維度只能一座」調整為「每座市政廳擁有自己的可設定範圍」。同一維度可以有多座 Town Hall，但新核心的 cubic range 不得與既有核心範圍重疊；不同維度的核心可以並存。Town Hall 預設半徑維持 64，最大半徑為 512。

新增同維度 AABB／cubic range overlap 判定。相同 marker 重掃仍回傳 `EXISTING`；新核心與任何既有核心重疊時回傳 `DUPLICATE`，不寫入；範圍不重疊的新核心回傳 `REGISTERED`。新增 `/civitas townhall radius <index> <radius>`，並由 `/civilization` 相容別名共用相同 handler、驗證與 Tab completion；半徑更新若會與其他核心重疊則拒絕並保留舊值。

有效住宅與 warehouse 現在會依同維度及 marker 座標尋找唯一 Town Hall。成功時將 `colony_id` 與 `colony_binding_reason=bound` 寫入 `BuildingObservation`；沒有市政廳、超出所有市政廳範圍或位於重疊範圍時，分別保存 `no_town_hall`、`outside_town_hall`、`overlapping_town_hall`，不參與居民導航、warehouse 物流、`assign` 或通用綁定裝置。Town Hall 自身若與既有範圍衝突，保留 observation 但標記 `duplicate_town_hall` invalid。

`BuildingObservation` 新增 `colony_id` 與 `colony_binding_reason`，放入既有 nested `CodecTail` 的 optional fields，保留舊 resident、roster 與 storage SavedData 相容性。新增或調整 Town Hall 半徑後會即時刷新既有 building 的 colony binding；legacy building 的舊 `status=bound` 可讀取，但沒有 colony_id 時不視為 `isColonyBound()`。

### 影響檔案

- `src/main/java/com/civilizationmod/TownHallCore.java`：新增 `MAX_RADIUS`、`overlaps` 與半徑更新 helper。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：支援多核心註冊、半徑更新、Town Hall binding resolver、SavedData refresh 與同 marker 去重。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：新增 colony_id／binding reason 與 optional CodecTail 欄位。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 Town Hall 半徑命令、Tab completion、inspect/list colony 顯示與 assign colony gate。
- `src/main/java/com/civilizationmod/BuildingResidentService.java`：只讓 colony-bound 建築執行居民導航。
- `src/main/java/com/civilizationmod/BuildingLogisticsService.java`：只讓 colony-bound warehouse 執行入庫物流。
- `src/main/java/com/civilizationmod/ResidenceBindingDeviceInteraction.java`：只允許 colony-bound 有效建築進行居民綁定。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增半徑、殖民地綁定與未綁定原因英文。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文半徑、殖民地綁定與未綁定原因。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步繁體中文 locale 政策。
- `src/test/java/com/civilizationmod/CivitasCoreTest.java`：新增多核心不重疊、半徑更新拒絕重疊、跨維度並存、Town Hall binding status 與 BuildingObservation colony Codec 測試。
- `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md` 與全域 Minecraft skill：同步可重用規則。

### 查證與驗證

- 版本：Minecraft 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Loom 1.17.19、Gradle 9.5.1、Java 25。
- `gradlew.bat --no-daemon compileJava compileClientJava test --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon runDatagen --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon build --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon runClient --console=plain`：已確認 Fabric、Minecraft 26.2、Civitas common initializer、Indigo、Render thread 與 client initialization 正常；完成 smoke 後停止 client。
- JUnit：`CivitasCoreTest` 6 個案例通過，包含本次 Town Hall range／colony Codec 回歸。
- 先前完整 build 曾因 legacy `BuildingObservation.status=bound` 的測試預期失敗；已修正為保留舊 status、僅以 colony_id 判定 `isColonyBound()`，之後回歸測試與完整 build 通過。

### 未完成與風險

本次尚未建立 GameTest 來自動生成實際 ItemFrame、Town Hall、住宅與 warehouse 世界；遊戲內仍需人工確認多核心範圍與 marker scan。既有舊世界若已保存互相重疊的 Town Hall 核心，會被 binding resolver 標記為重疊狀態，玩家需以半徑命令調整其中一座；本次不會自動刪除核心或搬移殖民地資料。半徑目前是軸對齊立方體，不是圓形半徑，尚未加入 UI slider 或資料包設定。

### 下一步

在遊戲內驗收 `/civitas townhall radius 1 128`、跨範圍多核心、範圍外建築未綁定，以及範圍內住宅／warehouse 顯示相同 colony_id。驗收通過後，建立第一版 `ResidentRecord`／ResidentRegistry，將居民身份從單純 building roster 抽離，並保存居民的 colony_id、住宅、工作建築與 body type。


## 2026-08-21 — 市政廳 marker 改為無序合成

### 變更

依使用者新規則，市政廳 marker 不再要求固定排列。配方現在要求綠寶石、鐵劍、鐵鎬、鐵斧與鐵鏟各一份；五種材料可在合成台中任意排列。綠寶石仍是必要材料，也是 recipe advancement 的解鎖條件。

### 影響檔案

- `src/client/java/com/civilizationmod/client/CivilizationModRecipeProvider.java`：將 `town_hall_marker` 從 shaped recipe 改為 `shapeless`，使用五個指定 `ItemLike` 材料。
- `src/main/generated/data/civilizationmod/recipe/town_hall_marker.json`：datagen 生成 `minecraft:crafting_shapeless`，ingredients 為 emerald、iron_sword、iron_pickaxe、iron_axe、iron_shovel。
- `src/main/generated/data/civilizationmod/advancement/recipes/misc/town_hall_marker.json`：保留 recipe unlock，條件為持有綠寶石。
- `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md` 與全域 Minecraft skill：追加 26.2 `ShapelessRecipeBuilder` API 查證與配方規則。

### 查證與驗證

- 版本：Minecraft 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Loom 1.17.19、Gradle 9.5.1、Java 25。
- 26.2 common jar `javap` 確認 `ShapelessRecipeBuilder.requires(ItemLike)`、`requires(ItemLike,int)`、`unlockedBy(...)` 與 `save(...)` 公開存在。
- `gradlew.bat --no-daemon runDatagen --console=plain`：`BUILD SUCCESSFUL`，生成 recipe 與 advancement。
- `gradlew.bat --no-daemon test runFoodModelRegressionTest build --console=plain`：`BUILD SUCCESSFUL`；`CivitasCoreTest` 通過，`FoodModelRegressionTest: PASS`。
- 生成 JSON 已檢查，recipe type 為 `minecraft:crafting_shapeless`，五種材料各一份，輸出為 `civilizationmod:town_hall_marker`。

### 未完成與風險

尚未由玩家在遊戲內實際排列五種材料驗收；這是 recipe 資料變更，不影響既有 Town Hall SavedData 或 colony_id。若舊世界已取得舊 shaped recipe 的 advancement，重新載入後新 recipe ID 不變，玩家仍可使用同一 recipe 解鎖資料。

### 下一步

啟動 client 後，在合成台將綠寶石、鐵劍、鐵鎬、鐵斧與鐵鏟任意排列，確認可取得市政廳 marker；再回到 Town Hall 多核心範圍與 colony_id 的遊戲內驗收。


## 2026-08-21 — 將 Civitas 物品加入創造模式物品欄

### 變更

新增一個 common-side 的 `創世紀元 / Civitas` 自訂創造模式分頁，集中放入目前已註冊的 Civitas 物品：warehouse marker、住宅 marker 1/2/4/6、市政廳 marker、通用 Civitas 綁定裝置，以及保留相容性的 legacy `residence_binding_device`。分頁 icon 使用市政廳 marker，物品順序依 warehouse、住宅容量、市政廳、通用裝置與 legacy 裝置排列。

分頁標題使用 `Component.translatable("itemGroup.civilizationmod.civitas")`，並同步加入 `en_us`、`zh_tw` 與 `zh_cn`；後兩者維持專案政策的繁體中文。

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationItems.java`：建立並註冊 `CIVITAS_CREATIVE_TAB_KEY` 與 `CIVITAS_CREATIVE_TAB`，在 `initialize()` 加入 creative tab registry。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增英文分頁名稱。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文分頁名稱。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：同步繁體中文分頁名稱。
- `.cursor/skills/civitas-fabric-262/SKILL.md`、`skills/minecraft-civilization-fabric-262/SKILL.md` 與全域 Minecraft skill：同步官方文件與實際 Fabric API jar 的 creative tab 查證。

### 查證與驗證

- 版本：Minecraft 26.2、Fabric Loader 0.19.3、Fabric API 0.158.0+26.2、Loom 1.17.19、Gradle 9.5.1、Java 25。
- Fabric 官方文件確認 `FabricCreativeModeTab.builder()`、`displayItems(...)`、`Component.translatable(...)` 與 creative tab registry 流程：[Custom Creative Tabs 26.2](https://docs.fabricmc.net/develop/items/custom-creative-tabs)。
- 實際 `fabric-creative-tab-api-v1-5.0.14+d871b99e9e.jar` class list 確認 package 為 `net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab`；初次使用不存在的 `itemgroup.v1` package 時 compile 失敗，已依實際 jar 修正。
- `gradlew.bat --no-daemon compileJava compileClientJava --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon runDatagen --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon test runFoodModelRegressionTest build --console=plain`：`BUILD SUCCESSFUL`；`FoodModelRegressionTest: PASS`，JUnit 通過。
- 初次將 `test`、`runFoodModelRegressionTest`、`runDatagen` 與 `build` 放在同一次 Gradle invocation 時，Gradle 9.5 回報 `runDatagen` 與 `sourcesJar` 的 implicit dependency validation；將 datagen 與 build 分開執行後均成功，未修改 Gradle 以掩蓋問題。
- `gradlew.bat --no-daemon runClient --console=plain`：Fabric creative tab API、Civitas initializer、Indigo、Render thread 與 client initialization 成功；smoke test 完成後停止 client。Realms authorization 與開發環境 run directory cleanup 訊息不影響模組初始化。

### 未完成與風險

尚未由玩家在實際創造模式背包中點擊確認分頁與八個物品的視覺排序；這需要遊戲內人工驗收。自訂分頁目前只放入已註冊的 item registry，未加入未註冊的 future blocks 或自訂 ItemStack 變體。

### 下一步

啟動 `runClient`，進入創造模式開啟物品欄，確認出現 `創世紀元 / Civitas` 分頁，並確認 warehouse、四種住宅、市政廳、通用綁定裝置與 legacy 裝置都能取得。驗收後繼續 ResidentRecord／ResidentRegistry 資料層。


## 2026-08-21 — ResidentRecord／ResidentRegistry 第一版居民資料層

### 變更

完成第一版居民資料層，將 Civitas 居民的永久邏輯身份與目前 Minecraft body 分離。新增 `ResidentRecord` 與 `ResidentRegistry`；`residentId` 是永久身份，`entityUuid` 是目前原版 Villager body UUID。舊 `BuildingObservation.residents` roster 會在 `CivilizationWorldData` 載入後冪等遷移；第一階段 legacy UUID 同時作為 residentId 與 entityUuid，新 assign 則建立獨立的 residentId。

`ResidentRecord` 現在保存殖民地 ID、住宅 building key、工作 building key、角色、body type、lifecycle、診斷名稱、建立時間與最近觀測時間。建築關係使用 `dimension@x,y,z` marker key，不保存易變的一次性 building scan index。未載入 chunk 找不到 body 時不會清除 entityUuid，第一版也不自動復活死亡原版村民。

`CivilizationWorldData` 的 SavedData Codec 新增 optional `resident_registry` 欄位，schema version 由 7 升至 8。`BuildingResidentService` 改為優先使用 registry 的 assigned building key 與 entityUuid 導航、套用角色外觀及執行 warehouse 物流；`findBuildingAssignedTo(String)` 保留 legacy roster fallback。`/civitas assign`、`/civitas unassign` 與通用綁定裝置會同步更新 registry。

新增 `/civitas resident list`，並由既有共用命令樹自動提供 `/civilization resident list` 相容別名。列表顯示 residentId、body UUID、colony、住宅、工作、角色、body type、lifecycle 與名稱；三份 locale 均已加入翻譯 key。

### 影響檔案

- `src/main/java/com/civilizationmod/ResidentRecord.java`：新增永久居民記錄、雙 UUID、建築 key、角色與 lifecycle Codec。
- `src/main/java/com/civilizationmod/ResidentRegistry.java`：新增 canonical list、legacy roster migration、lookup、assignment upsert 與解除指派。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：SavedData optional registry、schema 8、遷移與 resident lookup／assignment API。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：assign／unassign 同步 registry，新增 `resident list` 命令。
- `src/main/java/com/civilizationmod/BuildingResidentService.java`：改用 registry 解析居民並保留物流閉環。
- `src/main/java/com/civilizationmod/ResidenceBindingDeviceInteraction.java`：綁定裝置同步建立／更新 ResidentRecord。
- `src/test/java/com/civilizationmod/CivitasCoreTest.java`：新增 legacy migration 冪等、雙 UUID 分離、building key 與 Codec round-trip 測試。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增 resident list 英文訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增 resident list 繁體中文訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依專案政策新增相同繁體中文訊息。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加 ResidentRecord、ResidentRegistry 與 26.2 API 查證規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步技能副本。
- `Mods.md`：追加本次進度。

### 查證與驗證

- 版本：Minecraft `26.2`；Fabric Loader `0.19.3`；Fabric API `0.158.0+26.2`；Fabric Loom 實際 `1.17.19`；Gradle `9.5.1`；Java toolchain `25`。
- 本機 JDK 25 `javap` 已確認 `Entity.getUUID()`、`Entity.setUUID(UUID)`、`ServerLevel.getEntityInAnyDimension(UUID)`、`ServerLevel.getDataStorage()`、`net.minecraft.world.level.storage.SavedDataStorage.computeIfAbsent(SavedDataType<T>)` 與 `SavedDataType(Identifier, Supplier<T>, Codec<T>, DataFixTypes)`。
- 明確禁止使用未查證的 `level.getEntity(UUID)`。
- `gradlew.bat --no-daemon compileJava compileClientJava --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon test runFoodModelRegressionTest --console=plain`：JUnit 通過，`FoodModelRegressionTest: PASS`。
- `gradlew.bat --no-daemon build --console=plain`：`BUILD SUCCESSFUL`，check、jar、assemble 均通過。

### 未完成與風險

- 尚未由遊戲內手動驗收 `/civitas resident list` 的文字、`/civilization resident list` 相容別名與 Tab completion。
- 尚未以實際既有世界重開確認 `resident_registry` 在 `civilization_world.dat` 中保存並成功遷移；本次已通過 Registry Codec 與編譯／測試，但尚未建立 GameTest 的 SavedData reload 情境。
- ItemFrame 真實掃描、居民重新進入世界後的 body lookup、村民死亡／移除 lifecycle、body rebind 與多居民住宅的完整 gameplay 仍待後續驗收。
- `BuildingObservation.residents` 仍保留作為舊世界與相容層資料；下一階段才考慮將新的 assign 寫入改為 residentId-aware roster，而不應直接刪除 legacy 欄位。

### 下一步

在遊戲內驗收 `/civitas resident list` 與 `/civilization resident list`；確認既有住宅／warehouse 指派不重複建立居民、重開世界後列表仍存在，再進入 `ResidentRecord` 的 lifecycle 更新與住宅／工作雙重關係查詢。暫時不建立自訂 NPC Entity。

### 來源

- [1] Fabric Saved Data 26.2：https://docs.fabricmc.net/develop/saved-data
- [2] Fabric Commands 26.2：https://docs.fabricmc.net/develop/commands/basics
- [3] Minecraft 26.2 common jar：`C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`，以 JDK 25 `javap` 查證，2026-08-21。


## 2026-08-21 — 修正村民死亡後住宅容量未釋放

### 根因

使用者提供的 `latest.log` 已確認 server-side Villager 確實死亡，例如 `21:16:07`、`21:16:16` 與 `21:16:18`。原本 `BuildingResidentService` 對居民只執行導航與物流；當死亡 body 不再能被 lookup 時直接跳過，沒有更新 ResidentRecord lifecycle，也沒有從 `BuildingObservation.residents` 移除該 entity UUID。因此住宅仍計算死亡居民，容量不會釋放。latest.log 沒有出現 Civitas exception，問題是缺少死亡生命週期同步，而不是啟動或存檔錯誤。

### 變更

已由本機 Fabric API 0.158.0+26.2 的 `fabric-entity-events-v1` jar 查證 `ServerLivingEntityEvents.AFTER_DEATH` 與 `AfterDeath.afterDeath(LivingEntity, DamageSource)`，並在 common-side `BuildingResidentService.register()` 註冊 server death callback。callback 只處理 server-side 原版 Villager，避免 client/server 重複處理。

新增 `CivilizationWorldData.markResidentDead(UUID, long)`。死亡時會將 ResidentRecord 的 lifecycle 設為 `dead`，保留 `residentId`、`entityUuid`、`colonyId` 與診斷名稱；同時清除 home building、work building 與 role，並從所有 `BuildingObservation.residents` roster 移除該 entity UUID，最後呼叫 `setDirty()`。因此死亡居民仍保留歷史身份，但不再占用住宅容量，也不會繼續導航或執行 warehouse 物流。

新增 `ResidentRecord.withDeath(long)` 與 `CivilizationWorldData.addBuildingObservation(...)` 測試登記入口；新增 JUnit 回歸測試確認死亡居民保留雙 UUID 與 lifecycle 歷史，而住宅 roster 數量由 2 降至 1。

### 影響檔案

- `src/main/java/com/civilizationmod/BuildingResidentService.java`：註冊 `ServerLivingEntityEvents.AFTER_DEATH`。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：新增 `markResidentDead` 與建築 observation 測試登記入口。
- `src/main/java/com/civilizationmod/ResidentRecord.java`：新增 `withDeath` lifecycle 轉換。
- `src/test/java/com/civilizationmod/CivitasCoreTest.java`：新增死亡容量釋放與歷史身份回歸測試。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加死亡 callback 與 chunk unload 邊界規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步死亡 callback 與住宅容量規則。
- `Mods.md`：追加本次 bug 修正。

### 查證與驗證

- 版本：Minecraft `26.2`；Fabric Loader `0.19.3`；Fabric API `0.158.0+26.2`；Fabric entity-events `5.0.5+06488ac19e`；Gradle `9.5.1`；Java toolchain `25`。
- `ServerLivingEntityEvents` javap：確認公開 `AFTER_DEATH`。
- `ServerLivingEntityEvents$AfterDeath` javap：確認 `afterDeath(LivingEntity, DamageSource)`。
- `gradlew.bat --no-daemon compileJava compileClientJava --console=plain`：`BUILD SUCCESSFUL`。
- `gradlew.bat --no-daemon test runFoodModelRegressionTest --console=plain`：JUnit 通過，`FoodModelRegressionTest: PASS`。
- `gradlew.bat --no-daemon build --console=plain`：`BUILD SUCCESSFUL`，check、jar、assemble 均通過。

### 未完成與風險

尚未在實際 Minecraft 世界中重新驗收死亡 callback。這次刻意沒有在每 20 tick 中把 lookup 不到的 Villager 直接判為死亡，因為 chunk unload 不等於死亡；死亡使用已查證的 server death callback。非死亡的 entity removal、卸載後永久遺失與 body rebind 仍需另外設計與查證。

### 下一步

啟動遊戲，在一間容量為 2 的住宅中綁定兩名村民；先執行 `/civitas building inspect <index>` 記錄居民數，殺死其中一名 Villager，等待一秒後重新執行 `building inspect` 與 `/civitas resident list`，確認住宅居民數下降、死亡居民 lifecycle 為 `dead`、residentId 仍存在。之後再測試死亡後重新指派新 Villager 是否能重新使用釋放的容量。

### 來源

- [1] Fabric Events 26.2：https://docs.fabricmc.net/develop/events
- [2] Fabric API entity-events-v1 0.158.0+26.2：`fabric-entity-events-v1-5.0.5+06488ac19e.jar`，JDK 25 `javap` 查證日期 2026-08-21。
- [3] 使用者提供的 `latest.log`：server-side Villager death 記錄位於 21:16:07、21:16:16、21:16:18。


## 2026-08-21 — README.md 同步目前 Civitas 殖民地建設狀態

### 變更

重新整理 README.md，使公開專案說明與目前實際程式狀態一致。README 現在以殖民地建設為主線，說明 Town Hall 殖民地核心、可設定且不可重疊的多核心範圍、住宅與 warehouse 的 `colony_id` 歸屬、ItemFrame marker 與 territory、warehouse storage snapshot、安全 allowlist 物流、27 格 Civitas Villager backpack、居民 `ResidentRecord`／`ResidentRegistry`、村民死亡後釋放住宅容量，以及目前仍未完成的農田、工作站、自訂 NPC 與完整 lifecycle。

同步修正舊版 README 中把 Town Hall 寫成「尚未實作」、把住宅描述成「仍沿用單一 resident」以及沒有居民永久身份與死亡容量資訊的過時內容。README 也補充目前命令表、Tab completion 政策、Java 25 建置命令、`runDatagen` 與 `build` 分開執行的 Gradle 注意事項、遊戲內死亡容量驗收流程、Git 上傳範圍與官方參考連結。

### 影響檔案

- `README.md`：完整同步目前公開專案說明與驗收指引。
- `Mods.md`：追加本次 README 狀態同步紀錄。

### 文件內容重點

README 保留 AI 輔助生成免責聲明，並以表格整理目前系統狀態。已明確標示：Town Hall 與 `colony_id` 程式切片已完成但多核心完整人工驗收仍待確認；ResidentRecord／ResidentRegistry 與死亡容量修正已完成自動化驗證但需要遊戲內重新驗收；外部村莊 adapter、自訂 NPC Entity、農田與工作站仍未完成。

README 的 References 使用 Fabric 官方 Saved Data 與 Events 文件，不把尚未遊戲內驗收的項目宣稱為完整 gameplay 成功。[1] [2]

### 驗證與下一步

本次修改為 Markdown 文件，不改變 runtime 程式碼。提交前執行 README 內容檢查、`git diff --check` 與既有 `gradlew.bat --no-daemon build --console=plain`；若 build 成功，提交並推送到 `origin/main`。下一步仍是遊戲內驗收死亡居民釋放住宅容量，以及 Town Hall 多核心範圍內外的 colony binding。

### 來源

- [1] Fabric Saved Data：https://docs.fabricmc.net/develop/saved-data
- [2] Fabric Events 26.2：https://docs.fabricmc.net/develop/events
- [3] 專案目前程式碼、測試結果與 `Mods.md` 歷史，日期 2026-08-21。


## 2026-08-21 — 公開命令整理與 `/civitas help`

### 設計決策

依目前殖民地主線，公開命令樹不再暴露早期文明模擬與測試用命令。已移除 `/civitas settlement`、`/civitas scan`、`/civitas sc`、`/civitas simulate` 與 `/civitas building generate` 的命令註冊。FoodDemandModel、SettlementAdapter 與 deterministic building generator 保留在程式及測試層，作為既有資料模型、回歸測試或未來重新設計的內部能力，但不再被當成目前玩家功能。

新增 `/civitas help`。Help 由 common-side command handler 逐行透過 `CivilizationMessages.translatable(...)` 發送，說明目前仍可用的 status、building、assign、unassign、resident 與 Town Hall 命令；每行都會保留金色品牌前綴。`/civilization help` 由同一個 root builder 自動提供相容版本。Help 只使用 literal，不新增未提供 suggestions 的自由文字參數。

目前公開命令如下：

| 命令 | 狀態 |
|---|---|
| `/civitas help` | 新增，顯示使用說明。 |
| `/civitas status` | 保留，查詢文明與 SavedData 摘要。 |
| `/civitas building scan/list/inspect` | 保留，服務目前 marker／殖民地主線。 |
| `/civitas townhall` 與 `townhall radius` | 保留，管理 Town Hall core 與 colony binding。 |
| `/civitas assign/unassign` | 保留，管理居民建築指派。 |
| `/civitas resident list` | 保留，查詢 ResidentRecord。 |
| `/civilization ...` | 保留，作為相容命令根。 |

### 影響檔案

- `src/main/java/com/civilizationmod/CivilizationCommands.java`：新增 help handler，移除 settlement、早期村莊 scan、sc、simulate 與 building generate 的公開註冊及無用 handler／suggestions。
- `src/main/resources/assets/civilizationmod/lang/en_us.json`：新增英文 help 訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_tw.json`：新增繁體中文 help 訊息。
- `src/main/resources/assets/civilizationmod/lang/zh_cn.json`：依專案政策同步繁體中文 help 訊息。
- `src/test/java/com/civilizationmod/CivitasCoreTest.java`：新增命令樹回歸測試，確認 help、building、townhall、resident 存在，舊 simulate／scan／settlement／sc 不存在，且兩個命令根都已註冊。
- `README.md`：同步目前公開命令表、移除舊命令說明並補充 help 使用方式。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加公開命令整理與 help 規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步技能副本。
- `Mods.md`：追加本次命令整理進度。

### 查證與驗證

- 26.2 Brigadier literal 命令樹沿用現有可編譯的 command registration；help、alias 與動態建築／Town Hall index suggestions 均由既有命令 builder 管理，沒有新增未查證的 command API。
- `compileJava compileClientJava processResources`：`BUILD SUCCESSFUL`。
- 三份 locale 使用明確 UTF-8 解析：`en_us.json`、`zh_tw.json`、`zh_cn.json` 均通過 JSON 解析。
- `test runFoodModelRegressionTest`：`BUILD SUCCESSFUL`；`FoodModelRegressionTest: PASS`，命令樹回歸測試通過。
- `build`：在 help 實作與 locale 更新後通過，check、jar、assemble 與 sourcesJar 均成功。

### 未完成與下一步

尚未在實際 Minecraft client 中輸入 `/civitas help`、`/civilization help`，也尚未手動確認 help 每一行品牌前綴的顏色、換行與 Tab completion；這是本次唯一尚待遊戲內驗收的命令功能。README 已標示新的公開命令表。下一步先完成 help 遊戲內驗收，再回到居民死亡容量與 Town Hall 多核心範圍的人工驗收。

### 來源

- [1] Fabric Commands 26.2：https://docs.fabricmc.net/develop/commands/basics
- [2] Fabric Command Arguments 26.2：https://docs.fabricmc.net/develop/commands/arguments
- [3] 專案現有 `CivilizationMessages`、命令樹 JUnit 與 Gradle build，日期 2026-08-21。


## 2026-08-21 — 收斂 ResidentRegistry 與 legacy roster 雙寫資料源

### 根因與架構決策

目前 `BuildingObservation.residents`／`resident_uuid` 與 `ResidentRegistry` 同時保存居民關係，確實存在 assign 一邊成功、另一邊失敗、查詢路徑分叉及除錯時真相不明的風險。本次依架構建議停止加深雙寫：`ResidentRegistry` 現在是所有新居民 assignment 的唯一 canonical 寫入與持久化來源；`BuildingObservation` roster 仍保留於 Codec，只作舊世界遷移與相容讀取。

`CivilizationWorldData.get(...)` 現在只執行冪等 `ResidentRegistry.migrateLegacy(buildings)`。它只把 canonical registry 缺少的 legacy roster assignment 補入 registry，不再呼叫會由 roster 反向覆蓋 canonical assignment 的 `refreshAssignmentsFromBuildings`。既有 ResidentRecord 優先，legacy roster 不會覆蓋 active、cleared 或 dead 居民資料。

### Runtime 行為變更

`/civitas assign`、`/civitas unassign` 與通用 Civitas 綁定裝置不再呼叫 `withResident`、`withAddedResident`、`withoutResident` 或 `replaceBuilding` 來保存居民關係。它們統一透過 `ensureResidentAssignment` 與 `clearResidentAssignment` 寫入 registry；同一個 active entity 若已指向另一個 building key，新的 assignment 會被拒絕。

住宅容量、building inspect 居民名稱、居民導航、warehouse 物流與死亡狀態現在以 registry-derived active assignment 查詢。新增 `countActiveAssignedTo`、`findActiveAssignedTo` 及 WorldData wrappers；死亡居民 lifecycle 為 `dead` 後立即不計入容量。死亡流程不再刪除 legacy roster，避免以新的 lifecycle 再次寫入舊資料源；居民永久身份與歷史 body UUID 仍然保留。

移除 warehouse、住宅或其他已登記 marker 時，`removeBuilding` 與 `removeMissingBuildings` 會透過 `clearAssignmentsForBuilding` 清除 active registry building relationship，但不刪除 ResidentRecord，也不改寫 legacy roster。這避免 marker 消失後 registry 留下失效 building key。

`BuildingObservation` 的 roster mutation helpers 已標記為 legacy compatibility API，要求後續新功能不得使用。建築重掃與 Codec 仍可保留原有 roster 內容以維持舊世界相容；住宅容量 validation 已改用 registry active count，而非 `observation.residentCount()`。

### 影響檔案

- `src/main/java/com/civilizationmod/ResidentRegistry.java`：移除 roster-driven refresh API；新增 active assignment count、查詢與建築關係清除。
- `src/main/java/com/civilizationmod/CivilizationWorldData.java`：移除 load-time roster refresh、取消 lookup fallback、加入 registry-only assignment／capacity／building removal 清理，死亡不再寫 roster。
- `src/main/java/com/civilizationmod/CivilizationCommands.java`：assign／unassign 改用 registry canonical path；building inspect 的居民與住宅容量改用 active registry records。
- `src/main/java/com/civilizationmod/ResidenceBindingDeviceInteraction.java`：移除 direct roster mutation，綁定與容量訊息改用 registry。
- `src/main/java/com/civilizationmod/BuildingObservation.java`：legacy roster mutation helpers 加上相容 API 標記與使用限制說明。
- `src/test/java/com/civilizationmod/CivitasCoreTest.java`：新增 registry-only assignment／unassign 測試，死亡容量測試改驗證 legacy roster 保持、active registry count 下降及 dead lookup 不再返回建築。
- `README.md`：同步唯一資料源與 legacy roster 邊界。
- `.cursor/skills/civitas-fabric-262/SKILL.md`：追加 registry-only 規則。
- `skills/minecraft-civilization-fabric-262/SKILL.md`：同步 registry-only 規則。
- `Mods.md`：追加本次架構收斂記錄。

### 查證與驗證

- source audit：production caller 不再呼叫 `withResident`、`withAddedResident`、`withoutResident` 或 `refreshAssignmentsFromBuildings`；這些只剩 BuildingObservation legacy API／測試與遷移相容用途。
- `gradlew.bat --no-daemon test runFoodModelRegressionTest --console=plain`：`BUILD SUCCESSFUL`；新增 registry-only 測試與既有測試通過，`FoodModelRegressionTest: PASS`。
- `gradlew.bat --no-daemon build --console=plain`：`BUILD SUCCESSFUL`；compileJava、compileClientJava、resources、check、jar、assemble 均通過。
- common/server 邊界保持不變；本次沒有新增 client-only 類別或未查證的 Fabric 26.2 API。

### 未完成與風險

舊世界的 legacy roster 欄位仍會保存，這是刻意保留的相容策略，不代表它仍是新功能的寫入真相。若未來要移除欄位，必須先建立舊世界實際載入／重開與 migration telemetry，再另開 schema migration 切片。遊戲內尚未重新驗收：新 assign 不改 roster、住宅容量以 registry 計算、刪除 marker 後 registry assignment 清除，以及死亡後 legacy roster 保持但 active capacity 釋放。

### 下一步

在遊戲內建立一間容量為 2 的住宅並指派兩名村民，執行 `building inspect` 確認容量來自 active registry；解除其中一名後確認容量由 2 降至 1 且 legacy roster 不會造成重複；再殺死一名居民與拆除 marker，分別確認 registry lifecycle／building assignment 正確清理。驗收通過後再進入 body rebind 與完整 resident lifecycle，不要重新引入 roster 雙寫。

### 來源

- [1] Fabric Saved Data：https://docs.fabricmc.net/develop/saved-data
- [2] Fabric Events 26.2：https://docs.fabricmc.net/develop/events
- [3] 專案 `ResidentRegistry`、`CivilizationWorldData`、`CivitasCoreTest` 與實際 Gradle build，查證日期 2026-08-21。
