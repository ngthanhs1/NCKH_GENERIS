# Phân tích rút ra từ đồ thị và các mối quan hệ (minh họa)

## 1. Mục tiêu của phần phân tích (đúng theo đề tài)

Phần này tập trung vào mục tiêu: **phân tích được rút ra từ đồ thị cụ thể và các mối quan hệ**. Cụ thể:
- Dựa trên nodes/relationships đã mô  hình hóa, rút ra các nhận xét/pattern liên quan bài toán gian lận.
- Truy vết theo chuỗi quan hệ để giải thích “vì sao” một thực thể đáng nghi.
- Dùng trực quan hóa để minh họa kết quả phân tích.

---

## 2. Đồ thị demo trong đồ án

### 2.1. Node
Trong phạm vi đồ án, đồ thị gồm 3 loại node:
- `Email`
- `URL`
- `IPAddress`

Mỗi node có các thuộc tính hỗ trợ trình bày và đánh giá:
- `value`: giá trị thực (email/url/ip)
- `riskScore` (0–100) *(được cập nhật bởi `GET /api/graph/analyze`)*
- `verdict` (SAFE/SUSPICIOUS/FRAUD/UNKNOWN) *(UNKNOWN khi chưa đủ triad hoặc chưa phân tích)*

Ghi chú:
- Trong API `GET /api/graph/full` (phục vụ vẽ D3), backend hiện trả tối thiểu các field: `id`, `label`, `type`, `riskScore`, `verdict` (và `layoutX/layoutY` nếu có).
- Các field như `riskLevel`, `indicators` (nếu cần) được trình bày rõ hơn ở nhóm API phân tích chi tiết `GET /api/graph/analysis/*`.

### 2.2. Relationship
Ba loại quan hệ chính:
- `(:Email)-[:CONTAINS_LINK]->(:URL)`
- `(:Email)-[:SENT_FROM]->(:IPAddress)`
- `(:URL)-[:HOSTED_BY]->(:IPAddress)`

Mẫu kịch bản cốt lõi:

```
Email  ──CONTAINS_LINK──>  URL  ──HOSTED_BY──>  IP
  │
  └────────────SENT_FROM──────────────────────> IP
```

---

## 2.3. Ví dụ đồ thị tối giản (1 Email – 1 URL – 1 IP)

Dựa trên dữ liệu demo tối giản (quan sát trên Neo4j Browser hoặc dữ liệu trả về từ API `GET /api/graph/full`), đồ thị có thể gồm:

- Email: `scam@test.com`
- URL: `http://fake-bank.com`
- IPAddress: `192.168.1.10`

Các node được định danh duy nhất và phản ánh đúng dữ liệu mà hệ thống đang xử lý trong môi trường demo.

### 2.3.1. Ý nghĩa của các mối quan hệ

| Quan hệ | Ý nghĩa |
|---|---|
| `(:Email)-[:CONTAINS_LINK]->(:URL)` | Email chứa liên kết |
| `(:Email)-[:SENT_FROM]->(:IPAddress)` | Email được gửi từ địa chỉ IP |
| `(:URL)-[:HOSTED_BY]->(:IPAddress)` | URL/website được host trên địa chỉ IP |

Ba mối quan hệ này tạo thành cấu trúc tam giác Email – URL – IPAddress, là mô hình cơ bản để mô tả chuỗi hành vi trong các kịch bản lừa đảo qua email.

---

## 3. Các pattern gian lận minh họa

### 3.1. Shared URL (phishing campaign)
Nhiều Email khác nhau cùng trỏ đến một URL:

```cypher
MATCH (u:URL)<-[:CONTAINS_LINK]-(e:Email)
RETURN u.value, count(e) AS emailCount
ORDER BY emailCount DESC;
```

Giải thích:
- Nếu một URL xuất hiện trong nhiều email, có khả năng đó là **chiến dịch** (campaign) hoặc link phát tán.

### 3.2. Shared IP (spam/botnet hạ tầng)
Nhiều Email được gửi từ cùng một IP:

```cypher
MATCH (i:IPAddress)<-[:SENT_FROM]-(e:Email)
RETURN i.value, count(e) AS emailCount
ORDER BY emailCount DESC;
```

Giải thích:
- IP gửi nhiều email có thể là dấu hiệu spam/botnet, hoặc một hạ tầng gửi hàng loạt.

### 3.3. Truy vết chuỗi Email → URL → IP

