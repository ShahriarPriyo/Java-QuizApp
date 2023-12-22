import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class QuizApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter user role (admin/student): ");
        String userRole = scanner.nextLine().toLowerCase();

        if (userRole.equals("admin")) {
            adminLogin();
        } else if (userRole.equals("student")) {
            studentLogin();
        } else {
            System.out.println("Invalid user role. Exiting.");
        }
    }

    private static void adminLogin() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("System:> Enter your username\nUser:> ");
        String username = scanner.nextLine();
        System.out.print("System:> Enter password\nUser:> ");
        String password = scanner.nextLine();

        // Check if the entered credentials match those in the users.json file
        if (validateCredentials(username, password, "admin")) {
            System.out.println("System:> Welcome admin! Please create new questions in the question bank.");
            addQuestions();
        } else {
            System.out.println("System:> Invalid credentials. Exiting.");
        }
    }

    private static void addQuestions() {
        Scanner scanner = new Scanner(System.in);
        List<Question> quizData = new ArrayList<>();

        while (true) {
            System.out.print("System:> Input your question\nAdmin:> ");
            String question = scanner.nextLine();

            String[] options = new String[4];
            for (int i = 0; i < 4; i++) {
                System.out.print("System: Input option " + (i + 1) + ":\nAdmin: ");
                options[i] = scanner.nextLine();
            }

            System.out.print("System: What is the answer key?\nAdmin:> ");
            int answerKey = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            quizData.add(new Question(question, options, answerKey));

            System.out.print("System:> Saved successfully! Do you want to add more questions? (press s for start and q for quit)\nAdmin:> ");
            String moreQuestions = scanner.nextLine().toLowerCase();
            if (moreQuestions.equals("q")) {
                break;
            }
        }

        try (FileWriter fileWriter = new FileWriter("quiz.json")) {
            fileWriter.write("[\n");
            for (int i = 0; i < quizData.size(); i++) {
                fileWriter.write(quizData.get(i).toJsonString());
                if (i < quizData.size() - 1) {
                    fileWriter.write(",\n");
                }
            }
            fileWriter.write("\n]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void studentLogin() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("System:> Enter your username\nUser:> ");
        String username = scanner.nextLine();
        System.out.print("System:> Enter password\nUser:> ");
        String password = scanner.nextLine();

        // Check if the entered credentials match those in the users.json file
        if (validateCredentials(username, password, "student")) {
            System.out.println("System:> Welcome to the quiz! We will throw you 10 questions. Each MCQ mark is 1 and no negative marking. Are you ready? Press 's' for start.");
            startQuiz();
        } else {
            System.out.println("System:> Invalid credentials. Exiting.");
        }
    }

    private static boolean validateCredentials(String username, String password, String role) {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get("./src/main/resources/users.json")), StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonContent);

            // Iterate through the array to validate credentials
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userObject = jsonArray.getJSONObject(i);
                String storedUsername = userObject.getString("username");
                String storedPassword = userObject.getString("password");
                String storedRole = userObject.getString("role");

                if (username.equals(storedUsername) && password.equals(storedPassword) && role.equals(storedRole)) {
                    return true; // Credentials match
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false; // Credentials do not match or an error occurred
    }

    private static void startQuiz() {
        List<Question> quizData = readQuizData();
        Collections.shuffle(quizData);

        int score = 0;
        Scanner scanner = new Scanner(System.in);

        for (int i = 0; i < 10; i++) {
            Question currentQuestion = quizData.get(i);
            System.out.printf("\n[Question %d] %s\n", i + 1, currentQuestion.getQuestion());

            for (int j = 0; j < 4; j++) {
                System.out.printf("%d. %s\n", j + 1, currentQuestion.getOptions()[j]);
            }

            System.out.print("Student:> ");
            int userAnswer;
            try {
                userAnswer = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character
                if (1 <= userAnswer && userAnswer <= 4) {
                    if (userAnswer == currentQuestion.getAnswerKey()) {
                        score++;
                    }
                } else {
                    System.out.println("System:> Invalid input. Moving to the next question.");
                }
            } catch (Exception e) {
                System.out.println("System:> Invalid input. Moving to the next question.");
                scanner.nextLine(); // Consume the invalid input
            }
        }

        System.out.printf("\nResult: You have got %d out of 10.\n", score);

        if (score >= 8) {
            System.out.println("Excellent! Well done!");
        } else if (score >= 5) {
            System.out.println("Good. Keep it up!");
        } else if (score >= 2) {
            System.out.println("Very poor! You need to improve.");
        } else {
            System.out.println("Very sorry, you have failed. Better luck next time.");
        }

        System.out.print("Would you like to start again? Press 's' for start or 'q' for quit\nStudent:> ");
        String playAgain = scanner.nextLine().toLowerCase();
        if (playAgain.equals("s")) {
            startQuiz();
        } else {
            System.out.println("Exiting.");
        }
    }

    private static List<Question> readQuizData() {
        List<Question> quizData = new ArrayList<>();

        try (FileReader fileReader = new FileReader("./src/main/resources/quiz.json")) {
            Scanner scanner = new Scanner(fileReader);
            StringBuilder jsonString = new StringBuilder();
            while (scanner.hasNextLine()) {
                jsonString.append(scanner.nextLine());
            }
            scanner.close();

            String[] questions = jsonString.toString().split("\\},\\{");
            for (String question : questions) {
                if (question.startsWith("[{")) {
                    question = question.substring(2);
                }
                if (question.endsWith("}]")) {
                    question = question.substring(0, question.length() - 2);
                }
                quizData.add(Question.fromJsonString("{" + question + "}"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return quizData;
    }
}

class Question {
    private String question;
    private String[] options;
    private int answerKey;

    public Question(String question, String[] options, int answerKey) {
        this.question = question;
        this.options = options;
        this.answerKey = answerKey;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public int getAnswerKey() {
        return answerKey;
    }

    public String toJsonString() {
        return String.format("{\"question\":\"%s\",\"option 1\":\"%s\",\"option 2\":\"%s\",\"option 3\":\"%s\",\"option 4\":\"%s\",\"answerkey\":%d}",
                question, options[0], options[1], options[2], options[3], answerKey);
    }

    public static Question fromJsonString(String jsonString) {
        jsonString = jsonString.replace("{", "").replace("}", "");
        String[] parts = jsonString.split(",");
        String question = parts[0].split(":")[1].replace("\"", "").trim();
        String option1 = parts[1].split(":")[1].replace("\"", "").trim();
        String option2 = parts[2].split(":")[1].replace("\"", "").trim();
        String option3 = parts[3].split(":")[1].replace("\"", "").trim();
        String option4 = parts[4].split(":")[1].replace("\"", "").trim();
        int answerKey = Integer.parseInt(parts[5].split(":")[1].trim());

        return new Question(question, new String[]{option1, option2, option3, option4}, answerKey);
    }
}
