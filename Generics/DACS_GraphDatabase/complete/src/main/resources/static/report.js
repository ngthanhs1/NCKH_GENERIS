const statusText = document.getElementById('statusText');
const statusDot = document.getElementById('statusDot');
const kpisEl = document.getElementById('kpis');
const rulesEl = document.getElementById('rules');
const topSharedEl = document.getElementById('topShared');
const tablesEl = document.getElementById('tables');
const metaEl = document.getElementById('meta');

const btnPrint = document.getElementById('btnPrint');
const btnCsv = document.getElementById('btnCsv');
const btnBack = document.getElementById('btnBack');

function setStatus(state, text) {
	statusText.textContent = text;
	if (state === 'ok') statusDot.style.background = 'green';
	else if (state === 'warn') statusDot.style.background = 'orange';
	else statusDot.style.background = '#999';
}

function escapeHtml(s) {
	return String(s ?? '')
		.replaceAll('&', '&amp;')
		.replaceAll('<', '&lt;')
		.replaceAll('>', '&gt;')
		.replaceAll('"', '&quot;')
		.replaceAll("'", '&#39;');
}

function normalizedVerdict(v) {
	const raw = String(v ?? '').trim();
	const upper = raw.toUpperCase();
	if (upper === 'SAFE' || upper.includes('SAFE')) return 'SAFE';
	if (upper === 'SUSPICIOUS' || upper.includes('SUSPICIOUS')) return 'SUSPICIOUS';
	if (upper === 'FRAUD' || upper.includes('FRAUD')) return 'FRAUD';
	if (upper === 'UNKNOWN' || upper.includes('UNKNOWN')) return 'UNKNOWN';
	if (upper.includes('AN TOÀN') || upper.includes('AN TOAN')) return 'SAFE';
	if (upper.includes('NGHI NGỜ') || upper.includes('NGHI NGO') || upper.includes('ĐÁNG NGHI') || upper.includes('DANG NGHI')) return 'SUSPICIOUS';
	if (upper.includes('GIAN LẬN') || upper.includes('GIAN LAN')) return 'FRAUD';
	if (upper.includes('KHÔNG XÁC') || upper.includes('KHONG XAC') || upper.includes('CHƯA XÁC') || upper.includes('CHUA XAC')) return 'UNKNOWN';
	return 'UNKNOWN';
}

function primaryType(node) {
	const labels = Array.isArray(node.type) ? node.type : [];
	if (labels.includes('Email')) return 'Email';
	if (labels.includes('URL')) return 'URL';
	if (labels.includes('IPAddress')) return 'IPAddress';
	return labels[0] ?? 'UNKNOWN';
}

function linkEndId(end) {
	if (end == null) return null;
	if (typeof end === 'string') return end;
	if (typeof end === 'object') return end.id ?? null;
	return String(end);
}

function computeGraphStats(nodes, links) {
	const byId = new Map(nodes.map(n => [n.id, n]));

	const out = new Map(); // id -> {CONTAINS_LINK:Set, SENT_FROM:Set, HOSTED_BY:Set}
	const inCount = {
		urlEmail: new Map(), // urlId -> count email
		ipEmail: new Map(),  // ipId -> count email
		ipUrl: new Map(),    // ipId -> count url
	};

	function ensureOut(id) {
		if (!out.has(id)) {
			out.set(id, {
				CONTAINS_LINK: new Set(),
				SENT_FROM: new Set(),
				HOSTED_BY: new Set(),
			});
		}
		return out.get(id);
	}

	for (const l of links) {
		const s = linkEndId(l.source);
		const t = linkEndId(l.target);
		if (!s || !t) continue;
		const type = l.type;
		if (!type) continue;

		ensureOut(s)[type]?.add(t);

		const sNode = byId.get(s);
		const tNode = byId.get(t);
		if (!sNode || !tNode) continue;
		const sType = primaryType(sNode);
		const tType = primaryType(tNode);

		if (type === 'CONTAINS_LINK' && sType === 'Email' && tType === 'URL') {
			inCount.urlEmail.set(t, (inCount.urlEmail.get(t) ?? 0) + 1);
		}
		if (type === 'SENT_FROM' && sType === 'Email' && tType === 'IPAddress') {
			inCount.ipEmail.set(t, (inCount.ipEmail.get(t) ?? 0) + 1);
		}
		if (type === 'HOSTED_BY' && sType === 'URL' && tType === 'IPAddress') {
			inCount.ipUrl.set(t, (inCount.ipUrl.get(t) ?? 0) + 1);
		}
	}

	return { byId, out, inCount };
}

