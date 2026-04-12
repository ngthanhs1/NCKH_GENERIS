const statusText = document.getElementById('statusText');
const statusDot = document.getElementById('statusDot');
const message = document.getElementById('message');
const tooltip = document.getElementById('tooltip');
const panelBody = document.getElementById('panelBody');

const inEmail = document.getElementById('inEmail');
const inUrl = document.getElementById('inUrl');
const inIp = document.getElementById('inIp');
const btnLink = document.getElementById('btnLink');
const btnSeed = document.getElementById('btnSeed');
const btnSaveLayout = document.getElementById('btnSaveLayout');
const btnReport = document.getElementById('btnReport');
const btnRefresh = document.getElementById('btnRefresh');

const filters = {
	email: document.getElementById('fEmail'),
	url: document.getElementById('fUrl'),
	ip: document.getElementById('fIp'),
	fraud: document.getElementById('fFraud'),
	suspicious: document.getElementById('fSuspicious'),
	safe: document.getElementById('fSafe'),
	unknown: document.getElementById('fUnknown'),
};

function setStatus(state, text) {
	statusText.textContent = text;
	if (state === 'ok') statusDot.style.background = 'green';
	else if (state === 'warn') statusDot.style.background = 'orange';
	else statusDot.style.background = '#999';
}

function enc(v) {
	return encodeURIComponent(String(v ?? ''));
}

async function linkPartial({ email, url, ip }) {
	const params = new URLSearchParams();
	if (email) params.set('email', email);
	if (url) params.set('url', url);
	if (ip) params.set('ip', ip);

	const res = await fetch(`/api/graph/link-partial?${params.toString()}`);
	if (!res.ok) throw new Error(`Link failed (${res.status})`);
	return await res.json();
}

function setBusy(isBusy) {
	const disabled = Boolean(isBusy);
	if (btnLink) btnLink.disabled = disabled;
	if (btnSeed) btnSeed.disabled = disabled;
	if (btnSaveLayout) btnSaveLayout.disabled = disabled;
	if (btnReport) btnReport.disabled = disabled;
	if (btnRefresh) btnRefresh.disabled = disabled;
}

function toFiniteNumber(v) {
	const n = (typeof v === 'number') ? v : Number(v);
	return Number.isFinite(n) ? n : null;
}

async function saveLayout(nodes) {
	const res = await fetch('/api/graph/layout/save', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(nodes ?? []),
	});
	const data = await res.json().catch(() => ({}));
	if (!res.ok) throw new Error(data?.message ?? `Save layout failed (${res.status})`);
	return data;
}

async function seedDemo() {
	// 4 calls đủ tạo 2 pattern: shared URL + shared IP
	const rows = [
		{ email: 'scam1@test.com', url: 'http://fake-bank.com', ip: '10.0.0.1' },
		{ email: 'scam2@test.com', url: 'http://fake-bank.com', ip: '10.0.0.2' },
		{ email: 'spam1@test.com', url: 'http://promo.example', ip: '10.0.0.9' },
		{ email: 'spam2@test.com', url: 'http://promo2.example', ip: '10.0.0.9' },
	];

	for (const r of rows) {
		await linkPartial(r);
	}
}

function normalizedVerdict(v) {
	const raw = String(v ?? '').trim();
	const upper = raw.toUpperCase();

	// EN
	if (upper === 'SAFE' || upper.includes('SAFE')) return 'SAFE';
	if (upper === 'SUSPICIOUS' || upper.includes('SUSPICIOUS')) return 'SUSPICIOUS';
	if (upper === 'FRAUD' || upper.includes('FRAUD')) return 'FRAUD';
	if (upper === 'UNKNOWN' || upper.includes('UNKNOWN')) return 'UNKNOWN';

	// VI (một số biến thể hay gặp)
	if (upper.includes('AN TOÀN') || upper.includes('AN TOAN')) return 'SAFE';
	if (upper.includes('NGHI NGỜ') || upper.includes('NGHI NGO') || upper.includes('ĐÁNG NGHI') || upper.includes('DANG NGHI')) return 'SUSPICIOUS';
	if (upper.includes('GIAN LẬN') || upper.includes('GIAN LAN')) return 'FRAUD';
	if (upper.includes('KHÔNG XÁC') || upper.includes('KHONG XAC') || upper.includes('CHƯA XÁC') || upper.includes('CHUA XAC')) return 'UNKNOWN';

	return 'UNKNOWN';
}

