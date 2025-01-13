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

    const currentUsername = $('#currentUsername').text().trim();
    const currentRole = $('#currentRole').text().trim();

    setupSidebarToggle();
    fetchIssues(); // 預設載入使用者的issues

    /**
     * 根據對應抓取 issues 並顯示
     * */
    // function fetchIssues() {
    //     $.getJSON('/api/issues', function(issues) {
    //         const $issueTable = $('#newIssueTable tbody');
    //         $issueTable.empty(); //清空已存在的rows
    //         issues.forEach(issue => {
    //             if (issue.assignee === currentUsername) {
    //                 addIssueToTable(issue);
    //             }
    //         });
    //     }).fail(function(error) {
    //         console.error('Fetching Issues error:', error);
    //     });
    // }
    function fetchIssues(viewAll = false) {
        const apiUrl = viewAll ? '/api/issues/all' : '/api/issues'; // 根據視圖選擇 API
        $.getJSON(apiUrl, function (issues) {
            const $issueTable = $('#newIssueTable tbody');
            $issueTable.empty(); // 清空表格
            issues.forEach(issue => {
                addIssueToTable(issue);
            });
        }).fail(function (error) {
            console.error('Fetching Issues error:', error);
        });
    }


    /**
     * 新增 Issue to table
     * */
    function addIssueToTable(issue) {
        const $issueTable = $('#newIssueTable tbody');
        const $row = $('<tr>').attr('data-id', issue.id);
        const $titleCell = $('<td>').text(issue.title);
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
                .text(issue.attachmentName);
            $descriptionCell.append(issue.description, $('<br>'), $attachmentLink);
        } else {
            $descriptionCell.text(issue.description);
        }


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

        const formData = $(this).serializeArray(); // 創建表單數據
        const data = {};
        $.each(formData, function(_, field) {
            data[field.name] = field.value;
        });
        data.reporter = currentUsername;

        $.ajax({
            url: '/api/issues',
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
            url: `/api/issues`,
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
            url: `/api/issues/${issueId}/status`,
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
            url: `/api/issues/${editIssueId}`,
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
     * 刪除 issue
     * */
    function deleteIssue(issueId, $row) {
        if (confirm('This Issue will be deleted, are you sure?')) {
            $.ajax({
                url: `/api/issues/${issueId}`,
                type: 'DELETE',
                success: function(response) {
                    if (response.ok) {
                        $row.remove();
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
     * 切換主頁Issue按鈕
     * 預設只顯示自己的 Issue
     * */
    let isViewingAllIssues = false;
    $('#toggleViewButton').on('click', function () {
        isViewingAllIssues = !isViewingAllIssues;
        const buttonText = isViewingAllIssues ? 'View Own Issues' : 'View All Issues';
        $(this).text(buttonText); // 更新按鈕文字
        fetchIssues(isViewingAllIssues); // 根據模式抓取對應的 Issues
    });


});

