# CivilizationMod — Civitas

> **⚠️ 免責聲明：本專案絕大多數的程式碼與內容皆由 AI 輔助生成。雖已盡力確保功能與品質，但仍請知悉此專案的開發過程高度依賴 AI。歡迎任何貢獻、錯誤回報與反饋！**

**Civitas（創世紀元）** 是一個以 Minecraft Java Edition 26.2 與 Fabric 為基礎的殖民地建設及文明模擬模組。玩家透過原版方塊親手建造房屋與基礎設施，再使用 Civitas 建築標誌宣告建築用途，建立市政廳殖民地、住宅、倉庫與居民之間的關係。

本專案的長期目標是建立可擴充、可保存、可資料驅動的文明模擬框架。玩家將逐步管理殖民地的建築、居民、住宅、食物、資源物流、工作職能、政策與文明事件。文明真實狀態由 logical server 保存與驗證，client 主要負責畫面、渲染與玩家輸入；外部村莊模組只會透過可選 adapter、tags 或資料驅動方式整合，不會成為核心硬依賴。

## 目前核心玩法

Civitas 採用「玩家宣告意圖，系統驗證最低條件」的建築哲學。玩家不是要求模組猜測所有房屋形狀，而是使用專用 marker 建立 territory，再把 marker 放入物品展示框。ItemFrame 及其 marker 是建築功能的明確宣告；server 會驗證 marker、領地、維度、展示框位置、建築功能、殖民地歸屬與重複登記。

### Town Hall 殖民地核心

Town Hall 是目前殖民地建設主線的核心建築。市政廳 marker 放入附著於牆面的 ItemFrame 後，透過 building scan 登記為 Town Hall core，資料會保存於 `CivilizationWorldData`。每座核心都有自己的 `colony_id`、維度、座標、建立時間與範圍半徑。

同一維度可以存在多座 Town Hall，但各自的軸對齊立方體範圍不可重疊；不同維度的 Town Hall 可以並存。預設半徑為 64，允許使用的最大半徑為 512。可使用以下命令調整指定核心的半徑：

```text
/civitas townhall
/civitas townhall radius <index> <radius>
```

有效的住宅與 warehouse 只有在唯一 Town Hall 範圍內時，才會獲得 `colony_id` 並成為 colony-bound 建築。為了避免舊世界在尚未建立市政廳時突然停止運作，若該維度完全沒有 Town Hall，**有效但未綁定的建築會以過渡模式繼續執行居民導航、warehouse 物流、居民指派與通用綁定裝置功能**；建築列表與 assign／綁定成功訊息會明確顯示過渡模式。當該維度建立 Town Hall 後，規則立即恢復嚴格模式：範圍外與重疊範圍的建築仍不可 assign、不可物流、不可使用綁定裝置，也不會取得 `colony_id`。不同維度各自判定，因此某維度沒有市政廳不會放寬另一個已有市政廳維度的範圍限制。

### Warehouse 倉庫與物流

玩家可以使用 `warehouse_marker` 宣告倉庫。玩家先用 marker 左鍵選取 territory 的 A 點，再以右鍵選取 B 點；完成的 territory 會保存於 ItemStack 的 CustomData。接著把 marker 放入領地內、附著於牆面的 ItemFrame，再執行建築掃描。

`warehouse_marker` 的合成配方為周圍八個原版儲物箱、中央一個鐵錠。倉庫領地只清楚定義玩家指定的工作區域，不會依賴門、屋頂或牆面猜測房屋用途。warehouse marker 被拆除時，Civitas 只清除建築登記；不會搬動、掉落、回收或清空領地內的任何箱子、方塊或物品。

有效 warehouse 會掃描領地內已載入的普通原版箱子，保存 storage snapshot。第一版物流使用上一個成功掃描得到的物品 registry ID 作為安全 allowlist；只有被玩家明確觀測過的物品，才會由物流服務嘗試入庫。普通單箱與雙箱可用，trapped chest、barrel、hopper、shulker box 及外部模組容器目前不在支援範圍。