function hasTriadForEmail(emailId, stats) {
	const bucket = stats.out.get(emailId);
	if (!bucket) return false;
	const urls = bucket.CONTAINS_LINK;
	const ips = bucket.SENT_FROM;
	if (!urls || !ips || urls.size === 0 || ips.size === 0) return false;

	for (const urlId of urls) {
		const urlOut = stats.out.get(urlId);
		if (!urlOut) continue;
		for (const ipId of urlOut.HOSTED_BY ?? []) {
			if (ips.has(ipId)) return true;
		}
	}
	return false;
}

function hasTriadForUrl(urlId, stats) {
	const urlOut = stats.out.get(urlId);
	if (!urlOut || (urlOut.HOSTED_BY?.size ?? 0) === 0) return false;

	// triad nếu có ít nhất 1 Email trỏ tới URL và Email đó có SENT_FROM tới đúng IP host
	const byId = stats.byId;
	for (const [src, bucket] of stats.out.entries()) {
		const n = byId.get(src);
		if (!n || primaryType(n) !== 'Email') continue;
		if (!bucket.CONTAINS_LINK?.has(urlId)) continue;
		if (hasTriadForEmail(src, stats)) return true;
	}
	return false;
}

function hasTriadForIp(ipId, stats) {
	// triad nếu tồn tại Email SENT_FROM ip và Email chứa URL mà URL HOSTED_BY ip
	const byId = stats.byId;
	for (const [src, bucket] of stats.out.entries()) {
		const n = byId.get(src);
		if (!n || primaryType(n) !== 'Email') continue;
		if (!bucket.SENT_FROM?.has(ipId)) continue;
		if (!bucket.CONTAINS_LINK || bucket.CONTAINS_LINK.size === 0) continue;
		for (const urlId of bucket.CONTAINS_LINK) {
			const urlOut = stats.out.get(urlId);
			if (urlOut?.HOSTED_BY?.has(ipId)) return true;
		}
	}
	return false;
}

function buildKpis(health, nodes, links) {
	const c = health?.counts ?? {};
	const byVerdict = { FRAUD: 0, SUSPICIOUS: 0, SAFE: 0, UNKNOWN: 0 };
	for (const n of nodes) {
		const v = normalizedVerdict(n.verdict);
		byVerdict[v] = (byVerdict[v] ?? 0) + 1;
	}

	const items = [
		{ l: 'Email', v: c.Email ?? nodes.filter(n => primaryType(n) === 'Email').length },
		{ l: 'URL', v: c.URL ?? nodes.filter(n => primaryType(n) === 'URL').length },
		{ l: 'IP', v: c.IPAddress ?? nodes.filter(n => primaryType(n) === 'IPAddress').length },
		{ l: 'Cạnh', v: c.relationships ?? links.length },
		{ l: 'FRAUD', v: byVerdict.FRAUD },
		{ l: 'SUSPICIOUS', v: byVerdict.SUSPICIOUS },
		{ l: 'SAFE', v: byVerdict.SAFE },
		{ l: 'UNKNOWN', v: byVerdict.UNKNOWN },
	];

	kpisEl.innerHTML = items.map(it => `
		<div class="kpi">
			<div class="l">${escapeHtml(it.l)}</div>
			<div class="v">${escapeHtml(it.v)}</div>
		</div>
	`).join('');
}

function buildTable(nodes) {
	// deprecated: giữ lại để không lỗi, nhưng render mới dùng buildTables()
}

