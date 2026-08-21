---
name: civitas-fabric-262
description: Guides Cursor agents developing Civitas (civilizationmod) for Minecraft Java Edition 26.2 + Fabric at C:\Minecraft. Use when editing this repo, AGENTS.md, Mods.md, /civitas commands, Saved Data, warehouse markers, villager backpacks, logistics, Mixins, datagen, or Fabric 26.2 APIs. Read AGENTS.md, Mods.md, and this skill's verified facts first. After each javap/docs verification, append the new knowledge to this skill so later tasks can decide without re-investigating. Also append Mods.md after every change.
---

# Civitas Fabric 26.2（Cursor）

專案路徑：`C:\Minecraft`。Mod ID：`civilizationmod`。這是給 Cursor Agent 用的專案 skill；原 Claude／Codex `.skill` 的工作流已改寫為 Cursor 工具與本機 Gradle／javap 流程。

## 何時套用

編輯本倉庫、文明模擬、倉庫、村民、命令、Saved Data、Mixin、datagen、locale 或 Fabric 26.2 API 時套用。先讀本檔硬規則，再依任務讀對應 reference，不要一次載入全部長文。

## Cursor 執行方式

1. 用 Read 先讀 `AGENTS.md`、`Mods.md`、本檔「已確認判斷捷徑」，再讀即將修改的 Java／JSON。
2. 用 Grep／Glob 找現有 handler、locale key、Codec 欄位；不要重寫一套平行實作。
3. API 先對照本 skill 已寫入的查證。已有日期、jar、簽名與採用規則時直接沿用，不要重跑 javap。缺口才 WebSearch／WebFetch 官方 26.2 文件，再用 Shell 對實際 jar 跑 `javap`。Windows sandbox 若擋 javap／Gradle，改以完整權限重跑，不要改猜簽名。
4. **查證完成當次必須把新知識寫進 skill**（見下方「查證後寫回 skill」）。未寫回不得宣稱查證完成。
5. 驗證用 `gradlew.bat --no-daemon`。不要預設啟動 `runClient`／`runServer`（會佔住終端）。未執行的命令在 `Mods.md` 標成未驗證。
6. 玩家可見回覆用繁體中文。程式識別字、命令、類別名維持現有英文。
7. 未更新 `Mods.md` 不得宣稱完成。

### javap

```bat
"C:\Program Files\Java\jdk-25.0.2\bin\javap.exe" -classpath "%USERPROFILE%\.gradle\caches\fabric-loom\26.2\minecraft-common.jar" net.minecraft.world.entity.npc.villager.Villager
```

client-only 類別改查對應 `minecraft-client.jar`。常見 jar 路徑：`C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`。

### 依任務讀 reference

| 任務 | 再讀 |
|---|---|
| 食物、Saved Data、simulate、人口 provider | [verified-apis.md](verified-apis.md) |
| warehouse_marker、ItemFrame、門、floor scan、glint | [building-geometry-26.2.md](building-geometry-26.2.md) |
| TekTopia 房屋判定對照 | [tektopia-structure-detection.md](tektopia-structure-detection.md) |
| MineColonies 資料分層對照 | [minecolonies-reference.md](minecolonies-reference.md) |
| 指派、外套 Mixin、27 格背包、物流、datagen | [verified-apis.md](verified-apis.md) |

## 已確認判斷捷徑

先用這張表決定實作方向。細節在對應 reference；表上沒有的才查證並寫回。

| 情境 | 直接採用 | 不要做 |
|---|---|---|
| warehouse 啟用 | ItemFrame + 完整 territory + 支撐牆；`WarehouseTerritoryValidator` | 不要再用門／floor／roof 當啟用門檻 |
| Marker 部署 | `handStack.copyWithCount(1)` 再 `shrink(1)` | 不要 `new ItemStack` 重建，會丟 CustomData |
| Marker 選點 | 左鍵 A、右鍵 B；未完成 A 在切換物品時清除 | 不要挖掉 A 點或放置 B 點方塊 |
| 背包 | 27 格 `CivitasBackpack` Mixin，物流讀這份 | 不要用原版 8 格 `AbstractVillager.getInventory()` 當物流來源 |
| 背包 GUI 標題 | `titleLabelX/Y`、`inventoryLabelX/Y` | 不要再加 `leftPos`／`topPos` |
| 物流 | snapshot allowlist、每次一格、只 `Blocks.CHEST` | 不要 trapped chest／barrel／hopper |
| 村民外套畫面 | client overlay layer + `civitas_role` | 不要以為胸甲槽寫入就會畫出來 |
| Mixin `addLayer` | `@Mixin(LivingEntityRenderer)` + `@Invoker` | 不要在 `VillagerRenderer` `@Shadow addLayer` |
| Saved Data | Codec + `optionalFieldOf` + `setDirty()` | 不要改 schema 卻不升 `CURRENT_SCHEMA_VERSION` |
| 命令 | `/civitas` 與 `/civilization` 同一 handler | 不要兩套命令樹 |
| 玩家訊息 | `CivilizationMessages.translatable` | 不要 `sendSuccess`／硬編碼中文 |
| 外部村莊模組 | adapter／tags／可選相容 | 不要當成硬依賴或直接抄 Forge API |

## 查證後寫回 skill

每次 javap、官方文件、bytecode 或編譯確認了**新的**類別、方法、事件、Mixin 注入點、資料規則或「不要做」之後，在同一輪工作寫回 skill，讓下次直接判斷。

