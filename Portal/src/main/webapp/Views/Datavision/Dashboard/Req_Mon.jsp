<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Dashboard/CSS_&_JS2.jsp" %>

 <style>
  
  
  
/* Card Container */
.sales {
    background-color: #ffffff; /* Dark background */
    border-radius: 30px;
    /* box-shadow: 0 5px 15px rgb(255, 255, 255); */
    width: 300px;
    text-align: center;
    margin-bottom:10px;
    display: flex;
    justify-content: space-around;
    align-items: center;
    height: 150px;
}
.sales:hover{
	  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
	  transform: scale(1.05);
}
/* Icon Style */
.material-symbols-sharp {
	text-align:center;
    font-size: 36px;
    color: #4caf50; /* Green color */
    margin-bottom: 10px;
}

.success h3 {
    font-size: 18px;
    color: #4caf50;
}

.success h1 {
    font-size: 20px;
    font-weight: bold;
}

.progress {
    position: relative;
    width: 80px;
    height: 80px;
}

.number {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 16px;
    font-weight: bold;
    color: #000000;
}
small {
    font-size: 12px;
    color: #aaa; 
}
.failure h3 {
    font-size: 18px;
    color: red;
}

.failure h1 {
    font-size: 20px;
    font-weight: bold;
}
.time-out h3 {
    font-size: 18px;
    color: #ffba08;
}

.time-out h1 {
    font-size: 20px;
    font-weight: bold;
}
.rtsi-time-out h3 {
    font-size: 18px;
    color: #0E41B0;
}

