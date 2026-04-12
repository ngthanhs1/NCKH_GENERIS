# Kịch bản demo (thuyết trình 5–7 phút)

Mục tiêu: chứng minh 3 ý đúng với đề tài
1) Có **CSDL đồ thị** (nodes/relationships rõ ràng)
2) Có **mô hình dữ liệu** cho bài toán gian lận
3) Có **phân tích đồ thị cụ thể** + trực quan hóa

---

## 0) Chuẩn bị

- Chạy app trong thư mục `complete/`.
- Mở: `http://localhost:8080`

---

## 1) Tạo dữ liệu đồ thị (2 phút)

Mục tiêu của bước này là tạo các cấu trúc đặc trưng để chụp ảnh demo:
- 01 triad tối thiểu (Email–URL–IP)
- 01 chiến dịch shared URL (nhiều Email → 01 URL)
- 01 hạ tầng shared IP (nhiều Email → 01 IP)
- 01 tình huống thiếu triad (để minh họa `UNKNOWN`)

Gợi ý chạy nhanh bằng `curl` (dùng `--data-urlencode` để tránh lỗi ký tự `http://...`):

```bash
BASE="http://localhost:8080"
API="$BASE/api/graph"
```

### 1.1. 01 triad tối thiểu (Email–URL–IP)

```bash
curl -G "$API/link" \
	--data-urlencode "email=triad1@demo.local" \
	--data-urlencode "url=http://min.example/one" \
	--data-urlencode "ip=10.0.0.10"
```

### 1.2. Shared URL (campaign)

Nhiều Email khác nhau cùng trỏ về 01 URL.

```bash
curl -G "$API/link" \
	--data-urlencode "email=camp1@demo.local" \
	--data-urlencode "url=http://campaign.example/login" \
	--data-urlencode "ip=10.0.0.21"
curl -G "$API/link" \
	--data-urlencode "email=camp2@demo.local" \
	--data-urlencode "url=http://campaign.example/login" \
	--data-urlencode "ip=10.0.0.22"
curl -G "$API/link" \
	--data-urlencode "email=camp3@demo.local" \
	--data-urlencode "url=http://campaign.example/login" \
	--data-urlencode "ip=10.0.0.23"
```

### 1.3. Shared IP (spam/botnet)

Nhiều Email khác nhau cùng gửi từ 01 IP (hạ tầng gửi tập trung).

```bash
curl -G "$API/link" \
	--data-urlencode "email=infra1@demo.local" \
	--data-urlencode "url=http://infra.example/a" \
	--data-urlencode "ip=10.0.0.30"
curl -G "$API/link" \
	--data-urlencode "email=infra2@demo.local" \
	--data-urlencode "url=http://infra.example/b" \
	--data-urlencode "ip=10.0.0.30"
curl -G "$API/link" \
	--data-urlencode "email=infra3@demo.local" \
	--data-urlencode "url=http://infra.example/c" \
	--data-urlencode "ip=10.0.0.30"
```

### 1.4. Thiếu triad (minh họa UNKNOWN)

Chỉ nhập Email + URL (thiếu IP và thiếu cạnh tam giác đầy đủ) để hệ thống giữ `UNKNOWN` khi analyze.

```bash
curl -G "$API/link-partial" \
	--data-urlencode "email=orphan1@demo.local" \
	--data-urlencode "url=http://orphan.example/only"
```

---

## 2) Chạy phân tích và chấm điểm (1 phút)

```bash
curl "http://localhost:8080/api/graph/analyze"
```

Giải thích nhanh scoring Email (minh họa):
- +30 có URL
- +20 có IP nguồn
- +30 shared IP
- +30 shared URL

---

## 3) Minh họa trực quan trên UI (2–3 phút)

- Reload trang `http://localhost:8080`
- Dùng filter để bật/tắt các verdict.
- Click từng node để thấy panel giải thích: liên kết + đếm shared.

Điểm nhấn:
- URL `fake-bank.com` được nhiều email trỏ tới → campaign.
- IP `10.0.0.9` gửi nhiều email → hub/spam.

---

## 4) Kết luận (30s)

- Graph DB giúp mô hình hóa quan hệ, truy vấn pattern nhanh, trực quan.
- Scoring rule-based chỉ là minh họa; có thể mở rộng thêm dữ liệu/ML.
