// Customer WebSocket connection (chỉ kết nối khi đã login)
function connectOrderStatusWS(userId) {
    if (!userId) return;
    const socket = new SockJS('/ws');
    const stomp = Stomp.over(socket);
    stomp.connect({}, function() {
        stomp.subscribe('/topic/order-status/' + userId, function(msg) {
            const data = JSON.parse(msg.body);
            console.log('Order update:', data);
            // Có thể show toast
        });
    });
}