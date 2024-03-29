package DAO;

import JDBCHelper.DatabaseConnection;
import Model.Question;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    public static List<Question> selectAll() {
        var list = new ArrayList<Question>();
        var query = "select * from questions";
        try (var statement = DatabaseConnection.getConnectionInstance().createStatement()) {
            var resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                list.add(
                        new Question(
                                resultSet.getLong("question_id"),
                                resultSet.getLong("exam_id"),
                                resultSet.getInt("level"),
                                resultSet.getString("content")
                        )
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public static Question selectByID(long questionID) {
        var question = new Question();
        var query = "select * from questions where question_id=?";
        try (var ps = DatabaseConnection.getConnectionInstance().prepareStatement(query)) {
            ps.setLong(1, questionID);
            var resultSet = ps.executeQuery();
            if (resultSet.next()) {
                question.setQuestion_id(resultSet.getLong("question_id"));
                question.setExam_id(resultSet.getLong("exam_id"));
                question.setLevel(resultSet.getInt("level"));
                question.setContent(resultSet.getString("content"));
                return question;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static boolean insert(Question question) {
        var query = "insert into questions(exam_id,level,content) values(?,?,?)";
        try (var ps = DatabaseConnection.getConnectionInstance().prepareStatement(query)) {
            ps.setLong(1, question.getExam_id());
            ps.setInt(2, question.getLevel());
            ps.setString(3, question.getContent());
            var count = ps.executeUpdate();
            return count != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean update(Question question) {
        var query = "update questions set exam_id = ?, level = ?, content = ?  where question_id = ?";
        try (var ps = DatabaseConnection.getConnectionInstance().prepareStatement(query)) {
            ps.setLong(1, question.getExam_id());
            ps.setInt(2, question.getLevel());
            ps.setString(3, question.getContent());
            ps.setLong(4, question.getQuestion_id());
            var count = ps.executeUpdate();
            return count != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean delete(long question_id) {
        var query = "delete from questions where question_id = ?";
        try (var ps = DatabaseConnection.getConnectionInstance().prepareStatement(query)) {
            ps.setLong(1, question_id);
            var count = ps.executeUpdate();
            return count != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        List<Question> questions = QuestionDAO.selectAll();
        System.out.println(questions.get(0).getExam_id());
        Question question = QuestionDAO.selectByID(1);
        System.out.println(
                (question != null ? question.getLevel() : -1)
                        + " "
                        + (question != null ? question.getContent() : "empty")
        );
//        Question question1 = new Question(6,2,"What's is this?");
//        System.out.println("Insert: " + QuestionDAO.insert(question1));
//        question.setLevel(3);
//        System.out.println("Update: " + QuestionDAO.update(question));
//        System.out.println("delete: " + QuestionDAO.delete(2));
    }


    public static Question mapFromString(String questionString) {
        // Split the questionString into different fields
        String[] fields = questionString.split(",");

        // Extract the values from the fields
        long questionId = Long.parseLong(getValueFromField(fields[0]));
        long examId = Long.parseLong(getValueFromField(fields[1]));
        int level = Integer.parseInt(getValueFromField(fields[2]));
        String content = getValueFromField(fields[3]);

        // Create and return a new Question object
        return new Question(questionId, examId, level, content);
    }

    private static String getValueFromField(String field) {
        // Remove the field name and any leading/trailing white spaces
        String value = field.substring(field.indexOf('=') + 1).trim();

        if (value.endsWith("}")) {
            value = value.substring(0, value.length()-1);
        }
        // Remove single quotes ('') if present
        if (value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }
}
