 $(document).ready(function() {
        const timeoutDuration = 15 * 60 * 1000; // 30 minutes in milliseconds
       // const warningDuration = 28 * 60 * 1000; // 28 minutes in milliseconds

        let timeoutHandle;
       // let warningHandle;

        function resetTimeout() {
            clearTimeout(timeoutHandle);
          //  clearTimeout(warningHandle);

            // Set warning and timeout handlers
           // warningHandle = setTimeout(showWarning, warningDuration);
            timeoutHandle = setTimeout(End_session, timeoutDuration);
        }

        //function showWarning() {
          //  alert("Your session will expire soon due to inactivity. Please interact with the page to stay logged in.");
        //}

        //function logoutUser() {
          //  window.location.href = '/your-app/session-timeout'; // Redirect to your timeout handling URL
        //}

        // Reset the timeout on any user activity
        $(document).on('mousemove keypress click', resetTimeout);

        // Initialize the timeout when the page loads
        resetTimeout();
});
 
/*$(document).ready(function() {

    startTimer();
});

var minute_calc = 15;
var second_calc = 00;

var timer_calc = minute_calc + ":" + second_calc;

function startTimer() {

    var presentTime = timer_calc;
    var timeArray = presentTime.split(/[:]+/);
    var min = timeArray[0];
    var secd = checkSecond((timeArray[1] - 1));
    if (secd == 59) {
        min = min - 1
    }

    if (min < 0) {
    	
    	End_session();
    	
    	return;
    	 
    }

    timer_calc = min + ":" + secd;
  
    setTimeout(startTimer, 1000);
}


function checkSecond(sec) {
    if (sec < 10 && sec >= 0) {
        sec = "0" + sec
    }; 
    if (sec < 0) {
        sec = "59"
    };
    return sec;
}
*/

function End_session()
{
	var data = new FormData();
	
	data.append("sesDomain", $("#ULOGISUBORG").val());
	data.append("sesSessionID", $("#ULOGINSESSION").val());
	data.append("sesUserId", $("#ULOGINUSER").val());
	data.append("sessionIP", $("#ULOGINUSERIP").val());

	$.ajax({		 
		url  :  $("#ContextPath").val() + "/auto/logout",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{   
			 //alert("Your current Session is over due to inactivity.");

		     //window.location = $("#ContextPath").val() + "/auto/logout";
			
			Swal.fire({
                icon: 'warning',
                title: 'Session Ended',
                text: 'Your current session is over due to inactivity.',
                allowOutsideClick: false,
                confirmButtonText: 'OK'
            }).then(() => {
                // Redirect after alert confirmation
                window.location = $("#ContextPath").val() + "/auto/logout";
            });
		},
		beforeSend: function( xhr )
 		{
 			
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
			  text:  Info,		 		  
			  timer: 2000,
			  showConfirmButton: false 
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
			  timer: 2000,
			  showConfirmButton: false 
		});
	}
	else if(Type == "warning")
	{
		Swal.fire({
			  icon: 'warning',
			  title: Title ,
			  text:  Info ,		 
			  timer: 2000,
			  showConfirmButton: false 
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