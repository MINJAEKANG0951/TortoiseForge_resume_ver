var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

 
// pagination variables
var currentMessageType; 
var currentPageNum;
var currentPageSize;
var totalNumberOfPages;


// DOM elements
let msgTypeRadios;
let inboxMsgRadio;
let sentMsgRadio;

let composeBtn;
let sendMessageDrawerOnOff;
let recipient;					
let messageToSend;				
let messageToSendCurrentLength;	
let sendMessageGuide;			
let sendMessageBtn;

let paginationBtn;
let currentPage;
let lastPage;

let selectAllBtn;
let deleteSelectedBtn;
let unreadSelectedBtn;
let readSelectedBtn;

let messages;
let emptyMessages;

let moreBtn;
let moreBtnArrow;
let moreBtnProgressCircle;

let boardBottomNavBar;
let pagesAround;
let pagesAroundLeftArrow;
let pagesAroundRightArrow;

let readMessageDrawerOnOff;
let readMessageSeq;
let readMessageType;
let readMessageDirection;
let readMessageParticipant;
let readMessageCreated;
let readMessageContent;
let readMessageGuide;
let readMessageDeleteBtn;
let readMessageReplyBtn;


 
document.addEventListener('DOMContentLoaded',()=>{

	// INITIALIZE DOM ELEMENTS
	msgTypeRadios			= document.getElementsByClassName('messageType');
	inboxMsgRadio 			= document.getElementById('inboxMessage');
	sentMsgRadio			= document.getElementById('sentMessage');
	
	composeBtn					= document.getElementById('composeBtn');
	sendMessageDrawerOnOff		= document.getElementById('sendMessageDrawerOnOff');
	recipient					= document.getElementById('recipient');
	messageToSend				= document.getElementById('message');
	messageToSendCurrentLength	= document.getElementById('messageCurrentLength');
	sendMessageGuide			= document.getElementById('sendMessageGuide');
	sendMessageBtn				= document.getElementById('sendMessageBtn');
	
	paginationBtn			= document.getElementById('paginationBtn');
	currentPage				= document.getElementById('currentPage');
	lastPage				= document.getElementById('lastPage');
	selectAllBtn			= document.getElementById('selectAll');
	deleteSelectedBtn		= document.getElementById('deleteSelectedBtn');
	unreadSelectedBtn		= document.getElementById('unreadSelectedBtn');
	readSelectedBtn			= document.getElementById('readSelectedBtn');
	messages				= document.getElementById('messages');
	emptyMessages			= document.querySelector('.emptyMessages');
	moreBtn					= document.getElementById('moreBtn');
	moreBtnArrow			= document.getElementById('moreBtnArrow');
	moreBtnProgressCircle	= document.getElementById('moreBtnProgressCircle');
	boardBottomNavBar		= document.querySelector('.boardBottomNavBar');
	pagesAround				= document.getElementById('pagesAround');
	pagesAroundLeftArrow	= document.getElementById('pagesAroundLeftArrow');
	pagesAroundRightArrow	= document.getElementById('pagesAroundRightArrow');
	
	
	// INITIALIZE VARIABLES
	for(let i=0;i<msgTypeRadios.length;i++) {
		if(msgTypeRadios[i].checked) {
			currentMessageType = msgTypeRadios[i].value;
			break;
		}
	}
	currentPageNum 			= parseInt( document.getElementById('currentPage').innerText );
	currentPageSize			= parseInt( document.getElementById('pageSize').value );
	totalNumberOfPages 		= parseInt( document.getElementById('lastPage').innerText );
	
	
	readMessageDrawerOnOff	= document.getElementById('readMessageDrawerOnOff');
	readMessageSeq			= document.getElementById('readMessageSeq');
	readMessageType			= document.getElementById('readMessageType');
	readMessageDirection	= document.getElementById('readMessageDirection');
	readMessageParticipant	= document.getElementById('readMessageParticipant');
	readMessageCreated		= document.getElementById('readMessageCreated');
	readMessageContent		= document.getElementById('readMessageContent');
	readMessageGuide		= document.getElementById('readMessageGuide');
	readMessageDeleteBtn	= document.getElementById('readMessageDeleteBtn');
	readMessageReplyBtn		= document.getElementById('readMessageReplyBtn');



	// REPRESH TIEM ELEMENTS
	refreshTimeElements();


	// MESSAGE TYPE RADIOS
	for(let i=0;i<msgTypeRadios.length;i++) 
	{
		let msgTypeRadio = msgTypeRadios[i];
		msgTypeRadio.addEventListener('change',function(){
			if(msgTypeRadio.checked){
				
				currentMessageType 	= parseInt(msgTypeRadio.value);
				currentPageNum		= 1;
				load(true);
				
			}
		});
	}
	
	
	
	// PAGINATION BTN
	paginationBtn.addEventListener('click',function(){

		setPageNavDialog();
		showDialog();
		
	});
	
	
	
	// COMPOSE BTN
	composeBtn.addEventListener('click',()=>{
		sendMessageOn(null);
	});
	
	
	// SEND MESSAGE DIALOG
	sendMessageDrawerOnOff.addEventListener('change', function(){
		
		if(sendMessageDrawerOnOff.checked) 
		{
			recipient.disabled 			= false;
			sendMessageBtn.disabled 	= false;
			messageToSend.disabled 		= false;
			
			recipient.value = '';
			messageToSend.value = '';
			sendMessageGuide.innerText = '';
			
			const inputEvent = new Event('input', { bubbles: true, cancelable: true });
			messageToSend.dispatchEvent(inputEvent);
			
			let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
			
			document.body.style.paddingRight = `${scrollBarWidth}px`;
			if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
				document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
			}
			document.documentElement.style.overflow = 'hidden';
			
		}
		else 
		{
			recipient.disabled 			= true;
			sendMessageBtn.disabled 	= true;
			messageToSend.disabled 		= true;
				
			setTimeout(()=>{
				document.documentElement.style.overflow 						= 'auto';
				document.body.style.paddingRight 								= '';
				document.getElementById('headerContainer').style.paddingRight 	= '';
			},300);
		}
		
	});
	
	
	// SEND MESSAGE BTN
	sendMessageBtn.addEventListener('click', function(){
		
		let recipientName	=	recipient.value;
		let messageContent	=	messageToSend.value;
		
		sendMessage(recipientName, messageContent);

	});
	

	// SELECT ALL BTN
	selectAllBtn.addEventListener('change',()=>{
		
		let messageCheckBoxes 	= document.querySelectorAll('.messageCheckbox');
		if(selectAllBtn.checked) 
		{
			for(let i=0;i<messageCheckBoxes.length;i++)
			{
				messageCheckBoxes[i].checked = true;	
			}
		} 
		else 
		{
			for(let i=0;i<messageCheckBoxes.length;i++)
			{
				messageCheckBoxes[i].checked = false;
			}
		}
		
	});
	
	
	
	// DELETE SELECTED
	deleteSelectedBtn.addEventListener('click',()=>{
		
		let text = 'Are you sure to delete the selected messages?';
		let methodToExecute = ()=>{
			
			let messageCheckboxes = document.getElementsByClassName('messageCheckbox');
			let checkedMessageSeqs	= [];
			
			for(let i=0;i<messageCheckboxes.length;i++) {
				let messageCheckbox = messageCheckboxes[i];
				if(messageCheckbox.checked) {
					checkedMessageSeqs.push(
						parseInt(messageCheckbox.getAttribute('messageSeq'))
					);
				}
			}
				
			deleteMessages(checkedMessageSeqs);	
			
		};
		
		dialog_yesOrNo2(text, methodToExecute);
		showDialog();
		
	});
	
	
	// READ SELECTED
	readSelectedBtn.addEventListener('click',()=>{
		if(sentMsgRadio.checked) {return;}
		
		let messageCheckboxes = document.getElementsByClassName('messageCheckbox');
		let checkedMessageSeqs	= [];
		
		for(let i=0;i<messageCheckboxes.length;i++) {
			let messageCheckbox = messageCheckboxes[i];
			if(messageCheckbox.checked) {
				checkedMessageSeqs.push(
					parseInt(messageCheckbox.getAttribute('messageSeq'))
				);
			}
		}
		
		let methodToExecute = ()=>{ markMessages(true, checkedMessageSeqs) };
		dialog_yesOrNo('Do you want to mark the selected messages as read?', methodToExecute);
		showDialog();

	});
	
	
	// UNREAD SELECTED
	unreadSelectedBtn.addEventListener('click',()=>{
		if(sentMsgRadio.checked) {return;}
		
		let messageCheckboxes = document.getElementsByClassName('messageCheckbox');
		let checkedMessageSeqs	= [];
		
		for(let i=0;i<messageCheckboxes.length;i++) {
			let messageCheckbox = messageCheckboxes[i];
			if(messageCheckbox.checked) {
				checkedMessageSeqs.push(
					parseInt(messageCheckbox.getAttribute('messageSeq'))
				);
			}
		}
		
		let methodToExecute = ()=>{ markMessages(false, checkedMessageSeqs) };
		dialog_yesOrNo('Do you want to mark the selected messages as unread?', methodToExecute);
		showDialog();
		
	});
	
	
	// MESSAGES
	messages.addEventListener('click',(event)=>{
		
		let selectMessage 	= event.target.closest('.selectMessage');
		let messageInfo 	= event.target.closest('.messageInfo');
		
		if(selectMessage)
		{
			return false;
		}
		else if(messageInfo)
		{
			// 열기. 그런데 열기 전에 세팅 다 해놓고 
			let messageSeq = parseInt( messageInfo.querySelector('.messageSeq').innerText );
			let messageType = messageInfo.parentElement.getAttribute('type');
			let messageDirection = messageInfo.querySelector('.direction').innerText;
			let messageCreated = messageInfo.querySelector('.created').innerText;
			let messageParticipant = messageInfo.querySelector('.participantName').innerText;
			let messageContent = messageInfo.querySelector('.messageContent').innerText;
			
			readMessageOn(messageSeq, messageType, messageDirection, messageParticipant, messageCreated, messageContent);
		}
		
	});
	
	readMessageDrawerOnOff.addEventListener('change', ()=>{
		
		if(readMessageDrawerOnOff.checked)
		{
			let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;

			document.body.style.paddingRight = `${scrollBarWidth}px`;
			if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
				document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
			}
			document.documentElement.style.overflow = 'hidden';
			readMessageDeleteBtn.disabled = false;
			readMessageReplyBtn.disabled = false;
		}
		else
		{
			readMessageDeleteBtn.disabled = true;
			readMessageReplyBtn.disabled = true;
			
			setTimeout(()=>{
				document.documentElement.style.overflow 						= 'auto';
				document.body.style.paddingRight 								= '';
				document.getElementById('headerContainer').style.paddingRight 	= '';
			},300);
		}
		
	});

	readMessageDeleteBtn.addEventListener('click',()=>{
		
		let messageToDelete = parseInt(readMessageSeq.innerText);
		
		let leftMethodToExecute = ()=>{	readMessageOnAgain(); };
		
		let rightMethodToExecute = ()=>{
			
			let messagesToDelete = [];
			messagesToDelete.push(messageToDelete);
			
			deleteMessages(messagesToDelete);
			
		};
		
		readMessageOff(); 
		showWaitingModalBackground(false);
		setTimeout(()=>{
			
			hideWaitingModalBackground(false);
			dialog_yesOrNo4('Are you sure to delete the message?', 'BACK', 'DELETE', leftMethodToExecute, rightMethodToExecute);
			showDialog();
	
		},301);
		
	});
	
	readMessageReplyBtn.addEventListener('click',()=>{
		
		let username = readMessageParticipant.innerText;
		readMessageOff();
		sendMessageOn(username);
		
	});
	
	// MORE BTN
	moreBtn.addEventListener('click',()=>{
		
		if(currentPageNum >= totalNumberOfPages) return;
		currentMessageType 	= currentMessageType;
		currentPageNum		= currentPageNum + 1;
		load(false);
		
	});
	
	
	// PAGES AROUND
	pagesAroundLeftArrow.addEventListener('click',()=>{
		if(currentPageNum <= 1) return;

		currentMessageType 	= currentMessageType;
		currentPageNum		= currentPageNum - 5;
		load(true);
		
	});
	
	pagesAroundRightArrow.addEventListener('click',()=>{
		if(currentPageNum >= totalNumberOfPages) return;
		
		currentMessageType 	= currentMessageType;
		currentPageNum		= currentPageNum + 5;
		load(true);

	});

	pagesAround.addEventListener('click',(event)=>{
		if(event.target.classList.contains('pageAround')) {
			currentPageNum = parseInt(event.target.innerText);
            load(true);
		} 
	});
	
	
	// SEND MESSAGE (TEXT AREA)
	messageToSend.addEventListener('input',()=>{
	
		let currentScroll = window.scrollY;
	
		messageToSend.style.height = 'auto';
		messageToSend.style.height = messageToSend.scrollHeight + 'px';
		
		let currentContentLength = parseInt( messageToSend.value.length );
		messageToSendCurrentLength.innerText = currentContentLength;
		window.scrollTo(0, currentScroll);

	});
	
	window.addEventListener('resize', function() {
	
		const inputEvent = new Event('input', { bubbles: true, cancelable: true });
		messageToSend.dispatchEvent(inputEvent);
    
	});
	
	window.addEventListener('pageshow', function() {
		
		messageToSend.value = '';
		messageToSend.style.height = 'auto';
		messageToSend.style.height = 
			messageToSend.scrollHeight + 'px';
	
	});
	
});

