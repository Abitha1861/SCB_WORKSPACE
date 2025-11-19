var EROWS = [];  var DROWS = [];  var REPORTSL = "";

$(document).ready(function() {
	
	
	$("#t1_add_header").click(function() { 
		
		Add_Header('#t1_headers');
	});
	
	$("#event_creation").click(function() { 
		
		Create_Event();
	});
	
	$("#form_reset").click(function() { 
		
		Reset();
	});
	
	$("#module").change(function(){
		
		Retrieve_submodulecode001();
		
	});
	
	$("#submodule").change(function(){
		
		get_events();
		
	});
	
	$("#event_name").change(function(){
		
		get_batch_id();
		
	});
	
	
	get_modulecode001();
});

function Reset()
{
	$("#report_data").hide();
	$("#event_creation").show();
	$("#act_grp").hide();
}

function Create_Event()
{
	var err = 0;

	if(!validation("module", "dd"))          { err++; }
	if(!validation("submodule", "dd"))     { err++; }
	if(!validation("event_name", "txt"))     { err++; }
	
	
	if(err !=0 )
	{
		return;
	}
	
	var data = new FormData();
	
	data.append("Module", $("#module").val());  
	data.append("Submodule", $("#submodule").val());
	data.append("Event_Code", $("#event_name").val());
	data.append("BATCH_ID", $("#batch_id").val());
	
	console.log($("#batch_id").val());
	
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Event_Run/Generate_Report",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{   
			Swal.close();
			
			if(data.Result == "Success")
			{
				
				
				load_data(data);
				
				$("#report_data").show();
				$("#event_creation").hide();
				
				$("#act_grp").show();
				$("#reportsl").val(data.REPORTSL);
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
	    error: function (jqXHR, textStatus, errorThrown) { Sweetalert("warning", "", "errrrr");  }
   });
}

function Push_Data()
{
	var err = 0;
 
	if(!validation("module", "dd"))          { err++; }
	if(!validation("submodule", "dd"))     { err++; }
	if(!validation("event_name", "txt"))     { err++; }
	if(err !=0 )
	{
		return;
	}
 
	var data = new FormData();
	data.append("BatchId", $("#batch_id").val());  
	data.append("ServiceCd", $("#event_name").val());
	data.append("ReportSl", $("#reportsl").val());
	console.log($("#reportsl").val());
	console.log($("#event_name").val());
	console.log($("#batch_id").val());
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Request/Dispatcher",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{   
			Swal.close();
			if(data.result == "success")
			{
				Sweetalert("success", "", data.message);
			}
			else
		    {
				Sweetalert("warning", "", data.message);
		    }	
		},
		beforeSend: function( xhr )
		{
			Sweetalert("load", "", "Please Wait");
        },
	    error: function (jqXHR, textStatus, errorThrown) { Sweetalert("warning", "", "exception has occured");  }
   });
}

function get_batch_id()
{     				 
	var data = new FormData();

	data.append("EVENT_NAME", $("#event_name").val());
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Configuration/Batch_id",
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
				$('#batch_id').empty();
				$('#batch_id').append($("<option></option>").text("Select").val("Select"));
				for(let i = 0 ; i < rateArr.length ; i++)
				{
					$('#batch_id').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));
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

