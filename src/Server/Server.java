package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class Server {
    private ArrayList<UserThread> users;
    private final int port;
    private Quiz quiz = new Quiz();
    public boolean startTheGame = false;

    // CONSTRUCTOR SETTING THE PORT TO 8000
    public Server() {
        port = 8000;
        users = new ArrayList<>();
    }

    // the main
    public static void main(String[] args) {
        // creating a type Server and calling the initiate function
        Server server = new Server();
        server.initiateServer();
    }

    // initiate function
    public void initiateServer() {
        try {
            // Create a serverSocket with the port number 8000
            ServerSocket serverSocket = new ServerSocket(port);
            // getting the IP and printing it so it can be shared with other users
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println("Server started at " + new Date() + "\n");
            System.out.println("Ask players to join following ip: " + inetAddress.getHostAddress());


            // Following while true loop waits for a client to connect to the socket,
            while (true) {
                // Listen for a connection request
                Socket socket = serverSocket.accept();

                // creating a new UserThread and adding it to the UserThread array if a game is not running an
                if(!startTheGame)
                {
                    UserThread user = new UserThread(this, socket, quiz);
                    users.add(user);
                    user.start(); // starting the UserThread (initializing its run() function)
                }
                else{
                    System.out.println("User tried to connect, but a game is already active");
                }
            }



        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // a function  using the DataOutputStream from each user in the array in order to send a message to all Clients.
    public synchronized void sendAll(String message) throws IOException {
        for (int i = 0; i < users.size(); i++) {
            users.get(i).sendMessage(message);
        }
    }


    // function sending winner and scoreboard to the current user,
    public void checkWinner(UserThread thisUser) throws IOException {
    String theWinner = null;
    int highscore = 0;
    StringBuilder scoreboard = new StringBuilder();

    // Running through all the users and creating a scoreboard string. Also checking who has the highest score
    for (int i = 0; i < users.size(); i++) {
        if(users.get(i).getScore() > highscore)
        {
            highscore=users.get(i).getScore();
            theWinner=users.get(i).getUserName();
        }
        scoreboard.append(users.get(i).getUserName()).append(": ").append(users.get(i).getScore()).append("\n");
    }

    // sending all of this back to only the current users client
    thisUser.sendMessage(theWinner);
    thisUser.sendMessage(scoreboard.toString());

    }


    // a function that returns fall if one or more user is not done
    public synchronized boolean allDone() throws IOException, InterruptedException {
        boolean done = true;
        for(int i = 0; i<users.size(); i++){
            if(!users.get(i).done){
                done=false;
            }
        }
        return done;
    }

}

class UserThread extends Thread{

    //ATTRIBUTES:
    private DataInputStream dis;
    private DataOutputStream dos;
    private final Socket s;
    private final Server server;
    private final Quiz quiz;
    private String name;
    private int score;
    public boolean done = false;


    // CONSTRUCTOR TAKING IN THE SERVER, SOCKET AND QUIZ FROM THE SERVER
    public UserThread(Server server, Socket s, Quiz quiz) {
        this.s = s;
        this.server = server;
        score=0;
        this.quiz = quiz;
    }

    // the run function which is initialized using .start() in the initiate function in the server
    @Override
    public void run()
    {
        try {
            // creating a new DataInputStream and a new DataOutputStream in order to communicate with the connected client
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            System.out.println("\nThread has started");

            // setting the username as the first message from the client
            String username = readMessage();
            setUserName(username);
            server.sendAll("\nNew user joined: " + name);

            //Lobby loop
            lobbyLoop();

            //QUIZ
            showQuestions();

            // WAITING LOOP
            while(true){
                if(server.allDone()){
                sendMessage("SHOWTHESCORE");
                break;
                }
            }

            // CHECKING WINNER
            server.checkWinner(this);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // for-loop going through all questions and sending them to the user by using th sendquestion function.
    // when all questions is send the user is set to be done.
    public void showQuestions() throws IOException, InterruptedException {
        Integer[] numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Collections.shuffle(Arrays.asList(numbers));
        for(int i = 0; i<quiz.questions.length; i++){
            sendQuestion(quiz, numbers[i]);
        }
        done = true;
        sendMessage("STOPTHEGAME");
    }

    // Using the DataOutputStream to send a message to the client
    public void sendMessage(String m) throws IOException {
            dos.writeUTF(m);
            dos.flush();
    }

    // reading a message from a client
    public String readMessage() throws IOException {
        return dis.readUTF();
    }

    // a function that sends a question, then the options and lastly it reads an int form the client and afterwards sends the right correct answer.
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


    // LOBBY LOOP CONSISTING OF A WHILE LOOP READING THE NEXT MESSAGE FROM THE CLIENT AND USING THE SEND ALL MESSAGE IN THE SERVER
    // IN ORDER TO SEND IT TO ALL CLIENTS
    // LOOP BREAKS IF THE SERVER GETS A "STARTTHEGAME" MESSAGE FROM THE CLIENT AND SENDS SAME MESSAGE RETURN TO ALL CLIENTS
    public void lobbyLoop() throws IOException {

        while (true) {
            String clientMessage = readMessage();

            if (clientMessage.equalsIgnoreCase("STARTTHEGAME")) {
                server.startTheGame = true;
                server.sendAll("STARTTHEGAME");
                break;
            }

            if (server.startTheGame) {
                break;
            }

            server.sendAll("\n" + getUserName() + ": " + clientMessage);

        }
    }

    // GETTERS AND SETTERS
    public int getScore(){
        return score;
    }

    public String getUserName(){
        return name;
    }

    public void setUserName(String name){
        this.name=name;
    }

}