.rtsi-time-out h1 {
    font-size: 20px;
    font-weight: bold;
}
/*percentage*/
.circle-success {
    width: 70px; /* Outer circle size */
    height: 70px; /* Outer circle size */
    border: 5px solid #4caf50; /* Circular green border */
    border-radius: 50%; /* Makes it circular */
    display: flex; /* Enables flexbox for centering */
    align-items: center; /* Vertically centers content */
    justify-content: center; /* Horizontally centers content */
    font-size: 12px; /* Font size for the value */
    font-weight: bold; /* Bold text */
    color: #4caf50; /* Text color matches the border */
    background: transparent; /* Transparent background */
}
.circle-fail {
    width: 70px; /* Outer circle size */
    height: 70px; /* Outer circle size */
    border: 5px solid red; /* Circular green border */
    border-radius: 50%; /* Makes it circular */
    display: flex; /* Enables flexbox for centering */
    align-items: center; /* Vertically centers content */
    justify-content: center; /* Horizontally centers content */
    font-size: 12px; /* Font size for the value */
    font-weight: bold; /* Bold text */
    color: red; /* Text color matches the border */
    background: transparent; /* Transparent background */
}
.circle-progress {
    width: 70px; /* Outer circle size */
    height: 70px; /* Outer circle size */
    border: 5px solid #0E41B0; /* Circular green border */
    border-radius: 50%; /* Makes it circular */
    display: flex; /* Enables flexbox for centering */
    align-items: center; /* Vertically centers content */
    justify-content: center; /* Horizontally centers content */
    font-size: 12px; /* Font size for the value */
    font-weight: bold; /* Bold text */
    color: #0E41B0; /* Text color matches the border */
    background: transparent; /* Transparent background */
}
.circle-time-out {
    width: 70px; /* Outer circle size */
    height: 70px; /* Outer circle size */
    border: 5px solid #ffba08; /* Circular green border */
    border-radius: 50%; /* Makes it circular */
    display: flex; /* Enables flexbox for centering */
    align-items: center; /* Vertically centers content */
    justify-content: center; /* Horizontally centers content */
    font-size: 12px; /* Font size for the value */
    font-weight: bold; /* Bold text */
    color: #ffba08; /* Text color matches the border */
    background: transparent; /* Transparent background */
}
  .material-symbols-wrong{
  	text-align:center;
    font-size: 36px;
    color: red; 
    margin-bottom: 10px
  }
  .material-symbols-time{
    text-align:center;
    font-size: 36px;
    color: #ffba08; 
    margin-bottom: 10px
  }
  .material-symbols-time-out{
    text-align:center;
    font-size: 36px;
    color: #0E41B0; 
    margin-bottom: 10px
  }
  
  
  
  
        .recent_order > .row > h2 {
            font-size: 24px;
            margin-bottom: 10px;
            margin-left:15px
        }
        
        tbody tr:hover {
            background-color: lightgray; /* Row color changes when hovered */
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }
        th, td {
            padding: 10px;
            text-align: left;
            border: 1px solid #ddd;
        }
        th {
            background-color: #f4f4f4;
        }
        .warning {
            color: orange;
        }
        .primary {
            color: blue;
            text-decoration: underline;
            cursor: pointer;
        }
        .additionalRow {
            display: none;
        }
        #toggleButton {
            color: blue;
            text-decoration: underline;
            cursor: pointer;
		
		

  
    </style>
    <body data-background-color="${Menu.get('body_color').getAsString()}"> 
	<div class="wrapper sidebar_minimize">
	
		<div class="main-header">
		
		    <%@ include file="../../../Headers_&_Footers/Default/common/Logo_Header.jsp" %>       
         <%@ include file="../../../Headers_&_Footers/Default/common/Navigation_Bar.jsp" %>         
	</div>
	
     <%@ include file="../../../Headers_&_Footers/Default/common/Side_Bar.jsp" %>     
	
    <div class="main-panel">
    	<div class="content">
    	
				<div class="panel-header bg-secondary-gradient">
					<div class="page-inner py-4">
						<div class="d-flex align-items-left align-items-md-center flex-column flex-md-row">
							<div>
								<h2 class="text-white pb-2 fw-bold">${Menu.get('Title').getAsString()}</h2>
								<h5 class="text-white op-7 mb-2"></h5>
							</div>
							<div class="ml-md-auto py-3">
								<!--  <a href="#" class="btn btn-sm btn-secondary btn-round mr-2">Today</a>
								<a href="#" class="btn btn-sm btn-secondary btn-round mr-2">Week</a>
								<a href="#" class="btn btn-sm btn-secondary btn-round mr-2">Month</a>
								<a href="#" class="btn btn-sm btn-secondary btn-round mr-2">Quarterly</a>
								<a href="#" class="btn btn-sm btn-secondary btn-round mr-2">Half-yearly</a>
								<a href="#" class="btn btn-sm btn-secondary btn-round mr-2">Annually</a> -->
							</div>
						</div>
					</div>
				</div>
				
				<div class="page-inner mt--5">
					
					<div class="row d-flex justify-content-around">
					<div  data-aos="zoom-in" data-aos-duration="1000">
						<div class="col-sm-6 col-md-3 nav-item"> 
							<div class="sales text-center">
								<div class="middle">
								<span class="material-symbols-sharp"><span class="material-symbols-outlined"><i class="flaticon-graph"></i></span></span>
									<div class="success">
										<h3>SUCCESS</h3>
										<h1 id="success">0</h1>
									</div>
								</div>


							</div>						
						</div>
						</div>
						<div  data-aos="zoom-in" data-aos-duration="1000">
						<div class="col-sm-6 col-md-3 nav-item"> 
							<div class="sales text-center">
								<div class="middle">
								<span class="material-symbols-wrong"><span><i class="flaticon-graph-1"></i></span></span>
									<div class="failure">
										<h3>FAILURE</h3>
										<h1 id="failed">0</h1>
									</div>
								</div>

							</div>						
						</div>
						</div>
						<div  data-aos="zoom-in" data-aos-duration="1000">
						<div class="col-sm-6 col-md-3 nav-item"> 
							<div class="sales text-center">
								<div class="middle">
								<span class="material-symbols-time-out"><span class="material-symbols-outlined"><i class="flaticon-clock"></i></span></span>
									<div class="rtsi-time-out">
										<h3>INPROGRESS</h3>
										<h1 id="inprogress">0</h1>
									</div>
								</div>
							</div>						
						</div>
						</div>
						
						<div  data-aos="zoom-in" data-aos-duration="1000">
						<div class="col-sm-6 col-md-3 nav-item"> 
							<div class="sales text-center">
								<div class="middle">
								<span class="material-symbols-time"><span class="material-symbols-outlined"><i class="flaticon-stopwatch"></i></span></span>
									<div class="time-out">
										<h3>TIME-OUT</h3>
										<h1 id="timeout">0</h1>
									</div>
								</div>
							</div>						
						</div>
						</div>
					</div>
