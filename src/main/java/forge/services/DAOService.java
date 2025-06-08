package forge.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import forge.consts.Consts_DiscussionBoard;
import forge.consts.Consts_General;
import forge.controller.Controller_DiscussionBoard;
import forge.controller.Controller_Messages;
import forge.model.DAO;
import forge.model.UserDTO;
import forge.model.discussionboard.PostCommentDTO;
import forge.model.discussionboard.PostCommentLikeDTO;
import forge.model.discussionboard.PostContentDTO;
import forge.model.discussionboard.PostDTO;
import forge.model.discussionboard.PostLikeDTO;
import forge.model.discussionboard.custom.BoardSearchResult;
import forge.model.discussionboard.custom.CommentCRUDParams;
import forge.model.discussionboard.custom.CommentCRUDResult;
import forge.model.discussionboard.custom.CommentLikeResult;
import forge.model.discussionboard.custom.PostDeleteResult;
import forge.model.discussionboard.custom.PostLikeResult;
import forge.model.discussionboard.custom.PostReadResult;
import forge.model.discussionboard.custom.PostUpdateResult;
import forge.model.messages.MessageDTO;
import forge.model.messages.custom.MessageSearchResult;
import forge.model.profile.ProfileUpdateResult;


@Service
public class DAOService {
	
	private final DAO dao;
	