```cypher
MATCH (e:Email)-[:CONTAINS_LINK]->(u:URL)-[:HOSTED_BY]->(i:IPAddress)
RETURN e.value AS email, u.value AS url, i.value AS ip;
```

Giải thích:
- Truy vết này giúp trả lời: “Email này dẫn tới website nào và website đó nằm trên IP nào?”

---

## 3.4. Mẫu hành vi nền tảng: Email – URL – IP Triangle

Từ cấu trúc đồ thị, hệ thống có thể xác định mẫu hành vi cơ bản:

```
Email → URL → IP
  ↘──────────↗
```

Mẫu này phản ánh chuỗi hành vi thường gặp trong các kịch bản phishing. Tuy nhiên, mẫu chỉ thực sự mang tính “đáng nghi mạnh” khi các mối quan hệ này lặp lại với nhiều email hoặc nhiều URL (shared URL/shared IP).

### 3.5. Biểu đồ minh họa Pattern Detection (shared URL / shared IP / triad)

Dưới đây là một sơ đồ (mang tính minh họa) mô tả luồng phát hiện pattern theo đúng mô hình của đồ án. Mục tiêu là giải thích rõ: hệ thống **không chỉ nhìn một node đơn lẻ**, mà dựa vào **cấu trúc liên kết** và **tần suất dùng chung (shared)**.

```mermaid
flowchart TD
  A[Ingest dữ liệu: Email / URL / IP] --> B{Đã hình thành đủ tam giác?
  (Email-URL, Email-IP, URL-IP)}
  B -- Chưa đủ --> B1[Chỉ ghi nhận quan hệ hiện có\nChưa kết luận mạnh (thiếu ngữ cảnh)]
  B -- Đủ tam giác --> C[Trích xuất thống kê lân cận]

  C --> C1[sharedURL = số Email trỏ cùng URL]
  C --> C2[sharedIP = số Email gửi từ cùng IP]
  C --> C3[hostedURLs = số URL cùng host trên IP]

  C1 --> D{Pattern nổi bật?}
  C2 --> D
  C3 --> D

  D -- sharedURL ≥ 2 --> P1[Phishing campaign\n(nhiều Email → 1 URL)]
  D -- sharedIP ≥ 2 --> P2[Spam/Botnet infra\n(nhiều Email → 1 IP)]
  D -- hostedURLs ≥ 2 --> P3[Hosting hub\n(nhiều URL → 1 IP)]
  D -- Không --> P0[Chuỗi đơn lẻ\n(ít bằng chứng)]

  P1 --> E[Chấm điểm/đánh nhãn theo rule-based]
  P2 --> E
  P3 --> E
  P0 --> E

  E --> F[Hiển thị: tô màu node + lý do (indicators)]
```

Gợi ý cách “đọc” sơ đồ khi thuyết trình:
- **Triad (tam giác)** đóng vai trò “ngữ cảnh tối thiểu” để mô tả kịch bản Email → URL → IP.
- **Shared URL** là dấu hiệu mạnh cho “campaign” (cùng link phát tán).
- **Shared IP / hosted URLs** phản ánh “hạ tầng” (nguồn gửi/host tập trung).

---

## 4. Chấm điểm rủi ro (rule-based, phục vụ minh họa)

Đồ án sử dụng cách chấm điểm đơn giản để **minh họa** cách khai thác quan hệ trong Graph Database. Điểm số nhằm hỗ trợ trực quan hóa (màu sắc/nhãn), không thay thế kết luận nghiệp vụ.

### 4.0. Mô tả thuật toán áp dụng cho đề tài

**Input**: đồ thị gồm các node `Email`, `URL`, `IPAddress` và các quan hệ `CONTAINS_LINK`, `SENT_FROM`, `HOSTED_BY`.

**Ý tưởng chính**: thay vì đánh giá “một thực thể có gian lận hay không” chỉ bằng thuộc tính đơn lẻ, đồ án minh họa đánh giá dựa trên **cấu trúc liên kết** và **tần suất dùng chung** (shared) trong đồ thị.

**Luồng xử lý (end-to-end)**:
1) Người dùng thêm dữ liệu (tạo node/relationship) qua API ingest.
2) Khi chạy `GET /api/graph/analyze`, hệ thống tính điểm `riskScore` và gán `verdict` cho node rồi **lưu ngược vào Neo4j**.
3) UI gọi `GET /api/graph/full` để lấy graph và tô màu node theo `verdict`.