function setPageNavDialog() {
	
	dialog_pageNavigation(currentPageNum,totalNumberOfPages, navigate);
	
}
function navigate() {
	
	let pageToGo = parseInt( document.getElementById('dialog').querySelector('.pageToGo').value );
	
	if(!pageToGo) return;
	
	currentPageNum = pageToGo;
	if(currentPageNum > totalNumberOfPages) {
		currentPageNum = totalNumberOfPages;
	} else if(currentPageNum < 1) {
		currentPageNum = 1;
	}
	load(true);
	
}


function load(clearBeforeAppend) {
	
	let formData = new FormData();
	formData.append('messageType', currentMessageType);
	formData.append('pageNumber', currentPageNum);
	
	let urlParams = new URLSearchParams(window.location.search);
	urlParams.set('messageType', currentMessageType);
	urlParams.set('pageNumber', currentPageNum);
	
	let url = `/messages/load?${urlParams.toString()}`;

	let xhr = new XMLHttpRequest();
	xhr.open('GET', url, true);
	xhr.setRequestHeader(csrfHeader, csrfToken);
	xhr.onreadystatechange = function(){
		
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				refresh(data.messageSearchResult, clearBeforeAppend);
			}
			else if(xhr.status === 403)
			{
				let methodToExecute = function() {};
				showEmptyMessage("Failed to load messages");
				dialog_confirm('Invalid or expired CSRF token. Please refresh the page and try again.', methodToExecute)
				showDialog();
			}
			else
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {console.log(data.message);}
				showEmptyMessage("Failed to load messages");
				dialog_confirm(data.message, methodToExecute);
				showDialog();
			}
			if(clearBeforeAppend) {
				hideWaitingModalBackground(false);
			} else {
				hideMoreBtnProgressbar();
			}
		}
	}
	xhr.send(formData);
	if(clearBeforeAppend) {
		showWaitingModalBackground(false);
	} else {
		showMoreBtnProgressbar();
	}
	
	
	
}


