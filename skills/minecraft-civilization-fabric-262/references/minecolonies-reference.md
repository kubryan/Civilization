# MineColonies 參考架構：Civitas 映射規則

> 本文件只記錄 MineColonies 的功能與資料分層參考；MineColonies 是 Forge 模組，不能把其類別、事件或 API 當成 Minecraft 26.2 Fabric API。任何 Civitas 版本敏感 API 仍必須查 Fabric／Minecraft 26.2 官方文件、26.2 mappings 或實際編譯結果。

## 研究來源

研究日期：2026-08-20。

| 編號 | 來源 |
|---|---|
| [1] | [MineColonies 官方首頁](https://minecolonies.com/) |
| [2] | [MineColonies Wiki 首頁](https://minecolonies.com/wiki/) |
| [3] | [Getting Started](https://minecolonies.com/wiki/tutorials/getting-started/) |
| [4] | [MineColonies GitHub repository](https://github.com/ldtteam/minecolonies) |
| [5] | [Requests](https://minecolonies.com/wiki/systems/request/) |
| [6] | [Warehouse](https://minecolonies.com/wiki/buildings/warehouse/) |
| [7] | [Courier's Hut](https://minecolonies.com/wiki/buildings/deliveryman/) |
| [8] | [Town Hall](https://minecolonies.com/wiki/buildings/townhall/) |

## 核心設計觀察

MineColonies 把聚落視為長生命週期的 server-owned aggregate。聚落有固定中心、唯一 id、名稱、維度、邊界／保護區、狀態、權限、事件、統計、居民、建築、工作訂單、研究、請求與物流等子系統。官方 API 介面 `IColony` 將這些責任分到多個 manager，而不是把所有欄位堆在一個平面資料類別。

`IColonyManager` 負責跨聚落索引與生命週期：建立／刪除聚落、依世界／維度／id／座標查找、判斷新聚落與既有聚落的距離、列出當前世界或全部世界的聚落，以及整體保存／載入。這提示 Civitas 長期應將「聚落查找服務」與「聚落內部狀態」分開；`CivilizationWorldData` 可以先作持久化根，但命令不應永久直接依賴 list index。

MineColonies 的建築是實例，而不是單一功能字串。`AbstractBuilding` 保存位置、建築類型、建造狀態、等級、客製名稱、prestige、requester，以及可持久化 building modules。建築亦可建立 request、註冊 request resolver、保存模組資料，並向 colony 標記 dirty。Civitas 要維持 `BuildingFunction` 與 `BuildingObservation`／building instance state 分離；未來可加入 level、validation、capacity、health、custom name、statistics 與 provider state。

Town Hall 是聚落中心建築的特化例子：單一中心、固定保護／claim 邏輯、聚落事件與權限事件入口、居民與工作設定、工作訂單與統計查詢。Civitas 不必直接複製 Town Hall block；可把它抽象成日後的 `SettlementAnchor` 或 `SettlementGovernance`，並保持外部聚落沒有 Town Hall 時仍可透過 `SettlementAdapter` 運作。

## Warehouse 與 storage topology

MineColonies 的 Warehouse 是中央 storage／物流服務的建築實例，等級會影響可同時使用的 Courier 數量與容量；`IWareHouse` 另有「worker 是否可存取」、「容器是否屬於此倉庫」與「升級容器」等責任。官方 Warehouse 頁面也將 Warehouse 的 inventory、racks、minimum stock、workers 與 storage upgrade 分開描述。

因此 Civitas 的設計規則如下：`warehouse_marker` 只啟用 `WAREHOUSE` building function，不等同箱子、不代表箱子內容，也不應在 marker scanner 中直接把附近所有箱子視為倉庫庫存。後續建立獨立 `BuildingStorageProvider` 或 `WarehouseInventoryProvider`，以已驗證的建築範圍／容器位置輸出 resource observation；在沒有驗證範圍時使用 empty／unknown fallback，不猜測整座村莊的箱子都屬於倉庫。

## Request 系統

MineColonies Requests 由 Warehouse 與 Courier 協作。居民／工作者先檢查自身 inventory、hut block、rack；找不到資源才建立 request。Warehouse 有貨時，Courier 將物品配送到 requester；缺貨時，系統可依其他工人的配方／職能建立製作鏈。無法自動滿足的 request 需要玩家介入，並可透過 Clipboard、Postbox、Stash 進行全域查看、提出請求或反向入庫。

`IRequestManager` 的最小責任集合包含 requester、requestable resource、provider、resolver／worker、token／id、request state、priority、create、assign、reassign、update state、overrule、provider／requester lifecycle、tick、dirty 與 serialize／deserialize。Civitas 未來可採用以下較小的資料模型：

| MineColonies 概念 | Civitas 未來抽象 |
|---|---|
| requester | 建築、居民、玩家或政策服務的穩定識別 |
| requestable | food、wood、tool、building_material 等資源描述 |
| provider | Warehouse、Container、Farm 或外部模組 adapter |
| resolver／worker | Courier、居民、物流任務執行者 |
| request state | `pending`、`assigned`、`in_progress`、`delivered`、`failed`、`cancelled` |
| priority | 建築、居民需求或事件的請求優先級 |
| token | server 生成的穩定 request id |

第一版不要實作完整 request graph、crafting chain 或 courier AI；先保存 building observation 與聚落級食物摘要，讓未來 request 能引用 building／settlement identity。

## Worker、citizen 與低頻模擬

MineColonies 將居民實體、居民資料、工作、inventory、睡眠、經驗、colony handler、job handler 與 AI state 分開。官方入門文件也將 Builder、Farmer、Fisher、Courier 等 worker 與建築職位連接，而不是讓每個建築直接硬編碼一個 NPC。

Civitas 應維持現有聚落級食物模型，不在第一版每 tick 模擬每個 NPC。未來可建立 `WorkerRole`、`CitizenObservation`、`WorkAssignment` 與 `LogisticsTask`，再由低頻 tick 或玩家接近時提升細節。人口 provider 與 building provider 要保留 observation origin 與來源，避免把外部聚落內部 NPC API 當成核心依賴。

## 對 Civitas 現有切片的具體規則

| MineColonies 參考 | Civitas 實作決策 |
|---|---|
| Town Hall／colony center | 目前保留 `SettlementAdapter` 中心與維度；日後加入 governance／anchor，不要求外部模組提供 Town Hall。 |
| Building instance | 使用 `BuildingObservation` 保存 marker position、dimension、function、status、first／last seen 與 settlement binding。 |
| Warehouse | `warehouse_marker` 只映射到 `BuildingFunction.WAREHOUSE`；箱子內容交給未來 storage provider。 |
| Courier | 暫不加入 NPC AI；未來用 logistics worker／task 抽象。 |
| Request | 暫不加入完整 request manager；未來使用 requester／provider／resolver／state／priority 分層。 |
| Building level | 第一版不猜測建築等級；後續只有在結構驗證或資料來源明確時才保存。 |
| Permissions | MineColonies 權限不等同 Civitas 四種玩家身份；未來由 `IdentityService` 與 `SettlementPermission` server-side 驗證。 |
| Events／statistics | 目前保留食物事件摘要；未來將事件 log、原因、來源與統計拆成獨立服務。 |

## 不可直接移植的內容

不要直接移植 MineColonies 的 Forge capability、Forge event、NBT object graph、GUI、worker AI、pathfinding、request token factory 或其版本專用 class names。MineColonies 原始碼可用來推導責任與資料邊界，但任何 Fabric 26.2 實作都必須重新查證 API，並在 common／client／server 邊界內設計。

## 開發前檢查

當要新增 Civitas 建築、倉庫、資源或 request 功能時，先讀本 reference，再回答以下問題：資料是聚落級還是建築實例級？來源是 marker、block entity、外部 adapter 還是玩家操作？狀態是否由 logical server 保存？是否需要 first／last observation 與失效狀態？是否能在沒有 MineColonies 時正常啟動？若需要 26.2 API，必須另查 Fabric／Minecraft 官方來源或用專案編譯驗證。
