<%@ include file="../../../Headers_&_Footers/Default/common/Header.jsp" %>
<%@ include file="../../../Headers_&_Footers/Default/Login/CSS_&_JS.jsp" %>   
<%@ page import="com.hdsoft.utils.PasswordUtils" %>
<%@ page import="com.hdsoft.utils.WebContext"%>

<%
    String rsalt = PasswordUtils.getSalt();
%>

<body>
    <div class="container login_box">
        <div class="row">
            <div class="col-sm-6 loginimage"> 
                <img src="<spring:url value="/resources/Default/img/Login_page_GIF.gif" />" />
            </div>
            
            <div class="col-sm-6">
                <div class="form_align">
                    <form id="loginForm" action="<%= request.getContextPath() %>/Datavision/login" method="post" autocomplete="off">
                      
                      <div class="container">
                        
                        <div id="c_logo" class="row mt--3 mb-3">
                            <div class="col col-sm-3"></div>  
                            <div class="col col-sm-6"><img src="<spring:url value="/resources/Default/img/scb_logo.jpeg" />" /></div>
                            <div class="col col-sm-3"></div>
                        </div> 
                      
                        <label for="uname"><b>Username</b></label>
                        <input type="text" id="txtUserId" placeholder="Enter Username" name="uname" required>

                        <label for="psw"><b>Password</b></label>
                        
                        <div class="passval">
                            <input type="password" id="txtPwd" placeholder="Enter Password" name="psw" required>
                            <div class="fas fa-eye" id="toggle"></div>
                        </div>
                        
                        <div class="row mt-2">
                            <div class="col-sm-12 text-center">
                                <label class="switch">
                                    <input type="checkbox" id="adSwitch" checked>
                                    <span class="slider round"></span>
                                </label>
                                <span>Use AD Account</span>
                            </div>
                        </div>

                      </div>
                      
                      <div class="container foot">
                        <div class="row">
                            <div class="col-sm-3">
                                <input type="hidden" id="SUBORGCODE" value="${SUBORGCODE}">
                                <input type="hidden" id="SYSCODE" value="${SYSCODE}">  
                                <input type="hidden" id="DBSTATUS" value="${DBSTATUS}">
                                
                                <input type="hidden" id="CSRFTOKEN" name="CSRFTOKEN" value="">
                                <input type="hidden" name="txtDomainId" id="txtDomainId">
                                <input type="hidden" name="hidColor" id="hidColor">
                                <input type="hidden" name="timeCheck1" id="timeCheck1">
                                <input type="hidden" name="timeCheck2" id="timeCheck2">
                                <input type="hidden" name="hashedPassword" id="hashedPassword" />
                                <input type="hidden" name="EncStr" id="EncStr" />
                                <input type="hidden" name="randomSalt" id="randomSalt" value="<%=rsalt %>" />
                                <input type="hidden" id="ContextPath" value="<%= request.getContextPath() %>/Datavision/">
                             </div>
                            
                            <div class="col-sm-2"><button type="button" class="loginbtn" id="loginbtn">Login</button></div>
                            <div class="col-sm-2"><button type="button" class="cancelbtn" id="cancelbtn">Cancel</button></div>
                            
                            <input type="hidden" name="" value=""/>
                            
                           <div class="col-sm-4 ml-3 mt-4 ">
                           </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-sm-2 mt-2" align="center">
                                 V4.05
                            </div>
                        </div>
                      </div>
                    </form>
                </div>    
            </div>
        </div>         
    </div>
    
    <div id="cookieConsentBanner" style="display:none;">
        <p>We use cookies to ensure you get the best experience on our website.</p>
        <button id="acceptCookies">Accept</button>
        <button id="rejectCookies" class="reject">Reject</button>
    </div>

</body>
</html>