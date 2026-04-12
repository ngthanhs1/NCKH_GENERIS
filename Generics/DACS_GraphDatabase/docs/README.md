# Tài liệu đề tài: Xây dựng và tìm hiểu CSDL đồ thị và minh họa cho bài toán cụ thể

## 📋 Thông tin đề tài

**Tên đề tài**: Xây dựng và tìm hiểu CSDL đồ thị và minh họa cho bài toán cụ thể

**Bài toán minh họa được lựa chọn**: Phát hiện gian lận/phishing (dựa trên liên kết Email – URL – IPAddress)

**Môn học**: Đảm bảo an toàn cơ sở dữ liệu (DACS)

**Công nghệ**: 
- Backend: Spring Boot (Java)
- Database: Neo4j (Graph Database)
- Frontend: HTML5, CSS3, JavaScript (D3.js)

---

## 📚 Cấu trúc tài liệu

Tài liệu đề tài được tổ chức thành các phần sau:

### 1. [Lý thuyết Graph Database](GRAPH_DATABASE_THEORY.md)
- Lý thuyết đồ thị cơ bản
- Khái niệm và đặc điểm Graph Database
- So sánh với CSDL quan hệ
- Mô hình Property Graph
- Neo4j và ngôn ngữ Cypher

### 2. [Mô hình dữ liệu phát hiện gian lận](FRAUD_DETECTION_MODEL.md)
- Lý do chọn mô hình đồ thị
- Xác định thực thể (Email, URL, IPAddress)
- Xác định quan hệ (CONTAINS_LINK, SENT_FROM, HOSTED_BY)
- Chuẩn hóa định danh
- Các truy vấn mẫu

### 3. [Phân tích đồ thị và đánh giá rủi ro](GRAPH_ANALYSIS.md)
- Cấu trúc đồ thị demo
- Phân tích hành vi dựa trên quan hệ
- Phát hiện pattern gian lận
- Thuật toán chấm điểm rủi ro
- So sánh với RDBMS

### 4. [API và Ví dụ Cypher](API_AND_CYPHER_EXAMPLES.md)
- REST API endpoints
- Ví dụ truy vấn Cypher từ cơ bản đến nâng cao
- Use cases thực tế
- Best practices

### 5. [Kịch bản demo / thuyết trình](DEMO_SCRIPT.md)
- Checklist tạo dữ liệu
- Luồng chạy phân tích
- Cách trình bày UI và kết luận

---

## 🎯 Mục tiêu đề tài (chuẩn theo đề bài)

1. **Phân tích và tìm hiểu CSDL đồ thị (Graph Database)**
2. **Xây dựng mô hình dữ liệu đồ thị cho bài toán được lựa chọn** (nodes, mối quan hệ) – bài toán phát hiện gian lận
3. **Rút ra phân tích từ đồ thị cụ thể và các mối quan hệ** (pattern, truy vết, thống kê theo liên kết)

## ✅ Kết quả mong đợi

Một **ứng dụng minh họa trực quan** các đối tượng và mối quan hệ trong bài toán cụ thể:
- Dữ liệu được lưu trong Neo4j dưới dạng nodes/relationships
- Truy vấn và minh họa các pattern liên quan gian lận
- Giao diện web hiển thị trực quan đồ thị và tương tác

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (Browser)                 │
│  ┌─────────────────────────────────────────────┐   │
│  │  HTML + CSS + JavaScript (D3.js)            │   │
│  │  - Graph Visualization                       │   │
│  │  - Risk Analysis Display                     │   │
│  │  - Filter & Interaction                      │   │
│  └─────────────────────────────────────────────┘   │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP REST API
                   ▼
