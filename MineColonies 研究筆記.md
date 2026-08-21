# MineColonies 研究筆記

## 研究範圍

研究目標是比較 MineColonies 的聚落、建築、工人、請求、倉庫、權限與事件設計，找出能抽象進 Civitas 的概念；不引入 MineColonies 為核心硬依賴，也不把其 Forge／版本 API 當成 Fabric 26.2 API 證明。

## 已查閱來源

1. MineColonies 官方首頁：https://minecolonies.com/
2. MineColonies Wiki 首頁：https://minecolonies.com/wiki/
3. MineColonies Getting Started：https://minecolonies.com/wiki/tutorials/getting-started/
4. MineColonies 官方 GitHub：https://github.com/ldtteam/minecolonies
5. MineColonies Requests：https://minecolonies.com/wiki/systems/request/
6. MineColonies Warehouse：https://minecolonies.com/wiki/buildings/warehouse/
7. MineColonies Courier's Hut：https://minecolonies.com/wiki/buildings/deliveryman/
8. MineColonies Town Hall：https://minecolonies.com/wiki/buildings/townhall/

## 官方產品與遊戲設計觀察

MineColonies 將玩家定位為殖民地的設計者、管理者與雇主；玩家決定工人類型、數量、居所與工作場所。官方首頁明確將 NPC worker、building、building tool、multiplayer permissions 視為核心特色，而不是單一的村民替換系統。

入門流程提供兩種建立聚落路徑：Supply Camp／Supply Ship 建立新聚落，或在世界生成的 abandoned colony 上重新啟用。Town Hall 建立聚落的中心與保護區；第一次放置的位置固定為聚落保護中心。Town Hall 放置後建立 colony、設定保護範圍，並提供建築升級、事件、工作訂單、權限、居民、統計與設定等管理入口。

建築不是只代表一個功能名稱，而是「建築實例」：建築有位置、類型、等級、工作者槽位、庫存／模組、工作訂單與可能的統計。Warehouse 的等級同時影響可用 Courier 數量與儲存容量；Courier's Hut 的等級影響單次攜帶堆疊數與可追蹤請求數。這顯示 Civitas 未來應把 building function 與 building instance state 分開。

## 倉庫與請求系統觀察

Requests 系統以 Warehouse 與 Courier 為兩個核心組件。居民或工作者先查看自己的 inventory、hut block、rack；找不到需要的物品後才建立 request。若 Warehouse 有貨，Courier 將物品從中央倉庫配送到工作場所；若沒有，系統可嘗試讓具備配方／職能的其他工人建立製作鏈，直到需求完成或無法滿足。

請求至少具有 requester、需求內容、來源／提供者、目標位置、狀態與配送任務等概念。MineColonies 也把手動無法自動滿足的 request 顯示給玩家，並以 Clipboard、Postbox、Stash 等工具提供全域查詢、玩家請求與反向入庫。

Courier 的配送不是單純瞬間搬運，而是有工作者容量、可追蹤請求數、路徑／工作任務與回 Warehouse 的循環。建築端可設定 pickup priority；高 priority 建築先被取貨，而 Warehouse 到其他建築的配送被視為高優先。這是很適合 Civitas 未來建立低頻、可觀測的物流模擬層的參考。

## 可映射至 Civitas 的初步結論

| MineColonies 概念 | Civitas 對應方向 | 第一版是否實作 |
|---|---|---|
| Town Hall／聚落中心 | SettlementAdapter 的中心、維度、來源與後續 claim／治理欄位 | 否，先保留現有 settlement scan |
| 建築實例 | BuildingObservation：marker 位置、功能、狀態、聚落綁定、首次／最近觀測 | 是，warehouse marker 垂直切片 |
| Warehouse | warehouse building function；後續可成為聚落物流中心 | 目前只辨識功能，不讀箱子庫存 |
| Courier | 未來的物流 worker／任務執行者抽象 | 否，先不加入 NPC AI |
| Request | 未來 ResourceRequest：requester、resource、quantity、destination、priority、state | 否，先完成 building observation |
| Building level | BuildingObservation 的 level／capacity／health 等未來欄位 | 否，第一版不推斷建築等級 |
| Town Hall permissions | 未來 server-side Identity／SettlementPermission，與玩家四身份分開 | 否，身份系統尚未開始 |
| Colony events／statistics | 未來事件與聚落級統計摘要 | 否，現在只保存食物事件摘要 |

## 重要邊界

MineColonies 官方文件與 GitHub 原始碼是功能與資料模型參考，不是 Civitas 的 Fabric 26.2 API 來源。Civitas 仍採 server-owned Saved Data、外部模組可選、SettlementAdapter 軟整合、命令主動 scan、common/client 邊界與 locale／品牌訊息規則。

