var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');



// global variables
var postSeq						= null;

var textContentTypeSeq 			= null;
var boldTextContentTypeSeq 		= null;
var imageContentTypeSeq 		= null;

var maxTextContentNumber		= 0;
var maxBoldTextContentNumber	= 0;
var maxImageContentNumber		= 0;

var maxPostTitleLength 			= 0;
var maxTextContentLength 		= 0;
var maxImgFileSize				= 0;

var howmanyImagesToConvert		= 0;

var commaReplacement 			= null;


var isImageConversionSuccessful	= true;
var imageFailedToLoad			= null;


/* 

	drag and drop image upload 기능을 위한 추가 global 변수
	
	drag and drop 으로 input[type=file] 에 image 를 업로드 할 수 없기에,
	drag and drop 으로 업로드 할 이미지 파일을 담아놓는 변수
	[
		{
			key:(postContentRef),
			value:(imageFile)
		},
		
		...
	]
	
	이런식으로 담아놓았다가 나중에 'post'버튼 클릭 시 실제파일 찾아감
	
*/
var postImgContentStorage	= [];



document.addEventListener('DOMContentLoaded',()=>{
	
	
	// INITIALIZE VARIABLES
	postSeq						= document.getElementById('postSeq') === null 
									? null : document.getElementById('postSeq').value;	
									
	textContentTypeSeq 			= parseInt( document.getElementById('textContentTypeSeq').value );
	boldTextContentTypeSeq 		= parseInt( document.getElementById('boldTextContentTypeSeq').value );
	imageContentTypeSeq 		= parseInt( document.getElementById('imageContentTypeSeq').value );		
				
	maxTextContentNumber 		= parseInt( document.getElementById('maxTextContentNumber').value );
	maxBoldTextContentNumber 	= parseInt( document.getElementById('maxBoldTextContentNumber').value );
	maxImageContentNumber		= parseInt( document.getElementById('maxImageContentNumber').value );
	
	maxPostTitleLength 		= parseInt( document.getElementById('maxPostTitleLength').value );
	maxTextContentLength 	= parseInt( document.getElementById('maxTextContentLength').value );
	maxImgFileSize			= parseInt( document.getElementById('maxImgFileSize').value );
	
	howmanyImagesToConvert	= parseInt( document.getElementById('howmanyImagesToConvert').value );

	commaReplacement = document.getElementById('commaReplacement').value;
	
	// TITLE
	document.getElementById('postTitle').addEventListener('input',()=>{ 
		
		let currentContentLength 
			= parseInt( document.getElementById('postTitle').value.length );
		
		document.getElementById('postTitleCurrentLength').innerText = currentContentLength;
	});
	
	
	// UTC TO LOCAL TIME
	refreshTimeElements();
	
	
	
	// ADD LISTENERS TO DEFAULT CONTENT ITEMS -> replacement for the logic below
	setDefaultContents();
	

	// HEADER_POST
	document.getElementById('postPreview').addEventListener('click',()=>{
		showPreview();
	});
	document.getElementById('addTextBox').addEventListener('click',()=>{
		addTextBox();
	});
	document.getElementById('addBoldTextBox').addEventListener('click',()=>{
		addTextBox_Bold();
	});
	document.getElementById('addImage').addEventListener('click',()=>{
		addImage();
	});



	// POST BUTTON
	document.getElementById('postBtn').onclick = ()=>{
		if(postSeq === null) {
			createPost();
		} else {
			updatePost();
		}
	};
	
	
	// REFRESH NUMBER OF EACH CONTENT
	countEachContentType();


	// PREVIEW
	document.getElementById('previewOnOff').addEventListener('change',()=>{
		
		let checked = document.getElementById('previewOnOff').checked;
		if(checked)
		{
			let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
			
			document.body.style.paddingRight = `${scrollBarWidth}px`;
			if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
				document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
			}
			document.documentElement.style.overflow = 'hidden';
		}
		else 
		{
			setTimeout(()=>{
				document.documentElement.style.overflow 						= 'auto';
				document.body.style.paddingRight 								= '';
				document.getElementById('headerContainer').style.paddingRight 	= '';
			},300);
		}
		
	});
})


window.addEventListener('pageshow', function() {

	if(postSeq != null) {
		return;
	}
	
	document.getElementById('postTitle').value = '';
	
	for(let i=0;i<document.getElementsByName('textContent').length;i++) {	// default textContentItem
		
		document.getElementsByName('textContent')[i].value = '';
		document.getElementsByName('textContent')[i].style.height = 'auto';
		document.getElementsByName('textContent')[i].style.height = 
			document.getElementsByName('textContent')[i].scrollHeight + 'px';
			
	};

});

window.addEventListener('resize', function() {
	
	for(let i=0;i<document.getElementsByName('textContent').length;i++) {
		
		document.getElementsByName('textContent')[i].style.height = 'auto';
		document.getElementsByName('textContent')[i].style.height = 
			document.getElementsByName('textContent')[i].scrollHeight + 'px';
			
	};
	
	for(let i=0;i<document.getElementsByName('boldTextContent').length;i++) {
		
		document.getElementsByName('boldTextContent')[i].style.height = 'auto';
		document.getElementsByName('boldTextContent')[i].style.height = 
			document.getElementsByName('boldTextContent')[i].scrollHeight + 'px';
			
	};
    
});

