package com.revature;

import java.util.Objects;

public class User {
	private int userid;
	private String username;
	private int passhash;
	private String userPrivelege;
	
	public boolean login(String username, String password) {
		if(username == this.username && password.hashCode() == this.passhash) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public User(int userid, String username, int passhash, String userPrivelege) {
		super();
		this.userid = userid;
		this.username = username;
		this.passhash = passhash;
		this.userPrivelege = userPrivelege;
	}

	public User(int userid2, String username2, String userPrivelege2) {
		// TODO Auto-generated constructor stub
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getPasshash() {
		return passhash;
	}

	public void setPasshash(int passhash) {
		this.passhash = passhash;
	}
	
	public String getUserPrivelege() {
		return userPrivelege;
	}

	public void setUserPrivelege(String userPrivelege) {
		this.userPrivelege = userPrivelege;
	}

	@Override
	public int hashCode() {
		return Objects.hash(passhash, userPrivelege, userid, username);
	}

	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return passhash == other.passhash && Objects.equals(userPrivelege, other.userPrivelege)
				&& userid == other.userid && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "User [userid=" + userid + ", username=" + username + ", userPrivelege=" + userPrivelege + "]";
	}
	
	
	
	
	

}