<div class="row">
 
<div class="col-md-12">
    <div class="card ml-5 mr-5">
        <div class="card-body">
 
    <div class="recent_order">
  
  
 
<div class="row">
    
   <div  class= "col-3 col-md-4 mt-2 col-sm-12"><h2>RTSIS OVERVIEW</h2></div>
   
   <div id = "date1" class="form-group row col-md-3 d-flex justify-content-end">
    		<label for="fromDate" class="col-sm-2 col-form-label">From</label>
    		<div class="col-sm-10">
    			<input type="date" id="fromDate" name="fromDate" class="form-control">
    			<span id="fromDateError" class="error-message d-none" style="color: red;">* This field is required</span>
    		</div>       
   </div>
   
    <div id = "date2" class="form-group row col-md-3 d-flex justify-content-end">
    		<label for="toDate" class="col-sm-2 col-form-label">To</label>
    		<div class="col-sm-10">
    			<input type="date" id="toDate" name="toDate" class="form-control">
    			<span id="toDateError" class="error-message d-none" style="color: red;">* This field is required</span>
    		</div> 
    		 
   </div>
      
  	<div class="form-group row col-md-2 d-flex justify-content-end">
	    <div class="col-sm-10">
	      <button type="submit" class="btn btn-primary" id = "Date_submit">SUBMIT</button>
	    </div>
	</div>
	

</div> 


