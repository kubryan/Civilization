# TekTopia 建築標識研究摘要

查證來源：
- https://sites.google.com/view/tektopia/home/getting-started
- https://sites.google.com/view/tektopia/home/structures

## 可借鑒概念

TekTopia 的重要建築需要 structure marker。有效結構通常在木門上方或相鄰位置放置物品展示框，再把建築標識放入展示框；結構通過驗證後才會啟用。建築要求包含平整室內地板、屋頂與門等條件。

Structures 頁列出的基本限制包括：至少 4 格地板、至少 2 格高且不超過 30 格、需要屋頂、至少一扇門、室內地板在同一 Y 層，最大 500 格地板空間。特殊建築在符合基本條件後才會註冊為有效，但缺少專用方塊可能無法正常工作。

## Civitas 不直接複製的設計方向

Civitas 可採用「展示框中的 marker item 定義建築功能」概念，但 marker 應由 server 驗證，建築功能資料應由資料驅動 registry/schema 定義。第一版可先做倉庫 marker；之後擴充住宅、農場、糧倉、工坊與聚落中心。

需要額外決定：marker 是否使用自訂物品、普通物品加 NBT/data component，展示框與門的距離／方向規則，建築邊界驗證，owner/settlement 綁定，以及展示框被破壞或 marker 被取出時的停用與資料保存行為。

## 引用頁面內容的工程結論

TekTopia 使用「可見、可移動、玩家可理解」的建築標識來啟用功能；Civitas 應保留這種可視化優點，但不要把展示框本體當成文明真實狀態的唯一儲存位置。展示框是 activation/observation source，已驗證的建築功能與資源摘要仍由 logical server Saved Data 管理。

## Fabric 26.2 API 查證摘要

查證來源：
- https://docs.fabricmc.net/develop/items/custom-item-interactions
- https://docs.fabricmc.net/develop/items/first-item

Custom Item Interactions 文件確認自訂 Item 可覆寫 `useOn`（玩家對方塊右鍵）與 `use` 等 vanilla item methods；server-side 世界互動應避免在 client 執行，成功結果使用 `InteractionResult`。這適合作為建築標識物的放置或啟用入口，但展示框本身的偵測仍需使用已查證的 entity／interaction API，不應猜事件名稱。

Creating Your First Item 26.2 文件確認自訂物品應使用 `ResourceKey<Item>`、`Registries.ITEM` 與 `Registry.register(BuiltInRegistries.ITEM, itemKey, item)`，並由 common initializer 觸發類別初始化；物品名稱、模型、texture 與 client item JSON 分開提供。第一版建築標識可做成 server 可辨識的自訂 marker item，並以 locale／資產檔提供名稱與模型。

## 初步 Civitas 建築標識 schema

第一版建議資料欄位：`building_function`（例如 `warehouse`）、`marker_item`（例如 `civilizationmod:warehouse_marker`）、`activation_rule`（door-adjacent item frame）、`settlement_binding`、`status` 與 `last_seen`。展示框是可視化 activation source；真正已啟用建築與食物／資源摘要仍由 logical server Saved Data 保存。

## Minecraft 26.2 ItemFrame API 核對

以專案已解析的 `minecraft-common-043a8b3edf-26.2.jar` 實際 javap 核對：

- `net.minecraft.world.entity.decoration.ItemFrame` 存在。
- `ItemFrame.getItem()` 可取得展示框內 `ItemStack`。
- `ItemFrame.getRotation()` 可取得展示框旋轉值。
- `ItemFrame.getDirection()` 由 `HangingEntity` 提供，可取得附著方向。
- `ItemFrame` 可用 `Level.getEntities(EntityTypeTest, AABB, Predicate)` 作 server-side 空間查詢。
- 自訂 marker item 可依 Fabric 26.2 官方 item registry 文件使用 `ResourceKey<Item>`、`Registries.ITEM` 與 `Registry.register`。

