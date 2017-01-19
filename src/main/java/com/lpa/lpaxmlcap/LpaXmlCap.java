package com.lpa.lpaxmlcap;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.cognos.CAM_AAA.authentication.*;
import com.cognos.CAM_AAA.authentication.ISearchStep.SearchAxis;
import com.lpa.lpaxmlcap.adapters.Namespace;
import com.lpa.lpaxmlcap.adapters.UiClass;
import com.lpa.lpaxmlcap.adapters.Visa;

public class LpaXmlCap extends Namespace implements INamespaceAuthenticationProvider2 {
	static Logger logger = Logger.getLogger(LpaXmlCap.class);

	private AccountStore accountStore = null;

	private int recursionCount = 0;

	/*
	 * Takes this namespace out of service. This method is only called after all threads within the
	 * namespace's methods have exited or after a timeout period has passed. After this method is
	 * called, no other methods can be called with this namespace. This method cleans up any
	 * resources that are being held (for example, memory, file handles, threads) and ensures that
	 * any persistent state is synchronized with the current state of the namespace in memory.
	 */
	@Override
	public void destroy() {
		super.destroy();
		logger.info("LpaXmlCap destroyed");
	}

	/*
	 * Generate an exception with applicable display objects for the login prompt Note: If this is
	 * the initial logon, to avoid an empty logon log, set the errorDetails null.
	 */
	private void generateAndThrowExceptionForLogonPrompt(String errorDetails) throws UserRecoverableException {
		final UserRecoverableException e = new UserRecoverableException("Please type your credentials for authentication.", errorDetails);
		e.addDisplayObject(new ReadOnlyDisplayObject("Namespace:", "CAMNamespaceDisplayName", this.getName(Locale.getDefault())));
		e.addDisplayObject(new TextDisplayObject("User ID:", "CAMUsername"));
		e.addDisplayObject(new TextNoEchoDisplayObject("Password:", "CAMPassword"));
		throw e;
	}

	/*
	 * Places this authentication namespace into service. The init method is called exactly once for
	 * each instance of a namespace. The init method must complete successfully before a namespace
	 * can receive any requests.
	 */
	@Override
	public void init(INamespaceConfiguration theNamespaceConfiguration) throws UnrecoverableException {
		initializeLogger(theNamespaceConfiguration);
		logger.info("LpaXmlCap version " + Consts.VERSION + " started...");

		super.init(theNamespaceConfiguration);

		this.accountStore = new AccountStore(theNamespaceConfiguration);
	}

	private void initializeLogger(INamespaceConfiguration theNamespaceConfiguration) throws UnrecoverableException {
		String installLocation = theNamespaceConfiguration.getInstallLocation();
		String logFileName = installLocation + File.separator + "logs" + File.separator + "lpaxmlcap_" + theNamespaceConfiguration.getID() + ".log";

		Properties properties = new Properties();

		properties.setProperty("log4j.logger.com.lpa.lpaxmlcap", "DEBUG, A1");

		properties.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.A1.layout.ConversionPattern", "%d %5p %t %c - %m%n");
		properties.setProperty("log4j.appender.A1", "org.apache.log4j.RollingFileAppender");

		properties.setProperty("log4j.appender.A1.File", logFileName);

		properties.setProperty("log4j.appender.A1.MaxFileSize", "10MB");
		properties.setProperty("log4j.appender.A1.MaxBackupIndex", "2");

		PropertyConfigurator.configure(properties);
	}

	/*
	 * Logs off the user from this authentication namespace. After this call this visa will no longer
	 * be used by the system.
	 * 
	 * @param theVisa A visa that grants access to this authentication namespace. This visa was
	 * previously issued by this authentication namespace using the logon method.theBiBusHeader
	 * 
	 * @param theBiBusHeader contains the information used to authenticate the user. The biBusHeader
	 * can be modified to remove existing cookies, form fields, and so on.
	 */
	@Override
	public void logoff(IVisa theVisa, IBiBusHeader theBiBusHeader) {
		if (theVisa instanceof Visa) {
			try {
				((Visa) theVisa).destroy();
			}
			catch (UnrecoverableException e) {
				logger.error("Exception in logoff", e);
				e.printStackTrace();
			}
		}
	}

