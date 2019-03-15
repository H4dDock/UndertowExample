import java.sql.*;
import java.util.Arrays;
import java.util.stream.Collectors;


public class QuestionsDAO {

    public int getQuestionsCount() throws SQLException {

        try (Connection connection = DriverManager.getConnection("jdbc:h2:~/sampledb", "mylogin", "mypassword")) {

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("Select count(*) from Question")
            ) {
                resultSet.next();
                return resultSet.getInt(1);

            }
        }
    }

    public Question getQuestion(int num) throws SQLException {

        try (Connection connection = DriverManager.getConnection("jdbc:h2:~/sampledb", "mylogin", "mypassword")) {

            try (PreparedStatement statement =
                         connection.prepareStatement("Select ID, num, text, answers, right from Question WHERE NUM = ?");

            ) {
                statement.setInt(1, num);

                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return new Question(
                            resultSet.getInt("num"),
                            resultSet.getString("text"),
                            Arrays.stream(((Object[]) resultSet.getObject("answers"))).map(s -> s.toString()).collect(Collectors.toList()),
                            resultSet.getInt("right")
                    );

                }

            }
        }
    }
}