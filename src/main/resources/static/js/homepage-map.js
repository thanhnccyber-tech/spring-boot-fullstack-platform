(function () {
    'use strict';

    function initStoreFinder() {
        const mapElement = document.getElementById('uteStoreMap');
        const listElement = document.getElementById('uteStoreList');
        if (!mapElement || !listElement) return;

        const cards = Array.from(listElement.querySelectorAll('.ute-store-item'));
        const searchInput = document.getElementById('uteStoreSearch');
        const countElement = document.getElementById('uteStoreCount');
        const noResults = document.getElementById('uteStoreNoResults');
        const mapNotice = document.getElementById('uteMapNotice');
        const branchData = cards.map((card) => {
            const lat = Number.parseFloat(card.dataset.lat || '');
            const lng = Number.parseFloat(card.dataset.lng || '');
            return {
                card,
                id: card.dataset.branchId || '',
                name: card.dataset.name || '',
                address: card.dataset.address || '',
                phone: card.dataset.phone || '',
                lat,
                lng,
                hasCoords: Number.isFinite(lat) && Number.isFinite(lng) && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180
            };
        });

        let map = null;
        const markers = [];
        if (window.L) {
            map = L.map(mapElement, { scrollWheelZoom: false, zoomControl: true, tap: true });
            map.setView([10.8231, 106.6297], 12);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap</a>'
            }).addTo(map);

            branchData.filter((branch) => branch.hasCoords).forEach((branch) => {
                const marker = L.marker([branch.lat, branch.lng], { title: branch.name });
                const popup = document.createElement('div');
                popup.className = 'ute-map-popup';
                const title = document.createElement('strong');
                title.textContent = branch.name;
                const address = document.createElement('span');
                address.textContent = branch.address;
                popup.append(title, address);
                if (branch.phone) {
                    const phone = document.createElement('span');
                    phone.textContent = branch.phone;
                    popup.appendChild(phone);
                }
                const directions = document.createElement('a');
                directions.href = 'https://www.google.com/maps/dir/?api=1&destination=' + encodeURIComponent(branch.address);
                directions.target = '_blank';
                directions.rel = 'noopener noreferrer';
                directions.textContent = 'Mở chỉ đường →';
                popup.appendChild(directions);
                marker.bindPopup(popup);
                marker.addTo(map);
                markers.push({ branch, marker });

                branch.card.addEventListener('click', (event) => {
                    if (event.target.closest('a, button, form')) return;
                    cards.forEach((card) => card.classList.remove('is-active'));
                    branch.card.classList.add('is-active');
                    map.setView([branch.lat, branch.lng], Math.max(map.getZoom(), 15), { animate: true });
                    marker.openPopup();
                });
            });

            if (markers.length) {
                const bounds = L.featureGroup(markers.map((item) => item.marker)).getBounds();
                map.fitBounds(bounds, { padding: [28, 28], maxZoom: 14 });
                if (mapNotice) mapNotice.hidden = true;
            } else if (mapNotice) {
                mapNotice.hidden = false;
            }
            window.addEventListener('resize', () => map.invalidateSize());
        } else if (mapNotice) {
            mapNotice.hidden = false;
            mapNotice.textContent = 'Không tải được bản đồ tương tác. Bạn vẫn có thể tìm cửa hàng và mở chỉ đường từ danh sách.';
        }

        // Keep search and filtering functional even if the external map library is unavailable.
        const applyFilter = () => {
            const query = (searchInput ? searchInput.value : '').trim().toLocaleLowerCase('vi');
            let visibleCount = 0;
            branchData.forEach((branch) => {
                const haystack = (branch.name + ' ' + branch.address + ' ' + branch.phone).toLocaleLowerCase('vi');
                const visible = !query || haystack.includes(query);
                branch.card.hidden = !visible;
                if (visible) visibleCount += 1;
            });
            markers.forEach(({ branch, marker }) => {
                const haystack = (branch.name + ' ' + branch.address + ' ' + branch.phone).toLocaleLowerCase('vi');
                const visible = !query || haystack.includes(query);
                if (visible && map && !map.hasLayer(marker)) marker.addTo(map);
                if (!visible && map && map.hasLayer(marker)) map.removeLayer(marker);
            });
            if (countElement) countElement.textContent = visibleCount + ' địa điểm';
            if (noResults) noResults.hidden = visibleCount > 0 || !query;
        };
        if (searchInput) searchInput.addEventListener('input', applyFilter);
        applyFilter();

        if (searchInput) {
            searchInput.addEventListener('keydown', (event) => {
                if (event.key === 'Escape') {
                    searchInput.value = '';
                    searchInput.dispatchEvent(new Event('input', { bubbles: true }));
                    searchInput.blur();
                }
            });
            document.addEventListener('keydown', (event) => {
                if (event.key !== '/' || event.ctrlKey || event.metaKey || event.altKey) return;
                const active = document.activeElement;
                if (active && ['INPUT', 'TEXTAREA', 'SELECT'].includes(active.tagName)) return;
                event.preventDefault();
                searchInput.focus();
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initStoreFinder, { once: true });
    } else {
        initStoreFinder();
    }
}());
