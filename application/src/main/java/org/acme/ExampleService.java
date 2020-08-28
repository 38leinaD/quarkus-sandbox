package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ExampleService {

	@Inject
	VersionSpecificState state;
	
	public String greet() {
		return state.getMessage();
	}
}
