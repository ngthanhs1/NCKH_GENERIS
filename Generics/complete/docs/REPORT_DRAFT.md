# BIA CHINH

TRUONG: [Bo sung ten truong]

KHOA/DON VI: [Bo sung ten khoa hoac don vi]

BAO CAO TONG KET DE TAI SINH VIEN NGHIEN CUU KHOA HOC

TEN DE TAI:

TOI UU HOA MA NGUON VA TANG TINH TAI SU DUNG TRONG HE THONG PHAN TICH DO THI BANG KY THUAT GENERICS

Sinh vien thuc hien: [Bo sung]

Lop: [Bo sung]

Giang vien huong dan: [Bo sung]

Dia diem, thoi gian: [Bo sung]

---

# BIA PHU

BAO CAO TONG KET DE TAI SINH VIEN NGHIEN CUU KHOA HOC

TEN DE TAI:

TOI UU HOA MA NGUON VA TANG TINH TAI SU DUNG TRONG HE THONG PHAN TICH DO THI BANG KY THUAT GENERICS

Chu nhiem de tai: [Bo sung]

Thanh vien: [Bo sung]

Giang vien huong dan: [Bo sung]

Don vi quan ly: [Bo sung]

---

# MUC LUC

Muc luc se duoc cap nhat khi hoan thien ban Word cuoi cung.

# DANH MUC BANG BIEU

- Bang 1. Cac thanh phan Generics va OOP chinh trong he thong
- Bang 2. Cac endpoint REST API hien tai
- Bang 3. So sanh phan generic va phan scenario trong he thong
- Bang 4. Danh gia muc do dap ung muc tieu de tai theo ma nguon hien tai

# DANH MUC NHUNG TU VIET TAT

- API: Application Programming Interface
- DTO: Data Transfer Object
- HTML: HyperText Markup Language
- JSON: JavaScript Object Notation
- NCKH: Nghien cuu khoa hoc
- OOP: Object-Oriented Programming
- REST: Representational State Transfer
- UI: User Interface

# MO DAU

Trong nhieu he thong phan tich du lieu do thi, ma nguon ban dau thuong duoc viet bam sat mot bai toan cu the nhu gian lan giao dich, mang xa hoi hoac quan ly thuc the lien ket. Khi doi loai du lieu, nha phat trien phai sua lai nhieu lop backend, nhieu cau truc DTO va ca giao dien hien thi. Dieu nay lam giam manh tinh tai su dung cua he thong va khien ma nguon kho mo rong.

De tai nay tap trung giai quyet van de do bang cach ap dung ky thuat Generics trong Java de xay dung mot loi xu ly do thi tong quat. He thong duoc phat trien bang Spring Boot, Neo4j va giao dien web truc quan hoa graph. Thay vi dinh nghia model rieng cho tung domain, du an dua moi du lieu ve mot cau truc thong nhat gom node, edge va tap thuoc tinh mo rong. Tren nen chung do, he thong co the hien thi, loc, import va export nhieu loai du lieu do thi khac nhau.

Phien ban ma nguon hien tai con tach ro hai phan: graph core va giao dien duoc giu generic, trong khi phan phan tich duoc to chuc theo mo hinh scenario. Cach to chuc nay phan anh dung trang thai code hien tai va cung phu hop voi muc tieu cua de tai: generic hoa phan kien truc dung chung, dong thoi giu kha nang trien khai cac bai toan phan tich chuyen biet tren cung mot graph core.

# TONG QUAN TINH HINH NGHIEN CUU THUOC LINH VUC DE TAI

Trong linh vuc du lieu lien ket, co so du lieu do thi nhu Neo4j duoc su dung rong rai de bieu dien quan he giua cac doi tuong. Nhieu he thong hien nay co the truc quan hoa graph, tim duong di, phat hien cum lien ket va ho tro ra quyet dinh tren du lieu dang mang. Tuy nhien, trong cac do an hoac he thong minh hoa, ma nguon thuong bi gan chat voi mot domain cu the. Khi doi bai toan, he thong phai chinh sua tu lop du lieu, service, controller den frontend.

Generics trong Java la cong cu manh de tong quat hoa kieu du lieu, tang type safety va giam lap ma. Neu duoc ket hop dung voi OOP, Generics khong chi giup viet it code hon ma con lam cho kien truc de tai su dung hon. Voi bai toan graph, viec ap dung Generics vao DTO, response wrapper va service contract tao dieu kien de cung mot loi he thong phuc vu nhieu tap du lieu khac nhau.

Diem dang chu y o phien ban hien tai cua du an la su tach biet ro giua hai lop trach nhiem:

- Phan generic: graph data model, DTO, API graph, UI explorer, import/export.
- Phan chuyen biet: detection va risk analysis theo scenario.

# LY DO LUA CHON DE TAI

De tai duoc lua chon tu nhu cau thuc te trong viec giam phu thuoc domain cho cac he thong phan tich do thi. Trong nhieu do an, kien truc ban dau thuong hoat dong duoc voi mot bo du lieu mau nhung rat kho chuyen sang du lieu khac vi ten lop, API va giao dien deu viet co dinh theo tung thuc the. Khi mo rong sang bai toan khac, chi phi chinh sua tro nen lon.

Viec xay dung mot he thong graph explorer tong quat giup giai quyet truc tiep van de do. Thay vi chi lam mot ung dung minh hoa cho mot tap node va relation cu the, de tai huong toi mot nen tang nho co the dung lai cho nhieu bai toan. Trang thai code hien tai the hien ro dinh huong nay: graph core khong phu thuoc domain, giao dien tu sinh tu du lieu, con phan phan tich duoc thay the bang scenario ma khong can viet lai frontend.

# MUC TIEU, NOI DUNG, PHUONG PHAP NGHIEN CUU CUA DE TAI

## 1. Muc tieu nghien cuu

- Xay dung mo hinh graph tong quat co the tai su dung cho nhieu loai du lieu.
- Ap dung Generics vao DTO, response API va service abstraction de giam lap ma.
- Xay dung giao dien data-driven co the hien thi du lieu theo cau truc nodes va edges ma khong hard-code entity.
- To chuc phan phan tich theo scenario de thay doi bai toan ma khong phai thay doi graph core va UI.

## 2. Noi dung nghien cuu

- Nghien cuu ky thuat Generics trong Java va cach ket hop voi OOP.
- Thiet ke bo DTO tong quat gom ApiResponse, GraphData, NodeDTO, EdgeDTO.
- Xay dung GraphQueryService de truy van va import graph theo schema thong nhat tren Neo4j.
- Xay dung GraphApiController va DetectionApiController theo phong cach response thong nhat.
- Xay dung DetectionScenario, DetectionScenarioRegistry va cac scenario cu the de tach luat phan tich khoi graph core.
- Xay dung giao dien Generic Graph Explorer, trang Insights va trang Import / Export theo huong tu thich nghi voi dataset hien tai.

## 3. Phuong phap nghien cuu

- Phan tich kien truc phan mem cua mot he thong graph co kha nang tai su dung.
- Ap dung OOP ket hop Generics de tao cau truc tong quat cho du lieu va service.
- Thuc nghiem voi Spring Boot, Neo4j va giao dien web truc quan hoa graph.
- Doi chieu ket qua trien khai voi muc tieu de tai ve toi uu ma nguon va tang tinh tai su dung.

# DOI TUONG VA PHAM VI NGHIEN CUU

## 1. Doi tuong nghien cuu

Doi tuong nghien cuu la kien truc phan mem cua he thong phan tich do thi co kha nang tai su dung, trong do trong tam la cach dung Generics de chuan hoa cau truc du lieu va API, dong thoi tach business logic phan tich thanh cac scenario doc lap.

## 2. Pham vi nghien cuu

- Backend duoc xay dung bang Spring Boot.
- Co so du lieu su dung Neo4j.
- Schema du lieu chung cua he thong la (:Node)-[:RELATION]->(:Node).
- Frontend web hien thi graph, bo loc, thong tin chi tiet va import/export du lieu.
- Detection va risk analysis duoc trien khai theo scenario, trong do scenario mac dinh hien tai la ASSOCIATION_GRAPH.
- He thong chua di vao hoc may hay du doan nang cao, ma tap trung vao kien truc tai su dung va kha nang mo rong ma nguon.

# KET QUA NGHIEN CUU VA THAO LUAN

## CHUONG 1. CO SO LY THUYET VA NEN TANG THIET KE

### 1.1. Vai tro cua OOP va Generics trong de tai

Trong Java, OOP giup xay dung hanh vi chung qua interface, abstract class va nguyen tac phan lop trach nhiem. Generics bo sung kha nang tham so hoa kieu du lieu de cung mot cau truc co the su dung lai cho nhieu dang du lieu khac nhau ma van dam bao an toan kieu tai thoi diem bien dich.

Trong he thong hien tai, OOP va Generics ket hop theo dung tinh than thiet ke phan mem:

