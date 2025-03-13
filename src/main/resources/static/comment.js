$(document).ready(function() {

    const projectId = window.location.pathname.split("/")[2];
    const issueId = window.location.pathname.split("/")[4];

    /** 側邊欄 */
    const $sidebarToggle = $('#sidebarToggle');
    const $sidebar = $('#sidebar');
    function setupSidebarToggle(){
        $sidebarToggle.on('click', function(){
            $sidebar.toggleClass('show');
        })
        // 點其他地方收合 sidebar
        $(document).on('click', function(event){
            if (!$sidebar.is(event.target) &&
                !$sidebar.has(event.target).length &&
                !$sidebarToggle.is(event.target) &&
                !$sidebarToggle.has(event.target).length &&
                $sidebar.hasClass('show')){
                $sidebar.removeClass('show');
            }
        });
    }
    setupSidebarToggle();
    const currentUsername = $('#currentUsername').text().trim();
    const currentRole = $('#currentRole').text().trim();


    /** 取得 Issue 詳細資訊 */
    fetchIssueDetails();
    function fetchIssueDetails() {

        $.getJSON(`/api/projects/${projectId}/issues/${issueId}`, function(response) {
            if (!response) {
                console.error("Invalid issue data:", response);
                return;
            }

            $('#issue-title').text(response.title);
            $('#issue-description').text(response.description);
            $('#issue-status').text(response.status);
            $('#issue-due').text(response.due);
            $('#issue-assignee').text(response.assignee);
            $('#issue-reporter').text(response.reporter);

            // 更新留言
            $('#comment-list').empty();
            response.comments.forEach(comment => {
                $('#comment-list').append(`
                    <p><strong>${comment.author}</strong>: ${comment.content} 
                    <i>(${new Date(comment.timestamp).toLocaleString()})</i></p>
                `);
            });
        }).fail(function(error) {
            console.error('Fetching issue details error:', error);
        });
    }


    /** 新增留言 */
    $('#add-comment-btn').click(function() {
        const content = $('#comment-content').val().trim();
        if (!content) {
            alert("Comment cannot be empty!");
            return;
        }

        $.ajax({
            url: `/api/projects/${projectId}/issues/${issueId}/comments`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                content: content,
                author: currentUsername
            }),
            success: function(comment) {
                $('#comment-list').append(`
                    <p><strong>${comment.author}</strong>: ${comment.content} 
                    <i>(${new Date(comment.timestamp).toLocaleString()})</i></p>
                `);
                $('#comment-content').val(""); // 清空輸入框
            },
            error: function(error) {
                console.error('Adding comment error:', error);
            }
        });
    });
});
