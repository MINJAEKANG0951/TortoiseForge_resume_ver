
var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

var isUserLoggedIn = null;
var postSeq = null;
var maxCommentLength = null;

document.addEventListener('DOMContentLoaded',()=>{
	
	
	// INITIALIZE VARIABLES
	isUserLoggedIn		= document.getElementById('userLoginFlag') != null;
	postSeq				= parseInt( document.getElementById('postSeq').value );
	maxCommentLength	= parseInt( document.getElementById('maxCommentLength').value );


	
	// UTC TO LOCAL TIME
	refreshTimeElements();


	// POST WRITER INFO 1
	document.getElementById('postWriterInfo').addEventListener('click',()=>{
		
		let writerSeq = document.getElementById('postWriterInfo').getAttribute('writer_seq');
		document.location.href = "/userInfo/" + writerSeq;	
		
	});


	// POST LIKE BTN
	document.getElementById('postLikeBtn').addEventListener('click',()=>{
		if(shouldTellUserToLogin(isUserLoggedIn)) {
			return false;
		}
		likePost();
	});


	// POST RELATED BTNS
	document.getElementById('returnToBoardBtn').addEventListener('click',()=>{
		if (window.history.length > 1) {
        	window.history.back();
	    } else {
	        window.location.href = '/';
	    }
	});
	
	document.getElementById('postBtn').addEventListener('click',()=>{
		document.location.href = '/discussionBoard/create';
	});
	
	document.getElementById('modifyBtn').addEventListener('click',()=>{
		
		let urlParams = new URLSearchParams(window.location.search);
		urlParams.set('postSeq', postSeq);
		
		let url = `/discussionBoard/update?${urlParams.toString()}`;
		document.location.href = url;
		
	});
	
	document.getElementById('deleteBtn').addEventListener('click',()=>{
		
		let methodToExecute = function() {
			deletePost(postSeq);
		}
		dialog_yesOrNo('Are you sure to delete the post?', methodToExecute);
		showDialog();
	});





	// COMMENT RELATED BTNS
	document.getElementById('postComments').addEventListener('click',(event)=>{
		
		let postCommentWriter= event.target.closest('.postCommentWriter');
		let postCommentLikeImg = event.target.closest('.postCommentLikeImg');
		let postCommentReplyImg = event.target.closest('.postCommentReplyImg');
		let postCommentEtc = event.target.closest('.postCommentEtc');
		
		
		if(postCommentWriter)
		{
			let userSeq = 
				postCommentWriter
				.parentElement
				.querySelector('.postCommentWriterSeq')
				.innerText;
			document.location.href = "/userInfo/" + userSeq;
		} 
		else if(postCommentLikeImg)
		{
			if(shouldTellUserToLogin(isUserLoggedIn)) {
				return false;
			}
			let commentSeq = 
				parseInt(
					postCommentLikeImg
					.parentElement
					.parentElement
					.parentElement
					.querySelector('.postCommentSeq')
					.innerText
				);
			
			likeComment(postSeq, commentSeq, postCommentLikeImg);
		}
		else if(postCommentReplyImg)
		{
			if(shouldTellUserToLogin(isUserLoggedIn)) {
				return false;
			}

			let username = 
				postCommentReplyImg
				.parentElement
				.parentElement
				.parentElement
				.querySelector('.postCommentInfo')
				.querySelector('.postCommentWriter')
				.querySelector('.postCommentWriterName')
				.innerText;
				
			let subjectCommentSeq = 
				parseInt(
					postCommentReplyImg
					.parentElement
					.parentElement
					.parentElement
					.querySelector('.postCommentSeq')
					.innerText
				);
			
			let methodToExecute = function(){	
				let commentContent = document.getElementById('dialog').querySelector('.contentWrite').value;
				addComment(postSeq, commentContent, subjectCommentSeq);
			};
			
			dialog_replyToComment(username, maxCommentLength, methodToExecute);
			showDialog();

		}
		else if(postCommentEtc)
		{
			if(shouldTellUserToLogin(isUserLoggedIn)) {
				return false;
			}
			
			let subjectCommentSeq = 
			parseInt(
				postCommentEtc
				.parentElement
				.parentElement
				.querySelector('.postCommentSeq')
				.innerText
			);
			
			let subjectCommentContent = 
				postCommentEtc
				.parentElement
				.parentElement
				.querySelector('.postCommentContent')
				.querySelector('.postCommentContentText')
				.innerText;
			
			let reportFunc = function() {
				// later..!
			};
			
			let modifyFunc = function() {
				
				showDialogModalBackground();
				setTimeout(()=>{
					hideDialogModalBackground();
					let methodToExecute = function() {
						let commentContent = document.getElementById('dialog').querySelector('.contentWrite').value;
						updateComment(postSeq, subjectCommentSeq, commentContent);
					};
					dialog_modifyComment(subjectCommentContent, maxCommentLength, methodToExecute);
					showDialog();
				},300);
				
			};
			
			let deleteFunc = function() {
				showDialogModalBackground();
				setTimeout(()=>{
					hideDialogModalBackground();
					let methodToExecute = function() {
						deleteComment(postSeq, subjectCommentSeq);
					};
					dialog_yesOrNo('Are you sure to delete the comment?', methodToExecute);
					showDialog();
				},300);
			};
			
			dialog_additionalWorkOnComment(reportFunc, modifyFunc, deleteFunc);
			showDialog();
		};
		
		
	});



	// COMMENT WRITE CONTENT
	document.getElementById('CommentWriteContent').addEventListener('input',()=>{
	
		let currentScroll = window.scrollY;
	
		document.getElementById('CommentWriteContent').style.height = 'auto'; // Reset height to calculate new height
		document.getElementById('CommentWriteContent').style.height = document.getElementById('CommentWriteContent').scrollHeight + 'px'; // Set new height
		
		
		let currentContentLength = parseInt( document.getElementById('CommentWriteContent').value.length );
		document.getElementById('CommentWriteContentCurrentLength').innerText = currentContentLength;
		window.scrollTo(0, currentScroll);

	});

	
	window.addEventListener('resize', function() {
	
		const commentInput = document.getElementById('CommentWriteContent');
		const inputEvent = new Event('input', { bubbles: true, cancelable: true });
		commentInput.dispatchEvent(inputEvent);
    
	});

	
	window.addEventListener('pageshow', function() {
		
		document.getElementById('CommentWriteContent').value = '';
		document.getElementById('CommentWriteContent').style.height = 'auto';
		document.getElementById('CommentWriteContent').style.height = 
			document.getElementById('CommentWriteContent').scrollHeight + 'px';
	
	});
	


	document.getElementById('commentWriteBtn').addEventListener('click',()=>{
		
		if(shouldTellUserToLogin(isUserLoggedIn)) {
			return false;
		}
		
		let commentContent = document.getElementById('CommentWriteContent').value
		addComment(postSeq, commentContent, null);
	});

})