function buildRules() {
	rulesEl.innerHTML = `
		<div>Điều kiện “đủ bộ 3” (Email + URL + IP): tồn tại tam giác <span class="mono">Email-[:CONTAINS_LINK]->URL-[:HOSTED_BY]->IP</span> và đồng thời <span class="mono">Email-[:SENT_FROM]->IP</span>.</div>
		<div style="margin-top:8px">
			<strong>Email score (0–100, cắt ngưỡng 100)</strong>:
			<ul style="margin:6px 0 0 18px; padding:0; color:inherit">
				<li>+30: có URL</li>
				<li>+20: có IP nguồn</li>
				<li>+30: shared IP (IP nguồn được >1 Email dùng)</li>
				<li>+30: shared URL (URL xuất hiện trong >1 Email)</li>
			</ul>
		</div>
		<div style="margin-top:8px"><strong>URL/IP score</strong> (minh họa): +30 nếu được >1 Email dùng chung; nếu chưa tham gia bộ 3 thì <strong>UNKNOWN</strong>.</div>
	`;
}

function renderSmallTable(title, rows, columns) {
	if (!rows || rows.length === 0) {
		return `<div class="muted">${escapeHtml(title)}: chưa có dữ liệu.</div>`;
	}
	return `
		<div style="margin-top:4px; font-weight:700">${escapeHtml(title)}</div>
		<table class="report-table" style="margin-top:8px">
			<thead><tr>${columns.map(c => `<th>${escapeHtml(c.h)}</th>`).join('')}</tr></thead>
			<tbody>
				${rows.map(r => `<tr>${columns.map(c => `<td class="${c.mono ? 'mono' : ''}">${escapeHtml(r[c.k])}</td>`).join('')}</tr>`).join('')}
			</tbody>
		</table>
	`;
}

function buildTopShared(stats) {
	const { byId, inCount } = stats;
	const topUrls = Array.from(inCount.urlEmail.entries())
		.filter(([, c]) => c > 1)
		.sort((a, b) => b[1] - a[1])
		.slice(0, 10)
		.map(([id, c]) => ({
			id,
			value: byId.get(id)?.label ?? byId.get(id)?.value ?? byId.get(id)?.label ?? id,
			emailCount: c,
		}));

	const topIps = Array.from(inCount.ipEmail.entries())
		.filter(([, c]) => c > 1)
		.sort((a, b) => b[1] - a[1])
		.slice(0, 10)
		.map(([id, c]) => ({
			id,
			value: byId.get(id)?.label ?? byId.get(id)?.value ?? id,
			emailCount: c,
			urlCount: inCount.ipUrl.get(id) ?? 0,
		}));

	topSharedEl.innerHTML =
		renderSmallTable('Top URL được nhiều Email trỏ tới', topUrls, [
			{ h: 'Email count', k: 'emailCount' },
			{ h: 'Value', k: 'value' },
			{ h: 'ID', k: 'id', mono: true },
		]) +
		renderSmallTable('Top IP được nhiều Email sử dụng', topIps, [
			{ h: 'Email count', k: 'emailCount' },
			{ h: 'URL hosted', k: 'urlCount' },
			{ h: 'Value', k: 'value' },
			{ h: 'ID', k: 'id', mono: true },
		]);
}

function reasonsForEmail(emailId, stats) {
	const bucket = stats.out.get(emailId);
	const urls = bucket?.CONTAINS_LINK ?? new Set();
	const ips = bucket?.SENT_FROM ?? new Set();
	const hasTriad = hasTriadForEmail(emailId, stats);
	const reasons = [];

	if (!hasTriad) {
		reasons.push('Chưa đủ bộ 3 (Email+URL+IP) → UNKNOWN');
		return { hasTriad, reasons, score: 0 };
	}

	let score = 0;
	if (urls.size > 0) { score += 30; reasons.push('Có URL (CONTAINS_LINK) +30'); }
	if (ips.size > 0) { score += 20; reasons.push('Có IP nguồn (SENT_FROM) +20'); }

	const ipShared = Array.from(ips).some(ipId => (stats.inCount.ipEmail.get(ipId) ?? 0) > 1);
	if (ipShared) { score += 30; reasons.push('Shared IP: IP nguồn được >1 Email dùng +30'); }

	const urlShared = Array.from(urls).some(urlId => (stats.inCount.urlEmail.get(urlId) ?? 0) > 1);
	if (urlShared) { score += 30; reasons.push('Shared URL: URL xuất hiện trong >1 Email +30'); }

	if (score > 100) score = 100;
	return { hasTriad, reasons, score };
}

