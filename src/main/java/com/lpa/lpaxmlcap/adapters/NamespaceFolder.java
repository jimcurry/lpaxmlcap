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

import com.cognos.CAM_AAA.authentication.INamespaceFolder;


public class NamespaceFolder extends UiClass implements INamespaceFolder
{
	/**
	 * @param theSearchPath
	 */
	public NamespaceFolder(String theSearchPath)
	{
		super(theSearchPath);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IBaseClass#getHasChildren()
	 */
	public boolean getHasChildren()
	{
		return true;
	}
}
