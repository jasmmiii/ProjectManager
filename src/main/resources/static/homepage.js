$(document).ready(function() {
    /** 登入成功提示框 */
    setTimeout(function(){
        $('#successAlert').fadeOut(1000, function(){
            $(this).remove();
        });
    }, 5000);

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
    const projectId = window.location.pathname.split("/")[2];

    const currentPath = window.location.pathname;
    if (currentPath === "/projects") {
        // 在 homepage.html 執行載入 Projects
        loadProjects();
    } else if (currentPath.startsWith("/projects/")) {
        // 在 issue.html 執行載入 Issues
        fetchIssues();
    }
    /**
     * 根據對應Project 抓取Issues 並顯示
     * */
    function fetchIssues() {
        const projectId = window.location.pathname.split("/")[2];

        $.getJSON(`/api/projects/${projectId}/issues`, function(response) {
            if (!response || !response.issues) {
                console.error("Invalid isssue:", response);
                return;
            }
            $('#project-title').text(`Issues for: ${response.projectName}`);
            $('#project-status').text(response.projectStatus);
            $('#project-createdBy').text(response.projectCreatedBy);
            $('#project-createdDate').text(new Date(response.projectCreatedDate).toLocaleString());
            $('#project-description-display').html(response.projectDescription || "empty");
            quill.root.innerHTML = response.projectDescription || "";

            const $issueTable = $('#issues-list');
            $issueTable.empty(); //清空已存在的rows
            response.issues.forEach(issue => {
                // if (issue.assignee === currentUsername) {
                    addIssueToTable(issue);
                // }
            });
        }).fail(function(error) {
            console.error('Fetching Issues error:', error);
        });
    }
    // function fetchIssues(viewAll = false) {
    //     const apiUrl = viewAll ? '/api/issues/all' : '/api/issues'; // 根據視圖選擇 API
    //     $.getJSON(apiUrl, function (issues) {
    //         const $issueTable = $('#newIssueTable tbody');
    //         $issueTable.empty(); // 清空表格
    //         issues.forEach(issue => {
    //             addIssueToTable(issue);
    //         });
    //     }).fail(function (error) {
    //         console.error('Fetching Issues error:', error);
    //     });
    // }

    /** old
     * 切換主頁Issue按鈕
     * 預設只顯示自己的 Issue
     * */
    // let isViewingAllIssues = false;
    // $('#toggleViewButton').on('click', function () {
    //     isViewingAllIssues = !isViewingAllIssues;
    //     const buttonText = isViewingAllIssues ? 'View Own Issues' : 'View All Issues';
    //     $(this).text(buttonText); // 更新按鈕文字
    //     fetchIssues(isViewingAllIssues); // 根據模式抓取對應的 Issues
    // });


    /**
     * 新增 Issue to table
     * */
    function addIssueToTable(issue) {
        const $issueTable = $('#newIssueTable tbody');
        // const $row = $('<tr>').attr('data-id', issue.id);
        const $row = $('<tr>').attr('data-id', issue.id).attr('data-project-id', issue.projectId);
        // const $titleCell = $('<td>').text(issue.title);
        const $descriptionCell = $('<td>');
        const $statusCell = $('<td>');
        const $dueCell = $('<td>').text(issue.due);
        const $assigneeCell = $('<td>').text(issue.assignee);
        const $reporterCell = $('<td>').text(issue.reporter);
        const $editCell = $('<td>');
        const $deleteCell = $('<td>');

        if (issue.attachmentName) {
            const $attachmentLink = $('<a>')
                .attr('href', `/api/issues/${issue.id}/download`)
                .addClass('attachment-link')
                // .css('color', '#4d82bc')
                .text(issue.attachmentName);
            $descriptionCell.append(issue.description, $('<br>'), $attachmentLink);
        } else {
            $descriptionCell.text(issue.description);
        }

        // 點擊title 進入Issue comment頁面
        const $titleCell = $('<td>').append(
            $('<a>')
                .attr('href', `/projects/${projectId}/issues/${issue.id}`)
                .addClass('issue-link')
                .text(issue.title)
        );
        // const $titleCell = $('<td>')
        // if(isViewingAllIssues){
        //     const $titleLink = $('<a>')
        //         .attr('href', '#')
        //         .css('text-decoration', 'underline')
        //         .text(issue.title)
        //         .on('click', function(){
        //             openIssueDetails(issue.id);
        //         })
        //     $titleCell.append($titleLink);
        // } else {
        //     $titleCell.text(issue.title);
        // }


        const $statusSelect = $('<select>').addClass('form-select issue-status').attr('data-id', issue.id);
        ['ToDo', 'In Progress', 'Done'].forEach(status => {
            const $option = $('<option>').val(status).text(status);
            $statusSelect.append($option);
        });
        $statusSelect.val(issue.status || 'ToDo');
        $statusCell.append($statusSelect);

        $statusSelect.on('change', function() {
            const issueId = $(this).data('id');
            const newStatus = $(this).val();
            updateIssueStatus(issueId, newStatus);
        });

        const $editButton = $('<button>').addClass('btn btn-warning btn-sm').text('Edit');
        $editButton.on('click', function() {
            if (currentUsername === issue.reporter || currentRole === 'admin') {
                enterEditMode(issue, $row);
            } else {
                alert('You do not have permission');
            }
        });
        $editCell.append($editButton);

        const $deleteButton = $('<button>').addClass('btn btn-warning btn-sm').text('Delete');
        $deleteButton.on('click', function() {
            if (currentUsername === issue.reporter || currentRole === 'admin') {
                deleteIssue(issue.id, $row);
                alert('Issue deleted successfully.');
            } else {
                alert('You do not have permission');
            }
        });
        $deleteCell.append($deleteButton);


        $row.append($titleCell, $descriptionCell, $statusCell, $dueCell, $assigneeCell, $reporterCell, $editCell, $deleteCell);
        $issueTable.append($row);
    }


    /**
     *  創建 issue table
     *  */
    $('#issueForm').on('submit', function(event) {
        event.preventDefault(); //阻止默認表單提交

        const projectId = window.location.pathname.split("/")[2];
        const formData = $(this).serializeArray(); // 創建表單數據
        const data = {};
        $.each(formData, function(_, field) {
            data[field.name] = field.value;
        });

        data.projectId = projectId;
        data.reporter = currentUsername;

        $.ajax({
            url: `/api/projects/${projectId}/issues`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(issue) {
                addIssueToTable(issue);
            },
            error: function(error) {
                console.error('Creating issue error:', error);
            }
        });
    });



    /** Initialize Bootstrap dropdown */
    $('.dropdown-toggle').dropdown();
    /**
     * AJAX to submit create issue form
     * */
    // 下拉選單點create issue後跳AJAX
    $('#createIssueButton').on('click', function(e) {
        e.preventDefault();
        $('#createIssueModal').modal('show');
    });
    $('#createIssueForm').on('submit', function(e) {
        e.preventDefault();

        const projectId = window.location.pathname.split("/")[2]; // ✅ 取得當前 Project ID
        // console.log("Creating issue for project:", projectId);
        const createFormData = new FormData();
        createFormData.append('title', $('#createTitle').val());
        createFormData.append('description', $('#createDescription').val());
        createFormData.append('due', $('#createDue').val());
        createFormData.append('assignee', $('#createAssignee').val());
        createFormData.append('reporter', currentUsername);
        const attachment = $('#createAttachment')[0]?.files[0];
        if (attachment) {
            createFormData.append('attachment', attachment);
        }
        $('#uploadProgress').show(); // 進度條

        $.ajax({
            type: 'POST',
            url: `/api/projects/${projectId}/issues`,
            data: createFormData,
            processData: false, // 不處理數據
            contentType: false, // 不設置默認類型
            xhr: function () {
                const xhr = new window.XMLHttpRequest();
                xhr.upload.addEventListener('progress', function (e) {
                    if (e.lengthComputable) {
                        const percentComplete = (e.loaded / e.total) * 100;
                        $('#progressBar').css('width', `${percentComplete}%`).text(`${Math.round(percentComplete)}%`);
                    }
                }, false);
                return xhr;
            },
            success: function(issue) {
                $('#createIssueModal').modal('hide');
                console.log('Issue created:', issue);
                $('#uploadProgress').hide();
                fetchIssues();
            },
            error: function(error) {
                console.error('Creating issue error:', error);
                $('#uploadProgress').hide();
            }
        });
    });

    /**
     * User update issue status
     * */
    function updateIssueStatus(issueId, newStatus) {
        $.ajax({
            url: `/api/projects/${projectId}/issues/${issueId}/status`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({ status: newStatus }),
            success: function(issue) {
                fetchIssues(); // Refresh the issue list
                console.log('Issue status updated:', issue);
            },
            error: function(error) {
                console.error('Updating issue status error:', error);
            }
        });
    }



    /**
     * Edit issue 視窗
     * */
    let removeAttachmentFlag = false;
    $('#removeAttachment').on('click', function () {
        removeAttachmentFlag = true;
        $('#currentAttachment').text('');
        $('#editAttachment').val('');
        console.log('Attachment is removed.');

    });
    function enterEditMode(issue, $row) {

        $editIssueForm.find('[name="title"]').val(issue.title);
        $editIssueForm.find('[name="description"]').val(issue.description);
        $editIssueForm.find('[name="status"]').val($row.find('td').eq(2).find('select').val());
        $editIssueForm.find('[name="due"]').val(issue.due);
        $editIssueForm.find('[name="assignee"]').val($row.find('td').eq(4).text());

        console.log(issue);
        if (issue.attachmentName) {
            $('#currentAttachment').text(issue.attachmentName).show();
            $('#removeAttachment').show();
        } else {
            $('#currentAttachment').hide();
            $('#removeAttachment').hide();
        }
        if (issue.lastEditedAt) {
            $('#submitTimeDisplay').text(`Last Edited At: ${issue.lastEditedAt}`).show();
        } else {
            $('#submitTimeDisplay').text('').hide();
        }

        editIssueId = issue.id;
        $('#editIssueModal').modal('show');
    }

    /**
     * Edit issue 的表單提交
     * */
    const $editIssueForm = $('#editIssueForm');
    let editIssueId = null;
    $editIssueForm.on('submit', function(event) {
        event.preventDefault();

        const formData = new FormData();
        // 構建 issue 的 JSON
        const issueData = {
            title: $editIssueForm.find('[name="title"]').val(),
            description: $editIssueForm.find('[name="description"]').val(),
            status: $editIssueForm.find('[name="status"]').val(),
            due: $editIssueForm.find('[name="due"]').val(),
            assignee: $editIssueForm.find('[name="assignee"]').val()
        };
        // 添加 issue JSON 到 FormData
        formData.append('issue', new Blob([JSON.stringify(issueData)], { type: 'application/json' }));

        // 新上傳附件
        const newAttachment = $('#editAttachment')[0]?.files[0];
        if (newAttachment) {
            formData.append('attachment', newAttachment);
        }
        // 刪除附件的標記
        formData.append('removeAttachment', removeAttachmentFlag);

        $.ajax({
            url: `/api/projects/${projectId}/issues/${editIssueId}`,
            type: 'PUT',
            processData: false,
            contentType: false,
            data: formData,
            success: function(updatedIssue) {
                updateIssueInTable(updatedIssue);
                const submitTime = new Date().toLocaleString();
                $('#submitTimeDisplay').text(`Last edited at: ${submitTime}`).show();

                $('#editIssueModal').modal('hide');
                $editIssueForm[0].reset();
                removeAttachmentFlag = false;
            },
            error: function(error) {
                console.error('Editing issue error:', error);
            }
        });
    });

    /**
     * 修改完更新 table
     * */
    function updateIssueInTable(updatedIssue) {
        const $rows = $('#newIssueTable tbody').find('tr');
        $rows.each(function() {
            const $row = $(this);
            if ($row.data('id') === updatedIssue.id) {
                $row.find('td').eq(0).text(updatedIssue.title);
                $row.find('td').eq(1).text(updatedIssue.description);
                $row.find('td').eq(2).find('select').val(updatedIssue.status);
                $row.find('td').eq(3).text(updatedIssue.due);
                $row.find('td').eq(4).text(updatedIssue.assignee);
                return false;
            }
        });
        fetchIssues();
    }

    /**
     * 刪除 issue（後端不刪除 只標記）
     * */
    function deleteIssue(issueId, $row) {
        if (confirm('This Issue will be deleted, are you sure?')) {
            $.ajax({
                url: `/api/projects/${projectId}/issues/${issueId}`,
                type: 'DELETE',
                success: function(response) {
                    if (response.ok) {
                        $row.remove();
                        fetchIssues(); //重載
                        alert('Issue deleted successfully.');
                    } else {
                        // alert('Failed to delete issue');
                    }
                },
                error: function(error) {
                    console.error('Deleting issue error:', error);
                }
            });
        }
    }





    /**
     * 載入 Project
     * */
    loadProjects();
    function loadProjects(filters = {}){
        $.ajax({
            url: '/api/projects',
            method: 'GET',
            data: filters,
            success: function(data){
                console.log("Fetched projects:", data); // 測試返回數據
                const projectList = $('#project-list');
                projectList.empty();

                data.forEach(project => {
                    const projectRow = $(`
                        <tr data-project-id="${project.id}">
                            <td class="project-name" style="cursor: pointer; color: #316699;">
                                ${project.name}
                            </td>
                            <td>
                                <select class="status-dropdown" data-project-id="${project.id}">
                                    <option value="TODO" ${project.status === 'TODO' ? 'selected' : ''}>TODO</option>
                                    <option value="Process" ${project.status === 'Process' ? 'selected' : ''}>Process</option>
                                    <option value="Done" ${project.status === 'Done' ? 'selected' : ''}>Done</option>
                                    <option value="Archived" ${project.status === 'Archived' ? 'selected' : ''}>Archived</option>
                                </select>
                            </td>
                            <td>${project.createdBy}</td>
                            <td>${project.createdDate}</td>
                        </tr>
                    `);
                    // console.log('Project Created By:', project.createdBy); 測試
                projectRow.find('.project-name').click(function(e){
                    e.stopPropagation(); // 確保僅此事件觸發
                    const projectId = $(this).closest('tr').data('project-id');
                    const currentUsername = $('#currentUsername').text().trim(); // 確保 `currentUsername` 可用
                    window.location.href = `/projects/${projectId}/issues`;
                });

                /** 狀態下拉匡邏輯 */
                projectRow.find('.status-dropdown').click(function(e){
                    e.stopPropagation(); // 防止冒泡到父級元素
                });
                projectRow.find('.status-dropdown').focus(function(e){
                   e.stopPropagation(); // 確保下拉選單能正確展開
                });
                projectRow.find('.status-dropdown').change(function(e){
                    e.stopPropagation(); // 確保不冒泡
                    const projectId = $(this).data('project-id');
                    const newStatus = $(this).val();
                    updateProjectStatus(projectId, newStatus);
                });

                projectList.append(projectRow);
            });

                // $('.status-dropdown').change(function(){
                //     const projectId = $(this).attr('data-project-id');
                //     const newStatus = $(this).val();
                //     updateProjectStatus(projectId, newStatus);
                // })
            },
            error: function(err){
                console.error("Failed to load project", err);
            }
        });
    }
    /** 主頁project篩選 */
    $('#search-btn').click(function(){
        const statusValue = $('#filter-status').val();
        const createdByValue = $('#filter-created-by').val();
        const filters = {
            name: $('#filter-name').val().trim() ||'',
            status: statusValue ? statusValue.trim() : null, // Set status = null
            createdBy: createdByValue && createdByValue !== "" ? createdByValue.trim() : null // 讓Select Member傳遞 NULL 而避免""
        };
        console.log("Sending filters to backend:", filters); // Debug 確認參數是否正確
        loadProjects(filters);
    })

    /** 創建Project 互動視窗 */
    $('#create-project-btn').click(function(){
        // 清空輸入框
        $('#project-name').val('');
        $('#project-status').val('To-Do'); // 設定預設值
        $('#project-created-by').val('');

        if ($('#project-created-by option').length === 1){
            // console.log('Loading members for Create Project');
            loadMembers(); // 載入下拉選單 members
        }
        $('#createProjectModal').modal('show'); // 開啟modal
    });

    // 監聽 Modal 關閉事件 確保每次開啟都為預設值
    $('#createProjectModal').on('hidden.bs.modal', function() {
        $('#project-name').val('');
        $('#project-status').val('To-Do'); // 設定預設值
        $('#project-created-by').val('');
    });

    $('#submit-project-btn').click(function(){
        const project = {
            name: $('#project-name').val(),
            status: $('#project-status').val(),
            createdBy: $('#project-created-by').val(),
            createdDate: $('#project-createdDate').val()
        };
        if (!project.name || !project.createdBy) {
            alert("Please fill in all fields!");
            return;
        }
        $.ajax({
            url: 'api/projects',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(project),
            success: function(){
                alert('Project created successfully');
                $('#createProjectModal').modal('hide');
                loadProjects(); // 刷新
            },
            error: function(error){
                console.error("Error creating project:", error);
                alert('Failed to create project');
            }
        })
    })

    /** Member下拉選單 */
    loadMembers();
    function loadMembers(){
        $.ajax({
            url: '/api/projects/members',
            method: 'GET',
            success: function (members){

                const filterSelect = $('#filter-created-by');
                const createSelect = $('#project-created-by');
                filterSelect.empty();
                createSelect.empty();
                filterSelect.append('<option value="">篩選建立者</option>');
                createSelect.append('<option value="">Select Member</option>');

                members.forEach(member => {
                    createSelect.append(`<option value="${member.username}">${member.username}</option>`);
                    filterSelect.append(`<option value="${member.username}">${member.username}</option>`);
                });
            },
            error: function(err){
                console.error("Failed to load members", err);
            }
        });
    }

    /** 更新 Project Status for Project主頁 */
    function updateProjectStatus(projectId, newStatus){
        $.ajax({
            url: `/api/projects/${projectId}/status`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({ status: newStatus }),
            success: function(response){
                console.log("Project status updated:", response);
                loadProjects(); // reload projects

                // 如果是 homepage 頁面，刷新 projects 列表
                if ($('#project-list').length) {
                    loadProjects(); // 只在 homepage 需要
                }
                // 如果是 issue 頁面，更新 project 狀態顯示
                if ($('#project-status').length) {
                    $('#project-status').text(newStatus);
                }
            },
            error: function(err){
                console.error("Failed to update project:", err);
            }
        })
    }


    // 初始化Quill
    let quill = new Quill('#project-description', {
        theme: 'snow',
        placeholder: 'Enter project description...',
        modules: {
            toolbar: [
                [{ 'header': [1, 2, false] }],
                ['bold', 'italic', 'underline'],
                [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                [{ 'align': [] }],
                ['clean']
            ]
        },
        formats:[
            'header', 'bold', 'italic', 'underline', 'list', 'bullet'
        ]
    });

    /**
     * Issue頁
     * Project 資訊方框
     * */
    $('#update-project-btn').click(function() {
        const projectId = window.location.pathname.split("/")[2];
        const updatedDescription = quill.root.innerHTML; // 取Quill 內的HTML內容
        const updatedStatus = $('#project-status').val();

        $.ajax({
            url: `/api/projects/${projectId}`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({
                status: updatedStatus,
                description: updatedDescription
            }),
            success: function(response) {
                $('#project-status').val(response.status);
                $('#project-description-display').html(response.description);
                quill.root.innerHTML = response.description || "";

                alert("Project updated successfully!");
                console.log("Project updated successfully:", response);
            },
            error: function(error) {
                console.error("Failed to update project", error);
            }
        });
    });

    /** old
     * 新增Comment
     * */
    // function openIssueDetails(issueId){
    //     $('#issueDetailsModal').data('issue-id', issueId); // 保存 Issue ID
    //
    //     $.getJSON(`/api/issues/${issueId}/comments`, function(comments){
    //         $('#issueCommentsModal #issueDescription').text(`Comments for Issue ${issueId}`);
    //         const $commentList = $('#issueCommentsModal #commentList');
    //         $commentList.empty();
    //         comments.forEach(comment =>{
    //             const $commentItem = $('<p>').text(comment.content);
    //             $commentList.append($commentItem);
    //         });
    //
    //         // $('#addCommentForm').off('submit', function(e){
    //         //     e.preventDefault();
    //         //     const commentContent = $(this).find('[name="comment"]').val();
    //         //     addComment(issueId, commentContent);
    //         // });
    //
    //         $('#issueCommentsModal').modal('show');
    //     }).fail(function(error){
    //         console.error('Error fetching comments: ', error);
    //     })
    // }
    // function addComment(issueId, content){
    //     $.ajax({
    //         url: `/api/issues/${issueId}/comments`,
    //         type: 'POST',
    //         contentType: 'application/json',
    //         data: JSON.stringify({ content }),
    //         success: function (newComment) {
    //             const $commentItem = $('<p>').text(newComment.content);
    //             $('#issueCommentsModal #commentList').append($commentItem)
    //             alert('Comment added successfully');
    //             // $('#issueCommentsModal').modal('hide');
    //         },
    //         error: function (error) {
    //             console.error('Error adding comment:', error);
    //             alert('Failed to add comment');
    //         }
    //     })
    // }
    // $('#addCommentForm').on('submit', function (e) {
    //     e.preventDefault(); // 防止默認表單提交行為
    //     const content = $(this).find('[name="comment"]').val(); // 獲取評論內容
    //     if (!content.trim()) {
    //         alert('Comment cannot be empty');
    //         return;
    //     }
    //     const issueId = $('#issueCommentsModal').data('issue-id'); // 取得當前 Issue ID
    //     addComment(issueId, content);
    // });

});

