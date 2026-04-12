# Xây dựng mô hình dữ liệu đồ thị cho bài toán phát hiện gian lận

## 1. Lý do chọn mô hình đồ thị cho bài toán gian lận

Bài toán gian lận (phishing/scam) thường không xuất phát từ một dấu hiệu đơn lẻ như một email lạ, một URL lạ hay một địa chỉ IP bất thường, mà hình thành từ **chuỗi liên kết hành vi giữa nhiều thực thể**. Vì vậy, mô hình đồ thị đặc biệt phù hợp cho bài toán này do các lý do sau:

- **Quan hệ là trung tâm**: Gian lận thường hình thành theo kịch bản *Email → URL → IP*, và có thể mở rộng thêm nhiều bước khác.
- **Khả năng truy vết mạnh**: Chỉ với một truy vấn, hệ thống có thể đi qua nhiều quan hệ để tìm nguồn gốc hoặc xác định các cụm chiến dịch.
- **Mở rộng linh hoạt**: Khi có thêm dữ liệu (thiết bị, người nhận, domain, ASN…), chỉ cần bổ sung node và relationship mà không cần thiết kế lại mô hình như trong CSDL quan hệ.

---

## 2. Xác định thực thể (Nodes) trong mô hình

Trong phạm vi project hiện tại, mô hình dữ liệu đồ thị gồm ba loại node chính.

### 2.1. Node Email

- **Ý nghĩa**: Đại diện cho danh tính gửi (sender) hoặc nguồn email đáng nghi.
- **Vai trò trong gian lận**: Là điểm khởi đầu của kịch bản phishing, nơi kẻ tấn công "dẫn dụ" người dùng.

**Thuộc tính tiêu biểu** (mô tả báo cáo):

- `id`: định danh duy nhất, dạng `Email:<value>`
- `value`: địa chỉ email
- `riskScore`, `riskLevel`, `verdict`: phục vụ minh họa mức độ nghi ngờ
- `indicators`: danh sách dấu hiệu (nếu có)

---

### 2.2. Node URL

- **Ý nghĩa**: Liên kết xuất hiện trong email, là nơi người dùng bị dẫn tới.
- **Vai trò trong gian lận**: Thường là trang giả mạo (phishing page) hoặc trang trung gian chuyển hướng.

**Thuộc tính tiêu biểu** (mô tả báo cáo):

- `id`: định danh duy nhất, dạng `URL:<value>`
- `value`: địa chỉ URL
- `riskScore`, `riskLevel`, `verdict`, `indicators`

---

### 2.3. Node IPAddress

- **Ý nghĩa**: Thực thể hạ tầng mạng liên quan đến việc gửi email hoặc lưu trữ website.
- **Vai trò trong gian lận**: Không dùng để "kết tội IP", mà xem IP như một thành phần hạ tầng giúp xác định chiến dịch.

**Thuộc tính tiêu biểu** (mô tả báo cáo):

- `id`: định danh duy nhất, dạng `IPAddress:<value>`
- `value`: địa chỉ IP
- `riskScore`, `riskLevel`, `verdict`, `indicators`

---

## 3. Xác định quan hệ (Relationships) trong mô hình

Các mối quan hệ được thiết kế nhằm phản ánh đúng chuỗi hành vi trong các kịch bản scam/phishing.

### 3.1. CONTAINS_LINK (Email → URL)

**Biểu diễn**:
```cypher
(:Email)-[:CONTAINS_LINK]->(:URL)
```

- **Ý nghĩa**: Email có chứa liên kết URL.
- **Giá trị phân tích**: Giúp phát hiện URL nào được nhiều email "nhúng" vào, từ đó nhận diện dấu hiệu chiến dịch phishing.

**Ví dụ truy vấn**:
```cypher
// Tìm URL xuất hiện trong nhiều email (phishing campaign)
MATCH (u:URL)<-[:CONTAINS_LINK]-(e:Email)
RETURN u.value, count(e) AS emailCount
ORDER BY emailCount DESC;
```

---

### 3.2. SENT_FROM (Email → IPAddress)

**Biểu diễn**:
```cypher
(:Email)-[:SENT_FROM]->(:IPAddress)
```

- **Ý nghĩa**: Email được gửi từ một địa chỉ IP.
- **Giá trị phân tích**: IP nào gửi nhiều email có thể là dấu hiệu của spam, botnet hoặc hạ tầng đáng nghi.

**Ví dụ truy vấn**:
```cypher
// Tìm IP gửi nhiều email (spam/botnet)
MATCH (i:IPAddress)<-[:SENT_FROM]-(e:Email)
RETURN i.value, count(e) AS emailCount
ORDER BY emailCount DESC;
```

---

### 3.3. HOSTED_BY (URL → IPAddress)

**Biểu diễn**:
```cypher
(:URL)-[:HOSTED_BY]->(:IPAddress)
```

- **Ý nghĩa**: URL được host trên một địa chỉ IP.
- **Giá trị phân tích**: IP host nhiều URL đáng nghi hoặc các URL thuộc cùng một chiến dịch giúp củng cố bằng chứng gian lận.

**Ví dụ truy vấn**:
```cypher
// Tìm IP host nhiều URL
MATCH (i:IPAddress)<-[:HOSTED_BY]-(u:URL)
RETURN i.value, count(u) AS urlCount
ORDER BY urlCount DESC;
```

---

## 4. Chuẩn hóa định danh và đảm bảo tính nhất quán dữ liệu

