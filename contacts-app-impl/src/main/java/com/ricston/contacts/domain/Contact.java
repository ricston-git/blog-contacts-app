package com.ricston.contacts.domain;

public class Contact {
	
	private Long id;
	
	private String name;

	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Contact() {};

	public Contact(String name, String email) {
		this.name = name;
		this.email = email;
	};

}
