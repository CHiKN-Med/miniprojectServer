// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java


// LINJE 140 EMILS KODE
// *********************************************************





import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{
	public static void main(String[] args) throws IOException
	{
		// server is listening on port 5056
		ServerSocket ss = new ServerSocket(5056);

		// running infinite loop for getting
		// client request
		while (true)
		{
			Socket s = null;

			try
			{
				// socket object to receive incoming client requests
				s = ss.accept();

				System.out.println("A new client is connected : " + s);

				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());

				System.out.println("Assigning new thread for this client");

				// create a new thread object
				Thread t = new ClientHandler(s, dis, dos);

				// Invoking the start() method
				t.start();

			}
			catch (Exception e){
				s.close();
				e.printStackTrace();
			}
		}
	}
}

// ClientHandler class
class ClientHandler extends Thread
{
	DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
	DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
	final DataInputStream dis;
	final DataOutputStream dos;
	final Socket s;


	// Constructor
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
	{
		this.s = s;
		this.dis = dis;
		this.dos = dos;
	}

	@Override
	public void run()
	{
		String received;
		String toreturn;
		while (true)
		{
			try {

				// Ask user what he wants
				dos.writeUTF("What do you want?[Date | Time]..\n"+
						"Type Exit to terminate connection.");

				// receive the answer from client
				received = dis.readUTF();

				if(received.equals("Exit"))
				{
					System.out.println("Client " + this.s + " sends exit...");
					System.out.println("Closing this connection.");
					this.s.close();
					System.out.println("Connection closed");
					break;
				}

				// creating Date object
				Date date = new Date();

				// write on output stream based on the
				// answer from the client
				switch (received) {

					case "Date" :
						toreturn = fordate.format(date);
						dos.writeUTF(toreturn);
						break;

					case "Time" :
						toreturn = fortime.format(date);
						dos.writeUTF(toreturn);
						break;

					default:
						dos.writeUTF("Invalid input");
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try
		{
			// closing resources
			this.dis.close();
			this.dos.close();

		}catch(IOException e){
			e.printStackTrace();
		}
	}
} 
//_____________________________________________________________________________________________________________________

package Server;

		import java.io.*;
		import java.net.*;
		import java.util.ArrayList;
		import java.util.Date;

public class Server {
	ArrayList<UserThread> users;
	private ServerSocket serverSocket;
	private int port;
	Quiz quiz = new Quiz();
	boolean startTheGame = false;


	public Server(){
		port = 8000;
		users = new ArrayList<>();
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.initiateServer();
	}


	public void initiateServer(){
		try {
			// Create a server socket
			serverSocket = new ServerSocket(port);

			System.out.println("Server started at " + new Date() + "\n");
			while (true) {
				// Listen for a connection request
				Socket socket = serverSocket.accept();
				// Create data input and output streams
				UserThread user = new UserThread(this, socket, quiz);
				users.add(user);
				user.start();
				// ta.appendText("\n" + users.size());
				//});
			}

		}
		catch(IOException ex) {
			ex.printStackTrace();
		}


	}

	public void sendAll(String message){
		for(UserThread userThread : users){
			userThread.sendMessage(message);
		}
	}

	public boolean allAnswered(){
		boolean done = true;
		for(UserThread userThread : users)
			if (!userThread.haveAnswered) {
				done = false;
			}
		if(done==true){
			return true;
		}
		else{
			return false;
		}
	}

	public void sendAllBool(Boolean b){
		for(UserThread userThread : users){
			userThread.sendBool(b);
		}
	}

	public ArrayList<UserThread> getUsers() {
		return users;
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
	boolean[] question = {true,false,false,false};
	boolean haveAnswered = false;

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
			setUserName(readMessage());
			server.sendAll("\nNew user joined: " + name);
			while (true) {
				String clientMessage = readMessage();
				if(clientMessage.equals("STARTTHEGAME")){
					server.startTheGame=true;
				}
				if(server.startTheGame){
					server.sendAll("STARTTHEGAME");
					break;
				}
				server.sendAll("\n" + getUserName() + ": " + clientMessage);
			}

			while(true) {

				if (question[0]) {
					sendQuestion(quiz, 0);
					question[0] = false;
				}

				if (server.allAnswered()) {
					question[1] = true;
					haveAnswered = false;
				}

				if (question[1]) {
					sendQuestion(quiz, 1);
					question[1] = false;
				}

				if (server.allAnswered()) {
					question[2] = true;
					haveAnswered = false;
				}

				if (question[2]) {
					sendQuestion(quiz, 2);
					question[2] = false;
				}

				if (server.allAnswered()) {
					question[3] = true;
					haveAnswered = false;
				}

				if (question[3]) {
					sendQuestion(quiz, 3);
					question[3] = false;
				}

				if (server.allAnswered()) {
					question[4] = true;
					haveAnswered = false;
				}

				if (question[4]) {
					sendQuestion(quiz, 4);
					question[4] = false;
				}

				if (server.allAnswered()) {
					server.sendAll("\n" + name + "'s score is " + score);
					break;
				}


			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int getScore(){
		return score;
	}

	void setScore(int score){
		this.score=score;
	}

	String getUserName(){
		return name;
	}

	void setUserName(String name){
		this.name=name;
	}

	public void sendMessage(String m){
		try {
			dos.writeUTF(m);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String readMessage(){
		String m = null;
		try {
			m = dis.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}

	public int readInt(){
		int m = 0;
		try {
			m = dis.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}

	public boolean readBool(){
		boolean b = false;
		try {
			b = dis.readBoolean();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}

	public void sendBool(Boolean b){
		try {
			dos.writeBoolean(b);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendQuestion(Quiz quiz, int questionsNumber){
		sendMessage("\nQuestion: " + quiz.questions[questionsNumber] + "\n" + quiz.options[questionsNumber]);
		haveAnswered=readBool();
		int answer = readInt();
		if(answer==quiz.correctAnswers[questionsNumber]){
			sendMessage("\nCORRECT");
			score++;
		}
		else{
			sendMessage("\nWRONG");
		}
	}



}

