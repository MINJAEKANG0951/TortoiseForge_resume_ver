
// csrf token
var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


window.addEventListener('pageshow', function(event) {
	/*
    if (event.persisted) {
		document.getElementById('showMenu').checked = false;
    } else {
		document.getElementById('showMenu').checked = false;
    }
    
	let changeEvent = new Event('change');
	document.getElementById('showMenu').dispatchEvent(changeEvent);
	*/
});

document.getElementById('showMenu').addEventListener('change',function(){
	
		/* 
			hide/show scroll & layout protection ★★★ very important
		*/
		
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
		
		
		// pc chrome 에서만 가끔 scrollBarWidth 가 15px이 아닌 16px로 계산돼어 아주조금 layout 이 밀리는 현상 발생.
});


document.getElementById('home').addEventListener('click',function(){
	clickSidebarMenu("/");
});

document.getElementById('creations').addEventListener('click',function(){
	clickSidebarMenu("/creations");
});

document.getElementById('discussionBoard').addEventListener('click',function(){
	clickSidebarMenu("/discussionBoard");
});

document.getElementById('login').addEventListener('click',function(){
	clickSidebarMenu("/login");
});

document.getElementById('logout').addEventListener('click',function(){
	clickSidebarMenu("/logout");
});

document.getElementById('accountSetting').addEventListener('click',function(){
	let userSeq = document.getElementById('userSeq').innerText;
	if(userSeq) {
		clickSidebarMenu("/accountSetting");
	}
});
document.getElementById('messages').addEventListener('click',function(){
	let userSeq = document.getElementById('userSeq').innerText;
	if(userSeq) {
		clickSidebarMenu("/messages");
	}
});

checkNewMessages();


function clickSidebarMenu(whereToGo){
	
	document.getElementById('showMenu').checked = false;
	let changeEvent = new Event('change');
	document.getElementById('showMenu').dispatchEvent(changeEvent);
	
	setTimeout(()=>{
		document.location.href = whereToGo;
	},310);

}

function checkNewMessages() {
	
	let userSeq = document.getElementById('userSeq').innerText;
	
	if(!userSeq){return;}
	
	let xhr = new XMLHttpRequest();
	xhr.open('GET','/messages/check', true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				let isThereNewMessage = data.isThereNewMessage;
				
				if(isThereNewMessage) {
					document.getElementById('messages').querySelector('.redDot').style.display = "block";
				}
			}
		}
	}
	xhr.send();
	
	
}