function refreshTimeElements() {
	
	let postCreationTimeWrapper = document.getElementById('postCreationTime');
	let postUpdatedTimeWrapper 	= document.getElementById('postUpdatedTime');
	
	if(postCreationTimeWrapper != null) {
		postCreationTimeWrapper.innerText = utcToLocal(postCreationTimeWrapper.innerText);
	}
	
	if(postUpdatedTimeWrapper != null) {
		postUpdatedTimeWrapper.innerText = utcToLocal(postUpdatedTimeWrapper.innerText);
	}
	
	
	let commentCreatedTimes = document.querySelectorAll('.postCommentCreated');
	let commentUpdatedTimes = document.querySelectorAll('.postCommentUpdated');
	
	for(let i=0;i<commentCreatedTimes.length;i++) {
		commentCreatedTimes[i].innerText = utcToLocal(commentCreatedTimes[i].innerText) + ' created';
	}
	
	for(let i=0;i<commentUpdatedTimes.length;i++) {
		commentUpdatedTimes[i].innerText = utcToLocal(commentUpdatedTimes[i].innerText) + ' updated';
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



function likePost() {
	
		let formData = new FormData();
		formData.append('postSeq', postSeq);
		
		let xhr = new XMLHttpRequest();
		xhr.open('POST','/discussionBoard/read/postLike',true);
		xhr.setRequestHeader(csrfHeader,csrfToken);
		xhr.onreadystatechange = function(){
			if(xhr.readyState === 4)
			{
				if(xhr.status === 200)
				{
					let data = JSON.parse(xhr.responseText);
					refreshPostLike(data.howmanyPostLikes, data.postLikedOrNot);
				}
				else if(xhr.status === 403)
				{
					let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
					
					let methodToExecute = function() {console.log(text);}
					dialog_confirm(text, methodToExecute)
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

function likeComment(postSeq, commentSeq, postCommentLikeImgRef) {
	
	let formData = new FormData();
	formData.append('postSeq', postSeq);
	formData.append('commentSeq', commentSeq);
	
	let xhr = new XMLHttpRequest();
	xhr.open('POST', '/discussionBoard/read/commentLike', true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);

				postCommentLikeImgRef.
					parentElement
						.querySelector('.postCommentLikedOrNot').checked = data.isLiked;
			
				postCommentLikeImgRef.
					parentElement
						.querySelector('.postCommentLikeNumber').innerText = data.howmanyLiked;

			}
			else if(xhr.status === 403)
			{
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				
				let methodToExecute = function() {console.log(text);}
				dialog_confirm(text, methodToExecute)
				showDialog();
			}
			else if(xhr.status === 404)
			{
				let data = JSON.parse(xhr.responseText);
				refreshComments(data.postComments, data.howmanyComments);
				
				let methodToExecute = function() {console.log(data.message);}
				dialog_confirm(data.message, methodToExecute);
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
	};
	xhr.send(formData);
}




function addComment(postSeqToComment, commentContent, subjectCommentSeq) {
	
		let formData = new FormData();
		formData.append('postSeq', postSeqToComment);
		formData.append('commentContent', commentContent);
		if(subjectCommentSeq != null) {
			formData.append('subjectCommentSeq', subjectCommentSeq);
		}
		
		let xhr = new XMLHttpRequest();
		xhr.open('POST','/discussionBoard/read/comment',true);
		xhr.setRequestHeader(csrfHeader,csrfToken);
		xhr.onreadystatechange = function(){
			if(xhr.readyState === 4)
			{
				if(xhr.status === 200)
				{
					let data = JSON.parse(xhr.responseText);
					refreshComments(data.postComments, data.howmanyComments);
					
					const inputEvent = new Event('input', { bubbles: true, cancelable: true });
					document.getElementById('CommentWriteContent').value = '';
					document.getElementById('CommentWriteContent').dispatchEvent(inputEvent);
					
					hideWaitingModalBackground();
				}
				else if(xhr.status === 404)
				{
					let data = JSON.parse(xhr.responseText);
					refreshComments(data.postComments, data.howmanyComments);
					
					const inputEvent = new Event('input', { bubbles: true, cancelable: true });
					document.getElementById('CommentWriteContent').value = '';
					document.getElementById('CommentWriteContent').dispatchEvent(inputEvent);
					
					let methodToExecute = function() {console.log(data.message);}
					dialog_confirm(data.message, methodToExecute);
					setTimeout(()=>{
						hideWaitingModalBackground();
						showDialog(); 
					},300);
				}
				else if(xhr.status === 403)
				{
					let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
					
					let methodToExecute = function() {console.log(text);}
					dialog_confirm(text, methodToExecute)
					setTimeout(()=>{
						hideWaitingModalBackground();
						showDialog(); 
					},300);
				}
				else
				{
					let data = JSON.parse(xhr.responseText);
					let methodToExecute = function() {console.log(data.message);}
					dialog_confirm(data.message, methodToExecute);
					setTimeout(()=>{
						hideWaitingModalBackground();
						showDialog(); 
					},300);
				}
			}
		}
		xhr.send(formData);
		showWaitingModalBackground();
	
}


function updateComment(postSeq, commentSeq, commentContent) {
	
	
		let formData = new FormData();
		formData.append('postSeq', postSeq);
		formData.append('subjectCommentSeq', commentSeq);
		formData.append('commentContent', commentContent);
		
		let xhr = new XMLHttpRequest();
		xhr.open('PATCH','/discussionBoard/read/comment',true);
		xhr.setRequestHeader(csrfHeader,csrfToken);
		xhr.onreadystatechange = function(){
			if(xhr.readyState === 4)
			{
				if(xhr.status === 200)
				{
					let data = JSON.parse(xhr.responseText);
					refreshComments(data.postComments, data.howmanyComments);
					hideWaitingModalBackground();
				}
				else if(xhr.status === 404)
				{
					let data = JSON.parse(xhr.responseText);
					refreshComments(data.postComments, data.howmanyComments);
					
					let methodToExecute = function() {console.log(data.message);}
					dialog_confirm(data.message, methodToExecute);
					setTimeout(()=>{
						hideWaitingModalBackground();
						showDialog(); 
					},300);
				}
				else if(xhr.status === 403)
				{
					let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
					
					let methodToExecute = function() {console.log(text);}
					dialog_confirm(text, methodToExecute)
					setTimeout(()=>{
						hideWaitingModalBackground();
						showDialog(); 
					},300);
				}
				else
				{
					let data = JSON.parse(xhr.responseText);
					let methodToExecute = function() {console.log(data.message);}
					dialog_confirm(data.message, methodToExecute);
					setTimeout(()=>{
						hideWaitingModalBackground();
						showDialog(); 
					},300);
				}
			}
		}
		xhr.send(formData);
		showWaitingModalBackground();
	
}


function deleteComment(postSeq, commentSeq) {
	
	let formData = new FormData();	
	formData.append('postSeq', postSeq);
	formData.append('commentSeq', commentSeq);
	
	let xhr = new XMLHttpRequest();
	xhr.open('DELETE','/discussionBoard/read/comment',true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				refreshComments(data.postComments, data.howmanyComments);
				hideWaitingModalBackground();
			}
			else if(xhr.status === 404)
			{
				let data = JSON.parse(xhr.responseText);
				refreshComments(data.postComments, data.howmanyComments);

				let methodToExecute = function() {console.log(data.message);}
				dialog_confirm(data.message, methodToExecute);
				setTimeout(()=>{
					hideWaitingModalBackground();
					showDialog(); 
				},300);
			}
			else if(xhr.status === 403)
			{
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				let methodToExecute = function() {console.log(text);}
				dialog_confirm(text, methodToExecute)
				setTimeout(()=>{
					hideWaitingModalBackground();
					showDialog(); 
				},300);
			}
			else
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {console.log(data.message);}
				dialog_confirm(data.message, methodToExecute);
				setTimeout(()=>{
					hideWaitingModalBackground();
					showDialog(); 
				},300);
			}
		}
	}
	xhr.send(formData);
	showWaitingModalBackground();
	
}


function deletePost(postSeq) {

	let formData = new FormData();	
	formData.append('postSeq', postSeq);
	
	let xhr = new XMLHttpRequest();
	xhr.open('DELETE','/discussionBoard/post',true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {
					console.log(data.message);
					setTimeout(()=>{
						document.location.href = "/discussionBoard";
					},310);
				}
				dialog_confirm(data.message, methodToExecute);
				setTimeout(()=>{
					hideWaitingModalBackground();
					showDialog(); 
				},300);
			}
			else if(xhr.status === 404)
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {console.log(data.message);}
				dialog_confirm(data.message, methodToExecute);
				setTimeout(()=>{
					hideWaitingModalBackground();
					showDialog(); 
				},300);
			}
			else if(xhr.status === 403)
			{
				let text = 'Invalid or expired CSRF token. Please refresh the page and try again.';
				let methodToExecute = function() {console.log(text);}
				dialog_confirm(text, methodToExecute)
				setTimeout(()=>{
					hideWaitingModalBackground();
					showDialog(); 
				},300);
			}
			else
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {console.log(data.message);}
				dialog_confirm(data.message, methodToExecute);
				setTimeout(()=>{
					hideWaitingModalBackground();
					showDialog(); 
				},300);
			}
		}
	}
	xhr.send(formData);
	showWaitingModalBackground();
	
}