	/*
	 * Logs on the user to this authentication namespace.
	 * 
	 * @param theBiBusHeader The biBusHeader that contains the information used to authenticate the
	 * user. The BiBusHeader2 provides access to cookies, trusted/untrusted environment variables,
	 * trusted/untrusted credential values and form fields from a login page.
	 * 
	 * @returns A visa that grants access to the authentication namespace.
	 */
	@SuppressWarnings("unused")
	@Override
	public IVisa logon(IBiBusHeader2 theBiBusHeader2) throws UserRecoverableException, SystemRecoverableException, UnrecoverableException {
		final LpaXmlVisa visa = new LpaXmlVisa(this.accountStore);

		final boolean singleSignOnEnabled = false;

		// Look for trusted credentials
		ICredential credential = BiBusHeaderUtils.getCredentialFromTrustedCredentialValues(theBiBusHeader2);

		if (credential == null) {
			// Look for "untrusted" credentials
			credential = BiBusHeaderUtils.getCredentialFromCredentialValues(theBiBusHeader2);
		}
		if (credential == null && !singleSignOnEnabled) {
			// Look for credentials entered on a login form
			credential = BiBusHeaderUtils.getCrendtialFormFieldValues(theBiBusHeader2);
		}

		if (credential == null && singleSignOnEnabled) {
			// look for credentials for single signon. We are assuming they are in a trusted
			// environment variable.
			credential = BiBusHeaderUtils.getCredentialFromTrustedEnvironmentVaribleValue(theBiBusHeader2);
		}

		// No credentials were found so throw the proper exception to initiate collection of the
		// credentials
		if (credential == null) {
			if (singleSignOnEnabled) {
				// Note depending on need you can send a Kerberos authentication token, or request
				// multiple trusted environment variables
				// instead of the single trusted environment variable below. Details are in the javadoc
				// for SystemRecoverableException
				throw new SystemRecoverableException("Challenge for REMOTE_USER", "REMOTE_USER");

			}
			else {
				// queue up the logon page
				generateAndThrowExceptionForLogonPrompt(null);
			}
		}
		else {
			if (accountStore.isCredentialValid(credential, singleSignOnEnabled)) {
				visa.init(credential);
				if (logger.isDebugEnabled()) {
					logger.debug("logon successful for " + credential.getCredentialValue("username")[0]);
				}
			}
			else {
				generateAndThrowExceptionForLogonPrompt("Credentials Failed Authentication");
			}
		}
		return visa;
	}

	private void logQuery(IVisa visa, IQuery query) throws UnrecoverableException {
		if (logger.isDebugEnabled()) {
			logger.info(" ");
			logger.info("search method called");
			logger.info("user-" + visa.getAccount().getUserName());
			logger.info("query Properties");
			for (int i = 0; i < query.getProperties().length; i++) {
				String propertyName = query.getProperties()[i];
				logger.info("     Property(" + i + ")-" + propertyName);
			}
			logger.info("query Options");
			logger.info("      MaxCount-" + query.getQueryOption().getMaxCount());
			logger.info("      SkipCount-" + query.getQueryOption().getSkipCount());
			for (int i = 0; i < query.getProperties().length; i++) {
				String propertyName = query.getProperties()[i];
				if (query.getQueryOption().getRefProps(propertyName) != null) {
					for (int j = 0; j < query.getQueryOption().getRefProps(propertyName).length; j++) {
						String refProperty = query.getProperties()[j];
						logger.info("     " + propertyName + " Ref Property(" + j + ")-" + refProperty);
					}
				}
			}
			logger.info("query Search Expression");
			logger.info("      ObjectId-" + query.getSearchExpression().getObjectID());
			for (int i = 0; i < query.getSearchExpression().getSteps().length; i++) {
				ISearchStep searchStep = query.getSearchExpression().getSteps()[i];
				logger.info("      Step(" + i + ") Axis-" + Consts.decodeSearchAxis(searchStep.getAxis()));
				logger.info("      Step(" + i + ") Search Filter");

				ISearchFilter searchFilter = searchStep.getPredicate();
				this.recursionCount = 0;
				if (searchFilter != null) {
					logSearchFilter(searchFilter);
				}
			}

			logger.info("query Sort Properties");
			ISortProperty[] sortProperties = query.getSortProperties();
			if (sortProperties != null) {
				for (int i = 0; i < sortProperties.length; i++) {
					String propertyName = sortProperties[i].getPropertyName();
					String sortOrder = Consts.decodeSortOrder(sortProperties[i].getSortOrder());
					logger.info("     Sort(" + i + ")-" + propertyName + ", " + sortOrder);
				}
			}

			logger.info(" ");
		}
	}

