// Types
interface Answer {
    questionId: string;
    selectedAnswer: string;
}

interface AnswerResult {
    questionId: string;
    correct: boolean;
    correctAnswer: string;
}

document.addEventListener('DOMContentLoaded', function () {
    const triviaForm = document.getElementById('trivia-form') as HTMLFormElement;
    const errorMessage = document.getElementById('error-message') as HTMLDivElement;
    const submitButton = document.getElementById('submit-button') as HTMLButtonElement;

    if (!triviaForm || !errorMessage || !submitButton) {
        console.error('Required elements not found in the DOM');
        return;
    }

    triviaForm.addEventListener('submit', function (e: Event) {
        e.preventDefault();

        // Collect all the questions
        const questions = document.querySelectorAll('.question-id');
        const answers: Answer[] = [];
        let allAnswered = true;

        questions.forEach(questionElement => {
            const questionId = questionElement.getAttribute('value') ?? '';
            const selectedOption = document.querySelector(`input[name="question-${questionId}"]:checked`) as HTMLInputElement;

            if (selectedOption) {
                answers.push({
                    questionId: questionId,
                    selectedAnswer: selectedOption.value
                });
            } else {
                allAnswered = false;
            }
        });

        if (!allAnswered) {
            errorMessage.classList.remove('d-none');
            return;
        }

        errorMessage.classList.add('d-none');
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Submitting...';

        // Submit the answers to the server
        fetch('/checkanswers', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(answers)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then((results: AnswerResult[]) => {
                // Store results in sessionStorage for the results page
                sessionStorage.setItem('triviaResults', JSON.stringify(results));
                window.location.href = '/results';
            })
            .catch(error => {
                console.error('Error submitting answers:', error);
                errorMessage.textContent = 'There was an error submitting your answers. Please try again.';
                errorMessage.classList.remove('d-none');
                submitButton.disabled = false;
                submitButton.textContent = 'Submit Answers';
            });
    });
});