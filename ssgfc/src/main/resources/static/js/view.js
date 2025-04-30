// ✅ 프로필 팝업 열기
function openUserPopup(userId) {
    window.open('/popup?userId=' + userId, '_blank', 'width=500,height=700');
}

// ✅ 답글 입력창 토글
function toggleReplyForm(commentId) {
    const form = document.getElementById('replyForm-' + commentId);
    if (form) {
        form.style.display = (form.style.display === 'none' || form.style.display === '') ? 'flex' : 'none';
    }
}

// ✅ 댓글 수정 폼 열기/닫기
function toggleEditForm(commentId) {
    const form = document.getElementById('editForm-' + commentId);
    if (form) {
        form.style.display = form.style.display === 'none' ? 'block' : 'none';
    }
}

// ✅ 버튼 데이터 넘기기 
function handleReportButton(el) {
    const type = el.getAttribute('data-type');
    const id = el.getAttribute('data-id');
    const boardId = el.getAttribute('data-board-id');
    const parentId = el.getAttribute('data-parent-id');

    openReportModal(type, id, boardId, parentId);
}

// ✅ 신고 모달 열기 및 제출 처리
function openReportModal(type, id, boardId, parentId) {
    console.log('✅ openReportModal 호출됨:', type, id, boardId, parentId);

    const reportType = document.getElementById('reportType');
    const targetId = document.getElementById('targetId');
    const reason = document.getElementById('reason');
    const reportForm = document.getElementById('reportForm');

    // 추가된 hidden input
    const boardIdInput = document.getElementById('boardId');
    const parentIdInput = document.getElementById('parentId');

    if (reportType && targetId && reason && reportForm && boardIdInput && parentIdInput) {
        // ✅ 초기값 세팅
        reportType.value = type;
        targetId.value = id;
        reason.value = '';
        boardIdInput.value = boardId || '';
        parentIdInput.value = parentId || '';

        // ✅ CSRF 토큰 추출
        const csrfToken = document.querySelector('meta[name=\"_csrf\"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name=\"_csrf_header\"]').getAttribute('content');

        // ✅ 폼 제출 이벤트 등록
        reportForm.onsubmit = function(event) {
            event.preventDefault();

            let url = '';
            if (type === 'BOARD') {
                url = '/report/board/' + id;
            } else if (type === 'COMMENT') {
                url = '/report/comment/' + id;
            }

            // ✅ fetch로 신고 요청 보내기
            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    reason: reason.value,
                    boardId: boardIdInput.value,  
                    parentId: parentIdInput.value 
                })
            })
            .then(function(response) {
                if (response.ok) {
                    alert('✅ 신고가 접수되었습니다.');
                    bootstrap.Modal.getInstance(document.getElementById('reportModal')).hide();
                } else {
                    alert('❌ 신고에 실패했습니다.');
                }
            })
            .catch(function(error) {
                console.error('❌ 신고 에러:', error);
                alert('❌ 신고 중 오류가 발생했습니다.');
            });
        };

        // ✅ 모달 열기
        const modalInstance = new bootstrap.Modal(document.getElementById('reportModal'));
        modalInstance.show();
    }
}
