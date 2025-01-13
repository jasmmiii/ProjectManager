
function handleLogin(){

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    // const recaptchaResponse = recaptchaResponse.getResponse();
    const captcha = document.getElementById('captcha').value;

    if (!username || !password || !captcha){
        alert("請完整輸入帳號、密碼及驗證碼！");
        return;
    }

    // 用 loginFormData 包裝資料
    const loginFormData = new FormData();
    loginFormData.append('username', username);
    loginFormData.append('password', password);
    // loginFormData.append('g-recaptcha-response', recaptchaResponse);
    loginFormData.append('captcha', captcha);

    fetch("/login", {
        method: "POST",
        headers: {},
        body: loginFormData,
    })
        .then(response => response.json())
        .then(data => {
            const responseMessage = document.getElementById('responseMessage');
            if(data.success){
                responseMessage.classList.remove('alert-danger');
                window.location.href = '/homepage';
            } else{
                responseMessage.classList.remove('alert-success');
                responseMessage.classList.add('alert-danger');
                responseMessage.textContent = data.message;
                responseMessage.style.display = 'block';
            }
        })
        .catch(error => {
            console.error("Error:", error);
            const responseMessage = document.getElementById('responseMessage');
            responseMessage.classList.add('alert-danger');
            responseMessage.textContent = 'Login Failed';
            responseMessage.style.display = 'block';
        });
}

