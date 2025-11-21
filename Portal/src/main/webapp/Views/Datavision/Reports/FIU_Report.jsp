<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>

<%@ include file="../../../Headers_&_Footers/Default/Reports/CSS_&_JS.jsp" %>   


<style>
	.text-danger
	{
		display:none;
		margin-bottom: 0px !important;
	}
	
	table.dataTable thead, tfoot {
		background-color: #6c757d;
		color:white;
	}
	
	.btn.btn-secondary {
		color: #fff !important;
	}
	
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
				<div class="page-inner">
					
				<%@ include file="../../../Headers_&_Footers/Default/common/Form_header.jsp" %>   
						
					<div class="row">
						<div class="col-md-12 mt--1">
							<div id="colour_body" class="card">
							
								<div class="card-body">
								
									<div class="row">	
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Report Code</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<select id="i_paytype" class="form-control">
													<option>Select</option>
													<option value="CTRs">Currency (Cash) Transactions</option>		
													<option value="EFTs">Electronic Fund Transfer Transaction</option>					
												</select>
												<p id="i_paytype_msg" class="text-danger"></p>
											</div>
										</div>

										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Submission Code</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<select id="i_sub_code" class="form-control">
													<option>Select</option>
													<option>Manual</option>
													<option>Electronic</option>	
												</select>
												<p id="i_subcode_msg" class="text-danger"></p>
											</div>
										</div>
									</div>
									
									
									
									<div class="row">	
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">From Date</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<input type="date" class="form-control" id="i_fromdate" placeholder="" onkeydown='if(event.keyCode != 9) {event.preventDefault()}'>
												<p id="i_fromdate_msg" class="text-danger"></p>
											</div>
										</div>										
									
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">To Date</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<input type="date" class="form-control" id="i_todate" placeholder="" onkeydown='if(event.keyCode != 9) {event.preventDefault()}'>
												<p id="i_todate_msg" class="text-danger"></p>
												<p id="i_todate_span" class="text-danger"></p>
											</div>
										</div>

									</div>
									
									<div class="row">	
									<!-- 	<div class="col-md-2">
											<div class="form-group">	
												<label for="email2"> Amount limit</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<input type="number" class="form-control" id="i_famount" placeholder="Equivalent or above (In USD)"> 
												<p id="i_famount_msg" class="text-danger"></p>
											</div>
										</div>				
										 -->



<div class="col-md-2">
    <div class="form-group">	
        <label>Amount limit</label>
    </div>
</div>

<div class="col-md-4">
    <div class="form-group d-flex gap-2">

        <input type="number" class="form-control mr-1" id="i_famount" 
               placeholder="From AMT">            
        <input type="number" class="form-control ml-1" id="i_tamount" 
               placeholder="To AMT">
    </div>
    
    <p id="i_famount_msg" class="text-danger"></p>
    
</div>



<!--  -->
																
									
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Debit / Credit</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<select id="i_dc" class="form-control">
													<option>Select</option>
													<option value="D">Debit</option>
													<option value="C">Credit</option>	
													<option value="B">Both</option>								
												</select>
												<p id="i_dc_msg" class="text-danger"></p>
											</div>
										</div>
	
									</div>

								</div>
<div class="row">										
<div class="col-md-6">											
<button id="SUBMIT" class="btn btn-secondary float-right mr-3 mb-4">SUBMIT</button>																								
</div>
</div>

<div id="reportSection" style="display:none; padding-left: 20px; padding-right: 20px;">
<hr>
<div class="table-responsive data_report sm-table">
<table id="myTable" class="table table-striped table-hover table-bordered dt-responsive nowrap" style="width:100%">
<thead>
<tr role="row">
<th>S.No</th>
<th>Report Type</th>
<th>Transaction Type</th>
<th>Transaction Date</th>
<th>Debit/Credit</th>
<th>Transaction Amount</th>
<th>Currency</th>
<th>Debit Account</th>
<th>Credit Account</th>
<th>Sender Name</th>
<th>Receiver Name</th>
<th>Source Bank</th>
<th>Destination Bank</th>
</tr>
</thead>
<tbody>																																						
</tbody>
</table>
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
</html>