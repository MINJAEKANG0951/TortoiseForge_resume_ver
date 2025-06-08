var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');




document.addEventListener('DOMContentLoaded',()=>{
	
	document.getElementById('changePasswordBtn');
	document.getElementById('changePasswordDrawerOnOff');
	document.getElementById('deleteAccountBtn');
	
})



// CHANGE PASSWORD EVENT LISTENERS
document.getElementById('changePasswordBtn').onclick = function(){
	if(document.getElementById('disableBtns').checked) {return;}
	changePasswordOn();
}

document.getElementById('changePasswordDrawerOnOff').addEventListener('change', function(){
	
	const changePasswordOnOff = document.getElementById('changePasswordDrawerOnOff');
	
	if(changePasswordOnOff.checked) 
	{
		document.getElementById('currentPassword').disabled 	= false;
		document.getElementById('newPassword').disabled 		= false;
		document.getElementById('confirmNewPassword').disabled 	= false;
		document.getElementById('passwordSaveBtn').disabled 	= false;
		
		
		let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
		
		document.body.style.paddingRight = `${scrollBarWidth}px`;
		if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
			document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
		}
		document.documentElement.style.overflow = 'hidden';
		
	}
	else 
	{
		document.getElementById('currentPassword').disabled 	= true;
		document.getElementById('newPassword').disabled 		= true;
		document.getElementById('confirmNewPassword').disabled 	= true;
		document.getElementById('passwordSaveBtn').disabled 	= true;
		
		document.getElementById('currentPassword').value 	= '';
		document.getElementById('newPassword').value 		= '';
		document.getElementById('confirmNewPassword').value = '';
		
		
		setTimeout(()=>{
			document.documentElement.style.overflow 						= 'auto';
			document.body.style.paddingRight 								= '';
			document.getElementById('headerContainer').style.paddingRight 	= '';
		},300);
	}
	
	
});

document.getElementById('passwordSaveBtn').addEventListener('click',function(){
	
	let currentPassword 	= document.getElementById('currentPassword').value;
	let newPassword 		= document.getElementById('newPassword').value;
	let newPasswordConfirm 	= document.getElementById('confirmNewPassword').value;
	
	let formData = new FormData();	
	formData.append('currentPassword', currentPassword);
	formData.append('newPassword', newPassword);
	formData.append('newPasswordConfirm', newPasswordConfirm);
	
	let xhr = new XMLHttpRequest();
	xhr.open('PUT','/accountSetting/password',true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				let message = data.message;
				let methodToExecute = function(){
					console.log('password changed successfully');
				};
				
				
				changePasswordOff();
				
				/*
					changePasswordOff() 시 0.3초뒤 스크롤이 복귀하는데,
					그 전에 showDialog()로 인해 dialog가 생성되면
					dialog가 보여지는 상태에서 스크롤이 복귀되기때문에 어색함.  
					그래서 dialog를 스크롤 복귀 하고 나서 호출함.
					그러면서 header layout 또한 스크롤에 의해 살짝 밀리는 현상이 발생
					
					>> 더 좋은 방법 있으면 나중에 수정
				*/
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
				guideChangePassword(data.message);
			}
			else if(xhr.status === 403)
			{
				hideWaitingModalBackground(false);
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				guideChangePassword(text);
			}
			else
			{
				hideWaitingModalBackground(false);
				let data = JSON.parse(xhr.responseText);
				guideChangePassword(data.message);
			}
		}
	}
	xhr.send(formData);
	showWaitingModalBackground(false);
	
});






// DELETE ACCOUNT EVENT LISTENERS
document.getElementById('deleteAccountBtn').onclick = function(){
	if(document.getElementById('disableBtns').checked) {return;}
	deleteAccountOn();
}


