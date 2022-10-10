package com.revature;
import java.util.ArrayList;
import java.util.List;

import com.revature.Employee;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class Connection {
	public static void main(String[] args) {
		
		User[] currentUser = {null};
		
		ArrayList<User> employees = new ArrayList<>();
		Employee e1 = new Employee(1, "fred", "dippitydoo".hashCode(), "Employee");
		Employee e2 = new Employee(2, "Pamela", "helloworld".hashCode(), "Employee");
		Employee e3 = new Employee(3, "Marco", "FatBunnyCheeseButter".hashCode(), "Employee");
		Manager m1 = new Manager (4, "Manny Ger", "IAmBossMan".hashCode(), "Manager");
		
		employees.add(e1);
		employees.add(e2);
		employees.add(e3);
		employees.add(m1);
		
		
		
		
		Javalin app = Javalin.create().start();
		
		app.post("/newUser", ctx ->{
			boolean userExists = false;
			Login userInfo = ctx.bodyAsClass(Login.class);
			
			for(User e: employees) {
				if(e.getUsername().equals(userInfo.getUsername())) {
					userExists = true;
					break;
				}
			}
			
			if(!userExists) {
				Employee newUser = new Employee((employees.size() + 1), userInfo.getUsername(), 
						userInfo.getPassword().hashCode(), "Employee");
				employees.add(newUser);
				ctx.status(HttpStatus.ACCEPTED);
				ctx.html("<h1>New User " + newUser.getUsername() + " successfully created!</h1>");
			}
			else {
				ctx.status(HttpStatus.CONFLICT);
				ctx.html("<h1>Username " + userInfo.getUsername() + " already exists</h1>");
			}
		});

		
		app.post("/login", ctx -> {
			Login userInfo = ctx.bodyAsClass(Login.class);
			
			for(User e : employees) {
				
				if (userInfo.getUsername().equals(e.getUsername()) && 
						(userInfo.getPassword().hashCode() == e.getPasshash())){
					
					currentUser[0] = e;
					ctx.html("<h1>Welcome back " + currentUser[0].getUsername() + "!</h1>");
					break;
					
				}
				else {
					ctx.html("<h1>Invalid username/password combo</h1>");
				}
			}
			
		});
		
		app.get("/users", ctx ->{
			if(currentUser[0] != null) {
				ctx.json(employees);
				ctx.status(HttpStatus.ACCEPTED);
			}
			else {
				ctx.status(HttpStatus.LOCKED);
				ctx.html("<p>You need to be logged in to view this information</p>");
			}
		});

	

	
	}
}

