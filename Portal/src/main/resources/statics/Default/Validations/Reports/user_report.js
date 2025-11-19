$(document).ready(function() {
	
	view();
	
});

function view()
{
	
	
  var data = new FormData();
 
$.ajax({		 
	url  :  $("#ContextPath").val()+"/User/Reports/View",
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
						{ "mData": "REGDATE"},
						{ "mData": "UNAME"},
						{ "mData": "ROLECD"}
					
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
		Sweetalert("load", "", "Please Wait..");
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