function markMessages(markAsRead, messagesToUpdate) {
	
	let formData = new FormData();
	formData.append('markAsRead', markAsRead);
	for(let i=0;i<messagesToUpdate.length;i++) {
		formData.append('messageSeqs', messagesToUpdate[i]);
	}
	
	let xhr = new XMLHttpRequest();
	xhr.open('PATCH','/messages/message', true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function() {
		
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				document.querySelectorAll('.message').forEach(messageElement => {
					let seq = parseInt(messageElement.querySelector('.messageSeq').innerText);
				    if (messagesToUpdate.includes(seq)) {
				        messageElement
				        	.querySelector('.statusHolder')
				        	.setAttribute('status',markAsRead ? 'read':'unread');
				    }
				});
			}
			else if(xhr.status === 403)
			{
				let methodToExecute = function() {};
				dialog_confirm('Invalid or expired CSRF token. Please refresh the page and try again.', methodToExecute)
				showDialog();
			}
			else
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {console.log(data.message);}
				dialog_confirm(data.message, methodToExecute);
				showDialog();
			}
		}
		
	}
	xhr.send(formData);
	
	
}

function notifyMessageRead() {
	
}



function refresh(messageSearchResult, clearBeforeAppend) {
	
	currentMessageType		= parseInt(messageSearchResult.messageType); 
	currentPageNum			= messageSearchResult.currentPageNum; 
	currentPageSize			= messageSearchResult.currentPageSize; 
	totalNumberOfPages		= messageSearchResult.totalNumberOfPages; 
	
	let messagesToLoad 		= messageSearchResult.messages;
	let messagesHolder		= document.createDocumentFragment();
	let pagesAroundToLoad 	= messageSearchResult.pagesAround;
	
	
	//
	if(messagesToLoad.length === 0) {
		showEmptyMessage(null);
	} else {
		hideEmptyMessage();
	}
	
	
	
	
	// PAGE NAVIATION
	currentPage.innerText 	= currentPageNum;
	lastPage.innerText		= totalNumberOfPages;
	
	// MORE BTN
	moreBtn.style.display = currentPageNum>=totalNumberOfPages ? 'none':'flex';

	// PAGES AROUND
	let pagesAroundStr = "";
	for(let i=0;i<pagesAroundToLoad.length;i++) {
		pagesAroundStr += 
			`<div class="${pagesAroundToLoad[i]===currentPageNum ? 'pageCurrent' : 'pageAround'}">${pagesAroundToLoad[i]}</div>`;
	};
	pagesAround.innerHTML = pagesAroundStr;
	
	pagesAroundLeftArrow.style.opacity 	= currentPageNum<=1 ? '0.2':'1';
	pagesAroundRightArrow.style.opacity	= currentPageNum>=totalNumberOfPages ? '0.2':'1';
	

	// MESSAGES
	if(clearBeforeAppend) {	messages.innerHTML = ''; }
	if(clearBeforeAppend && selectAllBtn.checked){ selectAllBtn.click(); }

	
	// DO NOT RENDER A DUPLICATED MESSAGE
	let messageSeqs 		= document.getElementsByClassName('messageSeq');
	let smallestMessageSeq	= messageSeqs.length === 0 ? null : parseInt(messageSeqs[messageSeqs.length-1].innerText);		

	
	for(let i=0;i<messagesToLoad.length;i++) {
		
		let message = messagesToLoad[i];
		
			
		let messageElement = document.createElement('div');
		messageElement.className = 'message';
		messageElement.setAttribute(
			'type', 
			parseInt(messageSearchResult.messageType) === 0? 'inbox':'sent'
		);
		
		
		
		// statusHolder
		let statusHolderElement = document.createElement('div');
		statusHolderElement.className = 'statusHolder';
		if(currentMessageType === 0)
		{
			statusHolderElement.setAttribute('status', message.readAt === null ? 'unread':'read');
		}
		else
		{
			statusHolderElement.setAttribute('status', message.openedAt === null ? 'unread':'read');
		}
	

		let selectMessageElement 	= document.createElement('label');
		selectMessageElement.className = 'selectMessage';
		
		let messageCheckBoxElement	= document.createElement('input');
		messageCheckBoxElement.className = 'messageCheckbox';
		messageCheckBoxElement.setAttribute('type', 'checkbox');
		messageCheckBoxElement.setAttribute('messageSeq', message.messageSeq);
		
		let messageUncheckedIconElement			= document.createElement('img');
		messageUncheckedIconElement.className 	= 'messageUncheckedIcon';
		messageUncheckedIconElement.src 		= '/img/original/select_all_unchecked.png';
		
		let messageCheckedIconElement			= document.createElement('img');
		messageCheckedIconElement.className 	= 'messageCheckedIcon';
		messageCheckedIconElement.src 			= '/img/original/select_all_checked.png';
		
		selectMessageElement.appendChild(messageCheckBoxElement);
		selectMessageElement.appendChild(messageUncheckedIconElement);
		selectMessageElement.appendChild(messageCheckedIconElement);
		
		
		let messageInfoElement			=	document.createElement('div');
		messageInfoElement.className	=	'messageInfo';
		
		let messageSeqElement			=	document.createElement('div');
		messageSeqElement.className		=	'messageSeq';
		messageSeqElement.innerText		=	message.messageSeq;
		
		let messagePreviewElement		=	document.createElement('div');
		messagePreviewElement.className	=	'messagePreview';
		messagePreviewElement.innerText	=	message.messagePreview;
		
		let messageContentElement		=	document.createElement('pre');
		messageContentElement.className	=	'messageContent';
		messageContentElement.innerText =	message.messageContent;
		
		
		let messageParticipantElement		=	document.createElement('div');
		messageParticipantElement.className	=	'participant';
				
		let directionElement 		= document.createElement('span');
		directionElement.className	= 'direction';
		directionElement.innerText	= parseInt(messageSearchResult.messageType) === 0 ? 'from' : 'to';	
				
		let participantNameElement 			= document.createElement('span');
		participantNameElement.className	= 'participantName';
		participantNameElement.innerText	= parseInt(messageSearchResult.messageType) === 0 ? message.senderName : message.recipientName;	
				
		messageParticipantElement.appendChild(directionElement);
		messageParticipantElement.appendChild(participantNameElement);
				
		let messageCreatedElement		= document.createElement('div');
		messageCreatedElement.className	= 'created';
		messageCreatedElement.innerText	= utcToLocal(message.messageCreationTime);		
	
						
		messageInfoElement.appendChild(messageSeqElement);
		messageInfoElement.appendChild(messagePreviewElement);
		messageInfoElement.appendChild(messageContentElement);
		messageInfoElement.appendChild(messageParticipantElement);
		messageInfoElement.appendChild(messageCreatedElement);


		let messageStatusElement = document.createElement('div');
		messageStatusElement.className = 'messageStatus';
		
		let messageReadImageElement = document.createElement('img');
		messageReadImageElement.className = 'messageReadImage';
		messageReadImageElement.src = '/img/original/read.png';
		
		let messageUnreadImageElement = document.createElement('img');
		messageUnreadImageElement.className = 'messageUnreadImage';
		messageUnreadImageElement.src = '/img/original/unread.png';

		messageStatusElement.appendChild(messageReadImageElement);
		messageStatusElement.appendChild(messageUnreadImageElement);


		messageElement.appendChild(statusHolderElement);
		messageElement.appendChild(selectMessageElement);
		messageElement.appendChild(messageInfoElement);
		messageElement.appendChild(messageStatusElement);
		
		let messageSeq = parseInt(message.messageSeq);

		if(smallestMessageSeq === null) 
		{
			messagesHolder.appendChild(messageElement);
		} 
		else if(smallestMessageSeq > messageSeq) 
		{
			messagesHolder.appendChild(messageElement);
		}

	}
	
	
	messages.appendChild(messagesHolder);
	if(clearBeforeAppend) {	window.scrollTo({ top: 0, behavior: 'smooth' }); }
	
	
	let urlParams = new URLSearchParams(window.location.search);
	urlParams.set('messageType', currentMessageType);
	urlParams.set('pageNumber', currentPageNum);


	let historyURL = `/messages?${urlParams.toString()}`;
	history.replaceState(null, '', historyURL);
	
	
	
	// clearBeforeAppend true 	=> empty and load
	// clearBeforeAppend false 	=> more
	
}


