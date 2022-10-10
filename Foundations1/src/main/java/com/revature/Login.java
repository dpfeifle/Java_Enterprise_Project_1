package com.revature;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Login {
	
	
	private String username;
	private String password;
	
	public Login(@JsonProperty("username") String username, @JsonProperty("password") String password) {
		super();
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	
    
}
