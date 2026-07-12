/**
 * Verantwortungsbereich: Gemeinsame Frontend-Entwicklung,
 * Benutzerinteraktion und Echtzeitkommunikation im Chat.
 *
 * @author Zeynep Ünver
 * @author Nilüfer Civelek
 */
const messageForm = document.getElementById("messageForm");
const messageInput = document.getElementById("messageInput");
const messageArea = document.getElementById("messageArea");
const logoutButton = document.getElementById("logoutButton");
const currentUserNameEl = document.getElementById("currentUserName");
const contactsList = document.getElementById("contactsList");

const chatHeaderName = document.getElementById("chatHeaderName");
const chatHeaderPersonalMsg = document.getElementById("chatHeaderPersonalMsg");

const currentUserAvatar = document.getElementById("currentUserAvatar");
const avatarInput = document.getElementById("avatarInput");
const activeReceiverAvatar = document.getElementById("activeReceiverAvatar");

const buddyLargeAvatar = document.getElementById("buddyLargeAvatar");
const selfLargeAvatar = document.getElementById("selfLargeAvatar");

const userStatusSelect = document.getElementById("userStatusSelect");
const currentUserPersonalMsg = document.getElementById("currentUserPersonalMsg");

const currentUser = localStorage.getItem("username");
window.currentUser = currentUser;

if (!currentUser) {
    window.location.href = "index.html";
}

// Display current logged in user name
currentUserNameEl.textContent = currentUser;

let activeReceiver = null;
window.activeReceiver = activeReceiver;
let activeReceiverIsGroup = false;
let allUsers = [];
let allGroups = [];
let socket = null;
window.socket = socket;

const groupsList = document.getElementById("groupsList");
const createGroupBtn = document.getElementById("createGroupBtn");

function escapeHTML(str) {
    if (!str) return "";
    return str.replace(/[&<>'"]/g,
        tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag)
    );
}

// Avatar upload trigger
if (currentUserAvatar) {
    currentUserAvatar.addEventListener("click", () => {
        avatarInput.click();
    });
}
const changeAvatarBtn = document.getElementById("changeAvatarBtn");
if (changeAvatarBtn) {
    changeAvatarBtn.addEventListener("click", () => {
        avatarInput.click();
    });
}

// Avatar file upload handler
avatarInput.addEventListener("change", (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onloadend = () => {
        const base64String = reader.result;

        // Upload to Backend
        fetch("/uploadProfilePicture", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: currentUser,
                profilePicture: base64String
            })
        })
            .then(response => {
                avatarInput.value = ""; // Reset uploader value
                if (response.ok) {
                    currentUserAvatar.innerHTML = `<img src="${base64String}" alt="avatar" />`;
                    loadUsers(); // Refresh sidebar for all
                } else {
                    alert("Fehler beim Hochladen des Profilbilds.");
                }
            })
            .catch(err => {
                avatarInput.value = ""; // Reset uploader value
                console.error("Upload error: ", err);
            });
    };
    reader.readAsDataURL(file);
});

// Personal message update
currentUserPersonalMsg.addEventListener("change", () => {
    const msg = currentUserPersonalMsg.value.trim();
    fetch("/updatePersonalMessage", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: currentUser, personalMessage: msg })
    })
        .then(res => {
            if (res.ok) loadUsers();
        });
});

// Status selection update
userStatusSelect.addEventListener("change", () => {
    const status = userStatusSelect.value;
    fetch("/updateStatus", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: currentUser, status: status })
    })
        .then(res => {
            if (res.ok) loadUsers();
        });
});

// Initialize WebSocket Connection
function connectWebSocket() {
    const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";
    const wsUrl = wsProtocol + window.location.host + "/chat-ws?user=" + encodeURIComponent(currentUser);

    socket = new WebSocket(wsUrl);
    window.socket = socket;

    socket.onopen = function() {
        console.log("WebSocket connected.");
        loadMessages(); // Load initial history
    };

    socket.onmessage = function(event) {
        const message = JSON.parse(event.data);

        // Render message only if it belongs to the current conversation
        const belongsToConversation = activeReceiverIsGroup
            ? (message.receiver === activeReceiver)
            : ((message.sender === currentUser && message.receiver === activeReceiver) ||
                (message.sender === activeReceiver && message.receiver === currentUser));

        if (belongsToConversation) {
            appendMessage(message);
        }
    };

    socket.onclose = function() {
        console.log("WebSocket connection closed. Reconnecting in 3 seconds...");
        setTimeout(connectWebSocket, 3000);
    };

    socket.onerror = function(error) {
        console.error("WebSocket error: ", error);
    };
}

