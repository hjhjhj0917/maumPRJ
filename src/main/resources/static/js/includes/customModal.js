let customAlertCallback = null;
let customConfirmOkCallback = null;
let customConfirmCancelCallback = null;

$(document).ready(function() {
    $('#customAlertOk').on('click', function() {
        if (customConfirmOkCallback) customConfirmOkCallback();
        if (customAlertCallback) customAlertCallback();
        closeCustomModal();
    });

    $('#customAlertCancel').on('click', function() {
        if (customConfirmCancelCallback) customConfirmCancelCallback();
        closeCustomModal();
    });

    $('#customAlertClose').on('click', function() {
        if (customConfirmCancelCallback) customConfirmCancelCallback();
        closeCustomModal();
    });
});

// 확인 모달
function showCustomAlert(title, message, callback) {
    $('#customAlertTitle').text(title || '알림');
    $('#customAlertMessage').text(message);

    $('#customAlertCancel').hide(); // 취소 버튼 숨김
    $('#customAlertOverlay').css('display', 'flex').hide().fadeIn(200);

    customAlertCallback = callback || null;
    customConfirmOkCallback = null;
    customConfirmCancelCallback = null;
}

// 확인 취소 모달
function showCustomConfirm(title, message, onConfirm, onCancel) {
    $('#customAlertTitle').text(title || '확인');
    $('#customAlertMessage').text(message);

    $('#customAlertCancel').show(); // 취소 버튼 보임
    $('#customAlertOverlay').css('display', 'flex').hide().fadeIn(200);

    customConfirmOkCallback = onConfirm || null;
    customConfirmCancelCallback = onCancel || null;
    customAlertCallback = null;
}

function closeCustomModal() {
    $('#customAlertOverlay').fadeOut(200, function() {
        customAlertCallback = null;
        customConfirmOkCallback = null;
        customConfirmCancelCallback = null;
    });
}