package com.whis.base.core;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ModelMapper<T> implements RowMapper<T> {

    private Class<T> clazz;

    public ModelMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        Map<String, Object> rowData = new HashMap<String, Object>(columns);
        for (int i = 1; i <= columns; ++i) {
            rowData.put(md.getColumnName(i), rs.getObject(i));
        }

        return Core.M().convertMapToModel(rowData, clazz);
    }
}