Để tránh trùng lặp node khi ingest dữ liệu, hệ thống sử dụng nguyên tắc định danh duy nhất:

- **Email**: `Email:<email>`
- **URL**: `URL:<url>`
- **IP**: `IPAddress:<ip>`

Khi tạo dữ liệu, hệ thống sử dụng lệnh `MERGE` trong Cypher nhằm:

- Không tạo trùng node
- Không tạo trùng quan hệ

**Ví dụ Cypher (tóm tắt)**:

```cypher
MERGE (e:Email {id: 'Email:' + $email, value: $email})
MERGE (u:URL {id: 'URL:' + $url, value: $url})
MERGE (i:IPAddress {id: 'IPAddress:' + $ip, value: $ip})

MERGE (e)-[:CONTAINS_LINK]->(u)
MERGE (e)-[:SENT_FROM]->(i)
MERGE (u)-[:HOSTED_BY]->(i)
```

Cách tiếp cận này giúp đồ thị ổn định và phản ánh đúng nguyên tắc **"một thực thể – một node"**.

---

## 5. Mô hình hóa hành vi gian lận thay vì kết tội thực thể

Một điểm quan trọng của mô hình đồ thị trong đồ án là:

- Không kết luận email, URL hay IP là gian lận chỉ vì chúng "trông có vẻ lạ".
- Hệ thống đánh giá dựa trên **mật độ và cấu trúc quan hệ**, bao gồm:
  - URL xuất hiện trong nhiều email
  - IP gửi nhiều email
  - Nhiều email và nhiều URL cùng quy về một IP
  - Hình thành các cụm (community) hoặc chiến dịch (campaign)

Do đó, mô hình đồ thị tập trung mô tả **kịch bản liên kết hành vi (scenario-based)**, thay vì mô hình phân loại đơn điểm.

---

## 6. Các truy vấn mẫu chứng minh mô hình hiệu quả

### 6.1. URL nào được nhiều email trỏ tới?

```cypher
MATCH (u:URL)<-[:CONTAINS_LINK]-(e:Email)
RETURN u.value, count(e) AS emailCount
ORDER BY emailCount DESC;
```

Truy vấn này được sử dụng để phát hiện các URL có tần suất xuất hiện cao trong email. Những URL được nhiều email cùng trỏ tới có khả năng thuộc cùng một chiến dịch phishing, từ đó giúp hệ thống nâng mức đánh giá rủi ro và khoanh vùng các hành vi gian lận có chủ đích.

---

### 6.2. IP nào gửi nhiều email?

```cypher
MATCH (i:IPAddress)<-[:SENT_FROM]-(e:Email)
RETURN i.value, count(e) AS emailCount
ORDER BY emailCount DESC;
```

Truy vấn này giúp xác định các địa chỉ IP gửi nhiều email nhất. Những IP có số lượng email cao có thể là dấu hiệu của hành vi spam, botnet hoặc hạ tầng phục vụ các chiến dịch gian lận.

---

### 6.3. Truy vết kịch bản Email → URL → IP

```cypher
MATCH (e:Email)-[:CONTAINS_LINK]->(u:URL)-[:HOSTED_BY]->(i:IPAddress)
RETURN e, u, i;
```

Truy vấn này cho phép truy vết toàn bộ chuỗi hành vi từ email chứa liên kết, liên kết dẫn tới website và website được host trên địa chỉ IP nào. Đây là truy vấn cốt lõi để phân tích kịch bản phishing theo nhiều bước trong mô hình đồ thị.

---

### 6.4. Phát hiện cụm chiến dịch phishing

```cypher
MATCH (e1:Email)-[:CONTAINS_LINK]->(u:URL)<-[:CONTAINS_LINK]-(e2:Email)
WHERE e1 <> e2
RETURN u.value AS suspiciousURL, 
       collect(DISTINCT e1.value) AS emails,
       count(DISTINCT e1) AS emailCount
ORDER BY emailCount DESC;
```

Truy vấn này tìm các URL được chia sẻ bởi nhiều email khác nhau, giúp phát hiện các chiến dịch phishing có quy mô.

---

## 7. Sơ đồ mô hình đồ thị

```
        ┌──────────┐
        │  Email   │
        └────┬─────┘
             │ CONTAINS_LINK
             ▼
        ┌──────────┐
        │   URL    │
        └────┬─────┘
             │ HOSTED_BY
             ▼
      ┌──────────────┐
      │  IPAddress   │
      └──────────────┘
             ▲
             │ SENT_FROM
        ┌────┴─────┐
        │  Email   │
        └──────────┘
```

**Mô hình tam giác Email - URL - IP**:
- Email chứa URL (CONTAINS_LINK)
- Email gửi từ IP (SENT_FROM)
- URL host trên IP (HOSTED_BY)

---

## 8. Kết luận

Mô hình dữ liệu đồ thị cho bài toán phát hiện gian lận được xây dựng dựa trên:

- **3 thực thể cốt lõi**: Email, URL, IPAddress
- **3 quan hệ chính**: CONTAINS_LINK, SENT_FROM, HOSTED_BY

Mô hình này:
- Tập trung mô tả chuỗi hành vi phishing thông qua liên kết giữa các thực thể
- Giúp hệ thống dễ dàng truy vết, phân tích rủi ro
- Mở rộng trong tương lai khi dữ liệu tăng lên
- Phát hiện gian lận dựa trên **pattern** và **hành vi** thay vì thuộc tính đơn lẻ
