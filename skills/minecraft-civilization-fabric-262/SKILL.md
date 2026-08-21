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


## Warehouse Marker tooltip、直接部署與 duplicate（2026-08-20）

`warehouse_marker` 的 tooltip 使用 Minecraft 26.2 common jar 已查證的 `Item.appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`，只讀 ItemStack CustomData，不在 tooltip 中改動 server state。已完成 Marker 顯示原始 point A、point B 與 warehouse territory configured/unconfigured；pending marker 顯示 point B 待確認。為了讓 tooltip 與玩家實際點擊一致，CustomData 同時保存原始 A/B 與正規化 min/max bounds；驗證與領地搜尋使用 min/max，顯示使用原始 A/B。

直接蹲下右鍵空 ItemFrame 的部署使用已查證的 Fabric 26.2 `UseEntityCallback`。官方 Javadoc（https://maven.fabricmc.net/docs/fabric-api-0.158.0+26.2/net/fabricmc/fabric/api/event/player/UseEntityCallback.html）確認 logical client 回傳 `SUCCESS` 會取消原版流程、揮手並送 server packet；logical server 回傳非 `PASS` 會取消原版流程。因此符合條件時 client/server 都回傳 `SUCCESS`；server 必須檢查主手、蹲下、非 spectator、ItemFrame 為空、Marker territory 完整、維度一致、frame 在 territory 內、frame 有支撐牆，才將 `handStack.copyWithCount(1)` 寫入 frame、`handStack.shrink(1)` 消耗手上 Marker、套用 glint，並回傳成功。轉移必須使用同一份帶 CustomData 的 stack copy，不得重建只含 item identity 的新 Marker；玩家拆下 ItemFrame 後應取得相同 territory 資料。

直接 ItemFrame 部署失敗時不可消耗手上 Marker。非蹲下、非主手、非空 ItemFrame 或非 territory marker 互動應回傳 `PASS`，讓 vanilla 行為繼續；蹲下但 server 驗證失敗時回傳 `FAIL`，避免 vanilla 將 Marker 放入錯誤位置。ItemFrame entity callback 要自行檢查 spectator，因官方 callback 在 spectator check 前觸發。已撤回蹲下右鍵空氣後以視線尋找 ItemFrame 的 `UseItemCallback` 路徑，不得重新加入。

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


## 住宅綁定裝置、重開恢復與互動修正（2026-08-21）

- 已用 Minecraft 26.2 common jar `javap` 確認 `Player.getInventory()`、`Inventory.getContainerSize()`、`Inventory.getItem(int)` 可用於 server-side 掃描玩家背包；快速部署可在手持同類 marker 沒有完成 territory 時，尋找背包內同類且帶完成 `WarehouseTerritory` 的 stack，將其 `DataComponents.CUSTOM_DATA` 複製到轉入 ItemFrame 的 stack，再消耗實際資料來源 stack。不可重建只含 item identity 的 marker。
- `DataComponents.CUSTOM_DATA`、`CustomData.copyTag()`、`CustomData.of(CompoundTag)` 與既有 `WarehouseTerritory` 寫法可用來保存住宅綁定裝置選取的維度與 marker 座標。綁定裝置的 server interaction 必須先確認 ItemFrame 內為住宅 marker、building observation 存在且有效，再以 `ResidenceValidator.validate(...)` 即時重驗證領地與床位，最後才將 villager UUID 加入 `BuildingObservation.residents`。
- 住宅居民 roster 的 Codec round-trip 與最近世界的 `civilization_world.dat` 已確認能保存 `schema_version=6`、`resident_uuid` 與 `residents`；若重開後玩家看不到模組效果，優先檢查 role visual reapply、building validation gate 與 `findBuildingAssignedTo`，不要先重寫 roster Codec。`BuildingResidentService` 必須對保存 roster 的村民重新套用角色外套；invalid 建築可以停止導航／物流，但不應阻止保存的居民恢復外觀與背包管理。
- `UseEntityCallback` 的 binding device 應在 common initializer 先註冊，讓蹲下右鍵有效住宅 ItemFrame 保存選取，之後右鍵 Villager 完成 server-authoritative binding。裝置目前為單件 stack；是否新增配方另行設計，開發驗收可用 `/give @p civilizationmod:residence_binding_device`。
- 新增 `/civitas unassign <building_index> [villager]` 與 `/civilization` 相容別名時，沿用既有 building index suggestions、`EntityArgument.entity()`、server 維度／Villager 類型驗證與 `BuildingObservation.withoutResident(UUID)`；解除後只有在村民沒有其他 Civitas building assignment 時才清除帶 `civitas_role` 的角色胸甲。

