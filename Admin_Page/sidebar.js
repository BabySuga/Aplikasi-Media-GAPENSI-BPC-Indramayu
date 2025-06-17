document.addEventListener("DOMContentLoaded", () => {
    fetch("sidebar.html")
        .then(response => response.text())
        .then(data => {
            const container = document.getElementById("sidebar-container");
            if (container) container.innerHTML = data;
            initializeSidebar();
        })
        .catch(error => console.error("Error loading sidebar:", error));
});

function initializeSidebar() {
    const sidebar = document.getElementById("sidebar");
    const menuToggle = document.getElementById("menu-toggle");
    const overlay = document.getElementById("sidebar-overlay");

    if (!sidebar || !menuToggle) return;

    const hideSidebar = () => {
        sidebar.classList.add("hidden");
        sidebar.classList.remove("visible");
        if (overlay) overlay.classList.remove("active");
    };

    const showSidebar = () => {
        sidebar.classList.remove("hidden");
        sidebar.classList.add("visible");
        if (overlay) overlay.classList.add("active");
    };

    menuToggle.addEventListener("click", () => {
        if (sidebar.classList.contains("hidden")) {
            showSidebar();
        } else {
            hideSidebar();
        }
    });

    if (overlay) {
        overlay.addEventListener("click", hideSidebar);
    }
    
    document.querySelectorAll("#sidebar .group > button").forEach(button => {
        button.addEventListener("click", () => {
            const dropdown = button.nextElementSibling;
            const isExpanded = button.getAttribute("aria-expanded") === "true";
            dropdown.classList.toggle("hidden", isExpanded);
            button.setAttribute("aria-expanded", !isExpanded);
        });
    });

    const currentPage = window.location.pathname.split("/").pop();
    document.querySelectorAll("#sidebar nav a").forEach(link => {
        if (link.getAttribute("href") === currentPage) {
            link.classList.add("selected");
            const parentDropdown = link.closest('.dropdown');
            if (parentDropdown) {
                parentDropdown.classList.remove('hidden');
                const parentButton = parentDropdown.previousElementSibling;
                if (parentButton) parentButton.setAttribute("aria-expanded", "true");
            }
        }
    });
}