messageForm.addEventListener("submit", function (event) {
    event.preventDefault();

    if (!activeReceiver) {
        alert("Bitte wähle einen Kontakt aus, um zu chatten.");
        return;
    }

    const text = messageInput.value.trim();
    if (text === "") return;

    if (socket && socket.readyState === WebSocket.OPEN) {
        // Send message via WebSocket
        socket.send(JSON.stringify({
            receiver: activeReceiver,
            text: text
        }));
        messageInput.value = "";
    } else {
        alert("Verbindung zum Server wird hergestellt. Bitte versuche es gleich noch einmal.");
    }
});

function loadUsers() {
    fetch("/users")
        .then(response => response.json())
        .then(users => {
            // Find and update current user's details
            const me = users.find(u => u.username === currentUser);
            if (me) {
                if (me.profilePicture) {
                    currentUserAvatar.innerHTML = `<img src="${me.profilePicture}" alt="avatar" />`;
                    selfLargeAvatar.innerHTML = `<img src="${me.profilePicture}" alt="avatar" />`;
                } else {
                    currentUserAvatar.textContent = currentUser.charAt(0).toUpperCase();
                    selfLargeAvatar.textContent = currentUser.charAt(0).toUpperCase();
                }

                if (document.activeElement !== currentUserPersonalMsg) {
                    currentUserPersonalMsg.value = me.personalMessage || "";
                }
                userStatusSelect.value = me.status || "Online";
            }

            allUsers = users.filter(u => u.username !== currentUser);
            contactsList.innerHTML = "";

            if (allUsers.length === 0) {
                contactsList.innerHTML = `<p style="padding: 20px; color: #888;">Noch keine anderen Benutzer registriert.</p>`;
                activeReceiver = null;
                window.activeReceiver = null;
                chatHeaderName.textContent = "Kein aktiver Chat";
                chatHeaderPersonalMsg.textContent = "Wähle einen Kontakt aus";
                activeReceiverAvatar.textContent = "?";
                buddyLargeAvatar.textContent = "?";
                messageArea.innerHTML = "";
                return;
            }

            // Find current active receiver details if they changed
            const activeUserObj = allUsers.find(u => u.username === activeReceiver);
            if (activeUserObj) {
                updateHeaderAvatar(activeUserObj);
            }

            // If no active receiver is selected, select the first one
            if (!activeReceiver && allUsers.length > 0) {
                selectReceiver(allUsers[0]);
            }

            const statusColorMap = {
                "Online": "#31a24c",
                "Busy": "#e02424",
                "Away": "#f59e0b",
                "Offline": "#9ca3af"
            };

            allUsers.forEach(user => {
                const div = document.createElement("div");
                div.className = "contact-box" + (user.username === activeReceiver ? " active" : "");

                const avatarHtml = user.profilePicture
                    ? `<img src="${user.profilePicture}" alt="avatar" />`
                    : user.username.charAt(0).toUpperCase();

                const statusNameMap = {
                    "Online": "Online",
                    "Busy": "Beschäftigt",
                    "Away": "Abwesend",
                    "Offline": "Offline"
                };
                const statusColor = statusColorMap[user.status] || "#31a24c";
                const displayStatus = statusNameMap[user.status] || user.status || "Online";
                const displayName = `${escapeHTML(user.username)} <span style="font-weight: normal; font-size: 11px; color: ${statusColor}">(${escapeHTML(displayStatus)})</span>`;
                const personalMsgHtml = user.personalMessage
                    ? `<p class="contact-personal-msg" style="font-size: 10px; font-style: italic; color: #597394; max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">"${escapeHTML(user.personalMessage)}"</p>`
                    : `<p style="font-size: 10px; color: #7b94b2;">Klicke zum Chatten</p>`;

                div.innerHTML = `
                    <div class="avatar small" style="border: 2px solid ${statusColor};">${avatarHtml}</div>
                    <div style="flex: 1; min-width: 0;">
                        <h4 style="display: flex; justify-content: space-between; align-items: center; font-size: 12px;">${displayName}</h4>
                        ${personalMsgHtml}
                    </div>
                `;
                div.addEventListener("click", () => selectReceiver(user));
                contactsList.appendChild(div);
            });
        });
}