1. 先搜本 skill 是否已有相同結論。有則只補缺的簽名、禁止事項或日期；不要複製整段。
2. 依主題追加到正確檔案，**追加不覆寫歷史**：
   - 幾乎每次都會用到的硬規則／捷徑 → 本檔「已確認判斷捷徑」加一列
   - Fabric／Minecraft 26.2 API、切片規則、Mixin、背包、物流 → [verified-apis.md](verified-apis.md)
   - ItemFrame、領地、幾何 → [building-geometry-26.2.md](building-geometry-26.2.md)
   - TekTopia／MineColonies 研究 → 對應 reference
   - 新主題且會反覆用到 → 新增同層 `.md`，並在「依任務讀 reference」加一列
3. 同步追加到 `skills/minecraft-civilization-fabric-262/` 對應檔，避免 Cursor 與 Codex 技能分叉。
4. `SKILL.md` 維持精簡（目標 < 500 行）。完整 javap 輸出不要貼進主檔。
5. 推測、未執行的命令、第三方論壇說法**不得**寫成已確認知識。
6. `Mods.md` 仍要記本次變更；skill 負責可重用判斷，`Mods.md` 負責這次做了什麼。

追加格式：

```markdown
## YYYY-MM-DD — <一句結論>

- 任務：<這次要決定什麼>
- 已確認：`<Class.method>`／簽名／回傳值
- 採用：<之後直接照做的規則>
- 禁止：<之後不要再試的做法>
- 查證：jar 或官方 URL；日期 YYYY-MM-DD
```

## Civitas 開發規則（for Cursor Agents）

本規則彙整自 C:\Minecraft 專案內所有 *.md 文件，適用於任何參與開發的 AI 助手。
修改前必須閱讀：AGENTS.md、Mods.md（專案根目錄）及相關技能檔案。
修改後必須更新：Mods.md（追加記錄，不覆寫歷史）。

### 1. 專案環境與工具鏈

| 項目 | 版本 / 設定 |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 |
| Fabric Loom | 1.17-SNAPSHOT（實際 1.17.19） |
| Fabric API | 0.158.0+26.2 |
| Gradle | 9.5.1 |
| Java | 專用工具鏈 25（build.gradle 已設定 toolchain { languageVersion = JavaLanguageVersion.of(25) }） |
| Mod ID | civilizationmod |
| Maven group | com.civilizationmod |

不得將 release 降回 Java 21，全域 Java 21 保持不變。
執行 Gradle 任務時統一使用 `gradlew.bat --no-daemon`（如 build、runClient）。
任何新增 API 使用前，必須以 javap 查證實際 minecraft-common.jar 或 client.jar 的簽名，不可憑舊版文件或猜測。

### 2. 核心開發原則（最高優先）

- 先讀檔再改檔：每次修改前，必須讀取 AGENTS.md、Mods.md 及相關程式碼。
- 每次修改後更新 Mods.md：追加日期、變更內容、影響檔案、驗證結果、風險與下一步；不得另建重複進度檔。
- 不要猜 API：所有版本敏感的 API（Saved Data、Commands、Events、Mixin 等）必須查證官方文件或實際 javap。
- 保護 server/client 邊界：src/main/java 不得依賴 client-only 類別；GUI、Renderer、Screen 等放在 src/client/java。
- 外部模組不可作為硬依賴：缺少外部村莊模組時，civilizationmod 仍應能啟動。
- 先做可驗證的垂直切片：優先完成最小閉環（Saved Data → 掃描 → 指派 → 物流），避免一次實作完整 AI 或 worldgen。
- 所有玩家可見文字必須本地化：使用 Component.translatable，同步更新三份語言檔（en_us、zh_tw、zh_cn，後兩者內容相同為繁體中文）。
- 所有玩家訊息必須有金色品牌前綴：透過 CivilizationMessages.translatable(...) 包裝，格式為 |Civitas| : {message}（英文）或 |創世紀元| : {模組訊息}（中文），僅前綴金色。
- 所有命令與參數必須支援 Tab completion：Brigadier literal 與參數都必須提供 suggestions。
- 命令別名必須共用邏輯：例如 /civitas 與 /civilization 使用相同的 handler、suggestions 與權限。

### 3. 原始碼結構（common / client 分離）

- src/main/java → common（server + shared）
- src/client/java → client only（Screen、Renderer、Mixin target client classes）
- src/main/resources → common 資源
- src/client/resources → client 資源（含 civilizationmod.client.mixins.json）

規則：

- client-only 類別 必須 放在 src/client，src/main 不得引用。
- fabric.mod.json 中 只 註冊 common entrypoint；client entrypoint 在 src/client 獨立處理。
- client Mixin 設定在獨立的 civilizationmod.client.mixins.json，並在 fabric.mod.json 中宣告。

### 4. 命令系統（Commands）

- 主命令根為 /civitas，同時保留 /civilization 作為相容別名。
- 使用 Brigadier 建立命令樹，所有 literal、參數都必須可 Tab 補全。
- 動態參數（如 index、玩家名稱）需提供 SuggestionProvider。
- 權限控制使用 Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)。
- 回饋訊息必須透過 CivilizationMessages.translatable(...) 發送，不直接 sendSuccess / sendFailure。
- 所有錯誤訊息也必須本地化。

### 5. 數據持久化（Saved Data）