function load_data(dtl)  
{
	
	
	REPORTSL = dtl.REPORTSL;
	
	var Headers = dtl.Headers;
	
	var hd_content = '<ul class="nav nav-pills nav-secondary" id="pills-tab" role="tablist">';
	
	var bd_content = '<div class="tab-content mt-2 mb-3" id="pills-tabContent">';
	
	for(var z=0; z<Headers.length; z++)
	{
		hd_content+= '<li class="nav-item">' +
		                '<a class="nav-link '+(z==0 ? 'active' : '')+'" id="pills-home-tab" data-toggle="pill" href="#'+Headers[z]+'" role="tab" aria-controls="'+Headers[z]+'" aria-selected="'+(z==0 ? 'true' : 'false')+'">'+Headers[z]+'</a>'+
		              '</li>';
		
		var tbname = 'example_'+z;
		
		bd_content += '<div class="tab-pane fade '+(z==0 ? 'show active' : '')+'" id="'+Headers[z]+'" role="tabpanel" aria-labelledby="pills-home-tab">';
		
		bd_content += '<table id="'+tbname+'" class="table table-striped table-hover table-bordered dt-responsive" aria-describedby="example_info"></table>';
		
		bd_content += '</div>';
	}
	
	hd_content+= '</ul>';
	
	bd_content+= '</div>';
		
	$('.data_report').html(hd_content + bd_content);
	
	for(var z=0; z<Headers.length; z++)
	{
		var DRTL = dtl.details_list[z];
		
		var tbname = 'example_'+z;
		
		
		
		EROWS = [];  DROWS = []; REPORTSL = "";
		
		REPORTSL = dtl.REPORTSL;
		
		var Heading_details = DRTL.Heading_details;
		var Columns_details = DRTL.Columns_details;
		var Edit_columns_details = DRTL.Edit_columns_details;
		var Report_details = DRTL.Report_details;
		var Access_details = DRTL.Access_details;

		var len = Columns_details.length - 1;
		
		Columns_details = eval(Columns_details);
		Edit_columns_details = eval(Edit_columns_details);
		Report_details = eval(Report_details);
		
		var table = $('#'+tbname).DataTable( {
			  "aaData": Report_details,
			  "aoColumns": Columns_details,
			  "paging":true,
			  "destroy": true,
			  "deferRender": true,
			  "responsive": true,
			  "ordering": false,
			  "dom" :
				    "<'row'<'col-sm-4'l><'col-sm-4 text-center'><'col-sm-4'f>>"+
				    "<'row'<'col-sm-12'tr>>" +
				    "<'row'<'col-sm-6'i><'col-sm-6'p>>", 
			 "columnDefs": [
		            {
		                "targets": [ 0 ],
		                "visible": false,
		                "searchable": true
		            }
			    ],
	          "lengthMenu": [[5, 10, 50, 100, -1], [5, 10, 50, 100, "All"]],
			  "pageLength": 10					 
		}); 
		
		if(Access_details != null && Access_details != "") 
		{
			var example2 = new BSTable(tbname, {
				editableColumns: Access_details,   
				onEdit:function(e) {
					
					const elements = get_elements(e);
					
					console.log(e);
				
					EROWS.push(elements);
				},
				onDelete:function(e) {
					
					const elements = get_elements(e);
					
					DROWS.push(elements);
				},
				advanced: {
					columnLabel: 'Action'
				}
			});
			
			example2.init();	
		}	
	}
} 


function get_elements(e)
{
	const html = $(e).html();
	
	var result = [];

	var tr = document.createElement('tr');
	tr.innerHTML = html;

	var tds = tr.cells;
	
	for (var i=0, iLen = tds.length; i<iLen; i++) 
	{
		if (tds[i].children.length == 0) 
		{
			result.push(tds[i].textContent);
		}
	}
	
	return result.join("|");
}


function Save_Data()
{
	Sweetalert("success", "", "Changes updated successfully");
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
 			 
        },
	    error: function (jqXHR, textStatus, errorThrown) { }
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
				$('#submodule').empty();
				$('#submodule').append($("<option></option>").text("Select").val("Select"));
				for(let i = 0 ; i < rateArr.length ; i++){
					console.log(rateArr[i]);
					$('#submodule').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));
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

function randomString(length) 
{
	var chars =  '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
	
    var result = '';

    for (var i = length; i > 0; --i) result += chars[Math.floor(Math.random() * chars.length)];

    return result;
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
		return false;
	}
	else
	{ 
		$("#"+Id+"_error").hide();	
		return true;
	}
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