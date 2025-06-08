var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

var currentPageNum;
var currentPageSize;
var currentSearchQuery;
var currentSearchType;

var totalNumberOfPages;

document.addEventListener('DOMContentLoaded',()=>{

	// INITIALIZE VARIABLES
	currentPageNum 			= parseInt( document.getElementById('currentPage').innerText );
	currentPageSize			= parseInt(document.getElementById('pageSize').value);
	currentSearchQuery 		= document.getElementById('searchQuery').value;
	currentSearchType		= document.getElementById('searchType').value;
	totalNumberOfPages 		= parseInt( document.getElementById('lastPage').innerText );



	// UTC TO LOCAL TIME
	refreshTimeElements();


	// ADD LISTENERS
	document.getElementById('searchQuery').addEventListener('keydown',(event)=>{
		if ( event.key === 'Enter' ) {
			document.getElementById('searchBtn').click();
    	}
	});
	
	document.getElementById('searchBtn').addEventListener('click',()=>{
		
		currentPageNum 		= 1;
		currentPageSize 	= currentPageSize;
		currentSearchType  	= document.getElementById('searchType').value;
		currentSearchQuery 	= document.getElementById('searchQuery').value;
		
		if(currentSearchQuery.length === 0) return; 
		
		move();
	});
	
	document.getElementById('pageSize').addEventListener('change',(event)=>{
		
		let formerPageSize = currentPageSize;
		
		currentPageNum 		= 1;
		currentPageSize 	= parseInt( event.target.value );
		currentSearchType	= currentSearchType;
		currentSearchQuery 	= currentSearchQuery;
		
		// save currentPageSize and Move
		savePageSizeAndMove(formerPageSize);
		
	});
	
	document.getElementById('postBtn').addEventListener('click',()=>{	
		
		if(document.getElementById('postBtn').getAttribute('name') === 'postBtn') {
			document.location.href = "/discussionBoard/create";
		} else {
			let message = 'You need to log in to create a post (If you are already logged in, please refresh the page).';
			let methodToExecute = function(){
				console.log('Please login to create a post.');
			};
			dialog_confirm(message, methodToExecute);
			showDialog();
		}

	});
	
	document.getElementById('paginationBtn').addEventListener('click',()=>{
		setPageNavDialog();
		showDialog();
	});
	
	document.getElementById('removeFilterBtn').addEventListener('click',()=>{
		document.location = "/discussionBoard";
	});
	
	
	document.getElementById('posts').addEventListener('click',(event)=>{
		
		let post = event.target.closest('.post');
    	let comment = event.target.closest('.comment');
		
		if(post)
		{
			let postPageNumber 	= parseInt(post.querySelector('.postPageNumber').innerText);
			
			let urlParams = new URLSearchParams(window.location.search);
			urlParams.set('pageNumber',postPageNumber);
			urlParams.set('pageSize', currentPageSize);
			urlParams.set('searchQuery',currentSearchQuery);
			urlParams.set('searchType',currentSearchType);
			
			let historyURL = `/discussionBoard?${urlParams.toString()}`;
			history.replaceState(null, '', historyURL);
			
			let postSeq = parseInt(post.querySelector('.postSeq').innerText);
			readPost(parseInt(postSeq));
		}
		else if(comment)
		{	
			let postPageNumber 	= parseInt(comment.querySelector('.postPageNumber').innerText);
			
			let urlParams = new URLSearchParams(window.location.search);
			urlParams.set('pageNumber',postPageNumber);
			urlParams.set('pageSize', currentPageSize);
			urlParams.set('searchQuery',currentSearchQuery);
			urlParams.set('searchType',currentSearchType);
			
			let historyURL = `/discussionBoard?${urlParams.toString()}`;
			history.replaceState(null, '', historyURL);
			
			let postSeq = parseInt(comment.querySelector('.postSeq').innerText);
			let commentSeq = parseInt(comment.querySelector('.commentSeq').innerText);
			readPost(parseInt(postSeq), parseInt(commentSeq));
		}

	})
	
	
	document.getElementById('moreBtn').addEventListener('click',()=>{
		if(currentPageNum >= totalNumberOfPages) return;
		more();
	});
	
	document.getElementById('pagesAroundLeftArrow').addEventListener('click',()=>{
		if(currentPageNum <= 1) return;

		currentPageNum 		= currentPageNum - 5;
		currentPageSize 	= currentPageSize;
		currentSearchType	= currentSearchType;
		currentSearchQuery 	= currentSearchQuery;
		
		move();
	});
	
	document.getElementById('pagesAroundRightArrow').addEventListener('click',()=>{
		if(currentPageNum >= totalNumberOfPages) return;
		
		currentPageNum 		= currentPageNum + 5;
		currentPageSize 	= currentPageSize;
		currentSearchType	= currentSearchType;
		currentSearchQuery 	= currentSearchQuery;
		
		move();
	});

	document.getElementById('pagesAround').addEventListener('click',(event)=>{
		if(event.target.classList.contains('pageAround')) {
			currentPageNum = parseInt(event.target.innerText);
            move();
		} 
	});
	

});