function refreshTimeElements() {
	
	let postCreationTimeWrapper = document.getElementById('postCreatedTime_preview');
	
	if(postCreationTimeWrapper != null) {
		postCreationTimeWrapper.innerText = utcToLocal(postCreationTimeWrapper.innerText);
	}
	
}

function utcToLocal(utc) {
	
	let date = new Date(utc);
	
	let year 	= date.getFullYear();
    let month 	= String(date.getMonth() + 1).padStart(2, '0');
    let day 	= String(date.getDate()).padStart(2, '0');
    let hours 	= String(date.getHours()).padStart(2, '0');
    let minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;

}




function setDefaultContents() {	// 각 DEFAULT POST CONTENT 에 기능 부여 

	let inputEvent = new Event('input');
	document.getElementById('postTitle').dispatchEvent(inputEvent);
	
	if(howmanyImagesToConvert > 0) {
		showWaitingModalBackground();
	};
	
	for(let i=0;i<document.getElementsByClassName('postContent').length;i++) {
		
		let postContent 	= document.getElementsByClassName('postContent')[i];
		let postContentType = parseInt(postContent.getAttribute('content-type'));
		
		let postContentWrapper
			= postContent
				.parentElement;
		
		let postContentLength
			= postContent
				.parentElement
				.querySelector('.postContentLength');
		
		let postContentCurrentLength
			= postContent
				.parentElement
				.querySelector('.postContentLength')
				.querySelector('.currentLength');
				
		let postContentDeleteBtn
			= postContent
				.parentElement
				.querySelector('.postContentDelete');
		
		
		if(postContentType === textContentTypeSeq || postContentType === boldTextContentTypeSeq)
		{

			postContent.addEventListener('input',()=>{
				const currentScroll = window.scrollY;
		
				postContent.style.height = 'auto'; 
				postContent.style.height = postContent.scrollHeight + 'px';
				
				let currentContentLength = parseInt( postContent.value.length );
				
				if(currentContentLength > maxTextContentLength) {
					postContentLength.style.color = 'red';
				} else {
					postContentLength.style.color = '#ddd';
				}
				
				postContentCurrentLength.innerText 
					= currentContentLength;
				
				window.scrollTo(0, currentScroll);
			});
			
			postContent.addEventListener('focus',()=>{
				document.getElementById('headerContainer').style.opacity = '0.7';
			});
			postContent.addEventListener('blur',()=>{
				document.getElementById('headerContainer').style.opacity = '1';
			});
			postContentDeleteBtn.addEventListener('click',()=>{
				let methodToExecute = ()=>{
					postContentWrapper.remove();
					countEachContentType();
				}
				dialog_yesOrNo('Are you sure to delete the selected post content?',methodToExecute);
				showDialog();
			});
				
			let inputEvent = new Event('input');
			postContent.dispatchEvent(inputEvent);
			
		}
		else if(postContentType === imageContentTypeSeq)
		{
			let imgUploadInput = postContent.querySelector('.imgUpload');
			
			let imgUploadClick = ()=> {
				imgUploadInput.click();
			};
			
			let handleImgUploadBtnClick = (event)=> {
				
				let isImgUploaded = true;
		
				for(let i=0;i<postImgContentStorage.length;i++) {
					if( postImgContentStorage[i].key === postContent ) {
						if(postImgContentStorage[i].value === null) {
							isImgUploaded = false;
						}
					}
				};
				
				if(isImgUploaded) {
			
					event.preventDefault(); 
					
					let methodToExecute = ()=>{
						
						imgUploadInput.value 					= '';
						postContentLength.style.color 			= '#ddd';
						postContentCurrentLength.innerText 		= '0';
						let formerLoadedImg = postContent.querySelector('.loadedImg');
						if(formerLoadedImg) {
							formerLoadedImg.remove();
						}
						
						
						for(let i=0;i<postImgContentStorage.length;i++) {
							if( postImgContentStorage[i].key === postContent ) {
								postImgContentStorage[i].value = null;
								break;
							}
						};
						
					}
					dialog_yesOrNo('Are you sure to delete the selected image?',methodToExecute);
					showDialog();
					
				}
				
			};
			
			let handleLoadedImage = (event)=> {
				
				let file = event.target.files[0];
				
				imgUploadInput.value = '';
				
				if(!file) {	
					return;
				}
				
				if (!file.type.startsWith('image/')) {
					return;
				}
				
				let reader 		= new FileReader();
				reader.onload 	= function(e) {
					
					let formerLoadedImg = postContent.querySelector('.loadedImg');
					if(formerLoadedImg) {	formerLoadedImg.remove();	}
					
					
					let imgLoaded 		= document.createElement('img');
					
					imgLoaded.className	= 'loadedImg';
					imgLoaded.src 		= e.target.result;
					
					postContent.appendChild(imgLoaded);
					
				};
				reader.readAsDataURL(file);
				if(file.size > maxImgFileSize) {
					postContentLength.style.color = 'red';
				} else {
					postContentLength.style.color = '#ddd';
				}
				postContentCurrentLength.innerText = (file.size / 1024 / 1024).toFixed(2); 
				
				for(let i=0;i<postImgContentStorage.length;i++) {
					if( postImgContentStorage[i].key === postContent ) {
						postImgContentStorage[i].value = file;
						return;
					}
				};
				
			};
			
			let dropUploadImage = (event)=> {
				
				event.preventDefault();
			  
			  
			  	let files = event.dataTransfer.files;
			  	if(files.length <= 0) {return;}
			  	
			  	let file = files[0];
				if(!file) {	return; }
				
				if (!file.type.startsWith('image/')) {	return;	}
		
		
				let reader 		= new FileReader();
				reader.onload 	= function(e) {
					
					let formerLoadedImg = postContent.querySelector('.loadedImg');
					if(formerLoadedImg) {	formerLoadedImg.remove();	}
					
					
					let imgLoaded 		= document.createElement('img');
					
					imgLoaded.className	= 'loadedImg';
					imgLoaded.src 		= e.target.result;
					
					postContent.appendChild(imgLoaded);
					
				};
				reader.readAsDataURL(file);
				if(file.size > maxImgFileSize) {
					postContentLength.style.color = 'red';
				} else {
					postContentLength.style.color = '#ddd';
				}
				postContentCurrentLength.innerText = (file.size / 1024 / 1024).toFixed(2); 
		
				
				for(let i=0;i<postImgContentStorage.length;i++) {
					if( postImgContentStorage[i].key === postContent ) {
						postImgContentStorage[i].value = file;
						return;
					}
				};
	  			
				
			};
			
			let deleteListener = ()=> {
				
				let methodToExecute = ()=>{
			
					for(let i=0;i<postImgContentStorage.length;i++) {
						if( postImgContentStorage[i].key === postContent ) {
							postImgContentStorage.splice(i,1);
							break;
						}
					};
			
				postContentWrapper.remove();
				countEachContentType();
			
				};
				dialog_yesOrNo('Are you sure to delete the selected post content?',methodToExecute);
				showDialog();
			};
			
			
			postContent.addEventListener('click',imgUploadClick);
	
			imgUploadInput.addEventListener('click',handleImgUploadBtnClick);
			imgUploadInput.addEventListener('change',handleLoadedImage);
			
			// Necessary to allow the drop
			postContent.addEventListener('dragover', (event) => {event.preventDefault();});
			postContent.addEventListener('drop',dropUploadImage);
			
			postContentDeleteBtn.addEventListener('click',deleteListener);
			
			
			// store image file to array
			let loadedImgSrc = postContent.querySelector('.loadedImg').src;
			imgConvertAndSave(postContent, postContentCurrentLength, loadedImgSrc);
			
		}
		
		

	};
};


