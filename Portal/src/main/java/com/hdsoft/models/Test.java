package com.hdsoft.models;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FilenameUtils;
import org.xhtmlrenderer.util.Util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Token_System;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Base64;
import javax.crypto.Cipher;

public class Test {

	public  static String  DBPASSSSS = "login1234";
		
		    public static void main(String[] args) throws ParseException, UnsupportedEncodingException 
		    {
		    	try 
		    	{
		    		String payload = "{\r\n" + 
		    				"  \"loanInformation\": [\r\n" + 
		    				"    {\r\n" + 
		    				"      \"reportingDate\": \"<string>\",\r\n" + 
		    				"      \"customerIdentificationNumber\": \"<string>\",\r\n" + 
		    				"      \"accountNumber\" :\"32323423254\",\r\n" + 
		    				"      \"clientName\": \"<string>\",\r\n" + 
		    				"      \"borrowerCountry\": \"<string>\",\r\n" + 
		    				"      \"ratingStatus\": \"<boolean>\",\r\n" + 
		    				"      \"crRatingBorrower\": \"<string>\",\r\n" + 
		    				"      \"gradesUnratedBanks\": \"<string>\",\r\n" + 
		    				"      \"borrowerCategory\": \"<string>\",\r\n" + 
		    				"      \"gender\": \"<string>\",\r\n" + 
		    				"      \"disability\": \"<string>\",\r\n" + 
		    				"      \"clientType\": \"<string>\",\r\n" + 
		    				"      \"clientSubType\": \"<string>\",\r\n" + 
		    				"      \"groupName\": \"<string>\",\r\n" + 
		    				"      \"groupCode\": \"<string>\",\r\n" + 
		    				"      \"relatedParty\": \"<string>\",\r\n" + 
		    				"      \"relationshipCategory\": \"<string>\",\r\n" + 
		    				"      \"loanNumber\": \"12132334er\",\r\n" + 
		    				"      \"loanType\": \"<string>\",\r\n" + 
		    				"      \"loanEconomicActivity\": \"<string>\",\r\n" + 
		    				"      \"loanPhase\": \"<string>\",\r\n" + 
		    				"      \"transferStatus\": \"<string>\",\r\n" + 
		    				"      \"purposeMortgage\": \"<string>\",\r\n" + 
		    				"      \"purposeOtherLoans\": \"<string>\",\r\n" + 
		    				"      \"sourceFundMortgage\": \"<string>\",\r\n" + 
		    				"      \"amortizationType\": \"<string>\",\r\n" + 
		    				"      \"branchCode\": \"<string>\",\r\n" + 
		    				"      \"loanOfficer\": \"<string>\",\r\n" + 
		    				"      \"loanSupervisor\": \"<string>\",\r\n" + 
		    				"      \"groupVillageNumber\": \"<string>\",\r\n" + 
		    				"      \"cycleNumber\": \"<number>\",\r\n" + 
		    				"      \"loanInstallment\": \"<number>\",\r\n" + 
		    				"      \"repaymentFrequency\": \"<string>\",\r\n" + 
		    				"      \"currency\": \"<string>\",\r\n" + 
		    				"      \"contractDate\": \"<string>\",\r\n" + 
		    				"      \"orgSanctionAmount\": \"<number>\",\r\n" + 
		    				"      \"usdSanctionAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsSanctionAmount\": \"<number>\",\r\n" + 
		    				"      \"orgDisbursedAmount\": \"<number>\",\r\n" + 
		    				"      \"usdDisbursedAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsDisbursedAmount\": \"<number>\",\r\n" + 
		    				"      \"disbursementDate\": \"<string>\",\r\n" + 
		    				"      \"maturityDate\": \"<string>\",\r\n" + 
		    				"      \"realEndDate\": \"<string>\",\r\n" + 
		    				"      \"restructuringDate\": \"<string>\",\r\n" + 
		    				"      \"orgOutstandingPrincipalAmount\": \"<number>\",\r\n" + 
		    				"      \"usdOutstandingPrincipalAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsOutstandingPrincipalAmount\": \"<number>\",\r\n" + 
		    				"      \"orgInstallmentAmount\": \"<number>\",\r\n" + 
		    				"      \"usdInstallmentAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsInstallmentAmount\": \"<number>\",\r\n" + 
		    				"      \"loanInstallmentPaid\": \"<number>\",\r\n" + 
		    				"      \"gracePeriodPaymentPrincipal\": \"<number>\",\r\n" + 
		    				"      \"primeLendingRate\": \"<number>\",\r\n" + 
		    				"      \"interestPricingMethod\": \"<string>\",\r\n" + 
		    				"      \"annualInterestRate\": \"<number>\",\r\n" + 
		    				"      \"effectiveAnnualInterestRate\": \"<number>\",\r\n" + 
		    				"      \"firstInstallmentPaymentDate\": \"<string>\",\r\n" + 
		    				"      \"lastPaymentDate\": \"<string>\",\r\n" + 
		    				"      \"collateralPledgedList\": [\r\n" + 
		    				"        {\r\n" + 
		    				"          \"collateralPledged\": \"<string>\",\r\n" + 
		    				"          \"orgCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"usdCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"tzsCollateralValue\": \"<number>\"\r\n" + 
		    				"        },\r\n" + 
		    				"        {\r\n" + 
		    				"          \"collateralPledged\": \"<string>\",\r\n" + 
		    				"          \"orgCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"usdCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"tzsCollateralValue\": \"<number>\"\r\n" + 
		    				"        }\r\n" + 
		    				"      ],\r\n" + 
		    				"      \"loanFlagType\": \"<string>\",\r\n" + 
		    				"      \"pastDueDays\": \"<number>\",\r\n" + 
		    				"      \"pastDueAmount\": \"<number>\",\r\n" + 
		    				"      \"internalRiskGroup\": \"<string>\",\r\n" + 
		    				"      \"orgAccruedInterestAmount\": \"<number>\",\r\n" + 
		    				"      \"usdAccruedInterestAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsAccruedInterestAmount\": \"<number>\",\r\n" + 
		    				"      \"orgPenaltyChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"usdPenaltyChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsPenaltyChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"orgPenaltyPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"usdPenaltyPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsPenaltyPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"orgLoanFeesChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"usdLoanFeesChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsLoanFeesChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"orgLoanFeesPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"usdLoanFeesPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsLoanFeesPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"orgTotMonthlyPaymentAmount\": \"<number>\",\r\n" + 
		    				"      \"usdTotMonthlyPaymentAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsTotMonthlyPaymentAmount\": \"<number>\",\r\n" + 
		    				"      \"sectorSnaClassification\": \"<string>\",\r\n" + 
		    				"      \"assetClassificationCategory\": \"<string>\",\r\n" + 
		    				"      \"negStatusContract\": \"<string>\",\r\n" + 
		    				"      \"customerRole\": \"<string>\",\r\n" + 
		    				"      \"allowanceProbableLoss\": \"<number>\",\r\n" + 
		    				"      \"botProvision\": \"<number>\",\r\n" + 
		    				"      \"tradingIntent\": \"<string>\",\r\n" + 
		    				"      \"orgSuspendedInterest\": \"<number>\",\r\n" + 
		    				"      \"usdSuspendedInterest\": \"<number>\",\r\n" + 
		    				"      \"tzsSuspendedInterest\": \"<number>\"\r\n" + 
		    				"    },\r\n" + 
		    				"    {\r\n" + 
		    				"      \"reportingDate\": \"<string>\",\r\n" + 
		    				"      \"customerIdentificationNumber\": \"<string>\",\r\n" + 
		    				"      \"accountNumber\": \"<string>\",\r\n" + 
		    				"      \"clientName\": \"<string>\",\r\n" + 
		    				"      \"borrowerCountry\": \"<string>\",\r\n" + 
		    				"      \"ratingStatus\": \"<boolean>\",\r\n" + 
		    				"      \"crRatingBorrower\": \"<string>\",\r\n" + 
		    				"      \"gradesUnratedBanks\": \"<string>\",\r\n" + 
		    				"      \"borrowerCategory\": \"<string>\",\r\n" + 
		    				"      \"gender\": \"<string>\",\r\n" + 
		    				"      \"disability\": \"<string>\",\r\n" + 
		    				"      \"clientType\": \"<string>\",\r\n" + 
		    				"      \"clientSubType\": \"<string>\",\r\n" + 
		    				"      \"groupName\": \"<string>\",\r\n" + 
		    				"      \"groupCode\": \"<string>\",\r\n" + 
		    				"      \"relatedParty\": \"<string>\",\r\n" + 
		    				"      \"relationshipCategory\": \"<string>\",\r\n" + 
		    				"      \"loanNumber\": \"<string>\",\r\n" + 
		    				"      \"loanType\": \"<string>\",\r\n" + 
		    				"      \"loanEconomicActivity\": \"<string>\",\r\n" + 
		    				"      \"loanPhase\": \"<string>\",\r\n" + 
		    				"      \"transferStatus\": \"<string>\",\r\n" + 
		    				"      \"purposeMortgage\": \"<string>\",\r\n" + 
		    				"      \"purposeOtherLoans\": \"<string>\",\r\n" + 
		    				"      \"sourceFundMortgage\": \"<string>\",\r\n" + 
		    				"      \"amortizationType\": \"<string>\",\r\n" + 
		    				"      \"branchCode\": \"<string>\",\r\n" + 
		    				"      \"loanOfficer\": \"<string>\",\r\n" + 
		    				"      \"loanSupervisor\": \"<string>\",\r\n" + 
		    				"      \"groupVillageNumber\": \"<string>\",\r\n" + 
		    				"      \"cycleNumber\": \"<number>\",\r\n" + 
		    				"      \"loanInstallment\": \"<number>\",\r\n" + 
		    				"      \"repaymentFrequency\": \"<string>\",\r\n" + 
		    				"      \"currency\": \"<string>\",\r\n" + 
		    				"      \"contractDate\": \"<string>\",\r\n" + 
		    				"      \"orgSanctionAmount\": \"<number>\",\r\n" + 
		    				"      \"usdSanctionAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsSanctionAmount\": \"<number>\",\r\n" + 
		    				"      \"orgDisbursedAmount\": \"<number>\",\r\n" + 
		    				"      \"usdDisbursedAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsDisbursedAmount\": \"<number>\",\r\n" + 
		    				"      \"disbursementDate\": \"<string>\",\r\n" + 
		    				"      \"maturityDate\": \"<string>\",\r\n" + 
		    				"      \"realEndDate\": \"<string>\",\r\n" + 
		    				"      \"restructuringDate\": \"<string>\",\r\n" + 
		    				"      \"orgOutstandingPrincipalAmount\": \"<number>\",\r\n" + 
		    				"      \"usdOutstandingPrincipalAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsOutstandingPrincipalAmount\": \"<number>\",\r\n" + 
		    				"      \"orgInstallmentAmount\": \"<number>\",\r\n" + 
		    				"      \"usdInstallmentAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsInstallmentAmount\": \"<number>\",\r\n" + 
		    				"      \"loanInstallmentPaid\": \"<number>\",\r\n" + 
		    				"      \"gracePeriodPaymentPrincipal\": \"<number>\",\r\n" + 
		    				"      \"primeLendingRate\": \"<number>\",\r\n" + 
		    				"      \"interestPricingMethod\": \"<string>\",\r\n" + 
		    				"      \"annualInterestRate\": \"<number>\",\r\n" + 
		    				"      \"effectiveAnnualInterestRate\": \"<number>\",\r\n" + 
		    				"      \"firstInstallmentPaymentDate\": \"<string>\",\r\n" + 
		    				"      \"lastPaymentDate\": \"<string>\",\r\n" + 
		    				"      \"collateralPledgedList\": [\r\n" + 
		    				"        {\r\n" + 
		    				"          \"collateralPledged\": \"<string>\",\r\n" + 
		    				"          \"orgCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"usdCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"tzsCollateralValue\": \"<number>\"\r\n" + 
		    				"        },\r\n" + 
		    				"        {\r\n" + 
		    				"          \"collateralPledged\": \"<string>\",\r\n" + 
		    				"          \"orgCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"usdCollateralValue\": \"<number>\",\r\n" + 
		    				"          \"tzsCollateralValue\": \"<number>\"\r\n" + 
		    				"        }\r\n" + 
		    				"      ],\r\n" + 
		    				"      \"loanFlagType\": \"<string>\",\r\n" + 
		    				"      \"pastDueDays\": \"<number>\",\r\n" + 
		    				"      \"pastDueAmount\": \"<number>\",\r\n" + 
		    				"      \"internalRiskGroup\": \"<string>\",\r\n" + 
		    				"      \"orgAccruedInterestAmount\": \"<number>\",\r\n" + 
		    				"      \"usdAccruedInterestAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsAccruedInterestAmount\": \"<number>\",\r\n" + 
		    				"      \"orgPenaltyChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"usdPenaltyChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsPenaltyChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"orgPenaltyPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"usdPenaltyPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsPenaltyPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"orgLoanFeesChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"usdLoanFeesChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsLoanFeesChargedAmount\": \"<number>\",\r\n" + 
		    				"      \"orgLoanFeesPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"usdLoanFeesPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsLoanFeesPaidAmount\": \"<number>\",\r\n" + 
		    				"      \"orgTotMonthlyPaymentAmount\": \"<number>\",\r\n" + 
		    				"      \"usdTotMonthlyPaymentAmount\": \"<number>\",\r\n" + 
		    				"      \"tzsTotMonthlyPaymentAmount\": \"<number>\",\r\n" + 
		    				"      \"sectorSnaClassification\": \"<string>\",\r\n" + 
		    				"      \"assetClassificationCategory\": \"<string>\",\r\n" + 
		    				"      \"negStatusContract\": \"<string>\",\r\n" + 
		    				"      \"customerRole\": \"<string>\",\r\n" + 
		    				"      \"allowanceProbableLoss\": \"<number>\",\r\n" + 
		    				"      \"botProvision\": \"<number>\",\r\n" + 
		    				"      \"tradingIntent\": \"<string>\",\r\n" + 
		    				"      \"orgSuspendedInterest\": \"<number>\",\r\n" + 
		    				"      \"usdSuspendedInterest\": \"<number>\",\r\n" + 
		    				"      \"tzsSuspendedInterest\": \"<number>\"\r\n" + 
		    				"    }\r\n" + 
		    				"  ],\r\n" + 
		    				"  \"signature\": \"<string>\"\r\n" + 
		    				"}";
		    		
		    		String Loannumber = RegexValue(payload, "\"accountNumber\"\\s*:\\s*\"([^>]*?)\"");
		    		
		    		System.out.println(Loannumber);
		    		
		    		
		        } 
		    	catch (Exception e) 
		    	{
		            System.err.println("Error retrieving port: " + e.getMessage());
		        }
		    }
		    
		    public static String RegexValue(String input, String regex) 
			{
		    	String uuid = "";
		    	
				try
				{
			        Pattern pattern = Pattern.compile(regex);
			        Matcher matcher = pattern.matcher(input);

			        if(matcher.find()) 
			        {
			            uuid = matcher.group(1);
			        } 
			        else 
			        {
			        	//logger.debug("string not found in the input string.");
			        }
				 }
				 catch(Exception e)
				 {
					 //logger.debug("Exception in RegexValue  :::: "+e.getLocalizedMessage());
				 }
				
				 return uuid;
			}
}