function reasonsForUrl(urlId, stats) {
	const hasTriad = hasTriadForUrl(urlId, stats);
	const emailCount = stats.inCount.urlEmail.get(urlId) ?? 0;
	const reasons = [];
	if (!hasTriad) {
		reasons.push('Chưa tham gia bộ 3 (tam giác) → UNKNOWN');
		return { hasTriad, reasons, score: 0, emailCount };
	}
	let score = 0;
	if (emailCount > 1) { score += 30; reasons.push(`Shared URL: được ${emailCount} Email trỏ tới +30`); }
	return { hasTriad, reasons, score, emailCount };
}

function reasonsForIp(ipId, stats) {
	const hasTriad = hasTriadForIp(ipId, stats);
	const emailCount = stats.inCount.ipEmail.get(ipId) ?? 0;
	const urlCount = stats.inCount.ipUrl.get(ipId) ?? 0;
	const reasons = [];
	if (!hasTriad) {
		reasons.push('Chưa tham gia bộ 3 (tam giác) → UNKNOWN');
		return { hasTriad, reasons, score: 0, emailCount, urlCount };
	}
	let score = 0;
	if (emailCount > 1) { score += 30; reasons.push(`Shared IP: được ${emailCount} Email dùng +30`); }
	if (urlCount > 0) reasons.push(`HOSTED_BY: đang host ${urlCount} URL`);
	return { hasTriad, reasons, score, emailCount, urlCount };
}

function buildTables(nodes, links) {
	const stats = computeGraphStats(nodes, links);

	const rowsEmail = [];
	const rowsUrl = [];
	const rowsIp = [];

	for (const n of nodes) {
		const t = primaryType(n);
		const verdictDb = normalizedVerdict(n.verdict);
		const scoreDb = Number(n.riskScore ?? 0);
		if (t === 'Email') {
			const r = reasonsForEmail(n.id, stats);
			rowsEmail.push({
				type: 'Email',
				verdict: verdictDb,
				score: scoreDb,
				hasTriad: r.hasTriad ? 'YES' : 'NO',
				value: n.label ?? n.id,
				reasons: r.reasons.join(' | '),
				id: n.id,
			});
		} else if (t === 'URL') {
			const r = reasonsForUrl(n.id, stats);
			rowsUrl.push({
				type: 'URL',
				verdict: verdictDb,
				score: scoreDb,
				hasTriad: r.hasTriad ? 'YES' : 'NO',
				emailCount: String(r.emailCount),
				value: n.label ?? n.id,
				reasons: r.reasons.join(' | '),
				id: n.id,
			});
		} else if (t === 'IPAddress') {
			const r = reasonsForIp(n.id, stats);
			rowsIp.push({
				type: 'IP',
				verdict: verdictDb,
				score: scoreDb,
				hasTriad: r.hasTriad ? 'YES' : 'NO',
				emailCount: String(r.emailCount),
				urlCount: String(r.urlCount),
				value: n.label ?? n.id,
				reasons: r.reasons.join(' | '),
				id: n.id,
			});
		}
	}

	function sortRows(a, b) {
		const va = a.verdict;
		const vb = b.verdict;
		const rank = (v) => (v === 'FRAUD' ? 0 : (v === 'SUSPICIOUS' ? 1 : (v === 'UNKNOWN' ? 2 : 3)));
		return (rank(va) - rank(vb)) || (Number(b.score) - Number(a.score)) || String(a.id).localeCompare(String(b.id));
	}

	rowsEmail.sort(sortRows);
	rowsUrl.sort(sortRows);
	rowsIp.sort(sortRows);

	const makeTable = (title, rows, extraCols) => {
		if (rows.length === 0) return `<div class="muted">${escapeHtml(title)}: chưa có dữ liệu.</div>`;
		const cols = [
			{ h: 'Verdict', k: 'verdict' },
			{ h: 'Score', k: 'score' },
			{ h: 'Bộ 3', k: 'hasTriad' },
			...extraCols,
			{ h: 'Value', k: 'value' },
			{ h: 'Lý do', k: 'reasons' },
			{ h: 'ID', k: 'id', mono: true },
		];

		return `
			<div style="margin-top:4px; font-weight:800">${escapeHtml(title)}</div>
			<table class="report-table" style="margin-top:8px">
				<thead><tr>${cols.map(c => `<th>${escapeHtml(c.h)}</th>`).join('')}</tr></thead>
				<tbody>
					${rows.map(r => `<tr>${cols.map(c => `<td class="${c.mono ? 'mono' : ''}">${escapeHtml(r[c.k] ?? '')}</td>`).join('')}</tr>`).join('')}
				</tbody>
			</table>
		`;
	};

	tablesEl.innerHTML =
		makeTable('Email', rowsEmail, []) +
		makeTable('URL', rowsUrl, [
			{ h: 'Email count', k: 'emailCount' },
		]) +
		makeTable('IP', rowsIp, [
			{ h: 'Email count', k: 'emailCount' },
			{ h: 'URL hosted', k: 'urlCount' },
		]);

	return { stats, rowsEmail, rowsUrl, rowsIp };
}