- 使用 SavedData、SavedDataType<T>、DimensionDataStorage.computeIfAbsent(...)。
- 所有持久化資料 必須 使用 Codec 與 RecordCodecBuilder。
- 每次 Schema 變更時 必須 提升 CURRENT_SCHEMA_VERSION，並確保舊資料可透過 optionalFieldOf 或預設值相容載入。
- 修改資料後 必須 呼叫 setDirty() 以確保存檔。
- 新增欄位應優先使用 optionalFieldOf 並提供 orElse(...) 避免破壞舊存檔。

### 6. 建築 / Marker / 領地機制（Warehouse）

#### 6.1 Warehouse Marker

- 使用 warehouse_marker 物品（合成配方：周圍 8 個原版箱子 + 中央鐵錠）。
- 玩家左鍵選 point A，右鍵選 point B（須手持同一 marker），生成 WarehouseTerritory（正規化 bounds）。
- Territory 資料寫入 DataComponents.CUSTOM_DATA，隨 ItemStack 保存。

#### 6.2 直接部署至 ItemFrame

- 玩家蹲下並直接右鍵空的 ItemFrame，且手中 marker 已有完整 territory，才會將 marker 原封不動（含全部 CustomData）轉移至 ItemFrame，手中數量減 1。
- 使用 `copyWithCount(1)` 保留資料，不得重建新 ItemStack。
- 已撤回「蹲下右鍵空氣後以視線尋找 ItemFrame」路徑；不可重新加入 `UseItemCallback` air-target handler。

#### 6.3 Building Scan

- /civitas building scan [radius] 掃描附近已載入 ItemFrame，只辨識 BuildingMarkerRegistry 中已註冊的 marker。
- 新增 / 更新 BuildingObservation，包含 dimension、marker position、function、validation status、resident、storage snapshot 等。
- 已消失的 marker 由 BuildingResidentService 每 20 tick 清理（removeMissingBuildings），但不會強制載入 chunk。

#### 6.4 驗證（Validation）

- 使用 WarehouseTerritoryValidator 優先檢查 territory、維度、ItemFrame 支撐牆。
- 驗證成功時，server 對 ItemFrame 內的 marker 設定 DataComponents.ENCHANTMENT_GLINT_OVERRIDE=true（附魔光效）；失敗則移除。
- BuildingObservation.validationStatus 為 valid / invalid；無 territory 的舊 marker 回傳 territory_missing。

### 7. 村民指派與角色（Resident）

- 命令：/civitas assign <building_index> [villager]（省略村民時從玩家視線選取最近的存活 Villager）。
- 指派關係保存於 BuildingObservation（residentUuid、residentName）。
- 指派時套用角色裝備（BuildingRoleEquipment）：warehouse 為黃色皮革胸甲 + civitas_role=warehouse。
- 同一村民不能重複指派至不同建築。
- 村民導航：BuildingResidentService 每 20 tick 檢查距離，超過 8 格則呼叫 PathNavigation.moveTo(marker位置)。

### 8. 村民 27 格背包（Civitas Backpack）

- 使用 common Mixin 注入 Villager 實體資料（獨立於原版 8 格交易 inventory）。
- 資料保存於 ValueOutput.child("CivitasBackpack")，使用 ContainerHelper.saveAllItems/loadAllItems。
- 背包為 27 格（9×3），繼承 ChestMenu 並使用三列構造器。
- 右擊已指派且有效的村民時，開啟 CivitasVillagerBackpackMenu（取代原版交易）。
- GUI 使用 ContainerScreen（原版 container 背景），標題位置為 titleLabelX/Y，不得 再加 leftPos/topPos。
- 三份 locale 提供背包標題與玩家物品欄文字。

### 9. 物流（Warehouse Logistics）

- BuildingLogisticsService 每 20 tick 對已抵達 marker（距離 < 8 格）的 warehouse 村民執行。
- 只允許 上一次 storage snapshot 中已存在的物品 registry ID 入庫（暫時 allowlist）。
- 從村民的 27 格背包逐格取出符合條件的物品，放入 territory 內普通箱子（支援雙箱）。
- 每次最多處理一格，成功後立即重新掃描 storage 並更新 BuildingObservation.storage。
- 不支援 trapped chest、barrel、hopper 等（第一版僅限 Blocks.CHEST）。

### 10. 物品與配方（Datagen）

- 所有配方使用 Fabric Datagen（FabricRecipeProvider）產生，存放於 src/main/generated。
- 執行 gradlew.bat runDatagen 生成 JSON。
- 配方必須包含解鎖 advancement。
- warehouse_marker 配方固定為 Shaped：周圍 8 個箱子 + 中央鐵錠。

### 11. 測試（Regression）

- 純 Java 回歸測試位於 src/test/java，使用 main 與 AssertionError（無 JUnit）。
- 執行：gradlew.bat runFoodModelRegressionTest（由 Gradle task 定義）。
- 涵蓋：食物模型、建築幾何、territory、storage snapshot、resident persistence 等。
- 新增邏輯應盡可能新增對應的 regression 測試。

### 12. API 查證與參考來源

所有 Minecraft / Fabric API 使用前，必須確認其簽名存在於實際 26.2 jar：

- common: C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar
- client: 對應的 minecraft-client.jar
- 常用命令：javap -cp <jar> <class>

Fabric 官方文件（26.2）：Creating a Project、Saved Data、Commands、Events、Items、Entities / Renderers、Porting to 26.2。
外部模組（TekTopia、MineColonies）僅供設計參考，不可直接移植 API。

### 13. 專案文件管理

