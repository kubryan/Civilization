# 已查證 API 與切片規則

任務碰到食物、warehouse、村民指派、背包、物流、Mixin 或 datagen 時再讀本檔；不要一次載入全部。
**新的 javap／官方文件結論必須追加在本檔（或正確的 sibling reference），不要只寫在對話或 `Mods.md`。** 已有相同結論時只補差異。

寫回前先掃本檔標題，避免重複調查：食物模型、人口 provider、warehouse 選點／tooltip／duplicate、指派導航、VillagerRenderer Mixin、storage snapshot、物流 allowlist、marker cleanup、27 格 backpack、datagen recipe、ContainerScreen 標題座標。

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

直接蹲下右鍵空 ItemFrame 的部署使用已查證的 Fabric 26.2 `UseEntityCallback`。server 必須檢查主手、蹲下、非 spectator、ItemFrame 為空、Marker territory 完整、維度一致、frame 在 territory 內、frame 有支撐牆，才將 `handStack.copyWithCount(1)` 寫入 frame、`handStack.shrink(1)` 消耗手上 Marker、套用 glint，並回傳成功。轉移必須使用同一份帶 CustomData 的 stack copy，不得重建只含 item identity 的新 Marker；玩家拆下 ItemFrame 後應取得相同 territory 資料。

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
