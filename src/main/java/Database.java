import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Database {
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:~/sampledb", "mylogin", "mypassword")) {

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("Select * from Question")
            ) {

                while (resultSet.next()){
                    System.out.println(
                            Arrays.asList(
                                    resultSet.getInt(1),
                                    resultSet.getInt(2),
                                    resultSet.getString(3),
                                    Arrays.stream((Object[]) resultSet.getObject(4)).map(e -> e.toString()).collect(Collectors.joining(",","[","]")),
                                    resultSet.getInt(5)
                            ).stream().map(Object::toString).collect(Collectors.joining(" ")));
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
