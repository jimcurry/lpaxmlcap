package com.lpa.lpaxmlcap;

import java.io.File;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cognos.CAM_AAA.authentication.*;
import com.lpa.lpaxmlcap.adapters.Account;
import com.lpa.lpaxmlcap.adapters.Group;
import com.lpa.lpaxmlcap.adapters.NamespaceFolder;
import com.lpa.lpaxmlcap.adapters.Role;
import com.lpa.lpaxmlcap.adapters.UiClass;

/**
 * Provides access to the xml file containing the account information the CAP uses
 * 
 * @author Jim
 *
 */
public class AccountStore {
	static Logger logger = Logger.getLogger(AccountStore.class);

	private Document document;
	private ArrayList<String> customPropertyNames = new ArrayList<String>();
	private String fileName;
	private long lastModified = 0;

	/**
	 * Initializes the Account store from a file located in the same directory as the jar.
	 * 
	 * @param theNamespaceConfiguration The namespace configuration that we can use to derive the
	 *        location and name of the XML file that contains the account information.
	 * @throws UnrecoverableException
	 */
	public AccountStore(INamespaceConfiguration theNamespaceConfiguration) throws UnrecoverableException {
		String installLocation = theNamespaceConfiguration.getInstallLocation();
		fileName = installLocation + File.separator + "configuration" + File.separator + "lpacap_accounts_" + theNamespaceConfiguration.getID() + ".xml";

		loadAccountStoreIfNeeded();
	}

	/**
	 * Builds an Account object based on the information in the XML file for the passed in object id.
	 * the object id is of the form account:username, eg "account:rramjet".
	 * 
	 * @param userName user name of account to look up. Search will be case insensitive.
	 * @param locale
	 * @return Account object, null if one is not found for passed in user
	 * @throws UnrecoverableException
	 */
	public Account getAccountByObjectId(String objectId) throws UnrecoverableException {
		String userName = objectId.substring(Consts.ACCOUNT_OBJECT_ID_PREFIX.length());

		return getAccountByUserName(userName);
	}