function verdictColor(verdict) {
	const v = normalizedVerdict(verdict);
	if (v === 'FRAUD') return 'red';
	if (v === 'SUSPICIOUS') return 'orange';
	if (v === 'SAFE') return 'green';
	return '#999';
}

function cssVar(name, fallback) {
	try {
		const v = getComputedStyle(document.documentElement).getPropertyValue(name);
		const s = String(v ?? '').trim();
		return s || fallback;
	} catch {
		return fallback;
	}
}

function cssNumber(name, fallback) {
	const raw = cssVar(name, null);
	const n = Number(raw);
	return Number.isFinite(n) ? n : fallback;
}

function getTargetPosition(node, width, height) {
	// IP luôn ở trung tâm để làm “hub”
	if (primaryType(node) === 'IPAddress') {
		return { x: width / 2, y: height / 2 };
	}

	const v = normalizedVerdict(node?.verdict);
	// Layout “mỗi màu một vùng” (nhẹ tay, không ép quá cứng)
	if (v === 'SAFE') return { x: width * 0.18, y: height * 0.52 };
	if (v === 'SUSPICIOUS') return { x: width * 0.50, y: height * 0.22 };
	if (v === 'FRAUD') return { x: width * 0.82, y: height * 0.52 };
	return { x: width * 0.50, y: height * 0.82 };
}

function edgeStyle(type) {
	if (type === 'CONTAINS_LINK') return { stroke: cssVar('--edgeContains', 'rgba(138, 180, 248, 0.55)'), dash: null };
	if (type === 'SENT_FROM') return { stroke: cssVar('--edgeSentFrom', 'rgba(167, 243, 208, 0.45)'), dash: '3,3' };
	if (type === 'HOSTED_BY') return { stroke: cssVar('--edgeHostedBy', 'rgba(253, 230, 138, 0.45)'), dash: '8,4' };
	return { stroke: cssVar('--edgeDefault', 'rgba(232, 236, 255, 0.25)'), dash: null };
}

function primaryType(node) {
	const labels = Array.isArray(node.type) ? node.type : [];
	if (labels.includes('Email')) return 'Email';
	if (labels.includes('URL')) return 'URL';
	if (labels.includes('IPAddress')) return 'IPAddress';
	return labels[0] ?? 'UNKNOWN';
}

function displayType(node) {
	const t = primaryType(node);
	if (t === 'IPAddress') return 'IP';
	return t;
}

function escapeHtml(s) {
	return String(s ?? '')
		.replaceAll('&', '&amp;')
		.replaceAll('<', '&lt;')
		.replaceAll('>', '&gt;')
		.replaceAll('"', '&quot;')
		.replaceAll("'", '&#39;');
}

function linkEndId(end) {
	if (end == null) return null;
	if (typeof end === 'string') return end;
	if (typeof end === 'object') return end.id ?? null;
	return String(end);
}

function computeGraphStats(nodes, links) {
	const byId = new Map(nodes.map(n => [n.id, n]));

	const neighborsByType = new Map(); // nodeId -> { Email:Set, URL:Set, IPAddress:Set }
	const relCounts = new Map(); // nodeId -> { CONTAINS_LINK: n, SENT_FROM: n, HOSTED_BY: n }
	const urlEmailCount = new Map(); // urlId -> number
	const ipEmailCount = new Map(); // ipId -> number

	function ensureNeighborBucket(id) {
		if (!neighborsByType.has(id)) {
			neighborsByType.set(id, {
				Email: new Set(),
				URL: new Set(),
				IPAddress: new Set(),
			});
		}
		return neighborsByType.get(id);
	}

	function incRel(id, type) {
		if (!relCounts.has(id)) relCounts.set(id, {});
		const bag = relCounts.get(id);
		bag[type] = (bag[type] ?? 0) + 1;
	}

	for (const l of links) {
		const s = l.source;
		const t = l.target;
		const sNode = byId.get(s);
		const tNode = byId.get(t);
		if (!sNode || !tNode) continue;

		const sType = primaryType(sNode);
		const tType = primaryType(tNode);
		ensureNeighborBucket(s)[tType].add(t);
		ensureNeighborBucket(t)[sType].add(s);

		// relation counts: đếm ở cả 2 phía để phục vụ UI
		if (l.type) {
			incRel(s, l.type);
			incRel(t, l.type);
		}

		// global counts để giải thích rule "shared"
		if (l.type === 'CONTAINS_LINK' && sType === 'Email' && tType === 'URL') {
			urlEmailCount.set(t, (urlEmailCount.get(t) ?? 0) + 1);
		}
		if (l.type === 'SENT_FROM' && sType === 'Email' && tType === 'IPAddress') {
			ipEmailCount.set(t, (ipEmailCount.get(t) ?? 0) + 1);
		}
	}

	return { byId, neighborsByType, relCounts, urlEmailCount, ipEmailCount };
}

