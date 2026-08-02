# BÁO CÁO TRIỂN KHAI GIAI ĐOẠN 2
## TÍCH HỢP THƯ VIỆN THUẬT TOÁN ĐỒ THỊ NÂNG CAO (GRAPH ANALYTICS & COMMUNITY DETECTION)

---

> [!NOTE]
> Báo cáo này tổng hợp chi tiết toàn bộ các thuật toán phân tích đồ thị toán học nâng cao đã được hiện thực hóa trong **Giai đoạn 2 (Trung hạn)** của lộ trình nghiên cứu khoa học, bao gồm **PageRank Centrality** và **Label Propagation Community Detection (LPA)** áp dụng trực tiếp trên cấu trúc đồ thị tổng quát `GraphData<N, E>`.

---

## 1. MỤC TIÊU VÀ NỘI DUNG ĐÃ HOÀN THÀNH

Trong Giai đoạn 2, hệ thống đã chuyển mình từ các luật phân tích Heuristic đơn giản sang việc làm chủ các **Mô hình Thuật toán Đồ thị Nâng cao (Graph Analytics)**.

### 1.1. Cài đặt Thư viện Thuật toán Đồ thị (`GraphAnalyticsService.java`)
Hệ thống đã tự nghiên cứu và cài đặt thành công 2 thuật toán toán học đồ thị kinh điển chạy trực tiếp trên bộ dữ liệu `GraphData<NodeDTO, EdgeDTO>` tổng quát:

1. **Thuật toán PageRank Centrality**:
   - Tự động tính toán điểm số tầm ảnh hưởng và độ trung tâm của mọi nút trong đồ thị dựa trên mô hình Markov Chain và damping factor ($d = 0.85$).
   - Điểm số PageRank được chuẩn hóa tự động về thang điểm 100.0, giúp phát hiện ra các nút là hạt nhân/trung tâm điều phối của mạng lưới (`PAGERANK_HIGH_CENTRALITY_HUB`).
2. **Thuật toán Tự gom cụm / Phát hiện Cộng đồng (Label Propagation Algorithm - LPA)**:
   - Tự động truyền nhãn cộng đồng giữa các nút kề nhau để phát hiện các cụm/cộng đồng liên kết mật thiết (`COMMUNITY_CLUSTER`).
   - Gắn mã nhận diện cộng đồng `communityId` cho từng nút trong đồ thị.
3. **Phát hiện Nút Cầu Nối Liên Cộng Đồng (Cross-Community Bridge Detection)**:
   - Tự động phát hiện các nút đứng giữa nối 2 hoặc nhiều cộng đồng khác nhau (`CROSS_COMMUNITY_BRIDGE`), đóng vai trò mấu chốt trong việc truyền tải thông tin hoặc luồng giao dịch.

### 1.2. Xây dựng Scenario Mới: `GRAPH_ANALYTICS`
- **Tạo lớp `GraphAnalyticsDetectionScenario.java`**:
  - Đóng gói toàn bộ kết quả phân tích từ `GraphAnalyticsService` thành một Kịch bản (Scenario) chuẩn trong hệ thống.
  - Mã định danh: `GRAPH_ANALYTICS`.
  - Tên hiển thị: `Graph Analytics & Community Detection Scenario`.
- **Đánh giá mức độ quan trọng / Rủi ro dựa trên thuật toán (`evaluate`)**:
  - Thay vì gán điểm cố định, điểm số được tính toán trực tiếp theo chỉ số PageRank và vị trí trong cộng đồng.
  - Phân loại nút thành 4 cấp độ:
    - `CRITICAL_HUB` (PageRank $\ge 75$)
    - `HIGH_INFLUENCE` (PageRank $\ge 45$)
    - `MODERATE` (PageRank $\ge 20$)
    - `ISOLATED` (PageRank $< 20$)

