<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Reports/CSS_&_JS3.jsp" %>
 
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
			
	        <div class="row fluid-container">
	        
				<div class="col-md-12 mt--1">
	
					<div id="colour_body" class="card">
	
						<div id="tab_card" class="card-body">
							
						 	<div class="row">
						 	
								<div class="col-md-6">
									<div class="row">
										<div class="offset-2 col-md-2">
											<div class="form-group">
												<label for="apicode">API Code</label>
											</div>
										</div>
										<div class="col-md-6">
											<div class="form-group">
												<input type="text" class="form-control" placeholder="" id="apicode">
												<label id="apicode_error" class="text-danger"></label>
											</div>
										</div>
									 </div>
								 </div>
								 
								 <div class="col-md-6">
									 <div class="row">
										<div class="col-md-2">
											<div class="form-group">
												<label for="batchid">Batch Id</label>
											</div>
										</div>
										<div class="col-md-6">
											<div class="form-group">
												<select class="form-control" id="batchid">
												    <option value="Select">Select</option> 
												</select>
												<label id="batchid_error" class="text-danger"></label>
											</div>
										</div>
									</div>
								</div>	
										
							</div>
											
							<div class="row">
						 	
								<div class="col-md-6">
									<div class="row">
										<div class="offset-2 col-md-2">
											<div class="form-group">
												<label for="fdate">From Date</label>
											</div>
										</div>
										<div class="col-md-6">
											<div class="form-group">
												<input type="date" class="form-control" placeholder="" id="fdate" onkeydown="event.preventDefault();">
												<label id="fdate_error" class="text-danger"></label>
											</div>
										</div>
									 </div>
								 </div>
								 
								 <div class="col-md-6">
									 <div class="row">
										<div class="col-md-2">
											<div class="form-group">
												<label for="tdate">To Date</label>
											</div>
										</div>
										<div class="col-md-6">
											<div class="form-group">
												<input type="date" class="form-control" placeholder="" id="tdate" onkeydown="event.preventDefault();">
												<label id="tdate_error" class="text-danger"></label>
												<p id="tdate_span" class="text-danger"></p>
											</div>
										</div>
									</div>
								</div>	
										
							</div>
							
							<div class="row">
						 	
								<div class="col-md-6">
									<div class="row">
										<div class="offset-2 col-md-2">
											<div class="form-group">
												<label for="status">Status</label>
											</div>
										</div>
										<div class="col-md-6">
											<div class="form-group">
												<select class="form-control" name="respdesc" id="status">
													  <option value="">Select</option>
													  <option value="SUCCESS">SUCCESS</option>
													  <option value="FAILED">FAILED</option>  
													  <option value="Pending">Pending</option>  
												</select>
											</div>
										</div>
									 </div>
								 </div>
								 
								 <div class="col-md-6">
									 <div class="row">
										<div class="col-md-2">
											<div class="form-group">
												<label for="respdesc" id="label_resp">Resp Desc</label>
											</div>
										</div>
										<div class="col-md-6">
											<div class="form-group">
												<select class="form-control" name="respdesc" id="respdesc">
													  <option value="Select">Select</option>
													  <option value="RTSIS token timeout">RTSIS token timeout</option>
													  <option value="failed">failed</option>
													  <option value="Others">Others</option>	
													  <option value="SUCCESS">SUCCESS</option>  
												</select>
												<label id="respdesc_error" class="text-danger"></label>
											</div>
										</div>
									</div>
								</div>	
										
							</div>
							
							<div class="col-md-6 offset-5">
									<div class="row">
										<div class="form-group">
											<button id="get_value" class="btn btn-secondary">Get Report</button>	
										</div>	
									</div>
							</div>
	
							<hr> 
				              <div class="table-responsive data_report sm-table">
				              
								<table id="myTable" class="table table-striped table-hover table-bordered dt-responsive nowrap" style="width:100%">
						
									<thead>
										<tr role="row">
											<th>S.No</th>
											<th>Req Date</th>
											<th>Req Time</th>
											<th>Ref No</th>
											<th>Batch Id</th>
											<th>Api Code</th>
											<th>Report Serial</th>
											<th>Start sl</th>
											<th>End sl</th>
											<th>Bot Ref No</th>
											<th>Status</th>
											<th>Resp Code</th>
											<th>Resp Desc</th>
											<th>Action </th>									
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
			<div id="view_product" class="modal custom-modal fade" role="dialog">
			<div class="modal-dialog  modal-xl" role="document">
			<div class="modal-content">
			<div class="modal-header">
				<h5 class="modal-title text-center w-100" >View Report</h5>
				<button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
				<span aria-hidden="true">&times;</span></button>
			</div>
			
			<div class="modal-body">
					<input type='hidden' id='reportsl' name='reportsl'>
					<input type='hidden' id='StartSl' name='StartSl'>
					<input type='hidden' id='EndSl' name='EndSl'>
									
						<div id="report_data" class="row" style="display:none">	
							<div class="col-md-12" align="center">
					
								 <div class="table-responsive data_report1 sm-table sm_row justify-content-center">
				                									
								 </div>
							
							</div>
						</div>
			</div>
</div>
</div>
</div>   
	</body>
</html>