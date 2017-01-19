package com.lpa.lpaxmlcap;

import com.cognos.CAM_AAA.authentication.IBiBusHeader;
import com.cognos.CAM_AAA.authentication.IBiBusHeader2;
import com.cognos.CAM_AAA.authentication.ICredential;
import com.lpa.lpaxmlcap.adapters.Credential;

/**
 * Some utilities that work against the BiBusHeader object.
 * 
 * @author Jim
 *
 */
public class BiBusHeaderUtils {
	/**
	 * Builds a Credential from the Credential Value information contained in the BiBusHeader2
	 * 
	 * @param biBusHeader2
	 * @return ICredential or null if there is no information to build a Credential
	 */
	public static ICredential getCredentialFromCredentialValues(final IBiBusHeader2 biBusHeader2) {
		boolean credentialsFound = false;

		final Credential credential = new Credential();
		final String[] usernames = biBusHeader2.getCredentialValue("username");
		final String[] passwords = biBusHeader2.getCredentialValue("password");

		if (usernames != null && usernames.length > 0) {
			credentialsFound = true;
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (passwords != null && passwords.length > 0) {
			for (int i = 0; i < passwords.length; i++) {
				credential.addCredentialValue("password", passwords[i]);
			}
		}

		if (credentialsFound) {
			return credential;
		}
		else {
			return null;
		}
	}

	/**
	 * Builds a Credential from the Credential Value information contained in the BiBusHeader
	 * 
	 * @param biBusHeader
	 * @return ICredential or null if there is no information to build a Credential
	 */
	public static ICredential getCredentialFromCredentialValues(final IBiBusHeader biBusHeader) {
		boolean credentialsFound = false;

		final Credential credential = new Credential();
		final String[] usernames = biBusHeader.getCredentialValue("username");
		final String[] passwords = biBusHeader.getCredentialValue("password");

		if (usernames != null && usernames.length > 0) {
			credentialsFound = true;
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (passwords != null && passwords.length > 0) {
			for (int i = 0; i < passwords.length; i++) {
				credential.addCredentialValue("password", passwords[i]);
			}
		}

		if (credentialsFound) {
			return credential;
		}
		else {
			return null;
		}
	}

	/**
	 * Builds a Credential from the Trusted Credential Environment information contained in the
	 * BiBusHeader2. A trusted environment variable is an environment variable that has been set by a
	 * trusted party (i.e. a Cognos gateway, the dispatcher or a trusted signon namespace). The
	 * required variable is identified in a SystemRecoverableException, which an authentication
	 * provider throws when there is missing information.
	 * 
	 * @param biBusHeader2
	 * @return ICredential or null if there is no information to build a Credential
	 */
	public static Credential getCredentialFromTrustedEnvironmentVaribleValue(final IBiBusHeader2 biBusHeader2) {
		boolean credentialsFound = false;

		final Credential credential = new Credential();
		final String[] usernames = biBusHeader2.getTrustedEnvVarValue("REMOTE_USER");

		if (usernames != null && usernames.length > 0) {
			credentialsFound = true;
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (credentialsFound) {
			return credential;
		}
		else {
			return null;
		}
	}

	/**
	 * Builds a Credential from the Form Field information contained in the BiBusHeader2
	 * 
	 * @param biBusHeader2
	 * @return ICredential or null if there is no information to build a Credential
	 */
	public static Credential getCrendtialFormFieldValues(final IBiBusHeader2 biBusHeader2) {
		boolean credentialsFound = false;

		final Credential credential = new Credential();
		final String[] usernames = biBusHeader2.getFormFieldValue("CAMUsername");
		final String[] passwords = biBusHeader2.getFormFieldValue("CAMPassword");

		if (usernames != null && usernames.length > 0) {
			credentialsFound = true;
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (passwords != null && passwords.length > 0) {
			for (int i = 0; i < passwords.length; i++) {
				credential.addCredentialValue("password", passwords[i]);
			}
		}

		if (credentialsFound) {
			return credential;
		}
		else {
			return null;
		}
	}
	/**
	 * Builds a Credential from the Trusted Credential Environment information contained in the
	 * BiBusHeader. A trusted environment variable is an environment variable that has been set by a
	 * trusted party (i.e. a Cognos gateway, the dispatcher or a trusted signon namespace). The
	 * required variable is identified in a SystemRecoverableException, which an authentication
	 * provider throws when there is missing information.
	 * 
	 * @param biBusHeader
	 * @return ICredential or null if there is no information to build a Credential
	 */
	public static Credential getCredentialFromTrustedEnvironmentVaribleValue(final IBiBusHeader biBusHeader) {
		boolean credentialsFound = false;

		final Credential credential = new Credential();
		final String[] usernames = biBusHeader.getTrustedEnvVarValue("REMOTE_USER");

		if (usernames != null && usernames.length > 0) {
			credentialsFound = true;
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (credentialsFound) {
			return credential;
		}
		else {
			return null;
		}
	}

	/**
	 * Builds a Credential from the Form Field information contained in the BiBusHeader
	 * 
	 * @param biBusHeader
	 * @return ICredential or null if there is no information to build a Credential
	 */
	public static Credential getCrendtialFormFieldValues(final IBiBusHeader biBusHeader) {
		boolean credentialsFound = false;

		final Credential credential = new Credential();
		final String[] usernames = biBusHeader.getFormFieldValue("CAMUsername");
		final String[] passwords = biBusHeader.getFormFieldValue("CAMPassword");

		if (usernames != null && usernames.length > 0) {
			credentialsFound = true;
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (passwords != null && passwords.length > 0) {
			for (int i = 0; i < passwords.length; i++) {
				credential.addCredentialValue("password", passwords[i]);
			}
		}

		if (credentialsFound) {
			return credential;
		}
		else {
			return null;
		}
	}

	/**
	 * Builds a Credential from the Trusted Credential information contained in the BiBusHeader2
	 * 
	 * @param biBusHeader2
	 * @return ICredential or null if there is no information to build a Credential
	 */
	public static Credential getCredentialFromTrustedCredentialValues(final IBiBusHeader2 biBusHeader2) {
		boolean credentialsFound = false;

		final Credential credential = new Credential();
		final String[] usernames = biBusHeader2.getTrustedCredentialValue("username");
		final String[] passwords = biBusHeader2.getTrustedCredentialValue("password");

		if (usernames != null && usernames.length > 0) {
			credentialsFound = true;
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (passwords != null && passwords.length > 0) {
			for (int i = 0; i < passwords.length; i++) {
				credential.addCredentialValue("password", passwords[i]);
			}
		}

		if (credentialsFound) {
			return credential;
		}
		else {
			return null;
		}
	}

}
