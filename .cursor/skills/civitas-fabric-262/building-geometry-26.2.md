# Civitas Building Geometry 26.2 Reference

查證日期：2026-08-20。目標：Minecraft Java Edition 26.2、Fabric、Java 25。

## 使用時機

當任務涉及 `warehouse_marker`、ItemFrame、DoorBlock、建築門口 anchor、房間／floor scan、AABB、容器範圍或 `BuildingObservation.validationStatus` 時讀取本 reference。不要把 MineColonies、TekTopia、舊版 Minecraft 或 NeoForge 的方法簽名直接套用到 Fabric 26.2。

## 官方文件查證

- [Fabric Block States 26.2](https://docs.fabricmc.net/develop/blocks/blockstates)：`BlockState` 保存單一方塊的 properties，官方示例使用 `BlockState.getValue(...)`。
- [Fabric Block Entities 26.2](https://docs.fabricmc.net/develop/blocks/block-entities)：inventory 等額外資料屬於 `BlockEntity`，官方示例使用 `level.getBlockEntity(pos)`。
- [Fabric Block Containers 26.2](https://docs.fabricmc.net/develop/blocks/block-containers)：`Container` 與 `BlockEntity` 的容器責任；不要在 marker scan 階段把鄰近箱子直接視為倉庫內容。
- [Fabric Debugging Mods 26.2](https://docs.fabricmc.net/develop/debugging)：官方示例使用 `level.getBlockState(targetPos)`。
- [Fabric Block Tinting 26.2](https://docs.fabricmc.net/develop/blocks/block-tinting)：官方示例使用 `level.getBlockState(pos.below())`。
- [Fabric Porting to 26.2](https://docs.fabricmc.net/develop/porting/)：版本敏感 API 必須對照目標 mappings、官方文件、範例與實際編譯。

## 實際 26.2 common jar 查證

查證 jar：`C:\Users\User\.gradle\caches\fabric-loom\26.2\minecraft-common.jar`；查證工具：`C:\Program Files\Java\jdk-25.0.2\bin\javap.exe`。

已確認下列公開類別與方法：

| 類別 | 已確認 API | 用途 |
|---|---|---|
| `net.minecraft.world.level.block.DoorBlock` | `FACING`、`HALF`、`HINGE`、`OPEN`、`POWERED`、`type()`、`isOpen(BlockState)`、`isWoodenDoor(BlockState)` | 找到門並讀取方向／上下半部。 |
| `net.minecraft.world.level.block.state.properties.DoubleBlockHalf` | `UPPER`、`LOWER`、`getOtherHalf()`、`getDirectionToOther()` | 將門位置正規化為 lower half。 |
| `net.minecraft.world.level.block.state.StateHolder` | `getValue(Property<T>)` | 讀取 `DoorBlock.FACING` 與 `DoorBlock.HALF`。 |
| `net.minecraft.core.Direction` | `getOpposite()`、`getClockWise()`、`getCounterClockWise()`、`getStepX/Y/Z()` | 計算門兩側與室內掃描方向。 |
| `net.minecraft.core.BlockPos` | `(int,int,int)`、`offset(int,int,int)`、`relative(Direction[,int])`、`above/below/north/south/east/west`、`betweenClosed(...)` | 建立有界方塊掃描範圍。 |
| `net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase` | `getBlock()`、`isSolid()`、`isAir()`、`liquid()`、`getCollisionShape(BlockGetter, BlockPos)`、`isSolidRender()`、`isFaceSturdy(BlockGetter, BlockPos, Direction)` | 判斷室內通行性、地板支撐與天花板支撐。 |
| `net.minecraft.world.phys.shapes.VoxelShape` | `isEmpty()` | 判斷方塊 collision shape 是否為空，作為 26.2 bounded room scan 的 passability 基礎。 |
| `net.minecraft.world.level.Level` | `getBlockState(BlockPos)`、`getBlockEntity(BlockPos)`、`isLoaded(BlockPos)` | server-side 方塊觀測與避免未載入範圍。 |
| `net.minecraft.world.entity.decoration.ItemFrame`／`HangingEntity` | constructors `(Level, BlockPos, Direction)`、`blockPosition()`、`getDirection()`、`getItem()`、`setItem(ItemStack, boolean)` | 取得展示框位置、附著面方向與 marker item；支撐牆位置是 `frame.blockPosition().relative(frame.getDirection().getOpposite())`。 |
| `net.minecraft.world.item.ItemStack`／`net.minecraft.core.component.DataComponents` | `copy()`、`set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean)`、`remove(...)`、`isSameItemSameComponents(...)` | server 驗證後只切換 Marker 附魔光效，不替換 Marker 物品。 |

## bounded flood fill BuildingGeometryValidator 規則

`BuildingGeometryValidator` 是 common/server-side 的 bounded、TekTopia-inspired 幾何驗證器。它由 `/civitas building scan` 主動呼叫，不註冊未查證的 ItemFrame、方塊或門互動事件，也不強制載入 chunk。正式 scan 保留 `ItemFrame` 實體，不能只把它壓成 `BlockPos`；debug 也使用同一個 frame-aware validator。

1. 在 marker 周圍水平 4、垂直 3 的已載入範圍內搜尋候選 `DoorBlock`，並將 upper half 正規化為 lower half。
2. 以門 lower position 為中心，在水平 2、垂直 2 的已載入 ItemFrame 範圍內收集合法 Marker；候選 Marker 必須是已註冊物品、ItemFrame 必須具有水平 `getDirection()` 且附著支撐方塊為非空氣／非液體。支撐牆位置依 26.2 bytecode 定義為 `frame.blockPosition().relative(frame.getDirection().getOpposite())`。目前建築只能有一個合法 Marker；Marker 不在門旁回傳 `marker_not_at_door`，合法 Marker 超過一個回傳 `marker_ambiguous`。
3. 依門的 `FACING`，分別以門兩側相鄰的一格作為 flood-fill room anchor；每側使用 queue 與 visited set，只向四個水平方向擴展。
4. 對每個 room anchor，從 `doorY - 1` 附近向上／下最多 `FLOOR_SEARCH_VERTICAL_RADIUS=8` 搜尋 floor column；支撐方塊不可通行，正上方 room block 必須可通行或是箱子。第一個有效 floor Y 會成為該側 floor Y，後續 floor candidate 必須與它相同，符合第一版「室內地板同一 Y 高度」規則。
5. 每側至少需要 `MIN_FLOOR_SUPPORT_BLOCKS=4` 格 floor；水平擴展限制為起點周圍 `MAX_HORIZONTAL_RADIUS=8`，floor tile 上限為 `MAX_FLOOR_TILES=256`。所有候選、地板與高度 sample 都必須 `level.isLoaded(...)`；超出上限回傳受控 `scan_limit`。
6. floor／room／ceiling 的 passability 判定使用 `!state.liquid()` 且 `state.getCollisionShape(level, position).isEmpty()`；這些 `BlockStateBase` 與 `VoxelShape.isEmpty()` 方法已由目標 common jar 查證。牆體則接受任何非空氣、非液體方塊，因此玻璃、階梯、半磚、柵欄門與柵欄都可作為邊界。
7. 從每個 floor column 的 room block 向上逐格搜尋第一個非 passable 方塊，最多 `MAX_ROOM_SCAN_HEIGHT=30`；至少 `MIN_ROOM_CLEARANCE=2` 格可通行高度，且每個 floor column 都必須找到屋頂，才能通過 ceiling 條件。屋頂依非空氣碰撞方塊判定，不依賴固定世界 Y；階梯、半磚與玻璃可接受。
8. floor flood fill 完成後，對 floor 區域外圍的非 door wall 方向逐格檢查牆體；門以外的三面邊界必須由非空氣、非液體方塊連續覆蓋。入口則檢查門外與第一個 room position 的玩家站立空間是否可通行。
9. 一般結構至少需要室內 air 4、floor 4、ceiling 4、完整牆體與可進入性；warehouse function 額外要求有效室內 floor 範圍內至少一個 `Blocks.CHEST`，否則回傳 `no_container`。
10. `SideDiagnostics` 額外保存合法 Marker 數、floor Y range、room height、ceiling non-air、wall blocks／segments、wall completion、entry accessibility、container count 與 bounds。這些 diagnostics 是 transient，不寫入 Saved Data；`BuildingObservation` 只保存 validation status／reason。
11. 不通過時保留 `BuildingObservation` 歷史記錄，只更新 validation status／reason，不刪除觀測。

## Civitas 資料邊界

`BuildingObservation.status` 表示 settlement binding（`bound`／`unbound`）；`validationStatus` 表示幾何驗證（`detected`／`valid`／`invalid`）。兩者不可混成同一欄位。`validationReason` 是受控識別字串，玩家輸出必須映射到 locale key 並經 `CivilizationMessages` 包裝。

`warehouse_marker` 定義建築功能；第一版要求至少一個箱子位於有效室內 floor 範圍內，才能讓 warehouse 通過結構／功能驗證。箱子數量與內容仍應由後續 `BuildingStorageProvider` 觀測；不要因為 marker 附近存在箱子，就直接把箱子加入聚落食物存量。正式 building scan 通過時，server 在來源 ItemFrame 的 Marker stack 設定 `ENCHANTMENT_GLINT_OVERRIDE=true`；失效時移除該 component。這只改視覺光效，不改 Marker item、不消耗物品、不改 Saved Data schema。

## 驗證與風險

本次以目標 common jar 的 `javap`、ItemFrame bytecode 與專案實際 Gradle build 驗證 `ItemFrame.blockPosition()`、`getDirection()`、`getItem()`、`setItem(...)`、附著支撐位置公式、`ItemStack` component API、`DataComponents.ENCHANTMENT_GLINT_OVERRIDE`、`DoorBlock`、`Level.getEntities`、`BlockState.getCollisionShape`、`VoxelShape.isEmpty`、`BlockState.isAir`、`BlockState.liquid` 與 `Blocks.CHEST` 用法。仍需在遊戲內測試：單一 Marker、門附近多 Marker、Marker 上方／左右位置、ItemFrame 背牆、4 格同高 floor、屋頂洞、玻璃／階梯／半磚／柵欄牆、無箱 warehouse、valid glint、invalid 移除 glint 與不同方向的門。

## 來源

- [1] https://docs.fabricmc.net/develop/blocks/blockstates — Fabric Block States 26.2
- [2] https://docs.fabricmc.net/develop/blocks/block-entities — Fabric Block Entities 26.2
- [3] https://docs.fabricmc.net/develop/blocks/block-containers — Fabric Block Containers 26.2
- [4] https://docs.fabricmc.net/develop/debugging — Fabric Debugging Mods 26.2
- [5] https://docs.fabricmc.net/develop/blocks/block-tinting — Fabric Block Tinting 26.2
- [6] https://docs.fabricmc.net/develop/porting/ — Fabric Porting to 26.2

## 書本右鍵 ItemFrame 除錯互動

查證日期：2026-08-20。使用 Fabric API `UseEntityCallback.EVENT` 監聽實體右鍵，callback signature 為 `interact(Player, Level, InteractionHand, Entity, EntityHitResult)`。官方 Javadoc 說明 callback 發生在 spectator check 之前，因此 handler 必須自行檢查 `player.isSpectator()`；logical client 應回傳 `PASS`，讓原版流程與 server packet 繼續；logical server 回傳非 `PASS` 才會取消後續實體互動。[7] [8]

Civitas 的 debug handler 只接受 logical server、主手、普通 `Items.BOOK`、非 spectator 玩家與已註冊 BuildingMarkerRegistry 的 `ItemFrame`。它以實際 frame entity 與 function id 呼叫 `BuildingGeometryValidator.diagnose(...)`，透過 `Player.sendSystemMessage(CivilizationMessages.translatable(...))` 顯示合法 Marker 數、marker attached 狀態、validation reason、air、floor、ceiling、floor Y、room height、牆體、入口與箱子計數；不更新 `BuildingObservation`、不呼叫 `setDirty()`、不切換 glint、不消耗書本。成功處理後回傳 `InteractionResult.SUCCESS`，避免原版 ItemFrame 改變 marker rotation；其他實體、非 marker、非書本或非主手一律回傳 `PASS`。

不要為這個診斷功能建立 client-only HUD、networking payload 或 Mixin。Fabric 官方 Custom Item Interactions 文件確認物品互動應先使用既有 `InteractionResult` 與 vanilla／Fabric hook；Fabric Events 文件確認 callback 以 `.EVENT.register(...)` 註冊。[8] [9]

來源：

- [7] https://maven.fabricmc.net/docs/fabric-api-0.154.2+26.2/net/fabricmc/fabric/api/event/player/UseEntityCallback.html — Fabric API 0.154.2+26.2 UseEntityCallback Javadoc。
- [8] https://docs.fabricmc.net/develop/events — Fabric Events 26.2。
- [9] https://docs.fabricmc.net/develop/items/custom-item-interactions — Fabric Custom Item Interactions 26.2。

## Invalid 診斷計數保留規則

`ValidationResult` 的 invalid 分支不得把取樣計數一律重設為零。`evaluate(...)` 的檢查順序是：door、scanComplete、interiorAir、floorSupport、ceiling；除 `no_door` 或沒有上下文之外，所有失敗都必須把目前的 air／floor／ceiling 計數傳回。否則遊戲內看到 `reason=no_ceiling, air=0, floor=0, ceiling=0` 時，無法區分「取樣完全沒有執行」與「取樣已執行但天花板不足」。

本次 log-driven 修正確認，原本 zero metrics 的直接根因是 `invalid(String reason)` 將所有失敗結果的三個數值欄位重設為零，而不是先證明門方向或 `isFaceSturdy` 判定錯誤。回歸測試必須覆蓋 `no_room`、`no_floor`、`no_ceiling`，並確認每個失敗 reason 都保留輸入計數。

## 詳細 geometry diagnostics

詳細診斷應與持久化 `BuildingObservation` 分離。`GeometryDiagnostic` 暫存 marker position、最近門 lower position、`DoorBlock.FACING`、selected side、合法 Marker 數與兩側 `SideDiagnostics`；`SideDiagnostics` 保存 floor／ceiling／wall／entry／container 計數、floor Y、room height、牆體狀態與 bounds。這些資料只在書本除錯互動中顯示，不改 schema、不寫入 Saved Data、不呼叫 `setDirty()`。

建議保留 `validate(level, marker)` 作為既有 scan 的相容入口，內部委派 `diagnose(level, marker).result()`；需要 debug 細節時才呼叫 `diagnose(...)`。如此可避免為了增加診斷資訊而改動 building scan 的持久化流程。

診斷訊息應同時顯示 selected side 與 facing／opposite side，因為單一 selected 統計無法判斷另一側是否其實才是室內。sample bounds 使用與迴圈相同的 door position、side、depth、lateral width 與 room height 計算，避免「顯示的範圍」和「實際掃描的範圍」不一致。這是定位 `ceiling=0`、marker 與門高度錯位、屋頂高於固定 room height 或側面選擇錯誤的最小診斷切片。


## Warehouse 領地宣告優先規則（2026-08-20）

Civitas warehouse 從嚴格的「Marker 附近自動尋找門並驗證完整房屋幾何」改為玩家宣告優先。玩家手持 `warehouse_marker` 左鍵方塊設定 point A，再在仍手持同一 Marker 時右鍵方塊設定 point B；兩點正規化為包含式 `min_x/min_y/min_z` 到 `max_x/max_y/max_z` warehouse territory，資料寫入 Marker 的 `DataComponents.CUSTOM_DATA`。若玩家切換快捷欄，`Item.inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot)` 會對未選中的 stack 傳入 `equipmentSlot == null`；只有尚未完成的 point A 會被清除，已完成的 territory 保留。

Fabric 26.2 官方 Javadoc（https://maven.fabricmc.net/docs/fabric-api-0.158.0+26.2/net/fabricmc/fabric/api/event/player/AttackBlockCallback.html）確認左鍵 callback 的 logical client 結果：`SUCCESS` 取消原版流程、揮手並送 packet；`FAIL` 取消且不送 packet。warehouse_marker 左鍵 handler 因此在 client 回傳 `SUCCESS`，在 server 設定 point A 後回傳 `FAIL`，避免玩家挖掉選取方塊。右鍵第二點使用已查證的自訂 Item `useOn(UseOnContext)`，server 完成 territory 後回傳 `SUCCESS`。

第一版 warehouse scan 不再要求領地內有門、固定 Y 的 floor、屋頂、三面牆、入口或箱子。只要 scanner 找到一個 registered `warehouse_marker` ItemFrame，ItemStack 帶有完整 territory、維度與 frame 座標符合，且 ItemFrame 依 `frame.blockPosition().relative(frame.getDirection().getOpposite())` 有合法支撐牆，即判定 warehouse `valid` 並套用 Marker glint。缺少 territory、跨維度、Marker 在領地外或 ItemFrame 未附著才回傳受控 invalid reason。舊 door/floor/roof/wall flood fill 保留作相容與純 geometry diagnostic seam，不再是 warehouse activation gate。

Marker territory 目前安全上限為每軸 64 格、總體積 262144 格；這是 server-side 資料完整性與掃描成本保護，不代表玩家建築必須符合固定房屋尺寸。warehouse marker 的功能仍由 item registry 定義，territory 只定義玩家宣告的空間範圍，兩者不可混為箱子內容或聚落食物存量。


## Marker tooltip 與快速部署（2026-08-20）

Warehouse Marker CustomData 同時保存玩家原始點擊的 A/B 與正規化 min/max bounds；tooltip 顯示原始點，server validator 使用正規化 bounds。26.2 `Item.appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)` 只讀 component，不應在 tooltip 中改變狀態。

玩家蹲下右鍵領地內的空 ItemFrame 時，可由 Fabric 26.2 `UseEntityCallback` server-side 驗證並使用 `handStack.copyWithCount(1)` 將同一份帶 CustomData 的 Marker 寫入 frame，再以 `handStack.shrink(1)` 消耗手上物品。不能重建純 item identity 的 Marker，否則拆下 ItemFrame 後會失去 warehouse territory。非空 ItemFrame、領地外、跨維度、未完成 territory 或未附著牆面時不得消耗 Marker。相同維度與 min/max bounds 的第二個有效 Marker 必須保存為 `duplicate_territory`，不能建立第二個 warehouse。