<div class="table-responsive">

    <table class="table">
        <thead>
            <tr>
                <th>DOMAIN</th>
                <th>SUCCESS</th>
                <th>FAILED</th>
                <th>INPROGRESS</th>
                <th>VIEW</th>
            </tr>
        </thead>
        <tbody>
			<tr>
			    <td>COUNTRY</td>
			    <td id="COUNTRY_value">0</td>
			    <td id="COUNTRY2_value">0</td>
			    <td id="COUNTRY3_value">0</td>
			   <!--  <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/Country_Reports')">Details</td>  -->
			   
			   
			   
			   <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/Country_Reports')">
				  <a href="javascript:void(0);"
				     style="text-decoration: none;">
				    Details
				  </a>
				</td>
			   
			</tr>
			<tr>
			    <td>FINANCE</td>
			    <td id="FINANCE_value">0</td>
			    <td id="FINANCE1_value">0</td>
			    <td id="FINANCE3_value">0</td>
			  	<td class="primary" onclick="url_with_date_and_redirect('/Dashboard/FINANCE_Reports')">Details</td>
			
			</tr>
			<tr>
			    <td>TRADE</td>
			    <td id="TRADE_value">0</td>
			    <td id="TRADE2_value">0</td>
			    <td id = "TRADE3_value">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/TRADE_Reports')">Details</td>			
			</tr>
			<tr>
			    <td>ACBS</td>
			    <td id="ACBS_value">0</td>
			    <td id="ACBS1_value">0</td>
			    <td id = "ACBS3_value">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/ACBS_Reports')">Details</td>			
			</tr>
			<!-- Additional rows initially hidden -->
			<!--  <tr>
			    <td>MARCIS</td>
			    <td id="MARCIS_value">4563</td>
			    <td id="MARCIS1_value">100</td>
			    <td id = "MARCIS3_value">INPROGRESS</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/MARCIS_Reports')">Details</td>			
			</tr> -->
			<tr>
			    <td>GEMS</td>
			    <td id="GEMS_value">0</td>
			    <td id="GEMS1_value">0</td>
			    <td id = "GEMS3_value">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/GEMS_Reports')">Details</td>			
			</tr>
			<tr class="additionalRow">
			    <td>CLIENT COVERAGE</td>
			    <td id="CLIENT_COVERAGE_value">0</td>
			    <td id="CLIENT_COVERAGE1_value">0</td>
			    <td id = "CLIENT_COVERAGE3_value">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/CC_Reports')">Details</td>			
			</tr>
			<tr class="additionalRow">
			    <td>FM</td>
			    <td id="FM_value">0</td>
			    <td id="FM1_value">0</td>
			    <td id = "FM3_value">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/FM_Reports')">Details</td>			
			</tr>
			<tr class="additionalRow">
			    <td>CASH</td>
			    <td id="CASH_value">0</td>
			    <td id="CASH1_value">0</td>
			    <td id = "CASH3_value">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/CASH_Reports')">Details</td>			
			</tr>
			<tr class="additionalRow">
			    <td>EBBS</td>
			    <td id="EBBS_value">0</td>
			    <td id="EBBS1_value">0</td>
			    <td id = "EBBS3_value">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/EBBS_Reports')">Details</td>					    
			</tr>
			<tr class="additionalRow">
			    <td>CADM</td>
			    <td id="CADM_SUCCESS">0</td>
			    <td id="CADM_FAIL">0</td>
			    <td id = "CADM_INPROG">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/CADM_Reports')">Details</td>			
			</tr>
			<tr class="additionalRow">
			    <td>APARTA</td>
			    <td id="APARTA_SUCCESS">0</td>
			    <td id="APARTA_FAIL">0</td>
			    <td id = "APARTA_INPRO">0</td>
			    <td class="primary" onclick="url_with_date_and_redirect('/Dashboard/APARTA_Reports')">Details</td>				
			</tr>
        </tbody>
    </table>
    </div>
    <a href="#" id="toggleButton">Show All</a>
</div>
</div>

 </div>
  </div>																					
			</div>
			   </div>																				
			</div>
		</div>
	</div>	

</body>
<script>

const toggleButton = document.getElementById("toggleButton");
const additionalRows = document.querySelectorAll(".additionalRow");
const tableRows = document.querySelectorAll("tbody tr");


toggleButton.addEventListener("click", function(e) 
		{
		    e.preventDefault();
		    additionalRows.forEach(row => 
		    {
		        row.style.display = (row.style.display === "none" || row.style.display === "") ? "table-row" : "none";
		    });
    		toggleButton.textContent = (toggleButton.textContent === "Show All") ? "Show Less" : "Show All";
		});


const Date_submit = document.getElementById("Date_submit");

//-------------------


//------------------

Date_submit.addEventListener("click", function(e) 
		{
    e.preventDefault();

    const fromDate = document.getElementById('fromDate');
    const toDate = document.getElementById('toDate');
    const fromDateError = document.getElementById('fromDateError');
    const toDateError = document.getElementById('toDateError');
    
    let formIsValid = true;

    // Check if fromDate is empty
    if (!fromDate.value) {
        fromDateError.classList.remove('d-none'); // Show error for fromDate
        formIsValid = false;
    } else {
        fromDateError.classList.add('d-none'); // Hide error if fromDate is filled
    }

    // Check if toDate is empty
    if (!toDate.value) {
        toDateError.classList.remove('d-none'); // Show error for toDate
        formIsValid = false;
    } else {
        toDateError.classList.add('d-none'); // Hide error if toDate is filled
    }

    // If both dates are filled, log "done"
   if (formIsValid) {
        console.log("done");
        checkDatesAndFetchData(fromDate.value, toDate.value); // Pass the date values to the function
        req_mon_overall_two(fromDate.value, toDate.value);
    }
    
});


