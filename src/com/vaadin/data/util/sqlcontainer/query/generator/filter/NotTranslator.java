/*
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.data.util.sqlcontainer.query.generator.filter;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.sqlcontainer.query.generator.StatementHelper;

public class NotTranslator implements FilterTranslator {

    public boolean translatesFilter(Filter filter) {
        return filter instanceof Not;
    }

    public String getWhereStringForFilter(Filter filter, StatementHelper sh) {
        Not not = (Not) filter;
        if (not.getFilter() instanceof IsNull) {
            IsNull in = (IsNull) not.getFilter();
            return QueryBuilder.quote(in.getPropertyId()) + " IS NOT NULL";
        }
        return "NOT "
                + QueryBuilder.getWhereStringForFilter(not.getFilter(), sh);
    }

}
