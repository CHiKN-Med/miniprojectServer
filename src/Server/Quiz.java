package Server;

import java.util.Scanner;

public class Quiz {

    String quizDescription;

    String[] questions = {"Hvad hedder Eva?", "Hvad hedder george?", "Er lukas tysker?", "Er chi fra vietnam"};
    String[] options = {"1)lort \n2)ikke lort\n3)Stor lort", "1)lort \n2)ikke lort", "1) nej\n2) ja", "1) ja\n2) nej"};
    int[] correctAnswers = {2,1,1,0};
    int nrOfQuestions = questions.length;



    void checkAnswer(UserThread user, Scanner userInput, int i) {
        if(userInput.nextInt() == correctAnswers[i]) {
            user.score++;
            System.out.println("korrekt"); }
        else {
            System.out.println("forkert"); }
    }
}