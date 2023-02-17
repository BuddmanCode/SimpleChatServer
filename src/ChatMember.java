import java.io.IOException;
import java.util.Objects;
public class ChatMember implements Runnable, Comparable<ChatMember> {
    private final String name;
    private Connection connection;
    private ChatServer server;
    private int hashCode;
    boolean repeat = true;
    public ChatMember(String name, Connection connection, ChatServer server) {
        this.name = name;
        this.connection = connection;
        this.server = server;
        this.hashCode = Objects.hash(name);
    }

    public void disconnect() throws IOException {
        repeat = false;
        connection.close();
    }
    public void send(String message) {
        try {
            connection.sendString(message);
        } catch (Exception e) {
            server.removeMember(this);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(repeat) {
            try {
                server.processMessage(this, connection.getString());
            } catch (Exception e) {
                server.removeMember(this);
                //e.printStackTrace();
            }
        }
    }
    public String getName() {
        return name;
    }
    public boolean isMe(Object o) {
        return this == o;
    }
    private boolean likeMe(Object o) {
        return this.getClass() == o.getClass();
    }
    @Override
    public boolean equals(Object o) {
        if (o!=null)
            if(likeMe(o))
                return isMe(o) || this.name.equals(((ChatMember) o).getName());
        return false;
    }
    @Override
    public int hashCode() {
        return hashCode;
    }
    @Override
    public int compareTo(ChatMember o) {
        return this.name.compareTo(o.getName());
    }
}
