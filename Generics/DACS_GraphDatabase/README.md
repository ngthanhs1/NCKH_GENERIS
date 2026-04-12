# Xây dựng và tìm hiểu CSDL đồ thị và minh họa cho bài toán cụ thể (phát hiện gian lận)

## Giới thiệu

Đây là đồ án môn **Đảm bảo an toàn cơ sở dữ liệu (DACS)** với mục tiêu **tìm hiểu CSDL đồ thị (Graph Database)** và xây dựng một **ứng dụng minh họa trực quan** các đối tượng và mối quan hệ trong một bài toán cụ thể.

Trong đồ án này, bài toán được lựa chọn để minh họa là **phát hiện gian lận/phishing** dựa trên các mối liên kết giữa **Email – URL – IPAddress**.

## Mục tiêu đồ án (đúng theo đề tài)

1. **Phân tích và tìm hiểu CSDL đồ thị (Graph Database)**
2. **Xây dựng mô hình dữ liệu đồ thị cho bài toán lựa chọn** (nodes, relationships) – bài toán phát hiện gian lận
3. **Rút ra phân tích từ đồ thị cụ thể và các mối quan hệ** (pattern, truy vết, thống kê theo liên kết)

## Kết quả

Một **ứng dụng web minh họa trực quan**:
- Tạo/hiển thị các node và relationships trong Neo4j
- Truy vấn và minh họa các pattern cơ bản liên quan gian lận
- Trực quan hóa đồ thị bằng D3.js và tương tác trên trình duyệt

### Công nghệ sử dụng

- **Backend**: Spring Boot (Java)
- **Database**: Neo4j (Graph Database)
- **Frontend**: HTML5, CSS3, JavaScript (D3.js)
- **Build Tools**: Maven/Gradle

## 🎯 Chức năng minh họa chính

### 1) Mô hình hóa dữ liệu đồ thị

- Lưu trữ 3 loại nodes: **Email**, **URL**, **IPAddress**
- 3 loại relationships:
  - `Email -[:CONTAINS_LINK]-> URL` (email chứa link)
  - `Email -[:SENT_FROM]-> IPAddress` (email gửi từ IP)
  - `URL -[:HOSTED_BY]-> IPAddress` (URL được host trên IP)

### 2) Minh họa bài toán gian lận (phân tích từ quan hệ)

Ứng dụng minh họa cách phân tích dựa trên **pattern trong đồ thị** (ví dụ: shared URL/shared IP, truy vết Email → URL → IP) và chấm điểm **rule-based** để trình diễn.

Luồng thuật toán (dạng đơn giản, phục vụ minh họa):
1) Ingest dữ liệu (tạo node/relationship) bằng `MERGE`.
2) Chạy phân tích (`GET /api/graph/analyze`) để tính `riskScore` và gán `verdict` cho các node.
3) UI lấy graph từ `GET /api/graph/full` và tô màu theo `verdict`.

Lưu ý:
- Trong phần minh họa này, một “mối quan hệ/sự kiện” được xem là đầy đủ khi có **đủ bộ 3 Email + URL + IP** (tạo thành tam giác Email–URL–IP). Bạn có thể tạo node/relationship từng phần, nhưng **kết quả chấm điểm chính (verdict/riskScore) chỉ được xác định rõ khi node tham gia đủ bộ 3**.
- Trước khi chạy phân tích, hoặc khi node chưa tham gia bộ 3, node sẽ ở trạng thái **UNKNOWN** (màu xám).

#### Email (0-100 điểm)
- +30 điểm: Email có ít nhất 1 URL (`CONTAINS_LINK`)
- +20 điểm: Email có ít nhất 1 IP nguồn (`SENT_FROM`)
- +30 điểm: Có IP nguồn được dùng bởi > 1 Email (**shared IP**)
- +30 điểm: Có URL xuất hiện trong > 1 Email (**shared URL**)