function imgConvertAndSave(postContent, postContentCurrentLength, imgSrc) {
	
	let xhr = new XMLHttpRequest();
	xhr.open('GET', imgSrc, true);
	xhr.responseType = 'blob';
	
	xhr.onload = function() {
		
		if(xhr.status === 200) {
			
			let blob = xhr.response;
			
			let mimeType = blob.type;
			let extension = mimeType.split("/")[1];
			let fileName = 'image' + '.' + extension;
			let file = new File([blob], fileName, { type: blob.type });
			
			postImgContentStorage.push({
				key:postContent,
				value:file
			});
			
			postContentCurrentLength.innerText = (file.size / 1024 / 1024).toFixed(2);
			if(file.size > maxImgFileSize) {
				postContentCurrentLength.parentElement.style.color = 'red';
			} else {
				postContentCurrentLength.parentElement.style.color = '#ddd';
			}
			
			
		} else {
			isImageConversionSuccessful = false;
			postImgContentStorage.push({
				key:postContent,
				value:null
			});
			postContent.querySelector('.loadedImg').remove();
			imageFailedToLoad = postContent;
		}
		
		howmanyImagesToConvert--;
		if(howmanyImagesToConvert <= 0) 
		{
			hideWaitingModalBackground();
			if(!isImageConversionSuccessful) {	// 마지막 && 이미지 로드 실패 내역이 존재할 떄
				let text = 'Failed to load some images.'
				let methodToExecute = ()=>{
					imageFailedToLoad.scrollIntoView({ behavior: "smooth", block: "center" });
				};
				
				dialog_confirm(text, methodToExecute);
				showDialog();
			}
		}
		
		
	};

	xhr.onerror = function() {

		isImageConversionSuccessful = false;
		postImgContentStorage.push({
			key:postContent,
			value:null
		});
		postContent.querySelector('.loadedImg').remove();
		imageFailedToLoad = postContent;
		
		howmanyImagesToConvert--;
		if(howmanyImagesToConvert <= 0) 
		{
			hideWaitingModalBackground();
			if(!isImageConversionSuccessful) {
				let text = 'Failed to load some images.'
				let methodToExecute = ()=>{
					imageFailedToLoad.scrollIntoView({ behavior: "smooth", block: "center" });
				};
				
				dialog_confirm(text, methodToExecute);
				showDialog();
			}
		}

	}
	
	xhr.send();
}


