<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.math.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="ISO-8859-1">
<title>404</title>

<link href='<spring:url value="/resources/Default/css/404-Page/style.css" />' rel="stylesheet"> 

</head>

<body>
	
	 <div id="notfound">
		<div class="notfound">
			<div class="notfound-404" style="margin-bottom: 60px">
				<h1>Oops!</h1>
			</div>
			<h2>404 - Page not found</h2>
			<p>The page you are looking for might have been removed had its name changed or is temporarily unavailable.</p>
			<a href="<%= request.getContextPath() %>/">Go To login</a>
		</div>
	</div>
	
</body>
</html>