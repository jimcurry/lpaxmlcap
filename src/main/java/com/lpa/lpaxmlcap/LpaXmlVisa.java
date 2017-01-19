package com.lpa.lpaxmlcap;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.cognos.CAM_AAA.authentication.*;
import com.lpa.lpaxmlcap.adapters.Credential;
import com.lpa.lpaxmlcap.adapters.TrustedCredential;
import com.lpa.lpaxmlcap.adapters.Visa;

public class LpaXmlVisa extends Visa {
	static Logger logger = Logger.getLogger(LpaXmlVisa.class);
	
	private AccountStore accountStore = null;

	public LpaXmlVisa(AccountStore accountStore) {
		super();
		this.accountStore = accountStore;
	}

	@Override
	public ICredential generateCredential(IBiBusHeader biBusHeader) throws UserRecoverableException, SystemRecoverableException, UnrecoverableException {
		if (logger.isDebugEnabled()) {
			logger.debug("generateCredential entered...");
		}
		final Credential credential = new Credential();
		final String[] usernames = biBusHeader.getCredentialValue("username");
		final String[] passwords = biBusHeader.getCredentialValue("password");

		if (usernames != null && usernames.length > 0) {
			for (int i = 0; i < usernames.length; i++) {
				credential.addCredentialValue("username", usernames[i]);
			}
		}

		if (passwords != null && passwords.length > 0) {
			for (int i = 0; i < passwords.length; i++) {
				credential.addCredentialValue("password", passwords[i]);
			}
		}
		return credential;
	}

	@Override
	public ITrustedCredential generateTrustedCredential(IBiBusHeader biBusHeader) throws UserRecoverableException, SystemRecoverableException, UnrecoverableException {
		if (logger.isDebugEnabled()) {
			logger.debug("generateTrustedCredential entered...");
			logger.debug("biBusHeader.getCredentialValue('username') " + biBusHeader.getCredentialValue("username"));
			logger.debug("biBusHeader.getCredentialValue('password') " + biBusHeader.getCredentialValue("password"));
			logger.debug("biBusHeader.getFormFieldValue('CAMUsername') " + biBusHeader.getFormFieldValue("CAMUsername"));
			logger.debug("biBusHeader.getFormFieldValue('CAMPassword') " + biBusHeader.getFormFieldValue("CAMPassword"));
		}
		
		// Look for "untrusted" credentials
		ICredential credential = BiBusHeaderUtils.getCredentialFromCredentialValues(biBusHeader);
		
		if (credential == null) {
			// Look for credentials entered on a login form
			credential = BiBusHeaderUtils.getCrendtialFormFieldValues(biBusHeader);
		}
		

		// No credentials were found so throw the proper exception to initiate collection of the
		// credentials
		if (credential == null) {
				// queue up the logon page
				generateAndThrowExceptionForLogonPrompt(null, biBusHeader.getContentLocale());
		}
		else {
			if (!this.accountStore.isCredentialValid(credential, false)) {
				generateAndThrowExceptionForLogonPrompt("Credentials Failed Authentication", biBusHeader.getContentLocale());
			}
		}

		final TrustedCredential trustedCredential = new TrustedCredential();
		
		final String[] usernames = credential.getCredentialValue("username");
		final String[] passwords = credential.getCredentialValue("password");

		if (usernames != null && usernames.length > 0 && passwords != null && passwords.length > 0) {
			for (int i = 0; i < usernames.length; i++) {
				trustedCredential.addCredentialValue("username", usernames[i]);
			}
			for (int i = 0; i < passwords.length; i++) {
				trustedCredential.addCredentialValue("password", passwords[i]);
			}
			logger.debug("generateTrustedCredential created credential...");
		}

		return trustedCredential;
	}

	/**
	 * @param credential

	 * @throws UnrecoverableException
	 */
	public void init(ICredential credential) throws UnrecoverableException {
		final String username = credential.getCredentialValue("username")[0];

		super.init(this.accountStore.getAccountByUserName(username));

		List<IGroup> groups = this.accountStore.getGroupsForUsername(username);
		for (Iterator<IGroup> it = groups.iterator(); it.hasNext();) {
			IGroup group = it.next();
			this.addGroup(group);
		}

		List<IRole> roles = this.accountStore.getRolesForUsername(username);
		for (Iterator<IRole> it = roles.iterator(); it.hasNext();) {
			IRole role = it.next();
			this.addRole(role);
		}
	}
	/*
	 * Generate an exception with applicable display objects for the login prompt Note: If this is
	 * the initial logon, to avoid an empty logon log, set the errorDetails null.
	 */
	private void generateAndThrowExceptionForLogonPrompt(String errorDetails, Locale locale) throws UserRecoverableException {
		final UserRecoverableException e = new UserRecoverableException("Please type your credentials for authentication.", errorDetails);
		e.addDisplayObject(new ReadOnlyDisplayObject("Namespace:", "CAMNamespaceDisplayName", locale.getDisplayName()));
		e.addDisplayObject(new TextDisplayObject("User ID:", "CAMUsername"));
		e.addDisplayObject(new TextNoEchoDisplayObject("Password:", "CAMPassword"));
		throw e;
	}
}
