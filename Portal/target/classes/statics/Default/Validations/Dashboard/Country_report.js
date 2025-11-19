$(document).ready(function() {
	
	//view();
	
	$("#get_value").attr('disabled','disabled');
	
	$('#myTable').DataTable();
	 
	 $("#apicode").blur(function(){
		 validate_apicode();
	 });
	 
	 $("#fdate").blur(function(){
		 validate_fdate();
	 });
	 
	 $("#tdate").blur(function(){
		 validateDates();
	 });
	 
	 $("#status").blur(function(){
		 validateReport();
	 });
	 
	 $("#respdesc").blur(function(){
		 validate_respdesc();
	 });
	
	$("#get_value").click(function(){
			Generate_Report();
	});
	
	$("#apicode").change(function(){
		
		get_batch_id();
		
	});
	 var defaultHeaders = ['S.No', 'Req Date', 'Req Time', 'Ref No', 'Batch Id', 'Api Code', 'Report Serial', 'Start sl', 'End sl', 'Bot Ref No', 'Status', 'Resp Code', 'Resp Desc'];
	
	function updateHeaders(inputValue) {
        
        var newHeaders;

        if (inputValue === 'Pending') {
         
          newHeaders = ['S.No', 'SUBORGCODE', 'SYSCODE', 'PAYTYPE', 'REQDATE', 'REFNO', 'REQSL', 'BATCHID', 'APICODE', 'REPORTSERIAL', 'STARTSL', 'ENDSL', 'STATUS'];
        } else {
		 
          newHeaders = defaultHeaders;
          
        }
        $('#myTable thead tr').empty(); 
        
        $.each(newHeaders, function(index, value) {
          $('#myTable thead tr').append('<th>' + value + '</th>');
        });
     	
	}
	
	$('#status').on('input', function() {
        var inputValue = $(this).val();
        
        updateHeaders(inputValue);
    });
	
	API_Code_Suggestions();
	
		
});

/* function revalidate()
{	     
   	var errorCount = 0;
   	
   	if(!validate_tdate())          { errorCount++; }
	if(!validate_apicode())          { errorCount++; }
	if(!validate_fdate())          { errorCount++; }//

		if(errorCount == 0)
		{	
			//proceed();
			Generate_Report();
		}
		else
		{
			return false;
		}
}


function validate_tdate()
{
	if($("#tdate").val() == "")
	{
		$("#tdate_error").html('Required');
		$("#tdate_error").show();
		return false;
	}
	else
	{
		$("#tdate_error").hide();
		return true;
	}	
}

function validate_fdate()
{
	if($("#fdate").val() == "")
	{
		$("#fdate_error").html('Required');
		$("#fdate_error").show();
		return false;
	}
	else
	{
		$("#fdate_error").hide();
		return true;
	}	
}

*/


function validateDates() {
      const fromDate = new Date(document.getElementById("fdate").value);
      const toDate = new Date(document.getElementById("tdate").value);
      
      if (fromDate > toDate) {
		  $("#tdate_span").html('Todate should be after Fromdate');
		  $("#tdate_span").show();
		  $("#get_value").attr("disabled","disabled");
		  return false; 
       }else
		{ 
			$("#tdate_span").hide();
			return true;
		} 
 }
 
function validate_apicode()
{
	if($("#apicode").val() == "")
	{
		$("#apicode_error").html('Required');
		$("#apicode_error").show();
		$("#get_value").attr('disabled','disabled');
		return false;
	
	}
	else
	{
		$("#apicode_error").hide();
		$("#get_value").attr('disabled',false);
		return true;
	}	
}

/*function validate_batchid(){
	
	if($("#apicode").val() != "" && $("#batchid").val() != ""){
		$("#batchid_error").hide();
		$("#get_value").attr('disabled',false);
		return false;
	}
}*/

function validateReport(){
	
	if ($("#status").val() == "Pending")
	{
		$("#respdesc").attr('disabled','disabled');
		$("#label_resp").attr('disabled','disabled');
		$("#get_value").attr('disabled',false);
		return true;
	}
	
	if($("#status").val() == "SUCCESS" || $("#status").val() == "FAILED"){
		$("#respdesc_error").html('Required');
		$("#respdesc_error").show();
		$("#get_value").attr('disabled','disabled');
		return false;
	}else
	{
		$("#respdesc_error").hide();
		$("#get_value").attr('disabled',false);
		return true;
	}	
	
}

