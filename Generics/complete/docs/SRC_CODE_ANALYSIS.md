# PHÂN TÍCH CHI TIẾT TOÀN BỘ MÃ NGUỒN SRC HIỆN TẠI

## 1. Mục đích tài liệu

Tài liệu này được viết để phân tích toàn bộ thư mục `src` của dự án hiện tại trong `Generics/complete`. Mục tiêu không chỉ là liệt kê tên file, mà còn giải thích vai trò của từng nhóm mã nguồn, cách các lớp phối hợp với nhau, điểm mạnh của kiến trúc hiện tại, các giới hạn còn tồn tại và những hướng mở rộng tiếp theo.

Phạm vi tài liệu gồm ba phần lớn:

- Backend Java Spring Boot trong `src/main/java`
- Tài nguyên cấu hình và giao diện tĩnh trong `src/main/resources`
- Kiểm thử trong `src/test/java`

---

## 2. Bức tranh tổng thể của mã nguồn

Thư mục `src` hiện tại thể hiện một hệ thống web Spring Boot dùng Neo4j làm cơ sở dữ liệu graph, trong đó phần lõi của dự án được tổ chức theo hướng tổng quát hóa dữ liệu graph bằng Generics. Kiến trúc tổng thể có thể tóm tắt như sau:

1. Ứng dụng khởi chạy bằng Spring Boot.
2. Frontend gọi REST API để lấy graph và dữ liệu phân tích.
3. Controller nhận request và trả response chuẩn hóa.
4. Service truy vấn Neo4j hoặc tổng hợp logic detection/risk.
5. Dữ liệu được truyền qua các DTO generic như `GraphData<N, E>`, `NodeDTO<A>`, `EdgeDTO<A>`, `ApiResponse<T>`.
6. Frontend dựng giao diện theo hướng data-driven từ dữ liệu API trả về.

Điểm đáng chú ý nhất của codebase là phần graph core đã được tách khỏi domain cụ thể. Thay vì tạo model riêng như `Email`, `Url`, `IPAddress`, hệ thống quy toàn bộ dữ liệu về một schema chung `Node - RELATION - Node`, sau đó dùng `type`, `relation` và `attributes` để mang ngữ nghĩa domain.

---

## 3. Phân tích backend Java

### 3.1 Điểm vào ứng dụng

File `ServingWebContentApplication.java` là entry point chuẩn của Spring Boot. File này rất nhỏ, chỉ có nhiệm vụ khởi động application context và nạp toàn bộ bean của dự án.

Vai trò chính:

- Khởi động ứng dụng.
- Kích hoạt component scan của Spring.
- Nối toàn bộ controller, service, config và exception handler vào cùng một runtime.

Ý nghĩa thiết kế:

- File này đơn giản và đúng chuẩn một ứng dụng Spring Boot.
- Không chứa logic nghiệp vụ, do đó không gây rối cho phần lõi hệ thống.

### 3.2 Nhóm Controller

Backend hiện có ba file controller-level hoặc gần controller:

- `GraphApiController.java`
- `DetectionApiController.java`
- `RestExceptionHandler.java`

#### a) `GraphApiController`

Đây là controller trung tâm của graph core. Nó cung cấp hai endpoint quan trọng:

- `GET /api/graph`: trả toàn bộ graph tổng quát.
- `POST /api/graph/import`: nhập graph từ payload JSON.

Ý nghĩa kiến trúc:

- Controller này chính là bằng chứng rõ nhất cho việc hệ thống đã chuyển từ mô hình domain cụ thể sang mô hình graph tổng quát.
- API không yêu cầu kiểu thực thể cụ thể; thay vào đó nhận `GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>>`.
- Response được bọc qua `ApiResponse<T>`, tạo ra hợp đồng API ổn định và đồng nhất.

Điểm tốt:

- Thiết kế gọn, không chứa business logic.
- Phân chia rõ trách nhiệm: controller chỉ nhận/trả dữ liệu, service mới xử lý graph.

#### b) `DetectionApiController`

Controller này phục vụ phần mở rộng phân tích, gồm ba endpoint:

- `GET /api/detection/patterns`
- `GET /api/detection/risk`
- `GET /api/detection/profile`

