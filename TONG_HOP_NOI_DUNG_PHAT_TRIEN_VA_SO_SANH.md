# TỔNG HỢP NỘI DUNG PHÁT TRIỂN & BẢNG SO SÁNH NÂNG CẤP HỆ THỐNG
## ĐỀ TÀI: TỐI ƯU HÓA MÃ NGUỒN MỞ (CODE) TRONG PHÁT TRIỂN PHẦN MỀM THÔNG QUA KỸ THUẬT TỔNG QUÁT HÓA (GENERICS) ĐỐI TƯỢNG

---

> [!NOTE]
> Tài liệu này tổng hợp toàn bộ quá trình tiến hóa của hệ thống từ phiên bản sơ khởi (bản báo cáo ban đầu) đến phiên bản nâng cấp hoàn chỉnh. Tài liệu đóng vai trò là **khung đối chiếu và cơ sở dữ liệu học thuật** để xây dựng lại cuốn Báo cáo Tổng kết NCKH / Thuyết minh Đồ án Tốt nghiệp chất lượng cao.

---

## PHẦN I. BẢNG SO SÁNH TOÀN DIỆN (TRƯỚC VÀ SAU KHI PHÁT TRIỂN)

| Tiêu chí So sánh | Trạng thái Ban đầu (Phiên bản PDF cũ) | Trạng thái Sau khi Nâng cấp (Phiên bản Hiện tại) | Ý nghĩa Kỹ thuật & Học thuật |
|---|---|---|---|
| **1. Cơ chế Đổi Kịch bản (Scenario Switching)** | **Tĩnh (Static)**: Phải sửa cấu hình trong file `application.properties` rồi dừng và chạy lại server Spring Boot. | **Động tại Runtime (Dynamic)**: Chuyển đổi tức thời qua REST API `POST /api/detection/switch-scenario` và Menu Dropdown trực tiếp trên Web UI. | Nâng cao tính linh hoạt, không làm gián đoạn hệ thống khi người dùng muốn đổi góc nhìn phân tích dữ liệu. |
| **2. Truy vấn & Hiển thị Đồ thị (Graph Querying)** | **Toàn phần (Full Graph)**: Chỉ có hàm `fetchGraph()`, tải toàn bộ nút và cạnh về bộ nhớ trình duyệt, dễ treo khi dữ liệu lớn. | **Linh hoạt (Full + Sub-graph k-hop)**: Bổ sung thuật toán Cypher `fetchSubGraph()` truy vấn theo bán kính lân cận nút chọn; nút "Lân cận" trên UI. | Giải quyết triệt để bài toán nghẽn bộ nhớ (Memory Bottleneck) và quá tải UI đối với đồ thị mạng lưới quy mô lớn. |
| **3. Mức độ Thuật toán Đồ thị (Graph Analytics)** | **Heuristic đơn giản**: Chỉ đếm số bậc liên kết (`degree`) và đếm số nút cùng trỏ vào một đích (`shared target`). | **Toán học Đồ thị Nâng cao**: Tích hợp **PageRank Centrality**, **Label Propagation (LPA)**, **Dijkstra Shortest Path**, **Jaccard Similarity**. | Chuyển đổi từ mô hình đếm thủ công sang mô hình thuật toán toán học cấu trúc mạng lưới chuẩn quốc tế. |
| **4. Trí tuệ Nhân tạo & Học máy (Graph AI)** | **Hoàn toàn chưa có**: Chưa có bất kỳ mô hình AI hay Machine Learning nào. | **Tích hợp Graph Machine Learning**: Thuật toán nhúng vector không gian **Node2Vec ($\mathbb{R}^8$)** và **Mô hình Phát hiện Bất thường (Anomaly Detection)**. | Đưa hệ thống bắt kịp xu hướng AI định hướng đồ thị (Graph AI), nâng tầm đề tài lên mức nghiên cứu học thuật chuyên sâu. |
| **5. Số lượng Kịch bản Phân tích (Scenarios)** | **2 Kịch bản**: `ASSOCIATION_GRAPH` và `EMAIL_URL_IP`. | **4 Kịch bản Hoàn chỉnh**: Bổ sung thêm `GRAPH_ANALYTICS` (Toán học đồ thị) và `GRAPH_AI_ANOMALY` (AI/Machine Learning). | Đa dạng hóa các tầng tiếp cận bài toán: từ quy tắc nghiệp vụ, cấu trúc liên kết, giải thuật toán học đến trí tuệ nhân tạo. |
| **6. Giao diện Người dùng (Data-Driven UI)** | Hiển thị đồ thị cơ bản, chưa có tính năng điều khiển Scenario trực tiếp, chưa hiển thị phân tích AI. | Bổ sung **Scenario Selector Dropdown**, nút tương tác **k-hop Subgraph**, tích hợp bảng **Graph Analytics & AI Patterns** tại `insights.html`. | Giao diện Data-Driven thực thụ: tự thích ứng kiểu dữ liệu, nhãn hiển thị và cập nhật kết quả phân tích theo thời gian thực. |
| **7. Kiểm định & Phản hồi API (API & Testing)** | Response cơ bản, ít test case cho tầng phân tích thuật toán. | Chuẩn hóa `ApiResponse.ok(data, message)` và phủ đầy đủ **Unit Test Suite** cho toàn bộ Controller, Service, Analytics & AI. | Đảm bảo tính ổn định, độ tin cậy và đạt chuẩn kỹ thuật phần mềm doanh nghiệp (Production-ready). |

