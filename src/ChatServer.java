import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.TreeSet;


public class ChatServer implements Runnable{
    private final int port;
    TreeSet<ChatMember> chatMembers = new TreeSet<ChatMember>();
    ArrayDeque<String> history = new ArrayDeque<String>();

    public ChatServer(int port) {
        this.port = port;
    }

    protected boolean registerMember(ChatMember member) {
        if (!chatMembers.contains(member)) {
            member.send("================================================");
            for (Iterator<String> i = history.iterator(); i.hasNext();) {
                member.send(i.next());
            }
            spreadMessage(null, new StringBuilder(member.getName()).append(" заходит в чат.").toString());
            chatMembers.add(member);
            new Thread(member).start();
            return true;
        } else {
            return false;
        }
    }
    public void removeMember(ChatMember member) {
        try {
            member.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            chatMembers.remove(member);
            spreadMessage(null, new StringBuilder(member.getName()).append(" покинул чат.").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Connection closed");
    }
    protected void processMessage(ChatMember member, String message) {
        if (message != null) {
            if (message.equals(":leave")) {
                removeMember(member);
            } else {
                spreadMessage(member, message);
            }
        } else {
            removeMember(member);
        }
    }
    protected void spreadMessage(ChatMember member, String message) {
        StringBuilder buff = new StringBuilder(member==null?"Server":member.getName()).append(": ").append(message);
        if(history.size()>14) {
            history.pop();
        }
        history.add(buff.toString());
        for(Iterator<ChatMember> i = chatMembers.iterator(); i.hasNext();) {
            ChatMember item = i.next();
            if (item!=member) {
                item.send(buff.toString());
            }
        }
    }
    public static void main(String[] args) {
        ChatServer srv = new ChatServer(1234);
        Thread thread = new Thread(srv);
        thread.start();
        while(true) {
            break; //Здесь возможно добавлю меню сервера
        }
    }
    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            System.out.println("Waiting...");
            try {
                Socket socket = server.accept();
                System.out.println("New connection");
                Connection connection = new Connection(this, socket);
                Thread thread = new Thread(connection);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}