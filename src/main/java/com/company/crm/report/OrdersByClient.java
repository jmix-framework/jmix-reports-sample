package com.company.crm.report;

import com.company.crm.entity.Client;
import com.company.crm.entity.Order;
import com.company.crm.entity.OrderStatus;
import com.company.crm.security.FullAccessRole;
import com.company.crm.view.client.ClientListView;
import io.jmix.core.DataManager;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        code = "orders-by-client",
        group = DesignTimeReportsGroup.class,
        name = "Orders by Client with Status Grouping",
        uuid = "c8a32999-af6a-45c6-8704-2ccdcac6c953"
)
@AvailableForRoles(roleClasses = FullAccessRole.class)
@AvailableInViews(viewClasses = ClientListView.class)

@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "com/company/crm/reports/orders-by-client/orders-by-client-template.xlsx",
        outputType = ReportOutputType.XLSX,
        outputNamePattern = "orders-by-client.xlsx"
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
        name = "Client",
        parent = "Root",
        dataSets = @DataSetDef(name = "client", type = DataSetType.DELEGATE)
)

@BandDef(
        name = "OrderStatus",
        parent = "Client",
        dataSets = @DataSetDef(name = "orderStatus", type = DataSetType.DELEGATE)
)

@BandDef(
        name = "Order",
        parent = "OrderStatus",
        dataSets = @DataSetDef(name = "order", type = DataSetType.DELEGATE)
)

@BandDef(
        name = "ClientTotal",
        parent = "Client",
        dataSets = @DataSetDef(name = "clientTotal", type = DataSetType.DELEGATE)
)

@BandDef(
        name = "GrandTotal",
        parent = "Root",
        dataSets = @DataSetDef(name = "grandTotal", type = DataSetType.DELEGATE)
)

public class OrdersByClient {

    private final DataManager dataManager;

    private final ThreadLocal<BigDecimal> runningClientTotal = ThreadLocal.withInitial(() -> BigDecimal.ZERO);
    private final ThreadLocal<BigDecimal> runningGrandTotal = ThreadLocal.withInitial(() -> BigDecimal.ZERO);

    public OrdersByClient(DataManager dataManager) {
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

    @DataSetDelegate(name = "client")
    public ReportDataLoader clientDataLoader() {
        return (reportQuery, parentBand, params) -> {
            List<Client> clients = dataManager.load(Client.class)
                    .query("""
                            select c from Client c
                            where exists (select 1 from Order_ o where o.client.id = c.id and o.date >= :dateFrom and o.date <= :dateTo)
                            order by c.name""")
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list();
            return clients.stream()
                    .map(client -> Map.of("id", client.getId(), "name", (Object) client.getName()))
                    .toList();
        };
    }

    @DataSetDelegate(name = "orderStatus")
    public ReportDataLoader orderStatusDataLoader() {
        return (reportQuery, parentBand, params) -> {
            return dataManager.loadValue("""
                                    select o.status from Order_ o
                                    where o.client.id = :clientId and o.date >= :dateFrom and o.date <= :dateTo
                                    group by o.status order by o.status""",
                            Integer.class)
                    .parameter("clientId", parentBand.getData().get("id"))
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list()
                    .stream()
                    .map(statusId -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("statusId", statusId);
                        map.put("status", OrderStatus.fromId(statusId));
                        return map;
                    })
                    .toList();
        };
    }

    @DataSetDelegate(name = "order")
    public ReportDataLoader orderDataLoader() {
        return (reportQuery, parentBand, params) -> {
            return dataManager.load(Order.class)
                    .query("""
                            select o from Order_ o
                            where o.client.id = :clientId and o.status = :status and o.date >= :dateFrom and o.date <= :dateTo
                            order by o.date""")
                    .parameter("clientId", parentBand.getParentBand().getData().get("id"))
                    .parameter("status", parentBand.getData().get("statusId"))
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list()
                    .stream()
                    .map(order -> {
                        runningClientTotal.set(runningClientTotal.get().add(order.getTotal()));
                        Map<String, Object> map = new HashMap<>();
                        map.put("order", order);
                        map.put("date", order.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        map.put("quote", order.getQuote());
                        map.put("comment", order.getComment());
                        map.put("total", order.getTotal());
                        return map;
                    })
                    .toList();
        };
    }

    @DataSetDelegate(name = "clientTotal")
    public ReportDataLoader clientTotalDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("clientTotal", runningClientTotal.get());

            runningGrandTotal.set(runningGrandTotal.get().add(runningClientTotal.get()));
            runningClientTotal.set(BigDecimal.ZERO);

            return List.of(map);
        };
    }

    @DataSetDelegate(name = "grandTotal")
    public ReportDataLoader grandTotalDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("grandTotal", runningGrandTotal.get());

            runningGrandTotal.set(BigDecimal.ZERO);

            return List.of(map);
        };
    }
}