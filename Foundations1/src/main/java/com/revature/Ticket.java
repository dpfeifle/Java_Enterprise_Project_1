package com.revature;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ticket {
	
	private String username;
	private String description;
	private float amount;
	private String Status;
	
	public Ticket(@JsonProperty("username")String username, @JsonProperty("description")String description, @JsonProperty("amount")float amount) {
		super();
		this.username = username;
		this.description = description;
		this.amount = amount;
		Status = "Pending";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	@Override
	public String toString() {
		return "Ticket [username=" + username + ", description=" + description + ", amount=" + amount + ", Status="
				+ Status + "]";
	}
	
	
	
	
}
