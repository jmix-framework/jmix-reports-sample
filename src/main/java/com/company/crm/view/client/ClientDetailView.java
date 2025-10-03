package com.company.crm.view.client;

import com.company.crm.entity.Client;
import com.company.crm.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "clients/:id", layout = MainView.class)
@ViewController(id = "Client.detail")
@ViewDescriptor(path = "client-detail-view.xml")
@EditedEntityContainer("clientDc")
// tag::report-actions[]
public class ClientDetailView extends StandardDetailView<Client> {

    @Autowired
    private UiReportRunner uiReportRunner; // <1>

    @Subscribe(id = "printClientProfileButton", subject = "clickListener")
    public void onPrintClientProfileButtonClick(final ClickEvent<JmixButton> event) {
        uiReportRunner.byReportCode("client-profile")
                .addParam("client", getEditedEntity())
                .withTemplateCode("HTML → HTML")
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();
    }
}
// end::report-actions[]