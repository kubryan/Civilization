# Claude 分析：TekTopia 房屋結構與 Civitas 應用筆記

查證日期：2026-08-20。目標版本：Minecraft Java Edition 26.2、Fabric、Java 25。

## 一、筆記目的與資料範圍

本筆記整理使用者提供的 Claude 分析，並與 TekTopia 反編譯原始碼、目前 Civitas 的 `BuildingGeometryValidator` 與最新 `latest.log` 對照。Claude 分享頁面本身沒有提供可供文字擷取的正文，因此本筆記以同一份分析的文字附件 `pasted_content_2.txt`、已保存的 TekTopia 查證 reference，以及本次提供的 `latest.log` 為依據。Claude 原文中的房屋演算法描述，凡涉及 TekTopia 行為者，優先以反編譯原始碼交叉核對；凡涉及 Fabric 26.2 API 者，不直接採用，必須另行查證。

## 二、Claude 分析的核心結論

Claude 認為 TekTopia 的房屋判定不是固定尺寸的矩形取樣，而是從門內側地板開始的連通式 flood fill。`doFloorScan()` 呼叫 `scanFloor()`；`scanFloor()` 以目前地板位置為中心，檢查房間高度與地板支撐，通過後向四個水平方向遞迴擴展。這使它能處理不規則形狀，而不是只依賴門的 facing/opposite 兩側各一個固定 3×1×3 取樣框。[1] [2]

Claude 也指出，TekTopia 的房間高度是逐格計算。`scanRoomHeight()` 從每一個 floor tile 向上找第一個不可通行方塊，最多搜尋 30 格；只要至少保留兩格可通行高度，該位置才可能成為有效 floor tile。這表示屋頂不必位於固定的 `doorY + ROOM_HEIGHT` 位置。[2] [3]

在門的解析方面，Claude 正確指出 TekTopia 會依 ItemFrame 的 hanging position 與 facing direction 檢查少數固定候選位置，而不是任意搜尋最近的門；找到上半部時，再修正到門的 lower half。[2] 這與 Civitas 目前在 marker 附近搜尋最近 `DoorBlock` 的策略不同。

Claude 另外指出，TekTopia 在每次 floor scan 時可由 `scanSpecialBlock()` 收集建築專用方塊，Storage 結構則在已掃描出的 floor 範圍內收集箱子。這說明「marker 定義建築功能」與「建築內部方塊提供功能資源」可以分層處理，而不應把 warehouse marker 直接等同箱子內容。[4]

## 三、與最新 latest.log 的對照

本次最新 log 的關鍵幾何訊息如下：

| 診斷項目 | facing side | opposite side |
|---|---:|---:|
| Scan 完整度 | `27/27` | `27/27` |
| Air | `27` | `27` |
| Floor support | `9` | `9` |
| Ceiling support | `0` | `0` |
| Ceiling non-air | `0` | `0` |
| 取樣高度 | `y=74` 至 `y=77` | `y=74` 至 `y=77` |

這組數字首先證明：目前取樣迴圈完整執行，並不是舊版 `air=0 / floor=0 / ceiling=0` 那種「尚未掃描到任何方塊」的情況。兩側都有空氣與地板支撐，但固定 ceiling 取樣位置全部是空氣。因此，Claude 所說「目前問題已確定是取樣範圍沒有對齊房屋」不能直接套用到這份最新 log；它仍可能是房屋形狀與固定取樣框不匹配，但目前最直接的證據是固定 ceiling 高度沒有碰到屋頂。

最保守的解釋是：門與地板位於 `y=74`，Civitas 目前把 `y=77` 當成 ceiling 位置，但木屋屋頂可能位於更高的 `y=78` 或以上。另一種可能是屋頂不在這個固定 3×3 取樣範圍內。因為 `ceilingNonAir=0`，目前不能把問題歸因於方塊存在但未通過 `isFaceSturdy`；固定 ceiling 位置本身就是空氣。[5]

Log 中的 Mojang authentication 401、Realms authentication error 與 profile key pair 401 屬於開發環境帳號服務連線問題，與 `civilizationmod` 的房屋幾何驗證沒有直接關係；模組已完成初始化、整合伺服器已啟動，且書本除錯訊息已正常顯示。

