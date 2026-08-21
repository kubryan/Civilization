---
name: minecraft-civilization-fabric-262
description: Minecraft Java Edition 26.2 Fabric 文明模擬模組的長期開發工作流。用於建立、修改、移植、除錯或規劃整合型文明模組，尤其是外部模組村莊適配、村莊起始點、玩家身份、食物需求、世界資料保存、server/client networking、datagen 與 dedicated server 驗證；每次修改後都必須更新專案根目錄的 mods.md。
---

# Minecraft Civilization Fabric 26.2

## 目標

以 Minecraft Java Edition 26.2 與 Fabric 為目標，建立可長期擴充的文明模擬框架。文明聚落可來自原版村莊、其他模組村莊或本模組資料驅動內容；玩家可以選擇觀察者／干預者、領袖、居民或獨立者，並在遊戲內依聲望、資源、事件與政治條件轉換身份。

## 不可違反的規則

1. **不要猜版本敏感 API。** 先查 Minecraft 官方、Fabric 官方文件、Fabric Example Mod、26.2 mappings 或實際編譯結果；不能把舊版、NeoForge、論壇或第三方模組的類別名稱直接當成 Fabric 26.2 API。
2. **每次修改都更新專案根目錄的進度檔。** 先讀取專案根目錄的 `mods.md`；若檔案系統已存在 `Mods.md`、`MODS.md` 或其他大小寫版本，沿用現有檔名，不要建立重複檔案。完成修改、測試與決策後，在同一次工作中追加日期、修改內容、影響檔案、驗證結果、未完成事項與下一步。若檔案不存在，才在實際 mod 專案根目錄建立 `mods.md`。不要把 Manus 共享檔案當作實際程式專案的唯一進度檔。
3. **修改流程不可省略進度記錄。** 修改順序固定為：讀取專案根目錄現有的 `Mods.md`／`mods.md` → 查證必要文件 → 修改 → 執行最小驗證 → 更新同一個根目錄進度檔 → 回報結果。未更新實際專案根目錄的進度檔，不得宣稱本次修改完成。
4. **server 擁有真實狀態。** 文明、聚落、資源、事件、玩家身份與身份轉換由 logical server 驗證、保存與推進；client 只負責畫面、輸入與必要的摘要同步。
5. **優先公開 API。** 優先使用 Fabric events、Saved Data、commands、正式 networking、registry、tags 與 datagen；只有沒有合適 hook 才使用 Mixin，並記錄目標、side、注入點、相容性風險與回歸測試。
6. **保持 common/client/server 分層。** `src/main/java` 不得依賴 client-only 類別；GUI、HUD、renderer 與 client command 放在 `src/client/java`；server 狀態與資料模型放在 common/server 可用的程式碼。
7. **不把單一外部村莊模組列為硬依賴。** 以 settlement adapter、設定、資料包或 tags 整合不同來源；外部模組只作可選相容性測試目標。
8. **所有命令與參數必須支援 Tab completion。** 使用 Brigadier literal、sub-command 與已查證的 argument type 建立可補全命令樹；動態資料使用已查證的 suggestion provider；權限受限節點使用 `requires()`，避免不具資格的節點出現在補全結果。不得加入只能接受任意字串、沒有補全與驗證的參數。每次新增命令都要實測根命令、子命令前綴、參數與權限差異。
9. **所有玩家可見文字必須本地化。** 使用 `Component.translatable` 與 locale JSON，不要在 Java、GUI、HUD、事件或命令中硬編碼玩家可見訊息。`en_us` 使用英文；`zh_tw` 使用台灣繁體中文；`zh_cn` 依專案政策也使用與 `zh_tw` 相同的繁體中文。
10. **所有傳給玩家的訊息必須經過品牌包裝器。** 使用共用 `CivilizationMessages`，中文格式為 `|創世紀元| : {模組訊息}`，英文格式為 `|Civitas| : {模組訊息}`；只有前綴使用金色，正文維持一般樣式，不再加入尾端品牌。命令、事件、GUI、HUD、身份、食物與錯誤訊息不得直接發送未包裝文字；console logger 不屬於玩家訊息。

## 查證後寫回 skill

每次 javap、官方文件或編譯確認了新的 API／規則，必須在同一次工作追加到本檔或 `references/`，並同步 `.cursor/skills/civitas-fabric-262/`。已有相同結論只補差異。推測不得寫成已確認知識。先讀 skill 再決定要不要重查。

## 固定工作流

### 1. 接收任務與讀取上下文

先確認 Minecraft 版本、Edition、Java、Fabric Loader、Fabric API、Loom、Gradle、mappings、side、專案路徑與最近的 `Mods.md`／`mods.md`。若版本未提供，使用 Minecraft Java Edition 26.2 作為明確假設，並把 Loader、API、Loom、Gradle 與 Java 分開標記為未確認。

先分類任務是：架構規劃、建立專案、聚落適配、玩家身份、模擬規則、資料保存、networking、client UI、worldgen、datagen、移植、除錯或伺服器測試。只載入對應 reference，不要把所有舊範例混在一起。

### 2. 查證外部資料

版本或 API 敏感時，優先查以下來源：

| 用途 | 優先來源 |
|---|---|
| Minecraft 版本與 vanilla 行為 | Minecraft 官方 26.2 公告 |
| Fabric 工具鏈與移植 | Fabric 26.2 公告、Fabric Develop、Fabric Docs、Fabric Example Mod |
| Saved Data、events、commands、networking、datagen | 26.2 Fabric Docs 對應頁面 |
| 外部模組功能與版本支援 | Modrinth／CurseForge 頁面，只作第三方產品事實與相容性線索 |

每項外部結論要記錄 URL、標題、版本與查證日期。若官方文件與第三方頁面衝突，以目標版本的官方文件為 API 依據；第三方模組只能證明「有人實作過某功能」，不能證明本模組可以直接呼叫相同 API。

### 3. 設計文明核心

先定義可持久化的資料模型，再定義內容。最小核心應包含：世界狀態、聚落、資源、需求、穩定度、玩家身份、事件序號與資料版本。第一個可玩閉環採用一座聚落、四種玩家身份、食物需求、少量資源、一個政策與少量事件。

文明模擬採低頻率時間步進。不要讓所有 NPC 每 tick 執行完整 AI；先用聚落／區域級摘要模擬，玩家接近時才提高細節。每次步進要有運算上限、事件原因與可查詢結果。

### 4. 整合外部模組村莊

將聚落來源抽象成 `SettlementAdapter`，至少保存來源 namespace、structure ID 或 tag、維度、中心位置、辨識時間、人口來源、可用資源與適配狀態。不能只依賴固定 `minecraft:village`；應支援可設定的 structure tag 或資料驅動適配器。

第一版優先掃描已生成的村莊，再建立 Saved Data 適配記錄；不要同時重寫其他模組的 worldgen。未知結構不得自動判定為文明聚落。外部村莊模組要用獨立相容性測試驗證，不能讓核心在缺少該模組時無法啟動。

### 5. 新世界起始村莊

把功能拆為兩層：

1. **搜尋既有村莊。** 新世界建立後依設定 tag、搜尋範圍與時間限制尋找村莊，找到後設定起始位置；找不到時使用明確 fallback 並記錄原因。
2. **保證起點生成村莊。** 這屬於後續 worldgen／structure 階段，必須先查證 26.2 正式 API，不能用猜測的事件或方法名提前實作。

起始村莊搜尋必須可關閉、可設定、可測試；不能無限搜尋、阻塞主執行緒或破壞已存在的世界。

### 6. 玩家身份與操作

身份由 server 保存並驗證，不等同 OP 權限：

| 身份 | 第一版可驗證操作 |
|---|---|
| 觀察者／干預者 | 查詢聚落摘要、協助或破壞事件 |
| 領袖 | 選擇一項政策、管理有限資源 |
| 居民 | 提交工作、捐獻或取得聚落配給 |
| 獨立者 | 進行聚落外交易、建立聲望 |

身份可以在遊戲內轉換，但必須有條件、事件或服務層，不允許 client 自行改值。先以 server command 驗證，再建立 client Screen；GUI 與 command 必須共用同一個 `IdentityService`。

### 7. 命令與 Tab completion