function createPost() {
	
	
	let form = new FormData();
	form.append('postType', parseInt( document.getElementById('postTypeSelect').value) );
	form.append('postTitle',document.getElementById('postTitle').value);
	for(let i=0;i<document.getElementsByClassName('postContent').length;i++){
		
		let postContent 	= document.getElementsByClassName('postContent')[i];
		let postContentType	= parseInt( postContent.getAttribute('content-type') );
		
		
		
		if(postContentType === textContentTypeSeq)
		{
			form.append('postContentTypes', textContentTypeSeq);
			let contentValue = postContent.value.replaceAll(",",commaReplacement);
			form.append('textContents', ":" + contentValue);
		}
		else if(postContentType === boldTextContentTypeSeq)
		{
			form.append('postContentTypes', boldTextContentTypeSeq);
			let contentValue = postContent.value.replaceAll(",",commaReplacement);
			form.append('boldTextContents', ":" + contentValue);
		}
		else if(postContentType === imageContentTypeSeq)
		{
			for(let i=0;i<postImgContentStorage.length;i++) {
				if( postImgContentStorage[i].key === postContent ) {
					
					form.append('postContentTypes', imageContentTypeSeq);
					
					if(postImgContentStorage[i].value == null) {	
						let emptyBlob = new Blob([], { type: 'image/png' });
						form.append('imageContents', emptyBlob);		
					} else {
						form.append('imageContents', postImgContentStorage[i].value);
					}

					break;
				}
			};
		}
		
	}

	let xhr = new XMLHttpRequest();
	xhr.open('POST','/discussionBoard/post',true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				document.location = '/discussionBoard';
			}
			else if(xhr.status === 207)
			{
				let data = JSON.parse( xhr.responseText );
				let methodToExecute = function(){
					document.location = '/discussionBoard';
				};
				dialog_confirm(data.message, methodToExecute);
				showDialog();
				
				document.getElementById('postBtn').setAttribute('name','disabled');
				document.getElementById('postBtn').onclick = ()=> {
					let methodToExecute = function(){
						document.location = '/discussionBoard';
					};
					dialog_confirm(data.message, methodToExecute);
					showDialog();
				};
			}
			else if(xhr.status === 403)	// forbidden
			{
				let message = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				let methodToExecute = function(){
					console.log('failed to post');
				};
				dialog_confirm(message, methodToExecute);
				showDialog();
			}
			else 
			{
				// guide dialog 로 안내하기(message 내용을 dialog 에 담아서 보여주기 ㄱ) -> 안내 dialog 모듈작업 ㄱ
				let data = JSON.parse( xhr.responseText );
				let methodToExecute = function(){
					console.log('failed to post');
				};
				dialog_confirm(data['message'], methodToExecute);
				showDialog();
			}
			document.getElementById('modalBackground').style.display = 'none';
		}
	}
	xhr.send(form);
	document.getElementById('modalBackground').style.display = 'flex';
	
}

function updatePost() {
	
	let form = new FormData();
	form.append('postSeq', postSeq);
	form.append('postType', parseInt( document.getElementById('postTypeSelect').value) );
	form.append('postTitle',document.getElementById('postTitle').value);
	for(let i=0;i<document.getElementsByClassName('postContent').length;i++){
		
		let postContent 	= document.getElementsByClassName('postContent')[i];
		let postContentType	= parseInt( postContent.getAttribute('content-type') );
		
		
		
		if(postContentType === textContentTypeSeq)
		{
			form.append('postContentTypes', textContentTypeSeq);
			let contentValue = postContent.value.replaceAll(",",commaReplacement);
			form.append('textContents', ":" + contentValue);
		}
		else if(postContentType === boldTextContentTypeSeq)
		{
			form.append('postContentTypes', boldTextContentTypeSeq);
			let contentValue = postContent.value.replaceAll(",",commaReplacement);
			form.append('boldTextContents', ":" + contentValue);
		}
		else if(postContentType === imageContentTypeSeq)
		{
			for(let i=0;i<postImgContentStorage.length;i++) {
				if( postImgContentStorage[i].key === postContent ) {
					
					form.append('postContentTypes', imageContentTypeSeq);
					
					if(postImgContentStorage[i].value == null) {	
						let emptyBlob = new Blob([], { type: 'image/png' });
						form.append('imageContents', emptyBlob);		
					} else {
						form.append('imageContents', postImgContentStorage[i].value);
					}

					break;
				}
			};
		}
		
	}

	let xhr = new XMLHttpRequest();
	xhr.open('PUT','/discussionBoard/post',true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				document.location = '/discussionBoard';
			}
			else if(xhr.status === 207)
			{
				let data = JSON.parse( xhr.responseText );
				let methodToExecute = function(){
					document.location = '/discussionBoard';
				};
				dialog_confirm(data.message, methodToExecute);
				showDialog();
				
				document.getElementById('postBtn').setAttribute('name','disabled');
				document.getElementById('postBtn').onclick = ()=> {
					let methodToExecute = function(){
						document.location = '/discussionBoard';
					};
					dialog_confirm(data.message, methodToExecute);
					showDialog();
				};
			}
			else if(xhr.status === 403)	// forbidden
			{
				message = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				let methodToExecute = function(){
					console.log('failed to post');
				};
				dialog_confirm(message, methodToExecute);
				showDialog();
			}
			else 
			{
				// guide dialog 로 안내하기(message 내용을 dialog 에 담아서 보여주기 ㄱ) -> 안내 dialog 모듈작업 ㄱ
				let data = JSON.parse( xhr.responseText );
				let methodToExecute = function(){
					console.log('failed to post');
				};
				dialog_confirm(data['message'], methodToExecute);
				showDialog();
			}
			document.getElementById('modalBackground').style.display = 'none';
		}
	}
	xhr.send(form);
	document.getElementById('modalBackground').style.display = 'flex';
	
}


