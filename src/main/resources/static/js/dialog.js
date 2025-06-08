

document.addEventListener('DOMContentLoaded',()=>{ 
	
	window.addEventListener('pageshow', function(event) {
	    if (event.persisted) {
			document.getElementById('dialogOnOff').checked = false;
	    } else {
			document.getElementById('dialogOnOff').checked = false;
	    }
	    
		let changeEvent = new Event('change');
		document.getElementById('dialogOnOff').dispatchEvent(changeEvent);
	});
	
	
	document.getElementById('dialogOnOff').addEventListener('change',()=>{
		
		let checked = document.getElementById('dialogOnOff').checked;
		let headerContainer = document.getElementById('headerContainer');
		
		if(checked)
		{
			let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
			
			document.body.style.paddingRight = `${scrollBarWidth}px`;
			if ( headerContainer!=null && headerContainer.getAttribute('type') === null ) {
				headerContainer.style.paddingRight = `${scrollBarWidth}px`;
			}
			document.documentElement.style.overflow = 'hidden';
		}
		else 
		{
			document.documentElement.style.overflow 						= 'auto';
			document.body.style.paddingRight 								= '';
			if(headerContainer!=null) {
				document.getElementById('headerContainer').style.paddingRight 	= '';
			}
		}
		
	});
	
});


function showDialog() {
	
	document.getElementById('dialogOnOff').checked = true;
	let changeEvent = new Event('change');
	document.getElementById('dialogOnOff').dispatchEvent(changeEvent);
	
};
function hideDialog(){
	
	document.getElementById('dialogOnOff').checked = false;
	let changeEvent = new Event('change');
	document.getElementById('dialogOnOff').dispatchEvent(changeEvent);
	
}



