$(document).ready(function() {
  
  	$("#t1_add_header").click(function() { 
		
		Add_Header('#t1_headers');
	});

	$("#module").change(function(){
		
		Retrieve_submodulecode001();
		
	});
	
	$("#submodule").change(function(){
		
		get_events();
		
	});	
	
	
	 
   $("#Download_excel").click(function(event) {
       
   	DownLoad_Excel_Sheet();
   	
      
   });
   
    record();
    
    get_modulecode001();
    download_validatio();
    
   
});

function download_validatio(){
	
	 $('#Download_excel').attr('disabled', true);
	    $('#module, #submodule, #event_name, #record').change(function() {
	       
	        var moduleValue = $('#module').val();
	        var submoduleValue = $('#submodule').val();
	        var eventNameValue = $('#event_name').val();
	        var recordValue = $('#record').val();

	        if (moduleValue !== '' && submoduleValue !== '' && eventNameValue !== '' && recordValue !== '') {
	          
	          $('#Download_excel').attr('disabled', false);
	        } else {
	          
	          $('#Download_excel').attr('disabled', true);
	        }

	   
		
	  });
}
function Retrieve_submodulecode001()
{
	var data = new FormData();
	
	data.append("MODULE_NAME", $("#module").val());
	
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Configuration/Event_Creation/submodule_Module/Data_retrieve",
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
				var rateArr = data.SubModule_Desc;
				
				for(let i = 0 ; i < rateArr.length ; i++){
					console.log(rateArr[i]);
					$('#submodule').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));
				}
			}
		},
		beforeSend: function( xhr )
 		{
			$('#submodule').empty();
			$('#submodule').append($("<option></option>").text("Select").val("Select"));
        },
	    error: function (jqXHR, textStatus, errorThrown) {  }
   });
}	



function get_modulecode001()
{				 
	var data = new FormData();
	
	$.ajax({	
		url  :  $("#ContextPath").val()+"/Configuration/Event_Creation/suggestion_Module",
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
				var rateArr = data.Report_code_DD;
				
				for(let i = 0 ; i < rateArr.length ; i++)
				{
					  $('#module').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));  
				}
			}
			else
			{
				
			}
		},
		beforeSend: function( xhr )
 		{
 			
 			 $("#module").append($('<option></option>').val("").html("Select")); 
        },
	    error: function (jqXHR, textStatus, errorThrown) { }
   });
	
}


	
function get_events()
{     				 
	var data = new FormData();
	
	data.append("MODULE", $("#module").val());
	data.append("SUB_MODULE", $("#submodule").val());
	
	
	
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Configuration/Events_Names/Data_retrieve",
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
				var rateArr = data.events;
				
				$('#event_name').empty();
				$('#event_name').append($("<option></option>").text("Select").val("Select"));
				
				for(let i = 0 ; i < rateArr.length ; i++)
				{
					$('#event_name').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));
				}
			}
		},
		beforeSend: function( xhr )
 		{
 			Sweetalert("load", "", "Please Wait");
        },
	    error: function (jqXHR, textStatus, errorThrown) {  }
   });
	    		
}

function DownLoad_Excel_Sheet()
{	
	
	event.preventDefault();
    
    
    var serviceCode = $("#event_name").val();  
    var numberOfRecords =  $("#record").val(); 
   
    var downloadUrl = $("#ContextPath2").val()+'/Excel-Download/?SERVICECD=' + encodeURIComponent(serviceCode) +
    '&NoOfRecords=' + encodeURIComponent(numberOfRecords);


var downloadLink = $('<a>')
    .attr('href', downloadUrl)
    .attr('download', 'filename.xlsx') 
    .appendTo('body');


downloadLink[0].click();


downloadLink.remove();
}

	function validation(Id, Type)
	{
		var valid = false;
		
		if(Type == "dd")
		{
			if($("#"+Id).val() != "Select" && $("#"+Id).val() != "") 
			{  
				valid = true;
			}
		}
		
		if(Type == "txt" && $("#"+Id).val() != '')
		{
			valid = true;
		}
		
		if(!valid)    
		{  
			$("#"+Id+"_error").html('Required');  
			$("#"+Id+"_error").show();	
			$("#submit").attr('disabled', true);
			return false;
		}
		else
		{ 
			$("#"+Id+"_error").hide();	
			
			return true;
		}
	}
	
	function Push_Data()
	{
		var err = 0;

		if(!validation("module", "dd"))          { err++; }
		if(!validation("submodule", "dd"))     { err++; }
		if(!validation("event_name", "dd"))     { err++; }
		if(!validation("record", "txt"))     { err++; }
		
		if(err !=0 )
		{
			return;
		}
		
	}
	
	function uploadfile()
	{
		
        var file = document.getElementById("file");
        if (file.style.display === "none" || file.style.display === "") {
        	file.style.display = "block";
        	$("#Upload_excel").hide();
        	$("#Download_excel").hide();
        	$("#upload").hide();
        	
        } else {
        	file.style.display = "none";
        	$("#upload").hide();
        }
	}
	
   function record(){
	   
		const record = document.getElementById("record");
	
		record.addEventListener("input", function() {
		    
	    const value = record.value;

	    if (parseFloat(value) < 0) {
	        
	        record.value = "0";
	        $("#record_error").html('Should be positive value');
			$("#record_error").show();
	    }
	    else{
	    	$("#record_error").hide();
	    }
	});
		
	function Upload_Data()
	{
		Sweetalert("success", "", "Changes updated successfully");
	}
	
	
	
	
	
	function Sweetalert(Type, Title, Info)
	{
		if(Type == "success")
		{
			Swal.fire({
				  icon: 'success',
				  title: Title ,
				  text: Info ,			 
				  timer: 2000
			});
		}
		else if(Type == "success_load_Current")
		{
			Swal.fire({
				  icon: 'success',
				  title: Title ,
				  text:  Info,
				  timer: 2000
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
				  timer: 2000
			});
		}
		else if(Type == "warning")
		{
			Swal.fire({
				  icon: 'warning',
				  title: Title ,
				  text:  Info ,		 
				  timer: 2000
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

  }