function validate_respdesc()
{
	if($("#respdesc").val() == "Select" && ($("#status").val() == "SUCCESS" || $("#status").val() == "FAILED"))
	{
		$("#respdesc_error").html('Required');
		$("#respdesc_error").show();
		$("#get_value").attr('disabled','disabled');
		return false;
	}
	else
	{
		$("#respdesc_error").hide();
		$("#get_value").attr('disabled',false);
		return true;
	}	
}

function get_batch_id()
{     				 
	var data = new FormData();

	data.append("APICODE", $("#apicode").val());
	
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Configuration/Batch_id1",
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
				$('#batchid').empty();
				$('#batchid').append($("<option></option>").text("Select").val("Select"));
				for(let i = 0 ; i < rateArr.length ; i++)
				{
					$('#batchid').append($("<option></option>").text(rateArr[i]).val(rateArr[i]));
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
      
function Generate_Report()
{	
	var apicode = $("#apicode").val();
	var batchid = $("#batchid").val();
	var fdate = $("#fdate").val();
	var tdate = $("#tdate").val();
	var status = $("#status").val();
	var respdesc = $("#respdesc").val();
	
	var data = new FormData();
	
	data.append("APICODE",apicode);
	data.append("BATCHID",batchid);
	data.append("FDATE",fdate);
	data.append("TDATE",tdate);
	data.append("STATUS",status);
	data.append("RESPDESC",respdesc);
	

	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Rtsis/Report_filter",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{ 
			Swal.close();
			
			
			
			//alert(data.result);
			
			if(data.result == 'success')
			{	
				 $('.data_report').html('<table id="myTable" class="table table-striped table-hover table-bordered dt-responsive nowrap" aria-describedby="example_info"></table>');

				var reports = data.Info;
				
				//reports = eval(reports);
				
				table = $('#myTable').DataTable( {
				  "aaData": reports,
				  "aoColumns": [
					    {"sTitle" : "S.No","mData":"rowNum"},
					    {"sTitle" : "Req Date","mData":"REQDATE"},
						{"sTitle" : "Req Time", "mData": "REQTIME"},
						{"sTitle" : "Ref No", "mData": "REFNO"},
						{"sTitle" : "Batch Id","mData": "BATCHID"},
						{"sTitle" : "Api Code","mData":"APICODE"},
						{"sTitle" : "Report Serial", "mData": "REPORTSERIAL"},
						{"sTitle" : "Start sl", "mData": "STARTSL"},
						{"sTitle" : "End sl", "mData": "ENDSL"},
						{"sTitle" : "Bot Ref No","mData":"BOTREFNO"},
						{"sTitle" : "Status","mData": "STATUS"},
						{"sTitle" : "Resp Code", "mData": "RESCODE"},
						{"sTitle" : "Resp Desc", "mData": "RESPDESC"},
						{"sTitle" : "Action", "mData": "ACTION"}
					
				  ],
				  "dom": "<'row'<'col-sm-6'l><'col-sm-6 text-center'f>>"+
	  				     	 "<'row'<'col-sm-12'tr>>" +
	  				      "<'row'<'col-sm-4'i><'col-sm-4 text-center'B><'col-sm-4'p>>",
				  "buttons": [
				  	                   
				  	                    {
				  	                        extend: 'csvHtml5',
				  	                        title: 'RTSIS-REPORT' + apicode + '-' + getCurrentDateTime(),
				  	                        text: 'Download CSV',
				  	                        className: 'btn btn-secondary mt-3'			                       
				  	                    }
				  	                   
				  		            ],
				  "paging":true,
				  "destroy": true,
				  "deferRender": true,
				  "responsive": true,
				  "lengthMenu": [[5, 10, 50, 75, -1], [5, 10, 50, 75, "All"]],
				  "pageLength": 10						 
			}); 
			
		}else if(data.result == 'pending'){
			 $('.data_report').html('<table id="myTable" class="table table-striped table-hover table-bordered dt-responsive nowrap" aria-describedby="example_info"></table>');

				var reports = data.Info;
				
				//reports = eval(reports);
				
				table = $('#myTable').DataTable( {
				  "aaData": reports,
				  "aoColumns": [
					    
					    {"sTitle" : "S.No","mData":"rowNum"},
					    {"sTitle" : "SUBORGCODE","mData":"SUBORGCODE"},
						{"sTitle" : "SYSCODE", "mData": "SYSCODE"},
						{"sTitle" : "PAYTYPE", "mData": "PAYTYPE"},
						{"sTitle" : "REQDATE","mData": "REQDATE"},
						{"sTitle" : "REFNO","mData":"REFNO"},
						{"sTitle" : "REQSL", "mData": "REQSL"},
						{"sTitle" : "BATCHID", "mData": "BATCHID"},
						{"sTitle" : "APICODE", "mData": "APICODE"},
						{"sTitle" : "REPORTSERIAL","mData":"REPORTSERIAL"},
						{"sTitle" : "STARTSL","mData": "STARTSL"},
						{"sTitle" : "ENDSL", "mData": "ENDSL"},
						{"sTitle" : "STATUS", "mData": "STATUS"},
						{"sTitle" : "Action", "mData": "ACTION"}
					
				  ],
				  "dom": "<'row'<'col-sm-6'l><'col-sm-6 text-center'f>>"+
				     	 "<'row'<'col-sm-12'tr>>" +
				      	 "<'row'<'col-sm-4'i><'col-sm-4 text-center'B><'col-sm-4'p>>",
		  			"buttons": [
		  	                   
		  	                    {
		  	                        extend: 'csvHtml5',
		  	                        title: 'RTSIS-REPORT' + apicode + '-' + getCurrentDateTime(),
		  	                        text: 'Download CSV',
		  	                        className: 'btn btn-secondary mt-3'			                       
		  	                    }
		  	                   
		  		            ],
				  "paging":true,
				  "destroy": true,
				  "deferRender": true,
				  "responsive": true,
				  "lengthMenu": [[5, 10, 50, 75, -1], [5, 10, 50, 75, "All"]],
				  "pageLength": 10						 
			});	
			
		}
		
		else
		    {
				Sweetalert("warning", "", data.message);
		    }	
	},
	beforeSend: function( xhr )
	{	
		Sweetalert("load", "", "Please Wait..");
    },
    error: function (jqXHR, textStatus, errorThrown) 
    { 
    	
    }
        
   });
}

