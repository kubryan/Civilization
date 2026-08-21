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

### 7. 實作與驗證順序

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