// SEND MESSAGE
function sendMessage(recipientName, messageContent)  {
	
	let formData = new FormData();
	formData.append('recipientName', recipientName);
	formData.append('message', messageContent);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST','/messages/message', true);
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
					if(sentMsgRadio.checked) {
						
						currentMessageType = currentMessageType;
						currentPageNum = 1;
						load(true);
						
					} else {
						
						dialog_confirm(message, methodToExecute);
						showDialog();
						
					}

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
				let data = JSON.parse(xhr.responseText);
				guideSendMessage(data.message);
				hideWaitingModalBackground(false);
			}
			
		}
		
	}
	xhr.send(formData);
	showWaitingModalBackground(false);
	
	
}

function deleteMessages(messageSeqs) {
	
	let formData = new FormData();
	formData.append('messageSeqs', messageSeqs);
	
	let xhr = new XMLHttpRequest();
	xhr.open('DELETE', '/messages/message', true)
	xhr.setRequestHeader(csrfHeader, csrfToken);
	xhr.onreadystatechange = function(){
		
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				
				if(selectAllBtn.checked){selectAllBtn.click();}
				
				document.querySelectorAll('.message').forEach(messageElement => {
				let seq = parseInt(messageElement.querySelector('.messageSeq').innerText);
				    if (messageSeqs.includes(seq)) {
				        messageElement.remove();
				    }
				});
				
				
				load(false);
			}
			else if(xhr.status === 403)
			{
				let methodToExecute = function() {};
				dialog_confirm('Invalid or expired CSRF token. Please refresh the page and try again.', methodToExecute)
				showDialog();
			}
			else 
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {console.log(data.message);}
				dialog_confirm(data.message, methodToExecute);
				showDialog();
			}
		}
		
	}
	xhr.send(formData);
	
}

