package thaw.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.vertx.core.json.JsonArray;
import thaw.utils.Parser;

public class DataBase {
	
	private Connection con = null;
	private static DataBase instance;
	private final Object lock = new Object();
	
	/**
	 * create our databse unique instance
	 */
	private DataBase(){
		init();
	}
	
	/**
	 * get singleteon instance
	 */
	public static DataBase getInstance(){
		if(instance == null){
			instance = new DataBase();
		}
		return instance;
	}
	
	/**
	 * initialize databse
	 * create user table
	 */
	private void init(){
		final String query = "CREATE TABLE IF NOT EXISTS Users (_id INTEGER PRIMARY KEY, Username TEXT NOT NULL, Password TEXT NOT NULL)";
		setQueryUpdate(query);
	}
	
	/**
	 * create connection
	 * @return statement
	 */
	private Statement getConnection() throws SQLException{
		con = DriverManager.getConnection("jdbc:sqlite:db.db");
		Statement statement = con.createStatement();
		return statement;
	}
	
	/**
	 * execute / update queries
	 * @param query
	 */
	public void setQueryUpdate(String query) {
		synchronized(lock){
			try {
				Statement statement = getConnection();
				statement.setQueryTimeout(30);
				statement.executeUpdate(query);
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * interrogate database
	 * @param query
	 * @return database JsonArray result
	 */
	public JsonArray execQuery(String query) {
		synchronized(lock){
			try {
				Statement statement = getConnection();
				statement.setQueryTimeout(30);
				ResultSet rs = statement.executeQuery(query);
				JsonArray array = Parser.resultSetToJson(rs);
				statement.close();
				return array;
			} catch (SQLException e) {
				e.printStackTrace();
				return new JsonArray();
			}
		}
	}

	/**
	 * shutdown database connection
	 */
	public void close() throws SQLException{
		synchronized(lock){
			con.close();
		}
	}

}
