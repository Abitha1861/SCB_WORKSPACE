$(document).ready(function() {
	
	var DB = $("#DBSTATUS").val();
	
	if(DB == "false")
	{
	   alert('Technical issue while connecting to the Database, Please contact Database admin');
	   
	   return false;
	}
	
	 window.addEventListener('load', function() {
	        if (!getCookie('userConsent')) {
	            showConsentBanner();
	        }
	    });
	 

	 document.getElementById('acceptCookies').onclick = function() {
	     setCookie('userConsent', 'accepted', 365);
	     document.getElementById('cookieConsentBanner').style.display = 'none';
	     // Load non-essential scripts or cookies here
	     console.log('Cookies accepted');
	 };

	 // Event listener for rejecting cookies
	 document.getElementById('rejectCookies').onclick = function() {
	     setCookie('userConsent', 'rejected', 365);
	     document.getElementById('cookieConsentBanner').style.display = 'none';
	     console.log('Cookies rejected');
	 };
	
	$('#txtPwd').keypress(function (e) {
		
		 var key = e.which;
		 
		 if(key == 13)  
		 {
			 document.getElementById("txtPwd").setAttribute('type', 'password');

			 revalidate(); 
		 }
	});  
	
	$('#loginbtn').click(function (e) {
		 
		 document.getElementById("txtPwd").setAttribute('type', 'password');
		
		  revalidate();
	});   
	
	$('#cancelbtn').click(function (e) {
		 
		 
		  clearFields();
	});   
	
	$('#txtPwd').on("cut copy paste",function(e) {
	      e.preventDefault();
	});
	
	$('#toggle').click(function (e) {
		 
		var eyeicon = document.getElementById("toggle");
		var passinp = document.getElementById("txtPwd");

			if(passinp.type === 'password'){
				passinp.setAttribute('type', 'text');
				eyeicon.classList.remove('fa-eye');
				eyeicon.classList.add('fa-eye-slash');
			}else{
				passinp.setAttribute('type', 'password');
				eyeicon.classList.add('fa-eye');
				eyeicon.classList.remove('fa-eye-slash');
			}
		
	});   
	
	
	var isIE = false || !!document.documentMode;
	var isEdge = !isIE && !!window.StyleMedia;
	var showButton = !(isIE || isEdge)
	if (!showButton) {
	    document.getElementById("toggle").style.display = "none";
	}
	
	LOADER3();
	
});

	
	
	var w_LastEntdFld;
	var w_Token;
	var w_RtnVal;
	var w_SaveFlg;
	var w_Ok;
	var w_Addr;
	var w_HostAddr;
	var w_HostName;
	var w_ErrMsg;
	var revalidationFlag = false;
	
	function LOADER3()
    {
		document.getElementById('loginForm').setAttribute("autocomplete", "off");
		document.getElementById('txtDomainId').setAttribute("autocomplete","off");
		document.getElementById('txtUserId').setAttribute("autocomplete","off");
		document.getElementById('txtPwd').setAttribute("autocomplete","off");
		
  		clearFields();
  		
  		document.getElementById('hidColor').value=0;
	  	 
  		document.getElementById('txtDomainId').value = $("#SUBORGCODE").val();
  		document.getElementById("chkDissolvedate").checked = false;
  		document.getElementById('randomSalt').value=$("#randomSalt").val(); 		
  }
	
  function revalidate()
  {	     
  	 	document.getElementById('txtDomainId').value= $("#SUBORGCODE").val();
  	 	
  	 	document.getElementById("txtPwd").setAttribute('type', 'password');
  	 	
       	var errorCount = 0;
       	
       	if(!isvc_txtDomainId())
       	{
       		errorCount++;
       	} 
       	
       	if(!isvc_txtUserId())	
       	{
       		errorCount++;
       	}
       	
       	if(!isvc_txtPwd())	
       	{
			errorCount++;
  		}
       
  		if(errorCount == 0)
  		{	
  			if($("#adSwitch").prop('checked') === true)
  			{
  				var ciphertext = des(document.getElementById("txtUserId").value+document.getElementById("txtPwd").value, 1, 0); 
  				
  	       		var EncStr = hex_sha256(ciphertext);
  	       		
  	       	 	var finalHash = doEncrypt(EncStr,document.getElementById('randomSalt').value);
  	       	 	
  	   			document.getElementById("hashedPassword").value = document.getElementById("txtPwd").value;
  	   		
  	   			document.getElementById("txtPwd").value="xxxxxxxyyyyyyyyyyyyyzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
  	   			
  				proceed_Adlogin();
  			}
  			else
  			{
  				var ciphertext = des(document.getElementById("txtUserId").value+document.getElementById("txtPwd").value, 1, 0); 
  				
  	       		var EncStr = hex_sha256(ciphertext);
  	       		
  	       	 	var finalHash = doEncrypt(EncStr,document.getElementById('randomSalt').value);
  	       	 	
  	   			document.getElementById("hashedPassword").value = finalHash;
  	   		
  	   			document.getElementById("txtPwd").value="xxxxxxxyyyyyyyyyyyyyzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
  	   			
  	   			proceed_login();
  			}	
       	}
  		else
  		{
       	 	Sweetalert("warning", "", "User Id or Password Should Not Be Blank");
       	 	
       		return false;
 		}
  }
  
  function proceed_login()
  {
	    var data = new FormData();
		
	    //data.append('INTFLAG' , $("#CSRFTOKEN").val());
		data.append('txtDomainId' , $("#txtDomainId").val());
		data.append('txtUserId' , $("#txtUserId").val());
		data.append('hashedPassword' , $("#hashedPassword").val());  
		data.append('randomSalt' , $("#randomSalt").val());
		
	   $.ajax({		 
			url  :  $("#ContextPath").val() + "/login/validate",
			type :  'POST',
			data :  data,
			cache : false,
			contentType : false,
			processData : false,
			success: function (data) 
			{ 
				
				
				if(data.Result == 'Success')
				{
					window.location = data.Action;
					
					
				}
				else
				{
					$("#txtPwd").val('');
					
					Sweetalert("warning", "", data.Message);
				}
			},
			beforeSend: function( xhr )
	 		{
	 			Sweetalert("load", "", "Please Wait");
	        },
		    error: function (jqXHR, textStatus, errorThrown) 
		    { 
		    	alert(errorThrown);
		    }
	   });
  }
  
  function proceed_Adlogin()
  {
	    var data = new FormData();
		
	    //data.append('INTFLAG' , $("#CSRFTOKEN").val());
		data.append('txtDomainId' , $("#txtDomainId").val());
		data.append('txtUserId' , $("#txtUserId").val());
		data.append('hashedPassword' , $("#hashedPassword").val());  
		data.append('randomSalt' , $("#randomSalt").val());
		
	   $.ajax({		 
			url  :  $("#ContextPath").val() + "/AD/login/validate",
			type :  'POST',
			data :  data,
			cache : false,
			contentType : false,
			processData : false,
			success: function (data) 
			{ 
				
				
				if(data.Result == 'Success')
				{
					window.location = data.Action;
					
					
				}
				else
				{
					$("#txtPwd").val('');
					
					Sweetalert("warning", "", data.Message);
				}
			},
			beforeSend: function( xhr )
	 		{
	 			Sweetalert("load", "", "Please Wait");
	        },
		    error: function (jqXHR, textStatus, errorThrown) 
		    { 
		    	alert(errorThrown);
		    }
	   });
  }
  
  function clearFields()
  {
	  document.all.txtUserId.value="";
	  document.all.txtPwd.value="";
	  w_HostAddr = "0";
	  w_HostName = "0";
	  w_ErrMsg = "";
	  w_Ok = false;
	  document.all.txtUserId.focus();
	  w_SaveFlg = 1;	
  }
  
  function validateDomainId(obj)
  {
		isvc_txtDomainId();	
  }
  
  function isvc_txtDomainId()
  {
		if(isBlank(document.getElementById('txtDomainId').value))
		{
			return false;
		}
		else
		{
			return true;
		}
  }
  
  function validateUserId()
  {
		isvc_txtUserId();
  } 
  
   function isvc_txtUserId()	
   {
		if(isBlank(document.getElementById('txtUserId').value))
		{
			return false;
		}
		else
		{
			return true;
		}
   }
   
	function validatePwd(obj)
	{	
		isvc_txtPwd();
	} 
	 
	function isvc_txtPwd()	
	{
		if(isBlank(document.getElementById('txtPwd').value))
	{
		return false;
	}
	else
	{
		return true;
	}
 } 