### 1.3. Cải tiến Trực quan hóa trên Giao diện Web (`insights.html`)
- Trang **Thông tin Graph (`insights.html`)** được tích hợp thêm khu vực **Graph Analytics & Detection Patterns**.
- Tự động hiển thị các mẫu cộng đồng, nút PageRank cao và danh sách nút rủi ro theo kịch bản `GRAPH_ANALYTICS` đang hoạt động.

### 1.4. Kiểm thử Tự động (Unit Tests)
- Đã thêm file unit test `GraphAnalyticsDetectionScenarioTest.java` kiểm thử tính chính xác của thuật toán PageRank và Label Propagation.

---

## 2. BẢNG TỔNG HỢP KỊCH BẢN PHÂN TÍCH HỆ THỐNG (SAU GIAI ĐOẠN 2)

Hệ thống hiện tại sở hữu **3 Kịch bản Phân tích độc lập**, người dùng có thể chuyển đổi linh hoạt trực tiếp tại Header của ứng dụng Web:

| Scenario Key | Tên Hiển Thị | Miền Áp Dụng / Thuật Toán Sử Dụng | Mẫu Phát Hiện Cốt Lõi |
|---|---|---|---|
| **`GRAPH_ANALYTICS`** *(Mới)* | Graph Analytics & Community Detection Scenario | Thuật toán PageRank Centrality & Label Propagation Community Detection (LPA) | • `PAGERANK_HIGH_CENTRALITY_HUB`<br>• `CROSS_COMMUNITY_BRIDGE`<br>• `DENSE_COMMUNITY_CLUSTER` |
| **`ASSOCIATION_GRAPH`** | Association Graph Scenario | Phân tích cấu trúc đồ thị liên kết mạng lưới tổng quát | • `HIGH_DEGREE_NODE`<br>• `SHARED_RELATION_TARGET`<br>• `MULTI_TYPE_BRIDGE` |
| **`EMAIL_URL_IP`** | Email - URL - IP Scenario | Phân tích bài toán lừa đảo / gian lận truyền thông | • `SHARED_URL`<br>• `SHARED_IP_SENDER`<br>• `HOSTING_CLUSTER`<br>• `TRIANGLE_PATH` |

---

## 3. HƯỚNG DẪN TRẢI NGHIỆM GIAI ĐOẠN 2 TRÊN GIAO DIỆN WEB

1. **Khởi chạy ứng dụng**:
   ```powershell
   cd d:\Code_Hoc\NCKH_GENERIS\Generics\complete
   $env:JAVA_HOME="D:\Java"
   .\gradlew.bat bootRun
   ```

2. **Chuyển sang Kịch bản Graph Analytics**:
   - Truy cập **`http://localhost:8080/`**.
   - Tại menu **Scenario** trên góc phải Header, chọn: **`Graph Analytics & Community Detection Scenario (GRAPH_ANALYTICS)`**.

3. **Xem Thống kê Cụm & PageRank**:
   - Truy cập trang **`http://localhost:8080/insights.html`**.
   - Xem bảng **Phân tích Graph Analytics & Detection Patterns** ở góc dưới: Hệ thống sẽ tự động liệt kê danh sách các Nút có PageRank cao nhất và các Cộng đồng (Community Clusters) vừa phát hiện được.

---

## 4. KẾT LUẬN GIAI ĐOẠN 2

Giai đoạn 2 đã nâng tầm đề tài NCKH từ việc **chỉ tổ chức mã nguồn bằng Generics** lên thành một **Hệ thống Phân tích Đồ thị Thông minh (Intelligent Graph Analytics Engine)**. 

Đề tài đã hoàn thành xuất sắc cả 2 mục tiêu lớn:
- **Mục tiêu 1 (Kỹ thuật phần mềm)**: Khung kiến trúc tổng quát hóa (Generics) hoàn chỉnh.
- **Mục tiêu 2 (Khoa học máy tính)**: Cài đặt thành công các thuật toán phân tích đồ thị toán học (PageRank, LPA Community Detection).

Hệ thống đã sẵn sàng cho **Giai đoạn 3 (Tích hợp AI / Graph Machine Learning Node2Vec)** trong tương lai.