┌─────────────────────────────────────────────────────┐
│            Backend (Spring Boot)                     │
│  ┌──────────────────┐    ┌──────────────────┐      │
│  │   Controllers    │    │    Services      │      │
│  │  - GraphCtrl     │───▶│  - GraphQuery    │      │
│  │  - EmailCtrl     │    │  - EmailSvc      │      │
│  └──────────────────┘    └──────────────────┘      │
│           │                        │                 │
│           │                        ▼                 │
│           │              ┌──────────────────┐       │
│           │              │   Repositories   │       │
│           └─────────────▶│  - EmailRepo     │       │
│                          │  - UrlRepo       │       │
│                          │  - IPRepo        │       │
│                          └──────────────────┘       │
└──────────────────┬──────────────────────────────────┘
                   │ Neo4j Driver
                   ▼
┌─────────────────────────────────────────────────────┐
│              Neo4j Graph Database                    │
│  ┌─────────────────────────────────────────────┐   │
│  │  Nodes: Email, URL, IPAddress               │   │
│  │  Relationships: CONTAINS_LINK, SENT_FROM,   │   │
│  │                 HOSTED_BY                    │   │
│  │  Properties: riskScore, verdict, ...        │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

## 📊 Mô hình dữ liệu

### Node Types

| Node Type | ID Format | Properties |
|---|---|---|
| Email | `Email:<value>` | value, riskScore, verdict *(có thể có thêm: status, riskLevel, indicators)* |
| URL | `URL:<value>` | value, riskScore, verdict *(có thể có thêm: status, riskLevel, indicators)* |
| IPAddress | `IPAddress:<value>` | value, riskScore, verdict *(có thể có thêm: status, riskLevel, indicators)* |

### Relationship Types

| Relationship | Direction | Meaning |
|--------------|-----------|---------|
| CONTAINS_LINK | Email → URL | Email chứa URL |
| SENT_FROM | Email → IPAddress | Email gửi từ IP |
| HOSTED_BY | URL → IPAddress | URL host trên IP |

### Graph Pattern

```
    ┌──────────┐
    │  Email   │
    └─────┬────┘
          │ CONTAINS_LINK
          ▼
    ┌──────────┐
    │   URL    │
    └─────┬────┘
          │ HOSTED_BY
          ▼
  ┌──────────────┐
  │  IPAddress   │
  └──────────────┘
          ▲
          │ SENT_FROM
    ┌─────┴────┐
    │  Email   │
    └──────────┘
```

---

## 🔍 Thuật toán phát hiện gian lận

### Bảng chấm điểm (Risk Scoring)

| Điều kiện | Điểm | Giải thích |
|-----------|------|------------|
| Email có chứa URL | +30 | Email phishing thường chứa link |
| Email có IP gửi | +20 | Có thể truy vết nguồn gốc |
| IP gửi ≥ 2 email | +30 | Dấu hiệu spam/botnet |
| URL trong ≥ 2 email | +30 | Dấu hiệu phishing campaign |

### Phân loại Verdict

Ghi chú triển khai:
- Nếu một node **chưa tham gia đủ triad (tam giác Email–URL–IP)** thì hệ thống giữ `verdict=UNKNOWN` và `riskScore=0`.
- Khi đã đủ triad, verdict được gán theo `riskScore`.

| Điều kiện | Verdict | Màu sắc | Ý nghĩa |
|---|---|---|---|
| Chưa đủ triad | UNKNOWN | Xám | Thiếu ngữ cảnh để kết luận |
| score ≥ 60 | FRAUD | Đỏ | Gian lận |
| 0 < score < 60 | SUSPICIOUS | Cam | Cần theo dõi |
| score = 0 | SAFE | Xanh | Không đáng nghi |

### Ví dụ minh họa

**Kịch bản 1**: Email đơn lẻ
```
Email: scam@test.com
URL: http://fake-bank.com  
IP: 192.168.1.10

Score = 30 (URL) + 20 (IP) = 50 → SUSPICIOUS
```

**Kịch bản 2**: Phishing campaign (shared URL)
```
Email1: scam1@test.com ──┐
Email2: scam2@test.com ──┼──> http://fake-bank.com
Email3: scam3@test.com ──┘

Score = 30 + 20 + 30 (shared URL) = 80 → FRAUD
```

