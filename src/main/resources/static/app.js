document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const elements = {
        size: document.getElementById("size"),
        capacity: document.getElementById("capacity"),
        remaining: document.getElementById("remaining"),
        messageBox: document.getElementById("message-box"),
        resultBox: document.getElementById("result-box"),
        messageInput: document.getElementById("message-input"),
        enqueueBtn: document.getElementById("enqueue-btn"),
        dequeueBtn: document.getElementById("dequeue-btn"),
        clearBtn: document.getElementById("clear-btn"),
        refreshBtn: document.getElementById("refresh-btn")
    };

    const controls = [elements.enqueueBtn, elements.dequeueBtn, elements.clearBtn, elements.refreshBtn];

    const showBox = (text, type) => {
        elements.messageBox.textContent = text;
        elements.messageBox.className = `message-box ${type}`;
    };

    const hideBox = () => {
        elements.messageBox.className = "message-box hidden";
    };

    const setControlsDisabled = (disabled) => {
        controls.forEach(btn => { btn.disabled = disabled; });
    };

    const showWaiting = (btn) => {
        btn.setAttribute("data-original-text", btn.textContent);
        btn.textContent = "Waiting...";
        setControlsDisabled(true);
    };

    const restoreButton = (btn) => {
        const original = btn.getAttribute("data-original-text");
        if (original) {
            btn.textContent = original;
            btn.removeAttribute("data-original-text");
        }
        setControlsDisabled(false);
    };

    const loadStatus = async () => {
        try {
            const res = await fetch("/api/messages/status");
            if (!res.ok) throw new Error("Failed to load queue status");
            const data = await res.json();
            elements.size.textContent = data.size;
            elements.capacity.textContent = data.capacity;
            elements.remaining.textContent = data.remainingCapacity;

            // 큐 상태에 따라 버튼 활성화/비활성화
            const isFull = data.remainingCapacity === 0;
            const isEmpty = data.size === 0;

            elements.enqueueBtn.disabled = isFull;
            elements.dequeueBtn.disabled = isEmpty;
            
            if (isFull) {
                elements.enqueueBtn.title = "Queue is full";
            } else {
                elements.enqueueBtn.title = "";
            }

            if (isEmpty) {
                elements.dequeueBtn.title = "Queue is empty";
            } else {
                elements.dequeueBtn.title = "";
            }

        } catch (error) {
            showBox(error.message, "error");
        }
    };

    const enqueue = async () => {
        const message = elements.messageInput.value.trim();
        if (!message) {
            showBox("Message must not be blank", "error");
            return;
        }

        showWaiting(elements.enqueueBtn);

        try {
            const res = await fetch("/api/messages", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ message })
            });

            const data = await res.json();
            if (!res.ok) throw new Error(data.error || "Enqueue failed");

            elements.messageInput.value = "";
            showBox(`Enqueued: ${message}`, "success");
        } catch (err) {
            showBox(err.message, "error");
        } finally {
            restoreButton(elements.enqueueBtn);
            await loadStatus();
        }
    };

    const dequeue = async () => {
        showWaiting(elements.dequeueBtn);

        try {
            const res = await fetch("/api/messages/next");
            const data = await res.json();
            if (!res.ok) throw new Error(data.error || "Dequeue failed");

            elements.resultBox.textContent = data.message;
            elements.resultBox.className = "result-box";
            showBox("Message dequeued successfully", "success");
        } catch (err) {
            showBox(err.message, "error");
        } finally {
            restoreButton(elements.dequeueBtn);
            await loadStatus();
        }
    };

    const clearQueue = async () => {
        setControlsDisabled(true);

        try {
            const res = await fetch("/api/messages", { method: "DELETE" });
            if (!res.ok) {
                const data = await res.json();
                throw new Error(data.error || "Clear failed");
            }
            showBox("Queue cleared", "success");
        } catch (err) {
            showBox(err.message, "error");
        } finally {
            setControlsDisabled(false);
            await loadStatus();
        }
    };

    // Event Listeners
    elements.enqueueBtn.addEventListener("click", enqueue);
    elements.dequeueBtn.addEventListener("click", dequeue);
    elements.clearBtn.addEventListener("click", clearQueue);
    elements.refreshBtn.addEventListener("click", () => { loadStatus(); hideBox(); });

    elements.messageInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") enqueue();
    });

    // Initial Load
    loadStatus();
});
