# BÁO CÁO TRIỂN KHAI GIAI ĐOẠN 1
## TỐI ƯU HÓA KIẾN TRÚC & TRẢI NGHIỆM NGƯỜI DÙNG TẠI THỜI ĐIỂM CHẠY (RUNTIME)

---

> [!NOTE]
> Báo cáo này tổng hợp chi tiết toàn bộ các tính năng và cải tiến đã được hiện thực hóa trong **Giai đoạn 1 (Ngắn hạn)** của lộ trình nghiên cứu khoa học, tập trung vào khả năng chuyển đổi kịch bản phân tích động và truy vấn đồ thị con theo bán kính.

---

## 1. MỤC TIÊU VÀ NỘI DUNG ĐÃ HOÀN THÀNH

Trong Giai đoạn 1, hệ thống đã được nâng cấp mạnh mẽ ở cả 3 tầng: **Service**, **REST API** và **Data-Driven Frontend UI**.

### 1.1. Chuyển đổi Kịch bản Phân tích Động tại Runtime (Dynamic Scenario Switching)
- **Cải tiến `DetectionScenarioRegistry`**:
  - Bổ sung trường `currentActiveKey` quản lý bằng bộ nhớ (`volatile` & `synchronized`).
  - Cung cấp phương thức `setActiveScenario(String scenarioKey)` cho phép chuyển đổi kịch bản ngay tức thì mà **không cần dừng hay khởi động lại ứng dụng Spring Boot**.
- **Endpoint REST API mới (`POST /api/detection/switch-scenario`)**:
  - Cho phép client gửi yêu cầu chuyển Scenario với tham số `scenarioKey` (VD: `ASSOCIATION_GRAPH`, `EMAIL_URL_IP`, `EDUCATION_GRAPH`).
  - Trả về thông tin kịch bản mới vừa kích hoạt kèm danh sách các kịch bản khả dụng.
- **Tích hợp Bộ chọn trên Giao diện Web (Scenario Selector UI)**:
  - Bổ sung thanh chọn Dropdown ngay tại Header bar trên giao diện `index.html`.
  - Khi người dùng đổi kịch bản trong Dropdown, UI tự động phát API `POST /api/detection/switch-scenario` và cập nhật thông tin phân tích rủi ro/pattern tương ứng.

### 1.2. Truy vấn Đồ thị Con theo Bán kính k-hop (k-hop Subgraph Query)
- **Bổ sung phương thức `fetchSubGraph` trong `GraphQueryService`**:
  - Cho phép truy vấn mạng lưới xung quanh nút tâm (`centerId`) trong bán kính `depth` (bán kính k-hop từ 1 đến 5).
  - Sử dụng câu truy vấn Cypher tối ưu:
    ```cypher
    MATCH (center:Node {id: $centerId})
    MATCH p = (center)-[:RELATION*0..2]-(n:Node)
    WITH DISTINCT n
    ...
    ```
- **Endpoint REST API mới (`GET /api/graph/subgraph`)**:
  - Nhận tham số `nodeId` và `depth` (mặc định depth = 2).
  - Giúp Frontend chỉ tải vùng đồ thị cần quan tâm thay vì tải toàn bộ ngân hàng dữ liệu lớn.
- **Nút tương tác k-hop trên UI Explorer**:
  - Tại bảng thông tin chi tiết nút (Detail Panel), bổ sung nút **"Lân cận (k-hop)"**.
  - Khi người dùng bấm chọn một nút và nhấn **"Lân cận (k-hop)"**, đồ thị tự động lọc và phóng to tập trung vào vùng đồ thị xung quanh nút đó.

### 1.3. Chuẩn hóa Phản hồi & Kiểm thử Tự động (Testing & Validation)
- **Mở rộng `ApiResponse`**:
  - Thêm phương thức tĩnh `ApiResponse.ok(R data, String message)` hỗ trợ trả về phản hồi chuẩn kèm thông điệp thông báo tùy chỉnh.
- **Bổ sung Unit Tests**:
  - Đã thêm các test case cho `switchScenario` trong `DetectionApiControllerTest`.
  - Đã thêm các test case cho `getSubGraph` trong `GraphApiControllerTest`.

---

## 2. MINH HỌA DANH SÁCH API GIAI ĐOẠN 1

| Phương thức | Endpoint REST API | Tham số | Chức năng |
|---|---|---|---|
| **POST** | `/api/detection/switch-scenario` | `scenarioKey` (query param) | Chuyển đổi kịch bản phân tích active tại runtime |
| **GET** | `/api/graph/subgraph` | `nodeId`, `depth` (mặc định 2) | Trả về đồ thị con k-hop xung quanh `nodeId` |
| **GET** | `/api/detection/profile` | *Không có* | Trả về thông tin kịch bản hiện tại và danh sách kịch bản khả dụng |
| **GET** | `/api/graph` | *Không có* | Trả về toàn bộ đồ thị hiện tại |
| **POST** | `/api/graph/import` | `replace` (boolean) | Nhập dữ liệu đồ thị theo cấu trúc generic |
| **DELETE**| `/api/graph/nodes/{nodeId}` | `nodeId` (path var) | Xóa nút và toàn bộ các cạnh liên quan |

---

## 3. HƯỚNG DẪN KIỂM THỬ TÍNH NĂNG GIAI ĐOẠN 1

### 3.1. Thử nghiệm Chuyển đổi Scenario Động bằng PowerShell / cURL
Chạy lệnh PowerShell sau để chuyển đổi kịch bản từ `ASSOCIATION_GRAPH` sang `EMAIL_URL_IP`:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/detection/switch-scenario?scenarioKey=EMAIL_URL_IP" -Method Post | ConvertTo-Json -Depth 5
```

### 3.2. Thử nghiệm Truy vấn Subgraph k-hop
Chạy lệnh PowerShell sau để lấy đồ thị bán kính 2-hop xung quanh nút `P101`:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/graph/subgraph?nodeId=P101&depth=2" -Method Get | ConvertTo-Json -Depth 5
```

### 3.3. Thao tác Trực quan trên Trình duyệt Web
1. Mở trang chủ: **`http://localhost:8080/`**
2. Quan sát thanh Header: Thay đổi tùy chọn trong ô **Scenario** (ví dụ chọn *Email - URL - IP Scenario*), hệ thống sẽ chuyển kịch bản ngay tức khắc.
3. Nhấp chọn một nút bất kỳ trên mạng đồ thị -> Bấm nút **"Lân cận (k-hop)"** ở bảng bên phải để chỉ hiển thị mạng đồ thị bao quanh nút đó.
4. Bấm nút **"Tải toàn bộ"** để quay về màn hình xem toàn bộ đồ thị.

---

## 4. KẾT LUẬN GIAI ĐOẠN 1

Giai đoạn 1 đã giải quyết triệt để 2 hạn chế lớn nhất trước đó:
1. **Loại bỏ việc phải khởi động lại server khi đổi bài toán phân tích.**
2. **Khắc phục nghẽn hiển thị bằng cách hỗ trợ khoanh vùng đồ thị con (k-hop Subgraph).**

Hệ thống đã sẵn sàng để bước vào **Giai đoạn 2: Tích hợp Thư viện Thuật toán Đồ thị Nâng cao (Graph Analytics: PageRank, Louvain Community Detection)**.