---

## 🎨 Giao diện người dùng

### Trang chính (index.html)

1. **Header**: Tiêu đề và status kết nối Neo4j
2. **Legend**: Chú thích màu sắc verdict
3. **Filters**: Lọc theo node type và verdict
4. **Graph Canvas**: Hiển thị đồ thị D3.js force-directed
5. **Detail Panel**: Chi tiết node khi hover/click

### Tính năng tương tác

- ✅ Hover node: Xem thông tin nhanh
- ✅ Click node: Ghim thông tin chi tiết
- ✅ ESC: Bỏ ghim
- ✅ Filter: Ẩn/hiện nodes theo loại và verdict
- ✅ Force simulation: Layout tự động theo verdict

---

## 🚀 Hướng dẫn sử dụng

### Cài đặt và chạy

1. **Clone repository**:
```bash
git clone https://github.com/ngthanhs1/DACS_GraphDatabase.git
cd DACS_GraphDatabase/complete
```

2. **Cấu hình Neo4j** trong `application.properties`

3. **Chạy ứng dụng**:
```bash
./mvnw spring-boot:run
```

4. **Truy cập**: http://localhost:8080

### Tạo dữ liệu demo

```bash
# Email đơn
curl "http://localhost:8080/api/graph/link?email=scam@test.com&url=http://fake-bank.com&ip=192.168.1.10"

# Phishing campaign
curl "http://localhost:8080/api/graph/link?email=phishing@spam.com&url=http://fake-bank.com&ip=192.168.1.100"

# Chạy phân tích
curl http://localhost:8080/api/graph/analyze
```

---

## 📈 Kết quả đạt được

### Về mặt kỹ thuật
✅ Xây dựng thành công ứng dụng web hoàn chỉnh

✅ Tích hợp Neo4j Graph Database

✅ Phát triển thuật toán chấm điểm rủi ro

✅ Visualization graph trực quan với D3.js

✅ REST API đầy đủ cho CRUD và phân tích

### Về mặt học thuật
✅ Hiểu rõ lý thuyết Graph Database

✅ Nắm vững ngôn ngữ Cypher

✅ Phân tích ưu điểm Graph DB so với RDBMS

✅ Áp dụng vào bài toán thực tế

### Về mặt ứng dụng
✅ Phát hiện shared IP/URL (spam/phishing campaign)

✅ Truy vết chuỗi hành vi Email → URL → IP

✅ Tự động đánh giá rủi ro dựa trên pattern

✅ Hiển thị trực quan kết quả phân tích

---

## 🔮 Hướng phát triển

### Ngắn hạn
- Thêm authentication/authorization
- Tích hợp API kiểm tra URL (Google Safe Browsing, VirusTotal)
- Thêm domain analysis
- Export báo cáo PDF/Excel

### Dài hạn
- Machine Learning để phát hiện gian lận
- Real-time detection với Kafka/RabbitMQ
- Dashboard analytics với Grafana
- Hệ thống cảnh báo tự động
- Mobile app

---

## 📚 Tài liệu tham khảo

1. Neo4j Documentation: https://neo4j.com/docs/
2. Spring Data Neo4j: https://spring.io/projects/spring-data-neo4j
3. D3.js Force-Directed Graph: https://d3js.org/
4. Graph Algorithms for Data Science: O'Reilly
5. Fraud Detection with Graph Analytics: IEEE Papers

---

## 👥 Thông tin liên hệ

**Repository**: https://github.com/ngthanhs1/DACS_GraphDatabase

**Issues**: https://github.com/ngthanhs1/DACS_GraphDatabase/issues

---

**Lưu ý**: Đây là project học tập minh họa. Trong thực tế, phát hiện gian lận cần kết hợp nhiều yếu tố phức tạp hơn như Machine Learning, threat intelligence, behavior analysis, etc.
