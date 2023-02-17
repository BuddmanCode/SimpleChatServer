import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Scanner;

public class Connection implements Runnable{
    private Socket socket;
    private ChatServer server;
    private InputStream is;
    private OutputStream os;
    private Scanner in;
    private PrintStream out;
    public Connection(ChatServer server, Socket socket) throws IOException {
        this.socket = socket;
        this.server = server;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            in = new Scanner(is);
            out = new PrintStream(os);
        } catch (IOException e) {
            throw e;
        }
    }
    public void sendString(String message) throws Exception {
        try {
            out.println(message);
        } catch (Exception e) {
            throw e;
        }
    }
    public String getString() throws Exception {
        try {
            return in.nextLine();
        } catch (Exception e) {
            throw e;
        }
    }
    @Override
    public void run() {
        String name;
        boolean repeat = true;
        try {
            sendString("Server: Привет новый друг! Как теба зовут?");
            while (repeat) {
                try {
                    name = getString();
                    if (name != null) {
                        if (!name.equals("Server") && !name.equals("") && server.registerMember(new ChatMember(name, this, server))) {
                            repeat = false;
                            sendString(new StringBuilder("Добро пожаловать, ").append(name).append("!\nДля выхода напечатай :leave").toString());;
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
