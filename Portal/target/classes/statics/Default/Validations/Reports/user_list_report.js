$(document).ready(function() {
	
	User_list_Report();
	
});

function User_list_Report()
{	
	var data = new FormData();
	
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Report/list_of_users",
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
				var reports = data.User_list_report;
				
				//alert('HIII2');

				reports = eval(reports);
				
				table = $('#user_list_table_id').DataTable( {
					  "aaData": reports,
					  "aoColumns": [
							{ "mData": "ROWNUM"},			
							{ "mData": "USERSCD"},		
							{ "mData": "UNAME"},  	   	        
							{ "mData": "ROLECD"},	
							{ "mData": "Last_Login_date"},
							{ "mData": "ACCOUNT_TYPE"},
							{ "mData": "STATUS"}
					  ],
					  "scrollX": true,
					  "scrollY": "330px",
				      "scrollCollapse": true,
					  "paging":true,
					  "destroy": true,
					  "deferRender": true,
					  "responsive": true,
					  //"bSort": false,
					  "dom": "<'row'<'col-sm-2'l><'col-sm-6 text-center'<'toolbar'>><'col-sm-1 text-center'<'resetbar'>><'col-sm-3'f>>"+
					     	 "<'row'<'col-sm-12'tr>>" +
					     	 "<'row'<'col-sm-4'i><'col-sm-4 text-center'B><'col-sm-4'p>>",
			          "buttons": [
		                    {
		                        extend: 'excelHtml5',
		                        title: 'User_list_report',
		                        text:'Excel',         
		                        className: 'btn btn-secondary mt-3' 
		                    },
		                    {
		                        extend: 'pdfHtml5',
		                        title: 'User_list_report',
		                        text:'PDF',         
		                        className: 'btn btn-secondary mt-3' 
		                    },
		                   /* {
		                        extend: 'pdfHtml5',
		                        title: 'User_list_report',
		                        text: 'PDF',
		                        className: 'btn btn-secondary mt-3',
		                        orientation: 'landscape',
		                        pageSize: 'LEGAL',
		                        customize: function ( doc ) {
		                            doc.content.splice( 0, 0, {
		                                margin: [ 0, 0, 0, 6 ],
		                                alignment: 'left'
		                            } );
		                        }		                    
		                    },*/	
		                    {
		                        extend: 'csvHtml5',
		                        title: 'User_list_report',
		                        text: 'CSV',
		                        className: 'btn btn-secondary mt-3'			                       
		                    },
		                    {
		                        extend: 'copy',
		                        title: 'User_list_report',
		                        text: 'COPY',
		                        className: 'btn btn-secondary mt-3'			                       
		                    }
			            ],
			          "lengthMenu": [[5, 10, 50, 75, -1], [5, 10, 50, 75, "All"]],
					  "pageLength": 10						 
				}); 
				
				$('.dataTables_paginate').addClass('mt-3');
				
				var Max_date = get_currentDate();
				
				var content = '<div class="row">' +
								'<div class="col-md-2 col-sm-6"><label><b> From: <b></label></div>'+ 
									'<div class="col-md-4 col-sm-6">'+ 
									  	'<input type="date" id="min" class="form-control" max="'+Max_date+'">' +  
									'</div>'+
									'<div class="col-md-2 col-sm-6"><label><b> To : </b></label></div>'+ 
									'<div class="col-md-4 col-sm-6">'+ 
									  	'<input type="date" id="max" class="form-control" max="'+Max_date+'">' + 
									'</div>'+
							    '</div>'; 
				
				var Reset_Content = '<div class="row">' +
									'<div class="col-md-6 col-sm-6">'+ 
										'<button id="filter_search" type="button" class="btn btn-icon btn-round btn-secondary" data-toggle="tooltip" data-placement="top" title="Search">'+
											'<i class="fas fa-search"></i> </button>'+ 
									'</div>'+
									'<div class="col-md-6 col-sm-6">'+ 
									'<button id="filter_reset" type="button" class="btn btn-icon btn-round btn-secondary" data-toggle="tooltip" data-placement="top" title="Clear filters">'+
										'<i class="fas fa-undo"></i> </button>'+ 
								'</div>'+
							    '</div>'; 
				
				$("div.toolbar").html(content);

				$("div.resetbar").html(Reset_Content);
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
