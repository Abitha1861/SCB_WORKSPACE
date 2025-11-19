<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Reports/CSS_&_JS4.jsp" %>
 
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
							
				              <div class="table-responsive sm-table">
				              
								<table id="user_list_table_id" class="table table-striped table-hover table-bordered dt-responsive nowrap" style="width:100%">
						
									<thead>
										<tr role="row">
											<th>S.No</th>
											<th>USER ID</th> 
											<th>OWNER</th> 
											<th>ROLE CODE</th>
											<th>LAST LOGIN DATE</th>
											<th>ACCOUNT TYPE</th>
											<th>ACCOUNT STATUS</th>
										</tr>
									</thead>
									
									 <tbody>																																						
									</tbody>
									
								</table>
							</div>
									</div></div>
								</div>
							</div>
						</div>	
					</div>
				</div>	
			</div>   
	</body>
</html>