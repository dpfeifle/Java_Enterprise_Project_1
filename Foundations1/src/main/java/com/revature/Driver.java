package com.revature;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.revature.Employee;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Driver {
	
	public static Connection createConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(
					System.getenv("DB_URL_Project1"),
					System.getenv("DB_username"),
					System.getenv("DB_password"));
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return conn;
	}
	
	
	public static void main(String[] args) {
		
		boolean[] madeEmployees = {false};
		
		ArrayList<User> employees = new ArrayList<>();
		
		Javalin app = Javalin.create().start();
		
		app.before(ctx ->{
			Connection conn = null;
			Statement stmt = null;
			ResultSet set = null;
			
			try {
				
				if(!madeEmployees[0]){
					
				ctx.cookie("currentUser", "none,none");	
				conn = createConnection();
				stmt = conn.createStatement();
				
				set = stmt.executeQuery("SELECT * FROM EMPLOYEE");
				
				Employee nextEmployee = null;
				
				while(set.next()) {
					nextEmployee = new Employee(
							set.getInt(1),
							set.getString(2),
							set.getString(4)
							);
					employees.add(nextEmployee);
				}
				madeEmployees[0] = true;
				
				
				}

			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		});
		
		app.post("/newUser", ctx ->{
			
			Login userInfo = ctx.bodyAsClass(Login.class);
			
			Connection conn = null;
			Statement stmt = null;
			ResultSet set = null;
			
			try {
				conn = createConnection();
				stmt = conn.createStatement();
				
				set = stmt.executeQuery("SELECT USERNAME FROM Employee WHERE username = '" + userInfo.getUsername()+ "'");
				
				if(set.next()) {
					ctx.status(HttpStatus.CONFLICT);
					ctx.html("<h1>Username " + userInfo.getUsername() + " already exists</h1>");
				}
				
				else {
					stmt.execute("INSERT INTO EMPLOYEE (username, passhash) Values "
							+ "('" + userInfo.getUsername() + "', '" + 
							userInfo.getPassword().hashCode() + "')");
					
					ctx.status(HttpStatus.ACCEPTED);
					ctx.html("<h1>New User " + userInfo.getUsername() + " successfully created!</h1>");
					
					
					set = stmt.executeQuery("SELECT * FROM EMPLOYEE WHERE username = '" + userInfo.getUsername() + "'");
					set.next();
					Employee newEmployee = new Employee(
							set.getInt(1),
							set.getString(2),
							set.getString(4)
							);
					employees.add(newEmployee);
				}
				
			}
			finally {
				conn.close();
				stmt.close();
				
			}
			
		});

		
		app.post("/login", ctx -> {
			Login userInfo = ctx.bodyAsClass(Login.class);
			
			Connection conn = null;
			Statement stmt = null;
			ResultSet set = null;
			
			try {
				conn = createConnection();
				stmt = conn.createStatement();
				
				set = stmt.executeQuery("SELECT username, passhash, permissionLevel FROM employee "
						+ "WHERE username = '" + userInfo.getUsername() + "'");
				
				
				if(set.next()) {
					
					Login matchingUser = new Login(set.getString(1), set.getString(2));
					
					if(Integer.parseInt(matchingUser.getPassword()) == userInfo.getPassword().hashCode()) {
						ctx.cookie("currentUser", (set.getString(1) + "," + set.getString(3)));
						ctx.html("<h1>Welcome back " + set.getString(1) + "!</h1>");
					}
					else {
						ctx.html("<h1>Invalid username/password combo</h1>");
					}
				}
				else {
					ctx.html("<h1>Invalid username/password combo</h1>");
				}
					
				}
			
			
			finally {
				conn.close();
				set.close();
				stmt.close();
			}
			
		});
		
		app.get("/users", ctx ->{
			if(!(ctx.cookie("currentUser").split(",")[1].equals("none"))) {
				ctx.json(employees);
				ctx.status(HttpStatus.ACCEPTED);
			}
			
			else {
				ctx.status(HttpStatus.LOCKED);
				ctx.html("<p>You need to be logged in to view this information</p>");
			}
		});
		
		app.post("/newTicket", ctx -> {
			
			if(!(ctx.cookie("currentUser").split(",")[0].equals("none"))) {
					Connection conn = createConnection();
					ResultSet set = null;
					PreparedStatement sql = null;
					
				try {
					
					
					Ticket newTicket = ctx.bodyAsClass(Ticket.class);

					sql = conn.prepareStatement("INSERT INTO REQUEST (username, description, amount)"
							+ "VALUES (?, ?, ?)");
					
					sql.setString(1, newTicket.getUsername());
					sql.setString(2, newTicket.getDescription());
					sql.setFloat(3, newTicket.getAmount());
					
					System.out.println(newTicket.toString()); 
					
					System.out.println(sql.executeUpdate());
					
					ctx.status(HttpStatus.CREATED);
					ctx.html("<h1>Successfully submitted new ticket!</h1>");
				}
				
				catch(SQLException e) {
					e.printStackTrace();
				}
				
				finally {
					
					try {
						
						conn.close();
						if(sql != null) {
							sql.close();
							
						}
						if(set != null) {
							set.close();
						}
					}
					catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				ctx.status(HttpStatus.LOCKED);
				ctx.html("<h1>You must be logged in to access this service</h1>");
			}
			
		});

	

	
	}
}

