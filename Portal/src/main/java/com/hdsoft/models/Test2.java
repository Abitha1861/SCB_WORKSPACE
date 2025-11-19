package com.hdsoft.models;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import com.hdsoft.common.Common_Utils;

public class Test2 
{

	public static void main(String[] args) throws ParseException, UnsupportedEncodingException
	{
		
		String val = "1§21552984§21099325§1§11130487§§13000090§27§LELOANTLN§25§OUTER§1§USD§30§UNSEC§20270421§0§63§YRS§§§§§0§Condtn 21552984§Y§Y§N§CMSSCCHN§CMSSCCHN§CMSSCCHN§20121212§20231221§20231221§42§U§N§64§Senior§70§N§92§NP§93§S§1021538601§CM§§301§Credit§§§N\r\n" + 
				"";
		
		//String val = "1|2023-05-19|N|SAVINGS - TAJIRIKA|N|C|A||||1";

       // val = new String(val.getBytes("ISO-8859-1"), StandardCharsets.UTF_8);

        //System.out.println(val);
		
        String[] parts = val.split("§", -1);  // Correct delimiter
        
        System.out.println("Total parts: " + parts.length);
        
        for (int i = 0; i < parts.length; i++) 
        {
            System.out.println("[" + (i+1) + "] => " + parts[i]);
        }
	}
	
}
