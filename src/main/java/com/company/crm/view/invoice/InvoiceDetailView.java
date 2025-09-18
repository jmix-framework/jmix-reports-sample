package com.company.crm.view.invoice;

import com.company.crm.entity.Invoice;
import com.company.crm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "invoices/:id", layout = MainView.class)
@ViewController(id = "Invoice.detail")
@ViewDescriptor(path = "invoice-detail-view.xml")
@EditedEntityContainer("invoiceDc")
public class InvoiceDetailView extends StandardDetailView<Invoice> {
}