- OOP tao bo khung to chuc cho controller, service, scenario va strategy.
- Generics giup bo khung do lam viec voi nhieu kieu du lieu graph ma khong phai viet lai lop moi cho tung domain.

### 1.2. Cac thanh phan generic chinh trong ma nguon

Bang 1. Cac thanh phan Generics va OOP chinh trong he thong

| Thanh phan | Vai tro trong he thong |
|---|---|
| ApiResponse<T> | Chuan hoa du lieu tra ve tu API |
| GraphData<N, E> | Mo ta graph tong quat gom nodes va edges |
| NodeDTO<A> | Mo ta node voi id, type va attributes tong quat |
| EdgeDTO<A> | Mo ta edge voi from, to, relation va attributes tong quat |
| BaseService<T, ID> | Interface service tong quat cho thao tac dung chung |
| BaseServiceImpl<T, ID> | Abstract class gom logic mac dinh cho service |
| PatternRule<M> | Contract tong quat cho mot luat phat hien pattern |
| RiskScoreStrategy<C, R> | Contract tong quat cho chien luoc cham diem |
| DetectionScenario | Contract cho mot kich ban phan tich doc lap |

He thong hien dung GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> lam cau truc graph chuan cho ca backend va frontend. Day la diem then chot tao nen tinh tai su dung cua ma nguon.

### 1.3. Tu tuong kien truc hien tai

Phien ban code hien tai duoc xay dung theo nguyen tac sau:

- Generic hoa phan loi luu tru, truy van va hien thi graph.
- Khong generic hoa cuong ep toan bo business logic phan tich.
- Tach phan nghiep vu phat hien pattern va cham diem thanh scenario co the thay doi.

## CHUONG 2. THIET KE VA TRIEN KHAI HE THONG

### 2.1. Kien truc tong the

He thong duoc trien khai theo chuoi xu ly sau:

Frontend Web

REST API

GraphApiController / DetectionApiController

GraphQueryService / PatternDetectionService / RiskAnalysisService

DetectionScenarioRegistry

Neo4jClient

Neo4j Database

Trong kien truc nay, GraphQueryService dong vai tro graph core. Moi luong hien thi va phan tich deu lay du lieu tu graph core chung thay vi tu truy van rieng theo tung domain.

### 2.2. Loi graph generic

GraphQueryService truy van du lieu tu Neo4j theo schema thong nhat (:Node)-[:RELATION]->(:Node). Du lieu node duoc doc tu cac truong id, type va properties(n). Du lieu edge duoc doc tu from, to, relation va properties(r). Cac thuoc tinh dong duoc gom vao attributes de khong khoa cung cau truc cua node va edge.

Khi import du lieu, he thong nhan payload GraphData voi hai mang nodes va edges. Nguoi dung co the chon append hoac replace. Cach trien khai nay cho phep cung mot backend nhan cac dataset khac nhau nhu PERSON-DEVICE-ACCOUNT, STUDENT-COURSE-ROOM hoac EMAIL-URL-IP mien la du lieu duoc dua ve dung schema chung.

### 2.3. Tang API hien tai

Bang 2. Cac endpoint REST API hien tai

| Endpoint | Chuc nang |
|---|---|
| GET /api/graph | Tra ve toan bo graph tong quat |
| POST /api/graph/import | Import graph moi vao Neo4j |
| DELETE /api/graph/nodes/{nodeId} | Xoa mot node va cac canh lien quan theo id |
| GET /api/detection/patterns | Tra ve cac pattern phat hien theo scenario dang active |
| GET /api/detection/risk | Tra ve danh sach cham diem rui ro theo scenario dang active |
| GET /api/detection/profile | Tra ve scenario hien hanh va danh sach scenario kha dung |

Endpoint detection khong con tra ve profile tinh nhu truoc. Thay vao do, controller lay thong tin tu DetectionScenarioRegistry de phan anh scenario dang hoat dong thuc su trong ma nguon.

### 2.4. Tang phan tich theo scenario

Day la thay doi quan trong nhat cua phien ban hien tai.

DetectionScenario la interface mo ta mot kich ban phan tich, gom:

- key
- displayName
- detect
- evaluate
- describe

DetectionScenarioRegistry quan ly toan bo scenario co trong he thong va chon scenario active thong qua cau hinh detection.active-scenario trong application.properties.

Hai scenario hien dang co trong ma nguon la:

- ASSOCIATION_GRAPH: scenario mac dinh, phu hop voi du lieu dang ACCOUNT, PERSON, DEVICE, TRANSACTION hoac cac graph lien ket tong quat.
- EMAIL_URL_IP: scenario minh hoa cho bai toan Email - URL - IP tu giai doan truoc.