// COUNT POST EACH TYPE OF POST CONTENT
function countEachContentType() {
	
	let howmanyContents			= 0;
	let howmanyTextContent 		= 0;
	let howmanyBoldTextContent 	= 0;
	let howmanyImageContent		= 0;
	
	for(let i=0;i<document.getElementsByClassName('postContent').length;i++) {
		
		howmanyContents++;
		let postContent 	= document.getElementsByClassName('postContent')[i];
		let postContentName = postContent.getAttribute('name'); 
		
		if(postContentName === 'textContent') 
		{
			howmanyTextContent++;
		} 
		else if(postContentName === 'boldTextContent') 
		{
			howmanyBoldTextContent++;
		} 
		else if(postContentName === 'imgContent') 
		{
			howmanyImageContent++;
		} 
		
	}
	
	console.log('total content num : ' + howmanyContents);
	
	let max = 99;
	
	howmanyTextContent 		= howmanyTextContent 		> max ? max + '+' : howmanyTextContent;
	howmanyBoldTextContent 	= howmanyBoldTextContent 	> max ? max + '+' : howmanyBoldTextContent;
	howmanyImageContent 	= howmanyImageContent 		> max ? max + '+' : howmanyImageContent;
	
	document.getElementById('currentTextContentNum').innerText		= howmanyTextContent 		+ '/' 	+ maxTextContentNumber;
	document.getElementById('currentBoldTextContentNum').innerText	= howmanyBoldTextContent 	+ '/' 	+ maxBoldTextContentNumber;
	document.getElementById('currentImageContentNum').innerText		= howmanyImageContent 		+ '/' 	+ maxImageContentNumber;
	
	document.getElementById('currentTextContentNum').style.color 		= howmanyTextContent > maxTextContentNumber 		? 'red':'black';
	document.getElementById('currentBoldTextContentNum').style.color 	= howmanyBoldTextContent > maxBoldTextContentNumber ? 'red':'black';
	document.getElementById('currentImageContentNum').style.color 		= howmanyImageContent > maxImageContentNumber 		? 'red':'black';
	
}



// CONTENT COMPONENT ADD FUNCTIONS

function addTextBox() {
	
	// Wrapper
	let postContentWrapperElement 		= document.createElement('div');
	postContentWrapperElement.className = 'postContentWrapper';
	

	// textContent
	let	postContentElement			= document.createElement('textarea');
	postContentElement.className 	= 'postContent';
	postContentElement.setAttribute('content-type', textContentTypeSeq);
	postContentElement.name			= 'textContent';
	postContentElement.placeholder	= 'Text Content';
	postContentElement.spellcheck	= false;
	postContentElement.rows			= 1;
	postContentElement.maxLength	= maxTextContentLength;
	
	postContentWrapperElement.appendChild(postContentElement);
	
	// Title
	let postContentTitleElement			= document.createElement('span');
	postContentTitleElement.className	= 'postContentTitle';
	postContentTitleElement.innerText	= 'TEXT CONTENT';
	
	postContentWrapperElement.appendChild(postContentTitleElement);
	
	
	// ContentLength
	let postContentLengthElement				= document.createElement('div');
	postContentLengthElement.className			= 'postContentLength';
	
	let postContentCurrentLengthElement			= document.createElement('span');
	postContentCurrentLengthElement.className	= 'currentLength';
	postContentCurrentLengthElement.innerText	= '0';
	
	let postContentMaxLengthElement				= document.createElement('span');
	postContentMaxLengthElement.className		= 'maxLength';
	postContentMaxLengthElement.innerText		= maxTextContentLength;
	
	postContentLengthElement.appendChild(postContentCurrentLengthElement);
	postContentLengthElement.appendChild(postContentMaxLengthElement);
	
	postContentWrapperElement.appendChild(postContentLengthElement);
	
	
	// DeleteBtn
	let postContentDeleteElement		= document.createElement('img');
	postContentDeleteElement.className	= 'postContentDelete';
	postContentDeleteElement.src		= '/img/mini/delete_mini.png';
	
	postContentWrapperElement.appendChild(postContentDeleteElement);
	
	
	
	// ADD EVENT LISTENER
	postContentElement.addEventListener('input',()=>{
		
		const currentScroll = window.scrollY;
		
		postContentElement.style.height = 'auto'; 
		postContentElement.style.height = postContentElement.scrollHeight + 'px';
		
		let currentContentLength = parseInt( postContentElement.value.length );
		
		postContentCurrentLengthElement.innerText 
			= currentContentLength;
		
		window.scrollTo(0, currentScroll);
		
	});
	postContentElement.addEventListener('focus',()=>{
		document.getElementById('headerContainer').style.opacity = '0.7';
	});
	postContentElement.addEventListener('blur',()=>{
		document.getElementById('headerContainer').style.opacity = '1';
	});
	postContentDeleteElement.addEventListener('click',()=>{
		let methodToExecute = ()=>{
			postContentWrapperElement.remove();
			countEachContentType();
		}
		dialog_yesOrNo('Are you sure to delete the selected post content?',methodToExecute);
		showDialog();
	});
	
	
	// UNFOLD SIDEBAR
	document.getElementById('showMenu').checked = false;
	let changeEvent = new Event('change');
	document.getElementById('showMenu').dispatchEvent(changeEvent);
	
	
	// ADD FINALLY
	document.getElementById('postContentWrapperContainer').appendChild(postContentWrapperElement);
	postContentElement.focus();
	
	
	// FIT SIZE
	postContentElement.style.height = 'auto';
	postContentElement.style.height = 
		postContentElement.scrollHeight + 'px';
	
	
	countEachContentType();
}

