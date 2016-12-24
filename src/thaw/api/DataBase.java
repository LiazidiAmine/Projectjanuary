package thaw.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DataBase {
	
	private Connection con = null;
	private static DataBase instance;
	
	private DataBase(){
		
	}
	
	public static DataBase getInstance(){
		if(instance == null){
			instance = new DataBase();
		}
		return instance;
	}
	
	
	public void setQueryUpdate(String query) {
		// create database connection
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:db.db")) {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JsonArray execQuery(String query) {
		// create database connection
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:db.db")) {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery(query);
			JsonArray jsonArray = new JsonArray();
			while (rs.next()) {
				int rows = rs.getMetaData().getColumnCount();
				JsonObject obj = new JsonObject();
				for (int i = 0; i < rows; i++) {
					obj.put(rs.getMetaData().getColumnLabel(i + 1).toLowerCase(), rs.getObject(i + 1));
				}
				jsonArray.add(obj);
			}
			statement.close();
			return jsonArray;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new JsonArray();
		}
	}
	
	public void close() throws SQLException{
		con.close();
	}

}
