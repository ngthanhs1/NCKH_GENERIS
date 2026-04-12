// Dữ liệu test theo chuẩn NCKH (đã ADAPT theo giới hạn Neo4j):
// - Neo4j không cho phép Map/JSON lồng trong property.
// - Vì vậy attributes được flatten thành các key-value phẳng (prefix attr_).
// - Khi xuất API, controller sẽ gom lại thành trường attributes (Map).

CREATE
(n1:Node {
  id:"N1",
  type:"Person",
  attr_name:"Alice",
  attr_age:20
}),
(n2:Node {
  id:"N2",
  type:"Person",
  attr_name:"Bob"
}),
(n3:Node {
  id:"N3",
  type:"Course",
  attr_title:"AI"
}),

(n1)-[:RELATION {type:"FRIEND"}]->(n2),
(n1)-[:RELATION {type:"ENROLL"}]->(n3);

