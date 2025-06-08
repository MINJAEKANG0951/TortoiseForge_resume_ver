package forge.model;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import forge.model.discussionboard.PostCommentDTO;
import forge.model.discussionboard.PostCommentLikeDTO;
import forge.model.discussionboard.PostContentDTO;
import forge.model.discussionboard.PostDTO;
import forge.model.discussionboard.PostLikeDTO;
import forge.model.discussionboard.custom.CommentCRUDParams;
import forge.model.messages.MessageDTO;

@Mapper
public interface DAO {

	
	/** USER TABLE **/
	
	int createUser(String username, String password, String email);
	
	UserDTO getUser(String username);
	UserDTO getUserBySeq(int userSeq);
	UserDTO getUserByEmail(String userEmail);

	void updateUserPassword(String userEmail, String newPassword);
	
	
	
	
	/** POST TABLE **/
	
	
	// ANNOUNCEMENT POSTS
	List<PostDTO> getAnnouncements();
	
	
	// NORMAL POSTS
	List<PostDTO> getPosts(
			@Param("pageNum")int pageNumber, 
			@Param("pageSize") int pageSize
	);
	
	List<PostDTO> getPostsByTitle(
			@Param("pageNum")int pageNumber, 
			@Param("pageSize") int pageSize,
			@Param("searchQuery") String searchQuery
	);
	
	List<PostDTO> getPostsByContent(
			@Param("pageNum")int pageNumber, 
			@Param("pageSize") int pageSize,
			@Param("searchQuery") String searchQuery
	);
	
	List<PostDTO> getPostsByTitleAndContent(
			@Param("pageNum")int pageNumber, 
			@Param("pageSize") int pageSize,
			@Param("searchQuery") String searchQuery
	);
	
	List<PostDTO> getPostsByUsername(
			@Param("pageNum")int pageNumber, 
			@Param("pageSize") int pageSize,
			@Param("searchQuery") String searchQuery
	);
	
	List<PostCommentDTO> getCommentsByContent(
			@Param("pageNum")int pageNumber, 
			@Param("pageSize") int pageSize,
			@Param("searchQuery") String searchQuery
	);
	
	int countPosts();
	
	int countPostsByTitle(@Param("searchQuery") String searchQuery);

	int countPostsByContent(@Param("searchQuery") String searchQuery);

	int countPostsByTitleAndContent(@Param("searchQuery") String searchQuery);

	int countPostsByUsername(@Param("searchQuery") String searchQuery);

	int countCommentsByContent(@Param("searchQuery") String searchQuery);

	
	
	// POST CREATION
	void createPost(
			@Param("postType")	int postType, 
			@Param("postTitle")	String postTitle, 
			@Param("userSeq")	int userSeq
	);
	
	// POST DELETE
	void deletePost(
		@Param("postSeq") int postSeq,
		@Param("userSeq") int userSeq
	);
	
	// POST UPDATE
	void updatePost(
			@Param("postSeq")	int postSeq, 
			@Param("postType")	int postType, 
			@Param("postTitle")	String postTitle, 
			@Param("userSeq")	int userSeq
	);
	
	
	void createPostContent(
			@Param("postSeq")		int postSeq, 
			@Param("contentType")	int contentType, 
			@Param("value")			String value
	);
	void deletePostContents(
			@Param("postSeq")	int postSeq
	);
	PostDTO getTheLastPost(@Param("userSeq") int userSeq);

	
	// POST READ
	void increasePostViews(@Param("postSeq") int postSeq);
	
	PostDTO getPost(@Param("postSeq") int postSeq);
	
	List<PostContentDTO> getPostContents(@Param("postSeq") int postSeq);
	
	List<PostLikeDTO> getPostLikes(@Param("postSeq") int postSeq);
	
	List<PostCommentDTO> getPostComments(@Param("postSeq") int postSeq);
	List<PostCommentDTO> getPostCommentsWhenUserLoggined(@Param("postSeq") int postSeq, @Param("userSeq") int userSeq);
	
