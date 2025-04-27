document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get('userId');

    if (!userId) {
        alert("유저 정보가 없습니다.");
        window.close();
        return;
    }

    fetch(`/userpopup/${userId}`)
        .then(response => response.json())
        .then(data => {
            // 프로필 이미지
            const profileImg = document.getElementById('profileImg');
            if (data.profileImg) {
                profileImg.src = `/images/userimage/${data.profileImg}`;
            } else {
                profileImg.src = '/images/userimage/default-profile.png';
            }

            document.getElementById('nickName').textContent = data.nickName;
            document.getElementById('introduce').textContent = data.introduce || "소개글이 없습니다.";
            document.getElementById('createDate').textContent = `가입일: ${formatDate(data.createDate)}`;

            // 아코디언으로 각각 작성
            makeAccordionSection('작성한 글', data.boards, 'boardList', 'board');
            makeAccordionSection('작성한 댓글', data.comments, 'commentList', 'comment');
            makeAccordionSection('좋아요한 글', data.likes, 'likeList', 'like');
        })
        .catch(error => {
            console.error('에러 발생:', error);
            alert("유저 정보를 불러오지 못했습니다.");
            window.close();
        });
});

function makeAccordionSection(title, items, containerId, type) {
    const container = document.getElementById(containerId);
    
    // 제목
    const titleDiv = document.createElement('div');
    titleDiv.className = "fw-bold mb-2";
    titleDiv.style.cursor = "pointer";
    titleDiv.style.color = "#0d6efd";
    titleDiv.textContent = title;

    // 내용
    const contentDiv = document.createElement('div');
    contentDiv.style.display = "none";
    contentDiv.className = "ps-2";

    // 리스트 작성
    if (items.length > 0) {
        items.forEach(item => {
            const itemDiv = document.createElement('div');
            itemDiv.className = "mb-1";

            let text = '';
            let link = '';

            if (type === 'board') {
                text = item.title;
                link = `/board/view/${item.id}`;
            } else if (type === 'comment') {
                text = item.content;
                link = `/board/view/${item.id}`;  // 댓글의 게시글 id로 수정할 수도 있음
            } else if (type === 'like') {
                text = item.title;
                link = `/board/view/${item.boardId}`;
            }

            const linkA = document.createElement('a');
            linkA.href = "#";
            linkA.textContent = text;
            linkA.onclick = (e) => {
                e.preventDefault();
                moveTo(link);
            };
            linkA.style.color = "#212529";
            linkA.style.textDecoration = "none";

            itemDiv.appendChild(linkA);
            contentDiv.appendChild(itemDiv);
        });
    } else {
        contentDiv.innerHTML = "<p class='text-muted'>없습니다.</p>";
    }

    // 제목 클릭하면 열고 닫기
    titleDiv.onclick = () => {
        contentDiv.style.display = (contentDiv.style.display === "none") ? "block" : "none";
    };

    container.appendChild(titleDiv);
    container.appendChild(contentDiv);
}

function moveTo(link) {
    window.opener.location.href = link;
    window.close();
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
}
