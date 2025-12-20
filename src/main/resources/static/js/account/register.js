let currentStep = 1;

$(document).ready(function() {
    updateStepper(1);
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
    updateStepper(step);
}

function updateStepper(step) {
    $('.stepper-item').removeClass('active');
    for (let i = 1; i <= step; i++) {
        $('#stepper' + i).addClass('active');
    }
}