var getCurrentDateTime = function () {
    var currentDateTime = new Date();
    var year = currentDateTime.getFullYear();
    var month = (currentDateTime.getMonth() + 1).toString().padStart(2, '0');
    var day = currentDateTime.getDate().toString().padStart(2, '0');
    var hours = currentDateTime.getHours().toString().padStart(2, '0');
    var minutes = currentDateTime.getMinutes().toString().padStart(2, '0');
    var seconds = currentDateTime.getSeconds().toString().padStart(2, '0');
    return year + '-' + month + '-' + day + '_' + hours + ':' + minutes + ':' + seconds;
};

function view_option(reportsl,StartSl,EndSl){
	
	
	var data = new FormData();
	
	data.append("REPORTSERIAL",reportsl);
	data.append("STARTSL",StartSl);
	data.append("ENDSL",EndSl);
	
	
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/retrieve_data",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{  
			Swal.close();
			
			var details = data.Info;
			
			if(data.Result == "Success")
			{
				
				load_data(data);
				
				$("#report_data").show();
				$("#reportsl").val(data.REPORTSL);
				$("#StartSl").val(data.StartSl);
				$("#EndSl").val(data.EndSl);
				
			}
			else
		    {
				Sweetalert("warning", "", data.message,"");
		    }	
		},
		beforeSend: function( xhr )
 		{
 			Sweetalert("load", "", "Please Wait");
        },
	    error: function (jqXHR, textStatus, errorThrown) { 
	    	
	    }
   });

	
}