function hasTriadForEmail(emailId, stats) {
	const { byId, neighborsByType } = stats;
	const emailNode = byId.get(emailId);
	if (!emailNode || primaryType(emailNode) !== 'Email') return false;

	const bucket = neighborsByType.get(emailId);
	const urls = bucket ? bucket.URL : new Set();
	const ips = bucket ? bucket.IPAddress : new Set();
	if (urls.size === 0 || ips.size === 0) return false;

	// Triad nếu tồn tại URL host đúng IP và Email cũng SENT_FROM đúng IP đó
	for (const urlId of urls) {
		const urlNode = byId.get(urlId);
		if (!urlNode || primaryType(urlNode) !== 'URL') continue;
		const urlBucket = neighborsByType.get(urlId);
		const hostedIps = urlBucket ? urlBucket.IPAddress : new Set();
		for (const ipId of hostedIps) {
			if (ips.has(ipId)) return true;
		}
	}
	return false;
}

function buildEmailReasons(node, stats) {
	const { neighborsByType, urlEmailCount, ipEmailCount } = stats;
	const bucket = neighborsByType.get(node.id);
	const urls = bucket ? bucket.URL : new Set();
	const ips = bucket ? bucket.IPAddress : new Set();
	const hasTriad = hasTriadForEmail(node.id, stats);

	const reasons = [];
	let score = 0;

	if (!hasTriad) {
		reasons.push({ text: 'Chưa đủ bộ 3 (Email + URL + IP) → giữ UNKNOWN', points: 0 });
		return { reasons, score: 0 };
	}

	if (urls.size > 0) {
		score += 30;
		reasons.push({ text: 'Email có chứa URL', points: 30 });
	}
	if (ips.size > 0) {
		score += 20;
		reasons.push({ text: 'Email có địa chỉ IP gửi', points: 20 });
	}

	const ipShared = Array.from(ips).some(ipId => (ipEmailCount.get(ipId) ?? 0) > 1);
	if (ipShared) {
		score += 30;
		reasons.push({ text: 'IP được dùng bởi >1 Email', points: 30 });
	}

	const urlShared = Array.from(urls).some(urlId => (urlEmailCount.get(urlId) ?? 0) > 1);
	if (urlShared) {
		score += 30;
		reasons.push({ text: 'URL xuất hiện trong >1 Email', points: 30 });
	}

	return { reasons, score };
}