// SEND MESSAGE DAILOG
function sendMessageOn(username) {
	
	sendMessageDrawerOnOff.checked = true;
	let changeEvent = new Event('change');
	sendMessageDrawerOnOff.dispatchEvent(changeEvent);
	
	if(username != null) {
		recipient.value = username;
		recipient.disabled = true;
	}
	
}

function sendMessageOff() {
	
	sendMessageDrawerOnOff.checked = false;
	let changeEvent = new Event('change');
	sendMessageDrawerOnOff.dispatchEvent(changeEvent);
	
}

function guideSendMessage(text) {
	
	sendMessageGuide.innerText =  text;
	
	setTimeout(()=>{
		sendMessageGuide.innerText = '';
	},3000);
	
}


// READ MESSAGE DIALOG
function readMessageOn(messageSeq, messageType, messageDirection, messageParticipant, messageCreated, messageContent) {
	
	readMessageDrawerOnOff.checked = true;
	let changeEvent = new Event('change');
	readMessageDrawerOnOff.dispatchEvent(changeEvent);
	
	readMessageSeq.innerText = messageSeq;
	readMessageType.innerText = messageType;
	readMessageDirection.innerText = messageDirection.toUpperCase();
	readMessageParticipant.innerText = messageParticipant;
	readMessageCreated.innerText = messageCreated;
	readMessageContent.innerText = messageContent;
	
	if(messageType === 'inbox') {
		markMessages(true, [parseInt(messageSeq)]);
	}

}