Điểm được chuẩn hóa về 0–100 (cắt ngưỡng 100) và được lưu vào Neo4j (`riskScore`, `verdict`).

#### IPAddress
- Minh họa dựa trên tần suất: nếu 1 IP được > 1 Email sử dụng thì đánh dấu đáng nghi.

#### URL
- Minh họa dựa trên tần suất: nếu 1 URL xuất hiện trong > 1 Email thì đánh dấu đáng nghi.

### 3. Phân loại Verdict

- **SAFE (BÌNH THƯỜNG/CHƯA CÓ DẤU HIỆU)**: score = 0 - màu xanh
- **SUSPICIOUS (ĐÁNG NGHI)**: score > 0 và < 60 - màu cam
- **FRAUD (GIAN LẬN)**: điểm ≥ 60 - màu đỏ

Ghi chú: trong đồ án, verdict thể hiện **mức độ rủi ro theo dấu hiệu từ quan hệ trong đồ thị**, không phải “kết luận pháp lý”. Node mới thêm trước khi chạy phân tích sẽ ở trạng thái **UNKNOWN** (màu xám).

### 3) Trực quan hóa

- Hiển thị graph tương tác sử dụng D3.js force-directed layout
- Màu sắc node theo mức độ rủi ro
- Hover để xem chi tiết
- Click để ghim thông tin
- Filter theo loại node và verdict

## 🚀 Hướng dẫn cài đặt và chạy

### Yêu cầu hệ thống

- Java 17 trở lên
- Maven 3.6+ hoặc Gradle 7+
- Neo4j Database (Neo4j Aura hoặc local)

### Bước 1: Clone repository

```bash
git clone https://github.com/ngthanhs1/DACS_GraphDatabase.git
cd DACS_GraphDatabase/complete
```

### Bước 2: Cấu hình Neo4j

Thiết lập biến môi trường (khuyến nghị, tránh commit mật khẩu vào repo):

```bash
export NEO4J_URI='neo4j+s://your-instance.databases.neo4j.io'
export NEO4J_USERNAME='neo4j'
export NEO4J_PASSWORD='your-password'
export NEO4J_DATABASE='neo4j'
```

Hoặc mở file `complete/src/main/resources/application.properties` và điền các biến tương ứng.

```properties
spring.neo4j.uri=${NEO4J_URI}
spring.neo4j.authentication.username=${NEO4J_USERNAME}
spring.neo4j.authentication.password=${NEO4J_PASSWORD}
spring.data.neo4j.database=${NEO4J_DATABASE:neo4j}
```

### Bước 3: Build và chạy ứng dụng

**Sử dụng Maven:**
```bash
./mvnw spring-boot:run
```

**Sử dụng Gradle:**
```bash
./gradlew bootRun
```

### Bước 4: Truy cập ứng dụng

Mở trình duyệt và truy cập: `http://localhost:8080`

## 📚 API Endpoints

### Health Check
- `GET /api/graph/health` - Kiểm tra kết nối Neo4j và thống kê nodes

### Tạo dữ liệu
- `POST /api/email` - Tạo Email node
- `POST /api/email/with-url` - Tạo Email kèm URL
- `GET /api/graph/link?email=...&url=...&ip=...` - Tạo mối quan hệ đầy đủ
- `GET /api/graph/link-partial?email=...&url=...&ip=...` - Tạo node/relationship (không bắt buộc đủ 3)
- `DELETE /api/graph/node?id=...` - Xóa 1 node theo id (DETACH DELETE)

### Phân tích
- `GET /api/graph/analyze` - Chạy phân tích rủi ro toàn bộ graph
- `GET /api/graph/analysis/email-risk` - Phân tích rủi ro Email
- `GET /api/graph/analysis/url-risk` - Phân tích rủi ro URL
- `GET /api/graph/analysis/ip-risk` - Phân tích rủi ro IP

### Truy vấn
- `GET /api/graph/full` - Lấy toàn bộ graph data để visualization

