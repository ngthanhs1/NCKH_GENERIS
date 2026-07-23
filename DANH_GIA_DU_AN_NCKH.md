# BÁO CÁO ĐÁNH GIÁ DỰ ÁN NGHIÊN CỨU KHOA HỌC
## TỐI ƯU HÓA MÃ NGUỒN MỞ (CODE) TRONG PHÁT TRIỂN PHẦN MỀM THÔNG QUA KỸ THUẬT TỔNG QUÁT HÓA (GENERICS) ĐỐI TƯỢNG

---

> [!NOTE]
> Báo cáo này tổng hợp đánh giá toàn diện về hiện trạng kiến trúc mã nguồn, các kết quả đã đạt được, những hạn chế còn tồn tại, đồng thời đề xuất danh mục các **thuật toán phân tích đồ thị (Graph Analytics & Graph AI)** cần bổ sung để nâng tầm đề tài nghiên cứu khoa học.

---

## 1. NHỮNG NỘI DUNG ĐÃ LÀM ĐƯỢC (ACCOMPLISHMENTS)

Hệ thống hiện tại đã xây dựng thành công bộ khung kiến trúc phần mềm tái sử dụng mức độ cao, đạt được các mục tiêu khoa học cốt lõi:

### 1.1. Lõi Đồ thị Tổng quát hóa (Generic Graph Core)
- **Mô hình hóa dữ liệu thống nhất**: Đưa mọi dữ liệu miền (Fraud, Giáo dục, Logistics...) về một lược đồ Neo4j dùng chung: `(:Node {id, type, attr_*}) -[:RELATION {type, attr_*}]-> (:Node)`.
- **Bộ DTO Generic linh hoạt**: Thiết kế các lớp dữ liệu không bị khóa cứng vào miền bài toán cụ thể:
  - `GraphData<N, E>`: Chứa danh sách nodes và edges.
  - `NodeDTO<A>` & `EdgeDTO<A>`: Chứa thông tin thuộc tính dạng map/object mở rộng (`attributes`).
  - `ApiResponse<T>`: Chuẩn hóa dữ liệu phản hồi REST API.

### 1.2. Phân tách Kiến trúc theo Mô hình Kịch bản (Scenario-Driven Architecture)
- **Tách biệt Business Logic khỏi Lõi Đồ thị**: Tách luật phân tích phát hiện mẫu (`PatternRule`) và chấm điểm rủi ro (`RiskScoreStrategy`) thành các **Scenario** độc lập.
- **Quản lý Đa kịch bản (Scenario Registry)**: Cài đặt `DetectionScenarioRegistry` cho phép thay đổi bài toán phân tích thông qua cấu hình `detection.active-scenario` trong `application.properties` mà **không cần sửa lại mã nguồn Lõi Đồ thị hay Controller**.
- **Các Kịch bản đã triển khai**:
  1. `ASSOCIATION_GRAPH` (Mặc định): Phân tích đồ thị liên kết tổng quát (`HIGH_DEGREE_NODE`, `SHARED_RELATION_TARGET`, `MULTI_TYPE_BRIDGE`).
  2. `EMAIL_URL_IP`: Phân tích đồ thị lừa đảo Email-URL-IP (`SHARED_URL`, `SHARED_IP_SENDER`, `HOSTING_CLUSTER`, `TRIANGLE_PATH`).

### 1.3. Giao diện Người dùng Hướng Dữ liệu (Data-Driven UI)
- **Trực quan hóa tự thích nghi (Generic Graph Explorer)**: Tự động phân màu nút theo `Node Type`, tự động trích xuất nhãn (`label`), tự động sinh bộ lọc theo `Type` và `Relation` từ dữ liệu thực tế thu được từ API.
- **Thao tác tương tác trực tiếp**: Xem thông tin chi tiết dạng JSON động, xóa nút và cản gỡ cạnh liên kết (`DETACH DELETE`).
- **Trang Quản lý Riêng biệt**: 
  - `insights.html`: Thống kê số lượng nút, cạnh và tỷ lệ phân bố.
  - `export.html`: Nhập/Xuất file JSON với 2 chế độ **Append** (Gộp) và **Replace** (Ghi đè) có cơ chế validate lỗi tham chiếu.

