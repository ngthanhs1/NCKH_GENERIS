// Dữ liệu mẫu bài toán phát hiện gian lận (Fraud Detection)
// Schema đúng với app hiện tại:
//   - Node: (:Node {id, type, attr_*...})
//   - Edge: (a)-[:RELATION {type, attr_*...}]->(b)
//
// Quy ước an toàn:
//   - Tất cả id đều có prefix "FD_" để dễ dọn dữ liệu.
//   - Bạn có thể xoá sạch dataset này bằng:
//       MATCH (n:Node) WHERE toString(n.id) STARTS WITH 'FD_' DETACH DELETE n;

// --------------------
// 1) PERSON (khách hàng)
// --------------------
MERGE (n:Node {id:'FD_P_ALICE'})
  SET n.type='PERSON',
      n.attr_name='Alice Nguyen',
      n.attr_riskTier='MEDIUM',
      n.attr_country='VN';

MERGE (n:Node {id:'FD_P_BOB'})
  SET n.type='PERSON',
      n.attr_name='Bob Tran',
      n.attr_riskTier='HIGH',
      n.attr_country='VN';

MERGE (n:Node {id:'FD_P_CHARLIE'})
  SET n.type='PERSON',
      n.attr_name='Charlie Le',
      n.attr_riskTier='LOW',
      n.attr_country='VN';

MERGE (n:Node {id:'FD_P_DAN'})
  SET n.type='PERSON',
      n.attr_name='Dan Pham',
      n.attr_riskTier='HIGH',
      n.attr_country='VN';

// --------------------
// 2) ACCOUNT (tài khoản/ ví)
// --------------------
MERGE (n:Node {id:'FD_A_ALICE_1'})
  SET n.type='ACCOUNT', n.attr_provider='WalletX', n.attr_status='ACTIVE';
MERGE (n:Node {id:'FD_A_BOB_1'})
  SET n.type='ACCOUNT', n.attr_provider='WalletX', n.attr_status='ACTIVE';
MERGE (n:Node {id:'FD_A_CHARLIE_1'})
  SET n.type='ACCOUNT', n.attr_provider='WalletX', n.attr_status='ACTIVE';
MERGE (n:Node {id:'FD_A_DAN_1'})
  SET n.type='ACCOUNT', n.attr_provider='WalletX', n.attr_status='ACTIVE';

// --------------------
// 3) DEVICE & IP (thiết bị / địa chỉ IP)
// --------------------
MERGE (n:Node {id:'FD_D_PHONE_01'})
  SET n.type='DEVICE', n.attr_deviceType='PHONE', n.attr_os='Android', n.attr_fingerprint='fp_01';
MERGE (n:Node {id:'FD_D_PHONE_02'})
  SET n.type='DEVICE', n.attr_deviceType='PHONE', n.attr_os='Android', n.attr_fingerprint='fp_02';
MERGE (n:Node {id:'FD_D_EMU_01'})
  SET n.type='DEVICE', n.attr_deviceType='EMULATOR', n.attr_os='Android', n.attr_fingerprint='fp_emu_01';

MERGE (n:Node {id:'FD_IP_1'})
  SET n.type='IP', n.attr_ip='203.0.113.10', n.attr_asn='AS-EXAMPLE', n.attr_geo='HCM';
MERGE (n:Node {id:'FD_IP_2'})
  SET n.type='IP', n.attr_ip='203.0.113.11', n.attr_asn='AS-EXAMPLE', n.attr_geo='HCM';
MERGE (n:Node {id:'FD_IP_3'})
  SET n.type='IP', n.attr_ip='198.51.100.77', n.attr_asn='AS-FOREIGN', n.attr_geo='SG';

// --------------------
// 4) MERCHANT (đơn vị nhận tiền)
// --------------------
MERGE (n:Node {id:'FD_M_SHOP_A'})
  SET n.type='MERCHANT', n.attr_name='Shop A', n.attr_category='E-COMMERCE';
MERGE (n:Node {id:'FD_M_SHOP_B'})
  SET n.type='MERCHANT', n.attr_name='Shop B', n.attr_category='DIGITAL_GOODS';

// --------------------
// 5) EMAIL & PHONE (định danh)
// --------------------
MERGE (n:Node {id:'FD_E_ALICE'})
  SET n.type='EMAIL', n.attr_email='alice@example.com';
MERGE (n:Node {id:'FD_E_BOB'})
  SET n.type='EMAIL', n.attr_email='bob+alias@example.com';
MERGE (n:Node {id:'FD_E_SHARED'})
  SET n.type='EMAIL', n.attr_email='shared_mailbox@example.com';

