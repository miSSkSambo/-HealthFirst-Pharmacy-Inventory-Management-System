package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDao;
import com.healthfirst.pims.dao.SupplierDao;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Supplier;
import com.healthfirst.pims.util.FormatUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Full CRUD panel for the core medicines inventory table. */
public final class MedicinePanel extends JPanel {
    private final MedicineDao medicineDao = new MedicineDao(); private final SupplierDao supplierDao = new SupplierDao();
    private final JTextField search = new JTextField(20), name = new JTextField(16), company = new JTextField(16), type = new JTextField(12), price = new JTextField(10), quantity = new JTextField(8), reorder = new JTextField(8), expiry = new JTextField(10);
    private final JComboBox<Supplier> supplier = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID","Name","Company","Type","Price","In stock","Reorder","Expiry","Supplier"},0) { @Override public boolean isCellEditable(int r,int c){return false;} };
    private final JTable table = new JTable(tableModel); private int selectedId = -1;

    public MedicinePanel() { super(new BorderLayout(10,10)); setBackground(UiStyle.BG); setBorder(BorderFactory.createEmptyBorder(14,14,14,14)); buildUi(); loadSuppliers(); loadTable(); }
    private void buildUi() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT)); toolbar.setBackground(UiStyle.BG); toolbar.add(new JLabel("Find medicine:")); toolbar.add(search); JButton clear = new JButton("Clear search"); clear.addActionListener(e->{search.setText("");}); toolbar.add(clear); add(toolbar,BorderLayout.NORTH);
        search.getDocument().addDocumentListener(new DocumentListener(){ public void insertUpdate(DocumentEvent e){loadTable();} public void removeUpdate(DocumentEvent e){loadTable();} public void changedUpdate(DocumentEvent e){loadTable();} });
        UiStyle.configureTable(table); table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()) populateFromRow();}); add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8,8)); bottom.setBackground(UiStyle.BG); bottom.add(form(),BorderLayout.CENTER); bottom.add(buttons(),BorderLayout.SOUTH); add(bottom,BorderLayout.SOUTH);
    }
    private JPanel form() {
        JPanel p = new JPanel(new GridLayout(2,8,8,4)); p.setBackground(UiStyle.BG);
        addField(p,"Name",name); addField(p,"Company",company); addField(p,"Type",type); addField(p,"Price (R)",price); addField(p,"Quantity",quantity); addField(p,"Reorder level",reorder); addField(p,"Expiry (yyyy-mm-dd)",expiry); addField(p,"Supplier",supplier); return p;
    }
    private void addField(JPanel p, String label, JComponent field) { JPanel wrap=new JPanel(new BorderLayout(2,2));wrap.setBackground(UiStyle.BG);wrap.add(new JLabel(label),BorderLayout.NORTH);field.setBorder(UiStyle.fieldBorder());wrap.add(field,BorderLayout.CENTER);p.add(wrap); }
    private JPanel buttons() { JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT));p.setBackground(UiStyle.BG); JButton add=UiStyle.button("Add medicine",UiStyle.GREEN), update=UiStyle.button("Update selected",UiStyle.BLUE), delete=UiStyle.button("Delete selected",UiStyle.RED), clear=new JButton("Clear form"); add.addActionListener(e->save(false));update.addActionListener(e->save(true));delete.addActionListener(e->delete());clear.addActionListener(e->clearForm());p.add(add);p.add(update);p.add(delete);p.add(clear);return p; }
    private void loadSuppliers() { try { supplier.removeAllItems(); for(Supplier s:supplierDao.findAll()) supplier.addItem(s); } catch(SQLException e){ Ui.error(this,e); } }
    private void loadTable() { try { tableModel.setRowCount(0); for(Medicine m:medicineDao.findAll(search.getText())) tableModel.addRow(new Object[]{m.id(),m.name(),m.company(),m.medicineType(),FormatUtil.money(m.price()),m.quantityInStock(),m.reorderLevel(),FormatUtil.date(m.expiryDate()),m.supplierName()}); } catch(SQLException e){Ui.error(this,e);} }
    private void populateFromRow() { int row=table.getSelectedRow();if(row<0)return;int id=(int)tableModel.getValueAt(table.convertRowIndexToModel(row),0);try{Medicine m=medicineDao.findById(id).orElseThrow();selectedId=m.id();name.setText(m.name());company.setText(m.company());type.setText(m.medicineType());price.setText(m.price().toPlainString());quantity.setText(String.valueOf(m.quantityInStock()));reorder.setText(String.valueOf(m.reorderLevel()));expiry.setText(m.expiryDate().toString());for(int i=0;i<supplier.getItemCount();i++)if(supplier.getItemAt(i).id()==m.supplierId())supplier.setSelectedIndex(i);}catch(Exception e){Ui.error(this,e);} }
    private void save(boolean editing) { try { if(editing&&selectedId<0)throw new IllegalArgumentException("Select a medicine row to update."); String n=name.getText().trim(), c=company.getText().trim(), t=type.getText().trim();if(n.isBlank()||c.isBlank()||t.isBlank()||supplier.getSelectedItem()==null)throw new IllegalArgumentException("Name, company, type and supplier are required.");BigDecimal p=new BigDecimal(price.getText().trim());int q=Integer.parseInt(quantity.getText().trim()), r=Integer.parseInt(reorder.getText().trim());LocalDate d=LocalDate.parse(expiry.getText().trim());if(p.signum()<0||q<0||r<0)throw new IllegalArgumentException("Price, quantity and reorder level cannot be negative.");if(editing)medicineDao.update(selectedId,n,c,t,p,q,r,d,((Supplier)supplier.getSelectedItem()).id());else medicineDao.create(n,c,t,p,q,r,d,((Supplier)supplier.getSelectedItem()).id());Ui.info(this,editing?"Medicine updated.":"Medicine added.");clearForm();loadTable(); } catch(DateTimeParseException e){Ui.error(this,new IllegalArgumentException("Expiry date must be yyyy-mm-dd."));}catch(Exception e){Ui.error(this,e);} }
    private void delete(){try{if(selectedId<0)throw new IllegalArgumentException("Select a medicine row to delete.");if(Ui.confirm(this,"Delete the selected medicine? This is not possible when it has sales history.")){medicineDao.delete(selectedId);clearForm();loadTable();Ui.info(this,"Medicine deleted.");}}catch(Exception e){Ui.error(this,e);}}
    private void clearForm(){selectedId=-1;table.clearSelection();name.setText("");company.setText("");type.setText("");price.setText("");quantity.setText("");reorder.setText("");expiry.setText("");if(supplier.getItemCount()>0)supplier.setSelectedIndex(0);}
}
