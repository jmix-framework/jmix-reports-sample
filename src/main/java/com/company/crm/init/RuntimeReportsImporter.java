package com.company.crm.init;

import io.jmix.core.Resources;
import io.jmix.core.security.Authenticated;
import io.jmix.reports.ReportImportExport;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

// tag::class[]
@Component
public class RuntimeReportsImporter {

    private static final Logger log = LoggerFactory.getLogger(RuntimeReportsImporter.class);

    @Autowired
    private Resources resources;

    @Autowired
    protected ReportImportExport reportImportExport; // <1>

    private static final String[] archives = new String[] { "client-profile.zip", "orders-by-client.zip",
            "orders-by-status.zip", "revenue-by-month.zip" };

    @EventListener(ApplicationReadyEvent.class) // <2>
    @Authenticated // <3>
    public void importReports(ApplicationReadyEvent event) {
        log.info("Starting runtime report import");
        for (String archive : archives) {
            try (InputStream is = resources.getResourceAsStream("/com/company/crm/runtime-reports/" + archive)) {
                if (is != null) {
                    reportImportExport.importReports(IOUtils.toByteArray(is));
                } else {
                    log.warn("Report file not found: {}", archive);
                }
            } catch (IOException e) {
                log.error("Failed to import report: {}", archive, e);
            }
        }
        log.info("Runtime report import completed");
    }
}
// end::class[]
