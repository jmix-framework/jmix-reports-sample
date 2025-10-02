package com.company.crm.report;

import com.company.crm.entity.Client;
import com.company.crm.entity.Contact;
import com.company.crm.security.EmployeeRole;
import com.company.crm.security.FullAccessRole;
import com.company.crm.security.ManagerRole;
import com.company.crm.view.client.ClientDetailView;
import com.company.crm.view.client.ClientListView;
import io.jmix.core.MetadataTools;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

// tag::report-def[]
@ReportDef(
        code = "client-profile", // <1>
        group = DesignTimeReportsGroup.class, // <2>
        name = "Client Profile", // <3>
        description = "Simple report with different templates and output formats" // <4>
)
// end::report-def[]

@AvailableForRoles(roleClasses = {FullAccessRole.class, EmployeeRole.class, ManagerRole.class})

@AvailableInViews(viewClasses = {ClientDetailView.class, ClientListView.class})

@TemplateDef(
        isDefault = true,
        code = "HTML → HTML",
        filePath = "com/company/crm/report/client-profile-report.html",
        outputType = ReportOutputType.HTML,
        outputNamePattern = "client-profile.html",
        templateEngine = TemplateMarkupEngine.FREEMARKER
)

@TemplateDef(
        code = "DOCX → DOCX",
        filePath = "com/company/crm/report/client-profile-report.docx",
        outputType = ReportOutputType.DOCX,
        outputNamePattern = "client-profile.docx"
)

@TemplateDef(
        code = "DOCX → PDF",
        filePath = "com/company/crm/report/client-profile-report.docx",
        outputType = ReportOutputType.PDF,
        outputNamePattern = "client-profile.pdf"
)

@TemplateDef(
        code = "JRXML → PDF",
        filePath = "com/company/crm/report/client-profile-report.jrxml",
        outputType = ReportOutputType.PDF,
        outputNamePattern = "client-profile.pdf"
)

@InputParameterDef(
        alias = "client",
        name = "msg://com.company.crm.entity/Client",
        type = ParameterType.ENTITY,
        required = true,
        entity = @EntityParameterDef(entityClass = Client.class)
)

@BandDef(
        name = "Root",
        root = true,
        dataSets = @DataSetDef(name = "root", type = DataSetType.DELEGATE)
)

@BandDef(
        name = "Client",
        parent = "Root",
        dataSets = @DataSetDef(name = "client", type = DataSetType.DELEGATE)
)

@BandDef(
        name = "Contacts",
        parent = "Root",
        dataSets = @DataSetDef(name = "contacts", type = DataSetType.DELEGATE)
)
// tag::report-class[]
public class ClientProfileReport {
// end::report-class[]

    private final MetadataTools metadataTools;

    public ClientProfileReport(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @DataSetDelegate(name = "root")
    public ReportDataLoader rootDataLoader() {
        return (reportQuery, parentBand, params) ->
                List.of(Map.of("generatedAt", ReportUtils.formatDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"))
        );
    }

    @DataSetDelegate(name = "client")
    public ReportDataLoader clientDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Client client = (Client) params.get("client");
            Map<String, Object> fields = new HashMap<>();
            fields.put("name", client.getName());
            fields.put("fullName", client.getFullName());
            fields.put("type", client.getType().name());
            fields.put("address", client.getAddress());
            fields.put("vatNumber", client.getVatNumber());
            fields.put("regNumber", client.getRegNumber());
            fields.put("website", client.getWebsite());
            fields.put("accountManager", client.getAccountManager() == null ? "" : metadataTools.getInstanceName(client.getAccountManager()));
            return List.of(fields);
        };
    }

    @DataSetDelegate(name = "contacts")
    public ReportDataLoader contactsDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Client client = (Client) params.get("client");
            return IntStream.range(0, client.getContacts().size())
                    .mapToObj(i -> {
                        Map<String, Object> fields = new HashMap<>();
                        fields.put("idx", i + 1);
                        Contact contact = client.getContacts().get(i);
                        fields.put("person", contact.getPerson());
                        fields.put("position", contact.getPosition());
                        fields.put("phone", contact.getPhone());
                        fields.put("email", contact.getEmail());
                        return fields;
                    })
                    .toList();
        };
    }
// tag::report-class[]
}
// end::report-class[]