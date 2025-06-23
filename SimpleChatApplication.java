import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

// Enum for User Status
enum UserStatus {
    ONLINE, OFFLINE
}

/**
 * Represents a user in the chat system.
 */
class User {
    private final String userId;
    private String username;
    private String password; // In a real app, this would be hashed
    private UserStatus status;

    public User(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = UserStatus.OFFLINE;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserStatus getStatus() {
        return status;
    }

    // Methods
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return username + " (" + status + ")";
    }
}

/**
 * Represents a single message within a chat.
 */
class Message {
    private final String messageId;
    private final String senderId;
    private final String content;
    private final Instant timestamp;

    public Message(String messageId, String senderId, String content) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = Instant.now();
    }

    // Getters
    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

/**
 * Abstract base class for all chat types.
 */
abstract class Chat {
    protected final String chatId;
    protected final List<String> userIds;
    protected final List<Message> messages;

    public Chat(String chatId) {
        this.chatId = chatId;
        this.userIds = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public String getChatId() {
        return chatId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public abstract String getChatName(String currentUserId, Map<String, User> userDatabase);
}

/**
 * Represents a one-on-one direct chat between two users.
 */
class DirectChat extends Chat {
    public DirectChat(String chatId, String userId1, String userId2) {
        super(chatId);
        this.userIds.add(userId1);
        this.userIds.add(userId2);
    }

    @Override
    public String getChatName(String currentUserId, Map<String, User> userDatabase) {
        String otherUserId = userIds.stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);
        return userDatabase.getOrDefault(otherUserId, new User(null, "Unknown", null)).getUsername();
    }
}

/**
 * Represents a group chat with multiple users.
 */
class GroupChat extends Chat {
    private String groupName;

    public GroupChat(String chatId, String groupName, String creatorId) {
        super(chatId);
        this.groupName = groupName;
        this.userIds.add(creatorId);
    }

    public void addUser(String userId) {
        if (!userIds.contains(userId)) {
            userIds.add(userId);
        }
    }

    public void removeUser(String userId) {
        userIds.remove(userId);
    }

    @Override
    public String getChatName(String currentUserId, Map<String, User> userDatabase) {
        return groupName;
    }
}

/**
 * Facade class to manage all chat application logic.
 */
class ChatService {
    // In-memory databases to simulate storage
    private final Map<String, User> userDatabase = new HashMap<>();
    private final Map<String, Chat> chatDatabase = new HashMap<>();
    private int userCounter = 0;
    private int chatCounter = 0;
    private int messageCounter = 0;

    public User registerUser(String username, String password) {
        if (userDatabase.values().stream().anyMatch(u -> u.getUsername().equals(username))) {
            System.out.println("Error: Username '" + username + "' already exists.");
            return null;
        }
        String userId = "user" + (++userCounter);
        User newUser = new User(userId, username, password);
        userDatabase.put(userId, newUser);
        System.out.println("User '" + username + "' registered successfully with ID: " + userId);
        return newUser;
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userDatabase.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();

        if (userOpt.isPresent() && userOpt.get().checkPassword(password)) {
            userOpt.get().setStatus(UserStatus.ONLINE);
            System.out.println("User '" + username + "' logged in.");
            return userOpt;
        }
        System.out.println("Error: Invalid username or password.");
        return Optional.empty();
    }

    public void logout(String userId) {
        if (userDatabase.containsKey(userId)) {
            userDatabase.get(userId).setStatus(UserStatus.OFFLINE);
            System.out.println("User '" + userDatabase.get(userId).getUsername() + "' logged out.");
        }
    }

    public Chat createDirectChat(String userId1, String userId2) {
        String chatId = "chat" + (++chatCounter);
        DirectChat chat = new DirectChat(chatId, userId1, userId2);
        chatDatabase.put(chatId, chat);
        System.out.println("Direct chat created between " + userDatabase.get(userId1).getUsername() + " and "
                + userDatabase.get(userId2).getUsername());
        return chat;
    }