function downloadCsv(filename, rows) {
	const header = Object.keys(rows[0] ?? { id: '', type: '', verdict: '', riskScore: 0, value: '' });
	const esc = (v) => {
		const s = String(v ?? '');
		if (/[\n\r\t,\"]/g.test(s)) return '"' + s.replaceAll('"', '""') + '"';
		return s;
	};
	const csv = [header.join(',')].concat(rows.map(r => header.map(h => esc(r[h])).join(','))).join('\n');
	const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
	const url = URL.createObjectURL(blob);
	const a = document.createElement('a');
	a.href = url;
	a.download = filename;
	document.body.appendChild(a);
	a.click();
	a.remove();
	URL.revokeObjectURL(url);
}

async function boot() {
	try {
		setStatus('warn', 'Đang phân tích & tải dữ liệu…');

		// đảm bảo verdict/score mới nhất
		await fetch('/api/graph/analyze');

		const [fullRes, healthRes] = await Promise.all([
			fetch('/api/graph/full'),
			fetch('/api/graph/health'),
		]);

		const full = await fullRes.json();
		const health = healthRes.ok ? await healthRes.json() : null;

		const nodes = Array.isArray(full?.nodes) ? full.nodes : [];
		const links = Array.isArray(full?.links) ? full.links : [];

		metaEl.textContent = `Thời điểm tạo: ${new Date().toLocaleString()} • Nodes: ${nodes.length} • Links: ${links.length}`;

		buildKpis(health, nodes, links);
		buildRules();
		const built = buildTables(nodes, links);
		buildTopShared(built.stats);

		setStatus('ok', 'Sẵn sàng in báo cáo');

		if (btnCsv) {
			btnCsv.addEventListener('click', () => {
				const builtNow = buildTables(nodes, links);
				const all = [];
				for (const r of [...builtNow.rowsEmail, ...builtNow.rowsUrl, ...builtNow.rowsIp]) {
					all.push({
						id: r.id,
						type: r.type,
						verdict: r.verdict,
						riskScore: r.score,
						hasTriad: r.hasTriad,
						value: r.value,
						reasons: r.reasons,
					});
				}

				const rows = all
					.filter(r => r.verdict === 'FRAUD' || r.verdict === 'SUSPICIOUS')
					.sort((a, b) => (Number(b.riskScore) - Number(a.riskScore)) || String(a.type).localeCompare(String(b.type)));

				if (rows.length === 0) {
					alert('Chưa có dòng đáng nghi để xuất CSV.');
					return;
				}
					downloadCsv(`bao-cao-dang-nghi-${Date.now()}.csv`, rows);
			});
		}
	} catch (e) {
		setStatus('warn', 'Không tải được');
		tablesEl.innerHTML = `<div class="muted">Lỗi: ${escapeHtml(e?.message ?? e)}</div>`;
	}
}

if (btnPrint) btnPrint.addEventListener('click', () => window.print());
if (btnBack) btnBack.addEventListener('click', () => window.location.href = '/');

boot();
