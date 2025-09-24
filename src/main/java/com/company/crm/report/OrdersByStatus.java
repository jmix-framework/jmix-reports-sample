package com.company.crm.report;

import com.company.crm.entity.OrderStatus;
import com.company.crm.security.FullAccessRole;
import com.company.crm.view.client.ClientDetailView;
import io.jmix.core.DataManager;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        code = "orders-by-status",
        group = DesignTimeReportsGroup.class,
        name = "Orders by Status",
        description = "Sales Pipeline Snapshot",
        uuid = "dd0f6c44-c7c7-4374-a497-3b759b98921d"
)
@AvailableForRoles(roleClasses = FullAccessRole.class)
@AvailableInViews(viewClasses = ClientDetailView.class)

@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "com/company/crm/reports/orders-by-status/orders-by-status-template.xlsx",
        outputType = ReportOutputType.XLSX,
        outputNamePattern = "orders-by-status.xlsx"
)

@InputParameterDef(
        alias = "dateFrom",
        name = "From",
        type = ParameterType.DATE,
        required = true
)

@InputParameterDef(
        alias = "dateTo",
        name = "To",
        type = ParameterType.DATE,
        required = true,
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

@BandDef(
        name = "OrderStatus",
        parent = "Root",
        dataSets = @DataSetDef(name = "orderStatus", type = DataSetType.DELEGATE)
)

@BandDef(
        name = "Chart",
        parent = "Root"
)

public class OrdersByStatus {

    private final DataManager dataManager;

    public OrdersByStatus(DataManager dataManager) {
        this.dataManager = dataManager;
    }

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

    @DataSetDelegate(name = "orderStatus")
    public ReportDataLoader orderStatusDataLoader() {
        return (reportQuery, parentBand, params) -> {
            return dataManager.loadValues("""
                                    select o.status, count(o), sum(o.total) from Order_ o
                                    where o.date >= :dateFrom and o.date <= :dateTo
                                    group by o.status order by o.status""")
                    .properties("statusId", "count", "total")
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list()
                    .stream()
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

}