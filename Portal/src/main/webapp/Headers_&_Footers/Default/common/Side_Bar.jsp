<div class="sidebar sidebar-style-2" data-background-color="${Menu.get('sidebar_color').getAsString()}">	 
			<div class="sidebar-wrapper scrollbar scrollbar-inner">
				<div class="sidebar-content">
					<div class="user">
						<div class="avatar-sm float-left mr-2">
							<img src="<%= session.getAttribute("sess_user_photo") %>" alt="..." class="avatar-img rounded-circle" id="user_img">
						</div>
						<div class="info">
							<a data-toggle="collapse" href="#collapseExample" aria-expanded="true">
								<span>
									<%=	session.getAttribute("sesUserName") %>
									<span class="user-level"><%=	session.getAttribute("sesRole") %></span>
									<span class="caret"></span>
								</span>
							</a>
							<div class="clearfix"></div>

							
						</div>
					</div>
					
					${Menu.get("Menu_Content").getAsString()}

				</div>
			</div>
		</div>
		
	<input type="hidden" id="ContextPath" value="<%= request.getContextPath() %>/Datavision" />
	<input type="hidden" id="ContextPath2" value="<%= request.getContextPath() %>" />
	
	<input type="hidden" id="ULOGISUBORG" value="<%=	session.getAttribute("sesDomainID") %>" />
	<input type="hidden" id="ULOGINSESSION" value="<%=	session.getAttribute("sesSessionID") %>" />
	<input type="hidden" id="ULOGINUSER" value="<%=	session.getAttribute("sesUserId") %>" />  
	<input type="hidden" id="ULOGINUSERIP" value="<%=	session.getAttribute("sessionIP") %>" />