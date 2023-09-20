const apiUrl = 'https://localhost:4567';

function login(username, password) {
    // encode the credentials for HTTP basic authentication
    let credentials = 'Basic ' + btoa(username + ':' + password);

    fetch(apiUrl + '/sessions', {
        method: 'POST',
        // this is required for CORS requests. 
        // allows the API to set cookies on the response
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': credentials
        }
    })
    .then(res => {
       // if successful, set the csrfToken cookie and
       // redirect to the chatter UI
       if (res.ok) {
         res.json().then(json => {
            document.cookie = 'csrfToken=' + json.token +
                ';Secure;SameSite=strict';
            window.location.replace('/chatter.html');
         });
       }
    })
    // otherwise log the error to the console
    .catch(error => console.error('Error logging in: ', error));
}

// set up an event listener to intercept form submit
window.addEventListener('load', function(e) {
    document.getElementById('login')
        .addEventListener('submit', processLoginSubmit);
});

function processLoginSubmit(e) {
    e.preventDefault();

    let username = document.getElementById('username').value;
    let password = document.getElementById('password').value;

    login(username, password);
    return false;
}