function readMessageOnAgain() {
	
	readMessageDrawerOnOff.checked = true;
	let changeEvent = new Event('change');
	readMessageDrawerOnOff.dispatchEvent(changeEvent);
	
}

function readMessageOff() {
	
	readMessageDrawerOnOff.checked = false;
	let changeEvent = new Event('change');
	readMessageDrawerOnOff.dispatchEvent(changeEvent);
	
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


// MORE BTN PROGRESSBAR FUNCTIONS
function showMoreBtnProgressbar(){
	moreBtnProgressCircle.style.display = "block";
	moreBtnArrow.style.display = "none";
}
function hideMoreBtnProgressbar(){
	moreBtnProgressCircle.style.display = "none";
	moreBtnArrow.style.display = "block";	
}


// REFRESH TIME ELEMENTS
function refreshTimeElements() {
	
	let messageCreatedTimes = document.querySelectorAll('.created');
	let messageReadAtTimes 	= document.querySelectorAll('.readAt');
	let messageOpenedAtTimes 	= document.querySelectorAll('.openedAt');
	
	for(let i=0;i<messageCreatedTimes.length;i++) {
		messageCreatedTimes[i].innerText = utcToLocal(messageCreatedTimes[i].innerText);
	}
	
	for(let i=0;i<messageReadAtTimes.length;i++) {
		messageReadAtTimes[i].innerText = utcToLocal(messageReadAtTimes[i].innerText);
	}
	
	for(let i=0;i<messageReadAtTimes.length;i++) {
		messageOpenedAtTimes[i].innerText = utcToLocal(messageOpenedAtTimes[i].innerText);
	}
}

// EMPTY MESSAGE0
function showEmptyMessage(text) {
	messages.innerHTML = '';
	emptyMessages.style.display = "flex";
	emptyMessages.innerText = text===null ? "Nothing has been found":text;
	boardBottomNavBar.style.display = "none";
}
function hideEmptyMessage() {
	emptyMessages.style.display = "none";
	boardBottomNavBar.style.display = "flex";
}


function utcToLocal(utc) {
	
	if(!utc) {return utc};
	
	let date = new Date(utc);
	
	let year 	= date.getFullYear();
    let month 	= String(date.getMonth() + 1).padStart(2, '0');
    let day 	= String(date.getDate()).padStart(2, '0');
    let hours 	= String(date.getHours()).padStart(2, '0');
    let minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;

}