- 唯一正式進度檔：Mods.md（專案根目錄）。
- 所有 AI 進入專案時 必須 先讀取 Mods.md 與 AGENTS.md。
- 修改任何檔案後 必須 更新 Mods.md，追加日期、變更、影響檔案、驗證結果與下一步。
- 不得覆寫既有歷史，不得另建第二份進度檔。
- Cursor 技能位於 `.cursor/skills/civitas-fabric-262/`；Claude／Codex 副本位於 `skills/minecraft-civilization-fabric-262/`。查證新知識必須兩邊同步追加。

### 14. 標準開發工作流（每次修改）

1. 讀取 AGENTS.md、Mods.md、本 skill 判斷捷徑、相關 Java 檔案。
2. 確認任務類型與 side（common / client）。
3. 對不確定的 API：先讀 skill 已查證內容；缺口才查官方文件或 javap。
4. 設計最小可行修改（包含失敗 fallback）。
5. 實作並編譯：`gradlew.bat --no-daemon compileJava compileClientJava`
6. 執行回歸測試：`gradlew.bat --no-daemon runFoodModelRegressionTest`
7. 完整建構：`gradlew.bat --no-daemon build`
8. （必要時）執行 runClient 進行煙霧測試（手動停止）。
9. 把本次新的已確認知識追加進 Cursor skill 與 `skills/minecraft-civilization-fabric-262/`。
10. 更新 Mods.md。
11. 提交（若使用版本控制）。

### 附錄：常用命令速查

| 任務 | 指令 |
|---|---|
| 編譯 common + client | `gradlew.bat --no-daemon compileJava compileClientJava` |
| 完整建構 | `gradlew.bat --no-daemon build` |
| 執行回歸測試 | `gradlew.bat --no-daemon runFoodModelRegressionTest` |
| 生成配方 / 資料 | `gradlew.bat --no-daemon runDatagen` |
| 啟動遊戲（煙霧測試） | `gradlew.bat --no-daemon runClient` |
| 檢查 Java 工具鏈 | `gradlew.bat --no-daemon javaToolchains` |

最後更新： 2026-08-20（基於 Mods.md 最新進度）
維護者： 愷愷及所有參與開發的 AI

## 來源 skill 不可省略的補充

桌面 `minecraft-civilization-fabric-262.skill` 另要求：

1. 不要猜版本敏感 API；不能把舊版、NeoForge、論壇或第三方模組類別名當成 Fabric 26.2 API。
2. 進度檔沿用現有檔名 `Mods.md`，不要另建 `mods.md`。
3. server 擁有真實狀態；client 只負責畫面、輸入與必要摘要。
4. 優先 Fabric events、Saved Data、commands、networking、registry、tags、datagen；沒有合適 hook 才用 Mixin，並記錄目標、side、注入點、風險與回歸。
5. 聚落來源用 `SettlementAdapter`（namespace、structure ID 或 tag、維度、中心、辨識時間、人口來源、資源、適配狀態）。未知結構不得自動當成文明聚落。
6. 新世界起始村莊分兩層：先搜尋既有村莊＋明確 fallback；保證生成村莊屬後續 worldgen，未查證 26.2 API 不得實作。
7. 身份由 server 保存，不等同 OP。觀察者／干預者、領袖、居民、獨立者的 GUI 與 command 共用同一個 `IdentityService`。
8. 命令縮寫必須是額外 literal alias，與完整命令共用 builder／handler。

### Mods.md 追加格式

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

## 官方文件

- https://docs.fabricmc.net/develop/getting-started/creating-a-project
- https://docs.fabricmc.net/develop/getting-started/project-structure
- https://docs.fabricmc.net/develop/saved-data
- https://docs.fabricmc.net/develop/events
- https://docs.fabricmc.net/develop/networking
- https://docs.fabricmc.net/develop/commands/basics
- https://docs.fabricmc.net/develop/commands/arguments
- https://docs.fabricmc.net/develop/text-and-translations
- https://docs.fabricmc.net/develop/data-generation/setup
- https://docs.fabricmc.net/develop/porting/
- https://fabricmc.net/2026/06/15/262.html


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


## 2026-08-21 — 住宅 marker 材質與 item model 資產規則

- 任務：讓 residential marker 1／2／4／6 在遊戲中有獨立且可辨識的視覺。
- 已確認：每級使用獨立 `16×16` RGBA PNG；item model 使用已驗證的 `minecraft:item/generated`，`textures.layer0` 分別指向對應 `civilizationmod:item/residential_marker_<capacity>`。
- 採用：共同使用側視床輪廓，容量數字 1／2／4／6 加上階級色彩區分；正式圖示保留一像素透明外框。視覺差異由 PNG 提供，不增加 client renderer 或 model loader。
- 禁止：住宅 marker 不得重用 warehouse 的箱子／容器紋理，也不要為簡單 item icon 猜測或引入未查證的 model override API。
- 查證：正式專案四個 PNG 的 IHDR 均為 `16×16`、color type `6` RGBA；四個 model 的 layer0 均指向各自貼圖；`clean build` 與 `runClient` smoke test 通過。日期 2026-08-21。


## 2026-08-21 — 住宅綁定裝置與重開後互動恢復