## 待深入查閱

需要再讀 MineColonies API 與 core 實作中的 `IColony`、`Colony`、`ColonyManager`、`AbstractBuilding`、`BuildingTownHall`、`IWareHouse`、`IRequestManager`、`StandardRequestManager`、`AbstractEntityCitizen`、worker／building module 與事件／統計介面，確認哪些責任應由 Civitas 的 SettlementAdapter、BuildingObservation、未來 ResourceRequest、WorkerIdentity 與 EventLog 分別承擔。

## API 分層研究：IColony

目前主分支的 `IColony` 把聚落視為一個長生命週期的 server-owned aggregate。它至少暴露固定中心 `getCenter()`、唯一 id、名稱、維度、保護範圍判斷、與其他聚落的距離、聚落狀態、是否 active，以及 `markDirty()`、`write(CompoundTag)`、`read(CompoundTag)` 等保存／同步責任。

同一個聚落 aggregate 下再分出多個 manager：building manager、citizen manager、event manager、statistics manager、request manager、work manager、research manager、permissions、raider、visitor、reproduction、connections、package manager 等。這種分層證明 Civitas 不應把建築、食物、身份、事件與請求全部塞進 `SettlementAdapter`；現有 `SettlementAdapter` 應維持聚落來源與聚落級摘要，其他子系統應透過獨立資料模型或 manager 掛接。

`IColony` 也有依座標取得 building requester 的入口、依座標檢查是否在聚落範圍內的入口，以及向玩家廣播重要訊息的入口。對 Civitas 而言，這可轉化為未來 `SettlementService`：以維度／中心／邊界／來源查找聚落，再由 `BuildingObservation` 或建築服務查找建築，而不是讓命令直接操作 list index。

## API 分層研究：IColonyManager

`IColonyManager` 是跨聚落索引與生命週期管理器。它提供建立／刪除聚落、依世界／維度／id／座標尋找聚落、判斷與其他聚落距離是否足夠、列出當前世界或全部世界的聚落、依座標尋找 building，以及保存／載入整體 manager NBT。

重要設計是「聚落位置查找」與「聚落內部資料」分開。MineColonies 不是每次命令都重新搜尋所有方塊，而是先透過 manager 找到聚落，再由聚落 aggregate 管理 buildings、citizens、events、requests。Civitas 目前的 `CivilizationWorldData` 同時保存 settlement list 與 building list，仍可作為 MVP；長期可抽出 `SettlementIndex` 或服務層，保留 Saved Data 作為持久化根。

## API 分層研究：IWareHouse

`IWareHouse` 不是單純的「箱子位置」。它繼承建築介面，額外負責 worker 是否能存取倉庫、升級容器、取得倉庫 tile entity，以及判斷某個容器位置是否屬於倉庫。也就是說，Warehouse building function 與實際 storage topology 是兩件事。

這直接支持 Civitas 的現有設計決策：`warehouse_marker` 只定義建築功能；箱子／容器再由後續的 storage scanner 或 provider 判斷是否屬於倉庫，不能把 marker 本身誤當成倉庫內容。未來可設計 `BuildingStorageProvider`，輸入 `BuildingObservation`，輸出可查詢的 container positions 與聚合 food／resource stock。

## API 分層研究：IRequestManager

`IRequestManager` 把請求系統拆成 requester、requestable、resolver、provider、token、state 與 manager。核心操作是 create request、assign、reassign、取得 request／resolver、更新 state、overrule、provider／requester 加入移除、colony update 觸發重新分配，以及序列化、tick、dirty 管理。

對 Civitas 最有價值的不是直接採用其複雜 token 系統，而是採用其責任分離：

| Request 元件 | Civitas 未來抽象 |
|---|---|
| requester | 建築、居民、玩家或政策服務提出需求的識別來源 |
| requestable | 食物、木材、工具、建材等可被請求的資源描述 |
| provider | 倉庫、箱子、農場或外部模組 adapter 提供資源 |
| resolver／worker | 未來 courier、居民或物流任務執行者 |
| request state | pending、assigned、in_progress、delivered、failed、cancelled |
| priority | 建築或事件的需求優先級 |
| token／id | 穩定的 request UUID 或 server 生成 long id |

第一版不應加入完整 request manager；但建築觀測資料的保存格式要避免阻礙未來加入 requester／provider 關聯。`BuildingObservation` 保存 marker identity、function、status、settlement binding、first／last seen 是合理的最小邊界。