查證來源：本機 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar` 的 `javap`（2026-08-21）；Fabric 26.2 Events 文件：https://docs.fabricmc.net/develop/events；Fabric 26.2 Items 文件：https://docs.fabricmc.net/develop/items/custom-item-interactions。


## 已查證：Minecraft 26.2 item definition 資源鏈（2026-08-21）

本專案實際 `build/resources/main` 檢查確認，26.2 item registry 要正常解析物品圖示，除了 `assets/<modid>/models/item/<id>.json` 與其 texture 外，還必須有 `assets/<modid>/items/<id>.json`。後者使用：`{"model":{"type":"minecraft:model","model":"<modid>:item/<id>"}}` 導向 item model。`warehouse_marker` 與 `residential_marker_1` 的既有工作資源鏈均採此格式；住宅綁定裝置原先缺少 `items/residence_binding_device.json`，因此遊戲中 item icon 出現紫／青 missing-texture 佔位圖，雖然 PNG 與 legacy model 本身存在。

採用規則：每次新增 item 都要同時檢查 registry id、`items/<id>.json`、`models/item/<id>.json` 與 `textures/item/<id>.png`，並在 `processResources` 或 `build` 後確認四者均進入 `build/resources/main`。不要只以 PNG 存在或 texture atlas 載入成功判斷 item model 完整。

查證：專案既有正常 marker 的 26.2 item definition、`gradlew.bat --no-daemon processResources --console=plain` 與 `build/resources/main` 檔案檢查；日期 2026-08-21。


## 已查證：住宅綁定裝置 shaped recipe 材料（2026-08-21）

本次使用實際 Minecraft 26.2 common jar 的 `javap` 確認 `net.minecraft.world.item.Items` 公開 `public static final Item STICK`。住宅綁定裝置配方採用既有 `FabricRecipeProvider`／`RecipeProvider.shaped(...)` 流程：pattern `sss`、`sis`、`sss`，`s = Items.STICK`、`i = Items.IRON_INGOT`，以 `has(Items.STICK)` 作為解鎖條件，輸出 `CivilizationItems.RESIDENCE_BINDING_DEVICE`。配方是 3×3 有序合成，不是 shapeless。

查證：`C:\Program Files\Java\jdk-25.0.2\bin\javap.exe` 對 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar` 的 `net.minecraft.world.item.Items` 輸出；日期 2026-08-21。


## 已確認：市政廳 marker 視覺 prototype 與功能分層（2026-08-21）

市政廳第一步只建立可顯示的 `town_hall_marker` item、16×16 RGBA 貼圖、`models/item/town_hall_marker.json` 與 26.2 `items/town_hall_marker.json`。視覺主題採石磚對稱市政建築、深藍圓頂、金色市政徽記與深紅旗幟，刻意避開 warehouse 箱子輪廓與 residential marker 床／容量數字。

在 `BuildingFunction` 尚未加入 `TOWN_HALL`、`BuildingMarkerRegistry` 尚未定義 Town Hall metadata、殖民地範圍／居民上限／建築解鎖規則尚未決定前，市政廳 item 應保持純視覺 prototype，不要把它註冊成可掃描 building marker 或讓共用 territory selection callback 誤把它當成 warehouse／residence。完成資產後的下一個最小功能切片才是 server-authoritative Town Hall registration、單一殖民地核心與基礎範圍規則。

查證：現有 `BuildingFunction` 只有 WAREHOUSE／RESIDENCE，`BuildingMarkerRegistry.registerDefaults()` 只有 warehouse 與 residence；本次 `compileJava`、`compileClientJava`、`build` 與 `runClient` smoke test 通過；日期 2026-08-21。


## 已確認：marker 選點不因切換手持物而取消（2026-08-21）

玩家決定取消「沒有手持 marker 就清除座標」的便利性限制。`CivitasBuildingMarkerItem` 不再覆寫 `inventoryTick` 清除 pending point-A；玩家可以先左鍵設定 A 點、切換快捷欄或暫時拿其他物品，再拿回同一個 marker 右鍵設定 B 點。A/B 與已完成 `WarehouseTerritory` 仍由 ItemStack 的 `DataComponents.CUSTOM_DATA` 保存。

新的資料生命週期是：左鍵寫入 pending A；右鍵同維度 B 成功後轉為 completed territory；右鍵不同維度時仍由 `WarehouseTerritory.complete(...)` 清除不相容的 pending selection 並回傳 `DIFFERENT_DIMENSION`；過大範圍仍保留 pending A，讓玩家可重新選 B。切換物品不再是取消條件，舊的 `civilizationmod.building.selection.cancelled` 玩家文案同步移除。

採用：回歸測試鎖定 pending A 可在 marker stack copy 後保留，並可成功完成 territory。不要讓 inventory tick 依目前 equipment slot 推導選點資料生命週期。


## 已查證：市政廳核心 SavedData 與 ItemFrame API（2026-08-21）

本機 Minecraft 26.2 common jar 的 javap 確認 `ServerLevel.getDataStorage()` 回傳 `net.minecraft.world.level.storage.SavedDataStorage`，其公開 `computeIfAbsent(SavedDataType<T>)` 可取得或建立 Codec-backed SavedData；`SavedDataType<T>` 建構子為 `SavedDataType(Identifier, Supplier<T>, Codec<T>, DataFixTypes)`。因此市政廳核心先放入既有 overworld-scoped `CivilizationWorldData`，以 Codec optional field 保存，資料修改後呼叫 `setDirty()`。

同一 jar 與專案 geometry reference 確認 ItemFrame 可使用 `getItem()`、`setItem(ItemStack)`／`setItem(ItemStack, boolean)`，以及繼承的 `blockPosition()`、`getDirection()`；支撐牆位置使用 `frame.blockPosition().relative(frame.getDirection().getOpposite())`。市政廳辨識採既有 server-side `Level.getEntities(EntityTypeTest.forClass(ItemFrame.class), AABB, predicate)` 掃描，不建立未查證的 ItemFrame 專用事件。