function refreshPostRead() {
	
	
	
	
	
	
}

function refreshPostContents() {
	
}


function refreshPostLike(howmanyLiked, isLiked) {
	
	document.getElementById('postLikeBtnCheckBox').checked = isLiked;
	
	let postLikesElements = document.getElementsByName('howmanyPostLikes');
	for(let i=0;i<postLikesElements.length;i++) {
		let postLikeElement = postLikesElements[i];
		postLikeElement.innerText = howmanyLiked;		
	};

}


function refreshComments(postComments, howmanyComments) {
	
	let postCommentsElements = document.getElementsByName('howmanyPostComments');
	for(let i=0;i<postCommentsElements.length;i++) {
		let postCommentsElement = postCommentsElements[i];
		postCommentsElement.innerText = howmanyComments;		
	};
	
	if(postComments === null || postComments.length === 0) {
		document.getElementById('postComments').style.display = 'none'
	} else {
		document.getElementById('postComments').style.display = 'block'
	}
	
	const parentElement = document.getElementById('postComments');
	const fragement 	= document.createDocumentFragment();
	
	
	for(let i=0;i<postComments.length;i++)
	{
		let postComment = postComments[i];
		

		let postCommentElement = document.createElement('div');
		postCommentElement.className = 'postComment';
		postCommentElement.setAttribute('name', postComment.parentCommentSeq != null ? 'childComment' : 'parentComment');
		
		
		let postCommentSeqElement = document.createElement('div');
		postCommentSeqElement.className = 'postCommentSeq';
		postCommentSeqElement.innerText = postComment.commentSeq;
		
		
		let postCommentInfoElement = document.createElement('div');
		postCommentInfoElement.className = 'postCommentInfo';
	
	
		let postCommentWriterElement = document.createElement('div');
		postCommentWriterElement.className = 'postCommentWriter';
		
		let postChildCommentArrowElement = document.createElement('img');
		postChildCommentArrowElement.className = 'postChildCommentArrow';
		postChildCommentArrowElement.src = '/img/mini/comment_mini.png';
		
		let postCommentWriterImgElement = document.createElement('img');
		postCommentWriterImgElement.className = 'postCommentWriterImg';
		postCommentWriterImgElement.src = '/img/mini/tortoise_type_user_colored_mini.png';
		
		let postCommentWriterNameElement = document.createElement('span');
		postCommentWriterNameElement.className = 'postCommentWriterName';
		postCommentWriterNameElement.innerText = postComment.username;
		
		let postCommentWriterSeqElement = document.createElement('span');
		postCommentWriterSeqElement.className = 'postCommentWriterSeq';
		postCommentWriterSeqElement.innerText = postComment.userSeq;
		
		if(postComment.parentCommentSeq != null) {
			postCommentWriterElement.append(postChildCommentArrowElement);
		};
		postCommentWriterElement.append(postCommentWriterImgElement);
		postCommentWriterElement.append(postCommentWriterNameElement);
		postCommentWriterElement.append(postCommentWriterSeqElement);
		
		let postCommentEtcElement = document.createElement('img');
		postCommentEtcElement.className = 'postCommentEtc';
		postCommentEtcElement.src = '/img/mini/kebab_icon_mini.png';
		if(!postComment.userComment) {
			postCommentEtcElement.style.display = 'none';
		}
		
		postCommentInfoElement.append(postCommentWriterElement);
		postCommentInfoElement.append(postCommentEtcElement);
		
		
		
		let postCommentContentElement = document.createElement('pre');
		postCommentContentElement.className = 'postCommentContent';
		
		let targetCommentSeqElement = document.createElement('span');
		targetCommentSeqElement.className = 'targetCommentSeq';
		targetCommentSeqElement.innerText = postComment.targetCommentSeq;
		
		let targetedCommentWriterElement = document.createElement('span');
		targetedCommentWriterElement.className = 'targetedCommentWriter';
		targetedCommentWriterElement.innerText = '@' + postComment.targetCommentWriter + ' ';
		
		let commentContentElement = document.createElement('span');
		commentContentElement.className = 'postCommentContentText';
		commentContentElement.innerText = postComment.commentContent;
		
		if(postComment.targetCommentSeq != null) {
			postCommentContentElement.appendChild(targetCommentSeqElement);
			postCommentContentElement.appendChild(targetedCommentWriterElement);
		};

		postCommentContentElement.appendChild(commentContentElement);
		
		
		
		
		let postCommentRelatedBtnsElement = document.createElement('div');
		postCommentRelatedBtnsElement.className = 'postCommentRelatedBtns';
		
		
		let postCommentLikeElement = document.createElement('div');
		postCommentLikeElement.className = 'postCommentLike';
		
		let postCommentLikedOrNotElement = document.createElement('input');
		postCommentLikedOrNotElement.className = 'postCommentLikedOrNot';
		postCommentLikedOrNotElement.type = 'checkbox';
		postCommentLikedOrNotElement.checked = postComment.liked;
		
		let postCommentLikedImgElement = document.createElement('img');
		postCommentLikedImgElement.className = 'postCommentLikeImg';
		postCommentLikedImgElement.setAttribute('name','postCommentLikedImg');
		postCommentLikedImgElement.src = '/img/mini/comment_liked_mini.png';
		
		let postCommentUnLikedImgElement = document.createElement('img');
		postCommentUnLikedImgElement.className = 'postCommentLikeImg';
		postCommentUnLikedImgElement.setAttribute('name','postCommentUnLikedImg');
		postCommentUnLikedImgElement.src = '/img/mini/comment_like_mini.png';
		
		let postCommentLikeNumberElement = document.createElement('span');
		postCommentLikeNumberElement.className = 'postCommentLikeNumber';
		postCommentLikeNumberElement.innerText = postComment.likes;
		
		postCommentLikeElement.appendChild(postCommentLikedOrNotElement);
		postCommentLikeElement.appendChild(postCommentLikedImgElement);
		postCommentLikeElement.appendChild(postCommentUnLikedImgElement);
		postCommentLikeElement.appendChild(postCommentLikeNumberElement);
		
		
		let postCommentReplyElement = document.createElement('div');
		postCommentReplyElement.className = 'postCommentReply';
		
		let postCommentReplyImgElement = document.createElement('img');
		postCommentReplyImgElement.className = 'postCommentReplyImg';
		postCommentReplyImgElement.src = '/img/mini/comment2_mini.png';
		
		postCommentReplyElement.append(postCommentReplyImgElement);
		
		postCommentRelatedBtnsElement.appendChild(postCommentLikeElement);
		postCommentRelatedBtnsElement.appendChild(postCommentReplyElement);
		
		
	
		let postCommentCreatedElement = document.createElement('div');
		postCommentCreatedElement.className = 'postCommentCreated';
		postCommentCreatedElement.innerText = utcToLocal(postComment.commentCreationTime) + ' created';
		
		let postCommentUpdatedElement = document.createElement('div');
		postCommentUpdatedElement.className = 'postCommentUpdated';
		postCommentUpdatedElement.innerText = utcToLocal(postComment.commentUpdatedTime) + ' updated';
		
		
		postCommentElement.appendChild(postCommentSeqElement);
		postCommentElement.appendChild(postCommentInfoElement);
		postCommentElement.appendChild(postCommentContentElement);
		postCommentElement.appendChild(postCommentRelatedBtnsElement);
		postCommentElement.appendChild(postCommentCreatedElement);
		if(postComment.commentUpdatedTime !== null) {
			postCommentElement.appendChild(postCommentUpdatedElement);
		}

		fragement.appendChild(postCommentElement);
	}
	
	
	parentElement.innerHTML = '';
	parentElement.appendChild(fragement);

	
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

function showDialogModalBackground() {
	document.getElementById('dialogModalBackground').style.display = 'flex';
	let scrollBarWidth = window.innerWidth - document.documentElement.clientWidth;
	
	document.body.style.paddingRight = `${scrollBarWidth}px`;
	if ( document.getElementById('headerContainer').getAttribute('type') === null ) {
		document.getElementById('headerContainer').style.paddingRight = `${scrollBarWidth}px`;
	}
	document.documentElement.style.overflow = 'hidden';
}

function hideDialogModalBackground() {
	document.getElementById('dialogModalBackground').style.display = 'none';
	document.documentElement.style.overflow 						= 'auto';
	document.body.style.paddingRight 								= '';
	document.getElementById('headerContainer').style.paddingRight 	= '';
}




function shouldTellUserToLogin(isUserLoggedIn) {
	if(isUserLoggedIn) {
		return false;
	} else {
		let text = 'Please login to use the feature.\n\n(If you are already logged in, please refresh the page)';
		let methodToExecute = function(){};
		dialog_confirm(text, methodToExecute);
		showDialog();
		return true;
	}
}