- 任務：修正住宅 roster 重開後看似失效，並新增住宅綁定裝置、解除指派與直接 ItemFrame 部署 fallback。
- 已確認：Minecraft 26.2 common jar 的 `Player.getInventory()`、`Inventory.getContainerSize()`、`Inventory.getItem(int)` 可供 server-side inventory 掃描；`DataComponents.CUSTOM_DATA`、`CustomData.copyTag()`、`CustomData.of(CompoundTag)` 可保存與複製 ItemStack CustomData。
- 採用：住宅綁定裝置以 `UseEntityCallback` 先蹲下右鍵有效住宅 ItemFrame 保存維度與 marker 座標，再右鍵 Villager 由 server 重新驗證 building、territory、床位與容量後加入 roster。開發測試可用 `/give @p civilizationmod:residence_binding_device`。
- 採用：若手持同類 marker 沒有完成 territory，直接 ItemFrame 部署可從玩家 inventory 找到同類且帶完成 `WarehouseTerritory` 的 stack，複製完整 CustomData 後再消耗實際資料來源；不可重建只含 item identity 的 marker。空氣右鍵視線尋找 ItemFrame 的路徑已撤回。
- 採用：`BuildingResidentService` 對保存 roster 的村民重新套用角色外套；invalid 建築只停止導航／物流，不阻止外觀與背包管理。新增 `/civitas unassign <building_index> [villager]`，相容 `/civilization`，沿用 suggestions 與 `EntityArgument.entity()`。
- 禁止：不要因 SavedData NBT 已含 `resident_uuid`／`residents` 就先重寫 roster Codec；重開後失效優先查 role reapply、validation gate 與 `findBuildingAssignedTo`。不要用未查證的 ItemFrame 專用事件或猜測 inventory 欄位。
- 查證：本機 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar` 的 javap；Fabric Events：https://docs.fabricmc.net/develop/events；Fabric Custom Item Interactions：https://docs.fabricmc.net/develop/items/custom-item-interactions；日期 2026-08-21。


## 2026-08-21 — 26.2 item definition 是 item icon 資源鏈必要節點

- 任務：診斷住宅綁定裝置在 GUI 內顯示紫／青 missing-texture 佔位圖。
- 已確認：專案正常 marker 的 `assets/civilizationmod/items/<id>.json` 使用 `{"model":{"type":"minecraft:model","model":"civilizationmod:item/<id>"}}`；這個 item definition 與 `models/item/<id>.json`、`textures/item/<id>.png` 共同構成 26.2 item icon 資源鏈。
- 根因：`residence_binding_device` 原本只有 `models/item/residence_binding_device.json` 與 PNG，缺少 `items/residence_binding_device.json`；因此 PNG 存在且可進 texture atlas，物品 icon 仍可能顯示 missing-texture。
- 採用：新增 item 時必須同時檢查 registry id、`items/<id>.json`、`models/item/<id>.json` 與 `textures/item/<id>.png`，並在 `processResources`／`build` 後確認四者進入 `build/resources/main`。
- 禁止：不要只以 PNG 存在、`models/item` 存在或 texture atlas 載入成功宣稱 item icon 資源完整。
- 查證：專案既有 `warehouse_marker.json`、`residential_marker_1.json`、`processResources` 成功輸出與 `build/resources/main` 檔案檢查；日期 2026-08-21。


## 2026-08-21 — 住宅綁定裝置配方材料查證

- 任務：新增住宅綁定裝置的 3×3 有序合成配方。
- 已確認：Minecraft 26.2 common jar 的 `net.minecraft.world.item.Items` 公開 `public static final Item STICK`；既有 `CivilizationModRecipeProvider` 使用 `FabricRecipeProvider` 與 `RecipeProvider.shaped(...)`。
- 採用：配方 pattern 為 `sss`、`sis`、`sss`，`s = Items.STICK`、`i = Items.IRON_INGOT`，輸出 `CivilizationItems.RESIDENCE_BINDING_DEVICE`，解鎖條件為 `has(Items.STICK)`。
- 禁止：不要將本配方改成 shapeless；不要把木棍寫成猜測的字串或自行建立新材料物品。
- 查證：本機 Minecraft 26.2 common jar 的 `javap`、既有 recipe provider；日期 2026-08-21。


## 2026-08-21 — 市政廳 marker 有序合成配方材料查證

- 任務：依使用者指定新增市政廳 marker 的 3×2 有序合成配方。
- 已確認：Minecraft 26.2 common jar 的 `net.minecraft.world.item.Items` 公開 `EMERALD`、`IRON_SWORD`、`IRON_PICKAXE`、`IRON_AXE` 與 `IRON_SHOVEL`；Fabric 官方 Recipe Generation 26.2 文件確認 `pattern`、`define`、`unlockedBy` 與 `save(output)` 的 datagen 流程。
- 採用：pattern 為 `esp`、`ah `；第一列依序是綠寶石、鐵劍、鐵鎬，第二列依序是鐵斧、鐵鏟並靠左排列，第三格為空白。Minecraft 26.2 的 `ShapedRecipeBuilder.pattern(...)` 要求每列寬度相同，不能直接使用兩字元的 `ah`；輸出 `CivilizationItems.TOWN_HALL_MARKER`，以綠寶石作為 recipe advancement unlock。
- 禁止：不要把此配方改成 shapeless；不要用字串 ID 取代已查證的 `Items` 常數，也不要手寫 generated JSON 取代 recipe provider。
- 查證：[Fabric Recipe Generation 26.2](https://docs.fabricmc.net/develop/data-generation/recipes)、本機 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar` 的 JDK 25 `javap`；日期 2026-08-21。


## 2026-08-21 — 自訂 NPC Entity 路線評估