---

## PHẦN II. CHI TIẾT CÁC NỘI DUNG ĐÃ PHÁT TRIỂN MỚI

Hệ thống được mở rộng và phát triển theo 4 trụ cột chính:

```
                                  ┌───────────────────────────────────────────────────────────┐
                                  │   HỆ THỐNG ĐỒ THỊ TỔNG QUÁT HÓA MỞ RỘNG (ENHANCED CORE)   │
                                  └─────────────────────────────┬─────────────────────────────┘
                                                                │
         ┌──────────────────────────────┬───────────────────────┴───────────────────────┬─────────────────────────────┐
         ▼                              ▼                                               ▼                             ▼
┌──────────────────┐          ┌──────────────────┐                            ┌───────────────────┐         ┌───────────────────┐
│ 1. RUNTIME &     │          │ 2. SUBGRAPH      │                            │ 3. GRAPH          │         │ 4. GRAPH AI &     │
│    SCENARIO REG  │          │    K-HOP ENGINE  │                            │    ANALYTICS MATH │         │    MACHINE LEARN  │
├──────────────────┤          ├──────────────────┤                            ├───────────────────┤         ├───────────────────┤
│ • In-Memory      │          │ • Cypher k-hop   │                            │ • PageRank        │         │ • Node2Vec Random │
│   Switching      │          │   Neighborhood   │                            │   Centrality      │         │   Walk Embedding  │
│ • POST /switch-  │          │ • GET /subgraph  │                            │ • LPA Community   │         │ • 8D Vector Space │
│   scenario       │          │ • UI Isolation   │                            │ • Dijkstra &      │         │ • Centroid Model  │
│ • Dropdown UI    │          │   Button         │                            │   Jaccard         │         │   Anomaly Score   │
└──────────────────┘          └──────────────────┘                            └───────────────────┘         └───────────────────┘
```

---

