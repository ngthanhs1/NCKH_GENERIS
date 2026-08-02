# DANH MỤC THUẬT TOÁN VÀ MÔ HÌNH TOÁN HỌC ĐỒ THỊ ĐÃ TÍCH HỢP HỆ THỐNG
## TỐI ƯU HÓA MÃ NGUỒN MỞ BẰNG GENERICS & GRAPH ANALYTICS / GRAPH AI

---

> [!NOTE]
> Báo cáo này tổng hợp đầy đủ toàn bộ **4 nhóm Thuật toán Đồ thị nâng cao (Graph Analytics)** và **Mô hình Trí tuệ Nhân tạo / Học máy trên Đồ thị (Graph Machine Learning & Graph AI)** đã được nghiên cứu và cài đặt trực tiếp vào hệ thống trên bộ dữ liệu tổng quát `GraphData<N, E>`.

---

## 1. TỔNG QUAN HỆ THỐNG THUẬT TOÁN ĐÃ BỔ SUNG

Để giải quyết triệt để hạn chế của các luật Heuristic/Rule-based đơn giản, hệ thống đã được trang bị **4 nhóm Thuật toán Toán học Đồ thị & AI chuyên sâu**:

```
                              ┌────────────────────────────────────────────────────────┐
                              │  MÔ HÌNH THUẬT TOÁN ĐỒ THỊ TỔNG QUÁT (GRAPH ENGINE)    │
                              └───────────────────────────┬────────────────────────────┘
                                                          │
         ┌────────────────────────┬───────────────────────┴───────────────────────┬────────────────────────┐
         ▼                        ▼                                               ▼                        ▼
┌──────────────────┐    ┌───────────────────┐                           ┌──────────────────┐    ┌────────────────────┐
│ 1. CENTRALITY    │    │ 2. COMMUNITY      │                           │ 3. PATHFINDING   │    │ 4. GRAPH MACHINE   │
│    ALGORITHMS    │    │    DETECTION      │                           │    & SIMILARITY  │    │    LEARNING / AI   │
├──────────────────┤    ├───────────────────┤                           ├──────────────────┤    ├────────────────────┤
│ • PageRank       │    │ • Label           │                           │ • Dijkstra       │    │ • Node2Vec Random  │
│   Centrality     │    │   Propagation     │                           │   Shortest Path  │    │   Walk Embedding   │
│ • Degree         │    │   Algorithm (LPA) │                           │ • Jaccard        │    │ • Centroid Anomaly │
│   Centrality     │    │ • Cross-Community │                           │   Similarity     │    │   Detection Model  │
│                  │    │   Bridge          │                           │   Coefficient    │    │                    │
└──────────────────┘    └───────────────────┘                           └──────────────────┘    └────────────────────┘
```

---

## 2. CHI TIẾT 4 NHÓM THUẬT TOÁN VÀ MÔ HÌNH TOÁN HỌC

### NHÓM 1: THUẬT TOÁN ĐỘ TRUNG TÂM (CENTRALITY ALGORITHMS)

#### 1.1. Thuật toán PageRank Centrality
- **Công thức toán học**:
  $$PR(u) = \frac{1-d}{N} + d \sum_{v \in In(u)} \frac{PR(v)}{Out(v)}$$
  *Trong đó:* $d = 0.85$ (Damping factor), $N$ là tổng số nút, $In(u)$ là tập các nút trỏ tới $u$, $Out(v)$ là số lượng cạnh đi ra của $v$.
- **Mục đích**: Xác định nút có tầm ảnh hưởng lớn nhất trong mạng lưới (kẻ cầm đầu trong đường dây lừa đảo, hoặc môn học hạt nhân trong chuỗi chương trình đào tạo).
- **Mã cài đặt**: `GraphAnalyticsService.calculatePageRank()`
- **Pattern sinh ra**: `PAGERANK_HIGH_CENTRALITY_HUB` (trong scenario `GRAPH_ANALYTICS`).