function renderPanel(node, stats, locked) {
	if (!node) {
		panelBody.innerHTML = '<div class="panel-empty">Chưa chọn node</div>';
		return;
	}

	const { neighborsByType, relCounts } = stats;
	const bucket = neighborsByType.get(node.id) ?? { Email: new Set(), URL: new Set(), IPAddress: new Set() };
	const t = primaryType(node);
	const verdict = node.verdict ?? 'UNKNOWN';
	const score = node.riskScore ?? 0;
	const value = node.label ?? node.id;
	const rel = relCounts.get(node.id) ?? {};

	let linksSummary = '';
	if (t === 'Email') {
		linksSummary = `
			<li>${bucket.URL.size} URL</li>
			<li>${bucket.IPAddress.size} IP</li>
		`;
	} else if (t === 'URL') {
		linksSummary = `
			<li>${bucket.Email.size} Email</li>
			<li>${bucket.IPAddress.size} IP</li>
		`;
	} else if (t === 'IPAddress') {
		linksSummary = `
			<li>${bucket.Email.size} Email</li>
			<li>${bucket.URL.size} URL</li>
		`;
	} else {
		linksSummary = `
			<li>${bucket.Email.size} Email</li>
			<li>${bucket.URL.size} URL</li>
			<li>${bucket.IPAddress.size} IP</li>
		`;
	}

	let reasonsHtml = '';
	if (t === 'Email') {
		const explain = buildEmailReasons(node, stats);
		const rs = explain.reasons.length
			? explain.reasons.map(r => `<li>${escapeHtml(r.text)} <span class="muted">(+${r.points})</span></li>`).join('')
			: '<li class="muted">Không có rule nào được kích hoạt</li>';
		reasonsHtml = `
			<div class="panel-section">
				<div class="panel-h">Lý do (rule-based)</div>
				<ul class="panel-list">${rs}</ul>
			</div>
		`;
	}

	const relItems = Object.keys(rel).length
		? Object.entries(rel)
				.sort((a, b) => b[1] - a[1])
				.map(([k, v]) => `<li>${escapeHtml(k)} <span class="muted">(${v})</span></li>`)
				.join('')
		: '<li class="muted">Chưa có quan hệ</li>';

	const deleteButtonHtml = `
		<div class="panel-section">
			<div class="panel-h">Hành động</div>
			<button
				id="btnDeleteNode"
				class="btn btn-danger"
				data-id="${escapeHtml(node.id)}"
				${locked ? '' : 'disabled'}
				title="${locked ? 'Xóa node khỏi Neo4j' : 'Click node để ghim trước khi xóa'}"
			>
				Xóa node
			</button>
			<div class="muted" style="margin-top:6px; font-size:12px; line-height:1.4;">
				Xóa sẽ <strong>DETACH DELETE</strong> (xóa kèm toàn bộ quan hệ).
			</div>
		</div>
	`;

	panelBody.innerHTML = `
		<div class="panel-kv">
			<div class="k">Trạng thái</div>
			<div class="v">${locked ? '<span class="badge">ĐÃ GHIM</span>' : '<span class="muted">(đang hover)</span>'}</div>
		</div>

		<div class="panel-kv">
			<div class="k">Loại</div>
			<div class="v">${escapeHtml(displayType(node))}</div>
		</div>
		<div class="panel-kv">
			<div class="k">Giá trị</div>
			<div class="v">${escapeHtml(value)}</div>
		</div>
		<div class="panel-kv">
			<div class="k">Risk score</div>
			<div class="v">${escapeHtml(score)}</div>
		</div>
		<div class="panel-kv">
			<div class="k">Verdict</div>
			<div class="v"><span class="verdict" style="--c:${verdictColor(verdict)}"><span class="dot"></span>${escapeHtml(verdict)}</span></div>
		</div>

		<div class="panel-section">
			<div class="panel-h">Liên kết</div>
			<ul class="panel-list">${linksSummary}</ul>
		</div>

		${reasonsHtml}

		<div class="panel-section">
			<div class="panel-h">Cạnh (relationships)</div>
			<ul class="panel-list">${relItems}</ul>
		</div>

		${deleteButtonHtml}
	`;

	const btnDeleteNode = document.getElementById('btnDeleteNode');
	if (btnDeleteNode) {
		btnDeleteNode.addEventListener('click', async () => {
			if (!locked) return;

			const id = btnDeleteNode.getAttribute('data-id');
			if (!id) return;

			const ok = window.confirm(`Xóa node này khỏi Neo4j?\n\n${id}`);
			if (!ok) return;

			try {
				setBusy(true);
				setStatus('warn', 'Đang xóa node…');
				const res = await fetch(`/api/graph/node?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
				const data = await res.json().catch(() => ({}));
				if (!res.ok) throw new Error(data?.message ?? `Delete failed (${res.status})`);

				window.location.reload();
			} catch (e) {
				setBusy(false);
				setStatus('warn', `Xóa thất bại: ${e?.message ?? e}`);
			}
		});
	}
}

async function boot() {
	try {
		setStatus('warn', 'Đang phân tích & tải dữ liệu…');

		// 1) Tính riskScore + verdict trước
		await fetch('/api/graph/analyze');

		// 2) Lấy graph + health để hiển thị
		const [fullRes, healthRes] = await Promise.all([
			fetch('/api/graph/full'),
			fetch('/api/graph/health')
		]);
		const data = await fullRes.json();
		const health = healthRes.ok ? await healthRes.json() : null;

		if (health && health.status === 'connected') {
			const c = health.counts || {};
			setStatus('ok', `Neo4j OK • Email ${c.Email ?? 0} • URL ${c.URL ?? 0} • IP ${c.IPAddress ?? 0}`);
		} else {
			setStatus('warn', 'Neo4j: chưa xác nhận');
		}

		if (!data || !Array.isArray(data.nodes) || data.nodes.length === 0) {
			message.textContent = 'Chưa có dữ liệu đồ thị. Dùng "Seed demo" hoặc bấm "Link" để tạo nhanh.';
			return;
		}

		message.remove();

		// Dữ liệu gốc (master) — KHÔNG BAO GIỜ sửa/đưa trực tiếp vào D3 forceLink
		// Lý do: D3 sẽ mutate link.source/link.target thành object => làm hỏng filter nếu dùng lại.
		const graphRaw = {
			nodes: (data.nodes ?? []).map(n => ({...n})),
			links: (data.links ?? []).map(l => ({
				source: linkEndId(l.source),
				target: linkEndId(l.target),
				type: l.type,
			})),
		};
		const stats = computeGraphStats(graphRaw.nodes, graphRaw.links);

		let lockedNodeId = null;
		let hoveredNodeId = null;
		let layoutDirty = false;

		const svg = d3.select("svg");
		let simulation = null;
		let currentNodeSelection = null;
		let currentTextSelection = null;
		let currentLinkSelection = null;
		let currentSimNodes = [];

		function applyThemeToEdges() {
			if (!currentLinkSelection) return;
			currentLinkSelection
				.attr('stroke-width', cssNumber('--edgeWidth', 1.35))
				.style('opacity', cssNumber('--edgeOpacity', 0.45))
				.style('stroke', d => edgeStyle(d.type).stroke)
				.style('stroke-dasharray', d => edgeStyle(d.type).dash);
		}

		window.addEventListener('themechange', applyThemeToEdges);

		function setSaveEnabled() {
			if (!btnSaveLayout) return;
			btnSaveLayout.disabled = !layoutDirty;
		}

		function isNodeVisible(n) {
			const t = primaryType(n);
			const v = normalizedVerdict(n.verdict);

			const typeOk =
				(t === 'Email' && filters.email.checked) ||
				(t === 'URL' && filters.url.checked) ||
				(t === 'IPAddress' && filters.ip.checked) ||
				(t !== 'Email' && t !== 'URL' && t !== 'IPAddress');

			const verdictOk =
				(v === 'FRAUD' && filters.fraud.checked) ||
				(v === 'SUSPICIOUS' && filters.suspicious.checked) ||
				(v === 'SAFE' && filters.safe.checked) ||
				(v === 'UNKNOWN' && filters.unknown.checked);

			return typeOk && verdictOk;
		}

		function getNodeForPanel() {
			const id = lockedNodeId ?? hoveredNodeId;
			return id ? stats.byId.get(id) : null;
		}

		function refreshPanel() {
			renderPanel(getNodeForPanel(), stats, Boolean(lockedNodeId));
		}

		function applyFilters() {
			const filteredNodes = graphRaw.nodes.filter(isNodeVisible);
			const allowedIds = new Set(filteredNodes.map(n => n.id));
			const filteredLinks = graphRaw.links
				.filter(l => allowedIds.has(linkEndId(l.source)) && allowedIds.has(linkEndId(l.target)))
				.map(l => ({
					source: linkEndId(l.source),
					target: linkEndId(l.target),
					type: l.type,
				}));
			renderGraph(filteredNodes, filteredLinks);

			// Nếu node đang ghim bị ẩn -> bỏ ghim
			if (lockedNodeId && !allowedIds.has(lockedNodeId)) lockedNodeId = null;
			if (hoveredNodeId && !allowedIds.has(hoveredNodeId)) hoveredNodeId = null;
			refreshPanel();
		}

		function radiusFor(n) {
			const t = primaryType(n);
			if (t !== 'IPAddress') return 15;
			const bucket = stats.neighborsByType.get(n.id);
			const emailCount = bucket ? bucket.Email.size : 0;
			const urlCount = bucket ? bucket.URL.size : 0;
			const hubScore = (emailCount * 2) + (urlCount * 1.5);
			return 15 + Math.min(10, hubScore);
		}

		function updateSelectedStyles() {
			if (!currentNodeSelection) return;
			currentNodeSelection
				.style('stroke-width', d => (d.id === lockedNodeId ? 3.5 : (primaryType(d) === 'IPAddress' ? 1.6 : 1)))
				.style('stroke', d => (d.id === lockedNodeId ? 'rgba(255,255,255,0.95)' : 'rgba(255,255,255,0.55)'));
		}

		function renderGraph(nodes, links) {
			svg.selectAll('*').remove();

			const width = svg.node().clientWidth;
			const height = svg.node().clientHeight;

			// Zoom/Pan
			const root = svg.append("g");
			const zoom = d3.zoom()
				.scaleExtent([0.3, 3])
				.on("zoom", (event) => {
					root.attr("transform", event.transform);
				});
			svg.call(zoom).on("dblclick.zoom", null);

			// click nền để bỏ ghim
			svg.on('click', (event) => {
				if (event.target && event.target.tagName && event.target.tagName.toLowerCase() === 'svg') {
					lockedNodeId = null;
					refreshPanel();
					updateSelectedStyles();
				}
			});

			// Clone để D3 mutate thoải mái mà không làm bẩn state/filter
			const simNodes = nodes.map(n => ({...n}));
			const simLinks = links.map(l => ({...l}));
			currentSimNodes = simNodes;

			// Áp dụng layout đã lưu (nếu có) -> ghim fx/fy để giữ bố cục
			for (const n of simNodes) {
				const x = toFiniteNumber(n.layoutX);
				const y = toFiniteNumber(n.layoutY);
				if (x == null || y == null) continue;
				n.x = x;
				n.y = y;
				n.fx = x;
				n.fy = y;
			}

			// Adjacency để highlight theo node đang focus
			const idOf = (x) => (typeof x === 'object' && x ? (x.id ?? x.index ?? String(x)) : String(x));
			const neighbor = new Map(); // id -> Set(id)
			const ensure = (id) => {
				if (!neighbor.has(id)) neighbor.set(id, new Set());
				return neighbor.get(id);
			};
			for (const l of simLinks) {
				const s = String(l.source);
				const t = String(l.target);
				ensure(s).add(t);
				ensure(t).add(s);
			}

			if (simulation) simulation.stop();
			simulation = d3.forceSimulation(simNodes)
				// Giãn node ra để nhìn link rõ hơn
				.force("link", d3.forceLink(simLinks)
					.id(d => d.id)
					.distance(d => {
						const type = d.type;
						if (type === 'CONTAINS_LINK') return 165;
						if (type === 'SENT_FROM') return 145;
						if (type === 'HOSTED_BY') return 145;
						return 155;
					})
					.strength(0.9)
				)
				.force("charge", d3.forceManyBody().strength(-520))
				.force("collide", d3.forceCollide(d => radiusFor(d) + 10).strength(0.85))
				.force("center", d3.forceCenter(width / 2, height / 2))
				// Kéo theo verdict để bớt rối (IP ở giữa)
				.force("x", d3.forceX(d => getTargetPosition(d, width, height).x)
					.strength(d => (primaryType(d) === 'IPAddress' ? 0.55 : 0.22)))
				.force("y", d3.forceY(d => getTargetPosition(d, width, height).y)
					.strength(d => (primaryType(d) === 'IPAddress' ? 0.55 : 0.22)));

			currentLinkSelection = root.append("g")
				.selectAll("line")
				.data(simLinks)
				.enter().append("line")
				.attr('stroke-width', cssNumber('--edgeWidth', 1.35))
				.attr('stroke-linecap', 'round')
				.style("stroke", d => edgeStyle(d.type).stroke)
				.style("stroke-dasharray", d => edgeStyle(d.type).dash)
				.style('opacity', cssNumber('--edgeOpacity', 0.45))
				.on('mouseenter', (event, d) => {
					tooltip.innerHTML = `
						<div><strong>Relationship</strong></div>
						<div class="muted">${escapeHtml(d.type ?? 'UNKNOWN')}</div>
					`;
					tooltip.style.opacity = '1';
					tooltip.style.transform = 'translateY(0px)';
				})
				.on('mousemove', (event) => {
					const rect = svg.node().getBoundingClientRect();
					const x = event.clientX - rect.left + 14;
					const y = event.clientY - rect.top + 14;
					tooltip.style.left = `${Math.min(x, rect.width - 340)}px`;
					tooltip.style.top = `${Math.min(y, rect.height - 120)}px`;
				})
				.on('mouseleave', () => {
					tooltip.style.opacity = '0';
					tooltip.style.transform = 'translateY(4px)';
				});

			applyThemeToEdges();

			currentNodeSelection = root.append("g")
				.selectAll("circle")
				.data(simNodes)
				.enter().append("circle")
				.attr("r", d => radiusFor(d))
				.style("fill", d => verdictColor(d.verdict))
				.style('stroke-width', d => (primaryType(d) === 'IPAddress' ? 1.6 : 1))
				.style('opacity', 1)
				.on('mouseenter', (event, d) => {
					hoveredNodeId = d.id;
					refreshPanel();

					applyFocus(lockedNodeId ?? d.id);

					const title = displayType(d);
					const verdict = d.verdict ?? 'UNKNOWN';
					const score = (d.riskScore ?? 0);
					tooltip.innerHTML = `
						<div><strong>${escapeHtml(title)}</strong></div>
						<div class="muted">${escapeHtml(d.label ?? d.id ?? '')}</div>
						<div style="margin-top:6px; display:flex; gap:10px; align-items:center;">
							<span class="dot" style="background:${verdictColor(verdict)}"></span>
							<div><strong>${escapeHtml(verdict)}</strong><span class="muted"> • score ${escapeHtml(score)}</span></div>
						</div>
					`;
					tooltip.style.opacity = '1';
					tooltip.style.transform = 'translateY(0px)';
				})
				.on('mousemove', (event) => {
					const rect = svg.node().getBoundingClientRect();
					const x = event.clientX - rect.left + 14;
					const y = event.clientY - rect.top + 14;
					tooltip.style.left = `${Math.min(x, rect.width - 340)}px`;
					tooltip.style.top = `${Math.min(y, rect.height - 120)}px`;
				})
				.on('mouseleave', () => {
					hoveredNodeId = null;
					if (!lockedNodeId) refreshPanel();
					tooltip.style.opacity = '0';
					tooltip.style.transform = 'translateY(4px)';

					applyFocus(lockedNodeId ?? null);
				})
				.on('click', (event, d) => {
					event.stopPropagation();
					lockedNodeId = (lockedNodeId === d.id) ? null : d.id;
					refreshPanel();
					updateSelectedStyles();

					applyFocus(lockedNodeId ?? null);
				})
				.call(d3.drag()
					.on("start", dragstarted)
					.on("drag", dragged)
					.on("end", dragended)
				);

			currentTextSelection = root.append("g")
				.selectAll("text")
				.data(simNodes)
				.enter().append("text")
				.text(d => d.label)
				.attr("font-size", "10px")
				.style('opacity', 0.95);

			function applyFocus(focusId) {
				if (!currentNodeSelection || !currentLinkSelection || !currentTextSelection) return;
				if (!focusId) {
					currentNodeSelection.style('opacity', 1);
					currentTextSelection.style('opacity', 0.95);
					currentLinkSelection
						.style('opacity', 0.45)
						.attr('stroke-width', 1.35);
					return;
				}

				const neigh = neighbor.get(String(focusId)) ?? new Set();
				currentNodeSelection.style('opacity', n => {
					const id = String(n.id);
					return (id === String(focusId) || neigh.has(id)) ? 1 : 0.14;
				});
				currentTextSelection.style('opacity', n => {
					const id = String(n.id);
					return (id === String(focusId) || neigh.has(id)) ? 0.98 : 0.08;
				});
				currentLinkSelection
					.style('opacity', l => {
						const s = idOf(l.source);
						const t = idOf(l.target);
						return (s === String(focusId) || t === String(focusId)) ? 0.95 : 0.06;
					})
					.attr('stroke-width', l => {
						const s = idOf(l.source);
						const t = idOf(l.target);
						return (s === String(focusId) || t === String(focusId)) ? 2.6 : 1.0;
					});
			}

			simulation.on("tick", () => {
				currentLinkSelection
					.attr("x1", d => d.source.x)
					.attr("y1", d => d.source.y)
					.attr("x2", d => d.target.x)
					.attr("y2", d => d.target.y);

				currentNodeSelection
					.attr("cx", d => d.x)
					.attr("cy", d => d.y);

				currentTextSelection
					.attr("x", d => d.x + radiusFor(d) + 6)
					.attr("y", d => d.y + 4);
			});

			updateSelectedStyles();

			// Nếu đang ghim sẵn node (re-render do filter) thì focus lại
			applyFocus(lockedNodeId ?? null);

			function dragstarted(event, d) {
				if (!event.active) simulation.alphaTarget(0.3).restart();
				layoutDirty = true;
				setSaveEnabled();
				d.fx = d.x; d.fy = d.y;
			}
			function dragged(event, d) {
				d.fx = event.x; d.fy = event.y;
			}
			function dragended(event, d) {
				if (!event.active) simulation.alphaTarget(0);
				// Giữ node tại vị trí vừa kéo (để "giữ bố cục")
				d.fx = event.x;
				d.fy = event.y;

				// Ghi lại vào graphRaw để re-render (filter) vẫn giữ được ngay cả khi chưa bấm Save
				const raw = stats.byId.get(d.id);
				if (raw) {
					raw.layoutX = d.fx;
					raw.layoutY = d.fy;
				}
			}
		}

		// Keyboard: Esc để bỏ ghim
		window.addEventListener('keydown', (e) => {
			if (e.key === 'Escape') {
				lockedNodeId = null;
				refreshPanel();
				updateSelectedStyles();
			}
		});

		Object.values(filters).forEach(el => el.addEventListener('change', applyFilters));

		renderPanel(null, stats, false);
		renderGraph(graphRaw.nodes, graphRaw.links);
		setSaveEnabled();

		if (btnSaveLayout) {
			btnSaveLayout.addEventListener('click', async () => {
				if (!layoutDirty) return;
				try {
					setBusy(true);
					setStatus('warn', 'Đang lưu bố cục…');

					const rows = (currentSimNodes ?? [])
						.map(n => {
							const x = toFiniteNumber(n.fx ?? n.x);
							const y = toFiniteNumber(n.fy ?? n.y);
							return {
								id: n.id,
								x,
								y,
							};
						})
						.filter(r => r.id && r.x != null && r.y != null);

					const res = await saveLayout(rows);
					layoutDirty = false;
					setSaveEnabled();
					setStatus('ok', `Đã lưu bố cục (${res.saved ?? rows.length} nodes)`);
				} catch (e) {
					setStatus('warn', `Lưu thất bại: ${e?.message ?? e}`);
				} finally {
					setBusy(false);
				}
			});
		}
	} catch (e) {
		setStatus('warn', 'Không tải được dữ liệu');
		message.textContent = `Lỗi: ${e?.message ?? e}`;
	}
}

if (btnRefresh) {
	btnRefresh.addEventListener('click', () => window.location.reload());
}

if (btnReport) {
	btnReport.addEventListener('click', () => {
		window.open('/report.html', '_blank', 'noopener');
	});
}

if (btnLink) {
	btnLink.addEventListener('click', async () => {
		const email = (inEmail?.value ?? '').trim();
		const url = (inUrl?.value ?? '').trim();
		const ip = (inIp?.value ?? '').trim();

		if (!email && !url && !ip) {
			setStatus('warn', 'Nhập ít nhất 1 trong 3: Email / URL / IP');
			return;
		}

		try {
			setBusy(true);
			setStatus('warn', 'Đang tạo dữ liệu…');
			const result = await linkPartial({ email, url, ip });
			if (result?.status === 'no_input') {
				setBusy(false);
				setStatus('warn', result?.message ?? 'Thiếu dữ liệu');
				return;
			}
			window.location.reload();
		} catch (e) {
			setBusy(false);
			setStatus('warn', `Lỗi tạo dữ liệu: ${e?.message ?? e}`);
		}
	});
}

if (btnSeed) {
	btnSeed.addEventListener('click', async () => {
		try {
			setBusy(true);
			setStatus('warn', 'Đang seed dữ liệu demo…');
			await seedDemo();
			window.location.reload();
		} catch (e) {
			setBusy(false);
			setStatus('warn', `Seed thất bại: ${e?.message ?? e}`);
		}
	});
}

boot();
