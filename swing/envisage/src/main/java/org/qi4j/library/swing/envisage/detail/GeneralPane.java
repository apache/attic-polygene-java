/*  Copyright 2009 Tonny Kohar.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.envisage.detail;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.util.TableData;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;

/**
 * Implementation of General DetailPane
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class GeneralPane extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JTable table;
    private GeneralTableModel tableModel;

    public GeneralPane()
    {
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        tableModel = new GeneralTableModel();
        table.setModel( tableModel );

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn( 0 ).setPreferredWidth( 25 );
        columnModel.getColumn( 1 ).setPreferredWidth( 400 );
    }

    protected void clear()
    {
        tableModel.clear();
    }

    public void setDescriptor( Object objectDesciptor )
    {
        clear();
        tableModel.reload( objectDesciptor );
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout( 0, 0 ) );
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add( scrollPane1, BorderLayout.CENTER );
        table = new JTable();
        scrollPane1.setViewportView( table );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    public class GeneralTableModel extends AbstractTableModel
    {
        /**
         * the column names for this model
         */
        protected String[] columnNames = { bundle.getString( "Name.Column" ), bundle.getString( "Value.Column" ) };
        protected ArrayList<TableData> data;

        protected String nameRow = "name";
        protected String classRow = "class";
        protected String visibilityRow = "visibility";


        public GeneralTableModel()
        {
            data = new ArrayList<TableData>();
        }

        public void reload( Object objectDesciptor )
        {
            if( objectDesciptor instanceof ServiceDetailDescriptor )
            {
                ServiceDescriptor descriptor = ( (ServiceDetailDescriptor) objectDesciptor ).descriptor();
                data.add( new TableData( 2, new Object[]{ nameRow, descriptor.identity() } ) );
                data.add( new TableData( 2, new Object[]{ classRow, descriptor.type().getName() } ) );
                data.add( new TableData( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
            }
            else if( objectDesciptor instanceof EntityDetailDescriptor )
            {
                EntityDescriptor descriptor = ( (EntityDetailDescriptor) objectDesciptor ).descriptor();
                data.add( new TableData( 2, new Object[]{ nameRow, descriptor.type().getSimpleName() } ) );
                data.add( new TableData( 2, new Object[]{ classRow, descriptor.type().getName() } ) );
                data.add( new TableData( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
            }
            else if( objectDesciptor instanceof ObjectDetailDescriptor )
            {
                ObjectDescriptor descriptor = ( (ObjectDetailDescriptor) objectDesciptor ).descriptor();
                data.add( new TableData( 2, new Object[]{ nameRow, descriptor.type().getSimpleName() } ) );
                data.add( new TableData( 2, new Object[]{ classRow, descriptor.type().getName() } ) );
                data.add( new TableData( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
            }

            fireTableDataChanged();
        }

        public Object getValueAt( int rowIndex, int columnIndex )
        {
            TableData row = data.get( rowIndex );
            return row.get( columnIndex );
        }

        public void clear()
        {
            data.clear();
            fireTableDataChanged();
        }

        public int getColumnCount()
        {
            return columnNames.length;
        }

        public String getColumnName( int col )
        {
            return columnNames[ col ];
        }

        public int getRowCount()
        {
            return data.size();
        }
    }
}