function selectReceiver(receiver, isGroup) {
    if (isGroup) {
        activeReceiver = "group_" + receiver.id;
        window.activeReceiver = activeReceiver;
        activeReceiverIsGroup = true;
        chatHeaderName.textContent = receiver.name;
        chatHeaderPersonalMsg.innerHTML = `<span style="color: #028ec4; font-weight: bold;">Gruppenchat</span> (Erstellt von: ${escapeHTML(receiver.creator)})`;

        buddyLargeAvatar.innerHTML = `<span style="font-size: 48px;">👥</span>`;

        // Highlight active group in groups list, remove active class from others
        document.querySelectorAll("#groupsList .contact-box").forEach(box => {
            const h4 = box.querySelector("h4");
            if (h4 && h4.textContent.includes(receiver.name)) {
                box.classList.add("active");
            } else {
                box.classList.remove("active");
            }
        });
        document.querySelectorAll("#contactsList .contact-box").forEach(box => box.classList.remove("active"));
    } else {
        activeReceiver = receiver.username;
        window.activeReceiver = activeReceiver;
        activeReceiverIsGroup = false;
        chatHeaderName.textContent = receiver.username;

        const statusColorMap = {
            "Online": "#31a24c",
            "Busy": "#e02424",
            "Away": "#f59e0b",
            "Offline": "#9ca3af"
        };
        const statusNameMap = {
            "Online": "Online",
            "Busy": "Beschäftigt",
            "Away": "Abwesend",
            "Offline": "Offline"
        };
        const statusColor = statusColorMap[receiver.status] || "#31a24c";
        const displayStatus = statusNameMap[receiver.status] || receiver.status || "Online";

        chatHeaderPersonalMsg.innerHTML = `<span style="color: ${statusColor}; font-weight: bold;">${escapeHTML(displayStatus)}</span>${receiver.personalMessage ? ' - "' + escapeHTML(receiver.personalMessage) + '"' : ''}`;

        updateHeaderAvatar(receiver);

        // Update buddy large avatar
        if (receiver.profilePicture) {
            buddyLargeAvatar.innerHTML = `<img src="${receiver.profilePicture}" alt="avatar" />`;
        } else {
            buddyLargeAvatar.innerHTML = receiver.username.charAt(0).toUpperCase();
        }

        // Highlight active contact, remove active class from groups
        document.querySelectorAll("#contactsList .contact-box").forEach(box => {
            const h4El = box.querySelector("h4");
            if (h4El && h4El.textContent.startsWith(receiver.username)) {
                box.classList.add("active");
            } else {
                box.classList.remove("active");
            }
        });
        document.querySelectorAll("#groupsList .contact-box").forEach(box => box.classList.remove("active"));
    }

    loadMessages();
}

function updateHeaderAvatar(user) {
    if (user.profilePicture) {
        activeReceiverAvatar.innerHTML = `<img src="${user.profilePicture}" alt="avatar" />`;
    } else {
        activeReceiverAvatar.innerHTML = user.username.charAt(0).toUpperCase();
    }
}

function appendMessage(message) {
    const div = document.createElement("div");
    div.className = `message-line ${message.sender === currentUser ? 'self' : 'buddy'}`;

    div.innerHTML = `
        <div class="msg-header">${escapeHTML(message.sender)} sagt (${escapeHTML(message.time)}):</div>
        <div class="msg-body">${escapeHTML(message.text)}</div>
    `;
    messageArea.appendChild(div);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function loadMessages() {
    if (!activeReceiver) {
        messageArea.innerHTML = "";
        return;
    }

    fetch("/messages?user=" + (activeReceiverIsGroup ? activeReceiver : currentUser))
        .then(response => response.json())
        .then(messages => {
            messageArea.innerHTML = "";

            // Filter messages only between currentUser and activeReceiver (or group messages)
            const conversation = messages.filter(message => {
                if (activeReceiverIsGroup) {
                    return message.receiver === activeReceiver;
                } else {
                    return (message.sender === currentUser && message.receiver === activeReceiver) ||
                        (message.sender === activeReceiver && message.receiver === currentUser);
                }
            });

            conversation.forEach(message => {
                appendMessage(message);
            });

            messageArea.scrollTop = messageArea.scrollHeight;
        });
}

logoutButton.addEventListener("click", function () {
    if (socket) {
        socket.close();
    }
    localStorage.removeItem("username");
    window.location.href = "/index.html";
});

// Group listing and creation handlers
const createGroupModal = document.getElementById("createGroupModal");
const closeGroupModalBtn = document.getElementById("closeGroupModalBtn");
const submitCreateGroupBtn = document.getElementById("submitCreateGroupBtn");
const newGroupNameInput = document.getElementById("newGroupNameInput");
const groupMembersChecklist = document.getElementById("groupMembersChecklist");

if (createGroupBtn) {
    createGroupBtn.addEventListener("click", () => {
        // Clear inputs
        newGroupNameInput.value = "";
        groupMembersChecklist.innerHTML = "";

        // Populate checklist with other users
        if (allUsers.length === 0) {
            groupMembersChecklist.innerHTML = `<p style="font-size: 11px; color: #888;">Keine Kontakte online/registriert</p>`;
        } else {
            allUsers.forEach(user => {
                const label = document.createElement("label");
                label.style.display = "flex";
                label.style.alignItems = "center";
                label.style.gap = "6px";
                label.style.fontSize = "12px";
                label.style.cursor = "pointer";
                label.style.color = "#1e395b";
                label.innerHTML = `<input type="checkbox" class="group-member-checkbox" value="${escapeHTML(user.username)}" /> ${escapeHTML(user.username)}`;
                groupMembersChecklist.appendChild(label);
            });
        }

        // Show modal
        createGroupModal.style.display = "flex";
    });
}

if (closeGroupModalBtn) {
    closeGroupModalBtn.addEventListener("click", () => {
        createGroupModal.style.display = "none";
    });
}

if (submitCreateGroupBtn) {
    submitCreateGroupBtn.addEventListener("click", () => {
        const name = newGroupNameInput.value.trim();
        if (!name) {
            alert("Bitte gib einen Gruppennamen ein.");
            return;
        }

        // Get selected members
        const selectedMembers = [];
        document.querySelectorAll(".group-member-checkbox:checked").forEach(cb => {
            selectedMembers.push(cb.value);
        });

        fetch("/groups", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: name,
                creator: currentUser,
                members: selectedMembers.join(",")
            })
        })
            .then(res => {
                if (res.ok) {
                    createGroupModal.style.display = "none";
                    loadGroups();
                } else {
                    alert("Fehler beim Erstellen der Gruppe.");
                }
            })
            .catch(err => console.error("Error creating group:", err));
    });
}