	private void logSearchFilter(ISearchFilter searchFilter) throws UnrecoverableException {
		if (this.recursionCount++ > 100) {
			logger.error("Death by recursion in logSearchFilter");
			throw new UnrecoverableException("Death by recursion in logSearchFilter", "Death by recursion in logSearchFilter");
		}

		final String pad = "                    ";
		logger.info(pad + "Filter Type-" + Consts.decodeSearchFilterType(searchFilter.getSearchFilterType()));
		if (searchFilter.getSearchFilterType() == ISearchFilter.ConditionalExpression) {
			logger.info(pad + "Conditional Operator-" + ((ISearchFilterConditionalExpression) searchFilter).getOperator());
			ISearchFilter[] searchFilters = ((ISearchFilterConditionalExpression) searchFilter).getFilters();
			if (searchFilters != null) {
				for (int i = 0; i < searchFilters.length; i++) {
					logSearchFilter(searchFilters[i]);
				}
			}
		}
		else if (searchFilter.getSearchFilterType() == ISearchFilter.FunctionCall) {
			logger.info(pad + "Function Call-" + ((ISearchFilterFunctionCall) searchFilter).getFunctionName());
			for (int j = 0; j < ((ISearchFilterFunctionCall) searchFilter).getParameters().length; j++) {
				String parameter = ((ISearchFilterFunctionCall) searchFilter).getParameters()[j];
				logger.info(pad + "Function Call parameter-" + parameter);
			}
		}
		else if (searchFilter.getSearchFilterType() == ISearchFilter.RelationalExpression) {
			logger.info(pad + "Relation Expression Constraint-" + ((ISearchFilterRelationExpression) searchFilter).getConstraint());
			logger.info(pad + "Relation Expression Operator-" + ((ISearchFilterRelationExpression) searchFilter).getOperator());
			logger.info(pad + "Relation Expression Property-" + ((ISearchFilterRelationExpression) searchFilter).getPropertyName());
		}

	}

