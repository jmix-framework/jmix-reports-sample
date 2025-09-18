package com.company.crm.view.payment;

import com.company.crm.entity.Payment;
import com.company.crm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "payments", layout = MainView.class)
@ViewController(id = "Payment.list")
@ViewDescriptor(path = "payment-list-view.xml")
@LookupComponent("paymentsDataGrid")
@DialogMode(width = "64em")
public class PaymentListView extends StandardListView<Payment> {
}