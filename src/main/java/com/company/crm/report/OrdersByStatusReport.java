package com.company.crm.report;

import com.company.crm.entity.OrderStatus;
import com.company.crm.security.FullAccessRole;
import com.company.crm.security.ManagerRole;
import com.company.crm.view.client.ClientDetailView;
import com.company.crm.view.client.ClientListView;
import com.company.crm.view.order.OrderListView;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        code = "orders-by-status",
        group = DesignTimeReportsGroup.class,
        name = "Orders by Status",
        description = "Tabular report with a chart",
        uuid = "dd0f6c44-c7c7-4374-a497-3b759b98921d"
)

@AvailableForRoles(roleClasses = {FullAccessRole.class, ManagerRole.class})

@AvailableInViews(viewClasses = {ClientListView.class, OrderListView.class})

@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "com/company/crm/report/orders-by-status-report.xlsx",
        outputType = ReportOutputType.XLSX,
        outputNamePattern = "orders-by-status.xlsx"
)

@InputParameterDef(
        alias = "dateFrom",
        name = "From",
        type = ParameterType.DATE
)

@InputParameterDef(
        alias = "dateTo",
        name = "To",
        type = ParameterType.DATE,
        defaultDateIsCurrent = true
)

@BandDef(
        name = "Root",
        root = true
)

@BandDef(
        name = "Header",
        parent = "Root",
        dataSets = @DataSetDef(name = "header", type = DataSetType.DELEGATE)
)

// tag::order-status-band[]
@BandDef(
        name = "OrderStatus",
        parent = "Root",
        dataSets = @DataSetDef(name = "orderStatus", type = DataSetType.DELEGATE)
)
// end::order-status-band[]

// tag::chart-band[]
@BandDef(
        name = "Chart",
        parent = "Root"
)
// end::chart-band[]
// tag::report-class[]
public class OrdersByStatusReport {
    // end::report-class[]

    // tag::order-status-band[]

    @Autowired
    private DataManager dataManager;

    // end::order-status-band[]

    @DataSetDelegate(name = "header")
    public ReportDataLoader headerDataLoader() {
        return (reportQuery, parentBand, params) ->
                List.of(
                        Map.of(
                                "dateFrom", ReportUtils.formatDateTime(params.get("dateFrom"), "yyyy-MM-dd"),
                                "dateTo", ReportUtils.formatDateTime(params.get("dateTo"), "yyyy-MM-dd"),
                                "generatedAt", ReportUtils.formatDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss")
                        )
                );
    }

    // tag::order-status-band[]
    @DataSetDelegate(name = "orderStatus")
    public ReportDataLoader orderStatusDataLoader() {
        return (reportQuery, parentBand, params) -> {
            List<KeyValueEntity> keyValueEntities = dataManager.loadValues("""
                            select o.status, count(o), sum(o.total) from Order_ o
                            where (:dateFrom is null or o.date >= :dateFrom) and
                                (:dateTo is null or o.date <= :dateTo)
                            group by o.status order by o.status""")
                    .properties("statusId", "count", "total")
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list();
            return keyValueEntities.stream()
                    .map(kve -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("status", OrderStatus.fromId(kve.getValue("statusId")));
                        map.put("count", kve.getValue("count"));
                        map.put("total", kve.getValue("total"));
                        return map;
                    })
                    .toList();
        };
    }
    // end::order-status-band[]
    // tag::report-class[]
}
// end::report-class[]