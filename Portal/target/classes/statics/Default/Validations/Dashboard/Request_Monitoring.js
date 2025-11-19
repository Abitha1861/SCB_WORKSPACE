$(document).ready(function() {
	
	Request_Monotoring();
	
	New_Dashboard();
	
	New_Dashboard2();

});




function Request_Monotoring()
{
	var data = new FormData();
	
	$.ajax({		 
		url  :  $("#ContextPath").val()+"/Dashboard/Request_Monitoring",
		type :  'POST',
		data :  data,
		cache : false,
		contentType : false,
		processData : false,
		success: function (data) 
		{   
			if(data.Result === 'Success')
			{
				var Bill = data.Bill;
				
				$("#total_bill").html(Bill.TOTAL);
				$("#gepg_bill").html(Bill.GEPG);
				$("#msc_bill").html(Bill.MSC);
				$("#tigo_bill").html(Bill.TIGO);
				
				var PAYMENT = data.PAYMENT;
				
				$("#total_payment").html(PAYMENT.TOTAL);
				$("#out_gepg").html(PAYMENT.Outward_GEPG);
				$("#out_msc").html(PAYMENT.Outward_MSC);
				$("#in_tips").html(PAYMENT.Inward_TIPS);
				$("#out_tips").html(PAYMENT.Outward_TIPS);
				
				var ACCOUNT_Lookup = data.ACCOUNT;
				
				$("#total_ac").html(ACCOUNT_Lookup.TOTAL);
				$("#ac_gepg").html(ACCOUNT_Lookup.GEPG);
				$("#ac_msc").html(ACCOUNT_Lookup.MSC);
				$("#ac_tips").html(ACCOUNT_Lookup.TIPS);
				
				var Recon = data.RECON;
				
				$("#total_recon").html(Recon.TOTAL);
				$("#recon_geog").html(Recon.GEPG);
				$("#recon_msc").html(Recon.MSC);
				$("#recon_tips").html(Recon.TIPS);
				
				RECON_Channel(data.RECON_CHANNEL);
				
				RECON_Week(data.RECON_WEEK);	
			}
		},
		beforeSend: function( xhr )
		{
			
        },
	    error: function (jqXHR, textStatus, errorThrown) 
	    { 
	    	
	    }
   });
}

function RECON_Week(Info)
{
	var Days = Info.Total_Days;

	Highcharts.chart('container_one', {
	    chart: {
	        type: 'areaspline'
	    },
	    title: {
	        text: ''
	    },
	    legend: {
	      
	        borderWidth: 1,
	        backgroundColor:
	            Highcharts.defaultOptions.legend.backgroundColor || '#FFFFFF'
	    },
	    xAxis: {
	    	categories : Days,
	        plotBands: [{ 
	            from: 4.5,
	            to: 6.5,
	            color: 'rgba(68, 170, 213, .2)'
	        }]
	    },
	    yAxis: {
	        title: {
	            text: 'Total No of Request'
	        }
	    },
	    tooltip: {
	        shared: true,
	        valueSuffix: ' Request'
	    },
	    credits: {
	        enabled: false
	    },
	    plotOptions: {
	        areaspline: {
	            fillOpacity: 0.5
	        }
	    },
	    series: [{
	        name: 'Matched',
	        data: Info.Matched
	    }, 
	    {
	        name: 'Not Matched',
	        data: Info.Not_Matched
	    },
	    {
	        name: 'Pending',
	        data: Info.Pending
	    }]
	});
}

function RECON_Channel(Recon)
{
	 var IB_PENDING = Recon.IB_PENDING;
	 var IB_MATCHED = Recon.IB_MATCHED;
	 var IB_NOT_MATCHED = Recon.IB_NOT_MATCHED;
	
	 var myPieChart1 = new Chart( $("#IB") , {
	    type: 'doughnut',
		data: {
				datasets: [{
					data: [ IB_MATCHED, IB_NOT_MATCHED, IB_PENDING],
					backgroundColor : ['#f3545d', '#fdaf4b', '#1d7af3'],
					borderWidth: 0
				}],
				labels: ['Matched', 'Not Matched', 'Pending'] 
			},
			options : {
				responsive: true, 
				maintainAspectRatio: false,
				legend: {
					position : 'bottom',
					labels : {
						fontColor: 'rgb(154, 154, 154)',
						fontSize: 10,
						usePointStyle : true,
						padding: 20
					}
				},
				pieceLabel: {
					render: 'value',
					fontColor: 'white',
					fontSize: 10,
				},
				tooltips: false,
				layout: {
					padding: {
						left: 20,
						right: 20,
						top: 0,
						bottom: 0
					}
				}
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

