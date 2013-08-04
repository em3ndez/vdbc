package org.indp.vdbc.ui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 */
public class ResultSetTable extends VerticalLayout {
    private static final Logger LOG = LoggerFactory.getLogger(ResultSetTable.class);
    private Table table;

    public ResultSetTable(ResultSet resultSet) {
        this(resultSet, null);
    }

    public ResultSetTable(ResultSet resultSet, List<String> columns) {
        Component content;
        try {
            table = createResultsTable(resultSet, columns);
            content = table;
        } catch (Exception ex) {
            content = new Label("Failed to process supplied result set: " + ex.getMessage());
        }

        setSizeFull();
        addComponent(content);
    }

    public Collection<?> getItemIds() {
        return null != table ? table.getItemIds() : Collections.emptyList();
    }

    protected Table createResultsTable(ResultSet rs, List<String> columns) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        Container c = new IndexedContainer();
        for (int i = 0; i < md.getColumnCount(); ++i) {
            String label = md.getColumnLabel(i + 1);
            if (null == columns || columns.contains(label)) {
                Class<?> clazz = getClassForSqlType(md.getColumnClassName(i + 1));
                c.addContainerProperty(label, clazz, null);
            }
        }

        int i = 0;
        while (rs.next()) {
            Item item = c.addItem(i++);
            for (Object propertyId : c.getContainerPropertyIds()) {
                item.getItemProperty(propertyId).setValue(rs.getObject(propertyId.toString()));
            }
        }

        Table t = new Table(null, c);
        t.setSizeFull();
        t.setPageLength(50);
        t.setColumnReorderingAllowed(true);
        t.setColumnCollapsingAllowed(true);
        t.setSelectable(true);
        t.setNullSelectionAllowed(false);
        return t;
    }

    protected Class<?> getClassForSqlType(String name) {
        try {
            return Class.forName(name);
        } catch (Exception ex) {
            LOG.warn("unable to get class for name " + name, ex);
            return null;
        }
    }
}