---

## 2. ĐÁNH GIÁ ƯU ĐIỂM VÀ NHƯỢC ĐIỂM (PROS & CONS)

### 2.1. Ưu điểm (Strengths)
1. **Tính Tái Sử Dụng Cực Cao (High Reusability)**: Đạt tiêu chí *"Write Once, Apply Everywhere"*. Cùng một codebase backend và frontend có thể phục vụ cho nhiều miền dữ liệu khác nhau mà không phải viết lại lớp Model hay UI.
2. **Khả năng Mở rộng Mạch lạc (Extensibility)**: Việc thêm bài toán phân tích mới chỉ đơn giản là tạo thêm một lớp implement interface `DetectionScenario`, hoàn toàn không ảnh hưởng tới các module đang chạy.
3. **An toàn Kiểu dữ liệu & Giảm lặp code**: Vận dụng triệt để Java Generics (`<N, E>`, `<T, ID>`, `<A>`) giúp mã nguồn ngắn gọn, tăng độ Type-Safety ở thời điểm biên dịch.
4. **Data-Driven Frontend**: UI hoàn toàn trung lập với miền bài toán, không hard-code tên trường hiển thị.

### 2.2. Nhược điểm (Weaknesses)
1. **Chưa có cơ chế Chuyển Đổi Scenario Động trên UI**: Hiện tại việc đổi Scenario vẫn phải sửa file `application.properties` và khởi động lại ứng dụng, chưa hỗ trợ đổi trực tiếp từ giao diện web qua REST API.
2. **Quản lý Thuộc tính Động (Dynamic Attributes)**: Việc ép toàn bộ thuộc tính về `Map<String, Object>` giúp đạt tính tổng quát nhưng làm mất đi sự kiểm tra kiểu dữ liệu khắt khe (Strongly-typed validation) đối với các trường nghiệp vụ cụ thể.
3. **Hiệu năng Query trên Tập Dữ liệu Cực Lớn (Scalability)**: Đồ thị hiện tại lấy toàn bộ nút và cạnh về memory qua `fetchGraph()`. Nếu tập dữ liệu lên đến hàng triệu node, việc render trên web (`vis-network`) và tính toán trên RAM sẽ bị nghẽn (bottleneck).

---

## 3. THIẾU SÓT VỀ THUẬT TOÁN VÀ MÔ HÌNH TOÁN HỌC (MISSING ALGORITHMS)

> [!WARNING]
> Hạn chế lớn nhất hiện nay của hệ thống từ góc độ **Nghiên cứu Khoa học chuyên sâu** là các kịch bản phân tích (`Scenario`) mới chỉ dừng lại ở các **Luật Heuristic/Rule-based đơn giản** (đếm số bậc `degree`, đếm liên kết trùng). Hệ thống **chưa tích hợp các Thuật toán Đồ thị nâng cao (Graph Analytics)** và **Học máy trên đồ thị (Graph Machine Learning)**.

### Các Thuật toán Cần Bổ sung:

| STT | Nhóm Thuật toán | Thuật toán Cụ thể | Mục đích & Ứng dụng trong Bài toán |
|---|---|---|---|
| **1** | **Centrality Algorithms** *(Độ trung tâm)* | • PageRank<br>• Betweenness Centrality<br>• Closeness Centrality | Xác định nút có tầm ảnh hưởng lớn nhất trong mạng lưới, kẻ cầm đầu trong đường dây lừa đảo, hoặc môn học tiên quyết quan trọng nhất. |
| **2** | **Community Detection** *(Phát hiện cụm/Cộng đồng)* | • Louvain Algorithm<br>• Label Propagation Algorithm (LPA)<br>• Weakly Connected Components (WCC) | Tự động gom nhóm các tài khoản/nút thành các cộng đồng ngầm, phát hiện các băng nhóm giao dịch bất hợp pháp. |
| **3** | **Pathfinding & Similarity** *(Tìm đường & Độ tương đồng)* | • Dijkstra / A* Shortest Path<br>• Jaccard Similarity / Cosine Similarity | Tìm đường đi ngắn nhất giữa 2 đối tượng nghi ngờ, đo độ tương đồng về hành vi giữa 2 nút trong đồ thị. |
| **4** | **Graph Embedding & Machine Learning** | • Node2Vec / GraphSAGE<br>• Graph Convolutional Networks (GCN) | Chuyển đổi cấu trúc đồ thị thành các vector số học (Embeddings) để đưa vào mô hình AI phân loại rủi ro tự động. |

---

## 4. LỘ TRÌNH VÀ HƯỚNG PHÁT TRIỂN TIẾP THEO (ROADMAP)

### Giai đoạn 1: Tối ưu hóa Kiến trúc & Trải nghiệm Người dùng (Ngắn hạn)
- [ ] **API Đổi Scenario Động**: Thêm endpoint `POST /api/detection/active-scenario` và bổ sung Dropdown menu trên UI cho phép người dùng chuyển đổi Scenario trực tiếp tại runtime.
- [ ] **Validate Payload Nâng cao**: Tích hợp JSON Schema validation cho từng miền dữ liệu để đảm bảo thuộc tính nghiệp vụ không bị sai lệch kiểu dữ liệu khi import.
- [ ] **Phân trang Đồ thị (Graph Pagination / Sub-graph Query)**: Hỗ trợ truy vấn đồ thị theo bán kính (k-hop neighborhood) từ một nút được chọn để tránh quá tải bộ nhớ trình duyệt khi đồ thị lớn.

### Giai đoạn 2: Tích hợp Thư viện Thuật toán Đồ thị (Trung hạn)
- [ ] **Tích hợp Neo4j Graph Data Science (GDS) Library**:
  - Viết các Service gọi thuật toán **PageRank** và **Louvain Community Detection** từ Neo4j GDS.
  - Đóng gói kết quả tính toán độ trung tâm và mã cộng đồng vào thuộc tính tổng quát `attributes` của `NodeDTO`.
- [ ] **Xây dựng Scenario "Graph Analytics General"**:
  - Tạo `GraphAnalyticsDetectionScenario` tận dụng chỉ số PageRank và Community ID để tự động gắn nhãn mức độ quan trọng/rủi ro cho nút.

### Giai đoạn 3: Tích hợp AI / Machine Learning trên Đồ thị (Dài hạn)
- [ ] **Module Graph Embedding (Node2Vec)**: Tự động trích xuất đặc trưng cấu hình mạng lưới (topology) thành vector đặc trưng.
- [ ] **Mô hình Phát hiện Bất thường (Anomaly Detection)**: Sử dụng Isolation Forest hoặc Autoencoder trên các vector Graph Embedding để phát hiện các mối nối bất thường trong hệ thống mở.

---

## 5. KẾT LUẬN

Đề tài NCKH đã hoàn thành xuất sắc **Phần 1 - Kiến trúc phần mềm & Tối ưu hóa mã nguồn mở bằng Generics**. Hệ thống đạt tính đóng gói và tái sử dụng cao. 

Để phát triển đề tài lên tầm **Công trình NCKH xuất sắc/Bài báo khoa học Quốc tế**, trọng tâm của giai đoạn tiếp theo là **Phần 2 - Tích hợp các Thuật toán Đồ thị (Graph Analytics & Graph AI)** vào trong các `DetectionScenario` tổng quát của hệ thống.
