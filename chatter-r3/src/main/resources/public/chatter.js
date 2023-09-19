const apiUrl = 'https://localhost:4567';
function createSpace(name, owner) {
    let data = { name: name, owner: owner };

    // Use the Fetch API to call the Catter API endpoint.
    // Pass the request data as JSON with the correct Content-Type.
    fetch(apiUrl + '/spaces', {
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(data),
        headers: {
            'Content-Type': 'application/json'
        }
    })
    // Parse the response JSON or throw an error if unsuccessful.
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw Error(response.statusText);
        }
    })
    .then(json => console.log('Created space: ', json.name, json.uri))
    .catch(error => console.error('Error: ', error));
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
    