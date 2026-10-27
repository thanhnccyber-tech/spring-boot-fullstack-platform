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

        // Google Maps embed (no API key needed): shows the selected store with the red pin.
        const embedUrl = (branch) => {
            const q = branch.hasCoords ? branch.lat + ',' + branch.lng : (branch.address || branch.name);
            return 'https://www.google.com/maps?q=' + encodeURIComponent(q) + '&hl=vi&z=16&output=embed';
        };
        const frame = document.createElement('iframe');
        frame.title = 'Bản đồ cửa hàng UTETra';
        frame.loading = 'lazy';
        frame.referrerPolicy = 'no-referrer-when-downgrade';
        frame.allowFullscreen = true;
        mapElement.appendChild(frame);

        let activeBranch = null;
        const showBranch = (branch) => {
            if (!branch || branch === activeBranch) return;
            activeBranch = branch;
            cards.forEach((card) => card.classList.toggle('is-active', card === branch.card));
            frame.src = embedUrl(branch);
        };
        branchData.forEach((branch) => {
            branch.card.addEventListener('click', (event) => {
                if (event.target.closest('a, button, form')) return;
                showBranch(branch);
            });
        });
        if (branchData.length) {
            showBranch(branchData[0]);
            if (mapNotice) mapNotice.hidden = true;
        } else if (mapNotice) {
            mapNotice.hidden = false;
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
            const firstVisible = branchData.find((branch) => !branch.card.hidden);
            if (firstVisible && (!activeBranch || activeBranch.card.hidden)) showBranch(firstVisible);
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