    public Chat createGroupChat(String groupName, String creatorId) {
        String chatId = "chat" + (++chatCounter);
        GroupChat chat = new GroupChat(chatId, groupName, creatorId);
        chatDatabase.put(chatId, chat);
        System.out.println("Group chat '" + groupName + "' created by " + userDatabase.get(creatorId).getUsername());
        return chat;
    }

    public void addUserToGroup(String chatId, String userId) {
        Chat chat = chatDatabase.get(chatId);
        if (chat instanceof GroupChat) {
            ((GroupChat) chat).addUser(userId);
            System.out.println("User " + userDatabase.get(userId).getUsername() + " added to group "
                    + ((GroupChat) chat).getChatName(userId, userDatabase));
        }
    }

    public void sendMessage(String senderId, String chatId, String content) {
        if (!userDatabase.containsKey(senderId) || !chatDatabase.containsKey(chatId)) {
            System.out.println("Error: Invalid sender or chat ID.");
            return;
        }
        Chat chat = chatDatabase.get(chatId);
        if (!chat.getUserIds().contains(senderId)) {
            System.out.println("Error: Sender is not a member of this chat.");
            return;
        }

        String messageId = "msg" + (++messageCounter);
        Message message = new Message(messageId, senderId, content);
        chat.addMessage(message);
    }

    public void displayChat(String chatId, String currentUserId) {
        if (!chatDatabase.containsKey(chatId)) {
            System.out.println("Chat not found.");
            return;
        }
        Chat chat = chatDatabase.get(chatId);
        System.out.println("\n--- Chat: " + chat.getChatName(currentUserId, userDatabase) + " ---");

        for (Message msg : chat.getMessages()) {
            String senderName = userDatabase.get(msg.getSenderId()).getUsername();
            System.out.println("[" + msg.getTimestamp().toString().substring(11, 19) + "] " + senderName + ": "
                    + msg.getContent());
        }
        System.out.println("------------------------------------");
    }

    public void listUsers() {
        System.out.println("\n--- All Users ---");
        userDatabase.values().forEach(System.out::println);
        System.out.println("-------------------");
    }
}

/**
 * Main class to demonstrate the Simple Chat Application.
 */
public class SimpleChatApplication {
    public static void main(String[] args) {
        // 1. Setup
        ChatService service = new ChatService();

        // 2. Register users
        User alice = service.registerUser("Alice", "pass123");
        User bob = service.registerUser("Bob", "pass456");
        User charlie = service.registerUser("Charlie", "pass789");

        service.listUsers();

        // 3. Users log in
        service.login("Alice", "pass123");
        service.login("Bob", "pass456");

        // 4. Create a direct chat between Alice and Bob
        Chat directChat = service.createDirectChat(alice.getUserId(), bob.getUserId());

        // 5. Send messages in the direct chat
        service.sendMessage(alice.getUserId(), directChat.getChatId(), "Hi Bob!");
        service.sendMessage(bob.getUserId(), directChat.getChatId(), "Hey Alice, how are you?");

        // 6. Display the direct chat
        service.displayChat(directChat.getChatId(), alice.getUserId());

        // 7. Create a group chat
        Chat groupChat = service.createGroupChat("Project Team", alice.getUserId());

        // 8. Add members to the group chat
        service.addUserToGroup(groupChat.getChatId(), bob.getUserId());
        service.addUserToGroup(groupChat.getChatId(), charlie.getUserId()); // Charlie is offline

        // 9. Send messages in the group chat
        service.sendMessage(alice.getUserId(), groupChat.getChatId(), "Welcome to the team chat!");
        service.sendMessage(bob.getUserId(), groupChat.getChatId(), "Glad to be here.");

        // 10. Display the group chat
        service.displayChat(groupChat.getChatId(), alice.getUserId());

        // 11. User logs out
        service.logout(bob.getUserId());
        service.listUsers();
    }
}