查證來源：本機 javap 與 Fabric Saved Data 官方文件 https://docs.fabricmc.net/develop/saved-data；日期 2026-08-21。


## 已採用：市政廳核心辨識與 SavedData（2026-08-21）

市政廳第一版加入 `BuildingFunction.TOWN_HALL` 與 `BuildingMarkerRegistry` metadata，使 `town_hall_marker` 能由既有 server building scanner 辨識。共用兩點 territory 選取與 warehouse 快速放置只接受 warehouse／residence marker，不得因市政廳成為 known marker 而誤觸發。

新增 `TownHallCore` common record，保存 `colony_id`、`dimension`、marker 座標、`created_at` 與 `radius`。核心資料寫入既有 `CivilizationWorldData` 的 optional `town_halls` Codec list，`schema_version` 由 6 升至 7；舊世界未含此欄位時載入空清單。第一版每個維度只允許一座核心：同一 marker 重掃為 `EXISTING`，不改建立時間；同維度不同 marker 為 `DUPLICATE`；不同維度可建立另一核心。固定初始 radius 為 64，保存在 core 資料中供後續殖民地範圍使用。

`TownHallValidator` 只要求 server 已載入且 ItemFrame 透過既有支撐牆公式附著，不套用 warehouse／residence 的門、地板、屋頂、牆體或床位規則。重複核心仍保存 BuildingObservation，但以 `duplicate_town_hall` 受控 reason 標記 invalid。`/civitas townhall` 與相容別名可查詢保存核心；`building scan` 額外回報新增核心與衝突數量。

驗證包含 26.2 common jar javap、Fabric Saved Data 官方文件、TownHallCore Codec／唯一性純 Java regression、Java 25 compile/build、client smoke test 與 dedicated server 初始化；日期 2026-08-21。


## 已查證：marker glint 失效與 ItemFrame 拆除前清理（2026-08-21）

Minecraft 26.2 common jar 的 javap 確認 `ItemStack.remove(DataComponentType<? extends T>)` 會回傳被移除的 component，`DataComponents.ENCHANTMENT_GLINT_OVERRIDE` 是公開 Boolean component type；`ItemFrame.setItem(ItemStack)` 委派 `setItem(ItemStack, true)`，雙參數 boolean 只控制紅石鄰居更新，entity data 仍會更新。因此 invalid marker 仍在 ItemFrame 時可由既有 `BuildingMarkerVisualState.apply(frame, false)` 移除 glint；玩家直接打掉 ItemFrame 時則因 frame 已消失而沒有 invalid path，掉落 stack 會保留 glint。

採用 Fabric API 0.158.0+26.2 的 `AttackEntityCallback.EVENT` server-side handler，在原版移除 ItemFrame 與掉落前，複製已知 Civitas marker stack，只移除 `ENCHANTMENT_GLINT_OVERRIDE`，以 `frame.setItem(updated, false)` 寫回，最後回傳 `PASS` 保留原版拆除、掉落與 CustomData。非 marker、無 glint、client 或 spectator 一律 PASS。不得取消攻擊、重建 item、清除 `CUSTOM_DATA` 或 territory。

查證來源：本機 26.2 common jar／Fabric API jar javap、Fabric Events 26.2 文件 https://docs.fabricmc.net/develop/events、Custom Item Interactions 26.2 文件 https://docs.fabricmc.net/develop/items/custom-item-interactions；日期 2026-08-21。


## 已查證：通用 Civitas 綁定裝置與空氣右鍵快速部署（2026-08-21）

正式通用物品 ID 採 `civitas_binding_device`，顯示名稱為 Civitas Binding Device／Civitas 綁定裝置。舊 `residence_binding_device` 不刪除，改由相容 subclass 提供相同通用行為與 tooltip，避免既有 item stack 與世界資料失效；新配方輸出正式通用 ID。綁定 CustomData schema 座標格式不變，新資料 key 使用 `civitas_binding`，讀取時 fallback 到舊 `civitas_residence_binding`。

綁定 interaction 已泛化為所有已知且有效的 Civitas building marker。住宅仍做即時 territory／床位檢查、容量與 roster append；warehouse／Town Hall 沿用 `/civitas assign` 的 single-resident overwrite 語義，不新增未設計的容量規則。

Fabric API 0.158.0+26.2 jar javap 確認 `UseItemCallback.EVENT` 與 `interact(Player, Level, InteractionHand)`。直接右鍵空 ItemFrame 繼續由 `UseEntityCallback` 處理；蹲下右鍵空氣新增 `UseItemCallback`，以 16 格 AABB、視線 dot 門檻 0.92、`Player.hasLineOfSight` 與最近距離選取空 ItemFrame，再共用 server-side dimension、territory、牆面附著、重複 marker、CustomData copy 與背包 fallback 驗證。快速部署接受 warehouse／residence territory marker，排除 Town Hall；未完成 territory 只回傳 localized incomplete message，不修改 ItemFrame。

查證來源：Fabric Events 26.2 https://docs.fabricmc.net/develop/events、Custom Item Interactions 26.2 https://docs.fabricmc.net/develop/items/custom-item-interactions、本機 Fabric API 0.158.0+26.2 jar javap、專案 `BuildingResidentService.findLookedAtVillager`；日期 2026-08-21。


## 已查證：市政廳 marker 有序合成配方（2026-08-21）