function addTextBox_Bold() {
	
	// Wrapper
	let postContentWrapperElement 		= document.createElement('div');
	postContentWrapperElement.className = 'postContentWrapper';
	

	// boldTextContent
	let	postContentElement			= document.createElement('textarea');
	postContentElement.className 	= 'postContent';
	postContentElement.setAttribute('content-type', boldTextContentTypeSeq);
	postContentElement.name			= 'boldTextContent';
	postContentElement.placeholder	= 'Text Content';
	postContentElement.spellcheck	= false;
	postContentElement.rows			= 1;
	postContentElement.maxLength	= maxTextContentLength;
	
	postContentWrapperElement.appendChild(postContentElement);
	
	// Title
	let postContentTitleElement			= document.createElement('span');
	postContentTitleElement.className	= 'postContentTitle';
	postContentTitleElement.innerText	= 'TEXT CONTENT (BOLD TEXT)';
	
	postContentWrapperElement.appendChild(postContentTitleElement);
	
	
	// ContentLength
	let postContentLengthElement				= document.createElement('div');
	postContentLengthElement.className			= 'postContentLength';
	
	let postContentCurrentLengthElement			= document.createElement('span');
	postContentCurrentLengthElement.className	= 'currentLength';
	postContentCurrentLengthElement.innerText	= '0';
	
	let postContentMaxLengthElement				= document.createElement('span');
	postContentMaxLengthElement.className		= 'maxLength';
	postContentMaxLengthElement.innerText		= maxTextContentLength;
	
	postContentLengthElement.appendChild(postContentCurrentLengthElement);
	postContentLengthElement.appendChild(postContentMaxLengthElement);
	
	postContentWrapperElement.appendChild(postContentLengthElement);
	
	
	// DeleteBtn
	let postContentDeleteElement		= document.createElement('img');
	postContentDeleteElement.className	= 'postContentDelete';
	postContentDeleteElement.src		= '/img/mini/delete_mini.png';
	
	postContentWrapperElement.appendChild(postContentDeleteElement);
	
	
	
	// ADD EVENT LISTENER
	
	let inputListener = ()=>{
		const currentScroll = window.scrollY;
		postContentElement.style.height = 'auto'; 
		postContentElement.style.height = postContentElement.scrollHeight + 'px';
		
		let currentContentLength = parseInt( postContentElement.value.length );
		
		postContentCurrentLengthElement.innerText 
			= currentContentLength;
		
		window.scrollTo(0, currentScroll);
	}
	let focusListener = ()=>{
		document.getElementById('headerContainer').style.opacity = '0.7';
	}
	let blurListener = ()=>{
		document.getElementById('headerContainer').style.opacity = '1';
	}
	let deleteListener = ()=>{
		let methodToExecute = ()=>{
				postContentWrapperElement.remove();
				countEachContentType();
		}
		dialog_yesOrNo('Are you sure to delete the selected post content?',methodToExecute);
		showDialog();
	}
	
	postContentElement.addEventListener('input',inputListener);
	postContentElement.addEventListener('focus',focusListener);
	postContentElement.addEventListener('blur',blurListener);
	postContentDeleteElement.addEventListener('click',deleteListener);
	
	
	
	// UNFOLD SIDEBAR
	document.getElementById('showMenu').checked = false;
	let changeEvent = new Event('change');
	document.getElementById('showMenu').dispatchEvent(changeEvent);
	
	
	// ADD FINALLY
	document.getElementById('postContentWrapperContainer').appendChild(postContentWrapperElement);
	postContentElement.focus();

	
	// FIT SIZE
	postContentElement.style.height = 'auto';
	postContentElement.style.height = 
		postContentElement.scrollHeight + 'px';
	
	
	countEachContentType();
}

