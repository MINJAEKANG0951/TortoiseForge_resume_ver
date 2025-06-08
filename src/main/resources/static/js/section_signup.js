// csrf token - header.js 에 원래 넣는데 signUp page 에선 header 없으니까
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
	document.getElementById('page3Radio');
	
	
	// checkboxes
	document.getElementById('termsOfService');
	document.getElementById('privacyPolicy');
	
	// input boxes
	document.getElementById('username');
	document.getElementById('password');
	document.getElementById('confirmPassword');
	document.getElementById('email');
	
	document.getElementById('verificationCode');
	
	
	// text div
	document.getElementById('page1PopupText');
	document.getElementById('page2PopupText');
	document.getElementById('page3PopupText');
	
	
	// guide(warning)
	document.getElementById('page1Guide');
	document.getElementById('page2Guide');
	document.getElementById('page3Guide');
	
	// buttons
	document.getElementById('page1Button');
	document.getElementById('page2Button');
	document.getElementById('page3Button');
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

document.getElementById('page1Radio').addEventListener('change',()=>{
	
	let isChecked = this.checked;
	alert(isChecked);
	
})

document.getElementById('page1Button').addEventListener('click',()=>{
	submitSignupInfo(1);	// to the next step
})

document.getElementById('page2Button').addEventListener('click',()=>{
	submitSignupInfo(2);	// verification code sending
})

document.getElementById('page3Button').addEventListener('click',()=>{
	submitSignupInfo(3);	// finally signup
})

document.getElementById('reset').addEventListener('click',()=>{
	setViewPagerPage(1);
	document.getElementById('reset').style.display = 'none';
})



function submitSignupInfo(currentPhase) {
	
	let currentStep 			= currentPhase;
	let termsOfService 			= document.getElementById('termsOfService').checked;
	let privacyPolicy			= document.getElementById('privacyPolicy').checked;
	let username				= document.getElementById('username').value;
	let userEmail				= document.getElementById('email').value;
	let userPassword			= document.getElementById('password').value;
	let userPasswordConfirm		= document.getElementById('confirmPassword').value;
	let userVerificationCode	= document.getElementById('verificationCode').value;
	
	let form = new FormData();
	form.append("currentStep", currentStep);
	form.append("termsOfService", termsOfService);
	form.append("privacyPolicy", privacyPolicy);
	form.append("username", username);
	form.append("userEmail", userEmail);
	form.append("userPassword", userPassword);
	form.append("userPasswordConfirm", userPasswordConfirm);
	form.append("userVerificationCode", userVerificationCode);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST','/submitSignupInfo', false);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function() {
		if(xhr.readyState === 4) 
		{
			if(xhr.status === 200)
			{
				document.location = "/workDone/signup";
			}
			else if(xhr.status === 202)
			{
				let response 		= JSON.parse( xhr.responseText );
				let stepToGo 		= response['stepToGo'];
				let messageToShow	= response['message'];
				
				moveStepAndShowText(stepToGo, messageToShow);
			}
			else if(xhr.status === 403)
			{
				
				let stepToGo		= '1';
				let messageToShow 	= 'Invalid or expired CSRF token. Please refresh the page and try again.';
				
				moveStepAndShowWarning(stepToGo, messageToShow);
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
	}
	else if(step === '3')
	{
		setViewPagerPage(3);
		document.getElementById('page3Guide').innerText = message;
		setTimeout(()=>{
			document.getElementById('page3Guide').innerText = null;
		}, 4000);
		
		
		// show 'RESTART' btns when error occurs in step3
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
	else if(step === '3')
	{
	
		setViewPagerPage(3);
		document.getElementById('page3PopupText').innerText = message;
	}
	
}


/* 
	추가
	
	다른 viewPager 의 inputBox로 tab 으로 넘어갈 수 있는것 때문에 추가됨.
	현재 page 내에 존재하는 inputBoxes 안에섬나 이동가능하도록 다른곳은 disable 시킴 

*/
function setViewPagerPage(pageNumber) {
	
	document.getElementById('termsOfService')	.disabled = true;
	document.getElementById('privacyPolicy')	.disabled = true;
	document.getElementById('username')			.disabled = true;
	document.getElementById('password')			.disabled = true;
	document.getElementById('confirmPassword')	.disabled = true;
	document.getElementById('email')			.disabled = true;
	document.getElementById('verificationCode')	.disabled = true;
	
	if(pageNumber === 1)
	{
		document.getElementById('page1Radio')		.checked = true;
		document.getElementById('termsOfService')	.disabled = false;
		document.getElementById('privacyPolicy')	.disabled = false;
	}
	else if(pageNumber === 2)
	{
		document.getElementById('page2Radio')		.checked = true;	
		document.getElementById('username')			.disabled = false;
		document.getElementById('password')			.disabled = false;
		document.getElementById('confirmPassword')	.disabled = false;
		document.getElementById('email')			.disabled = false;	
	}
	else if(pageNumber === 3)
	{
		document.getElementById('page3Radio')		.checked = true;
		document.getElementById('verificationCode')	.disabled = false;
	}
	
}


// RECAPTCHA -> see MINJAE LAB



