# TekTopia 結構偵測與房屋驗證參考

查證日期：2026-08-20。此文件只記錄 TekTopia 1.12.2 反編譯原始碼與其 Wiki 的行為，不提供 Fabric 26.2 API 簽名。

## 原始碼來源

- [VillageManager.java](https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/VillageManager.java)
- [VillageStructure.java](https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java)
- [VillageStructureType.java](https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureType.java)
- [VillageStructureStorage.java](https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureStorage.java)
- [BasePathingNode.java](https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/pathing/BasePathingNode.java)
- [TekTopia Structures Wiki](https://sites.google.com/view/tektopia/home/structures)
- [TekTopia Getting Started Wiki](https://sites.google.com/view/tektopia/home/getting-started)

## 判定流程

1. `VillageManager` 以玩家位置建立掃描盒，掃描盒內找 ItemFrame；用 marker item 對應 `VillageStructureType`。建築建立時保存 ItemFrame 掛點與朝向。
2. `VillageStructure.setup()` 先 `findDoor()`，再 `doFloorScan()`，最後 `validate()`。`findDoor()` 只從展示框附近的少數候選位置找門：展示框朝向反向一格後，檢查左右兩側，以及向下兩格的候選；若命中門的上半部，向下修正到門下半部。
3. 地板掃描起點是門內側一格，即門位置沿 ItemFrame 朝向反方向一格。掃描使用四方向遞迴 flood fill，避免重複位置，最多收集 500 個 floor tiles。
4. 對每個 floor tile，`scanRoomHeight()` 從該方塊向上逐格找第一個不可通行方塊，最多 30 格；只有高度至少 2，且 floor 下方不是可通行方塊時，才加入 floor tiles 並繼續四方向擴展。這表示 TekTopia 的 ceiling 高度是動態、逐 floor tile 計算，不是固定的 `doorY + constant`。
5. `BasePathingNode.isPassable()` 以液體判定和原版 `Block.isPassable(world, pos)` 為核心，另外把木門與 fence gate 當作 portal/passable；不是以 `isFaceSturdy` 作為 ceiling 的唯一條件。`scanRoomHeight()` 實際是尋找第一個不可通行方塊，並要求下面保留至少兩格通行高度。
6. 基本 `validate()` 檢查門仍存在且為木門或 gate、floor tiles 不超過 500、ItemFrame entity 仍存在且位置未移動、marker item 沒被更換，以及 floor tiles 至少 4 格。定期 validate，建築有效時定期重做 floor scan。
7. `VillageStructureStorage` 在 floor scan 每個地板位置檢查 chest block entity，把有效 AABB/floor 範圍內的箱子加入 storage；marker 定義功能，箱子內容是後續 storage 資源觀測，不是 marker 幾何本身。

## Wiki 條件

TekTopia Wiki 描述的玩家可見條件是：至少 4 個 floor blocks；至少 2 格高且不超過 30 格；必須有屋頂；至少一扇門；室內 floor 位於同一個 y-level；最大 500 個 floor spaces。ItemFrame 可放在木門上方或相鄰上方，marker 放入後經村莊驗證才會啟用。

## 對 Civitas 的決策意義

Civitas 目前固定 `ROOM_HEIGHT=3`、採樣兩側矩形並以 `isFaceSturdy` 計算 ceiling，這和 TekTopia 的動態 flood fill 有實質差異。若要借鑑 TekTopia，優先方向是以已查證的門內側起點做有界四方向 flood fill，對每個 floor tile 動態向上搜尋 ceiling/阻擋方塊，並保存 floor count、room height min/max/average、ceiling non-air 與中止原因；不要只把 `ceiling=0` 解讀成固定高度錯誤，也不要直接移植舊版 Forge/MCP API。

任何實際 Minecraft 26.2 實作仍須重新查證 26.2 common jar 或 Fabric 官方文件的 passability 等價 API、方塊碰撞 API 與區塊載入行為。