目前 warehouse 的基本閉環如下：

```text
合成 warehouse_marker
→ 選取 warehouse territory
→ 直接蹲下右鍵空 ItemFrame 部署
→ /civitas building scan
→ 掃描箱子與物品摘要
→ 指派原版 Villager
→ 開啟 27 格 Civitas backpack
→ Villager 走向 marker
→ 將 allowlist 物品收入 warehouse
```

「蹲下右鍵空氣後依視線自動尋找 ItemFrame」的快速部署路徑已撤回，不是目前支援的功能。正式流程必須直接蹲下右鍵空的 ItemFrame。

### 多容量住宅 marker

住宅使用四種獨立 marker，分別代表 1、2、4 或 6 名居民的容量。住宅 validator 會掃描 territory 內的床位，並以 marker 等級限制可登記的居民數量。床位數與居民 roster 數量是兩個獨立條件：住宅必須有足夠床位，且已登記居民不可超過 marker 容量。

| Marker | 居民容量 | 主要判定 |
|---|---:|---|
| `residential_marker_1` | 1 | 至少 1 張有效床位，最多 1 名居民 |
| `residential_marker_2` | 2 | 至少 2 張有效床位，最多 2 名居民 |
| `residential_marker_4` | 4 | 至少 4 張有效床位，最多 4 名居民 |
| `residential_marker_6` | 6 | 至少 6 張有效床位，最多 6 名居民 |

住宅支援多名原版 Villager。當居民死亡時，server 端會將 ResidentRecord lifecycle 改為 `dead`；registry-derived 容量立即釋放，而 `BuildingObservation.residents` legacy roster 不再被新生命週期流程寫入。居民的 Civitas 歷史身份仍會保留。這個死亡事件已完成程式與自動化回歸測試，但仍需要玩家在遊戲內重新驗收。

## 居民資料層

居民資料目前由 `ResidentRecord` 與 `ResidentRegistry` 管理，原版 Villager 只是居民目前的 Minecraft body。`residentId` 是 Civitas 永久邏輯身份；`entityUuid` 是目前 body UUID。未來即使更換居民 body，也只需要更新 `entityUuid`，不必重新建立永久居民身份。

`ResidentRecord` 保存殖民地、住宅建築、工作建築、角色、body type、lifecycle、名稱、建立時間與最近觀測時間。建築關係使用 `dimension@x,y,z` marker key，不使用容易因重掃而改變的一次性 building index。`ResidentRegistry` 是 assignment 的唯一 canonical 寫入來源；舊版 `BuildingObservation.residents` 與 `resident_uuid` 只會在 SavedData 載入時作缺漏遷移與相容讀取，既有 registry record 優先，legacy roster 不會再反向覆蓋 assignment。

可使用以下命令查詢居民：

```text
/civitas resident list
/civilization resident list
```

目前不會因為村民暫時離開載入範圍就刪除居民身份，也不會自動復活死亡的原版 Villager。死亡居民保留歷史資料，但不再占用住宅容量、執行導航或 warehouse 物流。

## 玩家可用命令

主要命令根為 `/civitas`，`/civilization` 保留作為相容別名。命令樹與 Tab completion 共用相同邏輯；所有動態參數由 server 驗證。

| 命令 | 用途 |
|---|---|
| `/civitas help` | 顯示目前玩家可用命令與使用方式。 |
| `/civitas status` | 查詢文明與 SavedData 摘要。 |
| `/civitas building scan [radius]` | 掃描附近已載入的 Civitas ItemFrame 建築。 |
| `/civitas building list` | 列出已登記建築與驗證／殖民地狀態。 |
| `/civitas building inspect <index>` | 查看指定建築、居民與 storage snapshot。 |
| `/civitas assign <building_index> [villager]` | 將 Villager 指派至有效 colony-bound 建築；該維度尚無 Town Hall 時，使用過渡模式。 |
| `/civitas unassign <building_index> [villager]` | 解除建築與 Villager 的指派關係。 |
| `/civitas resident list` | 查詢 ResidentRecord 清單。 |
| `/civitas townhall` | 查詢已保存的 Town Hall cores。 |
| `/civitas townhall radius <index> <radius>` | 調整 Town Hall 範圍並重新計算建築 colony binding。 |

