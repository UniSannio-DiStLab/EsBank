package it.unisannio.service;



import java.util.List;

import javax.ejb.Stateless;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import it.unisannio.model.Account;
import it.unisannio.model.Customer;

/**
 * Session Bean implementation class Branch
 */
/**
 * Session Bean implementation class Branch
 */
@Stateless
public class Branch implements BranchLocal {

	@PersistenceContext
	EntityManager em;


	public Branch() {

	}

	public double totalAmount() {
		List<Account> l = em.createNamedQuery("Account.findAllPositiveAmounts", Account.class).getResultList();
		double total = 0;
		for (int i = 0; i < l.size(); i++)
			total+=l.get(i).getBalance();
		return total;
	}

	public int createAccount(String cf, double am) throws Exception {
		Customer cust = em.find(Customer.class, cf);
		if (cust != null) {
			Account a = new Account(cust, am);
			em.persist(a);
			return a.getNumber();
		}
		throw new Exception(); // A specific Exception is needed here
	}
	public Account getAccount(int num) {
		return em.find(Account.class, num);
	}

	public List<Account> getAccounts(String cf) {
		//return em.createNamedQuery("Account.findAllCustomerAccounts", Account.class).setParameter("CF", cf).getResultList();

		Customer c = getCustomer(cf);
		return c.getAccounts();
	}
	public void deposit(int num, double a) throws Exception {
		Account account = em.find(Account.class, num);
		if (account != null) account.deposit(a);
		else throw new Exception();
	}
	public void withdraw(int num, double a) throws Exception{
		Account account = em.find(Account.class, num);
		if (account != null) account.withdraw(a);
		else throw new Exception();
	}
	public void createCustomer(String cf, String fn, String ln) throws Exception {
		Customer c = new Customer(cf, fn, ln);
		em.persist(c);
	}

	public Customer getCustomer(String cf) {
		Customer c = em.find(Customer.class, cf);
		return c;
	}
}
