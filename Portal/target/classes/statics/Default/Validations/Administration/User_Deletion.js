
$(document).ready(function() {
	
	
	/* $("#proceed").click(function() {
			proceed();
	 });*/
	 
	 $("#chek_sts").click(function() {
			QUEUECHECKER();
		});	
	 document.getElementById("proceed").addEventListener("click", function() {
		 
		 
		    // Validate fields
		    if ($("#tuserid").val() == "") {
		        $("#tuserid_error").html('Required');
		        $("#tuserid_error").show();
		        return;
		    } else {
		        $("#tuserid_error").hide();
		    }

		    if ($("#tbublock").val() == "") {
		        $("#tbublock_error").html('Required');
		        $("#tbublock_error").show();
		        return;
		    } else {
		        $("#tbublock_error").hide();
		    }

		    if ($("#trolecd1").val() == "") {
		        $("#trolecd1_error").html('Required');
		        $("#trolecd1_error").show();
		        return;
		    } else {
		        $("#trolecd1_error").hide();
		    }

		    // Trigger SweetAlert confirmation
		    Swal.fire({
		        icon: 'warning',
		        title: 'Are you sure?',
		        text: 'Do you want to delete this user?',
		        showCancelButton: true,
		        confirmButtonColor: '#3085d6',
		        cancelButtonColor: '#d33',
		        confirmButtonText: 'Yes, delete it!',
		        cancelButtonText: 'No, cancel!',
		        reverseButtons: false
		    }).then((result) => {
		    	console.log(result);
		        if (result.value === true) {
		            // User clicked 'Yes'
		            var data = new FormData();
		            data.append("tuserid", $("#tuserid").val());
		            
		            $.ajax({
		                url: $("#ContextPath").val() + "/Role_Delete_User",
		                type: 'POST',
		                data: data,
		                cache: false,
		                contentType: false,
		                headers: { 'ReqType': "APPLICATION" },
		                processData: false,
		                success: function(data) {
		                    if (data.Result == 'Success') {
		                        Sweetalert("success_load_Current", "", data.Message);
		                    } else {
		                        Sweetalert("warning", "", data.Message);
		                    }
		                },
		                beforeSend: function(xhr) {
		                    Sweetalert("load", "", "Please Wait");
		                },
		                error: function(jqXHR, textStatus, errorThrown) {
		                    console.error("AJAX Error: ", textStatus, errorThrown);
		                    console.error("Response: ", jqXHR.responseText);
		                    Sweetalert("error", "", "An error occurred. Please try again.");
		                }

		            });
		        } else if (result.dismiss === 'cancel') {
		            // User clicked 'No'
		            Swal.fire(
		                'Cancelled',
		                'The user is safe :)',
		                'info'
		            );
		        }
		    });
		});


	
});
	
	

function proceed()
	{
		
	
	    var data = new FormData();
			
		data.append("tuserid" , $("#tuserid").val());
		data.append("trolecd" , $("#trolecd1").val());
		data.append("tbublock" , $("#trolecd").val());
		data.append("mode", $("#mode").val());
			
	   $.ajax({		 
			url  :  $("#ContextPath").val()+ "/Role_Delete_User",
			type :  'POST',
			data :  data,
			cache : false,
			contentType : false,
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
   
   	function QUEUECHECKER()
	{
		if($("#tuserid").val() == "")
		{
			$("#tuserid_error").html('Required');
			$("#tuserid_error").show();
			
			return;
		}
		else
		{
			$("#tuserid_error").hide();
			$("#chek_sts").hide();
			$("#tuserid").prop('readonly', true);
			
		}	
		
		var data = new FormData();
		
		data.append("tuserid" , $("#tuserid").val());
		data.append("torgcd" , $("#SUBORGCODE").val());  
		data.append("pgmID" , "UNBLOCKUSERID");
		
	   $.ajax({		 
			url  :  $("#ContextPath").val()+ "/ROLE_QUEUECHECKER",
			type :  'POST',
			data :  data,
			cache : false,
			contentType : false,
			processData : false,
			success: function (data) 
			{ 
				if(data.Result == 'Success')
				{
					if(data.SucFlg == "1" ) 
		            {
						 if(data.STATUS == "Blocked")
						 {
						 	$("#tbublock").val("0").attr('disabled','disabled');
						 	
						 	//trolecd
						 	
						 	$("#un_blocked").hide();
						 	
						 	$("#status_row, #action_row, #blocked").show();
						 	
						 	$("#block").attr('disabled','disabled');
						 }
						 else
						 {
							 $("#tbublock").val("1").attr('disabled','disabled');
						 	
						 	$("#blocked").hide();
						 	
						 	$("#status_row, #action_row, #un_blocked").show();
						 	
						 	$("#unblock").attr('disabled','disabled');
						 }
						 
						 document.getElementById('mode').value =  data.mode;
						 
						 $("#trolecd").val(data.ROLECODE).attr('disabled','disabled');
						 
						/* document.getElementById('mode').value =  data.mode;
						 
						 $("#trolecd1").val(data.ROLECODE).attr('enable','enable');
						  */
						  
						  var selectedValue = data.ROLECODE;
					        var $trolecd1 = $('#trolecd1');
					        
					        // Enable all options first
					        $trolecd1.find('option').prop('disabled', false);
					        
					        // Disable the selected option
					        if (selectedValue) {
					            $trolecd1.find('option[value="' + selectedValue + '"]').attr('style', 'display:none').prop('disabled', true);
					        }
					 }
		             else
		             {
		            	 $("#status_row, #action_row").hide();           	
		             }
				}
                 else if(data.Stscode='HP005'){
					
					Sweetalert("warning", "", data.Message);
					$("#proceed").attr("disabled", "disabled");
					
				}
			},
			beforeSend: function( xhr )
	 		{
	 			//Sweetalert("load", "", "Please Wait");
	 			$('#trolecd1').find('option').attr('style', 'display:block').prop('disabled', false);
	        },
		    error: function (jqXHR, textStatus, errorThrown) { }
	   });
	}
	
	function reset()
	{
		location.reload();
	}
	