let currentStep = 1;

$(document).ready(function() {
    updateStepper(1);
    updateButtons(1);
});

function nextStep(step) {
    showStep(step);
}

function prevStep(step) {
    showStep(step);
}

function showStep(step) {
    currentStep = step;

    let translateValue = (step - 1) * -100;
    $('#slideTrack').css('transform', 'translateX(' + translateValue + '%)');

    $('.form-step').removeClass('active-slide');
    $('#step' + step).addClass('active-slide');

    updateButtons(step);
    updateStepper(step);
}

function updateButtons(step) {
    $('.btn-step-group').removeClass('active');
    $('#btns' + step).addClass('active');
}

function updateStepper(step) {
    $('.stepper-item').removeClass('active');
    for (let i = 1; i <= step; i++) {
        $('#stepper' + i).addClass('active');
    }
}