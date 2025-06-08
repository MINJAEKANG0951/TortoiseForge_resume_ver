var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');




document.addEventListener('DOMContentLoaded',()=>{
	
	document.getElementById('openSendMessage');
	document.getElementById('sendMessageDrawerOnOff');
	document.getElementById('openReport');
	document.getElementById('reportDrawerOnOff');
	
	
	// MESSAGE TEXTAREA SETTING
	document.getElementById('message').addEventListener('input',()=>{
	
		let currentScroll = window.scrollY;
	
		document.getElementById('message').style.height = 'auto';
		document.getElementById('message').style.height = document.getElementById('message').scrollHeight + 'px';
		
		let currentContentLength = parseInt( document.getElementById('message').value.length );
		document.getElementById('messageCurrentLength').innerText = currentContentLength;
		window.scrollTo(0, currentScroll);

	});
	
	// REPORT TEXTAREA SETTING
	document.getElementById('reportDetails').addEventListener('input',()=>{
	
		let currentScroll = window.scrollY;
	
		document.getElementById('reportDetails').style.height = 'auto';
		document.getElementById('reportDetails').style.height 
			= document.getElementById('reportDetails').scrollHeight + 'px';
		
		let currentContentLength = parseInt( document.getElementById('reportDetails').value.length );
		document.getElementById('reportDetailsCurrentLength').innerText = currentContentLength;
		window.scrollTo(0, currentScroll);

	});
	
	
	window.addEventListener('resize', function() {
	
		const commentInput = document.getElementById('message');
		const inputEvent = new Event('input', { bubbles: true, cancelable: true });
		commentInput.dispatchEvent(inputEvent);
    
	});

	
	window.addEventListener('pageshow', function() {
		
		document.getElementById('message').value = '';
		document.getElementById('message').style.height = 'auto';
		document.getElementById('message').style.height = 
			document.getElementById('message').scrollHeight + 'px';
	
	});
	
	
	
})



// CHANGE PASSWORD EVENT LISTENERS
document.getElementById('openSendMessage').onclick = function(){
	if(document.getElementById('disableBtns').checked) {return;}
	sendMessageOn();
}

document.getElementById('sendMessageDrawerOnOff').addEventListener('change', function(){
	
	const sendMessageDrawerOnOff = document.getElementById('sendMessageDrawerOnOff');
	
	if(sendMessageDrawerOnOff.checked) 
	{
		document.getElementById('sendMessageBtn').disabled 	= false;
		document.getElementById('message').disabled 		= false;
		
		document.getElementById('message').value = '';
		document.getElementById('sendMessageGuide').innerText = '';
		
		const commentInput = document.getElementById('message');
		const inputEvent = new Event('input', { bubbles: true, cancelable: true });
		commentInput.dispatchEvent(inputEvent);
		
		let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
		
		document.body.style.paddingRight = `${scrollBarWidth}px`;
		if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
			document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
		}
		document.documentElement.style.overflow = 'hidden';
		
	}
	else 
	{
		document.getElementById('sendMessageBtn').disabled 	= true;
		document.getElementById('message').disabled 		= true;
			
		setTimeout(()=>{
			document.documentElement.style.overflow 						= 'auto';
			document.body.style.paddingRight 								= '';
			document.getElementById('headerContainer').style.paddingRight 	= '';
		},300);
	}
	
});


document.getElementById('sendMessageBtn').addEventListener('click',function(){
	
	let recipientSeq 	= document.getElementById('userSeq').value;
	let message			= document.getElementById('message').value;
	
	let formData = new FormData();	
	formData.append('recipientSeq', recipientSeq);
	formData.append('message', message);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST', '/userInfo/message', true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				let message = data.message;
				let methodToExecute = function(){
					console.log('message sent successfully.');
				};
				
				sendMessageOff();
				
				setTimeout(()=>{
					hideWaitingModalBackground(false);
					dialog_confirm(message, methodToExecute);
					showDialog();
				},301);

			}
			else if(xhr.status === 403)
			{
				hideWaitingModalBackground(false);
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				guideSendMessage(text);
			}
			else
			{
				hideWaitingModalBackground(false);
				let data = JSON.parse(xhr.responseText);
				guideSendMessage(data.message);
			}
		}
	}
	xhr.send(formData);
	showWaitingModalBackground(false);
	
});






// DELETE ACCOUNT EVENT LISTENERS
document.getElementById('openReport').onclick = function(){
	if(document.getElementById('disableBtns').checked) {return;}
	reportOn();
}