MERGE (n:Node {id:'FD_PH_ALICE'})
  SET n.type='PHONE', n.attr_phone='+84901110001';
MERGE (n:Node {id:'FD_PH_BOB'})
  SET n.type='PHONE', n.attr_phone='+84901110002';
MERGE (n:Node {id:'FD_PH_SHARED'})
  SET n.type='PHONE', n.attr_phone='+84901119999';

// --------------------
// 6) TRANSACTION (giao dịch) — dùng làm ví dụ edge đa dạng
// --------------------
MERGE (n:Node {id:'FD_T_1001'})
  SET n.type='TRANSACTION', n.attr_amount=9500000, n.attr_currency='VND', n.attr_ts='2026-01-10T10:15:00Z', n.attr_channel='APP';
MERGE (n:Node {id:'FD_T_1002'})
  SET n.type='TRANSACTION', n.attr_amount=9800000, n.attr_currency='VND', n.attr_ts='2026-01-10T10:18:00Z', n.attr_channel='APP';
MERGE (n:Node {id:'FD_T_1003'})
  SET n.type='TRANSACTION', n.attr_amount=12000000, n.attr_currency='VND', n.attr_ts='2026-01-10T10:20:00Z', n.attr_channel='APP';
MERGE (n:Node {id:'FD_T_1004'})
  SET n.type='TRANSACTION', n.attr_amount=500000, n.attr_currency='VND', n.attr_ts='2026-01-10T11:00:00Z', n.attr_channel='WEB';

// --------------------
// 7) RELATIONSHIPS
// --------------------
// Person -> Account (OWNS)
MATCH (p:Node {id:'FD_P_ALICE'}), (a:Node {id:'FD_A_ALICE_1'})
MERGE (p)-[:RELATION {type:'OWNS', attr_since:'2024-06-01'}]->(a);

MATCH (p:Node {id:'FD_P_BOB'}), (a:Node {id:'FD_A_BOB_1'})
MERGE (p)-[:RELATION {type:'OWNS', attr_since:'2025-02-10'}]->(a);

MATCH (p:Node {id:'FD_P_CHARLIE'}), (a:Node {id:'FD_A_CHARLIE_1'})
MERGE (p)-[:RELATION {type:'OWNS', attr_since:'2023-09-21'}]->(a);

MATCH (p:Node {id:'FD_P_DAN'}), (a:Node {id:'FD_A_DAN_1'})
MERGE (p)-[:RELATION {type:'OWNS', attr_since:'2025-11-11'}]->(a);

// Person -> Email/Phone (REGISTERED)
MATCH (p:Node {id:'FD_P_ALICE'}), (e:Node {id:'FD_E_ALICE'})
MERGE (p)-[:RELATION {type:'REGISTERED_EMAIL'}]->(e);
MATCH (p:Node {id:'FD_P_ALICE'}), (ph:Node {id:'FD_PH_ALICE'})
MERGE (p)-[:RELATION {type:'REGISTERED_PHONE'}]->(ph);

MATCH (p:Node {id:'FD_P_BOB'}), (e:Node {id:'FD_E_BOB'})
MERGE (p)-[:RELATION {type:'REGISTERED_EMAIL'}]->(e);
MATCH (p:Node {id:'FD_P_BOB'}), (ph:Node {id:'FD_PH_BOB'})
MERGE (p)-[:RELATION {type:'REGISTERED_PHONE'}]->(ph);

// Shared identifiers (tín hiệu nghi vấn)
MATCH (p:Node {id:'FD_P_ALICE'}), (e:Node {id:'FD_E_SHARED'})
MERGE (p)-[:RELATION {type:'USES_EMAIL', attr_note:'shared inbox'}]->(e);
MATCH (p:Node {id:'FD_P_BOB'}), (e:Node {id:'FD_E_SHARED'})
MERGE (p)-[:RELATION {type:'USES_EMAIL', attr_note:'shared inbox'}]->(e);

MATCH (p:Node {id:'FD_P_BOB'}), (ph:Node {id:'FD_PH_SHARED'})
MERGE (p)-[:RELATION {type:'USES_PHONE', attr_note:'shared phone'}]->(ph);
MATCH (p:Node {id:'FD_P_DAN'}), (ph:Node {id:'FD_PH_SHARED'})
MERGE (p)-[:RELATION {type:'USES_PHONE', attr_note:'shared phone'}]->(ph);

