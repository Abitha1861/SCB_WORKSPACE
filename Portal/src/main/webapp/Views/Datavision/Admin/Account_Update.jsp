<!--  MUKESH LAST FILE UPDATE  - 26-09-2024 01:05 PM
      MUKESH LAST FILE UPDATE  - 26-09-2024 02:22 PM
-->


<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>

<%@ include file="../../../Headers_&_Footers/Default/Administration/CSS_&_JS10.jsp" %>   
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

	<div class="wrapper sidebar_minimize">
	
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
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Choose Account To Update</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<!-- <select class="form-control" id="acctoupd">
												    <option value="">Select</option>
												</select> -->
												<input type="text" class="form-control" id="acctoupd">
												
												
												<label id="acctoupd_error" class="text-danger"></label>
											</div>
										</div>
									
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Account Owner</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<input type="text" class="form-control" id="AccOwnr" placeholder="" maxlength="20" size="15" disabled>
												<label id="AccOwnr_error" class="text-danger"></label>
											</div>
										</div>
									</div>
								
									<div class="row">	
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Account Type</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<select class="form-control" id="AccTy" disabled>
													<option value="">Select</option>
												    <option value="User">User</option>
													<option value="Generic">Generic</option>
											    </select>
												<label id="AccTy_error" class="text-danger"></label>
											</div>
										</div>
										
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Email Address</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<input type="text" class="form-control" id="EmlAd" placeholder="" >
												
												<label id="EmlAd_error" class="text-danger"></label>
											</div>
										</div>										
									</div>
																	
								
									<div class="row">	
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Role Code</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<select class="form-control" id="rolecd" disabled>
												    <option value="">Select</option>
													<option value="ADMIN">ADMIN - ACCESS MANAGEMENT ACTIVITY</option>
													<option value="CHKR">CHKR - UPLOAD MODULE APPROVER</option>
													<option value="MAKR">MAKR - UPLOAD MODULE MAKER</option>
													<option value="APPSUPPORT">APPSUPPORT - PSS SUPPORT MEMBER FOR APPLICATION</option>
													<option value="REPORTVIEWER">REPORTVIEWER - USERS HAVE ACCESS TO REPORT</option>
													<option value="DASHBRDVIEWER">DASHBRDVIEWER - USERS HAVE ACCESS TO DASHBOARD</option>
												</select>
												<label id="rolecd_error" class="text-danger"></label>
											</div>
										</div>	
										<div class="col-md-2">
											<div class="form-group">	
												<label for="email2">Account Status</label>
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">		
												<select class="form-control" id="AccSts">
													<option value="">Select</option>
												    <option value="Active">Active</option>
													<option value="Inactive">Inactive</option>
												</select>
												<label id="AccSts_error" class="text-danger"></label>
											</div>
										</div>	
									</div>
									
								</div>
								
								<div class="card-action">
									<div class="row">	
										
										<div class="col-md-6">
											<button class="btn btn-secondary float-right" id= "submit" onclick="proceed()">Submit</button>	
										</div>
										
										<div class="col-md-6">	
											<button class="btn btn-danger float-left" onclick="reset()">Reset</button>
											
											<input type="hidden" id="cmenuoption">
										    <input type="hidden" id="mode">
										    
										    <input type="hidden" name="CSRFTOKEN" value="${sessionScope['CSRFTOKEN']}">
											<input type="hidden" id="calcurrbusDate" value="<%= session.getAttribute("sesMcontDate") %>" />
											<input type="hidden" name="hashedPassword" id="hashedPassword" />
											<input type="hidden" name="randomSalt" id="randomSalt" value="<%=rsalt %>" />
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