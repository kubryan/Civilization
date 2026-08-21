# CivilizationMod — Civitas

> **⚠️ 免責聲明：本專案絕大多數的程式碼與內容皆由 AI 輔助生成。雖已盡力確保功能與品質，但仍請知悉此專案的開發過程高度依賴 AI。歡迎任何貢獻、錯誤回報與反饋！**

**Civitas（創世紀元）** 是一個以 Minecraft Java Edition 26.2 與 Fabric 為基礎的殖民地建設與文明模擬模組。玩家不是只查看聚落數值，而是親手建造房屋與基礎設施，使用建築標誌宣告建築用途，指派原版村民，並觀察村民實際移動、工作與物資流動。

本模組的長期目標是建立一個可擴充的文明模擬框架，讓玩家逐步發展出包含市政廳、住宅、倉庫、農田、工作站、居民、資源物流、食物需求與聚落管理的殖民地。所有文明狀態由 server 保存與驗證，client 主要負責畫面、渲染與玩家輸入；外部村莊模組則以可選的 adapter 或資料驅動方式整合，不作為核心硬依賴。

## 模組重點

### 1. 玩家宣告建築用途

Civitas 不只依靠系統猜測玩家蓋的是什麼房子。玩家使用專用建築標誌，透過左鍵與右鍵選取兩個方塊建立 territory，再把帶有領地資料的 marker 放入物品展示框。ItemFrame 是玩家意圖的明確宣告，server 會檢查標誌、領地、維度、展示框位置、重複領地與建築功能。

目前 warehouse 與 residential marker 共用同一套領地選取與 ItemFrame 部署流程。標誌被移除時，Civitas 只清除建築登記，不會拆除方塊、移動床、搬走箱子或清空領地內物品。

### 2. Warehouse 倉庫與村民物流

Warehouse 是目前第一個已完成並通過實際驗收的殖民地建設垂直切片。玩家可以用八個儲物箱與一個鐵錠合成 `warehouse_marker`，圈選 warehouse territory，將標誌部署到領地內的 ItemFrame，然後使用以下命令查詢建築：

```text
/civitas building scan
/civitas building list
/civitas building inspect <building_index>
```

有效 warehouse 會掃描領地內的原版箱子與物品摘要。玩家可使用以下命令把原版村民指派為 warehouse resident：

```text
/civitas assign <building_index>
```

已指派村民會走向 warehouse marker，並開啟 Civitas 提供的 **27 格、9×3 村民背包**，而不是原版交易介面。村民背包內容可以保存；當背包中的物品符合 warehouse 已觀測的安全 allowlist 時，村民會把物品自動收入領地內的倉庫箱子。

完整 warehouse 閉環如下：

```text
合成 warehouse_marker
→ 選取 warehouse territory
→ ItemFrame 部署
→ building scan
→ 掃描箱子與物品摘要
→ 指派村民
→ 開啟 27 格 Civitas 背包
→ 村民走向 marker
→ 物品自動收入 warehouse
```

### 3. 多容量住宅標誌

住宅系統目前提供四種容量標誌：

| 標誌 | 床位容量門檻 | 配方概念 |
|---|---:|---|
| `residential_marker_1` | 至少 1 張床 | 1 張床與鐵錠 |
| `residential_marker_2` | 至少 2 張床 | 2 張床與鐵錠 |
| `residential_marker_4` | 至少 4 張床 | 4 張床與鐵錠 |
| `residential_marker_6` | 至少 6 張床 | 6 張床與鐵錠 |

住宅 marker 會掃描 territory 內的原版床，並把容量與實際床位數保存到建築觀測資料。第一版的 2、4、6 床是容量驗證與資料模型，現階段仍沿用單一 resident 綁定；多名村民入住、床位分配與居民搬遷會在後續切片處理。

### 4. 可保存的文明與建築狀態