#### 1.2. Thuật toán Degree & Betweenness Centrality
- **Công thức**: $C_D(u) = deg(u)$
- **Mục đích**: Xác định nút có số lượng liên kết trực tiếp cao vượt trội, đóng vai trò đầu mối giao tiếp trong đồ thị.
- **Pattern sinh ra**: `HIGH_DEGREE_NODE` (trong scenario `ASSOCIATION_GRAPH`).

---

### NHÓM 2: THUẬT TOÁN PHÁT HIỆN CỘNG ĐỒNG (COMMUNITY DETECTION)

#### 2.1. Thuật toán Truyền Nhãn Gom Cụm (Label Propagation Algorithm - LPA)
- **Cơ chế toán học**:
  $$\text{Label}(u) = \arg\max_{l} \sum_{v \in N(u)} \mathbb{I}(\text{Label}(v) = l)$$
  Mỗi nút ban đầu khởi tạo với nhãn duy nhất. Qua các vòng lặp ngẫu nhiên, từng nút tự động chọn nhãn xuất hiện nhiều nhất trong tập hàng xóm kề $N(u)$ cho đến khi hệ thống hội tụ.
- **Mục đích**: Tự động phát hiện các cụm/băng nhóm/cộng đồng ngầm trong đồ thị mà không cần biết trước số lượng cụm.
- **Mã cài đặt**: `GraphAnalyticsService.calculateCommunities()`
- **Pattern sinh ra**: `DENSE_COMMUNITY_CLUSTER` (trong scenario `GRAPH_ANALYTICS`).

#### 2.2. Thuật toán Nút Cầu Nối Liên Cộng Đồng (Cross-Community Bridge Detection)
- **Mục đích**: Tìm kiếm các nút nằm ở vị trí giao thoa giữa 2 hoặc nhiều cộng đồng độc lập, đóng vai trò mấu chốt trong việc luân chuyển dòng tiền hoặc thông tin.
- **Pattern sinh ra**: `CROSS_COMMUNITY_BRIDGE`.

---

### NHÓM 3: THUẬT TOÁN TÌM ĐƯỜNG VÀ ĐỘ TƯƠNG ĐỒNG (PATHFINDING & SIMILARITY)

#### 3.1. Thuật toán Tìm đường Đi Ngắn Nhất Dijkstra (Dijkstra Shortest Path)
- **Công thức khoảng cách**:
  $$dist[v] = \min (dist[v], dist[u] + w(u, v))$$
- **Mục đích**: Tìm chuỗi liên kết ngắn nhất kết nối giữa 2 đối tượng bất kỳ trong đồ thị (VD: Tìm đường nối ngắn nhất giữa 2 tài khoản giao dịch nghi vấn).
- **Mã cài đặt**: `GraphAnalyticsService.findShortestPathDijkstra()`

#### 3.2. Hệ số Độ Tương Đồng Jaccard (Jaccard Similarity Coefficient)
- **Công thức toán học**:
  $$J(u, v) = \frac{|N(u) \cap N(v)|}{|N(u) \cup N(v)|}$$
  *Trong đó:* $N(u)$ và $N(v)$ là tập các nút hàng xóm kề của $u$ và $v$.
- **Mục đích**: Đo lường độ trùng lặp về hành vi và liên kết giữa 2 nút trong đồ thị ($J(u,v) \in [0.0, 1.0]$).
- **Mã cài đặt**: `GraphAnalyticsService.calculateJaccardSimilarity()`

---

### NHÓM 4: TRÍ TUỆ NHÂN TẠO VÀ HỌC MÁY TRÊN ĐỒ THỊ (GRAPH MACHINE LEARNING & GRAPH AI)

