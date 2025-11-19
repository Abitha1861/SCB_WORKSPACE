$(document).ready(function() {
		
		 $("#tuserid").autocomplete({
			    source:  $("#ContextPath").val() + "/Suggestions/User_Id567",			
			    minLength: 1,			
			    change: function(event, ui) 
			    {
			    	
				        QUEUECHECKER();
				  
			    },		   
		  }).autocomplete( "instance" )._renderItem = function(ul,item) 
		  	{
		  		 return $( "<li><div>"+item.label+"</div></li>" ).appendTo(ul);
			};
			
			$("#tbublock").change(function() {
				
				if($("#tbublock").prop('checked') == true)
				{
					$("#tbublock_label").html('Block');
				}
				else
				{
					$("#tbublock_label").html('Unblock');
				}		
			});	

			$("#tuserid").change(function() {
				
			});		
			$("#reset").click(function() {
				reset();
			});	
	});
		
	function reset()
	{
		
		
		 $("#form").trigger("reset");
	}
	
	function validateEmail(email)
	{
	    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	    
	    return re.test(String(email).toLowerCase());
	}
	
	function date_format(val)
	{
        var dt = new Date(val); 

        var dd = dt.getDate(); 
        var mm = dt.getMonth() + 1; 
  
        var yyyy = dt.getFullYear(); 
        if (dd < 10) { 
            dd = '0' + dd; 
        } 
        if (mm < 10) { 
            mm = '0' + mm; 
        } 
        
        var condate = dd + '-' + mm + '-' + yyyy; 
        
        return condate;
  
	}
	
	function isvalidPassword(val) 
	{ 
        if (val.match(/[a-z]/g) && val.match(/[A-Z]/g) && val.match(/[0-9]/g) && val.match(/[^a-zA-Z\d]/g)) 
        {
        	return true;
        }   
        else 
        {
        	return false;
        }
    }

	
	function Form_reset(){
		document.getElementById("tuserid").value = "";
		document.getElementById("tbublock").checked = false;
		$("#tbublock_label").html("");
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