document.getElementById('reportDrawerOnOff').addEventListener('change', function(){
	
	const reportDrawerOnOff = document.getElementById('reportDrawerOnOff');
	
	if(reportDrawerOnOff.checked) 
	{
		document.getElementById('reportDetails').disabled 	= false;
		document.getElementById('reportBtn').disabled 		= false;
		
		document.getElementById('reportDetails').value = '';
		
		document.getElementById('reportGuide').innerText = '';
		
		const commentInput = document.getElementById('reportDetails');
		const inputEvent = new Event('input', { bubbles: true, cancelable: true });
		commentInput.dispatchEvent(inputEvent);
		
		let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
		
		document.body.style.paddingRight = `${scrollBarWidth}px`;
		if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
			document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
		}
		document.documentElement.style.overflow = 'hidden';
		
	}
	else 
	{
		document.getElementById('reportDetails').disabled 	= true;
		document.getElementById('reportBtn').disabled 		= true;
		
		setTimeout(()=>{
			document.documentElement.style.overflow 						= 'auto';
			document.body.style.paddingRight 								= '';
			document.getElementById('headerContainer').style.paddingRight 	= '';
		},300);
	}
	
	
});



document.getElementById('reportBtn').addEventListener('click', function(){
	
	let targetSeq 		= document.getElementById('userSeq').value;
	let reportDetails	= document.getElementById('reportDetails').value;
	
	let formData = new FormData();	
	formData.append('targetSeq', targetSeq);
	formData.append('reportDetails', reportDetails);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST','/userInfo/report',true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				let message = data.message;
				let methodToExecute = function(){
					console.log('reported successfully');
				};

				reportOff();
				
				setTimeout(()=>{
					hideWaitingModalBackground(false);
					dialog_confirm(message, methodToExecute);
					showDialog();
				},301);

			}
			else if(xhr.status === 404)
			{
				hideWaitingModalBackground(false);
				let data = JSON.parse(xhr.responseText);
				guideReport(data.message);
			}
			else if(xhr.status === 403)
			{
				hideWaitingModalBackground(false);
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				guideReport(text);
			}
			else
			{
				hideWaitingModalBackground(false);
				let data = JSON.parse(xhr.responseText);
				guideReport(data.message);
			}
		}
	}
	xhr.send(formData);
	showWaitingModalBackground(false);

});







// SEND MESSAGE DAILOG
function sendMessageOn() {
	
	const sendMessageDrawerOnOff = document.getElementById('sendMessageDrawerOnOff');
	
	sendMessageDrawerOnOff.checked = true;
	let changeEvent = new Event('change');
	sendMessageDrawerOnOff.dispatchEvent(changeEvent);
	
}

function sendMessageOff() {
	
	const sendMessageDrawerOnOff = document.getElementById('sendMessageDrawerOnOff');
	
	sendMessageDrawerOnOff.checked = false;
	let changeEvent = new Event('change');
	sendMessageDrawerOnOff.dispatchEvent(changeEvent);
	
}

function guideSendMessage(text) {
	
	const sendMessageGuide = document.getElementById('sendMessageGuide');
	sendMessageGuide.innerText =  text;
	
	setTimeout(()=>{
		sendMessageGuide.innerText = '';
	},3000);
	
}




// DELETE ACCOUNT FUNCTIONS
function reportOn() {
	
	const deleteAccountOnOff = document.getElementById('reportDrawerOnOff');
	
	deleteAccountOnOff.checked = true;
	let changeEvent = new Event('change');
	deleteAccountOnOff.dispatchEvent(changeEvent);
	
}

function reportOff() {
	
	const deleteAccountOnOff = document.getElementById('reportDrawerOnOff');
	
	deleteAccountOnOff.checked = false;
	let changeEvent = new Event('change');
	deleteAccountOnOff.dispatchEvent(changeEvent);
	
}

function guideReport(text) {
	
	const changePasswordGuide = document.getElementById('reportGuide');
	changePasswordGuide.innerText =  text;
	
	setTimeout(()=>{
		changePasswordGuide.innerText = '';
	},3000);
	
}



// PROGRESS MODAL FUNCTIONS
function showWaitingModalBackground(hideScroll) {
	
	document.getElementById('modalBackground').style.display = 'flex';
	
	if(hideScroll) {
		
		let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
	
		document.body.style.paddingRight = `${scrollBarWidth}px`;
		if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
			document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
		}
		document.documentElement.style.overflow = 'hidden';
		
	}

}

function hideWaitingModalBackground(showScroll) {
	
	document.getElementById('modalBackground').style.display 		= 'none';
	
	if(showScroll) {
		document.documentElement.style.overflow 						= 'auto';
		document.body.style.paddingRight 								= '';
		document.getElementById('headerContainer').style.paddingRight 	= '';
	}
}