Vai trò:

- Trả kết quả phát hiện pattern nghi vấn.
- Trả kết quả chấm điểm rủi ro cho từng node.
- Trả về profile cấu hình đang dùng cho lớp phân tích.

Điểm mạnh:

- Tách phần detection khỏi graph core.
- Dùng cùng cơ chế `ApiResponse.ok(data)` nên API vẫn thống nhất với nhóm `/api/graph`.

Giới hạn:

- Dù controller đã có hình thức generic, logic bên dưới vẫn đang đi theo profile minh họa mặc định Email - URL - IP.

#### c) `RestExceptionHandler`

File này là exception handler toàn cục cho REST API. Hiện tại nó xử lý lỗi `PermissionDeniedDataAccessException` và trả HTTP 401 với payload có cấu trúc.

Vai trò:

- Tránh để lỗi Neo4j văng thẳng ra frontend dưới dạng stack trace thô.
- Chuẩn hóa cách trả lỗi truy cập Neo4j.

Nhận xét:

- Thiết kế đúng hướng nhưng còn hẹp, vì hiện mới chỉ xử lý một loại lỗi.
- Có thể mở rộng thêm cho lỗi payload import không hợp lệ, lỗi parse JSON, lỗi validation và lỗi Neo4j runtime khác.

---

## 4. Phân tích nhóm DTO và mô hình dữ liệu

Đây là khu vực thể hiện tinh thần Generics rõ nhất trong codebase.

### 4.1 `ApiResponse<T>`

`ApiResponse<T>` là generic response wrapper cho toàn bộ API. Record này có hai trường:

- `data`
- `message`

Ngoài ra có phương thức tĩnh `ok(R data)` để tạo response thành công nhanh.

Ý nghĩa:

- Chuẩn hóa response cho tất cả API.
- Tránh việc mỗi controller phải tự tạo JSON trả về theo kiểu riêng.
- `T` giúp dữ liệu bên trong có thể là graph, pattern result, risk result hoặc bất kỳ kiểu nào khác.

### 4.2 `GraphData<N, E>`

Đây là container tổng quát cho một graph. Nó gồm:

- `List<N> nodes`
- `List<E> edges`

Ý nghĩa:

- Không khóa cứng graph vào một model node/edge cụ thể.
- Cho phép tái sử dụng cùng một cấu trúc cho nhiều loại node/edge khác nhau.

Trong dự án hiện tại, kiểu thực tế được dùng là:

- `N = NodeDTO<Map<String, Object>>`
- `E = EdgeDTO<Map<String, Object>>`

### 4.3 `NodeDTO<A>` và `EdgeDTO<A>`

Hai record này là biểu diễn tổng quát của node và edge.

`NodeDTO<A>` gồm:

- `id`
- `type`
- `attributes`

`EdgeDTO<A>` gồm:

- `from`
- `to`
- `relation`
- `attributes`

Ý nghĩa:

- `A` là kiểu generic của phần thuộc tính mở rộng.
- Trong hệ thống hiện tại, `A` là `Map<String, Object>`, cho phép giữ linh hoạt tối đa.
- Thiết kế này rất phù hợp với bài toán import dữ liệu graph từ nhiều domain khác nhau.

### 4.4 Các DTO phục vụ import và phân tích

Ngoài nhóm graph core, dự án còn có các DTO hỗ trợ:

- `GraphImportResult`: trả số node, số edge đã import và trạng thái replace mode.
- `DetectionResult<P>`: tổng hợp danh sách pattern, số lượng theo rule và tổng số pattern.
- `PatternMatch<M>`: mô tả một pattern cụ thể với metadata generic.
- `RiskResult<R>`: tổng hợp kết quả risk scoring.
- `RiskScoreItem<M>`: mô tả điểm rủi ro của một node và bằng chứng giải thích.

Điểm hay ở đây là không chỉ graph core dùng Generics, mà cả tầng phân tích cũng được tổ chức theo generic item/result. Điều đó cho thấy tác giả dự án đang cố áp dụng cùng một tư duy thiết kế xuyên suốt nhiều lớp, không chỉ dừng ở DTO hiển thị.