PatternDetectionService va RiskAnalysisService hien khong con nam giu logic phan tich cu the. Hai service nay chi goi GraphQueryService de lay graph hien tai va uy quyen cho scenario active de xu ly.

### 2.5. Scenario mac dinh ASSOCIATION_GRAPH

Scenario ASSOCIATION_GRAPH khong gia dinh node phai la Email, URL hay IP. Thay vao do, no dua tren cau truc lien ket cua graph de phat hien mot so mau dang chu y nhu:

- HIGH_DEGREE_NODE
- SHARED_RELATION_TARGET
- MULTI_TYPE_BRIDGE

Viec cham diem rui ro dua tren topology cua graph, vi du so bac cua node, so loai relation, so loai neighbor va viec node co la shared target hay khong. Ket qua duoc phan thanh SAFE, SUSPICIOUS hoac HIGH_INTEREST.

### 2.6. Moi quan he giua phan generic va phan scenario

Bang 3. So sanh phan generic va phan scenario trong he thong

| Thanh phan | Tinh chat |
|---|---|
| GraphData, NodeDTO, EdgeDTO, ApiResponse | Generic va dung chung |
| GraphQueryService | Generic va dung chung |
| GraphApiController | Generic va dung chung |
| index.html, insights.html, export.html | Generic va data-driven |
| DetectionApiController | Dung chung, nhung uy quyen cho scenario |
| DetectionScenarioRegistry | Bo chon scenario |
| AssociationGraphDetectionScenario | Business logic chuyen biet theo topology |
| EmailUrlIpDetectionScenario | Business logic chuyen biet theo bai toan Email - URL - IP |

## CHUONG 3. GIAO DIEN TRUC QUAN HOA VA THUC NGHIEM

### 3.1. Generic Graph Explorer

Trang index.html la giao dien chinh cua he thong, co ten Generic Graph Explorer. Giao dien nay lay du lieu tu endpoint GET /api/graph va xay dung toan bo phan hien thi dua tren du lieu nhan duoc.

Nhung dac diem quan trong cua giao dien gom:

- Node label duoc lay uu tien tu cac thuoc tinh nhu label, name, title roi moi fallback ve id.
- Mau node duoc gan theo type.
- Edge label duoc lay tu relation.
- Panel chi tiet hien thi toan bo attributes o dang JSON.
- Nguoi dung co the xoa node dang chon ngay tren panel chi tiet qua API DELETE /api/graph/nodes/{nodeId}.
- Bo loc node type va relation duoc sinh tu dong tu dataset hien tai.
- Khi du lieu thay doi, giao dien khong can sua ma de nhan loai node hoac relation moi.

### 3.2. Trang Import / Export

Trang export.html cho phep xem payload graph hien tai, tai xuong du lieu dang JSON va nhap mot payload moi thong qua POST /api/graph/import. Nguoi dung co the chon replace mode de ghi de du lieu hoac append mode de noi du lieu moi vao graph hien tai. Giao dien cung phan hoi ro loi import khi payload sai dinh dang hoac vi pham rang buoc co ban.

Vi du payload mau trong giao dien su dung cac loai node STUDENT, COURSE, ROOM va relation ENROLLED_IN, TAUGHT_IN. Dieu nay cho thay he thong khong bi khoa vao mot domain duy nhat ma co the tiep nhan du lieu moi mien la dung cau truc chung.

### 3.3. Trang Insights

Trang insights.html tach rieng phan thong ke va danh sach du lieu khoi man hinh graph chinh. Trang nay doc truc tiep tu GET /api/graph de hien thi so luong node, edge, node type va relation, dong thoi sinh danh sach type/relation hoan toan theo du lieu hien co trong database.

Viec tach trang nay giup giao dien chinh tap trung vao truc quan hoa va thao tac tren graph, con phan thong tin tong hop duoc gom ve mot man hinh rieng. Day cung la diem phu hop voi gop y cua giang vien: UI van generic, nhung cac nhom thao tac duoc to chuc lai ro rang hon theo muc dich su dung.

### 3.4. Thu nghiem theo ma nguon hien tai

Theo code hien tai, he thong co hai bai thu nghiem quan trong ve mat kien truc:

- Thu nghiem graph generic qua GraphApiControllerTest, bao gom lay graph, import graph va xoa node.
- Thu nghiem detection API theo scenario qua DetectionApiControllerTest.

Ngoai ra, cau truc ma nguon cho thay co the thu nghiem nhanh bang cach thay detection.active-scenario trong application.properties de dung scenario khac ma khong phai thay doi giao dien hoac graph API.

