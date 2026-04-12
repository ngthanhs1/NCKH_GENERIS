# Bai toan phan tich gian lan theo code hien tai, van dam bao de tai Generics

## 1) Bai toan cu the (theo mau code DACS_GraphDatabase)
- Mien bai toan: phat hien gian lan/phishing dua tren lien ket `Email - URL - IPAddress`.
- Node chinh: `Email`, `URL`, `IPAddress`.
- Relationship chinh:
	- `Email -[:CONTAINS_LINK]-> URL`
	- `Email -[:SENT_FROM]-> IPAddress`
	- `URL -[:HOSTED_BY]-> IPAddress`
- Muc tieu phan tich: tim cac mau nghi ngo va ket noi an, khong ket luan phap ly.

## 2) Giu dung de tai Generics (khong hard-code domain)
- Tang du lieu tong quat:
	- `ApiResponse<T>`
	- `GraphData<N, E>`
	- `NodeDTO<A>`
	- `EdgeDTO<A>`
- Tang detection tong quat:
	- `PatternRule<M>`: moi luat tu dinh nghia metadata rieng.
	- `PatternMatch<M>`: ket qua tung pattern.
	- `DetectionResult<P>`: tong hop pattern + thong ke theo rule.
- Ket luan: domain fraud chi la 1 profile, khung generics van tai su dung duoc cho domain khac.

Trang thai hien tai trong code:
- Da dua node type, relation type, score weight, threshold ra `analysis.profile.*` trong cau hinh.
- Core detection/scoring doc profile thay vi hard-code Email/URL/IP.
- Fraud hien tai chi con la profile mac dinh, co the doi bang config khi chuyen domain.

## 3) Luong phan tich dung thu tu
1. Ingest du lieu graph bang `MERGE` de tranh trung node/edge.
2. Truy van graph tong quat qua `GraphQueryService`.
3. Chay bo rule detection tren cung 1 cau truc `GraphData<N, E>`.
4. Tong hop ket qua, thong ke theo rule, tra API cho UI.
5. UI visualization + giai thich pattern nghi ngo theo metadata.

## 4) Rule fraud de xuat phu hop code mau Email-URL-IP
- `SHARED_URL`:
	- Dieu kien: 1 `URL` duoc nhieu `Email` cung tro toi (`CONTAINS_LINK`).
	- Y nghia: dau hieu campaign phishing.
- `SHARED_IP_SENDER`:
	- Dieu kien: 1 `IPAddress` gui nhieu `Email` (`SENT_FROM`).
	- Y nghia: dau hieu spam/botnet/ha tang dung chung.
- `HOSTING_CLUSTER`:
	- Dieu kien: 1 `IPAddress` host nhieu `URL` (`HOSTED_BY`).
	- Y nghia: dau hieu cum website nghi ngo.
- `TRIANGLE_PATH`:
	- Dieu kien: ton tai day du chuoi `Email -> URL -> IP` va `Email -> IP` cho cung ngu canh.
	- Y nghia: ket noi an duoc xac nhan qua nhieu loai quan he.

## 5) Chien luoc cham diem rui ro (van generic)
- Dung `RiskScoreStrategy<TContext>` de tach scoring khoi rule.
- Profile fraud Email-URL-IP co the bat dau voi bo diem sau:
	- +30: Email co URL.
	- +20: Email co IP gui.
	- +30: shared IP.
	- +30: shared URL.
- Verdict de xuat:
	- `SAFE`: score = 0
	- `SUSPICIOUS`: 1..59
	- `FRAUD`: >= 60

Trang thai hien tai trong code:
- Da co `RiskScoreStrategy<C, R>` generic.
- Da co profile strategy `FraudEmailUrlIpRiskStrategy`.
- Da co service tong hop `RiskAnalysisService`.

## 6) API de bai toan vua cu the vua tai su dung
- API tong quat:
	- `GET /api/graph` hoac `GET /api/graph/full`: tra graph data.
	- `GET /api/detection/patterns`: tra danh sach pattern nghi ngo.
	- `GET /api/detection/risk`: tra bang diem rui ro + verdict theo profile.
- API profile fraud (neu bat):
	- `GET /api/graph/analyze`
	- `GET /api/graph/analysis/email-risk`
	- `GET /api/graph/analysis/url-risk`
	- `GET /api/graph/analysis/ip-risk`

## 7) Tieu chi dat "ket qua chuan mong muon"
- Dung bai toan cu the gian lan Email-URL-IP de minh hoa ro rang.
- Van giu duoc khung generics (DTO + Rule + Detection service + Scoring strategy).
- Them rule moi hoac doi domain khong can sua API contract cot loi.
- Co test contract API va test rule-level de chung minh tinh dung va tinh tai su dung.

## 8) Lo trinh trien khai khuyen nghi
1. Chot schema fraud profile Email-URL-IP.
2. Chot bo rule fraud profile (`SHARED_URL`, `SHARED_IP_SENDER`, `HOSTING_CLUSTER`, `TRIANGLE_PATH`).
3. Cai dat scoring profile va verdict.
4. Hoan tat API detection + API analyze.
5. Hoan tat UI report cho pattern/risk.
6. Viet test tu dong cho ingest, detection, scoring, API.
7. Danh gia tai su dung bang cach thay profile domain (vi du social/logistics) nhung giu nguyen khung generics.
