package it.unisannio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity implementation class for Entity: User
 *
 */
@Entity
@XmlRootElement
public class Customer implements Serializable {

	@Id
	private String CF;
	private String firstName;
	private String lastName;
	// Not used with relational models
	@OneToMany(mappedBy="customer", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	@JsonIgnore
	private List<Account> accounts;
	private static final long serialVersionUID = 1L;

	public Customer() {
		super();
	}
	public Customer(String cf, String fn, String ln) {
		lastName = ln;
		firstName = fn;
		CF = cf;
	}
	public String getCF() {
		return this.CF;
	}

	public void setCF(String CF) {
		this.CF = CF;
	}
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<Account> getAccounts() {
		accounts.size();
		return this.accounts;
	}

	public void setAccount(List<Account> accounts) {
		this.accounts = accounts;
	}
}