## 四、Claude 建議的應用分級

| 建議 | 分級 | 判斷 | 目前可採用方式 |
|---|---|---|---|
| 從門內側一格開始掃描 | **可採用，但要先核對方向** | TekTopia 確實使用 door-inside 起點；但 TekTopia 依 ItemFrame facing 找門，Civitas 目前還要確認 ItemFrame 朝向與 `DoorBlock.FACING` 的實際關係。 | 先作為下一版 validator 的設計入口，不直接把舊版方向 API 貼進 26.2。 |
| 使用水平 flood fill／queue／visited set | **可直接採用為架構方向** | 這是 TekTopia 與目前固定矩形取樣的主要差異，可處理 L 形與不規則房屋。 | 保持 bounded scan，設置 floor tile、水平範圍、垂直高度與總訪問量上限。 |
| 每個 floor tile 動態向上找房高 | **可採用為演算法方向** | 能直接解釋目前 `ceilingNonAir=0`：不再把屋頂綁定在 `doorY+3`。 | 先查證 Fabric 26.2 的 passability／collision 等價 API，再整合到 common/server validator。 |
| 將天花板判定從 sturdy 改為 passability | **不可直接套用，需 API 查證** | TekTopia 使用舊版 `Block.isPassable`；這不是 Fabric 26.2 可直接沿用的證明。 | 保留現有 `isFaceSturdy`／`isSolid` 作為目前保守 fallback；查證 26.2 後才決定補充或替代。 |
| 把門解析改成 ItemFrame 相對位置候選 | **可作為第二階段改進** | TekTopia 不是最近門搜尋；marker 與門的固定相對位置能降低選錯門風險。 | 保留目前 nearest-door fallback，但新增 `marker_anchor` 與 `nearest_fallback` 的 transient resolution diagnostics。 |
| 以 8–10 格作為 flood fill 半徑 | **不可直接採用數值** | Claude 提供的是保守建議，不是由 Civitas 實際建築資料或 26.2 行為推導出的數值。 | 先定義可測試的設定常數，透過測試木屋、L 形屋與大型倉庫調整。 |
| 直接移植 TekTopia 的 `isPassable`、`rotateY`、NBT 或 Forge/MCP 寫法 | **不可採用** | TekTopia 是舊版 1.12.2 反編譯碼，只能作演算法參考，不能作 Fabric 26.2 API 證明。 | 使用已查證的 26.2 common API，缺少等價 API 時再查官方文件、mappings 或實際編譯。 |
| 在 floor scan 中收集專用方塊與箱子 | **可作未來功能分層** | 符合 Civitas marker 與 storage provider 分離的設計。 | 先完成 geometry validator，再由 warehouse provider 在有效 floor 範圍內觀測容器，不修改 marker 語意。 |

## 五、可以直接應用到目前專案的項目

第一，**可以直接採用 bounded flood fill 的資料結構與責任邊界**。新的掃描器可以保留現有 `BuildingGeometryValidator.validate(level, marker)` 入口，內部改由門內側起點執行 bounded scan，最後仍回傳目前的 `ValidationResult`。這樣可以改善房屋辨識，但不需要立刻修改 `BuildingObservation` 或 `CivilizationWorldData` 的 Saved Data schema。

第二，**可以直接保留 transient diagnostics 並擴充掃描統計**。目前 `SideDiagnostics` 已能顯示 samples、air、floor、ceiling 與 `ceilingNonAirBlocks`；flood fill 版本可再增加訪問格數、有效 floor 數、最小／最大／平均房高、停止原因與是否碰到 scan limit。這些資料適合書本除錯或未來 debug command，不應直接寫入持久化文明狀態。

第三，**可以直接採用明確上限與失敗 fallback**。建議至少限制水平範圍、最高房高、floor tile 數、總訪問方塊數與未載入區塊行為。超出上限時回傳 controlled invalid reason，例如 `scan_limit` 或 `unloaded`，而不是無限遞迴或強制載入 chunk。這與 TekTopia 的 500 floor tiles／30 格房高限制相同方向，但數值仍應由 Civitas 自己決定。[2]