$(document).ready(function() 
		{
	req_mon_table();
	req_mon_overall();

	
});

function req_mon_overall()
{
	var data = {};
    $.ajax({
        url: $("#ContextPath2").val() + "/Dashboard/overall", // req_mon card data
        type: 'POST', // HTTP method
        data: data, // Send data
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) 
        {
            console.log("Response received: ", response); 
            
            $('#success').text(response.SUCCESS);
            $('#failed').text(response.FAILED);
            $('#inprogress').text(response.INPROGRESS);
            $('#timeout').text( response.TIMEOUT);
            
            
            const total = response.SUCCESS + response.FAILED + response.INPROGRESS + response.TIMEOUT;

            const getPercent = (count) => {
              return ((count / total) * 100).toFixed(1) + "%";
            };
            
           
            
            $('#success_percent').text(getPercent(response.SUCCESS));
            $('#failed_percent').text(getPercent(response.FAILED));
            $('#inprogress_percent').text(getPercent(response.INPROGRESS));
            $('#timeout_percent').text(getPercent(response.TIMEOUT));
            
            
            
        },
        error: function(xhr, status, error) {
            console.error("AJAX request failed: ", error);
        }
    });
}



function req_mon_table()
{
	var data = {};
    $.ajax({
        url: $("#ContextPath2").val() + "/Dashboard/Success_fail", // current date data
        type: 'POST', // HTTP method
        data: data, // Send data
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
            console.log("Response received: ", response); 
            common(response);
        },
        error: function(xhr, status, error) {
            console.error("AJAX request failed: ", error);
        }
    });
}
	

function checkDatesAndFetchData(fromDate , toDate) 
{
	
	    console.log("From Date: " + fromDate);
	    console.log("To Date: " + toDate);
	    
	    
		var data1 = new FormData();
		
		data1.append("fromDate",fromDate);
		data1.append("toDate",toDate);
		
	
        // Perform the second AJAX request
        $.ajax({
            url: $("#ContextPath2").val() + "/Dashboard/Success_fail_2", // select date data
            type: 'POST', // HTTP method
            data: data1, // Send data
            cache: false,
            contentType: false,
            processData: false,
            success: function(response) 
            {
                console.log("Response received: ", response); 
             
               
                common(response);
            },
            error: function(xhr, status, error) {
                console.error("AJAX request failed: ", error);
            }
        });
   
}


function req_mon_overall_two(fromDate , toDate)
{
	
	console.log("req_mon_overall_two From Date: " + fromDate);
    console.log("req_mon_overall_two To Date: " + toDate);
    
    
	var data1 = new FormData();
	
	data1.append("fromDate",fromDate);
	data1.append("toDate",toDate);

	var data = {};
    $.ajax({
        url: $("#ContextPath2").val() + "/Dashboard/overall_two", // req_mon card data
        type: 'POST', // HTTP method
        data: data1, // Send data
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) 
        {
            console.log("Response received: ", response); 
            
            $('#success').text(response.SUCCESS);
            $('#failed').text(response.FAILED);
            $('#inprogress').text(response.INPROGRESS);
            $('#timeout').text( response.TIMEOUT);
            
            
            const total = response.SUCCESS + response.FAILED + response.INPROGRESS + response.TIMEOUT;

            const getPercent = (count) => {
              return ((count / total) * 100).toFixed(1) + "%";
            };
            
           
            
            $('#success_percent').text(getPercent(response.SUCCESS));
            $('#failed_percent').text(getPercent(response.FAILED));
            $('#inprogress_percent').text(getPercent(response.INPROGRESS));
            $('#timeout_percent').text(getPercent(response.TIMEOUT));
            
            
            
        },
        error: function(xhr, status, error) {
            console.error("AJAX request failed: ", error);
        }
    });
}