## 🏗️ Cấu trúc dự án

```
complete/
├── src/main/java/com/example/servingwebcontent/
│   ├── ServingWebContentApplication.java    # Main application
│   ├── Controller/
│   │   ├── EmailController.java             # REST API cho Email
│   │   ├── GraphController.java             # REST API cho Graph
│   │   ├── IPAddressController.java         # REST API cho IPAddress
│   │   └── UrlController.java               # REST API cho URL
│   ├── Service/
│   │   ├── EmailService.java                # Business logic Email
│   │   ├── GraphQueryService.java           # Cypher queries và phân tích
│   │   ├── IPAddressService.java            # Business logic IP
│   │   └── UrlService.java                  # Business logic URL
│   ├── Repository/
│   │   ├── EmailRepository.java             # Neo4j repository Email
│   │   ├── IPAddressRepository.java         # Neo4j repository IP
│   │   └── UrlRepository.java               # Neo4j repository URL
│   ├── config/
│   │   └── Neo4jTestConfig.java             # Cấu hình hỗ trợ test
│   └── model/
│       ├── Email.java                       # Entity Email
│       ├── IPAddress.java                   # Entity IPAddress
│       └── Url.java                         # Entity URL
└── src/main/resources/
    ├── application.properties               # Cấu hình ứng dụng
    ├── dl.md                                # Dataset Cypher (demo)
    └── static/
        ├── index.html                       # Frontend UI
        ├── report.html                      # Trang report
        ├── script.js                        # Logic UI (D3)
        ├── report.js                        # Logic report
        ├── theme.js                         # Toggle/theme
        └── style.css                        # Styling

```

Ghi chú: ngoài ra còn có package `validation/` (phục vụ kiểm tra input/ingest) trong `src/main/java/com/example/servingwebcontent/`.

## 🧪 Ví dụ sử dụng

### Tạo dữ liệu mẫu

```bash
# Tạo mối quan hệ Email -> URL -> IP
curl "http://localhost:8080/api/graph/link?email=phishing@spam.com&url=http://malicious-site.com&ip=192.168.1.100"

# Tạo thêm email khác cùng IP (shared IP - spam pattern)
curl "http://localhost:8080/api/graph/link?email=scam@spam.com&url=http://fake-bank.com&ip=192.168.1.100"
```

### Chạy phân tích

```bash
curl http://localhost:8080/api/graph/analyze
```

### Xem kết quả trên web

Truy cập `http://localhost:8080` để xem graph visualization với màu sắc thể hiện mức độ rủi ro.

## 📊 Phân tích rút ra từ đồ thị

Các phân tích minh họa tập trung vào **quan hệ** và **cấu trúc liên kết** (không nhằm thay thế hệ thống chống gian lận thực tế):

1. Đếm mức độ liên kết (degree) của Email/URL/IP
2. Phát hiện thực thể dùng chung (shared IP/shared URL)
3. Truy vết chuỗi quan hệ Email → URL → IP
4. Minh họa chấm điểm (rule-based) để trực quan hóa mức độ nghi ngờ

### Ưu điểm của Graph Database

- **Phát hiện mạng lưới**: Dễ dàng truy vấn các pattern phức tạp
- **Performance**: Truy vấn relationship nhanh hơn SQL JOIN
- **Mở rộng**: Dễ thêm node types và relationships mới
- **Trực quan**: Graph visualization giúp hiểu rõ mối liên hệ

## 👥 Nhóm thực hiện

- Sinh viên: Nguyễn Văn Thành-23010191
- Sinh viên: Hoàng Thị Ngọc-23010156
- Sinh viên: Cao Thanh Thảo-23010168
- Trường Công Nghệ Thông Tin

## 📝 License

MIT License

---

**Lưu ý**: Đây là project học tập. Phần chấm điểm/rủi ro được thiết kế ở mức đơn giản để minh họa cách khai thác Graph Database và các mối quan hệ.