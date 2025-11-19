<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Reports/CSS_&_JS6.jsp" %>
 
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
				              
								<table id="Security_report_table_id" class="table table-striped table-hover table-bordered dt-responsive nowrap" style="width:100%">
						
									<thead>
										<tr role="row">
											<th>S.NO</th>
											<th>ROLE NAME</th> 
											<th>ROLE DESCRIPTION</th> 
											<th>ENTITLEMENTS / PERMISSIONS</th>
											
										</tr>
									</thead>
									
									 <tbody>
									         <tr>
									            <td>1</td>
									            <td>ADMIN</td>
									            <td>ACCESS MANAGEMENT ACTIVITY</td>
									            <td>ADMIN</td>
									        </tr>
									         <tr>
									            <td>2</td>
									            <td>CHECKER</td>
									            <td>UPLOAD MODULE APPROVER</td>
									            <td>CHKR</td>
									        </tr>
									         <tr>
									            <td>3</td>
									            <td>MAKER</td>
									            <td>UPLOAD MODULE MAKER</td>
									            <td>MAKR</td>
									        </tr>
									         <tr>
									            <td>4</td>
									            <td>APP SUPPORT</td>
									            <td>PSS SUPPORT MEMBER FOR APPLICATION</td>
									            <td>APPSUPPORT</td>
									        </tr>
									         <tr>
									            <td>5</td>
									            <td>REPORT VIEWER</td>
									            <td>USERS HAVE ACCESS TO REPORT</td>
									            <td>REPORTVIEWER</td>
									        </tr>
									         <tr>
									            <td>6</td>
									            <td>DASHBOARD VIEWER</td>
									            <td>USERS HAVE ACCESS TO DASHBOARD</td>
									            <td>DASHBRDVIEWER</td>
									        </tr>
														 																																						
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