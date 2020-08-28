package org.acme;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class ExampleResource {

	@Inject
	VersionSpecificState state;
	
	@Inject
	ExampleService service;
	
	@Path("v1")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String version1() {
		
		return CallScopeContext.get().with(() -> {
			state.setMessage("Hello from version 1");
			
			String message = service.greet();
			System.out.println(message);
			return message;
		});
	}
	
	@Path("v2")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String version2() {
		
		return CallScopeContext.get().with(() -> {
			state.setMessage("Hello from version 2");
			
			String message = service.greet();
			System.out.println(message);
			return message;
		});
	}
}