function common(response) {
	  const team = response.teams[0];
	  const team2 = response.teams[1];

	  $('#COUNTRY_value').text(team.EXCEL_SUCCESS);
	  $('#COUNTRY2_value').text(team.EXCEL_FAIL);
	  $('#COUNTRY3_value').text(team2.EXCEL_INPROGRESS);

	  $('#FINANCE_value').text(team.Finance_SUCCESS);
	  $('#FINANCE1_value').text(team.Finance_FAIL);
	  $('#FINANCE3_value').text(team2.FINANCE_INPROGRESS);

	  $('#TRADE_value').text(team.TRADE_SUCCESS);
	  $('#TRADE2_value').text(team.TRADE_FAIL);
	  $('#TRADE3_value').text(team2.TRADE_INPROGRESS);

	  $('#ACBS_value').text(team.ACBS_SUCCESS);
	  $('#ACBS1_value').text(team.ACBS_FAIL);
	  $('#ACBS3_value').text(team2.ACBS_INPROGRESS);

	  $('#GEMS_value').text(team.GEMS_SUCCESS);
	  $('#GEMS1_value').text(team.GEMS_FAIL);
	  $('#GEMS3_value').text(team2.GEMS_INPROGRESS);

	  $('#CLIENT_COVERAGE_value').text(team.CC_SUCCESS);
	  $('#CLIENT_COVERAGE1_value').text(team.CC_FAIL);
	  $('#CLIENT_COVERAGE3_value').text(team2.CC_INPROGRESS);

	  $('#FM_value').text(team.FM_SUCCESS);
	  $('#FM1_value').text(team.FM_FAIL);
	  $('#FM3_value').text(team2.FM_INPROGRESS);

	  $('#CASH_value').text(team.CASH_SUCCESS);
	  $('#CASH1_value').text(team.CASH_FAIL);
	  $('#CASH3_value').text(team2.CASH_INPROGRESS);

	  $('#EBBS_value').text(team.EBBS_SUCCESS);
	  $('#EBBS1_value').text(team.EBBS_FAIL);
	  $('#EBBS3_value').text(team2.EBBS_INPROGRESS);

	  $('#CADM_SUCCESS').text(team.CADM_SUCCESS);
	  $('#CADM_FAIL').text(team.CADM_FAIL);
	  $('#CADM_INPROG').text(team2.CADM_INPROGRESS);

	  $('#APARTA_SUCCESS').text(team.APARTA_SUCCESS);
	  $('#APARTA_FAIL').text(team.APARTA_FAIL);
	  $('#APARTA_INPRO').text(team2.Aparta_INPROGRESS);
	};


	document.getElementById('fromDate').valueAsDate = new Date();
	document.getElementById('toDate').valueAsDate = new Date();

	Date_submit.addEventListener("click", function(e) 
			{
			    e.preventDefault();

			    const fromDateValue = document.getElementById("fromDate").value;
			    const toDateValue = document.getElementById("toDate").value;

			    if (!fromDateValue || !toDateValue) 
			    {
			        alert("Please select both dates.");
			        return;
			    }

			    console.log("Button Click - Sending selected dates:");
			 
			});
		



function url_with_date_and_redirect(path) 
{
    const fromDate = document.getElementById('fromDate').value;
    const toDate = document.getElementById('toDate').value;
    
	
    console.log("before encryption fromDate:", fromDate); // Should log: "2022-05-06"
    console.log("before encryption toDate:", toDate); // Should log: "2022-05-06"


    
 //   const contextPath = window.location.origin + '<%= request.getContextPath() %>';

 //   const url = contextPath + path + '?fromDate=' + encodeURIComponent(fromDate) + '&toDate=' + encodeURIComponent(toDate);
    
    
    const encodedFrom = btoa(fromDate);  // Base64 encode
    const encodedTo = btoa(toDate);

    const contextPath = window.location.origin + '<%= request.getContextPath() %>';
    const url = contextPath + path + '?from=' + encodeURIComponent(encodedFrom) + '&to=' + encodeURIComponent(encodedTo);

    
    window.location.href = url;

    
}


 
</script>

</html>

