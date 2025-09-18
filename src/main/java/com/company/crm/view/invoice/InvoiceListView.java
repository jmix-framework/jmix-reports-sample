package com.company.crm.view.invoice;

import com.company.crm.entity.Invoice;
import com.company.crm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "invoices", layout = MainView.class)
@ViewController(id = "Invoice.list")
@ViewDescriptor(path = "invoice-list-view.xml")
@LookupComponent("invoicesDataGrid")
@DialogMode(width = "64em")
public class InvoiceListView extends StandardListView<Invoice> {
}