document.getElementById('deleteAccountDrawerOnOff').addEventListener('change', function(){
	
	const changePasswordOnOff = document.getElementById('deleteAccountDrawerOnOff');
	
	if(changePasswordOnOff.checked) 
	{
		document.getElementById('userPassword').disabled 		= false;
		document.getElementById('anonymizeRadio').disabled 		= false;
		document.getElementById('deleteRadio').disabled 		= false;
		document.getElementById('accountDeleteBtn').disabled 	= false;
		
		let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
		
		document.body.style.paddingRight = `${scrollBarWidth}px`;
		if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
			document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
		}
		document.documentElement.style.overflow = 'hidden';
		
	}
	else 
	{
		document.getElementById('userPassword').disabled 		= true;
		document.getElementById('anonymizeRadio').disabled 		= true;
		document.getElementById('deleteRadio').disabled 		= true;
		document.getElementById('accountDeleteBtn').disabled 	= true;
		
		document.getElementById('userPassword').value 			= '';
		document.getElementById('anonymizeRadio').checked 		= false;
		document.getElementById('deleteRadio').checked 			= false;
		
		setTimeout(()=>{
			document.documentElement.style.overflow 						= 'auto';
			document.body.style.paddingRight 								= '';
			document.getElementById('headerContainer').style.paddingRight 	= '';
		},300);
	}
	
	
});


document.getElementById('anonymizeRadioWrapper').addEventListener('click', function(){
	document.getElementById('anonymizeRadio').click();
});
document.getElementById('deleteRadioWrapper').addEventListener('click', function(){
	document.getElementById('deleteRadio').click();
});


document.getElementById('accountDeleteBtn').addEventListener('click', function(){
	
	let userPassword 		= document.getElementById('userPassword').value;
	let wayToDeleteAccount	= null;
	
	let wayToDeleteRadios = document.getElementsByName('wayToDeleteAccount');
	console.log(wayToDeleteRadios);
	for(let i=0; i<wayToDeleteRadios.length; i++) {
		if(wayToDeleteRadios[i].checked) {
			wayToDeleteAccount = wayToDeleteRadios[i].value;
			break;
		}
	}
	
	let formData = new FormData();	
	formData.append('userPassword', userPassword);
	
	if(wayToDeleteAccount != null) {
		formData.append('wayToDeleteAccount', wayToDeleteAccount);
	}
	
	
	let xhr = new XMLHttpRequest();
	xhr.open('DELETE','/accountSetting/account',true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				let message = data.message;
				let methodToExecute = function(){
					console.log('account deleted successfully');
				};
				
				disableAccountSettingPage();
				
				deleteAccountOff();
				
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
				guideDeleteAccount(data.message);
			}
			else if(xhr.status === 403)
			{
				hideWaitingModalBackground(false);
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				guideDeleteAccount(text);
			}
			else
			{
				hideWaitingModalBackground(false);
				let data = JSON.parse(xhr.responseText);
				guideDeleteAccount(data.message);
			}
		}
	}
	xhr.send(formData);
	showWaitingModalBackground(false);
	
});







// CHANGE PASSWORD FUNCTIONS
function changePasswordOn() {
	
	const changePasswordOnOff = document.getElementById('changePasswordDrawerOnOff');
	
	changePasswordOnOff.checked = true;
	let changeEvent = new Event('change');
	changePasswordOnOff.dispatchEvent(changeEvent);
	
}

function changePasswordOff() {
	
	const changePasswordOnOff = document.getElementById('changePasswordDrawerOnOff');
	
	changePasswordOnOff.checked = false;
	let changeEvent = new Event('change');
	changePasswordOnOff.dispatchEvent(changeEvent);
	
}

function guideChangePassword(text) {
	
	const changePasswordGuide = document.getElementById('changePasswordGuide');
	changePasswordGuide.innerText =  text;
	
	setTimeout(()=>{
		changePasswordGuide.innerText = '';
	},3000);
	
}




// DELETE ACCOUNT FUNCTIONS
function deleteAccountOn() {
	
	const deleteAccountOnOff = document.getElementById('deleteAccountDrawerOnOff');
	
	deleteAccountOnOff.checked = true;
	let changeEvent = new Event('change');
	deleteAccountOnOff.dispatchEvent(changeEvent);
	
}

function deleteAccountOff() {
	
	const deleteAccountOnOff = document.getElementById('deleteAccountDrawerOnOff');
	
	deleteAccountOnOff.checked = false;
	let changeEvent = new Event('change');
	deleteAccountOnOff.dispatchEvent(changeEvent);
	
}

function guideDeleteAccount(text) {
	
	const deleteAccountGuide = document.getElementById('deleteAccountGuide');
	deleteAccountGuide.innerText =  text;
	
	setTimeout(()=>{
		deleteAccountGuide.innerText = '';
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



function disableAccountSettingPage() { // executed once user account is scheduled for deletion/deactivation
	document.getElementById('disableBtns').checked = true;
}