function refreshTimeElements() {
	
	let postCreationTimes 		= document.querySelectorAll('.postCreationTime');
	let commentCreationTimes 	= document.querySelectorAll('.commentCreationTime');
	
	for(let i=0;i<postCreationTimes.length;i++) {
		postCreationTimes[i].innerText = utcToLocal(postCreationTimes[i].innerText);
	}
	
	for(let i=0;i<commentCreationTimes.length;i++) {
		commentCreationTimes[i].innerText = utcToLocal(commentCreationTimes[i].innerText);
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




function move(){
	
	let urlParams = new URLSearchParams();
	urlParams.set('pageNumber',currentPageNum);
	urlParams.set('pageSize',currentPageSize);
	urlParams.set('searchQuery',currentSearchQuery);
	urlParams.set('searchType',currentSearchType);
	
	let url = `/discussionBoard?${urlParams.toString()}`;
	
	window.location.href = url;
};

/*
	mobile 에서 
	페이지 이동 후 backButton 시, 이전페이지에서 DOMLoaded event 가 발생하지 않을 때가
	있기에 추가.
	
	DOMLoaded event 가 발생하면 selectBox 값만 유지하면 currentPageSize 가 set 되나,
	발생하지 않으면 move 하기 전 따로 currentPageSize 을 유지하는 코드를 추가해야 함.

 */
function savePageSizeAndMove(pageSizeToSave) {	
	
	let urlParams = new URLSearchParams();
	urlParams.set('pageNumber',currentPageNum);
	urlParams.set('pageSize',currentPageSize);
	urlParams.set('searchQuery',currentSearchQuery);
	urlParams.set('searchType',currentSearchType);
	
	let url = `/discussionBoard?${urlParams.toString()}`;
	
	let pageSizeTypes = document.getElementsByClassName('pageSizeType');
	for(let i=0;i<pageSizeTypes.length;i++){
		if(pageSizeToSave === parseInt(pageSizeTypes[i].value)) {
			pageSizeTypes[i].selected = true;
			break;
		}
	}
	currentPageSize = pageSizeToSave;
	
	window.location.href = url;
}

function more() {
	
	let urlParams = new URLSearchParams(window.location.search);
	urlParams.set('pageNumber',currentPageNum + 1);
	urlParams.set('pageSize',document.getElementById('pageSize').value);
	urlParams.set('searchQuery',currentSearchQuery);
	urlParams.set('searchType',currentSearchType);
	
	let url = `/discussionBoard/more?${urlParams.toString()}`;
	let historyURL = `/discussionBoard?${urlParams.toString()}`;
	
	let xhr = new XMLHttpRequest();
	xhr.open('GET',url,true);
	xhr.setRequestHeader(csrfHeader,csrfToken);
	xhr.onreadystatechange = function(){
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200)
			{
				let data = JSON.parse(xhr.responseText);
				refresh(data.boardSearchResult);
				setPageNavDialog();
				
				history.replaceState(null, '', historyURL);
			}
			else if(xhr.status === 403)
			{
				let methodToExecute = function() {
					console.log('Invalid or expired CSRF token. Please refresh the page and try again.');
				}
				dialog_confirm('Invalid or expired CSRF token. Please refresh the page and try again.', methodToExecute)
				showDialog();
			}
			else
			{
				let data = JSON.parse(xhr.responseText);
				let methodToExecute = function() {
					console.log(data.message);
				}
				dialog_confirm(data.message, methodToExecute);
				showDialog();
			}
			document.getElementById('moreBtnProgressCircle').style.display = "none";
			document.getElementById('moreBtnArrow').style.display = "block";	
		}
	}
	xhr.send();
	document.getElementById('moreBtnArrow').style.display = "none";	
	document.getElementById('moreBtnProgressCircle').style.display = "block";
	
};