function load_data(dtl)  
{
	//console.log(dtl);
	
	//dtl = JSON.parse(dtl);
	
	REPORTSL = dtl.REPORTSL;
	StartSl = dtl.StartSl;
	EndSl = dtl.EndSl;
	
	var Headers = dtl.Headers;
	
	var hd_content = '<ul class="nav nav-pills nav-secondary" id="pills-tab" role="tablist">';
	
	var bd_content = '<div class="tab-content mb-3" id="pills-tabContent">';
	
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
		
	$('.data_report1').html(hd_content + bd_content);
	
	for(var z=0; z<Headers.length; z++)
	{
		var DRTL = dtl.details_list[z];
		
		var tbname = 'example_'+z;
		
		//$('.data_report').html('<table id="example" class="table table-striped table-hover table-bordered dt-responsive" aria-describedby="example_info"></table>');
		
		//$('.data_report').prepend(hd_content);
		
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
			  "paging":false,
			  "lengthChange": false, 
        	  "searching": false,
        	  "info": false,
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


function API_Code_Suggestions()
{
	$("#apicode").autocomplete({
		source: function(request, response) 
		{
	        $.ajax({
	            url: $("#ContextPath").val()+"/suggestions/Api_domain",
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
	    	var Id = ui.item.id;
	    		
	    	Retrieve_API_Gateway(Id);
	    }
	 }).autocomplete( "instance" )._renderItem = function(ul,item) 
	  	{
		 	return $( "<li><div>"+item.label+"</div></li>" ).appendTo(ul);
		}; 	
}

function Retrieve_API_Gateway(values)
{
	var data = new FormData();
	
	var Res = values.split("|");
    
	data.append("CHCODE", Res[0]);
	data.append("SERVNAME", Res[1]);
	data.append("SERVICECD", Res[2]);

	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Find/API_Service",
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
				$("#Channel_Code").val(data.CHCODE);
				$("#Channel_Type").val(data.CHTYPE);
				$("#Service_Name").val(data.SERVNAME);
				$("#Service_Id").val(data.SERVICECD);
				$("#url_end_point").val(data.URI);
				$("#Flow").val(data.FLOW);
				$("#Protocol_type").val(data.PROTOCOL);
				$("#method").val(data.METHOD);
				$("#format").val(data.FORMAT);
				$("#Payload").val(data.PAYLOAD); 
				$("#Sign_Payload").val(data.SIGNPAYLOAD); 	
				$("#job_req").prop("checked", data.JOBREQ === "1" ? true : false); 
				
				if(data.Headers.length >= 1)
				{
					$(".api_key").eq(0).val(data.Headers[0].Key);  
					$(".api_value").eq(0).val(data.Headers[0].Value);
					
					$("#view_exising_header, #view_exising_auth").show();
				}
				
				for(var i=1;i<data.Headers.length;i++)
				{
					var key = data.Headers[i].Key;
					var val = data.Headers[i].Value;
					
					Retrieve_Header("#t1_headers", key, val);
				}
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
	    error: function (jqXHR, textStatus, errorThrown) 
		{ 
	    	//Sweetalert("warning", "", "errrrr");  
	    }
   });
}

function view()
{
	
	
  var data = new FormData();
 
$.ajax({		 
	url  :  $("#ContextPath").val()+"/Rtsis/Reports/View",
	type :  'POST',
	data :  data,
	cache : false,
	contentType : false,
	processData : false,
	success: function (data) 
	{ 
		Swal.close();
		
		if(data.stscode == "200")
		{
			var reports = data.Info;
			
			
			reports = eval(reports);
			
			table = $('#myTable').DataTable( {
				  "aaData": reports,
				  "aoColumns": [
					    {"mData":"rowNum"},
						{ "mData": "REQTIME"},
						{ "mData": "REFNO"},
						{ "mData": "BATCHID"},
						{"mData":"APICODE"},
						{ "mData": "REPORTSERIAL"},
						{ "mData": "STARTSL"},
						{ "mData": "ENDSL"},
						{"mData":"BOTREFNO"},
						{ "mData": "STATUS"},
						{ "mData": "RESCODE"},
						{ "mData": "RESPDESC"}
					
				  ],
				  "paging":true,
				  "destroy": true,
				  "deferRender": true,
				  "responsive": true,
				  
				  "lengthMenu": [[5, 10, 50, 75, -1], [5, 10, 50, 75, "All"]],
				  "pageLength": 10						 
			}); 
			
		}
	},
	beforeSend: function( xhr )
	{	
		Sweetalert("warning", "", "Please Wait..");
    },
    error: function (jqXHR, textStatus, errorThrown) 
    { 
    	
    }
});
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
			  text:  Info, 	 
			  timer: 2000
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