第四，**可以直接維持 server-side 與持久化邊界**。幾何掃描由 logical server 執行，`BuildingObservation` 只保存粗粒度的 validation status/reason；marker 仍只定義 `WAREHOUSE` 功能，箱子內容留給後續 storage provider。這與目前 `BuildingObservation`、`CivilizationWorldData` 與 `BuildingMarkerScanner` 的設計相容。

## 六、不能直接應用的項目

不能直接把 TekTopia 的舊版 `Block.isPassable`、`EnumFacing.rotateY`、Forge event、MCP mappings 或 NBT capability 寫法移植到 Fabric 26.2。它們只能證明「此演算法在另一個版本被實作過」，不能證明目前專案存在相同簽名或相同語意。

也不能因為 Claude 曾根據早期 `air=0 / floor=0 / ceiling=0` log 推斷「取樣框完全落在屋外」，就把這個判斷套用到最新 log。最新資料是兩側 `27/27` 完整取樣、`air=27`、`floor=9`、`ceiling=0`、`ceilingNonAir=0`；它更直接支持「固定 ceiling 高度是空氣」的判斷。

最後，不能直接把「8–10 格半徑」、「房屋必須符合某個固定高度」或「所有方塊都必須 sturdy」當成最終設計。這些數值與條件需要透過 Civitas 的測試房屋、不同門方向、L 形房屋、無屋頂房屋、超高屋頂與未載入邊界驗證。

## 七、建議的實作順序

| 階段 | 工作 | 完成判準 |
|---|---|---|
| A | 保留現有固定矩形 diagnostics | 已完成；`ceilingNonAir=0` 已能區分固定取樣高度是空氣。 |
| B | 查證 26.2 的 passability／collision 等價 API | 有官方文件、26.2 mappings 或實際 `javap`／compile 證據。 |
| C | 建立 bounded door-inside flood fill | 使用 queue 或受控 visited set，不強制載入 chunk。 |
| D | 動態計算每個 floor tile 的房高 | 不再固定 `doorY+3`；保留 min/max/average height diagnostics。 |
| E | 以既有 `ValidationResult` 接回 scan | 不先改 Saved Data schema，既有 building list／inspect 不受破壞。 |
| F | 加入純 Java 或可測試的幾何決策測試 | 覆蓋規則房屋、L 形房屋、無屋頂、門方向、超出上限與未載入。 |
| G | 遊戲內回歸測試 | 至少測試原版木屋、門在角落、不同方向、屋頂高於 3 格與箱子範圍。 |

## 八、最終建議

Claude 分析最值得採用的不是某一個固定 API 或數值，而是**從固定幾何猜測轉向「門錨定、連通掃描、逐格房高、明確上限」的驗證模型**。這個模型可以直接成為 Civitas 下一個建築幾何切片的架構方向，但必須保留目前的 `ceilingNonAirBlocks` 診斷，並先查證 26.2 對應的方塊通行性或碰撞判定。

目前不建議只把 `ROOM_HEIGHT` 從 3 改成 4 或 5，因為那仍然是固定高度猜測，無法處理不規則房屋，也無法判斷 floor tile 是否真的連通。比較穩健的下一步是先建立 bounded flood fill 的設計與測試，再替換固定兩側矩形掃描。

## References

[1]: https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/VillageManager.java "TekTopia VillageManager.java"

[2]: https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructure.java "TekTopia VillageStructure.java"

[3]: https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/pathing/BasePathingNode.java "TekTopia BasePathingNode.java"

[4]: https://github.com/TheShermanTanker/TekTopia-Deobfuscated/blob/master/net/tangotek/tektopia/structures/VillageStructureStorage.java "TekTopia VillageStructureStorage.java"

[5]: https://sites.google.com/view/tektopia/home/structures "TekTopia Structures Wiki"

[6]: https://sites.google.com/view/tektopia/home/getting-started "TekTopia Getting Started Wiki"

[7]: https://claude.ai/share/a5942760-109d-4bdc-8c26-687f45fe105e "Claude 分享頁面；本次動態頁面未提供可擷取正文"