	public DAOService(DAO dao) {
		this.dao = dao;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public BoardSearchResult searchPosts 
	(
		int pageNumber,
		int pageSize,
		String searchType,
		String searchQuery
	) 
	{
		
		BoardSearchResult boardSearchResult = new BoardSearchResult();
		
		
		// ANNOUNCEMENT POSTS
		boardSearchResult.setAnnouncements(getAnnouncements());
		
		
		// NORMAL POSTS
		
		if (
			pageSize != Controller_DiscussionBoard.PAGE_SIZE_TYPE1 &&
			pageSize != Controller_DiscussionBoard.PAGE_SIZE_TYPE2 &&
			pageSize != Controller_DiscussionBoard.PAGE_SIZE_TYPE3 &&
			pageSize != Controller_DiscussionBoard.PAGE_SIZE_TYPE4 &&
			pageSize != Controller_DiscussionBoard.PAGE_SIZE_TYPE5
		)
		{
			pageSize = Controller_DiscussionBoard.PAGE_SIZE_TYPE1;		// to avoid division by zero ( 총 페이지 수 구할 때 )
		}
		
		boardSearchResult.setCurrentPageSize(pageSize);
		boardSearchResult.setSearchQuery(searchQuery);
	
		if(searchQuery == null || searchQuery.length() == 0)
		{
			boardSearchResult.setTotalNumberOfItems( countPosts() );
			boardSearchResult.setSearchType(null);
		}
		else if(searchType == null)
		{
			boardSearchResult.setTotalNumberOfItems( countPostsByTitleAndContent(searchQuery) );
			boardSearchResult.setSearchType(Controller_DiscussionBoard.SEARCH_TYPE_TITLE_AND_CONTENT);
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_TITLE_AND_CONTENT))
		{
			boardSearchResult.setTotalNumberOfItems( countPostsByTitleAndContent(searchQuery) );
			boardSearchResult.setSearchType(Controller_DiscussionBoard.SEARCH_TYPE_TITLE_AND_CONTENT);
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_TITLE))
		{
			boardSearchResult.setTotalNumberOfItems( countPostsByTitle(searchQuery) );
			boardSearchResult.setSearchType(Controller_DiscussionBoard.SEARCH_TYPE_TITLE);
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_CONTENT))
		{
			boardSearchResult.setTotalNumberOfItems( countPostsByContent(searchQuery) );
			boardSearchResult.setSearchType(Controller_DiscussionBoard.SEARCH_TYPE_CONTENT);
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_USER))
		{
			boardSearchResult.setTotalNumberOfItems( countPostsByUsername(searchQuery) );
			boardSearchResult.setSearchType(Controller_DiscussionBoard.SEARCH_TYPE_USER);
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_COMMENT))
		{
			boardSearchResult.setTotalNumberOfItems( countCommentsByContent(searchQuery) );
			boardSearchResult.setSearchType(Controller_DiscussionBoard.SEARCH_TYPE_COMMENT);
		}
		else
		{
			boardSearchResult.setTotalNumberOfItems( countPostsByTitleAndContent(searchQuery) );
			boardSearchResult.setSearchType(Controller_DiscussionBoard.SEARCH_TYPE_TITLE_AND_CONTENT);
		}
		
		// get lastPage
		int lastPage = boardSearchResult.getTotalNumberOfItems() / boardSearchResult.getCurrentPageSize();
		if((boardSearchResult.getTotalNumberOfItems() % boardSearchResult.getCurrentPageSize()) > 0 ) {
			lastPage = lastPage + 1;
		}
		
		boardSearchResult.setTotalNumberOfPages(lastPage);
		
		if(pageNumber < 1) {
			pageNumber = 1;
		}
		
		if(pageNumber >= lastPage) {
			pageNumber = lastPage;
		} 
		
		boardSearchResult.setCurrentPageNum(pageNumber);
		
		if(searchQuery == null || searchQuery.length() == 0)
		{
			boardSearchResult.setPosts( getPosts(pageNumber, pageSize) );
		}
		else if(searchType == null)
		{
			boardSearchResult.setPosts( getPostsByTitleAndContent(pageNumber, pageSize, searchQuery) );
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_TITLE_AND_CONTENT))
		{
			boardSearchResult.setPosts( getPostsByTitleAndContent(pageNumber, pageSize, searchQuery) );
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_TITLE))
		{
			boardSearchResult.setPosts( getPostsByTitle(pageNumber, pageSize, searchQuery) );
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_CONTENT))
		{
			boardSearchResult.setPosts( getPostsByContent(pageNumber, pageSize, searchQuery) );
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_USER))
		{
			boardSearchResult.setPosts( getPostsByUsername(pageNumber, pageSize, searchQuery) );
		}
		else if(searchType.equals(Controller_DiscussionBoard.SEARCH_TYPE_COMMENT))
		{
			boardSearchResult.setComments( getCommentsByContent(pageNumber, pageSize, searchQuery) );
		}
		else
		{
			boardSearchResult.setPosts( getPostsByTitleAndContent(pageNumber, pageSize, searchQuery) );
		}
		
		
		// PAGES AROUND
		
		lastPage = boardSearchResult.getTotalNumberOfPages();
		ArrayList<Integer> pagesAround = new ArrayList<>();

		int howManyPagesAround 	= Controller_DiscussionBoard.HOW_MANY_PAGES_AROUND;
		int currentPage 		= boardSearchResult.getCurrentPageNum();			// bigger than 0 always unless there is no post at all.
		int value				= currentPage/howManyPagesAround;

		if(currentPage % howManyPagesAround == 0) {
			value = value-1;
		}

		int pagesAroundStart	= value*howManyPagesAround + 1;
		int pagesAroundEnd		= pagesAroundStart + howManyPagesAround - 1;


		for(int i=pagesAroundStart; i<=pagesAroundEnd; i++) {
			if(1 <= i && i <= lastPage) {
				pagesAround.add(i);
			}
		}

		boardSearchResult.setPagesAround(pagesAround);
		return boardSearchResult;
		
		
		
		/**
		
			정리
			
			1. 정해진 pageSize 가 입력되지 않음 -> 정해진 pageSize 중 가장 작은 20으로 reset
			
			2. searchQuery 가 없음		 -> searchType이 없는게 당연한거.
										 -> searchType이 있어도 null 로 reset. 
			
			3. searchQuery 가 있음		 -> searchType이 있는게 당연한거.
										 -> searchType이 없어도 Title & PostContent 로 Reset(검색으로 간주)
										 -> searchType이 있는데 정해진 Type이 아니어도 Title & PostContent 
										 	로 Reset(검색으로 간주)
		
			4. pageNumber 가 0 이하로 입력됨  -> 일단 1로 변경. 0번 페이지를 보여달라는건 애초에 비정상적.
										 -> 입력된 조건으로 검색 시 실제로 총 몇페이지가 나오는지 계산 
										 -> 포스트가 하나도 없으면 0, 있으면 0보다 큰 수가 반환됨
										 -> 만약 입력된 pageNumber 가 총 페이지 수(마지막 페이지)보다 크면,
										 	pageNumber 를 lastPage 와 같도록 reset
										 	마지막 페이지보다 큰 페이지를 보여준다는건 말이 안됨
										 -> 이 과정에서 만약 검색된 포스트가 없어 총 페이지가 0이면,
										    맨 처음에 1로 변경되었던 pageNumber 도 다시 0으로 reset 됨
										    pageNumber >= lastPage 일 경우 pageNumber = lastPage 
										    로 reset 되니까.
		
		**/
		
	}
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public int createPost 
	(
		int postType, 
		String postTitle, 
		ArrayList<PostContentDTO> postContents,
		int userSeq 
	) 
	{
		
		// INSERT POST
		createPost(postType, postTitle, userSeq);
		int createdPostSeq = getTheLastPost(userSeq).getPostSeq();
		
		// INSERT POST CONTENTS
		for(int i=0;i<postContents.size();i++) {
			PostContentDTO postContent = postContents.get(i);
			createPostContent(createdPostSeq, postContent.getTypeSeq(), postContent.getContentValue());
		}
		
		return createdPostSeq;
	}
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public PostUpdateResult updatePost 
	(
		int postSeq,
		int postType, 
		String postTitle, 
		ArrayList<PostContentDTO> newPostContents,
		int userSeq 
	) 
	{
		PostUpdateResult postUpdateResult = new PostUpdateResult();
		
		PostDTO post = getPost(postSeq);
		
		if(post == null) {
			postUpdateResult.setResult(Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_FOUND);
			return postUpdateResult;
		};
		
		if(post.getUserSeq() != userSeq) {
			postUpdateResult.setResult(Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_AUTHORIZED);
			return postUpdateResult;
		}
		
		postUpdateResult.setFormerPostContents( getPostContents(postSeq) );
		
		deletePostContents(postSeq);
		
		updatePost(postSeq, postType, postTitle, userSeq);
		
		for(int i=0;i<newPostContents.size();i++) {
			PostContentDTO postContent = newPostContents.get(i);
			createPostContent(postSeq, postContent.getTypeSeq(), postContent.getContentValue());
		}
		
		postUpdateResult.setResult(Consts_DiscussionBoard.POST_RESULT_SUCCESSFUL);
		return postUpdateResult;
	}
	
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public PostReadResult getPostReadResult(int postSeq, Integer userSeq)
	{
		PostReadResult postReadResult = new PostReadResult();
		
		PostDTO post = getPost(postSeq);
		
		if(post == null) {
			return null;
		}
		
		postReadResult.setPost(post);
		postReadResult.setPostContents(getPostContents(postSeq));
		postReadResult.setPostLikes(getPostLikes(postSeq));
		
		List<PostCommentDTO> postComments = null;
		if(userSeq != null) {
			postReadResult.setLiked(getPostLike(postSeq, userSeq) != null);
			postComments = getPostCommentsWhenUserLoggined(postSeq, userSeq);
		} else {
			postComments = getPostComments(postSeq);
		};
		
		postReadResult.setPostComments(postComments);
		
		return postReadResult;
	}
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public PostDeleteResult getPostDeleteResult(int postSeq, int userSeq)
	{
		PostDeleteResult postDeleteResult = new PostDeleteResult();
		
		PostDTO post = getPost(postSeq);
		
		if(post == null) 
		{
			postDeleteResult.setResult(Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_FOUND);
		}
		
		else if(post.getUserSeq() != userSeq)
		{
			System.out.println(post.getUserSeq());
			System.out.println(userSeq);
			postDeleteResult.setResult(Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_AUTHORIZED);
		}
		
		else 
		{
			postDeleteResult.setPostContents(getPostContents(postSeq));
			deletePost(postSeq, userSeq);
			postDeleteResult.setResult(Consts_DiscussionBoard.POST_RESULT_SUCCESSFUL);
		}
		
		return postDeleteResult;
	}


	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public PostLikeResult postLike(int postSeq, int userSeq)
	{
		PostLikeResult postLikeResult = new PostLikeResult();
		PostLikeDTO postLiked = getPostLike(postSeq, userSeq);
		
		if(postLiked == null) {
			doPostLike(postSeq, userSeq);
		} else {
			cancelPostLike(postSeq, userSeq);
		}
		
		postLikeResult.setPostLike(getPostLike(postSeq, userSeq));
		postLikeResult.setHowmanyLiked(getPostLikes(postSeq).size());
		
		return postLikeResult;
	}
	
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public CommentLikeResult commentLike(int postSeq, int commentSeq, int userSeq)
	{
		CommentLikeResult commentLikeResult = new CommentLikeResult();
		PostCommentDTO subjectComment = getComment(commentSeq);
		if( subjectComment == null ) 
		{
			commentLikeResult.setCommentLikeResult( Consts_DiscussionBoard.COMMENT_RESULT_FAILED_COMMENT_NOT_FOUND );	
			commentLikeResult.setPostComments( getPostCommentsWhenUserLoggined(postSeq, userSeq) );
		}
		else if( subjectComment.getPostSeq() != postSeq )
		{
			commentLikeResult.setCommentLikeResult( Consts_DiscussionBoard.COMMENT_RESULT_FAILED_POST_NOT_FOUND );	
		}
		else
		{
			commentLikeResult.setCommentLikeResult( Consts_DiscussionBoard.COMMENT_RESULT_SUCCESSFUL );	
			if( getCommentLike(commentSeq, userSeq) == null ) 
			{
				doCommentLike(commentSeq, userSeq);
				commentLikeResult.setLiked(true);
			} 
			else 
			{
				cancelCommentLike(commentSeq, userSeq);
				commentLikeResult.setLiked(false);
			}
			commentLikeResult.setHowmanyLiked(countCommentLikes(commentSeq));
		}
		
		return commentLikeResult;
	}
	
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public CommentCRUDResult addComment(int postSeq, Integer subjectCommentSeq, int userSeq, String commentContent)
	{
		CommentCRUDResult commentCRUDResult = new CommentCRUDResult();
		
		CommentCRUDParams params = new CommentCRUDParams();
		params.setUserSeq(userSeq);
		params.setPostSeq(postSeq);
		params.setSubjectCommentSeq(subjectCommentSeq);
		params.setCommentContent(commentContent);
			
		addComment(params);
		commentCRUDResult.setResult(params.getResult());
		commentCRUDResult.setPostComments( getPostCommentsWhenUserLoggined(postSeq, userSeq)  );
		
		return commentCRUDResult;
	}
	
	public void updateComment(CommentCRUDParams params) {
		dao.updateComment(params);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public CommentCRUDResult updateComment(int postSeq, int commentSeq, int userSeq, String commentContent)
	{
		CommentCRUDResult commentCRUDResult = new CommentCRUDResult();
		
		CommentCRUDParams params = new CommentCRUDParams();
		params.setUserSeq(userSeq);
		params.setPostSeq(postSeq);
		params.setSubjectCommentSeq(commentSeq);
		params.setCommentContent(commentContent);
			
		updateComment(params);
		commentCRUDResult.setResult(params.getResult());
		commentCRUDResult.setPostComments( getPostCommentsWhenUserLoggined(postSeq, userSeq)  );
		
		return commentCRUDResult;
	}
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public CommentCRUDResult deleteComment(int postSeq, int commentSeq, int userSeq)
	{
		CommentCRUDResult commentCRUDResult = new CommentCRUDResult();
		
		CommentCRUDParams params = new CommentCRUDParams();
		params.setUserSeq(userSeq);
		params.setPostSeq(postSeq);
		params.setSubjectCommentSeq(commentSeq);
		
		deleteComment(params);
		commentCRUDResult.setResult(params.getResult());
		commentCRUDResult.setPostComments( getPostCommentsWhenUserLoggined(postSeq, userSeq)  );
		
		return commentCRUDResult;
	}
	
	
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public ProfileUpdateResult updateUserProfile
	(
		int userSeq, 
		String aboutMe,
		String newImgPath,
		boolean userImgUpdated
	)
	{
		ProfileUpdateResult profileUpdateResult = new ProfileUpdateResult();
		
		UserDTO user = getUserBySeq(userSeq);
		
		if(user == null) {
			profileUpdateResult.setResult(Consts_General.CRUD_NOT_FOUND);
			return profileUpdateResult;
		};
		
		
		profileUpdateResult.setFormerUserImgPath(user.getUserImg());	// STORE FORMER IMAGE PATH 
		
		
		
		if(!userImgUpdated && newImgPath == null) 			// ABOUT ME 만 변경
		{
			updateProfile(userSeq, user.getUserImg(), aboutMe);
			profileUpdateResult.setResult(Consts_General.CRUD_SUCCESSFUL);
			return profileUpdateResult;
		} 
		else if(userImgUpdated && newImgPath != null)		// ABOUT ME 	+ 	USER IMG 변경
		{
			updateProfile(userSeq, newImgPath, aboutMe);
			profileUpdateResult.setResult(Consts_General.CRUD_SUCCESSFUL);
			return profileUpdateResult;
		}
		else if(userImgUpdated && newImgPath == null)		// ABOUT ME 변경 	& 	USER IMG 삭제
		{
			updateProfile(userSeq, null, aboutMe);
			profileUpdateResult.setResult(Consts_General.CRUD_SUCCESSFUL);
			return profileUpdateResult;
		}
		else 
		{
			profileUpdateResult.setResult(Consts_General.CRUD_FAILURE);	// BAD REQUEST 로 FAIL
			return profileUpdateResult;
		}

	}
	
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public MessageSearchResult getUserMessages
	(
		int userSeq, 
		String messageType, 
		int pageNumber, 
		int pageSize
	)
	{
		MessageSearchResult messageSearchResult = new MessageSearchResult();
		
		if (pageSize != Controller_Messages.PAGE_SIZE_TYPE1)
		{
			pageSize = Controller_Messages.PAGE_SIZE_TYPE1;		// to avoid division by zero ( 총 페이지 수 구할 때 )
		}
		
		
		messageSearchResult.setCurrentPageSize(pageSize);
		
		if(messageType == null) 
		{
			messageSearchResult.setMessageType(Controller_Messages.MESSAGE_TYPE_INBOX);
			messageSearchResult.setTotalNumberOfItems(countInboxMessages(userSeq));
		}
		else if(messageType.equals(Controller_Messages.MESSAGE_TYPE_INBOX)) 
		{
			messageSearchResult.setMessageType(Controller_Messages.MESSAGE_TYPE_INBOX);
			messageSearchResult.setTotalNumberOfItems(countInboxMessages(userSeq));
		}
		else if(messageType.equals(Controller_Messages.MESSAGE_TYPE_SENT))
		{
			messageSearchResult.setMessageType(Controller_Messages.MESSAGE_TYPE_SENT);
			messageSearchResult.setTotalNumberOfItems(countSentMessages(userSeq));
		}
		else 
		{
			messageSearchResult.setMessageType(Controller_Messages.MESSAGE_TYPE_INBOX);
			messageSearchResult.setTotalNumberOfItems(countInboxMessages(userSeq));
		}
		
		
		// get lastPage
		int lastPage = messageSearchResult.getTotalNumberOfItems() / messageSearchResult.getCurrentPageSize();
		if((messageSearchResult.getTotalNumberOfItems() % messageSearchResult.getCurrentPageSize()) > 0 ) {
			lastPage = lastPage + 1;
		}
		
		messageSearchResult.setTotalNumberOfPages(lastPage);
		
		if(pageNumber < 1) {
			pageNumber = 1;
		}
		
		if(pageNumber >= lastPage) {
			pageNumber = lastPage;
		} 
		
		messageSearchResult.setCurrentPageNum(pageNumber);

		if(messageSearchResult.getMessageType().equals(Controller_Messages.MESSAGE_TYPE_INBOX))
		{
			messageSearchResult.setMessages(getInboxMessages(userSeq, pageNumber, pageSize));
		}
		else
		{
			messageSearchResult.setMessages(getSentMessages(userSeq, pageNumber, pageSize));
		}
		
		
		
		// PAGES AROUND
		
		lastPage = messageSearchResult.getTotalNumberOfPages();
		ArrayList<Integer> pagesAround = new ArrayList<>();

		int howManyPagesAround 	= Controller_Messages.HOW_MANY_PAGES_AROUND;
		int currentPage 		= messageSearchResult.getCurrentPageNum();
		int value				= currentPage/howManyPagesAround;

		if(currentPage % howManyPagesAround == 0) {
			value = value-1;
		}

		int pagesAroundStart	= value*howManyPagesAround + 1;
		int pagesAroundEnd		= pagesAroundStart + howManyPagesAround - 1;


		for(int i=pagesAroundStart; i<=pagesAroundEnd; i++) {
			if(1 <= i && i <= lastPage) {
				pagesAround.add(i);
			}
		}

		messageSearchResult.setPagesAround(pagesAround);

		return messageSearchResult;
	}

	
	@Transactional(rollbackFor = Exception.class)
	public void deleteUserMessages(List<Integer> messageSeqs, int userSeq) {
		
		for(Integer messageSeq : messageSeqs) {
			deleteMessage(userSeq, messageSeq.intValue());
		}
		
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void updateUserMessages(List<Integer> messageSeqs, int userSeq, boolean read) {
		
		for(Integer messageSeq : messageSeqs) {
			updateMessage(userSeq, messageSeq.intValue(), read);
		}
		
	}
	
	
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public int updateMessageStatus()
	{
		return 0;
	}
	

	public int createUser(String username, String password, String email) {
		return dao.createUser(username, password, email);
	}

	public UserDTO getUser(String username) {
		return dao.getUser(username);
	}

	public UserDTO getUserByEmail(String userEmail) {
		return dao.getUserByEmail(userEmail);
	}

	public void updateUserPassword(String userEmail, String newPassword) {
		dao.updateUserPassword(userEmail, newPassword);
	}

	public List<PostDTO> getAnnouncements() {
		return dao.getAnnouncements();
	}
	
	public List<PostDTO> getPosts(int pageNumber, int pageSize) {
		return dao.getPosts(pageNumber, pageSize);
	}

	public List<PostDTO> getPostsByTitle(int pageNumber, int pageSize, String searchQuery) {
		return dao.getPostsByTitle(pageNumber, pageSize, searchQuery);
	}

	public List<PostDTO> getPostsByContent(int pageNumber, int pageSize, String searchQuery) {
		return dao.getPostsByContent(pageNumber, pageSize, searchQuery);
	}

	public List<PostDTO> getPostsByTitleAndContent(int pageNumber, int pageSize, String searchQuery) {
		return dao.getPostsByTitleAndContent(pageNumber, pageSize, searchQuery);
	}

	public List<PostDTO> getPostsByUsername(int pageNumber, int pageSize, String searchQuery) {
		return dao.getPostsByUsername(pageNumber, pageSize, searchQuery);
	}

	public List<PostCommentDTO> getCommentsByContent(int pageNumber, int pageSize, String searchQuery) {
		return dao.getCommentsByContent(pageNumber, pageSize, searchQuery);
	}

	public int countPosts() {
		return dao.countPosts();
	}

	public int countPostsByTitle(String searchQuery) {
		return dao.countPostsByTitle(searchQuery);
	}

	public int countPostsByContent(String searchQuery) {
		return dao.countPostsByContent(searchQuery);
	}

	public int countPostsByTitleAndContent(String searchQuery) {
		return dao.countPostsByTitleAndContent(searchQuery);
	}

	public int countPostsByUsername(String searchQuery) {
		return dao.countPostsByUsername(searchQuery);
	}

	public int countCommentsByContent(String searchQuery) {
		return dao.countCommentsByContent(searchQuery);
	}

	public void createPost(int postType, String postTitle, int userSeq) {
		dao.createPost(postType, postTitle, userSeq);
	}
	
	public void deletePost(int postSeq, int userSeq) {
		dao.deletePost(postSeq, userSeq);
	}

	public void createPostContent(int postSeq, int contentType, String value) {
		dao.createPostContent(postSeq, contentType, value);
	}

	public PostDTO getTheLastPost(int userSeq) {
		return dao.getTheLastPost(userSeq);
	}

	public UserDTO getUserBySeq(int userSeq) {
		return dao.getUserBySeq(userSeq);
	}

	public PostDTO getPost(int postSeq) {
		return dao.getPost(postSeq);
	}

	public List<PostContentDTO> getPostContents(int postSeq) {
		return dao.getPostContents(postSeq);
	}

	public List<PostLikeDTO> getPostLikes(int postSeq) {
		return dao.getPostLikes(postSeq);
	}

	public List<PostCommentDTO> getPostComments(int postSeq) {
		return dao.getPostComments(postSeq);
	}

	public List<PostCommentDTO> getPostCommentsWhenUserLoggined(int postSeq, int userSeq) {
		return dao.getPostCommentsWhenUserLoggined(postSeq, userSeq);
	}

	public PostLikeDTO getPostLike(int postSeq, int userSeq) {
		return dao.getPostLike(postSeq, userSeq);
	}

	public void doPostLike(int postSeq, int userSeq) {
		dao.doPostLike(postSeq, userSeq);
	}

	public void cancelPostLike(int postSeq, int userSeq) {
		dao.cancelPostLike(postSeq, userSeq);
	}

	public PostCommentDTO getComment(int commentSeq) {
		return dao.getComment(commentSeq);
	}

	public void increasePostViews(int postSeq) {
		dao.increasePostViews(postSeq);
	}

	public void doCommentLike(int commentSeq, int userSeq) {
		dao.doCommentLike(commentSeq, userSeq);
	}

	public void cancelCommentLike(int commentSeq, int userSeq) {
		dao.cancelCommentLike(commentSeq, userSeq);
	}

	public PostCommentLikeDTO getCommentLike(int commentSeq, int userSeq) {
		return dao.getCommentLike(commentSeq, userSeq);
	}

	public int countCommentLikes(int commentSeq) {
		return dao.countCommentLikes(commentSeq);
	}

	public void deleteChildPostComments(int parentCommentSeq) {
		dao.deleteChildPostComments(parentCommentSeq);
	}

	public void updateTargetingCommentContent(int targetedCommentSeq, String contentPrefix) {
		dao.updateTargetingCommentContent(targetedCommentSeq, contentPrefix);
	}

	public void deletePostComment(int commentSeq) {
		dao.deletePostComment(commentSeq);
	}

	public void addComment(CommentCRUDParams params) {
		dao.addComment(params);
	}

	public void deleteComment(CommentCRUDParams params) {
		dao.deleteComment(params);
	}

	public void deletePostContents(int postSeq) {
		dao.deletePostContents(postSeq);
	}

	public void updatePost(int postSeq, int postType, String postTitle, int userSeq) {
		dao.updatePost(postSeq, postType, postTitle, userSeq);
	}

	public void updateAboutMe(int userSeq, String userAboutMe) {
		dao.updateAboutMe(userSeq, userAboutMe);
	}

	public void updateProfile(int userseq, String userImg, String userAboutMe) {
		dao.updateProfile(userseq, userImg, userAboutMe);
	}

	public void updatePassword(int userSeq, String userPassword) {
		dao.updatePassword(userSeq, userPassword);
	}

	public void updateUserStatus(int userSeq, int statusSeq) {
		dao.updateUserStatus(userSeq, statusSeq);
	}

	public void anonymizeAccounts(int statusSeqToBe, int statusSeq, int days) {
		dao.anonymizeAccounts(statusSeqToBe, statusSeq, days);
	}

	public List<UserDTO> getAccountsToDelete(int statusSeq, int days) {
		return dao.getAccountsToDelete(statusSeq, days);
	}

	public List<PostContentDTO> getImagePostContentsToDelete(int userSeq) {
		return dao.getImagePostContentsToDelete(userSeq);
	}

	public void deleteAccount(int userSeq) {
		dao.deleteAccount(userSeq);
	}

	public void sendMessage(int senderSeq, int recipientSeq, String message) {
		dao.sendMessage(senderSeq, recipientSeq, message);
	}

	public List<MessageDTO> getInboxMessages(int userSeq, int pageNumber, int pageSize) {
		return dao.getInboxMessages(userSeq, pageNumber, pageSize);
	}

	public List<MessageDTO> getSentMessages(int userSeq, int pageNumber, int pageSize) {
		return dao.getSentMessages(userSeq, pageNumber, pageSize);
	}

	public int countInboxMessages(int userSeq) {
		return dao.countInboxMessages(userSeq);
	}

	public int countSentMessages(int userSeq) {
		return dao.countSentMessages(userSeq);
	}

	public void report(int reportPendingStatus, int reporterSeq, int targetSeq, String reportReason) {
		dao.report(reportPendingStatus, reporterSeq, targetSeq, reportReason);
	}

	public MessageDTO getMessage(int messageSeq) {
		return dao.getMessage(messageSeq);
	}

	public void deleteMessage(int userSeq, int messageSeq) {
		dao.deleteMessage(userSeq, messageSeq);
	}

	public void updateMessage(int recipientSeq, int messageSeq, boolean read) {
		dao.updateMessage(recipientSeq, messageSeq, read);
	}

	public boolean checkNewMessages(int userSeq) {
		return dao.checkNewMessages(userSeq);
	}

	public void purgeMessages() {
		dao.purgeMessages();
	}

	public void unlinkUserFromMessages(int userSeq) {
		dao.unlinkUserFromMessages(userSeq);
	}






	
	
	


}