	/*
	 * Retrieves a set of objects that exist within the current authentication namespace. The result
	 * only contains the objects that the user has the appropriate permissions for. If the user is
	 * not authenticated, the result will only contain the information visible to an anonymous user.
	 * 
	 * @param theVisa A visa that grants access to this authentication namespace, and identifies the
	 * user performing this search. This parameter will be null if the user is not authenticated in
	 * this namespace.
	 * 
	 * @param theQuery The details about which objects to return.
	 * 
	 * searches observed in testing to cover
	 * 
	 * after login there are 25 Self search request issued to retrieve the account object for the
	 * user who just logged in
	 */
	@Override
	public IQueryResult search(IVisa visa, IQuery query) throws UnrecoverableException {
		if (logger.isDebugEnabled()) {
			logQuery(visa, query);
		}
		
		final ISearchStep[] searchSteps = query.getSearchExpression().getSteps();
		final ISearchFilter searchFilter = searchSteps[0].getPredicate();
		
		if (searchSteps.length != 1) {
			logger.error("Only one step is support for search expression, this search has " + Integer.toString(searchSteps.length));
			logQuery(visa, query);
			throw new UnrecoverableException("Internal Error", "Invalid search expression. Only one step is supported for this namespace.");
		}

		final List<IBaseClass> identifiedObjects = new ArrayList<IBaseClass>();
		
		final ISearchStep searchStep = searchSteps[0];
		final int searchType = searchStep.getAxis();
		final String objectId = query.getSearchExpression().getObjectID();

		if (searchType == SearchAxis.Self) {
			if (objectId == null) {
				logger.error("ObjectID is null with SearchAxis.Self");
				logQuery(visa, query);
				throw new UnrecoverableException("Internal Error", "ObjectID is null with SearchAxis.Self.");
			}
			
			IUiClass uiClass = accountStore.getObjectByObjectId(objectId);
			if (uiClass != null) {
				identifiedObjects.add(uiClass);
			}
		}

		else if (searchType == SearchAxis.Child) {
			if (objectId == null) {
				Collection<IUiClass> rootObjects = accountStore.getRootObjects(visa);
				
				for (IUiClass uiObj: rootObjects) {
					if (uiObj instanceof UiClass) {
						if (((UiClass)uiObj).matchesFilter(searchFilter)) {
							identifiedObjects.add(uiObj);
						}
					}
				}
			}
			else {
				List<UiClass> uiClasses = accountStore.getChildrenOfObjectByObjectId(objectId);
				for (Iterator<UiClass> iterator = uiClasses.iterator(); iterator.hasNext();) {
					UiClass uiClass = (UiClass) iterator.next();
					if (uiClass.matchesFilter(searchFilter)) {
						identifiedObjects.add(uiClass);
					}
				}
			}
		}

		//gets all objects under the passed in one (will go more than one level down)
		else if (searchType == SearchAxis.Descendent) {
			if (objectId == null) {
				Collection<IUiClass> rootObjects = accountStore.getRootObjects(visa);
				
				for (IUiClass uiObj : rootObjects) {
					if (((UiClass) uiObj).matchesFilter(searchFilter)) {
						identifiedObjects.add(uiObj);
					}
					List<UiClass> children = accountStore.getChildrenOfObjectByObjectId(uiObj.getObjectID());
					for (UiClass uiClass : children) {
						if (((UiClass) uiClass).matchesFilter(searchFilter)) {
							identifiedObjects.add(uiClass);
						}
					}
				}
			}
			else {
				List<UiClass> uiClasses = accountStore.getChildrenOfObjectByObjectId(objectId);
				for (Iterator<UiClass> iterator = uiClasses.iterator(); iterator.hasNext();) {
					UiClass uiClass = (UiClass) iterator.next();
					if (uiClass.matchesFilter(searchFilter)) {
						identifiedObjects.add(uiClass);
					}
				}
			}
		}
//		else if (searchType == SearchAxis.DescendentOrSelf) {}
//		else if (searchType == SearchAxis.Ancestor) {}
//		else if (searchType == SearchAxis.AncestorOrSelf) {}
//		else if (searchType == SearchAxis.Parent) {}
//		else if (searchType == SearchAxis.Unknown) {}
		else {
			logger.error("Encountered an unhanded search axis (" + Consts.decodeSearchAxis(searchType) + ") in search method");
			throw new UnrecoverableException("Unhandled search Axis", "Encountered an unhanded search axis (" + Consts.decodeSearchAxis(searchType) + ") in search method");
		}
		
		ISortProperty[] sortProperties = query.getSortProperties();
		for (int i = 0; i < sortProperties.length; i++) {
			ISortProperty sortProperty = sortProperties[i];
			if (sortProperty.getPropertyName().equals("defaultName") && sortProperty.getSortOrder() == ISortProperty.SortOrderAscending) {
				Collections.sort(identifiedObjects, new Comparator<Object>() {
					@Override
					public int compare(Object a1, Object a2) {
						return ((IBaseClass) a1).getName(Locale.ENGLISH).compareToIgnoreCase(((IBaseClass) a2).getName(Locale.ENGLISH));
					}
				});
				break;
			}
		}
		

		//send back result, taking into account paging request, if present
		final QueryResult queryResult = new QueryResult();
		if (identifiedObjects.size() > 0) {
			int outputCount = 0;
			long maxcount = query.getQueryOption().getMaxCount();
			long skipcount = query.getQueryOption().getSkipCount();

			ListIterator<IBaseClass> iterator = identifiedObjects.listIterator((int) skipcount);
			while (((maxcount == -1) || (outputCount < maxcount)) && (iterator.hasNext())) {
				queryResult.addObject(iterator.next());
				outputCount++;
			}
		}
		
		return queryResult;
	}
}
