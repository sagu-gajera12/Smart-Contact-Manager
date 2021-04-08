console.log("hello world")

const  toggleSidebar =()=>{
    if($(".sidebar").is(":visible")==true)
    {
         $(".sidebar").css("display","none");
         $(".sidebar").css("margin-left","0%");
         $(".content").css("margin-left","0%");

    }else{
        $(".sidebar").css("display","block");
        $(".sidebar").css("margin-left","0%");
        $(".content").css("margin-left","20%");
    }

}

const search=()=>{

	let query=$("#search_input").val();
	

	if(query=='')
	{
     $(".search-result").hide();

	}else{
		//search
	console.log(query);


	let url=`http://localhost:8080/search/${query}`;
	fetch(url).then((response)=>{

		return response.json();
	}).then((data)=>{

//		console.log(data);

		let text=`<div class="list-group">`
       
       data.forEach((contact)=>{
         text+=`<a href="/user/${contact.cid}/contact" class="list-group-item list-group-item-action">${contact.name}</a>`
           
       });
       text+='</div>';
       $(".search-result").html(text);
       	$(".search-result").show();


	})

	}
}

//first request to server to creare order

const paymentStart=()=>{
	console.log("hello 1");
let amount=$("#paymentamount").val();
console.log(amount);
if(amount==''||amount==null)
{

      swal("failed!", "amount is reqired !!", "error");

}

$.ajax(
   {


   	url:'/user/create_order',
   	data:JSON.stringify({amount:amount,info:'order_request'}),
   	contentType:'application/json',
   	type: "POST",
    dataType: "json",
   	success: function(response){
        //invoked when success

    console.log(response);

    if(response.status=="created")
    {
    	let options={

    		key:'rzp_test_3KvzsvgNcPRBKc',
    		amount:response.amount,
    		currency:'INR',
    		name:'Smart Contact Manager',
    		description:"Donation",
    		order_id:response.id,
    		handler:function(response){
    			console.log(response.razorpay_payment_id)
    			console.log(response.razorpay_order_id)
    			console.log(response.razorpay_signature)
    		    console.log("payment successfull")
    		  
    		   updatePaymentOnServer(response.razorpay_payment_id,
                                     response.razorpay_order_id,
                                     "paid");

    		 //  swal("Good job!", "payment successfull !!", "success");
    		},
    		"prefill": {
				"name": "",
				"email": "",
				"contact": ""
				},
			"notes": {
				"address": "Razorpay Corporate Office"

				},
			"theme": {
				"color": "#3399cc"
				},
    	}
    	let rzp=new Razorpay(options);

        rzp.on('payment.failed', function (response){
		console.log(response.error.code);
		console.log(response.error.description);
		console.log(response.error.source);
		console.log(response.error.step);
		console.log(response.error.reason);
		console.log(response.error.metadata.order_id);
		console.log(response.error.metadata.payment_id);
		alert("oops payment failed")
		});

    	rzp.open();


    }

   	},
   error: function(error){
     //invoked when error

      swal("payment failed!", "something went wrong !!", "error");
   },


	});

}

function updatePaymentOnServer(payment_id,order_id,status)
{
$.ajax(
   {


   	url:'/user/update_order',
   	data:JSON.stringify({payment_id:payment_id,
   		order_id:order_id,
        status:status,
   	}),
   	contentType:'application/json',
   	type: "POST",
    dataType: "json",
    success:function(response)
    {
        swal("Good job!", "congrates !! payment successfull !!", "success");
    },
    error:function(xhr,status,error)
    {
    	alert(xhr.responseText)
    	alert(status)
    	console.log("hellooooooooooooooooooooooooooooooo")
       swal("failed!", "Your payment is successfull,but we did not get on server ,we will contact you soon", "error");	
    },
}
);


}