public void broadcastSystemMessage(String content) {
    Message msg = new Message();
    msg.setUsername("SYSTEM");
    msg.setContent(content);
    msg.setTimestamp(Instant.now());
    messageRepository.save(msg);

    String broadcast = String.format("{\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
            msg.getUsername(), msg.getContent(), msg.getTimestamp());

    for (WebSocketSession s : sessions) {
        try {
            if (s.isOpen()) s.sendMessage(new TextMessage(broadcast));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
