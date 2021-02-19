package br.edu.eduardo.treinamento.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="user")	
@XmlAccessorType(XmlAccessType.FIELD)
public class User {
	private String name;
	private int age;
	
	@XmlAttribute
	private Long id;

	public User(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}
	
	public User() {
	}	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
