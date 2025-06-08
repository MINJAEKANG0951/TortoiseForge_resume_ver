// csrf token - header.js 에 원래 넣는데 login page 에선 header 없으니까
var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


document.addEventListener('DOMContentLoaded',()=>{
	
	// navBtns
	document.getElementById('goBack');
	document.getElementById('home');
	
	
	// input boxes
	document.getElementById('username');
	document.getElementById('password');
	
	
	// guide
	document.getElementById('guide');
	
	
	// btn
	document.getElementById('loginBtn');
	
	
})

document.getElementById('goBack').addEventListener('click',()=>{

    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/';
    }

})
document.getElementById('home').addEventListener('click',()=>{
	window.location.href = '/';
})


document.getElementById('loginBtn').addEventListener('click',()=>{
	
	let username 	= document.getElementById('username').value;
	let password 	= document.getElementById('password').value;
	
	let form = new FormData();
	form.append('username',username);
	form.append('password',password);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST','/requestLogin', false);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if ( xhr.readyState === 4 ) {
			if(xhr.status === 200)
			{
				document.location = "/";
			}
			else if(xhr.status === 403) 
			{
				let data = JSON.parse( xhr.responseText );
				
				if(data['error'] === 'ACCOUNT_PENDING_DELETION') {
					
					// 계정삭제 취소할건지 묻기(yes or no dialog 로)
					let methodToExecute = function(){
						setTimeout(()=>{
							requestAccountActivation();
						},310);
					};
					dialog_yesOrNo2(data['message'], methodToExecute);
					showDialog();
					
				} else {
					let message = 'Invalid or expired CSRF token. Please refresh the page and try again.';
					document.getElementById('guide').innerText = message;
					setTimeout(()=>{
						document.getElementById('guide').innerText = null;
					},3000)
				}
			}
			else
			{
				let data = JSON.parse( xhr.responseText );
				document.getElementById('guide').innerText 	 = data['message'];
				setTimeout(()=>{
					document.getElementById('guide').innerText = null;
				},3000)
			}
		}
	}
	xhr.send(form);
	
	
})


function requestAccountActivation() {
	
	let username = document.getElementById('username').value;
	let password = document.getElementById('password').value;
	
	let form = new FormData();
	form.append('username',username);
	form.append('password',password);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST','/requestAccountActivation', false);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if ( xhr.readyState === 4 ) {
			if(xhr.status === 200)
			{
				document.getElementById('loginBtn').click();
			}
			else if(xhr.status === 403) 
			{
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				document.getElementById('guide').innerText 	 = text;
				setTimeout(()=>{
					document.getElementById('guide').innerText = null;
				},3000)
			}
			else
			{
				let data = JSON.parse( xhr.responseText );
				document.getElementById('guide').innerText 	 = data['message'];
				setTimeout(()=>{
					document.getElementById('guide').innerText = null;
				},3000)
			}
		}
	}
	xhr.send(form);
	
}