function isBlank(val)	
{
	if(val == undefined || val == null || val.trim() == "")	
	{ 
		return true;
	}
	
	return false;
}

function setCookie(name, value, days) {
    let expires = "";
    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days*24*60*60*1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "") + expires + "; path=/";
}

// Function to get a cookie by name
function getCookie(name) {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

// Function to show the consent banner
function showConsentBanner() {
    document.getElementById('cookieConsentBanner').style.display = 'block';
}



function Sweetalert(Type, Title, Info)
{
	if(Type == "success")
	{
		Swal.fire({
			  icon: 'success',
			  title: Title ,
			  text:  Info,		 		  
			  timer: 10000,
			  showConfirmButton: false 
		});
	}
	else if(Type == "success_load_Current")
	{
		Swal.fire({
			  icon: 'success',
			  title: Title ,
			  text:  Info,
			  timer: 10000
		}).then(function (result) {
			  if (true)
			  {							  
				  location.reload();
			  }
		});		
	}
	else if(Type == "error")
	{
		Swal.fire({
			  icon: 'error',
			  title: Title ,
			  text:  Info ,		 
			  timer: 10000,
			  showConfirmButton: false 
		});
	}
	else if(Type == "warning")
	{
		Swal.fire({
			  icon: 'warning',
			  title: Title ,
			  text:  Info ,		 
			  timer: 10000,
			  showConfirmButton: false 
		});
	}
	else if(Type == "load")
	{
		Swal.fire({
			  title: Title,
			  html:  Info,
			  timerProgressBar: true,
			  allowOutsideClick: false,
			  onBeforeOpen: () => {
			    Swal.showLoading()
			  },
			  onClose: () => {
			  }
		});	
	}	
}