package com.lpa.lpaxmlcap.adapters;

/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2005, 2012
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 */

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import com.cognos.CAM_AAA.authentication.*;
import com.lpa.lpaxmlcap.Consts;


public class UiClass implements IUiClass
{
	/**
	 * @param theObjectID
	 */
	public UiClass(String theObjectID)
	{
		super();
		names = null;
		descriptions = null;
		ancestors = null;
		objectID = theObjectID;
	}


	/**
	 * @param theLocale
	 * @param theDescription
	 */
	public void addDescription(Locale theLocale, String theDescription)
	{
		if (descriptions == null)
		{
			descriptions = new HashMap();
		}
		descriptions.put(theLocale, theDescription);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IUiClass#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale theLocale)
	{
		if (descriptions != null)
		{
			return (String) descriptions.get(theLocale);
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IUiClass#getAvailableDescriptionLocales()
	 */
	public Locale[] getAvailableDescriptionLocales()
	{
		if (descriptions != null)
		{
			Set keySet = descriptions.keySet();
			Locale[] array = new Locale[keySet.size()];
			return (Locale[]) keySet.toArray(array);
		}
		return null;
	}


	/**
	 * @param theAncestor
	 */
	public void addAncestors(IBaseClass theAncestor)
	{
		if (ancestors == null)
		{
			ancestors = new Stack();
		}
		ancestors.push(theAncestor);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IBaseClass#getAncestors()
	 */
	public IBaseClass[] getAncestors()
	{
		if (ancestors != null)
		{
			IBaseClass[] array = new IBaseClass[ancestors.size()];
			return (IBaseClass[]) ancestors.toArray(array);
		}
		return null;
	}


	/**
	 * @param theLocale
	 * @param theName
	 */
	public void addName(Locale theLocale, String theName)
	{
		if (names == null)
		{
			names = new HashMap();
		}
		names.put(theLocale, theName);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IBaseClass#getHasChildren()
	 */
	public boolean getHasChildren()
	{
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IBaseClass#getName(java.util.Locale)
	 */
	public String getName(Locale theLocale)
	{
		if (names != null)
		{
			return (String) names.get(theLocale);
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IBaseClass#getAvailableNameLocales()
	 */
	public Locale[] getAvailableNameLocales()
	{
		if (names != null)
		{
			Set keySet = names.keySet();
			Locale[] array = new Locale[keySet.size()];
			return (Locale[]) keySet.toArray(array);
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IBaseClass#getObjectID()
	 */
	public String getObjectID()
	{
		return objectID;
	}


	/**
	 * @param theObjectID
	 */
	protected void setObjectID(String theObjectID)
	{
		objectID = theObjectID;
	}

	private String	objectID;
	private HashMap	names;
	private HashMap	descriptions;
	private Stack	ancestors;
	
	public boolean matchesFilter(ISearchFilter theFilter) {
		if (theFilter == null) {
			return true;
		}
		switch (theFilter.getSearchFilterType()) {
		case ISearchFilter.ConditionalExpression: {
			ISearchFilterConditionalExpression item = (ISearchFilterConditionalExpression) theFilter;
			String operator = item.getOperator();
			ISearchFilter[] filters = item.getFilters();
			if (filters.length > 0) {
				boolean retval = false;
				for (int i = 0; i < filters.length; i++) {
					retval = matchesFilter(filters[i]);
					if (operator.equals(ISearchFilterConditionalExpression.ConditionalAnd)) {
						if (retval == false) {
							return false;
						}
					}
					else if (operator.equals(ISearchFilterConditionalExpression.ConditionalOr)) {
						if (retval == true) {
							return true;
						}
					}
					else {
						return false;
					}
				}

				if (operator.equals(ISearchFilterConditionalExpression.ConditionalAnd)) {
					return true;
				}
				else {
					return false;
				}
			}
		}
			break;
		case ISearchFilter.FunctionCall: {
			ISearchFilterFunctionCall item = (ISearchFilterFunctionCall) theFilter;
			String functionName = item.getFunctionName();
			if (functionName.equals(ISearchFilterFunctionCall.Contains)) {
				String[] parameter = item.getParameters();
				String propertyName = parameter[0];
				String value = parameter[1];
				if (propertyName.compareTo("@objectClass") == 0) {
					return (getObjectType().indexOf(value) > 0);
				}
				else if (propertyName.equals("@defaultName") || propertyName.equals("@name")) {
					Locale[] locales = getAvailableNameLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getName(locales[i]).toUpperCase(locales[i]).indexOf(value.toUpperCase()) != -1) {
								return true;
							}
						}
					}
				}
				else if (propertyName.equals("@description")) {
					Locale[] locales = getAvailableDescriptionLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getDescription(locales[i]).indexOf(value) != -1) {
								return true;
							}
						}
					}
				}
				return false;
			}
			else if (functionName.compareTo(ISearchFilterFunctionCall.StartsWith) == 0) {
				String[] parameter = item.getParameters();
				String propertyName = parameter[0];
				String value = parameter[1];
				if (propertyName.compareTo("@objectClass") == 0) {
					return (getObjectType().startsWith(value));
				}
				else if (propertyName.compareTo("@defaultName") == 0 || propertyName.compareTo("@name") == 0) {
					Locale[] locales = getAvailableNameLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getName(locales[i]).toUpperCase(locales[i]).startsWith(value.toUpperCase())) {
								return true;
							}
						}
					}
				}
				else if (propertyName.equals("@description")) {
					Locale[] locales = getAvailableDescriptionLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getDescription(locales[i]).startsWith(value)) {
								return true;
							}
						}
					}
				}
				return false;
			}
			else if (functionName.compareTo(ISearchFilterFunctionCall.EndsWith) == 0) {
				String[] parameter = item.getParameters();
				String propertyName = parameter[0];
				String value = parameter[1];
				if (propertyName.compareTo("@objectClass") == 0) {
					return (getObjectType().endsWith(value));
				}
				else if (propertyName.compareTo("@defaultName") == 0 || propertyName.compareTo("@name") == 0) {
					Locale[] locales = getAvailableNameLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getName(locales[i]).toUpperCase(locales[i]).endsWith(value.toUpperCase())) {
								return true;
							}
						}
					}
				}
				else if (propertyName.equals("@description")) {
					Locale[] locales = getAvailableDescriptionLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getDescription(locales[i]).endsWith(value)) {
								return true;
							}
						}
					}
				}
				return false;
			}
			else {
				return false;
			}
		}
		case ISearchFilter.RelationalExpression: {
			ISearchFilterRelationExpression item = (ISearchFilterRelationExpression) theFilter;
			String propertyName = item.getPropertyName();
			String constraint = item.getConstraint();
			String operator = item.getOperator();
			if (propertyName.equals("@objectClass")) {
				if (constraint.equals(getObjectType())) {
					return (operator.equals(ISearchFilterRelationExpression.EqualTo));
				}
				else {
					return false;
				}
			}
			else if (propertyName.equals("@defaultName") || propertyName.equals("@name")) {
				if (operator.equals(ISearchFilterRelationExpression.EqualTo)) {
					Locale[] locales = getAvailableNameLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getName(locales[i]).toUpperCase(locales[i]).compareTo(constraint.toUpperCase()) == 0) {
								return true;
							}
						}
					}
					return false;
				}
				else if (operator.equals(ISearchFilterRelationExpression.NotEqual)) {
					Locale[] locales = getAvailableDescriptionLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getName(locales[i]).compareTo(constraint) != 0) {
								return true;
							}
						}
					}
					return false;
				}
			}
			else if (propertyName.equals("@description")) {
				if (operator.equals(ISearchFilterRelationExpression.EqualTo)) {
					Locale[] locales = getAvailableDescriptionLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getDescription(locales[i]).compareTo(constraint) == 0) {
								return true;
							}
						}
					}
					return false;
				}
				else if (operator.equals(ISearchFilterRelationExpression.NotEqual)) {
					Locale[] locales = getAvailableDescriptionLocales();
					if (locales != null) {
						for (int i = 0; i < locales.length; i++) {
							if (getDescription(locales[i]).compareTo(constraint) != 0) {
								return true;
							}
						}
					}
					return false;
				}
			}
		}
		}
		return false;
	}
	
	public String getObjectType() {
		String objectType = "unknown";
		if (getObjectID().indexOf(Consts.ACCOUNT_OBJECT_ID_PREFIX) == 0) {
			objectType = "account";
		}
		else if (getObjectID().indexOf(Consts.GROUP_OBJECT_ID_PREFIX) == 0) {
			objectType = "group";
		}
		else if (getObjectID().indexOf(Consts.ROLE_OBJECT_ID_PREFIX) == 0) {
			objectType = "role";
		}
		else if (getObjectID().indexOf(Consts.NAMESPACE_OBJECT_ID_PREFIX) == 0) {
			objectType = "namespace";
		}
		else if (getObjectID().indexOf(Consts.FOLDER_OBJECT_ID_PREFIX) == 0) {
			objectType = "namespaceFolder";
		}
		
		return objectType;
	}

}
