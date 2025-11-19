package com.hdsoft.models;

/*
package com.scb.bpsi.das.connector;

import com.scb.bpsi.das.DASClient;
import com.scb.bpsi.exception.BPSIClientException;
import com.scb.bpsi.v2.proto.das.IdentitiesRecords;
import com.scb.bpsi.v2.proto.das.query.DASQueryResponse;
import com.scb.bpsi.v2.proto.das.query.DataResponse;
import com.scb.bpsi.v2.proto.errors.FabricError;
import com.spencerwi.either.Either;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DASClientConnector {

    AtomicLong responseCount = new AtomicLong(0L);
    
    public static void main(String args[])
    {
    	//DAS Client creation
    	DASClient client = new DASClient(jwtToken, dasUrl, dasGrpcCertCRTFilePath);
    	DASClientConnector connector = new DASClientConnector();

    	//sample code for recursive 
    	String fromDate = "2024-08-01";
    	String toDate = "2024-08-01";

    	int pageSize = 50;
    	int page = 0;

    	int totalPages = 0;

    	long totalRecordCount = 0;

    	do {
    	    try {
    	        Thread.sleep(1000);
    	        dataResponse = dasClientConnector.queryDomainDataByDateRange(jwtToken, client, schemaName, fromDate, toDate, page, pageSize);

    	        //TODO : Perform business operation here if required.
    	        printResponseData(dataResponse);

    	        responseCount.addAndGet(dataResponse.getEventsCount());
    	        log.info("\n\nTotalCount : {}, Page : {}, PageSize : {}, DateReceived Until Now : {}\n", dataResponse.getMetaHeader().getTotalEventCount(), (page + 1), dataResponse.getMetaHeader().getPageSize(), responseCount.get());
    	        log.info("Received count until now : " + responseCount.get());

    	        if (totalRecordCount == 0) {
    	            totalRecordCount = dataResponse.getMetaHeader().getTotalEventCount();
    	            totalPages = (int) (totalRecordCount / pageSize);
    	            if (totalPages * pageSize < totalRecordCount) {
    	                totalPages++;
    	            }
    	        }
    	        page++;
    	    } catch (Exception exception) {
    	        failedPages.append("Failed page : " + page + ", FromDate : " + fromDate + ", ToDate : " + toDate + "\n");
    	        log.info("Exception occurred on page : " + page + ", FromDate : " + fromDate + ", ToDate : " + toDate);
    	        log.error("Exception on Runner Recursive DateRange : " + exception);
    	    }
    	} while (page < totalPages);


    	//Close the client
    	try {
    	    client.close();
    	} catch (Exception ex) {
    	    ex.printStackTrace();
    	}
    }

    public Long queryDomainDataCountByDateRange(String jwtToken, DASClient client, String schemaName, String dateFrom, String dateTo) {
        log.info("COUNT : Query for domain data CoUNT By date range");

        Either<Long, FabricError> response = client.queryDomainDataCountByDateRange(jwtToken, schemaName, dateFrom, dateTo);

        if (response.isRight()) {
            log.error("Error on queryDomainDataCountByDateRange : " + response.getRight());
        }

        return response.getLeft();
    }

    public Long queryAllDomainDataCountForSchema(String jwtToken, DASClient client, String schemaName) {
        log.info("COUNT : Query for domain data COUNT for a schema.");

        Either<Long, FabricError> response = client.queryDomainDataCount(jwtToken, schemaName);

        if (response.isRight()) {
            log.error("Error on data count for a schema  : " + response.getRight());
        }

        return response.getLeft();
    }

    public DataResponse queryDomainDataByDateRangeWithTimestamp(String jwtToken, DASClient client, String schemaName, String startDate, String endDate, int page, int pageSize) throws BPSIClientException {
        try {
            log.info("Query for domain data by Date Range Timestamp.");

            responseCount.set(0L);

            Either<DASQueryResponse, FabricError> response = client.queryDomainDataByDateRangeWithTimestamp(jwtToken, schemaName, startDate, endDate, page, pageSize);

            if (response.isRight()) {
                log.error("Error on query domain by Date Range  : " + response.getRight());
            }

            return response.getLeft().getData();
        } catch (Exception exception) {
            log.error("\n\nException on Connector queryAllDomainDataForSchema : " + exception.getMessage());
            if (StringUtils.contains(exception.getMessage(), "CANCELLED: Failed to read message")) {
                log.error("\n\n*** Exception occurred while reading the message from stream. \nTo fix the issue, consider increasing the JVM memory or reduce the page size.\n");
            }

            throw new BPSIClientException(exception);
        }
    }

    public DataResponse queryDomainDataByDateRange(String jwtToken, DASClient client, String schemaName, String startDate, String endDate, int page, int pageSize) throws BPSIClientException {
        try {
            log.info("Query for domain data by Date Range.");

            responseCount.set(0L);

            Either<DASQueryResponse, FabricError> response = client.queryDomainDataByDateRange(jwtToken, schemaName, startDate, endDate, page, pageSize);

            if (response.isRight()) {
                log.error("Error on query domain by Date Range  : " + response.getRight());
            }

            return response.getLeft().getData();
        } catch (Exception exception) {
            log.error("\n\nException on Connector queryDomainDataByDateRange : " + exception.getMessage());
            if (StringUtils.contains(exception.getMessage(), "CANCELLED: Failed to read message")) {
                //log.error("\n\n*** Exception occurred while reading the message from stream. \nTo fix the issue, consider increasing the JVM memory or reduce the page size.\n");
            }

            throw new BPSIClientException(exception);
        }
    }

    public DataResponse queryAllDomainDataForSchema(String jwtToken, DASClient client, String schemaName, int page, int pageSize) throws BPSIClientException {
        try {
            log.info("Query day zero domain  data for a schema.");

            responseCount.set(0L);

            Either<DASQueryResponse, FabricError> response = client.queryAllDomainData(jwtToken, schemaName, page, pageSize);

            if (response.isRight()) {
                log.error("Error on query domain by schema  : " + response.getRight());
            }

            return response.getLeft().getData();
        } catch (Exception exception) {
            log.error("\n\nException on Connector queryAllDomainDataForSchema : " + exception.getMessage());
            if (StringUtils.contains(exception.getMessage(), "CANCELLED: Failed to read message")) {
                log.error("\n\n*** Exception occurred while reading the message from stream. \nTo fix the issue, consider increasing the JVM memory and reduce the page size.\n");
            }

            throw new BPSIClientException(exception);
        }
    }

    public IdentitiesRecords queryAllIdentities(String jwtToken, DASClient client, String schemaName) {
        log.info("Query for all Identities from a schema.");

        Either<IdentitiesRecords, FabricError> response = client.queryAllIdentities(jwtToken, schemaName);

        if (response.isRight()) {
            log.error("Error on query Identities : " + response.getRight());
        }

        return response.getLeft();
    }

    public IdentitiesRecords queryAllIdentitiesByDateRange(String jwtToken, DASClient client, String schemaName,String startDate, String endDate) {
        log.info("Query for all Identities from a schema By Date Range.");

        Either<IdentitiesRecords, FabricError> response = client.queryAllIdentitiesByDateRange(jwtToken, schemaName,startDate,endDate);

        if (response.isRight()) {
            log.error("Error on query Identities Date Range : " + response.getRight());
        }

        return response.getLeft();
    }
}*/