未來新增命令或參數時，必須維持 `/civitas`／`/civilization` 相容別名、Tab completion、server 驗證、本地化與品牌訊息格式。

## 目前開發狀態

| 系統 | 狀態 |
|---|---|
| Fabric 26.2 專案與 Java 25 toolchain | 已完成並可建置 |
| 世界級 SavedData、schema 與 Codec 相容 | 已完成第一版 |
| `/civitas` 與 `/civilization` 命令根 | 已完成 |
| 聚落資料與食物需求基礎 | 已完成第一版資料層；舊版模擬命令不再公開 |
| Warehouse marker、territory、ItemFrame 建築辨識 | 已完成，物流閉環已通過程式與先前遊戲驗收 |
| Warehouse storage snapshot 與安全 allowlist 物流 | 已完成第一版 |
| 27 格 Civitas Villager backpack | 已完成程式、build 與 client smoke 驗證，並已進行先前遊戲驗收 |
| 1／2／4／6 容量住宅 marker | 已完成程式、容量檢查與多居民 roster |
| Town Hall core、SavedData 與多核心非重疊範圍 | 已完成程式與初始遊戲驗收；多核心完整手動驗收仍待確認 |
| `colony_id` 建築歸屬切片 | 已完成程式與自動化測試；無 Town Hall 過渡模式已加入，完整範圍內外遊戲驗收仍待確認 |
| ResidentRecord／ResidentRegistry | 已完成第一版；已收斂為 assignment 唯一寫入來源並通過 SavedData／Codec 回歸測試 |
| 村民死亡後釋放住宅容量 | 已完成 server death callback 與回歸測試；等待本次遊戲內重新驗收 |
| 農田與工作站 | 尚未實作 |
| 食物實體物流與配送政策 | 尚未實作 |
| 自訂 NPC Entity | 尚未實作，原版 Villager 暫作 body |
| 完整 lifecycle、body rebind 與居民復活政策 | 尚未完成 |
| 外部模組村莊 adapter | 架構方向已保留，尚未接入新的外部模組 |

## 下一步路線

目前最近完成的是 ResidentRecord／ResidentRegistry、村民死亡後住宅容量釋放、玩家命令樹整理，以及無 Town Hall 的舊世界過渡運作。舊版 settlement／村莊 scan、聚落 simulate 與測試建築 generate 不再作為公開玩家命令；食物模型與測試工具仍保留在程式與自動化驗證層，避免污染目前殖民地主線。下一個優先遊戲內驗收是：在沒有 Town Hall 的維度確認有效 warehouse／住宅仍可 assign、導航與物流，接著建立 Town Hall，確認同一範圍內建築取得 `colony_id`，範圍外建築立即停止 colony 功能。

通過死亡容量的遊戲內驗收後，將繼續完善居民 lifecycle、移除與 body rebind 規則，再處理住宅居民的床位分配與工作建築關係。自訂 NPC Entity 暫不列入下一個切片，因為目前應先穩定 server SavedData、原版 Villager body、住宅容量與物流閉環。

長期路線包括農田與工作站、農田到 warehouse 的來源／目的地物流、食物消耗與配送、居民工作狀態、外部村莊 adapter、資料驅動建築與更完整的殖民地治理。每個功能會先拆成可編譯、可保存、可測試與可在遊戲內驗收的垂直切片。

## 開發環境

| 項目 | 版本 |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.158.0+26.2 |
| Fabric Loom | 1.17.19 實際執行版本 |
| Gradle | 9.5.1 wrapper |
| Java | 25；專案使用 Java toolchain，不要求修改系統全域 Java |
| Mod ID | `civilizationmod` |
| Maven group | `com.civilizationmod` |

