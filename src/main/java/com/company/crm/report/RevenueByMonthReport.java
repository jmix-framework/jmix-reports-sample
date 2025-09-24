package com.company.crm.report;

import com.company.crm.entity.Client;
import com.company.crm.security.FullAccessRole;
import com.company.crm.view.client.ClientDetailView;
import io.jmix.core.DataManager;
import io.jmix.reports.annotation.*;
import io.jmix.reports.delegate.ParameterValidator;
import io.jmix.reports.delegate.ParametersCrossValidator;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

import static com.company.crm.report.ReportUtils.formatDateTime;
import static com.company.crm.report.ReportUtils.getParam;

@ReportDef(
        code = "revenue-by-month",
        group = DesignTimeReportsGroup.class,
        name = "Revenue by Month",
        description = "Cross-tab report",
        uuid = "c27da027-599f-4992-9a49-a4f2d187ecad"
)

@AvailableForRoles(roleClasses = FullAccessRole.class)

@AvailableInViews(viewClasses = ClientDetailView.class)

@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "com/company/crm/report/revenue-by-month-report.xlsx",
        outputType = ReportOutputType.XLSX,
        outputNamePattern = "revenue-by-month.xlsx"
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
        required = true
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
        name = "CrossTabHeader",
        parent = "Root"
)

@BandDef(
        name = "Revenue",
        parent = "Root",
        orientation = Orientation.CROSS,
        dataSets = {@DataSetDef(name = "Revenue_dynamic_header", type = DataSetType.DELEGATE),
                @DataSetDef(name = "Revenue_master_data", type = DataSetType.DELEGATE),
                @DataSetDef(name = "Revenue", type = DataSetType.DELEGATE)
        }
)

public class RevenueByMonthReport {

    private final DataManager dataManager;

    public RevenueByMonthReport(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @InputParameterDelegate(alias = "dateFrom")
    public ParameterValidator<Date> dateFromValidator() {
        return value -> {
            if (!Objects.equals(value, ReportUtils.getFirstDayOfMonth(value))) {
                throw new ReportParametersValidationException("'From' date must be the first day of month");
            }
        };
    }
    
    @InputParameterDelegate(alias = "dateTo")
    public ParameterValidator<Date> dateToValidator() {
        return value -> {
            if (!Objects.equals(value, ReportUtils.getLastDayOfMonth(value))) {
                throw new ReportParametersValidationException("'To' date must be the last day of month");
            }
        };
    }

    @ReportDelegate()
    public ParametersCrossValidator parametersCrossValidator() {
        return params -> {
            Date dateFrom = getParam(params, "dateFrom");
            Date dateTo = getParam(params, "dateTo");
            if (dateFrom.after(dateTo)) {
                throw new ReportParametersValidationException("'From' date must be before 'To' date");
            }
        };
    }

    @DataSetDelegate(name = "header")
    public ReportDataLoader headerDataLoader() {
        return (reportQuery, parentBand, params) ->
                List.of(
                        Map.of(
                                "dateFrom", formatDateTime(params.get("dateFrom"), "yyyy-MM-dd"),
                                "dateTo", formatDateTime(params.get("dateTo"), "yyyy-MM-dd"),
                                "generatedAt", formatDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss")
                        )
                );
    }

    @DataSetDelegate(name = "Revenue_dynamic_header")
    public ReportDataLoader revenueDynamicHeaderDataLoader() {
        return (reportQuery, parentBand, params) -> {
            Date dateFrom = getParam(params, "dateFrom");
            Date dateTo = getParam(params, "dateTo");

            YearMonth start = YearMonth.from(ReportUtils.dateToLocalDate(dateFrom));
            YearMonth end = YearMonth.from(ReportUtils.dateToLocalDate(dateTo));

            List<Map<String, Object>> result = new ArrayList<>();
            YearMonth current = start;
            while (!current.isAfter(end)) {
                Map<String, Object> map = new HashMap<>();
                map.put("monthId", current.getYear() + "-" + current.getMonthValue());
                map.put("monthName", current.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + current.getYear());
                map.put("year", current.getYear());
                map.put("month", current.getMonthValue());
                result.add(map);
                current = current.plusMonths(1);
            }
            return result;
        };
    }

    @DataSetDelegate(name = "Revenue_master_data")
    public ReportDataLoader revenueMasterDataDataLoader() {
        return (reportQuery, parentBand, params) -> {
            List<Client> clients = dataManager.load(Client.class)
                    .query("""
                            select c from Client c
                            where exists (
                                    select 1 from Payment p where p.invoice.client.id = c.id and
                                        p.date >= :dateFrom and p.date <= :dateTo
                                    )
                            order by c.name""")
                    .parameter("dateFrom", params.get("dateFrom"))
                    .parameter("dateTo", params.get("dateTo"))
                    .list();
            return clients.stream()
                    .map(client -> Map.of("clientId", client.getId(), "name", (Object) client.getName()))
                    .toList();
        };
    }

     @DataSetDelegate(name = "Revenue")
     public ReportDataLoader revenueDataLoader() {
        return (reportQuery, parentBand, params) -> {
            List<Map<String, Object>> headerList = getParam(params, "Revenue_dynamic_header");

            List<Map<String, Object>> masterDataList = getParam(params, "Revenue_master_data");
            List<Object> clientIds = masterDataList.stream()
                    .map(m -> m.get("clientId"))
                    .toList();

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> headerData : headerList) {
                dataManager.loadValues("""
                            select p.invoice.client.id, sum(p.amount) from Payment p
                            where extract(month from p.date) = :month and
                                extract(year from p.date) = :year and
                                p.invoice.client.id in :clientIds
                            group by p.invoice.client.id
                            """)
                        .properties("clientId", "amount")
                        .parameter("month", headerData.get("month"))
                        .parameter("year", headerData.get("year"))
                        .parameter("clientIds", clientIds)
                        .list()
                        .forEach(kv -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("Revenue_dynamic_header@monthId", headerData.get("monthId"));
                            map.put("Revenue_master_data@clientId", kv.getValue("clientId"));
                            map.put("amount", kv.getValue("amount"));
                            result.add(map);
                        });
            }
            return result;
        };
    }
}