function loadGroups() {
    fetch("/groups?user=" + encodeURIComponent(currentUser))
        .then(res => res.json())
        .then(groups => {
            allGroups = groups;
            groupsList.innerHTML = "";
            if (groups.length === 0) {
                groupsList.innerHTML = `<p style="padding: 10px; color: #888; font-size: 11px;">Keine Gruppen vorhanden</p>`;
                return;
            }
            groups.forEach(group => {
                const div = document.createElement("div");
                div.className = "contact-box" + (("group_" + group.id) === activeReceiver ? " active" : "");
                div.style.display = "flex";
                div.style.justifyContent = "space-between";
                div.style.alignItems = "center";
                div.style.padding = "6px 8px";

                const infoDiv = document.createElement("div");
                infoDiv.style.flex = "1";
                infoDiv.style.cursor = "pointer";
                infoDiv.innerHTML = `
                    <h4 style="font-size: 12px; font-weight: bold; color: #1e395b; display: flex; align-items: center; gap: 4px;">
                        <span>👥</span> ${escapeHTML(group.name)}
                    </h4>
                    <p style="font-size: 10px; color: #7b94b2;">Erstellt von: ${escapeHTML(group.creator)}</p>
                `;
                infoDiv.addEventListener("click", () => {
                    selectReceiver(group, true);
                });

                div.appendChild(infoDiv);

                if (group.creator === currentUser) {
                    const deleteBtn = document.createElement("button");
                    deleteBtn.innerHTML = "❌";
                    deleteBtn.style.background = "none";
                    deleteBtn.style.border = "none";
                    deleteBtn.style.cursor = "pointer";
                    deleteBtn.style.fontSize = "10px";
                    deleteBtn.style.padding = "2px 4px";
                    deleteBtn.addEventListener("click", (e) => {
                        e.stopPropagation();
                        if (confirm(`Möchtest du die Gruppe "${group.name}" wirklich löschen?`)) {
                            fetch(`/groups/${group.id}`, {
                                method: "DELETE"
                            })
                                .then(res => {
                                    if (res.ok) {
                                        if (activeReceiver === "group_" + group.id) {
                                            activeReceiver = null;
                                            activeReceiverIsGroup = false;
                                            chatHeaderName.textContent = "Kein aktiver Chat";
                                            chatHeaderPersonalMsg.textContent = "Wähle einen Kontakt oder eine Gruppe aus";
                                        }
                                        loadGroups();
                                        loadMessages();
                                    } else {
                                        alert("Fehler beim Löschen der Gruppe.");
                                    }
                                });
                        }
                    });
                    div.appendChild(deleteBtn);
                }

                groupsList.appendChild(div);
            });
        })
        .catch(err => console.error("Error loading groups:", err));
}

// Initial load
loadUsers();
loadGroups();
connectWebSocket();

// Periodic polls for users and groups list
setInterval(() => {
    loadUsers();
    loadGroups();
}, 5000);