使用 `CommandRegistrationCallback.EVENT` 在 server 命令樹建立時註冊所有命令。Brigadier literal 與子命令節點會提供基本補全；新增參數時，選用 Minecraft／Brigadier 已查證的 argument type，對資料驅動值提供 suggestions，並在節點上放置必要的 `requires()`。命令樹在登入時同步給 client，因此避免 runtime 動態加入或移除命令；若不得不動態修改，必須查證並重新同步 command tree。

每個命令切片至少在遊戲內驗證：`/civilization <Tab>`、子命令前綴加 `<Tab>`、每個參數的 `<Tab>`、無權限玩家的補全結果，以及錯誤輸入不會繞過 server 驗證。主要命令根採用 `/civitas`，並暫時保留 `/civilization` 作為相容別名。兩個根命令都要維持相同子命令、Tab completion、server 驗證、權限與翻譯。其他命令縮寫應註冊為額外 literal alias，與完整命令共用同一個 builder／handler、參數、suggestions、權限與翻譯 key，避免兩套命令樹分歧。官方參考：[7] [12] [13]。

### 8. 本地化與語言資源

在 `src/main/resources/assets/civilizationmod/lang/` 建立 `en_us.json`、`zh_tw.json` 與 `zh_cn.json`。玩家可見命令、事件、錯誤、GUI 與 HUD 使用 `Component.translatable`；翻譯參數使用已查證的格式化 placeholder。`zh_cn` 不翻成簡體中文，而是複製 `zh_tw` 的繁體中文內容。所有訊息最後再由 `CivilizationMessages` 加上 locale 金色品牌前綴；正文維持一般樣式，不加入尾端品牌，不得讓任何玩家訊息繞過包裝器。新增 key 後要在三份 locale 檔同步維護，並在遊戲內切換 locale 測試。

官方參考：[14] [15]。

### 9. 實作與驗證順序

按以下順序切片，不要一次實作整個大型模組：

1. 建立 Fabric 26.2 最小骨架，通過 `./gradlew build`、client 啟動與 dedicated server 啟動。
2. 建立世界級 Saved Data、資料版本與 `/civilization status`。
3. 建立 `/civilization scan`，掃描設定的村莊來源並保存 `SettlementAdapter` 記錄。
4. 加入食物需求、資源消耗、穩定度與事件紀錄。
5. 加入四種身份與 server-side 權限差異。
6. 加入 `/civilization simulate` 與 `/civilization reset`，讓模擬可重現與可除錯。
7. 加入 networking payload、server 驗證與文明摘要同步。
8. 加入身份選擇 Screen、聚落摘要 UI 與必要 HUD。
9. 最後才加入新世界起始村莊、自然 worldgen、NPC AI、外交、戰爭與高成本 renderer。

每一個切片至少驗證：新世界、世界重開、單人、dedicated server、雙人連線、非法命令或封包、資料保存與失敗 fallback。沒有實際執行的命令要標記為「未驗證」。

## Fabric 模板產生器的選項決策

對文明模組的第一版，建議如下：

| 選項 | 建議 | 理由 |
|---|---|---|
| Kotlin 程式語言 | **不勾** | 目前以 Java 與 Fabric 26.2 官方範例為主，減少 Kotlin runtime 與團隊學習成本。 |
| 數據生成 | **勾選** | 需要 tags、翻譯、配方、loot、advancement 與日後資料驅動內容；Fabric 官方提供 `runDatagen`。 |
| 拆分客戶端和公共資源 | **勾選** | 文明狀態與命令在 common/server，Screen、HUD、renderer 在 client；可避免 dedicated server 載入 client 類別。 |
| Kotlin 建置腳本 | **不勾** | 使用 Groovy Gradle script 即可；它與 Kotlin 程式語言是分開的選項，現在不需要增加另一種 DSL。 |

因此你截圖中的選擇 **「Kotlin 程式語言不勾、數據生成勾、拆分客戶端和公共資源勾、Kotlin 建置腳本不勾」是正確的起點**。Fabric 官方建立專案文件也把 Kotlin、Kotlin buildscript 與 datagen 視為獨立的進階選項；datagen 啟用後通常會提供 `runDatagen` 與資料生成 entrypoint。[1] [2]

## 專案根目錄進度檔必填格式

每次修改後追加一筆，不覆寫既有歷史。若專案現有檔名是 `Mods.md`，就直接更新 `Mods.md`：

```markdown
## YYYY-MM-DD HH:MM — <修改標題>

### 變更
- <完成了什麼>

### 影響檔案
- `<path>`：<用途>

### 查證與驗證
- 版本：Minecraft=26.2；Loader/API/Loom/Gradle/Java=<已確認或未確認>
- 命令：`<command>`
- 結果：<通過、失敗或未執行>

### 未完成與風險
- <仍未查證的 API、side、相容性或效能問題>

### 下一步
- <下一個最小工作單位>

### 來源
- [1] <URL>
```

## 交付前檢查

交付前確認 `mods.md` 已更新；確認 `fabric.mod.json` 的 id、environment、entrypoints、depends 與 mixins；確認 common/client/server side；確認外部村莊不是硬依賴；確認 Saved Data、commands、networking 與 datagen 的 API 來自 26.2 對應文件或編譯結果；至少執行一次 build、啟動、datagen 或測試。若沒有程式專案，交付架構與選項建議，但不可宣稱模組已完成。

## References

[1]: https://docs.fabricmc.net/develop/getting-started/creating-a-project "Creating a Project 26.2"
[2]: https://docs.fabricmc.net/develop/data-generation/setup "Data Generation Setup 26.2"
[3]: https://docs.fabricmc.net/develop/getting-started/project-structure "Project Structure 26.2"
[4]: https://docs.fabricmc.net/develop/saved-data "Saved Data"
[5]: https://docs.fabricmc.net/develop/events "Events 26.2"
[6]: https://docs.fabricmc.net/develop/networking "Networking 26.2"
[7]: https://docs.fabricmc.net/develop/commands/basics "Creating Commands 26.2"
[8]: https://docs.fabricmc.net/develop/data-generation/tags "Tag Generation 26.2"
[9]: https://docs.fabricmc.net/develop/data-generation/features "Feature Generation 26.2"
[10]: https://fabricmc.net/2026/06/15/262.html "Fabric for Minecraft 26.2"
[11]: https://modrinth.com/project/KplTt9Ku "Village Spawn Point - Minecraft Mod"
[12]: https://docs.fabricmc.net/develop/commands/arguments "Command Arguments 26.2"
[13]: https://docs.fabricmc.net/develop/commands/basics "Creating Commands 26.2"
[14]: https://docs.fabricmc.net/develop/text-and-translations "Text and Translations 26.2"
[15]: https://docs.fabricmc.net/develop/data-generation/translations "Translation Generation 26.2"

## 建築幾何查證 reference

當任務涉及 `warehouse_marker`、ItemFrame、DoorBlock、門口 anchor、floor／room scan、AABB、容器範圍或 `BuildingObservation.validationStatus` 時，先讀 `references/building-geometry-26.2.md`。該 reference 保存 2026-08-20 以實際 Minecraft 26.2 common jar `javap`、ItemFrame bytecode 與 Gradle build 查證的 DoorBlock、ItemFrame、Level entity query、BlockState collision、VoxelShape、Blocks.CHEST、ItemStack component API 與目前第一版結構規則。正式 scan 必須保留 ItemFrame entity，使用 `frame.blockPosition().relative(frame.getDirection().getOpposite())` 驗證支撐牆；第一版要求恰好一個門附近合法 Marker、至少四格同一 Y 的連通地板、每個 floor column 上方有非空氣屋頂、門以外三面非空氣牆體與可由入口進入；warehouse 另需室內箱子。有效時 server 對 Marker 設定 `ENCHANTMENT_GLINT_OVERRIDE`，失效時移除，不替換或消耗 Marker。不要把 MineColonies／TekTopia／舊版或 NeoForge API 當作 Fabric 26.2 簽名。

當需要對照 TekTopia 的房屋幾何判定時，再讀 `references/tektopia-structure-detection.md`。已查證的重點是：TekTopia 從門內側一格開始做有界四方向 flood fill；每個 floor tile 動態向上尋找第一個不可通行方塊，要求至少兩格 clearance，最多 500 個 floor tiles、房高搜尋上限 30；它不是以固定 `doorY + constant` 或 `isFaceSturdy` 作為唯一 ceiling 條件。該 reference 同時列出原始碼與 Wiki URL，以及不要移植舊版 Forge/MCP API 到 Fabric 26.2 的限制。


## 已查證：食物需求切片與 Fabric 26.2 保存策略（2026-08-19）

