import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Connection implements Runnable{ //взаимодействует с клиентом, передаёт серверу потенциальных участников чата
    private final Socket socket;
    private final ChatServer server;
    private final Scanner in;
    private final PrintStream out;
    public Connection(ChatServer server, Socket socket) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new Scanner(socket.getInputStream());
        out = new PrintStream(socket.getOutputStream());
    }
    public void sendString(String message) throws Exception {
        out.println(message);
    }
    public String getString() throws Exception {
        return in.nextLine();
    }
    @Override
    public void run() {
        String name;
        boolean repeat = true;
        try {
            sendString("Server: Привет новый друг! Как теба зовут?");
            while (repeat) { //Выбор свободного имени
                try {
                    name = getString();
                    if (name != null) { //если ничего не отвалилось
                        //создаём экземпляр участника чата и пробуем зарегистрировать
                        if (!name.equals("Server") && !name.equals("") && server.registerMember(new ChatMember(name, this, server))) {
                            repeat = false;
                            sendString(new StringBuilder("Добро пожаловать, ").append(name).append("!\nДля выхода напечатай :leave").toString());
                        } else
                            sendString("Server: Это имя уже занято. Попробуй другое.");
                    } else {
                        System.out.println("Connection lost.");
                        repeat = false;
                        socket.close();
                    }
                } catch (Exception e) {
                    repeat = false;
                    e.printStackTrace();
                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void close() throws IOException {
        socket.close();
    }
}
