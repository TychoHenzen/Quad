// Types
interface AnswerResult {
    questionId: string;
    correct: boolean;
    correctAnswer: string;
}

document.addEventListener('DOMContentLoaded', function () {
    // Get results from sessionStorage
    const resultsJson = sessionStorage.getItem('triviaResults');
    const scoreSummary = document.getElementById('score-summary') as HTMLDivElement;
    const resultDetails = document.getElementById('result-details') as HTMLDivElement;

    if (!scoreSummary || !resultDetails) {
        console.error('Required elements not found in the DOM');
        return;
    }

    if (!resultsJson) {
        scoreSummary.innerHTML =
            '<div class="alert alert-warning">No results found. Please play the game first.</div>';
        return;
    }

    const results: AnswerResult[] = JSON.parse(resultsJson);

    // Calculate score
    const correctAnswers = results.filter(result => result.correct).length;
    const totalQuestions = results.length;
    const scorePercentage = Math.round(correctAnswers / totalQuestions * 100);

    // Update summary
    scoreSummary.innerHTML = `
        <span class="score-display">${correctAnswers}/${totalQuestions}</span>
        <p class="fs-5">You scored ${scorePercentage}%</p>
    `;

    // Apply results badge class based on performance
    let badgeClass = 'bg-danger';
    if (scorePercentage >= 80) {
        badgeClass = 'bg-success';
    } else if (scorePercentage >= 50) {
        badgeClass = 'bg-warning';
    }

    scoreSummary.classList.add('border', `border-${badgeClass.replace('bg-', '')}`);

    // Display each result
    results.forEach((result, index) => {
        const resultDiv = document.createElement('div');
        resultDiv.className = `card mb-3 ${result.correct ? 'border-success' : 'border-danger'}`;

        const resultStatus = document.createElement('div');
        resultStatus.className = `card-header ${result.correct ? 'bg-success' : 'bg-danger'} text-white`;

        resultStatus.innerHTML = `
            <span class="${result.correct ? 'correct-icon' : 'incorrect-icon'}">
                ${result.correct ? '✓' : '✗'}
            </span>
            ${result.correct ? 'Correct!' : 'Incorrect'}
        `;

        const answerInfo = document.createElement('div');
        answerInfo.className = 'card-body';

        // Create a temporary div to decode HTML entities
        const decoder = document.createElement('div');
        decoder.innerHTML = result.correctAnswer;
        const decodedAnswer = decoder.textContent ?? decoder.innerText;

        answerInfo.innerHTML = `
            <p class="card-title fw-bold">Question ${index + 1}</p>
            <p class="card-text"><strong>Correct Answer:</strong> ${decodedAnswer}</p>
        `;

        resultDiv.appendChild(resultStatus);
        resultDiv.appendChild(answerInfo);

        resultDetails.appendChild(resultDiv);
    });
});