package org.acme;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class ExampleResource {

	//@Inject
	//AgroalDataSource ds;
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String info() {
		String message = "hello @" + System.currentTimeMillis();
		System.out.println("[INFO] " + message);
		return message;
	}
}