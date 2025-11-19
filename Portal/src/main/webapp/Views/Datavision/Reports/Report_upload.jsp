<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Reports/CSS_&_JS4.jsp" %>
 <style>
    
    table {
        border-collapse: collapse;
        width: 100%;
        overflow-x: auto;
    }

   
    th, td {
        border: 1px solid black;
        padding: 8px;
        text-align: left;
    }

   
    th {
        background-color: #f2f2f2;
    }
    
    .table .thead-dark th {
    height: 40px;
    background-color: #740852;
    color: white;
}
.table-bordered th {
    border: 1px solid #dee2e6;
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
		
							<div id="tab_card" class="card-body">
								
										<div class="row mt--1">	
										
										<div class="col-md-4">
											<div class="row">
												<div class="col-md-3">
													<div class="form-group">	
														<label for="email2">Source System</label>
													</div>
												</div>
												<div class="col-md-9">
													<div class="form-group">
														<select class="form-control" id="source-system">
													    	 <option value="Select">Select</option> 
													    	 <option value="FM">FM</option> 
														</select>
														<label id="source-system_error" class="text-danger"></label>
													</div>
												</div>
												</div>
											</div>
											
										<div class="col-md-4">
											<div class="row">
												<div class="col-md-3">
													<div class="form-group">	
														<label for="email2">Report Type</label>
													</div>
												</div>
												<div class="col-md-9">
													<div class="form-group">
														<select class="form-control" id="report-type">
													    	 <option value="Select">Select</option> 
													    	 <option value="R1">Splice Risk View Report</option> 
													    	 <option value="R2">Past due days Report</option> 
													    	 <option value="R3">CDS Report</option> 
													    	 <option value="R4">GRID1 encumbrance Report</option> 
														</select>
														<label id="event_name_error" class="text-danger"></label>
													</div>
												</div>
												</div>
											</div>
											
											<div class="col-md-4">
												<div class="row">
											
													<div class="col-md-3">
													<div class="form-group">	
														<label for="email2">Upload File</label>
													</div>
												</div>
												<div class="col-md-9" id="file">
                                                      <div class="form-group">
                                                    <input type="file"  class="form-control custom-file-label" id="file1" accept=".csv">
                                                    </div>
                                                  <!-- <label class="custom-file-label" for="inputGroupFile01">Choose File</label>
                                                   -->       </div>
                                                     </div>
											</div>
										
										</div>
											</div>
											
																		
									<div class="row mt-3 offset-5">	
										<div class="col-md-4" align="center">
											<button id="upload_excel" class="btn btn-secondary ">Submit</button>	
										</div>
											</div>
											<div id="excel_upload" class="row" >	
										<div class="col-md-12 mt-4" align="center">
								
											 <div class="table-responsive data_report sm-table sm_row justify-content-center" >
							                	
							                			<table id="myTable" class="myTable"></table>					
											 </div>
											 <div id="validationJson" class="validArray"></div>
											
										</div>
										
										</div>
																														
									<div id="upload" class="row mt-3 offset-5" style="display:none">
										
										<button class="btn btn-secondary" >Upload</button>	
									
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