//  MUKESH LAST FILE UPDATE  - 26-09-2024 01:05 PM
//  MUKESH LAST FILE UPDATE  - 26-09-2024 02:22 PM


$(document).ready(function() {  
	
	  $("#acctoupd").blur(function() { 
		 validation_Select("acctoupd");
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
	  
	 // get_list_of_acc_DD();	
	  
	  Account_Id_Suggestions();
	  	  
			
});


function proceed(){
			var Count = 0;
	       	
			var data = new FormData();
			
			if(!validation_Select("acctoupd"))      { Count++; }
	       	if(!validation("AccOwnr"))     { Count++; }
	       	if(!validation_Select("AccTy"))     { Count++; }
	    	if(!validation("EmlAd"))     { Count++; }
	    	if(!validation_Select("rolecd"))       { Count++; }
	    	if(!validation_Select("AccSts"))    { Count++; }

	    	console.log(Count);
	    	if (Count == 0){

		   	 data.append("USERSCD", $("#acctoupd").val()); 
			 data.append("UNAME", $("#AccOwnr").val()); 
			 data.append("USERTYPE", $("#AccTy").val()); 
			 data.append("EMAILADD", $("#EmlAd").val());
			 data.append("ROLECD", $("#rolecd").val());
			 data.append("USERSTS", $("#AccSts").val());
			 	
			 
	    		
	    		$.ajax({	
	    			url  :   $("#ContextPath").val()+"/Account_update/update_or_insert",
					type :  'POST',
					data :  data,
					cache : false,
					contentType : false,
					headers: { 'ReqType': "APPLICATION" },
					processData : false,
					success: function (data) 
					{ 
						if(data.Result == 'Success')
						{
							Sweetalert("success_load_Current", "", data.Message);
							
						}
						else
						{
							Sweetalert("warning", "", data.Message);
						}
					},
					beforeSend: function( xhr )
			 		{
			 			Sweetalert("load", "", "Please Wait");
			        },
				    error: function (jqXHR, textStatus, errorThrown) { }
			   });
	    		
	    	}
	    		
}



function get_list_of_acc_DD(){
		       				 
	    		var data = new FormData();
	    		$.ajax({	
	    			url  :  $("#ContextPath").val()+"/Acount_update/list_of_account",
					type :  'POST',
					data :  data,
					cache : false,
					contentType : false,
					processData : false,
					success: function (data) 
					{ 
						
						if(data.Result == 'Success')
						{
							Swal.close();
														
							var rateArr = data.Accounts_DD;
							
							for(let i = 0 ; i < rateArr.length ; i++){
								console.log(rateArr[i]);
								
								  $('#acctoupd').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));
								  
								  $("#acctoupd").change(function(){
									  Retrieve_submodule();
								 });	
																
							}
						}
						else
						{
							
						}
					},
					beforeSend: function( xhr )
			 		{
			 			
			        },
				    error: function (jqXHR, textStatus, errorThrown) { }
			   });
	    		
	    	}



function Retrieve_submodule()
{
	
	var data = new FormData();
	
	data.append("USERSCD", $("#acctoupd").val());
			
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Acount_update/submodule_Module/Data_retrieve",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{   
			Swal.close();
			
			if(data.Result == 'Success')
			{
							
				$("#AccOwnr").val(data.UNAME);
				$("#AccTy").val(data.USERTYPE);
				$("#EmlAd").val(data.EMAILADD);
				$("#rolecd").val(data.ROLECD);
				$("#AccSts").val(data.USERSTS);
							
			}
			
				if(!validation_Select("acctoupd"))      { Count++; }
		       	if(!validation("AccOwnr"))     { Count++; }
		    	if(!validation_Select("AccTy"))     { Count++; }
		    	if(!validation("EmlAd"))     { Count++; }
		    	if(!validation_Select("rolecd"))    { Count++; }
		    	if(!validation_Select("AccSts"))    { Count++; }
		},
		beforeSend: function( xhr )
 		{
 			Sweetalert_closeOnConfirm("load", "", "Please Wait");
        },
	    error: function (jqXHR, textStatus, errorThrown) {  }
   });
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

function Account_Id_Suggestions()
{
	$("#acctoupd").autocomplete({
		source: function(request, response) 
		{
	        $.ajax({
	            url: $("#ContextPath").val()+"/suggestions/AccUpdate",
	            type :  'POST',
	            dataType: "json",
	            data:  
				{    term : request.term	    
				},
	            success: function(data) { response(data); }
	        });
    	},
	    minLength: 1,
	    select: function(event, ui) 
	    {
	    	$("#acctoupd").val(ui.item.label);
	    	    	
	    	Retrieve_submodule();
	    }
	 }).autocomplete( "instance" )._renderItem = function(ul,item) 
	  	{
		 	return $( "<li><div>"+item.label+"</div></li>" ).appendTo(ul);
		}; 	
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