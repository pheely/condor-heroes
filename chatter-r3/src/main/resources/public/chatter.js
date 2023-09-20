const apiUrl = 'https://localhost:4567';
function createSpace(name, owner) {
    let data = { name: name, owner: owner };
    // extract the csrfToken from the cookie
    let csrfToken = getCookie('csrfToken');

    // Use the Fetch API to call the Catter API endpoint.
    // Pass the request data as JSON with the correct Content-Type.
    fetch(apiUrl + '/spaces', {
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(data),
        headers: {
            'Content-Type': 'application/json',
            // include the CSRF token in the headers
            'X-CSRF-Token': csrfToken
        }
    })
    // Parse the response JSON or throw an error if unsuccessful.
    .then(response => {
        if (response.ok) {
            return response.json();
        } else if (response.status === 401) {
            // if receives a 401 status code, redirect to the login page
            window.location.replace('/login.html');
        } else {
            throw Error(response.statusText);
        }
    })
    .then(json => console.log('Created space: ', json.name, json.uri))
    .catch(error => console.error('Error: ', error));
}

function getCookie(cookieName) {
    var cookieValue = document.cookie.split(';')
        .map(item=> item.split('=').map(x=>decodeURIComponent(x.trim())))
        .filter(item=>item[0] === cookieName)[0]
    if (cookieValue) {
        return cookieValue[1];
    }
}

window.addEventListener('load', function (e) {
    document.getElementById('createSpace')
        .addEventListener('submit', processFromSubmit);
})

function processFromSubmit(e) {
    // suppress the default form behavior
    e.preventDefault();

    let spaceName = document.getElementById('spaceName').value;
    let owner = document.getElementById('owner').value;

    // call our API function with values from the form
    createSpace(spaceName, owner);

    return false;
}
    