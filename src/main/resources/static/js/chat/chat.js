$(document).ready(function() {
    const chatWindow = $('#chatWindow');
    const chatInput = $('#chatInput');
    const sendBtn = $('#sendBtn');

    function scrollToBottom() {
        chatWindow.scrollTop(chatWindow[0].scrollHeight);
    }

    function sendMessage() {
        const message = chatInput.val().trim();

        if (message) {
            const userMsgHtml = `
                <div class="message-row sent">
                    <div class="message-content">
                        <div class="bubble">${message}</div>
                    </div>
                </div>
            `;
            chatWindow.append(userMsgHtml);

            chatInput.val('');
            scrollToBottom();

            setTimeout(function() {
                receiveAiMessage();
            }, 1500);
        }
    }

    function receiveAiMessage() {
        const responses = [
            "그렇군요. 그런 생각이 들 수 있어요.",
            "말씀해주셔서 고마워요. 마음이 조금 편해지셨으면 좋겠어요.",
            "당신의 감정은 소중해요. 더 이야기해주시겠어요?",
            "저런, 많이 힘드셨겠네요. 제가 곁에 있을게요."
        ];
        const randomResponse = responses[Math.floor(Math.random() * responses.length)];

        const aiMsgHtml = `
            <div class="message-row received">
                <div class="profile-img">
                     <img src="/images/profile/robot_face.png" alt="AI" onerror="this.src='https://via.placeholder.com/50?text=AI'">
                </div>
                <div class="message-content">
                    <span class="sender-name">마음지킴이 AI</span>
                    <div class="bubble">${randomResponse}</div>
                </div>
            </div>
        `;

        chatWindow.append(aiMsgHtml);
        scrollToBottom();
    }

    sendBtn.click(sendMessage);

    chatInput.keypress(function(e) {
        if (e.which == 13) sendMessage();
    });

    scrollToBottom();
});