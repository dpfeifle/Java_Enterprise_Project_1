package com.revature;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.revature.Employee;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.HttpStatus;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.json.JSONObject;

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
		
		boolean[] setup = {false};
		
		Javalin app = Javalin.create().start();
		
		app.before(ctx ->{
			if(setup[0] == false) {
				ctx.cookie("currentUser", "none,none");	
				setup[0] = true;
			}
		});
		
		app.post("user/new", ctx ->{
			
			Login userInfo = ctx.bodyAsClass(Login.class);
			
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet set = null;
			
			try {
				conn = createConnection();
				stmt = conn.prepareStatement("SELECT USERNAME FROM Employee WHERE username = ?");
				
				stmt.setString(1, userInfo.getUsername());
				
				set = stmt.executeQuery();
				
				if(set.next()) {
					ctx.status(HttpStatus.CONFLICT);
					ctx.html("<h1>Username " + userInfo.getUsername() + " already exists</h1>");
				}
				
				else {
					stmt.close();
					
					stmt = conn.prepareStatement("INSERT INTO EMPLOYEE (username, passhash) Values (?,?)");
					stmt.setString(1, userInfo.getUsername());
					stmt.setString(2, userInfo.getPassword());
					
					stmt.execute();
					
					ctx.status(HttpStatus.ACCEPTED);
					ctx.html("<p>New User " + userInfo.getUsername() + " successfully created!</p>");
				}
				
			}
			finally {
				conn.close();
				stmt.close();
				set.close();
				
			}
			
		});

		
		app.post("user/login", ctx -> {
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
					System.out.println(set.getString(1) + "," + set.getString(3));
					
					if(Integer.parseInt(matchingUser.getPassword()) == userInfo.getPassword().hashCode()) {
						ctx.cookie("currentUser", (set.getString(1).replace(' ', '_') + "," + set.getString(3)));
						ctx.html("<p>Welcome back " + set.getString(1) + "!</p>");
					}
					else {
						ctx.html("<p>Invalid username/password combo</p>");
					}
				}
				else {
					ctx.html("<p>Invalid username/password combo</p>");
				}
					
				}
			
			
			finally {
				conn.close();
				set.close();
				stmt.close();
			}
			
		});
		
		app.get("user/view/all", ctx ->{
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet set = null;
			
			if(!(ctx.cookie("currentUser").split(",")[1].equals("none"))) {
				try {
					
					conn = createConnection();
					stmt = conn.prepareStatement("SELECT * FROM EMPLOYEE ORDER BY userid");
					set = stmt.executeQuery();
					
					String HTML = "";
					while(set.next()) {
						HTML += "ID : " + set.getInt(1) + "<br>"
						+ "username : " + set.getString(2) + "<br>"
						+ "privelege : " + set.getString(4) + "<br><br>";
					}
					
					ctx.html(HTML);
				}
				catch(SQLException e) {
					e.printStackTrace();
				}
				finally {
					if(conn != null) {
						conn.close();
						stmt.close();
						set.close();
					}
				}
				
			}
			
			else {
				ctx.status(HttpStatus.LOCKED);
				ctx.html("<p>You need to be logged in to view this information</p>");
			}
		});
		
		app.post("ticket/new", ctx -> {
			
			if(!(ctx.cookie("currentUser").split(",")[0].equals("none"))) {
					Connection conn = createConnection();
					ResultSet set = null;
					PreparedStatement sql = null;
					
				try {
					String JsonString = ctx.body();
					JSONObject json = new JSONObject(JsonString);
					
					if(!json.has("description") || !json.has("amount")) {
						ctx.html("description of request and requested reimbursment amount must be listed");
					}

					else {

						sql = conn.prepareStatement("INSERT INTO REQUEST (username, description, amount)"
								+ "VALUES (?, ?, ?)");

						sql.setString(1, ctx.cookie("currentUser").split(",")[0]);
						sql.setString(2, json.getString("description"));
						sql.setFloat(3, json.getFloat("amount")); 

						sql.executeUpdate();

						ctx.status(HttpStatus.CREATED);
						ctx.html("<p>Successfully submitted new ticket!</p>");
					}
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
		
		app.get("ticket/view/{type}", ctx ->{
			Connection conn = null;
			PreparedStatement sql = null;
			ResultSet set = null;

			try {

				if(ctx.pathParam("type").equals("next")) {
					if(ctx.cookie("currentUser").equals("Manager")) {
						conn = createConnection();
						sql = conn.prepareStatement("SELECT requestID, username , description, amount "
								+ "FROM request where requestID = "
								+ "(select min(requestID) from Request WHERE Status = 'Pending')");
	
						set = sql.executeQuery();
						if(set.next()){
							Ticket nextTicket = new Ticket(set.getString(2), set.getString(3), set.getFloat(4));
	
							ctx.html("<p>" + set.getString(1) + "<br>"
									+ "username : " + nextTicket.getUsername() + "<br>"
									+ "description : " + nextTicket.getDescription() + "<br>"
									+ "Amount : " + nextTicket.getAmount() + "<br>" 
									+ "Status : " + nextTicket.getStatus() + "</p>");
						}
	
						else{
							ctx.html("<p>There are no pending tickets</p>");
						}
					}
					else {
						ctx.html("<p>You must be a manager to access this service</p>");
					}
				}

				//ALL---------------------------------------------------------
				else if(ctx.pathParam("type").equals("all")) {

					if(ctx.cookie("currentUser").split(",")[1].equals("Manager")) {
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request");
						set = sql.executeQuery();
						String html = "<p>";
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						html += "</p>";
						ctx.html(html);
					}
					
					else if(ctx.cookie("currentUser").split(",")[1].equals("Employee")) {
						
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request WHERE username = ?");
						sql.setString(1, ctx.cookie("currentUser").split(",")[0]);
						
						set = sql.executeQuery();
						String html = "<p>";
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						html += "</p>";
						ctx.html(html);
					}
					else {
						ctx.status(HttpStatus.LOCKED);
						ctx.html("<p>You must be logged in to access this service</p>");
					}

				}
				//PENDING--------------------------------------------------------
				else if (ctx.pathParam("type").equals("pending")) {

					if(ctx.cookie("currentUser").split(",")[1].equals("Manager")) {
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request WHERE status = 'Pending'");
						set = sql.executeQuery();
						String html = "<p>";
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						html += "</p>";
						
						if(html.equals("<p></p>")) {
							html = "<p> There are no tickets that match your request</p>";
						}
						
						ctx.html(html);
					}

					else if(ctx.cookie("currentUser").split(",")[1].equals("Employee")) {
						
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request WHERE username = ? AND status = 'Pending'");
						sql.setString(1, ctx.cookie("currentUser").split(",")[0]);
						
						set = sql.executeQuery();
						String html = "<p>";
						
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						
						html += "</p>";
						
						if(html.equals("<p></p>")) {
							html = "<p> There are no tickets that match your request</p>";
						}
						
						ctx.html(html);
					}
					
					else {
						ctx.status(HttpStatus.LOCKED);
						ctx.html("<p>You must be logged in to access this service</p>");
					}
				}
				
				//APPROVED-------------------------------------------------------
				else if (ctx.pathParam("type").equals("approved")) {

					if(ctx.cookie("currentUser").split(",")[1].equals("Manager")) {
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request WHERE status = 'Approved'");
						set = sql.executeQuery();
						String html = "<p>";
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						html += "</p>";
						
						if(html.equals("<p></p>")) {
							html = "<p> There are no tickets that match your request</p>";
						}
						
						ctx.html(html);
					}

					else if(ctx.cookie("currentUser").split(",")[1].equals("Employee")) {
						
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request WHERE username = ? AND status = 'Approved'");
						sql.setString(1, ctx.cookie("currentUser").split(",")[0]);
						
						set = sql.executeQuery();
						String html = "<p>";
						
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						
						html += "</p>";
						
						if(html.equals("<p></p>")) {
							html = "<p> There are no tickets that match your request</p>";
						}
						
						ctx.html(html);
					}
					
					else {
						ctx.status(HttpStatus.LOCKED);
						ctx.html("<p>You must be logged in to access this service</p>");
					}
				}
				//DENIED---------------------------------------------------
				else if (ctx.pathParam("type").equals("denied")) {

					if(ctx.cookie("currentUser").split(",")[1].equals("Manager")) {
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request WHERE status = 'Denied'");
						set = sql.executeQuery();
						String html = "<p>";
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						html += "</p>";
						
						if(html.equals("<p></p>")) {
							html = "<p> There are no tickets that match your request</p>";
						}
						
						ctx.html(html);
					}

					else if(ctx.cookie("currentUser").split(",")[1].equals("Employee")) {
						
						conn = createConnection();
						sql = conn.prepareStatement("select * FROM request WHERE username = ? AND status = 'Denied'");
						sql.setString(1, ctx.cookie("currentUser").split(",")[0]);
						
						set = sql.executeQuery();
						String html = "<p>";
						
						while(set.next()){
							html += set.getString(1) + "<br>"
									+ "username : " + set.getString(2) + "<br>"
									+ "description : " + set.getString(3) + "<br>"
									+ "Amount : " + set.getFloat(4) + "<br>" 
									+ "Status : " + set.getString(5) + "<br><br>";
						}
						
						html += "</p>";
						
						if(html.equals("<p></p>")) {
							html = "<p> There are no tickets that match your request</p>";
						}
						
						ctx.html(html);
					}
					
					else {
						ctx.status(HttpStatus.LOCKED);
						ctx.html("<p>You must be logged in to access this service</p>");
					}
				}
				
				
				
			


			}
			catch(SQLException e){
				e.printStackTrace();
			}
			finally {
				if(conn != null) {
					conn.close();
				}
				if(set != null) {
					set.close();
				}
				if(sql != null) {
					sql.close();
				}
			}
		});
		
		
		app.get("ticket/approve/{requestID}", ctx -> {
			
			if(ctx.cookie("currentUser").split(",")[1].equals("Manager")) {
				
				Connection conn = null;
				PreparedStatement sql = null;
				ResultSet set = null;
				
				try {
					
			
				conn = createConnection();
				sql = conn.prepareStatement("select status from request where requestID = ?");
				sql.setInt(1, Integer.parseInt(ctx.pathParam("requestID")));
				set = sql.executeQuery();
				
				if(!set.next()) {
					ctx.status(HttpStatus.BAD_REQUEST);
					ctx.html("<p>There is no request with that ID</p>");
					sql.close();
				}
				
				else if(!(set.getString(1).equals("Pending"))) {
					ctx.status(HttpStatus.BAD_REQUEST);
					ctx.html("<p>This request has already been processed</p>");
					sql.close();
							
				}
				
				else {
					
					sql.close();
					
					sql = conn.prepareStatement("update request set status = 'Approved' where requestID = ?");
					sql.setInt(1, Integer.parseInt(ctx.pathParam("requestID")));
					sql.execute();
					
					ctx.status(HttpStatus.ACCEPTED);
					ctx.html("<p> Request # " + ctx.pathParam("requestID") + " successfully marked as 'Approved'");
					}
				}
				
				catch(SQLException e) {
					e.printStackTrace();
				}
				finally {
					if(conn != null) {
						conn.close();
						sql.close();
						set.close();
					}
				}
			}
			
			else {
				ctx.status(HttpStatus.LOCKED);
				ctx.html("<p>Only managers may approve/deny requests");
			}
	});
		
		app.get("ticket/deny/{requestID}", ctx -> {
			
			if(ctx.cookie("currentUser").split(",")[1].equals("Manager")) {
				
				Connection conn = null;
				PreparedStatement sql = null;
				ResultSet set = null;
				
				try {
					
			
				conn = createConnection();
				sql = conn.prepareStatement("select status from request where requestID = ?");
				sql.setInt(1, Integer.parseInt(ctx.pathParam("requestID")));
				set = sql.executeQuery();
				
				if(!set.next()) {
					ctx.status(HttpStatus.BAD_REQUEST);
					ctx.html("<p>There is no request with that ID</p>");
					sql.close();
				}
				
				else if(!(set.getString(1).equals("Pending"))) {
					ctx.status(HttpStatus.BAD_REQUEST);
					ctx.html("<p>This request has already been processed</p>");
					sql.close();
							
				}
				
				else {
					
					sql.close();
					
					sql = conn.prepareStatement("update request set status = 'Denied' where requestID = ?");
					sql.setInt(1, Integer.parseInt(ctx.pathParam("requestID")));
					sql.execute();
					
					ctx.status(HttpStatus.ACCEPTED);
					ctx.html("<p> Request # " + ctx.pathParam("requestID") + " successfully marked as 'Denied'");
					}
				}
				
				catch(SQLException e) {
					e.printStackTrace();
				}
				finally {
					if(conn != null) {
						conn.close();
						sql.close();
						set.close();
					}
				}
			}
			
			else {
				ctx.status(HttpStatus.LOCKED);
				ctx.html("<p>Only managers may approve/deny requests");
			}
	});
}
}

