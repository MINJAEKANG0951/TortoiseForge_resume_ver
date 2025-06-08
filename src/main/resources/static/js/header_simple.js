
// csrf token
var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


document.addEventListener('DOMContentLoaded',()=>{
	
	document.getElementById('backBtn').addEventListener('click',function(){
		if (window.history.length > 1) {
        	window.history.back();
	    } else {
	        window.location.href = '/';
	    }
	});
	
	if(document.getElementById('homeBtn') !== null) {
		document.getElementById('homeBtn').addEventListener('click',function(){
			window.location.href = '/';
		})
	};

	
});
