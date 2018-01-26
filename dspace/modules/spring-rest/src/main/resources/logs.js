$(document).ready(function() {
    var messageList = $("#messages");
    var getMessageString = function(message) {
        var date = new Date(message.date);
        return "<li><p>Received: " + date + "</p><div>" + message.content + "</li>";
    };
    var socket = new SockJS('/guestbook');
    var stompClient = Stomp.over(socket);
    stompClient.connect({ }, function(frame) {
        // subscribe to the /topic/entries endpoint which feeds newly added messages
        stompClient.subscribe('/topic/entries', function(data) {
            // when a message is received add it to the end of the list
            var body = data.body;
            var message = JSON.parse(body);
            messageList.append(getMessageString(message));
        });
    });
    $("#send").on("click", function() {
        // send the message
        stompClient.send("/app/guestbook", {}, $("#message").val());
        $("#message").val("");
    });
});