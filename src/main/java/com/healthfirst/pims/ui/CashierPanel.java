package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDao;
import com.healthfirst.pims.dao.SaleDao;
import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.SaleResult;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.FormatUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Cashier workspace supporting product lookup, stock checking, cart building, checkout and a bill. */
public final class CashierPanel extends JPanel {
    private final User cashier; private final MedicineDao medicineDao=new MedicineDao(); private final SaleDao saleDao=new SaleDao();
    private final JTextField search=new JTextField(22); private final JSpinner units=new JSpinner(new SpinnerNumberModel(1,1,999,1));
    private final DefaultTableModel productModel=new DefaultTableModel(new String[]{"ID","Medicine","Type","Price","Available","Expiry"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final DefaultTableModel cartModel=new DefaultTableModel(new String[]{"Medicine","Unit price","Quantity","Line total"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable products=new JTable(productModel), cartTable=new JTable(cartModel); private final JLabel total=new JLabel("Total: R0.00"); private final Map<Integer,CartItem> cart=new LinkedHashMap<>();
    public CashierPanel(User cashier){super(new BorderLayout(10,10));this.cashier=cashier;setBackground(UiStyle.BG);setBorder(BorderFactory.createEmptyBorder(14,14,14,14));build();loadProducts();}
    private void build(){
        JPanel top=new JPanel(new BorderLayout(8,8));top.setBackground(UiStyle.BG);JLabel title=new JLabel("Point of Sale");title.setFont(new Font("SansSerif",Font.BOLD,20));top.add(title,BorderLayout.WEST);JPanel find=new JPanel(new FlowLayout(FlowLayout.RIGHT));find.setBackground(UiStyle.BG);find.add(new JLabel("Search medicine:"));find.add(search);find.add(new JLabel("Quantity:"));find.add(units);top.add(find,BorderLayout.EAST);add(top,BorderLayout.NORTH);
        search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){loadProducts();}public void removeUpdate(DocumentEvent e){loadProducts();}public void changedUpdate(DocumentEvent e){loadProducts();}});
        UiStyle.configureTable(products);UiStyle.configureTable(cartTable);JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,productSide(),cartSide());split.setResizeWeight(.56);split.setDividerLocation(620);add(split,BorderLayout.CENTER);
    }
    private JComponent productSide(){JPanel p=new JPanel(new BorderLayout(7,7));p.setBackground(UiStyle.BG);p.setBorder(BorderFactory.createTitledBorder("Available medicines"));p.add(new JScrollPane(products),BorderLayout.CENTER);JPanel action=new JPanel(new FlowLayout(FlowLayout.RIGHT));action.setBackground(UiStyle.BG);JButton check=new JButton("Check stock"),add=UiStyle.button("Add to cart",UiStyle.GREEN);check.addActionListener(e->checkStock());add.addActionListener(e->addToCart());action.add(check);action.add(add);p.add(action,BorderLayout.SOUTH);return p;}
    private JComponent cartSide(){JPanel p=new JPanel(new BorderLayout(7,7));p.setBackground(UiStyle.BG);p.setBorder(BorderFactory.createTitledBorder("Current sale"));p.add(new JScrollPane(cartTable),BorderLayout.CENTER);JPanel controls=new JPanel(new BorderLayout());controls.setBackground(UiStyle.BG);JPanel edits=new JPanel(new FlowLayout(FlowLayout.LEFT));edits.setBackground(UiStyle.BG);JButton plus=new JButton("+1 selected"),minus=new JButton("−1 selected"),remove=new JButton("Remove selected");plus.addActionListener(e->changeSelected(1));minus.addActionListener(e->changeSelected(-1));remove.addActionListener(e->removeSelected());edits.add(plus);edits.add(minus);edits.add(remove);controls.add(edits,BorderLayout.WEST);total.setFont(new Font("SansSerif",Font.BOLD,18));controls.add(total,BorderLayout.EAST);p.add(controls,BorderLayout.SOUTH);JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT));footer.setBackground(UiStyle.BG);JButton clear=new JButton("Clear cart"),checkout=UiStyle.button("Checkout and generate bill",UiStyle.NAVY);clear.addActionListener(e->clearCart());checkout.addActionListener(e->checkout());footer.add(clear);footer.add(checkout);p.add(footer,BorderLayout.NORTH);return p;}
    private void loadProducts(){try{productModel.setRowCount(0);for(Medicine m:medicineDao.findAll(search.getText()))productModel.addRow(new Object[]{m.id(),m.name(),m.medicineType(),FormatUtil.money(m.price()),m.quantityInStock(),FormatUtil.date(m.expiryDate())});}catch(Exception e){Ui.error(this,e);}}
    private Medicine selectedMedicine() throws Exception {int row=products.getSelectedRow();if(row<0)throw new IllegalArgumentException("Select a medicine from the availability list.");int id=(int)productModel.getValueAt(products.convertRowIndexToModel(row),0);return medicineDao.findById(id).orElseThrow(()->new IllegalArgumentException("Medicine is no longer available."));}
    private void checkStock(){try{Medicine m=selectedMedicine();String status=m.quantityInStock()>0?"Available":"Out of stock";Ui.info(this,m.name()+"\nPrice: "+FormatUtil.money(m.price())+"\nAvailability: "+m.quantityInStock()+" unit(s) — "+status+"\nExpiry: "+FormatUtil.date(m.expiryDate()));}catch(Exception e){Ui.error(this,e);}}
    private void addToCart(){try{Medicine m=selectedMedicine();int requested=(Integer)units.getValue();if(requested>m.quantityInStock())throw new IllegalArgumentException("Only "+m.quantityInStock()+" unit(s) of "+m.name()+" are available.");CartItem existing=cart.get(m.id());int combined=requested+(existing==null?0:existing.quantity());if(combined>m.quantityInStock())throw new IllegalArgumentException("The cart already contains "+(existing==null?0:existing.quantity())+". Only "+m.quantityInStock()+" are available.");if(existing==null)cart.put(m.id(),new CartItem(m,requested));else existing.setQuantity(combined);refreshCart();}catch(Exception e){Ui.error(this,e);}}
    private void changeSelected(int delta){int row=cartTable.getSelectedRow();if(row<0){Ui.info(this,"Select a cart item first.");return;}CartItem item=new ArrayList<>(cart.values()).get(cartTable.convertRowIndexToModel(row));int proposed=item.quantity()+delta;if(proposed<1){cart.remove(item.medicine().id());}else if(proposed>item.medicine().quantityInStock()){Ui.info(this,"Cannot exceed available stock of "+item.medicine().quantityInStock()+".");return;}else item.setQuantity(proposed);refreshCart();}
    private void removeSelected(){int row=cartTable.getSelectedRow();if(row<0){Ui.info(this,"Select a cart item first.");return;}CartItem item=new ArrayList<>(cart.values()).get(cartTable.convertRowIndexToModel(row));cart.remove(item.medicine().id());refreshCart();}
    private void refreshCart(){cartModel.setRowCount(0);BigDecimal sum=BigDecimal.ZERO;for(CartItem item:cart.values()){cartModel.addRow(new Object[]{item.medicine().name(),FormatUtil.money(item.medicine().price()),item.quantity(),FormatUtil.money(item.lineTotal())});sum=sum.add(item.lineTotal());}total.setText("Total: "+FormatUtil.money(sum));}
    private void clearCart(){if(cart.isEmpty()||Ui.confirm(this,"Clear all items from the current cart?")){cart.clear();refreshCart();}}
    private void checkout(){try{SaleResult sale=saleDao.checkout(cashier,List.copyOf(cart.values()));cart.clear();refreshCart();loadProducts();new ReceiptDialog(SwingUtilities.getWindowAncestor(this),sale).setVisible(true);}catch(Exception e){Ui.error(this,e);loadProducts();}}
}
