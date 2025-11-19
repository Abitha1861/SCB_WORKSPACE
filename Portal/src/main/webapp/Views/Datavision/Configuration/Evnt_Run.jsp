<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Configuration/Event_Creation/CSS_&_JS2.jsp" %>
 
 
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
		
							<div id="tab_card" class="card-body">
								
									<div class="row mt--3">	
										<div class="col-md-6">
											<div class="row">
												<div class="col-md-4">
													<div class="form-group">	
														<label for="email2">Module</label>
													</div>
												</div>
												<div class="col-md-8">
													<div class="form-group">		
														<select id="module" class="form-control">
														<option value="Select">Select</option> 
														<!-- <option value="">Select</option>
														<option value="TRANMON">Transactions Monitoring</option>
														<option value="APPMON">Application Server Monitoring</option>
														<option value="DBMON">Database Monitoring</option>		
														<option value="FMON">File Monitoring</option>		
														<option value="WEBMON">Web service Monitoring</option>				
														<option value="EXEMON">Exception and Error Scanning</option>				
														<option value="CONMON">Connectivity Monitoring</option>		
														<option value="INFMON">Infra Monitoring</option> -->						
													</select>
														<label id="module_error" class="text-danger"></label>
													</div>
												</div>
											</div>
										</div>
										
										<div class="col-md-6">
											<div class="row">
												<div class="col-md-4">
													<div class="form-group">	
														<label for="email2">Sub Module</label>
													</div>
												</div>
											<div class="col-md-8">
												<div class="form-group">
													<select class="form-control" id="submodule">
												    	 <option value="Select">Select</option> 
													</select>			
													<!-- <input type="text" class="form-control" id="event_code" placeholder="" maxlength="25" size="20"> -->
													<label id="event_code_error" class="text-danger"></label>
												</div>
											</div>
											</div>
										</div>
										
										
									</div>
									
									<div class="row mt--3">	
										<div class="col-md-6">
											<div class="row">
												<div class="col-md-4">
													<div class="form-group">	
														<label for="email2">Event Name</label>
													</div>
												</div>
												<div class="col-md-8">
													<div class="form-group">
														<select class="form-control" id="event_name">
												    	 <option value="Select">Select</option> 
														</select>
														<label id="event_name_error" class="text-danger"></label>
													</div>
												</div>
											</div>
										</div>	
										
										<div class="col-md-6">
											<div class="row">
												<div class="col-md-4">
													<div class="form-group">	
														<label for="email2">Batch Id</label>
													</div>
												</div>
												<div class="col-md-8">
													<div class="form-group">
														<select class="form-control" id="batch_id">
												    	 <option value="Select">Select</option> 
														</select>
														<label id="batch_id_error" class="text-danger"></label>
													</div>
												</div>
											</div>
										</div>	
										
										
									</div>
								
																
									<div class="row mt-3">	
										<div class="col-md-12" align="center">
											<button id="event_creation" class="btn btn-secondary">Run Event</button>	
										</div>
									</div>	
									
									<input type='hidden' id='reportsl' name='reportsl'>
									
									<div id="report_data" class="row" style="display:none">	
										<div class="col-md-12 mt-4" align="center">
								
											 <div class="table-responsive data_report sm-table sm_row justify-content-center">
							                									
											 </div>
										
										</div>
									</div>
	
									<div id="act_grp" class="card-action" style="display:none">
										<div class="row mt-3">	
											<div class="col-md-6">
											
												<div class="btn-group float-right">
												  <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
												    Action
												  </button>
												  <div class="dropdown-menu">
												  	<a class="dropdown-item" href="#" onclick="Push_Data()">Push the data</a>
												    <div class="dropdown-divider"></div>
												    <a class="dropdown-item" href="#" onclick="Save_Data()">Save the Changes</a>
												    <div class="dropdown-divider"></div>
												    <a class="dropdown-item" href="#" onclick="Push_Data()">Save &amp; Push the data now</a>
												  </div>
												</div>
				
											</div>
											
											<div class="col-md-6">												
												<button id="form_reset" class="btn btn-danger float-left">Reset</button>														
											</div>
										</div>	
									</div>
									
								</div>
							</div>
							
						</div>
						
					</div>	
				</div>   
			</div>	
      	</div> 	</div>
      	
      	
	</body>
</html>