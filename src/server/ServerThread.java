package server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import DAO.*;
import Model.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServerThread implements Runnable {
    private Socket socketOfServer;
    private int clientNumber;
    private BufferedReader is;
    private BufferedWriter os;
    private boolean isClosed;

    private String response;

    public BufferedReader getIs() {
        return is;
    }

    public BufferedWriter getOs() {
        return os;
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public ServerThread(Socket socketOfServer, int clientNumber) {
        this.socketOfServer = socketOfServer;
        this.clientNumber = clientNumber;
        System.out.println("Server thread number " + clientNumber + " Started");
        isClosed = false;
    }

    public void write(String message) {
        try {
            os.write(message);
            os.newLine();
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            // Open input and output streams on the server socket
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            System.out.println("Thread " + clientNumber + " started successfully");
//            write("get-id" + "," + this.clientNumber);
//            Server.serverThreadBus.sendOnlineList();
//            Server.serverThreadBus.mutilCastSend("global-message" + "," + "---Client " + this.clientNumber + " has logged in---");

            String message;
            while (!isClosed) {
                message = is.readLine();
                if (message == null) {
                    break;
                }

//                String[] messageSplit = message.split(",");


                // Check the message type sent by the client
//                String messageType = messageSplit[0];
                String messageType = message;
                switch (messageType) {
                    case "GET /user":
                        handleUserRequest();
                        break;
                    case "GET /takeexam":
                        handleTakeExamRequest();
                        break;
                    case "GET /room":
                        handleRoomRequest();
                        break;
                    case "GET /question":
                        handleQuestionRequest();
                        break;
                    case "GET /qa":
                        handleQuestionAnswerRequest();
                        break;
                    case "GET /exam":
                        handleExamRequest();
                        break;
                    case "GET /enrollment":
                        handleEnrollmentRequest();
                        break;
                    case "GET /ea":
                        handleEnrollmentAnswerRequest();
                        break;
                    default:
                        // If the message type is not recognized, the server continues listening
                        break;
                }
            }
        } catch (IOException e) {
            isClosed = true;
            Server.serverThreadBus.remove(clientNumber);
            System.out.println("[Client " + this.clientNumber + "] - Exited");
//            Server.serverThreadBus.sendOnlineList();
//            Server.serverThreadBus.mutilCastSend("global-message" + "," + "---Client " + this.clientNumber + " has exited---");
        }
    }

    private void handleEnrollmentAnswerRequest() throws IOException {
        String response = "EnrollmentAnswer request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_ea_request = is.readLine();
        switch (type_of_ea_request) {
            case "selectAll":
                System.out.println("    [-] Select All");
                List<EnrollmentAnswer> list = EnrollmentAnswerDAO.selectAll();
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i = 0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "selectByID":
                System.out.println("    [-] Select by ID");
                String enrollID = is.readLine();
                EnrollmentAnswer enroll_tmp = EnrollmentAnswerDAO.selectByID(Long.parseLong(enrollID));
                if (enroll_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(enroll_tmp));
                } else {
                    write("INVALID");
                }
                break;

            case "insert":
                System.out.println("    [-] Insert EnrollmentAnswer");
                String eaStr = is.readLine();
                EnrollmentAnswer tmp = EnrollmentAnswerDAO.mapFromString(eaStr);
                Boolean status = EnrollmentAnswerDAO.insert(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "update":
                System.out.println("    [-] Update EnrollmentAnswer");
                eaStr = is.readLine();
                tmp = EnrollmentAnswerDAO.mapFromString(eaStr);
                status = EnrollmentAnswerDAO.update(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "delete":
                System.out.println("    [-] Delete EnrollmentAnswer");
                eaStr = is.readLine();
                status = EnrollmentAnswerDAO.delete(Long.parseLong(eaStr));
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;

            default:
                break;
        }
    }

    private void handleEnrollmentRequest() throws IOException {
        String response = "Enrollment request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_enrollment_request = is.readLine();
        switch (type_of_enrollment_request) {
            case "selectAll":
                System.out.println("    [-] Select All");
                List<Enrollment> list = EnrollmentDAO.selectAll();
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i = 0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "selectByID":
                System.out.println("    [-] Select by ID");
                String enrollID = is.readLine();
                Enrollment enroll_tmp = EnrollmentDAO.selectByID(Long.parseLong(enrollID));
                if (enroll_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(enroll_tmp));
                } else {
                    write("INVALID");
                }
                break;
            case "selectIDByModel":
                System.out.println("    [-] Select ID by Model");
                String enrollStr = is.readLine();
                enroll_tmp = EnrollmentDAO.mapFromString(enrollStr);
                long stat = EnrollmentDAO.selectIDByModel(enroll_tmp);
                if (stat != -1) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(stat));
                } else {
                    write("INVALID");
                }
                break;
            case "selectByUserID":
                System.out.println("    [-] Select by userID");
                String userID = is.readLine();
                List<Enrollment> list_enrollment = EnrollmentDAO.selectByUserID(userID);
                if (list_enrollment != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list_enrollment.size()));
                    for (int i = 0; i < list_enrollment.size(); i++) {
                        write(String.valueOf(list_enrollment.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "insert":
                System.out.println("    [-] Insert Enrollment");
                enrollStr = is.readLine();
                Enrollment tmp = EnrollmentDAO.mapFromString(enrollStr);
                Boolean status = EnrollmentDAO.insert(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "update":
                System.out.println("    [-] Update Enrollment");
                enrollStr = is.readLine();
                tmp = EnrollmentDAO.mapFromString(enrollStr);
                status = EnrollmentDAO.update(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "delete":
                System.out.println("    [-] Delete Enrollment");
                enrollStr = is.readLine();
                status = EnrollmentDAO.delete(Long.parseLong(enrollStr));
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;

            default:
                break;
        }
    }

    private void handleExamRequest() throws IOException {
        String response = "Exam request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_exam_request = is.readLine();
        switch (type_of_exam_request) {
            case "selectAll":
                System.out.println("    [-] Select All");
                List<Exam> list = ExamDAO.selectAll();
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i = 0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "selectByID":
                System.out.println("    [-] Select by ID");
                String examID = is.readLine();
                Exam exam_tmp = ExamDAO.selectByID(Long.parseLong(examID));
                if (exam_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(exam_tmp));
                } else {
                    write("INVALID");
                }
                break;
            case "insert":
                System.out.println("    [-] Insert Exam");
                String examStr = is.readLine();
                Exam tmp = ExamDAO.mapFromString(examStr);
                Boolean status = ExamDAO.insert(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "update":
                System.out.println("    [-] Update Exam");
                examStr = is.readLine();
                tmp = ExamDAO.mapFromString(examStr);
                status = ExamDAO.update(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "delete":
                System.out.println("    [-] Delete Exam");
                examStr = is.readLine();
                status = ExamDAO.delete(Long.parseLong(examStr));
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;

            default:
                break;
        }
    }

    private void handleQuestionAnswerRequest() throws IOException {
        String response = "QuestionAnswer request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_quest_answer_request = is.readLine();
        switch (type_of_quest_answer_request) {
            case "selectAll":
                System.out.println("    [-] Select All");
                List<QuestionAnswer> list = QuestionAnswerDAO.selectAll();
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i = 0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "selectByID":
                System.out.println("    [-] Select by ID");
                String questAnswerID = is.readLine();
                QuestionAnswer quest_tmp = QuestionAnswerDAO.selectByID(Long.parseLong(questAnswerID));
                if (quest_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(quest_tmp));
                } else {
                    write("INVALID");
                }
                break;
            case "insert":
                System.out.println("    [-] Insert QuestionAnswer");
                String questStr = is.readLine();
                QuestionAnswer tmp = QuestionAnswerDAO.mapFromString(questStr);
                Boolean status = QuestionAnswerDAO.insert(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "update":
                System.out.println("    [-] Update QuestionAnswer");
                questStr = is.readLine();
                tmp = QuestionAnswerDAO.mapFromString(questStr);
                status = QuestionAnswerDAO.update(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "delete":
                System.out.println("    [-] Delete QuestionAnswer");
                questStr = is.readLine();
                status = QuestionAnswerDAO.delete(Long.parseLong(questStr));
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;

            default:
                break;
        }
    }

    private void handleQuestionRequest() throws IOException {
        String response = "Question request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_quest_request = is.readLine();
        switch (type_of_quest_request) {
            case "selectAll":
                System.out.println("    [-] Select All");
                List<Question> list = QuestionDAO.selectAll();
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i = 0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "selectByID":
                System.out.println("    [-] Select by ID");
                String questID = is.readLine();
                Question quest_tmp = QuestionDAO.selectByID(Long.parseLong(questID));
                if (quest_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(quest_tmp));
                } else {
                    write("INVALID");
                }
                break;
            case "insert":
                System.out.println("    [-] Insert question");
                String questStr = is.readLine();
                Question tmp = QuestionDAO.mapFromString(questStr);
                Boolean status = QuestionDAO.insert(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "update":
                System.out.println("    [-] Update question");
                questStr = is.readLine();
                tmp = QuestionDAO.mapFromString(questStr);
                status = QuestionDAO.update(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "delete":
                System.out.println("    [-] Delete question");
                questStr = is.readLine();
                status = QuestionDAO.delete(Long.parseLong(questStr));
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;

            default:
                break;
        }
    }

    private void handleRoomRequest() throws IOException {
        response = "Room request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_room_request = is.readLine();
        switch (type_of_room_request) {
            case "selectAll":
                System.out.println("    [-] Select All");
                List<Room> list = RoomDAO.selectAll();
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i = 0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "selectVerifiedRoom":
                System.out.println("    [-] Select verified room");
                String roomID = is.readLine();
                String password_encrypted = is.readLine();
                Room room_tmp = RoomDAO.selectVerifiedRoom(roomID, password_encrypted);
                if (room_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(room_tmp));
                } else {
                    write("INVALID");
                }
                break;
            case "selectByID":
                System.out.println("    [-] Select by ID");
                roomID = is.readLine();
                room_tmp = RoomDAO.selectByID(Long.parseLong(roomID));
                if (room_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(room_tmp));
                } else {
                    write("INVALID");
                }
                break;
            case "insert":
                System.out.println("    [-] Insert room");
                String roomStr = is.readLine();
                Room tmp = RoomDAO.mapFromString(roomStr);
                Boolean status = RoomDAO.insert(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "update":
                System.out.println("    [-] Update room");
                roomStr = is.readLine();
                tmp = RoomDAO.mapFromString(roomStr);
                status = RoomDAO.update(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "delete":
                System.out.println("    [-] Delete room");
                roomStr = is.readLine();
                status = RoomDAO.delete(Long.parseLong(roomStr));
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;

            default:
                break;
        }
    }

    private void handleTakeExamRequest() throws IOException {
        response = "Take exam request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_take_exam_request = is.readLine();
        switch (type_of_take_exam_request) {
            case "verifyUserAlreadyTakenExam":
                System.out.println("    [-] Verify user already taken exam");
                String userID = is.readLine();
                String roomID = is.readLine();
                Boolean status = TakeExamDAO.verifyUserAlreadyTakenExam(userID, Long.parseLong(roomID));
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "selectExamOfRoom":
                System.out.println("    [-] Select exam of room");
                String room_id = is.readLine();
                Exam exam_tmp = TakeExamDAO.selectExamOfRoom(Long.parseLong(room_id));
                if (exam_tmp != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(exam_tmp));
                } else {
                    write("INVALID");
                }
                break;
            case "selectQuestionOfExam":
                System.out.println("    [-] Select Questions of Exam");
                roomID = is.readLine().trim();
                List<Question> list = TakeExamDAO.selectQuestionOfExam(Long.parseLong(roomID));
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i=0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "selectQuestionAnswerOfQuestion":
                System.out.println("    [-] Select Question Answer of Question");
                roomID = is.readLine().trim();
                List<QuestionAnswer> list_ = TakeExamDAO.selectQuestionAnswerOfQuestion(Long.parseLong(roomID));
                if (list_ != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list_.size()));
                    for (int i=0; i < list_.size(); i++) {
                        write(String.valueOf(list_.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            default:
                break;
        }
    }


    private void handleUserRequest() throws IOException {
        // TODO: Implement the login logic here
        // This is just a placeholder
        String response = "User request";
        System.out.printf("[Client %d] - %s\n", this.clientNumber, response);

        String type_of_user_request = is.readLine();
        switch (type_of_user_request) {
            case "selectByAccount":
                System.out.println("    [-] Select by account");
                String username = is.readLine();
                String password_encrypted = is.readLine();
                User loginUser = UserDAO.selectByAccount(username, password_encrypted);
                if (loginUser != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(loginUser));
                } else {
                    write("INVALID");
                }
                break;
            case "selectByID":
                System.out.println("    [-] Select by ID");
                String userID = is.readLine();
                loginUser = UserDAO.selectByID(userID);
                if (loginUser != null) {
//                    System.out.println(String.valueOf(loginUser));
                    write(String.valueOf(loginUser));
                } else {
                    write("INVALID");
                }
                break;
            case "selectAll":
                System.out.println("    [-] Select All");
                List<User> list = UserDAO.selectAll();
                if (list != null) {
//                    System.out.println(String.valueOf(list));
//                    write(String.valueOf(list));
                    write(String.valueOf(list.size()));
                    for (int i=0; i < list.size(); i++) {
                        write(String.valueOf(list.get(i)));
                    }
                } else {
                    write("INVALID");
                }
                break;
            case "insert":
                System.out.println("    [-] Insert user");
                String userStr = is.readLine();
                User tmp = UserDAO.mapFromString(userStr);
                Boolean status = UserDAO.insert(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "update":
                System.out.println("    [-] Update user");
                userStr = is.readLine();
                tmp = UserDAO.mapFromString(userStr);
                status = UserDAO.update(tmp);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            case "delete":
                System.out.println("    [-] Delete user");
                userStr = is.readLine();
                status = UserDAO.delete(userStr);
                if (status) {
                    write("true");
                } else {
                    write("false");
                }
                break;
            default:
                break;
        }

    }


}