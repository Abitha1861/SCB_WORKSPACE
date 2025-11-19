<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Configuration/CSS_&_JS5.jsp" %>
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
								
									<div class="row mt--3">	
									<div class="col-md-6">
											<div class="row">
												<div class="col-md-3">
													<div class="form-group">	
														<label for="email2">Module</label>
													</div>
												</div>
												<div class="col-md-9">
													<div class="form-group">		
														<select id="module" class="form-control">
													</select>
														<label id="module_error" class="text-danger"></label>
													</div>
												</div>
											</div>
										</div>
										
											<div class="col-md-6">
											<div class="row">
												<div class="col-md-3">
													<div class="form-group">	
														<label for="email2">Sub Module</label>
													</div>
												</div>
											<div class="col-md-9">
												<div class="form-group">
													  <select id="submodule" class="form-control" >
												    </select>			
														<label id="submodule_error" class="text-danger"></label>
												</div>
											</div>
											</div>
										</div>
										</div>
									 
										<div class="row mt--1">	
										<div class="col-md-6">
											<div class="row">
												<div class="col-md-3">
													<div class="form-group">	
														<label for="email2">Event Name</label>
													</div>
												</div>
												<div class="col-md-9">
													<div class="form-group">
														<select class="form-control" id="event_name">
												    	 <option value="Select">Select</option> 
														</select>
														<label id="event_name_error" class="text-danger"></label>
													</div>
												</div>
												</div>
												</div>
												<div class="col-md-6 ">
											<div class="row">
											
													<div class="col-md-3">
													<div class="form-group">	
														<label for="email2">Upload File</label>
													</div>
												</div>
												<div class="col-md-9" id="file">
                                                      <div class="form-group">
                                                    <input type="file"  class="form-control custom-file-label" id="file1" accept=".xlsx">
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
										
										
										<!-- 
										<div class="col-md-4">
											<div class="row">
												<div class="col-md-4">
														<label for="email2">Upload File</label>
	                                                    <input type="file"  class="form-control" id="upload_file">
														<label id="upload_file_error" class="text-danger"></label>
													
												</div>
											</div>
										</div>
										
									</div>
																
									<div class="row mt-3 offset-3">	
										<div class="col-md-4" align="center">
											<button id="Upload_excel" class="btn btn-secondary center">Submit</button>	
										</div>
									
									
										<div class="col-md-1" align="center">
											<button id="Download_excel" class="btn btn-secondary" disabled>Download Excel</button>	
										</div>
										
								    
									</div>	
									
								    <div class="row mt-3 offset-4" id="file" style="display: none;">
								        <input type="file" id="file1" accept=".xlsx" >
								        <button class="btn btn-secondary" id="submit">Submit</button>
								    </div>
									<br>
									<br>
									<div id="excel_upload" class="row" >	
										<div class="col-md-12 mt-4" align="center">
								
											 <div class="table-responsive data_report sm-table sm_row justify-content-center">
							                	
							                								
											 </div>
											 
											 
										</div>
									</div>-->
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
      	</div>
      	</div>
      	
	</body>
	
</html>