### 1. Cơ chế Chuyển đổi Kịch bản Động tại Runtime (Dynamic Scenario Switching)
* **Vấn đề giải quyết**: Trong phiên bản cũ, `DetectionScenarioRegistry` chỉ đọc cấu hình từ `application.properties` lúc khởi động. Muốn chuyển đổi bài toán phân tích thì bắt buộc phải dừng và chạy lại toàn bộ ứng dụng Spring Boot.
* **Cải tiến triển khai**:
  - Tối ưu hóa [`DetectionScenarioRegistry.java`](file:///d:/Code_Hoc/NCKH_GENERIS/Generics/complete/src/main/java/com/example/servingwebcontent/Service/detection/DetectionScenarioRegistry.java) với biến trạng thái `volatile String currentActiveKey` và hàm đồng bộ `setActiveScenario(String key)`.
  - Mở endpoint REST API: `POST /api/detection/switch-scenario?scenarioKey={key}` trong [`DetectionApiController.java`](file:///d:/Code_Hoc/NCKH_GENERIS/Generics/complete/src/main/java/com/example/servingwebcontent/Controller/DetectionApiController.java).
  - Tích hợp Dropdown Menu trực tiếp trên thanh điều hướng Header của giao diện [`index.html`](file:///d:/Code_Hoc/NCKH_GENERIS/Generics/complete/src/main/resources/static/index.html).

---

### 2. Tối ưu Truy vấn Đồ thị Con theo Bán kính k-hop (k-hop Sub-graph Query)
* **Vấn đề giải quyết**: Khi lượng dữ liệu đồ thị trong cơ sở dữ liệu Neo4j tăng lên hàng nghìn hoặc hàng triệu nút, việc gọi `GET /api/graph` để lấy toàn bộ dữ liệu sẽ gây tràn bộ nhớ (Out-Of-Memory) và làm đơ giao diện người dùng Web.
* **Cải tiến triển khai**:
  - Viết câu truy vấn Cypher tối ưu trong [`GraphQueryService.java`](file:///d:/Code_Hoc/NCKH_GENERIS/Generics/complete/src/main/java/com/example/servingwebcontent/Service/GraphQueryService.java):
    ```cypher
    MATCH (center:Node {id: $centerId})
    MATCH p = (center)-[:RELATION*0..2]-(n:Node)
    WITH DISTINCT n
    RETURN n.id AS id, n.type AS type, properties(n) AS props
    ```
  - Mở endpoint REST API: `GET /api/graph/subgraph?nodeId={id}&depth={depth}` trong [`GraphApiController.java`](file:///d:/Code_Hoc/NCKH_GENERIS/Generics/complete/src/main/java/com/example/servingwebcontent/Controller/GraphApiController.java).
  - Bổ sung nút **"Lân cận (k-hop)"** trên thanh điều khiển chi tiết nút của giao diện Web để người dùng có thể khoanh vùng quan sát mạng lưới liên quan.

---

### 3. Tích hợp Bộ Thuật toán Đồ thị Toán học Nâng cao (Advanced Graph Analytics)
* **Vấn đề giải quyết**: Khắc phục hạn chế chỉ dùng các luật đếm thô sơ bằng cách đưa vào các thuật toán toán học cấu trúc mạng lưới.
* **Cải tiến triển khai trong [`GraphAnalyticsService.java`](file:///d:/Code_Hoc/NCKH_GENERIS/Generics/complete/src/main/java/com/example/servingwebcontent/Service/detection/GraphAnalyticsService.java)**:
  1. **PageRank Centrality**:
     $$PR(u) = \frac{1-d}{N} + d \sum_{v \in In(u)} \frac{PR(v)}{Out(v)}$$
     - Tính toán độ quan trọng của các nút qua mô hình chuỗi Markov ($d = 0.85$, 20 vòng lặp).
     - Chuẩn hóa điểm về thang $[0..100]$, phát hiện mẫu `PAGERANK_HIGH_CENTRALITY_HUB`.
  2. **Label Propagation Algorithm (LPA) - Phát hiện Cộng đồng**:
     $$\text{Label}(u) = \arg\max_{l} \sum_{v \in N(u)} \mathbb{I}(\text{Label}(v) = l)$$
     - Tự động truyền nhãn để gom nhóm các nút liên kết chặt chẽ thành các cụm cộng đồng ngầm (`DENSE_COMMUNITY_CLUSTER`).
  3. **Cross-Community Bridge Detection**: Phát hiện các nút giữ vai trò cầu nối thông tin giữa các cộng đồng khác nhau (`CROSS_COMMUNITY_BRIDGE`).
  4. **Dijkstra Shortest Path**: Tìm đường đi ngắn nhất giữa 2 nút bất kỳ (`findShortestPathDijkstra`).
  5. **Jaccard Similarity Coefficient**: Đo độ tương đồng hành vi liên kết ($J(u,v) = \frac{|N(u) \cap N(v)|}{|N(u) \cup N(v)|}$).

---

### 4. Tích hợp Trí tuệ Nhân tạo & Học máy trên Đồ thị (Graph Machine Learning & Graph AI)
* **Vấn đề giải quyết**: Tạo bước đột phá khoa học, chuyển từ phân tích tĩnh sang phân tích thông minh dựa trên việc nhúng không gian vector đặc trưng (Feature Vector Space Embedding).
* **Cải tiến triển khai trong [`GraphEmbeddingService.java`](file:///d:/Code_Hoc/NCKH_GENERIS/Generics/complete/src/main/java/com/example/servingwebcontent/Service/detection/GraphEmbeddingService.java)**:
  1. **Node2Vec Random Walk Sampling**: Tự động phát các chuỗi bước đi ngẫu nhiên bậc hai xung quanh từng nút (Walk Length = 10, 10 walks/node) để khám phá không gian ngữ cảnh cấu trúc mạng.
  2. **Mã hóa Vector 8 Chiều (8-Dimensional Vector Embedding)**:
     $$\vec{v}_n = [e_{n,1}, e_{n,2}, \dots, e_{n,8}]^T \in \mathbb{R}^8$$
     Chuyển đổi toàn bộ cấu trúc đồ thị phức tạp thành các vector số học chuẩn hóa.
  3. **Mô hình Phát hiện Bất thường AI (Centroid Anomaly Detection Model)**:
     - Tính toán tâm khối $\vec{c} = \frac{1}{|V|} \sum_{u \in V} \vec{v}_u$.
     - Đo độ lệch phương sai $D(u) = \|\vec{v}_u - \vec{c}\|_2$ để tính điểm bất thường $\text{AnomalyScore}(u) \in [0.0..100.0]$.
     - Tự động nhận diện mẫu `AI_EMBEDDING_ANOMALY_NODE` và đưa ra kết luận phân loại `CRITICAL_AI_ANOMALY`, `HIGH_AI_RISK`.

---

### 5. Hệ sinh thái 4 Kịch bản Phân tích Toàn diện (4 Scenario Ecosystem)

| STT | Mã Kịch Bản | Tên Kịch Bản | Phân Loại Cấp Độ | Mẫu Nhận Diện Nổi Bật |
|---|---|---|---|---|
| **1** | **`GRAPH_AI_ANOMALY`** | Graph AI Embedding & Anomaly Detection Scenario | **Trí tuệ Nhân tạo (Graph AI / ML)**: Node2Vec & Anomaly Detection | `AI_EMBEDDING_ANOMALY_NODE`<br>`AI_ANOMALOUS_CLUSTER_PATTERN` |
| **2** | **`GRAPH_ANALYTICS`** | Graph Analytics & Community Detection Scenario | **Toán học Đồ thị Nâng cao**: PageRank & LPA Community | `PAGERANK_HIGH_CENTRALITY_HUB`<br>`CROSS_COMMUNITY_BRIDGE`<br>`DENSE_COMMUNITY_CLUSTER` |
| **3** | **`ASSOCIATION_GRAPH`** | Association Graph Scenario | **Phân tích Cấu trúc Liên kết**: Topology Degree Centrality | `HIGH_DEGREE_NODE`<br>`SHARED_RELATION_TARGET`<br>`MULTI_TYPE_BRIDGE` |
| **4** | **`EMAIL_URL_IP`** | Email - URL - IP Scenario | **Phân tích Nghiệp vụ Lừa đảo**: Domain Fraud Matching | `SHARED_URL`<br>`SHARED_IP_SENDER`<br>`HOSTING_CLUSTER`<br>`TRIANGLE_PATH` |

---

## PHẦN III. BẢN ĐỒ MÃ NGUỒN VÀ VAI TRÒ CÁC TỆP TIN ĐÃ PHÁT TRIỂN

```
Generics/complete/
├── src/main/java/com/example/servingwebcontent/
│   ├── ServingWebContentApplication.java               # Entry point hiển thị banner cổng 8080 nổi bật
│   ├── Model/dto/
│   │   ├── ApiResponse.java                            # Response wrapper chuẩn hóa có kèm custom message
│   │   ├── GraphData.java                              # Generic Graph container <N, E>
│   │   ├── NodeDTO.java & EdgeDTO.java                 # Node/Edge DTO mang generic attributes <A>
│   │   └── DetectionResult.java & RiskResult.java      # Generic DTO cho kết quả phát hiện và chấm điểm rủi ro
│   ├── Service/
│   │   ├── GraphQueryService.java                      # Lõi đồ thị: fetchGraph, fetchSubGraph k-hop, import, delete
│   │   └── detection/
│   │       ├── DetectionScenario.java                  # Interface chuẩn cho mọi kịch bản phân tích
│   │       ├── DetectionScenarioRegistry.java          # Bộ quản lý chuyển đổi Scenario động tại Runtime
│   │       ├── GraphAnalyticsService.java              # [MỚI] Thuật toán PageRank, LPA, Dijkstra, Jaccard
│   │       ├── GraphEmbeddingService.java              # [MỚI] AI Engine: Node2Vec Embedding & Anomaly Model
│   │       ├── GraphAnalyticsDetectionScenario.java    # [MỚI] Scenario phân tích đồ thị toán học
│   │       ├── GraphAiAnomalyDetectionScenario.java    # [MỚI] Scenario phân tích trí tuệ nhân tạo AI
│   │       ├── AssociationGraphDetectionScenario.java  # Scenario phân tích cấu trúc liên kết mạng
│   │       └── EmailUrlIpDetectionScenario.java        # Scenario phân tích lừa đảo Email-URL-IP
│   └── Controller/
│       ├── GraphApiController.java                     # API /api/graph, /api/graph/subgraph, /api/graph/import
│       └── DetectionApiController.java                 # API /patterns, /risk, /profile, /switch-scenario
└── src/main/resources/static/
    ├── index.html                                      # Giao diện Generic Explorer (vis-network, k-hop, Dropdown Scenario)
    ├── insights.html                                   # Giao diện Thống kê Graph & Bảng phân tích Analytics / AI Patterns
    └── export.html                                     # Giao diện Nhập/Xuất JSON đa miền (kèm dataset phong phú)
```

---

## PHẦN IV. KẾ HOẠCH NÂNG CẤP BÁO CÁO TỔNG KẾT NCKH / ĐỒ ÁN TỐT NGHIỆP

Dựa trên toàn bộ kết quả phát triển mới ở trên, cuốn báo cáo nâng cấp sẽ được tổ chức lại theo bố cục học thuật hoàn chỉnh gồm **4 Chương chính**:

1. **CHƯƠNG 1: TỔNG QUAN VỀ TỐI ƯU HÓA MÃ NGUỒN VÀ PHÂN TÍCH ĐỒ THỊ**
   - Vấn đề phụ thuộc miền dữ liệu (Domain-specific code lock-in).
   - Cơ sở lý thuyết về Java Generics, OOP và Thiết kế phần mềm hướng dữ liệu (Data-Driven Architecture).
   - Cơ sở toán học về Lý thuyết Đồ thị, Thuật toán Phân tích Đồ thị (Graph Analytics) và Học máy trên Đồ thị (Graph AI).
2. **CHƯƠNG 2: THIẾT KẾ VÀ KIẾN TRÚC HỆ THỐNG ĐỒ THỊ TỔNG QUÁT HÓA**
   - Kiến trúc tổng thể hệ thống phân tầng (Clean Layered Architecture).
   - Thiết kế mô hình dữ liệu tổng quát `GraphData<N, E>`, `NodeDTO<A>`, `EdgeDTO<A>`.
   - Cơ chế quản lý đa kịch bản động `DetectionScenarioRegistry` và giao tiếp REST API.
   - Giải pháp truy vấn đồ thị con bán kính k-hop (`k-hop Sub-graph`) chống nghẽn bộ nhớ.
3. **CHƯƠNG 3: HIỆN THỰC HÓA CÁC THUẬT TOÁN ĐỒ THỊ TOÁN HỌC VÀ TRÍ TUỆ NHÂN TẠO**
   - Cài đặt chi tiết thuật toán PageRank Centrality và Thuật toán Truyền nhãn LPA Community Detection.
   - Cài đặt thuật toán Tìm đường ngắn nhất Dijkstra và Hệ số tương đồng Jaccard.
   - Cài đặt mô hình AI: Nhúng vector không gian Node2Vec Random Walk và Mô hình Phát hiện Bất thường (Anomaly Detection).
   - Hiện thực hóa 4 Kịch bản Phân tích độc lập trong hệ thống.
4. **CHƯƠNG 4: THỰC NGHIỆM, ĐÁNH GIÁ VÀ TRỰC QUAN HÓA KẾT QUẢ**
   - Đánh giá thực nghiệm trên các bộ dữ liệu đa miền: Gian lận tài chính, Quản lý giáo dục, Mạng xã hội.
   - Trực quan hóa tương tác trên Web UI (Generic Graph Explorer, k-hop exploration, Insights Dashboard).
   - Đánh giá hiệu năng, tính tái sử dụng của mã nguồn mở và khả năng mở rộng trong tương lai.
   - **KẾT LUẬN VÀ KIẾN NGHỊ PHÁT TRIỂN**.