因此第一版建築辨識可先以 scan／命令主動查詢已載入 ItemFrame，讀取 `getItem()` 比對 marker item，避免一開始猜測或新增 item-frame 專用事件。後續若要即時啟用／停用，再查證互動 callback 或使用已查證的 Item `useOn`／server refresh 流程。

## TekTopia 反編譯 repo 目錄索引

來源： https://github.com/TheShermanTanker/TekTopia-Deobfuscated/tree/master/net/tangotek/tektopia

根目錄顯示 `items`、`storage`、`structures`、`blockfinder`、`Village.java`、`VillageManager.java`、`TekTopiaGlobalData.java` 等模組。`structures` 目錄包含 `VillageStructure.java`、`VillageStructureType.java`、`VillageStructureStorage.java`、`VillageStructureHome.java`、`VillageStructureKitchen.java`、`VillageStructureFarm` 相關與 Town Hall 等專用結構類別。

研究重點應優先閱讀 `VillageStructure.java`、`VillageStructureType.java`、`VillageStructureStorage.java`、`ModItems.java`、`items` 目錄、`Village.java`、`VillageManager.java` 與 `storage` 目錄，用來追蹤 marker 物品、結構辨識、建築功能註冊與村莊狀態保存。

## TekTopia VillageStructure / VillageStructureType 觀察

來源：
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureType.java

`VillageStructureType` 是一個建築類型註冊表；每個 enum entry 綁定一個 structure marker `ItemStack`、容量／限制數值，以及建立對應 `VillageStructure` 子類別的 factory。這種「marker item → building type → 專用功能類別」映射是 Civitas 最值得借鑒的部分。

`VillageStructure` 建構時接收 `EntityItemFrame`，保存門的位置、展示框位置、展示框面向與 marker entity id。`setup()` 先找門，再從門內側進行 floor scan；floor scan 以連通地板區域建立結構 AABB、記錄地板格與高度，並以基本結構條件進行驗證。伺服器端會透過 tick jobs 定期重新 validate 與 scan，結構狀態不是只在 marker 放入瞬間判定一次。

Civitas 不應直接移植 1.12 的 `EntityItemFrame`、`AxisAlignedBB` 或舊版事件，而應保留抽象：marker item 是宣告、展示框是 activation source、門與室內連通空間是 validation anchor、建築功能型別由 registry/schema 映射、server 定期重新驗證。第一版可先只做 marker 辨識與門口關聯，再逐步加入 floor/roof/door validation。

## TekTopia ModItems / Village 觀察

來源：
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/ModItems.java
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/Village.java

`ModItems` 將多個 structure marker 做成專用 `ItemStructureToken`，每個 marker 具備唯一名稱與成本，`VillageStructureType` 再把 marker 映射到具體結構類別。這支持 Civitas 使用自訂 `warehouse_marker` 作為明確功能宣告，而不是使用任意普通物品猜測功能。

`Village` 以 `Map<VillageStructureType, List<VillageStructure>>` 管理各型建築，另有 storage chest/scanner、村莊 origin/center、AABB、居民與 server jobs。它把建築集合、資源掃描與週期更新集中在 village domain object，而不是把邏輯分散到命令中。

Civitas 應借鑒 domain separation：`SettlementAdapter` 保存聚落摘要，另建立 `BuildingFunction`／`BuildingObservation` 和 server-side registry；展示框辨識、建築驗證與資源 provider 分開。不要直接把所有建筑實體與容器塞進目前的 settlement record，避免 Saved Data 變成舊版 TekTopia 那種大型 runtime object。

## TekTopia VillageStructureStorage / storage 觀察

來源：
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureStorage.java
- https://github.com/TheShermanTanker/TekTopia-Deobfuscated/tree/master/net/tangotek/tektopia/storage

`VillageStructureStorage` 是 `VillageStructure` 的專用子類，初始化時清空專用方塊集合；在 floor scan 遇到指定方塊時把 `TileEntityChest` 加入 storage block map。這表示 TekTopia 的倉庫不是單純 marker，而是 marker 啟用後再依結構掃描收集可用箱子。