## 建置與執行

Windows 開發環境需要安裝 JDK 25。從 `C:\Minecraft` 專案根目錄執行：

```bat
.\gradlew.bat --no-daemon test --console=plain
.\gradlew.bat --no-daemon runFoodModelRegressionTest --console=plain
.\gradlew.bat --no-daemon build --console=plain
.\gradlew.bat --no-daemon runClient --console=plain
```

資料生成請單獨執行，避免 Gradle 9.5 在 `runDatagen` 與 `build` 同一次 invocation 中產生 implicit dependency validation：

```bat
.\gradlew.bat --no-daemon runDatagen --console=plain
```

目前完整 build 會執行 Java 25 編譯、JUnit、FoodModel regression、check、jar 與 sourcesJar。`runClient` 是 smoke test 或遊戲內人工驗收入口，完成測試後應手動停止。

## 測試與驗收

純 Java／JUnit 測試目前涵蓋 FoodDemandModel、SettlementAdapter、BuildingObservation、住宅 roster、Town Hall 唯一性與範圍、colony binding、ResidentRegistry migration、雙 UUID、registry-only assignment／unassign、Codec round-trip，以及死亡居民釋放住宅容量。食物模型與舊聚落資料層仍供回歸測試使用，但其 `/simulate`、`/scan` 與 `/settlement` 公開命令已從命令樹移除。這些測試不等於完整 Minecraft gameplay；ItemFrame、實際村民死亡事件、SavedData 重開、命令 help 畫面與 GUI 仍需要在遊戲內驗收。[1] [2]

死亡容量的最小遊戲內驗收流程如下：

```text
/civitas building list
/civitas building inspect <住宅建築編號>
```

確認住宅有兩名居民後，手動殺死其中一名或執行：

```text
/kill @e[type=minecraft:villager,sort=nearest,limit=1]
```

等待約一秒，再執行：

```text
/civitas building inspect <住宅建築編號>
/civitas resident list
```

預期住宅居民數量下降一名，死亡居民仍出現在列表中且 lifecycle 為 `dead`。之後重新指派一名 Villager，確認釋放的容量可以再次使用。

## 專案規則與研究文件

`AGENTS.md` 保存給 AI 與貢獻者的專案規則；`Mods.md` 是唯一正式的開發進度檔。`.cursor/skills/` 與 `skills/` 保存可重複使用的開發工作流及已查證 API。專案中的 TekTopia、MineColonies 與房屋結構研究文件僅作設計參考，不會把外部模組列為核心硬依賴。

所有版本敏感的 Fabric 26.2 API 必須先由官方文件、26.2 mappings 或實際 jar `javap`／編譯查證。common/server 程式不得依賴 client-only 類別；所有文明真實狀態必須由 server 保存與驗證。所有玩家可見文字必須經過 `Component.translatable`、三份 locale 與 `CivilizationMessages` 品牌訊息包裝器；中文格式為 `|創世紀元| : {訊息}`，英文格式為 `|Civitas| : {message}`，只有品牌前綴使用金色。

## Git 上傳範圍

Repository 只提交可重現建置所需的原始碼、資源、資料生成輸出、Gradle 設定、專案規則、skills 與研究文件。Minecraft `run/` 世界資料、`build/` 產物、Gradle cache、IDE 設定、執行 log、API 反編譯暫存檔與本機診斷文字檔由 `.gitignore` 排除，不應提交到公開 repository。

## License

授權條款請參考 repository 根目錄的 `LICENSE` 檔案。

## References

[1]: [Fabric Saved Data](https://docs.fabricmc.net/develop/saved-data)

[2]: [Fabric Events 26.2](https://docs.fabricmc.net/develop/events)

[3]: [Fabric Creating a Project 26.2](https://docs.fabricmc.net/develop/getting-started/creating-a-project)