## Danh gia ket qua nghien cuu

Bang 4. Danh gia muc do dap ung muc tieu de tai theo ma nguon hien tai

| Tieu chi | Danh gia |
|---|---|
| Generic Data Model | Dat |
| Generic DTO | Dat |
| Generic Response API | Dat |
| Generic Graph Query Service | Dat |
| Generic UI | Dat |
| Generic Import / Export | Dat |
| Thay doi luat phan tich ma khong doi UI | Dat |
| Business logic phan tich hoan toan generic | Khong dat muc tieu o phien ban hien tai |

Ket qua nghien cuu cho thay he thong hien tai da dat duoc phan cot loi cua de tai: toi uu hoa ma nguon va tang tinh tai su dung o phan graph core, API, DTO va giao dien. So voi trang thai ban dau bam nhieu vao mot bai toan minh hoa, ma nguon hien da tien den cau truc hop ly hon: mot loi generic dung chung va mot tang scenario de thay business logic theo nhu cau.

# KET LUAN VA KIEN NGHI

## a) Ket luan

De tai da xay dung duoc mot he thong phan tich va truc quan hoa do thi theo huong tai su dung, trong do phan generic duoc ap dung ro rang vao mo hinh du lieu, DTO, API, service graph va giao dien hien thi. He thong su dung chung mot cau truc graph gom nodes, edges va attributes, cho phep tiep nhan nhieu loai dataset khac nhau ma khong phai thay doi kien truc loi.

Dong gop noi bat cua phien ban ma nguon hien tai la viec to chuc tang phan tich theo scenario. Cach lam nay giup he thong dat duoc hai muc tieu dong thoi:

- giu graph core va UI on dinh, co the tai su dung
- van trien khai duoc cac luat phan tich chuyen biet theo tung bai toan

Nhung dong gop chinh cua de tai gom:

- Xay dung bo DTO tong quat cho du lieu graph.
- Chuan hoa phan hoi REST bang ApiResponse<T>.
- Xay dung GraphQueryService cho truy van, import va xoa node theo schema chung.
- Xay dung giao dien Generic Graph Explorer, Insights va Import / Export theo huong data-driven.
- Tach luat phan tich thanh DetectionScenario va quan ly bang DetectionScenarioRegistry.
- Cai dat scenario mac dinh ASSOCIATION_GRAPH va duy tri them scenario EMAIL_URL_IP de minh hoa kha nang thay the business logic.

## b) Kien nghi

- Bo sung validate sau hon cho payload import de kiem tra them quy tac du lieu nghiep vu ngoai cac kiem tra co ban hien da co cho id, type, relation va lien ket tham chieu.
- Bo sung endpoint hoac giao dien cho phep doi scenario truc tiep thay vi chi cau hinh trong application.properties.
- Viet them scenario cho cac mien du lieu khac nhu giao duc, logistics hoac knowledge graph de chung minh manh hon tinh tai su dung cua graph core.
- Tiep tuc hoan thien test tich hop tren moi truong Java 17 de xac nhan day du hanh vi runtime cua toan bo he thong.

# TAI LIEU THAM KHAO

[1] Oracle, Java Generics Tutorial.

[2] Spring Boot Documentation.

[3] Spring Data Neo4j Documentation.

[4] Neo4j Cypher Manual.

[5] Tai lieu ve thiet ke REST API va truc quan hoa graph phuc vu nghien cuu he thong.

# PHU LUC

## Phu luc A. Vi du cau truc JSON graph tong quat

```json
{
  "nodes": [
    {
      "id": "P1",
      "type": "PERSON",
      "attributes": {
        "name": "Alice"
      }
    },
    {
      "id": "D1",
      "type": "DEVICE",
      "attributes": {
        "os": "Android"
      }
    }
  ],
  "edges": [
    {
      "from": "P1",
      "to": "D1",
      "relation": "USES_DEVICE",
      "attributes": {
        "since": "2026-03-10"
      }
    }
  ]
}
```

## Phu luc B. Y nghia cua endpoint profile hien tai

Endpoint GET /api/detection/profile hien tra ve:

- activeScenario: scenario dang chay
- details: mo ta scenario dang active
- availableScenarios: danh sach scenario dang duoc dang ky trong he thong

## Phu luc C. Ghi chu dinh dang ban in cuoi

- Kho giay A4, font Times New Roman, co chu 13.
- Gian dong 1.3 den 1.5.
- Le trai 3 cm; le tren, duoi, phai 2 cm.
- Danh so trang o giua phia tren.
