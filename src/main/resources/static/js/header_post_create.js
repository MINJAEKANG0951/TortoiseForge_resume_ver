// csrf token
var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
		document.getElementById('showMenu').checked = false;
    } else {
		document.getElementById('showMenu').checked = false;
    }
    
	let changeEvent = new Event('change');
	document.getElementById('showMenu').dispatchEvent(changeEvent);
});



document.addEventListener('DOMContentLoaded',()=>{
	
	
	document.getElementById('showMenu').addEventListener('change',function(){
		
		let checked = document.getElementById('showMenu').checked;
		if(checked)
		{
			let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
			
			document.body.style.paddingRight 								= `${scrollBarWidth}px`;
			document.getElementById('headerContainer').style.paddingRight 	= `${scrollBarWidth}px`;
			document.documentElement.style.overflow 						= 'hidden';
		}
		else 
		{
			document.documentElement.style.overflow 						= 'auto';
			document.body.style.paddingRight 								= '0';
			document.getElementById('headerContainer').style.paddingRight 	= '0';
		}
	
	});
	
	
	
	document.getElementById('logo').addEventListener('click',function(){
		
		if (window.history.length > 1) {
        	window.history.back();
	    } else {
	        window.location.href = '/';
	    }
	    
	});
	
	
	
	
	
	
});