---

## 5. Phân tích nhóm Service chung và OOP với Generics

### 5.1 `BaseService<T, ID>`

Đây là interface service tổng quát cho CRUD cơ bản, gồm:

- `findAll()`
- `findById(ID id)`
- `save(T entity)`
- `delete(ID id)`

Ý nghĩa:

- Đây là ví dụ rất rõ của việc dùng OOP kết hợp Generics.
- OOP nằm ở việc tạo ra một interface biểu diễn hành vi chung.
- Generics nằm ở việc cùng interface này dùng lại cho nhiều kiểu entity và ID khác nhau.

### 5.2 `BaseServiceImpl<T, ID>`

Đây là abstract class hiện thực mặc định cho `BaseService<T, ID>` dựa trên `Neo4jRepository<T, ID>`.

Ý nghĩa thiết kế:

- Tạo một khung CRUD mặc định cho các service tương lai.
- Tránh lặp code nếu hệ thống về sau bổ sung thêm entity-specific services.
- Là ví dụ trực tiếp cho cách học phần OOP thường triển khai abstract class + interface + type parameter.

Nhận xét thực tế trong dự án hiện tại:

- Phần graph core không sử dụng cặp `BaseService` này trực tiếp.
- Tuy nhiên, sự hiện diện của nó có giá trị học thuật vì thể hiện cách tác giả áp dụng Generics trong phong cách OOP truyền thống.

---

## 6. Phân tích `GraphQueryService`

`GraphQueryService` là service quan trọng nhất của toàn bộ graph core.

### 6.1 Chức năng chính

File này có ba nhóm vai trò:

1. Đọc graph từ Neo4j.
2. Chuẩn hóa property Neo4j thành `attributes` cho DTO.
3. Ghi graph từ payload import vào Neo4j.

### 6.2 Hàm chuyển đổi dữ liệu

Hai helper quan trọng là:

- `toAttributes(...)`
- `toStoredProperties(...)`

Ý nghĩa:

- `toAttributes` chuyển property thô của Neo4j về dạng dữ liệu ứng dụng sử dụng.
- `toStoredProperties` làm chiều ngược lại, chuẩn hóa attributes khi ghi xuống DB.

Điểm thú vị là dự án dùng tiền tố `attr_` để phân biệt property mở rộng với property hệ thống như `id`, `type`. Đây là một cách làm thực dụng, đơn giản và dễ kiểm soát.

### 6.3 `fetchGraph()`

Hàm này truy vấn Neo4j theo schema:

- `MATCH (n:Node)` cho node
- `MATCH (a:Node)-[r:RELATION]->(b:Node)` cho edge

Sau đó dịch dữ liệu sang:

- `List<NodeDTO<Map<String, Object>>>`
- `List<EdgeDTO<Map<String, Object>>>`

Ý nghĩa:

- Đây là nơi biến Neo4j graph thật thành graph model tổng quát của ứng dụng.
- Tầng frontend và detection đều phụ thuộc vào service này để lấy dữ liệu thống nhất.

### 6.4 `importGraph(...)`

Hàm này nhận payload graph generic và ghi vào Neo4j.

Luồng xử lý:

1. Lấy danh sách node và edge từ `GraphData`.
2. Nếu `replaceMode = true` thì xóa toàn bộ graph hiện có.
3. Chuẩn hóa node thành `nodeRows`.
4. Dùng `UNWIND + MERGE` để ghi node.
5. Chuẩn hóa edge thành `edgeRows`.
6. Dùng `UNWIND + MATCH + MERGE` để ghi edge.
7. Trả `GraphImportResult`.

Điểm mạnh:

- Hỗ trợ cả append lẫn replace.
- Có kiểm tra bỏ qua node/edge thiếu trường bắt buộc.
- `@Transactional` giúp toàn bộ quá trình import có tính nhất quán tốt hơn.

Điểm hạn chế:

- Chưa có validate chi tiết payload.
- Chưa thông báo rõ node/edge nào bị bỏ qua khi dữ liệu lỗi.
- Dùng kiểu `Object` cho ID là linh hoạt nhưng sẽ đòi hỏi kiểm soát kỹ nếu dữ liệu lớn và đa dạng hơn.