function addImage() {
	
	// Wrapper
	let postContentWrapperElement 		= document.createElement('div');
	postContentWrapperElement.className = 'postContentWrapper';
	
	// Image Content
	let	postContentElement			= document.createElement('div');
	postContentElement.className 	= 'postContent';
	postContentElement.setAttribute('content-type', imageContentTypeSeq);
	postContentElement.setAttribute('name', 'imgContent');	// .name = ~ 는 form 형태 tag(textarea, input 등에서만 적용)
	
	
	postContentWrapperElement.appendChild(postContentElement);
	
	// guide img
	let imageUploadImage 			= document.createElement('img');
	imageUploadImage.className		= 'imgUploadImage';
	imageUploadImage.src 			= '/img/mini/upload_mini.png';
	
	
	// input
	let imgUploadInput				= document.createElement('input');
	imgUploadInput.className		= 'imgUpload';
	imgUploadInput.type				= 'file'
	imgUploadInput.accept 			= 'image/*';
	
	postContentElement.appendChild(imageUploadImage);
	postContentElement.appendChild(imgUploadInput);
	
	// Title
	let postContentTitleElement			= document.createElement('span');
	postContentTitleElement.className	= 'postContentTitle';
	postContentTitleElement.innerText	= 'IMAGE (jpg, png, gif)';
	
	postContentWrapperElement.appendChild(postContentTitleElement);
	
	
	// ContentLength
	let postContentLengthElement				= document.createElement('div');
	postContentLengthElement.className			= 'postContentLength';
	
	let postContentCurrentLengthElement			= document.createElement('span');
	postContentCurrentLengthElement.className	= 'currentLength';
	postContentCurrentLengthElement.innerText	= '0';
	
	let postContentMaxLengthElement				= document.createElement('span');
	postContentMaxLengthElement.className		= 'maxLength';
	postContentMaxLengthElement.innerText		= maxImgFileSize/(1024*1024) + ' MB';
	
	postContentLengthElement.appendChild(postContentCurrentLengthElement);
	postContentLengthElement.appendChild(postContentMaxLengthElement);
	
	postContentWrapperElement.appendChild(postContentLengthElement);
	
	
	// DeleteBtn
	let postContentDeleteElement		= document.createElement('img');
	postContentDeleteElement.className	= 'postContentDelete';
	postContentDeleteElement.src		= '/img/mini/delete_mini.png';
	
	postContentWrapperElement.appendChild(postContentDeleteElement);
	
	
	
	// IMAGE STOREAGE
	postImgContentStorage.push({
		key:postContentElement,
		value:null
	});
	
	
	// ADD EVENT LISTENER
	let imgUploadClick = ()=>{
		imgUploadInput.click()
	}
	
	
	// COMPONENT CLICK HANDLING
	let handleImgUploadBtnClick = (event) => {
		
		
		let isImgUploaded = true;
		
		for(let i=0;i<postImgContentStorage.length;i++) {
			if( postImgContentStorage[i].key === postContentElement ) {
				if(postImgContentStorage[i].value === null) {
					isImgUploaded = false;
				}
			}
		};
		
		// ask user if they want to delete the selected picture when the component already holds image
		if(isImgUploaded) {
			
			event.preventDefault(); 
			
			let methodToExecute = ()=>{
				
				imgUploadInput.value 						= '';
				postContentLengthElement.style.color 		= '#ddd';
				postContentCurrentLengthElement.innerText 	= '0';
				
				let formerLoadedImg = postContentElement.querySelector('.loadedImg');
				if(formerLoadedImg) {
					formerLoadedImg.remove();
				}
				
				
				for(let i=0;i<postImgContentStorage.length;i++) {
					if( postImgContentStorage[i].key === postContentElement ) {
						postImgContentStorage[i].value = null;
						break;
					}
				};
				
			}
			dialog_yesOrNo('Are you sure to delete the selected image?',methodToExecute);
			showDialog();
			
		}
		
		
	}
	
	
	// INPUT[TYPE=FILE] IMAGE LOAD HANDLING
	
	let handleLoadedImage = (event)=> {	
		
		let file = event.target.files[0];
		
		imgUploadInput.value = '';
		
		if(!file) {	
			return;
		}
		
		if (!file.type.startsWith('image/')) {
			return;
		}
		
		
		let reader 		= new FileReader();
		reader.onload 	= function(e) {
			
			let formerLoadedImg = postContentElement.querySelector('.loadedImg');
			if(formerLoadedImg) {	formerLoadedImg.remove();	}
			
			
			let imgLoaded 		= document.createElement('img');
			
			imgLoaded.className	= 'loadedImg';
			imgLoaded.src 		= e.target.result;
			
			postContentElement.appendChild(imgLoaded);
			
		};
		reader.readAsDataURL(file);
		postContentCurrentLengthElement.innerText = (file.size / 1024 / 1024).toFixed(2); 
		if(file.size > maxImgFileSize) {
			postContentLengthElement.style.color = 'red';
		} else {
			postContentLengthElement.style.color = '#ddd';
		}
		
		
		for(let i=0;i<postImgContentStorage.length;i++) {
			if( postImgContentStorage[i].key === postContentElement ) {
				postImgContentStorage[i].value = file;
				return;
			}
		};

	}
	
	
	
	//	FILE DROP HANDLING
	
	let dropUploadImage = (event)=> {	
		
		event.preventDefault();
	  
	  
	  	let files = event.dataTransfer.files;
	  	if(files.length <= 0) {return;}
	  	
	  	let file = files[0];
		if(!file) {	return; }
		
		if (!file.type.startsWith('image/')) {	return;	}


		let reader 		= new FileReader();
		reader.onload 	= function(e) {
			
			let formerLoadedImg = postContentElement.querySelector('.loadedImg');
			if(formerLoadedImg) {	formerLoadedImg.remove();	}
			
			
			let imgLoaded 		= document.createElement('img');
			
			imgLoaded.className	= 'loadedImg';
			imgLoaded.src 		= e.target.result;
			
			postContentElement.appendChild(imgLoaded);
			
		};
		reader.readAsDataURL(file);
		postContentCurrentLengthElement.innerText = (file.size / 1024 / 1024).toFixed(2); 
		if(file.size > maxImgFileSize) {
			postContentLengthElement.style.color = 'red';
		} else {
			postContentLengthElement.style.color = '#ddd';
		}
		
		for(let i=0;i<postImgContentStorage.length;i++) {
			if( postImgContentStorage[i].key === postContentElement ) {
				postImgContentStorage[i].value = file;
				return;
			}
		};
		
		
	}
	
	
	let deleteListener = ()=>{
		
		let methodToExecute = ()=>{
			
			for(let i=0;i<postImgContentStorage.length;i++) {
				if( postImgContentStorage[i].key === postContentElement ) {
					postImgContentStorage.splice(i,1);
					break;
				}
			};
			
			postContentWrapperElement.remove();
			countEachContentType();
			
		}
		dialog_yesOrNo('Are you sure to delete the selected post content?',methodToExecute);
		showDialog();

	}
	
	postContentElement.addEventListener('click',imgUploadClick);
	
	imgUploadInput.addEventListener('click',handleImgUploadBtnClick);
	imgUploadInput.addEventListener('change',handleLoadedImage);
	
	// Necessary to allow the drop
	postContentElement.addEventListener('dragover', (event) => {event.preventDefault();});
	postContentElement.addEventListener('drop',dropUploadImage);
	
	postContentDeleteElement.addEventListener('click',deleteListener);

	
	
	// UNFOLD SIDEBAR
	document.getElementById('showMenu').checked = false;
	let changeEvent = new Event('change');
	document.getElementById('showMenu').dispatchEvent(changeEvent);
	
	
	// ADD FINALLY
	document.getElementById('postContentWrapperContainer').appendChild(postContentWrapperElement);
	postContentElement.scrollIntoView({ behavior: "smooth", block: "center" });

	countEachContentType();
}


