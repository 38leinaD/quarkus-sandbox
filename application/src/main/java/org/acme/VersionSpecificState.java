package org.acme;

@CallScoped
public class VersionSpecificState {
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
