import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class GetData {

    static String prefix = "project3.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding 
    // tables in your database
    String userTableName = null;
    String friendsTableName = null;
    String cityTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;

    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
        super();
        String dataType = u;
        oracleConnection = c;
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        cityTableName = prefix + dataType + "_CITIES";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITIES";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITIES";
    }

    // TODO: Implement this function
    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException {

        // This is the data structure to store all users' information
        JSONArray users_info = new JSONArray();
        
        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = stmt.executeQuery(
                "SELECT user_id, first_name, last_name, gender, year_of_birth AS YOB, " +
                "month_of_birth AS MOB, day_of_birth AS DOB FROM " + userTableName
            );
            
            while (rs.next()) {
                JSONObject user = new JSONObject();

                user.put("user_id", rs.getInt(1));
                user.put("first_name", rs.getString(2));
                user.put("last_name", rs.getString(3));
                user.put("gender", rs.getString(4));
                user.put("YOB", rs.getInt(5));
                user.put("MOB", rs.getInt(6));
                user.put("DOB", rs.getInt(7));
                
                PreparedStatement ps = oracleConnection.prepareStatement("SELECT user2_id AS friend FROM " + friendsTableName + 
                    " WHERE user1_id = " + rs.getInt(1) + 
                    " AND user2_id > user1_id" +
                    " UNION SELECT user1_id AS friend FROM " + friendsTableName +
                    " WHERE user2_id = " + rs.getInt(1) + 
                    " AND user2_id < user1_id"
                );

                ResultSet rs2 = ps.executeQuery();

                JSONArray friends = new JSONArray();

                while (rs2.next()){
                    friends.put(rs2.getInt(1));
                }

                rs2.close();
                ps.close();

                ps = oracleConnection.prepareStatement(
                    "SELECT city_name, state_name, country_name " +
                    "FROM " + cityTableName + " C, " + currentCityTableName + " N " +
                    "WHERE C.city_id = N.current_city_id AND N.user_id = " + rs.getInt(1)
                );

                ResultSet rs3 = ps.executeQuery();

                JSONObject current = new JSONObject();

                while (rs3.next()){
                    current.put("city", rs3.getString(1));
                    current.put("state", rs3.getString(2));
                    current.put("country", rs3.getString(3));
                }

                rs3.close();
                ps.close();

                ps = oracleConnection.prepareStatement(
                    "SELECT city_name, state_name, country_name " +
                    "FROM " + cityTableName + " C, " + hometownCityTableName + " N " +
                    "WHERE C.city_id = N.hometown_city_id AND N.user_id = " + rs.getInt(1)
                );

                ResultSet rs4 = ps.executeQuery();

                JSONObject hometown = new JSONObject();

                while (rs4.next()){
                    hometown.put("city", rs4.getString(1));
                    hometown.put("state", rs4.getString(2));
                    hometown.put("country", rs4.getString(3));
                }

                rs4.close();
                ps.close();

                user.put("friends", friends);
                user.put("hometown", hometown);
                user.put("current", current);
                users_info.put(user);
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return users_info;
    }

    // This outputs to a file "output.json"
    // DO NOT MODIFY this function
    public void writeJSON(JSONArray users_info) {
        try {
            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/output.json");
            file.write(users_info.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
