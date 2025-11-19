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
	
	
		
	$("#upload_excel").click(function() { 
		
		
		upload();
		
		
	});
	
  
   
   
   

    
    get_modulecode001();
    
    
   
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
				
					for(let i = 0 ; i < rateArr.length ; i++)
				{
					$('#event_name').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));
				}
			}
		},
		beforeSend: function( xhr )
 		{
			$('#event_name').empty();
			$('#event_name').append($("<option></option>").text("Select").val("Select"));
			
		
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


function upload(){
	
	var data = new FormData();
    data.append("submodule",$("#submodule").val());
    data.append("servicecd",$("#event_name").val());
	data.append("Attachments", $("#file1")[0].files[0]);
	if($('#file1').val()!= "" ) 
	{ 
	   data.append("file","true");
	}
	else{
		data.append("file","false");
	}
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Excel_File_Upload",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{   
			if(data.result == "success")
			{
				
				
				validation_success();
			
				Sweetalert("success", "", data.message); 
			}
			else if(data.stscode=="HP001") 
		    {
				Swal.close();
				
				displayValidationArray(data.Info);
				
		    }	
			else if(data.stscode=="HP003")
		    {
				Sweetalert("warning", "", data.message);
				
		    }	
			else if(data.stscode=="HP004")
		    {
				Sweetalert("warning", "", data.message);
				
		    }	
			else{
				
				Sweetalert("warning", "", data.message);
			}
           
			
		},
		beforeSend: function( xhr )
 		{
 			Sweetalert("load", "", "Please Wait");
        },
	    error: function (jqXHR, textStatus, errorThrown) 
		{ 
	    	
	    }
   });
	
}



function displayValidationArray(validationArrayString) {
    
    const validationArray = JSON.parse(validationArrayString);

    
    const validationDiv = $('#validationJson');

   
    if (Array.isArray(validationArray)) {
        
    	const container = $('<div></div>').addClass('container-fluid');

        const table = $('<table></table>').addClass('table table-striped table-bordered');

       
        const headerRow = $('<tr></tr>').append(
            $('<th></th>').text('S. No.'),
            $('<th></th>').text('Row'),
            $('<th></th>').text('Column'),
            $('<th></th>').text('Error')
        ).addClass('thead-dark');
        table.append(headerRow);

        
        const tableBody = $('<tbody></tbody>');
        var index=1;
       
        validationArray.forEach(error => {
        	var indexnig=index++;
            const row = $('<tr></tr>').append(
            	$('<td></td>').text(indexnig),
                $('<td></td>').text(error.Row),
                $('<td></td>').text(error.Column),
                $('<td></td>').text(error.Reason)
            );
            tableBody.append(row);
        });

       
        table.append(tableBody);

        
        validationDiv.empty().append(table);

        
        $(document).ready(function () {
            $('.myTable').DataTable({
                "paging": true,
                "searching": true,
                "scrollX": true,
                "scrollCollapse": true, 
               
            });
        });
        validationDiv.css('text-align', 'center');
    } else {
       
        validationDiv.empty();
    }
}

function validation_success(){
	
	const validationDiv = $('#validationJson');
	validationDiv.empty();
    
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
	
	function load_data(details)
	{
		alert("hi");
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

