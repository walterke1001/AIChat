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
        stream: true, // 你可以根据需要更改此参数以测试不同情况
        detail: false,
        variables: {},
        messages: [
            {
                content: messageContent,
                role: "user"
            }
        ]
    };

    if (chatRequest.stream) {
        fetch("http://127.0.0.1:8088/api/chat/stream", {
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
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let aiMessage = "";

                function read() {
                    reader.read().then(({ done, value }) => {
                        if (done) {
                            console.log("Stream complete");
                            return;
                        }

                        const chunk = decoder.decode(value, { stream: true });
                        const lines = chunk.split('\n');

                        for (let line of lines) {
                            line = line.trim();
                            if (line.startsWith('data: ')) {
                                line = line.slice(6).trim();
                            }

                            if (line === '[DONE]') {
                                console.log("Stream finished");
                                return;
                            }

                            if (line) {
                                try {
                                    const data = JSON.parse(line);
                                    if (data.choices && data.choices.length > 0) {
                                        aiMessage += data.choices[0].delta.content;
                                        appendMarkdownMessage(aiMessage, "ai", true);
                                    }
                                } catch (error) {
                                    console.error("JSON parsing error:", error);
                                }
                            }
                        }

                        read();
                    }).catch(error => {
                        console.error("Stream reading error:", error);
                    });
                }
                read();
            })
            .catch(error => {
                console.error("Fetch error:", error);
            });
    } else {
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
                    appendMarkdownMessage(aiMessage, "ai");
                }
            })
            .catch(error => {
                console.error("Error:", error);
            });
    }
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

function appendMarkdownMessage(message, sender, isStreaming = false) {
    const chatHistory = document.getElementById("chatHistory");
    let messageElement = document.querySelector('.ai-message');

    if (!isStreaming || !messageElement) {
        messageElement = document.createElement("div");
        messageElement.classList.add("chat-message");
        messageElement.classList.add(sender === "user" ? "user-message" : "ai-message");
        chatHistory.appendChild(messageElement);
    }

    messageElement.innerHTML = marked.parse(message);
    chatHistory.scrollTop = chatHistory.scrollHeight;
}