Civitas 使用 server-side Saved Data 保存聚落、建築、領地、驗證狀態、村民指派、住宅容量、warehouse storage snapshot、食物模擬資料與版本化 Codec。重新掃描不應任意重置既有建築居民、物品資料或文明狀態；舊世界資料則使用 optional 欄位與 nested Codec 逐步保持相容。

### 5. 本地化與開發規範

玩家可見文字使用 `en_us`、`zh_tw` 與 `zh_cn` 三份 locale。`zh_cn` 依專案政策使用繁體中文；所有玩家訊息由 Civitas 品牌訊息包裝器處理，中文前綴為 `|創世紀元|`，英文前綴為 `|Civitas|`，只有品牌前綴使用金色。

## 目前開發狀態

| 系統 | 狀態 |
|---|---|
| Fabric 26.2 專案與 Java 25 toolchain | 已完成 |
| 世界級 Saved Data 與文明模擬基礎 | 已完成第一版 |
| `/civitas` 與 `/civilization` 相容命令根 | 已完成 |
| 聚落掃描與食物需求基礎 | 已完成第一版 |
| Warehouse marker 與 territory | 已完成並實際驗收 |
| Warehouse storage snapshot | 已完成並實際驗收 |
| 村民指派與 warehouse 導航 | 已完成並實際驗收 |
| 27 格 Civitas 村民背包 | 已完成並實際驗收 |
| 村民背包自動入庫物流 | 已完成並實際驗收 |
| 1／2／4／6 床住宅 marker | 已完成程式與 build 驗證，等待住宅遊戲內驗收 |
| 市政廳 Town Hall | 尚未實作，下一個核心建築切片 |
| 多居民住宅入住 | 尚未實作 |
| 農田、工作站與食物配送 | 尚未實作 |

## 下一步路線

住宅 marker 完成遊戲內驗收後，下一個核心功能是 **Town Hall 市政廳**。市政廳將負責建立殖民地核心、保存殖民地中心、管理建築歸屬，並成為未來住宅容量、居民上限、建築解鎖與殖民地管理規則的入口。

市政廳之後，再逐步加入多居民住宅入住、農田與工作站、食物需求與配送、request／provider 物流任務、外部模組村莊 adapter，以及更完整的殖民地治理與文明事件。每個功能都會先切成可編譯、可保存、可重新開世界驗證的垂直切片。

## 開發環境

| 項目 | 版本 |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.158.0+26.2 |
| Fabric Loom | 1.17.19 |
| Gradle | 9.5.1 wrapper |
| Java | 25；專案使用 Java toolchain，不要求修改系統全域 Java |

## 建置與執行

Windows 開發環境需要先安裝 JDK 25。從專案根目錄執行：

```bat
gradlew.bat build
gradlew.bat runDatagen
gradlew.bat runClient
```

更多 Fabric 26.2 專案設定可參考 [Fabric 官方文件](https://docs.fabricmc.net/develop/getting-started/creating-a-project)。

## 專案規則、研究與進度

`AGENTS.md` 保存給 AI 與貢獻者的專案規則；`Mods.md` 是唯一正式的開發進度檔。`.cursor/skills/` 與 `skills/` 保存可重複使用的開發工作流與已查證 API；`Claude分析-房屋結構與Civitas應用筆記.md`、`MineColonies 研究筆記.md`、`TekTopia 建築標識研究摘要.md` 等文件保存設計研究與功能參考。

版本敏感的 Fabric 26.2 API 必須先由官方文件、26.2 mappings 或實際編譯查證。文明狀態由 server 保存與驗證，client 只負責畫面、渲染與輸入；外部模組不會成為 Civitas 的核心硬依賴。

## Git 上傳範圍

Repository 只提交可重現建置所需的原始碼、資源、資料生成輸出、Gradle 設定、專案規則、skills 與研究文件。Minecraft `run/` 世界資料、`build/` 產物、Gradle cache、IDE 設定、執行 log、API 反編譯暫存檔與本機診斷文字檔由 `.gitignore` 排除，不應提交到公開 repository。

## License

授權條款請參考 repository 根目錄的 `LICENSE` 檔案。
