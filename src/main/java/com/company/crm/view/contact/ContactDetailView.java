package com.company.crm.view.contact;

import com.company.crm.entity.Contact;
import com.company.crm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "contacts/:id", layout = MainView.class)
@ViewController(id = "Contact.detail")
@ViewDescriptor(path = "contact-detail-view.xml")
@EditedEntityContainer("contactDc")
public class ContactDetailView extends StandardDetailView<Contact> {
}