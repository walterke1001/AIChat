document.addEventListener("DOMContentLoaded", () => {
    const chatInput = document.getElementById("chatInput");
    chatInput.addEventListener("keyup", (event) => {
        if (event.key === "Enter") {
            sendMessage();
        }
    });
});

function sendMessage() {
    const chatInput = document.getElementById("chatInput");
    const messageContent = chatInput.value.trim();

    if (messageContent === "") return;

    appendMessage(messageContent, "user");
    chatInput.value = "";

    console.log("Sending message:", messageContent);

    // 构建 ChatReqDTO 请求对象
    const chatRequest = {
        model: "",
        chatId: "unique-chat-id", // 根据需要生成唯一的 chatId
        stream: false,
        detail: false,
        variables: {},
        messages: [
            {
                content: messageContent,
                role: "user"
            }
        ]
    };

    fetch("http://127.0.0.1:8088/api/chat", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(chatRequest)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log("Received response:", data);
            if (data.choices && data.choices.length > 0) {
                const aiMessage = data.choices[0].message.content;
                appendMessage(aiMessage, "ai");
            }
        })
        .catch(error => {
            console.error("Error:", error);
        });
}

function appendMessage(message, sender) {
    const chatHistory = document.getElementById("chatHistory");
    const messageElement = document.createElement("div");
    messageElement.classList.add("chat-message");
    messageElement.classList.add(sender === "user" ? "user-message" : "ai-message");
    messageElement.textContent = message;
    chatHistory.appendChild(messageElement);
    chatHistory.scrollTop = chatHistory.scrollHeight;
}