- 食物需求第一版採聚落級摘要模擬，不查詢每個 NPC，也不把外部村莊模組列為硬依賴。每次手動模擬步進以 `population × per-capita food demand` 計算需求，先從保存的 `food_stock` 扣除；不足量形成 `food_shortage`，並增加 `stability_debt`、降低 `stability`。
- `SettlementAdapter` 應保存 `population`、`food_stock`、`stability_debt` 與 `stability`，並保留既有來源、維度、中心、辨識時間與狀態欄位。新增欄位使用 `optionalFieldOf(...).forGetter(...)` 並提供預設值，以利舊世界 Codec 載入；schema 版本持續由 `CivilizationWorldData` 管理。
- 模擬邏輯應放在 common/server 可用的純 Java 模型（例如 `FoodDemandModel` 與 `StabilityDebt`），由 `CivilizationWorldData` 更新每個聚落並呼叫 `setDirty()`；不要在 client 或每 tick 直接執行完整 NPC AI。
- `/civitas simulate` 與 `/civilization simulate` 必須共用同一個 builder／handler，使用已註冊的 Brigadier integer argument 與 suggestions；不引入未查證的 runtime command API。
- Fabric 官方 Saved Data 文件確認：Saved Data 由 `SavedData`、`SavedDataType`、Codec 與 `DimensionDataStorage.computeIfAbsent()` 組成，修改後必須 `setDirty()` 才會保存。Fabric 官方命令文件確認：server command 使用 `CommandRegistrationCallback`／Brigadier，command 執行結果回傳正整數表示成功，命令樹可用 literal、argument 與 suggestions。
- 查證來源（2026-08-19）：[Fabric Saved Data](https://docs.fabricmc.net/develop/saved-data)、[Fabric Creating Commands 26.2](https://docs.fabricmc.net/develop/commands/basics)、[Fabric Porting to 26.2](https://docs.fabricmc.net/develop/porting/)。


## 已採用：可重現聚落級食物步進演算法（2026-08-19）

第一版食物閉環以 `SettlementAdapter` 為單位，在手動 `/civitas simulate` 或未來低頻率自動步進時執行；不掃描每個村民、不執行每 tick 個體 AI，也不依賴外部村莊模組內部 API。相同的初始 Saved Data 與相同步數必須產生相同結果，不使用隨機數。

| 欄位／常數 | 第一版規則 |
|---|---|
| `food_stock` | `long`，抽象食物單位，扣除需求後不得小於 0 |
| `population` | `int`，第一版掃描 fallback 為 20 |
| `food_consumption` | `int`，每人每步消耗，預設 1，最小值 1 |
| `stability` | `0–100`，初始 100 |
| `stability_debt` | 由短缺量累積，受上限保護 |
| `last_food_event` | `stable`、`surplus` 或 `shortage`，保存為可除錯識別字串 |
| 短缺穩定度損失 | 每次有短缺固定損失 5，不按短缺量重複扣除 |
| 盈餘穩定度恢復 | 每次符合盈餘條件最多恢復 1 |
| 初始 `food_stock` | 新掃描聚落 fallback 為 100 |

單步計算順序固定為：先計算 `population × food_consumption`；再消耗 `min(food_stock, demand)`；剩餘需求成為 shortage，剩餘存量為新的 food stock；若有 shortage，增加 stability debt、穩定度固定減 5、事件為 `shortage`；若無 shortage 且扣除後存量大於 `demand × 2`，穩定度最多增加 1、債務減 1、事件為 `surplus`；其餘情況事件為 `stable` 且穩定度與債務不變。世界層完成所有聚落後才增加 `simulation_steps` 並呼叫 `setDirty()`。

食物資料升級為 `schema_version = 3`。新聚落欄位使用 `Codec.optionalFieldOf` 預設值，讓 schema 2 或更舊的 SettlementAdapter 資料可用人口 0、食物 0、消耗 1、穩定度 100、事件 stable 載入；重複 scan 不得重置既有食物、穩定度或事件。事件顯示必須把受控識別字串映射到三份 locale 的 translation key，再由 `CivilizationMessages` 套用金色前綴。

最低驗證情境是：新世界掃描一座村莊後得到人口 20、食物 100、穩定度 100；連續模擬五步後食物歸零且穩定度仍為 100；下一步開始出現 `shortage`、stability debt 增加與穩定度下降；世界重開後步數、食物、債務、穩定度與事件仍存在；再次 scan 同一位置不新增聚落、不重置數值。


## 已採用：純 Java 食物模型回歸測試任務（2026-08-19）

當 `FoodDemandModel` 與 `StabilityDebt` 維持 common-side 純 Java、無 Minecraft world context 時，優先建立不增加第三方測試框架依賴的 `src/test/java` 回歸檢查。測試類別可用 `main` 搭配明確 AssertionError，並由 Gradle `JavaExec` task 執行；測試必須鎖定需求 normalization、正常消耗、短缺、盈餘、stable 門檻、debt／stability clamp 與多步驟 deterministic 結果。

本專案使用 `runFoodModelRegressionTest`：依賴 `testClasses`、classpath 使用 `sourceSets.test.runtimeClasspath`、main class 為 `com.civilizationmod.FoodModelRegressionTest`，並透過 `javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }` 固定 Java 25 執行。Gradle 官方 toolchain 文件確認 `JavaLauncher` 可指定 JavaExec／Test task 使用的 Java executable；不要以目前執行 Gradle daemon 的 Java 版本取代專案 toolchain。

官方查證：[Gradle Toolchains for JVM projects](https://docs.gradle.org/current/userguide/toolchains.html)、[Gradle JavaExec DSL](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.JavaExec.html)，查證日期 2026-08-19。


## 已查證：Minecraft 26.2 原版村民人口 provider（2026-08-19）

使用已解析的 Minecraft 26.2 common jar 核對 mappings：原版村民類別為 `net.minecraft.world.entity.npc.villager.Villager`；空間 entity 查詢可使用 `Level.getEntities(EntityTypeTest.forClass(Villager.class), AABB, Predicate)`；`EntityTypeTest.forClass(Class<T>)` 位於 `net.minecraft.world.level.entity.EntityTypeTest`；`AABB` 支援六個 double 參數建構子。`ServerLevel.getEntities(EntityTypeTest, Predicate)` 也存在，但不應用來掃描整個世界；聚落人口 provider 應使用有界 AABB。

第一版 provider 規則：`SettlementPopulationProvider` 回傳 `OptionalInt`，沒有已載入村民時回傳 empty，讓 caller 使用 deterministic fallback，不把尚未載入的村莊誤判為人口 0；`SettlementFoodProvider` 回傳 `OptionalLong`，先使用 bootstrap provider，未來可替換箱子、農田或外部模組 adapter。Provider 只觀測 server 世界，不強制載入 chunk，不改動 `FoodDemandModel`，也不把外部村莊模組列為硬依賴。

已核對來源：本機 Minecraft 26.2 common jar `minecraft-common-043a8b3edf-26.2.jar` 的實際 `javap` signatures；Fabric 26.2 porting 文件作版本背景：[Fabric Porting to 26.2](https://docs.fabricmc.net/develop/porting/)，查證日期 2026-08-19。


## Provider 觀測中心修正（2026-08-19）

不要假設 `findNearestMapStructure` 回傳的結構代表 `BlockPos` 就是玩家實際所在村莊的幾何中心。scan 應把玩家執行命令時的 `BlockPos` 作為人口觀測 origin 傳給 `SettlementPopulationProvider`；provider 可在沒有 origin 時退回使用 `SettlementAdapter` 的保存中心。這能避免玩家站在村民旁邊，但結構搜尋回傳中心漂移到另一個區塊時，AABB 查詢漏掉村民而錯誤使用人口 fallback。

保持「觀測」和「文明狀態」分離：重新 scan 只 refresh population observation；不得重置 food stock、stability debt、stability 或 last food event。若 origin 範圍沒有已載入村民，provider 回傳 empty，caller 才使用或保留既有 fallback，並應在玩家訊息或診斷輸出中明確呈現觀測結果。


## TekTopia 建築標識研究與 Civitas 轉化（2026-08-19）

研究來源：
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/tree/master/net/tangotek/tektopia
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/VillageManager.java
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureType.java
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureStorage.java
- https://docs.fabricmc.net/develop/items/first-item
- https://docs.fabricmc.net/develop/items/custom-item-interactions

TekTopia 的可借鑒模式是：玩家把專用 structure marker 放進建築門口的 ItemFrame；server 以 marker item registry 映射建築類型，再以 ItemFrame position/facing 找門、從門內側做連通 floor scan 建立 AABB 與 floor tiles；結構通過驗證後才加入村莊結構集合，並週期性重新 validate。`VillageManager` 以玩家位置建立 scan boxes，找到 ItemFrame 後用 factory 建立結構、以 frame position 去重，並把 marker 綁定到 village。`VillageStructureStorage` 只在驗證後建築的 floor scan 中收集箱子，將箱子集合交給 village storage，而不是掃描全村。

轉化到 Civitas 時不要複製 TekTopia 1.12／Forge API、NBT tag 或 runtime object graph。建立 common/server-side `BuildingMarkerRegistry` 或資料驅動 registry，把自訂 marker item 映射到 `BuildingFunction`；用 `BuildingObservation` 保存 marker position、dimension、function、validation status、door anchor、AABB／bounds、lastSeen 與 settlement binding。ItemFrame 是 activation/observation source，真正的建築狀態與資源摘要仍由 logical server Saved Data 保存。

第一個建築垂直切片建議依序為：主動 scan 已載入 ItemFrame → `ItemFrame.getItem()` 映射自訂 warehouse marker → 查找 marker 附近門與可連通室內 floor → server 驗證基本結構 → 以 marker position 去重與 settlement 綁定 → warehouse provider 只掃描驗證 AABB 內的 Container／BlockEntity。若 marker 被移除、展示框消失、門或結構失效，將 observation 標記 invalid／inactive，不應刪除歷史資料。第一版可先使用命令 refresh，等 26.2 callback 完整查證後再做即時事件。

Minecraft 26.2 自訂 item 依官方文件使用 `ResourceKey<Item>`、`Registries.ITEM` 與 `Registry.register(BuiltInRegistries.ITEM, itemKey, item)`；自訂 Item 可覆寫 `useOn`／`use`，但 ItemFrame 探測初期優先用 server-side `Level.getEntities(EntityTypeTest, AABB, Predicate)` 與已核對的 `ItemFrame.getItem()`、`getDirection()`、`getRotation()`，不要猜測 item-frame 專用事件。

## MineColonies 參考架構（2026-08-20）

使用 MineColonies 作為**功能與資料責任的參考**，不要把其 Forge API、事件、capability、GUI、worker AI、pathfinding 或 request token factory 直接移植到 Fabric 26.2。詳細研究與來源保存在 `references/minecolonies-reference.md`；專案副本位於 `C:\Minecraft\skills\minecraft-civilization-fabric-262\references\minecolonies-reference.md`。

研究後固定採用以下邊界：`SettlementAdapter` 保存聚落來源與聚落級摘要；`BuildingObservation` 保存建築實例 identity、marker、功能、狀態與聚落綁定；warehouse marker 只啟用 `WAREHOUSE` function，不代表箱子內容；未來 storage provider 才能提供已驗證容器範圍的資源觀測；未來 request 服務再分出 requester、requestable、provider、resolver、state、priority 與 id。MineColonies 的功能事實可用其官方首頁／Wiki／GitHub 交叉查閱，但所有版本敏感 Fabric API 仍要重新查官方 26.2 文件或實際編譯。


## Warehouse 領地選取 API 查證與規則（2026-08-20）

當任務涉及 warehouse_marker 的兩點領地選取時，使用已查證的 Fabric 26.2 hooks，不建立未查證的 Mixin。Fabric 官方 Events 文件（https://docs.fabricmc.net/develop/events）確認 `AttackBlockCallback.EVENT` callback 形式為 `(Player, Level, InteractionHand, BlockPos, Direction)`；本機 Fabric API 0.158.0+26.2 jar 的 method 為 `InteractionResult interact(Player, Level, InteractionHand, BlockPos, Direction)`。Fabric 官方 Custom Item Interactions 文件（https://docs.fabricmc.net/develop/items/custom-item-interactions）確認自訂 Item 可覆寫 `useOn(UseOnContext)`；`UseOnContext` 已由 26.2 common jar 查證具有 `getClickedPos()`、`getPlayer()`、`getHand()`、`getItemInHand()` 與 `getLevel()`。

`warehouse_marker` 的第一點使用 `AttackBlockCallback` 左鍵選取，第二點由 `WarehouseMarkerItem.useOn(...)` 右鍵選取。server-side handler 必須自行檢查 spectator、只接受主手、只在 `ServerLevel` 執行，並回傳可阻止原版挖掘／互動副作用的 `InteractionResult`。不要讓玩家用 warehouse_marker 選取時挖掉第一點方塊或放置第二點方塊。

26.2 `Item.inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot)` 已由 common jar 查證；`Inventory.tick()` 對所有非空 inventory stack 呼叫它，選取中的槽位傳 `EquipmentSlot.MAINHAND`，其餘槽位傳 `null`。因此未完成的 point A 應寫入 warehouse_marker 的 `DataComponents.CUSTOM_DATA`，並在 `equipmentSlot == null` 時清除；這實現「從手持 warehouse_marker 切換到其他物品，本次未完成選取立即取消」。兩點完成後的 warehouse territory 不可因切換快捷欄而清除。

26.2 `DataComponents.CUSTOM_DATA` 型別為 `DataComponentType<CustomData>`；`CustomData.of(CompoundTag)`、`CustomData.set(...)`、`CustomData.copyTag()` 與 `CompoundTag.putInt`、`getIntOr`、`putString`、`getStringOr`、`contains`、`remove` 已由 common jar 查證。warehouse_marker 可用此 component 保存版本化的 point A 或正規化後 `min_x/min_y/min_z/max_x/max_y/max_z` warehouse territory。building scan 不再猜最近的門；只要領地內有附著合法的 ItemFrame 且展示對應、帶同一領地資料的 warehouse_marker，就能啟用 warehouse。門、floor、roof、walls 與 entry 改為非阻斷性的診斷資訊，除非未來另行恢復品質門檻。


## Warehouse Marker tooltip、快速部署與 duplicate（2026-08-20）

`warehouse_marker` 的 tooltip 使用 Minecraft 26.2 common jar 已查證的 `Item.appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`，只讀 ItemStack CustomData，不在 tooltip 中改動 server state。已完成 Marker 顯示原始 point A、point B 與 warehouse territory configured/unconfigured；pending marker 顯示 point B 待確認。為了讓 tooltip 與玩家實際點擊一致，CustomData 同時保存原始 A/B 與正規化 min/max bounds；驗證與領地搜尋使用 min/max，顯示使用原始 A/B。

蹲下右鍵空 ItemFrame 的快速部署使用 Fabric 26.2 官方 `UseEntityCallback`。官方 Javadoc（https://maven.fabricmc.net/docs/fabric-api-0.158.0+26.2/net/fabricmc/fabric/api/event/player/UseEntityCallback.html）確認 logical client 回傳 `SUCCESS` 會取消原版流程、揮手並送 server packet；logical server 回傳非 `PASS` 會取消原版流程。因此符合條件時 client/server 都回傳 `SUCCESS`；server 必須檢查主手、蹲下、非 spectator、ItemFrame 為空、Marker territory 完整、維度一致、frame 在 territory 內、frame 有支撐牆，才將 `handStack.copyWithCount(1)` 寫入 frame、`handStack.shrink(1)` 消耗手上 Marker、套用 glint，並回傳成功。轉移必須使用同一份帶 CustomData 的 stack copy，不得重建只含 item identity 的新 Marker；玩家拆下 ItemFrame 後應取得相同 territory 資料。

快速部署失敗時不可消耗手上 Marker。非蹲下、非主手、非空 ItemFrame 或非 warehouse_marker 互動應回傳 `PASS`，讓 vanilla 行為繼續；蹲下但 server 驗證失敗時回傳 `FAIL`，避免 vanilla 將 Marker 放入錯誤位置。ItemFrame entity callback 要自行檢查 spectator，因官方 callback 在 spectator check 前觸發。

同一個完整 `WarehouseTerritory` record（維度與 bounds 相同）只允許第一個有效 ItemFrame 啟用；building scan 中以 `HashSet<WarehouseTerritory>` 按候選順序保留第一個 valid，後續相同領地建立 observation 但標為 `invalid`、reason=`duplicate_territory` 並移除其 glint。這是第一版 instance-level 去重，尚未跨未載入區域搜尋世界所有 ItemFrame；未來若需要強一致領地 claim，應建立 server SavedData 的 territory identity／owner 層。


## 殖民地建設第一切片：村民指派與 Marker 導航（2026-08-20）

Civitas 主線由數值文明模擬轉向殖民地建設。既有 `BuildingGeometryValidator`、`BuildingMarkerScanner`、`BuildingObservation`、`CivilizationWorldData`、`WarehouseMarkerItem`、`WarehouseTerritory` 與 `/civitas building` 指令保留，第一個殖民地垂直切片是把有效 warehouse 建築與原版 `Villager` 指派綁定，保存關係、提供顏色外套並定期導航到 Marker。

Minecraft 26.2 common jar 已查證：`Villager` 繼承 `Mob`；`Entity.getUUID()`、`getStringUUID()`、`getName()`、`getBoundingBox()`、`getEyePosition()`、`getViewVector(float)`、`distanceToSqr(...)` 可用於 server-side 目標驗證與視線選取；`Mob.getNavigation()` 回傳 `PathNavigation`；`PathNavigation.moveTo(double,double,double,double)`、`moveTo(BlockPos,double)`、`isDone()`、`stop()` 可用於最小導航行為。不能只接收 client 傳入 UUID 而不檢查 entity 類型、維度、存活與 server ownership。

26.2 `EntityArgument.entity()`、`EntityArgument.getEntity(context,name)` 與 argument suggestions 已查證。`/civitas assign <building_index> [villager]` 可用 optional `EntityArgument.entity()`；handler 必須限制目標為原版 `Villager`，且在執行者的 `ServerLevel`。省略目標時，server 以玩家 eye position、view vector 與有界 Villager 查詢做視線候選，不信任任意外部資料。

26.2 `DataComponents.DYED_COLOR` 型別為 `DataComponentType<DyedItemColor>`；`DyedItemColor(int)`、`Items.LEATHER_CHESTPLATE`、`DyeColor.YELLOW` 與 `DyeColor.GREEN` 已查證。第一版可建立皮革胸甲並設定黃色 warehouse／綠色 farm component 作為視覺回饋；實作時需記錄替換村民原有胸甲的風險，未來應保存並還原原裝備。

Fabric 26.2 lifecycle events 的 `ServerTickEvents` 有 `END_SERVER_TICK`；本機 4.1.3 lifecycle jar 查證其 callback 以 `MinecraftServer` 為參數，官方 Events 文件確認 Fabric callbacks 使用 `EVENT.register(...)`。指派服務每 20 server ticks 扫描 building observations；若指派村民距離 Marker 超過 8 格則呼叫 `villager.getNavigation().moveTo(markerX, markerY, markerZ, speed)`。服務必須保留 server-only、bounded、低頻率與 stale entity fallback，不要每 tick 重建 path。

`BuildingObservation` 的 resident UUID 優先以 `String residentUuid`（空字串表示未指派）與 `String residentName` 保存，以 `Codec.STRING.optionalFieldOf(...)` 兼容舊 Saved Data；服務層再以 `UUID.fromString` 轉回 UUID。building scan 的 `refreshed(...)` 必須保留 resident 欄位，不得因重新掃描 Marker 而清除指派。

外部來源：[Fabric Events 26.2](https://docs.fabricmc.net/develop/events)、[Fabric Creating Commands 26.2](https://docs.fabricmc.net/develop/commands/basics)。本機查證檔保存於 sandbox `/home/ubuntu/colony-assignment-api-findings.md`，查證日期 2026-08-20。


## 村民角色外套顯示修正（2026-08-20）

Minecraft 26.2 client common jar 的 `VillagerRenderer` constructor bytecode 已查證只註冊 `CustomHeadLayer`、`VillagerProfessionLayer` 與 `CrossedArmsItemLayer`，沒有 `HumanoidArmorLayer`。因此 server-side `Villager.setItemSlot(EquipmentSlot.CHEST, ...)` 可以保存與同步胸甲，但原版 VillagerRenderer 不會把胸甲畫在畫面上；不可把「裝備槽已寫入」誤當作「村民會顯示 armor」。

Minecraft 26.2 `VillagerRenderState` 繼承 `HoldingEntityRenderState`／`VillagerDataHolderRenderState`，不是 `HumanoidRenderState`；`VillagerModel` 也不是 `HumanoidModel`，所以不能直接把 `HumanoidArmorLayer` 套到原版 VillagerRenderer。修正採 client-only custom `RenderLayer<VillagerRenderState, VillagerModel>`，由透明 overlay texture 顯示 warehouse 角色外套；server 胸甲同時帶 `DataComponents.CUSTOM_DATA` 的 `civitas_role=warehouse` 標記，client 在精確的 `VillagerRenderer.extractRenderState(Villager, VillagerRenderState, float)` 注入點讀取同步胸甲並寫入 Mixin render-state access。`VillagerRenderer` constructor 的 client-only Mixin 在 RETURN 追加 role layer。Renderer、Mixin、texture 必須放在 `src/client`，common `src/main/java` 只能保存 server equipment 與 role data。

已查證並可使用的 26.2 client API：`RenderLayer` constructor、`RenderLayer.renderColoredCutoutModel(...)`、`RenderLayerParent`、`VillagerModel`、`VillagerRenderState`、`EntityRendererProvider.Context`、`VillagerRenderer` constructor descriptor、`extractRenderState` descriptor。Fabric 官方《Creating Your First Entity 26.2》說明 entity rendering 屬於 client side，並以 client renderer/layer 方式接入：https://docs.fabricmc.net/develop/entities/first-entity。根因與 bytecode 摘要保存於 `/home/ubuntu/villager-renderer-findings.md`，查證日期 2026-08-20。


## VillagerRenderer Mixin 黑屏修正（2026-08-20）

Minecraft 26.2 的 `LivingEntityRenderer.addLayer(RenderLayer<S, M>)` 是 superclass `LivingEntityRenderer` 宣告的 `protected final` 方法，不是 `VillagerRenderer` 自己宣告的方法。把它寫成 `@Shadow` 放在 `@Mixin(VillagerRenderer.class)` 會造成啟動時 `@Shadow method addLayer ... was not located in the target class net.minecraft.client.renderer.entity.VillagerRenderer`，使 client Mixin 套用失敗並可能在主畫面黑屏；不要把「可由 Java 繼承呼叫」誤當作「可由目標類別的 Mixin `@Shadow` 直接取得」。

已驗證的安全做法是建立 client-only `@Mixin(LivingEntityRenderer.class)` invoker interface，使用 `@Invoker("addLayer")` 暴露 superclass 方法；在 `VillagerRendererMixin` constructor RETURN 注入中，把 `this` cast 為該 invoker，再傳入已正確建立的 `WarehouseRoleLayer`。Invoker 的 Mixin target 必須是實際宣告 `addLayer` 的 `LivingEntityRenderer`，而不是只繼承它的 `VillagerRenderer`。該 accessor/invoker 與 renderer Mixin 都放在 `src/client/java`，並在 `civilizationmod.client.mixins.json` 註冊。

這次修正的正式驗證命令為 `gradlew.bat --no-daemon compileJava compileClientJava --console=plain` 與 `gradlew.bat --no-daemon build --console=plain`，均 `BUILD SUCCESSFUL`。`runClient` 已通過 Fabric/Minecraft 載入、client initialization、texture atlas 建立與 Render thread 啟動；最新 `run/logs/latest.log` 沒有再次出現原本的 `@Shadow addLayer`／VillagerRenderer Mixin failure。Realms authentication error 是開發環境登入服務訊息，與本模組 Mixin 黑屏根因不同。

未完成事項是尚未由玩家在含指派 warehouse 村民的實際世界進行最終畫面驗收；後續若外套仍不顯示，應先提供新的 `latest.log` 與 inspect 結果，再檢查 role state 與 texture，而不是重新改動 server assignment。


## Terran Villagers 外部資源包評估（2026-08-20）

來源：CurseForge 專案頁 <https://www.curseforge.com/minecraft/texture-packs/terran-villagers>、26.2 檔案頁 <https://www.curseforge.com/minecraft/texture-packs/terran-villagers/files/8650013>、作者 `sunracou`，Project ID `1544973`。26.2 檔案為 `Terran Villagers [6.5] 26.1-2.zip`，Release，檔案頁日期 2026-08-14。

CurseForge 的 Custom License 把內容分為兩部分：CEM 相容 source code／scripts／configuration files 使用 GPLv3；graphical assets、textures 與其他 static content 使用 CC BY-NC-ND 4.0。後者允許在遵守署名與授權連結的非商業條件下分享原始內容，但禁止商業用途，也禁止 remix、修改、轉換、改作後再分發。Creative Commons 官方 Deed <https://creativecommons.org/licenses/by-nc-nd/4.0/> 同樣確認 NoDerivatives 對修改後 material 的分發限制。

因此不可把 Terran Villagers 的 PNG、CEM、properties 或其他美術資產複製、改色、裁切、合併或重新打包進 `civilizationmod` jar；也不可把它修改成 Civitas warehouse overlay 後隨 mod 發布。最安全方案是讓玩家自行從作者／CurseForge 下載 26.2 resource pack，放入 instance 的 `resourcepacks` 並自行啟用；Civitas 只保留外部 pack 的相容性說明與來源連結。若未來要內建或改作，先取得作者明確書面許可，且許可必須涵蓋修改、嵌入 mod、再分發與發布渠道。

技術上，該 zip 的 `pack.mcmeta` 使用 `pack_format: 15`、`min_format: 15`、`max_format: 999`，描述明確寫有 `[Requires mods ETF/EMF]`。ETF Modrinth <https://modrinth.com/mod/entitytexturefeatures/versions> 顯示支援 Minecraft 26.2、Fabric 且為 client-side；EMF Modrinth <https://modrinth.com/mod/entity-model-features/versions> 顯示支援 Minecraft 26.2、Fabric 且提供 OptiFine CEM 相容模型。zip 內含 `assets/minecraft/textures/entity/villager/` 下大量 64x64 原版 villager variants、profession/type `.properties`，以及 `assets/minecraft/optifine/cem/villager.jem`、多個 `.jpm` 子模型；它是透過 ETF/EMF、CEM 與 properties 改變整體原版村民外觀，不是單一可直接放入 Civitas renderer 的 PNG。檔案也包含許多外部模組 namespace，會以玩家安裝的相關模組為條件發揮作用。

Civitas 目前的 warehouse role renderer 使用 `civilizationmod` namespace 的自有 overlay，並以 server 同步 role state 決定顯示；外部 Terran pack 若由玩家啟用，技術上可作為全域原版村民外觀，且與 Civitas 自有 overlay 分離。但它不會自然只套用到被 Civitas 指派的村民；若要實現「只有 Civitas 村民使用 Terran 外觀」，需得到授權後另做受 role state 控制的自有 texture／renderer，不能直接依賴外部 pack 的全域 `assets/minecraft` 覆蓋或把其 CEM 直接嵌入。原始 zip 尚有 `villager.jem` 參照 `villager_default_arms.jpm` 但在同一 zip 未找到的情況，使用 ETF/EMF 時仍應由玩家實際 client 驗收，不能把 CEM 完整性當成 Civitas API 保證。


## Warehouse Storage Provider API 查證與規則（2026-08-20）

本次以專案實際 Minecraft 26.2 common jar `minecraft-common-043a8b3edf-26.2.jar` 的 javap 查證，不猜測 API。`Level` 提供 `isLoaded(BlockPos)`、`getBlockState(BlockPos)`、`getBlockEntity(BlockPos)`；第一版 storage scan 只讀已載入位置，不用 `getChunkAt` 強制載入區塊。`Container` 提供 `getContainerSize()`、`getItem(int)`、`isEmpty()` 等已核對方法；物品堆疊用 `ItemStack.isEmpty()`、`getCount()` 與 `getItem()`。

26.2 `ChestBlock` 已查證有 `public static final EnumProperty<ChestType> TYPE` 與 `public static Container getContainer(ChestBlock, BlockState, Level, BlockPos, boolean)`。`ChestType` 有 `SINGLE`、`LEFT`、`RIGHT`。因此第一版只掃描 `Blocks.CHEST`，以 `SINGLE` 或 `LEFT` 作為 primary，呼叫 `ChestBlock.getContainer(...)` 將雙箱合併為一個 Container，避免雙箱重複計數；`RIGHT` 只作 partner half 跳過。這不是完整物流功能，亦不包含 trapped chest、barrel、hopper 或外部模組容器，後續要擴充時必須另行查證與定義。

`BuiltInRegistries.ITEM` 的 26.2 型別為 `DefaultedRegistry<Item>`；`Registry.getKey(T)` 回傳 `Identifier`。物品摘要應保存 registry ID 字串與 long count，不保存 display name 或 client Component。物品種類排序使用 registry ID，讓 Saved Data 與 `/civitas building inspect` 輸出穩定。

`BuildingStorageItem` 保存 `item_id` 與 `count`；`BuildingStorageSnapshot` 保存 scanned、container_count、unloaded_block_count、total_item_count、last_scanned_at 與 item list。`BuildingObservation` 原本已有 16 個 Codec 欄位，RecordCodecBuilder arity 上限不允許直接新增第 17 個欄位；修正方式是以 `RecordCodecBuilder.mapCodec` 把既有 top-level `resident_name` 與新的 top-level `storage` 合併為 `CodecTail`，再由主 Codec 的第 16 個欄位接回，保持舊 Saved Data 的 `resident_name` 欄位名稱與新 storage optional default。

storage scan 只在 warehouse validation valid 且 marker territory 可解析時執行；invalid、duplicate 或未載入的既有 observation 不應用空 snapshot 覆蓋上一次成功摘要。`BuildingObservation.refreshed(...)`、`withResident(...)` 必須保留 storage snapshot；指派村民或重掃 marker 不得清除箱子觀測。`building inspect` 顯示箱子數、物品種類、物品總數、未載入方塊、掃描 tick 與 item ID 摘要，所有玩家文字仍經 locale 與 `CivilizationMessages` 品牌包裝。

正式查證檔案曾保存於專案根目錄暫存 `api-level.txt`、`api-chestblock.txt`、`api-chesttype.txt`、`api-registry.txt` 與 `api-built-in-registries.txt`；這些是一次性 API 證據，完成記錄後可清理，不應作為遊戲資產提交。


## Warehouse 入庫物流 API 查證與安全規則（2026-08-20）

本次以實際 Minecraft 26.2 common jar javap 查證。`Villager` 繼承 `AbstractVillager`，由 `AbstractVillager.getInventory()` 取得 `SimpleContainer`；`SimpleContainer` 以 `Container` 介面提供 `getContainerSize()`、`getItem(int)`、`setItem(int, ItemStack)`、`setChanged()`。`Container` 另有已核對的 `canPlaceItem(int, ItemStack)`、`getMaxStackSize(ItemStack)`；入庫後必須呼叫 target container 的 `setChanged()`。

`ItemStack` 已查證提供 `split(int)`、`copy()`、`copyWithCount(int)`、`isSameItem(ItemStack, ItemStack)`、`isSameItemSameComponents(ItemStack, ItemStack)`、`getCount()`、`shrink(int)`、`grow(int)`、`isEmpty()`；`Item.getDefaultMaxStackSize()` 可取得該物品的預設堆疊上限。安全轉移應先計算每個 target slot 的可用空間，只對空 slot 或 `isSameItemSameComponents` 的堆疊合併，尊重 `canPlaceItem` 與 `getMaxStackSize(ItemStack)`，不直接覆蓋非同類物品；來源與 target 更新後各自呼叫 `setChanged()`。

第一個物流切片採用明確 allowlist：只有上一輪成功 warehouse storage snapshot 已觀測過的 item registry ID 才允許入庫；snapshot 未掃描或沒有任何 item 時不搬運。這避免空 warehouse 被任意物品填滿，也避免未經玩家意圖就搬走村民／玩家物品。每次 server tick 服務最多處理一個 villager inventory slot，完成後立即重新掃描 territory 並保存 snapshot；目前只允許普通 `Blocks.CHEST`，與 `BuildingStorageProvider` 的單箱／雙箱 primary 語意一致。物流服務只在 building validation valid、function warehouse、villager 仍在同一 ServerLevel、存活且不在交易時執行。



## Warehouse Marker 拆除與 Saved Data lifecycle 查證（2026-08-20）

Fabric 26.2 官方 Events 文件確認 callback 應以 `EVENT.register(...)` 註冊並優先於 Mixin；專案實際使用的 `fabric-entity-events-v1` 5.0.5+06488ac19e jar class 清單只有 `EntityElytraEvents`、`EntitySleepEvents`、`ServerEntityCombatEvents`、`ServerEntityLevelChangeEvents`、`ServerLivingEntityEvents` 與 `ServerPlayerEvents` 等，沒有通用 `EntityRemoveCallback`、`ItemFrameBreakCallback` 或 ItemFrame 專用 removal callback。不能猜測或新增不存在的事件名稱。

因此 warehouse marker cleanup 採用 server-side reconciliation，而不是為 ItemFrame 拆除新增 Mixin：`CivilizationWorldData.removeMissingBuildings(ServerLevel)` 只檢查與 observation 相同維度、且 marker `BlockPos` 已載入的區域；在該位置找不到相同 `functionId` 的 ItemFrame 才移除 `BuildingObservation` 並呼叫 `setDirty()`。未載入 marker 區域不得刪除 Saved Data，避免玩家離開區塊時誤刪合法建築。

cleanup 每 20 server ticks 由既有 `BuildingResidentService` 對所有 server dimensions 執行，並在 `scanBuildingMarkers(...)` 開始時再執行一次。因此玩家拆掉 marker 後不需要刪除指令；最多等待一個低頻率週期，再重新放置完成 territory 的 marker 並執行 building scan 即可重新建立 observation。若相同座標在清理週期內已放上相同功能的新 marker，視為同一筆 observation，不會刪除或重置 resident／storage 資料。

這個流程只能處理「marker ItemFrame 消失或 item 不再是相同 building function」；不會因為 marker 所在區塊未載入、維度未載入或暫時無法查詢而刪除資料。`removeBuilding(BuildingObservation)` 提供 server-side Saved Data 的明確移除 helper，但目前沒有對玩家暴露刪除命令，符合 warehouse marker 拆除即刪除的玩法決策。


## Civitas 村民背包介面 API 查證（2026-08-20）

Minecraft 26.2 common jar 已查證：`AbstractVillager.getInventory()` 回傳原版已持久化的 `SimpleContainer`，其欄位在 vanilla source 中為 `new SimpleContainer(8)`；因此第一版 Civitas 背包可以安全地把原版村民 8 格 inventory 以模組提供的 UI 暴露，不需要另造 entity NBT 或第二套持久化 inventory。原版 `AbstractVillager.addAdditionalSaveData(ValueOutput)`／`readAdditionalSaveData(ValueInput)` 已負責該 inventory 保存。

`ChestMenu` 已查證提供 `oneRow(int, PlayerInventory, Container)`、`getContainer()`、`stillValid(Player)` 與 `quickMoveStack(...)`。但 `ChestMenu` 的 standard generic menu 會要求 9 格容器；Civitas 第一版不直接使用 9 格 wrapper 以免多出可被誤用的空槽，而建立自訂 `AbstractContainerMenu`，加入 8 個村民 inventory `Slot` 再加入 player inventory slots。Menu 對 server 與 client 使用相同 `MenuType`；MenuType 建立使用 `new MenuType<>(supplier, FeatureFlags.DEFAULT_FLAGS)` 並透過已查證的 `BuiltInRegistries.MENU` 註冊。

`Player.openMenu(MenuProvider)` 已由 26.2 common jar 查證；`SimpleMenuProvider(MenuConstructor, Component)` 可將 server-side menu 交給玩家。`MenuScreens.register(MenuType, ScreenConstructor)`、`AbstractContainerScreen`、`GuiGraphicsExtractor` 的 client API 已由 26.2 clientOnly jar 查證；自訂 screen 可只繪製 8 槽 backpack layout，不依賴外部 UI mod。

Fabric API `UseEntityCallback` 0.158.0+26.2 binary 已查證其 callback 為 `InteractionResult interact(Player, Level, InteractionHand, Entity, EntityHitResult)`。Civitas 右鍵村民攔截應只在 server side、主手、非 spectator、目標為 `Villager` 且其 UUID 有效存在於 `CivilizationWorldData.findBuildingAssignedTo(...)` 時回傳 `SUCCESS` 並呼叫 `ServerPlayer.openMenu(...)`; 不符合 Civitas 指派身份時回傳 `PASS`，保留原版交易。Server menu 的 `stillValid` 必須重新以 server Saved Data 的 resident UUID、building validation valid、same dimension、villager alive 與玩家 entity interaction range 驗證，不能信任 client。

第一版背包規則：只替換「已指派給有效 Civitas building 的原版 Villager」的右鍵交易；未指派村民、失效建築與非主手互動繼續走 vanilla。UI 標題使用 `CivilizationMessages.translatable(...)` 與三份 locale，GUI 也不得硬編碼可見文字。取消資源回收需求：warehouse marker 刪除時不移動、不掉落、不清空 territory 內任何物品，僅清理 marker/building observation。

## 已查證：Fabric 26.2 shaped recipe datagen（2026-08-20）

配方資料生成使用 Fabric 官方 Recipe Generation 26.2 流程（https://docs.fabricmc.net/develop/data-generation/recipes）：建立 client-side `FabricRecipeProvider`，constructor 接收 `FabricPackOutput` 與 `CompletableFuture<HolderLookup.Provider>`；覆寫 `createRecipeProvider(HolderLookup.Provider, RecipeOutput)`，在內部 `RecipeProvider.buildRecipes()` 使用 `shaped(RecipeCategory, Item)`、`pattern(...)`、`define(...)`、`unlockedBy(...)` 與 `save(output)`。在 `DataGeneratorEntrypoint.onInitializeDataGenerator(...)` 以 `fabricDataGenerator.createPack().addProvider(ProviderClass::new)` 註冊。

Civitas warehouse marker 配方固定為：pattern `ccc/cic/ccc`，`c = Items.CHEST`、`i = Items.IRON_INGOT`，輸出 `CivilizationItems.WAREHOUSE_MARKER`，類別使用 `RecipeCategory.MISC`，解鎖條件為持有儲物箱。`runDatagen` 會在 `src/main/generated/data/civilizationmod/recipe/warehouse_marker.json` 與對應 advancement 產生資料；不要手猜 26.2 recipe builder、registry 或 datagen provider 簽名，優先以官方文件及 compile/runDatagen 驗證。

## 已查證：27 格 Villager Backpack 與原版三列容器幾何（2026-08-20）

Minecraft 26.2 common jar 已查證 `ChestMenu` 有 public constructor `ChestMenu(MenuType<?>, int, Inventory, Container, int)`，可用自訂 Container 與 row count 建立三列 menu；其三列配置使用 27 個 container slots，玩家 inventory 槽位接在容器區下方。client-only jar 顯示 26.2 使用 `ContainerScreen`（不是舊版獨立的泛型 `ChestScreen`）；只要自訂 menu 繼承 `ChestMenu`，就能讓 `ContainerScreen` 與 `MenuScreens.register` 以 `ChestMenu` generic 相容，直接使用原版容器背景與槽位繪製。若自訂 menu 不繼承 `ChestMenu`，才應改用 `AbstractContainerScreen<CustomMenu>`。

Civitas 27 格 backpack 只給被指派的原版 `Villager`，不改動原版 `AbstractVillager.getInventory()` 的交易 inventory。common Mixin 以 `CivitasVillagerBackpackHolder` 暴露 `NonNullList<ItemStack>`，並在 `AbstractVillager.addAdditionalSaveData(ValueOutput)`／`readAdditionalSaveData(ValueInput)` 尾端，使用已查證的 `ValueOutput.child(String)`、`ValueInput.childOrEmpty(String)` 與 `ContainerHelper.saveAllItems/loadAllItems` 保存／載入 `CivitasBackpack` 子節點；handler 必須以 `instanceof Villager` 限制角色範圍。物流服務若要搬運 GUI 內的物品，來源 Container 必須使用該 27 格 holder，而不是 vanilla 8 格 `getInventory()`。

原版風格的手繪 fallback 應沿用 Minecraft container 的 slot 幾何：container slot 背景左上角相對於 screen 為 `(7, 17)`，每格 18 px；三列後玩家 inventory 第一列相對 y 為 `85`，玩家 hotbar 相對 y 為 `143`；三列畫面高度為 `168`，玩家 inventory label 約在 y=74。正式使用原版 texture 時，優先以已查證的 26.2 ContainerScreen source／bytecode 對照，不猜測舊版 blit API。

查證來源：26.2 common/client deobf jars 的 `javap`、Fabric 官方 Custom Screens 26.2（https://docs.fabricmc.net/develop/rendering/gui/custom-screens），查證日期 2026-08-20。


## 已查證：26.2 ContainerScreen 標題座標（2026-08-20）

`AbstractContainerScreen` 的 protected 欄位 `titleLabelX`、`titleLabelY`、`inventoryLabelX`、`inventoryLabelY` 已由 Minecraft 26.2 client-only deobf jar 的 `javap` 查證；constructor 預設值為 title `(8, 6)`、inventory label x=`8`，三列容器的 inventory label y=`72`。`extractLabels(GuiGraphicsExtractor, int, int)` 內的 label 座標是相對於 container screen 的局部座標，不能再加 `leftPos` 或 `topPos`。若自訂 screen 覆寫 `extractLabels`，應使用 `this.titleLabelX`／`this.titleLabelY` 與 `this.inventoryLabelX`／`this.inventoryLabelY`，讓不同解析度與 GUI scale 仍由原版 screen layout 管理；直接使用 `leftPos + ...`、`topPos + ...` 會造成標題被額外偏移到 GUI 外側。

查證來源：Minecraft 26.2 client-only deobf jar `AbstractContainerScreen` bytecode 與本地 `backpack-client-api.txt`，查證日期 2026-08-20。


## 住宅容量 Marker 與床位掃描查證（2026-08-21）

本專案的住宅第一切片使用四個獨立 item registry：`residential_marker_1`、`residential_marker_2`、`residential_marker_4` 與 `residential_marker_6`。四者共用 territory selection、ItemFrame transfer 與 `BuildingFunction.RESIDENCE`，容量由 common-side `BuildingMarkerRegistry` 的 `MarkerDefinition` 保存，不在 client 自行判斷。未來市政廳尚未註冊，避免把殖民地核心規則與住宅切片混在一起。

Minecraft 26.2 實際編譯確認：彩色床不能以 `Items.BED` 當成單一 `Item`；配方材料與解鎖條件應使用 `ItemTags.BEDS`。住宅掃描可使用 `BlockTags.BEDS` 過濾 block state，再以已可編譯的 `BedBlock.PART == BedPart.FOOT` 每張床只計一次。26.2 官方 Snapshot 3 同時確認 bed block entity 已移除、床改用 block models，因此住宅資料應觀測 block state，不應依賴舊版 bed entity/NBT。

`BuildingObservation` 原本主 Codec group 已接近 RecordCodecBuilder group 欄位上限；新增住宅 `capacity` 與 `bed_count` 時，應把它們放入既有 `CodecTail` nested map codec，以 optional defaults 保持舊 warehouse observation 可載入。這是由 26.2 實際 compile error 與修正後 `compileJava` 成功查證。

查證來源：[Minecraft Java 26.2](https://www.minecraft.net/fi-fi/article/minecraft-java-edition-26-2)、[Minecraft 26.2 Snapshot 3](https://www.minecraft.net/en-us/article/minecraft-26-2-snapshot-3)、[Fabric Creating Your First Item 26.2](https://docs.fabricmc.net/develop/items/first-item)。


## 已查證：26.2 領地預覽 render context 與粒子 fallback（2026-08-21）

Fabric API `fabric-rendering-v1` `25.3.2+515ac5339e` 的實際 sources 已確認 `net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext` 為 `@ApiStatus.NonExtendable` interface，繼承 `LevelTerrainRenderContext`，並公開 `SubmitNodeCollector submitNodeCollector()` 與 `PoseStack poseStack()`。同一版本的 `LevelRenderEvents.BeforeGizmos` callback 形式為 `void beforeGizmos(LevelRenderContext context)`；這證明 client gizmo renderer 可以取得 26.2 `PoseStack`，但不代表可直接猜測 `VertexConsumer` 或 `LevelRenderer.renderLineBox` 的簽名。

本專案第一版領地預覽不依賴未查證的 client 線框 API，而採 common/server-side 粒子 fallback：`CivitasTerritoryPreviewService` 使用已編譯確認的 `ServerTickEvents.END_SERVER_TICK` 與 `ServerLevel.sendParticles(ServerPlayer, ParticleOptions, boolean, boolean, double, double, double, int, double, double, double, double)`，每 5 server ticks 只對持有 Civitas building marker 的選取玩家發送預覽。只有 point A 時發送 `HAPPY_VILLAGER`；完成 A/B 後以 12 條 cuboid 邊線採 `END_ROD` 粒子、兩端追加 endpoint 粒子。每條邊最多 24 個 interval，避免大型領地每 tick 產生無界粒子量。

預覽只讀 marker ItemStack 的 `WarehouseTerritory` CustomData／`selectedDimension`；不讀 client Saved Data，不改寫 server 狀態。切換離開未完成 marker 時，既有 `inventoryTick` 規則會清除 pending point；完成 territory 可以保存，但換回 marker 前不顯示預覽。此方案與 `LevelRenderEvents.BeforeGizmos` 的 client-only 路線分離，避免 dedicated server 載入 client 類別。

查證證據：Fabric API 25.3.2+515ac5339e sources `LevelRenderContext.java`、實際 26.2 compile 與 build；官方事件背景文件：[Fabric Events 26.2](https://docs.fabricmc.net/develop/events)。

## 已查證：住宅 marker 多居民容量與 SavedData 相容規則（2026-08-21）

住宅 marker 的容量仍由 common-side `BuildingMarkerRegistry.MarkerDefinition.capacity` 決定；`ResidenceValidator` 以領地內 `BlockTags.BEDS` 與 `BedBlock.PART == BedPart.FOOT` 計算唯一床位。住宅有效的基本條件是 `bedCount >= capacity`，而村民入住上限是已保存 roster 的 `residentCount <= capacity`；床位數與居民數是兩個獨立檢查，不能只用其中一個代替另一個。

`BuildingObservation` 保留舊版 `resident_uuid`／`resident_name` 欄位，並在 nested `CodecTail` 新增 optional `residents` list。讀取舊 warehouse 或舊單居民住宅資料時，canonical constructor 會把 legacy resident 正規化成 roster 第一項；新資料以 `ResidentAssignment` 保存 UUID 與診斷用名稱，重複 UUID 會去重。`refreshed`、`withStorageSnapshot` 與住宅容量更新 helper 必須保留 roster，重掃不能清除既有入住居民。

`/civitas assign` 的 server gate 對住宅先重新找目前載入的 marker ItemFrame 與 territory、重新計算即時床位；若 marker／territory 缺失或床位不足則拒絕。若同一名村民已在目標住宅，操作保持冪等；若目標住宅 roster 已達 marker capacity，拒絕新居民。warehouse 維持單一 resident 行為。building scan 若發現保存 roster 超過目前 marker capacity，保留資料但將 observation 標為 `invalid`、reason=`residents_over_capacity` 並移除有效 glint，不自動刪除村民資料。

`BuildingResidentService` 每 20 server ticks 逐一解析住宅 roster 中的原版 `Villager` UUID 並導航到 marker；warehouse 仍執行原本單一居民的物流入庫。`CivilizationWorldData.findBuildingAssignedTo` 必須按 roster 搜尋，確保住宅多居民右鍵互動仍能找到所屬 building。這次 schema version 升至 6；新增 roster 欄位有 optional default，舊世界可載入。

驗證方式：`runFoodModelRegressionTest` 檢查 roster 新增、重複去重、重掃保留、Codec round-trip 與 legacy single-resident compatibility；`compileJava`、`clean build` 與 `runClient` smoke test 需成功。


## 已確認：住宅 marker 材質與 item model 資產規則（2026-08-21）

住宅容量圖示採每級一張獨立 `16×16` RGBA PNG：`residential_marker_1/2/4/6`。共同輪廓是側視床，不使用 warehouse 的箱子／容器輪廓；以床墊階級色彩與清楚的金色容量數字區分 1、2、4、6。正式貼圖保留一像素透明外框，避免像素床腳貼到 icon 角落。

每個容量版本維持自己的 item model JSON，使用已驗證可載入的 `minecraft:item/generated` parent，並讓 `textures.layer0` 分別指向 `civilizationmod:item/residential_marker_1`、`_2`、`_4`、`_6`。目前不需要額外 override、client renderer 或 model loader；視覺差異由各自 PNG 提供，保持 common 資源與 server/client 邊界簡單。

驗證：PNG IHDR 四者皆為 `16×16`、color type `6`（RGBA）；四個 model 的 layer0 皆指向各自專用貼圖；`clean build` 與 `runClient` smoke test 通過。
