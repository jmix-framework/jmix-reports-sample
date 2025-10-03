package com.company.crm.report;

import com.company.crm.entity.Client;
import com.company.crm.entity.Order;
import com.company.crm.entity.OrderStatus;
import com.company.crm.security.FullAccessRole;
import com.company.crm.security.ManagerRole;
import com.company.crm.view.client.ClientListView;
import com.company.crm.view.order.OrderListView;
import io.jmix.core.DataManager;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        code = "orders-by-client",
        group = DesignTimeReportsGroup.class,
        name = "Orders by Client",
        description = "Multi-level tabular report with subtotals",
        uuid = "c8a32999-af6a-45c6-8704-2ccdcac6c953"
)

@AvailableForRoles(roleClasses = {FullAccessRole.class, ManagerRole.class})

@AvailableInViews(viewClasses = {ClientListView.class, OrderListView.class})

@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "com/company/crm/report/orders-by-client-report.xlsx",
        outputType = ReportOutputType.XLSX,
        outputNamePattern = "orders-by-client.xlsx"
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
// tag::report-class[]
public class OrdersByClientReport {
// end::report-class[]

    // tag::data-loading[]
    @Autowired
    private DataManager dataManager;

    private final ThreadLocal<BigDecimal> runningClientTotal =
            ThreadLocal.withInitial(() -> BigDecimal.ZERO); // <1>
    private final ThreadLocal<BigDecimal> runningGrandTotal =
            ThreadLocal.withInitial(() -> BigDecimal.ZERO); // <1>

    // end::data-loading[]

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

    // tag::data-loading[]
    @DataSetDelegate(name = "client")
    public ReportDataLoader clientDataLoader() {
        return (reportQuery, parentBand, params) -> {

            // Initialize thread locals on report start
            runningClientTotal.set(BigDecimal.ZERO);
            runningGrandTotal.set(BigDecimal.ZERO);

            List<Client> clients = dataManager.load(Client.class) // <2>
                    .query("""
                            select c from Client c
                            where exists (
                                            select 1 from Order_ o where o.client.id = c.id and
                                                (:dateFrom is null or o.date >= :dateFrom) and
                                                (:dateTo is null or o.date <= :dateTo)
                                         )
                            order by c.name""")
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list();
            return clients.stream()
                    .map(client -> Map.of("id", client.getId(), "name", (Object) client.getName()))
                    .toList();
        };
    }
    // end::data-loading[]

    @DataSetDelegate(name = "orderStatus")
    public ReportDataLoader orderStatusDataLoader() {
        return (reportQuery, parentBand, params) -> {
            List<Integer> statusIdList = dataManager.loadValue("""
                                    select o.status from Order_ o
                                    where o.client.id = :clientId and
                                        (:dateFrom is null or o.date >= :dateFrom) and
                                        (:dateTo is null or o.date <= :dateTo)
                                    group by o.status order by o.status""",
                            Integer.class)
                    .parameter("clientId", parentBand.getData().get("id"))
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list();
            return statusIdList.stream()
                    .map(statusId -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("statusId", statusId);
                        map.put("status", OrderStatus.fromId(statusId));
                        return map;
                    })
                    .toList();
        };
    }

    // tag::data-loading[]
    @DataSetDelegate(name = "order")
    public ReportDataLoader orderDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Object clientId = parentBand.getParentBand().getData().get("id");  // <3>
            Object statusId = parentBand.getData().get("statusId");
            List<Order> orderList = dataManager.load(Order.class)
                    .query("""
                            select o from Order_ o
                            where o.client.id = :clientId and o.status = :status and
                                (:dateFrom is null or o.date >= :dateFrom) and
                                (:dateTo is null or o.date <= :dateTo)
                            order by o.date""")
                    .parameter("clientId", clientId)
                    .parameter("status", statusId)
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list();
            return orderList.stream()
                    .map(order -> {
                        runningClientTotal.set(
                                runningClientTotal.get().add(order.getTotal())
                        ); // <4>
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
            map.put("clientTotal", runningClientTotal.get()); // <5>

            runningGrandTotal.set(
                    runningGrandTotal.get().add(runningClientTotal.get())
            ); // <6>
            runningClientTotal.set(BigDecimal.ZERO);

            return List.of(map);
        };
    }

    @DataSetDelegate(name = "grandTotal")
    public ReportDataLoader grandTotalDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("grandTotal", runningGrandTotal.get()); // <7>

            // Clean up thread locals on report finish
            runningClientTotal.remove();
            runningGrandTotal.remove();

            return List.of(map);
        };
    }
    // end::data-loading[]
// tag::report-class[]
}
// end::report-class[]