import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.TreeSet;


public class ChatServer implements Runnable{ //распределяет сообщения между участниками чата
    ServerSocket server;
    private final int port;
    TreeSet<ChatMember> chatMembers = new TreeSet<ChatMember>();
    ArrayDeque<String> history = new ArrayDeque<String>();

    public ChatServer(int port) {
        this.port = port;
    }

    protected boolean registerMember(ChatMember member) {
        if (!chatMembers.contains(member)) { //Если такого пользователя нет
            member.send("================================================");
            for (String s : history) { //рассказываем новичку, что было в чате
                member.send(s);
            }
            spreadMessage(null, new StringBuilder(member.getName()).append(" заходит в чат.").toString()); //оповещаем участников о новичке
            chatMembers.add(member); //и, наконец, добавляем новичка в чат
            new Thread(member).start(); //дальше он будет сам управлять соединением
            return true;
        } else {
            return false;
        }
    }
    public void removeMember(ChatMember member) { //выкидываем пользователя из чата
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
    protected void processMessage(ChatMember member, String message) { //обрабатываем сообщение участника чата
        if (message != null) {
            if (message.equals(":leave")) {
                removeMember(member);
            } else {
                spreadMessage(member, message); //в стандартном случае рассылаем сообщение участникам
            }
        } else {
            removeMember(member);
        }
    }
    protected void spreadMessage(ChatMember member, String message) {
        StringBuilder buff = new StringBuilder(member==null?"Server":member.getName()).append(": ").append(message); //готовим строку
        if(history.size()>14) {
            history.pop();
        }
        history.add(buff.toString()); //добавляем в историю (для новичков)
        for(Iterator<ChatMember> i = chatMembers.iterator(); i.hasNext();) { //рассылаем сообщение участникам чата
            ChatMember item = i.next();
            if (item!=member) { //разумеется отправителю не присылаем
                item.send(buff.toString());
            }
        }
    }
    public static void main(String[] args) {
        ChatServer srv = new ChatServer(1234);
        Thread thread = new Thread(srv);
        thread.start(); //Создаём экземпляр сервера в отдельном потоке
        while(true) {
            break; //Здесь потом возможно добавлю меню сервера
        }
    }
    @Override
    public void run() {
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
                Connection connection = new Connection(this, socket); //Новое клиентское соединение
                Thread thread = new Thread(connection);
                thread.start(); //будем знакомиться
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}