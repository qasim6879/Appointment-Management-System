package org.example;

public class Administrator extends User {
private String adminID;
public Administrator(String adminId,String email,String password) {
	super(email,password);
	this.adminID=adminId;
	
}
public String getAdminID() {
	return adminID;
	
}
public void setAdminID(String adminID) {
	this.adminID = adminID;
}
}