---

## 7. Phân tích module detection

Module detection nằm trong `Service/detection` và là phần mở rộng ở phía trên graph core.

### 7.1 `DetectionScenarioProperties`

Đây là lớp cấu hình scenario phân tích, đọc theo prefix `detection`.

Vai trò:

- Xác định scenario đang active thông qua `detection.active-scenario`.
- Tách cấu hình chọn bài toán phân tích ra khỏi controller và service orchestration.

Hiện trạng:

- Scenario mặc định là `ASSOCIATION_GRAPH`.
- Scenario `EMAIL_URL_IP` vẫn được giữ lại như một kịch bản minh họa có thể thay thế.

Ý nghĩa:

- Hệ thống không còn bị ràng buộc vào một profile cố định duy nhất.
- Tầng detection được tổ chức đúng với định hướng mới: generic ở graph core, thay thế được ở business logic.

### 7.2 `DetectionScenarioRegistry`

Đây là registry quản lý các scenario phân tích hiện có trong hệ thống.

Vai trò:

- Đăng ký toàn bộ implementation của `DetectionScenario`.
- Chọn scenario active dựa trên cấu hình.
- Cung cấp danh sách scenario khả dụng cho endpoint `/api/detection/profile`.

Ý nghĩa:

- Đây là lớp then chốt cho phép thay business logic mà không phải đổi UI hay graph API.
- Controller detection giờ phản ánh đúng trạng thái runtime thay vì trả một profile tĩnh.

### 7.3 `PatternRule<M>`

Đây là interface tổng quát cho các luật phát hiện pattern.

Ý nghĩa OOP + Generics:

- Interface biểu diễn hành vi chung của mọi detection rule.
- `M` cho phép mỗi rule mang metadata riêng nhưng vẫn dùng chung một khung kết quả `PatternMatch<M>`.

### 7.4 Các rule cụ thể

Các class rule hiện có:

- `SharedIdentifierRule`
- `SharedDeviceRule`
- `ChargebackTransactionRule`
- `TrianglePathRule`

Chức năng của từng rule:

- Các rule cũ vẫn tồn tại như các thành phần nghiệp vụ chuyên biệt cho scenario phù hợp.
- Ở kiến trúc hiện tại, chúng không còn đại diện cho toàn bộ tầng detection mà nằm bên dưới các implementation của `DetectionScenario`.
- Scenario `ASSOCIATION_GRAPH` ưu tiên các mẫu topology như node bậc cao, shared target và multi-type bridge.
- Scenario `EMAIL_URL_IP` tiếp tục dùng các rule mang ngữ nghĩa Email - URL - IP để minh họa khả năng thay thế bài toán.

Điểm mạnh:

- Mỗi rule độc lập, dễ thêm/bớt.
- Rule cùng dựa trên một graph model generic.

Hạn chế:

- Tên class `ChargebackTransactionRule` không còn thực sự khớp với ý nghĩa hiện tại là hosting cluster, có thể gây hiểu nhầm.
- Dù dùng profile support, logic ngữ nghĩa vẫn được xây quanh ba vai trò source/resource/infrastructure.

### 7.5 `PatternDetectionService`

Service này gọi `graphQueryService.fetchGraph()`, lấy scenario active từ registry và ủy quyền việc phát hiện pattern cho scenario đó.

Vai trò:

- Điều phối detection ở mức service.
- Giữ controller ổn định trong khi logic nghiệp vụ có thể thay đổi theo scenario.

Ưu điểm:

- Tách orchestration khỏi từng implementation cụ thể.
- Dễ mở rộng thêm scenario mới mà không phải sửa luồng gọi từ controller.

### 7.6 `RiskScoreStrategy<C, R>` và các implementation hiện có

`RiskScoreStrategy<C, R>` là generic strategy interface cho scoring.

Trong code hiện tại, các logic scoring cụ thể được dùng bên trong từng scenario tương ứng. Ví dụ `FraudEmailUrlIpRiskStrategy` phục vụ cho scenario `EMAIL_URL_IP`, còn scenario `ASSOCIATION_GRAPH` dùng cách đánh giá dựa trên topology của graph.

