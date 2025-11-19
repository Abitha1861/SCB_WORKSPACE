$(document).ready(function() {
	
	Security_report();
	
	//$('#Security_report_table_id').DataTable();
	
});



function Security_report()
{	
			 $('#Security_report_table_id').DataTable( {
					  "paging":true,
					  "destroy": true,
					  "deferRender": true,
					  "responsive": true,
					  "dom": "<'row'<'col-sm-2'l><'col-sm-6 text-center'<'toolbar'>><'col-sm-1 text-center'<'resetbar'>><'col-sm-3'f>>"+
					     	 "<'row'<'col-sm-12'tr>>" +
					     	 "<'row'<'col-sm-4'i><'col-sm-4 text-center'B><'col-sm-4'p>>",
			          "buttons": [
		                    {
		                        extend: 'excelHtml5',
		                        title: 'Security report',
		                        text:'Excel',         
		                        className: 'btn btn-secondary mt-3' 
		                    },
		                    {
		                        extend: 'pdfHtml5',
		                        title: 'Security report',
		                        text:'PDF',         
		                        className: 'btn btn-secondary mt-3' 
		                    },
		                    {
		                        extend: 'csvHtml5',
		                        title: 'Security report',
		                        text: 'CSV',
		                        className: 'btn btn-secondary mt-3'			                       
		                    },
		                    {
		                        extend: 'copy',
		                        title: 'Security report',
		                        text: 'COPY',
		                        className: 'btn btn-secondary mt-3'			                       
		                    }
			            ],
			          "lengthMenu": [[5, 10, 50, 75, -1], [5, 10, 50, 75, "All"]],
					  "pageLength": 10						 
				}); 
		
}
