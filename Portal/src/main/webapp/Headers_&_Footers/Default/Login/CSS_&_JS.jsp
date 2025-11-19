<title>Login</title>

	<link href='<spring:url value="/resources/Default/css/atlantis.min.css" />' rel="stylesheet">
	<link href='<spring:url value="/resources/Default/css/bootstrap.min.css" />' rel="stylesheet">
	<link href='<spring:url value="/resources/Default/css/fonts.min.css" />' rel="stylesheet">
	<link href='<spring:url value="/resources/Default/img/scb_fav.jpeg" />' rel="shortcut icon">
	<link href='<spring:url value="/resources/Default/css/login.css" />' rel="stylesheet">
	
	<!-- <link rel="icon" type="image/png" href='<spring:url value="/resources/Default/img/pulse_icon.png" />' >  -->
		
	<script type="text/javascript" src='<spring:url value="/resources/Default/Validations/Login/global.js" />' ></script> 										  															  
	<script type="text/javascript" src='<spring:url value="/resources/Default/Validations/Login/sha256.js" />' ></script> 													  
	   
	<script type="text/javascript" src='<spring:url value="/resources/Default/sweetalert/dist/sweetalert.min.js"/>' ></script>
	<script type="text/javascript" src='<spring:url value="/resources/Default/ajax/libs/jquery/3.5.0/jquery.min.js"/>' ></script>
	<script type="text/javascript" src='<spring:url value="/resources/Default/js/plugin/webfont/webfont.min.js"/>' ></script>
	<script type="text/javascript" src='<spring:url value="/resources/Default/js/core/bootstrap.min.js"/>' ></script>
	<script type="text/javascript" src='<spring:url value="/resources/Default/js/atlantis.min.js"/>' ></script>
	<script type="text/javascript" src='<spring:url value="/resources/Default/js/core/popper.min.js"/>' ></script>
	<script type="text/javascript" src='<spring:url value="/resources/Default/Validations/Login/Login.js" />' ></script> 

<style>

#cookieConsentBanner {
      position: fixed;
      bottom: 0;
      width: 100%;
      background-color: #333;
      color: white;
      text-align: center;
      padding: 15px;
      z-index: 1000;
  }
  #cookieConsentBanner button {
      background-color: #4CAF50;
      color: white;
      border: none;
      padding: 10px 20px;
      margin: 10px;
      cursor: pointer;
  }
  #cookieConsentBanner button.reject {
      background-color: #f44336;
  }
  
    .switch {
        position: relative;
        display: inline-block;
        width: 50px;
        height: 24px;
    }

    .switch input {
        opacity: 0;
        width: 0;
        height: 0;
    }

    .slider {
        position: absolute;
        cursor: pointer;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: #ccc;
        transition: .4s;
        border-radius: 34px;
    }

    .slider:before {
        position: absolute;
        content: "";
        height: 20px;
        width: 20px;
        left: 2px;
        bottom: 2px;
        background-color: white;
        transition: .4s;
        border-radius: 50%;
    }

    input:checked + .slider {
        background-color: #4CAF50;
    }

    input:checked + .slider:before {
        transform: translateX(26px);
    }

    .slider.round {
        border-radius: 34px;
    }

	
</style>

</head>