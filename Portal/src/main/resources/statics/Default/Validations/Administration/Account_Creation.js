//  MUKESH LAST FILE UPDATE  - 26-09-2024 01:05 PM
//  MUKESH LAST FILE UPDATE  - 26-09-2024 02:22 PM


$(document).ready(function() {  
	
	  $("#Accnm").blur(function() { 
		 validation("Accnm");
	  });
	  $("#AccOwnr").blur(function() {  
		 validation("AccOwnr");
	  });
	  $("#AccTy").blur(function() {  
		  validation_Select("AccTy");
	  });
	  $("#EmlAd").blur(function() {  
		  validation("EmlAd");
	  });
	  $("#rolecd").blur(function() {  
		  validation_Select("rolecd");
	  });
	  $("#AccSts").blur(function() {  
		  validation_Select("AccSts");
	  });
							
			
});


function proceed(){
			
		var Count = 0;
		
		//alert('Hii1');
       					
       	if(!validation("Accnm"))      { Count++; }
       	if(!validation("AccOwnr"))     { Count++; }
    	if(!validation_Select("AccTy"))     { Count++; }
    	if(!validation("EmlAd"))     { Count++; }
    	if(!validation_Select("rolecd"))    { Count++; }
    	if(!validation_Select("AccSts"))    { Count++; }

    	
    	if (Count == 0){
	
			//alert('Hii2');
		
			var accountName =  $("#Accnm").val();
	        var accountOwner = $("#AccOwnr").val();
	        var accountType = $("#AccTy").val();
	        var emailAddress = $("#EmlAd").val();
	        var accountStatus = $("#AccSts").val();
	        var accountDescription =  "";
	        var isPrivileged = "";
	        var lastLogin = "";
	        var roleName = $("#rolecd").val();
	        
	
			//alert('Hii4');
		
			// Create the JSON object dynamically
			var jsonData = {
			    "accountName": accountName,
			    "accountOwner": accountOwner,
			    "accountType": accountType,
			    "emailAddress": emailAddress,
			    "accountStatus": accountStatus,
			    "accountDescription": accountDescription,
			    "isPrivileged": isPrivileged,
			    "lastLogin": lastLogin,
			    
			    "roles" : [
			        {
			            "roleName": roleName
			        }
			    ]
			};
			    			    				 
			$.ajax({
			    url: $("#ContextPath").val()+"/accounts/createAccount/Application",  // Replace with your endpoint
			    type: 'POST',  					   			      // Request type (GET, POST, PUT, DELETE, etc.)
			    contentType: 'application/json',    			  // Content type of the request
			    headers: { 'ReqType': "APPLICATION" },
			    data: JSON.stringify(jsonData),   				  // The JSON data to send in the request			    
			    success: function (data) 
				{ 
					if(data.StatusCode == '200')
					{
						//alert('Hii5');
						Sweetalert_closeOnConfirm("success_load_Current", "", data.StatusMessage);
						
					}
					else
					{
						//alert('Hii6');
						Sweetalert_closeOnConfirm("warning", "", data.StatusMessage);
					}
				},
			    error: function(error) {          // Callback function for request error
			        //alert('Hii7');
			        console.error('Error:', error);
			    }
			});
			    		
    		
    	}
		   		
}



function proceed_bkp(){
			
		var Count = 0;
       	
		var data = new FormData();
		
		//alert("HI11");
		
       	if(!validation("Accnm"))      { Count++; }
       	if(!validation("AccOwnr"))     { Count++; }
    	if(!validation("AccTy"))     { Count++; }
    	if(!validation_Select("rolecd"))    { Count++; }
    	if(!validation_Select("AccSts"))    { Count++; }

    	
    	if (Count == 0){
    		
		 data.append("SUBORGCODE" , $("#orgcd").val());
		 data.append("USERSCD", $("#Accnm").val());
		 data.append("UNAME", $("#AccOwnr").val());
		 data.append("USERTYPE", $("#AccTy").val()); 
		 data.append("ROLECD", $("#rolecd").val());
		 data.append("USERSTS", $("#AccSts").val() === 'ACTIVE' ? "1" : "0");
		 
						
    		$.ajax({	
    			url  :   $("#ContextPath").val()+"/OneCert/accounts/create/application",
				type :  'POST',
				data :  data,
				cache : false,
				contentType : false,
				processData : false,
				success: function (data) 
				{ 
					if(data.StatusCode == '200')
					{
						Sweetalert_closeOnConfirm("success_load_Current", "", data.StatusMessage);
						
					}
					else
					{
						Sweetalert_closeOnConfirm("warning", "", data.StatusMessage);
					}
				},
				beforeSend: function( xhr )
		 		{
		 			Sweetalert_closeOnConfirm("load", "", "Please Wait");
		        },
			    error: function (jqXHR, textStatus, errorThrown) { }
		   });
    		
    	}
		   		
}




function validation(id){
			
			var input_control = document.getElementById(id);
			
			var Validate = false;
			
			if(input_control.value == "" || input_control.value == null || input_control.value == "undefined" || input_control.value.trim() == ""){
				Validate = false;
				$("#"+id+"_error").html("Required");
				$("#"+id+"_error").show();
			}else{
				Validate = true;
				$("#"+id+"_error").html("");
				$("#"+id+"_error").hide();
				
			}
			return Validate;
}


function validation_Select(id){
	
	var input_control = document.getElementById(id);
	
	var Validate = false;
	
	if(input_control.value == "Select" || input_control.value == "" ){
		Validate = false;
		$("#"+id+"_error").html("Required");
		$("#"+id+"_error").show();
	}else{
		Validate = true;
		$("#"+id+"_error").html("");
		$("#"+id+"_error").hide();
		
	}
	return Validate;
}


function reset()              /* Reset Function */
	{
		location.reload();
		//$('form :input').val('');
	}


function Sweetalert_closeOnConfirm(Type, Title, Info)
{
	if(Type == "success")
	{
		Swal.fire({
			  icon: 'success',
			  title: Title ,
			  text: Info ,			 
			  allowOutsideClick: false,
			  closeOnClickOutside: false,
			  closeOnConfirm: true
		});
	}
	else if(Type == "success_load_Current")
	{
		Swal.fire({
			  icon: 'success',
			  title: Title ,
			  text:  Info,
			  allowOutsideClick: false,
			  closeOnClickOutside: false,
			  closeOnConfirm: true
			  
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
			  allowOutsideClick: false,
			  closeOnClickOutside: false,
			  closeOnConfirm: true
		});
	}
	else if(Type == "warning")
	{
		Swal.fire({
			  icon: 'warning',
			  title: Title ,
			  text:  Info ,		 
			  allowOutsideClick: false,
			  closeOnClickOutside: false,
			  closeOnConfirm: true
		});
	}
	else if(Type == "validation")
	{
		Swal.fire({
			  icon: 'warning',
			  title: Title ,
			  html: Info
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