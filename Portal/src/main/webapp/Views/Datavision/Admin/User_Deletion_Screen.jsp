<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>

<%@ include file="../../../Headers_&_Footers/Default/Administration/CSS_&_JS9.jsp" %>   
<% 
	String rsalt = PasswordUtils.getSalt();
%>
<style type="text/css">
	.text-danger{
		display:none;
		margin-bottom: 0px !important;
		
	}
	.toggle-password {
    float: right;
    cursor: pointer;
    margin-right: 10px;
    margin-top: -25px;
    }
    
label
{
    width: 100px;
}

.alert
{
    display: none;
}

.requirements
{
    list-style-type: none;
}

.wrong .fa-check
{
    display: none;
}

.good .fa-times
{
    display: none;
}


</style>
<body data-background-color="${Menu.get('body_color').getAsString()}">
	<div class="wrapper">
	
		<div class="main-header">
		
		     <%@ include file="../../../Headers_&_Footers/Default/common/Logo_Header.jsp" %>       
		     
		     <%@ include file="../../../Headers_&_Footers/Default/common/Navigation_Bar.jsp" %>  
		     <%@ page import="com.hdsoft.utils.PasswordUtils" %>

			<%@ page import="com.hdsoft.utils.WebContext"%>   
		     
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
										
										<div class="col-md-2"></div>
									
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">User ID</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<input type="text" class="form-control" id="tuserid" placeholder="" maxlength="20" size="15" >
												<label id="tuserid_error" class="text-danger"></label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<input type="button" class="btn btn-secondary" id="chek_sts" value="Check Status">
											</div>
										</div>
									</div>
																	
								<div class="row" id="action_row" style="display:none">	
										<div class="col-md-2"></div>
									     <div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Current Role</label>
											</div>
										</div>
										
										<div class="col-md-4">
											<div class="form-group">		
												<select class="form-control" id="trolecd">
												    <option value="">Select</option>
													<option value="ADMIN">ADMIN - ACCESS MANAGEMENT ACTIVITY</option>
													<option value="CHKR">CHKR - UPLOAD MODULE APPROVER</option>
													<option value="MAKR">MAKR - UPLOAD MODULE MAKER</option>
													<option value="APPSUPPORT">APPSUPPORT - PSS SUPPORT MEMBER FOR APPLICATION</option>
													<option value="REPORTVIEWER">REPORTVIEWER - USERS HAVE ACCESS TO REPORT</option>
													<option value="DASHBRDVIEWER">DASHBRDVIEWER - USERS HAVE ACCESS TO DASHBOARD</option>
												</select>
												<label id="trolecd_error" class="text-danger"></label>
											</div>
										</div>
								</div>
								
							<!-- 	<div class="row" id="action_row" style="display:none">	
										<div class="col-md-2"></div>
									     <div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Change Role Code</label>
											</div>
										</div>
										
										<div class="col-md-4">
											<div class="form-group">
												<select id="trolecd1" class="form-control">
												    <option value="">Select</option>
													<option value="ADMIN">ADMIN - Admin User</option>
													<option value="CHKR">CHKR - Authorizer</option>
													<option value="MAKR">MAKR - Peer Role</option>
													<option value="SUPER_ADMIN">SUPER_ADMIN - Super Admin</option>											
													<option value="OPN">OPN - Operation Role</option>
													<option value="View">View Only</option>
												</select>
												<label id="trolecd1_error" class="text-danger"></label>
											</div>
										</div>
								</div> -->
																	
								</div>
								
								<div class="card-action">
									<div class="row">										
										<div class="col-md-6">
											<button class="btn btn-secondary float-right" id="proceed">Delete</button>	
										</div>
										<div class="col-md-6">	
											<button class="btn btn-danger float-left" onclick="reset()">Reset</button>
											
											<input type="hidden" id="cmenuoption">
										    <input type="hidden" id="mode">
										    
										    <input type="hidden" name="CSRFTOKEN" value="${sessionScope['CSRFTOKEN']}">
											<input type="hidden" id="calcurrbusDate" value="<%= session.getAttribute("sesMcontDate") %>" />
											<input type="hidden" name="hashedPassword" id="hashedPassword" />
											<input type="hidden" name="randomSalt" id="randomSalt" value="<%=rsalt %>" />   
											<input type="hidden" id="SUBORGCODE" value="${SUBORGCODE}">
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