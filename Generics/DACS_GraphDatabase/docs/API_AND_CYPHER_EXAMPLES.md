# Tài liệu API và Ví dụ truy vấn Cypher

Tài liệu này được viết theo hướng **đọc phát hiểu ngay**: mỗi API nằm ở file nào, gọi service nào, ghi/đọc dữ liệu Neo4j ra sao.

---

## 0. Luồng xử lý API (cực ngắn)

Frontend/Client → Controller (Spring MVC) → Service → `Neo4jClient` chạy Cypher → trả JSON (hoặc String/Entity) về client.

Trong project này:
- API tổng hợp và phân tích graph tập trung ở GraphController + GraphQueryService.
- API CRUD “nhỏ” cho từng loại node nằm ở EmailController/UrlController/IPAddressController.

---

## 0.1. Bản đồ endpoint → file code

### Nhóm API Graph (UI dùng nhiều)

- [complete/src/main/java/com/example/servingwebcontent/Controller/GraphController.java](../complete/src/main/java/com/example/servingwebcontent/Controller/GraphController.java)
- [complete/src/main/java/com/example/servingwebcontent/Service/GraphQueryService.java](../complete/src/main/java/com/example/servingwebcontent/Service/GraphQueryService.java)

### Nhóm API CRUD theo thực thể

- [complete/src/main/java/com/example/servingwebcontent/Controller/EmailController.java](../complete/src/main/java/com/example/servingwebcontent/Controller/EmailController.java)
- [complete/src/main/java/com/example/servingwebcontent/Controller/UrlController.java](../complete/src/main/java/com/example/servingwebcontent/Controller/UrlController.java)
- [complete/src/main/java/com/example/servingwebcontent/Controller/IPAddressController.java](../complete/src/main/java/com/example/servingwebcontent/Controller/IPAddressController.java)