- 任務：評估是否立即將原版 Villager 替換為 Civitas 自訂居民 Entity。
- 已確認：Fabric 官方 [Creating Your First Entity 26.2](https://docs.fabricmc.net/develop/entities/first-entity) 的最小流程是 `PathfinderMob` 子類、`EntityType.Builder.of(...).sized(...)`、`FabricDefaultAttributeRegistry.register(...)`、server-side goals，以及 client-only render state、model layer、renderer 與 `EntityRenderers.register(...)`。持久 entity-local state 使用 `addAdditionalSaveData(ValueOutput)`／`readAdditionalSaveData(ValueInput)`；需要顯示同步的短期狀態才使用 `SynchedEntityData`。
- 採用：Civitas 目前不立即新增自訂 Entity。先完成 Town Hall colony ownership、住宅／warehouse 指派與物流閉環，再從 `ResidentRecord`／`ResidentRegistry` 抽離居民真實資料；現有原版 Villager 先作為可替換 body adapter。
- 採用：`BuildingObservation.residents` 與 `CivilizationWorldData.findBuildingAssignedTo(String)` 可作為過渡，但尚缺全域 colony ID、住宅 building ID、工作 building ID、角色、生命週期與 body type 等 ResidentRecord 欄位。
- 禁止：不要把 colony truth 只塞進 Entity NBT；不要在同一個切片同時處理 EntityType、renderer、背包、物流、住宅容量與完整工作 AI；不要把官方 26.2 文件範例當成未經本機 compile／javap 驗證的任意舊版 API。
- 查證：官方 Entity 26.2 文件與目前 `BuildingObservation`／`CivilizationWorldData` 架構檢查；日期 2026-08-21。


## 2026-08-21 — ResidentRecord 雙 UUID 與 26.2 lookup 修正

- `residentId` 是 Civitas 永久邏輯身份，`entityUuid` 是目前 Minecraft body 的 UUID；原版 Villager 初次指派時使用 `villager.getUUID()` 作為 entityUuid，但未來換 body 時只更新 entityUuid，不更換 residentId。
- `BuildingObservation` 未來應保存 residentId，而不是把 entityUuid 當作殖民地身份。現有 `ResidentAssignment.uuid` 是 legacy：遷移舊世界時第一階段可把既有 UUID 同時當作 residentId 與 entityUuid，避免無法反查；新的 assign 才建立獨立 residentId。
- 不得在 chunk unload 時清除 entityUuid；lookup 找不到可能只是 body 未載入。只有 server 驗證的死亡、移除或 rebind 才能改 entityUuid／lifecycle。第一版不自動復活死亡原版村民。
- Minecraft 26.2 common jar javap 確認 `Entity.getUUID()`、`Entity.setUUID(UUID)`，`ServerLevel.getEntityInAnyDimension(UUID)`；未確認 `level.getEntity(UUID)`，`EntityGetter` 公開的是空間 `getEntities(...)` 與 `getPlayerByUUID(UUID)`。禁止把附件中的 `level.getEntity(entityUuid)` 當作 26.2 API。
- SavedData 保存 ResidentRecord canonical list／Codec；`byResidentId` 與 `byEntityUuid` 只是在載入後重建的 runtime indexes，不是第二個可修改的真相來源。building relationship 應使用 dimension + marker coordinates 或未來 immutable building ID，不得保存易變的 one-based scan index。
- 查證：本機 Minecraft 26.2 common jar `javap`，JDK 25；日期 2026-08-21。


## 2026-08-21 — Civitas 自動化測試分層

- Fabric 官方 [Automated Testing 26.2](https://docs.fabricmc.net/develop/automatic-testing) 將測試分為 Fabric Loader JUnit unit tests 與 Minecraft GameTest；JUnit 測試 helper、model、Codec 與 server-independent invariants，GameTest 啟動實際 Minecraft server/client 驗證 gameplay。
- JUnit 設定採 `testImplementation "net.fabricmc:fabric-loader-junit:${project.loader_version}"` 與 `test { useJUnitPlatform() }`。測試若觸及 registry-dependent 類別，`@BeforeAll` 必須先呼叫 `SharedConstants.tryDetectVersion()` 與 `Bootstrap.bootStrap()`。
- 本專案保留相容的 `runFoodModelRegressionTest` JavaExec，另以 `CivitasCoreTest` 納入 JUnit；Gradle `check` 依賴既有 regression task，因此 `build` 會同時執行兩層 common-side 回歸檢查。
- 第一批 JUnit 覆蓋 FoodDemandModel、SettlementAdapter 去重與狀態保留、BuildingObservation roster／Codec、building 維度與範圍綁定、WarehouseTerritory 邊界、Town Hall 唯一性與 Codec。ItemFrame 真實掃描、SavedData reload、居民互動、背包／物流與 client renderer 留待 GameTest。
- 不要把 client-only renderer 測試放入 common `src/test/java`；未建立 Loom GameTest source set 且未實際執行前，不得宣稱 server/client gameplay 已覆蓋。


## 2026-08-21 — 市政廳 marker 視覺 prototype 與功能分層

- 任務：建立市政廳 marker 的方塊式 item model 與獨立材質。
- 已確認：ItemFrame marker 目前使用 item model 資源鏈：`items/<id>.json` → `models/item/<id>.json` → `textures/item/<id>.png`；既有 `BuildingFunction` 只有 warehouse／residence。
- 採用：`town_hall_marker` 先註冊為純視覺、單件堆疊 item，材質使用石磚市政建築、深藍圓頂、金色徽記、深紅旗幟；與 warehouse 箱子及 residential 床位數字保持清楚差異。
- 採用：在 `TOWN_HALL` 功能、殖民地核心 SavedData、範圍、居民上限與建築解鎖規則決定前，不把 Town Hall 加入 `BuildingMarkerRegistry`，避免共用 territory selection 或 building scan 提前把 prototype 當成可運作建築。
- 禁止：不要把市政廳 marker 當成已完成 Town Hall 功能宣稱；不要在尚未定義 server 規則前複製 warehouse／residence validation branch。
- 查證：`BuildingFunction.java`、`BuildingMarkerRegistry.java`、`CivilizationItems.java` 與 26.2 build/runClient smoke test；日期 2026-08-21。


## 2026-08-21 — marker 選點不因切換手持物而取消

- 任務：降低領地選點操作的使用負擔。
- 已確認：舊機制在 `CivitasBuildingMarkerItem.inventoryTick(...)` 以 `equipmentSlot == null` 判定 marker 不在手上，呼叫 `WarehouseTerritory.clearPendingSelection(...)`；玩家現在要求移除這項自動清除。
- 採用：移除 base marker 的 `inventoryTick` 清除邏輯；A 點、已完成領地與 CustomData 在切換快捷欄後保留。跨維度 B 點仍由 `WarehouseTerritory.complete(...)` 清除不相容 pending selection；超大領地仍保留 A 點讓玩家重選 B。
- 採用：移除 `civilizationmod.building.selection.cancelled` stale locale key，並新增 regression 確認 pending A 在 ItemStack copy 後仍可完成 territory。
- 禁止：不要再用目前手持物或 equipment slot 自動推導玩家是否要取消選點。
- 查證：`CivitasBuildingMarkerItem.java`、`WarehouseTerritory.java`、`FoodModelRegressionTest.java`；日期 2026-08-21。


## 2026-08-21 — 市政廳核心 SavedData 與 ItemFrame API 查證

- 已確認：Minecraft 26.2 common jar 的 `ServerLevel.getDataStorage()` 回傳 `net.minecraft.world.level.storage.SavedDataStorage`；`SavedDataStorage.computeIfAbsent(SavedDataType<T>)` 可取得或建立 Codec-backed SavedData。
- 已確認：`SavedDataType<T>` 建構子為 `SavedDataType(Identifier, Supplier<T>, Codec<T>, DataFixTypes)`；現有 `CivilizationWorldData.TYPE` 的 `null` data fixer 寫法與實際 mapping 相容。
- 已確認：26.2 ItemFrame 公開 `getItem()`、`setItem(ItemStack)`／`setItem(ItemStack, boolean)`；ItemFrame 的 `blockPosition()`、`getDirection()` 由既有 `HangingEntity`／Entity API 提供，專案 geometry reference 已查證 frame 支撐位置為 `frame.blockPosition().relative(frame.getDirection().getOpposite())`。
- 採用：市政廳核心資料放入既有 overworld-scoped `CivilizationWorldData`，由 Codec optional field 保存，修改後呼叫 `setDirty()`；不建立 client-only state 或未查證 ItemFrame event。
- 查證：本機 `C:\Program Files\Java\jdk-25.0.2\bin\javap.exe` 對 `C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`；Fabric Saved Data 官方文件 https://docs.fabricmc.net/develop/saved-data；日期 2026-08-21。


## 2026-08-21 — 市政廳核心辨識與 SavedData

- 採用 `BuildingFunction.TOWN_HALL` 與 `BuildingMarkerRegistry` metadata；`town_hall_marker` 成為 building scanner 可辨識 marker，但 `CivitasBuildingMarkerItem` 與 `WarehouseMarkerQuickDeploy` 只接受 warehouse／residence territory marker，避免市政廳誤觸發兩點選取或 warehouse 快速放置。
- 新增 `TownHallCore` common record，保存 `colony_id`、`dimension`、marker 座標、`created_at` 與 `radius`。核心資料放在既有 `CivilizationWorldData` 的 optional `town_halls` Codec list，schema version 由 6 升為 7；舊世界缺少欄位時使用空 list。
- 第一版唯一性規則為每個維度一座 Town Hall；同一 marker 重掃回傳 `EXISTING` 且不改變建立時間，不同 marker 同維度回傳 `DUPLICATE`，不同維度可註冊另一座核心。初始 radius 為 64，保存在 core 資料中供後續殖民地範圍使用。
- `TownHallValidator` 只要求 server 已載入且 ItemFrame 以已查證支撐牆公式附著；不套用 warehouse／residence 的門、地板、屋頂、牆體或床位規則。第二座同維度 marker 仍保留 BuildingObservation，但以 `duplicate_town_hall` 標為 invalid。
- `/civitas townhall` 與 `/civilization townhall` 無參數查詢已保存核心；`building scan` 額外顯示新增核心與衝突數量。所有命令使用共用 handler 與 locale。
- 查證與驗證：26.2 common jar javap、Fabric Saved Data 官方文件、`runFoodModelRegressionTest`（含 TownHallCore Codec／唯一性測試）與 `build`；日期 2026-08-21。


## 2026-08-21 — marker glint 失效與 ItemFrame 拆除前清理

- 已確認：Minecraft 26.2 common jar 的 `ItemStack.remove(DataComponentType<? extends T>)` 會回傳被移除的 component；`DataComponents.ENCHANTMENT_GLINT_OVERRIDE` 是公開 Boolean component type。可用回傳值判斷 marker 是否真的帶有 Civitas glint。
- 已確認：`ItemFrame.setItem(ItemStack)` 會委派 `setItem(ItemStack, true)`；`setItem(ItemStack, boolean)` 的 boolean 只控制後續紅石鄰居更新，entity data 在兩種呼叫中都會更新，因此 invalid glint 問題不是 client sync 參數造成。
- 根因：掃描時仍在 ItemFrame 內的 invalid marker 可由 `BuildingMarkerVisualState.apply(frame, false)` 移除 glint；但玩家直接打掉 ItemFrame 後，ItemFrame 已不存在，沒有 frame 可供 invalid path 更新，掉落／拿下的 stack 會保留 glint component。
- 採用：以 Fabric API 0.158.0+26.2 的 `AttackEntityCallback.EVENT` server-side 監聽 ItemFrame 被攻擊，在原版移除與掉落前複製 marker stack、只移除 `ENCHANTMENT_GLINT_OVERRIDE`，再以 `frame.setItem(updated, false)` 寫回，最後回傳 `PASS` 保留原版拆除、掉落與 CustomData。非 marker、無 glint、client、spectator 一律 PASS。
- 禁止：不要取消 ItemFrame 攻擊、不要重建 marker item、不要清除 `CUSTOM_DATA` 或 territory；本次只修正視覺 glint。
- 查證：26.2 common jar javap、Fabric events/custom interactions 文件與 Fabric API events-interaction jar；日期 2026-08-21。


## 2026-08-21 — 通用 Civitas 綁定裝置與快速部署歷史

- 正式通用物品 ID 採 `civitas_binding_device`，顯示名稱為 Civitas Binding Device／Civitas 綁定裝置。舊 `residence_binding_device` 不刪除，改由相容 subclass 提供相同通用行為與 tooltip，避免既有物品 stack／世界資料失效；新配方輸出正式通用 ID。
- 綁定 CustomData schema 座標格式不變；新資料 key 使用 `civitas_binding`，讀取時 fallback 到舊 `civitas_residence_binding`，因此已選住宅資料可被新裝置繼續使用。
- `ResidenceBindingDeviceInteraction` 已泛化為所有 `BuildingMarkerRegistry.isKnownMarker(...)` 的有效 building observation。住宅仍做即時 territory／床位檢查、容量與 roster append；warehouse／Town Hall 沿用 `/civitas assign` 的 single-resident overwrite 語義，不新增未設計的容量規則。
- 歷史查證曾確認 Fabric API `UseItemCallback.EVENT` 與 `interact(Player, Level, InteractionHand)`，但本次撤回 air-target 快速部署後，runtime 不再註冊或使用此 callback。
- 目前只保留直接右鍵 ItemFrame 的 `UseEntityCallback` 路徑；其 server 驗證仍包含 territory、維度、牆面附著、重複 marker、CustomData copy 與背包 fallback。
- air-target 的視線 AABB／dot／line-of-sight 尋找器已撤回；不要把它當成可用功能或重新加入。
- 查證：Fabric Events／Custom Item Interactions 官方文件、Fabric API 0.158.0+26.2 jar javap；air-target 路徑撤回日期 2026-08-21。


## 2026-08-21 — Town Hall 可設定範圍與 colony_id 歸屬切片

- 任務：將 Town Hall 從每維度單一核心改為可設定、不重疊的多核心殖民地。
- 已確認：`TownHallCore.DEFAULT_RADIUS=64`、`MAX_RADIUS=512`；`contains` 是 inclusive cubic range；同維度 `overlaps` 以三軸距離與半徑總和判定。
- 採用：同 marker `EXISTING`、新核心不重疊 `REGISTERED`、重疊 `DUPLICATE`；`/civitas townhall radius <index> <radius>` 有 index／常用半徑 Tab completion；有效住宅／warehouse 只有唯一核心包含時才保存 `colony_id` 並成為 `isColonyBound()`。
- 採用：沒有核心、範圍外、重疊範圍分別保存 `no_town_hall`、`outside_town_hall`、`overlapping_town_hall`；未 colony-bound 建築不參與導航、物流、assign 或綁定裝置。
- 採用：`BuildingObservation` 新欄位放在 nested CodecTail optional fields；legacy status 可保留但沒有 colony_id 不算 colony-bound。
- 驗證：`compileJava compileClientJava test`、`runDatagen`、`runFoodModelRegressionTest`、`build` 均成功；日期 2026-08-21。


## 2026-08-21 — 市政廳無序合成 recipe API 查證

- 任務：將 Town Hall marker 改為綠寶石必須存在、四種鐵工具順序不限的 shapeless recipe。
- 已確認：Minecraft 26.2 common jar 的 `net.minecraft.data.recipes.ShapelessRecipeBuilder` 公開 `requires(ItemLike)`、`requires(ItemLike,int)`、`unlockedBy(...)` 與 `save(...)`；目前 `FabricRecipeProvider` 的 `shapeless(RecipeCategory, ItemLike)` 產生流程可編譯並由 runDatagen 驗證。
- 採用：`shapeless(RecipeCategory.MISC, TOWN_HALL_MARKER).requires(Items.EMERALD).requires(Items.IRON_SWORD).requires(Items.IRON_PICKAXE).requires(Items.IRON_AXE).requires(Items.IRON_SHOVEL)`；每種材料各一份，排列不影響配方，解鎖條件使用持有綠寶石。
- 禁止：不要保留市政廳的 `pattern(...)` shaped layout，也不要用單一 `ItemTags` 取代四種指定工具。
- 查證：Minecraft 26.2 common jar `ShapelessRecipeBuilder` javap，日期 2026-08-21。