storage 目錄提供 `InventoryScanner`、`ItemDesire`、`ItemDesireSet` 與 `VillagerInventory` 等分層。可借鑒的核心是「先辨識建築功能，再由功能提供資源掃描／需求規則」，而不是把所有箱子掃描邏輯直接放入食物模擬核心。

Civitas 第一版應先實作 marker detection 與建築功能觀測；下一個倉庫切片再使用已查證的 26.2 `Container`／`BlockEntity` API，從通過驗證的 warehouse 建築邊界掃描箱子，將食物 provider 接到該建築功能，而不是掃描整個村莊。

## TekTopia VillageManager 觀察

來源： https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/VillageManager.java

`VillageManager` 使用 WorldSavedData 保存村莊集合，透過週期性 player scan boxes 找出範圍內的 `EntityItemFrame`。每個展示框的 displayed item 交給 `VillageStructureFactory.getByItem(...)` 映射建築類型；找到 marker 後先找最近／所屬村莊，再用 `getStructureFromFrame` 去重，最後透過 factory 建立具體 `VillageStructure` 並加入 village。

TekTopia 還會把 structure marker 綁定到 village，並以 item tag 標記結構是否通過驗證；有效結構與無效結構的 tag 狀態會更新，讓玩家能從展示框視覺判斷 marker 是否已被接受。VillageManager 也只在 server 生命週期與掃描範圍處理這些資料。

Civitas 可借鑒的完整流程是：`ItemFrame` 探測 → marker registry 映射 → settlement binding → building identity 去重 → 基本結構驗證 → server Saved Data／runtime observation 更新 → 後續週期 refresh。第一版不需要複製 item tag NBT；可先用 `BuildingObservation.status` 保存 `detected/valid/invalid`，等自訂 marker item 與資料 component schema 定案後再加入視覺 feedback。

## TekTopia 核心原始碼精讀結論

精讀 raw source 後確認：

1. `VillageManager.procesScanBoxes()` 以玩家位置建立 scan boxes，查詢範圍內的 ItemFrame；用 `VillageStructureFactory.getByItem(itemFrame.getDisplayedItem())` 將展示框物品映射成結構類型；以 frame position 查找既有結構，避免同一展示框重複建立；找到有效結構後更新 marker tag。
2. `VillageStructure` 以 ItemFrame 的 hanging position 與 facing direction 保存門口 anchor；`findDoor()` 在展示框附近依固定方向尋找門；`doFloorScan()` 從門內側做連通地板掃描，建立 floor tiles 與 AABB；`validate()` 定期檢查門、floor 上限、展示框存在、位置未移動、展示框物品未變更與最低地板數。
3. `VillageStructureType` 以 enum registry 綁定 marker ItemStack、容量參數與具體結構 factory；`isItemEqual` 用來判定展示框 marker 是否仍符合原型。
4. `VillageStructureStorage` 在 floor scan 遇到箱子時建立新 storage map，scan 結束時增刪村莊 storage chest 集合；建築被破壞時移除其箱子。倉庫 marker 本身只宣告功能，箱子必須位於驗證後的建築 floor tiles 內。

Civitas 的轉化方案：

- 使用自訂 marker item registry 取代 TekTopia 舊版 ItemStack prototype。
- 使用 `BuildingMarkerRegistry`／資料驅動 schema 取代 enum 中所有 hardcoded 建築類別。
- 使用 `BuildingObservation` 保存 marker entity id/position、dimension、function、validation status、AABB／door anchor 與 lastSeen。
- 使用 server-side periodic refresh 或命令 refresh marker；第一版先做主動 scan，避免未查證 26.2 item-frame callback。
- 第一個 warehouse vertical slice 應先驗證 marker＋門＋室內 floor，再只掃描該 AABB 內的箱子，接到 `SettlementFoodProvider`，不掃整個聚落或世界。