## 📚 Mục lục
1. [REST API Endpoints](#rest-api-endpoints)
2. [Ví dụ truy vấn Cypher](#ví-dụ-truy-vấn-cypher)
3. [Use Cases thực tế](#use-cases-thực-tế)

---

## REST API Endpoints

Ghi chú nhanh:
- Các API dưới đây đa phần nhận tham số bằng `@RequestParam` (query string hoặc form).
- Các API “graph tổng hợp” thường trả `Map<String, Object>` (JSON linh hoạt).
- Tạo dữ liệu dùng `MERGE` để tránh tạo trùng node/relationship.

### 1. Health Check & Statistics

#### GET `/api/graph/health`

Kiểm tra kết nối Neo4j và thống kê số lượng nodes/relationships.

**Response**:
```json
{
  "status": "connected",
  "ping": { "ok": 1 },
  "counts": {
    "Email": 5,
    "URL": 3,
    "IPAddress": 2,
    "relationships": 9
  }
}
```

**Curl Example**:
```bash
curl http://localhost:8080/api/graph/health
```

**Code chạy ở đâu?**
- Controller: GraphController `health()`
- Service: GraphQueryService `health()` (Cypher đếm Email/URL/IP + relationships)

---

### 2. Tạo dữ liệu Graph

#### GET/POST `/api/graph/link`

Tạo Email, URL, IPAddress và các relationships giữa chúng.

**Parameters**:
- `email`: Địa chỉ email (required)
- `url`: URL trong email (required)
- `ip`: Địa chỉ IP gửi email (required)

**Response**: `"Graph linked OK: email → url → ip"`

**Curl Examples**:
```bash
# Tạo dữ liệu phishing đơn
curl "http://localhost:8080/api/graph/link?email=scam@test.com&url=http://fake-bank.com&ip=192.168.1.10"

# Tạo campaign với shared IP
curl "http://localhost:8080/api/graph/link?email=phishing@spam.com&url=http://malicious-site.com&ip=192.168.1.10"

# Tạo campaign với shared URL
curl "http://localhost:8080/api/graph/link?email=scam2@test.com&url=http://fake-bank.com&ip=192.168.1.11"
```

**Code chạy ở đâu?**
- Controller: GraphController `linkGet()` (GET) và `link()` (POST)
- Service: GraphQueryService `linkEmailUrlIp(email,url,ip)`
- Cypher: `MERGE` 3 node + `MERGE` 3 quan hệ (CONTAINS_LINK, SENT_FROM, HOSTED_BY)

---

#### GET `/api/graph/link-partial`

Tạo node/relationship với input **không bắt buộc đủ 3**.

- Nhập 1 field: chỉ tạo node
- Nhập 2 field: tạo node + quan hệ tương ứng
- Nhập đủ 3: tạo full chain Email → URL → IP

**Ví dụ**:
```bash
# Chỉ tạo Email
curl "http://localhost:8080/api/graph/link-partial?email=a@test.com"

# Tạo Email + IP + quan hệ SENT_FROM
curl "http://localhost:8080/api/graph/link-partial?email=a@test.com&ip=10.0.0.9"
```

**Code chạy ở đâu?**
- Controller: GraphController `linkPartial(...)`
- Service: GraphQueryService `linkPartial(email,url,ip)`
- Cypher: dùng `FOREACH ... CASE WHEN` để MERGE có điều kiện (tránh phải build query bằng string)

---

#### DELETE `/api/graph/node`

Xóa 1 node theo `id` (xóa kèm toàn bộ quan hệ bằng `DETACH DELETE`).

**Ví dụ**:
```bash
curl -X DELETE "http://localhost:8080/api/graph/node?id=Email:scam@test.com"
```

**Code chạy ở đâu?**
- Controller: GraphController `deleteNode(id)`
- Service: GraphQueryService `deleteNodeById(id)`

---

### 3. Phân tích rủi ro

#### GET `/api/graph/analyze`

Chạy phân tích rủi ro toàn bộ graph và cập nhật riskScore, verdict cho tất cả nodes.

**Response**:
```json
[
  {
    "email": "Email:scam@test.com",
    "score": 80,
    "verdict": "FRAUD",
    "hasTriad": true
  },
  {
    "email": "Email:orphan@gmail.com",
    "score": 0,
    "verdict": "UNKNOWN",
    "hasTriad": false
  }
]
```

**Curl Example**:
```bash
curl http://localhost:8080/api/graph/analyze
```

**Code chạy ở đâu?**
- Controller: GraphController `analyze()`
- Service: GraphQueryService `analyzeRisk()`

Giải thích đúng bản chất:
- API này **update trực tiếp** thuộc tính `riskScore` + `verdict` trên các node trong Neo4j.
- Trả về danh sách kết quả theo Email để bạn nhìn nhanh.
- Quy ước triển khai trong code: nếu Email **chưa tham gia đủ triad (tam giác Email-URL-IP)** thì `hasTriad=false`, `score=0` và `verdict=UNKNOWN`.

---

#### GET `/api/graph/analysis/email-risk`

Phân tích chi tiết rủi ro cho Email nodes.

**Response**:
```json
[
  {
    "email": "scam@test.com",
    "urlCount": 2,
    "ipCount": 1,
    "riskScore": 4,
    "riskLevel": "HIGH",
    "verdict": "GIAN LẬN",
    "indicators": ["EMAIL_TRO_TOI_NHIEU_URL", "MUC_DO_LIEN_KET_CAO"]
  }
]
```

---

#### GET `/api/graph/analysis/url-risk`

Phân tích chi tiết rủi ro cho URL nodes.

**Response**:
```json
[
  {
    "url": "http://fake-bank.com",
    "emailCount": 3,
    "riskScore": 3,
    "riskLevel": "MEDIUM",
    "verdict": "ĐÁNG NGHI NGỜ",
    "indicators": ["URL_DUOC_NHIEU_EMAIL_TRO_TOI"]
  }
]
```

---

#### GET `/api/graph/analysis/ip-risk`

Phân tích chi tiết rủi ro cho IPAddress nodes.

**Response**:
```json
[
  {
    "ip": "192.168.1.10",
    "emailCount": 2,
    "urlCount": 1,
    "riskScore": 3,
    "riskLevel": "MEDIUM",
    "verdict": "ĐÁNG NGHI NGỜ",
    "indicators": ["IP_DUNG_BOI_NHIEU_EMAIL"]
  }
]
```

---

### 4. Visualization Data

#### GET `/api/graph/full`

Lấy toàn bộ graph data để hiển thị trên frontend.

**Response**:
```json
{
  "nodes": [
    {
      "id": "Email:scam@test.com",
      "label": "scam@test.com",
      "type": ["Email"],
      "riskScore": 80,
      "verdict": "FRAUD"
    },
    {
      "id": "URL:http://fake-bank.com",
      "label": "http://fake-bank.com",
      "type": ["URL"],
      "riskScore": 30,
      "verdict": "SUSPICIOUS"
    }
  ],
  "links": [
    {
      "source": "Email:scam@test.com",
      "target": "URL:http://fake-bank.com",
      "type": "CONTAINS_LINK"
    }
  ]
}
```

**Code chạy ở đâu?**
- Controller: GraphController `fullGraph()`
- Service: GraphQueryService `getFullGraph()`

Lưu ý quan trọng: `getFullGraph()` hiện lấy dữ liệu bằng Cypher `MATCH (a)-[r]->(b)` nên chỉ trả về nodes/cạnh có nằm trong ít nhất 1 relationship.

---

#### POST `/api/graph/layout/save`

Lưu tọa độ layout (vị trí node sau khi kéo thả trên UI) vào properties của node (`layoutX`, `layoutY`).

**Request Body** (JSON array):
```json
[
  {"id": "Email:scam@test.com", "x": 120.5, "y": 340.2},
  {"id": "URL:http://fake-bank.com", "x": 520.0, "y": 210.0}
]
```

**Response**:
```json
{"status": "ok", "saved": 2}
```

**Code chạy ở đâu?**
- Controller: GraphController `saveLayout(...)`
- Service: GraphQueryService `saveLayout(nodes)`

---

## 5. CRUD nhanh theo thực thể (Email/URL/IP)

Nhóm này hữu ích khi bạn muốn tạo/lấy node đơn lẻ; UI demo thường dùng nhóm `/api/graph/*` nhiều hơn.

### 5.1. Email API

- `GET /api/email/health` → kiểm tra kết nối Neo4j qua EmailService
- `POST /api/email?value=...` → tạo Email node
- `POST /api/email/with-url?email=...&url=...` → tạo Email + gắn URL

Code:
- Controller: [complete/src/main/java/com/example/servingwebcontent/Controller/EmailController.java](../complete/src/main/java/com/example/servingwebcontent/Controller/EmailController.java)

### 5.2. URL API

- `POST /api/url?value=...` → tạo (hoặc lấy) URL
- `GET /api/url?value=...` → lấy URL theo value
- `GET /api/url/all` → lấy tất cả URL
- `GET /api/url/risk` → phân tích rủi ro URL (gọi GraphQueryService)

Code:
- Controller: [complete/src/main/java/com/example/servingwebcontent/Controller/UrlController.java](../complete/src/main/java/com/example/servingwebcontent/Controller/UrlController.java)

### 5.3. IPAddress API

- `POST /api/ip?value=...` → tạo (hoặc lấy) IP
- `GET /api/ip?value=...` → lấy IP theo value
- `GET /api/ip/all` → lấy tất cả IP
- `GET /api/ip/risk` → phân tích rủi ro IP (gọi GraphQueryService)

Code:
- Controller: [complete/src/main/java/com/example/servingwebcontent/Controller/IPAddressController.java](../complete/src/main/java/com/example/servingwebcontent/Controller/IPAddressController.java)

---

## Ví dụ truy vấn Cypher

### 1. Tạo dữ liệu (Create)

#### Tạo Email node
```cypher
CREATE (e:Email {
  id: 'Email:scam@test.com',
  value: 'scam@test.com',
  status: 'suspicious',
  riskScore: 0,
  verdict: 'UNKNOWN'
})
RETURN e;
```

#### Tạo chuỗi Email → URL → IP
```cypher
MERGE (e:Email {id: 'Email:scam@test.com', value: 'scam@test.com'})
MERGE (u:URL {id: 'URL:http://fake-bank.com', value: 'http://fake-bank.com'})
MERGE (i:IPAddress {id: 'IPAddress:192.168.1.10', value: '192.168.1.10'})

MERGE (e)-[:CONTAINS_LINK]->(u)
MERGE (e)-[:SENT_FROM]->(i)
MERGE (u)-[:HOSTED_BY]->(i)

RETURN e, u, i;
```

---

### 2. Truy vấn cơ bản (Read)

#### Tìm tất cả Email
```cypher
MATCH (e:Email)
RETURN e.value, e.riskScore, e.verdict
ORDER BY e.riskScore DESC;
```

#### Tìm Email có verdict = FRAUD
```cypher
MATCH (e:Email)
WHERE e.verdict = 'FRAUD'
RETURN e.value, e.riskScore;
```

#### Đếm số nodes theo loại
```cypher
MATCH (n)
RETURN labels(n) AS type, count(n) AS count;
```

---

### 3. Phân tích quan hệ (Relationships)

#### Tìm URL được nhiều email trỏ tới (Phishing Campaign)
```cypher
MATCH (u:URL)<-[:CONTAINS_LINK]-(e:Email)
WITH u, count(e) AS emailCount, collect(e.value) AS emails
WHERE emailCount > 1
RETURN u.value AS suspiciousURL, emailCount, emails
ORDER BY emailCount DESC;
```

#### Tìm IP gửi nhiều email (Spam/Botnet)
```cypher
MATCH (i:IPAddress)<-[:SENT_FROM]-(e:Email)
WITH i, count(e) AS emailCount, collect(e.value) AS emails
WHERE emailCount > 1
RETURN i.value AS suspiciousIP, emailCount, emails
ORDER BY emailCount DESC;
```

#### Truy vết chuỗi Email → URL → IP
```cypher
MATCH path = (e:Email)-[:CONTAINS_LINK]->(u:URL)-[:HOSTED_BY]->(i:IPAddress)
RETURN e.value AS email, u.value AS url, i.value AS ip;
```

---

### 4. Phát hiện Pattern

#### Pattern: Shared IP (IP Hub)
```cypher
MATCH (i:IPAddress)<-[:SENT_FROM]-(e:Email)
WITH i, collect(e) AS emails
WHERE size(emails) >= 2
RETURN i.value AS ipHub, 
       size(emails) AS emailCount,
       [e IN emails | e.value] AS emailList;
```

#### Pattern: Shared URL (Campaign)
```cypher
MATCH (u:URL)<-[:CONTAINS_LINK]-(e:Email)
WITH u, collect(e) AS emails
WHERE size(emails) >= 2
RETURN u.value AS campaignURL,
       size(emails) AS emailCount,
       [e IN emails | e.value] AS emailList;
```

#### Pattern: Triangle (Email-URL-IP cùng hạ tầng)
```cypher
MATCH (e:Email)-[:CONTAINS_LINK]->(u:URL)-[:HOSTED_BY]->(i:IPAddress),
      (e)-[:SENT_FROM]->(i)
RETURN e.value AS email, 
       u.value AS url, 
       i.value AS ip,
       'TRIANGLE_PATTERN' AS pattern;
```

---

### 5. Tính điểm rủi ro (Risk Scoring)

#### Tính điểm cho Email
```cypher
MATCH (e:Email)
OPTIONAL MATCH (e)-[:CONTAINS_LINK]->(u:URL)
OPTIONAL MATCH (e)-[:SENT_FROM]->(i:IPAddress)
WITH e, collect(DISTINCT u) AS urls, collect(DISTINCT i) AS ips
WITH e,
    CASE WHEN size(urls) > 0 THEN 30 ELSE 0 END AS scoreHasLink,
    CASE WHEN size(ips) > 0 THEN 20 ELSE 0 END AS scoreHasIp,
    CASE WHEN any(ip IN ips WHERE size((ip)<-[:SENT_FROM]-(:Email)) > 1) THEN 30 ELSE 0 END AS scoreIpShared,
    CASE WHEN any(url IN urls WHERE size((url)<-[:CONTAINS_LINK]-(:Email)) > 1) THEN 30 ELSE 0 END AS scoreUrlShared
WITH e, (scoreHasLink + scoreHasIp + scoreIpShared + scoreUrlShared) AS score
SET e.riskScore = score
SET e.verdict = 
    CASE
        WHEN score >= 60 THEN 'FRAUD'
        WHEN score >= 30 THEN 'SUSPICIOUS'
        ELSE 'SAFE'
    END
RETURN e.value, e.riskScore, e.verdict
ORDER BY e.riskScore DESC;
```

---

### 6. Cập nhật và Xóa (Update & Delete)

#### Cập nhật verdict cho một Email
```cypher
MATCH (e:Email {id: 'Email:scam@test.com'})
SET e.verdict = 'FRAUD', e.riskScore = 100
RETURN e;
```

#### Xóa một Email và relationships
```cypher
MATCH (e:Email {id: 'Email:scam@test.com'})
DETACH DELETE e;
```

#### Xóa toàn bộ graph (Cẩn thận!)
```cypher
MATCH (n)
DETACH DELETE n;
```

---

## Use Cases thực tế

### Use Case 1: Phát hiện Phishing Campaign

**Kịch bản**: Tìm các URL được sử dụng trong nhiều email khác nhau (dấu hiệu phishing campaign).

**Truy vấn**:
```cypher
MATCH (u:URL)<-[:CONTAINS_LINK]-(e:Email)
WITH u, count(e) AS emailCount, collect(e.value) AS emails
WHERE emailCount >= 3
RETURN u.value AS phishingURL,
       emailCount,
       emails,
       'PHISHING_CAMPAIGN' AS pattern
ORDER BY emailCount DESC;
```

**Kết quả mong đợi**:
```
phishingURL              | emailCount | emails                    | pattern
http://fake-paypal.com  | 5          | [scam1@..., scam2@...]   | PHISHING_CAMPAIGN
```

---

### Use Case 2: Phát hiện Spam Botnet

**Kịch bản**: Tìm IP gửi nhiều email với nội dung khác nhau.

**Truy vấn**:
```cypher
MATCH (i:IPAddress)<-[:SENT_FROM]-(e:Email)
WITH i, count(e) AS emailCount, collect(e.value) AS emails
WHERE emailCount >= 5
RETURN i.value AS botnetIP,
       emailCount,
       emails,
       'SPAM_BOTNET' AS pattern
ORDER BY emailCount DESC;
```

---

### Use Case 3: Truy vết nguồn gốc Email

**Kịch bản**: Khi nhận được email đáng nghi, truy vết toàn bộ chuỗi hành vi.

**Truy vấn**:
```cypher
MATCH path = (e:Email {value: 'scam@test.com'})-[*1..3]-(related)
RETURN path;
```

Hoặc chi tiết hơn:
```cypher
MATCH (e:Email {value: 'scam@test.com'})
OPTIONAL MATCH (e)-[:CONTAINS_LINK]->(u:URL)
OPTIONAL MATCH (e)-[:SENT_FROM]->(ip:IPAddress)
OPTIONAL MATCH (u)-[:HOSTED_BY]->(hostIP:IPAddress)
RETURN e.value AS email,
       collect(DISTINCT u.value) AS urls,
       collect(DISTINCT ip.value) AS senderIPs,
       collect(DISTINCT hostIP.value) AS hostingIPs;
```

---

### Use Case 4: Tìm Email có rủi ro cao nhất

**Truy vấn**:
```cypher
MATCH (e:Email)
WHERE e.riskScore >= 60
RETURN e.value AS email,
       e.riskScore AS score,
       e.verdict AS verdict
ORDER BY e.riskScore DESC
LIMIT 10;
```

---

### Use Case 5: Thống kê theo Verdict

**Truy vấn**:
```cypher
MATCH (e:Email)
RETURN e.verdict AS verdict,
       count(e) AS count,
       round(avg(e.riskScore)) AS avgScore
ORDER BY count DESC;
```

**Kết quả**:
```
verdict      | count | avgScore
FRAUD        | 12    | 75
SUSPICIOUS   | 8     | 45
SAFE         | 20    | 15
```

---

## Tổng kết

Tài liệu này cung cấp:

✅ **REST API đầy đủ** cho tương tác với hệ thống

✅ **Các truy vấn Cypher mẫu** từ cơ bản đến nâng cao

✅ **Use cases thực tế** cho bài toán phát hiện gian lận

✅ **Best practices** khi làm việc với Graph Database

Sử dụng tài liệu này như reference khi phát triển và demo ứng dụng!
