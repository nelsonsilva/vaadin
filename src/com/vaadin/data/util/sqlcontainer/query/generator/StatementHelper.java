/*
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.data.util.sqlcontainer.query.generator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StatementHelper is a simple helper class that assists TableQuery and the
 * query generators in filling a PreparedStatement. The actual statement is
 * generated by the query generator methods, but the resulting statement and all
 * the parameter values are stored in an instance of StatementHelper.
 * 
 * This class will also fill the values with correct setters into the
 * PreparedStatement on request.
 */
public class StatementHelper implements Serializable {

    private String queryString;

    private List<Object> parameters = new ArrayList<Object>();
    private Map<Integer, Class<?>> dataTypes = new HashMap<Integer, Class<?>>();

    public StatementHelper() {
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

    public void addParameterValue(Object parameter) {
        if (parameter != null) {
            parameters.add(parameter);
            dataTypes.put(parameters.size() - 1, parameter.getClass());
        }
    }

    public void addParameterValue(Object parameter, Class<?> type) {
        parameters.add(parameter);
        dataTypes.put(parameters.size() - 1, type);
    }

    public void setParameterValuesToStatement(PreparedStatement pstmt)
            throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i) == null) {
                handleNullValue(i, pstmt);
            } else {
                pstmt.setObject(i + 1, parameters.get(i));
            }
        }

        /*
         * The following list contains the data types supported by
         * PreparedStatement but not supported by SQLContainer:
         * 
         * [The list is provided as PreparedStatement method signatures]
         * 
         * setNCharacterStream(int parameterIndex, Reader value)
         * 
         * setNClob(int parameterIndex, NClob value)
         * 
         * setNString(int parameterIndex, String value)
         * 
         * setRef(int parameterIndex, Ref x)
         * 
         * setRowId(int parameterIndex, RowId x)
         * 
         * setSQLXML(int parameterIndex, SQLXML xmlObject)
         * 
         * setBytes(int parameterIndex, byte[] x)
         * 
         * setCharacterStream(int parameterIndex, Reader reader)
         * 
         * setClob(int parameterIndex, Clob x)
         * 
         * setURL(int parameterIndex, URL x)
         * 
         * setArray(int parameterIndex, Array x)
         * 
         * setAsciiStream(int parameterIndex, InputStream x)
         * 
         * setBinaryStream(int parameterIndex, InputStream x)
         * 
         * setBlob(int parameterIndex, Blob x)
         */
    }

    private void handleNullValue(int i, PreparedStatement pstmt)
            throws SQLException {
        if (BigDecimal.class.equals(dataTypes.get(i))) {
            pstmt.setBigDecimal(i + 1, null);
        } else if (Boolean.class.equals(dataTypes.get(i))) {
            pstmt.setNull(i + 1, Types.BOOLEAN);
        } else if (Byte.class.equals(dataTypes.get(i))) {
            pstmt.setNull(i + 1, Types.SMALLINT);
        } else if (Date.class.equals(dataTypes.get(i))) {
            pstmt.setDate(i + 1, null);
        } else if (Double.class.equals(dataTypes.get(i))) {
            pstmt.setNull(i + 1, Types.DOUBLE);
        } else if (Float.class.equals(dataTypes.get(i))) {
            pstmt.setNull(i + 1, Types.FLOAT);
        } else if (Integer.class.equals(dataTypes.get(i))) {
            pstmt.setNull(i + 1, Types.INTEGER);
        } else if (Long.class.equals(dataTypes.get(i))) {
            pstmt.setNull(i + 1, Types.BIGINT);
        } else if (Short.class.equals(dataTypes.get(i))) {
            pstmt.setNull(i + 1, Types.SMALLINT);
        } else if (String.class.equals(dataTypes.get(i))) {
            pstmt.setString(i + 1, null);
        } else if (Time.class.equals(dataTypes.get(i))) {
            pstmt.setTime(i + 1, null);
        } else if (Timestamp.class.equals(dataTypes.get(i))) {
            pstmt.setTimestamp(i + 1, null);
        } else {
            throw new SQLException("Data type not supported by SQLContainer: "
                    + parameters.get(i).getClass().toString());
        }
    }
}
