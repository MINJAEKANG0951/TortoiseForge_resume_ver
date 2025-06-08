var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');



document.addEventListener('DOMContentLoaded',()=>{
	
	// goBackButton
	document.getElementById('goBack');
	
	// homeBtn
	document.getElementById('home');
	
	// radios
	document.getElementById('page1Radio');
	document.getElementById('page2Radio');

	// input boxes
	document.getElementById('email');
	document.getElementById('password');
	document.getElementById('confirmPassword');
	document.getElementById('verificationCode');
	
	
	// text div
	document.getElementById('page1PopupText');
	document.getElementById('page2PopupText');
	
	
	// guide(warning)
	document.getElementById('page1Guide');
	document.getElementById('page2Guide');
	
	// buttons
	document.getElementById('page1Button');
	document.getElementById('page2Button');
	document.getElementById('reset');
	
	setViewPagerPage(1);
	
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

document.getElementById('page1Button').addEventListener('click',()=>{
	submitForgotPasswordInfo(1);
})

document.getElementById('page2Button').addEventListener('click',()=>{
	submitForgotPasswordInfo(2);
})

document.getElementById('reset').addEventListener('click',()=>{
	setViewPagerPage(1);
	document.getElementById('reset').style.display = 'none';
})


function submitForgotPasswordInfo(currentPhase) {
	
	let currentStep 			= currentPhase;
	let userEmail				= document.getElementById('email').value;
	let userPassword			= document.getElementById('password').value;
	let userPasswordConfirm		= document.getElementById('confirmPassword').value;
	let userVerificationCode	= document.getElementById('verificationCode').value;
	
	let form = new FormData();
	form.append("currentStep", currentStep);
	form.append("userEmail", userEmail);
	form.append("userPassword", userPassword);
	form.append("userPasswordConfirm", userPasswordConfirm);
	form.append("userVerificationCode", userVerificationCode);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST','/submitForgotPasswordInfo', false);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function() {
		if(xhr.readyState === 4) 
		{
			if(xhr.status === 200)
			{
				document.location = "/workDone/password";
			}
			else if(xhr.status === 202)
			{
				let response 		= JSON.parse( xhr.responseText );
				let stepToGo 		= response['stepToGo'];
				let messageToShow	= response['message'];
				
				moveStepAndShowText(stepToGo, messageToShow);
			}
			else
			{
				let response 		= JSON.parse( xhr.responseText );
				let stepToGo 		= response['stepToGo'];
				let messageToShow	= response['message'];
				
				moveStepAndShowWarning(stepToGo, messageToShow);
				
			}
		}
	};
	
	xhr.send(form);
}


function moveStepAndShowWarning(stepToGo, messageToShow) {
	
	let step 		= stepToGo;
	let message 	= messageToShow
	
	if(step === '1')
	{
		setViewPagerPage(1);
		document.getElementById('page1Guide').innerText = message;
		setTimeout(()=>{
			document.getElementById('page1Guide').innerText = null;
		}, 4000);
	}
	else if(step === '2')
	{
		setViewPagerPage(2);
		document.getElementById('page2Guide').innerText = message;
		setTimeout(()=>{
			document.getElementById('page2Guide').innerText = null;
		}, 4000);
		
		document.getElementById('reset').style.display = "flex";
	}
	
}


function moveStepAndShowText(stepToGo, messageToShow) {
	
	let step 		= stepToGo;
	let message 	= messageToShow
	
	if(step === '1')
	{
		setViewPagerPage(1);
		document.getElementById('page1PopupText').innerText = message;
	}
	else if(step === '2')
	{
		setViewPagerPage(2);
		document.getElementById('page2PopupText').innerText = message;
	}

}


function setViewPagerPage(pageNumber) {
	
	document.getElementById('email')			.disabled = true;
	document.getElementById('password')			.disabled = true;
	document.getElementById('confirmPassword')	.disabled = true;
	document.getElementById('verificationCode')	.disabled = true;
	
	if(pageNumber === 1)
	{
		document.getElementById('page1Radio')		.checked = true;
		document.getElementById('email')			.disabled = false;
	}
	else if(pageNumber === 2)
	{
		document.getElementById('page2Radio')		.checked = true;	
		document.getElementById('password')			.disabled = false;
		document.getElementById('confirmPassword')	.disabled = false;
		document.getElementById('verificationCode')	.disabled = false;	
	}

}




