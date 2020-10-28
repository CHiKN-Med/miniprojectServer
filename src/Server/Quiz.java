package Server;

import java.util.Scanner;

public class Quiz {

    String quizDescription;

    String[] questions = {"What is the biggest lake in the world?",
            "Who wrote the hit 'Ice Ice Baby' from 1989?",
            "What is the world's biggest land predator?",
            "What is the highest free fall a human has survived without parachute?",
            "In what year was the first Harry Potter book published?"};
    String[] options = {"1) Lake Michigan  \n2) The Caspian Sea \n3) Victoria Lake, \n4)Furesø",
            "1) Ice Cube \n2) Ice T \n3) Vanilla Ice \n4) Coldplay",
            "1) Polar Bear \n2) Lion \n3) Bengal Tiger \n4) Pug",
            "1) 32m \n2) 55m \n3) 93.8m \n4) over 10.000m",
            "1) 1993 \n2) 1995 \n3) 1998 \n4) 2001"};
    int[] correctAnswers = {2,3,1,4,3};
    int nrOfQuestions = questions.length;



    void checkAnswer(UserThread user, Scanner userInput, int i) {
        if(userInput.nextInt() == correctAnswers[i]) {
            user.score++;
            System.out.println("Correct"); }
        else {
            System.out.println("Wrong"); }
    }
}