package Server;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;

public class Server {
    ArrayList<UserThread> users;
    private final int port;
    Quiz quiz = new Quiz();
    boolean startTheGame = false;


    public Server() {
        port = 8000;
        users = new ArrayList<>();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.initiateServer();

    }



    public void initiateServer() {
        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Server started at " + new Date() + "\n");
            while (true) {
                // Listen for a connection request
                Socket socket = serverSocket.accept();
                // Create data input and output streams
                if(!startTheGame) {
                    UserThread user = new UserThread(this, socket, quiz);
                    users.add(user);
                    user.start();
                }
                else{
                    System.out.println("Unable to connect. A game is currently active.");
                }
            }



        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void sendAll(String message) throws IOException {
        for (int i = 0; i < users.size(); i++) {
            users.get(i).sendMessage(message);
        }
    }

        public void checkWinner(UserThread thisUser) throws IOException, InterruptedException {
        String theWinner = null;
        int highscore = 0;
        StringBuilder scoreboard = new StringBuilder();

        // Running through all the users and creating a scoreboard string. Also checking who has the highest score
        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getScore() > highscore)
            {
                theWinner = users.get(i).getUserName();
            }
            scoreboard.append(users.get(i).getUserName()).append(": ").append(users.get(i).getScore()).append("\n");
        }

        // sending all of this back to only the current users client
        thisUser.sendMessage(theWinner);
        thisUser.sendMessage(scoreboard.toString());

    }

    public synchronized boolean allDone() throws IOException, InterruptedException {
        boolean done = true;
        for(int i = 0; i<users.size(); i++){
            if(!users.get(i).done){
                done=false;
            }
        }
        return done;
    }



    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */

}

class UserThread extends Thread{
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket s;
    private Server server;
    private Quiz quiz;
    String name;
    int score;
    boolean done = false;

    public UserThread(Server server, Socket s, Quiz quiz) {
        this.s = s;
        this.server = server;
        score=0;
        this.quiz = quiz;
    }

    @Override
    public void run()
    {
        try {
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            System.out.println("\nThread has started");
            String username = readMessage();
            setUserName(username);
            server.sendAll("\nNew user joined: " + name);

            // LOBBY LOOP - >
            while (true) {
            String clientMessage = readMessage();

            if(clientMessage.equalsIgnoreCase("STARTTHEGAME")){
            server.startTheGame=true;
            server.sendAll("STARTTHEGAME");
            break;
            }

            if(server.startTheGame){
            break;
            }

            server.sendAll("\n" + getUserName() + ": " + clientMessage);

            }

            // QUIZ LOOP - >
            while(true) {

                for(int i = 0; i<quiz.questions.length; i++){
                    sendQuestion(quiz, i);
                }

                done = true;
                sendMessage("STOPTHEGAME");
                break;

            }

            // WAITING LOOP
            while(true){
                if(server.allDone()){
                    sendMessage("SHOWTHESCORE");
                    break;
                }
            }

            server.checkWinner(this);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    int getScore(){
        return score;
    }


    String getUserName(){
        return name;
    }

    void setUserName(String name){
        this.name=name;
    }

    public void sendMessage(String m) throws IOException {
            dos.writeUTF(m);
            dos.flush();
    }

    public String readMessage() throws IOException {
        return dis.readUTF();
    }


    public void sendQuestion(Quiz quiz, int questionsNumber) throws IOException, InterruptedException {
        sendMessage(quiz.questions[questionsNumber]);
        sendMessage(quiz.options[questionsNumber]);
        int answer = dis.readInt();
        if(answer==quiz.correctAnswers[questionsNumber])
        {
            score+=1;
        }
        System.out.println(getUserName() + ": " + getScore());
        dos.writeUTF(String.valueOf(quiz.correctAnswers[questionsNumber]));
        sleep(1000);
    }


}

