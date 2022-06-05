package it.unisannio.controller;


import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;



import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.logging.Logger;
import it.unisannio.model.Account;
import it.unisannio.model.Customer;
import it.unisannio.service.BranchLocal;


@Consumes("text/plain")
@Produces("text/plain")
@Path("/bank")
public class BankController  {
	private static final Logger LOGGER = Logger.getLogger(BankController.class);
	@EJB
	private BranchLocal branch;

	@Resource UserTransaction utx; // To handle user transactions from a Web component


	public BankController() {
		super();

	}


	@POST
	@Path("/accounts/{accountId}/deposits")
	public Response deposit(@PathParam("accountId") int accountNum, double amount) {
		LOGGER.info("BankController.deposit accountNum = " + accountNum + ", amount = " + amount);
		try {

			branch.deposit(accountNum, amount);

			return Response.ok().build();
		} catch (Exception e) {
			System.out.println(e);
			return Response.status(500).build();
		}
	}


	@POST
	@Path("/accounts/{accountId}/withdraws")
	public Response withdraw(@PathParam("accountId") int accountNum, double amount) {
		LOGGER.info("BankController.withdraw accountNum = " + accountNum + ", amount = " + amount);
		try {
			branch.withdraw(accountNum, amount);

			return Response.ok().build();
		} catch (Exception e) {
			System.out.println(e);
			return Response.status(500).build();
		}
	}

	@GET
	@Path("/accounts/{accountId}/balance")
	public Response getBalance(@PathParam("accountId") int accountNum) {
		LOGGER.info("BankController.getBalance accountNum = " + accountNum);
		Account a = branch.getAccount(accountNum);
		if (a == null) return Response.status(204).build();
		try {
			return Response.ok(a.getBalance()).lastModified(a.getLastModified()).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}

	@PUT
	@Path("/accounts/{accountId}/balance")
	public Response setBalance(@PathParam("accountId") int accountNum, double amount, @Context Request request) {
		LOGGER.info("BankController.setBalance accountNum = " + accountNum + ", amount = " + amount + ", request = " + request);
		Account a = branch.getAccount(accountNum);
		ResponseBuilder builder = null;
		try {
			//Workaround since we use Java8
			builder = request.evaluatePreconditions(Date.from(a.getLastModified().toInstant().truncatedTo(ChronoUnit.SECONDS)));
			if (builder == null) {
				utx.begin();
				branch.getAccount(accountNum).setBalance(amount);
				utx.commit();
				return Response.status(204).build();
			}
			return builder.build();


		} catch (Exception e) {
			return builder.status(500).build();
		}
	}

	@POST
	@Path("/accounts")
	public Response createAccount(@QueryParam("cf") String custCF, double amount) {
		LOGGER.info("BankController.createAccount custCF = " + custCF + ", amount = " + amount);
		try {
			return Response.created(new URI("/accounts/"+branch.createAccount(custCF, amount))).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}

	@POST
	@Path("/accounts/transfers")
	public Response transfer(@QueryParam("source") int srcAccount, @QueryParam("destination") int dstAccount, double amount) {
		LOGGER.info("BankController.transfer srcAccount = " + srcAccount + ", dstAccount = " + dstAccount + ", amount = " + amount);
		try {
			utx.begin();
			branch.getAccount(srcAccount).withdraw(amount);
			branch.getAccount(dstAccount).deposit(amount);
			utx.commit();

			return Response.ok().build();
		} catch (Exception e) {
			try { utx.rollback(); } catch (SystemException ee) {}
			return Response.status(500).build();
		}
	}

	@POST
	@Path("/customers/{custCF}/accounts")
	public Response createAccountOfCustomer(@PathParam("custCF") String custCF, double amount) {
		LOGGER.info("BankController.createAccountOfCustomer custCF = " + custCF + ", amount = " + amount);
		try {
			return Response.created(new URI("/customers/"+custCF+"/accounts/"+branch.createAccount(custCF, amount))).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@POST
	@Path("/customers")
	@Consumes("application/json")
	public Response createCustomer(Customer c) {
		LOGGER.info("BankController.createCustomer c = " + c);
		try {
			branch.createCustomer(c.getCF(), c.getFirstName(), c.getLastName());
			return Response.created(new URI("/customers/"+c.getCF())).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}

	@GET
	@Path("/customers/{custCF}")
	@Produces("application/json")
	public Response getCustomer(@PathParam("custCF") String cf) {
		LOGGER.info("BankController.getCustomer cf = " + cf);
		try {
			Customer c = branch.getCustomer(cf);
			if (c == null) return Response.status(404).build();
			c.setAccount(new ArrayList<>());
			return Response.ok(c).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}

}