#### 4.1. Thuật toán Nhúng Vector Cấu trúc Đồ thị (Node2Vec Random Walk Embedding)
- **Cơ chế toán học**:
  - **Random Walk Sampling**: Phát các chuỗi hành trình bước đi ngẫu nhiên bậc hai xung quanh nút với độ dài $L = 10$, $W = 10$ walks/node.
  - **Co-occurrence Matrix Projection**: Biến đổi ma trận xuất hiện đồng thời của ngữ cảnh bước đi ngẫu nhiên thành Vector đặc trưng 8 chiều:
    $$\vec{v}_n = [e_{n,1}, e_{n,2}, \dots, e_{n,8}]^T \in \mathbb{R}^8$$
- **Mục đích**: Mã hóa toàn bộ ngữ cảnh và vị trí kiến trúc của nút thành các vector số học để đưa vào mô hình học máy.
- **Mã cài đặt**: `GraphEmbeddingService.generateEmbeddingsFromWalks()`

#### 4.2. Mô hình AI Phát hiện Bất thường (Centroid Anomaly Detection Model)
- **Công thức khoảng cách phương sai tâm khối**:
  $$\vec{c} = \frac{1}{|V|} \sum_{u \in V} \vec{v}_u, \quad D(u) = \|\vec{v}_u - \vec{c}\|_2$$
  $$\text{AnomalyScore}(u) = \frac{D(u)}{\max_{w \in V} D(w)} \times 100$$
- **Mục đích**: Tự động phát hiện các nút có vị trí liên kết biến đổi bất thường so với cụm hạt nhân chung của toàn hệ thống.
- **Mã cài đặt**: `GraphEmbeddingService.calculateAnomalyScores()`
- **Pattern sinh ra**: `AI_EMBEDDING_ANOMALY_NODE` và `AI_ANOMALOUS_CLUSTER_PATTERN` (trong scenario `GRAPH_AI_ANOMALY`).

---

## 3. BẢNG THỐNG KÊ 4 KỊCH BẢN PHÂN TÍCH VÀ CÁC THUẬT TOÁN TÍCH HỢP

| Scenario Key | Tên Kịch Bản | Nhóm Thuật Toán Sử Dụng | Mẫu Phát Hiện (Rule Code) |
|---|---|---|---|
| **`GRAPH_AI_ANOMALY`** | Graph AI Embedding & Anomaly Detection Scenario | **Graph Machine Learning & AI**: Node2Vec Embedding + Distance Anomaly Model | • `AI_EMBEDDING_ANOMALY_NODE`<br>• `AI_ANOMALOUS_CLUSTER_PATTERN` |
| **`GRAPH_ANALYTICS`** | Graph Analytics & Community Detection Scenario | **Graph Analytics Toán học**: PageRank Centrality & LPA Community Detection | • `PAGERANK_HIGH_CENTRALITY_HUB`<br>• `CROSS_COMMUNITY_BRIDGE`<br>• `DENSE_COMMUNITY_CLUSTER` |
| **`ASSOCIATION_GRAPH`** | Association Graph Scenario | **Phân tích Cấu trúc Liên kết**: Topology Degree Centrality & Shared Target | • `HIGH_DEGREE_NODE`<br>• `SHARED_RELATION_TARGET`<br>• `MULTI_TYPE_BRIDGE` |
| **`EMAIL_URL_IP`** | Email - URL - IP Scenario | **Phân tích Nghiệp vụ Lừa đảo**: Domain Fraud Rule Matching | • `SHARED_URL`<br>• `SHARED_IP_SENDER`<br>• `HOSTING_CLUSTER`<br>• `TRIANGLE_PATH` |

---

## 4. KẾT LUẬN

Hệ thống hiện tại đã sở hữu đầy đủ **tất cả 4 nhóm Thuật toán Đồ thị Nâng cao & Mô hình Học máy AI** theo đúng yêu cầu khắt khe của một công trình Nghiên cứu Khoa học chuyên sâu.

Việc tích hợp các thuật toán này trên nền tảng **Generics đối tượng** giúp hệ thống có thể chạy PageRank, LPA Community Detection hay Node2Vec AI trên **BẤT KỲ bộ dữ liệu đồ thị nào** (Gian lận tài chính, Giáo dục, Mạng xã hội, Logistics...) mà không phải viết lại mã nguồn.