function dialog_yesOrNo(text, methodToExecute) 
{
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let normalTextWrapperElement 		= document.createElement('div');
	normalTextWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= text;
	
	normalTextWrapperElement.appendChild(normalTextElement);
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'noBtn';
	noBtnElement.innerText			= 'NO';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	let yesBtnElement				= document.createElement('label');
	yesBtnElement.className			= 'yesBtn';
	yesBtnElement.innerText			= 'YES';
	
	buttonPairElement.appendChild(noBtnElement);
	buttonPairElement.appendChild(yesBtnElement);
	
	
	dialog.appendChild(normalTextWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	dialog.querySelector('.yesBtn').addEventListener('click',()=>{
		hideDialog();
		methodToExecute();
	})
	
}



function dialog_yesOrNo2(text, methodToExecute) {
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let normalTextWrapperElement 		= document.createElement('div');
	normalTextWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= text;
	
	normalTextWrapperElement.appendChild(normalTextElement);
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'noBtn';
	noBtnElement.innerText			= 'NO';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	let yesBtnElement				= document.createElement('label');
	yesBtnElement.className			= 'deleteBtn';
	yesBtnElement.innerText			= 'YES';
	
	buttonPairElement.appendChild(noBtnElement);
	buttonPairElement.appendChild(yesBtnElement);
	
	
	dialog.appendChild(normalTextWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	dialog.querySelector('.deleteBtn').addEventListener('click',()=>{
		hideDialog();
		methodToExecute();
	})
	
}

function dialog_yesOrNo3(text, leftBtnText, rightBtnText, leftMethod, rightMethod) {
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let normalTextWrapperElement 		= document.createElement('div');
	normalTextWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= text;
	
	normalTextWrapperElement.appendChild(normalTextElement);
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let leftBtnElement				= document.createElement('label');
	leftBtnElement.className			= 'noBtn';
	leftBtnElement.innerText			= leftBtnText;
	
	let rightBtnElement				= document.createElement('label');
	rightBtnElement.className			= 'yesBtn';
	rightBtnElement.innerText			= rightBtnText;
	
	buttonPairElement.appendChild(leftBtnElement);
	buttonPairElement.appendChild(rightBtnElement);
	
	dialog.appendChild(normalTextWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	dialog.querySelector('.yesBtn').addEventListener('click',()=>{
		hideDialog();
		rightMethod();
	})
	dialog.querySelector('.noBtn').addEventListener('click',()=>{
		hideDialog();
		leftMethod();
	})
	
}

function dialog_yesOrNo4(text, leftBtnText, rightBtnText, leftMethod, rightMethod) {
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let normalTextWrapperElement 		= document.createElement('div');
	normalTextWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= text;
	
	normalTextWrapperElement.appendChild(normalTextElement);
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let leftBtnElement				= document.createElement('label');
	leftBtnElement.className			= 'noBtn';
	leftBtnElement.innerText			= leftBtnText;
	
	let rightBtnElement				= document.createElement('label');
	rightBtnElement.className			= 'deleteBtn';
	rightBtnElement.innerText			= rightBtnText;
	
	buttonPairElement.appendChild(leftBtnElement);
	buttonPairElement.appendChild(rightBtnElement);
	
	dialog.appendChild(normalTextWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	dialog.querySelector('.deleteBtn').addEventListener('click',()=>{
		hideDialog();
		rightMethod();
	})
	dialog.querySelector('.noBtn').addEventListener('click',()=>{
		hideDialog();
		leftMethod();
	})
	
}


function dialog_confirm(text, methodToExecute)	// 닫혀도 실행 버튼눌러도 실행되게 upgrade 필요함
{
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let normalTextWrapperElement 		= document.createElement('div');
	normalTextWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= text;
	
	normalTextWrapperElement.appendChild(normalTextElement);
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let confirmBtnElement				= document.createElement('label');
	confirmBtnElement.className			= 'confirmBtn';
	confirmBtnElement.innerText			= 'CONFIRM';
	
	buttonPairElement.appendChild(confirmBtnElement);
	
	dialog.appendChild(normalTextWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	dialog.querySelector('.confirmBtn').addEventListener('click',()=>{
		hideDialog();
		methodToExecute();
	});
	
}



function dialog_pageNavigation(currentPageNum, maxPage, methodToExecute) {
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let normalTextWrapperElement 		= document.createElement('div');
	normalTextWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= 'Please enter the page number you want to navigate to.';
	
	normalTextWrapperElement.appendChild(normalTextElement);
	
	let pageNavigatorElement		= document.createElement('div');
	pageNavigatorElement.className	= 'dialogContent';
	pageNavigatorElement.setAttribute('name','pageNavigator');
	
	let pageToGoInputElement		= document.createElement('input');
	pageToGoInputElement.className	= 'pageToGo';
	pageToGoInputElement.setAttribute('type','number');
	pageToGoInputElement.setAttribute('step','1');
	pageToGoInputElement.value		= currentPageNum;
	
	let pageMaxElement				= document.createElement('span');
	pageMaxElement.className		= 'pageMax';
	pageMaxElement.innerText		= maxPage;
	
	pageNavigatorElement.appendChild(pageToGoInputElement);
	pageNavigatorElement.appendChild(pageMaxElement);
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'noBtn';
	noBtnElement.innerText			= 'CANCEL';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	let yesBtnElement				= document.createElement('label');
	yesBtnElement.className			= 'yesBtn';
	yesBtnElement.innerText			= 'MOVE';
	
	buttonPairElement.appendChild(noBtnElement);
	buttonPairElement.appendChild(yesBtnElement);
	
	dialog.appendChild(normalTextWrapperElement);
	dialog.appendChild(pageNavigatorElement);
	dialog.appendChild(buttonPairElement);

	dialog.querySelector('.yesBtn').addEventListener('click',()=>{
		hideDialog();
		methodToExecute();
	});
	
	
}



function dialog_replyToComment(username, maxContentLength, methodToExecute) {
	
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let textWrapperElement 		= document.createElement('div');
	textWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= 'Would you like to reply to ';
	
	let boldTextElement 			= document.createElement('span');
	boldTextElement.className		= 'boldText';
	boldTextElement.innerText		= username;
	
	let normalTextElement2				= document.createElement('span');
	normalTextElement2.className		= 'normalText';
	normalTextElement2.innerText		= `'s comment?`;
	
	textWrapperElement.appendChild(normalTextElement);
	textWrapperElement.appendChild(boldTextElement);
	textWrapperElement.appendChild(normalTextElement2);
	
	
	let contentWriteWrapperElement			= document.createElement('div');
	contentWriteWrapperElement.className	= 'dialogContent'
	contentWriteWrapperElement.setAttribute('name','contentWriteWrapper');
	
	let contentWriteElement			=	document.createElement('textarea');
	contentWriteElement.className	=	'contentWrite';
	contentWriteElement.setAttribute('spellCheck','false');
	contentWriteElement.setAttribute('maxlength', maxContentLength);
	contentWriteElement.setAttribute('rows','1');

	let contentLengthWrapperElement			=	document.createElement('div');
	contentLengthWrapperElement.className	=	'contentLengthWrapper';
	
	let contentLengthElement		=	document.createElement('span');
	contentLengthElement.innerText	=	'0';
	
	let slashElement		=	document.createElement('span');
	slashElement.innerText	=	' / ';
	
	let contentMaxLengthElement			=	document.createElement('span');
	contentMaxLengthElement.innerText	=	maxContentLength;
	
	contentLengthWrapperElement.appendChild(contentLengthElement);
	contentLengthWrapperElement.appendChild(slashElement);
	contentLengthWrapperElement.appendChild(contentMaxLengthElement);
	
	contentWriteWrapperElement.appendChild(contentWriteElement);
	contentWriteWrapperElement.appendChild(contentLengthWrapperElement);
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'noBtn';
	noBtnElement.innerText			= 'CANCEL';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	let yesBtnElement				= document.createElement('label');
	yesBtnElement.className			= 'yesBtn';
	yesBtnElement.innerText			= 'COMMENT';
	
	buttonPairElement.appendChild(noBtnElement);
	buttonPairElement.appendChild(yesBtnElement);
	
	dialog.appendChild(textWrapperElement);
	dialog.appendChild(contentWriteWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	
	// ADD EVENT
	contentWriteElement.addEventListener('input',()=>{
		
		let currentContentLength = parseInt( contentWriteElement.value.length );
		contentLengthElement.innerText = currentContentLength;
		
	});
	
	
	dialog.querySelector('.yesBtn').addEventListener('click',()=>{
		hideDialog();
		methodToExecute();
	});
	
}



function dialog_additionalWorkOnComment(reportFunc, modifyFunc, deleteFunc) {
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let textWrapperElement 			= document.createElement('div');
	textWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= 'What would you like to do to the comment?';
	
	textWrapperElement.appendChild(normalTextElement);

	
	/*
	let reportMenuElement =	document.createElement('div');
	reportMenuElement.className = 'dialogContent';
	reportMenuElement.setAttribute('name','menu');
	
	let reportMenuNameAndImgWrapperElement = document.createElement('div');
	reportMenuNameAndImgWrapperElement.className = 'menuNameAndImg';
	
	let reportMenuNameElement = document.createElement('div');
	reportMenuNameElement.className = 'menuName';
	reportMenuNameElement.innerText = 'REPORT';
	
	let reportMenuImgElement = document.createElement('img');
	reportMenuImgElement.className = 'menuImg';
	reportMenuImgElement.src = '/img/report.png';
	
	reportMenuNameAndImgWrapperElement.appendChild(reportMenuNameElement);
	reportMenuNameAndImgWrapperElement.appendChild(reportMenuImgElement);
	
	let reportMenuArrowElement = document.createElement('img');
	reportMenuArrowElement.className = 'arrowImg';
	reportMenuArrowElement.src = '/img/backArrow.png';
	
	reportMenuElement.appendChild(reportMenuNameAndImgWrapperElement);
	reportMenuElement.appendChild(reportMenuArrowElement);
	*/
	
	
	let modifyMenuElement =	document.createElement('div');
	modifyMenuElement.className = 'dialogContent';
	modifyMenuElement.setAttribute('name','menu');
	
	let modifyMenuNameAndImgWrapperElement = document.createElement('div');
	modifyMenuNameAndImgWrapperElement.className = 'menuNameAndImg';
	
	let modifyMenuNameElement = document.createElement('div');
	modifyMenuNameElement.className = 'menuName';
	modifyMenuNameElement.innerText = 'MODIFY';
	
	let modifyMenuImgElement = document.createElement('img');
	modifyMenuImgElement.className = 'menuImg';
	modifyMenuImgElement.src = '/img/mini/hammer_mini.png';
	
	modifyMenuNameAndImgWrapperElement.appendChild(modifyMenuNameElement);
	modifyMenuNameAndImgWrapperElement.appendChild(modifyMenuImgElement);
	
	let modifyMenuArrowElement = document.createElement('img');
	modifyMenuArrowElement.className = 'arrowImg';
	modifyMenuArrowElement.src = '/img/mini/arrow_forward_mini.png';
	
	modifyMenuElement.appendChild(modifyMenuNameAndImgWrapperElement);
	modifyMenuElement.appendChild(modifyMenuArrowElement);
	
	
	
	let deleteMenuElement =	document.createElement('div');
	deleteMenuElement.className = 'dialogContent';
	deleteMenuElement.setAttribute('name','menu');
	
	let deleteMenuNameAndImgWrapperElement = document.createElement('div');
	deleteMenuNameAndImgWrapperElement.className = 'menuNameAndImg';
	
	let deleteMenuNameElement = document.createElement('div');
	deleteMenuNameElement.className = 'menuName';
	deleteMenuNameElement.innerText = 'DELETE';
	
	let deleteMenuImgElement = document.createElement('img');
	deleteMenuImgElement.className = 'menuImg';
	deleteMenuImgElement.src = '/img/mini/trashbin2_mini.png';
	
	deleteMenuNameAndImgWrapperElement.appendChild(deleteMenuNameElement);
	deleteMenuNameAndImgWrapperElement.appendChild(deleteMenuImgElement);
	
	let deleteMenuArrowElement = document.createElement('img');
	deleteMenuArrowElement.className = 'arrowImg';
	deleteMenuArrowElement.src = '/img/mini/arrow_forward_mini.png';
	
	deleteMenuElement.appendChild(deleteMenuNameAndImgWrapperElement);
	deleteMenuElement.appendChild(deleteMenuArrowElement);
	
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'cancelBtn';
	noBtnElement.innerText			= 'CANCEL';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	buttonPairElement.appendChild(noBtnElement);
	
	dialog.appendChild(textWrapperElement);
	//dialog.appendChild(reportMenuElement);
	dialog.appendChild(modifyMenuElement);
	dialog.appendChild(deleteMenuElement);
	dialog.appendChild(buttonPairElement);
	
	
	// ADD EVENT LISTENERS
	/*
	reportMenuElement.addEventListener('click',()=>{
		hideDialog();
		reportFunc();
	});
	*/
	modifyMenuElement.addEventListener('click',()=>{
		hideDialog();
		modifyFunc();
	});
	deleteMenuElement.addEventListener('click',()=>{
		hideDialog();
		deleteFunc();
	});
	

}



function dialog_modifyComment(oldCommentContent, maxContentLength, methodToExecute) {
		
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let textWrapperElement 		= document.createElement('div');
	textWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= 'Please enter the updated content.';
	
	textWrapperElement.appendChild(normalTextElement);
	
	let contentWriteWrapperElement			= document.createElement('div');
	contentWriteWrapperElement.className	= 'dialogContent'
	contentWriteWrapperElement.setAttribute('name','contentWriteWrapper');
	
	let contentWriteElement			=	document.createElement('textarea');
	contentWriteElement.className	=	'contentWrite';
	contentWriteElement.setAttribute('spellCheck','false');
	contentWriteElement.setAttribute('rows','1');
	contentWriteElement.setAttribute('maxlength', maxContentLength);
	contentWriteElement.value = oldCommentContent;

	let contentLengthWrapperElement			=	document.createElement('div');
	contentLengthWrapperElement.className	=	'contentLengthWrapper';
	
	let contentLengthElement		=	document.createElement('span');
	contentLengthElement.innerText	=	oldCommentContent.length;
	
	let slashElement		=	document.createElement('span');
	slashElement.innerText	=	' / ';
	
	let contentMaxLengthElement			=	document.createElement('span');
	contentMaxLengthElement.innerText	=	maxContentLength;
	
	contentLengthWrapperElement.appendChild(contentLengthElement);
	contentLengthWrapperElement.appendChild(slashElement);
	contentLengthWrapperElement.appendChild(contentMaxLengthElement);
	
	contentWriteWrapperElement.appendChild(contentWriteElement);
	contentWriteWrapperElement.appendChild(contentLengthWrapperElement);
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'noBtn';
	noBtnElement.innerText			= 'CANCEL';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	let yesBtnElement				= document.createElement('label');
	yesBtnElement.className			= 'yesBtn';
	yesBtnElement.innerText			= 'MODFIY';
	
	buttonPairElement.appendChild(noBtnElement);
	buttonPairElement.appendChild(yesBtnElement);
	
	dialog.appendChild(textWrapperElement);
	dialog.appendChild(contentWriteWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	
	// ADD EVENT
	contentWriteElement.addEventListener('input',()=>{
		
		let currentContentLength = parseInt( contentWriteElement.value.length );
		contentLengthElement.innerText = currentContentLength;
		
	});
	
	
	dialog.querySelector('.yesBtn').addEventListener('click',()=>{
		hideDialog();
		methodToExecute();
	});
	
}




function dialog_setUserProfileImage(uploadFunc, deleteFunc) {
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let textWrapperElement 			= document.createElement('div');
	textWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= 'What would you like to do to your profile image?';
	
	textWrapperElement.appendChild(normalTextElement);


	let uploadMenuElement =	document.createElement('div');
	uploadMenuElement.className = 'dialogContent';
	uploadMenuElement.setAttribute('name','menu');
	
	let uploadMenuNameAndImgWrapperElement = document.createElement('div');
	uploadMenuNameAndImgWrapperElement.className = 'menuNameAndImg';
	
	let uploadMenuNameElement = document.createElement('div');
	uploadMenuNameElement.className = 'menuName';
	uploadMenuNameElement.innerText = 'UPLOAD IMAGE';
	
	let uploadMenuImgElement = document.createElement('img');
	uploadMenuImgElement.className = 'menuImg';
	uploadMenuImgElement.src = '/img/mini/upload_mini.png';
	
	uploadMenuNameAndImgWrapperElement.appendChild(uploadMenuNameElement);
	uploadMenuNameAndImgWrapperElement.appendChild(uploadMenuImgElement);
	
	let uploadMenuArrowElement = document.createElement('img');
	uploadMenuArrowElement.className = 'arrowImg';
	uploadMenuArrowElement.src = '/img/mini/arrow_forward_mini.png';
	
	uploadMenuElement.appendChild(uploadMenuNameAndImgWrapperElement);
	uploadMenuElement.appendChild(uploadMenuArrowElement);

	

	let deleteMenuElement =	document.createElement('div');
	deleteMenuElement.className = 'dialogContent';
	deleteMenuElement.setAttribute('name','menu');
	
	let deleteMenuNameAndImgWrapperElement = document.createElement('div');
	deleteMenuNameAndImgWrapperElement.className = 'menuNameAndImg';
	
	let deleteMenuNameElement = document.createElement('div');
	deleteMenuNameElement.className = 'menuName';
	deleteMenuNameElement.innerText = 'USE DEFAULT IMAGE';
	
	let deleteMenuImgElement = document.createElement('img');
	deleteMenuImgElement.className = 'menuImg';
	deleteMenuImgElement.src = '/img/mini/tortoise_upperBody_colored_mini.png';
	
	deleteMenuNameAndImgWrapperElement.appendChild(deleteMenuNameElement);
	deleteMenuNameAndImgWrapperElement.appendChild(deleteMenuImgElement);
	
	let deleteMenuArrowElement = document.createElement('img');
	deleteMenuArrowElement.className = 'arrowImg';
	deleteMenuArrowElement.src = '/img/mini/arrow_forward_mini.png';
	
	deleteMenuElement.appendChild(deleteMenuNameAndImgWrapperElement);
	deleteMenuElement.appendChild(deleteMenuArrowElement);
	
	
	
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'cancelBtn';
	noBtnElement.innerText			= 'CANCEL';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	buttonPairElement.appendChild(noBtnElement);
	
	dialog.appendChild(textWrapperElement);
	dialog.appendChild(uploadMenuElement);
	dialog.appendChild(deleteMenuElement);
	dialog.appendChild(buttonPairElement);
	
	
	// ADD EVENT LISTENERS
	
	uploadMenuElement.addEventListener('click',()=>{
		hideDialog();
		uploadFunc();
	});
	
	deleteMenuElement.addEventListener('click',()=>{
		hideDialog();
		deleteFunc();
	});
	

}


function dialog_changePassword(methodToExecute) {
	
	
	const dialog = document.getElementById('dialog');
	dialog.innerHTML = '';
	
	let textWrapperElement 			= document.createElement('div');
	textWrapperElement.className 	= 'dialogContent';
	
	let normalTextElement 			= document.createElement('span');
	normalTextElement.className		= 'normalText';
	normalTextElement.innerText		= 'Enter your current password and new password.';
	
	textWrapperElement.appendChild(normalTextElement);
	
	
	//
	
	let currentPasswordInputWrapperElement				= document.createElement('form');
	currentPasswordInputWrapperElement.className		= 'dialogContent';
	currentPasswordInputWrapperElement.setAttribute('name','inputWrapper');
	
	let currentPasswordInputTitleElement		= document.createElement('div');
	currentPasswordInputTitleElement.className	= 'inputBoxTitle';
	currentPasswordInputTitleElement.innerText	= 'Current password';
	
	let currentPasswordInputElement				= document.createElement('input');
	currentPasswordInputElement.className		= 'inputBox';
	currentPasswordInputElement.type			= 'password';
	
	currentPasswordInputWrapperElement.appendChild(currentPasswordInputTitleElement);
	currentPasswordInputWrapperElement.appendChild(currentPasswordInputElement);
	
	
	
	let newPasswordInputWrapperElement			= document.createElement('form');
	newPasswordInputWrapperElement.className	= 'dialogContent';
	newPasswordInputWrapperElement.setAttribute('name','inputWrapper');
	
	let newPasswordInputTitleElement		= document.createElement('div');
	newPasswordInputTitleElement.className	= 'inputBoxTitle';
	newPasswordInputTitleElement.innerText	= 'New password';
	
	let newPasswordInputElement				= document.createElement('input');
	newPasswordInputElement.className		= 'inputBox';
	newPasswordInputElement.type			= 'password';
	
	newPasswordInputWrapperElement.appendChild(newPasswordInputTitleElement);
	newPasswordInputWrapperElement.appendChild(newPasswordInputElement);
	
	
	
	let newPasswordConfirmInputWrapperElement			= document.createElement('form');
	newPasswordConfirmInputWrapperElement.className		= 'dialogContent';
	newPasswordConfirmInputWrapperElement.setAttribute('name','inputWrapper');
	
	
	let newPasswordConfirmInputTitleElement			= document.createElement('div');
	newPasswordConfirmInputTitleElement.className	= 'inputBoxTitle';
	newPasswordConfirmInputTitleElement.innerText	= 'New password confirm';
	
	let newPasswordConfirmInputElement				= document.createElement('input');
	newPasswordConfirmInputElement.className		= 'inputBox';
	newPasswordConfirmInputElement.type				= 'password';
	
	newPasswordConfirmInputWrapperElement.appendChild(newPasswordConfirmInputTitleElement);
	newPasswordConfirmInputWrapperElement.appendChild(newPasswordConfirmInputElement);
	
	//
		
	let buttonPairElement			= document.createElement('div');
	buttonPairElement.className 	= 'dialogContent';
	buttonPairElement.setAttribute('name','buttonPair');
	
	let noBtnElement				= document.createElement('label');
	noBtnElement.className			= 'noBtn';
	noBtnElement.innerText			= 'CANCEL';
	noBtnElement.setAttribute('for', 'dialogOnOff');
	
	let yesBtnElement				= document.createElement('label');
	yesBtnElement.className			= 'yesBtn';
	yesBtnElement.innerText			= 'CHANGE';
	
	buttonPairElement.appendChild(noBtnElement);
	buttonPairElement.appendChild(yesBtnElement);


	dialog.appendChild(textWrapperElement);
	dialog.append(currentPasswordInputWrapperElement);
	dialog.append(newPasswordInputWrapperElement);
	dialog.append(newPasswordConfirmInputWrapperElement);
	dialog.appendChild(buttonPairElement);
	
	
	dialog.querySelector('.yesBtn').addEventListener('click',()=>{
		hideDialog();
		methodToExecute();
	});
	
}