	/**
	 * Builds an Account object based on the information in the XML file for the passed in user name.
	 * 
	 * @param userName user name of account to look up. Search will be case insensitive.
	 * @param locale
	 * @return Account object, null if one is not found for passed in user
	 * @throws UnrecoverableException
	 */
	public Account getAccountByUserName(String userName) throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		Account account = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//User[translate(@userName, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='" + userName.toLowerCase() + "']";
		try {
			Node node = (Node) xPath.compile(expression).evaluate(this.document, XPathConstants.NODE);
			if (node != null) {
				account = new Account(Consts.ACCOUNT_OBJECT_ID_PREFIX + userName.toLowerCase());

				Node attrNode = node.getAttributes().getNamedItem("userName");
				if (attrNode != null) {
					account.setUserName(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("givenName");
				if (attrNode != null) {
					account.setGivenName(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("surname");
				if (attrNode != null) {
					account.setSurname(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("businessPhone");
				if (attrNode != null) {
					account.setBusinessPhone(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("email");
				if (attrNode != null) {
					account.setEmail(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("faxPhone");
				if (attrNode != null) {
					account.setFaxPhone(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("homePhone");
				if (attrNode != null) {
					account.setHomePhone(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("mobilePhone");
				if (attrNode != null) {
					account.setMobilePhone(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("pagerPhone");
				if (attrNode != null) {
					account.setPagerPhone(attrNode.getNodeValue());
				}
				attrNode = node.getAttributes().getNamedItem("postalAddress");
				if (attrNode != null) {
					account.setPostalAddress(attrNode.getNodeValue());
				}
				// Add any custom properties present
				for (Iterator<String> it = this.customPropertyNames.iterator(); it.hasNext();) {
					String customPropertyName = it.next();
					attrNode = node.getAttributes().getNamedItem(customPropertyName);
					if (attrNode != null) {
						account.addCustomProperty(customPropertyName, attrNode.getNodeValue());
					}
				}

				// We aren't supporting multiple locales, so we pick one and use it
				account.addName(Locale.ENGLISH, account.getUserName());

			}
		}
		catch (Exception e) {
			logger.error("Exception in getAccountByUserName", e);

			throw new UnrecoverableException("Couldn't create an Account " + e + " exception", e.toString());
		}

		return account;
	}

	private List<Account> getAccountsWithGroupName(String groupName) throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		ArrayList<Account> accounts = new ArrayList<Account>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//GroupName[text()='" + groupName + "']/../..";
		try {
			NodeList nodes = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				Node attrNode = node.getAttributes().getNamedItem("userName");
				if (attrNode != null) {
					String userName = attrNode.getNodeValue();
					accounts.add(getAccountByUserName(userName));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAccountsWithGroupName", e);

			throw new UnrecoverableException("Error getting accounts with group name " + e + " exception", e.toString());
		}

		return accounts;
	}

	private List<Account> getAccountsWithRoleName(String roleName) throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		ArrayList<Account> accounts = new ArrayList<Account>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//RoleName[text()='" + roleName + "']/../..";
		try {
			NodeList nodes = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				Node attrNode = node.getAttributes().getNamedItem("userName");
				if (attrNode != null) {
					String userName = attrNode.getNodeValue();
					accounts.add(getAccountByUserName(userName));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAccountsWithGroupName", e);

			throw new UnrecoverableException("Error getting accounts with group name " + e + " exception", e.toString());
		}

		return accounts;
	}

	/**
	 * Returns a list of all the accounts
	 * 
	 * @return
	 * @throws UnrecoverableException
	 */
	public List<Account> getAllAccounts() throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		ArrayList<Account> accounts = new ArrayList<Account>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//User";
		try {
			NodeList nodes = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				Node attrNode = node.getAttributes().getNamedItem("userName");
				if (attrNode != null) {
					Account account = getAccountByUserName(attrNode.getNodeValue());
					accounts.add(account);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAllAccounts", e);

			throw new UnrecoverableException("Error getting all accounts " + e + " exception", e.toString());
		}

		return accounts;
	}

	/**
	 * Returns a list of all the groups
	 * 
	 * @return
	 * @throws UnrecoverableException
	 */
	public List<Group> getAllGroups() throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		ArrayList<Group> groups = new ArrayList<Group>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//GroupName";
		try {
			HashSet<String> groupNames = new HashSet<String>();
			NodeList nodes = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String groupName = node.getTextContent();
				groupNames.add(groupName);
			}
			for (Iterator<String> iterator = groupNames.iterator(); iterator.hasNext();) {
				String groupName = (String) iterator.next();
				Group group = new Group(Consts.GROUP_OBJECT_ID_PREFIX + groupName);
				group.addName(Locale.ENGLISH, groupName);
				groups.add(group);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAllGroups", e);

			throw new UnrecoverableException("Error getting all groups " + e + " exception", e.toString());
		}

		return groups;
	}

	/**
	 * Returns a list of all the roles
	 * 
	 * @return
	 * @throws UnrecoverableException
	 */
	public List<Role> getAllRoles() throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		ArrayList<Role> roles = new ArrayList<Role>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//RoleName";
		try {
			HashSet<String> roleNames = new HashSet<String>();
			NodeList nodes = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String roleName = node.getTextContent();
				roleNames.add(roleName);
			}
			for (Iterator<String> iterator = roleNames.iterator(); iterator.hasNext();) {
				String roleName = (String) iterator.next();
				Role role = new Role(Consts.ROLE_OBJECT_ID_PREFIX + roleName);
				role.addName(Locale.ENGLISH, roleName);
				roles.add(role);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAllGroups", e);

			throw new UnrecoverableException("Error getting all groups " + e + " exception", e.toString());
		}

		return roles;
	}

	/**
	 * Returns all the child objects under the object with the passed in objectId
	 * 
	 * @param objectId object id you want the children of.
	 * @return List of objects that are children of the object with the passed in id
	 * @throws UnrecoverableException
	 */
	public List<UiClass> getChildrenOfObjectByObjectId(String objectId) throws UnrecoverableException {
		List<UiClass> uiClasses = new ArrayList<UiClass>();

		if (objectId.indexOf(Consts.FOLDER_OBJECT_ID_PREFIX) == 0) {
			final String folderName = objectId.substring(Consts.FOLDER_OBJECT_ID_PREFIX.length());
			if (folderName.equals(Consts.FOLDER_NAME_USERS)) {
				List<Account> accounts = getAllAccounts();
				for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
					Account account = (Account) iterator.next();
					uiClasses.add((UiClass) account);
				}
			}
			if (folderName.equals(Consts.FOLDER_NAME_ROLES)) {
				List<Role> roles = getAllRoles();
				for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();) {
					Role role = (Role) iterator.next();
					uiClasses.add((UiClass) role);
				}
			}
			if (folderName.equals(Consts.FOLDER_NAME_GROUPS)) {
				List<Group> groups = getAllGroups();
				for (Iterator<Group> iterator = groups.iterator(); iterator.hasNext();) {
					Group group = (Group) iterator.next();
					uiClasses.add((UiClass) group);
				}
			}
		}
		else if (objectId.indexOf(Consts.GROUP_OBJECT_ID_PREFIX) == 0) {
			final String groupName = objectId.substring(Consts.GROUP_OBJECT_ID_PREFIX.length());
			List<Account> accounts = getAccountsWithGroupName(groupName);
			for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
				Account account = (Account) iterator.next();
				uiClasses.add((UiClass) account);
			}
		}
		else if (objectId.indexOf(Consts.ROLE_OBJECT_ID_PREFIX) == 0) {
			final String roleName = objectId.substring(Consts.ROLE_OBJECT_ID_PREFIX.length());
			List<Account> accounts = getAccountsWithRoleName(roleName);
			for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
				Account account = (Account) iterator.next();
				uiClasses.add((UiClass) account);
			}
		}

		return uiClasses;
	}

	/**
	 * Sends back the group object for the passed in objectId
	 * 
	 * @param objectId
	 * @throws UnrecoverableException
	 */
	public Group getGroupByObjectId(String objectId) throws UnrecoverableException {
		String groupName = objectId.substring(objectId.indexOf(":") + 1);

		Group group = new Group(objectId);
		group.addName(Locale.ENGLISH, groupName);

		List<Account> accounts = getAccountsWithGroupName(groupName);

		for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
			Account account = (Account) iterator.next();
			group.addMember(account);
		}

		return group;
	}

	/**
	 * Gets a list of groups the passed in user name is a member of.
	 * 
	 * @param username user name you want the groups for
	 * @return list of Groups
	 * @throws UnrecoverableException
	 */
	public List<IGroup> getGroupsForUsername(String username) throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		final List<IGroup> groups = new ArrayList<IGroup>();

		final XPath xPath = XPathFactory.newInstance().newXPath();
		final String expression = "//User[translate(@userName, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='" + username + "']/Groups/GroupName";
		try {
			final NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				String groupName = node.getTextContent().trim();
				Group group = new Group(Consts.GROUP_OBJECT_ID_PREFIX + groupName);
				group.addName(Locale.ENGLISH, groupName);
				groups.add(group);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getGroupsForUsername", e);

			throw new UnrecoverableException("Error getting groups for username " + e + " exception", e.toString());
		}
		return groups;
	}

	/**
	 * Sends back the namespace folder object for the passed in objectId
	 * 
	 * @param objectId
	 * @throws UnrecoverableException
	 */
	public NamespaceFolder getNamespaceFolderByObjectId(String objectId) throws UnrecoverableException {
		String folderName = objectId.substring(objectId.indexOf(":") + 1);

		if (folderName.equals("Users") || folderName.equals("Groups") || folderName.equals("Roles")) {
			NamespaceFolder namespaceFolder = new NamespaceFolder(objectId);
			namespaceFolder.addName(Locale.ENGLISH, folderName);
			return namespaceFolder;
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the object with the passed in object id
	 * 
	 * @param objectId the object id you want to get
	 * @return the object with the passed in object id
	 * @throws UnrecoverableException
	 */
	public IUiClass getObjectByObjectId(String objectId) throws UnrecoverableException {
		if (objectId.indexOf(Consts.ACCOUNT_OBJECT_ID_PREFIX) == 0) {
			return getAccountByObjectId(objectId);
		}
		else if (objectId.indexOf(Consts.FOLDER_OBJECT_ID_PREFIX) == 0) {
			return getNamespaceFolderByObjectId(objectId);
		}
		else if (objectId.indexOf(Consts.ROLE_OBJECT_ID_PREFIX) == 0) {
			return getRoleByObjectId(objectId);
		}
		else if (objectId.indexOf(Consts.GROUP_OBJECT_ID_PREFIX) == 0) {
			return getGroupByObjectId(objectId);
		}
		return null;
	}

	/**
	 * Sends back the role object for the passed in objectId
	 * 
	 * @param objectId
	 * @throws UnrecoverableException
	 */
	public Role getRoleByObjectId(String objectId) throws UnrecoverableException {
		String roleName = objectId.substring(objectId.indexOf(":") + 1);

		Role role = new Role(objectId);
		role.addName(Locale.ENGLISH, roleName);

		List<Account> accounts = getAccountsWithRoleName(roleName);

		for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
			Account account = (Account) iterator.next();
			role.addMember(account);
		}

		return role;
	}

	/**
	 * Gets a list of roles the passed in user name is a member of.
	 * 
	 * @param username user name you want the roles for
	 * @return list of Roles
	 * @throws UnrecoverableException
	 */
	public List<IRole> getRolesForUsername(String username) throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		final List<IRole> roles = new ArrayList<IRole>();

		final XPath xPath = XPathFactory.newInstance().newXPath();
		final String expression = "//User[translate(@userName, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='" + username + "']/Roles/RoleName";
		try {
			final NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				String roleName = node.getTextContent().trim();
				Role role = new Role(Consts.ROLE_OBJECT_ID_PREFIX + roleName);
				role.addName(Locale.ENGLISH, roleName);
				roles.add(role);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getRolesForUsername", e);

			throw new UnrecoverableException("Error getting roles for username " + e + " exception", e.toString());
		}
		return roles;
	}

	/**
	 * For this implementation will return three folders one for users, one for groups, and one for
	 * roles. In general this needs to return whatever you expect to see in the admin console when
	 * you drill into the name space on the security tab
	 * 
	 * @param visa
	 * @return the root objects
	 */
	public Collection<IUiClass> getRootObjects(IVisa visa) {
		ArrayList<IUiClass> rootObjects = new ArrayList<IUiClass>(3);

		NamespaceFolder userFolder = new NamespaceFolder(Consts.FOLDER_OBJECT_ID_PREFIX + Consts.FOLDER_NAME_USERS);
		userFolder.addName(Locale.ENGLISH, "Users");
		rootObjects.add(userFolder);

		NamespaceFolder groupFolder = new NamespaceFolder(Consts.FOLDER_OBJECT_ID_PREFIX + Consts.FOLDER_NAME_GROUPS);
		groupFolder.addName(Locale.ENGLISH, "Groups");
		rootObjects.add(groupFolder);

		NamespaceFolder roleFolder = new NamespaceFolder(Consts.FOLDER_OBJECT_ID_PREFIX + Consts.FOLDER_NAME_ROLES);
		roleFolder.addName(Locale.ENGLISH, "Roles");
		rootObjects.add(roleFolder);

		return rootObjects;
	}

	/**
	 * Checks if the passed in credential is valid.
	 * 
	 * @param credential Credential to validate
	 * @param isSingleSignon true if single sign on is valid, tells us if we need to look for a
	 *        password.
	 * @return true if the passed in credential is valid
	 * @throws UnrecoverableException
	 */
	public boolean isCredentialValid(ICredential credential, boolean isSingleSignon) throws UnrecoverableException {
		loadAccountStoreIfNeeded();

		boolean isValid = false;

		if (credential.getCredentialNames() != null && credential.getCredentialValue("username") != null && (!isSingleSignon && credential.getCredentialValue("password") != null || isSingleSignon)) {

			final String username = credential.getCredentialValue("username")[0];
			String expression;

			if (isSingleSignon) {
				expression = "//User[translate(@userName, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='" + username.toLowerCase() + "']";
			}
			else {
				final String password = credential.getCredentialValue("password")[0];
				expression = "//User[translate(@userName, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='" + username.toLowerCase() + "' and @password='" + password + "']";
			}

			XPath xPath = XPathFactory.newInstance().newXPath();
			try {
				Node node = (Node) xPath.compile(expression).evaluate(this.document, XPathConstants.NODE);
				if (node != null) {
					isValid = true;
				}
			}
			catch (Exception e) {
				logger.error("Exception in isCredentialValid", e);

				throw new UnrecoverableException("Couldn't validate Credential " + e + " exception", e.toString());
			}
		}
		return isValid;
	}

	private void loadAccountStoreIfNeeded() throws UnrecoverableException {
		File file = new File(fileName);
		long currentLastModified = file.lastModified();

		if (currentLastModified != this.lastModified) {
			if (logger.isDebugEnabled()) {
				logger.debug("Reloading account store");
			}

			this.lastModified = currentLastModified;
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			try {
				builder = builderFactory.newDocumentBuilder();
				this.document = builder.parse(fileName);
				loadCustomPropertyNames();
			}
			catch (Exception e) {
				logger.error("Exception in loadAccountStoreIfNeeded", e);

				throw new UnrecoverableException("Couldn't instantiate the AccountStore " + e + " exception", e.toString());
			}
		}
	}

	/*
	 * Loads the customerPropertyNames field with the list of custom property names.
	 */
	private void loadCustomPropertyNames() throws UnrecoverableException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//CustomProperty";
		NodeList nodeList;
		try {
			nodeList = (NodeList) xPath.compile(expression).evaluate(this.document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				this.customPropertyNames.add(node.getTextContent().trim());
			}
		}
		catch (Exception e) {
			logger.error("Exception in loadCustomPropertyNames", e);

			throw new UnrecoverableException("Couldn't an Account's custom properties " + e + " exception", e.toString());
		}
	}
}