function refresh(boardSearchResult) {
	
	currentPageNum 			= boardSearchResult.currentPageNum;	
	currentPageSize			= boardSearchResult.currentPageSize;
	currentSearchQuery 		= boardSearchResult.searchQuery;
	currentSearchType		= boardSearchResult.searchType;
	totalNumberOfPages 		= boardSearchResult.totalNumberOfPages;

	let posts			= boardSearchResult.posts;
	let comments		= boardSearchResult.comments;
	let pagesAround		= boardSearchResult.pagesAround;

	
	// DO NOT RENDER DUPLICATED, AND RENDER IN ORDER
	let postSeqs 		= document.getElementsByClassName('postSeq');
	let smallestPostSeq	= postSeqs.length === 0 ? null : parseInt(postSeqs[postSeqs.length-1].innerText);		
	
	let commentSeqs 		= document.getElementsByClassName('commentSeq');
	let smallestCommentSeq	= commentSeqs.length === 0 ? null : parseInt(commentSeqs[commentSeqs.length-1].innerText);		
	
	// CURRENT PAGE, LAST PAGE
	document.getElementById('currentPage').innerText 	= currentPageNum;
	document.getElementById('lastPage').innerText 		= totalNumberOfPages;
	
	// POSTS
	for(let i=0;i<posts.length;i++) {
		
	 	let postElement = document.createElement('div');
	    postElement.className = 'post';
	    
	    let postSeqElement = document.createElement('div');
	    postSeqElement.className = 'postSeq';
	    postSeqElement.textContent = posts[i].postSeq;
	    
	    let postPageNumberElement = document.createElement('div');
	    postPageNumberElement.className = 'postPageNumber';
	    postPageNumberElement.innerText = currentPageNum;
	    
	    let postInfoElement = document.createElement('div');
	    postInfoElement.className = 'postInfo';
	    
	    let postInfoLine1Element = document.createElement('div');
	    postInfoLine1Element.className = 'postInfoLine1';
	    
	    let postTitleImgElement = document.createElement('img');
	    postTitleImgElement.className = 'postTitleImg';
	    postTitleImgElement.src = parseInt(posts[i].howmanyImages)>0 ?'/img/mini/image_mini.png' : '/img/mini/text_mini.png';
	    
	    let postTitleElement = document.createElement('div');
	    postTitleElement.className = 'postTitle';
	    postTitleElement.textContent = posts[i].postTitle;
	    
	    postInfoLine1Element.appendChild(postTitleImgElement);
	    postInfoLine1Element.appendChild(postTitleElement);
	    
	    let postInfoLine2Element = document.createElement('div');
	    postInfoLine2Element.className = 'postInfoLine2';
	    
	    let postWriterElement = document.createElement('div');
	    postWriterElement.className = 'postWriter';
	    postWriterElement.textContent = posts[i].username;
	    
	    let postViewsElement = document.createElement('div');
	    postViewsElement.className = 'postViews';
	    postViewsElement.textContent = posts[i].postViews;
	    
	    let postLikesElement = document.createElement('div');
	    postLikesElement.className = 'postLikes';
	    postLikesElement.textContent = posts[i].likes;
	    
	    let postCreationTimeElement = document.createElement('div');
	    postCreationTimeElement.className = 'postCreationTime';
	    postCreationTimeElement.textContent = utcToLocal( posts[i].postCreationTime );
	    
	    postInfoLine2Element.appendChild(postWriterElement);
	    postInfoLine2Element.appendChild(postViewsElement);
	    postInfoLine2Element.appendChild(postLikesElement);
	    postInfoLine2Element.appendChild(postCreationTimeElement);
	    
	    postInfoElement.appendChild(postInfoLine1Element);
	    postInfoElement.appendChild(postInfoLine2Element);
	    
	    let postCommentsElement = document.createElement('div');
	    postCommentsElement.className = 'postHowManyComments';
	    postCommentsElement.textContent = posts[i].comments;
	    
	    postElement.appendChild(postSeqElement);
	    postElement.appendChild(postPageNumberElement);
	    postElement.appendChild(postInfoElement);
	    postElement.appendChild(postCommentsElement);
	    
	    let postSeq = parseInt(posts[i].postSeq);
	    
	    if(smallestPostSeq === null) 
	    {
			document.getElementById('posts').appendChild(postElement);
		} 
		else if(smallestPostSeq > postSeq) 
		{
			document.getElementById('posts').appendChild(postElement);
		}

	}
	
	// COMMENTS
	for(let i=0;i<comments.length;i++) {
		
		let commentElement = document.createElement('div');
		commentElement.className = 'comment';
		
		let commentSeqElement = document.createElement('div');
		commentSeqElement.className = 'commentSeq';
		commentSeqElement.textContent = comments[i].commentSeq;
		
		let postSeqElement = document.createElement('div');
		postSeqElement.className = 'postSeq';
		postSeqElement.textContent = comments[i].postSeq;
		
	    let postPageNumberElement = document.createElement('div');
	    postPageNumberElement.className = 'postPageNumber';
	    postPageNumberElement.innerText = currentPageNum;
		
		let commentInfoElement = document.createElement('div');
		commentInfoElement.className = 'commentInfo';
		
		let commentInfoLine1Element = document.createElement('div');
		commentInfoLine1Element.className = 'commentInfoLine1';
		
	    let postTitleImgElement = document.createElement('img');
	    postTitleImgElement.className = 'postTitleImg';
	    postTitleImgElement.src = parseInt(comments[i].postType) === 1 ? '/img/mini/emplifier_mini.png' : (
	    	parseInt(comments[i].howmanyImages) > 0 ?'/img/mini/image_mini.png' : '/img/mini/text_mini.png' );
		
		
		let postTitleElement = document.createElement('div');
		postTitleElement.className = 'postTitle';
		postTitleElement.textContent = comments[i].postTitle;
		
		commentInfoLine1Element.appendChild(postTitleImgElement);
		commentInfoLine1Element.appendChild(postTitleElement);
		
		let commentInfoLine2Element = document.createElement('div');
		commentInfoLine2Element.className = 'commentInfoLine2';
		
		let commentImgElement = document.createElement('img');
		commentImgElement.className = 'commentImg';
		commentImgElement.src = "/img/mini/comment_mini.png";
		
		let commentContentElement = document.createElement('div');
		commentContentElement.className = 'commentContent';
		commentContentElement.textContent = comments[i].commentContent;
		
		commentInfoLine2Element.appendChild(commentImgElement);
		commentInfoLine2Element.appendChild(commentContentElement);
		
		let commentInfoLine3Element = document.createElement('div');
		commentInfoLine3Element.className = 'commentInfoLine3';
		
		let commentWriterElement = document.createElement('div');
		commentWriterElement.className = 'commentWriter';
		commentWriterElement.textContent = comments[i].username;
		
		let commentCreationTimeElement = document.createElement('postTitle');
		commentCreationTimeElement.className = 'commentCreationTime';
		commentCreationTimeElement.textContent = utcToLocal( comments[i].commentCreationTime );
		
		commentInfoLine3Element.appendChild(commentWriterElement);
		commentInfoLine3Element.appendChild(commentCreationTimeElement);
		
		commentInfoElement.appendChild(commentInfoLine1Element);
		commentInfoElement.appendChild(commentInfoLine2Element);
		commentInfoElement.appendChild(commentInfoLine3Element);
		
		commentElement.appendChild(commentSeqElement);
		commentElement.appendChild(postSeqElement);
		commentElement.appendChild(postPageNumberElement);
		commentElement.appendChild(commentInfoElement);
		
		let commentSeq = parseInt(comments[i].commentSeq);
		
		if(smallestCommentSeq === null) 
	    {
			document.getElementById('posts').appendChild(commentElement);
		} 
		else if(smallestCommentSeq > commentSeq) 
		{
			document.getElementById('posts').appendChild(commentElement);
		}
		
	}
	
	// PAGES AROUND
	let pagesAroundStr = "";
	for(let i=0;i<pagesAround.length;i++) {
		pagesAroundStr += `<div class="${pagesAround[i]===currentPageNum ? 'pageCurrent' : 'pageAround'}">${pagesAround[i]}</div>`;
	};
	document.getElementById('pagesAround').innerHTML = pagesAroundStr;
	
	
	// ARROWS
	document.getElementById('pagesAroundLeftArrow').style.opacity 	= currentPageNum<=1 ? '0.2':'1';
	document.getElementById('pagesAroundRightArrow').style.opacity	= currentPageNum>=totalNumberOfPages ? '0.2':'1';
	if(pagesAround.length === 0 && currentPageNum === 0) {
		document.getElementById('pagesAroundLeftArrow').style.display = 'none';
		document.getElementById('pagesAroundRightArrow').style.display = 'none';
	} else {
		document.getElementById('pagesAroundLeftArrow').style.display = 'block';
		document.getElementById('pagesAroundRightArrow').style.display = 'block';
	}
	
	// MORE BUTTON
	document.getElementById('moreBtn').style.display = currentPageNum>=totalNumberOfPages ? 'none':'flex';
	
}

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
	move();
	
}



function readPost(postSeq, searchedCommentSeq){
	
	let urlParams = new URLSearchParams();
	urlParams.set('postSeq',postSeq);
	
	if((currentSearchType === '1' || currentSearchType === '3') && !!currentSearchQuery ) {	
		urlParams.set('contentSearchQuery', currentSearchQuery);	
	} 
	
	if(arguments.length === 2) {
		urlParams.set('searchedCommentSeq',searchedCommentSeq);
	}

	let url = `/discussionBoard/read?${urlParams.toString()}`;
	
	window.location.href = url;
};

