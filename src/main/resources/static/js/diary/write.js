$(document).ready(function() {

    const diaryDateInput = document.getElementById('diaryDate');
    if (diaryDateInput) {
        diaryDateInput.valueAsDate = new Date();
    }

    const fileInput = document.getElementById('diaryImage');
    const previewImg = document.getElementById('previewImg');
    const placeholder = document.getElementById('uploadPlaceholder');
    const removeBtn = document.getElementById('btnRemoveImg');
    const previewArea = document.getElementById('imagePreviewArea');

    if (fileInput) {
        fileInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    previewImg.src = e.target.result;
                    previewImg.style.display = 'block';
                    placeholder.style.display = 'none';
                    removeBtn.style.display = 'flex';
                    previewArea.style.borderStyle = 'solid';
                    previewArea.style.borderColor = '#eee';
                }
                reader.readAsDataURL(file);
            }
        });
    }

    if (removeBtn) {
        removeBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            fileInput.value = '';
            previewImg.src = '';
            previewImg.style.display = 'none';
            placeholder.style.display = 'flex';
            removeBtn.style.display = 'none';
            previewArea.style.borderStyle = 'dashed';
            previewArea.style.borderColor = '#ddd';
        });
    }

    const form = document.querySelector('.write-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            const title = document.getElementById('diaryTitle').value.trim();
            const content = document.getElementById('diaryContent').value.trim();

            if (!title || !content) {
                e.preventDefault();
                alert('제목과 내용을 모두 입력해주세요.');
            }
        });
    }
});