package providers;

import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name = "THIS SHOULD NEVER WORK.";

	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
	}

	public MyUsernamePasswordAuthUser(String password, String email) {
		super(password, email);
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
	}

	@Override
	public String getName() {
		return name;
	}
}