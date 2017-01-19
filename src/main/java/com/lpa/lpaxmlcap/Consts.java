package com.lpa.lpaxmlcap;

import com.cognos.CAM_AAA.authentication.ISearchFilter;
import com.cognos.CAM_AAA.authentication.ISearchStep;
import com.cognos.CAM_AAA.authentication.ISortProperty;

public class Consts {
	public static final String VERSION = "1";

	public static final String ACCOUNT_OBJECT_ID_PREFIX = "account:";
	public static final String FOLDER_OBJECT_ID_PREFIX = "folder:";
	public static final String GROUP_OBJECT_ID_PREFIX = "group:";
	public static final String ROLE_OBJECT_ID_PREFIX = "role:";
	public static final String NAMESPACE_OBJECT_ID_PREFIX = "namespace:";

	public static final String FOLDER_NAME_ROLES = "Roles";
	public static final String FOLDER_NAME_GROUPS = "Groups";
	public static final String FOLDER_NAME_USERS = "Users";

	
	public static final String decodeSearchAxis(int searchAxis) {
		if (searchAxis == ISearchStep.SearchAxis.Ancestor)
			return Integer.toString(ISearchStep.SearchAxis.Ancestor) + " - Ancestor";
		if (searchAxis == ISearchStep.SearchAxis.AncestorOrSelf)
			return Integer.toString(ISearchStep.SearchAxis.AncestorOrSelf) + " - AncestorOrSelf";
		if (searchAxis == ISearchStep.SearchAxis.Child)
			return Integer.toString(ISearchStep.SearchAxis.Child) + " - Child";
		if (searchAxis == ISearchStep.SearchAxis.Descendent)
			return Integer.toString(ISearchStep.SearchAxis.Descendent) + " - Descendent";
		if (searchAxis == ISearchStep.SearchAxis.DescendentOrSelf)
			return Integer.toString(ISearchStep.SearchAxis.DescendentOrSelf) + " - DescendentOrSelf";
		if (searchAxis == ISearchStep.SearchAxis.Parent)
			return Integer.toString(ISearchStep.SearchAxis.Parent) + " - Parent";
		if (searchAxis == ISearchStep.SearchAxis.Self)
			return Integer.toString(ISearchStep.SearchAxis.Self) + " - Self";
		if (searchAxis == ISearchStep.SearchAxis.Unknown)
			return Integer.toString(ISearchStep.SearchAxis.Unknown) + " - Unknown";
		return Integer.toString(searchAxis);
	}

	public static final String decodeSearchFilterType(int searchFilterType) {
		if (searchFilterType == ISearchFilter.ConditionalExpression)
			return Integer.toString(ISearchFilter.ConditionalExpression) + " - Conditional Expression";
		if (searchFilterType == ISearchFilter.FunctionCall)
			return Integer.toString(ISearchFilter.FunctionCall) + " - Function Call";
		if (searchFilterType == ISearchFilter.RelationalExpression)
			return Integer.toString(ISearchFilter.RelationalExpression) + " - Relational Expression";
		return Integer.toString(searchFilterType);
	}

	public static final String decodeSortOrder(int sortOrder) {
		if (sortOrder == ISortProperty.SortOrderAscending)
			return Integer.toString(ISortProperty.SortOrderAscending) + " - Ascending";
		if (sortOrder == ISortProperty.SortOrderDescending)
			return Integer.toString(ISortProperty.SortOrderDescending) + " - Descending";
		if (sortOrder == ISortProperty.SortOrderUnknown)
			return Integer.toString(ISortProperty.SortOrderUnknown) + " - Unknown";
		return Integer.toString(sortOrder);
	}
}