Fabric 官方 [Recipe Generation 26.2](https://docs.fabricmc.net/develop/data-generation/recipes) 確認 shaped recipe 使用 `pattern(String)`、`define(char, Item/Tag)`、`unlockedBy(...)` 與 `save(output)`；本機 Minecraft 26.2 common jar 的 javap 確認 `Items.EMERALD`、`Items.IRON_SWORD`、`Items.IRON_PICKAXE`、`Items.IRON_AXE` 與 `Items.IRON_SHOVEL` 都是公開 Item 常數。

市政廳 marker 採 3×2 shaped recipe：第一列 `esp` 代表綠寶石、鐵劍、鐵鎬；第二列使用 `ah `（第三格為空白）代表鐵斧、鐵鏟並靠左排列。Minecraft 26.2 的 `ShapedRecipeBuilder.pattern(...)` 要求每列寬度相同，不能直接使用兩字元的 `ah`。配方輸出 `CivilizationItems.TOWN_HALL_MARKER`，以持有綠寶石作為 advancement unlock 條件。正式 JSON 必須由 `runDatagen` 生成，不手寫與 provider 不一致的副本。

查證日期：2026-08-21；查證方式：Fabric 官方 recipe generation 文件與本機 Minecraft 26.2 common jar `javap`。


## 已查證：自訂 NPC Entity 路線評估（2026-08-21）

Fabric 官方 [Creating Your First Entity 26.2](https://docs.fabricmc.net/develop/entities/first-entity) 的最小流程是 `PathfinderMob` 子類、`EntityType.Builder.of(...).sized(...)`、`FabricDefaultAttributeRegistry.register(...)`、server-side goals，以及 client-only render state、model layer、renderer 與 `EntityRenderers.register(...)`。持久 entity-local state 使用 `addAdditionalSaveData(ValueOutput)`／`readAdditionalSaveData(ValueInput)`；需要顯示同步的短期狀態才使用 `SynchedEntityData`。

Civitas 目前不立即新增自訂 Entity。先完成 Town Hall colony ownership、住宅／warehouse 指派與物流閉環，再從 `ResidentRecord`／`ResidentRegistry` 抽離居民真實資料；現有原版 Villager 先作為可替換 body adapter。`BuildingObservation.residents` 與 `CivilizationWorldData.findBuildingAssignedTo(String)` 可作為過渡，但尚缺全域 colony ID、住宅 building ID、工作 building ID、角色、生命週期與 body type 等 ResidentRecord 欄位。

不要把 colony truth 只塞進 Entity NBT，也不要在同一個切片同時處理 EntityType、renderer、背包、物流、住宅容量與完整工作 AI。官方 26.2 文件範例仍須依本機 compile／javap 驗證後才能實作。


## 已查證：ResidentRecord 雙 UUID 與 26.2 lookup 修正（2026-08-21）

`residentId` 是 Civitas 永久邏輯身份，`entityUuid` 是目前 Minecraft body 的 UUID；原版 Villager 初次指派時使用 `villager.getUUID()` 作為 entityUuid，未來換 body 時只更新 entityUuid，不更換 residentId。`BuildingObservation` 未來應保存 residentId，而不是把 entityUuid 當作殖民地身份；舊 `ResidentAssignment.uuid` 遷移時第一階段可把既有 UUID 同時當作 residentId 與 entityUuid，新的 assign 才建立獨立 residentId。

不得在 chunk unload 時清除 entityUuid；lookup 找不到可能只是 body 未載入。只有 server 驗證的死亡、移除或 rebind 才能改 entityUuid／lifecycle。第一版不自動復活死亡原版村民。SavedData 保存 ResidentRecord canonical list／Codec，`byResidentId` 與 `byEntityUuid` 是載入後重建的 runtime indexes，不是第二個可修改的真相來源。建築關係應使用 dimension + marker coordinates 或未來 immutable building ID，不得保存易變的 one-based scan index。

本機 Minecraft 26.2 common jar javap 確認 `Entity.getUUID()`、`Entity.setUUID(UUID)` 與 `ServerLevel.getEntityInAnyDimension(UUID)`；未確認 `level.getEntity(UUID)`，`EntityGetter` 公開的是空間 `getEntities(...)` 與 `getPlayerByUUID(UUID)`。禁止把附件中的 `level.getEntity(entityUuid)` 當作 26.2 API。


## 已查證：Civitas 自動化測試分層（2026-08-21）

Fabric 官方 [Automated Testing 26.2](https://docs.fabricmc.net/develop/automatic-testing) 將測試分為 Fabric Loader JUnit unit tests 與 Minecraft GameTest；JUnit 測試 helper、model、Codec 與 server-independent invariants，GameTest 啟動實際 Minecraft server/client 驗證 gameplay。

JUnit 設定採 `testImplementation "net.fabricmc:fabric-loader-junit:${project.loader_version}"` 與 `test { useJUnitPlatform() }`。測試若觸及 registry-dependent 類別，`@BeforeAll` 必須先呼叫 `SharedConstants.tryDetectVersion()` 與 `Bootstrap.bootStrap()`。本專案保留相容的 `runFoodModelRegressionTest` JavaExec，另以 `CivitasCoreTest` 納入 JUnit；Gradle `check` 依賴既有 regression task，因此 `build` 會同時執行兩層 common-side 回歸檢查。

第一批 JUnit 覆蓋 FoodDemandModel、SettlementAdapter 去重與狀態保留、BuildingObservation roster／Codec、building 維度與範圍綁定、WarehouseTerritory 邊界、Town Hall 唯一性與 Codec。ItemFrame 真實掃描、SavedData reload、居民互動、背包／物流與 client renderer 留待 GameTest；不要把 client-only renderer 測試放入 common `src/test/java`，未建立 Loom GameTest source set 且未實際執行前，不得宣稱 server/client gameplay 已覆蓋。


## 已採用：Town Hall 可設定範圍與 colony_id 歸屬切片（2026-08-21）

Town Hall 不再限制為每個維度一座；每個核心保存自己的 `colony_id`、dimension、marker 座標與 radius。`TownHallCore.DEFAULT_RADIUS` 為 64，`MAX_RADIUS` 為 512；`contains(BlockPos)` 使用以 marker 為中心的 inclusive axis-aligned cubic range。兩個同維度核心以三軸距離是否小於等於兩者半徑總和判定範圍重疊；不同維度不互相衝突。

`CivilizationWorldData.registerTownHall(dimension, marker, observedAt, radius)` 的規則是：同一 marker 回傳 `EXISTING`；新核心若與任何同維度既有核心範圍重疊，回傳 `DUPLICATE` 且不寫入；不重疊核心可在同維度並存，跨維度也可並存。`updateTownHallRadius(index, radius)` 限制 1–512，若新半徑造成重疊則回傳 `OVERLAPPING` 且保留舊半徑。`/civitas townhall radius <index> <radius>` 與 `/civilization` 相容別名提供動態 Town Hall index 與常用半徑 Tab completion。

有效住宅與 warehouse 以 marker 維度／座標呼叫 `findTownHallBinding`。唯一核心包含該位置時，BuildingObservation 寫入 `status=bound`、`colony_id` 與 `colony_binding_reason=bound`；沒有核心、位於所有核心範圍外或位於重疊範圍時，分別保存 `no_town_hall`、`outside_town_hall` 或 `overlapping_town_hall`，不參與居民導航、warehouse 物流、assign 或通用綁定裝置。Town Hall marker 本身若和既有範圍衝突，保留 observation 但標記 `duplicate_town_hall` invalid。

BuildingObservation 將 `colony_id` 與 `colony_binding_reason` 放入既有 nested CodecTail 的 optional fields，確保舊 `resident_uuid`、`resident_name`、storage 與 roster 資料可載入；legacy status 可保留但沒有 colony_id 時不視為 `isColonyBound()`。新增或更新 Town Hall 半徑後會即時 refresh 已保存建築的 colony binding；結構 invalid 的建築不保留可運作 colony binding。

驗證：`compileJava compileClientJava test`、`runDatagen`、`runFoodModelRegressionTest` 與完整 `build` 均通過；`CivitasCoreTest` 新增多核心不重疊、半徑更新拒絕重疊、跨維度並存、Town Hall binding status 與 BuildingObservation colony Codec 測試。


## 已查證：市政廳 marker shapeless recipe（2026-08-21）

Minecraft 26.2 common jar 的 `net.minecraft.data.recipes.ShapelessRecipeBuilder` 已由 javap 確認公開 `requires(ItemLike)`、`requires(ItemLike,int)`、`unlockedBy(...)` 與 `save(...)`。市政廳 marker 的無序配方應使用 `FabricRecipeProvider` 的 `shapeless(RecipeCategory.MISC, ItemLike)`，依序加入 `Items.EMERALD`、`Items.IRON_SWORD`、`Items.IRON_PICKAXE`、`Items.IRON_AXE` 與 `Items.IRON_SHOVEL` 各一份；綠寶石為解鎖條件。排列不影響合成，不能保留 shaped pattern，也不能用單一 item tag 取代四種指定工具。


## 已查證：Civitas Creative Mode Tab（2026-08-21）

Fabric 官方 26.2 Custom Creative Tabs 文件確認：自訂創造模式分頁可用 `FabricCreativeModeTab.builder()` 建立，透過 `displayItems((params, output) -> output.accept(item))` 加入 Item／ItemStack，並用 `Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceKey<CreativeModeTab>, tab)` 註冊；分頁標題使用 `Component.translatable` 與 locale key。Civitas 採用 common-side 自訂分頁，加入 warehouse、住宅 1/2/4/6、Town Hall、通用綁定裝置與 legacy residence binding device；不在 client-only screen 中硬編碼物品或文字。

查證來源：[Fabric Custom Creative Tabs 26.2](https://docs.fabricmc.net/develop/items/custom-creative-tabs)、[Fabric Creating Your First Item 26.2](https://docs.fabricmc.net/develop/items/first-item)，日期 2026-08-21。


## 已確認：Fabric API 0.158.0+26.2 Creative Tab package（2026-08-21）

官方 Custom Creative Tabs 文件的 builder 流程可採用，但目前實際依賴 jar `fabric-creative-tab-api-v1-5.0.14+d871b99e9e.jar` 的公開 class package 是 `net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab`；同 jar 另有 `CreativeModeTabEvents`。不要使用不存在的 `net.fabricmc.fabric.api.itemgroup.v1` package。


## 已採用：ResidentRecord／ResidentRegistry 第一版資料層（2026-08-21）

`residentId` 是 Civitas 永久邏輯身份，`entityUuid` 是目前 Minecraft body UUID。legacy `BuildingObservation.ResidentAssignment.uuid` 遷移時第一階段把既有 UUID 同時作為兩者；新的 assign 建立獨立的隨機 `residentId`。`ResidentRecord` 保存 `colonyId`、`homeBuildingKey`、`workBuildingKey`、`role`、`bodyType`、`lifecycle`、診斷名稱與時間欄位。

`ResidentRegistry` 的 canonical list／Codec 是唯一持久化真相，lookup 由 list 查詢，不建立第二份可修改的 index。建築關係使用 `dimension@x,y,z` marker key，不保存易變的 one-based building scan index。`CivilizationWorldData` 新增 optional `resident_registry`，schema version 由 7 升至 8；SavedData 載入後冪等遷移 legacy roster，未載入 body 不清除 entityUuid，第一版不自動復活死亡村民。

本機 Minecraft 26.2 common jar javap 已確認 `Entity.getUUID()`、`Entity.setUUID(UUID)`、`ServerLevel.getEntityInAnyDimension(UUID)`、`ServerLevel.getDataStorage()`、`net.minecraft.world.level.storage.SavedDataStorage.computeIfAbsent(SavedDataType<T>)` 與 `SavedDataType(Identifier, Supplier<T>, Codec<T>, DataFixTypes)`。禁止使用未查證的 `level.getEntity(UUID)`。

`BuildingResidentService` 優先由 registry 的 assigned building key／entity UUID 導航與物流；`CivilizationWorldData.findBuildingAssignedTo(String)` 保留 legacy roster fallback。新增 `/civitas resident list` 與 `/civilization resident list`，只有 literal 子命令，不新增無 suggestions 的字串參數。

驗證：`compileJava compileClientJava`、`test`、`runFoodModelRegressionTest` 與 `build` 成功；新增 JUnit 覆蓋 legacy migration idempotency、雙 UUID 分離、building key 與 Registry Codec round-trip。ItemFrame 真實 gameplay、SavedData 重開與 body rebind 尚未由 GameTest 覆蓋。

查證日期：2026-08-21；查證方式：本機 JDK 25 `javap`、Minecraft 26.2 common jar、實際 Gradle 編譯／測試。


## 已採用：村民死亡釋放住宅容量（2026-08-21）

latest.log 已記錄 server-side Villager death，但原本 `BuildingResidentService` 只在每 20 tick 對活著且可 lookup 的 body 執行導航；死亡 body 找不到時直接跳過，因此沒有更新 lifecycle，也沒有從 `BuildingObservation.residents` 移除 UUID，住宅容量會持續被占用。

Fabric API `fabric-entity-events-v1` `5.0.5+06488ac19e` 的 `net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents` 已由 javap 確認公開 `AFTER_DEATH`，nested callback 為 `ServerLivingEntityEvents.AfterDeath.afterDeath(LivingEntity, DamageSource)`。common `BuildingResidentService.register()` 可安全註冊該 server-side callback。

`CivilizationWorldData.markResidentDead(UUID, long)` 將 ResidentRecord lifecycle 設為 `dead`，保留 residentId、entityUuid、colonyId 與名稱，清除 home/work/role，並從所有 BuildingObservation roster 移除該 entity UUID，再呼叫 `setDirty()`。不要在 tick 中因 `getEntityInAnyDimension` 暫時回傳 null 就判定死亡；chunk unload 不等於死亡。非死亡 removal 後續需另行查證 API。

驗證：`compileJava compileClientJava`、`test`、`runFoodModelRegressionTest` 與 `build` 成功；新增死亡居民保留歷史身份且住宅 roster 數量下降的 JUnit。遊戲內死亡事件尚未重新驗收。查證日期 2026-08-21，來源為 Fabric API jar javap、Minecraft 26.2 common 編譯與使用者 latest.log。


## 已採用：公開命令整理與 `/civitas help`（2026-08-21）

Civitas 目前公開命令樹只保留殖民地主線：`help`、`status`、`building`（scan／list／inspect）、`townhall`（含 radius）、`assign`、`unassign` 與 `resident list`。`/civilization` 仍是相容命令根，與 `/civitas` 共用同一套 builder、handler、權限與 suggestions。

舊版 settlement／村莊 scan、`sc`、聚落 `simulate` 與 `building generate` 不再註冊為公開命令。FoodDemandModel、SettlementAdapter 與 deterministic building generator 可保留作資料層、回歸測試或未來重新設計的內部程式，但不可在 help 或命令樹宣稱為目前玩家功能。

`/civitas help` 使用 literal `help`，逐行透過 `CivilizationMessages.translatable(...)` 發送本地化說明；每行都保留金色品牌前綴。Help translation keys 必須同步 `en_us`、`zh_tw` 與 `zh_cn`；後兩者使用繁體中文。Literal `help` 自動提供 Brigadier 基本 Tab completion，不新增無 suggestions 的自由字串參數。

採用：help 內容只說明目前仍存在的命令與參數，並明確提示 `/civilization` 相容命令根。禁止保留已移除命令的公開 README／help 說明，也不要為了顯示 help 而繞過 `CivilizationMessages`。

驗證：`compileJava compileClientJava processResources` 與完整 `build` 成功；三份 locale JSON 已處理；遊戲內 `/civitas help`、`/civilization help` 與 Tab completion 尚待人工驗收。查證日期 2026-08-21。


## 已採用：ResidentRegistry 成為唯一新 assignment 寫入來源（2026-08-21）

目前 `BuildingObservation.residents` 與 legacy `resident_uuid` 仍保留在 Codec，原因是舊世界遷移與相容讀取；但新 assign／unassign、綁定裝置、死亡清理、building removal 與容量計算不得再寫入或依賴 roster。`ResidentRegistry` canonical list 是新的 assignment 真相來源。

`CivilizationWorldData.get(...)` 載入後只執行冪等 `ResidentRegistry.migrateLegacy(buildings)`，把缺少 canonical record 的 legacy roster assignment 遷移進 registry；不可再呼叫會由 roster 反向更新 canonical assignment 的 refresh method。既有 registry record 優先，legacy roster 不會覆蓋 active、cleared 或 dead assignment。

新的 assignment 應透過 `CivilizationWorldData.ensureResidentAssignment(...)` 寫入 registry；該 API 必須拒絕已 active 且指向另一 building key 的 entity，避免同一 body 同時隸屬兩棟建築。解除指派使用 `clearResidentAssignment(...)`；死亡使用 `markResidentDead(...)`；刪除 marker 使用 registry 的 `clearAssignmentsForBuilding(...)`。這些操作保留 residentId 與歷史資料，並以 `setDirty()` 保存。

住宅容量、建築 inspect、居民名稱、導航與物流查詢使用 `ResidentRegistry.countActiveAssignedTo(...)`／`findActiveAssignedTo(...)` 或 WorldData wrapper。死亡居民 lifecycle 為 `dead`，不再計入 active capacity，但 legacy roster 可以保留原始資料，不應為了修正顯示而重新寫回 roster。

`BuildingObservation` 的 `withResident`、`withAddedResident`、`withoutResident` helpers 標記為 legacy compatibility API；新功能不得使用。Building refresh 可以原樣保留 legacy list 作 Codec 相容，但 residence capacity validation 必須以 registry-derived active count 為準。

驗證：`test`、`runFoodModelRegressionTest` 與 `build` 通過；新增 registry-only assignment／unassign test，確認新 assignment 不改動 legacy roster；死亡 test 改為確認 roster 保持 legacy、active registry count 下降、dead resident lookup 不再返回 building。查證日期 2026-08-21。


## 已採用：Town Hall colony gate 舊世界過渡模式（2026-08-21）

Town Hall colony gate 採用「無核心時過渡、建立核心後嚴格」政策，以避免舊世界在尚未放置市政廳時突然停止既有 warehouse／住宅功能。`CivilizationWorldData.isTownHallTransitionAllowed(BuildingObservation)` 只在建築本身 `validationStatus=valid` 且該建築所在維度的 `findTownHallBinding(...)` 回傳 `NO_TOWN_HALL` 時為 true；它不會放寬 `OUTSIDE` 或 `OVERLAPPING`。

`CivilizationWorldData.isBuildingOperational(...)` 是 assign、居民導航、warehouse 物流與通用綁定裝置共用的 server-side operational gate。有效 colony-bound 建築可以運作；有效但所在維度完全沒有 Town Hall 的建築以 transition mode 運作；無效建築、已有 Town Hall 但位於範圍外的建築、重疊範圍建築均不可運作。transition mode 不寫入 `colony_id`，建築仍保留 `no_town_hall` binding reason，建立 Town Hall 後由既有 refresh binding 流程取得 colony_id 或變成 outside／overlapping。

玩家可見的過渡模式必須透過 `CivilizationMessages` 與 locale keys 呈現：building inspect/list 顯示 transition binding，assign 與綁定裝置成功訊息告知「此維度尚未建立市政廳」，help 說明 transition policy。不要在每 tick 重複發送訊息；導航與物流只使用 operational gate 靜默執行。這一版不加入 config，先保持所有世界一致且容易回歸測試；未來若要可設定，必須另行查證 Fabric 26.2 config 方案並保存 server-authoritative 設定。

驗證：新增 JUnit 覆蓋「無 Town Hall 的有效 building 可運作」、「建立同維度 Town Hall 後範圍內 building 取得 bound 且範圍外仍拒絕」、「另一維度可獨立使用 transition mode」；`test`、`runFoodModelRegressionTest` 與 `build` 通過。查證日期 2026-08-21。


## 2026-08-21 — Town Hall 範圍必須明確標示為軸對齊立方體

- 任務：避免玩家把 Town Hall 的 `radius` 誤解為圓形或球形半徑。
- 已確認：`TownHallCore.contains(BlockPos)` 與核心範圍判定使用 X、Y、Z 三軸分量各自比較；半徑 64 的包含範圍是每軸從 `core - 64` 到 `core + 64` 的軸對齊立方體，邊長為 129 格，並非圓形距離判定。
- 採用：內部保存欄位與 API 維持 `radius` 以確保 SavedData 相容；所有玩家可見命令回饋、help、README 與 locale 使用「軸對齊立方範圍／範圍半徑」，並明確標示「不是圓形」。
- 測試：Town Hall regression 必須確認三軸各自的邊界可包含、任一軸超出一格即不包含；角落可包含不代表使用球形距離。
- 禁止：不要為了文案重命名已保存的 `radius` 欄位，不要把 `contains` 改成平方距離或水平圓形判定。
- 查證：目前 `TownHallCore.contains` 實作與 `CivitasCoreTest` 立方邊界回歸；日期 2026-08-21。


## 2026-08-21 — Town Hall 貼邊也視為重疊衝突

- 任務：明確化多 Town Hall 的不相交、貼邊與邊界格行為。
- 已確認：`TownHallCore.overlaps(other)` 對同維度核心使用三軸距離分別 `<= radiusA + radiusB`；距離剛好等於半徑總和時，兩個軸對齊立方範圍貼邊，仍回傳 overlap。`TownHallCore.contains(position)` 對每一軸使用 `<= radius`，因此唯一核心的邊界格仍包含在該核心範圍內。
- 採用：兩核心要完全不相交，至少一個軸的核心距離必須大於半徑總和；同維度貼邊註冊回傳 duplicate／conflict，避免邊界建築產生不確定 colony ownership。建築若同時被多個核心包含，binding status 為 `OVERLAPPING`，不可取得 colony_id。
- 驗收：至少測試核心距離 `radiusA + radiusB + 1`（不相交）、`radiusA + radiusB`（貼邊衝突）、唯一核心邊界格（可綁定）；也要測試貼邊／相交時的建築 marker binding。
- 禁止：不要把 overlaps 的 `<=` 擅自改成 `<`，不要把建築邊界判定改成圓形距離，也不要讓貼邊核心自動共用邊界建築。
- 查證：`TownHallCore.java` 與 `CivitasCoreTest.java` 的 common-side 回歸；日期 2026-08-21。


## 2026-08-22 — ResidentRecord gameplay 驗證必須使用 GameTest 分層

- 任務：補足 ResidentRecord 只通過 JUnit／build 而沒有實際 gameplay 證據的落差。
- 已確認：Fabric 官方 Automated Testing 26.2 將 Fabric Loader JUnit 定位為 component／Codec／helper 測試，Minecraft GameTest 才會啟動實際 server／client 來驗證 gameplay。官方 Loom 設定使用 `fabricApi.configureTests { createSourceSet = true; modId = ...; enableGameTests = true; enableClientGameTests = ...; eula = true }`，server 測試放在 `src/gametest/java`，descriptor 使用 `fabric-gametest` entrypoint；`build` 執行 server GameTest。
- 已查證：本機 Fabric API 0.158.0+26.2 對應 `fabric-gametest-api-v1` 4.0.21+4a7fa0819e；其 `GameTest` annotation、`CustomTestMethodInvoker` 與 Minecraft 26.2 `GameTestHelper.getLevel()`、`spawnWithNoFreeWill(EntityType, BlockPos)`、`kill(Entity)`、`runAtTickTime(long, Runnable)`、`succeed()` 均可由 javap 確認。
- 採用：ResidentRecord server GameTest 可驗證實際 Villager body UUID lookup、registry assignment、死亡 callback、dead lifecycle 與 active residential capacity 釋放；目前 `ResidentRecordGameTest` 已覆蓋這些項目。
- 採用：`resident list` 玩家聊天、世界重開後 `.dat` reload、Villager unload／reload、`removed` lifecycle 與 body rebind 仍需另外人工或 GameTest；未實際驗證前 README／Mods.md 不得宣稱完成。
- 禁止：不要用 JUnit Codec round-trip 或 `compileJava` 代替 gameplay proof；不要把 GameTest source set 的編譯成功當成 GameTest 實際通過；不要把暫時找不到 entity 當成 removed，因為 chunk unload 不等於死亡或移除。
- 查證：Fabric 官方 Automated Testing 26.2（https://docs.fabricmc.net/develop/automatic-testing）、本機 Java 25 javap 與 `build` 的 FabricGameTestRunner 實際輸出；日期 2026-08-22。


## 已確認：Resident assignment outcome classification（2026-08-22）

當任務涉及 `/civitas assign`、住宅綁定裝置或 ResidentRegistry assignment 時，使用 `CivilizationWorldData.ensureResidentAssignmentResult(...)` 作為 server-side 分類入口。結果必須區分 `CREATED`、`UPDATED`、`ALREADY_ASSIGNED_TO_TARGET`、`ALREADY_ASSIGNED_TO_OTHER_BUILDING` 與 `FAILED`。

`ResidentRegistry` 仍是唯一 canonical assignment 寫入來源。只有 `CREATED` 或 `UPDATED` 代表真正新增／變更，才應呼叫 `setDirty()` 並顯示 success／transition success；同一 `entityUuid` 已經 active 且其 `homeBuildingKey` 或 `workBuildingKey` 等於目標 building key 時，必須回傳 `ALREADY_ASSIGNED_TO_TARGET`，不改資料、不增加 active count，也不可顯示成功綁定訊息。若 active record 已有不同的 assigned building key，必須回傳 `ALREADY_ASSIGNED_TO_OTHER_BUILDING`，維持跨建築拒絕。

`/civitas assign` 與 `ResidenceBindingDeviceInteraction` 必須共用此 outcome 分類，並透過三份 locale 顯示「已是此建築居民」或「已被指派到其他建築」。舊的 nullable `ensureResidentAssignment(...)` 可保留作相容 wrapper，但新玩家入口不能只依 `ResidentRecord`／`upsert` nullable 或 boolean 結果推測新增與 no-op。`CivitasCoreTest` 與 Fabric 26.2 server `ResidentRecordGameTest` 應各自覆蓋同目標重複 assignment 且 active count 不變。