function showWaitingModalBackground() {
	document.getElementById('modalBackground').style.display = 'flex';
	let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
	
	document.body.style.paddingRight = `${scrollBarWidth}px`;
	if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
		document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
	}
	document.documentElement.style.overflow = 'hidden';
}

function hideWaitingModalBackground() {
	document.getElementById('modalBackground').style.display 		= 'none';
	document.documentElement.style.overflow 						= 'auto';
	document.body.style.paddingRight 								= '';
	document.getElementById('headerContainer').style.paddingRight 	= '';
}

function showPreview() {
	
	
	
	// post title 에 title 넣기
	 document.getElementById('postTitle_preview').innerText 
	 	= document.getElementById('postTitle').value;
	 	
	
	// create 모드에서는 create time 현재시간으로변경
	// update 모드에서는 update time 을 현재시간으로 변경
	if(postSeq === null)
	{
		document.getElementById('postCreatedTime_preview').innerText
			= getCurrentTimeFormatted();
	}
	else
	{
		document.getElementById('postUpdatedTime_preview').innerText
			= getCurrentTimeFormatted();
	}


	
	
	// 생성된 textbox / boldTextbox / imagebox 를 읽어서 postContent 로 css 스타일에 맞게 postContents 에 넣으면 됨
	
	let postContents = document.getElementById('postContents_preview');
	postContents.innerHTML = '';
	
	let renderMap = [];
	let howManyImagesToRender = 0;
	
	for(let i=0;i<document.getElementsByClassName('postContent').length;i++){
		let postContent 	= document.getElementsByClassName('postContent')[i];
		let postContentType	= parseInt( postContent.getAttribute('content-type') );
		
		if(postContentType === imageContentTypeSeq) {
			howManyImagesToRender++;
		};
	};
	
	for(let i=0;i<document.getElementsByClassName('postContent').length;i++){
		
		let postContent 	= document.getElementsByClassName('postContent')[i];
		let postContentType	= parseInt( postContent.getAttribute('content-type') );
		
		if(postContentType === textContentTypeSeq)
		{
			let postContentElement = document.createElement('div');
			postContentElement.className = 'postContent';
			
			let textContentElement = document.createElement('pre');
			textContentElement.className = 'textContent';
			textContentElement.innerText = postContent.value;
			
			postContentElement.appendChild(textContentElement);
			
			postContents.appendChild(postContentElement);
			
		}
		else if(postContentType === boldTextContentTypeSeq)
		{
			let postContentElement = document.createElement('div');
			postContentElement.className = 'postContent';
			
			let textContentElement = document.createElement('pre');
			textContentElement.className = 'boldTextContent';
			textContentElement.innerText = postContent.value;
			
			postContentElement.appendChild(textContentElement);
			
			postContents.appendChild(postContentElement);
		}
		else if(postContentType === imageContentTypeSeq)
		{
			for(let i=0;i<postImgContentStorage.length;i++) {
				if( postImgContentStorage[i].key === postContent ) {
					
					let postContentElement = document.createElement('div');
					postContentElement.className = 'postContent';
					
					let imgContentWrapperElement = document.createElement('div');
					imgContentWrapperElement.className = 'imageContent';
					
					let imgContentElement = document.createElement('img');
					
					postContents.appendChild(postContentElement);
					postContentElement.appendChild(imgContentWrapperElement);
					imgContentWrapperElement.appendChild(imgContentElement);
					
					
					let loadedImgElement = postContent.querySelector('.loadedImg');
					if(loadedImgElement === null) {
						
						renderMap.push({
							element:imgContentElement,
							src:''
						});
						
					} else {
		
						renderMap.push({
							element:imgContentElement,
							src:loadedImgElement.src
						});
						
					}
					
					
					if(renderMap.length === howManyImagesToRender) {
						setTimeout(()=>{
							for(let i=0;i<renderMap.length;i++) {
								renderMap[i].element.src = renderMap[i].src;
							}
						},310);
					}

					break;
					
				}
			};
		}
	
	}
	
	
	
	// UNFOLD SIDEBAR
	document.getElementById('showMenu').checked = false;
	let changeEvent = new Event('change');
	document.getElementById('showMenu').dispatchEvent(changeEvent);
	
	
	// OPEN PREVIEW DIALOG
	document.getElementById('previewOnOff').checked = true;
	let previewChangeEvent = new Event('change');
	document.getElementById('previewOnOff').dispatchEvent(previewChangeEvent);
	
}


function getCurrentTimeFormatted() {
    let date = new Date();
    
	let year 	= date.getFullYear();
    let month 	= String(date.getMonth() + 1).padStart(2, '0');
    let day 	= String(date.getDate()).padStart(2, '0');
    let hours 	= String(date.getHours()).padStart(2, '0');
    let minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
}