Ý nghĩa kiến trúc:

- Đây là strategy pattern điển hình.
- `C` là context đầu vào, `R` là kiểu kết quả.
- Nhờ đó hệ thống có thể thay chiến lược chấm điểm mà không cần đổi service orchestration hay graph UI.

Nhận xét:

- Kiến trúc tốt về mặt pattern.
- Scoring không phải generic tuyệt đối, nhưng đã được cô lập đúng chỗ để không làm hỏng tính tái sử dụng của lõi hệ thống.

### 7.7 `RiskAnalysisService`

Service này lấy graph, chọn scenario active và ủy quyền việc đánh giá rủi ro cho scenario đó, sau đó trả về `RiskResult`.

Vai trò:

- Điều phối risk analysis.
- Tách phần tính score ra khỏi controller.

---

## 8. Phân tích tài nguyên cấu hình

### 8.1 `application.properties`

File cấu hình đang chứa:

- URI Neo4j
- username Neo4j
- password Neo4j
- tên database
- cấu hình tắt cache Thymeleaf

Điểm rủi ro quan trọng:

- Thông tin xác thực Neo4j đang được đặt trực tiếp trong source tree.
- Đây là rủi ro bảo mật lớn nếu repo được đẩy công khai hoặc chia sẻ rộng.

Khuyến nghị:

- Chuyển credentials sang biến môi trường hoặc file secret ngoài repo.
- Dùng placeholder `${ENV_VAR}` trong `application.properties`.

### 8.2 `neo4j-fraud-test-data.cypher`

Tên file cho thấy dự án vẫn đang mang một dataset kiểm thử theo ngữ cảnh fraud. Dù graph core là generic, dataset đi kèm vẫn đang nghiêng về profile minh họa mặc định. Đây là điều nên được ghi nhận khi phân tích codebase.

---

## 9. Phân tích frontend tĩnh

Frontend nằm trong `src/main/resources/static` và gồm năm file chính:

- `index.html`
- `insights.html`
- `export.html`
- `styles.css`
- `theme.js`

### 9.1 `index.html`

Đây là giao diện chính `Generic Graph Explorer`.

Chức năng nổi bật:

- Tải graph từ `GET /api/graph`.
- Dựng đồ thị bằng `vis-network`.
- Tạo legend, bộ lọc type và relation động.
- Hiển thị chi tiết node/edge.
- Xóa node đang chọn qua `DELETE /api/graph/nodes/{nodeId}`.
- Hỗ trợ fit, zoom, reset, bật/tắt physics.

Điểm kỹ thuật quan trọng:

- Hàm `normalizeFacetKey(...)` chuẩn hóa key để gộp các biến thể type/relation.
- `buildFacets(...)` sinh thống kê cho bộ lọc.
- `getFilteredGraph()` là lõi lọc graph dựa trên lựa chọn người dùng.
- `buildNetwork()` dựng dữ liệu cho `vis.Network`.

Ý nghĩa kiến trúc:

- Frontend này đúng tinh thần data-driven.
- Nó không hard-code các thực thể như Email hay URL.
- Node label được lấy ưu tiên từ các thuộc tính hiển thị tự nhiên như `label`, `name`, `title`, sau đó mới fallback về `id`.
- Chỉ cần API giữ đúng schema tổng quát thì UI vẫn dùng lại được.

### 9.2 `insights.html`

Đây là trang tách riêng phần thông tin graph tổng hợp.

Chức năng:

- Tải graph từ `/api/graph`.
- Hiển thị số lượng node, edge, node type và relation.
- Sinh danh sách type và relation hoàn toàn theo dữ liệu hiện có.

Ý nghĩa:

- Giữ trang explorer tập trung vào trực quan hóa và thao tác.
- Chứng minh UI có thể tổ chức lại mà vẫn giữ nguyên lõi generic.

### 9.3 `export.html`

Đây là giao diện `Generic Graph Import / Export`.

Chức năng:

- Tải graph hiện tại từ `/api/graph`.
- Hiển thị JSON payload.
- Cho phép tải xuống file JSON.
- Nạp ví dụ dataset mẫu.
- Import dữ liệu mới qua `POST /api/graph/import`.
- Hiển thị lỗi import rõ ràng khi payload sai định dạng hoặc vi phạm kiểm tra cơ bản.

Ý nghĩa:

- Đây là phần làm cho hệ thống thực sự “mở” với dataset mới.
- Không chỉ xem graph, người dùng còn có thể thay graph ngay từ UI.

### 9.4 `theme.js`

File này quản lý chế độ sáng/tối.

Vai trò:

- Lưu theme vào `localStorage`.
- Đồng bộ với system theme.
- Phát `themechange` event để các phần khác cập nhật.

Điểm tốt:

- Tách logic giao diện phụ trợ ra file riêng.
- Không làm phình logic trong `index.html`.

### 9.5 `styles.css`

File CSS định nghĩa thiết kế chung cho cả explorer và import/export.

Đặc điểm:

- Dùng CSS variable cho theme light/dark.
- Có cấu trúc style khá đầy đủ cho card, panel, button, summary, graph stage.
- Giữ được giao diện nhất quán giữa hai trang.

Nhận xét:

- Frontend đủ mạnh để demo sản phẩm tốt.
- Tuy chưa phải dạng component-based frontend framework, nhưng với phạm vi đề tài sinh viên thì mức tách file hiện tại là hợp lý.

---

## 10. Phân tích phần test

Thư mục test hiện có hai file:

- `GraphApiControllerTest.java`
- `DetectionApiControllerTest.java`

### 10.1 `GraphApiControllerTest`

Test này dùng `@WebMvcTest` để kiểm tra lớp controller graph.

Nội dung chính:

- Kiểm tra `GET /api/graph` trả về response `OK` cùng dữ liệu node/edge đúng schema.
- Kiểm tra `POST /api/graph/import` nhận graph generic và trả `GraphImportResult` đúng.
- Kiểm tra `DELETE /api/graph/nodes/{nodeId}` trả kết quả xóa node đúng contract.

Ý nghĩa:

- Test đang xác nhận đúng contract của graph API.
- Đây là test quan trọng vì graph API là lõi giao tiếp giữa backend và frontend.

### 10.2 `DetectionApiControllerTest`

Test này kiểm tra ba endpoint detection:

- `/api/detection/patterns`
- `/api/detection/risk`
- `/api/detection/profile`

Ý nghĩa:

- Xác nhận response wrapper generic hoạt động đúng.
- Xác nhận controller detection trả ra cấu trúc JSON mà frontend hoặc người dùng API có thể tin cậy.

Hạn chế chung của nhóm test hiện tại:

- Chủ yếu là controller-level test.
- Chưa có integration test cho `GraphQueryService` với Neo4j thật.
- Chưa có test trực tiếp cho từng rule detection hay strategy scoring.

---

## 11. Luồng chạy end-to-end của hệ thống

### 11.1 Luồng xem graph

1. Người dùng mở `index.html`.
2. Frontend gọi `GET /api/graph`.
3. `GraphApiController` gọi `GraphQueryService.fetchGraph()`.
4. Service truy vấn Neo4j, chuẩn hóa dữ liệu về `GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>>`.
5. Response được bọc trong `ApiResponse.ok(...)`.
6. Frontend dựng network, legend, filter và panel chi tiết.

### 11.2 Luồng import graph

1. Người dùng mở `export.html`.
2. Người dùng dán JSON graph mới.
3. Frontend gọi `POST /api/graph/import?replace=...`.
4. `GraphApiController` chuyển payload vào `GraphQueryService.importGraph(...)`.
5. Service validate payload, sau đó ghi node/edge vào Neo4j.
6. Frontend tải lại graph để phản ánh dữ liệu mới.

### 11.3 Luồng xóa node

1. Người dùng chọn node trên `index.html`.
2. Frontend bật nút xóa trong panel chi tiết.
3. Người dùng xác nhận thao tác xóa.
4. Frontend gọi `DELETE /api/graph/nodes/{nodeId}`.
5. `GraphApiController` chuyển yêu cầu vào `GraphQueryService.deleteNodeById(...)`.
6. Service xóa node và các cạnh liên quan, sau đó frontend tải lại graph.