**Trạng thái màu xám (UNKNOWN)**:
- Node mới thêm **chưa chạy phân tích** hoặc **chưa có thuộc tính `verdict`** sẽ hiển thị **màu xám**.
- Sau khi chạy `analyze`, node sẽ đổi sang xanh/cam/đỏ tương ứng **nếu đã đủ ngữ cảnh triad**.
- Nếu node **chưa tham gia đủ bộ 3 (triad/tam giác Email–URL–IP)** thì hệ thống giữ `verdict=UNKNOWN` và `riskScore=0` để tránh kết luận khi thiếu ngữ cảnh.

### 4.1. Rule cho Email (0–100)
- Điều kiện áp dụng: Email phải tham gia đủ triad (tam giác Email–URL–IP). Nếu chưa đủ triad thì `riskScore=0` và `verdict=UNKNOWN`.
- +30: Email có chứa URL
- +20: Email có IP nguồn (`SENT_FROM`)
- +30: IP nguồn được dùng bởi > 1 email (**shared IP**)
- +30: URL xuất hiện trong > 1 email (**shared URL**)

Chuẩn hóa điểm: nếu tổng > 100 thì cắt về 100.

Phân loại verdict (đúng theo cách triển khai trong code):
- UNKNOWN: chưa đủ triad
- FRAUD: score ≥ 60
- SUSPICIOUS: 0 < score < 60
- SAFE: score = 0

### 4.2. Rule cho IPAddress và URL (minh họa)

Trong phần `analyze` tổng quan (phục vụ tô màu trực quan), đồ án dùng rule tối giản:
- **IPAddress**: nếu IP được > 1 Email sử dụng (nhiều quan hệ `SENT_FROM`) thì gán điểm 30 → `SUSPICIOUS`, ngược lại 0 → `SAFE`.
- **URL**: nếu URL xuất hiện trong > 1 Email (nhiều quan hệ `CONTAINS_LINK`) thì gán điểm 30 → `SUSPICIOUS`, ngược lại 0 → `SAFE`.

### 4.3. Áp dụng cho dữ liệu demo tối giản

Giả sử Email đã tham gia **đủ bộ 3** (tồn tại tam giác Email–URL–IP), hệ thống áp dụng luật chấm điểm rủi ro như sau:

| Điều kiện | Điểm |
|---|---:|
| Email có chứa URL | +30 |
| Email được gửi từ IP | +20 |
| IP gửi từ ≥ 2 email | +0 |
| URL xuất hiện trong ≥ 2 email | +0 |

👉 Tổng điểm rủi ro: **50**

Kết luận:
- Risk Score: **50**
- Verdict: **SUSPICIOUS (ĐÁNG NGHI)**

Kết quả này phản ánh đúng nguyên tắc của hệ thống: chưa kết luận gian lận khi dữ liệu chưa đủ lớn, đồng thời vẫn ghi nhận các dấu hiệu rủi ro ban đầu.

### 4.4. Ý nghĩa “đúng với đề tài”
- Đồ án không “kết tội” URL/IP chỉ dựa vào một đặc điểm.
- Điểm rủi ro được sinh ra từ **cấu trúc quan hệ trong đồ thị** (shared, campaign, hub).

---

## 5. Luồng demo đề xuất (đủ để thuyết trình)

1) Tạo dữ liệu theo chuỗi Email → URL → IP:
- Gọi `GET /api/graph/link?email=...&url=...&ip=...`

2) Tạo thêm vài email khác cùng URL hoặc cùng IP để hình thành pattern:
- Shared URL: nhiều email, cùng URL
- Shared IP: nhiều email, cùng IP

3) Chạy phân tích:
- Gọi `GET /api/graph/analyze`

4) Mở UI:
- Truy cập `/` để xem graph.
- Dùng filter theo verdict để minh họa node đổi màu theo kết quả phân tích.

---

## 6. Đánh giá và giới hạn

- Ưu điểm: minh họa rõ truy vấn pattern theo quan hệ; UI trực quan; mô hình dễ mở rộng thêm node/relationship.
- Giới hạn: scoring rule-based chỉ mang tính minh họa; thực tế có thể bổ sung dữ liệu (domain reputation, ASN, blacklist) và/hoặc ML.

---

## 7. Liên kết với hiện thực hóa trong code

- Backend truy vấn và phân tích: `GraphQueryService`.
- API chính cho frontend: `GET /api/graph/full`.
- UI trực quan hóa: `complete/src/main/resources/static/index.html` (D3.js).
