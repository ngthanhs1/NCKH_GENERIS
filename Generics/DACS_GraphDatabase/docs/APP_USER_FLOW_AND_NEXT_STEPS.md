# Phân tích ứng dụng: User flow & hướng phát triển (đúng đề tài)

Tài liệu này tóm tắt **ứng dụng hiện tại đang làm gì**, **người dùng (user) sẽ dùng như thế nào**, và **bước tiếp theo nên phát triển gì** để vừa đúng đề tài DACS vừa demo ổn định.

---

## 1) Ứng dụng đang làm gì?

### 1.1. Mục tiêu
Minh họa bài toán **phát hiện gian lận/phishing** bằng **Graph Database (Neo4j)** dựa trên chuỗi liên kết:

- Node: `Email`, `URL`, `IPAddress`
- Relationship:
  - `(:Email)-[:CONTAINS_LINK]->(:URL)`
  - `(:Email)-[:SENT_FROM]->(:IPAddress)`
  - `(:URL)-[:HOSTED_BY]->(:IPAddress)`

### 1.2. Kết quả đầu ra (demo)
- Dữ liệu đồ thị hiển thị trực quan bằng D3.js.
- Chạy phân tích rule-based để gán `riskScore` + `verdict` (SAFE/SUSPICIOUS/FRAUD/UNKNOWN).
- Có trang báo cáo tổng hợp (KPI, top shared URL/IP, bảng theo loại) và xuất CSV.

---

## 2) Người dùng (user) phù hợp nhất là ai?

Trong scope đồ án, user phù hợp nhất là:

- **Người xem/demo (giảng viên/nhóm thuyết trình)** hoặc **analyst cơ bản**
- Không cần đăng nhập/phân quyền (vì đề tài tập trung Graph DB + mô hình + phân tích)

Mục tiêu user: tạo nhanh dữ liệu → xem pattern → giải thích được “vì sao đáng nghi”.

---

## 3) User flow (luồng sử dụng chuẩn để demo)

### Bước A — Vào trang chính
- Mở `http://localhost:8080`.
- Quan sát trạng thái Neo4j (health/count).

### Bước B — Tạo dữ liệu
Có 2 cách:

1) **Seed demo**: tự tạo 4 chuỗi Email–URL–IP để sinh 2 pattern:
   - Shared URL (campaign)
   - Shared IP (hub/spam)

2) **Nhập tay**: nhập `Email / URL / IP` rồi bấm **Tạo / Link**.
   - Nhập 1 trường: tạo node
   - Nhập 2 trường: tạo node + relationship tương ứng
   - Nhập đủ 3: tạo full chain (tam giác Email–URL–IP)

### Bước C — Phân tích
- Hệ thống chạy `/api/graph/analyze` để cập nhật `riskScore` và `verdict` vào Neo4j.

**Điểm quan trọng**: một node sẽ chỉ được đánh giá “rõ” khi tham gia **đủ bộ 3** (tam giác).
Nếu chưa đủ bộ 3: giữ `UNKNOWN` để tránh kết luận khi chưa đủ bằng chứng quan hệ.

### Bước D — Khám phá đồ thị
- Dùng filter (type/verdict).
- Hover/click để xem panel giải thích rule-based.
- (Tuỳ chọn) kéo thả node và lưu layout.

### Bước E — Báo cáo
- Mở trang report để in PDF hoặc xuất CSV.

---

## 4) Những gì đã đủ “đúng đề tài”

- Đủ mô hình Graph DB (nodes/relationships) + ingest bằng `MERGE`.
- Có truy vấn pattern cơ bản (shared URL/shared IP, truy vết Email→URL→IP).
- Có scoring minh họa dựa trên cấu trúc quan hệ (rule-based).
- Có visualization + báo cáo để trình bày.

---

## 5) Nên phát triển tiếp gì cho đúng (theo ưu tiên)

### Ưu tiên 1 — Ổn định demo + đúng kỹ thuật
1) **Bảo mật cấu hình**: không commit mật khẩu DB; dùng env vars.
2) **Tối thiểu test API**: health/link/analyze/full để tránh lỗi lúc thuyết trình.
3) **Validation input**: email/url/ip nhập sai phải báo lỗi rõ.

### Ưu tiên 2 — Nâng chất “phân tích đồ thị” (ăn điểm phần phân tích)
1) Thêm 1–2 truy vấn nâng cao:
   - Tìm cụm (community/campaign) theo URL shared
   - Truy vết nhiều bước (k=2..3) để giải thích nguồn
2) Thêm chỉ số đồ thị đơn giản:
   - degree centrality (đếm số liên kết)
   - top hubs (IP/URL nổi bật)

### Ưu tiên 3 — Mở rộng mô hình (nếu còn thời gian)
- Thêm node `Domain` (tách domain khỏi URL) để phân tích theo domain.
- Thêm node `Recipient` (người nhận) để minh họa lan truyền.

---

## 6) Ranh giới scope (để không “lạc đề”)

- Không cần triển khai authentication/authorization phức tạp nếu đề bài không yêu cầu.
- Không cần ML thật; rule-based là hợp lý cho minh họa, nhưng phải giải thích đây là demo.
- Tập trung vào “quan hệ + pattern + truy vết” thay vì chỉ thêm nhiều màn hình.