### 11.4 Luồng detection/risk

1. Client gọi API detection.
2. Controller detection gọi service tương ứng.
3. Service lấy graph từ `GraphQueryService.fetchGraph()`.
4. Service lấy scenario active từ `DetectionScenarioRegistry`.
5. Scenario hoặc strategy tương ứng chạy trên graph đó.
6. Kết quả được tổng hợp thành `DetectionResult` hoặc `RiskResult`.

---

## 12. Đánh giá kiến trúc hiện tại

### 12.1 Điểm mạnh

- Đã xây dựng được graph core tổng quát, không bám chặt một entity domain cụ thể.
- Dùng Generics đúng chỗ ở DTO, response wrapper, strategy và rule abstraction.
- Frontend data-driven đủ tốt để chứng minh tính tái sử dụng.
- Có import/export graph nên giá trị demo rất rõ.
- Có test controller cho hai nhóm API chính.

### 12.2 Điểm còn hạn chế

- Tầng detection không còn phụ thuộc một profile tĩnh, nhưng vẫn là business logic theo scenario chứ chưa phải phân tích generic tuyệt đối.
- `application.properties` đang chứa credentials thật, là rủi ro bảo mật đáng kể.
- Chưa có integration test cho luồng import/xóa với Neo4j thật.
- Chưa có test đơn vị đủ sâu cho từng scenario và chiến lược scoring.
- Tên một số class detection chưa phản ánh chính xác vai trò hiện tại.

### 12.3 Nhận định tổng thể

Nếu xét riêng mục tiêu “tối ưu hóa mã nguồn và tăng tính tái sử dụng”, phần thành công nhất của codebase nằm ở graph core, DTO layer, API layer và frontend explorer/insights/import-export. Nếu xét mục tiêu “generic hoàn toàn ở mọi tầng”, codebase hiện mới đạt một phần vì detection vẫn là lớp mở rộng mang scenario nghiệp vụ cụ thể.

Nói cách khác, hệ thống hiện tại đã có một nền móng generic đủ rõ để làm đề tài NCKH, nhưng vẫn còn một số điểm domain-specific có thể tiếp tục được khái quát hóa trong các vòng phát triển sau.

---

## 13. Hướng mở rộng nên làm tiếp

1. Chuyển credentials Neo4j ra khỏi source tree.
2. Thêm validation chi tiết cho payload import graph.
3. Bổ sung integration test với Neo4j thật hoặc Neo4j test container.
4. Viết thêm scenario mới để chứng minh khả năng thay business logic trên cùng một graph core.
5. Nếu tiếp tục theo OOP + Generics, có thể tái sử dụng `BaseService<T, ID>` cho các module khác hoặc phát triển một generic repository/service pattern rõ ràng hơn.

---

## 14. Kết luận của tài liệu phân tích src

Toàn bộ thư mục `src` hiện tại cho thấy dự án đã đi theo một hướng rất rõ: biến một bài toán graph vốn dễ bị khóa vào domain cụ thể thành một nền tảng graph explorer có tính tổng quát cao hơn. Phần backend thể hiện điều đó qua DTO generic, graph service tổng quát và strategy/rule abstraction. Phần frontend thể hiện điều đó qua thiết kế data-driven. Phần test dù còn mỏng nhưng đã xác nhận được API contract của hai luồng chức năng quan trọng nhất.

Về mặt học thuật, đây là một codebase phù hợp để trình bày trong đề tài NCKH về Generics vì có đủ ví dụ trực tiếp cho các khái niệm:

- Generic DTO
- Generic response
- Generic service pattern
- Generic rule abstraction
- Generic strategy abstraction
- Tái sử dụng giao diện dựa trên cùng graph schema

Về mặt kỹ thuật, codebase cũng đủ thực dụng vì đã có khả năng xem, lọc, import và phân tích graph thật. Điểm cần làm tiếp không nằm ở việc viết lại toàn bộ, mà nằm ở việc làm sâu hơn phần generic hóa cho detection, củng cố kiểm thử và xử lý an toàn cấu hình môi trường.