// Account -> Device/IP (LOGIN / USED)
MATCH (a:Node {id:'FD_A_ALICE_1'}), (d:Node {id:'FD_D_PHONE_01'})
MERGE (a)-[:RELATION {type:'USED_DEVICE', attr_firstSeen:'2025-12-01'}]->(d);
MATCH (a:Node {id:'FD_A_ALICE_1'}), (ip:Node {id:'FD_IP_1'})
MERGE (a)-[:RELATION {type:'USED_IP', attr_firstSeen:'2025-12-01'}]->(ip);

MATCH (a:Node {id:'FD_A_BOB_1'}), (d:Node {id:'FD_D_EMU_01'})
MERGE (a)-[:RELATION {type:'USED_DEVICE', attr_firstSeen:'2026-01-10'}]->(d);
MATCH (a:Node {id:'FD_A_BOB_1'}), (ip:Node {id:'FD_IP_1'})
MERGE (a)-[:RELATION {type:'USED_IP', attr_firstSeen:'2026-01-10'}]->(ip);

MATCH (a:Node {id:'FD_A_DAN_1'}), (d:Node {id:'FD_D_EMU_01'})
MERGE (a)-[:RELATION {type:'USED_DEVICE', attr_firstSeen:'2026-01-10'}]->(d);
MATCH (a:Node {id:'FD_A_DAN_1'}), (ip:Node {id:'FD_IP_2'})
MERGE (a)-[:RELATION {type:'USED_IP', attr_firstSeen:'2026-01-10'}]->(ip);

// Transaction flow
// Account -> Transaction (INITIATED)
MATCH (a:Node {id:'FD_A_BOB_1'}), (t:Node {id:'FD_T_1001'})
MERGE (a)-[:RELATION {type:'INITIATED', attr_method:'QR'}]->(t);
MATCH (a:Node {id:'FD_A_BOB_1'}), (t:Node {id:'FD_T_1002'})
MERGE (a)-[:RELATION {type:'INITIATED', attr_method:'QR'}]->(t);
MATCH (a:Node {id:'FD_A_DAN_1'}), (t:Node {id:'FD_T_1003'})
MERGE (a)-[:RELATION {type:'INITIATED', attr_method:'CARD_ON_FILE'}]->(t);
MATCH (a:Node {id:'FD_A_CHARLIE_1'}), (t:Node {id:'FD_T_1004'})
MERGE (a)-[:RELATION {type:'INITIATED', attr_method:'BANK_TRANSFER'}]->(t);

// Transaction -> Merchant (PAID_TO)
MATCH (t:Node {id:'FD_T_1001'}), (m:Node {id:'FD_M_SHOP_B'})
MERGE (t)-[:RELATION {type:'PAID_TO', attr_status:'SETTLED'}]->(m);
MATCH (t:Node {id:'FD_T_1002'}), (m:Node {id:'FD_M_SHOP_B'})
MERGE (t)-[:RELATION {type:'PAID_TO', attr_status:'SETTLED'}]->(m);
MATCH (t:Node {id:'FD_T_1003'}), (m:Node {id:'FD_M_SHOP_B'})
MERGE (t)-[:RELATION {type:'PAID_TO', attr_status:'CHARGEBACK', attr_chargebackReason:'FRAUD_SUSPECTED'}]->(m);
MATCH (t:Node {id:'FD_T_1004'}), (m:Node {id:'FD_M_SHOP_A'})
MERGE (t)-[:RELATION {type:'PAID_TO', attr_status:'SETTLED'}]->(m);

// Suspicious links: shared device + rapid transactions
MATCH (p1:Node {id:'FD_P_BOB'}), (p2:Node {id:'FD_P_DAN'}), (d:Node {id:'FD_D_EMU_01'})
MERGE (p1)-[:RELATION {type:'ASSOCIATED_WITH', attr_reason:'shared device'}]->(p2)
MERGE (p1)-[:RELATION {type:'TOUCHED_DEVICE'}]->(d)
MERGE (p2)-[:RELATION {type:'TOUCHED_DEVICE'}]->(d);

// Money movement: Bob -> Dan (TRANSFER_TO)
MATCH (a1:Node {id:'FD_A_BOB_1'}), (a2:Node {id:'FD_A_DAN_1'})
MERGE (a1)-[:RELATION {type:'TRANSFER_TO', attr_amount=15000000, attr_ts='2026-01-10T10:25:00Z'}]->(a2);

// Foreign IP for high risk account
MATCH (a:Node {id:'FD_A_DAN_1'}), (ip:Node {id:'FD_IP_3'})
MERGE (a)-[:RELATION {type:'USED_IP', attr_firstSeen:'2026-01-10', attr_note:'geo mismatch'}]->(ip);