	PostLikeDTO getPostLike(@Param("postSeq") int postSeq, @Param("userSeq") int userSeq);
	
	void doPostLike(@Param("postSeq") int postSeq, @Param("userSeq") int userSeq);
	
	void cancelPostLike(@Param("postSeq") int postSeq, @Param("userSeq") int userSeq);
	
	PostCommentDTO getComment(@Param("commentSeq") int commentSeq);
	void addComment(@Param("params") CommentCRUDParams params);
	void updateComment(@Param("params") CommentCRUDParams params);
	void deleteComment(@Param("params") CommentCRUDParams params);
	
	
	// precedence for parentComment deletion
	void deleteChildPostComments(
		@Param("parentCommentSeq") int parentCommentSeq
	);
	
	// precedence for childComment deletion
	void updateTargetingCommentContent (
		@Param("targetedCommentSeq") int targetedCommentSeq,
		@Param("contentPrefix") String contentPrefix
	);
	
	void deletePostComment(
		@Param("commentSeq") int commentSeq
	);
	
	
	PostCommentLikeDTO getCommentLike(@Param("commentSeq") int commentSeq, @Param("userSeq") int userSeq);
	
	
	void doCommentLike(@Param("commentSeq") int commentSeq, @Param("userSeq") int userSeq);
	
	void cancelCommentLike(@Param("commentSeq") int commentSeq, @Param("userSeq") int userSeq);
	
	int countCommentLikes(@Param("commentSeq") int commentSeq); 
	
	
	
	
	
	
	
	
	/**	PROFILE	**/
	
	void updateAboutMe(@Param("userSeq") int userSeq, @Param("aboutMe") String userAboutMe);
	void updateProfile(
			@Param("userSeq") int userseq,
			@Param("userImg") String userImg,
			@Param("aboutMe") String userAboutMe
	);
	
	
	
	
	/** ACCOUNT SETTING **/
	
	void updatePassword(@Param("userSeq") int userSeq, @Param("userPassword") String userPassword);
	void updateUserStatus(@Param("userSeq") int userSeq, @Param("statusSeq") int statusSeq);
	
	void anonymizeAccounts(@Param("statusSeqToBe") int statusSeqToBe, @Param("statusSeq") int statusSeq, @Param("days") int days);
	List<UserDTO> getAccountsToDelete(@Param("statusSeq") int statusSeq, @Param("days") int days);
	List<PostContentDTO> getImagePostContentsToDelete(@Param("userSeq") int userSeq);
	void deleteAccount(@Param("userSeq") int userSeq);
	
	
	
	
	
	/** MESSAGES **/
	
	void sendMessage(
		@Param("senderSeq") int senderSeq,
		@Param("recipientSeq") int recipientSeq,
		@Param("message") String message
	);
	
	List<MessageDTO> getInboxMessages(
		@Param("userSeq") int userSeq,
		@Param("pageNum")int pageNumber, 
		@Param("pageSize") int pageSize
	);
	List<MessageDTO> getSentMessages(
		@Param("userSeq") int userSeq,
		@Param("pageNum")int pageNumber, 
		@Param("pageSize") int pageSize
	);
	
	int countInboxMessages(int userSeq);
	int countSentMessages(int userSeq);
	
	MessageDTO getMessage(@Param("messageSeq") int messageSeq);
	
	void deleteMessage(@Param("userSeq")int userSeq, @Param("messageSeq")int messageSeq);
	void updateMessage(
		@Param("recipientSeq")int recipientSeq, 
		@Param("messageSeq")int messageSeq, 
		@Param("read") boolean read
	);
	
	boolean checkNewMessages(@Param("userSeq")int userSeq);
	
	void purgeMessages();
	void unlinkUserFromMessages(@Param("userSeq")int userSeq);
	
	
	/** REPORT **/
	
	void report(
		@Param("reportPendingStatus") int reportPendingStatus,
		@Param("reporterSeq") int reporterSeq,
		@Param("targetSeq") int targetSeq,
		@Param("reportReason") String reportReason	
	);
	
	
	
}
