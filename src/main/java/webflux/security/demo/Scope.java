package webflux.security.demo;

public interface Scope {

	public static final String MONITOR = "demo:monitor";
	public static final String REGISTER = "demo:register";
	
	public static final String SECURITY_SCOPE_MONITOR = "SCOPE_demo:monitor";
	public static final String SECURITY_SCOPE_REGISTER = "SCOPE_demo:register";
}
